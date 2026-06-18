package dao;

import connection.Conexao;
import model.Despesa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DespesaDAO {

    public void inserir(Despesa d) throws SQLException {
        String sql = "INSERT INTO despesas (data_despesa, descricao, valor, forma_pagamento) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, d.getDataDespesa() != null ? d.getDataDespesa() : new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, d.getDescricao());
            stmt.setDouble(3, d.getValor());
            stmt.setString(4, d.getFormaPagamento());
            stmt.executeUpdate();
        }
    }

    public List<Despesa> listarPorPeriodo(java.util.Date inicio, java.util.Date fim) throws SQLException {
        List<Despesa> lista = new ArrayList<>();
        String sql = "SELECT * FROM despesas WHERE data_despesa BETWEEN ? AND ? ORDER BY data_despesa DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(inicio.getTime()));
            stmt.setTimestamp(2, new Timestamp(fim.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Despesa d = new Despesa();
                    d.setId(rs.getInt("id"));
                    d.setDataDespesa(rs.getTimestamp("data_despesa"));
                    d.setDescricao(rs.getString("descricao"));
                    d.setValor(rs.getDouble("valor"));
                    d.setFormaPagamento(rs.getString("forma_pagamento"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    public Despesa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM despesas WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Despesa d = new Despesa();
                    d.setId(rs.getInt("id"));
                    d.setDataDespesa(rs.getTimestamp("data_despesa"));
                    d.setDescricao(rs.getString("descricao"));
                    d.setValor(rs.getDouble("valor"));
                    d.setFormaPagamento(rs.getString("forma_pagamento"));
                    return d;
                }
            }
        }
        return null;
    }

    public void atualizar(Despesa d) throws SQLException {
        String sql = "UPDATE despesas SET data_despesa=?, descricao=?, valor=?, forma_pagamento=? WHERE id=?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, d.getDataDespesa());
            stmt.setString(2, d.getDescricao());
            stmt.setDouble(3, d.getValor());
            stmt.setString(4, d.getFormaPagamento());
            stmt.setInt(5, d.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM despesas WHERE id=?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public double getSomaPorFormaPagamento(String forma, java.util.Date inicio, java.util.Date fim) throws SQLException {
        String sql = "SELECT SUM(valor) FROM despesas WHERE forma_pagamento = ? AND data_despesa BETWEEN ? AND ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, forma);
            stmt.setTimestamp(2, new Timestamp(inicio.getTime()));
            stmt.setTimestamp(3, new Timestamp(fim.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public double getSomaTotal(java.util.Date inicio, java.util.Date fim) throws SQLException {
        String sql = "SELECT SUM(valor) FROM despesas WHERE data_despesa BETWEEN ? AND ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(inicio.getTime()));
            stmt.setTimestamp(2, new Timestamp(fim.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
}
