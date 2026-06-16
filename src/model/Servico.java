package model;

import java.math.BigDecimal;

public class Servico {

    private int id;
    private String nome;
    private String tipo; // "POR_HORA" ou "VALOR_FIXO"
    private BigDecimal valorBase;

    public Servico() {
    }

    public Servico(int id, String nome, String tipo, BigDecimal valorBase) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.valorBase = valorBase;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
    }

    @Override
    public String toString() {
        return getNome(); // Facilita a exibição em JComboBox
    }
}
