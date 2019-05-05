package ar.com.idus.www.idusappshiping.utilidades;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

import ar.com.idus.www.idusappshiping.DevolucionParcialActivity;
import ar.com.idus.www.idusappshiping.R;
import ar.com.idus.www.idusappshiping.modelos.Articulo;
import ar.com.idus.www.idusappshiping.modelos.ComprobanteItem;

public class AdaptadorArticulosDevolucion extends ArrayAdapter<Articulo> {
    private final List<Articulo> lista;
    private final Activity context;
    int contador = 0;

    public AdaptadorArticulosDevolucion(Activity context, List<Articulo> lista) {
        super(context, R.layout.articulos_a_devolver, lista);
        this.context = context;
        this.lista = lista;
    }

    static class ViewHolder
    {
        protected CheckBox checkElegido;
        protected EditText edCantidad;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        double cantidad;
        final int cantAux, cantOriginal;
        final Articulo articulo;
        final ViewHolder viewHolder;

        cantOriginal = (int) lista.get(position).getCantidad();


        System.out.println("en adaptador " + contador++);

        if(convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.articulos_a_devolver, null);
            viewHolder = new ViewHolder();

            TextView strDetalle = view.findViewById(R.id.lblDetalle);
            TextView strPrecioUnitario = view.findViewById(R.id.lblPrecioUnitario);
            final TextView strTotal = view.findViewById(R.id.lblTotal);
            //EditText edCant = view.findViewById(R.id.editCantidad);
            viewHolder.checkElegido = view.findViewById(R.id.checkArt);
            viewHolder.edCantidad = view.findViewById(R.id.editCantidad);
            //lista.get(position).setElegido(true);

            DecimalFormat format = new DecimalFormat("#.00");
            String detalle = lista.get(position).getNombre();

            System.out.println("view null ");

            //CheckBox checked = item.findViewById(R.id.checkArt);

            //si el detalle es amplio se reduce el tamaño de la letra
            if (detalle.length() > 20) {
                strDetalle.setTextSize(11);
            }
            cantidad = lista.get(position).getCantidad();
            cantAux = (int) cantidad;
            strDetalle.setText(detalle);
            viewHolder.edCantidad.setText(String.valueOf(cantAux));
            viewHolder.edCantidad.setEnabled(false);
            strPrecioUnitario.setText("PxUni: " + format.format(lista.get(position).getPrecioVenta()));
            strTotal.setText(format.format(lista.get(position).getPrecioVenta() * cantidad));

            viewHolder.edCantidad.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    DecimalFormat format = new DecimalFormat("#0.00");
                    double cantidad, precio;
                    int cantAux;

                    System.out.println("cambiando...");

                    if(s.length() > 0){
                        System.out.println("cambiando...");
                        cantidad = Double.valueOf(s.toString());
                        cantAux = (int) cantidad;

                        if(cantAux == 0){
                            Toast.makeText(getContext(), "No puedes colocar una cantidad de cero (0)",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(cantAux > cantOriginal){
                            Toast.makeText(getContext(), "No puedes colocar una cantidad mayor a la original",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Articulo articulo = (Articulo) viewHolder.edCantidad.getTag();
                        //articulo = lista.get(position);

                        //lista.get(position).setCantidad(cantidad);
                        articulo.setCantidad(cantidad);
                        precio = articulo.getPrecioVenta();
                        strTotal.setText(format.format(precio * cantAux));

                    }

                    if(viewHolder.edCantidad.equals("")){
                        strTotal.setText("");
                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            viewHolder.checkElegido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    System.out.println("checkbox 1");
                    Articulo articulo = (Articulo) viewHolder.checkElegido.getTag();
                    //articulo = lista.get(position);
                    articulo.setElegido(buttonView.isChecked());
                    viewHolder.edCantidad.setEnabled(buttonView.isChecked());
                }
            });

            /*
            viewHolder.checkElegido.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("checkbox 2");
                    //Articulo articulo = (Articulo) viewHolder.checkElegido.getTag();
                    Articulo articulo = lista.get(position);

                    articulo.setElegido(viewHolder.checkElegido.isChecked());
                    viewHolder.edCantidad.setEnabled(viewHolder.checkElegido.isChecked());
                }
            });*/

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            System.out.println("view con dato ");
            view = convertView;
            //((ViewHolder) view.getTag()).checkElegido.setTag(lista.get(position));
            //((ViewHolder) view.getTag()).edCantidad.setTag(lista.get(position));

        }

        /*if(articulo.isElegido()){
            viewHolder.edCantidad.setEnabled(false);
        } else {
            viewHolder.edCantidad.setEnabled(false);
        }*/


        return view;
    }

}