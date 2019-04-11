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
import java.util.List;
import java.util.UUID;

import ar.com.idus.www.idusappshiping.modelos.Comprobante;
import ar.com.idus.www.idusappshiping.modelos.ComprobanteItems;
import ar.com.idus.www.idusappshiping.modelos.Historico;
import cz.msebera.android.httpclient.Header;

public class MostrarComprobanteActivity extends AppCompatActivity {

    TextView strFecha, strComprobante, strCliente, strDomicilio, strVendedor, strTotal, strSaldo, strPago, strDirFirma;
    Button btnDetalle, btnHistorico, btnCancelar, btnPagar, btnFirma;
    EditText txtImporte;
    ListView listaDetalle;
    Spinner spOpciones;
    ProgressBar pbDetalle;
    ImageView imagenPago;
    ArrayList<ComprobanteItems> listaItems;
    ArrayList<Historico> listaHistorico;
    Comprobante comprobante;
    String _strUrl, root, fname, _codigoEmpresa, _idEmpresa;
    File myDir, file;
    Boolean requiereFirma = true;
    Boolean firmo = false;

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

        versionApp = BuildConfig.VERSION_NAME;
        versionAndroid = Build.VERSION.RELEASE;

        Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            DecimalFormat format = new DecimalFormat("#.00");
            comprobante = (Comprobante) getIntent().getSerializableExtra("comprobante");
            strFecha.setText(comprobante.getFecha());
            strComprobante.setText(comprobante.getComprobante());
            strCliente.setText(comprobante.getNombreCliente());
            strDomicilio.setText(comprobante.getDomicilioCliente());
            strVendedor.setText(comprobante.getNombreVendedor());
            strTotal.setText(format.format(comprobante.getTotal()));
            if (comprobante.getSaldo() <= 0.01) {
                strSaldo.setText(format.format(comprobante.getTotal()));
            } else {
                strSaldo.setText(format.format(comprobante.getSaldo()));
            }
            txtImporte.setText(format.format(comprobante.getSaldo()));
            _strUrl = recupera.getString("_strURL");
            _codigoEmpresa = recupera.getString("_codigoEmpresa");
            _idEmpresa = recupera.getString("_idEmpresa");


