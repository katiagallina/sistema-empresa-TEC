package view;

import dao.VendaDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

public class MainScreen extends JFrame {

    private JLabel lblVendasMes;
    private JLabel lblServicosMes;
    private JLabel lblLucroMes;
    private JLabel lblClientesCadastrados;
    
    private VendaDAO vendaDAO;

    public MainScreen() {
        setTitle("TEC Energia e Soluções - ERP Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        vendaDAO = new VendaDAO();

        // Painel Principal com BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 246, 250));

        // 🔹 1. PAINEL LATERAL (Menu)
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBackground(new Color(33, 37, 41)); // Dark Sidebar
        sideBar.setPreferredSize(new Dimension(220, 0));
        sideBar.setBorder(new EmptyBorder(15, 10, 15, 10));

        // Logo
        JLabel lblLogo = new JLabel("TEC Energia");
        lblLogo.setForeground(new Color(255, 193, 7)); // Amarelo Dourado
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));
        sideBar.add(lblLogo);

        // Botões do Menu
        JButton btnDashboard = createMenuButton("🏠  Dashboard");
        JButton btnClientes = createMenuButton("👥  Clientes");
        JButton btnProdutos = createMenuButton("📦  Produtos");
        JButton btnServicos = createMenuButton("🛠️  Serviços");
        JButton btnOrcamentos = createMenuButton("📄  Orçamentos");
        JButton btnFinalizarOS = createMenuButton("✅  Finalizar OS");
        JButton btnVendasDirect = createMenuButton("💰  Venda Rápida");
        JButton btnRelatorios = createMenuButton("📊  Relatórios");
        JButton btnConfig = createMenuButton("⚙️  Configurações");

