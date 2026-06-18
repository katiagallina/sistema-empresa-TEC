package view;

import dao.ClienteDAO;
import model.Cliente;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ClienteScreen extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;
    
    private JTextField txtNome;
    private JTextField txtTelefone;
    private JTextField txtEmail;
    private JTextField txtEndereco;
    private JComboBox<String> cbCidade;
    private JTextArea txtObservacoes;
    
    private JTextField txtPesquisa;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnLimpar;
    
    private Cliente clienteSelecionado = null;

    public ClienteScreen(Frame owner) {
        super(owner, "Cadastro de Clientes - TEC Energia", true);
        setSize(950, 640);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 🔹 Header Panel (Banner)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(235, 242, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblHeaderTitle = new JLabel("Cadastro de Clientes");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderTitle.setForeground(new Color(33, 37, 41));
        
        JLabel lblHeaderDesc = new JLabel("Gerencie as informações dos clientes, telefones, endereços e anotações");
        lblHeaderDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHeaderDesc.setForeground(new Color(100, 110, 120));
        
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        headerPanel.add(lblHeaderDesc, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 🔹 Painel Central (Tabela + Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(380);

        // Painel Superior (Pesquisa) - Será adicionado ao lado direito
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        TitledBorder searchTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Pesquisar Cliente",
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
        tableModel.addColumn("Telefone");
        tableModel.addColumn("Email");
        tableModel.addColumn("Cidade");

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
            "Formulário do Cliente",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        formPanel.setBorder(BorderFactory.createCompoundBorder(formTitle, BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblNome, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtNome = new JTextField();
        txtNome.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblTel = new JLabel("Telefone:");
        lblTel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblTel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtTelefone = new JTextField();
        txtTelefone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtTelefone, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblEm = new JLabel("Email:");
        lblEm.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblEm, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        JLabel lblEnd = new JLabel("Endereço:");
        lblEnd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblEnd, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        txtEndereco = new JTextField();
        txtEndereco.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtEndereco, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        JLabel lblCid = new JLabel("Cidade:");
        lblCid.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblCid, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        cbCidade = new JComboBox<>(new String[]{
            "Getúlio Vargas", "Erechim", "Passo Fundo", "Sertão", "Estação", 
            "Erebango", "Ipiranga do Sul", "Tapejara", "Charrua", "Floriano Peixoto"
        });
        cbCidade.setEditable(true);
        cbCidade.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(cbCidade, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        JLabel lblObs = new JLabel("Observações:");
        lblObs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblObs, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtObservacoes = new JTextArea(4, 20);
        txtObservacoes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtObservacoes.setLineWrap(true);
        formPanel.add(new JScrollPane(txtObservacoes), gbc);

        // Painel de botões do Form
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

        // 🔹 Ações
        btnPesquisar.addActionListener(e -> pesquisarClientes());
        txtPesquisa.addActionListener(e -> pesquisarClientes());
        btnSalvar.addActionListener(e -> salvarCliente());
        btnExcluir.addActionListener(e -> excluirCliente());
        btnLimpar.addActionListener(e -> limparCampos());
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarClienteSelecionado();
            }
        });

        pesquisarClientes(); // Carregar inicial
    }

    private void pesquisarClientes() {
        String query = txtPesquisa.getText().trim();
        try {
            ClienteDAO dao = new ClienteDAO();
            List<Cliente> lista;
            if (query.isEmpty()) {
                lista = dao.listar();
            } else {
                lista = dao.buscarPorNome(query);
            }

            tableModel.setRowCount(0);
            for (Cliente c : lista) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getNome(),
                        c.getTelefone(),
                        c.getEmail(),
                        c.getCidade()
                });
            }
            limparCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao listar clientes: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarClienteSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) table.getValueAt(selectedRow, 0);
            try {
                clienteSelecionado = new ClienteDAO().buscarPorId(id);
                if (clienteSelecionado != null) {
                    txtNome.setText(clienteSelecionado.getNome());
                    txtTelefone.setText(clienteSelecionado.getTelefone());
                    txtEmail.setText(clienteSelecionado.getEmail() != null ? clienteSelecionado.getEmail() : "");
                    txtEndereco.setText(clienteSelecionado.getEndereco() != null ? clienteSelecionado.getEndereco() : "");
                    cbCidade.setSelectedItem(clienteSelecionado.getCidade() != null ? clienteSelecionado.getCidade() : "");
                    txtObservacoes.setText(clienteSelecionado.getObservacoes() != null ? clienteSelecionado.getObservacoes() : "");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void salvarCliente() {
        String nome = txtNome.getText().trim();
        String telefone = txtTelefone.getText().trim();
        if (nome.isEmpty() || telefone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome e Telefone são campos obrigatórios.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ClienteDAO dao = new ClienteDAO();
            String cidade = (cbCidade.getSelectedItem() != null) ? cbCidade.getSelectedItem().toString().trim() : "";
            if (clienteSelecionado == null) {
                // Inserir
                Cliente c = new Cliente(0, nome, telefone, txtEmail.getText().trim(), txtEndereco.getText().trim(), cidade, txtObservacoes.getText().trim());
                dao.inserir(c);
                JOptionPane.showMessageDialog(this, "Cliente inserido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Atualizar
                clienteSelecionado.setNome(nome);
                clienteSelecionado.setTelefone(telefone);
                clienteSelecionado.setEmail(txtEmail.getText().trim());
                clienteSelecionado.setEndereco(txtEndereco.getText().trim());
                clienteSelecionado.setCidade(cidade);
                clienteSelecionado.setObservacoes(txtObservacoes.getText().trim());
                
                dao.atualizar(clienteSelecionado);
                JOptionPane.showMessageDialog(this, "Cliente atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            pesquisarClientes();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirCliente() {
        if (clienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o cliente " + clienteSelecionado.getNome() + "?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                new ClienteDAO().deletar(clienteSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Cliente excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                pesquisarClientes();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir cliente (ele pode estar vinculado a um orçamento/OS): " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        clienteSelecionado = null;
        txtNome.setText("");
        txtTelefone.setText("");
        txtEmail.setText("");
        txtEndereco.setText("");
        cbCidade.setSelectedItem("");
        txtObservacoes.setText("");
        table.clearSelection();
    }
}
