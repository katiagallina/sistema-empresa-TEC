package connection;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexao {

    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Aviso: Nao foi possivel carregar db.properties. Tentando usar configuracoes padrao de desenvolvimento.");
            properties.setProperty("db.url", "jdbc:postgresql://localhost:5432/postgres");
            properties.setProperty("db.user", "postgres");
            properties.setProperty("db.password", "postgres");
        }
    }

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver"); // Garante que o driver do PostgreSQL seja carregado
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String pass = properties.getProperty("db.password");
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erro ao conectar ao banco PostgreSQL: " + e.getMessage());
            return null;
        }
    }
}
