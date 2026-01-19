package erronka2;

import javax.swing.*;
import java.awt.Component;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
	
public class GUIren_metodoak {

	// KONEXIO DATUAK (Aldatu pasahitza behar izanez gero)
	// Erabili 127.0.0.1 eta gehitu parametro arruntak timezone/SSL/public-key arazoak saihesteko
	// Gehitu connectTimeout eta autoReconnect sare arazo txikiei aurre egiteko
	private static final String URL = "jdbc:mysql://127.0.0.1:3306/eskubaloi?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000&autoReconnect=true";
	private static final String USER = "root";
	private static final String PASSWORD = "";
	// DEBUG true egiteak stack trace osoa emango du, normalean itzali
	private static final boolean DEBUG = true;

	// SQL saiakeren eta erroreen azken zerrenda memorian gordetzeko (GUI-rako erakusteko)
	private static final List<String> lastSqlAttempts = Collections.synchronizedList(new ArrayList<>());

	private static void recordSqlAttempt(String s) {
		if (s == null)
			return;
		try {
			lastSqlAttempts.add(s);
			// azken 200 sartzeak mantendu gehienez
			if (lastSqlAttempts.size() > 200)
				lastSqlAttempts.remove(0);
		} catch (Exception ignored) {
		}
	}

	public static List<String> drainLastSqlAttempts() {
		List<String> copy;
		synchronized (lastSqlAttempts) {
			copy = new ArrayList<>(lastSqlAttempts);
			lastSqlAttempts.clear();
		}
		return copy;
	}

	// -------------------------------------------------------------------------
	// TALDEAK KARGATZEKO (PERTSONA TAULAREKIN JOIN EGINEZ)
	// -------------------------------------------------------------------------
	private static volatile boolean dbNotified = false; // erabiltzaileari behin jakinarazteko

	private static boolean isDatabaseReachable() {
		// JDBC URL-tik host:port atera eta TCP konektatzen saiatu denbora motzean
		try {
			String u = URL;
			int p = u.indexOf("//");
			if (p < 0)
				return true; // parseatu ezin bada -> optimista
			String hostPort = u.substring(p + 2);
			int slash = hostPort.indexOf('/');
			if (slash > 0)
				hostPort = hostPort.substring(0, slash);
			// parametroak kendu badaude
			int q = hostPort.indexOf('?');
			if (q > 0)
				hostPort = hostPort.substring(0, q);
			String host = hostPort;
			int port = 3306;
			if (hostPort.contains(":")) {
				String[] hp = hostPort.split(":");
				host = hp[0];
				try {
					port = Integer.parseInt(hp[1]);
				} catch (Exception ignored) {
				}
			}
			try (Socket s = new Socket()) {
				s.connect(new InetSocketAddress(host, port), 1500);
				return true;
			} catch (Exception ex) {
				if (DEBUG)
					System.out.println("DB reachability probe failed: " + ex.getMessage());
				return false;
			}
		} catch (Exception ex) {
			if (DEBUG)
				ex.printStackTrace();
			return true; // ez oztopatzeko
		}
	}

	public static List<Taldea> loadTaldeakFromDB(Component parent, String season) {
		// DB ez badago eskuragarri, .ser fitxategietara egin fallback eta jakinarazi behin bakarrik
		if (!isDatabaseReachable()) {
			if (!dbNotified) {
				dbNotified = true;
				JOptionPane.showMessageDialog(parent,
						"Cannot connect to database server at " + URL + ".\nUsing serialized files (.ser) as fallback.",
						"DB Unreachable", JOptionPane.WARNING_MESSAGE);
			}
			List<Taldea> fromSer = loadTaldeakFromSer(parent, season);
			return fromSer != null ? fromSer : new ArrayList<>();
		}

		List<Taldea> listaTaldeak = new ArrayList<>();

		// Driver kargatu eta ez badago, jakinarazi eta .ser erabilera egin
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			String msg = "MySQL JDBC Driver ez da aurkitu. Gehitu mysql-connector-java.jar classpath-era.\n"
				+ "Datuak fitxategi serializatuekin kargatuko dira fallback gisa.";
			JOptionPane.showMessageDialog(parent, msg, "Driver JDBC ez aurkitu", JOptionPane.WARNING_MESSAGE);
			List<Taldea> fromSer = loadTaldeakFromSer(parent, season);
			return fromSer != null ? fromSer : new ArrayList<>();
		}

		// DB kontsulta
		String sql = "SELECT t.Izena AS NombreEquipo, " + "j.NANa AS NANa, "
				+ "COALESCE(p.Izen_abizena, j.Izen_abizena) AS Izen_abizena, " + "p.Adina AS Adina, "
				+ "p.Helbidea AS Helbidea, " + "p.Tlfn AS Tlfn, " + "j.Dortsala AS Dortsala, "
				+ "j.Posizioa AS Posizioa, " + "j.Jaiotze_data AS Jaiotze_data " + "FROM jokalaria j "
				+ "LEFT JOIN taldea t ON j.taldea = t.Izena " + "LEFT JOIN pertsona p ON j.NANa = p.NANa "
				+ "ORDER BY t.Izena, j.Dortsala";

		try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				String nombreEquipo = rs.getString("NombreEquipo");

				// Taldea bilatu edo sortu
				Taldea equipo = buscarOcrearTaldea(listaTaldeak, nombreEquipo == null ? "" : nombreEquipo);

				// Jokalariaren datuak lortu
				String nana = rs.getString("NANa");
				String nombreCompleto = rs.getString("Izen_abizena");
				int edad = 0;
				try {
					edad = rs.getInt("Adina");
					if (rs.wasNull())
						edad = 0;
				} catch (SQLException ignore) {
					edad = 0;
				}
				String helbidea = null;
				try {
					helbidea = rs.getString("Helbidea");
				} catch (SQLException ignore) {
					helbidea = null;
				}
				String tlfn = null;
				try {
					tlfn = rs.getString("Tlfn");
				} catch (SQLException ignore) {
					tlfn = null;
				}
				int dorsal = 0;
				try {
					dorsal = rs.getInt("Dortsala");
					if (rs.wasNull())
						dorsal = 0;
				} catch (SQLException ignore) {
					dorsal = 0;
				}
				String posizioa = null;
				try {
					posizioa = rs.getString("Posizioa");
				} catch (SQLException ignore) {
					posizioa = null;
				}
				String jaiotze = null;
				try {
					jaiotze = rs.getString("Jaiotze_data");
				} catch (SQLException ignore) {
					jaiotze = null;
				}

				// Nombre eta Abizena banatu
				String nombre = "";
				String apellido = "";
				if (nombreCompleto != null) {
					String[] partes = nombreCompleto.trim().split(" ", 2);
					nombre = partes[0];
					if (partes.length > 1)
						apellido = partes[1];
				}

				// Jokalaria sortu eta gehitu
				Jokalaria j = new Jokalaria();
				j.setNana(nana);
				j.setNombre(nombre);
				j.setApellido(apellido);
				j.setEdad(edad);
				j.setHelbidea(helbidea);
				j.setTlfn(tlfn);
				j.setDorsal(dorsal);
				j.setPosizioa(posizioa == null ? "" : posizioa);
				j.setJaiotzeData(jaiotze == null ? "" : jaiotze);
				j.setTaldea(nombreEquipo);

