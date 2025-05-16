package taxiplus;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuWindow extends JFrame {

    public MainMenuWindow() {
        setTitle("Menú Principal - TaxiPlus");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JButton conductoresButton = new JButton("Conductores registrados");
        conductoresButton.setBounds(100, 50, 200, 30);
        add(conductoresButton);

        JButton registrarConductorButton = new JButton("Registrar conductor");
        registrarConductorButton.setBounds(100, 100, 200, 30);
        add(registrarConductorButton);

        JButton vehiculosButton = new JButton("Vehículos registrados");
        vehiculosButton.setBounds(100, 150, 200, 30);
        add(vehiculosButton);

        JButton registrarVehiculoButton = new JButton("Registrar vehículo");
        registrarVehiculoButton.setBounds(100, 200, 200, 30);
        add(registrarVehiculoButton);

        // Acción del botón "Registrar conductor"
        registrarConductorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cerrar el menú principal
                dispose();

                // Abrir la ventana de "Registrar Conductor"
                RegisterDriverWindow registerDriverWindow = new RegisterDriverWindow();
                registerDriverWindow.setVisible(true);
            }
        });

        // Ejemplo de otras acciones para los demás botones
        conductoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Mostrar conductores registrados");
            }
        });

        registrarVehiculoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Registrar un nuevo vehículo");
            }
        });

        vehiculosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Mostrar vehículos registrados");
            }
        });
    }
}