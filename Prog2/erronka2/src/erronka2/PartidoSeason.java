package erronka2;

import java.io.Serial;
import java.io.Serializable;

/**
 * Partido base para trabajar por temporadas en archivos .ser.
 */
public class PartidoSeason implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int jornada;
    private final String local;
    private final String visitante;
    private Integer golesLocal;
    private Integer golesVisitante;

    public PartidoSeason(int jornada, String local, String visitante) {
        this.jornada = jornada;
        this.local = local;
        this.visitante = visitante;
    }

    public int getJornada() { return jornada; }
    public String getLocal() { return local; }
    public String getVisitante() { return visitante; }

    public Integer getGolesLocal() { return golesLocal; }
    public Integer getGolesVisitante() { return golesVisitante; }

    public boolean isPlayed() {
        return golesLocal != null && golesVisitante != null;
    }

    public void setResultado(Integer golesLocal, Integer golesVisitante) {
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
    }
}
