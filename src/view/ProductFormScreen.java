package view;

import dao.ProdutoDAO;
import model.Produto;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ProductFormScreen extends JDialog {

    private JTextField txtNome;
    private JTextField txtPrecoCusto;
    private JTextField txtPrecoVenda;
    private JTextField txtQuantidade;
    private JComboBox<String> cbTipoVenda;

    private Produto produtoEdicao = null;
    private boolean salvoComSucesso = false;

    public ProductFormScreen(Window owner, Produto produto) {
        super(owner, produto == null ? "Cadastrar Produto" : "Editar Produto", ModalityType.APPLICATION_MODAL);
        this.produtoEdicao = produto;
        
        setSize(420, 360);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 🔹 Header Panel (Banner)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(235, 242, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblHeaderTitle = new JLabel(produto == null ? "Cadastrar Produto" : "Editar Produto");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeaderTitle.setForeground(new Color(33, 37, 41));
        
        JLabel lblHeaderDesc = new JLabel("Preencha as informações cadastrais do item");
        lblHeaderDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHeaderDesc.setForeground(new Color(100, 110, 120));
        
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        headerPanel.add(lblHeaderDesc, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(lblNome, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtNome = new JTextField();
        txtNome.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(txtNome, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblCusto = new JLabel("Preço de Custo (R$):");
        lblCusto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(lblCusto, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtPrecoCusto = new JTextField();
        txtPrecoCusto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(txtPrecoCusto, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblVenda = new JLabel("Preço de Venda (R$):");
        lblVenda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(lblVenda, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtPrecoVenda = new JTextField();
        txtPrecoVenda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(txtPrecoVenda, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        JLabel lblQtd = new JLabel("Quantidade/Estoque:");
        lblQtd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(lblQtd, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        txtQuantidade = new JTextField();
        txtQuantidade.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(txtQuantidade, gbc);

        // Row 4
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        JLabel lblTipo = new JLabel("Unidade de Medida:");
        lblTipo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(lblTipo, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        cbTipoVenda = new JComboBox<>(new String[]{"UNIDADE", "METRO", "PAR", "SERVICO"});
        cbTipoVenda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(cbTipoVenda, gbc);

        // Preencher se for edição
        if (produtoEdicao != null) {
            txtNome.setText(produtoEdicao.getNome());
            txtPrecoCusto.setText(String.valueOf(produtoEdicao.getPrecoCusto()));
            txtPrecoVenda.setText(String.valueOf(produtoEdicao.getPrecoVenda()));
            txtQuantidade.setText(String.valueOf(produtoEdicao.getQuantidade()));
            cbTipoVenda.setSelectedItem(produtoEdicao.getTipoVenda());
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));
        
        JButton btnSalvar = new JButton("Confirmar");
        btnSalvar.setBackground(new Color(40, 167, 69)); // Verde
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvar.setFocusPainted(false);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setFocusPainted(false);
        
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnCancelar);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> salvarProduto());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void salvarProduto() {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do produto é obrigatório.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double custo = Double.parseDouble(txtPrecoCusto.getText().trim().replace(",", "."));
            double venda = Double.parseDouble(txtPrecoVenda.getText().trim().replace(",", "."));
            double quantidade = Double.parseDouble(txtQuantidade.getText().trim().replace(",", "."));
            String tipo = (String) cbTipoVenda.getSelectedItem();

            ProdutoDAO dao = new ProdutoDAO();
            if (produtoEdicao == null) {
                Produto p = new Produto(0, nome, custo, venda, quantidade, tipo);
                dao.inserir(p);
            } else {
                produtoEdicao.setNome(nome);
                produtoEdicao.setPrecoCusto(custo);
                produtoEdicao.setPrecoVenda(venda);
                produtoEdicao.setQuantidade(quantidade);
                produtoEdicao.setTipoVenda(tipo);
                dao.atualizar(produtoEdicao);
            }

            salvoComSucesso = true;
            JOptionPane.showMessageDialog(this, "Produto salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, insira valores numéricos válidos para preços e quantidade.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro no Banco de Dados: " + ex.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean isSalvoComSucesso() {
        return salvoComSucesso;
    }
}
