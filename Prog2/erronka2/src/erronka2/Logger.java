package erronka2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger klasea: Gertaera garrantzitsuak testu-fitxategi batean gordetzen ditu.
 * Log fitxategia: erronka2/logs/app_log.txt
 */
public class Logger {

    private static final String LOG_FILE = "logs/app_log.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Log motak
    public static final String INFO = "INFO";
    public static final String WARNING = "ABISUA";
    public static final String ERROR = "ERROREA";
    public static final String LOGIN = "SAIOA_HASI";
    public static final String LOGOUT = "SAIOA_ITXI";
    public static final String ACTION = "EKINTZA";
    public static final String DB = "DATU_BASEA";

    /**
     * Karpeta sortu existitzen ez bada
     */
    static {
        java.io.File logDir = new java.io.File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    /**
     * Mezua log fitxategian idatzi
     * @param type Log mota (INFO, WARNING, ERROR, LOGIN, LOGOUT, ACTION, DB)
     * @param message Mezua
     */
    public static void log(String type, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] [%s] %s", timestamp, type, message);

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE, true)))) {
            out.println(logEntry);
        } catch (IOException e) {
            System.err.println("Ezin izan da log-a idatzi: " + e.getMessage());
        }

        // Kontsolan ere erakutsi (debug modurako)
        System.out.println(logEntry);
    }

    /**
     * Informazio mezua
     */
    public static void info(String message) {
        log(INFO, message);
    }

    /**
     * Abisu mezua
     */
    public static void warning(String message) {
        log(WARNING, message);
    }

    /**
     * Errore mezua
     */
    public static void error(String message) {
        log(ERROR, message);
    }

    /**
     * Saio hasiera mezua
     */
    public static void login(String userType) {
        log(LOGIN, "Erabiltzailea sartu da: " + userType);
    }

    /**
     * Saio itxiera mezua
     */
    public static void logout(String userType) {
        log(LOGOUT, "Erabiltzailea irten da: " + userType);
    }

    /**
     * Ekintza mezua
     */
    public static void action(String action) {
        log(ACTION, action);
    }

    /**
     * Datu-basearekin lotutako mezua
     */
    public static void database(String message) {
        log(DB, message);
    }

    /**
     * Partidua editatu
     */
    public static void partiduaEditatua(String season, Object[] originalRow, Object[] newRow) {
        StringBuilder sb = new StringBuilder();
        sb.append("Partidua editatua denboraldian [").append(season).append("]: ");
        sb.append("Lehenagokoa: [");
        for (int i = 0; i < originalRow.length; i++) {
            sb.append(originalRow[i]);
            if (i < originalRow.length - 1) sb.append(", ");
        }
        sb.append("] -> Berria: [");
        for (int i = 0; i < newRow.length; i++) {
            sb.append(newRow[i]);
            if (i < newRow.length - 1) sb.append(", ");
        }
        sb.append("]");
        log(ACTION, sb.toString());
    }

    /**
     * Jokalaria gehitu
     */
    public static void jokalariaGehitua(String taldea, String izena, String abizena) {
        log(ACTION, "Jokalaria gehitua [" + taldea + "]: " + izena + " " + abizena);
    }

    /**
     * Jokalaria editatua
     */
    public static void jokalariaEditatua(String taldea, String izena, String abizena) {
        log(ACTION, "Jokalaria editatua [" + taldea + "]: " + izena + " " + abizena);
    }

    /**
     * Jokalaria ezabatua
     */
    public static void jokalariaEzabatua(String taldea, String izena, String abizena) {
        log(ACTION, "Jokalaria ezabatua [" + taldea + "]: " + izena + " " + abizena);
    }

    /**
     * Jokalaria trasposatua
     */
    public static void jokalariaTraspasatua(String jatorriakoTaldea, String helburukoTaldea, String izena) {
        log(ACTION, "Jokalaria traspasatua: " + izena + " [" + jatorriakoTaldea + "] -> [" + helburukoTaldea + "]");
    }

    /**
     * Denboraldia hasita
     */
    public static void denboraldiaHasita(String season) {
        log(ACTION, "Denboraldia hasita: " + season);
    }

    /**
     * Denboraldia amaituta
     */
    public static void denboraldiaAmaituta(String season) {
        log(ACTION, "Denboraldia amaituta: " + season);
    }

    /**
     * Leihoa irekita
     */
    public static void leihoaIrekita(String leihoIzena) {
        log(INFO, "Leihoa irekita: " + leihoIzena);
    }

    /**
     * Aldaketak gordeta
     */
    public static void aldaketakGordeta(String deskribapena) {
        log(ACTION, "Aldaketak gordeta: " + deskribapena);
    }
}
