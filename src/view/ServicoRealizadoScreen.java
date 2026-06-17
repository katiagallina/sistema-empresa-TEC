package view;

import dao.ClienteDAO;
import dao.ServicoRealizadoDAO;
import model.Cliente;
import model.ServicoRealizado;
import util.PdfGenerator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ServicoRealizadoScreen extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;

    private JComboBox<Cliente> cbClientes;
    private JTextField txtDescricao;
    private JSpinner spinData;
    private JTextField txtValor;
    private JComboBox<String> cbFormaPagamento;
    private JSpinner spinParcelas;
    private JTextField txtValorParcela;

    private JSpinner spinInicio;
    private JSpinner spinFim;

    private JLabel lblTotalGeral;
    private JLabel lblTotalDinheiro;
    private JLabel lblTotalPix;
    private JLabel lblTotalCheque;
    private JLabel lblTotalBoleto;

    private ServicoRealizado servicoSelecionado = null;
    private List<ServicoRealizado> servicosCarregados;

    public ServicoRealizadoScreen(Frame owner) {
        super(owner, "Serviços Realizados e Fluxo de Caixa - TEC Energia", true);
        setSize(1000, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 🔹 1. Painel Superior (Filtro por Período)
        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        filtroPanel.setBorder(BorderFactory.createTitledBorder("Filtrar por Período"));

        filtroPanel.add(new JLabel("Data Início:"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        spinInicio = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        spinInicio.setEditor(new JSpinner.DateEditor(spinInicio, "dd/MM/yyyy"));
        filtroPanel.add(spinInicio);

        filtroPanel.add(new JLabel("Data Fim:"));
        spinFim = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinFim.setEditor(new JSpinner.DateEditor(spinFim, "dd/MM/yyyy"));
        filtroPanel.add(spinFim);

        JButton btnFiltrar = new JButton("Filtrar Serviços");
        btnFiltrar.setBackground(new Color(0, 102, 204));
        btnFiltrar.setForeground(Color.WHITE);
        filtroPanel.add(btnFiltrar);

        add(filtroPanel, BorderLayout.NORTH);

        // 🔹 2. Painel Central (Dividido em Tabela à esquerda e Formulário à direita)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(520);

        // Tabela
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("Data");
        tableModel.addColumn("Cliente");
        tableModel.addColumn("Serviço Prestado");
        tableModel.addColumn("Valor");
        tableModel.addColumn("Forma Pgto");
        tableModel.addColumn("Parc.");
        tableModel.addColumn("Vlr Parcela");

        table = new JTable(tableModel);
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Serviços Lançados"));
        splitPane.setLeftComponent(scrollTable);

        // Formulário lateral
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Lançamento de Serviço Realizado"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cbClientes = new JComboBox<>();
        carregarClientes();
        formPanel.add(cbClientes, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Serviço Prestado:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtDescricao = new JTextField();
        formPanel.add(txtDescricao, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Data do Serviço:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        spinData = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinData.setEditor(new JSpinner.DateEditor(spinData, "dd/MM/yyyy"));
        formPanel.add(spinData, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Valor Total (R$):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        txtValor = new JTextField();
        formPanel.add(txtValor, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Forma de Pagamento:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        cbFormaPagamento = new JComboBox<>(new String[]{"DINHEIRO", "PIX", "CHEQUE", "BOLETO"});
        formPanel.add(cbFormaPagamento, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Nº Parcelas:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        spinParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spinParcelas.setEnabled(false); // Só ativa se for BOLETO
        formPanel.add(spinParcelas, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Valor da Parcela (R$):"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        txtValorParcela = new JTextField();
        txtValorParcela.setEditable(false);
        txtValorParcela.setBackground(new Color(240, 240, 240));
        formPanel.add(txtValorParcela, gbc);

        // Botões do Form
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Confirmar");
        btnSalvar.setBackground(new Color(40, 167, 69)); // Verde
        btnSalvar.setForeground(Color.WHITE);
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(220, 53, 69)); // Vermelho
        btnExcluir.setForeground(Color.WHITE);
        JButton btnLimpar = new JButton("Limpar");

        formButtons.add(btnSalvar);
        formButtons.add(btnExcluir);
        formButtons.add(btnLimpar);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(formPanel, BorderLayout.CENTER);
        rightContainer.add(formButtons, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightContainer);
        add(splitPane, BorderLayout.CENTER);

        // 🔹 3. Painel Inferior (Fluxo de Caixa Totais + Exportar PDF)
        JPanel bottomContainer = new JPanel(new BorderLayout(5, 5));
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Tabela/Resumo Financeiro (Fluxo de Caixa)
        JPanel resumoPanel = new JPanel(new GridLayout(1, 5, 15, 5));
        resumoPanel.setBorder(BorderFactory.createTitledBorder("Fluxo de Caixa do Período"));
        
        lblTotalDinheiro = createResumoLabel("Dinheiro: R$ 0,00", new Color(0, 102, 204));
        lblTotalPix = createResumoLabel("PIX: R$ 0,00", new Color(0, 102, 204));
        lblTotalCheque = createResumoLabel("Cheque: R$ 0,00", new Color(0, 102, 204));
        lblTotalBoleto = createResumoLabel("Boleto (Parcelas): R$ 0,00", new Color(0, 102, 204));
        lblTotalGeral = createResumoLabel("Faturamento Total: R$ 0,00", new Color(40, 167, 69));

        resumoPanel.add(lblTotalDinheiro);
        resumoPanel.add(lblTotalPix);
        resumoPanel.add(lblTotalCheque);
        resumoPanel.add(lblTotalBoleto);
        resumoPanel.add(lblTotalGeral);
        bottomContainer.add(resumoPanel, BorderLayout.CENTER);

        // Botão Exportar PDF
        JPanel pdfPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPdf = new JButton("Exportar Excel/Relatório para PDF");
        btnPdf.setBackground(new Color(220, 53, 69)); // Vermelho
        btnPdf.setForeground(Color.WHITE);
        btnPdf.setFont(new Font("Arial", Font.BOLD, 12));
        pdfPanel.add(btnPdf);
        bottomContainer.add(pdfPanel, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);

        // 🔹 4. Listeners e Lógica
        btnFiltrar.addActionListener(e -> carregarServicos());
        btnSalvar.addActionListener(e -> salvarServico());
        btnExcluir.addActionListener(e -> excluirServico());
        btnLimpar.addActionListener(e -> limparCampos());
        btnPdf.addActionListener(e -> exportarPdf());

        cbFormaPagamento.addActionListener(e -> {
            boolean isBoleto = "BOLETO".equals(cbFormaPagamento.getSelectedItem());
            spinParcelas.setEnabled(isBoleto);
            if (!isBoleto) {
                spinParcelas.setValue(1);
            }
            calcularValorParcela();
        });

        // Atualizar valor da parcela automaticamente ao alterar valor total ou nº parcelas
        txtValor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calcularValorParcela(); }
            public void removeUpdate(DocumentEvent e) { calcularValorParcela(); }
            public void changedUpdate(DocumentEvent e) { calcularValorParcela(); }
        });
        spinParcelas.addChangeListener(e -> calcularValorParcela());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarServicoSelecionado();
            }
        });

        carregarServicos();
    }

    private JLabel createResumoLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return label;
    }

    private void carregarClientes() {
        try {
            List<Cliente> lista = new ClienteDAO().listar();
            cbClientes.removeAllItems();
            for (Cliente c : lista) {
                cbClientes.addItem(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calcularValorParcela() {
        try {
            double total = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
            int parcelas = (int) spinParcelas.getValue();
            double parcelaVal = total / parcelas;
            txtValorParcela.setText(String.format("%.2f", parcelaVal));
        } catch (Exception e) {
            txtValorParcela.setText("0.00");
        }
    }

    private void carregarServicos() {
        tableModel.setRowCount(0);
        Date inicio = (Date) spinInicio.getValue();
        Date fim = (Date) spinFim.getValue();

        try {
            ServicoRealizadoDAO dao = new ServicoRealizadoDAO();
            servicosCarregados = dao.listarPorPeriodo(inicio, fim);
            ClienteDAO clienteDAO = new ClienteDAO();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            for (ServicoRealizado sr : servicosCarregados) {
                Cliente c = clienteDAO.buscarPorId(sr.getClienteId());
                String clienteNome = c != null ? c.getNome() : "ID: " + sr.getClienteId();
                tableModel.addRow(new Object[]{
                        sr.getDataServico() != null ? sdf.format(sr.getDataServico()) : "",
                        clienteNome,
                        sr.getDescricaoServico(),
                        String.format("R$ %.2f", sr.getValor()),
                        sr.getFormaPagamento(),
                        sr.getNumParcelas(),
                        String.format("R$ %.2f", sr.getValorParcela())
                });
            }

            // Atualizar Fluxo de Caixa Totais
            double totalDinheiro = dao.getSomaPorFormaPagamento("DINHEIRO", inicio, fim);
            double totalPix = dao.getSomaPorFormaPagamento("PIX", inicio, fim);
            double totalCheque = dao.getSomaPorFormaPagamento("CHEQUE", inicio, fim);
            double totalBoleto = dao.getSomaPorFormaPagamento("BOLETO", inicio, fim);
            double totalGeral = dao.getFaturamentoTotal(inicio, fim);

            lblTotalDinheiro.setText(String.format("Dinheiro: R$ %.2f", totalDinheiro));
            lblTotalPix.setText(String.format("PIX: R$ %.2f", totalPix));
            lblTotalCheque.setText(String.format("Cheque: R$ %.2f", totalCheque));
            lblTotalBoleto.setText(String.format("Boleto (Parcelas): R$ %.2f", totalBoleto));
            lblTotalGeral.setText(String.format("Faturamento Total: R$ %.2f", totalGeral));

            limparCampos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao listar serviços realizados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void carregarServicoSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && servicosCarregados != null && selectedRow < servicosCarregados.size()) {
            servicoSelecionado = servicosCarregados.get(selectedRow);
            
            // Localizar cliente no ComboBox
            for (int i = 0; i < cbClientes.getItemCount(); i++) {
                if (cbClientes.getItemAt(i).getId() == servicoSelecionado.getClienteId()) {
                    cbClientes.setSelectedIndex(i);
                    break;
                }
            }
            
            txtDescricao.setText(servicoSelecionado.getDescricaoServico());
            spinData.setValue(new Date(servicoSelecionado.getDataServico().getTime()));
            txtValor.setText(String.format("%.2f", servicoSelecionado.getValor()));
            cbFormaPagamento.setSelectedItem(servicoSelecionado.getFormaPagamento());
            spinParcelas.setValue(servicoSelecionado.getNumParcelas());
            txtValorParcela.setText(String.format("%.2f", servicoSelecionado.getValorParcela()));
        }
    }

    private void salvarServico() {
        Cliente cliente = (Cliente) cbClientes.getSelectedItem();
        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Selecione ou cadastre um cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String desc = txtDescricao.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A descrição do serviço é obrigatória.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
            String pgto = (String) cbFormaPagamento.getSelectedItem();
            int parcelas = (int) spinParcelas.getValue();
            double vlrParcela = Double.parseDouble(txtValorParcela.getText().trim().replace(",", "."));
            Date data = (Date) spinData.getValue();

            ServicoRealizadoDAO dao = new ServicoRealizadoDAO();

            if (servicoSelecionado == null) {
                // Novo
                ServicoRealizado sr = new ServicoRealizado(0, new Timestamp(data.getTime()), cliente.getId(), desc, valor, pgto, parcelas, vlrParcela);
                dao.inserir(sr);
                JOptionPane.showMessageDialog(this, "Serviço realizado lançado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Atualizar
                servicoSelecionado.setClienteId(cliente.getId());
                servicoSelecionado.setDescricaoServico(desc);
                servicoSelecionado.setDataServico(new Timestamp(data.getTime()));
                servicoSelecionado.setValor(valor);
                servicoSelecionado.setFormaPagamento(pgto);
                servicoSelecionado.setNumParcelas(parcelas);
                servicoSelecionado.setValorParcela(vlrParcela);

                dao.atualizar(servicoSelecionado);
                JOptionPane.showMessageDialog(this, "Lançamento atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            carregarServicos();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Insira um valor numérico válido para o valor total.", "Erro de Digitação", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar serviço realizado: " + e.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirServico() {
        if (servicoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um lançamento na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Deseja excluir o lançamento selecionado?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                new ServicoRealizadoDAO().deletar(servicoSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Lançamento excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarServicos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        servicoSelecionado = null;
        txtDescricao.setText("");
        txtValor.setText("");
        cbFormaPagamento.setSelectedIndex(0);
        spinParcelas.setValue(1);
        spinParcelas.setEnabled(false);
        txtValorParcela.setText("0.00");
        spinData.setValue(new Date());
        table.clearSelection();
    }

    private void exportarPdf() {
        if (servicosCarregados == null || servicosCarregados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum serviço lançado no período atual para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date inicio = (Date) spinInicio.getValue();
        Date fim = (Date) spinFim.getValue();

        try {
            ServicoRealizadoDAO dao = new ServicoRealizadoDAO();
            double totalDinheiro = dao.getSomaPorFormaPagamento("DINHEIRO", inicio, fim);
            double totalPix = dao.getSomaPorFormaPagamento("PIX", inicio, fim);
            double totalCheque = dao.getSomaPorFormaPagamento("CHEQUE", inicio, fim);
            double totalBoleto = dao.getSomaPorFormaPagamento("BOLETO", inicio, fim);
            double totalGeral = dao.getFaturamentoTotal(inicio, fim);

            PdfGenerator.gerarPdfRelatorioServicosRealizados(
                    servicosCarregados,
                    totalDinheiro, totalPix, totalCheque, totalBoleto, totalGeral,
                    inicio, fim
            );

            JOptionPane.showMessageDialog(this, "Relatório de Serviços Realizados e Fluxo de Caixa PDF gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao calcular valores para o PDF: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
