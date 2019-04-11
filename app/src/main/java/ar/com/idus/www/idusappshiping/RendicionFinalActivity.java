package ar.com.idus.www.idusappshiping;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ar.com.idus.www.idusappshiping.modelos.MoneyR;

public class RendicionFinalActivity extends AppCompatActivity {

    private String _strURL = "";
    private String _idEmpresa, _idFletero, _caja, _planilla, _nombreFletero;
    private String totalARendir;
    private ArrayList<MoneyR> arrayMoneR;
    private ListView listaMonedas;
    private Button btnAceptar, btnCancelar;
    private TextView strNombreFletero, strPlanilla, strTotal, strARendir, strDiferencia;
    private ProgressBar pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rendicion_final);

        listaMonedas = (ListView) findViewById(R.id.listRendicion);
        btnAceptar = (Button) findViewById(R.id.btnRendicionConfirmar);
        btnCancelar = (Button) findViewById(R.id.btnRendicionCancelar);
        strNombreFletero = (TextView) findViewById(R.id.strNombreFletero);
        strPlanilla = (TextView) findViewById(R.id.strPlanilla);
        strTotal = (TextView) findViewById(R.id.strTotalGralRendicion);
        strARendir = (TextView) findViewById(R.id.strARendir);
        strDiferencia = (TextView) findViewById(R.id.strDiferencia);
        pBar = (ProgressBar) findViewById(R.id.pBarRendicion);

        strTotal.setText("0");
        strARendir.setText("0");

        Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            _idEmpresa = recupera.getString("_idEmpresa");
            _idFletero = recupera.getString("_idFletero");
            _caja = recupera.getString("_caja");
            _planilla = recupera.getString("_planilla");
            _strURL = recupera.getString("_strURL");
            _nombreFletero = recupera.getString("_nombreFletero");
            strPlanilla.setText("Planilla a Rendir: " + _caja + " | " + _planilla);
            strNombreFletero.setText(_nombreFletero);

            pBar.setVisibility(View.VISIBLE);
            Thread tr = new Thread() {
                @Override
                public void run() {
                    final String response = enviarGET(_idEmpresa, _caja, _planilla, "", 0);
                    final String resultado = enviarGET(_idEmpresa, _caja, _planilla, _idFletero, 1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            arrayMoneR = listarMonedas(response);
                            mostrarModeloMonedas();
                            pBar.setVisibility(View.GONE);

                            int r = obtDatosUnicoJSON(resultado);
                            if (r > 0) {
                                DecimalFormat format = new DecimalFormat("#0.00");
                                strARendir.setText(format.format(Double.parseDouble(totalARendir)));
                            }
                        }
                    });
                }
            };

        }

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listaMonedas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MoneyR moneda = (MoneyR) parent.getItemAtPosition(position);
                Intent i = new Intent(getApplicationContext(), EditaMonedaActivity.class);
                i.putExtra("_idEmpresa", _idEmpresa);
                i.putExtra("_idFletro", _idFletero);
                i.putExtra("itemMoneda", (Serializable) moneda);
                i.putExtra("_strURL", _strURL);
                startActivity(i);
            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta;
                alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext(), android.R.style.Theme_Material_Dialog_Alert);
                alerta.setMessage(R.string.strMensajeDeConfirimacionRendicio)
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
                                pBar.setVisibility(View.VISIBLE);
                                Thread tr = new Thread() {
                                    @Override
                                    public void run() {
                                        final int response = enviarDiasPOST(_idEmpresa, _caja, _planilla);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (response == 200) {
                                                    Toast.makeText(getApplicationContext(), R.string.strExistoMoneda, Toast.LENGTH_LONG).show();
                                                    pBar.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                                    }
                                };
                                tr.start();
                                Thread tr1 = new Thread() {
                                    @Override
                                    public void run() {


                                    }
                                };
                                tr1.start();
                            }
                        });
                AlertDialog alert = alerta.create();
                alert.show();
            }
        });

    }

    @Override
    protected void onResume() {
        pBar.setVisibility(View.VISIBLE);
        Thread tr = new Thread() {
            @Override
            public void run() {
                final String response = enviarGET(_idEmpresa, _caja, _planilla, "", 0);
                final String resultado = enviarGET(_idEmpresa, _caja, _planilla, _idFletero, 1);
                final Double[] _aRendir = new Double[1];
                final Double[] _tOtal = new Double[1];
                final Double[] _diferencia = new Double[1];
                _aRendir[0] = 0.0;
                _tOtal[0] = 0.0;
                _diferencia[0] = 0.0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        arrayMoneR = listarMonedas(response);
                        mostrarModeloMonedas();
                        pBar.setVisibility(View.GONE);
                        int r = obtDatosUnicoJSON(resultado);
                        if (r > 0) {
                            DecimalFormat format = new DecimalFormat("#0.00");
                            strARendir.setText(format.format(Double.parseDouble(totalARendir)));
                            _tOtal[0] = Double.parseDouble(strTotal.getText().toString());
                            _aRendir[0] = Double.parseDouble(strARendir.getText().toString());
                            _diferencia[0] = _aRendir[0] - _tOtal[0];
                            strDiferencia.setText(format.format(_diferencia[0]));
                        }
                    }
                });
            }
        };
        tr.start();
        super.onResume();
    }

    public String enviarGET(String idEmpresa, String caja, String planilla, String idFletero, int accion) {

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder result = null;

        try {
            if (verificaConexion(getApplicationContext())) {
                if (accion == 0) {
                    url = new URL(_strURL + "/listarMonedasDeLaPlanilla.php?_empresaID=" + idEmpresa +
                            "&_caja=" + caja +
                            "&_numeroPlanilla=" + planilla);
                }
                if (accion == 1) {
                    url = new URL(_strURL + "/buscarTotalPlanilla.php?_fleteroID=" + idFletero +
                            "&_caja=" + caja +
                            "&_numeroPlanilla=" + planilla);

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


    private ArrayList<MoneyR> listarMonedas(String response) {
        ArrayList<MoneyR> lista = new ArrayList<MoneyR>();
        DecimalFormat format = new DecimalFormat("#0.00");
        double suma = 0;

        try {
            if (response != null) {
                JSONArray json = new JSONArray(response);
                for (int i = 0; i < json.length(); i++) {

                    MoneyR item = new MoneyR();
                    item.setId(json.getJSONObject(i).getInt("ID"));
                    item.setDetalle(json.getJSONObject(i).getString("DETALLE"));
                    item.setCantidad(Integer.parseInt(json.getJSONObject(i).getString("CANTIDAD").toString()));
                    item.setValor(Double.parseDouble(json.getJSONObject(i).getString("VALOR").toString()));
                    item.setSubTotal(Double.parseDouble(json.getJSONObject(i).getString("SUB_TOTAL").toString()));
                    suma = suma + item.getSubTotal();
                    lista.add(item);
                }
            }
            strTotal.setText(format.format(suma));
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

    class AdaptadorMonedas extends ArrayAdapter<MoneyR> {
        AdaptadorMonedas(List<MoneyR> listaTransComprobante) {
            super(RendicionFinalActivity.this, R.layout.comprobante_historico, arrayMoneR);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            DecimalFormat format = new DecimalFormat("##0.00");

            View item = convertView;
            item = getLayoutInflater().inflate(R.layout.monedas_rendicion, null);
            TextView strID = (TextView) item.findViewById(R.id.strMoneyR_ID);
            TextView strDetalle = (TextView) item.findViewById(R.id.strMoneyR_Detalle);
            TextView strCantidad = (TextView) item.findViewById(R.id.strMoneyR_Cantidad);
            TextView strValor = (TextView) item.findViewById(R.id.strMoneyR_Valor);
            TextView strSubTotal = (TextView) item.findViewById(R.id.strMoneyR_SubTotal);

            //no es necesario que el usuario sepa el ID de la moneda
            //strID.setText(String.valueOf("ID: "+arrayMoneR.get(position).getId()));

            strDetalle.setText(arrayMoneR.get(position).getDetalle());

            strCantidad.setText("Cantidad: " + arrayMoneR.get(position).getCantidad());
            strValor.setText("$: " + format.format(arrayMoneR.get(position).getValor()));
            strSubTotal.setText("Sub Total: " + format.format(arrayMoneR.get(position).getSubTotal()));
            return item;
        }


    }

    private void mostrarModeloMonedas() {
        if (arrayMoneR != null) {
            RendicionFinalActivity.AdaptadorMonedas adapter =
                    new RendicionFinalActivity.AdaptadorMonedas(arrayMoneR);
            listaMonedas.setAdapter(adapter);
            listaMonedas.setVisibility(View.VISIBLE);
        }
    }

    public int enviarDiasPOST(String idEmrpesa, String caja, String planilla) {
        String linea = "";
        int respuesta = 0;
        StringBuilder result = null;
        String idMovimiento = UUID.randomUUID().toString();

        try {
            if (verificaConexion(getApplicationContext())) {

                String urlParametros = "_idEmpresa=" + idEmrpesa + "&_caja=" + caja + "&_planilla=" + planilla;
                urlParametros = urlParametros.replace(",", ".");
                HttpURLConnection cnx = null;
                URL url = new URL(_strURL + "/insertarTrabaRendicion.php");
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

    public int obtDatosUnicoJSON(String result) {
        int res = 0;

        try {
            JSONArray json = new JSONArray(result);
            if (json.length() > 0) {
                res = 1;
            }
            for (int i = 0; i < json.length(); i++) {
                _idFletero = json.getJSONObject(i).getString("FLETERO_ID");
                totalARendir = json.getJSONObject(i).getString("TOTAL");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
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