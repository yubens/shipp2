package ar.com.idus.www.idusappshiping.utilidades;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import ar.com.idus.www.idusappshiping.modelos.Articulo;
import ar.com.idus.www.idusappshiping.modelos.ComprobanteItem;

public abstract class Devoluciones {

    public static boolean insertarDevolucion(String idEmp, String idVend, String idClie, String idFlet,
                                             double total, int caja, int plani, List<Articulo> listaItems){
        String id = UUID.randomUUID().toString();
        String _strURLDev = "http://idus-express-return.dnsalias.com/webserviceidusexpress";
        boolean resultado = false;

        String urlParametros = "_id=" + id + "&_idempresa=" + idEmp + "&_idvendedor=" + idVend + "&_idcliente=" + idClie + "&_total=" + total
                + "&_idFletero=" + idFlet + "&_codigoCaja=" + caja + "&_planilla=" + plani;
        urlParametros = urlParametros.replace(",", ".");
        HttpURLConnection cnx = null;

        try {
            URL url = new URL(_strURLDev + "/insertar_devolucion.php");
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
                Iterator<Articulo> itemsIterator = listaItems.iterator();

                while (itemsIterator.hasNext()) {
                    String idItem = UUID.randomUUID().toString();
                    String idArticulo, cantidad, subTotal, pxUni, Desc, precDesc;

                    Articulo item = (Articulo) itemsIterator.next();
                    idArticulo = item.getId();
                    cantidad = String.valueOf(item.getCantidad());
                    pxUni = String.valueOf(item.getPrecioVenta());
                    precDesc = String.valueOf(item.getPrecioConDesc());
                    Desc = String.valueOf(item.getDescuento());
                    subTotal = String.valueOf(item.getCantidad() * item.getPrecioConDesc());

                    urlParametros = "_idcab=" + id + "&_id=" + idItem + "&_idarticulo=" + idArticulo + "&_cantidad=" + cantidad
                            +"&_subtotal=" + subTotal + "&_pxuni=" + pxUni +"&_prdesc="+ precDesc
                            +"&_pxcdesc="+ Desc;
                    urlParametros=urlParametros.replace(",",".");
                    URL url2 = new URL(_strURLDev + "/insertar_cuerpo_devolucion.php");
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

        return resultado;
    }
}
