package util;

import connection.Conexao;
import java.sql.Connection;
import java.sql.Statement;

public class DBSetup {
    public static void main(String[] args) {
        System.out.println("Iniciando setup do banco de dados...");
        try (Connection conn = Conexao.getConnection()) {
            if (conn == null) {
                System.out.println("Falha ao obter conexão");
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                // Ajustar clientes
                try {
                    stmt.executeUpdate("ALTER TABLE clientes ADD COLUMN email VARCHAR(100) AFTER telefone");
                    System.out.println("Coluna 'email' adicionada em 'clientes'");
                } catch (Exception e) {}
                try {
                    stmt.executeUpdate("ALTER TABLE clientes ADD COLUMN cidade VARCHAR(100) AFTER endereco");
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

                // Recriar tabela orcamento_itens se necessário ou garantir que existe
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orcamento_itens (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "orcamento_id INT, " +
                        "tipo_item ENUM('PRODUTO', 'SERVICO') NOT NULL, " +
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
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "cliente_id INT, " +
                        "data DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "total DECIMAL(10,2), " +
                        "status VARCHAR(20) DEFAULT 'ABERTO', " +
                        "FOREIGN KEY (cliente_id) REFERENCES clientes(id)" +
                        ")");
                System.out.println("Tabela 'ordem_servico' verificada/criada.");

                // Garantir ordem_servico_itens
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ordem_servico_itens (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "ordem_servico_id INT, " +
                        "tipo_item ENUM('PRODUTO', 'SERVICO') NOT NULL, " +
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

                // Recriar tabela vendas para ser idêntica à foto do usuário
                stmt.executeUpdate("DROP TABLE IF EXISTS vendas");
                stmt.executeUpdate("CREATE TABLE vendas (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "data_venda DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "ordem_servico_id INT NULL, " +
                        "tipo_item ENUM('PRODUTO', 'SERVICO') NOT NULL, " +
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
            }
            System.out.println("Banco de dados configurado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
