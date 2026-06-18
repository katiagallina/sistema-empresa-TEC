package view;

import dao.VendaDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

public class MainScreen extends JFrame {

    private LineChart chartVendasMes;
    private LineChart chartVendasAno;
    private LineChart chartLucroAno;
    
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
        JLabel lblLogo = new JLabel();
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
        try {
            ImageIcon icon = new ImageIcon("logo.png");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage();
                int targetW = 200;
                int targetH = (int) (((double) icon.getIconHeight() / icon.getIconWidth()) * targetW);
                
                // High quality scaling using BufferedImage and Graphics2D RenderingHints
                java.awt.image.BufferedImage resizedImg = new java.awt.image.BufferedImage(targetW, targetH, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resizedImg.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(img, 0, 0, targetW, targetH, null);
                g2d.dispose();
                
                lblLogo.setIcon(new ImageIcon(resizedImg));
            } else {
                lblLogo.setText("TEC Energia");
                lblLogo.setForeground(new Color(255, 193, 7)); // Amarelo Dourado
                lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            }
        } catch (Exception ex) {
            lblLogo.setText("TEC Energia");
            lblLogo.setForeground(new Color(255, 193, 7)); // Amarelo Dourado
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        }
        sideBar.add(lblLogo);

        // Botões do Menu
        JButton btnDashboard = createMenuButton("Dashboard", new MenuIcon(MenuIcon.IconType.DASHBOARD));
        JButton btnClientes = createMenuButton("Clientes", new MenuIcon(MenuIcon.IconType.CLIENTS));
        JButton btnProdutos = createMenuButton("Produtos", new MenuIcon(MenuIcon.IconType.PRODUCTS));
        JButton btnServicos = createMenuButton("Serviços Base", new MenuIcon(MenuIcon.IconType.SERVICES));
        JButton btnServicosRealizados = createMenuButton("Serviços Realizados", new MenuIcon(MenuIcon.IconType.SERVICES_REALIZED));
        JButton btnOrcamentos = createMenuButton("Orçamentos", new MenuIcon(MenuIcon.IconType.ORCAMENTOS));
        JButton btnFinalizarOS = createMenuButton("Finalizar OS", new MenuIcon(MenuIcon.IconType.FINALIZAR_OS));
        JButton btnVendasDirect = createMenuButton("Venda Rápida", new MenuIcon(MenuIcon.IconType.VENTA_RAPIDA));
        JButton btnRelatorios = createMenuButton("Relatórios", new MenuIcon(MenuIcon.IconType.RELATORIOS));
        JButton btnConfig = createMenuButton("Configurações", new MenuIcon(MenuIcon.IconType.CONFIGURACOES));

        sideBar.add(btnDashboard);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnClientes);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnProdutos);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnServicos);
        sideBar.add(Box.createVerticalStrut(8));
        sideBar.add(btnServicosRealizados);
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

        // Painel de Gráficos de Linha
        JPanel dashboardPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        dashboardPanel.setBackground(centerPanel.getBackground());

        // Gráfico 1: Vendas do Mês (Faturamento Diário)
        chartVendasMes = new LineChart("Vendas do Mês (Faturamento Diário)", null, false, false, Color.WHITE, new Color(0, 102, 204));
        dashboardPanel.add(chartVendasMes);

        // Painel inferior para dois gráficos lado a lado
        JPanel bottomChartsPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        bottomChartsPanel.setBackground(centerPanel.getBackground());

        // Gráfico 2: Vendas do Ano (Faturamento Mensal)
        chartVendasAno = new LineChart("Vendas do Ano (Faturamento Mensal)", null, true, false, Color.WHITE, new Color(155, 89, 182));
        bottomChartsPanel.add(chartVendasAno);

        // Gráfico 3: Lucro Estimado do Ano (Mensal com destaque em vermelho se <= 0)
        chartLucroAno = new LineChart("Lucro Estimado do Ano", null, true, true, Color.WHITE, new Color(40, 167, 69));
        bottomChartsPanel.add(chartLucroAno);

        dashboardPanel.add(bottomChartsPanel);

        centerPanel.add(dashboardPanel, BorderLayout.CENTER);
        
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
        btnServicosRealizados.addActionListener(e -> {
            ServicoRealizadoScreen srs = new ServicoRealizadoScreen(this);
            srs.setVisible(true);
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
            java.util.Map<Integer, Double> vendasMes = vendaDAO.getVendasDiariasMesAtual();
            java.util.Map<Integer, Double> vendasAno = vendaDAO.getVendasMensaisAnoAtual();
            java.util.Map<Integer, Double> lucroAno = vendaDAO.getLucroMensalAnoAtual();

            chartVendasMes.setData(vendasMes);
            chartVendasAno.setData(vendasAno);
            chartLucroAno.setData(lucroAno);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao atualizar gráficos do dashboard: " + e.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createMenuButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setIconTextGap(12);
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
