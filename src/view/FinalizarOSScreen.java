package view;

import dao.OrcamentoDAO;
import dao.OrdemServicoDAO;
import dao.ItemOrcamentoDAO;
import dao.ClienteDAO;
import model.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FinalizarOSScreen extends JDialog {

    private JTable tblOrcamentos;
    private DefaultTableModel tableModel;
    private OrcamentoDAO orcamentoDAO;
    private List<Orcamento> orcamentos;
    private JCheckBox chkAbrirPdf;

    public FinalizarOSScreen(Frame owner) {
        super(owner, "Finalizar Ordem de Serviço a partir de Orçamento", true);
        setSize(900, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        orcamentoDAO = new OrcamentoDAO();

        // 🔹 Header Panel (Banner)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(235, 242, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblHeaderTitle = new JLabel("Finalizar Ordem de Serviço (OS)");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderTitle.setForeground(new Color(33, 37, 41));
        
        JLabel lblHeaderDesc = new JLabel("Feche e fature ordens de serviço a partir de orçamentos aprovados, registrando as formas de pagamento");
        lblHeaderDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHeaderDesc.setForeground(new Color(100, 110, 120));
        
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        headerPanel.add(lblHeaderDesc, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Painel de Conteúdo
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        // Tabela
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("ID Orçamento");
        tableModel.addColumn("Cliente");
        tableModel.addColumn("Valor Total");
        tableModel.addColumn("Data");
        tableModel.addColumn("Status");

        tblOrcamentos = new JTable(tableModel);
        tblOrcamentos.setRowHeight(25);
        tblOrcamentos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblOrcamentos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblOrcamentos.setSelectionBackground(new Color(225, 235, 248));
        tblOrcamentos.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(tblOrcamentos);
        TitledBorder tblTitle = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 230), 1, true),
            "Orçamentos Disponíveis (Abertos)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(55, 71, 79)
        );
        scrollPane.setBorder(BorderFactory.createCompoundBorder(tblTitle, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Painel inferior
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        JLabel lblForma = new JLabel("Forma de Pagamento:");
        lblForma.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row1.add(lblForma);
        
        JComboBox<String> cbFormaPagamento = new JComboBox<>(new String[]{"PIX", "DINHEIRO", "BOLETO", "CARTAO", "TRANSFERENCIA"});
        cbFormaPagamento.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row1.add(cbFormaPagamento);
        
        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row1.add(lblStatus);
        
        JComboBox<String> cbStatusPagamento = new JComboBox<>(new String[]{"PAGO", "PENDENTE"});
        cbStatusPagamento.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row1.add(cbStatusPagamento);

        chkAbrirPdf = new JCheckBox("Gerar e Abrir PDF da OS", true);
        chkAbrirPdf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row1.add(chkAbrirPdf);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        
        JButton btnEditar = new JButton("Editar Orçamento");
        btnEditar.setBackground(new Color(0, 123, 255)); // Azul
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEditar.setFocusPainted(false);
        row2.add(btnEditar);

        JButton btnFinalizar = new JButton("Gerar e Finalizar Ordem de Serviço (OS)");
        btnFinalizar.setBackground(new Color(40, 167, 69)); // Verde sucesso
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFinalizar.setFocusPainted(false);
        row2.add(btnFinalizar);

        bottomPanel.add(row1);
        bottomPanel.add(row2);
        
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);

        // Ações
        btnEditar.addActionListener(e -> {
            int selectedRow = tblOrcamentos.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um orçamento na tabela para editar.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int orcamentoId = (int) tblOrcamentos.getValueAt(selectedRow, 0);
                Orcamento orcamento = orcamentoDAO.buscarPorId(orcamentoId);
                if (orcamento != null) {
                    OrcamentoScreen os = new OrcamentoScreen(this, orcamento);
                    os.setVisible(true);
                    loadOrcamentos(); // Recarrega os dados pós edição
                } else {
                    JOptionPane.showMessageDialog(this, "Orçamento não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir edição: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnFinalizar.addActionListener(e -> finalizarOrdemDeServico(
                (String) cbFormaPagamento.getSelectedItem(),
                (String) cbStatusPagamento.getSelectedItem()
        ));

        loadOrcamentos();
    }

    private void loadOrcamentos() {
        try {
            orcamentos = orcamentoDAO.listarAbertos();
            tableModel.setRowCount(0);
            ClienteDAO clienteDAO = new ClienteDAO();
            
            for (Orcamento o : orcamentos) {
                Cliente c = clienteDAO.buscarPorId(o.getClienteId());
                String clienteNome = c != null ? c.getNome() : "ID: " + o.getClienteId();
                tableModel.addRow(new Object[]{
                        o.getId(),
                        clienteNome,
                        String.format("R$ %.2f", o.getValorTotal()),
                        o.getDataOrcamento(),
                        o.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar orçamentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void finalizarOrdemDeServico(String formaPagamento, String statusPagamento) {
        int selectedRow = tblOrcamentos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um orçamento na tabela.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int orcamentoId = (int) tblOrcamentos.getValueAt(selectedRow, 0);
            Orcamento orcamento = orcamentoDAO.buscarPorId(orcamentoId);
            
            if (orcamento != null) {
                // Carrega os itens do orçamento
                ItemOrcamentoDAO itemOrcamentoDAO = new ItemOrcamentoDAO();
                List<ItemOrcamento> itensOrcamento = itemOrcamentoDAO.buscarPorIdOrcamento(orcamentoId);
                orcamento.setItens(itensOrcamento);

                // Cria a Ordem de Serviço
                OrdemServico novaOrdem = new OrdemServico();
                novaOrdem.setClienteId(orcamento.getClienteId());
                novaOrdem.setValorTotal(orcamento.getValorTotal());
                novaOrdem.setStatus(statusPagamento); // "PAGO" ou "PENDENTE"
                novaOrdem.setDataOrdem(new java.sql.Timestamp(System.currentTimeMillis())); // Definir data atual

                List<ItemOrdemServico> itensOrdem = new ArrayList<>();
                for (ItemOrcamento itemOrcamento : orcamento.getItens()) {
                    ItemOrdemServico itemOrdem = new ItemOrdemServico();
                    itemOrdem.setTipoItem(itemOrcamento.getTipoItem());
                    itemOrdem.setIdProduto(itemOrcamento.getIdProduto());
                    itemOrdem.setIdServico(itemOrcamento.getIdServico());
                    itemOrdem.setDescricao(itemOrcamento.getDescricao());
                    itemOrdem.setQuantidade(itemOrcamento.getQuantidade());
                    itemOrdem.setValorUnitario(itemOrcamento.getValorUnitario());
                    itemOrdem.setValorTotal(itemOrcamento.getValorTotal());
                    itensOrdem.add(itemOrdem);
                }
                novaOrdem.setItens(itensOrdem);

                // Inserir OS, dar baixa estoque e registrar venda (tudo na transação do DAO)
                OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();
                ordemServicoDAO.inserir(novaOrdem, formaPagamento);

                // Atualizar o status do orçamento para FINALIZADO
                orcamentoDAO.atualizarStatus(orcamentoId, "FINALIZADO");

                JOptionPane.showMessageDialog(this, 
                        "Ordem de Serviço #" + novaOrdem.getId() + " finalizada com sucesso!\n" +
                        "Estoque deduzido e Venda lançada financeiramente.", 
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                if (chkAbrirPdf.isSelected()) {
                    util.PdfGenerator.gerarPdfOrdemServico(novaOrdem);
                }
                
                // Recarregar a lista
                loadOrcamentos();

            } else {
                 JOptionPane.showMessageDialog(this, "Orçamento não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao finalizar a Ordem de Serviço: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
