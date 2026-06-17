package view;

import dao.VendaDAO;
import dao.ProdutoDAO;
import dao.ClienteDAO;
import model.Venda;
import model.Produto;
import model.Cliente;
import util.PdfGenerator;

import javax.swing.*;
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
    private JButton buscarButton;
    private util.DatePicker dataInicioPicker;
    private util.DatePicker dataFimPicker;
    private JLabel totalVendasLabel;
    private JLabel lucroTotalLabel;

    private List<Venda> vendasCarregadas;

    public RelatorioVendasScreen() {
        setTitle("Relatórios Financeiros e Operacionais - TEC Energia");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 🔹 Painel de Filtros (Norte)
        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtroPanel.setBorder(BorderFactory.createTitledBorder("Filtrar Vendas por Período"));
        
        filtroPanel.add(new JLabel("Data Início:"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // Filtro inicial: último mês
        Date dataInicioPadrao = cal.getTime();
        
        dataInicioPicker = new util.DatePicker(dataInicioPadrao);
        filtroPanel.add(dataInicioPicker);
 
        filtroPanel.add(new JLabel("Data Fim:"));
        dataFimPicker = new util.DatePicker(new Date());
        filtroPanel.add(dataFimPicker);

        buscarButton = new JButton("Filtrar Vendas");
        buscarButton.setBackground(new Color(0, 102, 204));
        buscarButton.setForeground(Color.WHITE);
        filtroPanel.add(buscarButton);

        panel.add(filtroPanel, BorderLayout.NORTH);

        // 🔹 Tabela de Vendas (Centro)
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
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Detalhamento de Vendas Diárias"));
        panel.add(scroll, BorderLayout.CENTER);

        // 🔹 Painel de Totais e Relatórios PDF (Sul)
        JPanel sulPanel = new JPanel(new BorderLayout(5, 5));
        
        // Indicadores Rápidos
        JPanel totaisPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        totalVendasLabel = new JLabel("Total Vendas: R$ 0,00");
        totalVendasLabel.setFont(new Font("Arial", Font.BOLD, 15));
        lucroTotalLabel = new JLabel("Lucro Total: R$ 0,00");
        lucroTotalLabel.setFont(new Font("Arial", Font.BOLD, 15));
        lucroTotalLabel.setForeground(new Color(40, 167, 69));
        
        totaisPanel.add(totalVendasLabel);
        totaisPanel.add(lucroTotalLabel);
        sulPanel.add(totaisPanel, BorderLayout.NORTH);

        // Ações de Exportação PDF
        JPanel pdfPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pdfPanel.setBorder(BorderFactory.createTitledBorder("Exportar Relatórios PDF"));
        
        JButton btnPdfVendas = new JButton("PDF Vendas e Lucro (Período)");
        btnPdfVendas.setBackground(new Color(220, 53, 69)); // Vermelho PDF
        btnPdfVendas.setForeground(Color.WHITE);
        
        JButton btnPdfProdutos = new JButton("PDF Geral de Produtos");
        btnPdfProdutos.setBackground(new Color(220, 53, 69));
        btnPdfProdutos.setForeground(Color.WHITE);
        
        JButton btnPdfClientes = new JButton("PDF Geral de Clientes");
        btnPdfClientes.setBackground(new Color(220, 53, 69));
        btnPdfClientes.setForeground(Color.WHITE);

        pdfPanel.add(btnPdfVendas);
        pdfPanel.add(btnPdfProdutos);
        pdfPanel.add(btnPdfClientes);
        
        sulPanel.add(pdfPanel, BorderLayout.CENTER);
        panel.add(sulPanel, BorderLayout.SOUTH);

        add(panel);

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

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar vendas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
