package model;

import java.sql.Timestamp;
import java.util.List;

public class Orcamento {

    private int id;
    private Timestamp dataOrcamento;
    private double valorTotal;
    private String clienteNome;
    private List<ItemOrcamento> itens;

    public Orcamento() {
    }

    public Orcamento(int id, Timestamp dataOrcamento, double valorTotal, String clienteNome, List<ItemOrcamento> itens) {
        this.id = id;
        this.dataOrcamento = dataOrcamento;
        this.valorTotal = valorTotal;
        this.clienteNome = clienteNome;
        this.itens = itens;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDataOrcamento() {
        return dataOrcamento;
    }

    public void setDataOrcamento(Timestamp dataOrcamento) {
        this.dataOrcamento = dataOrcamento;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public List<ItemOrcamento> getItens() {
        return itens;
    }

    public void setItens(List<ItemOrcamento> itens) {
        this.itens = itens;
    }
}