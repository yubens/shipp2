package ar.com.idus.www.idusappshiping;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ar.com.idus.www.idusappshiping.modelos.Comprobante;
import ar.com.idus.www.idusappshiping.signature.SignatureMainLayout;

public class FirmarActivity extends AppCompatActivity {

    public Comprobante comprobante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            comprobante = (Comprobante) recupera.getSerializable("_comprobante");
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(new SignatureMainLayout(this, comprobante));
    }
}
