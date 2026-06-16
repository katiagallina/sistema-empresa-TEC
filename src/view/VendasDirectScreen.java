package view;

import dao.ProdutoDAO;
import dao.ServicoDAO;
import dao.VendaDAO;
import model.Produto;
import model.Servico;
import model.Venda;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VendasDirectScreen extends JDialog {

    private JComboBox<Object> cbItens;
    private JTextField txtDescricao;
    private JTextField txtQuantidade;
    private JTextField txtValorTotal;
    private JComboBox<String> cbFormaPagamento;
    
    private List<Object> allItems = new ArrayList<>();
    private boolean isUpdatingCombo = false;

    public VendasDirectScreen(Frame owner) {
        super(owner, "Lançamento de Vendas Diárias - TEC Energia", true);
        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Carregar Itens
        try {
            List<Produto> produtos = new ProdutoDAO().listar();
            List<Servico> servicos = new ServicoDAO().listar();
            
            for (Produto p : produtos) {
                allItems.add(new OrcamentoScreen.ItemSelection("PRODUTO", p.getId(), p.getNome(), p.getPrecoVenda()));
            }
            for (Servico s : servicos) {
                allItems.add(new OrcamentoScreen.ItemSelection("SERVICO", s.getId(), s.getNome(), s.getValorBase()));
            }
            
            Collections.sort(allItems, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareToIgnoreCase(o2.toString());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Components
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Pesquisar Item:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cbItens = new JComboBox<>(allItems.toArray());
        cbItens.setEditable(true);
        add(cbItens, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        add(new JLabel("Descrição da Venda:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtDescricao = new JTextField();
        add(txtDescricao, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        txtQuantidade = new JTextField("1.0");
        add(txtQuantidade, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Valor Total Cobrado (R$):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        txtValorTotal = new JTextField();
        add(txtValorTotal, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Forma de Pagamento:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        cbFormaPagamento = new JComboBox<>(new String[]{"PIX", "DINHEIRO", "BOLETO", "CARTAO", "TRANSFERENCIA"});
        add(cbFormaPagamento, gbc);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRegistrar = new JButton("Confirmar Venda");
        btnRegistrar.setBackground(new Color(0, 102, 204));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 12));
        JButton btnCancelar = new JButton("Cancelar");
        buttonPanel.add(btnRegistrar);
        buttonPanel.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        // Listeners
        cbItens.addActionListener(e -> selectItem());
        btnRegistrar.addActionListener(e -> registrarVenda());
        btnCancelar.addActionListener(e -> dispose());

        setupAutocomplete();
    }

    private void setupAutocomplete() {
        JTextField editor = (JTextField) cbItens.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_UP ||
                    e.getKeyCode() == KeyEvent.VK_DOWN ||
                    e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    return;
                }

                String text = editor.getText();
                if (isUpdatingCombo) return;
                isUpdatingCombo = true;

                SwingUtilities.invokeLater(() -> {
                    cbItens.removeAllItems();
                    for (Object item : allItems) {
                        if (item.toString().toLowerCase().contains(text.toLowerCase())) {
                            cbItens.addItem(item);
                        }
                    }
                    editor.setText(text);
                    if (cbItens.getItemCount() > 0) {
                        cbItens.showPopup();
                    } else {
                        cbItens.hidePopup();
                    }
                    isUpdatingCombo = false;
                });
            }
        });
    }

    private void selectItem() {
        Object selected = cbItens.getSelectedItem();
        if (selected instanceof OrcamentoScreen.ItemSelection) {
            OrcamentoScreen.ItemSelection item = (OrcamentoScreen.ItemSelection) selected;
            txtDescricao.setText(item.nome);
            try {
                double qtd = Double.parseDouble(txtQuantidade.getText());
                txtValorTotal.setText(String.format("%.2f", item.preco * qtd));
            } catch (Exception ex) {
                txtValorTotal.setText(String.format("%.2f", item.preco));
            }
        }
    }

    private void registrarVenda() {
        Object selected = cbItens.getSelectedItem();
        if (!(selected instanceof OrcamentoScreen.ItemSelection)) {
            JOptionPane.showMessageDialog(this, "Selecione um produto ou serviço da lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        OrcamentoScreen.ItemSelection itemSel = (OrcamentoScreen.ItemSelection) selected;
        String desc = txtDescricao.getText().trim();
        if (desc.isEmpty()) {
            desc = itemSel.nome;
        }

        try {
            double qtd = Double.parseDouble(txtQuantidade.getText().trim());
            double total = Double.parseDouble(txtValorTotal.getText().trim().replace(",", "."));
            String pgto = (String) cbFormaPagamento.getSelectedItem();

            double custoTotal = 0.0;
            ProdutoDAO prodDAO = new ProdutoDAO();
            
            if ("PRODUTO".equals(itemSel.tipo)) {
                Produto prod = prodDAO.buscarPorId(itemSel.id);
                if (prod != null) {
                    if (prod.getQuantidade() < qtd) {
                        int choice = JOptionPane.showConfirmDialog(this, 
                                String.format("Estoque insuficiente! Disponível: %.2f. Deseja vender assim mesmo?", prod.getQuantidade()),
                                "Alerta de Estoque", JOptionPane.YES_NO_OPTION);
                        if (choice != JOptionPane.YES_OPTION) return;
                    }
                    
                    custoTotal = prod.getPrecoCusto() * qtd;
                    // Dar baixa no estoque
                    prod.setQuantidade(prod.getQuantidade() - qtd);
                    prodDAO.atualizar(prod);
                }
            } else {
                custoTotal = 0.0;
            }

            double lucro = total - custoTotal;

            Venda v = new Venda();
            v.setOrdemServicoId(null);
            v.setTipoItem(itemSel.tipo);
            v.setItemId(itemSel.id);
            v.setDescricao(desc);
            v.setQuantidade(qtd);
            v.setValorTotal(total);
            v.setCustoTotal(custoTotal);
            v.setLucro(lucro);
            v.setFormaPagamento(pgto);

            new VendaDAO().inserir(v);

            JOptionPane.showMessageDialog(this, "Venda diária registrada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantidade e Valor Total devem ser numéricos.", "Erro de Digitação", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar venda no Banco: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
