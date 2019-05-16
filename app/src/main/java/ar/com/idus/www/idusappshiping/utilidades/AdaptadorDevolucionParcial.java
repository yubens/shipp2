package ar.com.idus.www.idusappshiping.utilidades;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.util.List;

import ar.com.idus.www.idusappshiping.R;
import ar.com.idus.www.idusappshiping.modelos.Articulo;

public class AdaptadorDevolucionParcial extends ArrayAdapter <Articulo> {
    protected Activity context;
    protected List <Articulo> lista;
    protected List<Integer> cantidades;
    protected DecimalFormat format = new DecimalFormat("#.00");
    protected String detalle;


    public AdaptadorDevolucionParcial(Activity context, List <Articulo> lista, List <Integer> cantidades){
        super(context, R.layout.articulos_a_devolver, lista);
        this.context = context;
        this.lista = lista;
        this.cantidades = cantidades;
    }

    static class ViewHolder {
        CheckBox checkBox;
        EditText edCant;
        TextView txtTotal;
        TextView txtDetalle;
        TextView txtPrecio;
        TextView txtCant;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        final double cantidad, total;

        int cantAux;


        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.articulos_a_devolver, null);

            final ViewHolder viewHolder = new ViewHolder();

            viewHolder.checkBox = view.findViewById(R.id.checkArt);
            viewHolder.edCant = view.findViewById(R.id.editCantidad);
            viewHolder.txtTotal = view.findViewById(R.id.lblTotal);
            viewHolder.txtDetalle = view.findViewById(R.id.lblDetalle);
            viewHolder.txtPrecio = view.findViewById(R.id.lblPrecioUnitario);
            viewHolder.txtCant = view.findViewById(R.id.textCant);

            TextWatcher watcher = new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    double cantidad, total, fullTotalAux = 0.0;
                    int pos = viewHolder.edCant.getId();

                    if(s.length() > 0){

                        if(Double.valueOf(s.toString()).isNaN() ){
                            Toast.makeText(getContext(), "Dato ingresado no númerico",
                                            Toast.LENGTH_LONG).show();
                        } else {
                            Articulo articulo = (Articulo) viewHolder.edCant.getTag();

                            cantidad = Double.valueOf(s.toString());

                            if(cantidad == 0){
                                Toast.makeText(getContext(), "La cantidad a ingresar no puede ser cero (0)",
                                        Toast.LENGTH_LONG).show();
                                viewHolder.edCant.setText("");

                            } else if(cantidad > cantidades.get(pos)){
                                Toast.makeText(getContext(), "La cantidad a ingresar no puede ser mayor a la original",
                                        Toast.LENGTH_LONG).show();
                                viewHolder.edCant.setText("");

                            } else {
                                articulo.setCantidad(cantidad);
                                viewHolder.txtCant.setText(String.valueOf((int)cantidad));
                                total = cantidad * articulo.getPrecioVenta();
                                viewHolder.txtTotal.setText(format.format(total));
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };

            viewHolder.edCant.addTextChangedListener(watcher);

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Double total;
                    int cant;

                    int pos = viewHolder.checkBox.getId();
                    if (viewHolder.checkBox.isChecked()) {
                        lista.get(pos).setElegido(true);
                        viewHolder.edCant.setVisibility(View.VISIBLE);
                        viewHolder.edCant.requestFocus();
                        viewHolder.edCant.setEnabled(true);

                    } else {
                        lista.get(position).setElegido(false);
                        cant = cantidades.get(pos);
                        total = cant * lista.get(pos).getPrecioVenta();
                        viewHolder.edCant.setVisibility(View.INVISIBLE);
                        viewHolder.edCant.setEnabled(false);
                        viewHolder.edCant.setText("");
                        viewHolder.txtCant.setText(String.valueOf(cant));
                        viewHolder.txtTotal.setText(format.format(total));
                    }
                }
            });

            viewHolder.edCant.setTag(lista.get(position));
            viewHolder.checkBox.setTag(lista.get(position));
            viewHolder.txtTotal.setTag(lista.get(position));
            viewHolder.txtDetalle.setTag(lista.get(position));
            viewHolder.txtPrecio.setTag(lista.get(position));
            viewHolder.txtCant.setTag(lista.get(position));
            view.setTag(viewHolder);

        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).edCant.setTag(lista.get(position));
            ((ViewHolder) view.getTag()).checkBox.setTag(lista.get(position));
            ((ViewHolder) view.getTag()).txtTotal.setTag(lista.get(position));
            ((ViewHolder) view.getTag()).txtDetalle.setTag(lista.get(position));
            ((ViewHolder) view.getTag()).txtPrecio.setTag(lista.get(position));
            ((ViewHolder) view.getTag()).txtCant.setTag(lista.get(position));

        }

        ViewHolder holder = (ViewHolder) view.getTag();
        cantidad = lista.get(position).getCantidad();
        cantAux = (int) cantidad;
        total = cantidad * lista.get(position).getPrecioVenta();
        holder.txtCant.setText(String.valueOf(cantAux));
        holder.txtTotal.setText(format.format(total));

        holder.checkBox.setChecked(lista.get(position).isElegido());
        holder.checkBox.setId(position);
        holder.edCant.setId(position);

        if(holder.checkBox.isChecked()){
            holder.edCant.setVisibility(View.VISIBLE);
        } else {
            holder.edCant.setVisibility(View.INVISIBLE);
        }

        detalle = lista.get(position).getNombre();

        //si el detalle es amplio se reduce el tamaño de la letra y se recorta el detalle
        if (detalle.length() <= 25){
            holder.txtDetalle.setTextSize(14);
        } else {
            holder.txtDetalle.setTextSize(8);
        }

        holder.txtDetalle.setText(detalle);

        holder.txtPrecio.setText(format.format(lista.get(position).getPrecioVenta()));

        return view;
    }
}


