package view;

import dao.ClienteDAO;
import model.Cliente;

import javax.swing.*;
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
    private JTextField txtCidade;
    private JTextArea txtObservacoes;
    
    private JTextField txtPesquisa;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnLimpar;
    
    private Cliente clienteSelecionado = null;

    public ClienteScreen(Frame owner) {
        super(owner, "Cadastro de Clientes - TEC Energia", true);
        setSize(900, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 🔹 Painel Central (Tabela + Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(380);

        // Painel Superior (Pesquisa) - Será adicionado ao lado direito
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Pesquisar Cliente"));
        searchPanel.add(new JLabel("Nome:"));
        txtPesquisa = new JTextField(15);
        searchPanel.add(txtPesquisa);
        JButton btnPesquisar = new JButton("Pesquisar");
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
        JScrollPane scrollTable = new JScrollPane(table);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(searchPanel, BorderLayout.NORTH);
        rightPanel.add(scrollTable, BorderLayout.CENTER);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Formulário do Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtNome = new JTextField();
        formPanel.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtTelefone = new JTextField();
        formPanel.add(txtTelefone, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtEmail = new JTextField();
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Endereço:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        txtEndereco = new JTextField();
        formPanel.add(txtEndereco, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Cidade:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        txtCidade = new JTextField();
        formPanel.add(txtCidade, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Observações:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtObservacoes = new JTextArea(4, 20);
        txtObservacoes.setLineWrap(true);
        formPanel.add(new JScrollPane(txtObservacoes), gbc);

        // Painel de botões do Form
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Limpar");
        formButtons.add(btnSalvar);
        formButtons.add(btnExcluir);
        formButtons.add(btnLimpar);

        JPanel leftContainer = new JPanel(new BorderLayout());
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
                    txtCidade.setText(clienteSelecionado.getCidade() != null ? clienteSelecionado.getCidade() : "");
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
            if (clienteSelecionado == null) {
                // Inserir
                Cliente c = new Cliente(0, nome, telefone, txtEmail.getText().trim(), txtEndereco.getText().trim(), txtCidade.getText().trim(), txtObservacoes.getText().trim());
                dao.inserir(c);
                JOptionPane.showMessageDialog(this, "Cliente inserido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Atualizar
                clienteSelecionado.setNome(nome);
                clienteSelecionado.setTelefone(telefone);
                clienteSelecionado.setEmail(txtEmail.getText().trim());
                clienteSelecionado.setEndereco(txtEndereco.getText().trim());
                clienteSelecionado.setCidade(txtCidade.getText().trim());
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
        txtCidade.setText("");
        txtObservacoes.setText("");
        table.clearSelection();
    }
}
