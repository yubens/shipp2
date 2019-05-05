package ar.com.idus.www.idusappshiping;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button btnIngresar;
    ImageButton btnFacebook, btnTwitter, btnWeb, btnWhat, btnInsta, btnYout;
    EditText txtUsuario, txtPassword;
    TextView txtMensaje;
    ProgressBar pb00;
    String strURL = "http://idus-express-shiping.dnsalias.com/WebServiceIdusShiping";
    //    String strURL="http://rrios-casa.from-ar.com/WebServiceIdusShiping";
//    String strURL="http://192.168.0.208/WebServiceIdusShiping";
//    String strURL="http://190.15.192.28/WebServiceIdusShiping";
    String _idFletero, _idEmpresa, nombreFletero, nombreEmpresa, _codigoEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        btnIngresar = (Button) findViewById(R.id.btnIngresar);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnTwitter = findViewById(R.id.btnTwitter);
        btnWeb = findViewById(R.id.btnWeb);
        btnWhat = findViewById(R.id.btnWhat);
        btnInsta = findViewById(R.id.btnInsta);
        btnYout = findViewById(R.id.btnYout);
        txtUsuario = (EditText) findViewById(R.id.txtUsuario);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtMensaje = (TextView) findViewById(R.id.teMensaje);
        pb00 = (ProgressBar) findViewById(R.id.pb00);
        pb00.setVisibility(View.GONE);

        txtMensaje.setText("Versión :" + BuildConfig.VERSION_NAME);

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user, pass;

                //obtenemos los datos
                user = txtUsuario.getText().toString();
                pass = txtPassword.getText().toString();

                //corroborar que los datos no esten vacios
                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ambos datos deben completarse",
                            Toast.LENGTH_LONG).show();
                } else {
                    pb00.setVisibility(View.VISIBLE);
                    txtMensaje.setText("Intentando conectar al servidor IDUS®");

                    Thread tr = new Thread() {
                        @Override
                        public void run() {
                            final String resultado = enviarDatosGET(txtUsuario.getText().toString(), txtPassword.getText().toString(), getApplicationContext());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int r = obtDatosUnicoJSON(resultado);
                                    if (r > 0) {
                                        pb00.setVisibility(View.GONE);
                                        txtMensaje.setText("");
                                        Intent i = new Intent(getApplicationContext(), PrincipalActivity.class);
                                        i.putExtra("_idEmpresa", _idEmpresa);
                                        i.putExtra("_idFletero", _idFletero);
                                        i.putExtra("nombreEmpresa", nombreEmpresa);
                                        i.putExtra("nombreFletero", nombreFletero);
                                        i.putExtra("_strURL", strURL);
                                        i.putExtra("_codigoEmpresa", _codigoEmpresa);
                                        startActivity(i);
                                    } else {
                                        pb00.setVisibility(View.GONE);
                                        txtMensaje.setText("Posiblemente no se puede conectar al servidor IDUS®");
                                        Toast toast = Toast.makeText(getApplicationContext(), R.string.msgUsuarioIncorrecto, Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                }
                            });
                        }
                    };
                    tr.start();
                }


            }
        });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/100000282883664"));
                    startActivity(intent);
                } catch(Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/yubens81")));
                }
                */

                startActivity(
                        newFacebookIntent(getPackageManager(), "https://www.facebook.com/idusApp")
                );
            }
        });

        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("https://twitter.com/r77rios");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
            }
        });

        btnWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Uri webpage = Uri.parse("https://web.whatsapp.com/send?text=&phone=+5426122087997&abid=+5426122087997");
                Uri webpage = Uri.parse("https://wa.me/542612087997");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
            }
        });

        btnInsta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri webpage = Uri.parse("https://www.instagram.com/idusmdz/");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);

            }
        });
        btnYout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("https://www.youtube.com/channel/UCXeBlnaTTHXCQ56cH9fL5Pw");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
            }
        });


        btnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("https://idus.com.ar");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
            }
        });

    }

    //necesario para abrir la app de facebook
    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public String enviarDatosGET(String _username, String _password, Context context) {
        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder result = null;

        try {
            if (verificaConexion(getApplicationContext())) {
                url = new URL(strURL + "/validarUsuario.php?_username=" + _username + "&_password=" + _password);
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

                return result.toString();

            } else {
                return "";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public int obtDatosUnicoJSON(String result) {
        int res = 0;

        try {
            JSONArray json = new JSONArray(result);
            if (json.length() > 0) {
                res = 1;
            }
            for (int i = 0; i < json.length(); i++) {
                _codigoEmpresa = json.getJSONObject(i).getString("CODIGO_EMPRESA");
                _idFletero = json.getJSONObject(i).getString("ID_FLETERO");
                _idEmpresa = json.getJSONObject(i).getString("ID_EMPRESA");
                nombreFletero = json.getJSONObject(i).getString("NOMBRE_FLETERO");
                nombreEmpresa = json.getJSONObject(i).getString("NOMBRE_EMPRESA");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static boolean verificaConexion(Context ctx) {
        boolean bConectado = false;
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(ctx, R.string.msgErrInternet, Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
    }
}
