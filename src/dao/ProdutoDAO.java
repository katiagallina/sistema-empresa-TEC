package dao;

import connection.Conexao;
import model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    // 🔹 INSERT
    public void inserir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produtos (nome, preco_custo, preco_venda, quantidade, tipo_venda) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setDouble(2, produto.getPrecoCusto());
            stmt.setDouble(3, produto.getPrecoVenda());
            stmt.setDouble(4, produto.getQuantidade());
            stmt.setString(5, produto.getTipoVenda());
            stmt.executeUpdate();
        }
    }

    // 🔹 LISTAR TODOS
    public List<Produto> listar() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtos ORDER BY nome";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setPrecoCusto(rs.getDouble("preco_custo"));
                p.setPrecoVenda(rs.getDouble("preco_venda"));
                p.setQuantidade(rs.getDouble("quantidade"));
                p.setTipoVenda(rs.getString("tipo_venda"));
                lista.add(p);
            }
        }
        return lista;
    }

    // 🔹 BUSCAR POR ID
    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produtos WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produto p = new Produto();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    p.setPrecoCusto(rs.getDouble("preco_custo"));
                    p.setPrecoVenda(rs.getDouble("preco_venda"));
                    p.setQuantidade(rs.getDouble("quantidade"));
                    p.setTipoVenda(rs.getString("tipo_venda"));
                    return p;
                }
            }
        }
        return null;
    }

    // 🔹 BUSCAR POR NOME (Autocomplete)
    public List<Produto> buscarPorNome(String nome) throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtos WHERE nome LIKE ? ORDER BY nome";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto p = new Produto();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    p.setPrecoCusto(rs.getDouble("preco_custo"));
                    p.setPrecoVenda(rs.getDouble("preco_venda"));
                    p.setQuantidade(rs.getDouble("quantidade"));
                    p.setTipoVenda(rs.getString("tipo_venda"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    // 🔹 ATUALIZAR
    public void atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produtos SET nome=?, preco_custo=?, preco_venda=?, quantidade=?, tipo_venda=? WHERE id=?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setDouble(2, produto.getPrecoCusto());
            stmt.setDouble(3, produto.getPrecoVenda());
            stmt.setDouble(4, produto.getQuantidade());
            stmt.setString(5, produto.getTipoVenda());
            stmt.setInt(6, produto.getId());
            stmt.executeUpdate();
        }
    }

    // 🔹 DELETAR
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM produtos WHERE id=?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}