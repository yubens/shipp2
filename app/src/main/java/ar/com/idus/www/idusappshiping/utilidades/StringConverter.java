package ar.com.idus.www.idusappshiping.utilidades;

import java.text.DecimalFormat;

public abstract class StringConverter {
    static DecimalFormat format = new DecimalFormat("#0.00");

    public static double toDouble(String value){
        double aux = 0.0;
        return aux;
    }

    public static String setDot(String value){
        return value.replace(',', '.');
    }

    public static String formatString(Double value){
        return format.format(value);
    }
}
