package view;

import dao.ProdutoDAO;
import model.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ProductListScreen extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtPesquisa;

    public ProductListScreen(Frame owner) {
        super(owner, "Gerenciar Produtos - TEC Energia", true);
        setSize(900, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Painel Superior (Filtro/Busca)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Pesquisar Produto"));
        topPanel.add(new JLabel("Nome:"));
        txtPesquisa = new JTextField(30);
        topPanel.add(txtPesquisa);
        JButton btnPesquisar = new JButton("Buscar");
        topPanel.add(btnPesquisar);
        add(topPanel, BorderLayout.NORTH);

        // Modelo da tabela
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("ID");
        tableModel.addColumn("Nome");
        tableModel.addColumn("Preço de Custo");
        tableModel.addColumn("Preço de Venda");
        tableModel.addColumn("Estoque");
        tableModel.addColumn("Unidade de Medida");

        // Tabela
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Painel Inferior (Botões de Ação)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnAdicionar = new JButton("Adicionar Novo");
        btnAdicionar.setBackground(new Color(40, 167, 69)); // Verde
        btnAdicionar.setForeground(Color.WHITE);
        
        JButton btnEditar = new JButton("Editar Selecionado");
        btnEditar.setBackground(new Color(0, 102, 204)); // Azul
        btnEditar.setForeground(Color.WHITE);
        
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(220, 53, 69)); // Vermelho
        btnExcluir.setForeground(Color.WHITE);
        
        JButton btnFechar = new JButton("Fechar");

        bottomPanel.add(btnAdicionar);
        bottomPanel.add(btnEditar);
        bottomPanel.add(btnExcluir);
        bottomPanel.add(btnFechar);
        add(bottomPanel, BorderLayout.SOUTH);

        // Ações
        btnPesquisar.addActionListener(e -> loadProducts());
        txtPesquisa.addActionListener(e -> loadProducts());
        
        btnAdicionar.addActionListener(e -> {
            ProductFormScreen form = new ProductFormScreen(this, null);
            form.setVisible(true);
            if (form.isSalvoComSucesso()) {
                loadProducts();
            }
        });

        btnEditar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um produto para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) table.getValueAt(row, 0);
            try {
                Produto p = new ProdutoDAO().buscarPorId(id);
                if (p != null) {
                    ProductFormScreen form = new ProductFormScreen(this, p);
                    form.setVisible(true);
                    if (form.isSalvoComSucesso()) {
                        loadProducts();
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar produto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnExcluir.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um produto para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) table.getValueAt(row, 0);
            String nome = (String) table.getValueAt(row, 1);
            int opt = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o produto: " + nome + "?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    new ProdutoDAO().deletar(id);
                    JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    loadProducts();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir produto (pode estar vinculado a orçamentos/OS): " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnFechar.addActionListener(e -> dispose());

        loadProducts();
    }

    private void loadProducts() {
        String query = txtPesquisa.getText().trim();
        try {
            ProdutoDAO produtoDAO = new ProdutoDAO();
            List<Produto> produtos;
            if (query.isEmpty()) {
                produtos = produtoDAO.listar();
            } else {
                produtos = produtoDAO.buscarPorNome(query);
            }

            tableModel.setRowCount(0);
            for (Produto p : produtos) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getNome(),
                        String.format("R$ %.2f", p.getPrecoCusto()),
                        String.format("R$ %.2f", p.getPrecoVenda()),
                        String.format("%.2f", p.getQuantidade()),
                        p.getTipoVenda()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar lista de produtos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
