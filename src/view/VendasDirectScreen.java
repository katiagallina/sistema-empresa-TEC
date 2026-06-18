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
        setSize(500, 440);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        // 🔹 Header Panel (Banner)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(235, 242, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblHeaderTitle = new JLabel("Venda Rápida / Caixa");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeaderTitle.setForeground(new Color(33, 37, 41));
        
        JLabel lblHeaderDesc = new JLabel("Lance saídas rápidas de estoque ou faturamento direto de serviços");
        lblHeaderDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHeaderDesc.setForeground(new Color(100, 110, 120));
        
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        headerPanel.add(lblHeaderDesc, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

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

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Components
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblItem = new JLabel("Pesquisar Item:");
        lblItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblItem, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cbItens = new JComboBox<>(allItems.toArray());
        cbItens.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbItens.setEditable(true);
        formPanel.add(cbItens, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblDesc = new JLabel("Descrição da Venda:");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblDesc, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        txtDescricao = new JTextField();
        txtDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtDescricao, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblQtd = new JLabel("Quantidade:");
        lblQtd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblQtd, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        txtQuantidade = new JTextField("1.0");
        txtQuantidade.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtQuantidade, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblVal = new JLabel("Valor Total Cobrado (R$):");
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblVal, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        txtValorTotal = new JTextField();
        txtValorTotal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtValorTotal, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblPg = new JLabel("Forma de Pagamento:");
        lblPg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblPg, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        cbFormaPagamento = new JComboBox<>(new String[]{"PIX", "DINHEIRO", "BOLETO", "CARTAO", "TRANSFERENCIA"});
        cbFormaPagamento.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(cbFormaPagamento, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));
        
        JButton btnRegistrar = new JButton("Confirmar Venda");
        btnRegistrar.setBackground(new Color(40, 167, 69)); // Verde
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegistrar.setFocusPainted(false);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setFocusPainted(false);
        
        buttonPanel.add(btnRegistrar);
        buttonPanel.add(btnCancelar);

        add(buttonPanel, BorderLayout.SOUTH);

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
