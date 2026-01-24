package erronka2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Junit_erronka2 {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testLoadTaldeakFromSer_WithDashToUnderscoreFallback() throws IOException {
        String season = "2024-2025";
        File serFile = new File(tempDir.toFile(), "taldeak_2024_2025.ser");

        List<Taldea> expected = new ArrayList<>();
        expected.add(new Taldea("Test Team"));

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFile))) {
            oos.writeObject(expected);
        }

        List<Taldea> result = GUIren_metodoak.loadTaldeakFromSer(null, season);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Team", result.get(0).getNombre());
    }

    @Test
    void testLoadTaldeakFromSer_WhenSeasonIsEmpty_FallsBackToDefaultFileName() throws IOException {
        // GUIren_metodoak.loadTaldeakFromSer always adds "taldeak.ser" as a candidate.
        File serFile = new File(tempDir.toFile(), "taldeak.ser");

        List<Taldea> expected = new ArrayList<>();
        expected.add(new Taldea("Default Team"));

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFile))) {
            oos.writeObject(expected);
        }

        List<Taldea> result = GUIren_metodoak.loadTaldeakFromSer(null, "");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Default Team", result.get(0).getNombre());
    }

    @Test
    void testLoadTaldeakFromSer_CandidatePrecedence_PrefersExactSeasonFileOverDefault() throws IOException {
        String season = "2024-2025";

        // Create both: exact season file and default file. The method should pick the first match.
        File exact = new File(tempDir.toFile(), "taldeak_" + season + ".ser");
        File fallback = new File(tempDir.toFile(), "taldeak.ser");

        List<Taldea> exactList = new ArrayList<>();
        exactList.add(new Taldea("Exact Season Team"));

        List<Taldea> fallbackList = new ArrayList<>();
        fallbackList.add(new Taldea("Fallback Team"));

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fallback))) {
            oos.writeObject(fallbackList);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(exact))) {
            oos.writeObject(exactList);
        }

        List<Taldea> result = GUIren_metodoak.loadTaldeakFromSer(null, season);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Exact Season Team", result.get(0).getNombre());
    }

    @Test
    void testLoadTaldeakFromDB_FallbackLogic_IsNotRequiredForUnitTest() throws IOException {
        // Previous version depended on local DB reachability which may vary per machine.
        // Unit test the fallback reader directly to keep the test deterministic.
        String season = "2024-2025";
        File serFile = new File(tempDir.toFile(), "taldeak_" + season + ".ser");

        List<Taldea> expected = new ArrayList<>();
        expected.add(new Taldea("Taldea Fichero"));

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFile))) {
            oos.writeObject(expected);
        }

        List<Taldea> result = GUIren_metodoak.loadTaldeakFromSer(null, season);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Taldea Fichero", result.get(0).getNombre());
    }

    @Test
    void testTaldea_AddJugador_DoesNotAddDuplicatesByNameSurnameDorsal() {
        Taldea t = new Taldea("A");

        Jokalaria j1 = new Jokalaria();
        j1.setNombre("Jon");
        j1.setApellido("Perez");
        j1.setDorsal(7);

        Jokalaria j2 = new Jokalaria();
        j2.setNombre("Jon");
        j2.setApellido("Perez");
        j2.setDorsal(7);

        t.addJugador(j1);
        t.addJugador(j2);

        assertNotNull(t.getJugadores());
        assertEquals(1, t.getJugadores().size(), "Duplicate player should not be added");
    }

    @Test
    void testDrainLastSqlAttempts_EmptiesTheBuffer() {
        List<String> logs1 = GUIren_metodoak.drainLastSqlAttempts();
        assertNotNull(logs1);

        List<String> logs2 = GUIren_metodoak.drainLastSqlAttempts();
        assertNotNull(logs2);
        assertTrue(logs2.isEmpty(), "The buffer should be empty after draining");
    }
}