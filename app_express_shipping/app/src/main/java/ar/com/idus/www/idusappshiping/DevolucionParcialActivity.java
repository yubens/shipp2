package ar.com.idus.www.idusappshiping;

import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ar.com.idus.www.idusappshiping.modelos.Articulo;
import ar.com.idus.www.idusappshiping.modelos.ComprobanteItem;
import ar.com.idus.www.idusappshiping.utilidades.AdaptadorDevolucionParcial;
import ar.com.idus.www.idusappshiping.utilidades.Devoluciones;

public class DevolucionParcialActivity extends AppCompatActivity {
    String _idEmpresa, _idVendedor, _strURL, _idCliente, _idFletero, nombCliente, comprobante;
    int _caja, _planilla;
    ArrayList<ComprobanteItem> listaItems;
    ArrayList<Articulo> listaArticulos;
    //List <Articulo> pruebas;
    Articulo articulo;
    List<Articulo> articulos;
    List <Articulo> chequeados;
    TextView txtNombre, txtComprobante;
    Button btnEnviar;
    protected ListView listaDetalle;
    List <Integer> cantOrig;
    //CheckBox checked;
    //EditText edCant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devolucion_parcial);
        txtNombre      = findViewById(R.id.strNombreClientePar);
        txtComprobante = findViewById(R.id.strCompPar);
        listaDetalle = findViewById(R.id.listArt);
        btnEnviar = findViewById(R.id.btnEnviarPar);

        @SuppressWarnings("unchecked")
        Bundle recupera = getIntent().getExtras();

        if (recupera != null) {
            _idVendedor = recupera.getString("_idVendedor");
            _idEmpresa = recupera.getString("_idEmpresa");
            _idCliente = recupera.getString("_idCliente");
            _caja = recupera.getInt("_caja");
            _planilla = recupera.getInt("_planilla");
            _idFletero = recupera.getString("_idFletero");

            listaItems = (ArrayList<ComprobanteItem>) recupera.getSerializable("_lista");
            nombCliente = recupera.getString("_nombCliente");
            comprobante = recupera.getString("_comprobante");

            txtNombre.setText(nombCliente);
            txtComprobante.setText(comprobante);

            cantOrig = new ArrayList<>();

            if(nombCliente.length() > 20){
                txtNombre.setTextSize(14);
            }

            convertirItems();
        }

        AdaptadorDevolucionParcial adapter = new AdaptadorDevolucionParcial(this, articulos, cantOrig);
        listaDetalle.setAdapter(adapter);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alerta;

                int elegidos = verElegidos();

                if(elegidos >0 ){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        alerta = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
                    }

                    alerta.setTitle(R.string.tituloImportamte);
                    alerta.setMessage(R.string.alertaDevolParcial);
                    alerta.setCancelable(false);
                    alerta.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            devolver();
                        }
                    }).setNegativeButton(R.string.rechazar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Debes elegir al menos un articulo para devolver",
                                    Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public int verElegidos(){
        chequeados = new ArrayList<>();

        for (int i = 0; i < articulos.size(); i++) {
            if(articulos.get(i).isElegido()){
                chequeados.add(articulos.get(i));
            }
        }

        return chequeados.size();
    }

    public void devolver(){
        Thread tr = new Thread(){

            @Override
            public void run(){
                final boolean res = prepararDevolucion();
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
        tr.start();

    }

    public boolean prepararDevolucion(){
       Double total = 0.0;

        if ((_idCliente == null) || (_idVendedor == null) || (_idEmpresa == null)) {
            Toast.makeText(getApplicationContext(), R.string.errorNulo, Toast.LENGTH_LONG).show();
            finish();
        }

        for(int i = 0; i < chequeados.size(); i++){
            total = total + chequeados.get(i).getCantidad() * chequeados.get(i).getPrecioVenta();
        }

        return Devoluciones.insertarDevolucion(_idEmpresa, _idVendedor, _idCliente, _idFletero,
                                                    total, _caja, _planilla, chequeados);
    }

    void convertirItems(){
        Iterator<ComprobanteItem> iterator = listaItems.iterator();
        ComprobanteItem comprobanteItem;
        articulos = new ArrayList<>();

        //transforma los los comprobantesItem en articulos y guarda las cantidades originales
        while (iterator.hasNext()){
            articulo = new Articulo();
            comprobanteItem = iterator.next();

            articulo.setId(comprobanteItem.getIdArticulo());
            articulo.setCantidad(comprobanteItem.getCantidad());
            articulo.setPrecioVenta(comprobanteItem.getPrecioFinal());
            articulo.setPrecioConDesc(comprobanteItem.getPrecioFinal());
            articulo.setNombre(comprobanteItem.getDetalle());
            articulo.setDescuento(0.0);
            articulo.setElegido(false);

            articulos.add(articulo);
            cantOrig.add((int)articulo.getCantidad());
        }


    }

    /*private void mostrarListaItems() {
        System.out.println("mostrando...");
        if (listaItems != null) {

            pruebas = new ArrayList<Articulo>();
            List <Integer> cantidades = new ArrayList<>();

            for(int i = 1; i <= 20; i ++){
                Articulo test = new Articulo();
                test.setId(String .valueOf(i));
                test.setCantidad((int) (Math.random()* i + 1));
                test.setElegido(false);
                test.setNombre("art " + i);
                test.setPrecioVenta(Double.valueOf(Math.random() *i + 2));
                pruebas.add(test);
                //cantidades.add((int)test.getCantidad());
                cantidades.add((int) test.getCantidad());

            }


            AdaptadorDevolucionParcial adapter = new AdaptadorDevolucionParcial(this, pruebas, cantidades);
            listaDetalle.setAdapter(adapter);
            //listaDetalle.setClickable(true);
        }
    }*/



}
