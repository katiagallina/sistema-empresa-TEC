package view;

import dao.ServicoDAO;
import model.Servico;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ServicoScreen extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtNome;
    private JComboBox<String> cbTipo;
    private JTextField txtValorBase;

    private JTextField txtPesquisa;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnLimpar;

    private Servico servicoSelecionado = null;

    public ServicoScreen(Frame owner) {
        super(owner, "Cadastro de Serviços - TEC Energia", true);
        setSize(850, 540);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 🔹 Header Panel (Banner)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(235, 242, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblHeaderTitle = new JLabel("Cadastro de Serviços Base");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderTitle.setForeground(new Color(33, 37, 41));
        
        JLabel lblHeaderDesc = new JLabel("Defina a base de dados de serviços prestados e suas respectivas taxas de cobrança");
        lblHeaderDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHeaderDesc.setForeground(new Color(100, 110, 120));
        
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        headerPanel.add(lblHeaderDesc, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Painel Central (Tabela + Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(380);

        // Painel Superior (Pesquisa) - Será adicionado ao lado direito
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        TitledBorder searchTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Pesquisar Serviço",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        searchPanel.setBorder(BorderFactory.createCompoundBorder(searchTitle, BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        
        JLabel lblPesq = new JLabel("Nome:");
        lblPesq.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(lblPesq);
        
        txtPesquisa = new JTextField(15);
        txtPesquisa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(txtPesquisa);
        
        JButton btnPesquisar = new JButton("Pesquisar");
        btnPesquisar.setBackground(new Color(0, 102, 204));
        btnPesquisar.setForeground(Color.WHITE);
        btnPesquisar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPesquisar.setFocusPainted(false);
        searchPanel.add(btnPesquisar);

        // Tabela
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("ID");
        tableModel.addColumn("Nome");
        tableModel.addColumn("Tipo");
        tableModel.addColumn("Valor Base");

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(225, 235, 248));
        table.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollTable = new JScrollPane(table);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        rightPanel.add(searchPanel, BorderLayout.NORTH);
        rightPanel.add(scrollTable, BorderLayout.CENTER);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        TitledBorder formTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Formulário do Serviço",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        formPanel.setBorder(BorderFactory.createCompoundBorder(formTitle, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNome = new JLabel("Nome/Descrição:");
        lblNome.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblNome, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtNome = new JTextField();
        txtNome.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblTipo = new JLabel("Tipo Cobrança:");
        lblTipo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblTipo, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        cbTipo = new JComboBox<>(new String[]{"VALOR_FIXO", "POR_HORA"});
        cbTipo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(cbTipo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblValor = new JLabel("Valor Base (R$):");
        lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblValor, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtValorBase = new JTextField();
        txtValorBase.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtValorBase, gbc);

        // Botões do Form
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(new Color(40, 167, 69));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvar.setFocusPainted(false);
        
        btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(220, 53, 69));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExcluir.setFocusPainted(false);
        
        btnLimpar = new JButton("Limpar");
        btnLimpar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpar.setFocusPainted(false);
        
        formButtons.add(btnSalvar);
        formButtons.add(btnExcluir);
        formButtons.add(btnLimpar);

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        leftContainer.add(formPanel, BorderLayout.CENTER);
        leftContainer.add(formButtons, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftContainer);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // Ações
        btnPesquisar.addActionListener(e -> pesquisarServicos());
        txtPesquisa.addActionListener(e -> pesquisarServicos());
        btnSalvar.addActionListener(e -> salvarServico());
        btnExcluir.addActionListener(e -> excluirServico());
        btnLimpar.addActionListener(e -> limparCampos());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarServicoSelecionado();
            }
        });

        pesquisarServicos(); // Carregar inicial
    }

    private void pesquisarServicos() {
        String query = txtPesquisa.getText().trim();
        try {
            ServicoDAO dao = new ServicoDAO();
            List<Servico> lista;
            if (query.isEmpty()) {
                lista = dao.listar();
            } else {
                lista = dao.buscarPorNome(query);
            }

            tableModel.setRowCount(0);
            for (Servico s : lista) {
                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getNome(),
                        s.getTipo(),
                        String.format("R$ %.2f", s.getValorBase())
                });
            }
            limparCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao listar serviços: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarServicoSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) table.getValueAt(selectedRow, 0);
            try {
                servicoSelecionado = new ServicoDAO().buscarPorId(id);
                if (servicoSelecionado != null) {
                    txtNome.setText(servicoSelecionado.getNome());
                    cbTipo.setSelectedItem(servicoSelecionado.getTipo());
                    txtValorBase.setText(String.format("%.2f", servicoSelecionado.getValorBase()));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void salvarServico() {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome/Descrição é obrigatório.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double valorBase = Double.parseDouble(txtValorBase.getText().trim().replace(",", "."));
            ServicoDAO dao = new ServicoDAO();
            
            if (servicoSelecionado == null) {
                Servico s = new Servico(0, nome, (String) cbTipo.getSelectedItem(), valorBase);
                dao.inserir(s);
                JOptionPane.showMessageDialog(this, "Serviço inserido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                servicoSelecionado.setNome(nome);
                servicoSelecionado.setTipo((String) cbTipo.getSelectedItem());
                servicoSelecionado.setValorBase(valorBase);
                
                dao.atualizar(servicoSelecionado);
                JOptionPane.showMessageDialog(this, "Serviço atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            pesquisarServicos();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor Base inválido.", "Erro de Digitação", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar serviço: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirServico() {
        if (servicoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um serviço na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o serviço " + servicoSelecionado.getNome() + "?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                new ServicoDAO().deletar(servicoSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Serviço excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                pesquisarServicos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir serviço: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        servicoSelecionado = null;
        txtNome.setText("");
        txtValorBase.setText("");
        cbTipo.setSelectedIndex(0);
        table.clearSelection();
    }
}
