package model;

import java.sql.Timestamp;

public class Despesa {
    private int id;
    private Timestamp dataDespesa;
    private String descricao;
    private double valor;
    private String formaPagamento;

    public Despesa() {
    }

    public Despesa(int id, Timestamp dataDespesa, String descricao, double valor, String formaPagamento) {
        this.id = id;
        this.dataDespesa = dataDespesa;
        this.descricao = descricao;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDataDespesa() {
        return dataDespesa;
    }

    public void setDataDespesa(Timestamp dataDespesa) {
        this.dataDespesa = dataDespesa;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
}
