import view.MainScreen;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import connection.Conexao;
import javax.swing.JOptionPane;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {
        // 🔹 Configura o Look and Feel FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Falha ao inicializar o LaF. O tema padrão será usado.");
        }

        // 🔹 Teste de conexão e migração do banco
        try {
            Connection conn = Conexao.getConnection();
            if (conn != null) {
                // Garante que o status da ordem_servico seja VARCHAR(20) para suportar
                // PAGO/PENDENTE
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(
                            "ALTER TABLE ordem_servico ALTER COLUMN status TYPE VARCHAR(20), ALTER COLUMN status SET DEFAULT 'EM ANDAMENTO'");

                    // Garante que a tabela de despesas exista
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS despesas (" +
                            "id SERIAL PRIMARY KEY, " +
                            "data_despesa TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "descricao VARCHAR(255) NOT NULL, " +
                            "valor DECIMAL(10,2) NOT NULL, " +
                            "forma_pagamento VARCHAR(50) NOT NULL" +
                            ")");
                } catch (Exception ex) {
                    System.err.println("Erro ao rodar migracao automatica: " + ex.getMessage());
                }
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao conectar ao banco de dados.", "Erro de Conexao",
                    JOptionPane.ERROR_MESSAGE);
            return; // se não conectar, para o programa
        }

        // Usar o Event Dispatch Thread para garantir a segurança da thread
        SwingUtilities.invokeLater(() -> {
            MainScreen mainScreen = new MainScreen();
            mainScreen.setVisible(true);
        });
    }
}