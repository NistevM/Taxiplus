package taxiplus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleSelectionWindow extends JDialog {

    private JTable vehiclesTable;
    private DefaultTableModel tableModel;
    private String driverId;
    private EditDriverWindow parentWindow;

    public VehicleSelectionWindow(JFrame parent, String driverId) {
        super(parent, "Seleccionar Vehículo - TaxiPlus", true);
        this.driverId = driverId;
        this.parentWindow = (EditDriverWindow) parent;
        setSize(500, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Modelo de la tabla
        String[] columnNames = {"Placa", "Marca", "Modelo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Las celdas no son editables
            }
        };
        vehiclesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(vehiclesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Botón "Seleccionar vehículo"
        JButton selectVehicleButton = new JButton("Seleccionar vehículo");
        selectVehicleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = vehiclesTable.getSelectedRow();
                if (selectedRow != -1) {
                    String selectedPlate = (String) tableModel.getValueAt(selectedRow, 0);
                    assignVehicleToDriver(selectedPlate);
                } else {
                    JOptionPane.showMessageDialog(VehicleSelectionWindow.this, "Por favor, selecciona un vehículo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(selectVehicleButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadAvailableVehicles();
    }

    private void loadAvailableVehicles() {
        tableModel.setRowCount(0); // Limpiar la tabla antes de cargar
        String sql = "SELECT placa, marca, modelo FROM vehiculos";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String placa = resultSet.getString("placa");
                String marca = resultSet.getString("marca");
                int modelo = resultSet.getInt("modelo");
                tableModel.addRow(new Object[]{placa, marca, modelo});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los vehículos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void assignVehicleToDriver(String vehiclePlate) {
        String checkSql = "SELECT cedula_conductor FROM vehiculos WHERE placa = ?";
        String updateSql = "UPDATE vehiculos SET cedula_conductor = ? WHERE placa = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            checkStatement.setString(1, vehiclePlate);
            ResultSet checkResult = checkStatement.executeQuery();

            if (checkResult.next()) {
                String existingDriver = checkResult.getString("cedula_conductor");
                if (existingDriver != null && !existingDriver.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El vehículo con placa " + vehiclePlate + " ya está asignado al conductor con cédula " + existingDriver + ".", "Vehículo Ocupado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            updateStatement.setString(1, driverId);
            updateStatement.setString(2, vehiclePlate);
            int rowsUpdated = updateStatement.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Vehículo con placa " + vehiclePlate + " asignado exitosamente al conductor con cédula " + driverId + ".", "Asignación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                parentWindow.loadDriverData(); // Recargar datos en la ventana de edición
                dispose(); // Cerrar la ventana de selección
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo asignar el vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al asignar el vehículo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}