package taxiplus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegisteredVehiclesWindow extends JFrame {

    private JTable vehiclesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchFilterComboBox;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton searchButton;
    private JButton backButton;
    private JButton viewDriverButton;

    public RegisteredVehiclesWindow() {
        setTitle("Vehículos Registrados - TaxiPlus");
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel superior para el botón "Volver atrás" y la búsqueda
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = new JButton("Volver atrás");
        backButton.addActionListener(e -> {
            dispose();
            VehiclesMenuWindow vehiclesMenuWindow = new VehiclesMenuWindow();
            vehiclesMenuWindow.setVisible(true);
        });
        topPanel.add(backButton);

        JLabel searchLabel = new JLabel("Buscar por:");
        searchFilterComboBox = new JComboBox<>(new String[]{"Todos los campos", "Placa", "Marca", "Modelo", "Cédula Conductor"});
        searchField = new JTextField(20);
        searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> filterTable());

        topPanel.add(searchLabel);
        topPanel.add(searchFilterComboBox);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        // Modelo de la tabla
        tableModel = new DefaultTableModel();
        vehiclesTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        vehiclesTable.setRowSorter(sorter);

        // Añadir las columnas
        tableModel.addColumn("Placa");
        tableModel.addColumn("Marca");
        tableModel.addColumn("Modelo");
        tableModel.addColumn("Cédula Conductor");
        tableModel.addColumn("Fecha Seguro");
        tableModel.addColumn("Fecha Tecnomecánica");

        // Cargar los datos de la base de datos
        loadVehiclesData(tableModel);

        // Añadir la tabla a un JScrollPane
        JScrollPane scrollPane = new JScrollPane(vehiclesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior para el botón "Ver conductor asignado"
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewDriverButton = new JButton("Ver conductor asignado");
        viewDriverButton.addActionListener(e -> showAssignedDriver());
        bottomPanel.add(viewDriverButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadVehiclesData(DefaultTableModel model) {
        String sql = "SELECT placa, marca, modelo, cedula_conductor, DATE_FORMAT(fecha_seguro, '%d/%m/%Y') AS fecha_seguro, DATE_FORMAT(fecha_tecnomecanica, '%d/%m/%Y') AS fecha_tecnomecanica FROM vehiculos";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String placa = resultSet.getString("placa");
                String marca = resultSet.getString("marca");
                int modelo = resultSet.getInt("modelo");
                String cedulaConductor = resultSet.getString("cedula_conductor");
                String fechaSeguro = resultSet.getString("fecha_seguro");
                String fechaTecnomecanica = resultSet.getString("fecha_tecnomecanica");

                model.addRow(new Object[]{placa, marca, modelo, cedulaConductor, fechaSeguro, fechaTecnomecanica});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de los vehículos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterTable() {
        String text = searchField.getText().toLowerCase();
        int selectedFilter = searchFilterComboBox.getSelectedIndex();
        RowFilter<DefaultTableModel, Object> rf = null;

        try {
            if (selectedFilter == 0) { // Buscar en todos los campos
                rf = RowFilter.regexFilter(text);
            } else if (selectedFilter == 1) { // Placa
                rf = RowFilter.regexFilter(text, 0);
            } else if (selectedFilter == 2) { // Marca
                rf = RowFilter.regexFilter(text, 1);
            } else if (selectedFilter == 3) { // Modelo
                rf = RowFilter.regexFilter(text, 2);
            } else if (selectedFilter == 4) { // Cédula Conductor
                rf = RowFilter.regexFilter(text, 3);
            }
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(RowFilter.regexFilter("(?i)" + text, selectedFilter == 0 ? new int[]{0, 1, 2, 3} : new int[]{selectedFilter - 1}))));
    }

    private void showAssignedDriver() {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un vehículo para ver el conductor asignado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cedulaConductor = (String) tableModel.getValueAt(vehiclesTable.convertRowIndexToModel(selectedRow), 3);

        if (cedulaConductor == null || cedulaConductor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Este vehículo no tiene un conductor asignado.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Consultar la base de datos para obtener la información del conductor
        String sql = "SELECT nombres, apellidos, fecha_nacimiento, expedicion_licencia, telefono FROM conductores WHERE cedula = ?";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cedulaConductor);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String nombres = resultSet.getString("nombres");
                String apellidos = resultSet.getString("apellidos");
                LocalDate fechaNacimiento = resultSet.getDate("fecha_nacimiento").toLocalDate();
                LocalDate expedicionLicencia = resultSet.getDate("expedicion_licencia").toLocalDate();
                String telefono = resultSet.getString("telefono");

                String driverInfo = "Cédula: " + cedulaConductor + "\n" +
                                    "Nombres: " + nombres + "\n" +
                                    "Apellidos: " + apellidos + "\n" +
                                    "Fecha de Nacimiento: " + fechaNacimiento.format(dateFormatter) + "\n" +
                                    "Expedición de Licencia: " + expedicionLicencia.format(dateFormatter) + "\n" +
                                    "Teléfono: " + telefono;

                JOptionPane.showMessageDialog(this, driverInfo, "Información del Conductor", JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this, "No se encontró información para la cédula del conductor: " + cedulaConductor, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener la información del conductor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RegisteredVehiclesWindow registeredVehiclesWindow = new RegisteredVehiclesWindow();
            registeredVehiclesWindow.setVisible(true);
        });
    }
}