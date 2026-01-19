package erronka2;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

public class programaren_GUI extends JFrame {

    private JComboBox<String> userTypeCombo;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel passwordLabel;

    public programaren_GUI() {
        setTitle("Saioa hasi");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // atzeko kolorea
        getContentPane().setBackground(Color.decode("#990000"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        userTypeCombo = new JComboBox<>(new String[] {
                "Erabiltzailea", "Arbitroa", "Koordinatzailea"
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(userTypeCombo, gbc);

        passwordLabel = new JLabel("Pasahitza:");
        passwordLabel.setForeground(Color.WHITE); 
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(passwordLabel, gbc);
        passwordLabel.setVisible(false);

        passwordField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(passwordField, gbc);
        passwordField.setVisible(false);

        loginButton = new JButton("Saioa hasi");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(loginButton, gbc);

        // ADMIN ETA ARBITROENTZAT PASAHITZA ERAKUTSI
        userTypeCombo.addActionListener(e -> {
            String selected = (String) userTypeCombo.getSelectedItem();
            boolean necesitaPassword = selected.equals("Koordinatzailea") || selected.equals("Arbitroa");

            passwordLabel.setVisible(necesitaPassword);
            passwordField.setVisible(necesitaPassword);

            setSize(300, necesitaPassword ? 200 : 150);
        });

        // SAIO-HAASIERA
        loginButton.addActionListener(e -> {
            String selected = (String) userTypeCombo.getSelectedItem();
            String password = new String(passwordField.getPassword());

            if (selected.equals("Erabiltzailea")) {
                openUserWindow();
                dispose();
            } else if (selected.equals("Koordinatzailea")) {
                if (password.equals("admin")) {
                    openAdminWindow();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Pasahitza okerra");
                }
            } else if (selected.equals("Arbitroa")) {
                if (password.equals("arbitroa1234")) {
                    openArbitroWindow();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Pasahitza okerra");
                }
            }
        });

        setVisible(true);
    }

    // ------------------ ERABILTZAILEA IREKI ------------------
    private void openUserWindow() {
        JFrame userFrame = new JFrame("Erabiltzailea");
        userFrame.setSize(400, 300);
        userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        userFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#990000"));
        panel.setLayout(null);

        userFrame.getContentPane().setBackground(Color.decode("#990000"));

        JButton backButton = new JButton("\u2B05");
        backButton.setBounds(10, 10, 50, 30);
        backButton.addActionListener(e -> {
            userFrame.dispose();
            new programaren_GUI();
        });
        panel.add(backButton);

        JButton button1 = new JButton("TALDEAK");
        button1.setBounds(125, 60, 150, 40);
        button1.addActionListener(e -> openTaldeakWindow());
        panel.add(button1);

        JButton button2 = new JButton("JAURDUNALDIAK");
        button2.setBounds(125, 110, 150, 40);
        button2.addActionListener(e -> openJaurdunaldiakWindow());
        panel.add(button2);

        JButton button3 = new JButton("KLASIFIKASIOA");
        button3.setBounds(125, 160, 150, 40);
        button3.addActionListener(e -> openKlasifikazioaWindow());
        panel.add(button3);

        userFrame.add(panel);
        userFrame.setVisible(true);
    }

    // ------------------ ADMINISTRATZAILEA IREKI ------------------
    private void openAdminWindow() {
        JFrame adminFrame = new JFrame("Koordinatzailea");
        adminFrame.setSize(450, 350);
        adminFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        adminFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#990000"));
        panel.setLayout(null);

        adminFrame.getContentPane().setBackground(Color.decode("#990000"));

        JButton backButton = new JButton("\u2B05");
        backButton.setBounds(10, 10, 50, 30);
        backButton.addActionListener(e -> {
            adminFrame.dispose();
            new programaren_GUI();
        });
        panel.add(backButton);

        JButton button1 = new JButton("KUDEATU JOKALARIAK/TALDEAK");
        button1.setBounds(75, 60, 300, 50);
        button1.addActionListener(e -> openKudeatuJokalariakTaldeakWindow());
        panel.add(button1);

        JButton button2 = new JButton("KUDEATU JAURDUNALDIAK");
        button2.setBounds(75, 120, 300, 50);
        button2.addActionListener(e -> openKudeatuJaurdunaldiakWindow());
        panel.add(button2);

        JButton button3 = new JButton("KUDEATU KLASIFIKASIOA");
        button3.setBounds(75, 180, 300, 50);
        panel.add(button3);

        adminFrame.add(panel);
        adminFrame.setVisible(true);
    }

    // ------------------ ARBITROA IREKI ------------------
    private void openArbitroWindow() {
        JFrame arbitroFrame = new JFrame("Arbitroa");
        arbitroFrame.setSize(400, 300);
        arbitroFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        arbitroFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#990000"));
        panel.setLayout(null);

        arbitroFrame.getContentPane().setBackground(Color.decode("#990000"));

        JButton backButton = new JButton("\u2B05");
        backButton.setBounds(10, 10, 50, 30);
        backButton.addActionListener(e -> {
            arbitroFrame.dispose();
            new programaren_GUI();
        });
        panel.add(backButton);

        JButton button1 = new JButton("TALDEAK");
        button1.setBounds(125, 60, 150, 40);
        button1.addActionListener(e -> openTaldeakWindow());
        panel.add(button1);

        JButton button2 = new JButton("JAURDUNALDIAK KUDEATU");
        button2.setBounds(100, 110, 200, 40);
        button2.addActionListener(e -> openKudeatuJaurdunaldiakWindow());
        panel.add(button2);

        JButton button3 = new JButton("KLASIFIKASIOA");
        button3.setBounds(125, 160, 150, 40);
        button3.addActionListener(e -> openKlasifikazioaWindow());
        panel.add(button3);

        arbitroFrame.add(panel);
        arbitroFrame.setVisible(true);
    }

    private void aplicarEstiloTabla(JTable table, JScrollPane scrollPane) {
        // Scroll-aren atzeko kolorea (gorria)
        scrollPane.getViewport().setBackground(Color.decode("#990000"));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(true);

        table.setOpaque(false);
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.WHITE);
        table.setRowHeight(25);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setOpaque(false); // Garrantzitsua gorria ikusteko
        renderer.setForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, renderer);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setOpaque(true);
    }

    // ------------------ TALDEAK IKUSKERA ------------------
    private void openTaldeakWindow() {
        JFrame frame = new JFrame("Taldeak");
        frame.setSize(700, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        frame.getContentPane().setBackground(Color.decode("#990000"));

        List<String> seasonsList = GUIren_metodoak.loadSeasonsFromDB(this);
        if (seasonsList == null) seasonsList = new ArrayList<>();

        if (seasonsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ez da denboraldirik aurkitu datu-basean.", "Informazioa", JOptionPane.INFORMATION_MESSAGE);
            JComboBox<String> comboEmpty = new JComboBox<>(new String[]{""});
            comboEmpty.setEnabled(false);
            JPanel topEmpty = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            topEmpty.setBackground(Color.decode("#990000"));
            JLabel seasonLblEmpty = new JLabel("Denboraldia:");
            seasonLblEmpty.setForeground(Color.WHITE);
            topEmpty.add(seasonLblEmpty);
            topEmpty.add(comboEmpty);
            frame.add(topEmpty, BorderLayout.NORTH);

            String[] columnasEmpty = {"Izen_abizena", "Dortsala", "Posizioa", "Jaiotze_data"};
            DefaultTableModel modelEmpty = new DefaultTableModel(columnasEmpty, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
            JTable tableEmpty = new JTable(modelEmpty);
            tableEmpty.setEnabled(false);
            JScrollPane scrollEmpty = new JScrollPane(tableEmpty);
            aplicarEstiloTabla(tableEmpty, scrollEmpty);
            frame.add(scrollEmpty, BorderLayout.CENTER);

            frame.setVisible(true);
            return;
        }

        JComboBox<String> comboSeasons = new JComboBox<>(seasonsList.toArray(new String[0]));
        comboSeasons.setSelectedIndex(0);

        JComboBox<String> comboEquipos = new JComboBox<>();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.setBackground(Color.decode("#990000"));
        JLabel seasonLabel = new JLabel("Denboraldia:");
        seasonLabel.setForeground(Color.WHITE);
        JLabel equipoLabel = new JLabel("Taldea:");
        equipoLabel.setForeground(Color.WHITE);
        topPanel.add(seasonLabel);
        topPanel.add(comboSeasons);
        topPanel.add(Box.createHorizontalStrut(8));
        topPanel.add(equipoLabel);
        topPanel.add(comboEquipos);
        
        // Taldeen ikuspegian denboraldiaren egoeraren etiketa ez erakusten (erabiltzailearen eskaeraren arabera)

        frame.add(topPanel, BorderLayout.NORTH);

        // --- TAULA ---
        String[] columnas = {"Izen_abizena", "Dortsala", "Posizioa", "Jaiotze_data"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setEnabled(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        aplicarEstiloTabla(table, scrollPane);
        
        frame.add(scrollPane, BorderLayout.CENTER);

        final boolean[] notified = new boolean[]{false};
        java.util.function.Supplier<List<Taldea>> loadForSelected = () -> {
            String season = (String) comboSeasons.getSelectedItem();
            // Datu-basea iturri nagusi bezala
            // GUIren_metodoak.loadTaldeakFromDB barruan beharrezkoena denean .ser-ra jotzen du, beraz hemen ez deitu
            List<Taldea> per = GUIren_metodoak.loadTaldeakFromDB(this, season);
            return per != null ? per : new ArrayList<>();
        };

        final List<Taldea> taldeak = new ArrayList<>();

        Runnable refreshUI = () -> {
            int previous = comboEquipos.getSelectedIndex();

            comboEquipos.removeAllItems();
            for (Taldea t : taldeak) comboEquipos.addItem(t.getNombre());

            model.setRowCount(0);

            if (previous >= 0 && previous < comboEquipos.getItemCount()) {
                comboEquipos.setSelectedIndex(previous);
            } else if (comboEquipos.getItemCount() > 0) {
                comboEquipos.setSelectedIndex(0);
            }

            int idx = comboEquipos.getSelectedIndex();
            if (idx >= 0 && idx < taldeak.size()) {
                Taldea sel = taldeak.get(idx);
                if (sel.getJugadores() != null) {
                    Set<String> seen = new HashSet<>();
                    for (Jokalaria j : sel.getJugadores()) {
                        String key = j.getNombre() + "|" + j.getApellido() + "|" + j.getDorsal();
                        if (seen.add(key)) {
                            String full = (j.getNombre() == null ? "" : j.getNombre()) + (j.getApellido() == null ? "" : " " + j.getApellido());
                            model.addRow(new Object[]{full.trim(), j.getDorsal(), orNA(j.getPosizioa()), orNA(j.getJaiotzeData())});
                        }
                    }
                }
            }
        };

        List<Taldea> initial = loadForSelected.get();
        taldeak.clear();
        taldeak.addAll(initial);
        refreshUI.run();

        final int[] previousSeasonIndex = new int[]{comboSeasons.getSelectedIndex()};

        comboSeasons.addActionListener(e -> {
            int newIndex = comboSeasons.getSelectedIndex();
            if (newIndex == previousSeasonIndex[0]) return;
            String season = (String) comboSeasons.getSelectedItem();
            List<Taldea> per = GUIren_metodoak.loadTaldeakFromDB(this, season);
            if (per == null || per.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Denboraldia " + season + " oraindik hasi ez da edo ez du daturik. Ezinezkoa da denboraldia aldatzea.");
                SwingUtilities.invokeLater(() -> comboSeasons.setSelectedIndex(previousSeasonIndex[0]));
                return;
            }
            previousSeasonIndex[0] = newIndex;
            suppressAllCombo(comboEquipos, true);
            try {
                taldeak.clear();
                taldeak.addAll(per);
                comboEquipos.removeAllItems();
                for (Taldea t : taldeak) comboEquipos.addItem(t.getNombre());
                if (comboEquipos.getItemCount() > 0) comboEquipos.setSelectedIndex(0);
                refreshUI.run();
            } finally {
                suppressAllCombo(comboEquipos, false);
            }
        });

        comboEquipos.addActionListener(e -> { if (!isComboSuppressed(comboEquipos)) refreshUI.run(); });

        frame.setVisible(true);
    }

    // ------------------ JAURDUNALDIAK IKUSKERA ------------------
    private void openJaurdunaldiakWindow() {
        JFrame frame = new JFrame("Jaurdunaldiak");
        frame.setSize(900, 450);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        frame.getContentPane().setBackground(Color.decode("#990000"));

        // DB-tik denboraldiak kargatu (aurreprogramatutako balioak ordezkatu)
        List<String> seasons = GUIren_metodoak.loadSeasonsFromDB(this);
        if (seasons == null) seasons = new ArrayList<>();

        if (seasons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ez da denboraldirik aurkitu datu-basean.", "Informazioa", JOptionPane.INFORMATION_MESSAGE);
            // Leiho huts bat erakutsi gailuarekin
            JComboBox<String> comboEmpty = new JComboBox<>(new String[]{""});
            comboEmpty.setEnabled(false);
            JPanel topEmpty = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            topEmpty.setBackground(Color.decode("#990000"));
            JLabel seasonLblEmpty = new JLabel("Denboraldia:");
            seasonLblEmpty.setForeground(Color.WHITE);
            topEmpty.add(seasonLblEmpty);
            topEmpty.add(comboEmpty);
            frame.add(topEmpty, BorderLayout.NORTH);

            DefaultTableModel modelEmpty = new DefaultTableModel();
            JTable tableEmpty = new JTable(modelEmpty);
            tableEmpty.setEnabled(false);
            JScrollPane scrollEmpty = new JScrollPane(tableEmpty);
            aplicarEstiloTabla(tableEmpty, scrollEmpty);
            frame.add(scrollEmpty, BorderLayout.CENTER);

            frame.setVisible(true);
            return;
        }

        JComboBox<String> comboSeasons = new JComboBox<>(seasons.toArray(new String[0]));
        comboSeasons.setSelectedIndex(0);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(Color.decode("#990000"));
        JLabel seasonLbl = new JLabel("Denboraldia:");
        seasonLbl.setForeground(Color.WHITE);
        top.add(seasonLbl);
        top.add(comboSeasons);

        frame.add(top, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setEnabled(false);
        JScrollPane scroll = new JScrollPane(table);
        aplicarEstiloTabla(table, scroll);
        frame.add(scroll, BorderLayout.CENTER);

        Runnable loadForSeason = () -> {
            String season = (String) comboSeasons.getSelectedItem();
            model.setRowCount(0);
            model.setColumnCount(0);

            Object[] res = GUIren_metodoak.loadPartidosFromDB(this, season);
            String[] cols = (res != null && res.length > 0 && res[0] instanceof String[]) ? (String[]) res[0] : new String[0];
            Object[][] datos = (res != null && res.length > 1 && res[1] instanceof Object[][]) ? (Object[][]) res[1] : new Object[0][0];

            if (cols.length == 0) {
                List<String> attempts = GUIren_metodoak.drainLastSqlAttempts();
                String msg = "Ez da partidurik aurkitu denboraldian honetan.\n\nProbatutako kontsultak jarraian (azken saiakerak):\n";
                if (attempts == null || attempts.isEmpty()) msg += "(no hay registros de intentos SQL)";
                else {
                    for (String s : attempts) {
                        msg += "\n" + s;
                    }
                }
                JTextArea area = new JTextArea(msg);
                area.setEditable(false);
                JScrollPane sp = new JScrollPane(area);
                sp.setPreferredSize(new java.awt.Dimension(700, 300));
                JOptionPane.showMessageDialog(frame, sp, "Informazioa / Diagnostikoak", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (String c : cols) model.addColumn(c);
            for (Object[] row : datos) {
                Object[] r = new Object[cols.length];
                for (int i = 0; i < cols.length; i++) r[i] = (i < row.length) ? row[i] : null;
                model.addRow(r);
            }
        };

        // Goiko panelean egoera etiketa ezagutu eta eguneraketak lotu
        top.add(Box.createHorizontalStrut(12));
        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(Color.WHITE);
        top.add(statusLabel);

        // Denboraldiaren egoera eguneratzeko funtzioa
        Runnable updateSeasonStatus = () -> {
            String season = (String) comboSeasons.getSelectedItem();
            if (season == null) return;
            boolean started = DenboraldiarenKudeaketa.isStarted(season);
            boolean finalized = DenboraldiarenKudeaketa.isFinalized(season);
            String st = finalized ? "Amaituta" : (started ? "Hasita" : "Ez hasita");
            statusLabel.setText("Egoera: " + st);
        };

        // Entzule bakarra: datuak berriro kargatu eta egoera eguneratu
        comboSeasons.addActionListener(e -> { loadForSeason.run(); updateSeasonStatus.run(); });

        loadForSeason.run();

        frame.setVisible(true);
    }

    // ------------------ JAURDUNALDIAK KUDEATU (ADMIN) ------------------
    private void openKudeatuJaurdunaldiakWindow() {
        JFrame frame = new JFrame("KUDEATU JAURDUNALDIAK");
        frame.setSize(950, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(Color.decode("#990000"));

        List<String> seasons = GUIren_metodoak.loadSeasonsFromDB(this);
        if (seasons == null) seasons = new ArrayList<>();
        if (seasons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ez da denboraldirik aurkitu datu-basean.", "Informazioa", JOptionPane.INFORMATION_MESSAGE);
            JComboBox<String> comboEmpty = new JComboBox<>(new String[]{""});
            comboEmpty.setEnabled(false);
            JPanel topEmpty = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            topEmpty.setBackground(Color.decode("#990000"));
            JLabel seasonLblEmpty = new JLabel("Denboraldia:");
            seasonLblEmpty.setForeground(Color.WHITE);
            topEmpty.add(seasonLblEmpty);
            topEmpty.add(comboEmpty);
            frame.add(topEmpty, BorderLayout.NORTH);

            DefaultTableModel modelEmpty = new DefaultTableModel();
            JTable tableEmpty = new JTable(modelEmpty);
            tableEmpty.setEnabled(false);
            JScrollPane scrollEmpty = new JScrollPane(tableEmpty);
            aplicarEstiloTabla(tableEmpty, scrollEmpty);
            frame.add(scrollEmpty, BorderLayout.CENTER);

            frame.setVisible(true);
            return;
        }

        JComboBox<String> comboSeasons = new JComboBox<>(seasons.toArray(new String[0]));
        comboSeasons.setSelectedIndex(0);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(Color.decode("#990000"));
        JLabel seasonLbl = new JLabel("Denboraldia:"); seasonLbl.setForeground(Color.WHITE);
        top.add(seasonLbl); top.add(comboSeasons);

        frame.add(top, BorderLayout.NORTH);

        // Taularen model dinamikoa partiduentzat
        DefaultTableModel model = new DefaultTableModel() { @Override public boolean isCellEditable(int row, int column) { return false; } };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        aplicarEstiloTabla(table, scroll);
        frame.add(scroll, BorderLayout.CENTER);

        // Denboraldirako partiduen karga
        Runnable loadForSeason = () -> {
            String season = (String) comboSeasons.getSelectedItem();
            model.setRowCount(0);
            model.setColumnCount(0);

            Object[] res = GUIren_metodoak.loadPartidosFromDB(this, season);
            String[] cols = (res != null && res.length > 0 && res[0] instanceof String[]) ? (String[]) res[0] : new String[0];
            Object[][] datos = (res != null && res.length > 1 && res[1] instanceof Object[][]) ? (Object[][]) res[1] : new Object[0][0];

            if (cols.length == 0) {
                List<String> attempts = GUIren_metodoak.drainLastSqlAttempts();
                String msg = "Ez da partidurik aurkitu denboraldian honetan.\n\nProbatutako kontsultak jarraian (azken saiakerak):\n";
                if (attempts == null || attempts.isEmpty()) msg += "(no hay registros de intentos SQL)";
                else {
                    for (String s : attempts) {
                        msg += "\n" + s;
                    }
                }
                JTextArea area = new JTextArea(msg);
                area.setEditable(false);
                JScrollPane sp = new JScrollPane(area);
                sp.setPreferredSize(new java.awt.Dimension(700, 300));
                JOptionPane.showMessageDialog(frame, sp, "Informazioa / Diagnostikoak", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (String c : cols) model.addColumn(c);
            for (Object[] row : datos) {
                Object[] r = new Object[cols.length];
                for (int i = 0; i < cols.length; i++) r[i] = (i < row.length) ? row[i] : null;
                model.addRow(r);
            }
        };
        comboSeasons.addActionListener(e -> loadForSeason.run());

        // Admin ikuspegian denboraldiaren egoera erakutsi
        top.add(Box.createHorizontalStrut(12));
        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(Color.WHITE);
        top.add(statusLabel);

        // Denboraldiaren egoera eguneratzeko funtzioa
        Runnable updateSeasonStatus = () -> {
            String season = (String) comboSeasons.getSelectedItem();
            if (season == null) return;
            boolean started = DenboraldiarenKudeaketa.isStarted(season);
            boolean finalized = DenboraldiarenKudeaketa.isFinalized(season);
            String st = finalized ? "Amaituta" : (started ? "Hasita" : "Ez hasita");
            statusLabel.setText("Egoera: " + st);
        };

        // entzule bakarra: datuak berriro kargatu eta egoera eguneratu
        comboSeasons.addActionListener(e -> { loadForSeason.run(); updateSeasonStatus.run(); });

        // Beheko kontrolak: editatu, berrikusi, itzuli
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.setBackground(Color.decode("#990000"));
        JButton editBtn = new JButton("Partidua editatu");
        JButton refreshBtn = new JButton("Berrikusi");
        JButton backBtn = new JButton("\\u2B05");

        editBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) { JOptionPane.showMessageDialog(frame, "Aukeratu partidua lehenik."); return; }

            // Denboraldia amaituta badago, ezin da editatu
            String season = (String) comboSeasons.getSelectedItem();
            if (DenboraldiarenKudeaketa.isFinalized(season)) {
                JOptionPane.showMessageDialog(frame, "Denboraldia amaituta dago. Partiduen emaitzak ezin dira aldatu.");
                return;
            }

            // get current columns
            int colCount = model.getColumnCount();
            if (colCount < 1) { JOptionPane.showMessageDialog(frame, "Ezin da informazioa lortu."); return; }

            // capture original row values and column names
            Object[] originalRow = new Object[colCount];
            String[] colNames = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                originalRow[i] = model.getValueAt(sel, i);
                colNames[i] = model.getColumnName(i);
            }

            // Determine if the first column looks like an identifier (id/kod/...)
            String firstColName = colNames[0] == null ? "" : colNames[0].toLowerCase();
            boolean firstIsId = firstColName.contains("id") || firstColName.contains("kod") || firstColName.contains("codigo") || firstColName.contains("key") || firstColName.contains("kod_partidua") || firstColName.contains("kodpartidua");

            Object[] newRow = new Object[colCount];

            if (firstIsId) {
                // keep id at position 0 and prompt for other columns
                newRow[0] = originalRow[0];
                for (int c = 1; c < colCount; c++) {
                    String colName = colNames[c];
                    Object cur = originalRow[c];
                    String curStr = cur == null ? "" : String.valueOf(cur);
                    String nv = JOptionPane.showInputDialog(frame, colName + ":", curStr);
                    if (nv == null) return; // cancelled
                    newRow[c] = nv;
                }
            } else {
                // first column is not an id (likely 'Data' or similar) -> prompt for all columns including column 0
                for (int c = 0; c < colCount; c++) {
                    String colName = colNames[c];
                    Object cur = originalRow[c];
                    String curStr = cur == null ? "" : String.valueOf(cur);
                    String nv = JOptionPane.showInputDialog(frame, colName + ":", curStr);
                    if (nv == null) return; // cancelled
                    newRow[c] = nv;
                }
            }

            boolean ok = GUIren_metodoak.updatePartidoInDB(this, originalRow, newRow, colNames);
            if (ok) {
                // update table model values with what user entered
                for (int c = 0; c < colCount; c++) {
                    if (newRow[c] != null) model.setValueAt(newRow[c], sel, c);
                }

                // Do NOT recalculate sailkapena automatically based on match results anymore.
                // Previously the code called GUIren_metodoak.recalculateAndSaveSailkapena(frame, season);
                // We keep only a simple confirmation message.
                JOptionPane.showMessageDialog(frame, "Partidua eguneratua.");

                // refresh table and status
                try { loadForSeason.run(); } catch (Exception ex) { /* ignore */ }
                try { updateSeasonStatus.run(); } catch (Exception ex) { /* ignore */ }

            } else {
                JOptionPane.showMessageDialog(frame, "Ezin izan da partidua eguneratu.", "Errorea", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadForSeason.run());
        backBtn.addActionListener(e -> { frame.dispose(); new programaren_GUI(); });

        bottom.add(editBtn); bottom.add(refreshBtn); bottom.add(backBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        // Hasieratu/bukatu botoiak egoera gordetzeko eta UI eguneratzeko lotu
        // Hasieratu/bukatu kontrolak leiho honetatik kenduta daude erabiltzailearen eskaeragatik

        // hasierako karga
        loadForSeason.run();

        frame.setVisible(true);
    }

    // ADMIN-ek jokalariak eta taldeak kudeatzeko leihoa
    private void openKudeatuJokalariakTaldeakWindow() {
        JFrame frame = new JFrame("KUDEATU JOKALARIAK / TALDEAK");
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(Color.decode("#990000"));

        // Flag para rastrear si hay cambios pendientes
        final boolean[] hasChanges = new boolean[]{false};

        List<String> seasons = GUIren_metodoak.loadSeasonsFromDB(this);
        if (seasons == null) seasons = new ArrayList<>();
        if (seasons.isEmpty()) {
            // Fallback: oraindik .ser fitxategi bidez editatu ahal izateko aukera utzi
            seasons.add("");
        }

        JComboBox<String> comboSeasons = new JComboBox<>(seasons.toArray(new String[0]));
        if (comboSeasons.getItemCount() > 0) comboSeasons.setSelectedIndex(0);

        JComboBox<String> comboEquipos = new JComboBox<>();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(Color.decode("#990000"));
        JLabel seasonLbl = new JLabel("Denboraldia:"); seasonLbl.setForeground(Color.WHITE);
        JLabel equipoLbl = new JLabel("Taldea:"); equipoLbl.setForeground(Color.WHITE);
        top.add(seasonLbl); top.add(comboSeasons); top.add(Box.createHorizontalStrut(8)); top.add(equipoLbl); top.add(comboEquipos);

        frame.add(top, BorderLayout.NORTH);

        String[] columnas = {"Izen_abizena", "Dortsala", "Posizioa", "Jaiotze_data", "NANa", "Helbidea", "Tlfn"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        aplicarEstiloTabla(table, scroll);
        frame.add(scroll, BorderLayout.CENTER);

        // Taldeak kargatu eta memoriaan mantendu editatzeko
        final List<Taldea> taldeak = new ArrayList<>();
        Runnable reloadTeamsForSeason = () -> {
            String season = (String) comboSeasons.getSelectedItem();
            // DBa iturri nagusi bezala erabili. GUIren_metodoak barruan .ser-ra joango da soilik
            // DBa eskuragaitza bada edo eskema egokia ez bada.
            List<Taldea> fromDb = GUIren_metodoak.loadTaldeakFromDB(this, season);
            taldeak.clear();
            if (fromDb != null && !fromDb.isEmpty()) {
                taldeak.addAll(fromDb);
            }

            comboEquipos.removeAllItems();
            for (Taldea t : taldeak) comboEquipos.addItem(t.getNombre());
            if (comboEquipos.getItemCount() > 0) comboEquipos.setSelectedIndex(0);
            hasChanges[0] = false; // Reiniciar flag al cargar
        };

        Runnable refreshTable = () -> {
            model.setRowCount(0);
            int idx = comboEquipos.getSelectedIndex();
            if (idx >= 0 && idx < taldeak.size()) {
                Taldea sel = taldeak.get(idx);
                if (sel.getJugadores() != null) {
                    Set<String> seen = new HashSet<>();
                    for (Jokalaria j : sel.getJugadores()) {
                        String key = j.getNombre() + "|" + j.getApellido() + "|" + j.getDorsal();
                        if (seen.add(key)) {
                            String full = (j.getNombre() == null ? "" : j.getNombre()) + (j.getApellido() == null ? "" : " " + j.getApellido());
                            model.addRow(new Object[]{full.trim(), j.getDorsal(), orNA(j.getPosizioa()), orNA(j.getJaiotzeData()), orNA(j.getNana()), orNA(j.getHelbidea()), orNA(j.getTlfn())});
                        }
                    }
                }
            }
        };

        // WindowListener para preguntar al cerrar
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (hasChanges[0]) {
                    int option = JOptionPane.showConfirmDialog(frame, 
                        "Aldaketak gorde nahi dituzu datu-basean?", 
                        "Aldaketak gorde", 
                        JOptionPane.YES_NO_CANCEL_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
                    if (option == JOptionPane.YES_OPTION) {
                        // Guardar cambios en la base de datos
                        String season = (String) comboSeasons.getSelectedItem();
                        boolean saved = GUIren_metodoak.saveTaldeakToDB(frame, taldeak, season);
                        if (saved) {
                            JOptionPane.showMessageDialog(frame, "Aldaketak gordeta.");
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Ezin izan dira aldaketak gorde.", "Errorea", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (option == JOptionPane.NO_OPTION) {
                        // No guardar, simplemente cerrar
                        frame.dispose();
                    }
                    // Si es CANCEL, no hacer nada (no cerrar)
                } else {
                    frame.dispose();
                }
            }
        });

        comboSeasons.addActionListener(e -> {
            reloadTeamsForSeason.run();
            refreshTable.run();
            String s = (String) comboSeasons.getSelectedItem();
            if (s != null) {
                boolean started = DenboraldiarenKudeaketa.isStarted(s);
            }
        });

        comboEquipos.addActionListener(e -> refreshTable.run());

        // Beheko kontrolak
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.setBackground(Color.decode("#990000"));
        JButton addBtn = new JButton("Gehitu Jokalaria");
        JButton editBtn = new JButton("Editatu Jokalaria");
        JButton delBtn = new JButton("Ezabatu Jokalaria");
        JButton transferBtn = new JButton("Traspasatu Jokalaria");
        JButton backBtn = new JButton("\u2B05");

        bottom.add(addBtn); bottom.add(editBtn); bottom.add(delBtn); bottom.add(transferBtn); bottom.add(backBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        // Ekintzak
        addBtn.addActionListener(ae -> {
            int tIdx = comboEquipos.getSelectedIndex();
            if (tIdx < 0 || tIdx >= taldeak.size()) { JOptionPane.showMessageDialog(frame, "Aukeratu taldea lehenik."); return; }
            Taldea team = taldeak.get(tIdx);

            String s = (String) comboSeasons.getSelectedItem();
            if (s != null && DenboraldiarenKudeaketa.isStarted(s)) {
                JOptionPane.showMessageDialog(frame, "Ezin da jokalaririk gehitu denboraldia hasi ondoren.");
                return;
            }

            JTextField nombreF = new JTextField();
            JTextField apellidoF = new JTextField();
            JTextField dorsalF = new JTextField();
            JTextField posF = new JTextField();
            JTextField jaiF = new JTextField();
            JTextField nanaF = new JTextField();
            JTextField helbF = new JTextField();
            JTextField tlfnF = new JTextField();

            Object[] inputs = {
                "Izena:", nombreF,
                "Abizena:", apellidoF,
                "Dortsala:", dorsalF,
                "Posizioa:", posF,
                "Jaiotze data:", jaiF,
                "NANa:", nanaF,
                "Helbidea:", helbF,
                "Tlfn:", tlfnF
            };
            int ok = JOptionPane.showConfirmDialog(frame, inputs, "Gehitu Jokalaria", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;

            Jokalaria j = new Jokalaria();
            j.setNombre(nombreF.getText().trim());
            j.setApellido(apellidoF.getText().trim());
            try { j.setDorsal(Integer.parseInt(dorsalF.getText().trim())); } catch (Exception ex) { j.setDorsal(0); }
            j.setPosizioa(posF.getText().trim());
            j.setJaiotzeData(jaiF.getText().trim());
            j.setNana(nanaF.getText().trim());
            j.setHelbidea(helbF.getText().trim());
            j.setTlfn(tlfnF.getText().trim());
            j.setTaldea(team.getNombre());

            team.addJugador(j);
            hasChanges[0] = true; // Marcar que hay cambios
            refreshTable.run();
        });

        editBtn.addActionListener(ae -> {
            int tIdx = comboEquipos.getSelectedIndex();
            int r = table.getSelectedRow();
            if (tIdx < 0 || tIdx >= taldeak.size()) { JOptionPane.showMessageDialog(frame, "Aukeratu taldea lehenik."); return; }
            if (r < 0) { JOptionPane.showMessageDialog(frame, "Aukeratu jokalaria lehenik."); return; }
            Taldea team = taldeak.get(tIdx);
            Jokalaria sel = team.getJugadores().get(r);

            String s = (String) comboSeasons.getSelectedItem();
            if (s != null && DenboraldiarenKudeaketa.isStarted(s)) {
                JOptionPane.showMessageDialog(frame, "Ezin da jokarien datuak aldatu denboraldia hasi ondoren.");
                return;
            }

            JTextField nombreF = new JTextField(sel.getNombre());
            JTextField apellidoF = new JTextField(sel.getApellido());
            JTextField dorsalF = new JTextField(String.valueOf(sel.getDorsal()));
            JTextField posF = new JTextField(sel.getPosizioa());
            JTextField jaiF = new JTextField(sel.getJaiotzeData());
            JTextField nanaF = new JTextField(sel.getNana());
            JTextField helbF = new JTextField(sel.getHelbidea());
            JTextField tlfnF = new JTextField(sel.getTlfn());

            Object[] inputs = {
                "Izena:", nombreF,
                "Abizena:", apellidoF,
                "Dortsala:", dorsalF,
                "Posizioa:", posF,
                "Jaiotze data:", jaiF,
                "NANa:", nanaF,
                "Helbidea:", helbF,
                "Tlfn:", tlfnF
            };
            int ok = JOptionPane.showConfirmDialog(frame, inputs, "Editatu Jokalaria", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;

            sel.setNombre(nombreF.getText().trim());
            sel.setApellido(apellidoF.getText().trim());
            try { sel.setDorsal(Integer.parseInt(dorsalF.getText().trim())); } catch (Exception ex) { sel.setDorsal(0); }
            sel.setPosizioa(posF.getText().trim());
            sel.setJaiotzeData(jaiF.getText().trim());
            sel.setNana(nanaF.getText().trim());
            sel.setHelbidea(helbF.getText().trim());
            sel.setTlfn(tlfnF.getText().trim());

            hasChanges[0] = true; // Marcar que hay cambios
            refreshTable.run();
        });

        delBtn.addActionListener(ae -> {
            int tIdx = comboEquipos.getSelectedIndex();
            int r = table.getSelectedRow();
            if (tIdx < 0 || tIdx >= taldeak.size()) { JOptionPane.showMessageDialog(frame, "Aukeratu taldea lehenik."); return; }
            if (r < 0) { JOptionPane.showMessageDialog(frame, "Aukeratu jokalaria lehenik."); return; }
            Taldea team = taldeak.get(tIdx);
            Jokalaria sel = team.getJugadores().get(r);
            int ok = JOptionPane.showConfirmDialog(frame, "Ziur zaude jokalaria ezabatu nahi duzula?", "Konfirmatu", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            team.removeJugador(sel);
            hasChanges[0] = true; // Marcar que hay cambios
            refreshTable.run();
        });

        // Traspaso ekintza: hautatutako jokalaria beste taldera mugitzea (DB eguneraketa beharrezkoa)
        transferBtn.addActionListener(ae -> {
            int tIdx = comboEquipos.getSelectedIndex();
            int r = table.getSelectedRow();
            if (tIdx < 0 || tIdx >= taldeak.size()) { JOptionPane.showMessageDialog(frame, "Aukeratu taldea lehenik."); return; }
            if (r < 0) { JOptionPane.showMessageDialog(frame, "Aukeratu jokalaria lehenik."); return; }

            String season = (String) comboSeasons.getSelectedItem();
            boolean started = season != null && DenboraldiarenKudeaketa.isStarted(season);
            boolean finalized = season != null && DenboraldiarenKudeaketa.isFinalized(season);
            if (started && !finalized) {
                JOptionPane.showMessageDialog(frame, "Ezin da traspasorik egin denboraldia hasi eta amaitu gabe dagoenean.");
                return;
            }

            Taldea fromTeam = taldeak.get(tIdx);
            Jokalaria sel = fromTeam.getJugadores().get(r);

            String nana = sel.getNana();
            if (nana == null || nana.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ezin da DB eguneratu: NANa beharrezkoa da.");
                return;
            }

            List<String> names = new ArrayList<>();
            for (Taldea t : taldeak) if (!t.getNombre().equalsIgnoreCase(fromTeam.getNombre())) names.add(t.getNombre());
            if (names.isEmpty()) { JOptionPane.showMessageDialog(frame, "Ez da beste talde bakar bat ere aurkitu."); return; }

            JComboBox<String> targetCombo = new JComboBox<>(names.toArray(new String[0]));
            int ok = JOptionPane.showConfirmDialog(frame, new Object[]{"Aukeratu taldea:", targetCombo}, "Traspasatu Jokalaria", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            String targetTeamName = (String) targetCombo.getSelectedItem();
            if (targetTeamName == null || targetTeamName.trim().isEmpty()) return;

            boolean dbOk = GUIren_metodoak.updateJokalariaTaldeaInDB(frame, nana, targetTeamName);
            if (!dbOk) {
                return;
            }

            fromTeam.removeJugador(sel);
            sel.setTaldea(targetTeamName);
            Taldea target = null;
            for (Taldea t : taldeak) if (t.getNombre().equalsIgnoreCase(targetTeamName)) { target = t; break; }
            if (target == null) { target = new Taldea(targetTeamName); taldeak.add(target); comboEquipos.addItem(targetTeamName); }
            target.addJugador(sel);

            refreshTable.run();
            JOptionPane.showMessageDialog(frame, "Jokalariaren traspasoa eginda.");
        });

        backBtn.addActionListener(e -> {
            // Utilizar el mismo comportamiento que al cerrar la ventana
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });

        // hasierako karga
        reloadTeamsForSeason.run();
        refreshTable.run();

        frame.setVisible(true);
    }

    private void openKlasifikazioaWindow() {
        JFrame frame = new JFrame("Klasifikazioa / Sailkapena");
        frame.setSize(800, 450);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(Color.decode("#990000"));

        // GOI: denboraldi aukeragilea
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(Color.decode("#990000"));
        JLabel lbl = new JLabel("Denboraldia:"); lbl.setForeground(Color.WHITE);
        top.add(lbl);

        // Denboraldi comboa (reloadSeasons-ek beteko du)
        JComboBox<String> comboSeasons = new JComboBox<>();
        top.add(comboSeasons);

        Runnable reloadSeasons = () -> {
            SwingUtilities.invokeLater(() -> {
                List<String> s = GUIren_metodoak.getAvailableSailkapenaSeasons(this);
                if (s == null) s = new ArrayList<>();
                Object prev = comboSeasons.getSelectedItem();
                comboSeasons.removeAllItems();
                if (s.isEmpty()) {
                    comboSeasons.addItem("2024-2025");
                    comboSeasons.addItem("2025-2026");
                } else {
                    for (String v : s) comboSeasons.addItem(v);
                }
                if (prev != null) comboSeasons.setSelectedItem(prev);
                if (comboSeasons.getItemCount() > 0 && comboSeasons.getSelectedItem() == null) comboSeasons.setSelectedIndex(0);
            });
        };

        // hasierako denboraldi karga
        reloadSeasons.run();

        frame.add(top, BorderLayout.NORTH);

        // Taula eremua (hasieran hutsik)
        DefaultTableModel model = new DefaultTableModel(new String[0], 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setEnabled(false);
        JScrollPane scroll = new JScrollPane(table);
        aplicarEstiloTabla(table, scroll);
        frame.add(scroll, BorderLayout.CENTER);

        // Karga funtzioa
        Runnable loadForSeason = () -> {
            String season = (String) comboSeasons.getSelectedItem();
            if (season == null) return;
            Object[] res = GUIren_metodoak.loadSailkapenaFromDB(this, season);
            String[] cols = (res != null && res.length > 0 && res[0] instanceof String[]) ? (String[]) res[0] : new String[0];
            Object[][] rows = (res != null && res.length > 1 && res[1] instanceof Object[][]) ? (Object[][]) res[1] : new Object[0][0];

            SwingUtilities.invokeLater(() -> {
                model.setColumnCount(0);
                model.setRowCount(0);
                if (cols.length == 0) {
                    JOptionPane.showMessageDialog(frame, "Ez da sailkapenik aurkitu denboraldi honetan.", "Informazioa", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                for (String c : cols) model.addColumn(c);
                for (Object[] r : rows) model.addRow(r);
            });
        };

        comboSeasons.addActionListener(e -> {
            // gertaerak baztertu hutsa bada
            if (comboSeasons.getSelectedItem() == null) return;
            loadForSeason.run();
        });

         // hasierako karga
         loadForSeason.run();

         frame.setVisible(true);
    }

    // --------------------------------------------------------
    // LAGUNGARRI METODOAK - Combo kontrolaren erreentradak saihesteko
    // --------------------------------------------------------
    private Map<JComboBox, Boolean> suppressedCombos = new HashMap<>();

    private void suppressAllCombo(JComboBox box, boolean suppress) {
        suppressedCombos.put(box, suppress);
    }

    private boolean isComboSuppressed(JComboBox box) {
        return suppressedCombos.getOrDefault(box, false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new programaren_GUI();
        });
    }

    private String orNA(String v) {
        return (v == null || v.trim().isEmpty()) ? "N/A" : v;
    }
}
