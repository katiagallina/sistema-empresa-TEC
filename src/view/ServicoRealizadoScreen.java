package view;

import dao.ClienteDAO;
import dao.ServicoRealizadoDAO;
import dao.DespesaDAO;
import model.Cliente;
import model.ServicoRealizado;
import model.Despesa;
import util.PdfGenerator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
    private util.DatePicker spinData;
    private JTextField txtValor;
    private JComboBox<String> cbFormaPagamento;
    private JSpinner spinParcelas;
    private JTextField txtValorParcela;

    private util.DatePicker spinInicio;
    private util.DatePicker spinFim;

    private JLabel lblTotalGeral;
    private JLabel lblTotalDinheiro;
    private JLabel lblTotalPix;
    private JLabel lblTotalCheque;
    private JLabel lblTotalBoleto;

    private ServicoRealizado servicoSelecionado = null;
    private List<ServicoRealizado> servicosCarregados;

    // --- Componentes da Aba de Despesas ---
    private JTable tableDespesas;
    private DefaultTableModel tableModelDespesas;
    private JTextField txtDescricaoDespesa;
    private util.DatePicker spinDataDespesa;
    private JTextField txtValorDespesa;
    private JComboBox<String> cbFormaPagamentoDespesa;
    private Despesa despesaSelecionada = null;
    private List<Despesa> despesasCarregadas;

    public ServicoRealizadoScreen(Frame owner) {
        super(owner, "Serviços Realizados e Fluxo de Caixa - TEC Energia", true);
        setSize(1000, 720);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 🔹 1. Painel Superior (Header Banner + Filtro por Período)
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        // Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(235, 242, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblHeaderTitle = new JLabel("Lançamentos de Caixa (Entradas e Saídas)");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderTitle.setForeground(new Color(33, 37, 41));
        
        JLabel lblHeaderDesc = new JLabel("Controle a receita operacional de serviços prestados e registre saídas e despesas operacionais do caixa");
        lblHeaderDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHeaderDesc.setForeground(new Color(100, 110, 120));
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        headerPanel.add(lblHeaderDesc, BorderLayout.SOUTH);
        northPanel.add(headerPanel);

        // Filtro
        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        TitledBorder filterTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Filtrar por Período",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        filtroPanel.setBorder(BorderFactory.createCompoundBorder(filterTitle, BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JLabel lblIni = new JLabel("Data Início:");
        lblIni.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filtroPanel.add(lblIni);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        spinInicio = new util.DatePicker(cal.getTime());
        filtroPanel.add(spinInicio);

        JLabel lblFim = new JLabel("Data Fim:");
        lblFim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filtroPanel.add(lblFim);
        
        spinFim = new util.DatePicker(new Date());
        filtroPanel.add(spinFim);

        JButton btnFiltrar = new JButton("Filtrar Registros");
        btnFiltrar.setBackground(new Color(0, 102, 204));
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFiltrar.setFocusPainted(false);
        filtroPanel.add(btnFiltrar);

        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setBorder(BorderFactory.createEmptyBorder(5, 15, 0, 15));
        filterWrapper.add(filtroPanel, BorderLayout.CENTER);
        
        northPanel.add(filterWrapper);
        add(northPanel, BorderLayout.NORTH);

        // 🔹 2. JTabbedPane no Centro
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // ------------------ ABA 1: ENTRADAS (SERVIÇOS REALIZADOS) ------------------
        JSplitPane splitPaneEntradas = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPaneEntradas.setDividerLocation(520);

        // Tabela Entradas
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
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(225, 235, 248));
        table.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollTable = new JScrollPane(table);
        TitledBorder tblEntTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Serviços Lançados",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        scrollTable.setBorder(BorderFactory.createCompoundBorder(tblEntTitle, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        splitPaneEntradas.setLeftComponent(scrollTable);

        // Formulário lateral Entradas
        JPanel formPanel = new JPanel(new GridBagLayout());
        TitledBorder frmEntTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Lançamento de Serviço Realizado",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        formPanel.setBorder(BorderFactory.createCompoundBorder(frmEntTitle, BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblC1 = new JLabel("Cliente:");
        lblC1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblC1, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cbClientes = new JComboBox<>();
        cbClientes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        carregarClientes();
        formPanel.add(cbClientes, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblS1 = new JLabel("Serviço Prestado:");
        lblS1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblS1, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtDescricao = new JTextField();
        txtDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtDescricao, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblD1 = new JLabel("Data do Serviço:");
        lblD1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblD1, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        spinData = new util.DatePicker(new Date());
        formPanel.add(spinData, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        JLabel lblV1 = new JLabel("Valor Total (R$):");
        lblV1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblV1, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        txtValor = new JTextField();
        txtValor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtValor, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        JLabel lblF1 = new JLabel("Forma de Pagamento:");
        lblF1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblF1, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        cbFormaPagamento = new JComboBox<>(new String[]{"DINHEIRO", "PIX", "CHEQUE", "BOLETO"});
        cbFormaPagamento.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(cbFormaPagamento, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        JLabel lblP1 = new JLabel("Nº Parcelas:");
        lblP1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblP1, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        spinParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spinParcelas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spinParcelas.setEnabled(false); // Só ativa se for BOLETO
        formPanel.add(spinParcelas, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        JLabel lblVp1 = new JLabel("Valor da Parcela (R$):");
        lblVp1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblVp1, gbc);
        
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        txtValorParcela = new JTextField();
        txtValorParcela.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtValorParcela.setEditable(false);
        txtValorParcela.setBackground(new Color(240, 240, 240));
        formPanel.add(txtValorParcela, gbc);

        // Botões Entradas
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnSalvar = new JButton("Confirmar");
        btnSalvar.setBackground(new Color(40, 167, 69)); // Verde
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvar.setFocusPainted(false);
        
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(220, 53, 69)); // Vermelho
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExcluir.setFocusPainted(false);
        
        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpar.setFocusPainted(false);

        formButtons.add(btnSalvar);
        formButtons.add(btnExcluir);
        formButtons.add(btnLimpar);

        JPanel rightContainerEntradas = new JPanel(new BorderLayout());
        rightContainerEntradas.add(formPanel, BorderLayout.CENTER);
        rightContainerEntradas.add(formButtons, BorderLayout.SOUTH);

        splitPaneEntradas.setRightComponent(rightContainerEntradas);
        tabbedPane.addTab("Entradas (Serviços Realizados)", splitPaneEntradas);

        // ------------------ ABA 2: SAÍDAS (DESPESAS / PAGAMENTOS) ------------------
        JSplitPane splitPaneDespesas = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPaneDespesas.setDividerLocation(520);

        // Tabela Despesas
        tableModelDespesas = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModelDespesas.addColumn("Data");
        tableModelDespesas.addColumn("Descrição");
        tableModelDespesas.addColumn("Valor Pago");
        tableModelDespesas.addColumn("Forma Pgto");

        tableDespesas = new JTable(tableModelDespesas);
        tableDespesas.setRowHeight(25);
        tableDespesas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tableDespesas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDespesas.setSelectionBackground(new Color(225, 235, 248));
        tableDespesas.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollTableDespesas = new JScrollPane(tableDespesas);
        TitledBorder tblSaiTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Despesas Lançadas",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        scrollTableDespesas.setBorder(BorderFactory.createCompoundBorder(tblSaiTitle, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        splitPaneDespesas.setLeftComponent(scrollTableDespesas);

        // Formulário lateral Despesas
        JPanel formPanelDespesas = new JPanel(new GridBagLayout());
        TitledBorder frmSaiTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Lançamento de Despesa / Saída",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        formPanelDespesas.setBorder(BorderFactory.createCompoundBorder(frmSaiTitle, BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        GridBagConstraints gbcD = new GridBagConstraints();
        gbcD.insets = new Insets(8, 8, 8, 8);
        gbcD.fill = GridBagConstraints.HORIZONTAL;

        gbcD.gridx = 0; gbcD.gridy = 0;
        JLabel lblDescD = new JLabel("Descrição:");
        lblDescD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanelDespesas.add(lblDescD, gbcD);
        
        gbcD.gridx = 1; gbcD.gridy = 0; gbcD.weightx = 1.0;
        txtDescricaoDespesa = new JTextField();
        txtDescricaoDespesa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanelDespesas.add(txtDescricaoDespesa, gbcD);

        gbcD.gridx = 0; gbcD.gridy = 1; gbcD.weightx = 0.0;
        JLabel lblDtD = new JLabel("Data da Despesa:");
        lblDtD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanelDespesas.add(lblDtD, gbcD);
        
        gbcD.gridx = 1; gbcD.gridy = 1; gbcD.weightx = 1.0;
        spinDataDespesa = new util.DatePicker(new Date());
        formPanelDespesas.add(spinDataDespesa, gbcD);

        gbcD.gridx = 0; gbcD.gridy = 2; gbcD.weightx = 0.0;
        JLabel lblValD = new JLabel("Valor Pago (R$):");
        lblValD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanelDespesas.add(lblValD, gbcD);
        
        gbcD.gridx = 1; gbcD.gridy = 2; gbc.weightx = 1.0;
        txtValorDespesa = new JTextField();
        txtValorDespesa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanelDespesas.add(txtValorDespesa, gbcD);

        gbcD.gridx = 0; gbcD.gridy = 3; gbcD.weightx = 0.0;
        JLabel lblPgD = new JLabel("Forma de Pagamento:");
        lblPgD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanelDespesas.add(lblPgD, gbcD);
        
        gbcD.gridx = 1; gbcD.gridy = 3; gbcD.weightx = 1.0;
        cbFormaPagamentoDespesa = new JComboBox<>(new String[]{"DINHEIRO", "PIX", "CHEQUE", "BOLETO"});
        cbFormaPagamentoDespesa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanelDespesas.add(cbFormaPagamentoDespesa, gbcD);

        // Botões Despesas
        JPanel formButtonsDespesas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnSalvarDespesa = new JButton("Confirmar");
        btnSalvarDespesa.setBackground(new Color(40, 167, 69)); // Verde
        btnSalvarDespesa.setForeground(Color.WHITE);
        btnSalvarDespesa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvarDespesa.setFocusPainted(false);
        
        JButton btnExcluirDespesa = new JButton("Excluir");
        btnExcluirDespesa.setBackground(new Color(220, 53, 69)); // Vermelho
        btnExcluirDespesa.setForeground(Color.WHITE);
        btnExcluirDespesa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExcluirDespesa.setFocusPainted(false);
        
        JButton btnLimparDespesa = new JButton("Limpar");
        btnLimparDespesa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimparDespesa.setFocusPainted(false);

        formButtonsDespesas.add(btnSalvarDespesa);
        formButtonsDespesas.add(btnExcluirDespesa);
        formButtonsDespesas.add(btnLimparDespesa);

        JPanel rightContainerDespesas = new JPanel(new BorderLayout());
        rightContainerDespesas.add(formPanelDespesas, BorderLayout.CENTER);
        rightContainerDespesas.add(formButtonsDespesas, BorderLayout.SOUTH);

        splitPaneDespesas.setRightComponent(rightContainerDespesas);
        tabbedPane.addTab("Saídas (Despesas / Pagamentos)", splitPaneDespesas);

        add(tabbedPane, BorderLayout.CENTER);

        // 🔹 3. Painel Inferior (Fluxo de Caixa Totais + Exportar PDF)
        JPanel bottomContainer = new JPanel(new BorderLayout(5, 5));
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));

        // Tabela/Resumo Financeiro (Fluxo de Caixa)
        JPanel resumoPanel = new JPanel(new GridLayout(1, 5, 15, 5));
        TitledBorder totalTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Fluxo de Caixa do Período (Líquido)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11),
            new Color(55, 71, 79)
        );
        resumoPanel.setBorder(BorderFactory.createCompoundBorder(totalTitle, BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        lblTotalDinheiro = createResumoLabel("Dinheiro: R$ 0,00", new Color(0, 102, 204));
        lblTotalPix = createResumoLabel("PIX: R$ 0,00", new Color(0, 102, 204));
        lblTotalCheque = createResumoLabel("Cheque: R$ 0,00", new Color(0, 102, 204));
        lblTotalBoleto = createResumoLabel("Boleto (Parcelas): R$ 0,00", new Color(0, 102, 204));
        lblTotalGeral = createResumoLabel("Lucro Líquido Real: R$ 0,00", new Color(40, 167, 69));

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
        btnPdf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPdf.setFocusPainted(false);
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

        // Listeners da Aba de Despesas
        btnSalvarDespesa.addActionListener(e -> salvarDespesa());
        btnExcluirDespesa.addActionListener(e -> excluirDespesa());
        btnLimparDespesa.addActionListener(e -> limparCamposDespesa());

        tableDespesas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarDespesaSelecionada();
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
        tableModelDespesas.setRowCount(0);
        Date inicio = spinInicio.getValue();
        Date fim = spinFim.getValue();

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

            // Carrega as despesas
            DespesaDAO despesaDAO = new DespesaDAO();
            despesasCarregadas = despesaDAO.listarPorPeriodo(inicio, fim);

            for (Despesa d : despesasCarregadas) {
                tableModelDespesas.addRow(new Object[]{
                        d.getDataDespesa() != null ? sdf.format(d.getDataDespesa()) : "",
                        d.getDescricao(),
                        String.format("R$ %.2f", d.getValor()),
                        d.getFormaPagamento()
                });
            }

            // --- CÁLCULO DO FLUXO DO CAIXA LÍQUIDO ---
            double entDinheiro = dao.getSomaPorFormaPagamento("DINHEIRO", inicio, fim);
            double entPix = dao.getSomaPorFormaPagamento("PIX", inicio, fim);
            double entCheque = dao.getSomaPorFormaPagamento("CHEQUE", inicio, fim);
            double entBoleto = dao.getSomaPorFormaPagamento("BOLETO", inicio, fim);
            double entTotal = dao.getFaturamentoTotal(inicio, fim);

            double saiDinheiro = despesaDAO.getSomaPorFormaPagamento("DINHEIRO", inicio, fim);
            double saiPix = despesaDAO.getSomaPorFormaPagamento("PIX", inicio, fim);
            double saiCheque = despesaDAO.getSomaPorFormaPagamento("CHEQUE", inicio, fim);
            double saiBoleto = despesaDAO.getSomaPorFormaPagamento("BOLETO", inicio, fim);
            double saiTotal = despesaDAO.getSomaTotal(inicio, fim);

            double saldoDinheiro = entDinheiro - saiDinheiro;
            double saldoPix = entPix - saiPix;
            double saldoCheque = entCheque - saiCheque;
            double saldoBoleto = entBoleto - saiBoleto;
            double lucroLiquido = entTotal - saiTotal;

            lblTotalDinheiro.setText(String.format("Dinheiro: R$ %.2f", saldoDinheiro));
            lblTotalPix.setText(String.format("PIX: R$ %.2f", saldoPix));
            lblTotalCheque.setText(String.format("Cheque: R$ %.2f", saldoCheque));
            lblTotalBoleto.setText(String.format("Boleto (Parcelas): R$ %.2f", saldoBoleto));
            lblTotalGeral.setText(String.format("Lucro Líquido Real: R$ %.2f", lucroLiquido));

            if (lucroLiquido >= 0) {
                lblTotalGeral.setForeground(new Color(40, 167, 69)); // Verde
            } else {
                lblTotalGeral.setForeground(new Color(220, 53, 69)); // Vermelho
            }

            limparCampos();
            limparCamposDespesa();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao listar lançamentos e calcular fluxo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
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
            Date data = spinData.getValue();

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

    // --- Métodos de Controle da Aba de Despesas ---
    private void carregarDespesaSelecionada() {
        int selectedRow = tableDespesas.getSelectedRow();
        if (selectedRow != -1 && despesasCarregadas != null && selectedRow < despesasCarregadas.size()) {
            despesaSelecionada = despesasCarregadas.get(selectedRow);
            txtDescricaoDespesa.setText(despesaSelecionada.getDescricao());
            spinDataDespesa.setValue(new Date(despesaSelecionada.getDataDespesa().getTime()));
            txtValorDespesa.setText(String.format("%.2f", despesaSelecionada.getValor()));
            cbFormaPagamentoDespesa.setSelectedItem(despesaSelecionada.getFormaPagamento());
        }
    }

    private void salvarDespesa() {
        String desc = txtDescricaoDespesa.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A descrição da despesa é obrigatória.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double valor = Double.parseDouble(txtValorDespesa.getText().trim().replace(",", "."));
            String pgto = (String) cbFormaPagamentoDespesa.getSelectedItem();
            Date data = spinDataDespesa.getValue();

            DespesaDAO dao = new DespesaDAO();

            if (despesaSelecionada == null) {
                // Novo
                Despesa d = new Despesa(0, new Timestamp(data.getTime()), desc, valor, pgto);
                dao.inserir(d);
                JOptionPane.showMessageDialog(this, "Despesa lançada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Atualizar
                despesaSelecionada.setDescricao(desc);
                despesaSelecionada.setDataDespesa(new Timestamp(data.getTime()));
                despesaSelecionada.setValor(valor);
                despesaSelecionada.setFormaPagamento(pgto);

                dao.atualizar(despesaSelecionada);
                JOptionPane.showMessageDialog(this, "Despesa atualizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            carregarServicos();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Insira um valor numérico válido para a despesa.", "Erro de Digitação", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar despesa: " + e.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirDespesa() {
        if (despesaSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Deseja excluir a despesa selecionada?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                new DespesaDAO().deletar(despesaSelecionada.getId());
                JOptionPane.showMessageDialog(this, "Despesa excluída com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarServicos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir despesa: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCamposDespesa() {
        despesaSelecionada = null;
        txtDescricaoDespesa.setText("");
        txtValorDespesa.setText("");
        cbFormaPagamentoDespesa.setSelectedIndex(0);
        spinDataDespesa.setValue(new Date());
        tableDespesas.clearSelection();
    }

    private void exportarPdf() {
        if (servicosCarregados == null && despesasCarregadas == null) {
            JOptionPane.showMessageDialog(this, "Nenhum lançamento no período atual para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date inicio = spinInicio.getValue();
        Date fim = spinFim.getValue();

        try {
            ServicoRealizadoDAO dao = new ServicoRealizadoDAO();
            double entDinheiro = dao.getSomaPorFormaPagamento("DINHEIRO", inicio, fim);
            double entPix = dao.getSomaPorFormaPagamento("PIX", inicio, fim);
            double entCheque = dao.getSomaPorFormaPagamento("CHEQUE", inicio, fim);
            double entBoleto = dao.getSomaPorFormaPagamento("BOLETO", inicio, fim);
            double entTotal = dao.getFaturamentoTotal(inicio, fim);

            DespesaDAO despesaDAO = new DespesaDAO();
            double saiDinheiro = despesaDAO.getSomaPorFormaPagamento("DINHEIRO", inicio, fim);
            double saiPix = despesaDAO.getSomaPorFormaPagamento("PIX", inicio, fim);
            double saiCheque = despesaDAO.getSomaPorFormaPagamento("CHEQUE", inicio, fim);
            double saiBoleto = despesaDAO.getSomaPorFormaPagamento("BOLETO", inicio, fim);
            double saiTotal = despesaDAO.getSomaTotal(inicio, fim);

            PdfGenerator.gerarPdfRelatorioServicosRealizados(
                    servicosCarregados,
                    despesasCarregadas,
                    entDinheiro, entPix, entCheque, entBoleto, entTotal,
                    saiDinheiro, saiPix, saiCheque, saiBoleto, saiTotal,
                    inicio, fim
            );

            JOptionPane.showMessageDialog(this, "Relatório de Fluxo de Caixa Líquido PDF gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao calcular valores para o PDF: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
