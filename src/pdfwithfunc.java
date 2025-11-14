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

// Apache POI imports for Word and Excel
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

// Other imports
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;

public class pdfwithfunc extends JFrame {
    
    // Components
    private JTextField titleField;
    private JTextArea textArea;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JTextField imagePathField;
    private JTextField fileConverterPathField;
    private JList<String> elementsList;
    private DefaultListModel<String> elementsListModel;
    private List<PDFElement> pdfElements;
    
    // Element counter
    private int elementCounter = 0;
    
    public pdfwithfunc() {
        pdfElements = new ArrayList<>();
        setupUI();
    }
    
    private void setupUI() {
        setTitle("PDF Generator & Converter");
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
        
        // File Converter Tab
        tabbedPane.addTab("Convert File", createFileConverterPanel());
        
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
    
    private JPanel createFileConverterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
        pathPanel.add(new JLabel("Select File to Convert:"), BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        fileConverterPathField = new JTextField();
        fileConverterPathField.setFont(new Font("Arial", Font.PLAIN, 13));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseForFileToConvert());
        
        inputPanel.add(fileConverterPathField, BorderLayout.CENTER);
        inputPanel.add(browseButton, BorderLayout.EAST);
        pathPanel.add(inputPanel, BorderLayout.CENTER);
        
        JLabel infoLabel = new JLabel("<html><i><b>Supported file types:</b><br>" +
            "• Word Documents (.docx, .doc)<br>" +
            "• Excel Spreadsheets (.xlsx, .xls)<br>" +
            "• PowerPoint Presentations (.pptx)<br>" +
            "• Text Files (.txt)<br><br>" +
            "The file content will be extracted and added to your PDF.</i></html>");
        infoLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        pathPanel.add(infoLabel, BorderLayout.SOUTH);
        
        panel.add(pathPanel, BorderLayout.NORTH);
        
        // Add convert button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton convertButton = new JButton("Convert & Add to PDF");
        convertButton.setFont(new Font("Arial", Font.BOLD, 14));
        convertButton.setBackground(new Color(220, 20, 60));
        convertButton.setForeground(Color.WHITE);
        convertButton.setFocusPainted(false);
        convertButton.addActionListener(e -> convertFileAndAdd());
        buttonPanel.add(convertButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
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
        JMenuItem directConvertItem = new JMenuItem("Direct Convert to PDF");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        newItem.addActionListener(e -> clearAllElements());
        directConvertItem.addActionListener(e -> directConvertToPDF());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newItem);
        fileMenu.add(directConvertItem);
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
    
    private void browseForFileToConvert() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return f.isDirectory() || name.endsWith(".docx") || name.endsWith(".doc") ||
                       name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".pptx") ||
                       name.endsWith(".txt");
            }
            public String getDescription() {
                return "Office Files (*.docx, *.doc, *.xlsx, *.xls, *.pptx, *.txt)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            fileConverterPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void convertFileAndAdd() {
        String filePath = fileConverterPathField.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a file to convert!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "File does not exist!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            elementCounter++;
            ConvertedFileElement element = new ConvertedFileElement(filePath);
            pdfElements.add(element);
            elementsListModel.addElement(String.format("%d. Converted: %s", elementCounter, file.getName()));
            fileConverterPathField.setText("");
            JOptionPane.showMessageDialog(this, "File converted and added!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error converting file: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void directConvertToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Convert to PDF");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return f.isDirectory() || name.endsWith(".docx") || name.endsWith(".doc") ||
                       name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".pptx") ||
                       name.endsWith(".txt");
            }
            public String getDescription() {
                return "Office Files (*.docx, *.doc, *.xlsx, *.xls, *.pptx, *.txt)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File inputFile = fileChooser.getSelectedFile();
            
            // Ask for output location
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setDialogTitle("Save PDF As");
            String suggestedName = inputFile.getName().replaceFirst("[.][^.]+$", "") + ".pdf";
            saveChooser.setSelectedFile(new File(suggestedName));
            
            int saveResult = saveChooser.showSaveDialog(this);
            if (saveResult == JFileChooser.APPROVE_OPTION) {
                String outputPath = saveChooser.getSelectedFile().getAbsolutePath();
                if (!outputPath.toLowerCase().endsWith(".pdf")) {
                    outputPath += ".pdf";
                }
                
                try {
                    convertFileToPDF(inputFile.getAbsolutePath(), outputPath);
                    JOptionPane.showMessageDialog(this, 
                        "File converted successfully!\n" + outputPath, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error converting file: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void convertFileToPDF(String inputPath, String outputPath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();
        
        ConvertedFileElement element = new ConvertedFileElement(inputPath);
        element.addToDocument(document);
        
        document.close();
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
                
            case 3: // File Converter
                elementCounter--; // Don't increment for this tab
                JOptionPane.showMessageDialog(this, 
                    "Please use the 'Convert & Add to PDF' button in the Convert File tab!", 
                    "Info", JOptionPane.INFORMATION_MESSAGE);
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
        fileConverterPathField.setText("");
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
            "PDF Generator & Converter with GUI\nVersion 2.0\n\n" +
            "Features:\n" +
            "• Create PDFs with text, tables, and images\n" +
            "• Convert Word, Excel, PowerPoint, and Text files to PDF\n" +
            "• Combine multiple elements into one PDF",
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
    
    private class ConvertedFileElement extends PDFElement {
        String filePath;
        
        ConvertedFileElement(String filePath) {
            this.filePath = filePath;
        }
        
        void addToDocument(Document document) throws Exception {
            String fileName = filePath.toLowerCase();
            
            if (fileName.endsWith(".txt")) {
                convertTxtToPDF(document);
            } else if (fileName.endsWith(".docx")) {
                convertDocxToPDF(document);
            } else if (fileName.endsWith(".doc")) {
                convertDocToPDF(document);
            } else if (fileName.endsWith(".xlsx")) {
                convertXlsxToPDF(document);
            } else if (fileName.endsWith(".xls")) {
                convertXlsToPDF(document);
            } else if (fileName.endsWith(".pptx")) {
                convertPptxToPDF(document);
            } else {
                throw new Exception("Unsupported file type");
            }
        }
        
        private void convertTxtToPDF(Document document) throws Exception {
            // Read text file
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            // Add to PDF
            com.itextpdf.text.Font font = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);
            Paragraph para = new Paragraph(content.toString(), font);
            para.setSpacingAfter(15f);
            document.add(para);
        }
        
        private void convertDocxToPDF(Document document) throws Exception {
            FileInputStream fis = new FileInputStream(filePath);
            XWPFDocument docx = new XWPFDocument(fis);
            
            // Add title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Document: " + new File(filePath).getName(), titleFont);
            title.setSpacingAfter(10f);
            document.add(title);
            
            // Extract paragraphs
            List<XWPFParagraph> paragraphs = docx.getParagraphs();
            com.itextpdf.text.Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
            
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText().trim();
                if (!text.isEmpty()) {
                    Paragraph pdfPara = new Paragraph(text, textFont);
                    pdfPara.setSpacingAfter(8f);
                    document.add(pdfPara);
                }
            }
            
            // Extract tables
            List<XWPFTable> tables = docx.getTables();
            for (XWPFTable table : tables) {
                convertWordTableToPDF(document, table);
            }
            
            docx.close();
            fis.close();
        }
        
        private void convertWordTableToPDF(Document document, XWPFTable wordTable) throws Exception {
            List<XWPFTableRow> rows = wordTable.getRows();
            if (rows.isEmpty()) return;
            
            int numCols = rows.get(0).getTableCells().size();
            PdfPTable pdfTable = new PdfPTable(numCols);
            pdfTable.setWidthPercentage(100);
            pdfTable.setSpacingBefore(10f);
            pdfTable.setSpacingAfter(10f);
            
            com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
            
            for (XWPFTableRow row : rows) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    String cellText = cell.getText();
                    PdfPCell pdfCell = new PdfPCell(new Phrase(cellText, cellFont));
                    pdfCell.setPadding(4f);
                    pdfTable.addCell(pdfCell);
                }
            }
            
            document.add(pdfTable);
        }
        
        private void convertDocToPDF(Document document) throws Exception {
            // Note: .doc support is limited - newer .docx format is recommended
            com.itextpdf.text.Font font = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
            Paragraph para = new Paragraph(
                "Legacy .doc format has limited support. Please convert to .docx for better results.\n" +
                "File: " + new File(filePath).getName(), 
                font
            );
            para.setSpacingAfter(15f);
            document.add(para);
        }
        
        private void convertXlsxToPDF(Document document) throws Exception {
            FileInputStream fis = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            
            // Add title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Spreadsheet: " + new File(filePath).getName(), titleFont);
            title.setSpacingAfter(10f);
            document.add(title);
            
            // Process each sheet
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                
                // Add sheet name
                com.itextpdf.text.Font sheetFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLUE);
                Paragraph sheetTitle = new Paragraph("Sheet: " + sheet.getSheetName(), sheetFont);
                sheetTitle.setSpacingBefore(10f);
                sheetTitle.setSpacingAfter(8f);
                document.add(sheetTitle);
                
                // Find max columns
                int maxCols = 0;
                for (Row row : sheet) {
                    if (row.getLastCellNum() > maxCols) {
                        maxCols = row.getLastCellNum();
                    }
                }
                
                if (maxCols == 0) continue;
                
                // Create table
                PdfPTable table = new PdfPTable(maxCols);
                table.setWidthPercentage(100);
                table.setSpacingAfter(10f);
                
                com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
                
                // Add rows
                int rowCount = 0;
                for (Row row : sheet) {
                    if (rowCount > 100) break; // Limit rows to prevent huge PDFs
                    
                    for (int j = 0; j < maxCols; j++) {
                        Cell cell = row.getCell(j);
                        String cellValue = "";
                        
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    cellValue = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                        cellValue = cell.getDateCellValue().toString();
                                    } else {
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    cellValue = String.valueOf(cell.getBooleanCellValue());
                                    break;
                                case FORMULA:
                                    cellValue = cell.getCellFormula();
                                    break;
                                default:
                                    cellValue = "";
                            }
                        }
                        
                        PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue, cellFont));
                        pdfCell.setPadding(3f);
                        table.addCell(pdfCell);
                    }
                    rowCount++;
                }
                
                document.add(table);
            }
            
            workbook.close();
            fis.close();
        }
        
        private void convertXlsToPDF(Document document) throws Exception {
            FileInputStream fis = new FileInputStream(filePath);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            
            // Add title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Spreadsheet: " + new File(filePath).getName(), titleFont);
            title.setSpacingAfter(10f);
            document.add(title);
            
            // Process each sheet (similar to xlsx)
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                
                com.itextpdf.text.Font sheetFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLUE);
                Paragraph sheetTitle = new Paragraph("Sheet: " + sheet.getSheetName(), sheetFont);
                sheetTitle.setSpacingBefore(10f);
                sheetTitle.setSpacingAfter(8f);
                document.add(sheetTitle);
                
                int maxCols = 0;
                for (Row row : sheet) {
                    if (row.getLastCellNum() > maxCols) {
                        maxCols = row.getLastCellNum();
                    }
                }
                
                if (maxCols == 0) continue;
                
                PdfPTable table = new PdfPTable(maxCols);
                table.setWidthPercentage(100);
                table.setSpacingAfter(10f);
                
                com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
                
                int rowCount = 0;
                for (Row row : sheet) {
                    if (rowCount > 100) break;
                    
                    for (int j = 0; j < maxCols; j++) {
                        Cell cell = row.getCell(j);
                        String cellValue = "";
                        
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    cellValue = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    cellValue = String.valueOf(cell.getNumericCellValue());
                                    break;
                                case BOOLEAN:
                                    cellValue = String.valueOf(cell.getBooleanCellValue());
                                    break;
                                default:
                                    cellValue = "";
                            }
                        }
                        
                        PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue, cellFont));
                        pdfCell.setPadding(3f);
                        table.addCell(pdfCell);
                    }
                    rowCount++;
                }
                
                document.add(table);
            }
            
            workbook.close();
            fis.close();
        }
        
        private void convertPptxToPDF(Document document) throws Exception {
            FileInputStream fis = new FileInputStream(filePath);
            XMLSlideShow ppt = new XMLSlideShow(fis);
            
            // Add title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Presentation: " + new File(filePath).getName(), titleFont);
            title.setSpacingAfter(10f);
            document.add(title);
            
            // Process each slide
            List<XSLFSlide> slides = ppt.getSlides();
            com.itextpdf.text.Font slideFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLUE);
            com.itextpdf.text.Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            
            int slideNum = 1;
            for (XSLFSlide slide : slides) {
                // Add slide number
                Paragraph slideTitle = new Paragraph("Slide " + slideNum, slideFont);
                slideTitle.setSpacingBefore(15f);
                slideTitle.setSpacingAfter(8f);
                document.add(slideTitle);
                
                // Extract text from shapes
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            Paragraph para = new Paragraph(text.trim(), textFont);
                            para.setSpacingAfter(5f);
                            document.add(para);
                        }
                    }
                }
                
                slideNum++;
            }
            
            ppt.close();
            fis.close();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new pdfwithfunc());
    }
}
