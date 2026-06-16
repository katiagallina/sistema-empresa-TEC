package util;

import connection.Conexao;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBTestQuery {
    public static void main(String[] args) {
        try (Connection conn = Conexao.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM produtos")) {
            System.out.println("Produtos cadastrados no banco:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + 
                                   ", Nome: " + rs.getString("nome") + 
                                   ", Custo: " + rs.getDouble("preco_custo") + 
                                   ", Venda: " + rs.getDouble("preco_venda") + 
                                   ", Qtd: " + rs.getDouble("quantidade") + 
                                   ", Tipo: " + rs.getString("tipo_venda"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
