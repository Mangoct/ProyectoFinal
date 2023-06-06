package com.example.proyectofinal;

import java.util.ArrayList;
import java.util.List;

public class Clientes {
    private List<Cliente> clientes;

    public Clientes() {
        // Inicializa la lista de clientes con algunos clientes
        clientes = new ArrayList<>();
        clientes.add(new Cliente("El pozo"));
        clientes.add(new Cliente("Campofrio"));
        clientes.add(new Cliente("Cefusa"));
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }
}
