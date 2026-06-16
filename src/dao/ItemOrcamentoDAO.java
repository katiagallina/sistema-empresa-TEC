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

    public List<ItemOrcamento> buscarPorIdOrcamento(int idOrcamento) throws SQLException {
        String sql = "SELECT * FROM orcamento_itens WHERE orcamento_id = ?";
        List<ItemOrcamento> itens = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOrcamento);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ItemOrcamento item = new ItemOrcamento();
                    item.setId(rs.getInt("id"));
                    item.setIdOrcamento(rs.getInt("orcamento_id"));
                    item.setTipoItem(rs.getString("tipo_item"));
                    
                    int pId = rs.getInt("produto_id");
                    item.setIdProduto(rs.wasNull() ? null : pId);
                    
                    int sId = rs.getInt("servico_id");
                    item.setIdServico(rs.wasNull() ? null : sId);
                    
                    item.setDescricao(rs.getString("descricao"));
                    item.setQuantidade(rs.getDouble("quantidade"));
                    item.setValorUnitario(rs.getDouble("valor_unitario"));
                    item.setValorTotal(rs.getDouble("valor_total"));
                    itens.add(item);
                }
            }
        }
        return itens;
    }
}