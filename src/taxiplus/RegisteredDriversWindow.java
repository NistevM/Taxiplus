package taxiplus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
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

    // Botones que se habilitan/deshabilitan según la selección
    private final JButton generateIDButton;
    private final JButton editDriverButton;
    private final JButton assignedVehicleButton;
    private final JButton deleteDriverButton;

    // Control para ventana de edición
    private JFrame editWindow = null;

    public RegisteredDriversWindow() {
        setTitle("Conductores Registrados - TaxiPlus");
        setSize(800, 700); // Aumenté la altura para acomodar los nuevos botones
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Botón "Volver atrás"
        JButton backButton = new JButton("Volver atrás");
        backButton.setBounds(10, 10, 120, 30);
        add(backButton);

        backButton.addActionListener(e -> {
            dispose(); // Cierra esta ventana
            new DriversMenuWindow().setVisible(true); // Regresa al menú de conductores
        });

        JLabel searchLabel = new JLabel("Buscar:");
        searchLabel.setBounds(20, 50, 50, 30);
        add(searchLabel);

        searchField = new JTextField();
        searchField.setBounds(80, 50, 400, 30);
        add(searchField);

        JButton searchButton = new JButton("Buscar");
        searchButton.setBounds(500, 50, 100, 30);
        add(searchButton);

        // Tabla de resultados (ahora incluye teléfono)
        tableModel = new DefaultTableModel(new String[]{"Cédula", "Nombres", "Apellidos", "Nacimiento", "Licencia", "Teléfono"}, 0);
        resultsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBounds(20, 100, 580, 400);
        add(scrollPane);

        // Espacio para mostrar la foto
        photoLabel = new JLabel();
        photoLabel.setBounds(620, 100, 150, 200);
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(photoLabel);

        // Botones (inicialmente deshabilitados)
        generateIDButton = new JButton("Generar carnet");
        generateIDButton.setBounds(620, 320, 150, 30);
        generateIDButton.setEnabled(false);
        add(generateIDButton);

        editDriverButton = new JButton("Editar conductor");
        editDriverButton.setBounds(620, 360, 150, 30);
        editDriverButton.setEnabled(false);
        add(editDriverButton);

        assignedVehicleButton = new JButton("Vehículo asignado");
        assignedVehicleButton.setBounds(620, 400, 150, 30);
        assignedVehicleButton.setEnabled(false);
        add(assignedVehicleButton);

        deleteDriverButton = new JButton("Eliminar conductor");
        deleteDriverButton.setBounds(620, 440, 150, 30);
        deleteDriverButton.setEnabled(false);
        add(deleteDriverButton);

        // Acción del botón "Buscar"
        searchButton.addActionListener(e -> searchDrivers(searchField.getText()));

        // Acción al seleccionar una fila en la tabla
        resultsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                boolean hasSelection = resultsTable.getSelectedRow() != -1;

                // Habilitar/deshabilitar botones según la selección
                generateIDButton.setEnabled(hasSelection);
                editDriverButton.setEnabled(hasSelection && editWindow == null);
                assignedVehicleButton.setEnabled(hasSelection);
                deleteDriverButton.setEnabled(hasSelection);

                if (hasSelection) {
                    String cedula = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 0);
                    loadPhoto(cedula);
                }
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
            }
        });

        // Acción del botón "Editar conductor"
        editDriverButton.addActionListener(e -> {
            if (resultsTable.getSelectedRow() != -1) {
                String cedula = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 0);
                openEditDriverWindow(cedula);
            }
        });

        assignedVehicleButton.addActionListener(e -> {
            int selectedRow = resultsTable.getSelectedRow();
            if (selectedRow != -1) {
                String driverId = (String) tableModel.getValueAt(selectedRow, 0); // La cédula está en la primera columna (índice 0)
                AssignedVehicleWindow assignedVehicleWindow = new AssignedVehicleWindow(RegisteredDriversWindow.this, driverId);
                assignedVehicleWindow.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(RegisteredDriversWindow.this, "Por favor, selecciona un conductor.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Acción del botón "Eliminar conductor"
        deleteDriverButton.addActionListener(e -> {
            if (resultsTable.getSelectedRow() != -1) {
                String cedula = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 0);
                String nombres = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 1);
                String apellidos = (String) tableModel.getValueAt(resultsTable.getSelectedRow(), 2);

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "¿Está seguro que desea eliminar al conductor?\n" + nombres + " " + apellidos + " (Cédula: " + cedula + ")",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteDriver(cedula);
                }
            }
        });
    }

    private void searchDrivers(String query) {
        tableModel.setRowCount(0); // Limpiar resultados previos
        String sql = "SELECT cedula, nombres, apellidos, fecha_nacimiento, expedicion_licencia, telefono FROM Conductores WHERE cedula LIKE ? OR nombres LIKE ? OR apellidos LIKE ?";
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
                            resultSet.getString("expedicion_licencia"),
                            resultSet.getString("telefono")
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
                        BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(photoBytes));
                        photoLabel.setIcon(new ImageIcon(cropToCircle(image, photoLabel.getWidth(), photoLabel.getHeight())));
                    } else {
                        photoLabel.setIcon(null);
                    }
                }
            }
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar la foto: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openEditDriverWindow(String cedula) {
        if (editWindow != null) {
            editWindow.toFront(); // Si ya existe una ventana, la trae al frente
            return;
        }

        editWindow = new EditDriverWindow(cedula);
        editWindow.setVisible(true);
        editDriverButton.setEnabled(false); // Deshabilitar el botón mientras está abierta la ventana

        // Listener para cuando se cierre la ventana de edición
        editWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                editWindow = null;
                // Volver a habilitar el botón si hay una selección
                editDriverButton.setEnabled(resultsTable.getSelectedRow() != -1);
                // Actualizar la búsqueda para reflejar cambios
                searchDrivers(searchField.getText());
            }
        });
    }

    private void deleteDriver(String cedula) {
        String sql = "DELETE FROM Conductores WHERE cedula = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cedula);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Conductor eliminado exitosamente.");
                // Actualizar la tabla
                searchDrivers(searchField.getText());
                // Limpiar la foto
                photoLabel.setIcon(null);
            } else {
                JOptionPane.showMessageDialog(this, "Error: No se pudo eliminar el conductor.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el conductor: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void generatePDF(String cedula, String nombres, String apellidos, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Dimensiones de la página
            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            float margin = 20;

            // Fondo amarillo
            contentStream.setNonStrokingColor(255, 219, 67); // Amarillo
            contentStream.addRect(0, 0, width, height);
            contentStream.fill();

            // Foto circular (más grande)
            String sql = "SELECT foto FROM Conductores WHERE cedula = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cedula);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        byte[] photoBytes = resultSet.getBytes("foto");
                        if (photoBytes != null) {
                            BufferedImage photo = ImageIO.read(new java.io.ByteArrayInputStream(photoBytes));
                            BufferedImage circularPhoto = cropToCircle(photo, 250, 250); // Más grande: 250x250
                            PDImageXObject pdPhoto = PDImageXObject.createFromByteArray(document, imageToByteArray(circularPhoto), "photo");
                            contentStream.drawImage(pdPhoto, (width - 300) / 2, height - 350, 300, 300); // Centrado arriba
                        }
                    }
                }
            }

            // Código de barras con fondo transparente
            BufferedImage barcodeImage = generateBarcode(cedula); // Generar código de barras
            if (barcodeImage != null) {
                float barcodeWidth = 300;
                float barcodeHeight = 60;
                float barcodeX = (width - barcodeWidth) / 2;
                float barcodeY = height - 450;

                // Dibujar el código de barras directamente (sin fondo amarillo adicional)
                PDImageXObject barcode = PDImageXObject.createFromByteArray(document, imageToByteArray(barcodeImage), "barcode");
                contentStream.drawImage(barcode, barcodeX, barcodeY, barcodeWidth, barcodeHeight); // Más grande y centrado
            }

            // Número de cédula
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
            contentStream.setNonStrokingColor(0, 0, 0); // Negro
            contentStream.newLineAtOffset((width - 150) / 2, height - 480); // Posicionado cerca del código de barras
            contentStream.showText(cedula);
            contentStream.endText();

            // Nombres (en negro, centrado dinámicamente)
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 36);
            contentStream.setNonStrokingColor(0, 0, 0); // Negro

            // Calcular el ancho del texto de nombres para centrarlo perfectamente
            float nombresWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(nombres.toUpperCase()) / 1000f * 36;
            float nombresX = (width - nombresWidth) / 2;
            contentStream.newLineAtOffset(nombresX, height - 520);
            contentStream.showText(nombres.toUpperCase());
            contentStream.endText();

            // Apellidos (en negro, centrado dinámicamente)
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 36);
            contentStream.setNonStrokingColor(0, 0, 0); // Negro

            // Calcular el ancho del texto de apellidos para centrarlo perfectamente
            float apellidosWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(apellidos.toUpperCase()) / 1000f * 36;
            float apellidosX = (width - apellidosWidth) / 2;
            contentStream.newLineAtOffset(apellidosX, height - 560);
            contentStream.showText(apellidos.toUpperCase());
            contentStream.endText();

            // Teléfono (añadido debajo de apellidos)
            String sqlTelefono = "SELECT telefono FROM Conductores WHERE cedula = ?";
            String telefono = "";
            try (Connection connectionTel = DatabaseConnection.getConnection();
                 PreparedStatement statementTel = connectionTel.prepareStatement(sqlTelefono)) {
                statementTel.setString(1, cedula);
                try (ResultSet resultSetTel = statementTel.executeQuery()) {
                    if (resultSetTel.next()) {
                        telefono = resultSetTel.getString("telefono");
                    }
                }
            }

            if (telefono != null && !telefono.trim().isEmpty()) {contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 24); // Más pequeño que nombres/apellidos
                contentStream.setNonStrokingColor(0, 0, 0); // Negro

                // Calcular el ancho del texto de teléfono para centrarlo perfectamente
                float telefonoWidth = PDType1Font.HELVETICA.getStringWidth(telefono) / 1000f * 24;
                float telefonoX = (width - telefonoWidth) / 2;
                contentStream.newLineAtOffset(telefonoX, height - 600);
                contentStream.showText(telefono);
                contentStream.endText();
            }

            // Franja negra de "Conductor Asociado" (movida hacia arriba)
            float blackBarHeight = 70;
            float blackBarY = 160; // Subí de 120 a 160 para dar más espacio al logo
            contentStream.setNonStrokingColor(0, 0, 0); // Negro
            contentStream.addRect(0, blackBarY, width, blackBarHeight);
            contentStream.fill();

            // Texto "Conductor Asociado" (más grande)
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 36);
            contentStream.setNonStrokingColor(255, 255, 255); // Blanco

            // Calcular el ancho del texto para centrarlo perfectamente
            String conductorText = "Conductor Asociado";
            float conductorTextWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(conductorText) / 1000f * 36;
            float conductorTextX = (width - conductorTextWidth) / 2;
            contentStream.newLineAtOffset(conductorTextX, blackBarY + (blackBarHeight - 36) / 2); // Centrado vertical en la franja
            contentStream.showText(conductorText);
            contentStream.endText();

            // Logo extendido (más grande sin deformación)
            String footerImagePath = "C:\\Users\\User\\OneDrive\\Documentos\\NetBeansProjects\\Taxiplus\\image.png";
            File footerImageFile = new File(footerImagePath);
            if (footerImageFile.exists() && footerImageFile.isFile()) {
                try {
                    // Cargar la imagen para obtener sus dimensiones originales
                    BufferedImage originalLogo = ImageIO.read(footerImageFile);
                    float originalWidth = originalLogo.getWidth();
                    float originalHeight = originalLogo.getHeight();
                    float aspectRatio = originalWidth / originalHeight;

                    float logoWidth = width; // Ancho completo de la página
                    float logoHeight = logoWidth / aspectRatio; // Altura proporcional

                    // Ahora tenemos más espacio disponible hasta la franja (que está en 160)
                    float maxLogoHeight = blackBarY - margin - 20; // Dejamos 20px de separación
                    if (logoHeight > maxLogoHeight) {
                        logoHeight = maxLogoHeight;
                        logoWidth = logoHeight * aspectRatio;
                    }

                    // Centrar horizontalmente si es necesario
                    float logoX = (width - logoWidth) / 2;

                    PDImageXObject footerImage = PDImageXObject.createFromFile(footerImagePath, document);
                    contentStream.drawImage(footerImage, logoX, margin, logoWidth, logoHeight);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error al cargar la imagen del pie de página: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            contentStream.close();
            document.save(outputPath);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(outputPath));
            }

            JOptionPane.showMessageDialog(this, "Carnet generado con éxito.");
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el carnet: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private BufferedImage cropToCircle(BufferedImage source, int width, int height) {
        BufferedImage circleBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();

        g2.setClip(new Ellipse2D.Float(0, 0, width, height));
        g2.drawImage(source, 0, 0, width, height, null);
        g2.dispose();

        return circleBuffer;
    }

    private BufferedImage generateBarcode(String text) throws IOException {
        Code128Bean barcodeGenerator = new Code128Bean();
        final int dpi = 160;

        barcodeGenerator.setModuleWidth(0.2);
        barcodeGenerator.doQuietZone(false);
        barcodeGenerator.setFontSize(0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, "image/png", dpi, BufferedImage.TYPE_INT_ARGB, false, 0);
        barcodeGenerator.generateBarcode(canvas, text);
        canvas.finish();

        // Leer la imagen generada
        BufferedImage originalBarcode = ImageIO.read(new java.io.ByteArrayInputStream(out.toByteArray()));

        BufferedImage transparentBarcode = new BufferedImage(
                originalBarcode.getWidth(),
                originalBarcode.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = transparentBarcode.createGraphics();

        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, transparentBarcode.getWidth(), transparentBarcode.getHeight());

        g2d.setComposite(AlphaComposite.SrcOver);

        for (int x = 0; x < originalBarcode.getWidth(); x++) {
            for (int y = 0; y < originalBarcode.getHeight(); y++) {
                int rgb = originalBarcode.getRGB(x, y);

                if ((rgb & 0xFFFFFF) < 0x808080) {
                    transparentBarcode.setRGB(x, y, 0xFF000000);
                }
            }
        }

        g2d.dispose();
        return transparentBarcode;
    }

    private byte[] imageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}