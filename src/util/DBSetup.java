package util;

import connection.Conexao;
import java.sql.Connection;
import java.sql.Statement;

public class DBSetup {
    public static void main(String[] args) {
        System.out.println("Iniciando setup do banco de dados PostgreSQL (Supabase)...");
        try (Connection conn = Conexao.getConnection()) {
            if (conn == null) {
                System.out.println("Falha ao obter conexao");
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                // 1. Criar tabelas base se nao existirem
                
                // Clientes
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clientes (" +
                        "id SERIAL PRIMARY KEY, " +
                        "nome VARCHAR(150) NOT NULL, " +
                        "telefone VARCHAR(20), " +
                        "email VARCHAR(100), " +
                        "endereco VARCHAR(255), " +
                        "cidade VARCHAR(100), " +
                        "observacoes TEXT" +
                        ")");
                System.out.println("Tabela 'clientes' verificada/criada.");

                // Produtos
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS produtos (" +
                        "id SERIAL PRIMARY KEY, " +
                        "nome VARCHAR(150) NOT NULL, " +
                        "preco_custo DECIMAL(10,2) NOT NULL, " +
                        "preco_venda DECIMAL(10,2) NOT NULL, " +
                        "quantidade DECIMAL(10,2) NOT NULL, " +
                        "tipo_venda VARCHAR(50) NOT NULL" +
                        ")");
                System.out.println("Tabela 'produtos' verificada/criada.");

                // Servicos
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS servicos (" +
                        "id SERIAL PRIMARY KEY, " +
                        "nome VARCHAR(150) NOT NULL, " +
                        "tipo VARCHAR(50) NOT NULL, " + // POR_HORA ou VALOR_FIXO
                        "valor_base DECIMAL(10,2) NOT NULL" +
                        ")");
                System.out.println("Tabela 'servicos' verificada/criada.");

                // Orcamentos
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orcamentos (" +
                        "id SERIAL PRIMARY KEY, " +
                        "cliente_id INT, " +
                        "data TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "total DECIMAL(10,2) NOT NULL, " +
                        "status VARCHAR(20) DEFAULT 'ABERTO', " +
                        "FOREIGN KEY (cliente_id) REFERENCES clientes(id)" +
                        ")");
                System.out.println("Tabela 'orcamentos' verificada/criada.");

                // Ajustar clientes (para caso o banco ja existisse mas sem essas colunas, embora no Supabase seja novo)
                try {
                    stmt.executeUpdate("ALTER TABLE clientes ADD COLUMN email VARCHAR(100)");
                    System.out.println("Coluna 'email' adicionada em 'clientes'");
                } catch (Exception e) {}
                try {
                    stmt.executeUpdate("ALTER TABLE clientes ADD COLUMN cidade VARCHAR(100)");
                    System.out.println("Coluna 'cidade' adicionada em 'clientes'");
                } catch (Exception e) {}
                try {
                    stmt.executeUpdate("ALTER TABLE clientes ADD COLUMN observacoes TEXT");
                    System.out.println("Coluna 'observacoes' adicionada em 'clientes'");
                } catch (Exception e) {}

                // Ajustar orcamentos
                try {
                    stmt.executeUpdate("ALTER TABLE orcamentos ADD COLUMN status VARCHAR(20) DEFAULT 'ABERTO'");
                    System.out.println("Coluna 'status' adicionada em 'orcamentos'");
                } catch (Exception e) {}

                // Recriar tabela orcamento_itens se necessario ou garantir que existe
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orcamento_itens (" +
                        "id SERIAL PRIMARY KEY, " +
                        "orcamento_id INT, " +
                        "tipo_item VARCHAR(20) NOT NULL CHECK (tipo_item IN ('PRODUTO', 'SERVICO')), " +
                        "produto_id INT NULL, " +
                        "servico_id INT NULL, " +
                        "descricao VARCHAR(150), " +
                        "quantidade DECIMAL(10,2), " +
                        "valor_unitario DECIMAL(10,2), " +
                        "valor_total DECIMAL(10,2), " +
                        "FOREIGN KEY (orcamento_id) REFERENCES orcamentos(id), " +
                        "FOREIGN KEY (produto_id) REFERENCES produtos(id), " +
                        "FOREIGN KEY (servico_id) REFERENCES servicos(id)" +
                        ")");
                System.out.println("Tabela 'orcamento_itens' verificada/criada.");

                // Garantir ordem_servico
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ordem_servico (" +
                        "id SERIAL PRIMARY KEY, " +
                        "cliente_id INT, " +
                        "data TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "total DECIMAL(10,2), " +
                        "status VARCHAR(20) DEFAULT 'ABERTO', " +
                        "FOREIGN KEY (cliente_id) REFERENCES clientes(id)" +
                        ")");
                System.out.println("Tabela 'ordem_servico' verificada/criada.");

                // Garantir ordem_servico_itens
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ordem_servico_itens (" +
                        "id SERIAL PRIMARY KEY, " +
                        "ordem_servico_id INT, " +
                        "tipo_item VARCHAR(20) NOT NULL CHECK (tipo_item IN ('PRODUTO', 'SERVICO')), " +
                        "produto_id INT NULL, " +
                        "servico_id INT NULL, " +
                        "descricao VARCHAR(150), " +
                        "quantidade DECIMAL(10,2), " +
                        "valor_unitario DECIMAL(10,2), " +
                        "valor_total DECIMAL(10,2), " +
                        "FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico(id), " +
                        "FOREIGN KEY (produto_id) REFERENCES produtos(id), " +
                        "FOREIGN KEY (servico_id) REFERENCES servicos(id)" +
                        ")");
                System.out.println("Tabela 'ordem_servico_itens' verificada/criada.");

                // Para PostgreSQL, fazemos DROP TABLE IF EXISTS vendas CASCADE se quisermos recriar
                stmt.executeUpdate("DROP TABLE IF EXISTS vendas CASCADE");
                stmt.executeUpdate("CREATE TABLE vendas (" +
                        "id SERIAL PRIMARY KEY, " +
                        "data_venda TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "ordem_servico_id INT NULL, " +
                        "tipo_item VARCHAR(20) NOT NULL CHECK (tipo_item IN ('PRODUTO', 'SERVICO')), " +
                        "item_id INT NULL, " +
                        "descricao VARCHAR(150), " +
                        "quantidade DECIMAL(10,2) NOT NULL, " +
                        "valor_total DECIMAL(10,2) NOT NULL, " +
                        "custo_total DECIMAL(10,2) NOT NULL, " +
                        "lucro DECIMAL(10,2) NOT NULL, " +
                        "forma_pagamento VARCHAR(50), " +
                        "FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico(id)" +
                        ")");
                System.out.println("Tabela 'vendas' recriada com sucesso.");

                // Criar tabela de servicos realizados
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS servicos_realizados (" +
                        "id SERIAL PRIMARY KEY, " +
                        "data_servico TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "cliente_id INT, " +
                        "descricao_servico VARCHAR(255) NOT NULL, " +
                        "valor DECIMAL(10,2) NOT NULL, " +
                        "forma_pagamento VARCHAR(20) NOT NULL CHECK (forma_pagamento IN ('DINHEIRO', 'PIX', 'CHEQUE', 'BOLETO')), " +
                        "num_parcelas INT DEFAULT 1, " +
                        "valor_parcela DECIMAL(10,2) NOT NULL, " +
                        "FOREIGN KEY (cliente_id) REFERENCES clientes(id)" +
                        ")");
                System.out.println("Tabela 'servicos_realizados' verificada/criada.");
            }
            System.out.println("Banco de dados configurado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
