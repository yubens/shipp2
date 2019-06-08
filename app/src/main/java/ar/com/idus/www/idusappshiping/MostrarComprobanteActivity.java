package ar.com.idus.www.idusappshiping;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import ar.com.idus.www.idusappshiping.modelos.Articulo;
import ar.com.idus.www.idusappshiping.modelos.Comprobante;
import ar.com.idus.www.idusappshiping.modelos.ComprobanteItem;
import ar.com.idus.www.idusappshiping.modelos.Historico;
import ar.com.idus.www.idusappshiping.utilidades.Devoluciones;
import cz.msebera.android.httpclient.Header;

public class MostrarComprobanteActivity extends AppCompatActivity {

    TextView strFecha, strComprobante, strCliente, strDomicilio, strVendedor, strTotal, strSaldo, strPago, strDirFirma, strTxtSen;
    Button btnDetalle, btnHistorico, btnCancelar, btnPagar, btnFirma;
    EditText txtImporte;
    ListView listaDetalle;
    Spinner spOpciones;
    ProgressBar pbDetalle;
    ImageView imagenPago;
    ArrayList<ComprobanteItem> listaItems;
    ArrayList<Historico> listaHistorico;
    Comprobante comprobante;
    String _strUrl, root, fname, resultadoGET;
    String _codigoEmpresa, _idEmpresa, _idCliente, _idFletero, _idVendedor;
    String claseComp;
    int _caja, _planilla;
    File myDir, file;
    Boolean requiereFirma = true;
    Boolean firmo = false;
    boolean nc = false;
    double total, saldo;

