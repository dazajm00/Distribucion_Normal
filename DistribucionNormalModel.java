/**
 * =============================================================
 *  CLASE: DistribucionNormalModel
 * =============================================================
 *  Propósito:
 *    Encapsula toda la matemática de la distribución Normal.
 *    No contiene interfaz gráfica; solo cálculos puros.
 *
 *  Modelo estadístico:
 *    X ~ N(μ, σ²)
 *
 *  Función de densidad (diapositiva 4):
 *    f(x) = 1/(σ√(2π)) · exp(−(x−μ)² / 2σ²)
 *
 *  Estandarización (diapositiva 6):
 *    Z = (X − μ) / σ
 *    Z mide cuántas desviaciones estándar está X de la media.
 *
 *  Cálculo de P(Z > z) (diapositiva 6–7):
 *    Usando la función de error complementaria (erfc), que
 *    reproduce fielmente la tabla N(0,1) del PDF adjunto.
 *    Error máximo < 1.5 × 10⁻⁷  (Abramowitz & Stegun 7.1.26)
 * =============================================================
 */
public class DistribucionNormalModel {

    // ── Parámetros de la distribución ────────────────────────
    private double media;       // μ : media (centro de la campana)
    private double desviacion;  // σ : desviación estándar (ancho)

    /**
     * Constructor: define la distribución N(media, desviacion²).
     * @param media      μ — valor esperado
     * @param desviacion σ — debe ser estrictamente positivo
     */
    public DistribucionNormalModel(double media, double desviacion) {
        if (desviacion <= 0)
            throw new IllegalArgumentException(
                "La desviación estándar σ debe ser > 0.");
        this.media      = media;
        this.desviacion = desviacion;
    }

    // ==========================================================
    //  ESTANDARIZACIÓN   Z = (X − μ) / σ
    // ==========================================================

    /**
     * Convierte un valor X de la distribución N(μ,σ) al valor
     * Z de la distribución estándar N(0,1).
     *
     * Ejemplo (diapositiva 8 — Examen):
     *   X ~ N(60,10²),  x = 75
     *   z = (75 − 60) / 10 = 1.5
     *
     * @param x valor original en las unidades del problema
     * @return  valor z estandarizado
     */
    public double estandarizar(double x) {
        return (x - media) / desviacion;
    }

    // ==========================================================
    //  PROBABILIDADES  (diapositivas 7–19)
    // ==========================================================

    /**
     * P(X > x) — Cola derecha: probabilidad de superar x.
     *
     * Ejemplo (diapositiva 8 — Examen):
     *   P(X > 75) = P(Z > 1.5) ≈ 0.0668  → ~6.7% supera 75 pts
     *
     * @param x umbral
     * @return  probabilidad entre 0 y 1
     */
    public double probMayorQue(double x) {
        return pZMayorQue(estandarizar(x));
    }

    /**
     * P(X < x) — Acumulada izquierda: probabilidad de no superar x.
     * Complemento de la cola derecha: P(X < x) = 1 − P(X > x)
     *
     * Ejemplo (diapositiva 10 — Control de calidad):
     *   X ~ N(50, 0.2²),  x = 50.5
     *   P(X < 50.5) = 1 − P(Z > 2.5) ≈ 0.9938  → 99.38% dentro del límite
     *
     * @param x umbral
     * @return  probabilidad entre 0 y 1
     */
    public double probMenorQue(double x) {
        return 1.0 - probMayorQue(x);
    }

    /**
     * P(a < X < b) — Intervalo: probabilidad entre dos valores.
     * Formula (diapositiva 7):
     *   P(a ≤ X ≤ b) = P(Z ≤ zb) − P(Z ≤ za)
     *                = P(Z > za) − P(Z > zb)
     *
     * Ejemplo (diapositiva 18 — Contabilidad):
     *   X ~ N(200, 25²),  a = 180,  b = 230
     *   z_a = (180−200)/25 = −0.8,   z_b = (230−200)/25 = 1.2
     *   P = P(Z > −0.8) − P(Z > 1.2) ≈ 0.8849 − 0.1151 = 0.6731
     *
     * @param a extremo inferior
     * @param b extremo superior
     * @return  probabilidad entre 0 y 1
     */
    public double probIntervalo(double a, double b) {
        // Garantizamos orden correcto independiente del usuario
        double inf = Math.min(a, b);
        double sup = Math.max(a, b);
        return pZMayorQue(estandarizar(inf)) - pZMayorQue(estandarizar(sup));
    }

    /**
     * Función de densidad normal  f(x)  en el punto x.
     * Necesaria para dibujar la curva de campana en la gráfica.
     *
     * f(x) = 1/(σ√(2π)) · exp(−(x−μ)²/(2σ²))
     *
     * @param x punto donde se evalúa la densidad
     * @return  altura de la campana en x
     */
    public double densidad(double x) {
        double z = estandarizar(x);
        return Math.exp(-0.5 * z * z) / (desviacion * Math.sqrt(2.0 * Math.PI));
    }

    // ==========================================================
    //  TABLA N(0,1):  P(Z > z)
    //  Implementación interna — reproduce la tabla del PDF
    // ==========================================================

    /**
     * P(Z > z) para la distribución estándar N(0,1).
     * Usa la función de error complementaria (erfc):
     *   P(Z > z) = 0.5 · erfc(z / √2)
     */
    private double pZMayorQue(double z) {
        return 0.5 * erfc(z / Math.sqrt(2.0));
    }

    /**
     * Función de error complementaria erfc(x).
     *
     * Aproximación polinómica de Abramowitz & Stegun 7.1.26:
     *   erfc(x) ≈ (a1·t + a2·t² + a3·t³ + a4·t⁴ + a5·t⁵) · e^(−x²)
     *   donde  t = 1 / (1 + 0.3275911·|x|)
     *
     * Propiedades:
     *   erfc(x)  = 1 − erf(x)         para x ≥ 0
     *   erfc(−x) = 2 − erfc(x)        simetría
     *
     * Error máximo < 1.5 × 10⁻⁷ — suficiente para 5 decimales de tabla.
     */
    private double erfc(double x) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));
        // Evaluación por esquema de Horner (más eficiente y preciso)
        double y = 1.0 - (((((1.061405429  * t
                            - 1.453152027) * t
                            + 1.421413741) * t
                            - 0.284496736) * t
                            + 0.254829592) * t) * Math.exp(-x * x);
        return (x >= 0) ? y : 2.0 - y;
    }

    // ==========================================================
    //  GETTERS / SETTERS
    // ==========================================================

    public double getMedia()      { return media; }
    public double getDesviacion() { return desviacion; }

    public void setMedia(double media) {
        this.media = media;
    }
    public void setDesviacion(double desviacion) {
        if (desviacion <= 0)
            throw new IllegalArgumentException("σ debe ser > 0.");
        this.desviacion = desviacion;
    }

    /**
     * Descripción textual del modelo para mostrar en resultados.
     * @return cadena con la notación estadística N(μ, σ²)
     */
    @Override
    public String toString() {
        return String.format("X ~ N(%.2f,  %.2f²)", media, desviacion);
    }
}