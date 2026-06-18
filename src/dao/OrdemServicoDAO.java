package dao;

import connection.Conexao;
import model.OrdemServico;
import model.ItemOrdemServico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdemServicoDAO {

    public void inserir(OrdemServico ordemServico, String formaPagamento) throws SQLException {
        String sqlOrdem = "INSERT INTO ordem_servico (cliente_id, total, status) VALUES (?, ?, ?)";
        String sqlItem = "INSERT INTO ordem_servico_itens (ordem_servico_id, tipo_item, produto_id, servico_id, descricao, quantidade, valor_unitario, valor_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtOrdem = conn.prepareStatement(sqlOrdem, Statement.RETURN_GENERATED_KEYS)) {
                stmtOrdem.setInt(1, ordemServico.getClienteId());
                stmtOrdem.setDouble(2, ordemServico.getValorTotal());
                stmtOrdem.setString(3, ordemServico.getStatus() != null ? ordemServico.getStatus() : "EM ANDAMENTO");
                stmtOrdem.executeUpdate();

                try (ResultSet generatedKeys = stmtOrdem.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idOrdem = generatedKeys.getInt(1);
                        ordemServico.setId(idOrdem);

                        for (ItemOrdemServico item : ordemServico.getItens()) {
                            // 1. Inserir o item da OS
                            try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                                stmtItem.setInt(1, idOrdem);
                                stmtItem.setString(2, item.getTipoItem());
                                if (item.getIdProduto() != null && item.getIdProduto() > 0) {
                                    stmtItem.setInt(3, item.getIdProduto());
                                } else {
                                    stmtItem.setNull(3, Types.INTEGER);
                                }
                                if (item.getIdServico() != null && item.getIdServico() > 0) {
                                    stmtItem.setInt(4, item.getIdServico());
                                } else {
                                    stmtItem.setNull(4, Types.INTEGER);
                                }
                                stmtItem.setString(5, item.getDescricao());
                                stmtItem.setDouble(6, item.getQuantidade());
                                stmtItem.setDouble(7, item.getValorUnitario());
                                stmtItem.setDouble(8, item.getValorTotal());
                                stmtItem.executeUpdate();
                            }

                            // 2. Se a OS for finalizada, dar baixa no estoque e registrar a venda correspondente
                            String osStatus = ordemServico.getStatus();
                            if ("FINALIZADA".equals(osStatus) || "PAGO".equals(osStatus) || "PENDENTE".equals(osStatus)) {
                                double custoTotal = 0.0;
                                int itemId = 0;

                                if ("PRODUTO".equals(item.getTipoItem())) {
                                    itemId = item.getIdProduto();
                                    // Buscar preço de custo e estoque atual
                                    try (PreparedStatement stmtProd = conn.prepareStatement("SELECT preco_custo, quantidade FROM produtos WHERE id = ?")) {
                                        stmtProd.setInt(1, item.getIdProduto());
                                        try (ResultSet rsProd = stmtProd.executeQuery()) {
                                            if (rsProd.next()) {
                                                double precoCusto = rsProd.getDouble("preco_custo");
                                                double qtdAtual = rsProd.getDouble("quantidade");
                                                custoTotal = precoCusto * item.getQuantidade();

                                                // Atualizar estoque (Dedução)
                                                try (PreparedStatement stmtStock = conn.prepareStatement("UPDATE produtos SET quantidade = ? WHERE id = ?")) {
                                                    stmtStock.setDouble(1, qtdAtual - item.getQuantidade());
                                                    stmtStock.setInt(2, item.getIdProduto());
                                                    stmtStock.executeUpdate();
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (item.getIdServico() != null) {
                                        itemId = item.getIdServico();
                                    }
                                    custoTotal = 0.0; // Custos de serviços são considerados operacionais ou zero
                                }

                                double lucro = item.getValorTotal() - custoTotal;

                                // Registrar venda
                                String sqlVenda = "INSERT INTO vendas (ordem_servico_id, tipo_item, item_id, descricao, quantidade, valor_total, custo_total, lucro, forma_pagamento) " +
                                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                try (PreparedStatement stmtVenda = conn.prepareStatement(sqlVenda)) {
                                    stmtVenda.setInt(1, idOrdem);
                                    stmtVenda.setString(2, item.getTipoItem());
                                    if (itemId > 0) {
                                        stmtVenda.setInt(3, itemId);
                                    } else {
                                        stmtVenda.setNull(3, Types.INTEGER);
                                    }
                                    stmtVenda.setString(4, item.getDescricao());
                                    stmtVenda.setDouble(5, item.getQuantidade());
                                    stmtVenda.setDouble(6, item.getValorTotal());
                                    stmtVenda.setDouble(7, custoTotal);
                                    stmtVenda.setDouble(8, lucro);
                                    stmtVenda.setString(9, formaPagamento != null ? formaPagamento : "DINHEIRO");
                                    stmtVenda.executeUpdate();
                                }
                            }
                        }
                    }
                }
                conn.commit();
                System.out.println("Ordem de serviço inserida com sucesso!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<OrdemServico> listar() throws SQLException {
        String sql = "SELECT * FROM ordem_servico ORDER BY id DESC";
        List<OrdemServico> ordens = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OrdemServico ordem = new OrdemServico();
                ordem.setId(rs.getInt("id"));
                ordem.setClienteId(rs.getInt("cliente_id"));
                ordem.setDataOrdem(rs.getTimestamp("data"));
                ordem.setValorTotal(rs.getDouble("total"));
                ordem.setStatus(rs.getString("status"));
                ordens.add(ordem);
            }
        }
        return ordens;
    }

    public List<OrdemServico> listarPorPeriodo(java.util.Date dataInicio, java.util.Date dataFim) throws SQLException {
        String sql = "SELECT * FROM ordem_servico WHERE data BETWEEN ? AND ? AND status IN ('FINALIZADA', 'PAGO', 'PENDENTE') ORDER BY id DESC";
        List<OrdemServico> ordens = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, new java.sql.Timestamp(dataInicio.getTime()));
            stmt.setTimestamp(2, new java.sql.Timestamp(dataFim.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrdemServico ordem = new OrdemServico();
                    ordem.setId(rs.getInt("id"));
                    ordem.setClienteId(rs.getInt("cliente_id"));
                    ordem.setDataOrdem(rs.getTimestamp("data"));
                    ordem.setValorTotal(rs.getDouble("total"));
                    ordem.setStatus(rs.getString("status"));
                    ordens.add(ordem);
                }
            }
        }
        return ordens;
    }

    public void atualizarStatusEPagamento(int osId, String novoStatus, String novaFormaPagamento) throws SQLException {
        String sqlOS = "UPDATE ordem_servico SET status = ? WHERE id = ?";
        String sqlVendas = "UPDATE vendas SET forma_pagamento = ? WHERE ordem_servico_id = ?";
        
        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlOS)) {
                    stmt.setString(1, novoStatus);
                    stmt.setInt(2, osId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(sqlVendas)) {
                    stmt.setString(1, novaFormaPagamento);
                    stmt.setInt(2, osId);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public OrdemServico buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM ordem_servico WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    OrdemServico os = new OrdemServico();
                    os.setId(rs.getInt("id"));
                    os.setClienteId(rs.getInt("cliente_id"));
                    os.setDataOrdem(rs.getTimestamp("data"));
                    os.setValorTotal(rs.getDouble("total"));
                    os.setStatus(rs.getString("status"));
                    return os;
                }
            }
        }
        return null;
    }
}