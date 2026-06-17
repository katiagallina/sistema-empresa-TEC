package dao;

import connection.Conexao;
import model.ServicoRealizado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicoRealizadoDAO {

    public void inserir(ServicoRealizado sr) throws SQLException {
        String sql = "INSERT INTO servicos_realizados (data_servico, cliente_id, descricao_servico, valor, forma_pagamento, num_parcelas, valor_parcela) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, sr.getDataServico() != null ? sr.getDataServico() : new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, sr.getClienteId());
            stmt.setString(3, sr.getDescricaoServico());
            stmt.setDouble(4, sr.getValor());
            stmt.setString(5, sr.getFormaPagamento());
            stmt.setInt(6, sr.getNumParcelas());
            stmt.setDouble(7, sr.getValorParcela());
            stmt.executeUpdate();
        }
    }

    public List<ServicoRealizado> listarPorPeriodo(java.util.Date inicio, java.util.Date fim) throws SQLException {
        List<ServicoRealizado> lista = new ArrayList<>();
        String sql = "SELECT * FROM servicos_realizados WHERE data_servico BETWEEN ? AND ? ORDER BY data_servico DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(inicio.getTime()));
            stmt.setTimestamp(2, new Timestamp(fim.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ServicoRealizado sr = new ServicoRealizado();
                    sr.setId(rs.getInt("id"));
                    sr.setDataServico(rs.getTimestamp("data_servico"));
                    sr.setClienteId(rs.getInt("cliente_id"));
                    sr.setDescricaoServico(rs.getString("descricao_servico"));
                    sr.setValor(rs.getDouble("valor"));
                    sr.setFormaPagamento(rs.getString("forma_pagamento"));
                    sr.setNumParcelas(rs.getInt("num_parcelas"));
                    sr.setValorParcela(rs.getDouble("valor_parcela"));
                    lista.add(sr);
                }
            }
        }
        return lista;
    }

    public ServicoRealizado buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM servicos_realizados WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ServicoRealizado sr = new ServicoRealizado();
                    sr.setId(rs.getInt("id"));
                    sr.setDataServico(rs.getTimestamp("data_servico"));
                    sr.setClienteId(rs.getInt("cliente_id"));
                    sr.setDescricaoServico(rs.getString("descricao_servico"));
                    sr.setValor(rs.getDouble("valor"));
                    sr.setFormaPagamento(rs.getString("forma_pagamento"));
                    sr.setNumParcelas(rs.getInt("num_parcelas"));
                    sr.setValorParcela(rs.getDouble("valor_parcela"));
                    return sr;
                }
            }
        }
        return null;
    }

    public void atualizar(ServicoRealizado sr) throws SQLException {
        String sql = "UPDATE servicos_realizados SET data_servico=?, cliente_id=?, descricao_servico=?, valor=?, forma_pagamento=?, num_parcelas=?, valor_parcela=? WHERE id=?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, sr.getDataServico());
            stmt.setInt(2, sr.getClienteId());
            stmt.setString(3, sr.getDescricaoServico());
            stmt.setDouble(4, sr.getValor());
            stmt.setString(5, sr.getFormaPagamento());
            stmt.setInt(6, sr.getNumParcelas());
            stmt.setDouble(7, sr.getValorParcela());
            stmt.setInt(8, sr.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM servicos_realizados WHERE id=?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public double getSomaPorFormaPagamento(String forma, java.util.Date inicio, java.util.Date fim) throws SQLException {
        // Se a opção for BOLETO, somamos valor_parcela. Caso contrário, somamos o valor total
        String sql = "SELECT SUM(CASE WHEN forma_pagamento = 'BOLETO' THEN valor_parcela ELSE valor END) " +
                     "FROM servicos_realizados " +
                     "WHERE forma_pagamento = ? AND data_servico BETWEEN ? AND ?";
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

    public double getFaturamentoTotal(java.util.Date inicio, java.util.Date fim) throws SQLException {
        String sql = "SELECT SUM(valor) FROM servicos_realizados WHERE data_servico BETWEEN ? AND ?";
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
