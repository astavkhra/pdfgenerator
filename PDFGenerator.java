import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class PDFGenerator {
    
    // Main method to demonstrate PDF generation
    public static void main(String[] args) {
        try {
            generateSamplePDF("output.pdf");
            System.out.println("PDF generated successfully!");
            
            // Generate PDF with image demonstration
            generatePDFWithImage("output_with_image.pdf");
            System.out.println("PDF with image generated successfully!");
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a sample PDF with various elements
     */
    public static void generateSamplePDF(String filename) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        
        document.open();
        
        // Add title
        addTitle(document);
        
        // Add paragraph
        addParagraph(document);
        
        // Add table
        addTable(document);
        
        // Add list
        addList(document);
        
        // Add styled text
        addStyledText(document);
        
        document.close();
    }
    
    /**
     * Adds a formatted title to the document
     */
    private static void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("PDF Generator Demo", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);
    }
    
    /**
     * Adds a simple paragraph
     */
    private static void addParagraph(Document document) throws DocumentException {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Paragraph paragraph = new Paragraph(
            "This is a demonstration of PDF generation using iText library in Java. " +
            "You can create professional PDFs with various elements including text, tables, images, and more.",
            normalFont
        );
        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
        paragraph.setSpacingAfter(15f);
        document.add(paragraph);
    }
    
    /**
     * Adds a table with data
     */
    private static void addTable(Document document) throws DocumentException {
        // Add section header
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLUE);
        Paragraph tableHeader = new Paragraph("Sample Data Table", headerFont);
        tableHeader.setSpacingBefore(10f);
        tableHeader.setSpacingAfter(10f);
        document.add(tableHeader);
        
        // Create table
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15f);
        
        // Add headers
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        PdfPCell headerCell;
        
        String[] headers = {"Name", "Age", "City"};
        for (String header : headers) {
            headerCell = new PdfPCell(new Phrase(header, cellFont));
            headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPadding(8f);
            table.addCell(headerCell);
        }
        
        // Add data rows
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        String[][] data = {
            {"John Doe", "28", "New York"},
            {"Jane Smith", "34", "Los Angeles"},
            {"Bob Johnson", "45", "Chicago"}
        };
        
        for (String[] row : data) {
            for (String cell : row) {
                PdfPCell dataCell = new PdfPCell(new Phrase(cell, dataFont));
                dataCell.setPadding(5f);
                table.addCell(dataCell);
            }
        }
        
        document.add(table);
    }
    
    /**
     * Adds a bulleted list
     */
    private static void addList(Document document) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLUE);
        Paragraph listHeader = new Paragraph("Key Features", headerFont);
        listHeader.setSpacingBefore(10f);
        listHeader.setSpacingAfter(10f);
        document.add(listHeader);
        
        List list = new List(List.UNORDERED);
        Font listFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
        
        list.add(new ListItem("Easy PDF generation", listFont));
        list.add(new ListItem("Support for tables and images", listFont));
        list.add(new ListItem("Custom fonts and styling", listFont));
        list.add(new ListItem("Professional formatting options", listFont));
        
        list.setIndentationLeft(20f);
        list.setSymbolIndent(10f);
        document.add(list);
    }
    
    /**
     * Adds text with various styles
     */
    private static void addStyledText(Document document) throws DocumentException {
        Paragraph styled = new Paragraph();
        styled.setSpacingBefore(15f);
        
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, BaseColor.BLACK);
        Font underlineFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.UNDERLINE, BaseColor.BLACK);
        
        styled.add(new Chunk("Bold text ", boldFont));
        styled.add(new Chunk("Italic text ", italicFont));
        styled.add(new Chunk("Underlined text", underlineFont));
        
        document.add(styled);
        
        // Add footer
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
        Paragraph footer = new Paragraph("\nGenerated by PDFGenerator © 2025", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30f);
        document.add(footer);
    }
    
    /**
     * Example method to add an image (requires valid image path)
     */
    public static void addImage(Document document, String imagePath) throws DocumentException, IOException {
        Image image = Image.getInstance(new URL ("https://www.pexels.com/search/beautiful/"));
        image.scaleToFit(200f, 200f);
        image.setAlignment(Element.ALIGN_CENTER);
        document.add(image);
    }
    
    /**
     * Generates a PDF with image demonstration
     * This method creates a simple colored image programmatically
     */
    public static void generatePDFWithImage(String filename) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        
        document.open();
        
        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("PDF with Image Demo", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);
        
        // Add description
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Paragraph description = new Paragraph(
            "This PDF demonstrates how to add images. Below is a programmatically generated image:",
            normalFont
        );
        description.setSpacingAfter(15f);
        document.add(description);
        
        // Create a simple colored rectangle image programmatically
        PdfContentByte canvas = writer.getDirectContent();
        
        // Save state
        canvas.saveState();
        
        // Draw a colorful rectangle as demonstration
        float x = 200f;
        float y = 600f;
        float width = 200f;
        float height = 150f;
        
        // Draw blue rectangle with border
        canvas.setColorFill(new BaseColor(70, 130, 180)); // Steel blue
        canvas.rectangle(x, y, width, height);
        canvas.fill();
        
        canvas.setColorStroke(BaseColor.BLACK);
        canvas.setLineWidth(2f);
        canvas.rectangle(x, y, width, height);
        canvas.stroke();
        
        // Add text on the image
        canvas.beginText();
        canvas.setFontAndSize(BaseFont.createFont(), 16);
        canvas.setColorFill(BaseColor.WHITE);
        canvas.showTextAligned(Element.ALIGN_CENTER, "Sample Image", x + width/2, y + height/2, 0);
        canvas.endText();
        
        canvas.restoreState();
        
        // Add some space
        document.add(new Paragraph("\n\n\n\n\n\n\n\n"));
        
        // Alternative: Load image from URL (Internet connection required)
        try {
            Paragraph urlImageTitle = new Paragraph("Image from URL:", 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            urlImageTitle.setSpacingBefore(20f);
            urlImageTitle.setSpacingAfter(10f);
            document.add(urlImageTitle);
            
            // Use a sample image from a reliable source
            Image urlImage = Image.getInstance(new URL("https://via.placeholder.com/300x200/4169E1/FFFFFF?text=Sample+Image"));
            urlImage.scaleToFit(300f, 200f);
            urlImage.setAlignment(Element.ALIGN_CENTER);
            urlImage.setSpacingAfter(15f);
            document.add(urlImage);
            
            Paragraph urlNote = new Paragraph(
                "This image was loaded from a URL. You can also load images from local file paths.",
                normalFont
            );
            document.add(urlNote);
        } catch (Exception e) {
            Paragraph errorNote = new Paragraph(
                "Could not load image from URL (Internet connection may be required).",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.RED)
            );
            document.add(errorNote);
        }
        
        // Add footer
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
        Paragraph footer = new Paragraph("\nTo add your own images, use:\nImage.getInstance(\"path/to/your/image.jpg\")", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30f);
        document.add(footer);
        
        document.close();
    }
    
    /**
     * Example: Add image from local file path
     */
    public static void addLocalImage(Document document, String localImagePath) throws DocumentException, IOException {
        try {
            Image image = Image.getInstance(localImagePath);
            // Scale image to fit page width (with margins)
            image.scaleToFit(500f, 400f);
            image.setAlignment(Element.ALIGN_CENTER);
            image.setSpacingBefore(10f);
            image.setSpacingAfter(10f);
            document.add(image);
        } catch (IOException e) {
            System.err.println("Could not load image: " + localImagePath);
            throw e;
        }
    }
    
    /**
     * Example method to generate a simple invoice PDF
     */
    public static void generateInvoice(String filename, String customerName, 
                                      String[][] items, double total) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        
        // Invoice header
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);
        
        // Customer info
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        document.add(new Paragraph("Customer: " + customerName, normalFont));
        document.add(new Paragraph("Date: " + new java.util.Date().toString(), normalFont));
        document.add(new Paragraph(" "));
        
        // Items table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{3, 1, 1, 1});
        
        // Headers
        String[] headers = {"Description", "Quantity", "Price", "Total"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5f);
            table.addCell(cell);
        }
        
        // Items
        for (String[] item : items) {
            for (String value : item) {
                table.addCell(value);
            }
        }
        
        document.add(table);
        
        // Total
        Paragraph totalPara = new Paragraph("\nTotal: $" + String.format("%.2f", total), 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        totalPara.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalPara);
        
        document.close();
    }
}