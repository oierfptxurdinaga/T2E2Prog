package erronka2;

import java.io.*;
import java.util.*;

/**
 * Denboraldien egoerak (hasita/amaituta) kudeatzeko klasea.
 * 
 * Proiektu honetako erabilerak metodo estatikoen bidez egiten dira:
 * - isStarted / isFinalized
 * - setStarted / setFinalized
 * 
 * DB eskuragarri badago, GUIren_metodoak-en bidez gordetzen saiatzen da.
 * Bestela, season_states.ser fitxategian gordetzen du (fallback).
 */
public class DenboraldiarenKudeaketa implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILENAME = "season_states.ser";

    public static class SeasonState implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean started;
        private boolean finalized;

        public boolean isStarted() { return started; }
        public void setStarted(boolean s) { this.started = s; }
        public boolean isFinalized() { return finalized; }
        public void setFinalized(boolean f) { this.finalized = f; }
    }

    private final Map<String, SeasonState> map = new HashMap<>();
    private static final DenboraldiarenKudeaketa INSTANCE = load();

    private DenboraldiarenKudeaketa() {}

    private static DenboraldiarenKudeaketa load() {
        // DB first
        try {
            Map<String, boolean[]> db = GUIren_metodoak.loadAllSeasonStatesFromDB(null);
            if (db != null) {
                DenboraldiarenKudeaketa mgr = new DenboraldiarenKudeaketa();
                for (Map.Entry<String, boolean[]> e : db.entrySet()) {
                    SeasonState s = new SeasonState();
                    boolean[] v = e.getValue();
                    s.setStarted(v != null && v.length > 0 && v[0]);
                    s.setFinalized(v != null && v.length > 1 && v[1]);
                    mgr.map.put(e.getKey() == null ? "" : e.getKey(), s);
                }
                return mgr;
            }
        } catch (Throwable ignored) {
            // fallback
        }

        // file fallback
        File f = new File(System.getProperty("user.dir"), FILENAME);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = ois.readObject();
                if (obj instanceof DenboraldiarenKudeaketa) return (DenboraldiarenKudeaketa) obj;
            } catch (Exception ignored) {
                // ignore
            }
        }
        return new DenboraldiarenKudeaketa();
    }

    private synchronized void saveToFile() {
        File f = new File(System.getProperty("user.dir"), FILENAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        } catch (Exception ignored) {
        }
    }

    private synchronized void saveAllToDbOrFile() {
        boolean okAll = true;
        try {
            for (Map.Entry<String, SeasonState> e : map.entrySet()) {
                String season = e.getKey();
                SeasonState s = e.getValue();
                boolean ok = GUIren_metodoak.saveSeasonStateToDB(null, season, s.isStarted(), s.isFinalized());
                if (!ok) { okAll = false; break; }
            }
        } catch (Throwable t) {
            okAll = false;
        }
        if (!okAll) saveToFile();
    }

    public static synchronized SeasonState getStateFor(String season) {
        if (season == null) season = "";
        SeasonState s = INSTANCE.map.get(season);
        if (s == null) {
            s = new SeasonState();
            INSTANCE.map.put(season, s);
        }
        return s;
    }

    public static synchronized boolean isStarted(String season) {
        return getStateFor(season).isStarted();
    }

    public static synchronized boolean isFinalized(String season) {
        return getStateFor(season).isFinalized();
    }

    public static synchronized void setStarted(String season, boolean v) {
        SeasonState s = getStateFor(season);
        s.setStarted(v);
        boolean ok = false;
        try {
            ok = GUIren_metodoak.saveSeasonStateToDB(null, season, s.isStarted(), s.isFinalized());
        } catch (Throwable ignored) {}
        if (!ok) INSTANCE.saveAllToDbOrFile();
    }

    public static synchronized void setFinalized(String season, boolean v) {
        SeasonState s = getStateFor(season);
        s.setFinalized(v);
        boolean ok = false;
        try {
            ok = GUIren_metodoak.saveSeasonStateToDB(null, season, s.isStarted(), s.isFinalized());
        } catch (Throwable ignored) {}
        if (!ok) INSTANCE.saveAllToDbOrFile();
    }
}
