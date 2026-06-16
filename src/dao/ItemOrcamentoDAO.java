package dao;

import connection.Conexao;
import model.ItemOrcamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemOrcamentoDAO {

    public List<ItemOrcamento> buscarPorIdOrcamento(int idOrcamento) {
        String sql = "SELECT * FROM itens_orcamento WHERE id_orcamento = ?";
        List<ItemOrcamento> itens = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOrcamento);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemOrcamento item = new ItemOrcamento();
                item.setId(rs.getInt("id"));
                item.setIdOrcamento(rs.getInt("id_orcamento"));
                item.setIdProduto(rs.getInt("id_produto"));
                item.setQuantidade(rs.getDouble("quantidade"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setValorTotal(rs.getDouble("valor_total"));
                itens.add(item);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar itens do orçamento: " + e.getMessage());
        }

        return itens;
    }
}