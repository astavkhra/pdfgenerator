// Swing/AWT imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font; // AWT Font for GUI components
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

// iText imports - using full path to avoid conflicts
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// Other imports
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PDFGeneratorGUI extends JFrame {
    
    // Components
    private JTextField titleField;
    private JTextArea textArea;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JTextField imagePathField;
    private JList<String> elementsList;
    private DefaultListModel<String> elementsListModel;
    private List<PDFElement> pdfElements;
    
    // Element counter
    private int elementCounter = 0;
    
    public PDFGeneratorGUI() {
        pdfElements = new ArrayList<>();
        setupUI();
    }
    
    private void setupUI() {
        setTitle("PDF Generator");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Create main panels
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();
        
        // Add panels
        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        
        // Create menu bar
        createMenuBar();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create tabbed pane for different element types
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Text Tab
        tabbedPane.addTab("Text", createTextPanel());
        
        // Table Tab
        tabbedPane.addTab("Table", createTablePanel());
        
        // Image Tab
        tabbedPane.addTab("Image", createImagePanel());
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        // Add buttons at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton addButton = new JButton("Add to PDF");
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBackground(new Color(70, 130, 180));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addElementToPDF(tabbedPane.getSelectedIndex()));
        
        JButton clearButton = new JButton("Clear All");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 14));
        clearButton.addActionListener(e -> clearAllElements());
        
        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTextPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
        titlePanel.add(new JLabel("Title (optional):"), BorderLayout.NORTH);
        titleField = new JTextField();
        titleField.setFont(new Font("Arial", Font.PLAIN, 14));
        titlePanel.add(titleField, BorderLayout.CENTER);
        
        // Text area section
        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.add(new JLabel("Text Content:"), BorderLayout.NORTH);
        textArea = new JTextArea(10, 40);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Table controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRowButton = new JButton("Add Row");
        JButton removeRowButton = new JButton("Remove Row");
        JButton addColumnButton = new JButton("Add Column");
        JButton removeColumnButton = new JButton("Remove Column");
        
        controlPanel.add(addRowButton);
        controlPanel.add(removeRowButton);
        controlPanel.add(addColumnButton);
        controlPanel.add(removeColumnButton);
        
        // Create table
        String[] columns = {"Column 1", "Column 2", "Column 3"};
        Object[][] data = {
            {"", "", ""},
            {"", "", ""},
            {"", "", ""}
        };
        
        tableModel = new DefaultTableModel(data, columns);
        dataTable = new JTable(tableModel);
        dataTable.setFont(new Font("Arial", Font.PLAIN, 12));
        dataTable.setRowHeight(25);
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        
        // Button actions
        addRowButton.addActionListener(e -> tableModel.addRow(new Object[tableModel.getColumnCount()]));
        removeRowButton.addActionListener(e -> {
            int row = dataTable.getSelectedRow();
            if (row >= 0) tableModel.removeRow(row);
        });
        addColumnButton.addActionListener(e -> tableModel.addColumn("Column " + (tableModel.getColumnCount() + 1)));
        removeColumnButton.addActionListener(e -> {
            int col = dataTable.getSelectedColumn();
            if (col >= 0 && tableModel.getColumnCount() > 1) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    tableModel.setValueAt(null, i, col);
                }
                // Remove column by creating new model without that column
                String[] newColumns = new String[tableModel.getColumnCount() - 1];
                int newIndex = 0;
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    if (i != col) {
                        newColumns[newIndex++] = tableModel.getColumnName(i);
                    }
                }
                Object[][] newData = new Object[tableModel.getRowCount()][newColumns.length];
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    newIndex = 0;
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        if (j != col) {
                            newData[i][newIndex++] = tableModel.getValueAt(i, j);
                        }
                    }
                }
                tableModel.setDataVector(newData, newColumns);
            }
        });
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
        pathPanel.add(new JLabel("Image Path or URL:"), BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        imagePathField = new JTextField();
        imagePathField.setFont(new Font("Arial", Font.PLAIN, 13));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseForImage());
        
        inputPanel.add(imagePathField, BorderLayout.CENTER);
        inputPanel.add(browseButton, BorderLayout.EAST);
        pathPanel.add(inputPanel, BorderLayout.CENTER);
        
        JLabel infoLabel = new JLabel("<html><i>Supported formats: JPG, PNG, GIF, BMP<br>You can also use a URL (e.g., https://example.com/image.jpg)</i></html>");
        infoLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        pathPanel.add(infoLabel, BorderLayout.SOUTH);
        
        panel.add(pathPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(300, 600));
        
        JLabel titleLabel = new JLabel("PDF Elements");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Elements list
        elementsListModel = new DefaultListModel<>();
        elementsList = new JList<>(elementsListModel);
        elementsList.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane listScrollPane = new JScrollPane(elementsList);
        panel.add(listScrollPane, BorderLayout.CENTER);
        
        // Control buttons
        JPanel controlPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        
        JButton moveUpButton = new JButton("Move Up");
        JButton moveDownButton = new JButton("Move Down");
        JButton removeButton = new JButton("Remove Selected");
        JButton generateButton = new JButton("Generate PDF");
        
        generateButton.setBackground(new Color(60, 179, 113));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFont(new Font("Arial", Font.BOLD, 14));
        generateButton.setFocusPainted(false);
        
        moveUpButton.addActionListener(e -> moveElement(-1));
        moveDownButton.addActionListener(e -> moveElement(1));
        removeButton.addActionListener(e -> removeSelectedElement());
        generateButton.addActionListener(e -> generatePDF());
        
        controlPanel.add(moveUpButton);
        controlPanel.add(moveDownButton);
        controlPanel.add(removeButton);
        controlPanel.add(generateButton);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        newItem.addActionListener(e -> clearAllElements());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void browseForImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return f.isDirectory() || name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                       name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp");
            }
            public String getDescription() {
                return "Image Files (*.jpg, *.png, *.gif, *.bmp)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            imagePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void addElementToPDF(int tabIndex) {
        elementCounter++;
        
        switch (tabIndex) {
            case 0: // Text
                String title = titleField.getText().trim();
                String text = textArea.getText().trim();
                if (!text.isEmpty()) {
                    TextElement textElement = new TextElement(title, text);
                    pdfElements.add(textElement);
                    String display = title.isEmpty() ? 
                        String.format("%d. Text: %s...", elementCounter, text.substring(0, Math.min(30, text.length()))) :
                        String.format("%d. Title: %s", elementCounter, title);
                    elementsListModel.addElement(display);
                    textArea.setText("");
                    titleField.setText("");
                    JOptionPane.showMessageDialog(this, "Text element added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter some text!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                break;
                
            case 1: // Table
                if (tableModel.getRowCount() > 0 && tableModel.getColumnCount() > 0) {
                    TableElement tableElement = new TableElement(tableModel);
                    pdfElements.add(tableElement);
                    elementsListModel.addElement(String.format("%d. Table: %dx%d", 
                        elementCounter, tableModel.getRowCount(), tableModel.getColumnCount()));
                    JOptionPane.showMessageDialog(this, "Table element added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Table is empty!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                break;
                
            case 2: // Image
                String imagePath = imagePathField.getText().trim();
                if (!imagePath.isEmpty()) {
                    ImageElement imageElement = new ImageElement(imagePath);
                    pdfElements.add(imageElement);
                    String fileName = new File(imagePath).getName();
                    elementsListModel.addElement(String.format("%d. Image: %s", elementCounter, fileName));
                    imagePathField.setText("");
                    JOptionPane.showMessageDialog(this, "Image element added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter an image path or URL!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                break;
        }
    }
    
    private void moveElement(int direction) {
        int index = elementsList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Please select an element!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int newIndex = index + direction;
        if (newIndex >= 0 && newIndex < elementsListModel.size()) {
            PDFElement element = pdfElements.remove(index);
            pdfElements.add(newIndex, element);
            
            String item = elementsListModel.remove(index);
            elementsListModel.add(newIndex, item);
            
            elementsList.setSelectedIndex(newIndex);
        }
    }
    
    private void removeSelectedElement() {
        int index = elementsList.getSelectedIndex();
        if (index >= 0) {
            pdfElements.remove(index);
            elementsListModel.remove(index);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an element to remove!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void clearAllElements() {
        pdfElements.clear();
        elementsListModel.clear();
        elementCounter = 0;
        textArea.setText("");
        titleField.setText("");
        imagePathField.setText("");
    }
    
    private void generatePDF() {
        if (pdfElements.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add some elements first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF");
        fileChooser.setSelectedFile(new File("output.pdf"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filename.toLowerCase().endsWith(".pdf")) {
                filename += ".pdf";
            }
            
            try {
                createPDFDocument(filename);
                JOptionPane.showMessageDialog(this, 
                    "PDF generated successfully!\n" + filename, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error generating PDF: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void createPDFDocument(String filename) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        
        for (PDFElement element : pdfElements) {
            element.addToDocument(document);
        }
        
        document.close();
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "PDF Generator with GUI\nVersion 1.0\n\nCreate PDFs with text, tables, and images!",
            "About",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Inner classes for PDF elements
    private abstract class PDFElement {
        abstract void addToDocument(Document document) throws Exception;
    }
    
    private class TextElement extends PDFElement {
        String title;
        String text;
        
        TextElement(String title, String text) {
            this.title = title;
            this.text = text;
        }
        
        void addToDocument(Document document) throws Exception {
            if (!title.isEmpty()) {
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
                Paragraph titlePara = new Paragraph(title, titleFont);
                titlePara.setSpacingAfter(10f);
                document.add(titlePara);
            }
            
            com.itextpdf.text.Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Paragraph textPara = new Paragraph(text, textFont);
            textPara.setAlignment(Element.ALIGN_JUSTIFIED);
            textPara.setSpacingAfter(15f);
            document.add(textPara);
        }
    }
    
    private class TableElement extends PDFElement {
        Object[][] data;
        String[] headers;
        
        TableElement(DefaultTableModel model) {
            int rows = model.getRowCount();
            int cols = model.getColumnCount();
            
            headers = new String[cols];
            for (int i = 0; i < cols; i++) {
                headers[i] = model.getColumnName(i);
            }
            
            data = new Object[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    data[i][j] = model.getValueAt(i, j);
                }
            }
        }
        
        void addToDocument(Document document) throws Exception {
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(15f);
            
            // Add headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setPadding(5f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            
            // Add data
            com.itextpdf.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            for (Object[] row : data) {
                for (Object cell : row) {
                    String cellText = cell == null ? "" : cell.toString();
                    PdfPCell dataCell = new PdfPCell(new Phrase(cellText, dataFont));
                    dataCell.setPadding(5f);
                    table.addCell(dataCell);
                }
            }
            
            document.add(table);
        }
    }
    
    private class ImageElement extends PDFElement {
        String path;
        
        ImageElement(String path) {
            this.path = path;
        }
        
        void addToDocument(Document document) throws Exception {
            Image image;
            if (path.startsWith("http://") || path.startsWith("https://")) {
                image = Image.getInstance(new java.net.URL(path));
            } else {
                image = Image.getInstance(path);
            }
            
            image.scaleToFit(500f, 400f);
            image.setAlignment(Element.ALIGN_CENTER);
            image.setSpacingBefore(10f);
            image.setSpacingAfter(15f);
            document.add(image);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PDFGeneratorGUI());
    }
}