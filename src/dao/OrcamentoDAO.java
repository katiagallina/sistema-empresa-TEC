package dao;

import connection.Conexao;
import model.Orcamento;
import model.ItemOrcamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrcamentoDAO {

    public void inserir(Orcamento orcamento) throws SQLException {
        String sqlOrcamento = "INSERT INTO orcamentos (cliente_id, total, status) VALUES (?, ?, ?)";
        String sqlItem = "INSERT INTO orcamento_itens (orcamento_id, tipo_item, produto_id, servico_id, descricao, quantidade, valor_unitario, valor_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtOrc = conn.prepareStatement(sqlOrcamento, Statement.RETURN_GENERATED_KEYS)) {
                stmtOrc.setInt(1, orcamento.getClienteId());
                stmtOrc.setDouble(2, orcamento.getValorTotal());
                stmtOrc.setString(3, orcamento.getStatus() != null ? orcamento.getStatus() : "ABERTO");
                stmtOrc.executeUpdate();

                try (ResultSet generatedKeys = stmtOrc.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idOrcamento = generatedKeys.getInt(1);
                        orcamento.setId(idOrcamento);

                        try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                            for (ItemOrcamento item : orcamento.getItens()) {
                                stmtItem.setInt(1, idOrcamento);
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
                                stmtItem.addBatch();
                            }
                            stmtItem.executeBatch();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Orcamento> listar() throws SQLException {
        String sql = "SELECT * FROM orcamentos ORDER BY id DESC";
        List<Orcamento> orcamentos = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Orcamento o = new Orcamento();
                o.setId(rs.getInt("id"));
                o.setClienteId(rs.getInt("cliente_id"));
                o.setDataOrcamento(rs.getTimestamp("data"));
                o.setValorTotal(rs.getDouble("total"));
                o.setStatus(rs.getString("status"));
                orcamentos.add(o);
            }
        }
        return orcamentos;
    }

    public List<Orcamento> listarAbertos() throws SQLException {
        String sql = "SELECT * FROM orcamentos WHERE status = 'ABERTO' OR status = 'APROVADO' ORDER BY id DESC";
        List<Orcamento> orcamentos = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Orcamento o = new Orcamento();
                o.setId(rs.getInt("id"));
                o.setClienteId(rs.getInt("cliente_id"));
                o.setDataOrcamento(rs.getTimestamp("data"));
                o.setValorTotal(rs.getDouble("total"));
                o.setStatus(rs.getString("status"));
                orcamentos.add(o);
            }
        }
        return orcamentos;
    }

    public Orcamento buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM orcamentos WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Orcamento o = new Orcamento();
                    o.setId(rs.getInt("id"));
                    o.setClienteId(rs.getInt("cliente_id"));
                    o.setDataOrcamento(rs.getTimestamp("data"));
                    o.setValorTotal(rs.getDouble("total"));
                    o.setStatus(rs.getString("status"));
                    return o;
                }
            }
        }
        return null;
    }

    public void atualizarStatus(int id, String status) throws SQLException {
        String sql = "UPDATE orcamentos SET status = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
}