package dao;

import connection.Conexao;
import model.ItemOrdemServico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemOrdemServicoDAO {

    public List<ItemOrdemServico> buscarPorIdOrdemServico(int idOrdemServico) {
        String sql = "SELECT * FROM itens_ordem_servico WHERE id_ordem_servico = ?";
        List<ItemOrdemServico> itens = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOrdemServico);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemOrdemServico item = new ItemOrdemServico();
                item.setId(rs.getInt("id"));
                item.setIdOrdemServico(rs.getInt("id_ordem_servico"));
                item.setIdProduto(rs.getInt("id_produto"));
                item.setQuantidade(rs.getDouble("quantidade"));
                item.setValorUnitario(rs.getDouble("valor_unitario"));
                item.setValorTotal(rs.getDouble("valor_total"));
                itens.add(item);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar itens da ordem de serviço: " + e.getMessage());
        }

        return itens;
    }
}