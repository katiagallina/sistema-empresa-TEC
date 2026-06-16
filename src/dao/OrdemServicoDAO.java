package dao;

import connection.Conexao;
import model.OrdemServico;
import model.ItemOrdemServico;

import java.sql.*;

public class OrdemServicoDAO {

    public void inserir(OrdemServico ordemServico) {
        String sqlOrdem = "INSERT INTO ordens_servico (id_orcamento, valor_total, status) VALUES (?, ?, ?)";
        String sqlItem = "INSERT INTO itens_ordem_servico (id_ordem_servico, id_produto, quantidade, valor_unitario, valor_total) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtOrdem = conn.prepareStatement(sqlOrdem, Statement.RETURN_GENERATED_KEYS)) {

                stmtOrdem.setInt(1, ordemServico.getIdOrcamento());
                stmtOrdem.setDouble(2, ordemServico.getValorTotal());
                stmtOrdem.setString(3, ordemServico.getStatus());
                stmtOrdem.executeUpdate();

                try (ResultSet generatedKeys = stmtOrdem.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idOrdem = generatedKeys.getInt(1);

                        try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                            for (ItemOrdemServico item : ordemServico.getItens()) {
                                stmtItem.setInt(1, idOrdem);
                                stmtItem.setInt(2, item.getIdProduto());
                                stmtItem.setDouble(3, item.getQuantidade());
                                stmtItem.setDouble(4, item.getValorUnitario());
                                stmtItem.setDouble(5, item.getValorTotal());
                                stmtItem.addBatch();
                            }
                            stmtItem.executeBatch();
                        }
                    }
                }
                conn.commit();
                System.out.println("Ordem de serviço inserida com sucesso!");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erro ao inserir ordem de serviço: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }
    }

    public java.util.List<OrdemServico> listar() {
        String sql = "SELECT * FROM ordens_servico";
        java.util.List<OrdemServico> ordens = new java.util.ArrayList<>();

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrdemServico ordem = new OrdemServico();
                ordem.setId(rs.getInt("id"));
                ordem.setIdOrcamento(rs.getInt("id_orcamento"));
                ordem.setDataOrdem(rs.getTimestamp("data_ordem"));
                ordem.setValorTotal(rs.getDouble("valor_total"));
                ordem.setStatus(rs.getString("status"));
                ordens.add(ordem);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar ordens de serviço: " + e.getMessage());
        }

        return ordens;
    }

    public java.util.List<OrdemServico> listarPorPeriodo(java.util.Date dataInicio, java.util.Date dataFim) {
        String sql = "SELECT * FROM ordens_servico WHERE data_ordem BETWEEN ? AND ? AND status = 'FINALIZADA'";
        java.util.List<OrdemServico> ordens = new java.util.ArrayList<>();

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, new java.sql.Timestamp(dataInicio.getTime()));
            stmt.setTimestamp(2, new java.sql.Timestamp(dataFim.getTime()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrdemServico ordem = new OrdemServico();
                ordem.setId(rs.getInt("id"));
                ordem.setIdOrcamento(rs.getInt("id_orcamento"));
                ordem.setDataOrdem(rs.getTimestamp("data_ordem"));
                ordem.setValorTotal(rs.getDouble("valor_total"));
                ordem.setStatus(rs.getString("status"));
                ordens.add(ordem);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar ordens de serviço por período: " + e.getMessage());
        }

        return ordens;
    }
}