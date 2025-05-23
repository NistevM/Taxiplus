package taxiplus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccountingMenuWindow extends JFrame {

    public AccountingMenuWindow() {
        setTitle("Contabilidad - TaxiPlus");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));

        JButton facturasCompraButton = new JButton("Facturas de Compra");
        facturasCompraButton.setPreferredSize(new Dimension(250, 40));
        facturasCompraButton.addActionListener(e -> {
            // Cierra la ventana de Contabilidad
            dispose();
            // Crea e inicializa la ventana de Facturas de Compra y la hace visible
            BuyInvoiceWindow buyInvoiceWindow = new BuyInvoiceWindow();
            buyInvoiceWindow.setVisible(true); // <--- ¡Esta línea es la clave para que se abra!
        });
        add(facturasCompraButton);

        JButton facturasVentaButton = new JButton("Facturas de Venta");
        facturasVentaButton.setPreferredSize(new Dimension(250, 40));
        facturasVentaButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(AccountingMenuWindow.this, "Módulo de Facturas de Venta en desarrollo.", "Información", JOptionPane.INFORMATION_MESSAGE);
        });
        add(facturasVentaButton);

        JButton remisionesButton = new JButton("Remisiones");
        remisionesButton.setPreferredSize(new Dimension(250, 40));
        remisionesButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(AccountingMenuWindow.this, "Módulo de Remisiones en desarrollo.", "Información", JOptionPane.INFORMATION_MESSAGE);
        });
        add(remisionesButton);

        JButton volverButton = new JButton("Volver al Menú Principal");
        volverButton.setPreferredSize(new Dimension(250, 40));
        volverButton.addActionListener(e -> {
            dispose();
            // Asumo que existe una clase MainMenuWindow que puede ser instanciada y hecha visible
            new MainMenuWindow().setVisible(true);
        });
        add(volverButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AccountingMenuWindow().setVisible(true));
    }
}