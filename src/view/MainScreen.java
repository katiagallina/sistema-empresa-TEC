package view;

import javax.swing.*;
import java.awt.*;
import dao.OrcamentoDAO;
import dao.ItemOrcamentoDAO;
import model.Orcamento;
import model.ItemOrcamento;
import util.PdfGenerator;
import java.util.List;

public class MainScreen extends JFrame {

    public MainScreen() {
        setTitle("Sistema de Gestão");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Painel lateral (Menu)
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBackground(new Color(45, 52, 54));
        sideBar.setPreferredSize(new Dimension(200, 0));

        JLabel lblLogo = new JLabel("TEC Energia");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Arial", Font.BOLD, 20));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sideBar.add(lblLogo);

        // Botões
        JButton btnCadastrarProduto = createMenuButton("Cadastrar Produto");
        JButton btnListarProdutos = createMenuButton("Listar Produtos");
        JButton btnCriarOrcamento = createMenuButton("Criar Orçamento");
        JButton btnFinalizarOS = createMenuButton("Finalizar OS");
        JButton btnRelatorioVendas = createMenuButton("Relatório de Vendas");
        JButton btnGerarPdf = createMenuButton("Gerar PDF Orçamento");

        sideBar.add(btnCadastrarProduto);
        sideBar.add(btnListarProdutos);
        sideBar.add(btnCriarOrcamento);
        sideBar.add(btnFinalizarOS);
        sideBar.add(btnRelatorioVendas);
        sideBar.add(btnGerarPdf);

        mainPanel.add(sideBar, BorderLayout.WEST);

        // Painel central (Dashboard/Content)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(245, 246, 250));

        // Título central
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(contentPanel.getBackground());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        JLabel titleLabel = new JLabel("Dashboard Principal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // Ações
        btnCadastrarProduto.addActionListener(e -> new ProductFormScreen(this).setVisible(true));
        btnListarProdutos.addActionListener(e -> new ProductListScreen(this).setVisible(true));
        btnCriarOrcamento.addActionListener(e -> new OrcamentoScreen(this).setVisible(true));
        btnFinalizarOS.addActionListener(e -> new FinalizarOSScreen(this).setVisible(true));
        btnRelatorioVendas.addActionListener(e -> new RelatorioVendasScreen().setVisible(true));

        btnGerarPdf.addActionListener(e -> {
            String idStr = JOptionPane.showInputDialog(this, "Digite o ID do orçamento para gerar o PDF:", "Gerar PDF",
                    JOptionPane.PLAIN_MESSAGE);

            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int idOrcamento = Integer.parseInt(idStr);

                    OrcamentoDAO orcamentoDAO = new OrcamentoDAO();
                    Orcamento orcamento = orcamentoDAO.buscarPorId(idOrcamento);

                    if (orcamento != null) {
                        ItemOrcamentoDAO itemOrcamentoDAO = new ItemOrcamentoDAO();
                        List<ItemOrcamento> itens = itemOrcamentoDAO.buscarPorIdOrcamento(idOrcamento);
                        orcamento.setItens(itens);

                        PdfGenerator.gerarPdfOrcamento(orcamento);

                        JOptionPane.showMessageDialog(this, "PDF do orçamento " + idOrcamento + " gerado com sucesso!",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        JOptionPane.showMessageDialog(this, "Orçamento com ID " + idOrcamento + " não encontrado.",
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Por favor, digite um ID numérico válido.", "Erro de Formato",
                            JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + ex.getMessage(), "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Adicionar painel principal à janela
        add(mainPanel);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(45, 52, 54));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return btn;
    }

    public static void main(String[] args) {
        // Usar o Event Dispatch Thread para garantir a segurança da thread
        SwingUtilities.invokeLater(() -> {
            MainScreen mainScreen = new MainScreen();
            mainScreen.setVisible(true);
        });
    }
}
