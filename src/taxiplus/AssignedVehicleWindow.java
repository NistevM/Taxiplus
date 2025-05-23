package taxiplus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AssignedVehicleWindow extends JDialog { // Cambiado de JFrame a JDialog

    private JLabel placaLabel;
    private JLabel marcaLabel;
    private JLabel modeloLabel;
    private JLabel conductorLabel;
    private JLabel seguroLabel;
    private JLabel tecnomecanicaLabel;

    public AssignedVehicleWindow(JFrame parent, String driverId) { // Recibe un JFrame padre
        super(parent, "Vehículo Asignado - TaxiPlus", true); // Es un diálogo modal
        setSize(400, 250); // Aumenté el tamaño de la ventana
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // No hacer nada al cerrar
        setLocationRelativeTo(parent); // Centrar relativo a la ventana padre
        setLayout(new GridLayout(7, 1, 10, 10));

        JLabel titleLabel = new JLabel("Información del Vehículo Asignado");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);

        placaLabel = new JLabel("Placa: Cargando...");
        add(placaLabel);

        marcaLabel = new JLabel("Marca: Cargando...");
        add(marcaLabel);

        modeloLabel = new JLabel("Modelo: Cargando...");
        add(modeloLabel);

        conductorLabel = new JLabel("Cédula Conductor: " + driverId);
        add(conductorLabel);

        seguroLabel = new JLabel("Seguro: Cargando...");
        add(seguroLabel);

        tecnomecanicaLabel = new JLabel("Tecnomecánica: Cargando...");
        add(tecnomecanicaLabel);

        loadAssignedVehicle(driverId);

        // Añadir un WindowListener para manejar el cierre desde el botón "X"
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose(); // Simplemente cierra el diálogo
            }
        });
    }

    private void loadAssignedVehicle(String driverId) {
        String sql = "SELECT v.placa, v.marca, v.modelo, v.fecha_seguro, v.fecha_tecnomecanica " +
                     "FROM vehiculos v WHERE v.cedula_conductor = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, driverId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String placa = resultSet.getString("placa");
                String marca = resultSet.getString("marca");
                int modelo = resultSet.getInt("modelo");
                String seguro = resultSet.getString("fecha_seguro");
                String tecnomecanica = resultSet.getString("fecha_tecnomecanica");

                placaLabel.setText("Placa: " + placa);
                marcaLabel.setText("Marca: " + marca);
                modeloLabel.setText("Modelo: " + modelo);
                seguroLabel.setText("Seguro: " + (seguro != null ? seguro : "No registrado"));
                tecnomecanicaLabel.setText("Tecnomecánica: " + (tecnomecanica != null ? tecnomecanica : "No registrado"));
            } else {
                placaLabel.setText("No se encontró vehículo");
                marcaLabel.setText("");
                modeloLabel.setText("");
                seguroLabel.setText("");
                tecnomecanicaLabel.setText("");
            }

        } catch (SQLException e) {
            placaLabel.setText("Error al cargar la información");
            marcaLabel.setText("");
            modeloLabel.setText("");
            seguroLabel.setText("");
            tecnomecanicaLabel.setText("");
            e.printStackTrace();
        }
    }
}