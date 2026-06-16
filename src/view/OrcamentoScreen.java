package view;

import dao.OrcamentoDAO;
import dao.ProdutoDAO;
import model.ItemOrcamento;
import model.Orcamento;
import model.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OrcamentoScreen extends JDialog {

    private JTextField txtClienteNome;
    private JTable tblItens;
    private DefaultTableModel tableModel;
    private JLabel lblValorTotal;

    private List<ItemOrcamento> itens = new ArrayList<>();
    private double valorTotalOrcamento = 0;

    public OrcamentoScreen(Frame owner) {
        super(owner, "Criar Orçamento", true);
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Painel Superior (Cliente e Adicionar Item)
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel clientePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clientePanel.add(new JLabel("Cliente:"));
        txtClienteNome = new JTextField(30);
        clientePanel.add(txtClienteNome);
        topPanel.add(clientePanel, BorderLayout.NORTH);

        JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addItemPanel.add(new JLabel("ID Produto:"));
        JTextField txtIdProduto = new JTextField(5);
        addItemPanel.add(txtIdProduto);
        addItemPanel.add(new JLabel("Qtd:"));
        JTextField txtQuantidade = new JTextField(5);
        addItemPanel.add(txtQuantidade);
        JButton btnAdicionarItem = new JButton("Adicionar");
        addItemPanel.add(btnAdicionarItem);
        topPanel.add(addItemPanel, BorderLayout.CENTER);

        // Tabela de Itens
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Produto");
        tableModel.addColumn("Qtd");
        tableModel.addColumn("Vlr. Unit.");
        tableModel.addColumn("Vlr. Total");
        tblItens = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblItens);

        // Painel Inferior (Total e Botões)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        lblValorTotal = new JLabel("Valor Total: R$ 0.00");
        lblValorTotal.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(lblValorTotal, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel();
        JButton btnSalvar = new JButton("Salvar Orçamento");
        JButton btnCancelar = new JButton("Cancelar");
        buttonsPanel.add(btnSalvar);
        buttonsPanel.add(btnCancelar);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        // Adicionando Paineis ao Dialog
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Ações dos botões
        btnAdicionarItem.addActionListener(e -> adicionarItem(txtIdProduto.getText(), txtQuantidade.getText()));
        btnSalvar.addActionListener(e -> salvarOrcamento());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void adicionarItem(String idProdutoStr, String qtdStr) {
        try {
            int idProduto = Integer.parseInt(idProdutoStr);
            double quantidade = Double.parseDouble(qtdStr);

            ProdutoDAO produtoDAO = new ProdutoDAO();
            Produto produto = produtoDAO.buscarPorId(idProduto);

            if (produto != null) {
                ItemOrcamento item = new ItemOrcamento();
                item.setIdProduto(idProduto);
                item.setQuantidade(quantidade);
                item.setValorUnitario(produto.getPrecoVenda());
                double valorTotalItem = quantidade * produto.getPrecoVenda();
                item.setValorTotal(valorTotalItem);

                itens.add(item);
                valorTotalOrcamento += valorTotalItem;

                // Atualiza a UI
                tableModel.addRow(new Object[]{
                        produto.getNome(),
                        item.getQuantidade(),
                        item.getValorUnitario(),
                        item.getValorTotal()
                });
                lblValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotalOrcamento));

            } else {
                JOptionPane.showMessageDialog(this, "Produto não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID do produto e quantidade devem ser números.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarOrcamento() {
        if (txtClienteNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o nome do cliente.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (itens.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, adicione pelo menos um item ao orçamento.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Orcamento orcamento = new Orcamento();
            orcamento.setClienteNome(txtClienteNome.getText());
            orcamento.setItens(itens);
            orcamento.setValorTotal(valorTotalOrcamento);

            OrcamentoDAO orcamentoDAO = new OrcamentoDAO();
            orcamentoDAO.inserir(orcamento);

            JOptionPane.showMessageDialog(this, "Orçamento salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar o orçamento: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