    //para localizar el dispositivo
    private LocationManager locManager;
    private Location location;
    private String lat, lon, pres;
    private String versionApp, versionAndroid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_comprobante);
        strFecha = (TextView) findViewById(R.id.MCompFecha);
        strComprobante = (TextView) findViewById(R.id.MCompComprobante);
        strCliente = (TextView) findViewById(R.id.MCompNombreCliente);
        strDomicilio = (TextView) findViewById(R.id.MCompDomicilio);
        strVendedor = (TextView) findViewById(R.id.mCompNombreVendedor);
        strTotal = (TextView) findViewById(R.id.MCompTotal);
        strSaldo = (TextView) findViewById(R.id.MCompSaldo);
        txtImporte = (EditText) findViewById(R.id.MCompEdiTextImporte);
        listaDetalle = (ListView) findViewById(R.id.listaDetalle);
        spOpciones = (Spinner) findViewById(R.id.MCompSpiAccion);
        btnDetalle = (Button) findViewById(R.id.MCompBtnDetalle);
        btnHistorico = (Button) findViewById(R.id.MCompBtnPagos);
        btnPagar = (Button) findViewById(R.id.MCompBtnAceptar);
        btnCancelar = (Button) findViewById(R.id.MCompBtnCancelar);
        pbDetalle = (ProgressBar) findViewById(R.id.pgDetalleMostrarComprobante);
        imagenPago = (ImageView) findViewById(R.id.MCompImgPago);
        btnFirma = (Button) findViewById(R.id.MCompBtnFirma);
        strDirFirma = (TextView) findViewById(R.id.MCompStrFileFirma);
        strTxtSen = findViewById(R.id.strTextoSeñalador);

        versionApp = BuildConfig.VERSION_NAME;
        versionAndroid = Build.VERSION.RELEASE;

        final Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            DecimalFormat format = new DecimalFormat("#.00");
            comprobante = (Comprobante) getIntent().getSerializableExtra("comprobante");
            strFecha.setText(comprobante.getFecha());
            strComprobante.setText(comprobante.getComprobante());

            if(comprobante.getNombreCliente().length() > 15 && comprobante.getNombreCliente().length() <= 20){
                strCliente.setTextSize(15);
                strCliente.setText(comprobante.getNombreCliente());
            } else if (comprobante.getNombreCliente().length() > 20){
                strCliente.setTextSize(14);
                strCliente.setText(new StringTokenizer(comprobante.getNombreCliente(), "-").nextToken());
            }

            if(comprobante.getNombreVendedor().length() <= 25){
                strVendedor.setTextSize(14);
            } else {
                strVendedor.setTextSize(13);
            }
            strVendedor.setText(comprobante.getNombreVendedor());

            strDomicilio.setText(comprobante.getDomicilioCliente());

            total = comprobante.getTotal();
            total = Math.round(total * 100.0)/100.0;
            strTotal.setText(format.format(total));

            if (comprobante.getSaldo() <= 0.01) {
                strSaldo.setText(format.format(comprobante.getTotal()));
            } else {
                saldo = comprobante.getSaldo();
                saldo = Math.round(saldo * 100.0)/100.0;
                strSaldo.setText(format.format(saldo));
            }
            txtImporte.setText(format.format(comprobante.getSaldo()));
            _strUrl = recupera.getString("_strURL");
            _codigoEmpresa = recupera.getString("_codigoEmpresa");
            _idEmpresa = recupera.getString("_idEmpresa");
            _idCliente = comprobante.getIdCliente();
            _idFletero = comprobante.getIdFletero();
            _idVendedor = comprobante.getIdVendedor();
            _caja = comprobante.getCaja();
            _planilla = comprobante.getPlanilla();
            claseComp = comprobante.getClase();

            if(claseComp.equals("NC") || claseComp.equals("NI")){
                nc = true;
                inhabilitarOpciones();
            }

            pbDetalle.setVisibility(View.VISIBLE);

            Thread tr = new Thread() {
                @Override
                public void run() {
                    final int respuesta = enviarDiasGET(0, comprobante);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(respuesta == RESULT_OK){
                                listaItems = listarComprobanteItems(resultadoGET);
                                mostrarListaItems();
                                obtenerLocalizacion();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), resultadoGET, Toast.LENGTH_LONG).show();
                            }
                            pbDetalle.setVisibility(View.GONE);
                        }
                    });

                }
            };
            tr.start();
        }

        btnDetalle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbDetalle.setVisibility(View.VISIBLE);

                Thread td = new Thread() {
                    @Override
                    public void run() {
                        final int respuesta = enviarDiasGET(0, comprobante);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(respuesta == RESULT_OK){
                                    listaItems = listarComprobanteItems(resultadoGET);
                                    mostrarListaItems();
                                    obtenerLocalizacion();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), resultadoGET, Toast.LENGTH_LONG).show();
                                }
                                pbDetalle.setVisibility(View.GONE);
                            }
                        });

                    }
                };
                td.start();
            }
        });

        btnHistorico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbDetalle.setVisibility(View.VISIBLE);
                Thread tr = new Thread() {
                    @Override
                    public void run() {
                        final int respuesta = enviarDiasGET(1, comprobante);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(respuesta == RESULT_OK) {
                                    listaHistorico = listarHistorico(resultadoGET);
                                    mostrarListaHistorico();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), resultadoGET, Toast.LENGTH_LONG).show();
                                }
                                pbDetalle.setVisibility(View.GONE);
                            }
                        });
                    }
                };
                tr.start();
            }
        });

        btnPagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensaje = "";
                obtenerLocalizacion();
                Boolean requiereImporte = false;

                if (txtImporte.getText().toString().trim().equals("")) {
                    txtImporte.setText("0");
                }

                //final double total = Double.parseDouble(strTotal.getText().toString().replace(",", "."));
                final double importe = Double.parseDouble(txtImporte.getText().toString().replace(",", "."));

                if (importe > total) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.strTotalMenorAImporte, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    if (comprobante.getMhr() == 0) {
                        final int movimiento = spOpciones.getSelectedItemPosition();

                        switch (movimiento){
                            case 0:
                                mensaje = "¿Confirma dejar A PAGAR el comprobante: " + strComprobante.getText().toString() + " ?";

                                if(requiereFirma && !firmo) {
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.strNoEstaFirmado, Toast.LENGTH_LONG);
                                    toast.show();
                                    return;
                                }
                                break;

                            case 1:
                                mensaje = "¿Confirma el PAGO TOTAL del comprobante: " + strComprobante.getText().toString() + " por un valor de $ " + txtImporte.getText().toString() + " ?";
                                requiereImporte = true;
                                break;

                            case 2:
                                mensaje = "¿Confirma el PAGO PARCIAL Del comprobante: " + strComprobante.getText().toString() + " por un valor de $ " + txtImporte.getText().toString() + " ?";
                                requiereImporte = true;
                                break;

                            case 3:
                                mensaje = "¿Confirma REENVIAR el comprobante: " + strComprobante.getText().toString() + " ?";
                                break;

                            case 4:
                                mensaje = "¿Confirma dejar el comprobante " + strComprobante.getText().toString() + " EN ESPERA?";
                                break;

                            default:
                                break;
                        }

                        if (((requiereImporte) && (importe > 0)) || ((movimiento == 0) || (movimiento == 3) || (movimiento == 4))) {
                            AlertDialog.Builder alerta;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext(), android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
                            }
                            alerta.setMessage(mensaje)
                                    .setTitle("Advertencia!!!")
                                    .setCancelable(false)
                                    .setNegativeButton(R.string.strCancelar, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .setPositiveButton(R.string.bntAceptar, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Thread tr = new Thread() {
                                                @Override
                                                public void run() {
                                                    final int respuesta = enviarDiasPOST(3, comprobante, importe, movimiento, lat, lon, pres, versionApp, versionAndroid);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (respuesta == 200) {
                                                                Toast toast = Toast.makeText(getApplicationContext(), R.string.strMensajeInsertado, Toast.LENGTH_LONG);
                                                                toast.show();
                                                                if (movimiento != 0) {
                                                                    finish();
                                                                }
                                                            }
                                                            else{
                                                                Toast.makeText(getApplicationContext(),
                                                                        "Se produjo un error al insertar la rendición del fletero",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            };
                                            tr.start();
                                            if ((movimiento == 0) && (firmo) && (requiereFirma)) {
                                                subirFoto();
                                                finish();
                                            }
                                        }
                                    })
                                    .setNeutralButton("Firmar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(getApplicationContext(), FirmarActivity.class);
                                            i.putExtra("_comprobante", comprobante);
                                            startActivity(i);

                                        }
                                    });
                            AlertDialog alert = alerta.create();
                            alert.show();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.strImporteMayorACero, Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                    else {
                        final int movimiento = spOpciones.getSelectedItemPosition();
                        if ((movimiento == 3) || (movimiento == 4)) {
                            mensaje = "No se puede usar esta opción para comprobantes que están para cobrar";
                            Toast toast = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            if(movimiento == 0){
                                mensaje = "¿Confirma dejar A PAGAR el comprobante: " + strComprobante.getText().toString() + " ?";
                            } else {
                                mensaje = "¿Confirma el pago del comprobante: " + strComprobante.getText().toString() + " por un valor de $ " + txtImporte.getText().toString() + " ?";
                            }

                            if ((importe > 0) || (movimiento == 0)) {
                                 AlertDialog.Builder alerta;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext(), android.R.style.Theme_Material_Dialog_Alert);
                                } else {
                                    alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
                                }
                                alerta.setMessage(mensaje)
                                        .setTitle("Advertencia!!!")
                                        .setCancelable(false)
                                        .setNegativeButton(R.string.strCancelar, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setPositiveButton(R.string.bntAceptar, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Thread tr = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        final int respuesta = enviarDiasPOST(3, comprobante, importe, movimiento, lat, lon, pres, versionApp, versionAndroid);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (respuesta == 200) {
                                                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.strMensajeInsertado, Toast.LENGTH_LONG);
                                                                    toast.show();
                                                                    finish();
                                                                }
                                                                else{
                                                                    Toast.makeText(getApplicationContext(),
                                                                            "Se produjo un error al insertar la rendición del fletero",
                                                                            Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                };
                                                tr.start();
                                            }
                                        });
                                AlertDialog alert = alerta.create();
                                alert.show();
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), R.string.strImporteMayorACero, Toast.LENGTH_LONG);
                                toast.show();
                                txtImporte.setText("");
                            }
                        }
                    }
                }
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnFirma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FirmarActivity.class);
                i.putExtra("_comprobante", comprobante);
                startActivity(i);
            }
        });

        spOpciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        txtImporte.setText("");
                        txtImporte.setEnabled(false);
                        requiereFirma = true;
                        imagenPago.setImageResource(R.mipmap.button_a_pagar);
                        break;

                    case 1:
                        txtImporte.setText(strSaldo.getText().toString().replace(",", "."));
                        txtImporte.setEnabled(true);
                        requiereFirma = false;
                        imagenPago.setImageResource(R.mipmap.button_pago_total);
                        break;

                    case 2:
                        txtImporte.setText("");
                        txtImporte.setEnabled(true);
                        txtImporte.setActivated(true);
                        txtImporte.requestFocus();
                        requiereFirma = false;
                        imagenPago.setImageResource(R.mipmap.button_pago_parcial);
                        break;

                    case 3:
                        txtImporte.setText("");
                        txtImporte.setEnabled(false);
                        requiereFirma = false;
                        imagenPago.setImageResource(R.mipmap.button_reenvio);
                        break;

                    case 4:
                        txtImporte.setText("");
                        txtImporte.setEnabled(false);
                        requiereFirma = false;
                        imagenPago.setImageResource(R.mipmap.button_espera);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!nc){
            if(comprobante.getMhr() == 0){
                getMenuInflater().inflate(R.menu.opciones_devolucion_0, menu);
            } else{
                getMenuInflater().inflate(R.menu.opciones_devolucion_1, menu);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String cliente[];

        if (id == R.id.mnu01DevolucionSinComp) {

            if(_codigoEmpresa.equals("534")){
                Toast.makeText(getApplicationContext(), "La empresa actual, no permite el ingreso a esta opción",
                                Toast.LENGTH_LONG).show();
            }
            else{
                cliente = comprobante.getIdCliente().split("CLIENTE");

                Intent intent = new Intent(getApplicationContext(), DevolucionSinComprobanteActivity.class);
                intent.putExtra("_idVendedor", _idVendedor);
                intent.putExtra("_idEmpresa", _idEmpresa);
                intent.putExtra("_codCliente", cliente[1]);
                intent.putExtra("_caja", _caja);
                intent.putExtra("_planilla", _planilla);
                intent.putExtra("_idFletero", _idFletero);

                startActivity(intent);
            }

        } else if (id == R.id.mnu02DevolucionParcial){

            Intent intent = new Intent(getApplicationContext(), DevolucionParcialActivity.class);
            intent.putExtra("_idVendedor", _idVendedor);
            intent.putExtra("_idEmpresa", _idEmpresa);
            intent.putExtra("_idCliente", _idCliente);
            intent.putExtra("_idFletero", _idFletero);
            intent.putExtra("_caja", _caja);
            intent.putExtra("_planilla", _planilla);
            intent.putExtra("_lista", listaItems);
            intent.putExtra("_nombCliente", comprobante.getNombreCliente());
            intent.putExtra("_comprobante", comprobante.getComprobante());

            startActivity(intent);

        } else if (id == R.id.mnu03DevolucionTotal){
            AlertDialog.Builder alerta;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
            }

            alerta.setTitle(R.string.tituloImportamte);
            alerta.setMessage(R.string.alertaDevolTotal);
            alerta.setCancelable(false);
            alerta.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    totalReturn();
                }
            }).setNegativeButton(R.string.rechazar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }

        return super.onContextItemSelected(item);
    }

    public void inhabilitarOpciones(){
        imagenPago.setVisibility(View.INVISIBLE);
        //spOpciones.setEnabled(false);
        spOpciones.setVisibility(View.INVISIBLE);
        strTxtSen.setVisibility(View.INVISIBLE);
        btnPagar.setEnabled(false);

    }

    @Override
    protected void onResume() {
        //cos la firma
        root = Environment.getExternalStorageDirectory().toString();

        // the directory where the signature will be saved
        myDir = new File(root + "/saved_signature");

        // make the directory if it does not exist yet
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // set the file name of your choice
        fname = comprobante.getId().toString() + ".png";

        // in our case, we delete the previous file, you can remove this
        file = new File(myDir, fname);
        if (file.exists()) {
            strDirFirma.setText(file.getName());
            firmo = true;
        } else {
            strDirFirma.setText("");
            firmo = false;
        }
        super.onResume();
    }

    //muestra los items del comprobante
    private ArrayList<ComprobanteItem> listarComprobanteItems(String response) {
        ArrayList<ComprobanteItem> lista = new ArrayList<ComprobanteItem>();

        try {
            JSONArray json = new JSONArray(response);
            for (int i = 0; i < json.length(); i++) {

                ComprobanteItem item = new ComprobanteItem();
                item.setId(json.getJSONObject(i).getString("ID"));
                item.setCantidad(json.getJSONObject(i).getDouble("CANTIDAD"));
                item.setDetalle(json.getJSONObject(i).getString("DETALLE"));
                item.setCodigoArticulo(json.getJSONObject(i).getString("CODIGO"));
                item.setIdArticulo(json.getJSONObject(i).getString("ARTICULO_ID"));
                item.setItem(json.getJSONObject(i).getInt("ITEM"));
                item.setPorcentajeDescuento(json.getJSONObject(i).getDouble("PORCENTAJEDESCUENTO"));
                item.setPrecioCosto(json.getJSONObject(i).getDouble("PRECIOCOSTO"));
                item.setPrecioImpInt(json.getJSONObject(i).getDouble("IMPORTEIMPUESTOINTERNO"));
                item.setPrecioIva(json.getJSONObject(i).getDouble("IMPORTEIVA"));
                item.setPrecioIvaO(json.getJSONObject(i).getDouble("IMPORTEIVAOTRO"));
                item.setPrecioNeto(json.getJSONObject(i).getDouble("PRECIONETO"));
                item.setPrecioFinal(json.getJSONObject(i).getDouble("PRECIO_FINAL"));
                item.setTotal(item.getCantidad() * item.getPrecioFinal());
                //total += total + item.getTotal();

                lista.add(item);
            }
            return lista;
        } catch (JSONException je) {
            je.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Error al buscar los items del comprobante Json Error:" + je.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Error sin definir al cargar el detalle:" + e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
    }

    class AdaptadorComprobantesItems extends ArrayAdapter<ComprobanteItem> {
        AdaptadorComprobantesItems(List<ComprobanteItem> listaTransComprobante) {
            super(MostrarComprobanteActivity.this, R.layout.comprobante_items, listaItems);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String aux = "";
            StringTokenizer token;
            DecimalFormat format = new DecimalFormat("#.00");
            String detalle = listaItems.get(position).getDetalle();

            View item;
            item = getLayoutInflater().inflate(R.layout.comprobante_items, null);
            TextView strCodigo = (TextView) item.findViewById(R.id.lblCodigo);
            TextView strDetalle = (TextView) item.findViewById(R.id.lblDetalle);
            TextView strCantidad = (TextView) item.findViewById(R.id.lblCantidad);
            TextView strPrecioUnitario = (TextView) item.findViewById(R.id.lblPrecioUnitario);
            TextView strTotal = (TextView) item.findViewById(R.id.lblTotal);

            strCodigo.setText(listaItems.get(position).getCodigoArticulo());

            //si el detalle es amplio se reduce el tamaño de la letra y se recorta el detalle
            if (detalle.length() <= 30){
                strDetalle.setTextSize(14);
            } else {
                strDetalle.setTextSize(13);
            }

            strDetalle.setText(detalle);

            if(detalle.length() > 30) {
                token = new StringTokenizer(detalle, "(");
                aux = token.nextToken();
                strDetalle.setText(aux);
            }

            if(aux.length() > 35){
                strDetalle.setTextSize(12.7f);
            }

            strCantidad.setText("Cantidad: " + format.format(listaItems.get(position).getCantidad()));
            strPrecioUnitario.setText("PxUni: " + format.format(listaItems.get(position).getPrecioFinal()));
            strTotal.setText(format.format(listaItems.get(position).getTotal()));
            return item;
        }

    }

    private void mostrarListaItems() {
        if (listaItems != null) {
            MostrarComprobanteActivity.AdaptadorComprobantesItems adapter =
                    new AdaptadorComprobantesItems(listaItems);
            listaDetalle.setAdapter(adapter);
            listaDetalle.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<Historico> listarHistorico(String response) {
        ArrayList<Historico> lista = new ArrayList<Historico>();
        try {
            if (response != null) {
                JSONArray json = new JSONArray(response);
                for (int i = 0; i < json.length(); i++) {
                    Historico item = new Historico();
                    if ((json.getJSONObject(i).isNull("FECHA") == false) &&
                            (json.getJSONObject(i).isNull("SUCURSAL") == false) &&
                            (json.getJSONObject(i).isNull("NUMERO") == false)) {
                        item.setId(json.getJSONObject(i).getString("ID"));
                        item.setTipo(json.getJSONObject(i).getString("TIPO"));
                        item.setClase(json.getJSONObject(i).getString("CLASE"));
                        item.setSucursal(json.getJSONObject(i).getInt("SUCURSAL"));
                        item.setNumero(json.getJSONObject(i).getInt("NUMERO"));
                        item.setFecha(json.getJSONObject(i).getString("FECHA"));
                        item.setSaldo(json.getJSONObject(i).getDouble("SALDO"));
                        item.setTotal(json.getJSONObject(i).getDouble("TOTAL"));
                        lista.add(item);
                    }
                }
            }
            return lista;
        } catch (JSONException je) {
            je.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Error al buscar el histórico del comprobante Json Error:" + je.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Error sin definir al cargar el detalle:" + e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
    }

    class AdaptadorHistorico extends ArrayAdapter<Historico> {
        AdaptadorHistorico(List<Historico> listaTransComprobante) {
            super(MostrarComprobanteActivity.this, R.layout.comprobante_historico, listaHistorico);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            DecimalFormat format = new DecimalFormat("#.00");

            View item = convertView;
            item = getLayoutInflater().inflate(R.layout.comprobante_historico, null);
            TextView strFecha = (TextView) item.findViewById(R.id.lblFecha);
            TextView strComprobante = (TextView) item.findViewById(R.id.lblComprobante);
            TextView strTotal = (TextView) item.findViewById(R.id.strTotal);

            strFecha.setText(listaHistorico.get(position).getFecha());
            strComprobante.setText(listaHistorico.get(position).getTipo() +
                    listaHistorico.get(position).getClase() + " " +
                    listaHistorico.get(position).getSucursal() + " " +
                    listaHistorico.get(position).getNumero());
            strTotal.setText(format.format(listaHistorico.get(position).getTotal()));
            return item;
        }

    }

    private void mostrarListaHistorico() {
        if (listaHistorico != null) {
            MostrarComprobanteActivity.AdaptadorHistorico adapter =
                    new AdaptadorHistorico(listaHistorico);
            listaDetalle.setAdapter(adapter);
            listaDetalle.setVisibility(View.VISIBLE);
        }
    }

    public void totalReturn(){
        Thread thread = new Thread() {
            @Override
            public void run() {
               final boolean res = insert();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msj;
                        if (res) {

                            msj = getApplicationContext().getString(R.string.mensajeInsercionOK);
                        }
                        else{
                            msj = getApplication().getString(R.string.mensajeInsercionError);
                        }

                        Toast.makeText(getApplicationContext(), msj, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        };
        thread.start();
    }

    public boolean insert(){
        if ((_idCliente == null) || (_idVendedor == null) || (_idEmpresa == null)) {
            Toast.makeText(getApplicationContext(), R.string.errorNulo, Toast.LENGTH_LONG).show();
            finish();
        }

        //convierto los items en articulos a devolver
        List<Articulo> articulos = new ArrayList<>();
        Articulo articulo;
        ComprobanteItem comprobanteItem;
        Iterator<ComprobanteItem> iterator = listaItems.iterator();

        while (iterator.hasNext()){
            articulo = new Articulo();
            comprobanteItem = iterator.next();

            articulo.setId(comprobanteItem.getIdArticulo());
            articulo.setCantidad(comprobanteItem.getCantidad());
            articulo.setPrecioVenta(comprobanteItem.getPrecioFinal());
            articulo.setPrecioConDesc(comprobanteItem.getPrecioFinal());
            articulo.setDescuento(0.0);

            articulos.add(articulo);
        }

        return Devoluciones.insertarDevolucion(_idEmpresa, _idVendedor, _idCliente, _idFletero,
                                                total, _caja, _planilla, articulos);
    }

    public boolean insertReturn(){
        String id = UUID.randomUUID().toString();
        String _strURLDev = "http://idus-express-return.dnsalias.com/webserviceidusexpress";
        boolean resultado = false;

        if ((_idCliente == null) || (_idVendedor == null) || (_idEmpresa == null)) {
            Toast.makeText(getApplicationContext(), R.string.errorNulo, Toast.LENGTH_LONG).show();

        } else {
            String urlParametros = "_id=" + id + "&_idempresa=" + _idEmpresa + "&_idvendedor=" + _idVendedor + "&_idcliente=" + _idCliente + "&_total=" + total
                    + "&_idFletero=" + _idFletero + "&_codigoCaja=" + _caja + "&_planilla=" + _planilla;
            urlParametros = urlParametros.replace(",", ".");
            HttpURLConnection cnx = null;

            try {
                URL url = new URL(_strURLDev + "/insertar_devolucion.php");
                cnx = (HttpURLConnection) url.openConnection();

                //estableciendo el metodo
                cnx.setRequestMethod("POST");
                //longitud de los parametros que estamos enviando
                cnx.setRequestProperty("Context-length", "" + Integer.toString(urlParametros.getBytes().length));
                //se menciona para la salida de datos
                cnx.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(cnx.getOutputStream());
                wr.writeBytes(urlParametros);
                wr.close();

                InputStream in = cnx.getInputStream();
                int respuesta = cnx.getResponseCode();

                //comienzo a grabar los items
                if (respuesta == HttpURLConnection.HTTP_OK) {
                    //Iterator it = items.iterator();
                    Iterator<ComprobanteItem> it = listaItems.iterator();

                    while (it.hasNext()) {
                        String idItem = UUID.randomUUID().toString();
                        String idArticulo, cantidad, subTotal, pxUni, Desc, precDesc;

                        ComprobanteItem item = (ComprobanteItem) it.next();
                        idArticulo = item.getId();
                        cantidad = String.valueOf(item.getCantidad());
                        pxUni = String.valueOf(item.getPrecioFinal());
                        precDesc = "0.0"; // verificar
                        Desc = "0.0"; // verificar
                        subTotal = String.valueOf(item.getTotal());


                        urlParametros = "_idcab=" + id + "&_id=" + idItem + "&_idarticulo=" + idArticulo + "&_cantidad=" + cantidad
                                +"&_subtotal=" + subTotal + "&_pxuni=" + pxUni +"&_prdesc="+ precDesc
                                +"&_pxcdesc="+ Desc;
                        urlParametros=urlParametros.replace(",",".");
                        URL url2 = new URL(_strURLDev + "/insertar_cuerpo_devolucion.php");
                        cnx = (HttpURLConnection) url2.openConnection();
                        cnx.setRequestMethod("POST");
                        cnx.setRequestProperty("Context-length", "" + Integer.toString(urlParametros.getBytes().length));
                        cnx.setDoOutput(true);

                        DataOutputStream wr1 = new DataOutputStream(cnx.getOutputStream());
                        wr1.writeBytes(urlParametros);
                        wr1.close();

                        InputStream in1 = cnx.getInputStream();
                        int respuesta1 = cnx.getResponseCode();
                        if (respuesta1 == HttpURLConnection.HTTP_OK) {
                            resultado = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //return true;
        return resultado;
    }

    //envio y recepción webservice
    public int enviarDiasGET(int numButton, Comprobante comp) {

        URL url = null;
        String linea = "";
        int respuesta = RESULT_CANCELED;
        StringBuilder result = null;

        try {
            if (verificaConexion(getApplicationContext())) {

                if (numButton == 0) {
                    url = new URL(_strUrl + "/buscarComprobanteDetalleID.php?_id=" + comp.getId());
                } else if (numButton == 1) {
                    url = new URL(_strUrl + "/listarHistoricoCliente.php?_idCliente=" + comp.getIdCliente());
                }
                HttpURLConnection cnx = (HttpURLConnection) url.openConnection();
                respuesta = cnx.getResponseCode();

                if (respuesta == HttpURLConnection.HTTP_OK) {
                    result = new StringBuilder();
                    InputStream in = new BufferedInputStream(cnx.getInputStream());
                    BufferedReader leer = new BufferedReader(new InputStreamReader(in));

                    while ((linea = leer.readLine()) != null) {
                        result.append(linea);
                    }
                    resultadoGET = result.toString();
                    respuesta = RESULT_OK;
                }
                else{
                    resultadoGET = "Se produjo un error al buscar el detalle del comprobante";
                }
            } else {
                resultadoGET = getApplicationContext().getString(R.string.msgErrInternet);
            }
        } catch (MalformedURLException e) {
            resultadoGET = e.getMessage();
        } catch (IOException e) {
            resultadoGET = e.getMessage();
        }

        return respuesta;

    }

    public int enviarDiasPOST(int numButton, Comprobante comp, double importe, int movimiento, String lat, String lon,
                              String pres, String versionApp, String versionAndroid) {
        String linea = "";
        int respuesta = 0;
        StringBuilder result = null;
        String idMovimiento = UUID.randomUUID().toString();

        try {
            if (verificaConexion(getApplicationContext())) {

                String urlParametros = "_idMovimiento=" + idMovimiento +
                        "&_idComprobante=" + comp.getId() + "&_tipoMovimiento=" + movimiento + "&_importe=" + importe +
                        "&_idFletero=" + comp.getIdFletero() + "&_lat=" + lat + "&_lon=" + lon + "&_pres" +
                        "&_versionApp=" + versionApp + "&_versionAndroid=" + versionAndroid + "&_caja=" + comp.getCaja() +
                        "&_numeroCaja=" + comp.getPlanilla();
                urlParametros = urlParametros.replace(",", ".");
                HttpURLConnection cnx = null;
                URL url = new URL(_strUrl + "/insertarMovimientoRendicionFletero.php");
                cnx = (HttpURLConnection) url.openConnection();

                //estableciendo el metodo
                cnx.setRequestMethod("POST");
                //longitud de los parametros que estamos enviando
                cnx.setRequestProperty("Context-length", "" + Integer.toString(urlParametros.getBytes().length));
                //se menciona para la salida de datos
                cnx.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(cnx.getOutputStream());
                wr.writeBytes(urlParametros);
                wr.close();

                InputStream in = cnx.getInputStream();
                respuesta = cnx.getResponseCode();

                return respuesta;

            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.msgErrInternet, Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return respuesta;

    }

    private void subirFoto() {
        AsyncHttpClient cliente = new AsyncHttpClient();
        String url = _strUrl + "/cargaImagenEnServidor.php";
        RequestParams parametros = new RequestParams();
        try {
            parametros.put("_codigoEmpresa", _codigoEmpresa);
            parametros.put("_imagenFirma", file);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        RequestHandle post = cliente.post(url, parametros, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode == 200) {
                    Toast.makeText(getApplicationContext(), "La imagen se subio correctameente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "Error al subir la imagen", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static boolean verificaConexion(Context ctx) {
        boolean bConectado = false;
        ConnectivityManager connec = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        // No sólo wifi, también GPRS
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        // este bucle debería no ser tan ñapa
        for (int i = 0; i < 2; i++) {
            // ¿Tenemos conexión? ponemos a true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                bConectado = true;
            }
        }
        return bConectado;
    }

    public void obtenerLocalizacion() {
        Criteria criterios = new Criteria();
        criterios.setAccuracy(Criteria.ACCURACY_FINE);
        criterios.setPowerRequirement(Criteria.POWER_LOW);
        //intento buscar la geoPosicion
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(MostrarComprobanteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MostrarComprobanteActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.strMensajeDeNoPermisos, Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        String proveedor = locManager.getBestProvider(criterios, true);
        if (proveedor == null) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.strErrorDispositivos, Toast.LENGTH_LONG);
            toast.show();
        } else {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = String.valueOf(location.getLatitude());
                    lon = String.valueOf(location.getLongitude());
                    pres = String.valueOf(location.getAccuracy());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locManager.requestLocationUpdates(proveedor, 5, 50, locationListener);
            location = locManager.getLastKnownLocation(proveedor);
            if (location != null) {
                lat = String.valueOf(location.getLatitude());
                lon = String.valueOf(location.getLongitude());
                pres = String.valueOf(location.getAccuracy());
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.strErrorGPS, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}
