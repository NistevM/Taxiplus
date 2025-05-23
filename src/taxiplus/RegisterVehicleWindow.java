package taxiplus;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RegisterVehicleWindow extends JFrame {

    private JTextField placaField;
    private JComboBox<String> marcaComboBox; // Cambiado a JComboBox
    private JTextField modeloField;
    private JTextField cedulaConductorField;
    private JFormattedTextField fechaSeguroField;
    private JFormattedTextField fechaTecnomecanicaField;
    private JButton registerButton;
    private JButton backButton;
    private List<String> marcasList; // Para almacenar las marcas cargadas

    public RegisterVehicleWindow() {
        setTitle("Registrar Vehículo - TaxiPlus");
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        marcasList = loadMarcas(); // Cargar las marcas desde la base de datos
        String[] marcasArray = marcasList.toArray(new String[0]);

        int yOffset = 30;
        int labelWidth = 180;
        int fieldWidth = 200;
        int xOffset = 30;
        int spacing = 35;

        // Placa
        JLabel placaLabel = new JLabel("Placa (Ej: AAA123):");
        placaLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(placaLabel);
        placaField = new JTextField();
        placaField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        ((AbstractDocument) placaField.getDocument()).setDocumentFilter(new PlateNumberFilter());
        add(placaField);
        yOffset += spacing;

        // Marca (Ahora un JComboBox)
        JLabel marcaLabel = new JLabel("Marca:");
        marcaLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(marcaLabel);
        marcaComboBox = new JComboBox<>(marcasArray);
        marcaComboBox.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        add(marcaComboBox);
        yOffset += spacing;

        // Modelo
        JLabel modeloLabel = new JLabel("Modelo (Año):");
        modeloLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(modeloLabel);
        modeloField = new JTextField();
        modeloField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        ((AbstractDocument) modeloField.getDocument()).setDocumentFilter(new FixedLengthNumberFilter(4));
        add(modeloField);
        yOffset += spacing;

        // Cédula Conductor (Opcional)
        JLabel cedulaConductorLabel = new JLabel("Cédula Conductor (Opcional):");
        cedulaConductorLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(cedulaConductorLabel);
        cedulaConductorField = new JTextField();
        cedulaConductorField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        ((AbstractDocument) cedulaConductorField.getDocument()).setDocumentFilter(new NumberFilter());
        add(cedulaConductorField);
        yOffset += spacing;

        // Fecha Seguro
        JLabel fechaSeguroLabel = new JLabel("Fecha Seguro:");
        fechaSeguroLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(fechaSeguroLabel);
        fechaSeguroField = new JFormattedTextField(createFormatter("##/##/####"));
        fechaSeguroField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        fechaSeguroField.addFocusListener(new DateFocusListener(fechaSeguroField));
        add(fechaSeguroField);
        yOffset += spacing;

        // Fecha Tecnomecánica
        JLabel fechaTecnomecanicaLabel = new JLabel("Fecha Tecnomecánica:");
        fechaTecnomecanicaLabel.setBounds(xOffset, yOffset, labelWidth, 25);
        add(fechaTecnomecanicaLabel);
        fechaTecnomecanicaField = new JFormattedTextField(createFormatter("##/##/####"));
        fechaTecnomecanicaField.setBounds(xOffset + labelWidth + 10, yOffset, fieldWidth, 25);
        fechaTecnomecanicaField.addFocusListener(new DateFocusListener(fechaTecnomecanicaField));
        add(fechaTecnomecanicaField);
        yOffset += spacing + 15;

        // Botón Registrar
        registerButton = new JButton("Registrar Vehículo");
        registerButton.setBounds(xOffset + 50, yOffset, 150, 30);
        registerButton.addActionListener(e -> registerNewVehicle());
        add(registerButton);

        // Botón Volver atrás
        backButton = new JButton("Volver atrás");
        backButton.setBounds(xOffset + 210, yOffset, 150, 30);
        backButton.addActionListener(e -> {
            dispose();
            VehiclesMenuWindow vehiclesMenuWindow = new VehiclesMenuWindow(); // Abre VehiclesMenuWindow
            vehiclesMenuWindow.setVisible(true);
        });
        add(backButton);

        // WindowListener para manejar el cierre de la ventana (Enfoque 2)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                VehiclesMenuWindow vehiclesMenuWindow = new VehiclesMenuWindow(); // Abre VehiclesMenuWindow
                vehiclesMenuWindow.setVisible(true);
            }
        });
    }

    private List<String> loadMarcas() {
        List<String> marcas = new ArrayList<>();
        String sql = "SELECT nombre_marca FROM marcas_vehiculos ORDER BY nombre_marca";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                marcas.add(resultSet.getString("nombre_marca"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las marcas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return marcas;
    }

    private MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
            formatter.setPlaceholderCharacter('_');
        } catch (java.text.ParseException exc) {
            System.err.println("Formato incorrecto: " + exc.getMessage());
        }
        return formatter;
    }

    private void registerNewVehicle() {
        String placa = placaField.getText().trim().toUpperCase();
        String marca = (String) marcaComboBox.getSelectedItem(); // Obtener la marca del JComboBox
        String modeloText = modeloField.getText().trim();
        String cedulaConductor = cedulaConductorField.getText().trim();
        String fechaSeguroText = fechaSeguroField.getText().trim().replace("_", "");
        String fechaTecnomecanicaText = fechaTecnomecanicaField.getText().trim().replace("_", "");

        // Validaciones básicas de campos obligatorios y formato de placa/modelo
        if (placa.length() != 6 || marca == null || marca.isEmpty() || modeloText.isEmpty() || fechaSeguroText.length() != 10 || fechaTecnomecanicaText.length() != 10) {
            JOptionPane.showMessageDialog(this, "Todos los campos obligatorios deben estar completos y con el formato correcto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!placa.matches("^[A-Z]{3}\\d{3}$")) {
            JOptionPane.showMessageDialog(this, "Formato de placa inválido. Debe ser 3 letras mayúsculas seguidas de 3 números.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!modeloText.matches("^\\d{4}$")) {
            JOptionPane.showMessageDialog(this, "El modelo debe ser un año de 4 dígitos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fechaSeguroLocalDate = null;
        LocalDate fechaTecnomecanicaLocalDate = null;
        LocalDate ahora = LocalDate.now();

        try {
            fechaSeguroLocalDate = LocalDate.parse(fechaSeguroText, dateFormatter);
            if (fechaSeguroLocalDate.isAfter(ahora)) {
                JOptionPane.showMessageDialog(this, "La fecha de seguro no puede ser posterior a la fecha actual.", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException e) {
            // Ya se validó en tiempo real, este catch es por si acaso
            JOptionPane.showMessageDialog(this, "Formato de fecha de seguro inválido.", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            fechaTecnomecanicaLocalDate = LocalDate.parse(fechaTecnomecanicaText, dateFormatter);
            if (fechaTecnomecanicaLocalDate.isAfter(ahora)) {
                JOptionPane.showMessageDialog(this, "La fecha de tecnomecánica no puede ser posterior a la fecha actual.", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException e) {
            // Ya se validó en tiempo real, este catch es por si acaso
            JOptionPane.showMessageDialog(this, "Formato de fecha de tecnomecánica inválido.", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO vehiculos (placa, marca, modelo, cedula_conductor, fecha_seguro, fecha_tecnomecanica) VALUES (?, ?, ?, ?, STR_TO_DATE(?, '%d/%m/%Y'), STR_TO_DATE(?, '%d/%m/%Y'))";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, placa);
            statement.setString(2, marca);
            statement.setInt(3, Integer.parseInt(modeloText));
            statement.setString(4, cedulaConductor.isEmpty() ? null : cedulaConductor); // Permitir valor nulo
            statement.setString(5, fechaSeguroText);
            statement.setString(6, fechaTecnomecanicaText);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Vehículo registrado correctamente.");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar el vehículo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void clearFields() {
        placaField.setText("");
        marcaComboBox.setSelectedIndex(-1); // Deseleccionar la marca
        modeloField.setText("");
        cedulaConductorField.setText("");
        fechaSeguroField.setText("");
        fechaTecnomecanicaField.setText("");
    }

    // Filtro para la placa (exactamente 3 letras mayúsculas y 3 números, pegados)
    private static class PlateNumberFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength()).toUpperCase();
            StringBuilder buffer = new StringBuilder(currentText);
            buffer.insert(offset, string.toUpperCase());
            String newText = buffer.toString();

            if (newText.matches("^[A-Z]{0,3}\\d{0,3}$") && newText.length() <= 6) {
                super.insertString(fb, offset, string.toUpperCase(), attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength()).toUpperCase();
            StringBuilder buffer = new StringBuilder(currentText);
            buffer.replace(offset, offset + length, text.toUpperCase());
            String newText = buffer.toString();

            if (newText.matches("^[A-Z]{0,3}\\d{0,3}$") && newText.length() <= 6) {
                super.replace(fb, offset, length, text.toUpperCase(), attrs);
            }
        }
    }

    // Filtro para solo números con longitud fija
    private static class FixedLengthNumberFilter extends DocumentFilter {
        private final int maxLength;

        public FixedLengthNumberFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string.matches("\\d*") && fb.getDocument().getLength() + string.length() <= maxLength) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text.matches("\\d*") && fb.getDocument().getLength() - length + text.length() <= maxLength) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    // Filtro para solo números
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

    // FocusListener para validar las fechas al perder el foco
    private class DateFocusListener implements FocusListener {
        private final JFormattedTextField textField;

        public DateFocusListener(JFormattedTextField textField) {
            this.textField = textField;
        }

        @Override
        public void focusGained(FocusEvent e) {
            // No necesitamos hacer nada al ganar el foco
        }

        @Override
        public void focusLost(FocusEvent e) {
            String dateText = textField.getText().replace("_", "");
            if (dateText.length() == 10) {
                try {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate date = LocalDate.parse(dateText, dateFormatter);
                    LocalDate now = LocalDate.now();
                    if (date.isAfter(now)) {
                        JOptionPane.showMessageDialog(RegisterVehicleWindow.this, "La fecha no puede ser posterior a la fecha actual.", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
                        textField.requestFocusInWindow(); // Devolver el foco al campo
                    } else {
                        int day = date.getDayOfMonth();
                        int month = date.getMonthValue();
                        if (day < 1 || day > date.lengthOfMonth() || month < 1 || month > 12) {
                            JOptionPane.showMessageDialog(RegisterVehicleWindow.this, "Fecha inválida.", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
                            textField.requestFocusInWindow(); // Devolver el foco al campo
                        }
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(RegisterVehicleWindow.this, "Formato de fecha inválido (DD/MM/AAAA).", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
                    textField.requestFocusInWindow(); // Devolver el foco al campo
                }
            } else if (!dateText.isEmpty()) {
                JOptionPane.showMessageDialog(RegisterVehicleWindow.this, "Formato de fecha incompleto (DD/MM/AAAA).", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
                textField.requestFocusInWindow(); // Devolver el foco al campo
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RegisterVehicleWindow registerVehicleWindow = new RegisterVehicleWindow();
            registerVehicleWindow.setVisible(true);
        });
    }
}