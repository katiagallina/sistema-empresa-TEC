package view;

import dao.OrcamentoDAO;
import dao.OrdemServicoDAO;
import dao.ItemOrcamentoDAO;
import dao.ClienteDAO;
import model.*;

import javax.swing.*;
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

    public FinalizarOSScreen(Frame owner) {
        super(owner, "Finalizar Ordem de Serviço a partir de Orçamento", true);
        setSize(850, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        orcamentoDAO = new OrcamentoDAO();

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
        JScrollPane scrollPane = new JScrollPane(tblOrcamentos);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Orçamentos Disponíveis (Abertos)"));
        add(scrollPane, BorderLayout.CENTER);

        // Painel inferior
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        formPanel.add(new JLabel("Forma de Pagamento:"));
        JComboBox<String> cbFormaPagamento = new JComboBox<>(new String[]{"PIX", "DINHEIRO", "BOLETO", "CARTAO", "TRANSFERENCIA"});
        formPanel.add(cbFormaPagamento);
        bottomPanel.add(formPanel, BorderLayout.WEST);

        JButton btnFinalizar = new JButton("Gerar e Finalizar Ordem de Serviço (OS)");
        btnFinalizar.setBackground(new Color(40, 167, 69)); // Verde sucesso
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.setFont(new Font("Arial", Font.BOLD, 12));
        bottomPanel.add(btnFinalizar, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);

        // Ações
        btnFinalizar.addActionListener(e -> finalizarOrdemDeServico((String) cbFormaPagamento.getSelectedItem()));

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

    private void finalizarOrdemDeServico(String formaPagamento) {
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
                novaOrdem.setStatus("FINALIZADA");

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
