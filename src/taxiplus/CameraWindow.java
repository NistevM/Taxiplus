package taxiplus;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import javax.imageio.ImageIO;
import org.opencv.videoio.Videoio;

public class CameraWindow extends JFrame {
    public interface PhotoCaptureListener {
        void onPhotoCaptured(BufferedImage photo);
    }

    private PhotoCaptureListener listener;
    private VideoCapture webcam;
    private JLabel videoLabel;
    private boolean isRunning;

    public CameraWindow(PhotoCaptureListener listener) {
        this.listener = listener;
        setTitle("Cámara");
        setSize(650, 535);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        videoLabel = new JLabel();
        add(videoLabel, BorderLayout.CENTER);

        JButton captureButton = new JButton("Tomar foto");
        captureButton.addActionListener(e -> capturePhoto());
        add(captureButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // Centrar la ventana en la pantalla

        initCamera();
    }

    private void initCamera() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Cargar la biblioteca nativa de OpenCV
        webcam = new VideoCapture(0); // Abrir la webcam (0 para la cámara predeterminada)
        if (!webcam.isOpened()) {
            JOptionPane.showMessageDialog(this, "No se pudo acceder a la cámara.");
            System.err.println("Error: No se pudo abrir la webcam.");
            dispose();
            return;
        }

        // Configurar resolución alta
        webcam.set(Videoio.CAP_PROP_FRAME_WIDTH, 800);
        webcam.set(Videoio.CAP_PROP_FRAME_HEIGHT, 600);

        isRunning = true;
        Thread videoFeedThread = new Thread(() -> {
            Mat frame = new Mat();
            while (isRunning) {
                if (webcam.read(frame)) {
                    ImageIcon icon = new ImageIcon(matToBufferedImage(frame));
                    videoLabel.setIcon(icon);
                } else {
                    System.err.println("Error: No se pudo leer el frame de la webcam.");
                }
            }
            frame.release();
        });
        videoFeedThread.start();
    }

    private void capturePhoto() {
        Mat frame = new Mat();
        if (webcam.read(frame)) {
            BufferedImage photo = matToBufferedImage(frame);
            listener.onPhotoCaptured(photo); // Notificar al listener con la foto tomada
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo tomar la foto.");
            System.err.println("Error: No se pudo capturar el frame de la webcam.");
        }
        dispose(); // Cerrar la ventana después de tomar la foto
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob); // Cambiado a PNG para evitar pérdida de calidad
        byte[] byteArray = mob.toArray();

        try {
            return ImageIO.read(new java.io.ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void dispose() {
        isRunning = false;
        if (webcam != null && webcam.isOpened()) {
            webcam.release(); // Liberar la cámara
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CameraWindow(photo -> {
                JOptionPane.showMessageDialog(null, "¡Foto tomada!");
            }).setVisible(true);
        });
    }
}