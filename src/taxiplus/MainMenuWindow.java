package taxiplus;

import javax.swing.*;
import java.awt.*;

public class MainMenuWindow extends JFrame {

    public MainMenuWindow() {
        setTitle("Menú Principal - TaxiPlus");
        setSize(300, 300); // Aumentar un poco el tamaño para el nuevo botón
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));

        JButton conductoresButton = new JButton("Conductores");
        conductoresButton.setPreferredSize(new Dimension(200, 40));
        conductoresButton.addActionListener(e -> openSubMenu(new DriversMenuWindow()));
        add(conductoresButton);

        JButton vehiculosButton = new JButton("Vehículos");
        vehiculosButton.setPreferredSize(new Dimension(200, 40));
        vehiculosButton.addActionListener(e -> openSubMenu(new VehiclesMenuWindow()));
        add(vehiculosButton);

        JButton contabilidadButton = new JButton("Contabilidad");
        contabilidadButton.setPreferredSize(new Dimension(200, 40));
        contabilidadButton.addActionListener(e -> {
            dispose(); // Cierra el menú principal
            new AccountingMenuWindow().setVisible(true); // Abre la ventana de Contabilidad
        });
        add(contabilidadButton);

        // Botón de Logística
        JButton logisticaButton = new JButton("Logística");
        logisticaButton.setPreferredSize(new Dimension(200, 40));
        logisticaButton.addActionListener(e -> {
            dispose(); // Cierra el menú principal
            new LogisticsMenuWindow().setVisible(true); // Abre la nueva ventana de Logística
        });
        add(logisticaButton);
    }

    private void openSubMenu(JFrame subMenuWindow) {
        dispose();
        subMenuWindow.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenuWindow().setVisible(true));
    }
}

// Ventana para las opciones de Conductores
class DriversMenuWindow extends JFrame {
    public DriversMenuWindow() {
        setTitle("Conductores - TaxiPlus");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));

        JButton registrarConductorButton = new JButton("Registrar conductor");
        registrarConductorButton.setPreferredSize(new Dimension(250, 40));
        registrarConductorButton.addActionListener(e -> {
            dispose(); // Cierra la ventana actual de Conductores
            new RegisterDriverWindow().setVisible(true); // Abre la ventana de registro de conductor
        });
        add(registrarConductorButton);

        JButton conductoresRegistradosButton = new JButton("Conductores registrados");
        conductoresRegistradosButton.setPreferredSize(new Dimension(250, 40));
        conductoresRegistradosButton.addActionListener(e -> {
            dispose(); // Cierra la ventana actual de Conductores
            new RegisteredDriversWindow().setVisible(true);
        });
        add(conductoresRegistradosButton);

        JButton volverButton = new JButton("Volver al Menú Principal");
        volverButton.setPreferredSize(new Dimension(250, 40));
        volverButton.addActionListener(e -> {
            dispose();
            new MainMenuWindow().setVisible(true);
        });
        add(volverButton);
    }
}

// Ventana para las opciones de Vehículos
class VehiclesMenuWindow extends JFrame {
    public VehiclesMenuWindow() {
        setTitle("Vehículos - TaxiPlus");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));

        JButton registrarVehiculoButton = new JButton("Registrar vehículo");
        registrarVehiculoButton.setPreferredSize(new Dimension(250, 40));
        registrarVehiculoButton.addActionListener(e -> {
            dispose(); // Cierra la ventana actual de Vehículos
            // *** CORRECCIÓN AQUÍ: Descomentar la línea para abrir la ventana real ***
            new RegisterVehicleWindow().setVisible(true); // Abre la ventana de registro de vehículo
            // *** Eliminar el JOptionPane y el dispose() que estaban aquí ***
        });
        add(registrarVehiculoButton);

        JButton vehiculosRegistradosButton = new JButton("Vehículos registrados");
        vehiculosRegistradosButton.setPreferredSize(new Dimension(250, 40));
        vehiculosRegistradosButton.addActionListener(e -> {
            dispose(); // Cierra la ventana actual de Vehículos
            // *** CORRECCIÓN AQUÍ: Descomentar la línea para abrir la ventana real ***
            new RegisteredVehiclesWindow().setVisible(true); // Abre la ventana de vehículos registrados
            // *** Eliminar el JOptionPane y el dispose() que estaban aquí ***
        });
        add(vehiculosRegistradosButton);

        JButton volverButton = new JButton("Volver al Menú Principal");
        volverButton.setPreferredSize(new Dimension(250, 40));
        volverButton.addActionListener(e -> {
            dispose();
            new MainMenuWindow().setVisible(true);
        });
        add(volverButton);
    }
}