import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * =============================================================
 *  CLASE: DistribucionNormalApp   ← CLASE PRINCIPAL (contiene main)
 * =============================================================
 *  Propósito:
 *    Ventana principal de la aplicación. Coordina las otras
 *    tres clases y sirve como punto de entrada del programa.
 *
 *  Arquitectura MVC simplificada:
 *    DistribucionNormalModel  → Modelo  (matemática pura)
 *    GraficaPanel             → Vista 1 (curva + área sombreada)
 *    ResultadosPanel          → Vista 2 (desarrollo paso a paso)
 *    DistribucionNormalApp    → Controlador + ventana principal
 *
 *  Casos prácticos precargados (diapositivas 8–19):
 *    1. Examen          X ~ N(60, 10²)  — diap. 8
 *    2. Control calidad X ~ N(50, 0.2²) — diap. 10–11
 *    3. Salud pública   X ~ N(120, 15²) — diap. 12–13
 *    4. Educación       X ~ N(500, 100²)— diap. 14–15
 *    5. Negocios        X ~ N(48, 5²)   — diap. 16–17
 *    6. Contabilidad    X ~ N(200, 25²) — diap. 18–19
 *    7. Personalizado   (el usuario ingresa sus propios valores)
 *
 *  Para ejecutar:
 *    javac *.java
 *    java DistribucionNormalApp
 * =============================================================
 */
public class DistribucionNormalApp extends JFrame {

    // ── Paleta de colores del tema oscuro ────────────────────
    static final Color BG        = new Color(10,  14,  26);
    static final Color PANEL_BG  = new Color(18,  24,  42);
    static final Color ACCENT    = new Color(99, 179, 237);
    static final Color ACCENT2   = new Color(72, 209, 164);
    static final Color TEXT_PRI  = new Color(226, 232, 245);
    static final Color TEXT_SEC  = new Color(130, 145, 175);
    static final Color INPUT_BG  = new Color(28,  38,  60);
    static final Color BORDER_C  = new Color(50,  65, 100);

    // ── Definición de los casos prácticos ───────────────────
    // Cada caso: { nombre, μ, σ, x1, x2, tipo }
    // tipo: 0=cola derecha, 1=acumulada, 2=intervalo
    private static final Object[][] CASOS = {
        // Nombre                       μ       σ      x1     x2     tipo
        {"Examen (diap. 8)",           60.0,   10.0,  75.0,  0.0,   GraficaPanel.COLA_DERECHA},
        {"Control de calidad (d.10)", 50.0,   0.2,   50.5,  0.0,   GraficaPanel.ACUMULADA},
        {"Salud pública (diap. 12)",  120.0,  15.0,  150.0, 0.0,   GraficaPanel.COLA_DERECHA},
        {"Educación (diap. 14)",      500.0,  100.0, 650.0, 0.0,   GraficaPanel.COLA_DERECHA},
        {"Negocios (diap. 16)",        48.0,   5.0,   55.0,  0.0,   GraficaPanel.COLA_DERECHA},
        {"Contabilidad (diap. 18)",   200.0,  25.0,  180.0, 230.0, GraficaPanel.INTERVALO},
        {"Personalizado",              0.0,    1.0,   0.0,   0.0,   GraficaPanel.COLA_DERECHA}
    };

    // ── Controles de entrada ─────────────────────────────────
    private JTextField  tfMedia, tfDesv, tfX1, tfX2;
    private JComboBox<String> cbTipo;
    private JComboBox<String> cbCaso;   // selector de caso práctico

    // ── Paneles de visualización ─────────────────────────────
    private GraficaPanel    graficaPanel;
    private ResultadosPanel resultadosPanel;

    // ── Caso actualmente seleccionado ────────────────────────
    private String nombreCasoActual = "Personalizado";

    // ==========================================================
    //  CONSTRUCTOR
    // ==========================================================

    public DistribucionNormalApp() {
        super("Distribución Normal — Ingeniería de Sistemas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 740);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(crearEncabezado(),   BorderLayout.NORTH);
        add(crearCuerpo(),       BorderLayout.CENTER);
        add(crearPie(),          BorderLayout.SOUTH);

        // Cargar el primer caso práctico al iniciar
        cargarCaso(0);
    }

    // ==========================================================
    //  CONSTRUCCIÓN DE LA INTERFAZ
    // ==========================================================

    /**
     * Barra superior: título del programa y subtítulo del caso.
     */
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PANEL_BG);
        p.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
        p.setPreferredSize(new Dimension(0, 68));

