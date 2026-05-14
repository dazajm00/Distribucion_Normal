import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

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

    // ── Nombre del caso (siempre Personalizado) ──────────────
    private final String nombreCasoActual = "Personalizado";

    // ── Controles de entrada ─────────────────────────────────
    private JTextField  tfMedia, tfDesv, tfX1, tfX2;
    private JComboBox<String> cbTipo;

    // ── Paneles de visualización ─────────────────────────────
    private GraficaPanel    graficaPanel;
    private ResultadosPanel resultadosPanel;

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
     * Panel izquierdo: etiqueta "Personalizado", parámetros y botón calcular.
     */
    private JPanel crearPanelControles() {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(18, 14, 18, 14));

        // ── Sección de caso (solo Personalizado) ─────────────
        p.add(etiquetaSeccion("CASOS PRÁCTICOS"));
        p.add(Box.createVerticalStrut(7));

        // Etiqueta estática en lugar del combo con scroll
        JLabel lblPersonalizado = new JLabel("Personalizado");
        lblPersonalizado.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblPersonalizado.setForeground(TEXT_PRI);
        lblPersonalizado.setBackground(INPUT_BG);
        lblPersonalizado.setOpaque(true);
        lblPersonalizado.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C),
            new EmptyBorder(5, 7, 5, 7)));
        lblPersonalizado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(lblPersonalizado);

        // ── Parámetros de la distribución ───────────────────
        p.add(Box.createVerticalStrut(16));
        p.add(etiquetaSeccion("PARÁMETROS  X ~ N(μ, σ²)"));
        p.add(Box.createVerticalStrut(8));

        tfMedia = new JTextField("0");
        tfDesv  = new JTextField("1");
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
        tfX1 = new JTextField("0");
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
     * Lee los campos, valida, crea el modelo y actualiza las dos vistas.
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
        DistribucionNormalModel modelo = new DistribucionNormalModel(mu, sigma);

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

    private void estilizarCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cb.setForeground(TEXT_PRI);
        cb.setBackground(INPUT_BG);
        cb.setBorder(new LineBorder(BORDER_C));
    }

    // ==========================================================
    //  PUNTO DE ENTRADA DEL PROGRAMA
    // ==========================================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() ->
            new DistribucionNormalApp().setVisible(true)
        );
    }
}