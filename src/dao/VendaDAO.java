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
        String sqlVendas = "SELECT * FROM vendas WHERE data_venda BETWEEN ? AND ? ORDER BY data_venda DESC";
        String sqlServicos = "SELECT * FROM servicos_realizados WHERE data_servico BETWEEN ? AND ? ORDER BY data_servico DESC";
        
        try (Connection conn = Conexao.getConnection()) {
            // 1. Carrega as vendas normais
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas)) {
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
            
            // 2. Carrega os serviços realizados
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos)) {
                stmt.setTimestamp(1, new Timestamp(inicio.getTime()));
                stmt.setTimestamp(2, new Timestamp(fim.getTime()));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Venda v = new Venda();
                        v.setId(-rs.getInt("id")); // Diferenciar IDs
                        v.setDataVenda(rs.getTimestamp("data_servico"));
                        v.setOrdemServicoId(null);
                        v.setTipoItem("SERVICO");
                        v.setItemId(0);
                        v.setDescricao("[SERVIÇO REALIZADO] " + rs.getString("descricao_servico"));
                        v.setQuantidade(1.0);
                        v.setValorTotal(rs.getDouble("valor"));
                        v.setCustoTotal(0.0);
                        v.setLucro(rs.getDouble("valor")); // 100% lucro operacinal
                        v.setFormaPagamento(rs.getString("forma_pagamento"));
                        lista.add(v);
                    }
                }
            }
        }
        
        // 3. Ordena a lista por data_venda decrescente
        lista.sort((v1, v2) -> {
            if (v1.getDataVenda() == null || v2.getDataVenda() == null) return 0;
            return v2.getDataVenda().compareTo(v1.getDataVenda());
        });
        
        return lista;
    }

    public double getFaturamentoDiario() throws SQLException {
        String sqlVendas = "SELECT SUM(valor_total) FROM vendas WHERE CAST(data_venda AS DATE) = CURRENT_DATE";
        String sqlServicos = "SELECT SUM(valor) FROM servicos_realizados WHERE CAST(data_servico AS DATE) = CURRENT_DATE";
        
        double total = 0.0;
        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
        }
        return total;
    }

    public double getFaturamentoMensal() throws SQLException {
        String sqlVendas = "SELECT SUM(valor_total) FROM vendas WHERE EXTRACT(MONTH FROM data_venda) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM data_venda) = EXTRACT(YEAR FROM CURRENT_DATE)";
        String sqlServicos = "SELECT SUM(valor) FROM servicos_realizados WHERE EXTRACT(MONTH FROM data_servico) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM data_servico) = EXTRACT(YEAR FROM CURRENT_DATE)";
        
        double total = 0.0;
        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
        }
        return total;
    }

    public double getLucroDiario() throws SQLException {
        String sqlVendas = "SELECT SUM(lucro) FROM vendas WHERE CAST(data_venda AS DATE) = CURRENT_DATE";
        String sqlServicos = "SELECT SUM(valor) FROM servicos_realizados WHERE CAST(data_servico AS DATE) = CURRENT_DATE";
        
        double total = 0.0;
        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
        }
        return total;
    }

    public double getLucroMensal() throws SQLException {
        String sqlVendas = "SELECT SUM(lucro) FROM vendas WHERE EXTRACT(MONTH FROM data_venda) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM data_venda) = EXTRACT(YEAR FROM CURRENT_DATE)";
        String sqlServicos = "SELECT SUM(valor) FROM servicos_realizados WHERE EXTRACT(MONTH FROM data_servico) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM data_servico) = EXTRACT(YEAR FROM CURRENT_DATE)";
        
        double total = 0.0;
        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
        }
        return total;
    }

    public double getFaturamentoServicosMensal() throws SQLException {
        String sqlVendas = "SELECT SUM(valor_total) FROM vendas WHERE tipo_item = 'SERVICO' AND EXTRACT(MONTH FROM data_venda) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM data_venda) = EXTRACT(YEAR FROM CURRENT_DATE)";
        String sqlServicos = "SELECT SUM(valor) FROM servicos_realizados WHERE EXTRACT(MONTH FROM data_servico) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM data_servico) = EXTRACT(YEAR FROM CURRENT_DATE)";
        
        double total = 0.0;
        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getDouble(1);
                }
            }
        }
        return total;
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
        String sqlVendas = "SELECT * FROM vendas ORDER BY data_venda DESC";
        String sqlServicos = "SELECT * FROM servicos_realizados ORDER BY data_servico DESC";
        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
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
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Venda v = new Venda();
                    v.setId(-rs.getInt("id"));
                    v.setDataVenda(rs.getTimestamp("data_servico"));
                    v.setOrdemServicoId(null);
                    v.setTipoItem("SERVICO");
                    v.setItemId(0);
                    v.setDescricao("[SERVIÇO REALIZADO] " + rs.getString("descricao_servico"));
                    v.setQuantidade(1.0);
                    v.setValorTotal(rs.getDouble("valor"));
                    v.setCustoTotal(0.0);
                    v.setLucro(rs.getDouble("valor"));
                    v.setFormaPagamento(rs.getString("forma_pagamento"));
                    lista.add(v);
                }
            }
        }
        
        lista.sort((v1, v2) -> {
            if (v1.getDataVenda() == null || v2.getDataVenda() == null) return 0;
            return v2.getDataVenda().compareTo(v1.getDataVenda());
        });
        
        return lista;
    }

    public java.util.Map<Integer, Double> getVendasDiariasMesAtual() throws SQLException {
        java.util.Map<Integer, Double> dados = new java.util.TreeMap<>();
        int diasNoMes = java.time.YearMonth.now().lengthOfMonth();
        for (int i = 1; i <= diasNoMes; i++) {
            dados.put(i, 0.0);
        }

        String sqlVendas = "SELECT EXTRACT(DAY FROM data_venda) as dia, SUM(valor_total) as total " +
                           "FROM vendas " +
                           "WHERE EXTRACT(MONTH FROM data_venda) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM data_venda) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                           "GROUP BY EXTRACT(DAY FROM data_venda)";
        String sqlServicos = "SELECT EXTRACT(DAY FROM data_servico) as dia, SUM(valor) as total " +
                             "FROM servicos_realizados " +
                             "WHERE EXTRACT(MONTH FROM data_servico) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM data_servico) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                             "GROUP BY EXTRACT(DAY FROM data_servico)";

        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int dia = rs.getInt("dia");
                    double total = rs.getDouble("total");
                    dados.put(dia, dados.getOrDefault(dia, 0.0) + total);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int dia = rs.getInt("dia");
                    double total = rs.getDouble("total");
                    dados.put(dia, dados.getOrDefault(dia, 0.0) + total);
                }
            }
        }
        return dados;
    }

    public java.util.Map<Integer, Double> getVendasMensaisAnoAtual() throws SQLException {
        java.util.Map<Integer, Double> dados = new java.util.TreeMap<>();
        for (int i = 1; i <= 12; i++) {
            dados.put(i, 0.0);
        }

        String sqlVendas = "SELECT EXTRACT(MONTH FROM data_venda) as mes, SUM(valor_total) as total " +
                           "FROM vendas " +
                           "WHERE EXTRACT(YEAR FROM data_venda) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                           "GROUP BY EXTRACT(MONTH FROM data_venda)";
        String sqlServicos = "SELECT EXTRACT(MONTH FROM data_servico) as mes, SUM(valor) as total " +
                             "FROM servicos_realizados " +
                             "WHERE EXTRACT(YEAR FROM data_servico) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                             "GROUP BY EXTRACT(MONTH FROM data_servico)";

        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    double total = rs.getDouble("total");
                    dados.put(mes, dados.getOrDefault(mes, 0.0) + total);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    double total = rs.getDouble("total");
                    dados.put(mes, dados.getOrDefault(mes, 0.0) + total);
                }
            }
        }
        return dados;
    }

    public java.util.Map<Integer, Double> getLucroMensalAnoAtual() throws SQLException {
        java.util.Map<Integer, Double> dados = new java.util.TreeMap<>();
        for (int i = 1; i <= 12; i++) {
            dados.put(i, 0.0);
        }

        String sqlVendas = "SELECT EXTRACT(MONTH FROM data_venda) as mes, SUM(lucro) as total " +
                           "FROM vendas " +
                           "WHERE EXTRACT(YEAR FROM data_venda) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                           "GROUP BY EXTRACT(MONTH FROM data_venda)";
        String sqlServicos = "SELECT EXTRACT(MONTH FROM data_servico) as mes, SUM(valor) as total " +
                             "FROM servicos_realizados " +
                             "WHERE EXTRACT(YEAR FROM data_servico) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                             "GROUP BY EXTRACT(MONTH FROM data_servico)";
        String sqlDespesas = "SELECT EXTRACT(MONTH FROM data_despesa) as mes, SUM(valor) as total " +
                             "FROM despesas " +
                             "WHERE EXTRACT(YEAR FROM data_despesa) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                             "GROUP BY EXTRACT(MONTH FROM data_despesa)";

        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    double total = rs.getDouble("total");
                    dados.put(mes, dados.getOrDefault(mes, 0.0) + total);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlServicos);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    double total = rs.getDouble("total");
                    dados.put(mes, dados.getOrDefault(mes, 0.0) + total);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlDespesas);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    double total = rs.getDouble("total");
                    dados.put(mes, dados.getOrDefault(mes, 0.0) - total);
                }
            }
        }
        return dados;
    }
}
