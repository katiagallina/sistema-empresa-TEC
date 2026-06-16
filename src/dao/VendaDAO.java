package dao;

import connection.Conexao;
import model.Venda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    public void inserir(Venda venda) throws SQLException {
        String sql = "INSERT INTO vendas (ordem_servico_id, tipo_item, item_id, descricao, quantidade, valor_total, custo_total, lucro, forma_pagamento) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (venda.getOrdemServicoId() != null) {
                stmt.setInt(1, venda.getOrdemServicoId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, venda.getTipoItem());
            if (venda.getItemId() > 0) {
                stmt.setInt(3, venda.getItemId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, venda.getDescricao());
            stmt.setDouble(5, venda.getQuantidade());
            stmt.setDouble(6, venda.getValorTotal());
            stmt.setDouble(7, venda.getCustoTotal());
            stmt.setDouble(8, venda.getLucro());
            stmt.setString(9, venda.getFormaPagamento());
            stmt.executeUpdate();
        }
    }

    public List<Venda> listarVendasPorPeriodo(java.util.Date inicio, java.util.Date fim) throws SQLException {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT * FROM vendas WHERE data_venda BETWEEN ? AND ? ORDER BY data_venda DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(inicio.getTime()));
            stmt.setTimestamp(2, new Timestamp(fim.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Venda v = new Venda();
                    v.setId(rs.getInt("id"));
                    v.setDataVenda(rs.getTimestamp("data_venda"));
                    int osId = rs.getInt("ordem_servico_id");
                    v.setOrdemServicoId(rs.wasNull() ? null : osId);
                    v.setTipoItem(rs.getString("tipo_item"));
                    v.setItemId(rs.getInt("item_id"));
                    v.setDescricao(rs.getString("descricao"));
                    v.setQuantidade(rs.getDouble("quantidade"));
                    v.setValorTotal(rs.getDouble("valor_total"));
                    v.setCustoTotal(rs.getDouble("custo_total"));
                    v.setLucro(rs.getDouble("lucro"));
                    v.setFormaPagamento(rs.getString("forma_pagamento"));
                    lista.add(v);
                }
            }
        }
        return lista;
    }

    public double getFaturamentoDiario() throws SQLException {
        String sql = "SELECT SUM(valor_total) FROM vendas WHERE DATE(data_venda) = CURDATE()";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getFaturamentoMensal() throws SQLException {
        String sql = "SELECT SUM(valor_total) FROM vendas WHERE MONTH(data_venda) = MONTH(CURDATE()) AND YEAR(data_venda) = YEAR(CURDATE())";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getLucroDiario() throws SQLException {
        String sql = "SELECT SUM(lucro) FROM vendas WHERE DATE(data_venda) = CURDATE()";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getLucroMensal() throws SQLException {
        String sql = "SELECT SUM(lucro) FROM vendas WHERE MONTH(data_venda) = MONTH(CURDATE()) AND YEAR(data_venda) = YEAR(CURDATE())";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getFaturamentoServicosMensal() throws SQLException {
        String sql = "SELECT SUM(valor_total) FROM vendas WHERE tipo_item = 'SERVICO' AND MONTH(data_venda) = MONTH(CURDATE()) AND YEAR(data_venda) = YEAR(CURDATE())";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public int getQuantidadeClientes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Venda> listarTodos() throws SQLException {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT * FROM vendas ORDER BY data_venda DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Venda v = new Venda();
                v.setId(rs.getInt("id"));
                v.setDataVenda(rs.getTimestamp("data_venda"));
                int osId = rs.getInt("ordem_servico_id");
                v.setOrdemServicoId(rs.wasNull() ? null : osId);
                v.setTipoItem(rs.getString("tipo_item"));
                v.setItemId(rs.getInt("item_id"));
                v.setDescricao(rs.getString("descricao"));
                v.setQuantidade(rs.getDouble("quantidade"));
                v.setValorTotal(rs.getDouble("valor_total"));
                v.setCustoTotal(rs.getDouble("custo_total"));
                v.setLucro(rs.getDouble("lucro"));
                v.setFormaPagamento(rs.getString("forma_pagamento"));
                lista.add(v);
            }
        }
        return lista;
    }
}
