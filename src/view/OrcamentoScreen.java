package view;

import dao.ClienteDAO;
import dao.ProdutoDAO;
import dao.ServicoDAO;
import dao.OrcamentoDAO;
import dao.ItemOrcamentoDAO;
import model.Cliente;
import model.Produto;
import model.Servico;
import model.Orcamento;
import model.ItemOrcamento;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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

    private JComboBox<Cliente> cbClientes;
    private JComboBox<Object> cbItens; // JComboBox pesquisável
    private JTextField txtPrecoUnitario;
    private JTextField txtQuantidade;
    
    private JTable tblItens;
    private DefaultTableModel tableModel;
    private JLabel lblValorTotal;
    private JCheckBox chkGerarPdf;

    private List<ItemOrcamento> itens = new ArrayList<>();
    private double valorTotalOrcamento = 0;
    
    private List<Object> allItems = new ArrayList<>(); // Lista completa de produtos e serviços para filtro
    private boolean isUpdatingCombo = false;

    private Orcamento existingOrcamento = null;

    public OrcamentoScreen(Window owner) {
        this(owner, null);
    }

    public OrcamentoScreen(Window owner, Orcamento existingOrcamento) {
        super(owner, existingOrcamento == null ? "Criar Orçamento - TEC Energia" : "Editar Orçamento #" + existingOrcamento.getId() + " - TEC Energia", Dialog.ModalityType.APPLICATION_MODAL);
        this.existingOrcamento = existingOrcamento;
        setSize(900, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 🔹 Header Panel (Banner)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(235, 242, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblHeaderTitle = new JLabel(existingOrcamento == null ? "Criar Novo Orçamento" : "Editar Orçamento #" + existingOrcamento.getId());
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderTitle.setForeground(new Color(33, 37, 41));
        
        JLabel lblHeaderDesc = new JLabel("Adicione produtos e mão de obra para simular custos e gerar propostas comerciais em PDF");
        lblHeaderDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHeaderDesc.setForeground(new Color(100, 110, 120));
        
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        headerPanel.add(lblHeaderDesc, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

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

        // Painel de Conteúdo
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        // 🔹 Painel Superior (Seleção do Cliente e Formulário de Itens)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Bloco do Cliente
        JPanel clientePanel = new JPanel(new GridBagLayout());
        TitledBorder clienteTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Dados do Cliente",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        clientePanel.setBorder(BorderFactory.createCompoundBorder(clienteTitle, BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblCli = new JLabel("Selecione o Cliente:");
        lblCli.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clientePanel.add(lblCli, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cbClientes = new JComboBox<>();
        cbClientes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (Cliente c : clientes) {
            cbClientes.addItem(c);
        }
        clientePanel.add(cbClientes, gbc);
        
        JButton btnNovoCliente = new JButton("+ Novo");
        btnNovoCliente.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNovoCliente.setFocusPainted(false);
        btnNovoCliente.addActionListener(e -> {
            Frame parentFrame = JOptionPane.getFrameForComponent(this);
            ClienteScreen cs = new ClienteScreen(parentFrame);
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
        TitledBorder itemTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Adicionar Produto / Serviço",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        itemPanel.setBorder(BorderFactory.createCompoundBorder(itemTitle, BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        GridBagConstraints gbcItem = new GridBagConstraints();
        gbcItem.insets = new Insets(5, 5, 5, 5);
        gbcItem.fill = GridBagConstraints.HORIZONTAL;

        gbcItem.gridx = 0; gbcItem.gridy = 0; gbcItem.weightx = 0;
        JLabel lblBus = new JLabel("Buscar Item:");
        lblBus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemPanel.add(lblBus, gbcItem);

        gbcItem.gridx = 1; gbcItem.gridy = 0; gbcItem.gridwidth = 4; gbcItem.weightx = 1.0;
        cbItens = new JComboBox<>(allItems.toArray());
        cbItens.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbItens.setEditable(true);
        itemPanel.add(cbItens, gbcItem);

        gbcItem.gridx = 0; gbcItem.gridy = 1; gbcItem.gridwidth = 1; gbcItem.weightx = 0;
        JLabel lblPrc = new JLabel("Preço Unitário (R$):");
        lblPrc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemPanel.add(lblPrc, gbcItem);

        gbcItem.gridx = 1; gbcItem.gridy = 1; gbcItem.weightx = 0.3;
        txtPrecoUnitario = new JTextField();
        txtPrecoUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemPanel.add(txtPrecoUnitario, gbcItem);

        gbcItem.gridx = 2; gbcItem.gridy = 1; gbcItem.weightx = 0;
        JLabel lblQtd = new JLabel("Quantidade:");
        lblQtd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemPanel.add(lblQtd, gbcItem);

        gbcItem.gridx = 3; gbcItem.gridy = 1; gbcItem.weightx = 0.2;
        txtQuantidade = new JTextField("1.0");
        txtQuantidade.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemPanel.add(txtQuantidade, gbcItem);

        JButton btnAdicionarItem = new JButton("Adicionar");
        btnAdicionarItem.setBackground(new Color(0, 102, 204));
        btnAdicionarItem.setForeground(Color.WHITE);
        btnAdicionarItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdicionarItem.setFocusPainted(false);
        gbcItem.gridx = 4; gbcItem.gridy = 1; gbcItem.weightx = 0.1;
        itemPanel.add(btnAdicionarItem, gbcItem);

        topPanel.add(itemPanel);
        contentPanel.add(topPanel, BorderLayout.NORTH);

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
        tblItens.setRowHeight(25);
        tblItens.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblItens.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblItens.setSelectionBackground(new Color(225, 235, 248));
        tblItens.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(tblItens);
        TitledBorder tblTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Itens Adicionados",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        scrollPane.setBorder(BorderFactory.createCompoundBorder(tblTitle, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Menu de Contexto para Excluir item adicionado
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("Remover Item");
        deleteMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteMenuItem.addActionListener(e -> removerItemSelecionado());
        popupMenu.add(deleteMenuItem);
        tblItens.setComponentPopupMenu(popupMenu);

        // 🔹 Painel Inferior (Total e Botões de Ação)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        lblValorTotal = new JLabel("Valor Total do Orçamento: R$ 0,00");
        lblValorTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValorTotal.setForeground(new Color(0, 102, 204));
        bottomPanel.add(lblValorTotal, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        chkGerarPdf = new JCheckBox("Gerar e Abrir PDF", true);
        chkGerarPdf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton btnSalvar = new JButton("Salvar Orçamento");
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSalvar.setBackground(new Color(40, 167, 69));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setFocusPainted(false);
        
        buttonsPanel.add(chkGerarPdf);
        buttonsPanel.add(btnSalvar);
        buttonsPanel.add(btnCancelar);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // 🔹 Ações
        cbItens.addActionListener(e -> selectItem());
        btnAdicionarItem.addActionListener(e -> adicionarItem());
        btnSalvar.addActionListener(e -> salvarOrcamento());
        btnCancelar.addActionListener(e -> dispose());

        // Se for edição, carregar dados do orçamento existente
        if (existingOrcamento != null) {
            // Seleciona o cliente correspondente
            for (int i = 0; i < cbClientes.getItemCount(); i++) {
                Cliente c = cbClientes.getItemAt(i);
                if (c.getId() == existingOrcamento.getClienteId()) {
                    cbClientes.setSelectedIndex(i);
                    break;
                }
            }
            cbClientes.setEnabled(false); // Não permite alterar cliente na edição
            btnNovoCliente.setEnabled(false); // Desativa botão de novo cliente

            // Carrega os itens do orçamento
            try {
                ItemOrcamentoDAO itemOrcamentoDAO = new ItemOrcamentoDAO();
                List<ItemOrcamento> itensCarregados = itemOrcamentoDAO.buscarPorIdOrcamento(existingOrcamento.getId());
                this.itens.addAll(itensCarregados);
                
                for (ItemOrcamento item : this.itens) {
                    tableModel.addRow(new Object[]{
                            item.getTipoItem(),
                            item.getDescricao(),
                            String.format("%.2f", item.getQuantidade()),
                            String.format("R$ %.2f", item.getValorUnitario()),
                            String.format("R$ %.2f", item.getValorTotal())
                    });
                    valorTotalOrcamento += item.getValorTotal();
                }
                lblValorTotal.setText(String.format("Valor Total do Orçamento: R$ %.2f", valorTotalOrcamento));
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar itens do orçamento existente: " + e.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
            }
        }
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
        String desc = sel.nome;

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
            if (existingOrcamento != null) {
                existingOrcamento.setClienteId(cliente.getId());
                existingOrcamento.setValorTotal(valorTotalOrcamento);
                existingOrcamento.setItens(itens);

                new OrcamentoDAO().atualizar(existingOrcamento);

                JOptionPane.showMessageDialog(this, "Orçamento #" + existingOrcamento.getId() + " atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                if (chkGerarPdf.isSelected()) {
                    util.PdfGenerator.gerarPdfOrcamento(existingOrcamento);
                }
            } else {
                Orcamento orcamento = new Orcamento();
                orcamento.setClienteId(cliente.getId());
                orcamento.setValorTotal(valorTotalOrcamento);
                orcamento.setStatus("ABERTO");
                orcamento.setItens(itens);

                new OrcamentoDAO().inserir(orcamento);

                JOptionPane.showMessageDialog(this, "Orçamento #" + orcamento.getId() + " salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                if (chkGerarPdf.isSelected()) {
                    util.PdfGenerator.gerarPdfOrcamento(orcamento);
                }
            }
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
