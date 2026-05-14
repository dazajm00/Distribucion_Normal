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
 *
 *  Tipos de probabilidad soportados (constantes públicas):
 *    COLA_DERECHA  → P(X > x₁)    área a la derecha de x₁
 *    ACUMULADA     → P(X < x₁)    área a la izquierda de x₁
 *    INTERVALO     → P(x₁<X<x₂)  área entre x₁ y x₂
 * =============================================================
 */
public class GraficaPanel extends JPanel {

    public static final int COLA_DERECHA = 0;
    public static final int ACUMULADA    = 1;
    public static final int INTERVALO    = 2;

    private static final Color FONDO_PANEL  = new Color(18,  24,  42);
    private static final Color CURVA_COLOR  = new Color(99, 179, 237);
    private static final Color AREA_COLOR   = new Color(99, 179, 237, 100);
    private static final Color LINEA_MU     = new Color(255, 220, 80);
    private static final Color LINEA_X1     = new Color(72,  209, 164);
    private static final Color LINEA_X2     = new Color(255, 130, 80);
    private static final Color TEXTO_COLOR  = new Color(180, 195, 220);
    private static final Color GRILLA_COLOR = new Color(50,  65, 100, 55);
    private static final Color RESULTADO_C  = new Color(72,  209, 164);

    private static final int MX      = 60;
    private static final int MY      = 40;
    private static final int MX_DER  = 30;
    private static final int MY_INF  = 50;

    private DistribucionNormalModel modelo;
    private double x1   = 0;
    private double x2   = 0;
    private int    tipo = COLA_DERECHA;
    private double prob = 0;

    public GraficaPanel() {
        setBackground(new Color(10, 14, 26));
        setPreferredSize(new Dimension(650, 340));
    }

    public void actualizar(DistribucionNormalModel modelo,
                           double x1, double x2, int tipo, double prob) {
        this.modelo = modelo;
        this.x1     = x1;
        this.x2     = x2;
        this.tipo   = tipo;
        this.prob   = prob;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (modelo == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W  = getWidth();
        int H  = getHeight();
        int bx = W - MX_DER;
        int by = H - MY_INF;
        int gW = bx - MX;
        int gH = by - MY;

        double mu    = modelo.getMedia();
        double sigma = modelo.getDesviacion();

        double xMin = mu - 4.0 * sigma;
        double xMax = mu + 4.0 * sigma;
        double yMax = modelo.densidad(mu) * 1.15;

        dibujarFondo(g2, gW, gH);
        dibujarGrilla(g2, mu, sigma, xMin, xMax, gW, gH, by);
        dibujarAreaSombreada(g2, xMin, xMax, yMax, gW, gH, by);
        dibujarCurva(g2, xMin, xMax, yMax, gW, gH);
        dibujarEjes(g2, bx, by, gW, gH);
        dibujarLineasVerticales(g2, mu, xMin, xMax, yMax, gW, gH, by);
        dibujarEtiquetasEjeX(g2, mu, sigma, xMin, xMax, gW, by);
        dibujarResultado(g2, mu, sigma);
    }

    private void dibujarFondo(Graphics2D g2, int gW, int gH) {
        g2.setColor(FONDO_PANEL);
        g2.fillRoundRect(MX - 12, MY - 12, gW + 24, gH + 24, 10, 10);
    }

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

    private void dibujarAreaSombreada(Graphics2D g2,
                                       double xMin, double xMax, double yMax,
                                       int gW, int gH, int by) {
        double axL, axR;
        switch (tipo) {
            case COLA_DERECHA:
                axL = x1;    axR = xMax; break;
            case ACUMULADA:
                axL = xMin;  axR = x1;   break;
            default:
                axL = Math.min(x1, x2);
                axR = Math.max(x1, x2);
                break;
        }

        Path2D area = new Path2D.Double();
        int basePx = yP(0, 0, yMax, gH);

        area.moveTo(xP(axL, xMin, xMax, gW), basePx);

        int pasos = 300;
        for (int i = 0; i <= pasos; i++) {
            double xv = axL + (axR - axL) * i / (double) pasos;
            area.lineTo(xP(xv, xMin, xMax, gW),
                        yP(modelo.densidad(xv), 0, yMax, gH));
        }

        area.lineTo(xP(axR, xMin, xMax, gW), basePx);
        area.closePath();

        g2.setPaint(new GradientPaint(
            MX, MY, new Color(99, 179, 237, 140),
            MX, by,  new Color(72, 209, 164, 30)));
        g2.fill(area);
    }

    private void dibujarCurva(Graphics2D g2,
                               double xMin, double xMax, double yMax,
                               int gW, int gH) {
        Path2D curva = new Path2D.Double();
        int pasos = 600;
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

    private void dibujarEjes(Graphics2D g2, int bx, int by, int gW, int gH) {
        g2.setColor(new Color(60, 80, 120));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(MX, by, bx, by);
        g2.drawLine(MX, MY, MX, by);
    }

    private void dibujarLineasVerticales(Graphics2D g2, double mu,
                                          double xMin, double xMax, double yMax,
                                          int gW, int gH, int by) {
        Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[]{7f, 5f}, 0f);
        g2.setStroke(dashed);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));

        traceLinea(g2, mu, xMin, xMax, gW, by, LINEA_MU,
                   String.format("μ=%.1f", mu));
        traceLinea(g2, x1, xMin, xMax, gW, by, LINEA_X1,
                   String.format("x₁=%.1f", x1));
        if (tipo == INTERVALO)
            traceLinea(g2, x2, xMin, xMax, gW, by, LINEA_X2,
                       String.format("x₂=%.1f", x2));
    }

    private void traceLinea(Graphics2D g2, double xv,
                             double xMin, double xMax, int gW, int by,
                             Color color, String label) {
        int px = xP(xv, xMin, xMax, gW);
        g2.setColor(color);
        g2.drawLine(px, MY + 2, px, by);
        g2.drawString(label, px + 3, MY + 14);
    }

    private void dibujarEtiquetasEjeX(Graphics2D g2, double mu, double sigma,
                                       double xMin, double xMax, int gW, int by) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXTO_COLOR);
        for (int i = -4; i <= 4; i++) {
            double xv = mu + i * sigma;
            int px = xP(xv, xMin, xMax, gW);
            String lbl = (xv == Math.floor(xv))
                         ? String.valueOf((long) xv)
                         : String.format("%.1f", xv);
            g2.drawString(lbl, px - lbl.length() * 3, by + 17);
        }
        g2.setColor(new Color(100, 120, 160));
        g2.drawString("x", getWidth() - MX_DER - 10, by + 17);
    }

    private void dibujarResultado(Graphics2D g2, double mu, double sigma) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(RESULTADO_C);
        g2.drawString(String.format("P = %.5f  (%.2f%%)", prob, prob * 100),
                      MX + 6, MY + 18);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(TEXTO_COLOR);
        g2.drawString(modelo.toString(), MX + 6, MY + 33);
    }

    private int xP(double x, double xMin, double xMax, int gW) {
        return (int) (MX + (x - xMin) / (xMax - xMin) * gW);
    }

    private int yP(double y, double yMin, double yMax, int gH) {
        return (int) (MY + gH - (y - yMin) / (yMax - yMin) * gH);
    }
}