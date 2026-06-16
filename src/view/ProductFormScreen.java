package view;

import dao.ProdutoDAO;
import model.Produto;

import javax.swing.*;
import java.awt.*;

public class ProductFormScreen extends JDialog {

    private JTextField txtNome;
    private JTextField txtPrecoCusto;
    private JTextField txtPrecoVenda;
    private JTextField txtQuantidade;
    private JComboBox<String> cbTipoVenda;

    public ProductFormScreen(Frame owner) {
        super(owner, "Cadastrar Produto", true);
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        formPanel.add(txtNome);

        formPanel.add(new JLabel("Preço de Custo:"));
        txtPrecoCusto = new JTextField();
        formPanel.add(txtPrecoCusto);

        formPanel.add(new JLabel("Preço de Venda:"));
        txtPrecoVenda = new JTextField();
        formPanel.add(txtPrecoVenda);

        formPanel.add(new JLabel("Quantidade:"));
        txtQuantidade = new JTextField();
        formPanel.add(txtQuantidade);

        formPanel.add(new JLabel("Tipo de Venda:"));
        cbTipoVenda = new JComboBox<>(new String[]{"UNIDADE", "METRO", "PAR", "SERVICO"});
        formPanel.add(cbTipoVenda);

        JPanel buttonPanel = new JPanel();
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnCancelar);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> salvarProduto());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void salvarProduto() {
        try {
            Produto produto = new Produto();
            produto.setNome(txtNome.getText());
            produto.setPrecoCusto(Double.parseDouble(txtPrecoCusto.getText()));
            produto.setPrecoVenda(Double.parseDouble(txtPrecoVenda.getText()));
            produto.setQuantidade(Double.parseDouble(txtQuantidade.getText()));
            produto.setTipoVenda((String) cbTipoVenda.getSelectedItem());

            ProdutoDAO produtoDAO = new ProdutoDAO();
            produtoDAO.inserir(produto);

            JOptionPane.showMessageDialog(this, "Produto salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, insira valores numéricos válidos para preços e quantidade.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro no Banco de Dados: " + ex.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar o produto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
