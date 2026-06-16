package view;

import dao.ServicoDAO;
import model.Servico;

import javax.swing.*;
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
        setSize(850, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Painel Superior (Pesquisa)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Pesquisar Serviço"));
        searchPanel.add(new JLabel("Nome:"));
        txtPesquisa = new JTextField(30);
        searchPanel.add(txtPesquisa);
        JButton btnPesquisar = new JButton("Pesquisar");
        searchPanel.add(btnPesquisar);
        add(searchPanel, BorderLayout.NORTH);

        // Painel Central (Tabela + Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);

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
        JScrollPane scrollTable = new JScrollPane(table);
        splitPane.setLeftComponent(scrollTable);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Formulário do Serviço"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nome/Descrição:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtNome = new JTextField();
        formPanel.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Tipo Cobrança:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        cbTipo = new JComboBox<>(new String[]{"VALOR_FIXO", "POR_HORA"});
        formPanel.add(cbTipo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Valor Base (R$):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtValorBase = new JTextField();
        formPanel.add(txtValorBase, gbc);

        // Botões do Form
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Limpar");
        formButtons.add(btnSalvar);
        formButtons.add(btnExcluir);
        formButtons.add(btnLimpar);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(formPanel, BorderLayout.CENTER);
        rightContainer.add(formButtons, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightContainer);
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
