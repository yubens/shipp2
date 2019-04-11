package ar.com.idus.www.idusappshiping.modelos;

import java.io.Serializable;

public class ComprobanteItems implements Serializable {
    private String id;
    private String detalle;
    private int item;
    private double cantidad;
    private double precioNeto;
    private double precioIva;
    private double precioIvaO;
    private double precioImpInt;
    private double porcentajeDescuento;
    private double precioCosto;
    private double precioFinal;
    private double total;

    private String idArticulo;
    private String codigoArticulo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioNeto() {
        return precioNeto;
    }

    public void setPrecioNeto(double precioNeto) {
        this.precioNeto = precioNeto;
    }

    public double getPrecioIva() {
        return precioIva;
    }

    public void setPrecioIva(double precioIva) {
        this.precioIva = precioIva;
    }

    public double getPrecioIvaO() {
        return precioIvaO;
    }

    public void setPrecioIvaO(double precioIvaO) {
        this.precioIvaO = precioIvaO;
    }

    public double getPrecioImpInt() {
        return precioImpInt;
    }

    public void setPrecioImpInt(double precioImpInt) {
        this.precioImpInt = precioImpInt;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public String getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(String idArticulo) {
        this.idArticulo = idArticulo;
    }

    public String getCodigoArticulo() {
        return codigoArticulo;
    }

    public void setCodigoArticulo(String codigoArticulo) {
        this.codigoArticulo = codigoArticulo;
    }

    public double getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(double precioCosto) {
        this.precioCosto = precioCosto;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public double getPrecioFinal() {
        return precioFinal;
    }

    public void setPrecioFinal(double precioFinal) {
        this.precioFinal = precioFinal;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
