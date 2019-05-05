package ar.com.idus.www.idusappshiping;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ar.com.idus.www.idusappshiping.modelos.Articulo;
import ar.com.idus.www.idusappshiping.modelos.Comprobante;
import ar.com.idus.www.idusappshiping.modelos.ComprobanteItem;
import ar.com.idus.www.idusappshiping.utilidades.AdaptadorArticulosDevolucion;

public class DevolucionParcialActivity extends AppCompatActivity {
    String _idEmpresa, _idVendedor, _strURL, _idCliente, _idFletero, nombCliente, comprobante;
    int _caja, _planilla;
    ArrayList<ComprobanteItem> listaItems;
    ArrayList<Articulo> listaArticulos;
    Articulo articulo;
    List<Articulo> articulos;
    TextView txtNombre, txtComprobante;
    protected ListView listaDetalle;
    //CheckBox checked;
    //EditText edCant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devolucion_parcial);
        txtNombre      = findViewById(R.id.strNombreClientePar);
        txtComprobante = findViewById(R.id.strCompPar);
        listaDetalle = findViewById(R.id.listArt);
        //edCant = findViewById(R.id.editCantidad);
        //checked = findViewById(R.id.checkArt);


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


            if(nombCliente.length() > 20){
                txtNombre.setTextSize(14);
            }

            convertirItems();
            mostrarListaItems();
            /*
            Thread tr = new Thread(){
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mostrarListaItems();
                        }
                    });
                }
            };

            tr.start();*/
        }

        System.out.println("despues de crear");

        /*listaDetalle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("eligiendo...");
                ComprobanteItem item = (ComprobanteItem) parent.getItemAtPosition(position);

                Toast.makeText(getApplicationContext(), "tocado", Toast.LENGTH_SHORT).show();

                //checked.setChecked(true);
                //edCant.setEnabled(true);
            }
        });*/

    }


    void convertirItems(){
        Iterator<ComprobanteItem> iterator = listaItems.iterator();
        ComprobanteItem comprobanteItem;
        articulos = new ArrayList<>();

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
        }


    }

    private void mostrarListaItems() {
        System.out.println("mostrando...");
        if (listaItems != null) {


            List <Articulo> pruebas;
            Articulo test;

            for(int i = 1; i <= 10; i ++){
                test = new Articulo();
                test.setCantidad(i);
                test.setElegido(false);
                test.setNombre("art " + i);
                test.setPrecioVenta(Double.valueOf(i*2));

            }

            AdaptadorArticulosDevolucion adapter = new AdaptadorArticulosDevolucion(this, articulos);
            listaDetalle.setAdapter(adapter);
            //listaDetalle.setClickable(true);
        }
    }

    /*public class AdaptadorArticulosDevolucion extends ArrayAdapter<ComprobanteItem> {
        public AdaptadorArticulosDevolucion(List<ComprobanteItem> lista) {
            super(DevolucionParcialActivity.this, R.layout.articulos_a_devolver, listaItems);
        }


        public View getView(int position, View convertView, ViewGroup parent) {

            DecimalFormat format = new DecimalFormat("#.00");
            String detalle = listaItems.get(position).getDetalle();
            double cantidad;
            int aux;

            System.out.println("en adaptador");

            View item = getLayoutInflater().inflate(R.layout.articulos_a_devolver, null);

            TextView strDetalle = (TextView) item.findViewById(R.id.lblDetalle);
            TextView strPrecioUnitario = (TextView) item.findViewById(R.id.lblPrecioUnitario);
            TextView strTotal = (TextView) item.findViewById(R.id.lblTotal);
            EditText edCant = item.findViewById(R.id.editCantidad);
            edCant.setEnabled(false);
            //CheckBox checked = item.findViewById(R.id.checkArt);

            //si el detalle es amplio se reduce el tamaño de la letra
            if (detalle.length() > 20) {
                strDetalle.setTextSize(11);
            }
            cantidad = listaItems.get(position).getCantidad();
            aux = (int) cantidad;
            strDetalle.setText(detalle);
            edCant.setText(String.valueOf(aux));
            //edCant.setEnabled(false);
            strPrecioUnitario.setText("PxUni: " + format.format(listaItems.get(position).getPrecioFinal()));
            strTotal.setText(format.format(listaItems.get(position).getTotal()));


            return item;
        }

    }*/
}
