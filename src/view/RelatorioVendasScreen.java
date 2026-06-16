package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class RelatorioVendasScreen extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton buscarButton;
    private JSpinner dataInicioSpinner;
    private JSpinner dataFimSpinner;
    private JLabel totalVendasLabel;
    private JLabel lucroTotalLabel;

    public RelatorioVendasScreen() {
        setTitle("Relatório de Vendas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Painel de Filtros
        JPanel filtroPanel = new JPanel(new FlowLayout());
        filtroPanel.add(new JLabel("Data Início:"));
        dataInicioSpinner = new JSpinner(new SpinnerDateModel());
        filtroPanel.add(dataInicioSpinner);

        filtroPanel.add(new JLabel("Data Fim:"));
        dataFimSpinner = new JSpinner(new SpinnerDateModel());
        filtroPanel.add(dataFimSpinner);

        buscarButton = new JButton("Buscar");
        filtroPanel.add(buscarButton);

        panel.add(filtroPanel, BorderLayout.NORTH);

        // Tabela de Vendas
        tableModel = new DefaultTableModel(new Object[] { "Data", "Descrição", "Valor Total", "Forma Pagamento" }, 0);
        table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Painel de Totais
        JPanel totaisPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalVendasLabel = new JLabel("Total Vendas: R$ 0,00");
        lucroTotalLabel = new JLabel("Lucro Total: R$ 0,00");
        totaisPanel.add(totalVendasLabel);
        totaisPanel.add(lucroTotalLabel);
        panel.add(totaisPanel, BorderLayout.SOUTH);

        add(panel);

        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Lógica para buscar as vendas no banco de dados
                buscarVendas();
            }
        });
    }

    private void buscarVendas() {
        // Limpa a tabela
        tableModel.setRowCount(0);

        Date dataInicio = (Date) dataInicioSpinner.getValue();
        Date dataFim = (Date) dataFimSpinner.getValue();

        dao.OrdemServicoDAO dao = new dao.OrdemServicoDAO();
        java.util.List<model.OrdemServico> vendas = dao.listarPorPeriodo(dataInicio, dataFim);

        double totalVendas = 0.0;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (model.OrdemServico venda : vendas) {
            tableModel.addRow(new Object[] {
                    venda.getDataOrdem() != null ? sdf.format(venda.getDataOrdem()) : "N/A",
                    "Ordem de Serviço #" + venda.getId(),
                    String.format("R$ %.2f", venda.getValorTotal()),
                    "Não informada" // Placeholder para forma de pagamento, caso implemente futuramente
            });
            totalVendas += venda.getValorTotal();
        }

        totalVendasLabel.setText(String.format("Total Vendas: R$ %.2f", totalVendas));
        // Simplificado, pois custo real depende do cadastro e cálculo por itens
        lucroTotalLabel.setText(String.format("Lucro Total (Est. 30%%): R$ %.2f", totalVendas * 0.3));
    }

}
