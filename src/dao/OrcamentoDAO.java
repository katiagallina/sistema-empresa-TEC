package dao;

import connection.Conexao;
import model.Orcamento;
import model.ItemOrcamento;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class OrcamentoDAO {

    public void inserir(Orcamento orcamento) {
        String sqlOrcamento = "INSERT INTO orcamentos (cliente_nome, valor_total) VALUES (?, ?)";
        String sqlItem = "INSERT INTO itens_orcamento (id_orcamento, id_produto, quantidade, valor_unitario, valor_total) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtOrcamento = conn.prepareStatement(sqlOrcamento, Statement.RETURN_GENERATED_KEYS)) {

                stmtOrcamento.setString(1, orcamento.getClienteNome());
                stmtOrcamento.setDouble(2, orcamento.getValorTotal());
                stmtOrcamento.executeUpdate();

                try (ResultSet generatedKeys = stmtOrcamento.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idOrcamento = generatedKeys.getInt(1);
                        orcamento.setId(idOrcamento);

                        try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                            for (ItemOrcamento item : orcamento.getItens()) {
                                stmtItem.setInt(1, idOrcamento);
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
                System.out.println("Orçamento inserido com sucesso!");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erro ao inserir orçamento: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }
    }

    public List<Orcamento> listar() {
        String sql = "SELECT * FROM orcamentos ORDER BY id DESC";
        List<Orcamento> orcamentos = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Orcamento o = new Orcamento();
                o.setId(rs.getInt("id"));
                o.setClienteNome(rs.getString("cliente_nome"));
                o.setValorTotal(rs.getDouble("valor_total"));
                
                orcamentos.add(o);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar orçamentos: " + e.getMessage());
        }

        return orcamentos;
    }

    public Orcamento buscarPorId(int id) {
        String sql = "SELECT * FROM orcamentos WHERE id = ?";
        Orcamento o = null;

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                o = new Orcamento();

                o.setId(rs.getInt("id"));
                o.setClienteNome(rs.getString("cliente_nome"));
                o.setValorTotal(rs.getDouble("valor_total"));
                
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar orçamento: " + e.getMessage());
        }

        return o;
    }
}