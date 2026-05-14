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
 *
 *  Cálculo de P(Z > z):
 *    Usando la función de error complementaria (erfc).
 *    Error máximo < 1.5 × 10⁻⁷  (Abramowitz & Stegun 7.1.26)
 * =============================================================
 */
public class DistribucionNormalModel {

    private double media;
    private double desviacion;

    public DistribucionNormalModel(double media, double desviacion) {
        if (desviacion <= 0)
            throw new IllegalArgumentException(
                "La desviación estándar σ debe ser > 0.");
        this.media      = media;
        this.desviacion = desviacion;
    }

    public double estandarizar(double x) {
        return (x - media) / desviacion;
    }

    public double probMayorQue(double x) {
        return pZMayorQue(estandarizar(x));
    }

    public double probMenorQue(double x) {
        return 1.0 - probMayorQue(x);
    }

    public double probIntervalo(double a, double b) {
        double inf = Math.min(a, b);
        double sup = Math.max(a, b);
        return pZMayorQue(estandarizar(inf)) - pZMayorQue(estandarizar(sup));
    }

    public double densidad(double x) {
        double z = estandarizar(x);
        return Math.exp(-0.5 * z * z) / (desviacion * Math.sqrt(2.0 * Math.PI));
    }

    private double pZMayorQue(double z) {
        return 0.5 * erfc(z / Math.sqrt(2.0));
    }

    private double erfc(double x) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));
        double y = 1.0 - (((((1.061405429  * t
                            - 1.453152027) * t
                            + 1.421413741) * t
                            - 0.284496736) * t
                            + 0.254829592) * t) * Math.exp(-x * x);
        return (x >= 0) ? y : 2.0 - y;
    }

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

    @Override
    public String toString() {
        return String.format("X ~ N(%.2f,  %.2f²)", media, desviacion);
    }
}