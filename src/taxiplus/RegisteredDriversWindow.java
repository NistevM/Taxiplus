package taxiplus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisteredDriversWindow extends JFrame {
    private final JTextField searchField;
    private final JTable resultsTable;
    private final JLabel photoLabel;
    private final DefaultTableModel tableModel;

    public RegisteredDriversWindow() {
        setTitle("Conductores Registrados - TaxiPlus");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Botón "Volver atrás"
        JButton backButton = new JButton("Volver atrás");
        backButton.setBounds(10, 10, 120, 30);
        add(backButton);

        backButton.addActionListener(e -> {
            dispose(); // Cierra esta ventana
            new MainMenuWindow().setVisible(true); // Regresa al menú principal
        });

        // Barra de búsqueda
        JLabel searchLabel = new JLabel("Buscar:");
        searchLabel.setBounds(20, 50, 50, 30);
        add(searchLabel);

        searchField = new JTextField();
        searchField.setBounds(80, 50, 400, 30);
        add(searchField);

        JButton searchButton = new JButton("Buscar");
        searchButton.setBounds(500, 50, 100, 30);
        add(searchButton);

        // Tabla de resultados
        tableModel = new DefaultTableModel(new String[]{"Cédula", "Nombres", "Apellidos", "Nacimiento", "Licencia"}, 0);
        resultsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBounds(20, 100, 580, 400);
        add(scrollPane);

        // Espacio para mostrar la foto
        photoLabel = new JLabel();
        photoLabel.setBounds(620, 100, 150, 200);
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(photoLabel);

        // Botón "Generar carnet"
        JButton generateIDButton = new JButton("Generar carnet");
        generateIDButton.setBounds(620, 320, 150, 30);
        add(generateIDButton);

        // Acción del botón "Buscar"
        searchButton.addActionListener(e -> searchDrivers(searchField.getText()));

        // Acción al seleccionar una fila en la tabla
        resultsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && resultsTable.getSelectedRow() != -1) {
                String cedula = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 0);
                loadPhoto(cedula);
            }
        });

        // Acción del botón "Generar carnet"
        generateIDButton.addActionListener(e -> {
            if (resultsTable.getSelectedRow() != -1) {
                String cedula = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 0);
                String nombres = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 1);
                String apellidos = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 2);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar carnet como");
                fileChooser.setSelectedFile(new File("carnet_" + cedula + ".pdf"));
                int userSelection = fileChooser.showSaveDialog(this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    generatePDF(cedula, nombres, apellidos, fileToSave.getAbsolutePath());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Seleccione un conductor para generar el carnet.");
            }
        });
    }

    private void searchDrivers(String query) {
        tableModel.setRowCount(0); // Limpiar resultados previos
        String sql = "SELECT cedula, nombres, apellidos, fecha_nacimiento, expedicion_licencia FROM Conductores WHERE cedula LIKE ? OR nombres LIKE ? OR apellidos LIKE ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + query + "%");
            statement.setString(2, "%" + query + "%");
            statement.setString(3, "%" + query + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tableModel.addRow(new Object[]{
                            resultSet.getString("cedula"),
                            resultSet.getString("nombres"),
                            resultSet.getString("apellidos"),
                            resultSet.getString("fecha_nacimiento"),
                            resultSet.getString("expedicion_licencia")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al realizar la búsqueda: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadPhoto(String cedula) {
        String sql = "SELECT foto FROM Conductores WHERE cedula = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cedula);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    byte[] photoBytes = resultSet.getBytes("foto");
                    if (photoBytes != null) {
                        ImageIcon photo = new ImageIcon(photoBytes);
                        Image scaledImage = photo.getImage().getScaledInstance(photoLabel.getWidth(), photoLabel.getHeight(), Image.SCALE_SMOOTH);
                        photoLabel.setIcon(new ImageIcon(scaledImage));
                    } else {
                        photoLabel.setIcon(null);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar la foto: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void generatePDF(String cedula, String nombres, String apellidos, String outputPath) {
    try (PDDocument document = new PDDocument()) {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // 1. Foto del conductor
        String sql = "SELECT foto FROM Conductores WHERE cedula = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cedula);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    byte[] photoBytes = resultSet.getBytes("foto");
                    if (photoBytes != null) {
                        PDImageXObject photo = PDImageXObject.createFromByteArray(document, photoBytes, "photo");
                        contentStream.drawImage(photo, 150, 500, 300, 300); // Foto con separación superior
                    }
                }
            }
        }

        // 2. Código de barras (sin número de cédula en el cuadro rojo)
        BufferedImage barcodeImage = generateBarcode(cedula);
        if (barcodeImage != null) {
            PDImageXObject barcode = PDImageXObject.createFromByteArray(document, imageToByteArray(barcodeImage), "barcode");
            contentStream.drawImage(barcode, 150, 420, 300, 60); // Más separado de la foto
        }

        // 3. Número de cédula (centrado y único)
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24); // Tamaño ajustado
        float textWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(cedula) / 1000) * 24;
        float centerX = (page.getMediaBox().getWidth() - textWidth) / 2; // Centrar horizontalmente
        contentStream.newLineAtOffset(centerX, 400); // Posicionado justo debajo del código de barras
        contentStream.showText(cedula);
        contentStream.endText();

        // 4. Texto: Nombres (centrado completamente)
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 36); // Tamaño reducido
        textWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(nombres.toUpperCase()) / 1000) * 36;
        centerX = (page.getMediaBox().getWidth() - textWidth) / 2; // Centrar horizontalmente
        contentStream.newLineAtOffset(centerX, 350); // Espaciado debajo de la cédula
        contentStream.showText(nombres.toUpperCase());
        contentStream.endText();

        // 5. Texto: Apellidos (centrado completamente)
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 36); // Tamaño reducido
        textWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(apellidos.toUpperCase()) / 1000) * 36;
        centerX = (page.getMediaBox().getWidth() - textWidth) / 2; // Centrar horizontalmente
        contentStream.newLineAtOffset(centerX, 320); // Espaciado debajo de nombres
        contentStream.showText(apellidos.toUpperCase());
        contentStream.endText();

        // 6. Texto: "Conductor Asociado" (centrado completamente)
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 36); // Tamaño reducido
        textWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth("CONDUCTOR ASOCIADO") / 1000) * 36;
        centerX = (page.getMediaBox().getWidth() - textWidth) / 2; // Centrar horizontalmente
        contentStream.newLineAtOffset(centerX, 280); // Espaciado debajo de apellidos
        contentStream.showText("CONDUCTOR ASOCIADO");
        contentStream.endText();

        // 7. Logo en la base del documento
        String footerImagePath = "C:\\Users\\User\\OneDrive\\Documentos\\NetBeansProjects\\Taxiplus\\image.png";
        File footerImageFile = new File(footerImagePath);
        if (footerImageFile.exists() && footerImageFile.isFile()) {
            try {
                PDImageXObject footerImage = PDImageXObject.createFromFile(footerImagePath, document);
                float footerWidth = page.getMediaBox().getWidth();
                float footerHeight = (footerImage.getHeight() * footerWidth) / footerImage.getWidth(); // Escalar proporcionalmente
                contentStream.drawImage(footerImage, 0, 0, footerWidth, footerHeight); // Sin márgenes
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen del pie de página: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "La imagen del pie de página no se encontró en: " + footerImagePath);
        }

        contentStream.close();

        // Guardar el PDF
        document.save(outputPath);

        // Abrir el PDF automáticamente
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(new File(outputPath));
        }

        JOptionPane.showMessageDialog(this, "Carnet generado con éxito en: " + outputPath);
    } catch (IOException | SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al generar el carnet: " + ex.getMessage());
        ex.printStackTrace();
    }
}

private BufferedImage generateBarcode(String text) throws IOException {
    Code128Bean barcodeGenerator = new Code128Bean();
    final int dpi = 160; // Resolución del código de barras

    barcodeGenerator.setModuleWidth(0.2); // Ancho del módulo
    barcodeGenerator.doQuietZone(false); // Desactivar zona silenciosa
    barcodeGenerator.setFontSize(0); // Desactivar el texto del número debajo del código de barras

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, "image/png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
    barcodeGenerator.generateBarcode(canvas, text);
    canvas.finish();

    return ImageIO.read(new java.io.ByteArrayInputStream(out.toByteArray()));
}

private byte[] imageToByteArray(BufferedImage image) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    return baos.toByteArray();
}
}