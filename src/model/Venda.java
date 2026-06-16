package model;

import java.sql.Timestamp;

public class Venda {
    private int id;
    private Timestamp dataVenda;
    private Integer ordemServicoId; // Pode ser nulo
    private String tipoItem; // "PRODUTO" ou "SERVICO"
    private int itemId; // ID do produto ou do serviço
    private String descricao;
    private double quantidade;
    private double valorTotal;
    private double custoTotal;
    private double lucro;
    private String formaPagamento;

    public Venda() {
    }

    public Venda(int id, Timestamp dataVenda, Integer ordemServicoId, String tipoItem, int itemId, String descricao, double quantidade, double valorTotal, double custoTotal, double lucro, String formaPagamento) {
        this.id = id;
        this.dataVenda = dataVenda;
        this.ordemServicoId = ordemServicoId;
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
        this.custoTotal = custoTotal;
        this.lucro = lucro;
        this.formaPagamento = formaPagamento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Timestamp dataVenda) {
        this.dataVenda = dataVenda;
    }

    public Integer getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(Integer ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public double getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(double custoTotal) {
        this.custoTotal = custoTotal;
    }

    public double getLucro() {
        return lucro;
    }

    public void setLucro(double lucro) {
        this.lucro = lucro;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
}
