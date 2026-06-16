package model;

public class ItemOrdemServico {
    private int id;
    private int idOrdemServico;
    private String tipoItem; // "PRODUTO" ou "SERVICO"
    private Integer idProduto; // Pode ser nulo
    private Integer idServico; // Pode ser nulo
    private String descricao;
    private double quantidade;
    private double valorUnitario;
    private double valorTotal;

    public ItemOrdemServico() {
    }

    public ItemOrdemServico(int id, int idOrdemServico, String tipoItem, Integer idProduto, Integer idServico, String descricao, double quantidade, double valorUnitario, double valorTotal) {
        this.id = id;
        this.idOrdemServico = idOrdemServico;
        this.tipoItem = tipoItem;
        this.idProduto = idProduto;
        this.idServico = idServico;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdOrdemServico() {
        return idOrdemServico;
    }

    public void setIdOrdemServico(int idOrdemServico) {
        this.idOrdemServico = idOrdemServico;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public Integer getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Integer idProduto) {
        this.idProduto = idProduto;
    }

    public Integer getIdServico() {
        return idServico;
    }

    public void setIdServico(Integer idServico) {
        this.idServico = idServico;
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

    public double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }
}