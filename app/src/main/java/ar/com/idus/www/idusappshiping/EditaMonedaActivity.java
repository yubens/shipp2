package ar.com.idus.www.idusappshiping;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.UUID;

import ar.com.idus.www.idusappshiping.modelos.MoneyR;

public class EditaMonedaActivity extends AppCompatActivity {

    private TextView strId, strDetalle, strValor, strSubTotal;
    private EditText ediCantidad;
    private Button btnAceptar, btnCancelar;
    private String _idEmpresa, _idFletero, _strUrl;
    private MoneyR moneda;
    private ProgressBar pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edita_moneda);

        strDetalle = (TextView) findViewById(R.id.strEM_Detv);
        strValor = (TextView) findViewById(R.id.strEM_Valorv);
        strSubTotal = (TextView) findViewById(R.id.strEM_SubTotalv);
        btnAceptar = (Button) findViewById(R.id.btnEMAceptar);
        btnCancelar = (Button) findViewById(R.id.btnEMCancelar);
        ediCantidad = (EditText) findViewById(R.id.ediEM_Cant);
        pBar = (ProgressBar) findViewById(R.id.pBar_EM);

        Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            moneda = (MoneyR) recupera.getSerializable("itemMoneda");
            _idEmpresa = recupera.getString("_idEmpresa");
            _idFletero = recupera.getString("idFletero");
            _strUrl = recupera.getString("_strURL");
            if (moneda != null) {
                DecimalFormat format = new DecimalFormat("#0.00");

                strDetalle.setText(moneda.getDetalle());
                strValor.setText(format.format(moneda.getValor()));
                strSubTotal.setText(format.format(moneda.getSubTotal()));
            }
        }

        ediCantidad.addTextChangedListener(textWa);

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pBar.setVisibility(View.VISIBLE);

                if(ediCantidad.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Coloca una cantidad para continuar",
                            Toast.LENGTH_SHORT).show();
                    pBar.setVisibility(View.GONE);
                }
                else{
                    Thread tr = new Thread() {
                        boolean cantVacia = false;
                        @Override
                        public void run() {

                            final int response = enviarDiasPOST(moneda.getId(), Double.parseDouble(ediCantidad.getText().toString()));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String msj;
                                    if (response == 200) {
                                        msj = getApplicationContext().getString(R.string.strExitoMoneda);
                                    }
                                    else{
                                        msj = getApplicationContext().getString((R.string.strErrorMoneda));
                                    }

                                    pBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), msj, Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        }

                    };
                    tr.start();
                }

            }
        });

    }

    private TextWatcher textWa = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            double importe, resultado;
            DecimalFormat format = new DecimalFormat("#0.00");
            importe = 0;
            resultado = 0;
            if (s.length() > 0) {
                importe = Double.parseDouble(s.toString());
                resultado = moneda.getValor() * importe;
                strSubTotal.setText(format.format(resultado));
            }
            System.out.println("cambiando...");

            if(ediCantidad.equals("")){
                strSubTotal.setText("");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public int enviarDiasPOST(int idMoneda, double cantidad) {
        String linea = "";
        int respuesta = 0;
        StringBuilder result = null;
        String idMovimiento = UUID.randomUUID().toString();

        try {
            if (verificaConexion(getApplicationContext())) {

                String urlParametros = "_idMoneda=" + idMoneda + "&_cantidad=" + cantidad;
                urlParametros = urlParametros.replace(",", ".");
                HttpURLConnection cnx = null;
                URL url = new URL(_strUrl + "/insertarCantidadMoneda.php");
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
