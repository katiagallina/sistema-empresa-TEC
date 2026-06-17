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
        
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        formPanel.add(txtNome);

        formPanel.add(new JLabel("Preço de Custo (R$):"));
        txtPrecoCusto = new JTextField();
        formPanel.add(txtPrecoCusto);

        formPanel.add(new JLabel("Preço de Venda (R$):"));
        txtPrecoVenda = new JTextField();
        formPanel.add(txtPrecoVenda);

        formPanel.add(new JLabel("Quantidade/Estoque:"));
        txtQuantidade = new JTextField();
        formPanel.add(txtQuantidade);

        formPanel.add(new JLabel("Unidade de Medida:"));
        cbTipoVenda = new JComboBox<>(new String[]{"UNIDADE", "METRO", "PAR", "SERVICO"});
        formPanel.add(cbTipoVenda);

        // Preencher se for edição
        if (produtoEdicao != null) {
            txtNome.setText(produtoEdicao.getNome());
            txtPrecoCusto.setText(String.valueOf(produtoEdicao.getPrecoCusto()));
            txtPrecoVenda.setText(String.valueOf(produtoEdicao.getPrecoVenda()));
            txtQuantidade.setText(String.valueOf(produtoEdicao.getQuantidade()));
            cbTipoVenda.setSelectedItem(produtoEdicao.getTipoVenda());
        }

        JPanel buttonPanel = new JPanel();
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(new Color(0, 102, 204));
        btnSalvar.setForeground(Color.WHITE);
        JButton btnCancelar = new JButton("Cancelar");
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnCancelar);

        add(formPanel, BorderLayout.CENTER);
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
