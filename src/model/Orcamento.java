package model;

import java.sql.Timestamp;
import java.util.List;

public class Orcamento {
    private int id;
    private int clienteId;
    private Timestamp dataOrcamento;
    private double valorTotal;
    private String status; // "ABERTO", "APROVADO", "REPROVADO", "FINALIZADO"
    private List<ItemOrcamento> itens;

    public Orcamento() {
    }

    public Orcamento(int id, int clienteId, Timestamp dataOrcamento, double valorTotal, String status, List<ItemOrcamento> itens) {
        this.id = id;
        this.clienteId = clienteId;
        this.dataOrcamento = dataOrcamento;
        this.valorTotal = valorTotal;
        this.status = status;
        this.itens = itens;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ItemOrcamento> getItens() {
        return itens;
    }

    public void setItens(List<ItemOrcamento> itens) {
        this.itens = itens;
    }
}