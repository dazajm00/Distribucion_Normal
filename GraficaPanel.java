import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * =============================================================
 *  CLASE: GraficaPanel
 * =============================================================
 *  Propósito:
 *    Componente visual que dibuja la curva de campana N(μ,σ)
 *    y sombrea el área correspondiente a la probabilidad pedida.
 *    El estilo imita los gráficos de las diapositivas 11,13,15,17,19.
 *
 *  Tipos de probabilidad soportados (constantes públicas):
 *    COLA_DERECHA  → P(X > x₁)    área a la derecha de x₁
 *    ACUMULADA     → P(X < x₁)    área a la izquierda de x₁
 *    INTERVALO     → P(x₁<X<x₂)  área entre x₁ y x₂
 *
 *  Coordenadas:
 *    El panel mapea el rango [μ−4σ, μ+4σ] en el eje X y
 *    [0, f(μ)·1.15] en el eje Y mediante funciones de escala.
 * =============================================================
 */
public class GraficaPanel extends JPanel {

    // ── Constantes de tipo de probabilidad ───────────────────
    /** P(X > x₁): sombreado desde x₁ hasta el extremo derecho */
    public static final int COLA_DERECHA = 0;
    /** P(X < x₁): sombreado desde el extremo izquierdo hasta x₁ */
    public static final int ACUMULADA    = 1;
    /** P(x₁ < X < x₂): sombreado entre x₁ y x₂ */
    public static final int INTERVALO    = 2;

    // ── Paleta de colores (inspirada en las diapositivas) ────
    private static final Color FONDO_PANEL  = new Color(18,  24,  42);
    private static final Color CURVA_COLOR  = new Color(99, 179, 237);   // azul acento
    private static final Color AREA_COLOR   = new Color(99, 179, 237, 100); // área semitransparente
    private static final Color LINEA_MU     = new Color(255, 220, 80);   // amarillo para μ
    private static final Color LINEA_X1     = new Color(72,  209, 164);  // verde para x₁
    private static final Color LINEA_X2     = new Color(255, 130, 80);   // naranja para x₂
    private static final Color TEXTO_COLOR  = new Color(180, 195, 220);
    private static final Color GRILLA_COLOR = new Color(50,  65, 100, 55);
    private static final Color RESULTADO_C  = new Color(72,  209, 164);

    // ── Márgenes internos del área de dibujo ─────────────────
    private static final int MX      = 60;  // margen izquierdo
    private static final int MY      = 40;  // margen superior
    private static final int MX_DER  = 30;  // margen derecho
    private static final int MY_INF  = 50;  // margen inferior

    // ── Estado del panel ─────────────────────────────────────
    private DistribucionNormalModel modelo; // lógica de cálculo
    private double x1   = 0;               // primer límite
    private double x2   = 0;               // segundo límite (solo intervalo)
    private int    tipo = COLA_DERECHA;    // tipo de probabilidad
    private double prob = 0;               // probabilidad ya calculada

    /**
     * Constructor: inicializa el panel con fondo oscuro.
     */
    public GraficaPanel() {
        setBackground(new Color(10, 14, 26));
        setPreferredSize(new Dimension(650, 340));
    }

    /**
     * Actualiza todos los parámetros y repinta la curva.
     * Llamado desde DistribucionNormalApp cada vez que el usuario
     * presiona "Calcular".
     *
     * @param modelo  modelo con μ y σ y los métodos de probabilidad
     * @param x1      primer valor límite
     * @param x2      segundo valor límite (ignorado si tipo ≠ INTERVALO)
     * @param tipo    COLA_DERECHA | ACUMULADA | INTERVALO
     * @param prob    probabilidad ya calculada por el modelo
     */
    public void actualizar(DistribucionNormalModel modelo,
                           double x1, double x2, int tipo, double prob) {
        this.modelo = modelo;
        this.x1     = x1;
        this.x2     = x2;
        this.tipo   = tipo;
        this.prob   = prob;
        repaint(); // redibuja el panel
    }

