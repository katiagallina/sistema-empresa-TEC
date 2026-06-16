package view;

import dao.OrcamentoDAO;
import dao.OrdemServicoDAO;
import model.Orcamento;
import model.OrdemServico;
import model.ItemOrcamento;
import model.ItemOrdemServico;
import dao.ItemOrcamentoDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FinalizarOSScreen extends JDialog {

    private JTable tblOrcamentos;
    private DefaultTableModel tableModel;
    private OrcamentoDAO orcamentoDAO;
    private List<Orcamento> orcamentos;

    public FinalizarOSScreen(Frame owner) {
        super(owner, "Finalizar Ordem de Serviço", true);
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        orcamentoDAO = new OrcamentoDAO();

        // Tabela
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID Orçamento");
        tableModel.addColumn("Cliente");
        tableModel.addColumn("Valor Total");
        tableModel.addColumn("Data");

        tblOrcamentos = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblOrcamentos);
        add(scrollPane, BorderLayout.CENTER);

        // Painel de botão
        JPanel buttonPanel = new JPanel();
        JButton btnFinalizar = new JButton("Finalizar OS a partir do Orçamento Selecionado");
        buttonPanel.add(btnFinalizar);
        add(buttonPanel, BorderLayout.SOUTH);

        // Ações
        btnFinalizar.addActionListener(e -> finalizarOrdemDeServico());

        loadOrcamentos();
    }

    private void loadOrcamentos() {
        // Por simplicidade, estamos listando todos. O ideal seria listar apenas os não finalizados.
        orcamentos = orcamentoDAO.listar();
        tableModel.setRowCount(0); // Limpa a tabela
        for (Orcamento o : orcamentos) {
            tableModel.addRow(new Object[]{
                    o.getId(),
                    o.getClienteNome(),
                    o.getValorTotal(),
                    o.getDataOrcamento()
            });
        }
    }

    private void finalizarOrdemDeServico() {
        int selectedRow = tblOrcamentos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um orçamento na tabela.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int orcamentoId = (int) tblOrcamentos.getValueAt(selectedRow, 0);
            Orcamento orcamento = orcamentoDAO.buscarPorId(orcamentoId); // Re-busca para garantir dados atualizados
            
            if (orcamento != null) {
                // Carrega os itens do orçamento
                ItemOrcamentoDAO itemOrcamentoDAO = new ItemOrcamentoDAO();
                List<ItemOrcamento> itensOrcamento = itemOrcamentoDAO.buscarPorIdOrcamento(orcamentoId);
                orcamento.setItens(itensOrcamento);

                // Cria a Ordem de Serviço
                OrdemServico novaOrdem = new OrdemServico();
                novaOrdem.setIdOrcamento(orcamentoId);
                novaOrdem.setValorTotal(orcamento.getValorTotal());
                novaOrdem.setStatus("FINALIZADA");

                List<ItemOrdemServico> itensOrdem = new ArrayList<>();
                for (ItemOrcamento itemOrcamento : orcamento.getItens()) {
                    ItemOrdemServico itemOrdem = new ItemOrdemServico();
                    itemOrdem.setIdProduto(itemOrcamento.getIdProduto());
                    itemOrdem.setQuantidade(itemOrcamento.getQuantidade());
                    itemOrdem.setValorUnitario(itemOrcamento.getValorUnitario());
                    itemOrdem.setValorTotal(itemOrcamento.getValorTotal());
                    itensOrdem.add(itemOrdem);
                }
                novaOrdem.setItens(itensOrdem);

                OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();
                ordemServicoDAO.inserir(novaOrdem);

                JOptionPane.showMessageDialog(this, "Ordem de Serviço finalizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                // Opcional: remover o orçamento da lista ou fechar a janela
                loadOrcamentos(); // Recarrega a lista
                // dispose();

            } else {
                 JOptionPane.showMessageDialog(this, "Orçamento não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao finalizar a Ordem de Serviço: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
