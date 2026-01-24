package erronka2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Jokalariz osatutako Talde bat irudikatzen du.
 * Jokalari bikoiztuak saihesteko logika inplementatzen du gako konposatu batean oinarrituta.
 */
class Taldea implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    
    /** Taldeko jokalarien zerrenda. */
    private List<Jokalaria> jugadores;

    /**
     * Eraikitzaile hutsa. Jokalarien zerrenda hasieratzen du.
     */
    public Taldea() {
        this.nombre = "";
        this.jugadores = new ArrayList<>();
    }

    /**
     * Taldearen izenarekin eraikitzailea.
     * @param nombre Taldearen izena.
     */
    public Taldea(String nombre) {
        this.nombre = nombre;
        this.jugadores = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Jokalaria> getJugadores() {
        return jugadores;
    }

    /**
     * Jokalarien zerrenda ezartzen du bikoiztuak iragaziz.
     * Zerrenda garbi bat sortzen du izena|abizena|dortsala erabiliz gako gisa.
     * * @param jugadores Jatorrizko jokalari zerrenda.
     */
    public void setJugadores(List<Jokalaria> jugadores) {
        // zerrenda ezarri aurretik normalizatu eta errepikapenak kendu
        if (jugadores == null) {
            this.jugadores = new ArrayList<>();
            return;
        }
        List<Jokalaria> clean = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Jokalaria j : jugadores) {
            if (j == null) continue;
            String key = j.getNombre() + "|" + j.getApellido() + "|" + j.getDorsal();
            if (seen.add(key)) clean.add(j);
        }
        this.jugadores = clean;
    }

    /**
     * Jokalari bat gehitzen du zerrendara aurretik existitzen ez bada.
     * Existentzia izena, abizena eta dortsalaren bidez egiaztatzen da.
     * * @param j Gehitu beharreko Jokalaria objektua.
     */
    public void addJugador(Jokalaria j) {
        if (j == null) return;
        if (this.jugadores == null) this.jugadores = new ArrayList<>();
        // errepikapenak saihestu: nombre|apellido|dorsal oinarrituta
        String key = j.getNombre() + "|" + j.getApellido() + "|" + j.getDorsal();
        for (Jokalaria existing : this.jugadores) {
            if (existing == null) continue;
            String ek = existing.getNombre() + "|" + existing.getApellido() + "|" + existing.getDorsal();
            if (ek.equals(key)) return; // jada dago
        }
        this.jugadores.add(j);
    }

    /**
     * Jokalari bat zerrendatik ezabatzen du.
     * @param j Ezabatu beharreko Jokalaria objektua.
     */
    public void removeJugador(Jokalaria j) {
        if (this.jugadores != null) this.jugadores.remove(j);
    }

    @Override
    public String toString() {
        return "Taldea{" + "nombre='" + nombre + '\'' + ", jugadores=" + (jugadores != null ? jugadores.size() : 0) + '}';
    }
}