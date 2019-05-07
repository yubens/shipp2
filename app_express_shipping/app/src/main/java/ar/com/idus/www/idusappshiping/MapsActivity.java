package ar.com.idus.www.idusappshiping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ar.com.idus.www.idusappshiping.modelos.Comprobante;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ArrayList<Comprobante> lvComprobante;
    private String _strUrl;
    private String _idEmpresa, _idFletero, _caja, _planilla, _codigoEmpresa;
    private LinearLayout lLeyDatos;
    private Button btnIr, btnCancelar;
    private TextView strNombre, strDomicilio, strLat, strLon, strDomgoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //para pruebas en emulador
        /*MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);*/

        mapFragment.getMapAsync(this);
        strNombre = (TextView) findViewById(R.id.strMapNombreCliente);
        strDomicilio = (TextView) findViewById(R.id.strMapDomicilio);
        strDomgoogle = (TextView) findViewById(R.id.strMapDireccionGoogle);
        strLat = (TextView) findViewById(R.id.strMapLat);
        strLon = (TextView) findViewById(R.id.strMapLon);

        Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            _strUrl = recupera.getString("_strURL");
            _idEmpresa = recupera.getString("_idEmpresa");
            _idFletero = recupera.getString("_ifFletero");
            lvComprobante = (ArrayList<Comprobante>) recupera.getSerializable("listaComprobante");
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        LatLng geoCli = null;
        for (int i = 0; i <= lvComprobante.size() - 1; i++) {
            Comprobante comp = (Comprobante) lvComprobante.get(i);
            if ((comp.getClienteLatitud().equals("null")) && (comp.getClienteLongitud().equals("null"))) {

            } else {
                geoCli = new LatLng(Double.parseDouble(comp.getClienteLatitud().toString()),
                        Double.parseDouble(comp.getClienteLongitud().toString()));
                if (comp.getMhr() == 0) {
                    mMap.addMarker(new MarkerOptions().position(geoCli)
                            .title(comp.getNombreCliente().toString())
                            .snippet(comp.getDomicilioCliente().toString())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.entrega_luiza48)));
                } else {
                    mMap.addMarker(new MarkerOptions().position(geoCli).title(comp.getNombreCliente())
                            .snippet(comp.getDomicilioCliente().toString())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.cobranza48)));
                }
            }
            if (geoCli != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoCli, 15));
            }
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.strMensajeDeNoPermisos, Toast.LENGTH_LONG);
                toast.show();
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerClickListener(this);
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> direcciones = null;
        LatLng geo = (LatLng) marker.getPosition();
        if (geo != null) {
            strNombre.setText("Nombre: " + marker.getTitle());
            strDomicilio.setText("Domicilio IDUS: " + marker.getSnippet());
            strDomgoogle.setText("");
            strLat.setText(String.valueOf(geo.latitude));
            strLon.setText(String.valueOf(geo.longitude));
            try {
                direcciones = geocoder.getFromLocation(geo.latitude, geo.longitude, 1);
                if (direcciones != null) {
                    Address direccion = direcciones.get(0);
                    String direccionText = String.format("%s, %s, %s, %s",
                            direccion.getThoroughfare(),
                            direccion.getSubThoroughfare(),
                            direccion.getLocality(),
                            direccion.getCountryName());
                    strDomgoogle.setText("Domicilio Google: " + direccionText);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}