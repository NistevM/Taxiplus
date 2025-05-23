package taxiplus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogisticsMenuWindow extends JFrame {

    public LogisticsMenuWindow() {
        setTitle("Logística - TaxiPlus");
        setSize(400, 300); // Ajusta el tamaño según necesites
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15)); // Un layout simple para organizar botones

        // Botón "Agregar Recorrido"
        JButton addTripButton = new JButton("Agregar Recorrido");
        addTripButton.setPreferredSize(new Dimension(250, 40));
        addTripButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Cierra la ventana actual de Logística
                new AddTripWindow().setVisible(true); // Abre la ventana para agregar un recorrido
            }
        });
        add(addTripButton);

        // Botón "Ver Recorridos"
        JButton viewTripsButton = new JButton("Ver Recorridos");
        viewTripsButton.setPreferredSize(new Dimension(250, 40));
        viewTripsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Cierra la ventana actual de Logística
                // TODO: Implementar y abrir la ventana para ver recorridos
                JOptionPane.showMessageDialog(LogisticsMenuWindow.this, "Funcionalidad 'Ver Recorridos' no implementada aún.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        add(viewTripsButton);

        // Botón "Asignar Rutas" (Ejemplo, si tienes esta funcionalidad en mente)
        JButton assignRoutesButton = new JButton("Asignar Rutas");
        assignRoutesButton.setPreferredSize(new Dimension(250, 40));
        assignRoutesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Cierra la ventana actual de Logística
                // TODO: Implementar y abrir la ventana para asignar rutas
                JOptionPane.showMessageDialog(LogisticsMenuWindow.this, "Funcionalidad 'Asignar Rutas' no implementada aún.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        add(assignRoutesButton);

        // Botón "Volver al Menú Principal"
        JButton backButton = new JButton("Volver al Menú Principal");
        backButton.setPreferredSize(new Dimension(250, 40));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Cierra la ventana actual de Logística
                new MainMenuWindow().setVisible(true); // Regresa al Menú Principal
            }
        });
        add(backButton);
    }

    // Método main para probar solo esta ventana (opcional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LogisticsMenuWindow().setVisible(true);
        });
    }
}