package view;

import dao.ProdutoDAO;
import model.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductListScreen extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;

    public ProductListScreen(Frame owner) {
        super(owner, "Lista de Produtos", true);
        setSize(800, 600);
        setLocationRelativeTo(owner);

        // Modelo da tabela
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Nome");
        tableModel.addColumn("Preço de Custo");
        tableModel.addColumn("Preço de Venda");
        tableModel.addColumn("Quantidade");
        tableModel.addColumn("Tipo de Venda");

        // Tabela
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Painel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        loadProducts();
    }

    private void loadProducts() {
        ProdutoDAO produtoDAO = new ProdutoDAO();
        List<Produto> produtos = produtoDAO.listar();

        // Limpa a tabela
        tableModel.setRowCount(0);

        // Adiciona os produtos na tabela
        for (Produto p : produtos) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    p.getPrecoCusto(),
                    p.getPrecoVenda(),
                    p.getQuantidade(),
                    p.getTipoVenda()
            });
        }
    }
}
