import java.awt.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * =============================================================
 *  CLASE: ResultadosPanel
 * =============================================================
 *  Propósito:
 *    Muestra el desarrollo matemático completo de cada cálculo,
 *    siguiendo exactamente el formato de las diapositivas de clase
 *    (Sesión 13):
 *      1. Descripción del problema
 *      2. Modelo estadístico  X ~ N(μ, σ²)
 *      3. Estandarización     Z = (X − μ) / σ
 *      4. Búsqueda en tabla N(0,1)
 *      5. Resultado numérico
 *      6. Interpretación en contexto
 *
 *  No realiza cálculos: solo formatea y presenta lo que
 *  DistribucionNormalModel calculó.
 * =============================================================
 */
public class ResultadosPanel extends JPanel {

    // ── Colores del tema ─────────────────────────────────────
    private static final Color BG      = new Color(14, 20, 36);
    private static final Color TEXT_PRI = new Color(226, 232, 245);

    // ── Formato numérico ─────────────────────────────────────
    /** Cinco decimales: coincide con la tabla N(0,1) del PDF */
    private final DecimalFormat df5 = new DecimalFormat("0.00000");
    /** Cuatro decimales para z */
    private final DecimalFormat df4 = new DecimalFormat("0.0000");

    private final JTextArea area;

