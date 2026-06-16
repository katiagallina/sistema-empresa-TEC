package dao;

import connection.Conexao;
import model.Servico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicoDAO {

    public void inserir(Servico servico) throws SQLException {
        String sql = "INSERT INTO servicos (nome, tipo, valor_base) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getTipo());
            stmt.setDouble(3, servico.getValorBase());
            stmt.executeUpdate();
        }
    }

    public List<Servico> listar() throws SQLException {
        List<Servico> lista = new ArrayList<>();
        String sql = "SELECT * FROM servicos ORDER BY nome";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Servico s = new Servico();
                s.setId(rs.getInt("id"));
                s.setNome(rs.getString("nome"));
                s.setTipo(rs.getString("tipo"));
                s.setValorBase(rs.getDouble("valor_base"));
                lista.add(s);
            }
        }
        return lista;
    }

    public Servico buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM servicos WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Servico s = new Servico();
                    s.setId(rs.getInt("id"));
                    s.setNome(rs.getString("nome"));
                    s.setTipo(rs.getString("tipo"));
                    s.setValorBase(rs.getDouble("valor_base"));
                    return s;
                }
            }
        }
        return null;
    }

    public List<Servico> buscarPorNome(String nome) throws SQLException {
        List<Servico> lista = new ArrayList<>();
        String sql = "SELECT * FROM servicos WHERE nome LIKE ? ORDER BY nome";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Servico s = new Servico();
                    s.setId(rs.getInt("id"));
                    s.setNome(rs.getString("nome"));
                    s.setTipo(rs.getString("tipo"));
                    s.setValorBase(rs.getDouble("valor_base"));
                    lista.add(s);
                }
            }
        }
        return lista;
    }

    public void atualizar(Servico servico) throws SQLException {
        String sql = "UPDATE servicos SET nome=?, tipo=?, valor_base=? WHERE id=?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getTipo());
            stmt.setDouble(3, servico.getValorBase());
            stmt.setInt(4, servico.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM servicos WHERE id=?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
