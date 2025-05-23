package taxiplus;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class BuyInvoiceWindow extends JFrame {

    private JFormattedTextField nitProveedorField;
    private JTextField razonSocialProveedorField;
    private JFormattedTextField fechaFacturaField;
    private JTextField prefijoField;
    private JFormattedTextField numeroField;
    private JComboBox<String> conceptoComboBox;
    private JTextField otroConceptoField;
    private JFormattedTextField cedulaConductorField;
    private JTextField nombreConductorField;
    private JFormattedTextField valorField;
    private JTextArea observacionesArea; // Campo de observaciones
    private JButton guardarButton;
    private List<String> conceptosList;

    // Cambiado a LENIENT para mayor tolerancia en el parseo de fechas.
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.LENIENT);

    public BuyInvoiceWindow() {
        setTitle("Registrar Factura de Compra - TaxiPlus");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        conceptosList = loadConceptos();

        // 1. Nit Proveedor (Implementación de solo números)
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("NIT Proveedor:"), gbc);
        try {
            // No usamos NumberFormatter para el NIT si solo queremos filtrar entrada de texto.
            // El DocumentFilter es más directo para esto.
            nitProveedorField = new JFormattedTextField(); // Cambiado a un JFormattedTextField básico
            nitProveedorField.setColumns(10);

            // DocumentFilter para permitir solo dÃ­gitos
            ((AbstractDocument) nitProveedorField.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    // Validar si el texto a insertar/reemplazar contiene solo dÃ­gitos
                    if (text == null) {
                        super.replace(fb, offset, length, text, attrs);
                        return;
                    }
                    StringBuilder sb = new StringBuilder(text.length());
                    for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (Character.isDigit(c)) {
                            sb.append(c);
                        }
                    }
                    super.replace(fb, offset, length, sb.toString(), attrs);
                }

                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if (string == null) {
                        super.insertString(fb, offset, string, attr);
                        return;
                    }
                    StringBuilder sb = new StringBuilder(string.length());
                    for (int i = 0; i < string.length(); i++) {
                        char c = string.charAt(i);
                        if (Character.isDigit(c)) {
                            sb.append(c);
                        }
                    }
                    super.insertString(fb, offset, sb.toString(), attr);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            nitProveedorField = new JFormattedTextField();
        }
        nitProveedorField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String nitLimpio = nitProveedorField.getText().trim();
                if (!nitLimpio.isEmpty()) {
                    buscarRazonSocialProveedor(nitLimpio);
                } else {
                    razonSocialProveedorField.setText("");
                }
            }
        });
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        add(nitProveedorField, gbc);

        // Razón Social Proveedor (No editable)
        gbc.gridx = 2;
        add(new JLabel("Razón Social:"), gbc);
        razonSocialProveedorField = new JTextField(25);
        razonSocialProveedorField.setEditable(false);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        add(razonSocialProveedorField, gbc);

        // 2. Fecha de Factura (JFormattedTextField con MaskFormatter)
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Fecha Factura (DD/MM/AAAA):"), gbc);
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            fechaFacturaField = new JFormattedTextField(dateMask);
            fechaFacturaField.setColumns(10);
            fechaFacturaField.setFocusLostBehavior(JFormattedTextField.COMMIT); // Asegura que el valor se comita al perder el foco
        } catch (ParseException e) {
            e.printStackTrace();
            fechaFacturaField = new JFormattedTextField();
        }
        fechaFacturaField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // No se realiza ninguna validaciÃ³n estricta aquÃ­.
                // La validación ahora se hace en el método guardarFactura() de forma más permisiva.
            }
        });
        gbc.gridx = 1;
        add(fechaFacturaField, gbc);

        // 3. Prefijo (más pequeño) y Número (solo números) en el mismo renglón
        gbc.gridx = 2;
        add(new JLabel("Prefijo:"), gbc);
        prefijoField = new JTextField(5);
        prefijoField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                prefijoField.setText(prefijoField.getText().toUpperCase());
            }
        });
        gbc.gridx = 3;
        add(prefijoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Número:"), gbc);
        try {
            NumberFormatter numFormatter = new NumberFormatter(new DecimalFormat("#"));
            numFormatter.setValueClass(Long.class);
            numFormatter.setAllowsInvalid(false);
            numFormatter.setCommitsOnValidEdit(true);
            numeroField = new JFormattedTextField(numFormatter);
            numeroField.setColumns(15);
        } catch (Exception e) {
            e.printStackTrace();
            numeroField = new JFormattedTextField();
        }
        gbc.gridx = 1;
        add(numeroField, gbc);

        // Concepto
        gbc.gridx = 2;
        gbc.gridy = 2;
        add(new JLabel("Concepto:"), gbc);
        String[] conceptosArray = conceptosList.toArray(new String[0]);
        conceptoComboBox = new JComboBox<>(conceptosArray);
        gbc.gridx = 3;
        add(conceptoComboBox, gbc);

        // 4. Otro Concepto (SIEMPRE disponible)
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Otro Concepto (Obs.):"), gbc);
        otroConceptoField = new JTextField(30);
        otroConceptoField.setEnabled(true); // Asegura que siempre esté habilitado
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        add(otroConceptoField, gbc);
        gbc.gridwidth = 1;

        // 5. Cédula Conductor Asociado (Más amplio, solo números)
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Cédula Conductor Asociado:"), gbc);
        try {
            NumberFormatter cedulaFormatter = new NumberFormatter(new DecimalFormat("#"));
            cedulaFormatter.setValueClass(Long.class);
            cedulaFormatter.setAllowsInvalid(false);
            cedulaFormatter.setCommitsOnValidEdit(true);
            cedulaConductorField = new JFormattedTextField(cedulaFormatter);
            cedulaConductorField.setColumns(10);
        } catch (Exception e) {
            e.printStackTrace();
            cedulaConductorField = new JFormattedTextField();
        }
        cedulaConductorField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String cedulaText = cedulaConductorField.getText().trim();
                if (!cedulaText.isEmpty()) {
                    if (cedulaText.matches("\\d+")) {
                        buscarNombreConductor(cedulaText);
                    } else {
                        nombreConductorField.setText("Cédula inválida");
                    }
                } else {
                    nombreConductorField.setText("");
                }
            }
        });
        gbc.gridx = 1;
        add(cedulaConductorField, gbc);

        // Nombre Conductor (No editable)
        gbc.gridx = 2;
        add(new JLabel("Nombre Conductor:"), gbc);
        nombreConductorField = new JTextField(25);
        nombreConductorField.setEditable(false);
        gbc.gridx = 3;
        add(nombreConductorField, gbc);

        // 6. Valor (Formato de moneda, con puntos de miles y comas decimales)
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(new JLabel("Valor:"), gbc);
        valorField = new JFormattedTextField(createCurrencyFormatter());
        valorField.setColumns(15);
        valorField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    valorField.commitEdit();
                    Object value = valorField.getValue();
                    if (value instanceof BigDecimal) {
                        BigDecimal bdValue = (BigDecimal) value;
                        if (bdValue.scale() <= 0) {
                             valorField.setText(createCurrencyFormatter().getFormat().format(bdValue));
                        }
                    } else if (value instanceof Number) {
                         BigDecimal bdValue = new BigDecimal(value.toString());
                         if (bdValue.scale() <= 0) {
                            valorField.setText(createCurrencyFormatter().getFormat().format(bdValue));
                         }
                    }
                } catch (ParseException ex) {
                    // El NumberFormatter ya maneja el error visualmente, aquí no hacemos más.
                }
            }
        });
        gbc.gridx = 1;
        add(valorField, gbc);

        // Observaciones (Mismo campo, solo se asegura que se guarde)
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(new JLabel("Observaciones:"), gbc);
        observacionesArea = new JTextArea(4, 30);
        JScrollPane observacionesScrollPane = new JScrollPane(observacionesArea);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;
        add(observacionesScrollPane, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;

        // Botón Guardar
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 4;
        guardarButton = new JButton("Guardar");
        guardarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarFactura();
            }
        });
        add(guardarButton, gbc);

        // Botón Volver
        gbc.gridy = 8;
        JButton volverButton = new JButton("Volver a Contabilidad");
        volverButton.addActionListener(e -> {
            dispose();
            new AccountingMenuWindow().setVisible(true);
        });
        add(volverButton, gbc);

        setVisible(true);
    }

    private List<String> loadConceptos() {
        List<String> conceptos = new ArrayList<>();
        String sql = "SELECT nombre_concepto FROM conceptos_factura_compra ORDER BY nombre_concepto";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                conceptos.add(resultSet.getString("nombre_concepto"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los conceptos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return conceptos;
    }

    private void buscarRazonSocialProveedor(String nit) {
        String sql = "SELECT razon_social FROM proveedores WHERE nit = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nit);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                razonSocialProveedorField.setText(resultSet.getString("razon_social"));
            } else {
                razonSocialProveedorField.setText("NIT no encontrado");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar proveedor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void buscarNombreConductor(String cedula) {
        String sql = "SELECT nombres, apellidos FROM conductores WHERE cedula = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cedula);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                nombreConductorField.setText(resultSet.getString("nombres") + " " + resultSet.getString("apellidos"));
            } else {
                nombreConductorField.setText("Conductor no encontrado");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar conductor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private NumberFormatter createCurrencyFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat currencyFormat = new DecimalFormat("#,##0.00", symbols);
        currencyFormat.setParseBigDecimal(true);

        NumberFormatter formatter = new NumberFormatter(currencyFormat);
        formatter.setValueClass(BigDecimal.class);
        formatter.setAllowsInvalid(false);
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);
        return formatter;
    }

    private void guardarFactura() {
        // 1. Validar NIT Proveedor
        String nitProveedor = nitProveedorField.getText().trim();
        if (nitProveedor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El NIT de proveedor es obligatorio.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            nitProveedorField.requestFocusInWindow();
            return;
        }
        // La validación de que solo contenga dígitos ya la maneja el DocumentFilter en el constructor.
        // Pero se deja una validación de seguridad aquí por si acaso, aunque no debería ser necesaria.
        if (!nitProveedor.matches("\\d+")) { 
             JOptionPane.showMessageDialog(this, "El NIT de proveedor solo puede contener dígitos numéricos.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
             nitProveedorField.requestFocusInWindow();
             return;
        }

        // 2. Procesar Fecha de Factura (SIN REGLAS DE VALIDACIÓN ESTRICTAS)
        LocalDate fechaFactura = null;
        String fechaText = fechaFacturaField.getText().trim(); // Obtener el texto del JFormattedTextField
        fechaText = fechaText.replace("_", ""); // Limpiar los placeholders

        if (fechaText.isEmpty()) {
            // Si el campo de fecha está vacío después de limpiar los placeholders, se permite,
            // y se insertará NULL en la base de datos para este campo.
            fechaFactura = null;
        } else {
            try {
                // Intentar parsear la fecha con el formatter LENIENT.
                // Esto permite fechas como 31/02/2024 (se ajustaría a 02/03/2024)
                // y es más tolerante con formatos que no sean exactamente DD/MM/AAAA
                fechaFactura = LocalDate.parse(fechaText, dateFormatter); 
            } catch (DateTimeParseException e) {
                // Si el parseo falla incluso con LENIENT, significa que el formato es inaceptable
                JOptionPane.showMessageDialog(this, "La fecha ingresada '" + fechaText + "' no tiene un formato de fecha válido (DD/MM/AAAA). Por favor, corrija la fecha.", "Error de Formato de Fecha", JOptionPane.ERROR_MESSAGE);
                fechaFacturaField.requestFocusInWindow();
                return;
            }
        }
        
        // 3. Validar Número de Factura
        String prefijo = prefijoField.getText().trim().toUpperCase();
        String numeroStr = numeroField.getText().trim();
        if (numeroStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El Número de Factura es obligatorio.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            numeroField.requestFocusInWindow();
            return;
        }
        if (!numeroStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "El Número de Factura solo puede contener dígitos numéricos.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            numeroField.requestFocusInWindow();
            return;
        }
        
        // 4. Validar "Otro Concepto" si "Otro" está seleccionado en el ComboBox
        String conceptoSeleccionado = (String) conceptoComboBox.getSelectedItem();
        String otroConceptoObservacion = otroConceptoField.getText().trim();
        
        if (conceptoSeleccionado.equals("Otro") && otroConceptoObservacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar una observación para el concepto 'Otro'.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            otroConceptoField.requestFocusInWindow();
            return;
        }


        // 5. Validar Cédula de Conductor Asociado (solo si no está vacía)
        String cedulaConductorAsociado = cedulaConductorField.getText().trim();
        if (!cedulaConductorAsociado.isEmpty() && !cedulaConductorAsociado.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "La Cédula de Conductor Asociado solo puede contener dígitos numéricos.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            cedulaConductorField.requestFocusInWindow();
            return;
        }

        // 6. Validar Valor
        BigDecimal valor = null;
        try {
            valorField.commitEdit();
            Object val = valorField.getValue();
            if (val instanceof BigDecimal) {
                valor = (BigDecimal) val;
            } else if (val instanceof Number) {
                valor = new BigDecimal(val.toString());
            }

            if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "El campo Valor es obligatorio y debe ser un número positivo.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                valorField.requestFocusInWindow();
                return;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de valor inválido. Asegúrese de usar el formato correcto (ej: 1.000,00).", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            valorField.requestFocusInWindow();
            return;
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Error inesperado al procesar el valor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            valorField.requestFocusInWindow();
            return;
        }

        // Obtener el texto de las observaciones (se guarda incluso si está vacío)
        String observaciones = observacionesArea.getText().trim(); // MODIFICADO: Obtener el texto del JTextArea

        int idConcepto = -1;

        String sqlConcepto = "SELECT id_concepto FROM conceptos_factura_compra WHERE nombre_concepto = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlConcepto)) {
            statement.setString(1, conceptoSeleccionado);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                idConcepto = resultSet.getInt("id_concepto");
            } else {
                JOptionPane.showMessageDialog(this, "Error: Concepto seleccionado no encontrado en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al consultar el ID del concepto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        String sqlInsert = "INSERT INTO facturas_compra (" +
                           "nit_proveedor, fecha_factura, prefijo, numero, id_concepto, " +
                           "otro_concepto_observacion, cedula_conductor_asociado, valor, observaciones" +
                           ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlInsert)) {
            
            statement.setString(1, nitProveedor);
            
            // Si fechaFactura es null (campo vacío o parseo fallido), se envía null a la DB.
            if (fechaFactura != null) {
                statement.setDate(2, java.sql.Date.valueOf(fechaFactura)); 
            } else {
                statement.setNull(2, java.sql.Types.DATE); 
            }

            statement.setString(3, prefijo.isEmpty() ? null : prefijo);
            statement.setString(4, numeroStr);
            statement.setInt(5, idConcepto);
            
            statement.setString(6, otroConceptoObservacion.isEmpty() ? null : otroConceptoObservacion);
            
            statement.setString(7, cedulaConductorAsociado.isEmpty() ? null : cedulaConductorAsociado);
            statement.setBigDecimal(8, valor);
            // CAMBIO CLAVE PARA OBSERVACIONES:
            // Asegura que se pasa el valor de observaciones al PreparedStatement.
            // Si está vacío, se inserta NULL, de lo contrario, el texto.
            statement.setString(9, observaciones.isEmpty() ? null : observaciones); 

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Factura de compra guardada correctamente.");
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar la factura de compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar la factura: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void limpiarFormulario() {
        nitProveedorField.setText("");
        razonSocialProveedorField.setText("");
        fechaFacturaField.setValue(null); 
        fechaFacturaField.setText(""); // IMPORTANTE: Resetea los placeholders del MaskFormatter
        prefijoField.setText("");
        numeroField.setText("");
        conceptoComboBox.setSelectedIndex(0);
        otroConceptoField.setText("");
        cedulaConductorField.setText("");
        nombreConductorField.setText("");
        valorField.setValue(null);
        observacionesArea.setText(""); // Limpia el JTextArea
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BuyInvoiceWindow buyInvoiceWindow = new BuyInvoiceWindow();
        });
    }
}