        sideBar.add(btnDashboard);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnClientes);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnProdutos);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnServicos);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnOrcamentos);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnFinalizarOS);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnVendasDirect);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnRelatorios);
        sideBar.add(Box.createVerticalStrut(30));
        sideBar.add(btnConfig);

        mainPanel.add(sideBar, BorderLayout.WEST);

        // 🔹 2. PAINEL CENTRAL (Dashboard / Área Principal)
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(new Color(245, 246, 250));
        centerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(centerPanel.getBackground());
        JLabel titleLabel = new JLabel("Painel Geral de Controle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(33, 37, 41));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnAtualizar = new JButton("🔄 Recarregar Dados");
        btnAtualizar.setBackground(new Color(0, 123, 255));
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAtualizar.setFocusPainted(false);
        headerPanel.add(btnAtualizar, BorderLayout.EAST);

        centerPanel.add(headerPanel, BorderLayout.NORTH);

        // Grid de KPI Cards (Área Central)
        JPanel gridCards = new JPanel(new GridLayout(2, 2, 20, 20));
        gridCards.setBackground(centerPanel.getBackground());

        // Card 1: Faturamento Mensal
        JPanel cardVendas = createKpiCard("TOTAL DE VENDAS DO MÊS", "R$ 0,00", new Color(0, 102, 204));
        lblVendasMes = (JLabel) cardVendas.getClientProperty("valueLabel");
        gridCards.add(cardVendas);

        // Card 2: Serviços Mensais
        JPanel cardServicos = createKpiCard("TOTAL DE SERVIÇOS DO MÊS", "R$ 0,00", new Color(230, 126, 34));
        lblServicosMes = (JLabel) cardServicos.getClientProperty("valueLabel");
        gridCards.add(cardServicos);

        // Card 3: Lucro do Mês
        JPanel cardLucro = createKpiCard("LUCRO ESTIMADO DO MÊS", "R$ 0,00", new Color(40, 167, 69));
        lblLucroMes = (JLabel) cardLucro.getClientProperty("valueLabel");
        gridCards.add(cardLucro);

        // Card 4: Clientes Cadastrados
        JPanel cardClientes = createKpiCard("CLIENTES ATIVOS CADASTRADOS", "0 Clientes", new Color(155, 89, 182));
        lblClientesCadastrados = (JLabel) cardClientes.getClientProperty("valueLabel");
        gridCards.add(cardClientes);

        centerPanel.add(gridCards, BorderLayout.CENTER);
        
        // Rodapé Informativo
        JLabel lblFooter = new JLabel("TEC Energia e Soluções Desktop v1.1.0 | Sistema preparado para expansões");
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(Color.GRAY);
        centerPanel.add(lblFooter, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        // 🔹 3. AÇÕES DO MENU
        btnClientes.addActionListener(e -> {
            ClienteScreen cs = new ClienteScreen(this);
            cs.setVisible(true);
            carregarDashboard();
        });
        btnProdutos.addActionListener(e -> {
            ProductListScreen pls = new ProductListScreen(this);
            pls.setVisible(true);
            carregarDashboard();
        });
        btnServicos.addActionListener(e -> {
            ServicoScreen ss = new ServicoScreen(this);
            ss.setVisible(true);
            carregarDashboard();
        });
        btnOrcamentos.addActionListener(e -> {
            OrcamentoScreen os = new OrcamentoScreen(this);
            os.setVisible(true);
            carregarDashboard();
        });
        btnFinalizarOS.addActionListener(e -> {
            FinalizarOSScreen fos = new FinalizarOSScreen(this);
            fos.setVisible(true);
            carregarDashboard();
        });
        btnVendasDirect.addActionListener(e -> {
            VendasDirectScreen vds = new VendasDirectScreen(this);
            vds.setVisible(true);
            carregarDashboard();
        });
        btnRelatorios.addActionListener(e -> {
            RelatorioVendasScreen rvs = new RelatorioVendasScreen();
            rvs.setVisible(true);
        });
        btnDashboard.addActionListener(e -> carregarDashboard());
        btnAtualizar.addActionListener(e -> carregarDashboard());
        btnConfig.addActionListener(e -> JOptionPane.showMessageDialog(this, 
                "Configurações do Sistema:\n- Banco de Dados: sistema_empresa (MySQL)\n- Look and Feel: FlatLaf Light\n- Geração de Relatórios: iText 7 PDF", 
                "Informações de Configuração", JOptionPane.INFORMATION_MESSAGE));

        // Carregar dados na abertura
        carregarDashboard();
    }

    private void carregarDashboard() {
        try {
            double faturamento = vendaDAO.getFaturamentoMensal();
            double servicos = vendaDAO.getFaturamentoServicosMensal();
            double lucro = vendaDAO.getLucroMensal();
            int clientesCount = vendaDAO.getQuantidadeClientes();

            lblVendasMes.setText(String.format("R$ %.2f", faturamento));
            lblServicosMes.setText(String.format("R$ %.2f", servicos));
            lblLucroMes.setText(String.format("R$ %.2f", lucro));
            lblClientesCadastrados.setText(clientesCount + (clientesCount == 1 ? " Cliente" : " Clientes"));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao atualizar indicadores do dashboard: " + e.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 42));
        btn.setPreferredSize(new Dimension(200, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(new Color(33, 37, 41));
        btn.setForeground(new Color(222, 226, 230));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Efeito Hover simples
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(49, 54, 60));
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(33, 37, 41));
                btn.setForeground(new Color(222, 226, 230));
            }
        });

        return btn;
    }

    private JPanel createKpiCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.putClientProperty("FlatLaf.style", "arc: 18; background: " + String.format("#%02x%02x%02x", bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(248, 249, 250)); // Branco gelo

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        // Passa o label de valor como propriedade para fácil atualização posterior
        card.putClientProperty("valueLabel", valueLabel);

        return card;
    }

    public static void main(String[] args) {
        // Look and Feel FlatLaf
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Falha ao inicializar o FlatLaf.");
        }

        SwingUtilities.invokeLater(() -> {
            MainScreen mainScreen = new MainScreen();
            mainScreen.setVisible(true);
        });
    }
}
