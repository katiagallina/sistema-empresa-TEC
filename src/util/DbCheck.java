package util;

import connection.Conexao;
import java.sql.*;

public class DbCheck {
    public static void main(String[] args) {
        System.out.println("Running database migration to modify status column...");
        try (Connection conn = Conexao.getConnection()) {
            if (conn == null) {
                System.out.println("Failed to connect to database!");
                return;
            }
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE ordem_servico ALTER COLUMN status TYPE VARCHAR(20), ALTER COLUMN status SET DEFAULT 'EM ANDAMENTO'");
                System.out.println("Successfully modified status column to VARCHAR(20)!");
            }
            
            System.out.println("--- Table: ordem_servico (After migration) ---");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT column_name, data_type, is_nullable, column_default FROM information_schema.columns WHERE table_name = 'ordem_servico'")) {
                while (rs.next()) {
                    System.out.printf("Field: %s, Type: %s, Null: %s, Default: %s%n",
                            rs.getString("column_name"), rs.getString("data_type"), rs.getString("is_nullable"),
                            rs.getString("column_default"));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