    /**
     * Método principal de Swing: se llama automáticamente al repintar.
     * Aquí orquestamos el dibujo en capas (de atrás hacia adelante):
     *   1. Fondo
     *   2. Grilla de referencia
     *   3. Área sombreada
     *   4. Curva de campana
     *   5. Ejes
     *   6. Líneas verticales y etiquetas
     *   7. Resultado numérico
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (modelo == null) return; // nada que dibujar aún

        // Activar suavizado (anti-aliasing) para curvas limpias
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Dimensiones útiles del área de dibujo
        int W  = getWidth();
        int H  = getHeight();
        int bx = W - MX_DER;  // extremo derecho
        int by = H - MY_INF;  // extremo inferior (línea del eje X)
        int gW = bx - MX;     // ancho del área de gráfica
        int gH = by - MY;     // alto del área de gráfica

        double mu    = modelo.getMedia();
        double sigma = modelo.getDesviacion();

        // Rango del eje X: μ ± 4σ  (cubre el 99.994% de la distribución)
        double xMin = mu - 4.0 * sigma;
        double xMax = mu + 4.0 * sigma;

        // Máximo de la densidad (en μ) × 1.15 de margen superior
        double yMax = modelo.densidad(mu) * 1.15;

        // ── Dibujar capas ────────────────────────────────────
        dibujarFondo(g2, gW, gH);
        dibujarGrilla(g2, mu, sigma, xMin, xMax, gW, gH, by);
        dibujarAreaSombreada(g2, xMin, xMax, yMax, gW, gH, by);
        dibujarCurva(g2, xMin, xMax, yMax, gW, gH);
        dibujarEjes(g2, bx, by, gW, gH);
        dibujarLineasVerticales(g2, mu, xMin, xMax, yMax, gW, gH, by);
        dibujarEtiquetasEjeX(g2, mu, sigma, xMin, xMax, gW, by);
        dibujarResultado(g2, mu, sigma);
    }

    // ==========================================================
    //  MÉTODOS DE DIBUJO (privados)
    // ==========================================================

    /** Rectángulo de fondo del área de gráfica */
    private void dibujarFondo(Graphics2D g2, int gW, int gH) {
        g2.setColor(FONDO_PANEL);
        g2.fillRoundRect(MX - 12, MY - 12, gW + 24, gH + 24, 10, 10);
    }

    /**
     * Líneas verticales tenues en cada σ: μ−4σ, μ−3σ, … μ+4σ.
     * Ayudan a visualizar la regla empírica 68-95-99.7.
     */
    private void dibujarGrilla(Graphics2D g2, double mu, double sigma,
                                double xMin, double xMax,
                                int gW, int gH, int by) {
        g2.setColor(GRILLA_COLOR);
        g2.setStroke(new BasicStroke(0.7f));
        for (int i = -4; i <= 4; i++) {
            int px = xP(mu + i * sigma, xMin, xMax, gW);
            g2.drawLine(px, MY, px, by);
        }
    }

    /**
     * Área sombreada bajo la curva según el tipo de probabilidad:
     *   - COLA_DERECHA: de x₁ al extremo derecho
     *   - ACUMULADA:    del extremo izquierdo a x₁
     *   - INTERVALO:    de x₁ a x₂
     */
    private void dibujarAreaSombreada(Graphics2D g2,
                                       double xMin, double xMax, double yMax,
                                       int gW, int gH, int by) {
        // Definir los límites del sombreado
        double axL, axR;
        switch (tipo) {
            case COLA_DERECHA:
                axL = x1;    axR = xMax; break;
            case ACUMULADA:
                axL = xMin;  axR = x1;   break;
            default: // INTERVALO
                axL = Math.min(x1, x2);
                axR = Math.max(x1, x2);
                break;
        }

        // Construir el Path2D de la región sombreada
        Path2D area = new Path2D.Double();
        int basePx = yP(0, 0, yMax, gH); // línea base (y=0)

        area.moveTo(xP(axL, xMin, xMax, gW), basePx);

        // Seguir la curva de densidad de axL a axR en 300 pasos
        int pasos = 300;
        for (int i = 0; i <= pasos; i++) {
            double xv = axL + (axR - axL) * i / (double) pasos;
            area.lineTo(xP(xv, xMin, xMax, gW),
                        yP(modelo.densidad(xv), 0, yMax, gH));
        }

        // Cerrar hacia la línea base
        area.lineTo(xP(axR, xMin, xMax, gW), basePx);
        area.closePath();

        // Degradado de arriba (más opaco) a abajo (más transparente)
        g2.setPaint(new GradientPaint(
            MX, MY, new Color(99, 179, 237, 140),
            MX, by,  new Color(72, 209, 164, 30)));
        g2.fill(area);
    }

