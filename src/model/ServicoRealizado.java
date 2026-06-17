package model;

import java.sql.Timestamp;

public class ServicoRealizado {
    private int id;
    private Timestamp dataServico;
    private int clienteId;
    private String descricaoServico;
    private double valor;
    private String formaPagamento; // "DINHEIRO", "PIX", "CHEQUE", "BOLETO"
    private int numParcelas;
    private double valorParcela;

    public ServicoRealizado() {
        this.numParcelas = 1;
    }

    public ServicoRealizado(int id, Timestamp dataServico, int clienteId, String descricaoServico, double valor, String formaPagamento, int numParcelas, double valorParcela) {
        this.id = id;
        this.dataServico = dataServico;
        this.clienteId = clienteId;
        this.descricaoServico = descricaoServico;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.numParcelas = numParcelas;
        this.valorParcela = valorParcela;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDataServico() {
        return dataServico;
    }

    public void setDataServico(Timestamp dataServico) {
        this.dataServico = dataServico;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getDescricaoServico() {
        return descricaoServico;
    }

    public void setDescricaoServico(String descricaoServico) {
        this.descricaoServico = descricaoServico;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public int getNumParcelas() {
        return numParcelas;
    }

    public void setNumParcelas(int numParcelas) {
        this.numParcelas = numParcelas;
    }

    public double getValorParcela() {
        return valorParcela;
    }

    public void setValorParcela(double valorParcela) {
        this.valorParcela = valorParcela;
    }
}
