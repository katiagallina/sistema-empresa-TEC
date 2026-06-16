package view;

import dao.ClienteDAO;
import dao.ProdutoDAO;
import dao.ServicoDAO;
import dao.OrcamentoDAO;
import model.Cliente;
import model.Produto;
import model.Servico;
import model.Orcamento;
import model.ItemOrcamento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrcamentoScreen extends JDialog {

    private static final Color COLOR_PRIMARY = new Color(0, 102, 204);

    private JComboBox<Cliente> cbClientes;
    private JComboBox<Object> cbItens; // JComboBox pesquisável
    private JTextField txtDescricao;
    private JTextField txtPrecoUnitario;
    private JTextField txtQuantidade;
    
    private JTable tblItens;
    private DefaultTableModel tableModel;
    private JLabel lblValorTotal;

    private List<ItemOrcamento> itens = new ArrayList<>();
    private double valorTotalOrcamento = 0;
    
    private List<Object> allItems = new ArrayList<>(); // Lista completa de produtos e serviços para filtro
    private boolean isUpdatingCombo = false;

    public OrcamentoScreen(Frame owner) {
        super(owner, "Criar Orçamento - TEC Energia", true);
        setSize(850, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));

        // 🔹 Carregar dados do banco de dados
        List<Cliente> clientes = new ArrayList<>();
        try {
            clientes = new ClienteDAO().listar();
            
            List<Produto> produtos = new ProdutoDAO().listar();
            List<Servico> servicos = new ServicoDAO().listar();
            
            for (Produto p : produtos) {
                allItems.add(new ItemSelection("PRODUTO", p.getId(), p.getNome(), p.getPrecoVenda()));
            }
            for (Servico s : servicos) {
                allItems.add(new ItemSelection("SERVICO", s.getId(), s.getNome(), s.getValorBase()));
            }
            
            // Ordenar por nome (ordem de descrição)
            Collections.sort(allItems, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareToIgnoreCase(o2.toString());
                }
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do banco: " + e.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
        }

        // 🔹 Painel Superior (Seleção do Cliente e Formulário de Itens)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Bloco do Cliente
        JPanel clientePanel = new JPanel(new GridBagLayout());
        clientePanel.setBorder(BorderFactory.createTitledBorder("Dados do Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        clientePanel.add(new JLabel("Selecione o Cliente:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cbClientes = new JComboBox<>();
        for (Cliente c : clientes) {
            cbClientes.addItem(c);
        }
        clientePanel.add(cbClientes, gbc);
        
        JButton btnNovoCliente = new JButton("+ Novo");
        btnNovoCliente.addActionListener(e -> {
            ClienteScreen cs = new ClienteScreen(owner);
            cs.setVisible(true);
            // Recarregar clientes
            try {
                List<Cliente> updated = new ClienteDAO().listar();
                cbClientes.removeAllItems();
                for (Cliente c : updated) {
                    cbClientes.addItem(c);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        clientePanel.add(btnNovoCliente, gbc);

        topPanel.add(clientePanel);
        topPanel.add(Box.createVerticalStrut(10));

        // Bloco de Adicionar Item
        JPanel itemPanel = new JPanel(new GridBagLayout());
        itemPanel.setBorder(BorderFactory.createTitledBorder("Adicionar Produto / Serviço"));
        GridBagConstraints gbcItem = new GridBagConstraints();
        gbcItem.insets = new Insets(5, 5, 5, 5);
        gbcItem.fill = GridBagConstraints.HORIZONTAL;

        gbcItem.gridx = 0; gbcItem.gridy = 0;
        itemPanel.add(new JLabel("Buscar Item (Digite para pesquisar):"), gbcItem);

        gbcItem.gridx = 1; gbcItem.gridy = 0; gbcItem.gridwidth = 3; gbcItem.weightx = 1.0;
        cbItens = new JComboBox<>(allItems.toArray());
        cbItens.setEditable(true);
        itemPanel.add(cbItens, gbcItem);

        gbcItem.gridx = 0; gbcItem.gridy = 1; gbcItem.gridwidth = 1; gbcItem.weightx = 0;
        itemPanel.add(new JLabel("Descrição (para orçamento):"), gbcItem);

        gbcItem.gridx = 1; gbcItem.gridy = 1; gbcItem.weightx = 1.0;
        txtDescricao = new JTextField();
        itemPanel.add(txtDescricao, gbcItem);

        gbcItem.gridx = 0; gbcItem.gridy = 2; gbcItem.weightx = 0;
        itemPanel.add(new JLabel("Preço Unitário (R$):"), gbcItem);

        gbcItem.gridx = 1; gbcItem.gridy = 2; gbcItem.weightx = 0.5;
        txtPrecoUnitario = new JTextField();
        itemPanel.add(txtPrecoUnitario, gbcItem);

        gbcItem.gridx = 2; gbcItem.gridy = 2; gbcItem.weightx = 0;
        itemPanel.add(new JLabel("Quantidade:"), gbcItem);

        gbcItem.gridx = 3; gbcItem.gridy = 2; gbcItem.weightx = 0.5;
        txtQuantidade = new JTextField("1.0");
        itemPanel.add(txtQuantidade, gbcItem);

        JButton btnAdicionarItem = new JButton("Adicionar Item");
        gbcItem.gridx = 0; gbcItem.gridy = 3; gbcItem.gridwidth = 4; gbcItem.weightx = 1.0;
        itemPanel.add(btnAdicionarItem, gbcItem);

        topPanel.add(itemPanel);

        // Configuração de Autocomplete no cbItens
        setupAutocomplete();

        // 🔹 Tabela de Itens do Orçamento
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("Tipo");
        tableModel.addColumn("Item / Descrição");
        tableModel.addColumn("Qtd");
        tableModel.addColumn("Vlr. Unitário");
        tableModel.addColumn("Vlr. Total");
        
        tblItens = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblItens);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Itens Adicionados"));

        // Menu de Contexto para Excluir item adicionado
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("Remover Item");
        deleteMenuItem.addActionListener(e -> removerItemSelecionado());
        popupMenu.add(deleteMenuItem);
        tblItens.setComponentPopupMenu(popupMenu);

        // 🔹 Painel Inferior (Total e Botões de Ação)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        
        lblValorTotal = new JLabel("Valor Total do Orçamento: R$ 0,00");
        lblValorTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblValorTotal.setForeground(COLOR_PRIMARY);
        bottomPanel.add(lblValorTotal, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnSalvar = new JButton("Salvar Orçamento");
        btnSalvar.setFont(new Font("Arial", Font.BOLD, 13));
        btnSalvar.setBackground(COLOR_PRIMARY);
        btnSalvar.setForeground(Color.WHITE);
        JButton btnCancelar = new JButton("Cancelar");
        
        buttonsPanel.add(btnSalvar);
        buttonsPanel.add(btnCancelar);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        // Adicionando componentes à janela
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // 🔹 Ações
        cbItens.addActionListener(e -> selectItem());
        btnAdicionarItem.addActionListener(e -> adicionarItem());
        btnSalvar.addActionListener(e -> salvarOrcamento());
        btnCancelar.addActionListener(e -> dispose());
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
        if (selected instanceof ItemSelection) {
            ItemSelection item = (ItemSelection) selected;
            txtDescricao.setText(item.nome);
            txtPrecoUnitario.setText(String.format("%.2f", item.preco));
        }
    }

    private void adicionarItem() {
        Object selected = cbItens.getSelectedItem();
        if (!(selected instanceof ItemSelection)) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um produto ou serviço válido da lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ItemSelection sel = (ItemSelection) selected;
        String desc = txtDescricao.getText().trim();
        if (desc.isEmpty()) {
            desc = sel.nome;
        }

        try {
            double qtd = Double.parseDouble(txtQuantidade.getText().trim());
            double unit = Double.parseDouble(txtPrecoUnitario.getText().trim().replace(",", "."));
            double total = qtd * unit;

            ItemOrcamento item = new ItemOrcamento();
            item.setTipoItem(sel.tipo);
            if ("PRODUTO".equals(sel.tipo)) {
                item.setIdProduto(sel.id);
                item.setIdServico(null);
            } else {
                item.setIdServico(sel.id);
                item.setIdProduto(null);
            }
            item.setDescricao(desc);
            item.setQuantidade(qtd);
            item.setValorUnitario(unit);
            item.setValorTotal(total);

            itens.add(item);
            valorTotalOrcamento += total;

            tableModel.addRow(new Object[]{
                    item.getTipoItem(),
                    item.getDescricao(),
                    String.format("%.2f", item.getQuantidade()),
                    String.format("R$ %.2f", item.getValorUnitario()),
                    String.format("R$ %.2f", item.getValorTotal())
            });

            lblValorTotal.setText(String.format("Valor Total do Orçamento: R$ %.2f", valorTotalOrcamento));

            // Resetar campos de adição de item
            txtDescricao.setText("");
            txtPrecoUnitario.setText("");
            txtQuantidade.setText("1.0");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Preço e Quantidade devem ser numéricos.", "Erro de Digitação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerItemSelecionado() {
        int row = tblItens.getSelectedRow();
        if (row != -1) {
            ItemOrcamento item = itens.get(row);
            valorTotalOrcamento -= item.getValorTotal();
            itens.remove(row);
            tableModel.removeRow(row);
            lblValorTotal.setText(String.format("Valor Total do Orçamento: R$ %.2f", valorTotalOrcamento));
        }
    }

    private void salvarOrcamento() {
        Cliente cliente = (Cliente) cbClientes.getSelectedItem();
        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Por favor, cadastre e selecione um cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (itens.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O orçamento deve possuir pelo menos um item.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Orcamento orcamento = new Orcamento();
            orcamento.setClienteId(cliente.getId());
            orcamento.setValorTotal(valorTotalOrcamento);
            orcamento.setStatus("ABERTO");
            orcamento.setItens(itens);

            new OrcamentoDAO().inserir(orcamento);

            JOptionPane.showMessageDialog(this, "Orçamento " + orcamento.getId() + " salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar orçamento no Banco de Dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Classe auxiliar de Seleção
    public static class ItemSelection {
        String tipo;
        int id;
        String nome;
        double preco;

        public ItemSelection(String tipo, int id, String nome, double preco) {
            this.tipo = tipo;
            this.id = id;
            this.nome = nome;
            this.preco = preco;
        }

        @Override
        public String toString() {
            return "[" + tipo + "] " + nome + " - R$ " + String.format("%.2f", preco);
        }
    }
}
