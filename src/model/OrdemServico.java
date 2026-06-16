package model;

import java.sql.Timestamp;
import java.util.List;

public class OrdemServico {
    private int id;
    private int clienteId;
    private Timestamp dataOrdem;
    private double valorTotal;
    private String status; // "EM ANDAMENTO", "FINALIZADA", "CANCELADA"
    private List<ItemOrdemServico> itens;

    public OrdemServico() {
    }

    public OrdemServico(int id, int clienteId, Timestamp dataOrdem, double valorTotal, String status, List<ItemOrdemServico> itens) {
        this.id = id;
        this.clienteId = clienteId;
        this.dataOrdem = dataOrdem;
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

    public Timestamp getDataOrdem() {
        return dataOrdem;
    }

    public void setDataOrdem(Timestamp dataOrdem) {
        this.dataOrdem = dataOrdem;
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

    public List<ItemOrdemServico> getItens() {
        return itens;
    }

    public void setItens(List<ItemOrdemServico> itens) {
        this.itens = itens;
    }
}