        // Título principal
        JLabel titulo = new JLabel("  📊  Distribución Normal Aplicada");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 20));
        titulo.setForeground(ACCENT);

        // Subtítulo con la notación
        JLabel sub = new JLabel("  Ingeniería de Sistemas — Sesión 13");
        sub.setFont(new Font("Monospaced", Font.PLAIN, 11));
        sub.setForeground(TEXT_SEC);

        JPanel izq = new JPanel(new GridLayout(2, 1));
        izq.setOpaque(false);
        izq.add(titulo);
        izq.add(sub);

        // Notación a la derecha
        JLabel der = new JLabel("X ~ N(μ, σ²)   |   Z = (X−μ)/σ   |   P(Z>z) = 1−Φ(z)  ");
        der.setFont(new Font("Monospaced", Font.PLAIN, 11));
        der.setForeground(TEXT_SEC);

        p.add(izq, BorderLayout.WEST);
        p.add(der, BorderLayout.EAST);
        return p;
    }

    /**
     * Cuerpo principal: panel de controles (izquierda) +
     * gráfica y resultados (derecha).
     */
    private JSplitPane crearCuerpo() {
        graficaPanel    = new GraficaPanel();
        resultadosPanel = new ResultadosPanel();

        // Dividir gráfica y resultados verticalmente
        JSplitPane derecho = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                graficaPanel, resultadosPanel);
        derecho.setDividerLocation(345);
        derecho.setDividerSize(4);
        derecho.setBorder(null);

        // Dividir controles y panel derecho horizontalmente
        JSplitPane cuerpo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                crearPanelControles(), derecho);
        cuerpo.setDividerLocation(305);
        cuerpo.setDividerSize(4);
        cuerpo.setBorder(null);
        return cuerpo;
    }

    /**
     * Panel izquierdo: selector de caso, parámetros y botón calcular.
     */
    private JPanel crearPanelControles() {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(18, 14, 18, 14));

        // ── Selector de caso práctico ────────────────────────
        p.add(etiquetaSeccion("CASOS PRÁCTICOS (Diapositivas)"));
        p.add(Box.createVerticalStrut(7));

        // Construir lista de nombres para el combo
        String[] nombres = new String[CASOS.length];
        for (int i = 0; i < CASOS.length; i++)
            nombres[i] = (String) CASOS[i][0];

        cbCaso = new JComboBox<>(nombres);
        estilizarCombo(cbCaso);
        cbCaso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        // Al cambiar de caso, actualizar los campos automáticamente
        cbCaso.addActionListener(e -> cargarCaso(cbCaso.getSelectedIndex()));
        p.add(cbCaso);

        // ── Parámetros de la distribución ───────────────────
        p.add(Box.createVerticalStrut(16));
        p.add(etiquetaSeccion("PARÁMETROS  X ~ N(μ, σ²)"));
        p.add(Box.createVerticalStrut(8));

        tfMedia = new JTextField("60");
        tfDesv  = new JTextField("10");
        p.add(fila("Media  μ :", tfMedia));
        p.add(Box.createVerticalStrut(6));
        p.add(fila("Desv. σ :", tfDesv));

        // ── Tipo de probabilidad ─────────────────────────────
        p.add(Box.createVerticalStrut(16));
        p.add(etiquetaSeccion("TIPO DE PROBABILIDAD"));
        p.add(Box.createVerticalStrut(8));

        cbTipo = new JComboBox<>(new String[]{
            "P(X > x₁)  — Cola derecha",   // COLA_DERECHA = 0
            "P(X < x₁)  — Acumulada",      // ACUMULADA    = 1
            "P(x₁<X<x₂) — Intervalo"       // INTERVALO    = 2
        });
        estilizarCombo(cbTipo);
        cbTipo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(cbTipo);

        // ── Valores límite ───────────────────────────────────
        p.add(Box.createVerticalStrut(10));
        tfX1 = new JTextField("75");
        tfX2 = new JTextField("0");
        p.add(fila("Valor  x₁ :", tfX1));
        p.add(Box.createVerticalStrut(6));
        p.add(fila("Valor  x₂ :", tfX2));

        // Nota sobre x₂
        JLabel nota = new JLabel(" x₂ solo para intervalo");
        nota.setFont(new Font("Monospaced", Font.ITALIC, 10));
        nota.setForeground(new Color(80, 100, 140));
        p.add(nota);

        // ── Botón calcular ───────────────────────────────────
        p.add(Box.createVerticalStrut(20));
        p.add(crearBoton());
        p.add(Box.createVerticalGlue());
        return p;
    }

    /**
     * Botón principal "CALCULAR" con efecto hover.
     */
    private JButton crearBoton() {
        JButton btn = new JButton("▶  CALCULAR");
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setForeground(BG);
        btn.setBackground(ACCENT);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 10, 10, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        btn.addActionListener(e -> calcular());

        // Efecto hover: cambia de azul a verde al pasar el mouse
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT2); }
            public void mouseExited (MouseEvent e) { btn.setBackground(ACCENT);  }
        });
        return btn;
    }

    /**
     * Pie de página: referencia a la tabla Z.
     */
    private JPanel crearPie() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(PANEL_BG);
        p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
        JLabel lbl = new JLabel(
            "Tabla Z: N(0,1)  |  P(Z > x) = 1 − Φ(x)  " +
            "|  Prof. Jonathan Lozano — UNIAJC  ");
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lbl.setForeground(TEXT_SEC);
        p.add(lbl);
        return p;
    }

    // ==========================================================
    //  LÓGICA DE CONTROL
    // ==========================================================

    /**
     * Carga un caso práctico de la tabla CASOS en los campos de entrada.
     * Se llama al seleccionar del combo o al iniciar la app.
     *
     * @param indice índice de la fila en CASOS[][]
     */
    private void cargarCaso(int indice) {
        if (indice < 0 || indice >= CASOS.length) return;

        Object[] c = CASOS[indice];
        nombreCasoActual = (String) c[0];

        // Rellenar campos con los valores del caso
        tfMedia.setText(String.valueOf(c[1]));
        tfDesv.setText (String.valueOf(c[2]));
        tfX1.setText   (String.valueOf(c[3]));
        tfX2.setText   (String.valueOf(c[4]));
        cbTipo.setSelectedIndex((int) c[5]);

        // Calcular automáticamente al cargar
        calcular();
    }

    /**
     * Lee los campos, valida, crea el modelo y actualiza las dos vistas.
     * Este es el método central del controlador.
     *
     * Flujo:
     *   1. Leer y parsear campos de texto
     *   2. Validar (σ > 0, valores numéricos)
     *   3. Crear DistribucionNormalModel(μ, σ)
     *   4. Calcular la probabilidad según el tipo
     *   5. Llamar a graficaPanel.actualizar(...)
     *   6. Llamar a resultadosPanel.mostrar(...)
     */
    private void calcular() {
        double mu, sigma, x1, x2 = 0.0;
        try {
            mu    = Double.parseDouble(tfMedia.getText().trim());
            sigma = Double.parseDouble(tfDesv.getText().trim());
            x1    = Double.parseDouble(tfX1.getText().trim());

            // x₂ solo es necesario si el tipo es INTERVALO
            if (cbTipo.getSelectedIndex() == GraficaPanel.INTERVALO)
                x2 = Double.parseDouble(tfX2.getText().trim());

            if (sigma <= 0)
                throw new NumberFormatException("σ debe ser > 0");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Error de entrada:\n" +
                "  • Todos los campos deben ser numéricos.\n" +
                "  • La desviación estándar σ debe ser > 0.",
                "Datos inválidos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int  tipo   = cbTipo.getSelectedIndex();
        // Construir el modelo con los parámetros leídos
        DistribucionNormalModel modelo = new DistribucionNormalModel(mu, sigma);

        // Calcular probabilidad según tipo seleccionado
        double prob = switch (tipo) {
            case GraficaPanel.COLA_DERECHA -> modelo.probMayorQue(x1);
            case GraficaPanel.ACUMULADA    -> modelo.probMenorQue(x1);
            default                        -> modelo.probIntervalo(x1, x2);
        };

        // Actualizar vista 1: gráfica
        graficaPanel.actualizar(modelo, x1, x2, tipo, prob);

        // Actualizar vista 2: desarrollo paso a paso
        resultadosPanel.mostrar(modelo, x1, x2, tipo, nombreCasoActual);
    }

    // ==========================================================
    //  UTILIDADES DE UI
    // ==========================================================

    /**
     * Etiqueta de sección con línea inferior decorativa.
     */
    private JLabel etiquetaSeccion(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Monospaced", Font.BOLD, 10));
        l.setForeground(ACCENT);
        l.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_C),
            new EmptyBorder(0, 0, 3, 0)));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return l;
    }

    /**
     * Fila etiqueta + campo de texto, alineados horizontalmente.
     */
    private JPanel fila(String etiq, JTextField tf) {
        estilizarCampo(tf);
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lbl = new JLabel(etiq);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SEC);
        lbl.setPreferredSize(new Dimension(90, 30));
        row.add(lbl, BorderLayout.WEST);
        row.add(tf,  BorderLayout.CENTER);
        return row;
    }

    /** Aplica el estilo oscuro a un JTextField */
    private void estilizarCampo(JTextField tf) {
        tf.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tf.setForeground(TEXT_PRI);
        tf.setBackground(INPUT_BG);
        tf.setCaretColor(ACCENT);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C),
            new EmptyBorder(3, 7, 3, 7)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    /** Aplica el estilo oscuro a un JComboBox */
    private void estilizarCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cb.setForeground(TEXT_PRI);
        cb.setBackground(INPUT_BG);
        cb.setBorder(new LineBorder(BORDER_C));
    }

    // ==========================================================
    //  PUNTO DE ENTRADA DEL PROGRAMA
    // ==========================================================

    /**
     * main: arranca la aplicación en el hilo de eventos de Swing.
     *
     * Swing es una librería de interfaz gráfica incluida en el JDK.
     * No requiere dependencias externas.
     * SwingUtilities.invokeLater garantiza que la UI se cree
     * en el Event Dispatch Thread (EDT), lo que es obligatorio en Swing.
     */
    public static void main(String[] args) {
        // Usar el look and feel cruzado para coherencia entre OS
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Crear y mostrar la ventana en el hilo de Swing
        SwingUtilities.invokeLater(() ->
            new DistribucionNormalApp().setVisible(true)
        );
    }
}