package ar.com.idus.www.idusappshiping.modelos;

import java.io.Serializable;

public class Comprobante implements Serializable {
    private String fecha;
    private String comprobante;
    private String id;
    private String nombreCliente;
    private String domicilioCliente;
    private String nombreVendedor;
    private String nombreFletero;
    private Double total;
    private Double saldo;
    private String tipo;
    private String clase;
    private int sucursal;
    private int numero;
    private int formaDePago;
    private int caja;
    private int planilla;
    private int mhr;
    private String idVendedor;
    private String idFletero;
    private String idCliente;
    private String pedidoGVS;
    private String clienteLatitud;
    private String clienteLongitud;
    private int estado;
    private int tipo_mov;
    private int envio_idus;

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDomicilioCliente() {
        return domicilioCliente;
    }

    public void setDomicilioCliente(String domicilioCliente) {
        this.domicilioCliente = domicilioCliente;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }

    public String getNombreFletero() {
        return nombreFletero;
    }

    public void setNombreFletero(String nombreFletero) {
        this.nombreFletero = nombreFletero;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public int getSucursal() {
        return sucursal;
    }

    public void setSucursal(int sucursal) {
        this.sucursal = sucursal;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getCaja() {
        return caja;
    }

    public void setCaja(int caja) {
        this.caja = caja;
    }

    public int getPlanilla() {
        return planilla;
    }

    public void setPlanilla(int planilla) {
        this.planilla = planilla;
    }

    public int getMhr() {
        return mhr;
    }

    public void setMhr(int mhr) {
        this.mhr = mhr;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getIdFletero() {
        return idFletero;
    }

    public void setIdFletero(String idFletero) {
        this.idFletero = idFletero;
    }

    public String getPedidoGVS() {
        return pedidoGVS;
    }

    public void setPedidoGVS(String pedidoGVS) {
        this.pedidoGVS = pedidoGVS;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public int getFormaDePago() {
        return formaDePago;
    }

    public void setFormaDePago(int formaDePago) {
        this.formaDePago = formaDePago;
    }

    public String getClienteLatitud() {
        return clienteLatitud;
    }

    public void setClienteLatitud(String clienteLatitud) {
        this.clienteLatitud = clienteLatitud;
    }

    public String getClienteLongitud() {
        return clienteLongitud;
    }

    public void setClienteLongitud(String clienteLongitud) {
        this.clienteLongitud = clienteLongitud;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getTipo_mov() {
        return tipo_mov;
    }

    public void setTipo_mov(int tipo_mov) {
        this.tipo_mov = tipo_mov;
    }

    public int getEnvio_idus() {
        return envio_idus;
    }

    public void setEnvio_idus(int envio_idus) {
        this.envio_idus = envio_idus;
    }
}
