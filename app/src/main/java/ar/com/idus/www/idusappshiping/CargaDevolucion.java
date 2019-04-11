package ar.com.idus.www.idusappshiping;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import ar.com.idus.www.idusappshiping.modelos.Articulo;


public class CargaDevolucion extends AppCompatActivity {

    TextView txtNombreCliente, txtDetalleArticulo, txtTotal, txtPrecio, txtMultiplo;
    EditText ediCodigo, ediCantidad, ediDescuento;
    ListView listaItem;
    ImageButton butBuscar;
    Button butAgregar, butLimpiar, butEnviar;


    String _idEmpresa, _idVendedor, _codCliente, _strURL, _idCliente, _nombreCliente, _domicilioCliente;
    //datos trans, articulos
    //String tIDArt,tCodArt,tDetArt,tPreArt,tMulArt;
    Articulo articulo;
    List<Articulo> articulos;

    //un array para los items
    ArrayList<String> items = new ArrayList<String>();
    //variable total
    double totalCuenta;
    //variable para precio original (se usa y se limpia por item)
    double _precioOriginal;
    //determino el decimal.
    DecimalFormat df = new DecimalFormat("#.00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carga_devolucion);

        _strURL = "http://idus-express-return.dnsalias.com/webserviceidusexpress";

        txtNombreCliente = (TextView) findViewById(R.id.txtNombreCliente);
        txtDetalleArticulo = (TextView) findViewById(R.id.txtDetalleArticulo);
        txtTotal = (TextView) findViewById(R.id.txtTotal);
        txtPrecio = (TextView) findViewById(R.id.txtPrecio);
        txtMultiplo = (TextView) findViewById(R.id.txtMultiplo);

        ediCodigo = (EditText) findViewById(R.id.ediCodigo);
        ediCantidad = (EditText) findViewById(R.id.ediCantidad);
        ediDescuento = (EditText) findViewById(R.id.ediDesc);

        butBuscar = (ImageButton) findViewById(R.id.imgButBuscar);
        butAgregar = (Button) findViewById(R.id.btnAgregar);
        butLimpiar = (Button) findViewById(R.id.btnLimpiar);
        butEnviar = (Button) findViewById(R.id.btnEnviar);


        listaItem = (ListView) findViewById(R.id.listItems);

        butAgregar.setActivated(false);
        butAgregar.setBackgroundResource(R.drawable.boton_deshabilitado);


        //inicializo la cuenta
        totalCuenta = 0.00;

        articulos = new ArrayList<>();

        Bundle recupera = getIntent().getExtras();
        if (recupera != null) {
            _idVendedor = recupera.getString("_idVendedor");
            _idEmpresa = recupera.getString("_idEmpresa");
            _codCliente = recupera.getString("_codCliente");
        }

        //busco el cliente
        Thread tr01 = new Thread() {
            @Override
            public void run() {
                final String resultado = enviarComandosGET(_idEmpresa, _idVendedor, _codCliente, "", "buscar.cliente");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String nom = procesarDatosCliente(resultado);
                        txtNombreCliente.setText(nom);
                    }
                });
            }
        };
        tr01.start();

        butBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread tr02 = new Thread() {
                    @Override
                    public void run() {
                        final String resul02 = enviarComandosGET(_idEmpresa, _idVendedor, _codCliente, ediCodigo.getText().toString().trim(), "buscar.articulo.x.codigo");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (resul02.equals("[]")) {
                                    Toast.makeText(getApplicationContext(), "No existe un articulo con ese ID",
                                            Toast.LENGTH_LONG).show();
                                    Limpiar("error");
                                } else {
                                    articulo = procesarBusquedaPorCodigo(resul02);

                                    if (articulo.getId() != null) {
                                        txtDetalleArticulo.setText(articulo.getNombre());
                                        //txtPrecio.setText(df.format(Double.valueOf(tPreArt.replace(",","."))));
                                        txtPrecio.setText(df.format(Double.valueOf(articulo.getPrecioVenta())));
                                        _precioOriginal = articulo.getPrecioVenta();
                                        //_precioOriginal=Double.valueOf(tPreArt.replace(",","."));

                                        txtMultiplo.setText(String.valueOf(articulo.getMultiplo()));

                                        butAgregar.setActivated(true);
                                        butAgregar.setBackgroundResource(R.drawable.boton_redondo_morado);
                                    } else {
                                        txtDetalleArticulo.setText("");
                                        txtPrecio.setText("");
                                        _precioOriginal = 0.00;
                                        txtMultiplo.setText("");
                                    }
                                }

                            }
                        });

                    }
                };
                tr02.start();
            }
        });

        butLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Limpiar("nuevo");
            }
        });

        butAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!butAgregar.isActivated()){
                    Toast.makeText(getApplicationContext(),
                            "No puedes agregar un articulo hasta completar los campos correspondientes",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    try {
                        String desc, cant, codigo;
                        desc = ediDescuento.getText().toString();
                        cant = ediCantidad.getText().toString();
                        codigo = ediCodigo.getText().toString();

                        if (codigo.isEmpty()){
                            Toast.makeText(getApplicationContext(), "Primero debes colocar un codigo",
                                    Toast.LENGTH_LONG).show();
                        }
                        else if (desc.isEmpty() || cant.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Debes colocar una cantidad y un descuento",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            final double _desc = Double.valueOf(desc.replace(",", "."));
                            int cantidad = Integer.valueOf(cant);
                            int resto;

                            if (_desc >= 0 && _desc <= 100) {
                                articulo.setDescuento(_desc);
                                articulo.setPrecioConDesc(_precioOriginal * (1 - (_desc / 100)));
                                if (cantidad > 0) {
                                    int multiplo = Integer.valueOf(txtMultiplo.getText().toString());

                                    articulo.setMultiplo(multiplo);
                                    articulo.setCantidad(cantidad);
                                    resto = cantidad % multiplo;
                                    if (resto == 0) {
                                        final double _pxOriginal = _precioOriginal;
                                        final double operacion = articulo.getPrecioConDesc() * cantidad;
                                        Thread trArt = new Thread() {
                                            @Override
                                            public void run() {
                                                final String texto = "Art: " + articulo.getCodigo() + " - " + articulo.getNombre() + " \n" +
                                                        "Cant: " + ediCantidad.getText().toString() +
                                                        " - Precio Unitario: $" + df.format(_pxOriginal) + " \n" +
                                                        "% Desc: " + df.format(_desc) +
                                                        " - Precio con Desc: $" + df.format(articulo.getPrecioConDesc()) + " \n" +
                                                        "SubTotal: $" + df.format(operacion);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        items.add(texto);
                                                        cargarItem(items);
                                                        totalCuenta = totalCuenta + operacion;
                                                        txtTotal.setText(String.valueOf(df.format(totalCuenta)));
                                                        Limpiar("nuevo");
                                                        ediCodigo.requestFocus();
                                                    }
                                                });
                                            }
                                        };
                                        trArt.start();
                                    } else {
                                        Toast toast1 = Toast.makeText(getApplicationContext(), R.string.mensajeMutiplo, Toast.LENGTH_LONG);
                                        toast1.show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Debes ingresar un numero entero como cantidad"
                                            , Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.descuentoNoAplicado, Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        listaItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;

                AlertDialog.Builder dialogo = new AlertDialog.Builder(CargaDevolucion.this);
                dialogo.setTitle(R.string.tituloImportamte)
                        .setMessage(R.string.mensajeBorrado)
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                items.remove(pos);
                                articulos.remove(pos);

                                //limpio la varible de total
                                totalCuenta = 0.00;

                                //calculo nuevamente el total
                                Iterator<Articulo> iterator = articulos.iterator();
                                while (iterator.hasNext()) {
                                    Articulo item = iterator.next();
                                    totalCuenta = totalCuenta + item.getPrecioConDesc() * item.getCantidad();
                                }

                                txtTotal.setText(df.format(totalCuenta));
                                cargarItem(items);
                            }
                        })
                        .setNegativeButton(R.string.rechazar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

            }
        });

        butEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AlertDialog.Builder dialogo = new AlertDialog.Builder(CargaDevolucion.this);
                    dialogo.setTitle(R.string.tituloImportamte)
                            .setMessage(R.string.mensajeDeEnvio)
                            .setCancelable(false)
                            .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Thread thread = new Thread() {
                                        @Override
                                        public void run() {
                                            butEnviar.setClickable(false);
                                            double _total = Double.valueOf(txtTotal.getText().toString().replace(",", "."));
                                            final boolean res = insertarDevolucion(_idEmpresa, _idVendedor, _idCliente, _total);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (res) {
                                                        Limpiar("todo");
                                                        Toast toastEnviar = Toast.makeText(CargaDevolucion.this, R.string.mensajeInsercionOK, Toast.LENGTH_LONG);
                                                        toastEnviar.show();
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    };
                                    thread.start();
                                }
                            })
                            .setNegativeButton(R.string.rechazar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    public String enviarComandosGET(String idEmpresa, String idVendedor, String codCliente, String codArticulo, String accion) {
        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuffer result = null;

        HttpURLConnection cnx = null;
        try {

            if (accion.equals("buscar.cliente")) {
                url = new URL(_strURL + "/buscar_cliente_x_codigo.php?_codcliente=" + codCliente.trim() + "&_idempresa=" + idEmpresa);
            } else if (accion.equals("buscar.articulo.x.codigo")) {
                url = new URL(_strURL + "/buscar_articulo_x_codigo.php?_codarticulo=" + codArticulo + "&_idempresa=" + idEmpresa);
            }

            cnx = (HttpURLConnection) url.openConnection();
            respuesta = cnx.getResponseCode();
            result = new StringBuffer();
            if (respuesta == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(cnx.getInputStream());
                BufferedReader leer = new BufferedReader(new InputStreamReader(in));

                while ((linea = leer.readLine()) != null) {
                    result.append(linea);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result.toString();
    }

    public String procesarDatosCliente(String response) {

        try {
            JSONArray json = new JSONArray(response);
            for (int i = 0; i < json.length(); i++) {
                _idCliente = json.getJSONObject(i).getString("ID");
                _nombreCliente = json.getJSONObject(i).getString("NOMBRE");
                _domicilioCliente = json.getJSONObject(i).getString("DOMICILIO");
                _codCliente = json.getJSONObject(i).getString("CODIGO");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return _nombreCliente;
    }

    public Articulo procesarBusquedaPorCodigo(String response) {
        Articulo articulo = new Articulo();

        try {
            JSONArray json = new JSONArray(response);
            for (int i = 0; i < json.length(); i++) {
                articulo.setId(json.getJSONObject(i).getString("ID"));
                articulo.setCodigo(json.getJSONObject(i).getString("CODIGO"));
                articulo.setPrecioVenta(Double.parseDouble(json.getJSONObject(i).getString("PRECIOVENTA")));
                articulo.setExistencia(Integer.parseInt(json.getJSONObject(i).getString("EXISTENCIA")));
                articulo.setMultiplo(Integer.parseInt(json.getJSONObject(i).getString("MULTIPLO")));
                articulo.setNombre(json.getJSONObject(i).getString("NOMBRE"));

                if (articulo.getMultiplo() == 0) {
                    articulo.setMultiplo(1);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        articulos.add(articulo);
        return articulo;
    }

    private void cargarItem(ArrayList<String> datos) {
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, datos);
        listaItem.setAdapter(adaptador);
    }

    public boolean insertarDevolucion(String idEmpresa, String idVendedor, String idCliente, double total) {
        String id = UUID.randomUUID().toString();
        boolean resultado = false;

        if ((_idCliente == null) || (_idVendedor == null) || (idEmpresa == null)) {
            Toast.makeText(getApplicationContext(), R.string.errorNulo, Toast.LENGTH_LONG).show();

        } else {
            String urlParametros = "_id=" + id + "&_idempresa=" + idEmpresa + "&_idvendedor=" + _idVendedor + "&_idcliente=" + _idCliente + "&_total=" + total;
            urlParametros = urlParametros.replace(",", ".");
            HttpURLConnection cnx = null;

            try {
                URL url = new URL(_strURL + "/insertar_devolucion.php");
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
                int respuesta = cnx.getResponseCode();
                //comienzo a grabar los items
                if (respuesta == HttpURLConnection.HTTP_OK) {
                    //Iterator it = items.iterator();
                    Iterator<Articulo> it = articulos.iterator();
                    while (it.hasNext()) {
                        String idItem = UUID.randomUUID().toString();
                        String idArticulo, cantidad, subTotal, pxUni, Desc, precDesc;
                        //String[] item = it.next().toString().split("~");
                        Articulo art = (Articulo) it.next();
                        idArticulo = art.getId();
                        cantidad = String.valueOf(art.getCantidad());
                        pxUni = String.valueOf(art.getPrecioVenta());
                        precDesc = String.valueOf(art.getPrecioConDesc());
                        Desc = String.valueOf(art.getDescuento());
                        subTotal = String.valueOf(art.getCantidad() * art.getPrecioConDesc());


                        urlParametros = "_idcab=" + id + "&_id=" + idItem + "&_idarticulo=" + idArticulo + "&_cantidad=" + cantidad
                                +"&_subtotal=" + subTotal + "&_pxuni=" + pxUni +"&_prdesc="+ precDesc
                                +"&_pxcdesc="+ Desc;
                        urlParametros=urlParametros.replace(",",".");
                        URL url2 = new URL(_strURL + "/insertar_cuerpo_devolucion.php");
                        cnx = (HttpURLConnection) url2.openConnection();
                        cnx.setRequestMethod("POST");
                        cnx.setRequestProperty("Context-length", "" + Integer.toString(urlParametros.getBytes().length));
                        cnx.setDoOutput(true);

                        DataOutputStream wr1 = new DataOutputStream(cnx.getOutputStream());
                        wr1.writeBytes(urlParametros);
                        wr1.close();

                        InputStream in1 = cnx.getInputStream();
                        int respuesta1 = cnx.getResponseCode();
                        if (respuesta1 == HttpURLConnection.HTTP_OK) {
                            resultado = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //return true;
        return resultado;
    }

    public void Limpiar(String accion) {
        butAgregar.setActivated(false);
        butAgregar.setBackgroundResource(R.drawable.boton_deshabilitado);

        if (accion.equals("nuevo")) {
            ediCodigo.setText("");
        } else if (accion.equals("todo")) {
            ediCodigo.setText("");
            txtTotal.setText("");
            items.clear();
            cargarItem(items);
        } else if (accion.equals("error")) {
            //no se hace nada extra
        }

        ediCantidad.setText("");
        ediDescuento.setText("");
        txtPrecio.setText("");
        txtMultiplo.setText("");
        txtDetalleArticulo.setText("");
        _precioOriginal = 0.00;
    }
}
