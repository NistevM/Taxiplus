package taxiplus;

public class Taxiplus {
    public static void main(String[] args) {
        // Iniciar el programa mostrando la ventana de Login
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}