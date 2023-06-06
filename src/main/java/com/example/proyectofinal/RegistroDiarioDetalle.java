package com.example.proyectofinal;


import java.sql.Date;

public class RegistroDiarioDetalle {

    private Date fecha;
    private int entrada;
    private int salida;
    private int defunciones;
    private int total;

    private int id;

    public RegistroDiarioDetalle(Date fecha, int entrada, int salida, int defunciones, int total) {
        this.fecha = fecha;
        this.entrada = entrada;
        this.salida = salida;
        this.defunciones = defunciones;
        this.total = total;
    }

    public RegistroDiarioDetalle(int id){
        this.id=id;
    }

    public RegistroDiarioDetalle() {

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "RegistroDiarioDetalle{" +
                "fecha=" + fecha +
                ", entrada=" + entrada +
                ", salida=" + salida +
                ", bajas=" + defunciones +
                ", totalAnimales=" + total +
                '}';
    }

}