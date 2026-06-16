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

        // 🔹 Teste de conexão
        try {
            Connection conn = Conexao.getConnection();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao conectar ao banco de dados.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            return; // se não conectar, para o programa
        }

        // Usar o Event Dispatch Thread para garantir a segurança da thread
        SwingUtilities.invokeLater(() -> {
            MainScreen mainScreen = new MainScreen();
            mainScreen.setVisible(true);
        });
    }
}