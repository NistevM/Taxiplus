package taxiplus;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder; // ¡Nuevo import para URLEncoder!
import java.io.UnsupportedEncodingException; // Nuevo import para manejar excepciones de codificación
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

// --- IMPORTS FALTANTES CORREGIDOS (confirmados de tu código) ---
import java.sql.ResultSet;
import java.io.IOException;

// Importaciones para GraphHopper
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class AddTripWindow extends JFrame {

    // --- Componentes del formulario ---
    private JComboBox<String> driverComboBox;
    private JTextField initialLocationField;
    private JTextField finalLocationField;
    private JLabel distanceLabel;
    private JLabel valorPorKmLabel;
    private JTextField valorPorKmField;
    private JLabel valorTotalLabel;
    private JFormattedTextField initialLatField;
    private JFormattedTextField initialLonField;
    private JFormattedTextField finalLatField;
    private JFormattedTextField finalLonField;

    private JButton calculateDistanceButton;
    private JButton saveTripButton;

    // --- Datos de la API de GraphHopper ---
    // ¡¡¡CLAVE API ACTUALIZADA CON LA DE TU DASHBOARD!!!
    private static final String GRAPHHOPPER_API_KEY = "c8690824-46aa-4e42-9670-45497e6fa66f";
    private OkHttpClient httpClient;

    // --- Variables para almacenar los valores calculados ---
    private double calculatedDistanceKm = 0.0;
    private double currentValorPorKm = 0.0;

    public AddTripWindow() {
        setTitle("Agregar Recorrido - TaxiPlus");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        httpClient = new OkHttpClient();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(formPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton backButton = new JButton("Volver Atrás");
        backButton.addActionListener(e -> {
            dispose();
            new LogisticsMenuWindow().setVisible(true);
        });
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(backButton);
        add(northPanel, BorderLayout.NORTH);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Conductor (Cédula):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        driverComboBox = new JComboBox<>();
        loadDriversIntoComboBox();
        formPanel.add(driverComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Ubicación Inicial:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        initialLocationField = new JTextField(30);
        formPanel.add(initialLocationField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Lat. Inicial (Calculada):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.5; gbc.anchor = GridBagConstraints.WEST;
        initialLatField = new JFormattedTextField(Double.valueOf(0.0));
        initialLatField.setEditable(false);
        formPanel.add(initialLatField, gbc);
        gbc.gridx = 2; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Lon. Inicial (Calculada):"), gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0.5; gbc.anchor = GridBagConstraints.WEST;
        initialLonField = new JFormattedTextField(Double.valueOf(0.0));
        initialLonField.setEditable(false);
        formPanel.add(initialLonField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Ubicación Final:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        finalLocationField = new JTextField(30);
        formPanel.add(finalLocationField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Lat. Final (Calculada):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.5; gbc.anchor = GridBagConstraints.WEST;
        finalLatField = new JFormattedTextField(Double.valueOf(0.0));
        finalLatField.setEditable(false);
        formPanel.add(finalLatField, gbc);
        gbc.gridx = 2; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Lon. Final (Calculada):"), gbc);
        gbc.gridx = 3; gbc.gridy = 4; gbc.weightx = 0.5; gbc.anchor = GridBagConstraints.WEST;
        finalLonField = new JFormattedTextField(Double.valueOf(0.0));
        finalLonField.setEditable(false);
        formPanel.add(finalLonField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        calculateDistanceButton = new JButton("Calcular Distancia (GraphHopper)");
        formPanel.add(calculateDistanceButton, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Distancia (Km):"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        distanceLabel = new JLabel("0.00");
        distanceLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        formPanel.add(distanceLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        valorPorKmLabel = new JLabel("Valor por Km:");
        formPanel.add(valorPorKmLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 7; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        valorPorKmField = new JTextField("0.00", 10);
        valorPorKmField.setHorizontalAlignment(JTextField.RIGHT);
        formPanel.add(valorPorKmField, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Valor Total:"), gbc);
        gbc.gridx = 1; gbc.gridy = 8; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        valorTotalLabel = new JLabel("0.00");
        valorTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valorTotalLabel.setForeground(Color.BLUE);
        formPanel.add(valorTotalLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        saveTripButton = new JButton("Guardar Recorrido");
        formPanel.add(saveTripButton, gbc);

        calculateDistanceButton.addActionListener(e -> calculateDistance());
        saveTripButton.addActionListener(e -> saveTrip());

        valorPorKmField.addActionListener(e -> updateValorTotal());
        valorPorKmField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateValorTotal();
            }
        });
        
        loadDefaultValorPorKm();
    }

    /**
     * Carga las cédulas de los conductores desde la base de datos
     * y las agrega al JComboBox.
     */
    private void loadDriversIntoComboBox() {
        driverComboBox.removeAllItems();
        String sql = "SELECT cedula, nombres, apellidos FROM Conductores ORDER BY nombres, apellidos";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String cedula = resultSet.getString("cedula");
                String nombres = resultSet.getString("nombres");
                String apellidos = resultSet.getString("apellidos");
                driverComboBox.addItem(cedula + " - " + nombres + " " + apellidos);
            }
            if (driverComboBox.getItemCount() > 0) {
                driverComboBox.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "No hay conductores registrados. Por favor, registre conductores primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                saveTripButton.setEnabled(false);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar conductores: " + ex.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Carga el valor predeterminado por KM desde la tabla 'Configuracion'
     * y lo establece en el campo de texto. Si no existe, usa 0.0.
     */
    private void loadDefaultValorPorKm() {
        String sql = "SELECT valor_por_km FROM Configuracion WHERE id = 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                currentValorPorKm = resultSet.getDouble("valor_por_km");
            } else {
                currentValorPorKm = 0.0;
            }
            valorPorKmField.setText(String.format("%.2f", currentValorPorKm));
            updateValorTotal();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar el valor por KM predeterminado: " + ex.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            currentValorPorKm = 0.0;
            valorPorKmField.setText("0.00");
            updateValorTotal();
        }
    }

    /**
     * Calcula la distancia entre las ubicaciones usando la API de GraphHopper
     * en un hilo de fondo para no bloquear la interfaz de usuario.
     */
    private void calculateDistance() {
        String initialLocation = initialLocationField.getText().trim();
        String finalLocation = finalLocationField.getText().trim();

        if (initialLocation.isEmpty() || finalLocation.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa tanto la ubicación inicial como la final.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        calculateDistanceButton.setEnabled(false);
        saveTripButton.setEnabled(false);
        distanceLabel.setText("Calculando...");
        valorTotalLabel.setText("Calculando...");

        // Usar SwingWorker para la operación de red en segundo plano
        new SwingWorker<Double[], Void>() {
            private String errorMessage = null;
            private double startLat, startLon, endLat, endLon;

            @Override
            protected Double[] doInBackground() throws Exception {
                String encodedInitialLocation;
                String encodedFinalLocation;

                try {
                    encodedInitialLocation = URLEncoder.encode(initialLocation, "UTF-8");
                    encodedFinalLocation = URLEncoder.encode(finalLocation, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    errorMessage = "Error de codificación de URL: " + e.getMessage();
                    e.printStackTrace();
                    return null;
                }

                String url = String.format(
                    "https://graphhopper.com/api/1/route?point=%s&point=%s&vehicle=car&locale=es&calc_points=true&instructions=false&elevation=false&key=%s",
                    encodedInitialLocation, // Usar la ubicación inicial codificada
                    encodedFinalLocation,   // Usar la ubicación final codificada
                    GRAPHHOPPER_API_KEY
                );

                System.out.println("URL COMPLETA ENVIADA A GRAPHHOPPER: " + url); // ¡Línea de depuración!

                Request request = new Request.Builder().url(url).build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        errorMessage = "Error en la API de GraphHopper: " + response.code() + " " + response.message();
                        if (response.body() != null) {
                            errorMessage += "\n" + response.body().string();
                        }
                        return null;
                    }

                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    // GraphHopper en su API de Routing no devuelve "hits" para geocodificación
                    // Si la ruta no se encuentra, busca en el "message" o en la ausencia de "paths"
                    if (jsonResponse.has("paths") && jsonResponse.getJSONArray("paths").length() > 0) {
                        JSONObject path = jsonResponse.getJSONArray("paths").getJSONObject(0);
                        double distanceMeters = path.getDouble("distance");
                        
                        if (path.has("points") && path.getJSONObject("points").has("coordinates")) {
                            JSONArray coordinates = path.getJSONObject("points").getJSONArray("coordinates");
                            if (coordinates.length() > 0) {
                                JSONArray startCoord = coordinates.getJSONArray(0);
                                startLon = startCoord.getDouble(0);
                                startLat = startCoord.getDouble(1);

                                JSONArray endCoord = coordinates.getJSONArray(coordinates.length() - 1);
                                endLon = endCoord.getDouble(0);
                                endLat = endCoord.getDouble(1);
                            }
                        }

                        return new Double[]{distanceMeters / 1000.0, startLat, startLon, endLat, endLon};
                    } else if (jsonResponse.has("message")) {
                        // Captura mensajes de error específicos de GraphHopper como "Cannot parse point"
                        errorMessage = "Error en la API: " + jsonResponse.getString("message");
                        return null;
                    } else {
                        errorMessage = "No se pudo calcular la ruta o la respuesta de la API es inesperada.";
                        return null;
                    }

                } catch (IOException e) {
                    errorMessage = "Error de conexión con la API de GraphHopper: " + e.getMessage();
                    e.printStackTrace();
                    return null;
                } catch (org.json.JSONException e) {
                    errorMessage = "Error al procesar la respuesta de la API: " + e.getMessage();
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done() {
                calculateDistanceButton.setEnabled(true);
                saveTripButton.setEnabled(true);

                try {
                    Double[] result = get();
                    if (result != null) {
                        calculatedDistanceKm = result[0];
                        initialLatField.setValue(result[1]);
                        initialLonField.setValue(result[2]);
                        finalLatField.setValue(result[3]);
                        finalLonField.setValue(result[4]);

                        distanceLabel.setText(String.format("%.2f", calculatedDistanceKm));
                        updateValorTotal();
                    } else {
                        JOptionPane.showMessageDialog(AddTripWindow.this, errorMessage, "Error de Cálculo de Distancia", JOptionPane.ERROR_MESSAGE);
                        distanceLabel.setText("Error");
                        valorTotalLabel.setText("Error");
                        calculatedDistanceKm = 0.0;
                        initialLatField.setValue(0.0);
                        initialLonField.setValue(0.0);
                        finalLatField.setValue(0.0);
                        finalLonField.setValue(0.0);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(AddTripWindow.this, "Error inesperado al calcular la distancia: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                    distanceLabel.setText("Error");
                    valorTotalLabel.setText("Error");
                    calculatedDistanceKm = 0.0;
                    initialLatField.setValue(0.0);
                    initialLonField.setValue(0.0);
                    finalLatField.setValue(0.0);
                    finalLonField.setValue(0.0);
                }
            }
        }.execute();
    }
    
    /**
     * Actualiza el valor total del recorrido basado en la distancia calculada y el valor por KM.
     */
    private void updateValorTotal() {
        try {
            currentValorPorKm = Double.parseDouble(valorPorKmField.getText().replace(",", "."));
            double valorTotal = calculatedDistanceKm * currentValorPorKm;
            valorTotalLabel.setText(String.format("%.2f", valorTotal));
        } catch (NumberFormatException e) {
            valorTotalLabel.setText("Inválido");
            JOptionPane.showMessageDialog(this, "Por favor, ingresa un número válido para 'Valor por Km'.", "Error de Formato", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Guarda el recorrido en la base de datos.
     */
    private void saveTrip() {
        if (driverComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un conductor.", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String selectedDriver = (String) driverComboBox.getSelectedItem();
        String cedulaConductor = selectedDriver.split(" - ")[0];

        String initialLocation = initialLocationField.getText().trim();
        String finalLocation = finalLocationField.getText().trim();

        if (initialLocation.isEmpty() || finalLocation.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Las ubicaciones inicial y final son obligatorias.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (calculatedDistanceKm <= 0) {
             JOptionPane.showMessageDialog(this, "Por favor, calcula la distancia antes de guardar el recorrido.", "Distancia Pendiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double valorTotal;
        try {
            valorTotal = Double.parseDouble(valorTotalLabel.getText().replace(",", "."));
            if (valorTotal < 0) {
                 JOptionPane.showMessageDialog(this, "El valor total no puede ser negativo. Revisa el valor por KM.", "Error de Cálculo", JOptionPane.WARNING_MESSAGE);
                 return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El valor total es inválido. Por favor, calcula la distancia y verifica el valor por KM.", "Error de Formato", JOptionPane.WARNING_MESSAGE);
            return;
        }


        String sql = "INSERT INTO recorridos (cedula_conductor, ubicacion_inicial, ubicacion_final, " +
                     "distancia_km, valor_por_km, valor_total, fecha_registro, " +
                     "lat_inicial, lon_inicial, lat_final, lon_final) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cedulaConductor);
            statement.setString(2, initialLocation);
            statement.setString(3, finalLocation);
            statement.setDouble(4, calculatedDistanceKm);
            statement.setDouble(5, currentValorPorKm);
            statement.setDouble(6, valorTotal);
            statement.setDate(7, java.sql.Date.valueOf(LocalDate.now()));
            statement.setDouble(8, (Double)initialLatField.getValue());
            statement.setDouble(9, (Double)initialLonField.getValue());
            statement.setDouble(10, (Double)finalLatField.getValue());
            statement.setDouble(11, (Double)finalLonField.getValue());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Recorrido guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Error: No se pudo guardar el recorrido.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el recorrido en la base de datos: " + ex.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Limpia todos los campos del formulario.
     */
    private void clearForm() {
        initialLocationField.setText("");
        finalLocationField.setText("");
        distanceLabel.setText("0.00");
        valorPorKmField.setText(String.format("%.2f", currentValorPorKm));
        valorTotalLabel.setText("0.00");
        initialLatField.setValue(0.0);
        initialLonField.setValue(0.0);
        finalLatField.setValue(0.0);
        finalLonField.setValue(0.0);
        calculatedDistanceKm = 0.0;
        if (driverComboBox.getItemCount() > 0) {
            driverComboBox.setSelectedIndex(0);
        }
    }
}