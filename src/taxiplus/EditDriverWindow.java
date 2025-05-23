package taxiplus;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class EditDriverWindow extends JFrame implements CameraWindow.PhotoCaptureListener {
    private final JTextField cedulaField;
    private final JTextField nombresField;
    private final JTextField apellidosField;
    private final JTextField telefonoField;
    private final JTextField nacimientoField;
    private final JTextField licenciaField;
    private final String originalCedula;
    private JButton saveButton;
    private JButton takePictureButton;
    private JButton assignOrRemoveVehicleButton; // Botón dinámico
    private final Map<JTextField, JButton> editButtons = new HashMap<>();
    private BufferedImage capturedPhoto; // Para almacenar la foto capturada
    private JLabel photoLabelDisplay; // Para mostrar la foto en la interfaz
    private boolean hasAssignedVehicle = false; // Para rastrear si el conductor tiene un vehículo asignado

    public EditDriverWindow(String cedula) {
        this.originalCedula = cedula;

        setTitle("Editar Conductor - TaxiPlus");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        int yOffset = 30;
        int labelWidth = 120;
        int fieldWidth = 200;
        int buttonWidth = 30;
        int xOffset = 30;
        int spacing = 40;

        // Cédula
        JLabel cedulaLabel = new JLabel("Cédula:");
        cedulaLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(cedulaLabel);
        cedulaField = new JTextField();
        cedulaField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        cedulaField.setEnabled(false);
        ((AbstractDocument) cedulaField.getDocument()).setDocumentFilter(new NumberFilter());
        add(cedulaField);
        JButton editCedulaButton = createEditButton(cedulaField);
        editCedulaButton.setBounds(xOffset + labelWidth + 10 + fieldWidth + 5, yOffset, buttonWidth, 25);
        add(editCedulaButton);
        editButtons.put(cedulaField, editCedulaButton);
        yOffset += spacing;

        // Nombres
        JLabel nombresLabel = new JLabel("Nombres:");
        nombresLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(nombresLabel);
        nombresField = new JTextField();
        nombresField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        nombresField.setEnabled(false);
        ((AbstractDocument) nombresField.getDocument()).setDocumentFilter(new TextWithSingleSpaceFilter());
        add(nombresField);
        JButton editNombresButton = createEditButton(nombresField);
        editNombresButton.setBounds(xOffset + labelWidth + 10 + fieldWidth + 5, yOffset, buttonWidth, 25);
        add(editNombresButton);
        editButtons.put(nombresField, editNombresButton);
        yOffset += spacing;

        // Apellidos
        JLabel apellidosLabel = new JLabel("Apellidos:");
        apellidosLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(apellidosLabel);
        apellidosField = new JTextField();
        apellidosField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        apellidosField.setEnabled(false);
        ((AbstractDocument) apellidosField.getDocument()).setDocumentFilter(new TextWithSingleSpaceFilter());
        add(apellidosField);
        JButton editApellidosButton = createEditButton(apellidosField);
        editApellidosButton.setBounds(xOffset + labelWidth + 10 + fieldWidth + 5, yOffset, buttonWidth, 25);
        add(editApellidosButton);
        editButtons.put(apellidosField, editApellidosButton);
        yOffset += spacing;

        // Teléfono
        JLabel telefonoLabel = new JLabel("Teléfono:");
        telefonoLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(telefonoLabel);
        telefonoField = new JTextField();
        telefonoField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        telefonoField.setEnabled(false);
        ((AbstractDocument) telefonoField.getDocument()).setDocumentFilter(new PhoneNumberFilter());
        add(telefonoField);
        JButton editTelefonoButton = createEditButton(telefonoField);
        editTelefonoButton.setBounds(xOffset + labelWidth + 10 + fieldWidth + 5, yOffset, buttonWidth, 25);
        add(editTelefonoButton);
        editButtons.put(telefonoField, editTelefonoButton);
        yOffset += spacing;

        // Nacimiento
        JLabel nacimientoLabel = new JLabel("Nacimiento (DD/MM/AAAA):");
        nacimientoLabel.setBounds(xOffset, yOffset, labelWidth + 30, 25);
        add(nacimientoLabel);
        nacimientoField = new JTextField();
        nacimientoField.setBounds(xOffset + labelWidth + 40, yOffset, fieldWidth, 25);
        nacimientoField.setEnabled(false);
        add(nacimientoField);
        JButton editNacimientoButton = createEditButton(nacimientoField);
        editNacimientoButton.setBounds(xOffset + labelWidth + 40 + fieldWidth + 5, yOffset, buttonWidth, 25);
        add(editNacimientoButton);
        editButtons.put(nacimientoField, editNacimientoButton);
        yOffset += spacing;

        // Licencia
        JLabel licenciaLabel = new JLabel("Licencia (DD/MM/AAAA):");
        licenciaLabel.setBounds(xOffset, yOffset, labelWidth + 30, 25);
        add(licenciaLabel);
        licenciaField = new JTextField();
        licenciaField.setBounds(xOffset + labelWidth + 40, yOffset, fieldWidth, 25);
        licenciaField.setEnabled(false);
        add(licenciaField);
        JButton editLicenciaButton = createEditButton(licenciaField);
        editLicenciaButton.setBounds(xOffset + labelWidth + 40 + fieldWidth + 5, yOffset, buttonWidth, 25);
        add(editLicenciaButton);
        editButtons.put(licenciaField, editLicenciaButton);
        yOffset += spacing + 10;

        // Etiqueta para mostrar la foto
        photoLabelDisplay = new JLabel();
        photoLabelDisplay.setBounds(xOffset, yOffset, 100, 100);
        photoLabelDisplay.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Borde para visualizar el espacio
        add(photoLabelDisplay);

        // Botón "Tomar otra foto"
        takePictureButton = new JButton("Tomar otra foto");
        takePictureButton.setBounds(xOffset + 120, yOffset, 150, 30);
        add(takePictureButton);
        takePictureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCameraWindow();
            }
        });

        // Botón dinámico para Asignar/Quitar vehículo
        assignOrRemoveVehicleButton = new JButton();
        assignOrRemoveVehicleButton.setBounds(xOffset, yOffset + 110, 200, 30);
        add(assignOrRemoveVehicleButton);
        assignOrRemoveVehicleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasAssignedVehicle) {
                    removeAssignedVehicle();
                } else {
                    openVehicleSelectionWindow();
                }
            }
        });

        // Botón "Guardar cambios"
        saveButton = new JButton("Guardar cambios");
        saveButton.setBounds(xOffset + 220, yOffset + 110, 150, 30);
        add(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });

        loadDriverData();
        updateAssignRemoveButtonText(); // Inicializar el texto del botón
    }

    private void updateAssignRemoveButtonText() {
        if (hasAssignedVehicle) {
            assignOrRemoveVehicleButton.setText("Quitar vehículo asignado");
        } else {
            assignOrRemoveVehicleButton.setText("Asignar vehículo");
        }
    }

    private void openVehicleSelectionWindow() {
        VehicleSelectionWindow vehicleSelectionWindow = new VehicleSelectionWindow(this, originalCedula);
        vehicleSelectionWindow.setVisible(true);
    }

    private void removeAssignedVehicle() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea quitar el vehículo asignado a este conductor?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "UPDATE vehiculos SET cedula_conductor = NULL WHERE cedula_conductor = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, originalCedula);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Vehículo asignado removido exitosamente.");
                    hasAssignedVehicle = false;
                    updateAssignRemoveButtonText();
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró ningún vehículo asignado a este conductor.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al quitar el vehículo asignado: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void openCameraWindow() {
        CameraWindow cameraWindow = new CameraWindow(this); // 'this' es la instancia de EditDriverWindow (el listener)
        cameraWindow.setVisible(true);
    }

    @Override
    public void onPhotoCaptured(BufferedImage photo) {
        this.capturedPhoto = photo;
        // Mostrar la foto capturada en la ventana EditDriverWindow
        if (photo != null) {
            ImageIcon icon = new ImageIcon(photo.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            photoLabelDisplay.setIcon(icon);
            revalidate();
            repaint();
        } else {
            photoLabelDisplay.setIcon(null);
            JOptionPane.showMessageDialog(this, "No se pudo mostrar la foto capturada.");
        }
    }

    private JButton createEditButton(JTextField textField) {
        JButton button = new JButton("✎");
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.addActionListener(new ActionListener() {
            private boolean isEditing = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                isEditing = !isEditing;
                textField.setEnabled(isEditing);
                button.setText(isEditing ? "🔒" : "✎");
            }
        });
        return button;
    }

    public void loadDriverData() {
        String sql = "SELECT cedula, nombres, apellidos, telefono, DATE_FORMAT(fecha_nacimiento, '%d/%m/%Y') AS fecha_nacimiento, DATE_FORMAT(expedicion_licencia, '%d/%m/%Y') AS expedicion_licencia, foto FROM Conductores WHERE cedula = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, originalCedula);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String cedulaBD = resultSet.getString("cedula");
                    String nombresBD = resultSet.getString("nombres");
                    String apellidosBD = resultSet.getString("apellidos");
                    String telefonoBD = resultSet.getString("telefono");
                    String nacimientoBD = resultSet.getString("fecha_nacimiento");
                    String licenciaBD = resultSet.getString("expedicion_licencia");
                    byte[] imageBytes = resultSet.getBytes("foto");

                    cedulaField.setText(cedulaBD);
                    nombresField.setText(nombresBD);
                    apellidosField.setText(apellidosBD);
                    telefonoField.setText(telefonoBD);
                    nacimientoField.setText(nacimientoBD);
                    licenciaField.setText(licenciaBD);

                    // Mostrar la foto cargada desde la base de datos
                    if (imageBytes != null) {
                        try (ByteArrayInputStream in = new ByteArrayInputStream(imageBytes)) {
                            BufferedImage loadedImage = ImageIO.read(in);
                            if (loadedImage != null) {
                                ImageIcon icon = new ImageIcon(loadedImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
                                photoLabelDisplay.setIcon(icon);
                            } else {
                                photoLabelDisplay.setIcon(null);
                                System.err.println("Error: No se pudo leer la imagen desde bytes.");
                            }
                        } catch (IOException ex) {
                            System.err.println("Error al cargar la imagen desde la base de datos: " + ex.getMessage());
                            ex.printStackTrace();
                            photoLabelDisplay.setIcon(null);
                        }
                    } else {
                        photoLabelDisplay.setIcon(null); // Si no hay foto en la BD
                    }

                    revalidate();
                    repaint();

                }
            }
        } catch (SQLException e) {
            System.err.println("Ocurrió un error al cargar los datos del conductor: " + e.getMessage());
            e.printStackTrace();
            photoLabelDisplay.setIcon(null);
        }

        // Verificar si el conductor tiene un vehículo asignado
        String vehicleSql = "SELECT placa FROM vehiculos WHERE cedula_conductor = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement vehicleStatement = connection.prepareStatement(vehicleSql)) {
            vehicleStatement.setString(1, originalCedula);
            ResultSet vehicleResultSet = vehicleStatement.executeQuery();
            hasAssignedVehicle = vehicleResultSet.next();
            updateAssignRemoveButtonText();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveChanges() {
        String cedula = cedulaField.getText();
        String nombres = nombresField.getText();
        String apellidos = apellidosField.getText();
        String telefono = telefonoField.getText();
        String nacimiento = nacimientoField.getText();
        String licencia = licenciaField.getText();

        // Validar solo los campos que estén habilitados para la edición
        if (editButtons.get(cedulaField).getText().equals("🔒") && !cedulaField.getText().matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "La cédula solo debe contener números.");
            return;
        }
        if (editButtons.get(nombresField).getText().equals("🔒") && !nombresField.getText().matches("^[a-zA-Z]+(\\s[a-zA-Z]+)?$")) {
            JOptionPane.showMessageDialog(this, "Los nombres solo deben contener letras y un espacio opcional.");
            return;
        }
        if (editButtons.get(apellidosField).getText().equals("🔒") && !apellidosField.getText().matches("^[a-zA-Z]+(\\s[a-zA-Z]+)?$")) {
            JOptionPane.showMessageDialog(this, "Los apellidos solo deben contener letras y un espacio opcional.");
            return;
        }
        if (editButtons.get(telefonoField).getText().equals("🔒") && !telefonoField.getText().matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "El teléfono debe contener exactamente 10 números.");
            return;
        }
        if (editButtons.get(nacimientoField).getText().equals("🔒")) {
            if (!nacimientoField.getText().matches("\\d{2}/\\d{2}/\\d{4}")) {
                JOptionPane.showMessageDialog(this, "El formato de nacimiento debe ser DD/MM/AAAA.");
                return;
            }
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate fechaNacimiento = LocalDate.parse(nacimientoField.getText(), dateFormatter);
                LocalDate ahora = LocalDate.now(java.time.ZoneId.of("America/Bogota"));
                LocalDate hace18Anos = ahora.minusYears(18);
                if (fechaNacimiento.isAfter(hace18Anos)) {
                    JOptionPane.showMessageDialog(this, "La fecha de nacimiento debe ser de al menos 18 años atrás.");
                    return;
                }
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Error al parsear la fecha de nacimiento.");
                return;
            }
        }
        if (editButtons.get(licenciaField).getText().equals("🔒")) {
            if (!licenciaField.getText().matches("\\d{2}/\\d{2}/\\d{4}")) {
                JOptionPane.showMessageDialog(this, "El formato de la licencia debe ser DD/MM/AAAA.");
                return;
            }
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate fechaNacimientoParsed = LocalDate.parse(nacimientoField.getText(), dateFormatter);
                LocalDate fechaLicencia = LocalDate.parse(licenciaField.getText(), dateFormatter);
                LocalDate dieciseisAnosDespues = fechaNacimientoParsed.plusYears(16);
                if (fechaLicencia.isBefore(dieciseisAnosDespues)) {
                    JOptionPane.showMessageDialog(this, "La fecha de la licencia debe ser al menos 16 años después de la fecha de nacimiento.");
                    return;
                }
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Error al parsear la fecha de la licencia.");
                return;
            }
        }

        String sql = "UPDATE Conductores SET cedula = ?, nombres = ?, apellidos = ?, telefono = ?, fecha_nacimiento = STR_TO_DATE(?, '%d/%m/%Y'), expedicion_licencia = STR_TO_DATE(?, '%d/%m/%Y'), foto = ? WHERE cedula = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cedula);
            statement.setString(2, nombres);
            statement.setString(3, apellidos);
            statement.setString(4, telefono);
            statement.setString(5, nacimiento);
            statement.setString(6, licencia);

            // Guardar la foto si se capturó una nueva
            if (capturedPhoto != null) {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    ImageIO.write(capturedPhoto, "png", bos); // Puedes usar otro formato como "jpg"
                    byte[] imageBytes = bos.toByteArray();
                    statement.setBytes(7, imageBytes);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error al convertir la imagen: " + ex.getMessage());
                    ex.printStackTrace();
                    return; // No continuar si hay un error con la imagen
                }
            } else {
                statement.setNull(7, java.sql.Types.BLOB); // Si no hay nueva foto, guardar como NULL
            }

            statement.setString(8, originalCedula); // El WHERE clause sigue siendo la cédula

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Datos actualizados correctamente.");
                dispose();
                new RegisteredDriversWindow().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la información.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar los cambios: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Filtro para permitir solo números
    private static class NumberFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string.matches("\\d*")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text.matches("\\d*")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    // Filtro para permitir solo letras y un espacio opcional entre palabras
    private static class TextWithSingleSpaceFilter extends DocumentFilter {
        private final Pattern pattern = Pattern.compile("^[a-zA-Z]+(\\s[a-zA-Z]+)?$");

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
            Matcher matcher = pattern.matcher(newText);
            if (matcher.matches()) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String newText = fb.getDocument().getText(0, offset) + text + fb.getDocument().getText(offset + length, fb.getDocument().getLength() - (offset + length));
            Matcher matcher = pattern.matcher(newText);
            if (matcher.matches()) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    // Filtro para permitir solo 10 números para el teléfono
    private static class PhoneNumberFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string.matches("\\d*") && fb.getDocument().getLength() + string.length() <= 10) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text.matches("\\d*") && fb.getDocument().getLength() - length + text.length() <= 10) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}