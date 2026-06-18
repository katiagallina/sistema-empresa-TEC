package util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.ColorConstants;

import dao.ClienteDAO;
import dao.ProdutoDAO;
import dao.ServicoDAO;
import model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

public class PdfGenerator {

    private static final DeviceRgb COLOR_PRIMARY = new DeviceRgb(0, 102, 204); // Azul TEC
    private static final DeviceRgb COLOR_SECONDARY = new DeviceRgb(240, 240, 240); // Cinza claro

    private static String formatarDinheiro(double valor) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(valor).replace(" ", ""); // Ex: R$935,00
    }

    private static void abrirPdf(String caminho) {
        try {
            File file = new File(caminho);
            if (!file.exists()) {
                System.err.println("Arquivo PDF não encontrado: " + caminho);
                return;
            }
            
            boolean opened = false;
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(file);
                    opened = true;
                }
            }
            
            if (!opened) {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", file.getAbsolutePath()});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", file.getAbsolutePath()});
                } else {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao abrir PDF automaticamente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static File encontrarLogo() {
        File f = new File("logo.png");
        if (f.exists()) return f;
        f = new File("logo.jpg");
        if (f.exists()) return f;
        f = new File("logo.jpeg");
        if (f.exists()) return f;
        return null;
    }

    private static void adicionarCabecalhoEmpresa(Document document, String tituloDocumento) {
        try {
            float[] headerWidths = {150f, 350f};
            Table headerTable = new Table(headerWidths);
            headerTable.setBorder(null);

            // Célula do Logo (Esquerda)
            Cell logoCell = new Cell();
            logoCell.setBorder(null);
            File logoFile = encontrarLogo();
            if (logoFile != null) {
                try {
                    Image img = new Image(ImageDataFactory.create(logoFile.getAbsolutePath()));
                    img.setWidth(130);
                    logoCell.add(img);
                } catch (Exception e) {
                    logoCell.add(new Paragraph("[LOGO]").setBold().setFontSize(14));
                }
            } else {
                logoCell.add(new Paragraph("⚡\nTEC ENERGIA")
                        .setFontSize(14)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(5));
            }
            headerTable.addCell(logoCell);

            // Célula de Identificação (Direita)
            Cell textCell = new Cell();
            textCell.setBorder(null);
            textCell.add(new Paragraph("TEC ENERGIA E SOLUÇÕES")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            textCell.add(new Paragraph("ENTREGANDO SERIEDADE E QUALIDADE")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            textCell.add(new Paragraph("CNPJ 59.241.256.0001-33")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            headerTable.addCell(textCell);

            document.add(headerTable.setMarginBottom(10));

            // Endereço e Contatos centralizados
            document.add(new Paragraph("Endereço: Rua Batista Guidi, 795 Bairro Santa Catarina\nGetúlio Vargas - RS")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            document.add(new Paragraph("CONTATOS: TIAGO 991838023   EMERSON 991825194")
                    .setFontSize(11)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            // Título do Documento
            document.add(new Paragraph(tituloDocumento)
                    .setFontSize(13)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15)
                    .setFontColor(COLOR_PRIMARY));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void adicionarCabecalhoInstitucional(Document document, Cliente cliente) {
        try {
            float[] headerWidths = {150f, 350f};
            Table headerTable = new Table(headerWidths);
            headerTable.setBorder(null);

            // Célula do Logo (Esquerda)
            Cell logoCell = new Cell();
            logoCell.setBorder(null);
            File logoFile = encontrarLogo();
            if (logoFile != null) {
                try {
                    Image img = new Image(ImageDataFactory.create(logoFile.getAbsolutePath()));
                    img.setWidth(130);
                    logoCell.add(img);
                } catch (Exception e) {
                    logoCell.add(new Paragraph("[LOGO]").setBold().setFontSize(14));
                }
            } else {
                logoCell.add(new Paragraph("⚡\nTEC ENERGIA")
                        .setFontSize(14)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(5));
            }
            headerTable.addCell(logoCell);

            // Célula de Identificação (Direita)
            Cell textCell = new Cell();
            textCell.setBorder(null);
            textCell.add(new Paragraph("TEC ENERGIA E SOLUÇÕES")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            textCell.add(new Paragraph("ENTREGANDO SERIEDADE E QUALIDADE")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            textCell.add(new Paragraph("CNPJ 59.241.256.0001-33")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            headerTable.addCell(textCell);

            document.add(headerTable.setMarginBottom(10));

            // Endereço e Contatos centralizados
            document.add(new Paragraph("Endereço: Rua Batista Guidi, 795 Bairro Santa Catarina\nGetúlio Vargas - RS")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            document.add(new Paragraph("CONTATOS: TIAGO 991838023   EMERSON 991825194")
                    .setFontSize(11)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            // Nome do cliente
            String nomeCliente = (cliente != null) ? cliente.getNome().toUpperCase() : "N/A";
            document.add(new Paragraph("NOME: " + nomeCliente)
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(15));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gerarPdfOrcamento(Orcamento orcamento) {
        try {
            String dest = "orcamento_" + orcamento.getId() + ".pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Cliente cliente = null;
            try {
                cliente = new ClienteDAO().buscarPorId(orcamento.getClienteId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Adiciona cabeçalho institucional igual a imagem do Excel
            adicionarCabecalhoInstitucional(document, cliente);

            // Tabela de Itens do Orçamento
            float[] columnWidths = {80f, 260f, 80f, 80f};
            Table table = new Table(columnWidths);
            
            table.addCell(new Cell().add(new Paragraph("QUANTIDADE").setBold().setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph("PRODUTO").setBold().setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph("VALOR UNIT.").setBold().setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph("VALOR TOTAL").setBold().setFontSize(9).setTextAlignment(TextAlignment.CENTER)));

            ProdutoDAO produtoDAO = new ProdutoDAO();
            double valorMaoDeObra = 0.0;

            for (ItemOrcamento item : orcamento.getItens()) {
                if ("SERVICO".equals(item.getTipoItem())) {
                    valorMaoDeObra += item.getValorTotal();
                } else {
                    Produto p = null;
                    try {
                        p = produtoDAO.buscarPorId(item.getIdProduto());
                    } catch (Exception e) {}

                    boolean isMetro = (p != null && "METRO".equalsIgnoreCase(p.getTipoVenda()));
                    String qtdStr = String.format("%.0f", item.getQuantidade()) + (isMetro ? "m" : "");
                    String valUnitStr = formatarDinheiro(item.getValorUnitario()) + (isMetro ? "m" : "");
                    String valTotalStr = formatarDinheiro(item.getValorTotal());
                    String nomeProd = (p != null) ? p.getNome().toUpperCase() : item.getDescricao().toUpperCase();

                    table.addCell(new Cell().add(new Paragraph(qtdStr).setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(nomeProd).setFontSize(9)));
                    table.addCell(new Cell().add(new Paragraph(valUnitStr).setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(valTotalStr).setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
                }
            }

            // Adicionar linha de Mão de Obra se houver
            if (valorMaoDeObra > 0) {
                table.addCell(new Cell().add(new Paragraph("").setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph("MÃO DE OBRA").setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph("").setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(formatarDinheiro(valorMaoDeObra)).setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
            }

            // Linha de Total Geral
            table.addCell(new Cell().add(new Paragraph("").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph("TOTAL").setBold().setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph("").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(formatarDinheiro(orcamento.getValorTotal())).setBold().setFontSize(9).setTextAlignment(TextAlignment.CENTER)));

            document.add(table.setMarginBottom(20));

            document.add(new Paragraph("Validade do Orçamento: 15 dias.")
                    .setFontSize(8)
                    .setItalic()
                    .setMarginTop(30));

            document.close();
            System.out.println("PDF do orçamento gerado com sucesso!");

            // Abrir PDF automaticamente
            abrirPdf(dest);

        } catch (FileNotFoundException e) {
            System.out.println("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    public static void gerarPdfOrdemServico(OrdemServico os) {
        try {
            String dest = "ordem_servico_" + os.getId() + ".pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Cliente cliente = null;
            try {
                cliente = new ClienteDAO().buscarPorId(os.getClienteId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Cabeçalho institucional idêntico
            adicionarCabecalhoInstitucional(document, cliente);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            document.add(new Paragraph("DADOS DA ORDEM DE SERVIÇO").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11));
            document.add(new Paragraph("Data de Abertura: " + (os.getDataOrdem() != null ? sdf.format(os.getDataOrdem()) : "N/A") +
                    " | Status atual: " + os.getStatus())
                    .setFontSize(10)
                    .setMarginBottom(15));

            // Tabela de itens da OS
            float[] columnWidths = {80f, 260f, 80f, 80f};
            Table table = new Table(columnWidths);
            
            table.addCell(new Cell().add(new Paragraph("QUANTIDADE").setBold().setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph("PRODUTO / SERVIÇO").setBold().setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph("VALOR UNIT.").setBold().setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph("VALOR TOTAL").setBold().setFontSize(9).setTextAlignment(TextAlignment.CENTER)));

            ProdutoDAO produtoDAO = new ProdutoDAO();
            double valorMaoDeObra = 0.0;

            for (ItemOrdemServico item : os.getItens()) {
                if ("SERVICO".equals(item.getTipoItem())) {
                    valorMaoDeObra += item.getValorTotal();
                } else {
                    Produto p = null;
                    try {
                        p = produtoDAO.buscarPorId(item.getIdProduto());
                    } catch (Exception e) {}

                    boolean isMetro = (p != null && "METRO".equalsIgnoreCase(p.getTipoVenda()));
                    String qtdStr = String.format("%.0f", item.getQuantidade()) + (isMetro ? "m" : "");
                    String valUnitStr = formatarDinheiro(item.getValorUnitario()) + (isMetro ? "m" : "");
                    String valTotalStr = formatarDinheiro(item.getValorTotal());
                    String nomeProd = (p != null) ? p.getNome().toUpperCase() : item.getDescricao().toUpperCase();

                    table.addCell(new Cell().add(new Paragraph(qtdStr).setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(nomeProd).setFontSize(9)));
                    table.addCell(new Cell().add(new Paragraph(valUnitStr).setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(valTotalStr).setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
                }
            }

            // Adicionar linha de Mão de Obra se houver
            if (valorMaoDeObra > 0) {
                table.addCell(new Cell().add(new Paragraph("").setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph("MÃO DE OBRA").setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph("").setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(formatarDinheiro(valorMaoDeObra)).setFontSize(9).setTextAlignment(TextAlignment.CENTER)));
            }

            // Linha de Total Geral
            table.addCell(new Cell().add(new Paragraph("").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph("TOTAL").setBold().setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph("").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(formatarDinheiro(os.getValorTotal())).setBold().setFontSize(9).setTextAlignment(TextAlignment.CENTER)));

            document.add(table.setMarginBottom(20));

            document.add(new Paragraph("\n\nAssinatura do Técnico: ___________________________\n\nAssinatura do Cliente: ___________________________")
                    .setFontSize(10)
                    .setMarginTop(30));

            document.close();

            // Abrir PDF automaticamente
            abrirPdf(dest);

        } catch (FileNotFoundException e) {
            System.out.println("Erro ao gerar PDF OS: " + e.getMessage());
        }
    }

    public static void gerarPdfRelatorioVendas(List<Venda> vendas, java.util.Date inicio, java.util.Date fim) {
        try {
            String dest = "relatorio_vendas.pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy");
            adicionarCabecalhoEmpresa(document, "RELATÓRIO DE VENDAS E FATURAMENTO\nPeríodo: " + dateSdf.format(inicio) + " a " + dateSdf.format(fim));

            float[] columnWidths = {80f, 180f, 50f, 70f, 70f, 75f};
            Table table = new Table(columnWidths);

            table.addCell(new Cell().add(new Paragraph("Data")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Descrição")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Qtd")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Faturamento")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Forma Pgto")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Lucro")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());

            double totalFaturamento = 0.0;
            double totalLucro = 0.0;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (Venda v : vendas) {
                table.addCell(new Cell().add(new Paragraph(v.getDataVenda() != null ? sdf.format(v.getDataVenda()) : "").setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(v.getDescricao()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", v.getQuantidade())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(formatarDinheiro(v.getValorTotal())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(v.getFormaPagamento()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(formatarDinheiro(v.getLucro())).setFontSize(8)));

                totalFaturamento += v.getValorTotal();
                totalLucro += v.getLucro();
            }

            document.add(table.setMarginBottom(20));

            document.add(new Paragraph(String.format("FATURAMENTO TOTAL: %s | LUCRO TOTAL: %s", formatarDinheiro(totalFaturamento), formatarDinheiro(totalLucro)))
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(COLOR_PRIMARY));

            document.close();

            // Abrir PDF automaticamente
            abrirPdf(dest);

        } catch (FileNotFoundException e) {
            System.out.println("Erro ao gerar PDF de Relatório de Vendas: " + e.getMessage());
        }
    }

    public static void gerarPdfRelatorioProdutos(List<Produto> produtos) {
        try {
            String dest = "relatorio_produtos.pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            adicionarCabecalhoEmpresa(document, "RELATÓRIO GERAL DE PRODUTOS E ESTOQUE");

            float[] columnWidths = {40f, 200f, 90f, 90f, 50f, 50f};
            Table table = new Table(columnWidths);

            table.addCell(new Cell().add(new Paragraph("ID")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Nome")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Preço Custo")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Preço Venda")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Qtd")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Tipo")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());

            for (Produto p : produtos) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getId())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(p.getNome()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(formatarDinheiro(p.getPrecoCusto())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(formatarDinheiro(p.getPrecoVenda())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", p.getQuantidade())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(p.getTipoVenda()).setFontSize(8)));
            }

            document.add(table);
            document.close();

            // Abrir PDF automaticamente
            abrirPdf(dest);

        } catch (FileNotFoundException e) {
            System.out.println("Erro ao gerar PDF de Relatório de Produtos: " + e.getMessage());
        }
    }

    public static void gerarPdfRelatorioClientes(List<Cliente> clientes) {
        try {
            String dest = "relatorio_clientes.pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            adicionarCabecalhoEmpresa(document, "RELATÓRIO GERAL DE CLIENTES CADASTRADOS");

            float[] columnWidths = {30f, 130f, 80f, 100f, 100f, 80f};
            Table table = new Table(columnWidths);

            table.addCell(new Cell().add(new Paragraph("ID")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Nome")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Telefone")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Email")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Endereço")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Cidade")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());

            for (Cliente c : clientes) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(c.getId())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(c.getNome()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(c.getTelefone()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(c.getEmail() != null ? c.getEmail() : "").setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(c.getEndereco() != null ? c.getEndereco() : "").setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(c.getCidade() != null ? c.getCidade() : "").setFontSize(8)));
            }

            document.add(table);
            document.close();

            // Abrir PDF automaticamente
            abrirPdf(dest);

        } catch (FileNotFoundException e) {
            System.out.println("Erro ao gerar PDF de Relatório de Clientes: " + e.getMessage());
        }
    }

    public static void gerarPdfRelatorioServicosRealizados(
            List<ServicoRealizado> servicos, 
            List<Despesa> despesas,
            double entDinheiro, double entPix, double entCheque, double entBoleto, double entTotal,
            double saiDinheiro, double saiPix, double saiCheque, double saiBoleto, double saiTotal,
            java.util.Date inicio, java.util.Date fim) {
        try {
            String dest = "relatorio_servicos_realizados.pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy");
            adicionarCabecalhoEmpresa(document, "RELATÓRIO DE FLUXO DE CAIXA LÍQUIDO E LANÇAMENTOS\nPeríodo: " + dateSdf.format(inicio) + " a " + dateSdf.format(fim));

            // Tabela de Serviços (Entradas)
            document.add(new Paragraph("ENTRADAS (SERVIÇOS REALIZADOS REGISTRADOS)").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11).setMarginBottom(5));
            float[] columnWidths = {70f, 120f, 150f, 60f, 80f, 40f, 60f};
            Table table = new Table(columnWidths);

            table.addCell(new Cell().add(new Paragraph("Data")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Cliente")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Serviço Renderizado")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Valor Total")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Forma Pgto")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Parc.")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Vlr Parcela")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            ClienteDAO clienteDAO = new ClienteDAO();

            for (ServicoRealizado sr : servicos) {
                String clienteNome = "";
                try {
                    Cliente c = clienteDAO.buscarPorId(sr.getClienteId());
                    clienteNome = c != null ? c.getNome() : "ID: " + sr.getClienteId();
                } catch (Exception ex) {
                    clienteNome = "ID: " + sr.getClienteId();
                }

                table.addCell(new Cell().add(new Paragraph(sr.getDataServico() != null ? sdf.format(sr.getDataServico()) : "").setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(clienteNome).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(sr.getDescricaoServico()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(formatarDinheiro(sr.getValor())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(sr.getFormaPagamento()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(sr.getNumParcelas())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(formatarDinheiro(sr.getValorParcela())).setFontSize(8)));
            }

            document.add(table.setMarginBottom(15));

            // Tabela de Despesas (Saídas)
            document.add(new Paragraph("SAÍDAS (DESPESAS / PAGAMENTOS REGISTRADOS)").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11).setMarginTop(10).setMarginBottom(5));
            float[] depWidths = {100f, 220f, 80f, 100f};
            Table depTable = new Table(depWidths);
            depTable.addCell(new Cell().add(new Paragraph("Data")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            depTable.addCell(new Cell().add(new Paragraph("Descrição")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            depTable.addCell(new Cell().add(new Paragraph("Valor Pago")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            depTable.addCell(new Cell().add(new Paragraph("Forma Pgto")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());

            for (Despesa d : despesas) {
                depTable.addCell(new Cell().add(new Paragraph(d.getDataDespesa() != null ? sdf.format(d.getDataDespesa()) : "").setFontSize(8)));
                depTable.addCell(new Cell().add(new Paragraph(d.getDescricao()).setFontSize(8)));
                depTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(d.getValor())).setFontSize(8)));
                depTable.addCell(new Cell().add(new Paragraph(d.getFormaPagamento()).setFontSize(8)));
            }
            document.add(depTable.setMarginBottom(15));

            // Resumo Fluxo de Caixa Líquido
            document.add(new Paragraph("RESUMO DO FLUXO DE CAIXA LÍQUIDO").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11).setMarginBottom(5));
            
            float[] flowWidths = {150f, 110f, 110f, 110f};
            Table flowTable = new Table(flowWidths);
            flowTable.addCell(new Cell().add(new Paragraph("Forma de Pagamento")).setBackgroundColor(COLOR_SECONDARY).setBold());
            flowTable.addCell(new Cell().add(new Paragraph("Entradas (R$)")).setBackgroundColor(COLOR_SECONDARY).setBold());
            flowTable.addCell(new Cell().add(new Paragraph("Saídas (R$)")).setBackgroundColor(COLOR_SECONDARY).setBold());
            flowTable.addCell(new Cell().add(new Paragraph("Saldo Líquido (R$)")).setBackgroundColor(COLOR_SECONDARY).setBold());

            // DINHEIRO
            flowTable.addCell(new Cell().add(new Paragraph("DINHEIRO")));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entDinheiro))));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(saiDinheiro))));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entDinheiro - saiDinheiro))));

            // PIX
            flowTable.addCell(new Cell().add(new Paragraph("PIX")));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entPix))));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(saiPix))));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entPix - saiPix))));

            // CHEQUE
            flowTable.addCell(new Cell().add(new Paragraph("CHEQUE")));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entCheque))));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(saiCheque))));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entCheque - saiCheque))));

            // BOLETO
            flowTable.addCell(new Cell().add(new Paragraph("BOLETO")));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entBoleto))));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(saiBoleto))));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entBoleto - saiBoleto))));

            // TOTAL LÍQUIDO / REAL
            flowTable.addCell(new Cell().add(new Paragraph("TOTAL GERAL / LUCRO LÍQUIDO").setBold().setFontColor(COLOR_PRIMARY)));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entTotal)).setBold().setFontColor(COLOR_PRIMARY)));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(saiTotal)).setBold().setFontColor(COLOR_PRIMARY)));
            flowTable.addCell(new Cell().add(new Paragraph(formatarDinheiro(entTotal - saiTotal)).setBold().setFontColor(COLOR_PRIMARY)));

            document.add(flowTable);
            document.close();

            // Abrir PDF automaticamente
            abrirPdf(dest);

        } catch (FileNotFoundException e) {
            System.out.println("Erro ao gerar PDF de Relatório de Serviços Realizados: " + e.getMessage());
        }
    }
}