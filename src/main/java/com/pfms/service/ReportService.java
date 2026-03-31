package com.pfms.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.pfms.dto.TransactionDto;
import com.pfms.entity.Transaction.TransactionType;
import com.pfms.entity.User;
import com.pfms.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * Generates PDF monthly reports using iTextPDF.
 */
@Service
public class ReportService {

    @Autowired private AuthService authService;
    @Autowired private TransactionService transactionService;
    @Autowired private TransactionRepository transactionRepository;

    /**
     * Generate a PDF report for the given month/year.
     */
    public byte[] generateMonthlyPdf(int month, int year) throws Exception {
        User user = authService.getCurrentUser();

        BigDecimal income = transactionRepository
                .sumAmountByUserIdAndTypeAndMonthAndYear(user.getId(), TransactionType.INCOME, month, year);
        BigDecimal expense = transactionRepository
                .sumAmountByUserIdAndTypeAndMonthAndYear(user.getId(), TransactionType.EXPENSE, month, year);

        List<TransactionDto.CategorySummary> categories = transactionService.getCategorySummary(month, year);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<TransactionDto.Response> txns = transactionService.getFilteredTransactions(
                buildFilter(start, end));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(44, 62, 80));
        Font headFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(52, 73, 94));
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
        Font greenFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(39, 174, 96));
        Font redFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(231, 76, 60));

        // Title
        Paragraph title = new Paragraph(
                "Personal Finance Report – " + Month.of(month).name() + " " + year, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6);
        doc.add(title);

        Paragraph sub = new Paragraph("Generated for: " + user.getFullName() + " (" + user.getEmail() + ")", normalFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(20);
        doc.add(sub);

        // Summary table
        doc.add(new Paragraph("Monthly Summary", headFont));
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(60);
        summary.setHorizontalAlignment(Element.ALIGN_LEFT);
        summary.setSpacingBefore(8);
        summary.setSpacingAfter(20);
        addSummaryRow(summary, "Total Income", "+" + user.getCurrency() + " " + income, greenFont);
        addSummaryRow(summary, "Total Expenses", "-" + user.getCurrency() + " " + expense, redFont);
        BigDecimal net = income.subtract(expense);
        addSummaryRow(summary, "Net Balance",
                (net.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + user.getCurrency() + " " + net,
                net.compareTo(BigDecimal.ZERO) >= 0 ? greenFont : redFont);
        doc.add(summary);

        // Category breakdown
        if (!categories.isEmpty()) {
            doc.add(new Paragraph("Category Breakdown", headFont));
            PdfPTable catTable = new PdfPTable(3);
            catTable.setWidthPercentage(80);
            catTable.setSpacingBefore(8);
            catTable.setSpacingAfter(20);
            addTableHeader(catTable, "Category", "Amount (" + user.getCurrency() + ")", "% of Expenses");
            for (TransactionDto.CategorySummary c : categories) {
                catTable.addCell(new PdfPCell(new Phrase(c.getCategory().name(), normalFont)));
                catTable.addCell(new PdfPCell(new Phrase(c.getTotalAmount().toPlainString(), normalFont)));
                catTable.addCell(new PdfPCell(new Phrase(String.format("%.1f%%", c.getPercentage()), normalFont)));
            }
            doc.add(catTable);
        }

        // Transaction list
        if (!txns.isEmpty()) {
            doc.add(new Paragraph("Transaction Detail", headFont));
            PdfPTable txTable = new PdfPTable(5);
            txTable.setWidthPercentage(100);
            txTable.setSpacingBefore(8);
            txTable.setWidths(new float[]{2.5f, 1.2f, 1.5f, 1.5f, 1f});
            addTableHeader(txTable, "Title", "Type", "Category", "Amount", "Date");
            for (TransactionDto.Response t : txns) {
                txTable.addCell(new PdfPCell(new Phrase(t.getTitle(), normalFont)));
                txTable.addCell(new PdfPCell(new Phrase(t.getType().name(), normalFont)));
                txTable.addCell(new PdfPCell(new Phrase(t.getCategory().name(), normalFont)));
                Font amtFont = t.getType() == TransactionType.INCOME ? greenFont : redFont;
                txTable.addCell(new PdfPCell(new Phrase(t.getAmount().toPlainString(), amtFont)));
                txTable.addCell(new PdfPCell(new Phrase(t.getTransactionDate().toString(), normalFont)));
            }
            doc.add(txTable);
        }

        doc.close();
        return baos.toByteArray();
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font valueFont) {
        Font lf = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPadding(5);
        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setPadding(5);
        table.addCell(lc);
        table.addCell(vc);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font hf = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hf));
            cell.setBackgroundColor(new BaseColor(44, 62, 80));
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private TransactionDto.FilterRequest buildFilter(LocalDate start, LocalDate end) {
        TransactionDto.FilterRequest f = new TransactionDto.FilterRequest();
        f.setStartDate(start);
        f.setEndDate(end);
        return f;
    }
}
