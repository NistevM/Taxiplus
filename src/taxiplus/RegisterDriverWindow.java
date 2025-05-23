package taxiplus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

public class RegisterDriverWindow extends JFrame {
    private JTextField cedulaField, nombresField, apellidosField, telefonoField, nacimientoField, licenciaField;
    private JLabel photoLabel;
    private ImageIcon photo;
    private BufferedImage originalCapturedPhoto;
    private JButton takePhotoButton;

    public RegisterDriverWindow() {
        setTitle("Registrar Conductor - TaxiPlus");
        setSize(400, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cambiado a DISPOSE_ON_CLOSE
        setLocationRelativeTo(null);
        setLayout(null);

        JButton backButton = new JButton("Volver atrás");
        backButton.setBounds(10, 10, 120, 25);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new DriversMenuWindow().setVisible(true); // Regresa a DriversMenuWindow
            }
        });
        add(backButton);

        JLabel cedulaLabel = new JLabel("Cédula:");
        cedulaLabel.setBounds(30, 50, 100, 25);
        add(cedulaLabel);

        cedulaField = new JTextField();
        cedulaField.setBounds(150, 50, 200, 25);
        cedulaField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || cedulaField.getText().length() >= 10) {
                    e.consume(); // Solo números permitidos y máximo 10 dígitos
                }
            }
        });
        add(cedulaField);

        JLabel nombresLabel = new JLabel("Nombres:");
        nombresLabel.setBounds(30, 90, 100, 25);
        add(nombresLabel);

        nombresField = new JTextField();
        nombresField.setBounds(150, 90, 200, 25);
        nombresField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String text = nombresField.getText();
                if (!Character.isLetter(c) && c != ' ' || (c == ' ' && text.contains(" "))) {
                    e.consume();
                }
            }
        });
        add(nombresField);

        JLabel apellidosLabel = new JLabel("Apellidos:");
        apellidosLabel.setBounds(30, 130, 100, 25);
        add(apellidosLabel);

        apellidosField = new JTextField();
        apellidosField.setBounds(150, 130, 200, 25);
        apellidosField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String text = apellidosField.getText();
                if (!Character.isLetter(c) && c != ' ' || (c == ' ' && text.contains(" "))) {
                    e.consume();
                }
            }
        });
        add(apellidosField);

        JLabel telefonoLabel = new JLabel("Teléfono:");
        telefonoLabel.setBounds(30, 170, 100, 25);
        add(telefonoLabel);

        telefonoField = new JTextField();
        telefonoField.setBounds(150, 170, 200, 25);
        telefonoField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || telefonoField.getText().length() >= 10) {
                    e.consume(); // Solo números permitidos y máximo 10 dígitos
                }
            }
        });
        add(telefonoField);

        JLabel nacimientoLabel = new JLabel("Fecha de nacimiento (DD/MM/AAAA):");
        nacimientoLabel.setBounds(30, 210, 300, 25);
        add(nacimientoLabel);

        nacimientoField = new JTextField();
        nacimientoField.setBounds(30, 240, 320, 25);
        nacimientoField.addKeyListener(new DateValidationKeyListener(nacimientoField, 18, "El conductor debe tener al menos 18 años."));
        add(nacimientoField);

        JLabel licenciaLabel = new JLabel("Expedición licencia (DD/MM/AAAA):");
        licenciaLabel.setBounds(30, 280, 300, 25);
        add(licenciaLabel);

        licenciaField = new JTextField();
        licenciaField.setBounds(30, 310, 320, 25);
        licenciaField.addKeyListener(new DateValidationKeyListener(licenciaField, 16, "La fecha de expedición de licencia debe ser al menos 16 años después de la fecha de nacimiento.", nacimientoField));
        add(licenciaField);

        JLabel photoTitleLabel = new JLabel("Foto:");
        photoTitleLabel.setBounds(30, 350, 100, 25);
        add(photoTitleLabel);

        photoLabel = new JLabel();
        photoLabel.setBounds(150, 350, 100, 100);
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(photoLabel);

        takePhotoButton = new JButton("Tomar foto");
        takePhotoButton.setBounds(125, 460, 150, 30);
        takePhotoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                takePhotoButton.setEnabled(false);
                openCamera();
            }
        });
        add(takePhotoButton);

        JButton registerButton = new JButton("Registrar");
        registerButton.setBounds(30, 510, 320, 30);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateFields() && !isCedulaRegistered(cedulaField.getText())) {
                    saveToDatabase();
                }
            }
        });
        add(registerButton);
    }

    private boolean validateFields() {
        if (cedulaField.getText().isEmpty() || nombresField.getText().isEmpty() || apellidosField.getText().isEmpty() ||
                telefonoField.getText().isEmpty() || nacimientoField.getText().isEmpty() || licenciaField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return false;
        }
        return true;
    }

    private boolean isCedulaRegistered(String cedula) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM Conductores WHERE cedula = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, cedula);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "La cédula ingresada ya está registrada.");
                return true;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al validar la cédula: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    private void openCamera() {
        CameraWindow cameraWindow = new CameraWindow(new CameraWindow.PhotoCaptureListener() {
            @Override
            public void onPhotoCaptured(BufferedImage capturedPhoto) {
                originalCapturedPhoto = capturedPhoto;

                ImageIcon thumbnail = new ImageIcon(capturedPhoto.getScaledInstance(photoLabel.getWidth(), photoLabel.getHeight(), Image.SCALE_SMOOTH));
                photoLabel.setIcon(thumbnail);
                photo = thumbnail;

                takePhotoButton.setText("Tomar otra foto");
                takePhotoButton.setEnabled(true);
            }
        });

        cameraWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (photo == null) {
                    takePhotoButton.setEnabled(true);
                }
            }
        });

        cameraWindow.setVisible(true);
    }

    private void saveToDatabase() {
        String cedula = cedulaField.getText();
        String nombres = nombresField.getText();
        String apellidos = apellidosField.getText();
        String telefono = telefonoField.getText();
        String fechaNacimiento = nacimientoField.getText();
        String expedicionLicencia = licenciaField.getText();

        byte[] fotoBytes = null;
        if (originalCapturedPhoto != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(originalCapturedPhoto, "png", baos);
                baos.flush();
                fotoBytes = baos.toByteArray();
                baos.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al procesar la foto: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Conductores (cedula, nombres, apellidos, telefono, fecha_nacimiento, expedicion_licencia, foto) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, cedula);
            statement.setString(2, nombres);
            statement.setString(3, apellidos);
            statement.setString(4, telefono);
            statement.setDate(5, new java.sql.Date(sdf.parse(fechaNacimiento).getTime()));
            statement.setDate(6, new java.sql.Date(sdf.parse(expedicionLicencia).getTime()));
            statement.setBytes(7, fotoBytes);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Conductor registrado correctamente.");
                clearFields(); // Limpiar los campos después del registro
            }
        } catch (SQLException | ParseException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        cedulaField.setText("");
        nombresField.setText("");
        apellidosField.setText("");
        telefonoField.setText("");
        nacimientoField.setText("");
        licenciaField.setText("");
        photoLabel.setIcon(null);
        photo = null;
        originalCapturedPhoto = null;
        takePhotoButton.setText("Tomar foto");
        takePhotoButton.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterDriverWindow().setVisible(true));
    }

    private static class DateValidationKeyListener extends KeyAdapter {
        private final JTextField textField;
        private final int minYears;
        private final String errorMessage;
        private JTextField relatedField;

        public DateValidationKeyListener(JTextField textField, int minYears, String errorMessage) {
            this.textField = textField;
            this.minYears = minYears;
            this.errorMessage = errorMessage;
        }

        public DateValidationKeyListener(JTextField textField, int minYears, String errorMessage, JTextField relatedField) {
            this.textField = textField;
            this.minYears = minYears;
            this.errorMessage = errorMessage;
            this.relatedField = relatedField;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            String text = textField.getText();
            if (!Character.isDigit(c) || text.length() >= 10) {
                e.consume();
                return;
            }
            if (text.length() == 2 || text.length() == 5) {
                textField.setText(text + "/");
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            String text = textField.getText();
            if (text.length() == 10) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                try {
                    Date date = sdf.parse(text);
                    Date today = new Date();
                    long diffInMillis = today.getTime() - date.getTime();
                    long years = diffInMillis / (1000L * 60 * 60 * 24 * 365);

                    if (relatedField != null) {
                        String relatedDateText = relatedField.getText();
                        if (relatedDateText.length() == 10) {
                            Date relatedDate = sdf.parse(relatedDateText);
                            long diffBetweenDates = date.getTime() - relatedDate.getTime();
                            long relatedYears = diffBetweenDates / (1000L * 60 * 60 * 24 * 365);
                            if (relatedYears < minYears) {
                                JOptionPane.showMessageDialog(null, errorMessage);
                                textField.setText("");
                            }
                        }
                    } else if (years < minYears) {
                        JOptionPane.showMessageDialog(null, errorMessage);
                        textField.setText("");
                    }
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(null, "Fecha inválida. Use el formato DD/MM/AAAA.");
                    textField.setText("");
                }
            }
        }
    }
}