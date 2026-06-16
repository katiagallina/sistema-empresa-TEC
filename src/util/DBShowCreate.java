package util;

import connection.Conexao;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBShowCreate {
    public static void main(String[] args) {
        String[] tables = {"clientes", "produtos", "servicos", "orcamentos", "orcamento_itens", "ordem_servico", "ordem_servico_itens", "vendas"};
        try (Connection conn = Conexao.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + table)) {
                    if (rs.next()) {
                        System.out.println("\n--- Table: " + table + " ---");
                        System.out.println(rs.getString(2));
                    }
                } catch (Exception e) {
                    System.out.println("Erro ao mostrar tabela " + table + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
