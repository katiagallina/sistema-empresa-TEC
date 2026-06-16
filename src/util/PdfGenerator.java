package util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import model.Orcamento;
import model.ItemOrcamento;

import java.io.FileNotFoundException;

public class PdfGenerator {

    public static void gerarPdfOrcamento(Orcamento orcamento) {
        try {
            String dest = "orcamento_" + orcamento.getId() + ".pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Orçamento #" + orcamento.getId()));
            document.add(new Paragraph("Cliente: " + orcamento.getClienteNome()));
            document.add(new Paragraph("Data: " + orcamento.getDataOrcamento()));
            document.add(new Paragraph("Valor Total: " + orcamento.getValorTotal()));

            document.add(new Paragraph("Itens do Orçamento:"));
            for (ItemOrcamento item : orcamento.getItens()) {
                document.add(new Paragraph(
                    "Produto ID: " + item.getIdProduto() +
                    ", Quantidade: " + item.getQuantidade() +
                    ", Valor Unitário: " + item.getValorUnitario() +
                    ", Valor Total: " + item.getValorTotal()
                ));
            }

            document.close();
            System.out.println("PDF do orçamento gerado com sucesso!");

        } catch (FileNotFoundException e) {
            System.out.println("Erro ao gerar PDF: " + e.getMessage());
        }
    }
}