package util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.ColorConstants;

import dao.ClienteDAO;
import dao.ProdutoDAO;
import dao.ServicoDAO;
import model.*;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.List;

public class PdfGenerator {

    private static final DeviceRgb COLOR_PRIMARY = new DeviceRgb(0, 102, 204); // Azul TEC
    private static final DeviceRgb COLOR_SECONDARY = new DeviceRgb(240, 240, 240); // Cinza claro

    private static void adicionarCabecalhoEmpresa(Document document, String tituloDocumento) {
        document.add(new Paragraph("TEC ENERGIA E SOLUÇÕES")
                .setFontSize(20)
                .setBold()
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("Instalação de Câmeras, Alarmes, Interfones, Motores de Portão, Energia Solar e Manutenção Elétrica\n" +
                "Telefone: (11) 99999-9999 | Email: contato@tecenergia.com.br")
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));
        
        document.add(new Paragraph(tituloDocumento)
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    public static void gerarPdfOrcamento(Orcamento orcamento) {
        try {
            String dest = "orcamento_" + orcamento.getId() + ".pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            adicionarCabecalhoEmpresa(document, "ORÇAMENTO #" + orcamento.getId());

            // Dados do Cliente
            Cliente cliente = null;
            try {
                cliente = new ClienteDAO().buscarPorId(orcamento.getClienteId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cliente != null) {
                document.add(new Paragraph("DADOS DO CLIENTE").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11));
                document.add(new Paragraph("Nome: " + cliente.getNome() + 
                        "\nTelefone: " + cliente.getTelefone() + 
                        "\nEmail: " + (cliente.getEmail() != null ? cliente.getEmail() : "Não informado") +
                        "\nEndereço: " + (cliente.getEndereco() != null ? cliente.getEndereco() : "") + 
                        " | Cidade: " + (cliente.getCidade() != null ? cliente.getCidade() : "") +
                        "\nObservações: " + (cliente.getObservacoes() != null ? cliente.getObservacoes() : ""))
                        .setFontSize(10)
                        .setMarginBottom(15));
            } else {
                document.add(new Paragraph("Cliente não encontrado (ID: " + orcamento.getClienteId() + ")").setMarginBottom(15));
            }

            // Itens do Orçamento
            document.add(new Paragraph("ITENS DO ORÇAMENTO").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11));
            
            float[] columnWidths = {250f, 60f, 100f, 100f};
            Table table = new Table(columnWidths);
            
            table.addCell(new Cell().add(new Paragraph("Descrição")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Qtd")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Vlr. Unitário")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Vlr. Total")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());

            ProdutoDAO produtoDAO = new ProdutoDAO();
            ServicoDAO servicoDAO = new ServicoDAO();

            for (ItemOrcamento item : orcamento.getItens()) {
                String desc = item.getDescricao();
                if (desc == null || desc.trim().isEmpty()) {
                    if ("PRODUTO".equals(item.getTipoItem())) {
                        try {
                            Produto p = produtoDAO.buscarPorId(item.getIdProduto());
                            desc = p != null ? p.getNome() : "Produto #" + item.getIdProduto();
                        } catch (Exception e) { desc = "Produto #" + item.getIdProduto(); }
                    } else {
                        try {
                            Servico s = servicoDAO.buscarPorId(item.getIdServico());
                            desc = s != null ? s.getNome() : "Serviço #" + item.getIdServico();
                        } catch (Exception e) { desc = "Serviço #" + item.getIdServico(); }
                    }
                }

                table.addCell(new Cell().add(new Paragraph(desc).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", item.getQuantidade())).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(String.format("R$ %.2f", item.getValorUnitario())).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(String.format("R$ %.2f", item.getValorTotal())).setFontSize(9)));
            }

            document.add(table.setMarginBottom(20));

            document.add(new Paragraph(String.format("VALOR TOTAL GERAL: R$ %.2f", orcamento.getValorTotal()))
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(COLOR_PRIMARY));

            document.add(new Paragraph("Validade do Orçamento: 15 dias.")
                    .setFontSize(8)
                    .setItalic()
                    .setMarginTop(30));

            document.close();
            System.out.println("PDF do orçamento gerado com sucesso!");

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

            adicionarCabecalhoEmpresa(document, "ORDEM DE SERVIÇO #" + os.getId());

            // Dados do Cliente
            Cliente cliente = null;
            try {
                cliente = new ClienteDAO().buscarPorId(os.getClienteId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cliente != null) {
                document.add(new Paragraph("DADOS DO CLIENTE").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11));
                document.add(new Paragraph("Nome: " + cliente.getNome() + 
                        "\nTelefone: " + cliente.getTelefone() + 
                        "\nEndereço: " + (cliente.getEndereco() != null ? cliente.getEndereco() : "") + 
                        " | Cidade: " + (cliente.getCidade() != null ? cliente.getCidade() : ""))
                        .setFontSize(10)
                        .setMarginBottom(15));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            document.add(new Paragraph("DADOS DA OS").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11));
            document.add(new Paragraph("Data de Abertura: " + (os.getDataOrdem() != null ? sdf.format(os.getDataOrdem()) : "N/A") +
                    "\nStatus atual: " + os.getStatus())
                    .setFontSize(10)
                    .setMarginBottom(15));

            // Itens Executados
            document.add(new Paragraph("ITENS E SERVIÇOS EXECUTADOS").setBold().setFontColor(COLOR_PRIMARY).setFontSize(11));
            
            float[] columnWidths = {250f, 60f, 100f, 100f};
            Table table = new Table(columnWidths);
            
            table.addCell(new Cell().add(new Paragraph("Descrição")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Qtd")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Vlr. Unitário")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());
            table.addCell(new Cell().add(new Paragraph("Vlr. Total")).setBackgroundColor(COLOR_PRIMARY).setFontColor(ColorConstants.WHITE).setBold());

            ProdutoDAO produtoDAO = new ProdutoDAO();
            ServicoDAO servicoDAO = new ServicoDAO();

            for (ItemOrdemServico item : os.getItens()) {
                String desc = item.getDescricao();
                if (desc == null || desc.trim().isEmpty()) {
                    if ("PRODUTO".equals(item.getTipoItem())) {
                        try {
                            Produto p = produtoDAO.buscarPorId(item.getIdProduto());
                            desc = p != null ? p.getNome() : "Produto #" + item.getIdProduto();
                        } catch (Exception e) { desc = "Produto #" + item.getIdProduto(); }
                    } else {
                        try {
                            Servico s = servicoDAO.buscarPorId(item.getIdServico());
                            desc = s != null ? s.getNome() : "Serviço #" + item.getIdServico();
                        } catch (Exception e) { desc = "Serviço #" + item.getIdServico(); }
                    }
                }

                table.addCell(new Cell().add(new Paragraph(desc).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", item.getQuantidade())).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(String.format("R$ %.2f", item.getValorUnitario())).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(String.format("R$ %.2f", item.getValorTotal())).setFontSize(9)));
            }

            document.add(table.setMarginBottom(20));

            document.add(new Paragraph(String.format("VALOR TOTAL: R$ %.2f", os.getValorTotal()))
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(COLOR_PRIMARY));

            document.add(new Paragraph("\n\nAssinatura do Técnico: ___________________________\n\nAssinatura do Cliente: ___________________________")
                    .setFontSize(10)
                    .setMarginTop(30));

            document.close();
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
                table.addCell(new Cell().add(new Paragraph(String.format("R$ %.2f", v.getValorTotal())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(v.getFormaPagamento()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.format("R$ %.2f", v.getLucro())).setFontSize(8)));

                totalFaturamento += v.getValorTotal();
                totalLucro += v.getLucro();
            }

            document.add(table.setMarginBottom(20));

            document.add(new Paragraph(String.format("FATURAMENTO TOTAL: R$ %.2f | LUCRO TOTAL: R$ %.2f", totalFaturamento, totalLucro))
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(COLOR_PRIMARY));

            document.close();
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
                table.addCell(new Cell().add(new Paragraph(String.format("R$ %.2f", p.getPrecoCusto())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.format("R$ %.2f", p.getPrecoVenda())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", p.getQuantidade())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(p.getTipoVenda()).setFontSize(8)));
            }

            document.add(table);
            document.close();
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
        } catch (FileNotFoundException e) {
            System.out.println("Erro ao gerar PDF de Relatório de Clientes: " + e.getMessage());
        }
    }
}