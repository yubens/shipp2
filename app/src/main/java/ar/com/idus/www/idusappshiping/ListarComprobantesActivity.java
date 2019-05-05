package ar.com.idus.www.idusappshiping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ar.com.idus.www.idusappshiping.modelos.Comprobante;

public class ListarComprobantesActivity extends AppCompatActivity {

    TextView strPlanilla;
    ListView listaComprobantes;
    Button btnGPS, btnActualizar;
    String _strUrl;
    String _idEmpresa, _idFletero, _caja, _planilla, _codigoEmpresa, _nombreFletero;
    ProgressBar pb01;
    boolean mostrarMenu = true;
    private ArrayList<Comprobante> lvComprobante;

    boolean ocultar_menu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_comprobantes);

        strPlanilla = (TextView) findViewById(R.id.strPlanilla);
        listaComprobantes = (ListView) findViewById(R.id.lvComprobantes);
        pb01 = (ProgressBar) findViewById(R.id.pb01);
        btnActualizar = (Button) findViewById(R.id.btnActualizarComprobantes);
        btnGPS = (Button) findViewById(R.id.btpDondeEstoy);
        _strUrl = "";

        Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            _idEmpresa = recupera.getString("_idEmpresa");
            _idFletero = recupera.getString("_idFletero");
            _caja = recupera.getString("_caja");
            _planilla = recupera.getString("_planilla");
            _codigoEmpresa = recupera.getString("_codigoEmpresa");
            _strUrl = recupera.getString("_strURL");
            _nombreFletero = recupera.getString("_nombreFletero");

            pb01.setVisibility(View.VISIBLE);

            //se ocultan los botones hasta obtener un resultado
            btnActualizar.setVisibility(View.GONE);
            btnGPS.setVisibility(View.GONE);
            strPlanilla.setText("Buscando comprobantes de la Caja: " + _caja + " " + _planilla);
            listaComprobantes.setVisibility(View.GONE);

            Thread tr = new Thread() {
                @Override
                public void run() {

                    final String resultado = enviarDiasGET(_codigoEmpresa, _caja, _planilla, _idFletero);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(resultado.equals("[]")){
                                strPlanilla.setText("No hay comprobantes para la planilla: " + _caja + " | " + _planilla);
                                mostrarMenu = false;
                                pb01.setVisibility(View.GONE);
                                invalidateOptionsMenu();
                            }
                            else{
                                lvComprobante = listaComprobantes01(resultado);
                                mostrarLista();
                            }
                        }
                    });
                }
            };
            tr.start();
        }

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResume();
            }
        });

        listaComprobantes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("click");
                Comprobante comprobante = (Comprobante) parent.getItemAtPosition(position);
                if (comprobante.getEstado() > 0) {
                    Toast.makeText(getApplicationContext(), "Este comprobante ya tiene registrada una actividad", Toast.LENGTH_LONG).show();
                }

                Intent i = new Intent(getApplicationContext(), MostrarComprobanteActivity.class);
                i.putExtra("_idEmpresa", _idEmpresa);
                i.putExtra("_idFletero", _idFletero);
                i.putExtra("_codigoEmpresa", _codigoEmpresa);
                i.putExtra("comprobante", comprobante);
                i.putExtra("_strURL", _strUrl);
                startActivity(i);
            }
        });

        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                i.putExtra("_idEmpresa", _idEmpresa);
                i.putExtra("_idFletero", _idFletero);
                i.putExtra("listaComprobante", lvComprobante);
                i.putExtra("_strURL", _strUrl);
                startActivity(i);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (mostrarMenu) {
            getMenuInflater().inflate(R.menu.opciones_cliente, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mnu01EnviarMensajes) {
            Toast toast = Toast.makeText(getApplicationContext(), "Pronto Mensajes!!", Toast.LENGTH_LONG);
            toast.show();
        } else if (id == R.id.mnu01FinReparto) {
            if (lvComprobante.size() == 0) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.strNoEstanFinalizadosTodosLosComprobantes, Toast.LENGTH_LONG);
                toast.show();
            } else {
                AlertDialog.Builder alerta;
                alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext(), android.R.style.Theme_Material_Dialog_Alert);
                alerta.setMessage(R.string.strMensajeFinDeReparto)
                        .setTitle(R.string.strAdvertencia)
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
                                        final int respuesta = enviarDiasPOST(_idEmpresa, _idFletero, _caja, _planilla);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (respuesta == 200) {
                                                    Intent i = new Intent(getApplicationContext(), RendicionFinalActivity.class);
                                                    i.putExtra("_idEmpresa", _idEmpresa);
                                                    i.putExtra("_idFletero", _idFletero);
                                                    i.putExtra("listaComprobante", lvComprobante);
                                                    i.putExtra("_strURL", _strUrl);
                                                    i.putExtra("_caja", _caja);
                                                    i.putExtra("_planilla", _planilla);
                                                    i.putExtra("_nombreFletero", _nombreFletero);
                                                    startActivity(i);
                                                }
                                                else{
                                                    Toast.makeText(getApplicationContext(), "Se produjo un error al insertar el registro de rendición",
                                                                    Toast.LENGTH_LONG).show();
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
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        strPlanilla.setText("Buscando comprobantes de la Caja: " + _caja + " " + _planilla);
        listaComprobantes.setVisibility(View.GONE);
        pb01.setVisibility(View.VISIBLE);

        Thread tr = new Thread() {
            @Override
            public void run() {
                //pb01.setVisibility(View.VISIBLE);
                final String resultado = enviarDiasGET(_codigoEmpresa, _caja, _planilla, _idFletero);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    if(resultado.equals("[]")){
                        strPlanilla.setText("No hay comprobantes para la planilla: " + _caja + " | " + _planilla);
                        mostrarMenu = false;
                        pb01.setVisibility(View.GONE);
                        invalidateOptionsMenu();
                    }
                    else{
                        lvComprobante = listaComprobantes01(resultado);
                        mostrarLista();
                    }
                    }
                });

            }
        };
        tr.start();
        super.onResume();
    }

    //metodo para obtener los datos desde el servidor
    public String enviarDiasGET(String codigoEmpresa, String caja, String planilla, String idFletero) {

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder result = null;

        try {
            if (verificaConexion(getApplicationContext())) {

                url = new URL(_strUrl + "/listarPlanillaPorFletero.php?_empresa=" + codigoEmpresa + "&_caja="
                        + caja + "&_planilla=" + planilla + "&_idFletero=" + idFletero);

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

    public int enviarDiasPOST(String idEmpresa, String idFletero, String caja, String numeroPlanilla) {
        //String linea = "";
        int respuesta = 0;
        //StringBuilder result = null;

        try {
            if (verificaConexion(getApplicationContext())) {

                String urlParametros = "_empresaID=" + idEmpresa +
                        "&_fleteroID=" + idFletero +
                        "&_caja=" + caja +
                        "&_numeroPlanilla=" + numeroPlanilla;
                urlParametros = urlParametros.replace(",", ".");
                HttpURLConnection cnx = null;
                URL url = new URL(_strUrl + "/insertarRegistrosDeRendicion.php");
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

                cnx.getInputStream();
                respuesta = cnx.getResponseCode();

                return respuesta;

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return respuesta;

    }

    private ArrayList<Comprobante> listaComprobantes01(String response) {
        ArrayList<Comprobante> lista = new ArrayList<Comprobante>();

        try {
            JSONArray json = new JSONArray(response);

            for (int i = 0; i < json.length(); i++) {
                Comprobante comp = new Comprobante();
                comp.setFecha(json.getJSONObject(i).getString("FECHA"));
                comp.setTipo(json.getJSONObject(i).getString("TIPO"));
                comp.setClase(json.getJSONObject(i).getString("CLASE"));
                comp.setSucursal(json.getJSONObject(i).getInt("SUCURSAL"));
                comp.setNumero(json.getJSONObject(i).getInt("NUMERO"));
                comp.setId(json.getJSONObject(i).getString("ID"));
                comp.setComprobante(comp.getTipo() + comp.getClase() + "-" + comp.getSucursal() + "-" + comp.getNumero());
                comp.setTotal(json.getJSONObject(i).getDouble("TOTAL"));
                comp.setSaldo(json.getJSONObject(i).getDouble("SALDO"));
                comp.setNombreVendedor(json.getJSONObject(i).getString("NOMBRE_VENDEDOR"));
                comp.setNombreFletero(json.getJSONObject(i).getString("NOMBRE_FLETERO"));
                comp.setNombreCliente(json.getJSONObject(i).getString("NOMBRE"));
                comp.setMhr(json.getJSONObject(i).getInt("MHR"));
                comp.setFormaDePago(json.getJSONObject(i).getInt("FORMADEPAGO"));
                if (comp.getMhr() == 1) {
                    comp.setDomicilioCliente(".:COBRAR- RESTO CTA CTE:.");
                } else {
                    comp.setDomicilioCliente(json.getJSONObject(i).getString("DOMICILIO"));
                }
                comp.setCaja(json.getJSONObject(i).getInt("CAJA"));
                comp.setPlanilla(json.getJSONObject(i).getInt("NUMEROCAJA"));
                comp.setIdVendedor(json.getJSONObject(i).getString("VENDEDOR_ID"));
                comp.setIdFletero(json.getJSONObject(i).getString("FLETERO_ID"));
                comp.setIdCliente(json.getJSONObject(i).getString("CLIENTE_ID"));
                comp.setClienteLatitud(json.getJSONObject(i).getString("LATITUD"));
                comp.setClienteLongitud(json.getJSONObject(i).getString("LONGITUD"));
                comp.setEstado(json.getJSONObject(i).getInt("ESTADO_COMPROBANTE"));
                comp.setTipo_mov(json.getJSONObject(i).getInt("TIPO_MOV"));
                comp.setEnvio_idus(json.getJSONObject(i).getInt("ENVIO_IDUS"));
                //lo meto en el lis que va a devolver.
                lista.add(comp);
            }
            return lista;
        } catch (JSONException je) {
            je.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Error al buscar comprobantes Json Error:" + je.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Error sin definir:" + e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
    }

    class AdaptadorComprobantes extends ArrayAdapter<Comprobante> {
        AdaptadorComprobantes(List<Comprobante> listaTransComprobante) {
            super(ListarComprobantesActivity.this, R.layout.comprobante, lvComprobante);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            System.out.println("adaptador comprobante");

            DecimalFormat format = new DecimalFormat("#0.00");
            View item = convertView;
            item = getLayoutInflater().inflate(R.layout.comprobante, null);
            TextView compFecha = (TextView) item.findViewById(R.id.compFecha);
            TextView compComp = (TextView) item.findViewById(R.id.compNumero);
            TextView compNombre = (TextView) item.findViewById(R.id.compNombre);
            TextView compDomicilio = (TextView) item.findViewById(R.id.compDomicilio);
            TextView compTotal = (TextView) item.findViewById(R.id.compTotal);
            ImageView imageView = (ImageView) item.findViewById(R.id.imgComp);

            if (lvComprobante.get(position).getMhr() == 1) {
                imageView.setImageResource(R.mipmap.cobranza);
                if (lvComprobante.get(position).getSaldo() > .10) {
                    compTotal.setText("Saldo \n$ " + format.format(lvComprobante.get(position).getSaldo()));
                } else {
                    compTotal.setText("Saldo \n$ " + format.format(lvComprobante.get(position).getTotal()));
                }
            } else {
                imageView.setImageResource(R.mipmap.entrega_luiza);
                compTotal.setText("Total \n$ " + format.format(lvComprobante.get(position).getTotal()));
            }

            compFecha.setText(lvComprobante.get(position).getFecha());
            compComp.setText(lvComprobante.get(position).getComprobante());
            compNombre.setText(lvComprobante.get(position).getNombreCliente());
            compDomicilio.setText(lvComprobante.get(position).getDomicilioCliente());

            switch (lvComprobante.get(position).getTipo_mov()) {

                case 0:
                    compComp.setBackgroundColor(getResources().getColor(R.color.colorRosaPagar));
                    compComp.setTextColor(Color.WHITE);
                    compFecha.setBackgroundColor(getResources().getColor(R.color.colorRosaPagar));
                    compFecha.setTextColor(Color.WHITE);
                    compTotal.setBackgroundColor(getResources().getColor(R.color.colorRosaPagar));
                    compTotal.setTextColor(Color.WHITE);
                    break;

                case 1:
                    compComp.setBackgroundColor(getResources().getColor(R.color.colorCelesteTotal));
                    compComp.setTextColor(Color.WHITE);
                    compFecha.setBackgroundColor(getResources().getColor(R.color.colorCelesteTotal));
                    compFecha.setTextColor(Color.WHITE);
                    compTotal.setBackgroundColor(getResources().getColor(R.color.colorCelesteTotal));
                    compTotal.setTextColor(Color.WHITE);
                    break;

                case 2:
                    compComp.setBackgroundColor(getResources().getColor(R.color.colorAmarilloParcial));
                    compFecha.setBackgroundColor(getResources().getColor(R.color.colorAmarilloParcial));
                    compTotal.setBackgroundColor(getResources().getColor(R.color.colorAmarilloParcial));
                    break;

                case 3:
                    compComp.setBackgroundColor(getResources().getColor(R.color.colorVerdeReenvio));
                    compComp.setTextColor(Color.WHITE);
                    compFecha.setBackgroundColor(getResources().getColor(R.color.colorVerdeReenvio));
                    compFecha.setTextColor(Color.WHITE);
                    compTotal.setBackgroundColor(getResources().getColor(R.color.colorVerdeReenvio));
                    compTotal.setTextColor(Color.WHITE);
                    break;

                case 4:
                    compComp.setBackgroundColor(getResources().getColor(R.color.colorNaranjaEspera));
                    compComp.setTextColor(Color.WHITE);
                    compFecha.setBackgroundColor(getResources().getColor(R.color.colorNaranjaEspera));
                    compFecha.setTextColor(Color.WHITE);
                    compTotal.setBackgroundColor(getResources().getColor(R.color.colorNaranjaEspera));
                    compTotal.setTextColor(Color.WHITE);
                    break;

                default:
                    break;
            }
            return item;
        }

    }

    private void mostrarLista() {

        AdaptadorComprobantes adapter = new AdaptadorComprobantes(lvComprobante);
        listaComprobantes.setAdapter(adapter);
        pb01.setVisibility(View.GONE);

        if (listaComprobantes.getCount() > 0) {
            strPlanilla.setText("Planilla: " + _caja + " | " + _planilla);

            //se muestran los botones al haber comprobantes
            btnActualizar.setVisibility(View.VISIBLE);
            btnGPS.setVisibility(View.VISIBLE);
        }

        listaComprobantes.setVisibility(View.VISIBLE);

    }

    //metodo que permite cargar el array list desde json
    @SuppressLint("ShowToast")
    private ArrayList<String> listarComprobantes(String response) {
        ArrayList<String> listado = new ArrayList<String>();
        try {
            JSONArray json = new JSONArray(response);
            String texto = "";
            for (int i = 0; i < json.length(); i++) {
                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                double total = json.getJSONObject(i).getDouble("TOTAL");
                double saldo = json.getJSONObject(i).getDouble("SALDO");
                texto = json.getJSONObject(i).getString("FECHA");
                texto = texto + " | " + json.getJSONObject(i).getString("TIPO")
                        + json.getJSONObject(i).getString("CLASE") + "-"
                        + json.getJSONObject(i).getString("SUCURSAL")
                        + " " + json.getJSONObject(i).getString("NUMERO")
                        + "| TOT$ " + decimalFormat.format(total);
                if (json.getJSONObject(i).getInt("MHR") == 1) {
                    texto = texto + "\n" + json.getJSONObject(i).getString("NOMBRE")
                            + "\n" + "-COBRAR- $" + decimalFormat.format(saldo);

                } else {
                    texto = texto + "\n" + json.getJSONObject(i).getString("NOMBRE")
                            + "\n" + json.getJSONObject(i).getString("DOMICILIO");
                }
                listado.add(texto);
            }
        } catch (JSONException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error al buscar los días permitidos de la empresa", Toast.LENGTH_LONG);
            toast.show();
            e.printStackTrace();
        }

        return listado;
    }

    //metodo que permite cargar la lista (listview)
    private void cargarLista(ArrayList<String> datos) {
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datos);
        listaComprobantes.setAdapter(adaptador);
        pb01.setVisibility(View.GONE);
        strPlanilla.setText("Ruta numero" + _caja + "-" + _planilla);
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
}
