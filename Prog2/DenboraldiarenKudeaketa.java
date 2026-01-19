package erronka2;

import java.io.*;
import java.util.*;
import javax.swing.*;

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

    private Map<String, SeasonState> map = new HashMap<>();

    private static DenboraldiarenKudeaketa INSTANCE = load();

    private DenboraldiarenKudeaketa() {}

    private static DenboraldiarenKudeaketa load() {
        // Try DB first via GUIren_metodoak helper; if DB unreachable return null
        try {
            Map<String, boolean[]> db = GUIren_metodoak.loadAllSeasonStatesFromDB(null);
            if (db != null) {
                DenboraldiarenKudeaketa mgr = new DenboraldiarenKudeaketa();
                for (Map.Entry<String, boolean[]> e : db.entrySet()) {
                    SeasonState s = new SeasonState();
                    boolean[] v = e.getValue();
                    s.setStarted(v != null && v.length > 0 ? v[0] : false);
                    s.setFinalized(v != null && v.length > 1 ? v[1] : false);
                    mgr.map.put(e.getKey() == null ? "" : e.getKey(), s);
                }
                return mgr;
            }
        } catch (Throwable ignored) {
            // fall back to file-based
        }

        // Fallback to file-based loading (legacy)
        File f = new File(System.getProperty("user.dir"), FILENAME);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = ois.readObject();
                if (obj instanceof DenboraldiarenKudeaketa) return (DenboraldiarenKudeaketa) obj;
            } catch (Exception e) {
                // ignore and return new manager
            }
        }
        return new DenboraldiarenKudeaketa();
    }

    private synchronized void save() {
        // Attempt DB save for all entries first; if DB unavailable, persist to file as fallback
        boolean anyDb = true;
        try {
            for (Map.Entry<String, SeasonState> e : map.entrySet()) {
                String season = e.getKey();
                SeasonState s = e.getValue();
                boolean ok = GUIren_metodoak.saveSeasonStateToDB(null, season, s.isStarted(), s.isFinalized());
                if (!ok) { anyDb = false; break; }
            }
        } catch (Throwable t) { anyDb = false; }

        if (anyDb) return; // saved to DB

        // fallback: write to file
        File f = new File(System.getProperty("user.dir"), FILENAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        } catch (Exception e) {
            // ignore
        }
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
        // try saving only this season to DB; if fails, persist whole manager to file
        boolean ok = GUIren_metodoak.saveSeasonStateToDB(null, season, s.isStarted(), s.isFinalized());
        if (!ok) INSTANCE.save();
    }

    public static synchronized void setFinalized(String season, boolean v) {
        SeasonState s = getStateFor(season);
        s.setFinalized(v);
        boolean ok = GUIren_metodoak.saveSeasonStateToDB(null, season, s.isStarted(), s.isFinalized());
        if (!ok) INSTANCE.save();
    }
}