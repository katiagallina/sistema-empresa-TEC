package model;

public class Servico {

    private int id;
    private String nome;
    private String tipo; // "POR_HORA" ou "VALOR_FIXO"
    private double valorBase;

    public Servico() {
    }

    public Servico(int id, String nome, String tipo, double valorBase) {
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

    public double getValorBase() {
        return valorBase;
    }

    public void setValorBase(double valorBase) {
        this.valorBase = valorBase;
    }

    @Override
    public String toString() {
        return getNome(); // Facilita a exibição em JComboBox
    }
}
