package util;

import connection.Conexao;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DBDiagnose {
    public static void main(String[] args) {
        try (Connection conn = Conexao.getConnection()) {
            if (conn == null) {
                System.out.println("Falha ao obter conexão");
                return;
            }
            System.out.println("Conectado ao Banco com Sucesso!");
            DatabaseMetaData dbmd = conn.getMetaData();
            
            // Listar tabelas
            try (ResultSet rs = dbmd.getTables("sistema_empresa", null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    System.out.println("\nTabela: " + tableName);
                    
                    // Colunas
                    try (Statement stmt = conn.createStatement();
                         ResultSet rsCol = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 1")) {
                        ResultSetMetaData rsmd = rsCol.getMetaData();
                        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                            System.out.println("  - Coluna: " + rsmd.getColumnName(i) + " (" + rsmd.getColumnTypeName(i) + ")");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