            pbDetalle.setVisibility(View.VISIBLE);
            Thread tr = new Thread() {
                @Override
                public void run() {
                    final String resultado = enviarDiasGET(0, comprobante);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listaItems = listarComprobanteItems(resultado);
                            mostrarListaItems();
                            obtenerLocalizacion();
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
                        final String resultado = enviarDiasGET(0, comprobante);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listaItems = listarComprobanteItems(resultado);
                                mostrarListaItems();
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
                        final String resultado = enviarDiasGET(1, comprobante);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listaHistorico = listarHistorico(resultado);
                                mostrarListaHistorico();
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
                final double total = Double.parseDouble(strTotal.getText().toString().replace(",", "."));
                final double importe = Double.parseDouble(txtImporte.getText().toString().replace(",", "."));
                if (importe > total) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.strTotalMenorAImporte, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    if (comprobante.getMhr() == 0) {
                        final int medio = spOpciones.getSelectedItemPosition();
                        if (medio == 0) {
                            mensaje = "¿Confirma dejar (A PAGAR) el comprobante: " + strComprobante.getText().toString() + " ?";
                            if ((requiereFirma) && (firmo)) {
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), R.string.strNoEstaFirmado, Toast.LENGTH_LONG);
                                toast.show();
                                return;
                            }
                        } else if (medio == 1) {
                            mensaje = "¿Confirma el pago TOTAL del comprobante: " + strComprobante.getText().toString() + " por un valor de $: " + txtImporte.getText().toString() + " ?";
                            requiereImporte = true;
                        } else if (medio == 2) {
                            mensaje = "¿Confirma el pago PARCIAL Del comprobante: " + strComprobante.getText().toString() + " por un valor de $: " + txtImporte.getText().toString() + " ?";
                            requiereImporte = true;
                        } else if (medio == 3) {
                            mensaje = "¿Confirma RE ENVIAR el comprobante: " + strComprobante.getText().toString() + " ?";
                        }
                        if (((requiereImporte) && (importe > 0)) || ((medio == 0) || (medio == 3))) {
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
                                                    final int respuesta = enviarDiasPOST(3, comprobante, importe, medio, lat, lon, pres, versionApp, versionAndroid);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (respuesta == 200) {
                                                                Toast toast = Toast.makeText(getApplicationContext(), R.string.strMensajeInsertado, Toast.LENGTH_LONG);
                                                                toast.show();
                                                                if (medio != 0) {
                                                                    finish();
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            };
                                            tr.start();
                                            if ((medio == 0) && (firmo) && (requiereFirma)) {
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
                    } else {
                        final int medio = spOpciones.getSelectedItemPosition();
                        if ((medio == 0) || (medio == 3)) {
                            mensaje = "No se puede usar esta opción para comprobantes que están para cobrar";
                            Toast toast = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            if (importe > 0) {
                                mensaje = "¿Confirma el pago Del comprobante: " + strComprobante.getText().toString() + " por un valor de $: " + txtImporte.getText().toString() + " ?";
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
                                                        final int respuesta = enviarDiasPOST(3, comprobante, importe, medio, lat, lon, pres, versionApp, versionAndroid);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (respuesta == 200) {
                                                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.strMensajeInsertado, Toast.LENGTH_LONG);
                                                                    toast.show();
                                                                    finish();
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
                if (position == 0) {
                    txtImporte.setText("");
                    txtImporte.setEnabled(false);
                    requiereFirma = true;
                    imagenPago.setImageResource(R.mipmap.button_a_pagar);
                } else if (position == 1) {
                    txtImporte.setText(strSaldo.getText().toString().replace(",", "."));
                    txtImporte.setEnabled(true);
                    txtImporte.requestFocus();
                    requiereFirma = false;
                    imagenPago.setImageResource(R.mipmap.button_pago_total);
                } else if (position == 2) {
                    txtImporte.setText("");
                    txtImporte.setEnabled(true);
                    txtImporte.requestFocus();
                    requiereFirma = false;
                    imagenPago.setImageResource(R.mipmap.button_pago_parcial);
                } else if (position == 3) {
                    txtImporte.setText("");
                    txtImporte.setEnabled(false);
                    requiereFirma = false;
                    imagenPago.setImageResource(R.mipmap.button_re_enviar);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opciones_devolucion, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String cliente[];

        if (id == R.id.mnu01Devolución) {

            if(_codigoEmpresa.equals("534")){
                Toast.makeText(getApplicationContext(), "La empresa actual, no permite el ingreso a esta opción",
                                Toast.LENGTH_LONG).show();
            }
            else{
                cliente = comprobante.getIdCliente().split("CLIENTE");

                Intent intent = new Intent(getApplicationContext(), CargaDevolucion.class);
                intent.putExtra("_idVendedor", comprobante.getIdVendedor());
                intent.putExtra("_idEmpresa", _idEmpresa);
                intent.putExtra("_codCliente", cliente[1]);
                startActivity(intent);
            }

        }

        return super.onContextItemSelected(item);
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
    private ArrayList<ComprobanteItems> listarComprobanteItems(String response) {
        ArrayList<ComprobanteItems> lista = new ArrayList<ComprobanteItems>();
        try {
            JSONArray json = new JSONArray(response);
            for (int i = 0; i < json.length(); i++) {
                ComprobanteItems items = new ComprobanteItems();
                items.setId(json.getJSONObject(i).getString("ID"));
                items.setCantidad(json.getJSONObject(i).getDouble("CANTIDAD"));
                items.setDetalle(json.getJSONObject(i).getString("DETALLE"));
                items.setCodigoArticulo(json.getJSONObject(i).getString("CODIGO"));
                items.setIdArticulo(json.getJSONObject(i).getString("ARTICULO_ID"));
                items.setItem(json.getJSONObject(i).getInt("ITEM"));
                items.setPorcentajeDescuento(json.getJSONObject(i).getDouble("PORCENTAJEDESCUENTO"));
                items.setPrecioCosto(json.getJSONObject(i).getDouble("PRECIOCOSTO"));
                items.setPrecioImpInt(json.getJSONObject(i).getDouble("IMPORTEIMPUESTOINTERNO"));
                items.setPrecioIva(json.getJSONObject(i).getDouble("IMPORTEIVA"));
                items.setPrecioIvaO(json.getJSONObject(i).getDouble("IMPORTEIVAOTRO"));
                items.setPrecioNeto(json.getJSONObject(i).getDouble("PRECIONETO"));
                items.setPrecioFinal(json.getJSONObject(i).getDouble("PRECIO_FINAL"));
                items.setTotal(items.getCantidad() * items.getPrecioFinal());
                lista.add(items);
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

    class AdaptadorComprobantesItems extends ArrayAdapter<ComprobanteItems> {
        AdaptadorComprobantesItems(List<ComprobanteItems> listaTransComprobante) {
            super(MostrarComprobanteActivity.this, R.layout.comprobante_items, listaItems);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            DecimalFormat format = new DecimalFormat("#.00");
            String detalle = listaItems.get(position).getDetalle();

            View item = convertView;
            item = getLayoutInflater().inflate(R.layout.comprobante_items, null);
            TextView strCodigo = (TextView) item.findViewById(R.id.lblCodigo);
            TextView strDetalle = (TextView) item.findViewById(R.id.lblDetalle);
            TextView strCantidad = (TextView) item.findViewById(R.id.lblCantidad);
            TextView strPrecioUnitario = (TextView) item.findViewById(R.id.lblPrecioUnitario);
            TextView strTotal = (TextView) item.findViewById(R.id.lblTotal);

            strCodigo.setText(listaItems.get(position).getCodigoArticulo());

            //si el detalle es amplio se reduce el tamaño de la letra
            if (detalle.length() > 25) {
                strDetalle.setTextSize(13);
            }
            strDetalle.setText(detalle);

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


    //envio y recepción webservice
    public String enviarDiasGET(int numButton, Comprobante comp) {

        URL url = null;
        String linea = "";
        int respuesta = 0;
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

                result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {
                    InputStream in = new BufferedInputStream(cnx.getInputStream());
                    BufferedReader leer = new BufferedReader(new InputStreamReader(in));

                    while ((linea = leer.readLine()) != null) {
                        result.append(linea);
                    }
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.msgErrInternet, Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

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
                    Toast.makeText(getApplicationContext(), "La imagen subio correctameente", Toast.LENGTH_LONG).show();
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
