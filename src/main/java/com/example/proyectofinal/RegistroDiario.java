package com.example.proyectofinal;


import java.sql.Date;
import java.time.LocalDate;

public class RegistroDiario {

    private int contrato_id;
    private Date fecha;
    private int entrada;
    private int salida;
    private int defunciones;
    private int nave;
    private String nombre_cliente;

    private int total;

    public RegistroDiario(int contrato_id, Date fecha, int entrada, int salida, int defunciones, int nave, String nombre_cliente, int total) {
        this.contrato_id = contrato_id;
        this.fecha = fecha;
        this.entrada = entrada;
        this.salida = salida;
        this.defunciones = defunciones;
        this.nave = nave;
        this.nombre_cliente = nombre_cliente;
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getContrato_id() {
        return contrato_id;
    }

    public void setContrato_id(int contrato_id) {
        this.contrato_id = contrato_id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFechaAsLocalDate() {
        return fecha.toLocalDate();
    }

    public void setFechaAsLocalDate(LocalDate localDate) {
        this.fecha = Date.valueOf(localDate);
    }

    public int getEntrada() {
        return entrada;
    }

    public void setEntrada(int entrada) {
        this.entrada = entrada;
    }

    public int getSalida() {
        return salida;
    }

    public void setSalida(int salida) {
        this.salida = salida;
    }

    public int getDefunciones() {
        return defunciones;
    }

    public void setDefunciones(int defunciones) {
        this.defunciones = defunciones;
    }

    public int getNave() {
        return nave;
    }

    public void setNave(int nave) {
        this.nave = nave;
    }

    public String getNombre_cliente() {
        return nombre_cliente;
    }

    public void setNombre_cliente(String nombre_cliente) {
        this.nombre_cliente = nombre_cliente;
    }

    @Override
    public String toString() {
        return "RegistroDiario{" +
                "contrato_id=" + contrato_id +
                ", fecha=" + fecha +
                ", entrada=" + entrada +
                ", salida=" + salida +
                ", defunciones=" + defunciones +
                ", nave=" + nave +
                ", nombre_cliente='" + nombre_cliente + '\'' +
                '}';
    }
}
