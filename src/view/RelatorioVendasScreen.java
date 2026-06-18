package view;

import dao.VendaDAO;
import dao.ProdutoDAO;
import dao.ClienteDAO;
import dao.OrdemServicoDAO;
import dao.ItemOrdemServicoDAO;
import model.Venda;
import model.Produto;
import model.Cliente;
import model.OrdemServico;
import util.PdfGenerator;
import util.DatePicker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RelatorioVendasScreen extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTable osTable;
    private DefaultTableModel osTableModel;
    private JButton buscarButton;
    private DatePicker dataInicioPicker;
    private DatePicker dataFimPicker;
    private JLabel totalVendasLabel;
    private JLabel lucroTotalLabel;

    private List<Venda> vendasCarregadas;

    public RelatorioVendasScreen() {
        setTitle("Relatórios Financeiros e Operacionais - TEC Energia");
        setSize(980, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 🔹 Header Panel (Banner)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(235, 242, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblHeaderTitle = new JLabel("Relatórios e Faturamento");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderTitle.setForeground(new Color(33, 37, 41));
        
        JLabel lblHeaderDesc = new JLabel("Monitore o faturamento, lucro líquido e faça a gestão de pagamentos de Ordens de Serviço (OS)");
        lblHeaderDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHeaderDesc.setForeground(new Color(100, 110, 120));
        
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        headerPanel.add(lblHeaderDesc, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 🔹 Painel Principal com BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        // 🔹 Painel de Filtros (Compartilhado para ambas as abas)
        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        TitledBorder filtroTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Filtrar por Período",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        filtroPanel.setBorder(BorderFactory.createCompoundBorder(filtroTitle, BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        
        JLabel lblDataInicio = new JLabel("Data Início:");
        lblDataInicio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filtroPanel.add(lblDataInicio);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // Filtro inicial: último mês
        Date dataInicioPadrao = cal.getTime();
        
        dataInicioPicker = new DatePicker(dataInicioPadrao);
        dataInicioPicker.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filtroPanel.add(dataInicioPicker);
 
        JLabel lblDataFim = new JLabel("Data Fim:");
        lblDataFim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filtroPanel.add(lblDataFim);
        
        dataFimPicker = new DatePicker(new Date());
        dataFimPicker.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filtroPanel.add(dataFimPicker);

        buscarButton = new JButton("Filtrar Dados");
        buscarButton.setBackground(new Color(0, 102, 204));
        buscarButton.setForeground(Color.WHITE);
        buscarButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        buscarButton.setFocusPainted(false);
        filtroPanel.add(buscarButton);

        mainPanel.add(filtroPanel, BorderLayout.NORTH);

        // 🔹 Criação das Abas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // ── ABA 1: ITENS VENDIDOS ──
        JPanel panelVendas = new JPanel(new BorderLayout(10, 10));
        
        // Tabela de Vendas
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("Data/Hora");
        tableModel.addColumn("Item / Descrição");
        tableModel.addColumn("Tipo");
        tableModel.addColumn("Quantidade");
        tableModel.addColumn("Valor Total");
        tableModel.addColumn("Custo Total");
        tableModel.addColumn("Lucro Líquido");
        tableModel.addColumn("Forma Pagamento");

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(225, 235, 248));
        table.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollVendas = new JScrollPane(table);
        TitledBorder vendasTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Detalhamento de Vendas Diárias",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        scrollVendas.setBorder(BorderFactory.createCompoundBorder(vendasTitle, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelVendas.add(scrollVendas, BorderLayout.CENTER);

        // Painel de Totais e PDF
        JPanel sulPanel = new JPanel(new BorderLayout(5, 5));
        
        JPanel totaisPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 8));
        totaisPanel.setBackground(new Color(248, 249, 250));
        totaisPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(225, 230, 235)),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        
        totalVendasLabel = new JLabel("Total Vendas: R$ 0,00");
        totalVendasLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalVendasLabel.setForeground(new Color(55, 71, 79));
        
        lucroTotalLabel = new JLabel("Lucro Total: R$ 0,00");
        lucroTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lucroTotalLabel.setForeground(new Color(40, 167, 69));
        
        totaisPanel.add(totalVendasLabel);
        totaisPanel.add(lucroTotalLabel);
        sulPanel.add(totaisPanel, BorderLayout.NORTH);

        JPanel pdfPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        TitledBorder pdfBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Exportar Relatórios PDF",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        pdfPanel.setBorder(BorderFactory.createCompoundBorder(pdfBorder, BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        
        JButton btnPdfVendas = new JButton("PDF Vendas e Lucro (Período)");
        btnPdfVendas.setBackground(new Color(220, 53, 69)); // Vermelho PDF
        btnPdfVendas.setForeground(Color.WHITE);
        btnPdfVendas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPdfVendas.setFocusPainted(false);
        
        JButton btnPdfProdutos = new JButton("PDF Geral de Produtos");
        btnPdfProdutos.setBackground(new Color(220, 53, 69));
        btnPdfProdutos.setForeground(Color.WHITE);
        btnPdfProdutos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPdfProdutos.setFocusPainted(false);
        
        JButton btnPdfClientes = new JButton("PDF Geral de Clientes");
        btnPdfClientes.setBackground(new Color(220, 53, 69));
        btnPdfClientes.setForeground(Color.WHITE);
        btnPdfClientes.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPdfClientes.setFocusPainted(false);

        pdfPanel.add(btnPdfVendas);
        pdfPanel.add(btnPdfProdutos);
        pdfPanel.add(btnPdfClientes);
        
        sulPanel.add(pdfPanel, BorderLayout.CENTER);
        panelVendas.add(sulPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Itens Vendidos (Faturamento)", panelVendas);

        // ── ABA 2: GESTÃO DE ORDENS DE SERVIÇO (OS) ──
        JPanel panelOS = new JPanel(new BorderLayout(10, 10));

        // Tabela de OS
        osTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        osTableModel.addColumn("ID OS");
        osTableModel.addColumn("Data/Hora");
        osTableModel.addColumn("Cliente");
        osTableModel.addColumn("Valor Total");
        osTableModel.addColumn("Status de Pagamento");

        osTable = new JTable(osTableModel);
        osTable.setRowHeight(25);
        osTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        osTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        osTable.setSelectionBackground(new Color(225, 235, 248));
        osTable.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollOS = new JScrollPane(osTable);
        TitledBorder osTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Ordens de Serviço do Período",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        scrollOS.setBorder(BorderFactory.createCompoundBorder(osTitle, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelOS.add(scrollOS, BorderLayout.CENTER);

        // Painel de botões de ações da OS
        JPanel osBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        TitledBorder osBottomBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Ações para Ordem de Serviço",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        osBottomPanel.setBorder(BorderFactory.createCompoundBorder(osBottomBorder, BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JButton btnMarcarPago = new JButton("Confirmar Pagamento (Marcar PAGO)");
        btnMarcarPago.setBackground(new Color(40, 167, 69)); // Verde
        btnMarcarPago.setForeground(Color.WHITE);
        btnMarcarPago.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMarcarPago.setFocusPainted(false);

        JButton btnGerarPdfOS = new JButton("Gerar / Abrir PDF da OS");
        btnGerarPdfOS.setBackground(new Color(220, 53, 69)); // Vermelho PDF
        btnGerarPdfOS.setForeground(Color.WHITE);
        btnGerarPdfOS.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGerarPdfOS.setFocusPainted(false);

        osBottomPanel.add(btnMarcarPago);
        osBottomPanel.add(btnGerarPdfOS);
        panelOS.add(osBottomPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Gestão de Ordens de Serviço (OS)", panelOS);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // 🔹 Ações
        buscarButton.addActionListener(e -> buscarVendas());
        
        btnPdfVendas.addActionListener(e -> {
            if (vendasCarregadas == null || vendasCarregadas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhuma venda carregada no período selecionado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Date inicio = dataInicioPicker.getValue();
            Date fim = dataFimPicker.getValue();
            PdfGenerator.gerarPdfRelatorioVendas(vendasCarregadas, inicio, fim);
            JOptionPane.showMessageDialog(this, "Relatório de Vendas PDF gerado com sucesso!", "Exportação PDF", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPdfProdutos.addActionListener(e -> {
            try {
                List<Produto> produtos = new ProdutoDAO().listar();
                PdfGenerator.gerarPdfRelatorioProdutos(produtos);
                JOptionPane.showMessageDialog(this, "Relatório de Produtos PDF gerado com sucesso!", "Exportação PDF", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar produtos para PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPdfClientes.addActionListener(e -> {
            try {
                List<Cliente> clientes = new ClienteDAO().listar();
                PdfGenerator.gerarPdfRelatorioClientes(clientes);
                JOptionPane.showMessageDialog(this, "Relatório de Clientes PDF gerado com sucesso!", "Exportação PDF", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar clientes para PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Ações da Gestão de OS
        btnMarcarPago.addActionListener(e -> {
            int selectedRow = osTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma Ordem de Serviço na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int osId = (int) osTable.getValueAt(selectedRow, 0);
            String statusAtual = (String) osTable.getValueAt(selectedRow, 4);
            
            if ("PAGO".equalsIgnoreCase(statusAtual)) {
                JOptionPane.showMessageDialog(this, "Esta Ordem de Serviço já está com status PAGO.", "Informação", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String[] opcoes = {"PIX", "DINHEIRO", "BOLETO", "CARTAO", "TRANSFERENCIA"};
            String pgto = (String) JOptionPane.showInputDialog(
                    this,
                    "Selecione a Forma de Pagamento para a OS #" + osId + ":",
                    "Confirmar Pagamento",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcoes,
                    opcoes[0]
            );
            
            if (pgto != null) {
                try {
                    OrdemServicoDAO osDAO = new OrdemServicoDAO();
                    osDAO.atualizarStatusEPagamento(osId, "PAGO", pgto);
                    JOptionPane.showMessageDialog(this, "Ordem de Serviço #" + osId + " marcada como PAGA com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    buscarVendas(); // Recarregar dados
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao atualizar pagamento da OS: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnGerarPdfOS.addActionListener(e -> {
            int selectedRow = osTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma Ordem de Serviço na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int osId = (int) osTable.getValueAt(selectedRow, 0);
            try {
                OrdemServicoDAO osDAO = new OrdemServicoDAO();
                OrdemServico os = osDAO.buscarPorId(osId);
                if (os != null) {
                    ItemOrdemServicoDAO itemDAO = new ItemOrdemServicoDAO();
                    os.setItens(itemDAO.buscarPorIdOrdemServico(osId));
                    PdfGenerator.gerarPdfOrdemServico(os);
                    JOptionPane.showMessageDialog(this, "PDF da OS #" + osId + " gerado e aberto com sucesso!", "PDF Gerado", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Ordem de Serviço não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar PDF da OS: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        buscarVendas(); // Carregar inicial
    }

    private void buscarVendas() {
        tableModel.setRowCount(0);
        Date dataInicio = dataInicioPicker.getValue();
        Date dataFim = dataFimPicker.getValue();

        try {
            VendaDAO dao = new VendaDAO();
            vendasCarregadas = dao.listarVendasPorPeriodo(dataInicio, dataFim);

            double totalVendas = 0.0;
            double totalLucro = 0.0;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (Venda v : vendasCarregadas) {
                tableModel.addRow(new Object[]{
                        v.getDataVenda() != null ? sdf.format(v.getDataVenda()) : "N/A",
                        v.getDescricao(),
                        v.getTipoItem(),
                        String.format("%.2f", v.getQuantidade()),
                        String.format("R$ %.2f", v.getValorTotal()),
                        String.format("R$ %.2f", v.getCustoTotal()),
                        String.format("R$ %.2f", v.getLucro()),
                        v.getFormaPagamento()
                });
                totalVendas += v.getValorTotal();
                totalLucro += v.getLucro();
            }

            totalVendasLabel.setText(String.format("Total Faturado: R$ %.2f", totalVendas));
            lucroTotalLabel.setText(String.format("Lucro Total Líquido: R$ %.2f", totalLucro));

            // Carregar dados das ordens de serviço
            buscarOrdemServicos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar vendas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void buscarOrdemServicos() {
        osTableModel.setRowCount(0);
        Date dataInicio = dataInicioPicker.getValue();
        Date dataFim = dataFimPicker.getValue();

        try {
            OrdemServicoDAO osDAO = new OrdemServicoDAO();
            List<OrdemServico> ordens = osDAO.listarPorPeriodo(dataInicio, dataFim);
            ClienteDAO clienteDAO = new ClienteDAO();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (OrdemServico os : ordens) {
                Cliente c = clienteDAO.buscarPorId(os.getClienteId());
                String clienteNome = c != null ? c.getNome() : "ID: " + os.getClienteId();
                osTableModel.addRow(new Object[]{
                        os.getId(),
                        os.getDataOrdem() != null ? sdf.format(os.getDataOrdem()) : "N/A",
                        clienteNome,
                        String.format("R$ %.2f", os.getValorTotal()),
                        os.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar ordens de serviço: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
