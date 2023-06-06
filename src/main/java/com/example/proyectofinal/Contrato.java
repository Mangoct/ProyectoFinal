package com.example.proyectofinal;


public class Contrato {

    private int numeroContrato;
    private int nave;

    private int id;
    private String nombreCliente;

    public Contrato(int numeroContrato, String nombreCliente, int nave) {
        this.numeroContrato = numeroContrato;
        this.nombreCliente = nombreCliente;
        this.nave = nave;
    }

    public Contrato(int numeroContrato, int nave) {
        this.numeroContrato = numeroContrato;
        this.nave = nave;
    }



    public Contrato(int numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public int getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(int numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public int getNave() {
        return nave;
    }

    public void setNave(int nave) {
        this.nave = nave;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    @Override
    public String toString() {
        return "Contrato{" +
                "numeroContrato=" + numeroContrato +
                ", nave=" + nave +
                '}';
    }

}



