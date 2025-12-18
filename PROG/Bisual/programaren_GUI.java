package erronka2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class programaren_GUI extends JFrame {

    private JComboBox<String> userTypeCombo;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel passwordLabel;

    public programaren_GUI() {
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Fondo de la ventana principal
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

        passwordLabel = new JLabel("Pasaitza:");
        passwordLabel.setForeground(Color.WHITE); // Texto visible sobre fondo rojo
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

        // MOSTRAR CONTRASEÑA PARA ADMIN Y ARBITRO
        userTypeCombo.addActionListener(e -> {
            String selected = (String) userTypeCombo.getSelectedItem();
            boolean necesitaPassword = selected.equals("Koordinatzailea") || selected.equals("Arbitroa");

            passwordLabel.setVisible(necesitaPassword);
            passwordField.setVisible(necesitaPassword);

            setSize(300, necesitaPassword ? 200 : 150);
        });

        // LOGIN
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
                    JOptionPane.showMessageDialog(this, "Pasaitza okerra");
                }
            } else if (selected.equals("Arbitroa")) {
                if (password.equals("arbitroa1234")) {
                    openArbitroWindow();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Pasaitza okerra");
                }
            }
        });

        setVisible(true);
    }

    // ------------------ VENTANA USUARIO ------------------
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

    // ------------------ VENTANA ADMIN ------------------
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
        panel.add(button1);

        JButton button2 = new JButton("KUDEATU JAURDUNALDIAK");
        button2.setBounds(75, 120, 300, 50);
        panel.add(button2);

        JButton button3 = new JButton("KUDEATU KLASIFIKASIOA");
        button3.setBounds(75, 180, 300, 50);
        panel.add(button3);

        adminFrame.add(panel);
        adminFrame.setVisible(true);
    }

    // ------------------ VENTANA ARBITRO ------------------
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
        panel.add(button2);

        JButton button3 = new JButton("KLASIFIKASIOA");
        button3.setBounds(125, 160, 150, 40);
        button3.addActionListener(e -> openKlasifikazioaWindow());
        panel.add(button3);

        arbitroFrame.add(panel);
        arbitroFrame.setVisible(true);
    }

    // ------------------ TALDEAK ------------------
    private void openTaldeakWindow() {
        JFrame frame = new JFrame("Taldeak");
        frame.setSize(700, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        frame.getContentPane().setBackground(Color.decode("#990000"));

        // --- DESPLEGABLE DE EQUIPOS ---
        String[] equipos = {"Equipo A", "Equipo B"};
        JComboBox<String> comboEquipos = new JComboBox<>(equipos);
        frame.add(comboEquipos, BorderLayout.NORTH);

        // --- TABLA ---
        String[] columnas = {
                "Nombre", "Apellido", "Edad", "Nacionalidad", "Posición", "Dorsal"
        };

        DefaultTableModel model = new DefaultTableModel(columnas, 0);
        JTable table = new JTable(model);
        table.setEnabled(false); // solo lectura
        table.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.decode("#990000"));
        frame.add(scrollPane, BorderLayout.CENTER);

        // --- DATOS DE EJEMPLO ---
        Object[][] equipoA = {
                {"Jon", "Garcia", 24, "España", "Delantero", 9},
                {"Iker", "Lopez", 27, "España", "Portero", 1}
        };

        Object[][] equipoB = {
                {"Aitor", "Perez", 22, "España", "Defensa", 5},
                {"Mikel", "Sanchez", 29, "España", "Centrocampista", 8}
        };

        // Cargar equipo inicial
        for (Object[] jugador : equipoA) {
            model.addRow(jugador);
        }

        // Cambio de equipo
        comboEquipos.addActionListener(e -> {
            model.setRowCount(0);
            Object[][] datos = comboEquipos.getSelectedItem().equals("Equipo A")
                    ? equipoA
                    : equipoB;

            for (Object[] jugador : datos) {
                model.addRow(jugador);
            }
        });

        frame.setVisible(true);
    }

    // ------------------ JAURDUNALDIAK ------------------
    private void openJaurdunaldiakWindow() {
        JFrame frame = new JFrame("Jaurdunaldiak");
        frame.setSize(850, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        frame.getContentPane().setBackground(Color.decode("#990000"));

        // --- DESPLEGABLE DE JORNADAS ---
        String[] jornadas = {"Jornada 1", "Jornada 2"};
        JComboBox<String> comboJornadas = new JComboBox<>(jornadas);
        frame.add(comboJornadas, BorderLayout.NORTH);

        // --- TABLA ---
        String[] columnas = {
                "ID Partido",
                "Etxeko taldea",
                "Kampoko taldea",
                "Data",
                "Non",
                "Etxeko taldearen golak",
                "Kampoko taldearen golak"
        };

        DefaultTableModel model = new DefaultTableModel(columnas, 0);
        JTable table = new JTable(model);
        table.setEnabled(false);
        table.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.decode("#990000"));
        frame.add(scrollPane, BorderLayout.CENTER);

        // --- DATOS DE EJEMPLO ---
        Object[][] jornada1 = {
                {"Equipo A", "Equipo B", "10/03/2025", "Estadio A", 2, 1},
                {"Equipo C", "Equipo D", "11/03/2025", "Estadio C", 0, 0}
        };

        Object[][] jornada2 = {
                {"Equipo B", "Equipo C", "17/03/2025", "Estadio B", 3, 2},
                {"Equipo D", "Equipo A", "18/03/2025", "Estadio D", 1, 4}
        };

        // --- GENERAR ID ALEATORIOS ÚNICOS ---
        java.util.Random rand = new java.util.Random();
        java.util.Set<Integer> idsUsados = new java.util.HashSet<>();

        java.util.function.Function<Object[][], Object[][]> agregarIDs = (datos) -> {
            Object[][] datosConID = new Object[datos.length][7];
            for (int i = 0; i < datos.length; i++) {
                int id;
                do {
                    id = rand.nextInt(10000) + 1;
                } while (idsUsados.contains(id));
                idsUsados.add(id);

                datosConID[i][0] = id;
                System.arraycopy(datos[i], 0, datosConID[i], 1, 6);
            }
            return datosConID;
        };

        Object[][] jornada1ConID = agregarIDs.apply(jornada1);
        Object[][] jornada2ConID = agregarIDs.apply(jornada2);

        for (Object[] partido : jornada1ConID) {
            model.addRow(partido);
        }

        comboJornadas.addActionListener(e -> {
            model.setRowCount(0);
            Object[][] datos = comboJornadas.getSelectedItem().equals("Jornada 1")
                    ? jornada1ConID
                    : jornada2ConID;

            for (Object[] partido : datos) {
                model.addRow(partido);
            }
        });

        frame.setVisible(true);
    }

    // ------------------ KLASIFIKASIOA ------------------
    private void openKlasifikazioaWindow() {
        JFrame frame = new JFrame("Klasifikazioa");
        frame.setSize(900, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        frame.getContentPane().setBackground(Color.decode("#990000"));

        String[] temporadas = {"2025-2026", "2026-2027"};
        JComboBox<String> comboTemporadas = new JComboBox<>(temporadas);
        frame.add(comboTemporadas, BorderLayout.NORTH);

        String[] columnas = {
                "Posizioa", "Talde Izena", "Puntuak", "JP", "IP", "GP", "BP", "AG", "KG", "GD"
        };
        DefaultTableModel model = new DefaultTableModel(columnas, 0);
        JTable table = new JTable(model);
        table.setEnabled(false);
        table.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.decode("#990000"));
        frame.add(scrollPane, BorderLayout.CENTER);

        Object[][] temporada1 = {
                {1, "Equipo A", 30, 12, 10, 2, 0, 25, 10, 15},
                {2, "Equipo B", 27, 12, 9, 3, 0, 20, 12, 8},
                {3, "Equipo C", 24, 12, 8, 4, 0, 18, 15, 3}
        };

        Object[][] temporada2 = {
                {1, "Equipo D", 32, 12, 11, 1, 0, 28, 8, 20},
                {2, "Equipo E", 28, 12, 9, 3, 0, 22, 10, 12},
                {3, "Equipo F", 25, 12, 8, 4, 0, 19, 12, 7}
        };

        for (Object[] fila : temporada1) {
            model.addRow(fila);
        }

        comboTemporadas.addActionListener(e -> {
            model.setRowCount(0);
            Object[][] datos = comboTemporadas.getSelectedItem().equals("2025-2026")
                    ? temporada1
                    : temporada2;

            for (Object[] fila : datos) {
                model.addRow(fila);
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(programaren_GUI::new);
    }
}
