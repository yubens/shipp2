package ar.com.idus.www.idusappshiping.modelos;

import java.io.Serializable;

public class Articulo implements Serializable {
    private String id;
    private String codigo;
    private Double precioVenta;
    private Double precioConDesc;
    private String nombre;
    private int existencia;
    private int unidadVenta;
    private int multiplo;
    private double cantidad;
    private Double descuento;
    private boolean elegido;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Double getPrecioConDesc() {
        return precioConDesc;
    }

    public void setPrecioConDesc(Double precioConDesc) {
        this.precioConDesc = precioConDesc;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public int getUnidadVenta() {
        return unidadVenta;
    }

    public void setUnidadVenta(int unidadVenta) {
        this.unidadVenta = unidadVenta;
    }

    public int getMultiplo() {
        return multiplo;
    }

    public void setMultiplo(int multiplo) {
        this.multiplo = multiplo;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public boolean isElegido() {
        return elegido;
    }

    public void setElegido(boolean elegido) {
        this.elegido = elegido;
    }
}