    /**
     * Curva de campana principal: traza f(x) en todo el rango [xMin, xMax].
     */
    private void dibujarCurva(Graphics2D g2,
                               double xMin, double xMax, double yMax,
                               int gW, int gH) {
        Path2D curva = new Path2D.Double();
        int pasos = 600; // más pasos = curva más suave
        for (int i = 0; i <= pasos; i++) {
            double xv = xMin + (xMax - xMin) * i / (double) pasos;
            int px = xP(xv, xMin, xMax, gW);
            int py = yP(modelo.densidad(xv), 0, yMax, gH);
            if (i == 0) curva.moveTo(px, py);
            else        curva.lineTo(px, py);
        }
        g2.setColor(CURVA_COLOR);
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND,
                                          BasicStroke.JOIN_ROUND));
        g2.draw(curva);
    }

    /** Eje X horizontal y eje Y vertical */
    private void dibujarEjes(Graphics2D g2, int bx, int by, int gW, int gH) {
        g2.setColor(new Color(60, 80, 120));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(MX, by, bx, by); // eje X
        g2.drawLine(MX, MY, MX, by); // eje Y
    }

    /**
     * Líneas verticales punteadas en μ, x₁ y opcionalmente x₂.
     * Imitan las líneas de referencia de las diapositivas.
     */
    private void dibujarLineasVerticales(Graphics2D g2, double mu,
                                          double xMin, double xMax, double yMax,
                                          int gW, int gH, int by) {
        // Estilo punteado
        Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[]{7f, 5f}, 0f);
        g2.setStroke(dashed);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));

        // μ en amarillo
        traceLinea(g2, mu, xMin, xMax, gW, by, LINEA_MU,
                   String.format("μ=%.1f", mu));
        // x₁ en verde
        traceLinea(g2, x1, xMin, xMax, gW, by, LINEA_X1,
                   String.format("x₁=%.1f", x1));
        // x₂ en naranja (solo si es intervalo)
        if (tipo == INTERVALO)
            traceLinea(g2, x2, xMin, xMax, gW, by, LINEA_X2,
                       String.format("x₂=%.1f", x2));
    }

    /** Dibuja una línea vertical punteada con etiqueta superior */
    private void traceLinea(Graphics2D g2, double xv,
                             double xMin, double xMax, int gW, int by,
                             Color color, String label) {
        int px = xP(xv, xMin, xMax, gW);
        g2.setColor(color);
        g2.drawLine(px, MY + 2, px, by);
        g2.drawString(label, px + 3, MY + 14);
    }

    /**
     * Etiquetas numéricas debajo del eje X: μ−4σ hasta μ+4σ.
     * Solo muestra las marcas que caben visualmente (cada σ).
     */
    private void dibujarEtiquetasEjeX(Graphics2D g2, double mu, double sigma,
                                       double xMin, double xMax, int gW, int by) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXTO_COLOR);
        for (int i = -4; i <= 4; i++) {
            double xv = mu + i * sigma;
            int px = xP(xv, xMin, xMax, gW);
            // Etiqueta: si es entero la muestra sin decimales, si no con 1
            String lbl = (xv == Math.floor(xv))
                         ? String.valueOf((long) xv)
                         : String.format("%.1f", xv);
            g2.drawString(lbl, px - lbl.length() * 3, by + 17);
        }
        // Texto del eje X
        g2.setColor(new Color(100, 120, 160));
        g2.drawString("x", getWidth() - MX_DER - 10, by + 17);
    }

    /**
     * Muestra en la esquina superior-izquierda:
     *   - Notación del modelo
     *   - Probabilidad calculada (número y porcentaje)
     * Similar a las etiquetas "P(X > ...) = ..." de las diapositivas.
     */
    private void dibujarResultado(Graphics2D g2, double mu, double sigma) {
        // Resultado en verde brillante
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(RESULTADO_C);
        g2.drawString(String.format("P = %.5f  (%.2f%%)", prob, prob * 100),
                      MX + 6, MY + 18);

        // Modelo en gris tenue
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(TEXTO_COLOR);
        g2.drawString(modelo.toString(), MX + 6, MY + 33);
    }

    // ==========================================================
    //  FUNCIONES DE ESCALA: valor real → píxel
    // ==========================================================

    /**
     * Convierte un valor X del dominio al píxel horizontal correspondiente.
     * Aplica interpolación lineal entre MX y (MX + gW).
     */
    private int xP(double x, double xMin, double xMax, int gW) {
        return (int) (MX + (x - xMin) / (xMax - xMin) * gW);
    }

    /**
     * Convierte un valor Y de la densidad al píxel vertical correspondiente.
     * Y=0 → fila `by` (abajo); Y=yMax → fila `MY` (arriba).
     */
    private int yP(double y, double yMin, double yMax, int gH) {
        return (int) (MY + gH - (y - yMin) / (yMax - yMin) * gH);
    }
}