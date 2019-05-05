package ar.com.idus.www.idusappshiping;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PrincipalActivity extends AppCompatActivity {

    TextView strNombreEmpresa, strNombreFletero;
    EditText txtCaja, txtPlanilla;
    String _strUrl, _idEmpresa, _idFletero, _codigoEmpresa, planilla, caja;
    Button btnAceptar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        strNombreEmpresa = (TextView) findViewById(R.id.strNombreEmpresa);
        strNombreFletero = (TextView) findViewById(R.id.strNombreFletero);
        txtCaja = (EditText) findViewById(R.id.txtCaja);
        txtPlanilla = (EditText) findViewById(R.id.txtPlanilla);
        btnAceptar = (Button) findViewById(R.id.btnAceptarPlanilla);
        _strUrl = "";
        Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            strNombreEmpresa.setText(recupera.getString("nombreEmpresa"));
            strNombreFletero.setText(recupera.getString("nombreFletero"));
            _strUrl = recupera.getString("_strURL");
            _idEmpresa = recupera.getString("_idEmpresa");
            _idFletero = recupera.getString("_idFletero");
            _codigoEmpresa = recupera.getString("_codigoEmpresa");

        } else {
            strNombreFletero.setText("No se pudo identificar al fletero");
        }
        ActivityCompat.requestPermissions(PrincipalActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //obtenemos los datos
                planilla = txtPlanilla.getText().toString();
                caja = txtCaja.getText().toString();

                //corroborar que los datos no esten vacios
                if (caja.isEmpty() || planilla.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ambos datos deben completarse",
                            Toast.LENGTH_LONG).show();
                } else {

                    //comprobar que los datos sean numero enteros
                    if (esEntero(caja) && esEntero(planilla)) {
                        Intent i = new Intent(getApplicationContext(), ListarComprobantesActivity.class);
                        i.putExtra("_idEmpresa", _idEmpresa);
                        i.putExtra("_idFletero", _idFletero);
                        i.putExtra("_strURL", _strUrl);
                        i.putExtra("_caja", caja);
                        i.putExtra("_planilla", planilla);
                        i.putExtra("_codigoEmpresa", _codigoEmpresa);
                        i.putExtra("_nombreFletero", strNombreFletero.getText().toString());
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "Debes ingresar números enteros",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    boolean esEntero(String numero) {
        try {
            Integer.parseInt(numero);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