				equipo.addJugador(j);
			}

		} catch (SQLException e) {
			// SQL erroreen kasuan fallback egin .ser erabiliz
			if (DEBUG)
				e.printStackTrace();
			if (e instanceof SQLSyntaxErrorException
					|| (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown column"))) {
				String userMsg = "SQL kontsulta errorea: datu-esquema desberdina izan daiteke (zutabea aurkitu ezina).\n"
						+ ".ser fitxategietatik kargatuko da alternatiba gisa.\n\nXehetasuna: " + e.getMessage();
				JOptionPane.showMessageDialog(parent, userMsg, "Error SQL - Fallback", JOptionPane.WARNING_MESSAGE);
				List<Taldea> fromSer = loadTaldeakFromSer(parent, season);
				return fromSer != null ? fromSer : new ArrayList<>();
			}
			JOptionPane.showMessageDialog(parent, "DB kargatze errorea: " + e.getMessage());
		}

		return listaTaldeak;
	}

	// Taldeen laguntzaile helper
	private static Taldea buscarOcrearTaldea(List<Taldea> lista, String nombre) {
		for (Taldea t : lista) {
			if (t.getNombre().equalsIgnoreCase(nombre)) {
				return t;
			}
		}
		Taldea nuevo = new Taldea(nombre);
		lista.add(nuevo);
		return nuevo;
	}

	// -------------------------------------------------------------------------
	// FITXATEGI METODOAK (.ser) - BATERAGARRITASUN ARRAZOIAK
	// -------------------------------------------------------------------------
	public static List<Taldea> loadTaldeakFromSer(Component parent, String season) {
		List<String> candidates = new ArrayList<>();
		candidates.add("taldeak_" + season + ".ser");
		candidates.add("taldeak_" + season.replace('-', '_') + ".ser");
		// gainera hasierako urtea ere saiatu (adib: 2025-2026 -> 2025)
		if (season != null && season.length() >= 4) {
			candidates.add("taldeak_" + season.substring(0, 4) + ".ser");
		}
		candidates.add("taldeak.ser");
		candidates.add("taldeak_2025-2026.ser");
		candidates.add("taldeak_2026-2027.ser");

		String base = System.getProperty("user.dir");
		for (String name : candidates) {
			File f = new File(base, name);
			if (!f.exists())
				continue;
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
				Object obj = ois.readObject();
				if (obj instanceof List) {
					// noinspection unchecked
					return (List<Taldea>) obj;
				} else if (obj instanceof Taldea[]) {
					Taldea[] arr = (Taldea[]) obj;
					return new ArrayList<>(Arrays.asList(arr));
				}
			} catch (Exception ex) {
				if (DEBUG)
					ex.printStackTrace();
				JOptionPane.showMessageDialog(parent,
						"Errorea fitxategi serializatua irakurtzen: " + f.getAbsolutePath() + "\n" + ex.getMessage());
				return new ArrayList<>();
			}
		}
		// Ez da fitxategi baliagarririk aurkitu
		return new ArrayList<>();
	}

	public static List<Taldea> loadTaldeakFromSer(Component parent) {
		return loadTaldeakFromSer(parent, "");
	}

	// -------------------------------------------------------------------------
	// GUI-k behar dituen besteko metodoak (PARTIDUAK / TEMPORADA / UPDATE kargatzea)
	// -------------------------------------------------------------------------
	// Metodo honek aurrekoa ordezkatzen du osoa
	// Ordezkatu metodo osoa GUIren_metodoak.java fitxategian
	public static Object[] loadPartidosFromDB(Component parent, String season) {
		if (!isDatabaseReachable()) {
			return new Object[] { new String[0], new Object[0][0] };
		}

		// Try multiple possible matches tables and column name variants to be tolerant to different schemas
		String[] tables = new String[] { "partidua", "partido", "partiduak", "partidos", "match", "matches", "partidoa" };

		String[][] columnSets = new String[][] {
			{ "Data", "Ordua", "Golak_lokala", "Golak_kanpokoak", "Zelaia", "Talde_lokala", "Kampoko_taldea" },
			{ "data", "ordua", "goles_local", "goles_visitante", "estadio", "local", "visitante" },
			{ "fecha", "hora", "goles_local", "goles_visitante", "estadio", "local_id", "visitante_id" },
			{ "Data", "Ordua", "Emaitza", "Zelaia", "Talde_lokala", "Kampoko_taldea" },
			{ "data", "ordua", "emaitza", "zelaia", "local", "visitante" },
			{ "fecha", "hora", "resultado", "estadio", "local_id", "visitante_id" }
		};

		String[] seasonCols = new String[] { "denboraldia", "season", "temporada", "anio", "anno" };

		for (String table : tables) {
			for (String[] cols : columnSets) {
				for (String seasonCol : seasonCols) {

					// Build query
					StringBuilder queryBuilder = new StringBuilder("SELECT ");
					for (int i = 0; i < cols.length; i++) {
						queryBuilder.append("`").append(cols[i]).append("`");
						if (i < cols.length - 1) queryBuilder.append(", ");
					}
					queryBuilder.append(" FROM `").append(table).append("` WHERE `").append(seasonCol).append("` = ?");

					String sql = queryBuilder.toString();

					// build season value variants to try
					List<String> seasonVariants = new ArrayList<>();
					String s0 = (season == null) ? "" : season.trim();
					seasonVariants.add(s0);
					seasonVariants.add(s0.replace('-', '_'));
					seasonVariants.add(s0.replace('-', '/'));
					seasonVariants.add(s0.replace('_', '-'));
					// short form 2025-2026 -> 25_26, 25-26, 2526? prefer 25_26
					String two = s0.replaceAll("20([0-9]{2})-20([0-9]{2})","$1_$2");
					if (!two.equals(s0)) seasonVariants.add(two);
					// also try dropping century: 2025-2026 -> 25-26
					String shortDash = s0.replaceAll("20([0-9]{2})-([0-9]{2})","$1-$2");
					if (!shortDash.equals(s0)) seasonVariants.add(shortDash);
					// add underscore variant of short
					if (shortDash.contains("-")) seasonVariants.add(shortDash.replace('-', '_'));
					// unique
					LinkedHashSet<String> svset = new LinkedHashSet<>(seasonVariants);
					seasonVariants = new ArrayList<>(svset);

					for (String seasonValue : seasonVariants) {
						recordSqlAttempt("Trying SQL: " + sql + " with seasonValue=" + seasonValue);

						try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
								PreparedStatement stmt = conn.prepareStatement(sql)) {

							stmt.setString(1, seasonValue);

							try (ResultSet rs = stmt.executeQuery()) {
								List<Object[]> rows = new ArrayList<>();
								while (rs.next()) {
									Object[] row = new Object[cols.length];
									for (int i = 0; i < cols.length; i++) row[i] = rs.getObject(i + 1);
									rows.add(row);
								}
								Object[][] data = rows.toArray(new Object[0][]);
								return new Object[] { cols, data };
							}

						} catch (SQLException ex) {
							recordSqlAttempt("SQL failed: " + ex.getMessage());
							String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
							if (msg.contains("doesn't exist") || msg.contains("unknown column") || msg.contains("unknown table")) {
								// try next seasonValue
								if (DEBUG) System.out.println("SQL variant failed: " + sql + " -> " + ex.getMessage());
								continue;
							}
							if (DEBUG) ex.printStackTrace();
							return new Object[] { new String[0], new Object[0][0] };
						}
					}
				}
			}
		}

		JOptionPane.showMessageDialog(parent, "Ez dira 'Golak_lokala' edo 'Golak_kanpokoak' zutabeak aurkitu 'partidua' taulan.", "DB Error", JOptionPane.WARNING_MESSAGE);
		return new Object[] { new String[0], new Object[0][0] };
	}

	public static List<String> loadSeasonsFromDB(Component parent) {
		// Lehenik DB probe egin
		if (!isDatabaseReachable()) {
			if (!dbNotified) {
				dbNotified = true;
				JOptionPane.showMessageDialog(parent,
						"Cannot connect to database server at " + URL + ".\nLoading seasons from local files.",
						"DB Unreachable", JOptionPane.WARNING_MESSAGE);
			}
			List<String> fromFiles = loadSeasonsFromSerFiles();
			return fromFiles;
		}
		List<String> seasons = new ArrayList<>();
		String[] tables = new String[] { "partidua", "partido", "partiduak" };
		String[] seasonCols = new String[] { "temporada", "denboraldia", "season", "anno", "anio" };

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			// Driver falta bada -> fallback fitxategi .ser
			JOptionPane.showMessageDialog(parent, "MySQL JDBC Driver not found. Loading seasons from local files.",
					"Driver missing", JOptionPane.WARNING_MESSAGE);
			List<String> fromFiles = loadSeasonsFromSerFiles();
			seasons.addAll(fromFiles);
			return seasons;
		}

		for (String table : tables) {
			for (String seasonCol : seasonCols) {
				String sql = String.format(
						"SELECT DISTINCT `%s` AS s FROM `%s` WHERE `%s` IS NOT NULL ORDER BY `%s` DESC", seasonCol,
						table, seasonCol, seasonCol);
				try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
					// Validate that the table and column exist to avoid SQLSyntaxErrorException
					try {
						DatabaseMetaData md = conn.getMetaData();
						boolean tableExists = false;
						try (ResultSet tRs = md.getTables(conn.getCatalog(), null, table, new String[] { "TABLE" })) {
							while (tRs.next()) {
								String tblName = tRs.getString("TABLE_NAME");
								if (tblName != null && tblName.equalsIgnoreCase(table)) {
									tableExists = true;
									break;
								}
							}
						}
						if (!tableExists) {
							// try next table
							continue;
						}

						boolean colExists = false;
						try (ResultSet cRs = md.getColumns(conn.getCatalog(), null, table, seasonCol)) {
							while (cRs.next()) {
								String colName = cRs.getString("COLUMN_NAME");
								if (colName != null && colName.equalsIgnoreCase(seasonCol)) {
									colExists = true;
									break;
								}
							}
						}
						if (!colExists) {
							// try next column name
							continue;
						}
					} catch (SQLException metaEx) {
						if (DEBUG)
							metaEx.printStackTrace();
						// If metadata check fails for some reason, fall back to attempting the query
						// below
					}

					// If we reach here, table and column appear to exist; run the query
					try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

						while (rs.next()) {
							String s = rs.getString("s");
							if (s != null && !s.trim().isEmpty())
								seasons.add(s);
						}
						if (!seasons.isEmpty())
							return seasons;

					} catch (SQLException ex) {
						if (DEBUG)
							ex.printStackTrace();
						String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
						if (msg.contains("unknown column") || msg.contains("doesn't exist")
								|| msg.contains("unknown table")) {
							// try next combination
							continue;
						}
						JOptionPane.showMessageDialog(parent,
								"Error loading seasons from DB: " + ex.getMessage()
										+ "\nFalling back to local serialized files.",
								"DB Error", JOptionPane.WARNING_MESSAGE);
						List<String> fromFiles = loadSeasonsFromSerFiles();
						seasons.addAll(fromFiles);
						return seasons;
					}

				} catch (SQLException ex) {
					if (DEBUG)
						ex.printStackTrace();
					String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
					if (msg.contains("unknown column") || msg.contains("doesn't exist")
							|| msg.contains("unknown table")) {
						continue; // try next combination
					}
					JOptionPane.showMessageDialog(parent,
							"Error loading seasons from DB: " + ex.getMessage()
									+ "\nFalling back to local serialized files.",
							"DB Error", JOptionPane.WARNING_MESSAGE);
					List<String> fromFiles = loadSeasonsFromSerFiles();
					seasons.addAll(fromFiles);
					return seasons;
				}
			}
		}

		return seasons;
	}

	// Fallback: scan current directory for taldeak_<season>.ser files and extract
	// season names
	private static List<String> loadSeasonsFromSerFiles() {
		List<String> list = new ArrayList<>();
		String base = System.getProperty("user.dir");
		File dir = new File(base);
		File[] files = dir.listFiles(
				(d, name) -> name.toLowerCase().startsWith("taldeak_") && name.toLowerCase().endsWith(".ser"));
		if (files == null)
			return list;
		for (File f : files) {
			String name = f.getName();
			// name like taldeak_2025-2026.ser or taldeak_2026.ser
			String core = name.substring("taldeak_".length(), name.length() - ".ser".length());
			if (!core.trim().isEmpty()) {
				if (!list.contains(core))
					list.add(core);
			}
		}
		// sort descending-like if seasons like 2026-2027
		list.sort(Comparator.reverseOrder());
		return list;
	}

	// Detect sailkapena_<suffix> tables and return normalized season strings for
	// the UI
	public static List<String> getAvailableSailkapenaSeasons(Component parent) {
		List<String> out = new ArrayList<>();
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			if (DEBUG)
				e.printStackTrace();
			return out;
		}

		// Probe DB before attempting queries
		if (!isDatabaseReachable()) {
			if (!dbNotified) {
				dbNotified = true;
				JOptionPane.showMessageDialog(parent,
						"Cannot connect to database server at " + URL + ".\nNo sailkapena_* tables will be detected.",
						"DB Unreachable", JOptionPane.WARNING_MESSAGE);
			}
			return out;
		}

		String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_name LIKE 'sailkapena_%'";
		try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
				PreparedStatement pst = conn.prepareStatement(sql)) {

			// assume database name is in URL path; use 'eskubaloi'
			pst.setString(1, "eskubaloi");
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					String tbl = rs.getString(1);
					if (tbl == null)
						continue;
					String suf = tbl.substring("sailkapena_".length());
					String display = normalizeSeasonFromSuffix(suf);
					if (display != null && !out.contains(display))
						out.add(display);
				}
			}
		} catch (SQLException ex) {
			if (DEBUG)
				ex.printStackTrace();
		}

		// sort newest first if they look like years
		out.sort(Comparator.reverseOrder());
		return out;
	}

	private static String normalizeSeasonFromSuffix(String suf) {
		if (suf == null)
			return null;
		String t = suf.replaceAll("[^0-9_\\-]", "_");
		t = t.replace('-', '_');
		String[] parts = t.split("_");
		if (parts.length >= 2) {
			String a = parts[0];
			String b = parts[1];
			if (a.length() == 2 && b.length() == 2) {
				return "20" + a + "-" + "20" + b;
			}
			if (a.length() == 4 && b.length() == 4) {
				return a + "-" + b;
			}
			// fallback: join with -
			return a + "-" + b;
		}
		return suf.replace('_', '-');
	}

	// -------------------------------------------------------------------------
	// ACTUALIZAR PARTIDO EN BD (TOLERANTE A ESQUEMAS)
	// -------------------------------------------------------------------------
	public static boolean updatePartidoInDB(Component parent, Object[] row) {
		if (row == null || row.length < 1 || row[0] == null)
			return false;

		// Probe DB first
		if (!isDatabaseReachable()) {
			if (!dbNotified) {
				dbNotified = true;
				JOptionPane.showMessageDialog(parent,
						"Cannot connect to database server at " + URL + ".\nCannot update matches.", "DB Unreachable",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}

		// row expected: id + other columns depending on schema
		String[] tables = new String[] { "partidua", "partido", "partiduak" };
		String[][] columnSets = new String[][] {
				{ "kod_partidua", "Data", "Ordua", "Emaitza", "Zelaia", "kod_lokala", "kod_kanpokoa" },
				{ "kod_partidua", "data", "ordua", "emaitza", "zelaia", "kod_lokala", "kod_kanpokoa" },
				{ "id", "fecha", "hora", "resultado", "estadio", "local_id", "visitante_id" },
				{ "id", "data", "hora", "resultado", "estadio", "local_id", "visitante_id" },
				// variants with separated goal columns and renamed team columns
				{ "kod_partidua", "Data", "Ordua", "Goles_Local", "Goles_Visitante", "Zelaia", "Talde_lokala",
						"Kampoko_taldea" },
				{ "kod_partidua", "data", "ordua", "goles_local", "goles_visitante", "zelaia", "Talde_lokala",
						"Kampoko_taldea" },
				{ "id", "fecha", "hora", "goles_local", "goles_visitante", "estadio", "local_id", "visitante_id" },
				{ "id", "data", "hora", "goles_local", "goles_visitante", "estadio", "local_id", "visitante_id" } };

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			JOptionPane.showMessageDialog(parent,
					"MySQL JDBC Driver not found. Cannot update match. Add mysql-connector-java.jar to classpath.",
					"Driver missing", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		for (String table : tables) {
			for (String[] cols : columnSets) {
				// Build SQL: UPDATE `table` SET `col2`=?,`col3`=?,... WHERE `col1`=?
				StringBuilder sb = new StringBuilder();
				sb.append("UPDATE `").append(table).append("` SET ");
				for (int i = 1; i < cols.length; i++) {
					if (i > 1)
						sb.append(", ");
					sb.append("`").append(cols[i]).append("` = ?");
				}
				sb.append(" WHERE `").append(cols[0]).append("` = ?");
				String sql = sb.toString();

				try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
						PreparedStatement pst = conn.prepareStatement(sql)) {

					// set params 1..N-1 from row[1]..row[N-1]
					for (int i = 1; i < cols.length; i++) {
						Object val = (i < row.length) ? row[i] : null;
						pst.setObject(i, val);
					}
					// id param at end
					pst.setObject(cols.length, row[0]);

					int updated = pst.executeUpdate();
					return updated >= 0;
				} catch (SQLException ex) {
					if (DEBUG)
						ex.printStackTrace();
					String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
					if (msg.contains("unknown column") || msg.contains("doesn't exist") || msg.contains("unknown table")
							|| msg.contains("column not found")) {
						// try next combination
						continue;
					}
					JOptionPane.showMessageDialog(parent, "Error updating match in DB: " + ex.getMessage(), "DB Error",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}

		// nothing matched
		JOptionPane.showMessageDialog(parent, "No suitable table/columns found to update match.", "DB Error",
				JOptionPane.ERROR_MESSAGE);
		return false;
	}

	// Overload used by GUI: provide original and new row values plus column names
    public static boolean updatePartidoInDB(Component parent, Object[] originalRow, Object[] newRow, String[] colNames) {
        if (colNames == null || colNames.length == 0) return false;
        if (originalRow == null || newRow == null) return false;

        // Probe DB
        if (!isDatabaseReachable()) {
            if (!dbNotified) { dbNotified = true; JOptionPane.showMessageDialog(parent, "Cannot connect to database server at " + URL + ".\nCannot update matches.", "DB Unreachable", JOptionPane.WARNING_MESSAGE); }
            return false;
        }

        String[] tables = new String[] { "partidua", "partido", "partiduak" };

        // try to detect id-like column in provided colNames
        int idIndex = -1;
        for (int i = 0; i < colNames.length; i++) {
            String n = colNames[i] == null ? "" : colNames[i].toLowerCase();
            if (n.contains("id") || n.contains("kod") || n.contains("codigo") || n.contains("key") || n.contains("kod_partidua") || n.contains("id_part") ) { idIndex = i; break; }
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            JOptionPane.showMessageDialog(parent, "MySQL JDBC Driver not found. Cannot update match. Add mysql-connector-java.jar to classpath.", "Driver missing", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        for (String table : tables) {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                // First, get the actual columns that exist in this table
                Set<String> existingColumns = new HashSet<>();
                try {
                    DatabaseMetaData md = conn.getMetaData();
                    try (ResultSet colsRs = md.getColumns(conn.getCatalog(), null, table, null)) {
                        while (colsRs.next()) {
                            String cname = colsRs.getString("COLUMN_NAME");
                            if (cname != null) existingColumns.add(cname.toLowerCase());
                        }
                    }
                } catch (SQLException ex) {
                    // If we can't get metadata, skip this table
                    if (DEBUG) ex.printStackTrace();
                    continue;
                }

                if (existingColumns.isEmpty()) continue; // table doesn't exist or has no columns

                // Build list of columns to update (only those that exist in the DB table)
                List<Integer> updateableIndices = new ArrayList<>();
                for (int i = 0; i < colNames.length; i++) {
                    String colName = colNames[i];
                    if (colName != null && existingColumns.contains(colName.toLowerCase())) {
                        updateableIndices.add(i);
                    }
                }

                if (updateableIndices.isEmpty()) continue; // no matching columns

                // Determine which column to use for WHERE clause
                int whereColIndex = -1;
                if (idIndex >= 0 && existingColumns.contains(colNames[idIndex].toLowerCase())) {
                    whereColIndex = idIndex;
                } else {
                    // fallback: find first column with non-null original value that exists in DB
                    for (int i = 0; i < colNames.length; i++) {
                        if (colNames[i] != null && existingColumns.contains(colNames[i].toLowerCase()) 
                            && i < originalRow.length && originalRow[i] != null) {
                            whereColIndex = i;
                            break;
                        }
                    }
                }

                if (whereColIndex < 0) continue; // can't build WHERE clause

                // Build SET clause from valid columns only
                StringBuilder sb = new StringBuilder();
                sb.append("UPDATE `").append(table).append("` SET ");
                boolean first = true;
                for (int idx : updateableIndices) {
                    if (!first) sb.append(", ");
                    first = false;
                    sb.append("`").append(colNames[idx]).append("` = ?");
                }
                sb.append(" WHERE `").append(colNames[whereColIndex]).append("` = ?");

                String sql = sb.toString();
                recordSqlAttempt("Trying validated UPDATE: " + sql + " on table=" + table);

                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    int param = 1;
                    // set SET params from newRow (use original if new is null)
                    for (int idx : updateableIndices) {
                        Object val = (idx < newRow.length) ? newRow[idx] : null;
                        if (val == null && idx < originalRow.length) val = originalRow[idx];
                        pst.setObject(param++, val);
                    }
                    // set WHERE param from originalRow
                    Object whereVal = (whereColIndex < originalRow.length) ? originalRow[whereColIndex] : null;
                    pst.setObject(param++, whereVal);

                    int updated = pst.executeUpdate();
                    if (updated >= 0) return true;
                } catch (SQLException ex) {
                    if (DEBUG) ex.printStackTrace();
                    String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
                    if (msg.contains("unknown column") || msg.contains("doesn't exist") || msg.contains("unknown table") || msg.contains("column not found")) {
                        // try next table
                        continue;
                    }
                    JOptionPane.showMessageDialog(parent, "Error updating match in DB: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (SQLException connEx) {
                if (DEBUG) connEx.printStackTrace();
                continue;
            }
        }

        // If generic attempts failed, try composite-key heuristic: data+hora+local+visitante
        String[] dateCandidates = new String[] {"Data","data","fecha","date"};
        String[] timeCandidates = new String[] {"Ordua","ordua","hora","time"};
        String[] localCandidates = new String[] {"Talde_lokala","Taldea_lokala","local","local_id","kod_lokala","Talde_lokala","local_team","Talde_lokala","Talde_lokala"};
        String[] visitCandidates = new String[] {"Kampoko_taldea","KampokoTaldea","Kampoko_taldea","visitante","visitante_id","kod_kanpokoa","away","away_team"};

        for (String table : tables) {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                DatabaseMetaData md = conn.getMetaData();
                // check columns existence
                ResultSet colsRs = md.getColumns(conn.getCatalog(), null, table, null);
                Set<String> available = new HashSet<>();
                while (colsRs.next()) {
                    String cname = colsRs.getString("COLUMN_NAME");
                    if (cname != null) available.add(cname.toLowerCase());
                }

                int idxDate = -1, idxTime = -1, idxLocal = -1, idxVisit = -1;
                for (int i = 0; i < colNames.length; i++) {
                    String n = colNames[i] == null ? "" : colNames[i].toLowerCase();
                    for (String cand : dateCandidates) if (n.equalsIgnoreCase(cand) && available.contains(n)) idxDate = i;
                    for (String cand : timeCandidates) if (n.equalsIgnoreCase(cand) && available.contains(n)) idxTime = i;
                    for (String cand : localCandidates) if (n.equalsIgnoreCase(cand) && available.contains(n)) idxLocal = i;
                    for (String cand : visitCandidates) if (n.equalsIgnoreCase(cand) && available.contains(n)) idxVisit = i;
                }

                if (idxDate >= 0 && idxTime >= 0 && idxLocal >= 0 && idxVisit >= 0) {
                    // build update using these four columns in WHERE
                    // Only include columns that exist in the database table
                    List<Integer> validIndices = new ArrayList<>();
                    for (int i = 0; i < colNames.length; i++) {
                        String colName = colNames[i];
                        if (colName != null && available.contains(colName.toLowerCase())) {
                            validIndices.add(i);
                        }
                    }
                    
                    if (validIndices.isEmpty()) continue; // no valid columns to update
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("UPDATE `").append(table).append("` SET ");
                    boolean first = true;
                    for (int idx : validIndices) {
                        if (!first) sb.append(", ");
                        first = false;
                        sb.append("`").append(colNames[idx]).append("` = ?");
                    }
                    sb.append(" WHERE `").append(colNames[idxDate]).append("` = ? AND `")
                      .append(colNames[idxTime]).append("` = ? AND `").append(colNames[idxLocal]).append("` = ? AND `").append(colNames[idxVisit]).append("` = ?");

                    String sql = sb.toString();
                    recordSqlAttempt("Trying composite UPDATE: " + sql + " on table=" + table);

                    try (PreparedStatement pst = conn.prepareStatement(sql)) {
                        int p = 1;
                        for (int idx : validIndices) {
                            Object val = (idx < newRow.length && newRow[idx] != null) ? newRow[idx] : (idx < originalRow.length ? originalRow[idx] : null);
                            pst.setObject(p++, val);
                        }
                        pst.setObject(p++, originalRow[idxDate]);
                        pst.setObject(p++, originalRow[idxTime]);
                        pst.setObject(p++, originalRow[idxLocal]);
                        pst.setObject(p++, originalRow[idxVisit]);

                        int updated = pst.executeUpdate();
                        if (updated >= 0) return true;
                    } catch (SQLException ex) {
                        if (DEBUG) ex.printStackTrace();
                        continue;
                    }
                }
            } catch (SQLException ex) {
                if (DEBUG) ex.printStackTrace();
                continue;
            }
        }

        JOptionPane.showMessageDialog(parent, "No suitable table/columns found to update match (generic path).", "DB Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

	public static Object[] loadSailkapenaFromDB(Component parent, String season) {
		// 1. Validaciones básicas
		if (season == null || season.trim().isEmpty()) {
			return new Object[] { new String[0], new Object[0][0] };
		}
		if (!isDatabaseReachable()) {
			return new Object[] { new String[0], new Object[0][0] };
		}

		// Build a set of candidate table names to try. The UI uses formats like "2025-2026",
		// but existing DB tables may be named either with full years (2025_2026) or short
		// years (25_26). Also try underscore/dash variants.
		String raw = season.trim();
		List<String> candidates = new ArrayList<>();

		// normalize separators
		String unders = raw.replace('-', '_');
		String dash = raw.replace('_', '-');

		// try as-is (with underscore)
		candidates.add("sailkapena_" + unders);
		// try with dash (less likely but harmless)
		candidates.add("sailkapena_" + dash);

		// try converting 20YY-20ZZ -> YY_ZZ and YY-ZZ -> YY_ZZ -> sailkapena_25_26
		String twoDigit = unders.replaceAll("20([0-9]{2})","$1");
		if (!twoDigit.equals(unders)) candidates.add("sailkapena_" + twoDigit);

		// if we received YY-ZZ or YY_ZZ produce full-year variant 20YY_20ZZ
		String[] parts = unders.split("[_\\-]");
		if (parts.length >= 2) {
			String a = parts[0];
			String b = parts[1];
			if (a.length() == 2 && b.length() == 2) {
				candidates.add("sailkapena_20" + a + "_20" + b);
			} else if (a.length() == 4 && b.length() == 4) {
				// also try short form
				String shortForm = "sailkapena_" + a.substring(2) + "_" + b.substring(2);
				candidates.add(shortForm);
			}
		}

		// ensure unique order-preserving
		LinkedHashSet<String> uniq = new LinkedHashSet<>(candidates);
		List<String> tryTables = new ArrayList<>(uniq);

		if (DEBUG) {
			System.out.println("loadSailkapenaFromDB: season='" + season + "' trying tables: " + tryTables);
		}

		// Try each candidate table until one yields results
		for (String tableName : tryTables) {
			String sql = "SELECT * FROM `" + tableName + "`";
			if (DEBUG) System.out.println("Attempting to load sailkapena table: " + tableName);
			try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
					PreparedStatement stmt = conn.prepareStatement(sql);
					ResultSet rs = stmt.executeQuery()) {

					ResultSetMetaData metaData = rs.getMetaData();
					int colCount = metaData.getColumnCount();
					String[] cols = new String[colCount];
					for (int i = 0; i < colCount; i++) {
						cols[i] = metaData.getColumnLabel(i + 1);
					}

					List<Object[]> rows = new ArrayList<>();
					while (rs.next()) {
						Object[] row = new Object[colCount];
						for (int i = 0; i < colCount; i++) {
							row[i] = rs.getObject(i + 1);
						}
						rows.add(row);
					}

					return new Object[] { cols, rows.toArray(new Object[0][]) };

			} catch (SQLException ex) {
				String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
				if (msg.contains("doesn't exist") || msg.contains("unknown table") || msg.contains("unknown column")) {
					// try next candidate table
					if (DEBUG) System.out.println("Table " + tableName + " not found or incompatible: " + ex.getMessage());
					continue;
				}
				if (DEBUG) ex.printStackTrace();
				return new Object[] { new String[0], new Object[0][0] };
			}
		}

		// none found yet — try in-memory calculation from matches first
		Object[] inMem = tryInMemorySailkapenaCalculation(parent, season);
		if (inMem != null) {
			if (DEBUG) System.out.println("Returning in-memory computed sailkapena for season " + season);
			return inMem;
		}

		// none found yet — try to recalculate-and-create the table automatically (useful if matches exist but table wasn't created)
		if (DEBUG) System.out.println("No sailkapena table found for season " + season + ". Attempting to recalculate and create it.");
		boolean recalc = false;
		try {
			recalc = recalculateAndSaveSailkapena(parent, season);
		} catch (Throwable t) {
			if (DEBUG) t.printStackTrace();
		}
		if (recalc) {
			if (DEBUG) System.out.println("Recalculation created the table; retrying load for season " + season);
			// retry once
			for (String tableName : tryTables) {
				String sql = "SELECT * FROM `" + tableName + "`";
				try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
						PreparedStatement stmt = conn.prepareStatement(sql);
						ResultSet rs = stmt.executeQuery()) {

						ResultSetMetaData metaData = rs.getMetaData();
						int colCount = metaData.getColumnCount();
						String[] cols = new String[colCount];
						for (int i = 0; i < colCount; i++) cols[i] = metaData.getColumnLabel(i + 1);

						List<Object[]> rows = new ArrayList<>();
						while (rs.next()) {
							Object[] row = new Object[colCount];
							for (int i = 0; i < colCount; i++) row[i] = rs.getObject(i + 1);
							rows.add(row);
						}

						return new Object[] { cols, rows.toArray(new Object[0][]) };

				} catch (SQLException ex) {
					String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
					if (msg.contains("doesn't exist") || msg.contains("unknown table") || msg.contains("unknown column")) {
						if (DEBUG) System.out.println("Retry: Table " + tableName + " not found: " + ex.getMessage());
						continue;
					}
					if (DEBUG) ex.printStackTrace();
					return new Object[] { new String[0], new Object[0][0] };
				}
			}
		}

		// still none found -> return informative single-row result so UI can show explanation
		if (DEBUG) System.out.println("After recalculation attempt, no sailkapena table exists for season " + season);

		// include recent SQL attempts in the informative message to help diagnosis
		List<String> attempts = drainLastSqlAttempts();
		StringBuilder info = new StringBuilder();
		info.append("Sailkapena table not found for season ").append(season).append(". Try recalculating or check DB.");
		info.append(" Tried candidate tables: ").append(tryTables);
		if (attempts != null && !attempts.isEmpty()) {
			info.append("\nRecent SQL attempts:\n");
			for (String s : attempts) info.append(s).append("\n");
		} else {
			info.append("\n(No recent SQL attempts recorded.)");
		}
		return new Object[] { new String[] { "Info" }, new Object[][] { { info.toString() } } };
	}

	// If no sailkapena table exists, try computing standings directly from matches (in-memory) before attempting DB recalc/creation
	private static Object[] tryInMemorySailkapenaCalculation(Component parent, String season) {
		if (DEBUG) System.out.println("No sailkapena table found for season " + season + ". Trying in-memory calculation from matches.");
		try {
			Object[] partidosRes = loadPartidosFromDB(parent, season);
			String[] pcols = (partidosRes != null && partidosRes.length > 0 && partidosRes[0] instanceof String[]) ? (String[]) partidosRes[0] : new String[0];
			Object[][] prows = (partidosRes != null && partidosRes.length > 1 && partidosRes[1] instanceof Object[][]) ? (Object[][]) partidosRes[1] : new Object[0][0];
			if (pcols.length > 0 && prows.length > 0) {
				// try to detect columns similarly to recalculateAndSaveSailkapena
				Map<String, Integer> colIndex = new HashMap<>();
				for (int i = 0; i < pcols.length; i++) colIndex.put((pcols[i] == null ? "" : pcols[i].toString()).toLowerCase(), i);

				Integer idxLocal = null, idxVisit = null, idxGLocal = null, idxGVisit = null, idxResult = null;
				for (String name : colIndex.keySet()) {
					if (idxLocal == null && (name.contains("local") || name.contains("talde") || name.contains("lokal"))) idxLocal = colIndex.get(name);
					if (idxVisit == null && (name.contains("visit") || name.contains("kanp") || name.contains("kamp") || name.contains("away"))) idxVisit = colIndex.get(name);
					if (idxGLocal == null && (name.contains("goles") || name.contains("gol") || name.contains("golak") || name.contains("gf"))) idxGLocal = colIndex.get(name);
					if (idxGVisit == null && (name.contains("goles") || name.contains("gol") || name.contains("golak") || name.contains("ga"))) idxGVisit = colIndex.get(name);
					if (idxResult == null && (name.contains("ema") || name.contains("resul") || name.contains("resultado") || name.contains("result"))) idxResult = colIndex.get(name);
				}

				// fallback heuristics by substring if still null
				if (idxLocal == null) for (String k : colIndex.keySet()) if (k.contains("local") || k.contains("talde") || k.contains("lokal")) { idxLocal = colIndex.get(k); break; }
				if (idxVisit == null) for (String k : colIndex.keySet()) if (k.contains("visit") || k.contains("kanp") || k.contains("kamp") || k.contains("away")) { idxVisit = colIndex.get(k); break; }
				if (idxGLocal == null) for (String k : colIndex.keySet()) if (k.contains("goles") || k.contains("gol") || k.contains("golak") || k.contains("gf")) { idxGLocal = colIndex.get(k); break; }
				if (idxGVisit == null) for (String k : colIndex.keySet()) if (k.contains("goles") || k.contains("gol") || k.contains("golak") || k.contains("ga")) { idxGVisit = colIndex.get(k); break; }
				// compute stats
				class Stat { int PJ=0, G=0, E=0, P=0, GF=0, GA=0; }
				Map<String, Stat> stats = new HashMap<>();
				for (Object[] row : prows) {
					String localTeam = null, visitTeam = null;
					Integer gLocal = null, gVisit = null;
					try {
						if (idxLocal != null && idxLocal < row.length && row[idxLocal] != null) localTeam = String.valueOf(row[idxLocal]);
						if (idxVisit != null && idxVisit < row.length && row[idxVisit] != null) visitTeam = String.valueOf(row[idxVisit]);
						if (idxGLocal != null && idxGLocal < row.length && row[idxGLocal] != null) gLocal = toInt(row[idxGLocal]);
						if (idxGVisit != null && idxGVisit < row.length && row[idxGVisit] != null) gVisit = toInt(row[idxGVisit]);
						if ((gLocal == null || gVisit == null) && idxResult != null && idxResult < row.length && row[idxResult] != null) {
							String resStr = String.valueOf(row[idxResult]);
							String[] parts = resStr.split("[-:]");
							if (parts.length >= 2) {
								try { gLocal = Integer.parseInt(parts[0].trim()); } catch (Exception ex) { gLocal = null; }
								try { gVisit = Integer.parseInt(parts[1].trim()); } catch (Exception ex) { gVisit = null; }
							}
						}
					} catch (Exception ignored) {}

					if ((localTeam == null || visitTeam == null)) continue;
					stats.computeIfAbsent(localTeam, k -> new Stat());
					stats.computeIfAbsent(visitTeam, k -> new Stat());
					Stat sl = stats.get(localTeam); Stat sv = stats.get(visitTeam);
					sl.PJ++; sv.PJ++;
					int gl = (gLocal == null ? 0 : gLocal);
					int gv = (gVisit == null ? 0 : gVisit);
					sl.GF += gl; sl.GA += gv;
					sv.GF += gv; sv.GA += gl;
					if (gLocal == null || gVisit == null) continue;
					if (gl > gv) { sl.G++; sv.P++; }
					else if (gl < gv) { sv.G++; sl.P++; }
					else { sl.E++; sv.E++; }
				}

				if (!stats.isEmpty()) {
					List<Map.Entry<String, Stat>> list = new ArrayList<>(stats.entrySet());
					list.sort((a,b) -> {
						int pa = a.getValue().G*2 + a.getValue().E;
						int pb = b.getValue().G*2 + b.getValue().E;
						if (pb != pa) return pb - pa;
						int gda = a.getValue().GF - a.getValue().GA;
						int gdb = b.getValue().GF - b.getValue().GA;
						if (gdb != gda) return gdb - gda;
						if (b.getValue().GF != a.getValue().GF) return b.getValue().GF - a.getValue().GF;
						return a.getKey().compareToIgnoreCase(b.getKey());
					});

					String[] outCols = new String[] { "team", "PJ", "G", "E", "P", "GF", "GA", "GD", "PTS" };
					Object[][] outRows = new Object[list.size()][];
					for (int i = 0; i < list.size(); i++) {
						Map.Entry<String, Stat> e = list.get(i);
						Stat s = e.getValue();
						int pts = s.G*2 + s.E;
						int gd = s.GF - s.GA;
						outRows[i] = new Object[] { e.getKey(), s.PJ, s.G, s.E, s.P, s.GF, s.GA, gd, pts };
					}
					return new Object[] { outCols, outRows };
				}
			}
		} catch (Throwable t) {
			if (DEBUG) t.printStackTrace();
		}
		return null;
	}

	public static boolean saveTaldeakToSer(Component parent, List<Taldea> taldeak, String season) {
		if (taldeak == null)
			return false;
		String fileName = (season == null || season.trim().isEmpty()) ? "taldeak.ser" : ("taldeak_" + season + ".ser");
		// normalize filename (replace spaces and dashes with underscore)
		fileName = fileName.replace(' ', '_').replace('-', '_');
		File f = new File(System.getProperty("user.dir"), fileName);
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
			oos.writeObject(taldeak);
			return true;
		} catch (Exception ex) {
			if (DEBUG)
				ex.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Ezin izan da taldeak gordetu: " + ex.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	// -------------------------------------------------------------------------
	// TALDEAK ETA JOKALARIAK DATU-BASEAN GORDETZEKO
	// -------------------------------------------------------------------------
	public static boolean saveTaldeakToDB(Component parent, List<Taldea> taldeak, String season) {
		if (taldeak == null || taldeak.isEmpty()) {
			return false;
		}

		if (!isDatabaseReachable()) {
			// Si no hay conexión a la BD, guardar en .ser como fallback
			return saveTaldeakToSer(parent, taldeak, season);
		}

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			JOptionPane.showMessageDialog(parent,
					"MySQL JDBC Driver not found. Saving to .ser file instead.",
					"Driver missing", JOptionPane.WARNING_MESSAGE);
			return saveTaldeakToSer(parent, taldeak, season);
		}

		try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
			int updatedCount = 0;
			
			for (Taldea t : taldeak) {
				if (t.getJugadores() == null) continue;
				for (Jokalaria j : t.getJugadores()) {
					String nana = j.getNana();
					if (nana == null || nana.trim().isEmpty()) continue;

					// Combinar nombre eta apellido Izen_abizena-rako
					String izenAbizena = ((j.getNombre() == null ? "" : j.getNombre()) + " " + 
					                      (j.getApellido() == null ? "" : j.getApellido())).trim();

					// 1. Aktualizatu jokalaria taula
					String sqlJokalaria = "UPDATE `jokalaria` SET `Izen_abizena` = ?, `Dortsala` = ?, `Posizioa` = ?, `Jaiotze_data` = ?, `taldea` = ? WHERE `NANa` = ?";
					try (PreparedStatement pst = conn.prepareStatement(sqlJokalaria)) {
						pst.setString(1, izenAbizena);
						pst.setInt(2, j.getDorsal());
						pst.setString(3, j.getPosizioa());
						pst.setString(4, j.getJaiotzeData());
						pst.setString(5, j.getTaldea() != null ? j.getTaldea() : t.getNombre());
						pst.setString(6, nana);
						int rows = pst.executeUpdate();
						if (rows > 0) updatedCount++;
					} catch (SQLException ex) {
						if (DEBUG) {
							System.out.println("Error updating jokalaria: " + ex.getMessage());
							ex.printStackTrace();
						}
						// Intentar con nombres de columna alternativos
						String sqlAlt = "UPDATE `jokalaria` SET `izen_abizena` = ?, `dortsala` = ?, `posizioa` = ?, `jaiotze_data` = ?, `taldea` = ? WHERE `nana` = ?";
						try (PreparedStatement pst2 = conn.prepareStatement(sqlAlt)) {
							pst2.setString(1, izenAbizena);
							pst2.setInt(2, j.getDorsal());
							pst2.setString(3, j.getPosizioa());
							pst2.setString(4, j.getJaiotzeData());
							pst2.setString(5, j.getTaldea() != null ? j.getTaldea() : t.getNombre());
							pst2.setString(6, nana);
							int rows = pst2.executeUpdate();
							if (rows > 0) updatedCount++;
						} catch (SQLException ex2) {
							if (DEBUG) ex2.printStackTrace();
						}
					}

					// 2. Aktualizatu pertsona taula (izena, helbidea, telefonoa)
					String sqlPertsona = "UPDATE `pertsona` SET `Izen_abizena` = ?, `Helbidea` = ?, `Tlfn` = ? WHERE `NANa` = ?";
					try (PreparedStatement pst = conn.prepareStatement(sqlPertsona)) {
						pst.setString(1, izenAbizena);
						pst.setString(2, j.getHelbidea());
						pst.setString(3, j.getTlfn());
						pst.setString(4, nana);
						pst.executeUpdate();
					} catch (SQLException ex) {
						if (DEBUG) {
							System.out.println("Error updating pertsona: " + ex.getMessage());
						}
						// Intentar con nombres de columna alternativos (minúsculas)
						String sqlAlt = "UPDATE `pertsona` SET `izen_abizena` = ?, `helbidea` = ?, `tlfn` = ? WHERE `nana` = ?";
						try (PreparedStatement pst2 = conn.prepareStatement(sqlAlt)) {
							pst2.setString(1, izenAbizena);
							pst2.setString(2, j.getHelbidea());
							pst2.setString(3, j.getTlfn());
							pst2.setString(4, nana);
							pst2.executeUpdate();
						} catch (SQLException ex2) {
							if (DEBUG) ex2.printStackTrace();
						}
					}
				}
			}

			if (DEBUG) System.out.println("saveTaldeakToDB: Updated " + updatedCount + " players");
			return true;

		} catch (SQLException ex) {
			if (DEBUG) ex.printStackTrace();
			JOptionPane.showMessageDialog(parent,
					"Errorea datu-basean gordetzean: " + ex.getMessage() + "\nGordeta .ser fitxategian.",
					"DB Errorea", JOptionPane.WARNING_MESSAGE);
			return saveTaldeakToSer(parent, taldeak, season);
		}
	}

	// -------------------------------------------------------------------------
	// JOKALARIA TALDEA EGUNERATZEKO (TOLERANTE A ESQUEMAS)
	// -------------------------------------------------------------------------
	public static boolean updateJokalariaTaldeaInDB(Component parent, String nana, String newTaldea) {
        if (nana == null || nana.trim().isEmpty()) return false;

        if (!isDatabaseReachable()) {
            if (!dbNotified) {
                dbNotified = true;
                JOptionPane.showMessageDialog(parent,
                        "Cannot connect to database server at " + URL + ".\nCannot update player team.", "DB Unreachable",
                        JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            JOptionPane.showMessageDialog(parent,
                    "MySQL JDBC Driver not found. Cannot update player. Add mysql-connector-java.jar to classpath.",
                    "Driver missing", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String[] tables = new String[] { "jokalaria", "jokalariak", "jugador", "player", "players" };
        String[] idCols = new String[] { "NANa", "nana", "NIF", "nif", "dni", "dni_id", "id" };
        String[] teamCols = new String[] { "taldea", "Taldea", "team", "equipo", "team_name" };

        for (String table : tables) {
            for (String teamCol : teamCols) {
                for (String idCol : idCols) {
                    String sql = String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?", table, teamCol, idCol);
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                            PreparedStatement pst = conn.prepareStatement(sql)) {
                        pst.setObject(1, newTaldea);
                        pst.setObject(2, nana);
                        int updated = pst.executeUpdate();
                        if (updated >= 0) return true;
                    } catch (SQLException ex) {
                        if (DEBUG) ex.printStackTrace();
                        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
                        if (msg.contains("unknown column") || msg.contains("doesn't exist") || msg.contains("unknown table")
                                || msg.contains("column not found")) {
                            // try next combination
                            continue;
                        }
                        JOptionPane.showMessageDialog(parent, "Error updating player team in DB: " + ex.getMessage(),
                                "DB Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
            }
        }

        // nothing matched
        JOptionPane.showMessageDialog(parent, "No suitable table/columns found to update player team.", "DB Error",
                JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // -------------------------------------------------------------------------
    // DENBORALDIEN EGOERAK (DB oinarritutako laguntzaileak)
    // -------------------------------------------------------------------------
    /**
	 * Denboraldi guztien egoerak DB-tik kargatzen ditu. Itzultzen duen mapa: denboraldia -> boolean[2] {hasita, amaituta}.
	 * Ez badago ezer, map huts bat itzuliko da. DB-ra ezin bada konektatu, null itzuliko du.
	 */
	public static Map<String, boolean[]> loadAllSeasonStatesFromDB(Component parent) {
        if (!isDatabaseReachable()) return null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // ensure table exists
            String create = "CREATE TABLE IF NOT EXISTS season_states (season VARCHAR(128) PRIMARY KEY, started TINYINT(1), finalized TINYINT(1))";
            try (PreparedStatement ps = conn.prepareStatement(create)) { ps.execute(); }

            String sql = "SELECT season, started, finalized FROM season_states";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                Map<String, boolean[]> map = new HashMap<>();
                while (rs.next()) {
                    String season = rs.getString("season");
                    boolean started = rs.getInt("started") != 0;
                    boolean finalized = rs.getInt("finalized") != 0;
                    map.put(season == null ? "" : season, new boolean[] { started, finalized });
                }
                return map;
            }
        } catch (SQLException ex) {
            recordSqlAttempt("SeasonStates load failed: " + ex.getMessage());
            if (DEBUG) ex.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
	 * Denboraldi baten egoera DB-ra gorde (insert edo update). Arrakastaz gorde badu true itzuliko du.
	 */
	public static boolean saveSeasonStateToDB(Component parent, String season, boolean started, boolean finalized) {
        if (season == null) season = "";
        if (!isDatabaseReachable()) return false;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String create = "CREATE TABLE IF NOT EXISTS season_states (season VARCHAR(128) PRIMARY KEY, started TINYINT(1), finalized TINYINT(1))";
            try (PreparedStatement ps = conn.prepareStatement(create)) { ps.execute(); }

            String sql = "INSERT INTO season_states (season, started, finalized) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE started = VALUES(started), finalized = VALUES(finalized)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, season);
                ps.setInt(2, started ? 1 : 0);
                ps.setInt(3, finalized ? 1 : 0);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException ex) {
            recordSqlAttempt("SeasonStates save failed: " + ex.getMessage());
            if (DEBUG) ex.printStackTrace();
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // SAILKAPENA BERRIKUSI ETA GORDEN DB-ra (TOLERANTE A ESQUEMAS)
    // -------------------------------------------------------------------------
    public static boolean recalculateAndSaveSailkapena(Component parent, String season) {
        if (season == null || season.trim().isEmpty()) return false;
        if (!isDatabaseReachable()) {
            if (!dbNotified) {
                dbNotified = true;
                JOptionPane.showMessageDialog(parent,
                        "Cannot connect to database server at " + URL + ".\nCannot recalculate classification.", "DB Unreachable",
                        JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        // Load matches using tolerant loader
        Object[] res = loadPartidosFromDB(parent, season);
        String[] cols = (res != null && res.length > 0 && res[0] instanceof String[]) ? (String[]) res[0] : new String[0];
        Object[][] rows = (res != null && res.length > 1 && res[1] instanceof Object[][]) ? (Object[][]) res[1] : new Object[0][0];

        if (cols.length == 0) return false;

        // Possible column name variants
        Set<String> localNameCandidates = new HashSet<>(Arrays.asList("Talde_lokala","Taldea_lokala","TaldeLokala","local","local_name","local_id","kod_lokala","Talde_lokala","local_team","Talde_lokala","Talde_lokala"));
        Set<String> visitNameCandidates = new HashSet<>(Arrays.asList("Kampoko_taldea","KampokoTaldea","Kampoko_taldea","visitante","visitante_name","visitante_id","kod_kanpokoa","away","away_team","Kampoko_taldea"));
        Set<String> golsLocalCandidates = new HashSet<>(Arrays.asList("Golak_lokala","Goles_Local","goles_local","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala","Golak_lokala"));
        Set<String> golsVisitCandidates = new HashSet<>(Arrays.asList("Golak_kanpokoak","Goles_Visitante","goles_visitante","Golak_kanpokoak"));
        Set<String> resultCandidates = new HashSet<>(Arrays.asList("Emaitza","emaitza","resultado","resultado_final","Resultado"));

        Map<String, Integer> colIndex = new HashMap<>();
        for (int i = 0; i < cols.length; i++) {
            String c = cols[i] == null ? "" : cols[i].toString();
            colIndex.put(c.toLowerCase(), i);
        }

        Integer idxLocal = null, idxVisit = null, idxGLocal = null, idxGVisit = null, idxResult = null;
        for (String name : colIndex.keySet()) {
            if (idxLocal == null && (name.contains("local") || name.contains("talde") || name.contains("lokal"))) idxLocal = colIndex.get(name);
            if (idxVisit == null && (name.contains("visit") || name.contains("kanp") || name.contains("kamp") || name.contains("away"))) idxVisit = colIndex.get(name);
            if (idxGLocal == null && (name.contains("goles") || name.contains("gol") || name.contains("golak") || name.contains("gf"))) idxGLocal = colIndex.get(name);
            if (idxGVisit == null && (name.contains("goles") || name.contains("gol") || name.contains("golak") || name.contains("ga"))) idxGVisit = colIndex.get(name);
            if (idxResult == null && (name.contains("ema") || name.contains("resul") || name.contains("resultado") || name.contains("result"))) idxResult = colIndex.get(name);
        }

        // fallback: try to detect by substring
        if (idxLocal == null) {
            for (String k : colIndex.keySet()) if (k.contains("local") || k.contains("lokal") || k.contains("talde")) { idxLocal = colIndex.get(k); break; }
        }
        if (idxVisit == null) {
            for (String k : colIndex.keySet()) if (k.contains("visit") || k.contains("kanp") || k.contains("kamp") || k.contains("away")) { idxVisit = colIndex.get(k); break; }
        }
        if (idxGLocal == null) {
            for (String k : colIndex.keySet()) if (k.contains("goles") || k.contains("gol") || k.contains("golak") || k.contains("golak") || k.contains("golak_lokala")) { idxGLocal = colIndex.get(k); break; }
        }
        if (idxGVisit == null) {
            for (String k : colIndex.keySet()) if (k.contains("visit") && (k.contains("gol") || k.contains("goles") || k.contains("golak"))) { idxGVisit = colIndex.get(k); break; }
        }
        if (idxResult == null) {
            for (String k : colIndex.keySet()) if (k.contains("ema") || k.contains("resul") || k.contains("resultado")) { idxResult = colIndex.get(k); break; }
        }

        // Stats map
        class Stat { int PJ=0, G=0, E=0, P=0, GF=0, GA=0; }
        Map<String, Stat> stats = new HashMap<>();
        BiConsumer<String, String> ensureTeams = (a,b) -> { if (a!=null && !a.trim().isEmpty() && !stats.containsKey(a)) stats.put(a, new Stat()); if (b!=null && !b.trim().isEmpty() && !stats.containsKey(b)) stats.put(b, new Stat()); };

        for (Object[] row : rows) {
            String localTeam = null, visitTeam = null;
            Integer gLocal = null, gVisit = null;
            try {
                if (idxLocal != null && idxLocal < row.length && row[idxLocal] != null) localTeam = String.valueOf(row[idxLocal]);
                if (idxVisit != null && idxVisit < row.length && row[idxVisit] != null) visitTeam = String.valueOf(row[idxVisit]);
                if (idxGLocal != null && idxGLocal < row.length && row[idxGLocal] != null) gLocal = toInt(row[idxGLocal]);
                if (idxGVisit != null && idxGVisit < row.length && row[idxGVisit] != null) gVisit = toInt(row[idxGVisit]);
                if ((gLocal == null || gVisit == null) && idxResult != null && idxResult < row.length && row[idxResult] != null) {
					String resStr = String.valueOf(row[idxResult]);
					// try formats like 2-1 or 2:1 or "2 - 1"
					String[] parts = resStr.split("[-:]");
					if (parts.length >= 2) {
						try { gLocal = Integer.parseInt(parts[0].trim()); } catch (Exception ex) { gLocal = null; }
						try { gVisit = Integer.parseInt(parts[1].trim()); } catch (Exception ex) { gVisit = null; }
					}
				}
            } catch (Exception ignored) {}

            if (localTeam==null && visitTeam==null) continue;
            ensureTeams.accept(localTeam==null?"":localTeam, visitTeam==null?"":visitTeam);
            if (localTeam==null || visitTeam==null) continue; // cannot compute

            Stat sLocal = stats.get(localTeam);
            Stat sVisit = stats.get(visitTeam);
            sLocal.PJ++; sVisit.PJ++;
            int gl = (gLocal==null?0:gLocal);
            int gv = (gVisit==null?0:gVisit);
            sLocal.GF += gl; sLocal.GA += gv;
            sVisit.GF += gv; sVisit.GA += gl;
            if (gLocal == null || gVisit == null) {
                // if no score provided, treat as not played
                continue;
            }
            if (gl > gv) { sLocal.G++; sVisit.P++; }
            else if (gl < gv) { sVisit.G++; sLocal.P++; }
            else { sLocal.E++; sVisit.E++; }
        }

        // Build ordered list
        class RowData { String team; Stat s; int points; int GD; }
        List<RowData> output = new ArrayList<>();
        for (Map.Entry<String, Stat> e : stats.entrySet()) {
            RowData rd = new RowData(); rd.team = e.getKey(); rd.s = e.getValue();
            rd.points = rd.s.G * 2 + rd.s.E * 1; // standard scoring
            rd.GD = rd.s.GF - rd.s.GA;
            output.add(rd);
        }
        output.sort((a,b) -> {
            if (b.points != a.points) return b.points - a.points;
            if (b.GD != a.GD) return b.GD - a.GD;
            if (b.s.GF != a.s.GF) return b.s.GF - a.s.GF;
            return a.team.compareToIgnoreCase(b.team);
        });

        // Convert season to suffix used in table name
        String suf = season.trim(); suf = suf.replaceAll("20([0-9]{2})", "$1"); suf = suf.replace("-", "_");
        String tableName = "sailkapena_" + suf;

        // Write to DB: create table if not exists, then delete and insert rows
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            JOptionPane.showMessageDialog(parent,
                    "MySQL JDBC Driver not found. Cannot save classification. Add mysql-connector-java.jar to classpath.",
                    "Driver missing", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Quote column identifiers in CREATE to be robust against reserved words and ensure columns are created with expected names
        String createSql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` ("
                + "`team` VARCHAR(128) PRIMARY KEY, `PJ` INT, `G` INT, `E` INT, `P` INT, `GF` INT, `GA` INT, `GD` INT, `PTS` INT)";

         try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
             try (PreparedStatement ps = conn.prepareStatement(createSql)) { ps.execute(); }

             // Ensure expected columns exist (if table existed with different schema)
             try {
                 ensureSailkapenaColumns(conn, tableName);
             } catch (SQLException se) {
                 // record and continue; CREATE should have produced correct columns but be tolerant
                 recordSqlAttempt("ensureSailkapenaColumns failed: " + se.getMessage());
                 if (DEBUG) se.printStackTrace();
             }

             // clear existing
             try (PreparedStatement ps = conn.prepareStatement("DELETE FROM `" + tableName + "`")) { ps.executeUpdate(); }

             // Quote column identifiers to avoid problems when identifiers look like literals or reserved words
             String insert = "INSERT INTO `" + tableName + "` (`team`, `PJ`, `G`, `E`, `P`, `GF`, `GA`, `GD`, `PTS`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
             try (PreparedStatement ps = conn.prepareStatement(insert)) {
                 for (RowData r : output) {
                     ps.setString(1, r.team);
                     ps.setInt(2, r.s.PJ);
                     ps.setInt(3, r.s.G);
                     ps.setInt(4, r.s.E);
                     ps.setInt(5, r.s.P);
                     ps.setInt(6, r.s.GF);
                     ps.setInt(7, r.s.GA);
                     ps.setInt(8, r.GD);
                     ps.setInt(9, r.points);
                     ps.addBatch();
                 }
                 ps.executeBatch();
             }
             return true;
         } catch (SQLException ex) {
            recordSqlAttempt("Saving sailkapena failed: " + ex.getMessage());
            if (DEBUG) ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error saving classification to DB: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Ensure the sailkapena table has the expected columns; add missing ones.
    private static void ensureSailkapenaColumns(Connection conn, String tableName) throws SQLException {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("team", "VARCHAR(128)");
        expected.put("PJ", "INT");
        expected.put("G", "INT");
        expected.put("E", "INT");
        expected.put("P", "INT");
        expected.put("GF", "INT");
        expected.put("GA", "INT");
        expected.put("GD", "INT");
        expected.put("PTS", "INT");

        // collect existing columns (lowercase)
        Set<String> existing = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT COLUMN_NAME FROM information_schema.columns WHERE table_schema = ? AND table_name = ?")) {
            String db = conn.getCatalog();
            ps.setString(1, db == null ? "eskubaloi" : db);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String c = rs.getString(1);
                    if (c != null) existing.add(c.toLowerCase());
                }
            }
        }

        for (Map.Entry<String, String> e : expected.entrySet()) {
            if (!existing.contains(e.getKey().toLowerCase())) {
                String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + e.getKey() + "` " + e.getValue();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.execute();
                    recordSqlAttempt("Added missing column with: " + sql);
                }
            }
        }
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        try {
            if (o instanceof Number) return ((Number)o).intValue();
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception e) { return null; }
    }
}