    /**
     * Constructor: crea el área de texto con scroll dentro del panel.
     */
    public ResultadosPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);

        area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setForeground(TEXT_PRI);
        area.setBackground(BG);
        area.setBorder(new EmptyBorder(10, 14, 10, 14));
        area.setText(
            "\n  Configure los parámetros y presione  [ CALCULAR ]\n\n" +
            "  Ingrese sus propios valores en el panel de parámetros."
        );

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);
    }

    // ==========================================================
    //  MÉTODO PRINCIPAL: renderizar resultados
    // ==========================================================

    /**
     * Genera el desarrollo completo y lo muestra en el área de texto.
     * Llamado desde DistribucionNormalApp después de calcular.
     *
     * @param modelo      modelo con μ, σ y métodos de probabilidad
     * @param x1          primer límite
     * @param x2          segundo límite (solo para INTERVALO)
     * @param tipo        GraficaPanel.COLA_DERECHA / ACUMULADA / INTERVALO
     * @param nombreCaso  nombre del caso práctico seleccionado
     */
    public void mostrar(DistribucionNormalModel modelo,
                        double x1, double x2, int tipo,
                        String nombreCaso) {

        double mu    = modelo.getMedia();
        double sigma = modelo.getDesviacion();

        // Calcular z-scores y probabilidad según el tipo
        double z1   = modelo.estandarizar(x1);
        double z2   = modelo.estandarizar(x2);   // solo usado en INTERVALO
        double prob = calcularProb(modelo, x1, x2, tipo);

        // Construir el texto
        StringBuilder sb = new StringBuilder();

        encabezado(sb, nombreCaso);
        seccionModelo(sb, mu, sigma);
        seccionEstandarizacion(sb, mu, sigma, x1, x2, z1, z2, tipo);
        seccionTablaZ(sb, modelo, x1, x2, z1, z2, tipo, prob);
        seccionResultado(sb, tipo, x1, x2, prob);
        seccionInterpretacion(sb, tipo, prob, x1, x2, nombreCaso);

        area.setText(sb.toString());
        area.setCaretPosition(0); // scroll al inicio
    }

    // ==========================================================
    //  SECCIONES DEL RESULTADO
    // ==========================================================

    /** Encabezado con nombre del caso y separador */
    private void encabezado(StringBuilder sb, String caso) {
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("  DISTRIBUCIÓN NORMAL — DESARROLLO PASO A PASO\n");
        sb.append("═══════════════════════════════════════════════════════\n\n");
        sb.append("📌 CASO:  ").append(caso).append("\n\n");
    }

    /**
     * Sección 1: modelo estadístico.
     */
    private void seccionModelo(StringBuilder sb, double mu, double sigma) {
        sb.append("┌─ 1. MODELO ESTADÍSTICO ────────────────────────────┐\n");
        sb.append(String.format("│  X ~ N(μ, σ²)  con  μ = %-8.2f  σ = %-8.2f     │%n",
                  mu, sigma));
        sb.append("│                                                     │\n");
        sb.append("│  f(x) = 1/(σ√2π) · exp(−(x−μ)² / 2σ²)            │\n");
        sb.append("│  La variable X es continua y sigue distribución     │\n");
        sb.append("│  normal (campana de Gauss).                         │\n");
        sb.append("└─────────────────────────────────────────────────────┘\n\n");
    }

    /**
     * Sección 2: estandarización.
     */
    private void seccionEstandarizacion(StringBuilder sb,
                                         double mu, double sigma,
                                         double x1, double x2,
                                         double z1, double z2, int tipo) {
        sb.append("┌─ 2. ESTANDARIZACIÓN   Z = (X − μ) / σ ────────────┐\n");
        sb.append(String.format(
            "│  z₁ = (%.2f − %.2f) / %.2f = %s%n",
            x1, mu, sigma, df4.format(z1)));

        if (tipo == GraficaPanel.INTERVALO) {
            sb.append(String.format(
                "│  z₂ = (%.2f − %.2f) / %.2f = %s%n",
                x2, mu, sigma, df4.format(z2)));
        }
        sb.append("│                                                     │\n");
        sb.append("│  Z indica cuántas σ está X alejado de la media μ.  │\n");
        sb.append("└─────────────────────────────────────────────────────┘\n\n");
    }

    /**
     * Sección 3: uso de la tabla Z o función CDF.
     */
    private void seccionTablaZ(StringBuilder sb,
                                DistribucionNormalModel m,
                                double x1, double x2,
                                double z1, double z2,
                                int tipo, double prob) {
        sb.append("┌─ 3. CÁLCULO CON TABLA N(0,1) ──────────────────────┐\n");
        sb.append("│  La tabla da P(Z > z) = 1 − Φ(z)                  │\n│\n");

        switch (tipo) {

            // ── Cola derecha: P(X > x₁) ──────────────────────
            case GraficaPanel.COLA_DERECHA:
                sb.append(String.format(
                    "│  P(X > %.4f) = P(Z > %s)%n", x1, df4.format(z1)));
                if (z1 >= 0) {
                    sb.append(String.format(
                        "│  → Buscar z = %s en tabla → P = %s%n",
                        df4.format(z1), df5.format(prob)));
                } else {
                    sb.append(String.format(
                        "│  z < 0 → simetría: P(Z > %s) = P(Z < %s)%n",
                        df4.format(z1), df4.format(-z1)));
                    sb.append(String.format(
                        "│         = 1 − P(Z > %s) = 1 − %s = %s%n",
                        df4.format(-z1), df5.format(m.probMayorQue(-x1 + 2 * m.getMedia())),
                        df5.format(prob)));
                }
                break;

            // ── Acumulada: P(X < x₁) ─────────────────────────
            case GraficaPanel.ACUMULADA:
                sb.append(String.format(
                    "│  P(X < %.4f) = P(Z < %s)%n", x1, df4.format(z1)));
                sb.append(String.format(
                    "│             = 1 − P(Z > %s)%n", df4.format(z1)));
                sb.append(String.format(
                    "│             = 1 − %s%n", df5.format(1.0 - prob)));
                sb.append(String.format(
                    "│             = %s%n", df5.format(prob)));
                break;

            // ── Intervalo: P(x₁ < X < x₂) ───────────────────
            case GraficaPanel.INTERVALO:
                double pz1 = m.probMayorQue(x1);
                double pz2 = m.probMayorQue(x2);
                sb.append(String.format(
                    "│  P(%.2f < X < %.2f)%n", x1, x2));
                sb.append(String.format(
                    "│    = P(%s < Z < %s)%n",
                    df4.format(z1), df4.format(z2)));
                sb.append(String.format(
                    "│    = P(Z > z₁) − P(Z > z₂)%n"));
                sb.append(String.format(
                    "│    = P(Z > %s) − P(Z > %s)%n",
                    df4.format(z1), df4.format(z2)));
                sb.append(String.format(
                    "│    = %s − %s%n", df5.format(pz1), df5.format(pz2)));
                sb.append(String.format(
                    "│    = %s%n", df5.format(prob)));
                break;
        }
        sb.append("└─────────────────────────────────────────────────────┘\n\n");
    }

    /**
     * Sección 4: resultado destacado con la etiqueta de probabilidad.
     */
    private void seccionResultado(StringBuilder sb,
                                   int tipo, double x1, double x2, double prob) {
        String etiq = etiqueta(tipo, x1, x2);
        sb.append("┌─ 4. RESULTADO ─────────────────────────────────────┐\n");
        sb.append(String.format(
            "│  ✅  %s = %s%n", etiq, df5.format(prob)));
        sb.append(String.format(
            "│  ✅  %s ≈ %.2f %%%n", etiq, prob * 100));
        sb.append("└─────────────────────────────────────────────────────┘\n\n");
    }

    /**
     * Sección 5: interpretación en lenguaje natural del contexto.
     */
    private void seccionInterpretacion(StringBuilder sb, int tipo,
                                        double prob, double x1, double x2,
                                        String caso) {
        double pct = prob * 100;
        sb.append("┌─ 5. INTERPRETACIÓN ────────────────────────────────┐\n");
        switch (tipo) {
            case GraficaPanel.COLA_DERECHA:
                sb.append(String.format(
                    "│  Aproximadamente el %.2f%% de los valores del%n" +
                    "│  caso '%s'%n" +
                    "│  superan el umbral %.2f.%n", pct, caso, x1));
                break;
            case GraficaPanel.ACUMULADA:
                sb.append(String.format(
                    "│  Aproximadamente el %.2f%% de los valores del%n" +
                    "│  caso '%s'%n" +
                    "│  se encuentran por debajo de %.2f.%n", pct, caso, x1));
                break;
            case GraficaPanel.INTERVALO:
                sb.append(String.format(
                    "│  Aproximadamente el %.2f%% de los valores del%n" +
                    "│  caso '%s'%n" +
                    "│  se ubican entre %.2f y %.2f.%n", pct, caso, x1, x2));
                break;
        }
        sb.append("└─────────────────────────────────────────────────────┘\n");
    }

    // ==========================================================
    //  UTILIDADES
    // ==========================================================

    private double calcularProb(DistribucionNormalModel m,
                                 double x1, double x2, int tipo) {
        return switch (tipo) {
            case GraficaPanel.COLA_DERECHA -> m.probMayorQue(x1);
            case GraficaPanel.ACUMULADA    -> m.probMenorQue(x1);
            default                        -> m.probIntervalo(x1, x2);
        };
    }

    private String etiqueta(int tipo, double x1, double x2) {
        return switch (tipo) {
            case GraficaPanel.COLA_DERECHA ->
                String.format("P(X > %.2f)",  x1);
            case GraficaPanel.ACUMULADA    ->
                String.format("P(X < %.2f)",  x1);
            default                        ->
                String.format("P(%.2f < X < %.2f)", x1, x2);
        };
    }
}