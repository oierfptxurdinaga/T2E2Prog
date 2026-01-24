package erronka2;

import javax.swing.*;
import java.awt.Component;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import javax.swing.table.DefaultTableModel;

public class GUIren_metodoak {

	// =========================================================================
	// KONEXIO KONSTANTEAK - Datu-basera konektatzeko datuak
	// =========================================================================
	
	private static final String URL = "jdbc:mysql://127.0.0.1:3306/eskubaloi?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000&autoReconnect=true";
	
	/** Datu-baseko erabiltzaile izena */
	private static final String USER = "root";
	
	/** Datu-baseko pasahitza (hutsik defektuz) */
	private static final String PASSWORD = "";
	
	/**
	 * DEBUG modua aktibatzeko/desaktibatzeko.
	 * true: Stack trace osoak erakutsiko dira erroreen kasuan
	 * false: Erroreak isiltasunean kudeatuko dira
	 */
	private static final boolean DEBUG = true;

	// =========================================================================
	// SQL SAIAKERAREN ERREGISTROA - Diagnostikorako
	// =========================================================================
	
	/**
	 * Azken SQL saiakeraren zerrenda sinkronizatua.
	 * GUI-rako erakusteko eta diagnostikorako erabiltzen da.
	 * Gehienez 200 sarrera mantentzen ditu memorian.
	 */
	private static final List<String> lastSqlAttempts = Collections.synchronizedList(new ArrayList<>());

	/**
	 * SQL saiakera bat erregistratzen du diagnostiko helburuetarako.
	 */
	private static void recordSqlAttempt(String s) {
		// Null balioak ez dira erregistratzen
		if (s == null)
			return;
		try {
			// Saiakera gehitu zerrendara
			lastSqlAttempts.add(s);
			// Gehienez 200 sarrera mantendu - zaharrenak ezabatu
			if (lastSqlAttempts.size() > 200)
				lastSqlAttempts.remove(0);
		} catch (Exception ignored) {
			// Edozein errore isiltasunean kudeatu
		}
	}

	/**
	 * Azken SQL saiakeren zerrenda lortu eta garbitu.
	 * Metodo hau thread-safe da (sinkronizatua).
	 */
	public static List<String> drainLastSqlAttempts() {
		List<String> copy;
		synchronized (lastSqlAttempts) {
			// Kopia bat sortu
			copy = new ArrayList<>(lastSqlAttempts);
			// Zerrenda originala garbitu
			lastSqlAttempts.clear();
		}
		return copy;
	}

	// =========================================================================
	// DATU-BASE KONEXIO EGIAZTAPENA
	// =========================================================================
	
	/**
	 * Aldagai boolearra erabiltzaileari behin bakarrik jakinarazteko
	 * datu-basea eskuraezin dagoenean.
	 */
	private static volatile boolean dbNotified = false;

	private static boolean isDatabaseReachable() {
		try {
			String u = URL;
			// "//" aurkitu URL-an (jdbc:mysql://...)
			int p = u.indexOf("//");
			if (p < 0)
				return true; // Ezin bada parseatu, optimista izan
			
			// Host:port zatia atera
			String hostPort = u.substring(p + 2);
			
			// "/" aurkitu datu-base izenaren aurretik
			int slash = hostPort.indexOf('/');
			if (slash > 0)
				hostPort = hostPort.substring(0, slash);
			
			// Query parametroak kendu (? ondoren)
			int q = hostPort.indexOf('?');
			if (q > 0)
				hostPort = hostPort.substring(0, q);
			
			// Host eta port banatu
			String host = hostPort;
			int port = 3306; // MySQL defektuzko portua
			
			if (hostPort.contains(":")) {
				String[] hp = hostPort.split(":");
				host = hp[0];
				try {
					port = Integer.parseInt(hp[1]);
				} catch (Exception ignored) {
					// Defektuzko portua mantendu
				}
			}
			
			// TCP socket bidez konektatu saiatzen da
			try (Socket s = new Socket()) {
				// 1500ms timeout-arekin konektatu
				s.connect(new InetSocketAddress(host, port), 1500);
				return true; // Konexioa arrakastatsua
			} catch (Exception ex) {
				if (DEBUG)
					System.out.println("DB reachability probe failed: " + ex.getMessage());
				return false; // Ezin da konektatu
			}
		} catch (Exception ex) {
			if (DEBUG)
				ex.printStackTrace();
			return true; // Erroreren kasuan, ez oztopatu
		}
	}

	// -------------------------------------------------------------------------
	// TALDEAK KARGATZEKO (PERTSONA TAULAREKIN JOIN EGINEZ)
	// -------------------------------------------------------------------------
	
	/**
	 * Taldeak datu-basitik kargatzen ditu, jokalaria eta pertsona taulekin JOIN eginez.
	 * 
	 * Metodo honek SQL kontsulta konplexu bat exekutatzen du:
	 * - jokalaria taulatik: NANa, Dortsala, Posizioa, Jaiotze_data
	 * - pertsona taulatik: Izen_abizena, Adina, Helbidea, Tlfn
	 * - taldea taulatik: Izena (taldearen izena)
	 * 
	 * LEFT JOIN erabiltzen du, beraz jokalari guztiak itzuliko ditu
	 * nahiz eta pertsona taulan ez egon.
	 * 
	 * Errorea gertatzen bada, .ser fitxategietatik kargatzen saiatuko da (fallback).
	 */
	public static List<Taldea> loadTaldeakFromDB(Component parent, String season) {
		// Emaitza gordetzeko zerrenda sortu
		List<Taldea> listaTaldeak = new ArrayList<>();

		// JDBC Driver kargatu saiatu
		// Driver-a ez badago classpath-ean, ClassNotFoundException jaurtiko du
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			// Driver-a ez dago - erabiltzaileari jakinarazi eta fallback egin
			String msg = "MySQL JDBC Driver ez da aurkitu. Gehitu mysql-connector-java.jar classpath-era.\n"
				+ "Datuak fitxategi serializatuekin kargatuko dira fallback gisa.";
			JOptionPane.showMessageDialog(parent, msg, "Driver JDBC ez aurkitu", JOptionPane.WARNING_MESSAGE);
			// .ser fitxategietatik kargatu alternatiba gisa
			List<Taldea> fromSer = loadTaldeakFromSer(parent, season);
			return fromSer != null ? fromSer : new ArrayList<>();
		}

		// SQL kontsulta konplexua - hiru taula elkartzen ditu
		// COALESCE erabiltzen du: pertsona.Izen_abizena lehentasuna du, bestela jokalaria.Izen_abizena
		String sql = "SELECT t.Izena AS NombreEquipo, " + "j.NANa AS NANa, "
				+ "COALESCE(p.Izen_abizena, j.Izen_abizena) AS Izen_abizena, " + "p.Adina AS Adina, "
				+ "p.Helbidea AS Helbidea, " + "p.Tlfn AS Tlfn, " + "j.Dortsala AS Dortsala, "
				+ "j.Posizioa AS Posizioa, " + "j.Jaiotze_data AS Jaiotze_data " + "FROM jokalaria j "
				+ "LEFT JOIN taldea t ON j.taldea = t.Izena " + "LEFT JOIN pertsona p ON j.NANa = p.NANa "
				+ "ORDER BY t.Izena, j.Dortsala";

		// Try-with-resources: konexioa, statement-a eta resultset-a automatikoki itxiko dira
		try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			// Emaitza bakoitza prozesatu
			while (rs.next()) {
				// Taldearen izena lortu
				String nombreEquipo = rs.getString("NombreEquipo");

				// Taldea bilatu zerrendan edo berria sortu
				Taldea equipo = buscarOcrearTaldea(listaTaldeak, nombreEquipo == null ? "" : nombreEquipo);

				// Jokalariaren NANa lortu (identifikatzaile bakarra)
				String nana = rs.getString("NANa");
				
				// Izen osoa lortu (izena + abizena)
				String nombreCompleto = rs.getString("Izen_abizena");
				
				// Adina lortu - null bada 0 ezarri
				int edad = 0;
				try {
					edad = rs.getInt("Adina");
					if (rs.wasNull()) // SQL NULL balio bada
						edad = 0;
				} catch (SQLException ignore) {
					edad = 0; // Zutabea ez bada existitzen
				}
				
				// Helbidea lortu
				String helbidea = null;
				try {
					helbidea = rs.getString("Helbidea");
				} catch (SQLException ignore) {
					helbidea = null;
				}
				
				// Telefono zenbakia lortu
				String tlfn = null;
				try {
					tlfn = rs.getString("Tlfn");
				} catch (SQLException ignore) {
					tlfn = null;
				}
				
				// Dortsala (kamiseta zenbakia) lortu
				int dorsal = 0;
				try {
					dorsal = rs.getInt("Dortsala");
					if (rs.wasNull())
						dorsal = 0;
				} catch (SQLException ignore) {
					dorsal = 0;
				}
				
				// Posizioa lortu (atezaina, aurrelaria, etab.)
				String posizioa = null;
				try {
					posizioa = rs.getString("Posizioa");
				} catch (SQLException ignore) {
					posizioa = null;
				}
				
				// Jaiotze data lortu
				String jaiotze = null;
				try {
					jaiotze = rs.getString("Jaiotze_data");
				} catch (SQLException ignore) {
					jaiotze = null;
				}

				// Izen osoa banatu: lehenengo zatia izena, gainerakoa abizena
				String nombre = "";
				String apellido = "";
				if (nombreCompleto != null) {
					// Lehen zuriunea aurkitu eta zatitu (gehienez 2 zati)
					String[] partes = nombreCompleto.trim().split(" ", 2);
					nombre = partes[0];
					if (partes.length > 1)
						apellido = partes[1];
				}

				// Jokalaria objektua sortu eta datuak ezarri
				Jokalaria j = new Jokalaria();
				j.setNana(nana);           // NAN/DNI zenbakia
				j.setNombre(nombre);        // Izena
				j.setApellido(apellido);    // Abizena
				j.setEdad(edad);            // Adina
				j.setHelbidea(helbidea);    // Helbidea
				j.setTlfn(tlfn);            // Telefonoa
				j.setDorsal(dorsal);        // Kamiseta zenbakia
				j.setPosizioa(posizioa == null ? "" : posizioa);  // Posizioa
				j.setJaiotzeData(jaiotze == null ? "" : jaiotze); // Jaiotze data
				j.setTaldea(nombreEquipo);  // Taldearen izena

				// Jokalaria taldeari gehitu
				equipo.addJugador(j);
			}

		} catch (SQLException e) {
			// SQL errorea gertatu da
			if (DEBUG)
				e.printStackTrace(); // Debug moduan stack trace erakutsi
			
			// Sintaxi errorea edo zutabe ezezaguna bada
			if (e instanceof SQLSyntaxErrorException
					|| (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown column"))) {
				// Erabiltzaileari jakinarazi eta .ser fitxategietatik kargatu
				String userMsg = "SQL kontsulta errorea: datu-esquema desberdina izan daiteke (zutabea aurkitu ezina).\n"
						+ ".ser fitxategietatik kargatuko da alternatiba gisa.\n\nXehetasuna: " + e.getMessage();
				JOptionPane.showMessageDialog(parent, userMsg, "Error SQL - Fallback", JOptionPane.WARNING_MESSAGE);
				List<Taldea> fromSer = loadTaldeakFromSer(parent, season);
				return fromSer != null ? fromSer : new ArrayList<>();
			}
			// Beste SQL errore bat
			JOptionPane.showMessageDialog(parent, "DB kargatze errorea: " + e.getMessage());
		}

		return listaTaldeak;
	}

	/**
	 * Taldea bilatu zerrendan izenaren arabera, edo berria sortu.
	 * Metodo laguntzailea loadTaldeakFromDB-rentzat.
	 * 
	 * @param lista Taldeen zerrenda non bilatu
	 * @param nombre Taldearen izena
	 * @return Aurkitutako taldea, edo sortu berria
	 */
	private static Taldea buscarOcrearTaldea(List<Taldea> lista, String nombre) {
		// Zerrenda osoan bilatu, ez kontuan hartu maiuskula/minuskula
		for (Taldea t : lista) {
			if (t.getNombre().equalsIgnoreCase(nombre)) {
				return t; // Aurkitu da - itzuli
			}
		}
		// Ez da aurkitu - talde berria sortu eta zerrendara gehitu
		Taldea nuevo = new Taldea(nombre);
		lista.add(nuevo);
		return nuevo;
	}

	// -------------------------------------------------------------------------
	// FITXATEGI METODOAK (.ser) - BATERAGARRITASUN ARRAZOIAK
	// -------------------------------------------------------------------------
	
	/**
	 * Taldeak .ser fitxategi serializatutik kargatzen ditu.
	 * Fallback metodoa gisa erabiltzen da datu-basea ez badago eskuragarri.
	 * 
	 * Fitxategi izen posible desberdinak saiatzen ditu:
	 * - taldeak_<season>.ser (adib: taldeak_2025-2026.ser)
	 * - taldeak_<season_underscore>.ser (adib: taldeak_2025_2026.ser)
	 * - taldeak_<hasiera_urtea>.ser (adib: taldeak_2025.ser)
	 * - taldeak.ser (defektuzko izena)
	 * 
	 * @param parent GUI osagai nagusia (elkarrizketetarako)
	 * @param season Denboraldia (adib: "2025-2026")
	 * @return Taldeen zerrenda, edo zerrenda huts bat ez bada aurkitzen
	 */
	public static List<Taldea> loadTaldeakFromSer(Component parent, String season) {
		// Fitxategi izen hautagaien zerrenda sortu
		List<String> candidates = new ArrayList<>();
		candidates.add("taldeak_" + season + ".ser");
		candidates.add("taldeak_" + season.replace('-', '_') + ".ser");
		// Hasierako urtea bakarrik saiatu (adib: 2025-2026 -> 2025)
		if (season != null && season.length() >= 4) {
			candidates.add("taldeak_" + season.substring(0, 4) + ".ser");
		}
		// Defektuzko izenak gehitu
		candidates.add("taldeak.ser");
		candidates.add("taldeak_2025-2026.ser");
		candidates.add("taldeak_2026-2027.ser");

		// Lan direktorioa lortu
		String base = System.getProperty("user.dir");
		
		// Fitxategi bakoitza saiatu
		for (String name : candidates) {
			File f = new File(base, name);
			if (!f.exists())
				continue; // Fitxategia ez bada existitzen, hurrengo saiatu
			
			// Fitxategia irakurri ObjectInputStream erabiliz
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
				Object obj = ois.readObject();
				
				// Objektu mota egiaztatu eta itzuli
				if (obj instanceof List) {
					// @SuppressWarnings: Generikoak ezin dira runtime-an egiaztatu
					return (List<Taldea>) obj;
				} else if (obj instanceof Taldea[]) {
					// Array bada, List-era bihurtu
					Taldea[] arr = (Taldea[]) obj;
					return new ArrayList<>(Arrays.asList(arr));
				}
			} catch (Exception ex) {
				// Irakurketa errorea
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

	/**
	 * Taldeak .ser fitxategitik kargatu, denboraldia zehaztu gabe.
	 * Overload metodoa.
	 * 
	 * @param parent GUI osagai nagusia
	 * @return Taldeen zerrenda
	 */
	public static List<Taldea> loadTaldeakFromSer(Component parent) {
		return loadTaldeakFromSer(parent, "");
	}

	// -------------------------------------------------------------------------
	// PARTIDUAK DATU-BASETIK KARGATZEKO METODOA
	// Tolerantea da eskema desberdinekiko - hainbat taula eta zutabe izen saiatzen ditu
	// -------------------------------------------------------------------------
	
	/**
	 * Partiduak datu-basetik kargatzen ditu, denboraldiaren arabera iragazita.
	 * 
	 * Metodo hau oso tolerantea da datu-base eskema desberdinekiko:
	 * - Hainbat taula izen saiatzen ditu: partidua, partido, partiduak, partidos, match, matches, partidoa
	 * - Hainbat zutabe izen konbinazio saiatzen ditu (euskaraz eta gazteleraz)
	 * - Hainbat denboraldi formatu saiatzen ditu: 2025-2026, 2025_2026, 25_26, etab.
	 * 
	 * @param parent GUI osagai nagusia
	 * @param season Denboraldia filtratzeko
	 * @return Object[2]: [0]=String[] zutabe izenak, [1]=Object[][] datuak
	 */
	public static Object[] loadPartidosFromDB(Component parent, String season) {
		// Datu-basea eskuragarri dagoen egiaztatu
		if (!isDatabaseReachable()) {
			return new Object[] { new String[0], new Object[0][0] };
		}

		// Taula izen posibleak - euskara, gaztelera eta ingelesa
		String[] tables = new String[] { "partidua", "partido", "partiduak", "partidos", "match", "matches", "partidoa" };

		// Zutabe konbinazio posibleak - eskema desberdinetarako
		String[][] columnSets = new String[][] {
			// Gol banatuak dituen eskema (Golak_lokala eta Golak_kanpokoak)
			{ "Data", "Ordua", "Golak_lokala", "Golak_kanpokoak", "Zelaia", "Talde_lokala", "Kampoko_taldea" },
			// Gaztelera eskema
			{ "data", "ordua", "goles_local", "goles_visitante", "estadio", "local", "visitante" },
			{ "fecha", "hora", "goles_local", "goles_visitante", "estadio", "local_id", "visitante_id" },
			// Emaitza zutabe bakarrarekin (adib: "2-1")
			{ "Data", "Ordua", "Emaitza", "Zelaia", "Talde_lokala", "Kampoko_taldea" },
			{ "data", "ordua", "emaitza", "zelaia", "local", "visitante" },
			{ "fecha", "hora", "resultado", "estadio", "local_id", "visitante_id" }
		};

		// Denboraldi zutabe izen posibleak
		String[] seasonCols = new String[] { "denboraldia", "season", "temporada", "anio", "anno" };

		// Taula, zutabe eta denboraldi konbinazio guztiak saiatu
		for (String table : tables) {
			for (String[] cols : columnSets) {
				for (String seasonCol : seasonCols) {

					// SQL kontsulta eraiki
					StringBuilder queryBuilder = new StringBuilder("SELECT ");
					for (int i = 0; i < cols.length; i++) {
						// Backtick-ak erabili zutabe izenak babesteko
						queryBuilder.append("`").append(cols[i]).append("`");
						if (i < cols.length - 1) queryBuilder.append(", ");
					}
					queryBuilder.append(" FROM `").append(table).append("` WHERE `").append(seasonCol).append("` = ?");

					String sql = queryBuilder.toString();

					// Denboraldi balio posibleak sortu (formatu desberdinak)
					List<String> seasonVariants = new ArrayList<>();
					String s0 = (season == null) ? "" : season.trim();
					seasonVariants.add(s0);                              // 2025-2026
					seasonVariants.add(s0.replace('-', '_'));            // 2025_2026
					seasonVariants.add(s0.replace('-', '/'));            // 2025/2026
					seasonVariants.add(s0.replace('_', '-'));            // Alderantziz
					
					// Formatu laburra: 2025-2026 -> 25_26
					String two = s0.replaceAll("20([0-9]{2})-20([0-9]{2})","$1_$2");
					if (!two.equals(s0)) seasonVariants.add(two);
					
					// 2025-2026 -> 25-26
					String shortDash = s0.replaceAll("20([0-9]{2})-([0-9]{2})","$1-$2");
					if (!shortDash.equals(s0)) seasonVariants.add(shortDash);
					
					// 25-26 -> 25_26
					if (shortDash.contains("-")) seasonVariants.add(shortDash.replace('-', '_'));
					
					// Errepikatuak kendu, ordena mantenduz
					LinkedHashSet<String> svset = new LinkedHashSet<>(seasonVariants);
					seasonVariants = new ArrayList<>(svset);

					// Denboraldi balio bakoitza saiatu
					for (String seasonValue : seasonVariants) {
						// Saiakera erregistratu diagnostikorako
						recordSqlAttempt("Trying SQL: " + sql + " with seasonValue=" + seasonValue);

						try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
								PreparedStatement stmt = conn.prepareStatement(sql)) {

							// Denboraldi parametroa ezarri
							stmt.setString(1, seasonValue);

							try (ResultSet rs = stmt.executeQuery()) {
								// Emaitzak zerrenda batean bildu
								List<Object[]> rows = new ArrayList<>();
								while (rs.next()) {
									Object[] row = new Object[cols.length];
									for (int i = 0; i < cols.length; i++) row[i] = rs.getObject(i + 1);
									rows.add(row);
								}
								// Arraietara bihurtu eta itzuli
								Object[][] data = rows.toArray(new Object[0][]);
								return new Object[] { cols, data };
							}

						} catch (SQLException ex) {
							// Errorea erregistratu
							recordSqlAttempt("SQL failed: " + ex.getMessage());
							String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
							
							// Taula edo zutabea ez bada existitzen, hurrengo konbinazioa saiatu
							if (msg.contains("doesn't exist") || msg.contains("unknown column") || msg.contains("unknown table")) {
								if (DEBUG) System.out.println("SQL variant failed: " + sql + " -> " + ex.getMessage());
								continue;
							}
							// Beste errore bat - itzuli
							if (DEBUG) ex.printStackTrace();
							return new Object[] { new String[0], new Object[0][0] };
						}
					}
				}
			}
		}

		// Ez da konbinazio egokia aurkitu - erabiltzaileari jakinarazi
		JOptionPane.showMessageDialog(parent, "Ez dira 'Golak_lokala' edo 'Golak_kanpokoak' zutabeak aurkitu 'partidua' taulan.", "DB Error", JOptionPane.WARNING_MESSAGE);
		return new Object[] { new String[0], new Object[0][0] };
	}

	// -------------------------------------------------------------------------
	// DENBORALDIAK DATU-BASETIK KARGATZEKO METODOA
	// -------------------------------------------------------------------------
	
	/**
	 * Eskuragarri dauden denboraldiak datu-basetik kargatzen ditu.
	 * 
	 * Metodo honek:
	 * 1. Lehenik DB konexioa egiaztatzen du
	 * 2. Hainbat taula eta zutabe izen konbinazio saiatzen ditu
	 * 3. Huts egiten badu, .ser fitxategietatik kargatzen du (fallback)
	 * 
	 * @param parent GUI osagai nagusia
	 * @return Denboraldien zerrenda (adib: ["2025-2026", "2024-2025"])
	 */
	public static List<String> loadSeasonsFromDB(Component parent) {
		// Lehenik DB eskuragarritasuna egiaztatu
		if (!isDatabaseReachable()) {
			// Erabiltzaileari behin bakarrik jakinarazi
			if (!dbNotified) {
				dbNotified = true;
				JOptionPane.showMessageDialog(parent,
						"Cannot connect to database server at " + URL + ".\nLoading seasons from local files.",
						"DB Unreachable", JOptionPane.WARNING_MESSAGE);
			}
			// Fallback: .ser fitxategietatik kargatu
			List<String> fromFiles = loadSeasonsFromSerFiles();
			return fromFiles;
		}
		
		// Emaitza zerrenda
		List<String> seasons = new ArrayList<>();
		
		// Taula izen posibleak
		String[] tables = new String[] { "partidua", "partido", "partiduak" };
		
		// Denboraldi zutabe izen posibleak
		String[] seasonCols = new String[] { "temporada", "denboraldia", "season", "anno", "anio" };

		// JDBC Driver kargatu saiatu
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

		// Taula eta zutabe konbinazio guztiak saiatu
		for (String table : tables) {
			for (String seasonCol : seasonCols) {
				// SQL kontsulta: denboraldi bakarrak lortu
				String sql = String.format(
						"SELECT DISTINCT `%s` AS s FROM `%s` WHERE `%s` IS NOT NULL ORDER BY `%s` DESC", 
						seasonCol, table, seasonCol, seasonCol);
				
				try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
					// Taula eta zutabea existitzen diren egiaztatu metadata bidez
					try {
						DatabaseMetaData md = conn.getMetaData();
						
						// Taula existitzen den egiaztatu
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
							// Taula ez da existitzen, hurrengo saiatu
							continue;
						}

						// Zutabea existitzen den egiaztatu
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
							// Zutabea ez da existitzen, hurrengo saiatu
							continue;
						}
					} catch (SQLException metaEx) {
						if (DEBUG)
							metaEx.printStackTrace();
						// Metadata huts egiten badu, kontsulta zuzenean saiatu
					}

					// Taula eta zutabea existitzen dira - kontsulta exekutatu
					try (PreparedStatement pst = conn.prepareStatement(sql); 
						 ResultSet rs = pst.executeQuery()) {

						// Emaitzak prozesatu
						while (rs.next()) {
							String s = rs.getString("s");
							if (s != null && !s.trim().isEmpty())
								seasons.add(s);
						}
						// Emaitzak aurkitu badira, itzuli
						if (!seasons.isEmpty())
							return seasons;

					} catch (SQLException ex) {
						if (DEBUG)
							ex.printStackTrace();
						String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
						// Zutabe/taula ezezaguna bada, hurrengo konbinazioa saiatu
						if (msg.contains("unknown column") || msg.contains("doesn't exist")
								|| msg.contains("unknown table")) {
							continue;
						}
						// Beste errore bat - fallback egin
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
						continue; // Hurrengo konbinazioa saiatu
					}
					// Beste errore bat - fallback egin
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

	// -------------------------------------------------------------------------
	// DENBORALDIAK FITXATEGIETATIK KARGATZEKO (FALLBACK)
	// -------------------------------------------------------------------------
	
	/**
	 * Denboraldiak .ser fitxategietatik kargatzen ditu.
	 * Fallback metodoa - datu-basea eskuraezin dagoenean erabiltzen da.
	 * 
	 * Fitxategi izenak eskanatzen ditu: taldeak_<season>.ser
	 * eta season zatia ateratzen du.
	 * 
	 * @return Denboraldien zerrenda, berrienak lehenik ordenatuta
	 */
	private static List<String> loadSeasonsFromSerFiles() {
		List<String> list = new ArrayList<>();
		
		// Lan direktorioa lortu
		String base = System.getProperty("user.dir");
		File dir = new File(base);
		
		// taldeak_*.ser fitxategiak bilatu
		File[] files = dir.listFiles(
				(d, name) -> name.toLowerCase().startsWith("taldeak_") && name.toLowerCase().endsWith(".ser"));
		
		if (files == null)
			return list;
		
		// Fitxategi bakoitzetik denboraldia atera
		for (File f : files) {
			String name = f.getName();
			// Adib: taldeak_2025-2026.ser -> 2025-2026
			String core = name.substring("taldeak_".length(), name.length() - ".ser".length());
			if (!core.trim().isEmpty()) {
				if (!list.contains(core))
					list.add(core);
			}
		}
		
		// Berrienak lehenik ordenatu
		list.sort(Comparator.reverseOrder());
		return list;
	}

	// -------------------------------------------------------------------------
	// SAILKAPENA TAULAK DETEKTATZEKO METODOA
	// -------------------------------------------------------------------------
	
	/**
	 * Datu-basean dauden sailkapena_* taulak detektatzen ditu
	 * eta denboraldi normalizatuak itzultzen ditu.
	 * 
	 * Adibidez: sailkapena_25_26 taula -> "2025-2026" itzultzen du
	 * 
	 * @param parent GUI osagai nagusia
	 * @return Denboraldien zerrenda normalizatuta
	 */
	public static List<String> getAvailableSailkapenaSeasons(Component parent) {
		List<String> out = new ArrayList<>();
		
		// Driver kargatu
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			if (DEBUG)
				e.printStackTrace();
			return out;
		}

		// DB eskuragarritasuna egiaztatu
		if (!isDatabaseReachable()) {
			if (!dbNotified) {
				dbNotified = true;
				JOptionPane.showMessageDialog(parent,
						"Cannot connect to database server at " + URL + ".\nNo sailkapena_* tables will be detected.",
						"DB Unreachable", JOptionPane.WARNING_MESSAGE);
			}
			return out;
		}

		// information_schema kontsultatu sailkapena_* taulak bilatzeko
		String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_name LIKE 'sailkapena_%'";
		try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
				PreparedStatement pst = conn.prepareStatement(sql)) {

			// Datu-base izena ezarri
			pst.setString(1, "eskubaloi");
			
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					String tbl = rs.getString(1);
					if (tbl == null)
						continue;
					// sailkapena_ aurrizkia kendu
					String suf = tbl.substring("sailkapena_".length());
					// Denboraldia normalizatu (adib: 25_26 -> 2025-2026)
					String display = normalizeSeasonFromSuffix(suf);
					if (display != null && !out.contains(display))
						out.add(display);
				}
			}
		} catch (SQLException ex) {
			if (DEBUG)
				ex.printStackTrace();
		}

		// Berrienak lehenik ordenatu
		out.sort(Comparator.reverseOrder());
		return out;
	}

	/**
	 * Taula atzizkitik denboraldi formatu normalizatua lortu.
	 * 
	 * Adibidez:
	 * - "25_26" -> "2025-2026"
	 * - "2025_2026" -> "2025-2026"
	 * 
	 * @param suf Taularen atzizkia
	 * @return Denboraldia formatu normalizatuan
	 */
	private static String normalizeSeasonFromSuffix(String suf) {
		if (suf == null)
			return null;
		
		// Karaktere ez-zenbakizkoak kendu
		String t = suf.replaceAll("[^0-9_\\-]", "_");
		t = t.replace('-', '_');
		
		// Zatitu _ karakterearen bidez
		String[] parts = t.split("_");
		
		if (parts.length >= 2) {
			String a = parts[0];
			String b = parts[1];
			
			// 2 digitukoak badira (25_26), mendea gehitu
			if (a.length() == 2 && b.length() == 2) {
				return "20" + a + "-" + "20" + b;
			}
			// 4 digitukoak badira (2025_2026), marratxoa jarri
			if (a.length() == 4 && b.length() == 4) {
				return a + "-" + b;
			}
		}
		
		// Ezin bada normalizatu, azpimarra marratxoarekin ordezkatu
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

        // Possible column name variants - errepikaturik kendu
        Set<String> localNameCandidates = new HashSet<>(Arrays.asList("Talde_lokala","Taldea_lokala","TaldeLokala","local","local_name","local_id","kod_lokala","local_team"));
        Set<String> visitNameCandidates = new HashSet<>(Arrays.asList("Kampoko_taldea","KampokoTaldea","visitante","visitante_name","visitante_id","kod_kanpokoa","away","away_team"));
        Set<String> golsLocalCandidates = new HashSet<>(Arrays.asList("Golak_lokala","Goles_Local","goles_local","gf_local","gol_local"));
        Set<String> golsVisitCandidates = new HashSet<>(Arrays.asList("Golak_kanpokoak","Goles_Visitante","goles_visitante","gf_visitante","gol_visitante"));
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

        // Write to DB: create table if needed, then delete and insert rows
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

    // -------------------------------------------------------------------------
    // SAILKAPENA TAULA (JTable model) DB-ra gordetzeko
    // -------------------------------------------------------------------------
    /**
     * JTable-ko DefaultTableModel-etik sailkapen taula DB-ra gordetzen du.
     * Denboraldiaren arabera sailkapena_<season> taula erabiliko du (adib. "2024-2025" -> sailkapena_2024_2025).
     */
    public static boolean saveSailkapenaTableToDB(java.awt.Component parent, String season, DefaultTableModel model) {
        if (season == null || season.trim().isEmpty() || model == null) return false;

        if (!isDatabaseReachable()) {
            if (!dbNotified) {
                dbNotified = true;
                JOptionPane.showMessageDialog(parent,
                        "Cannot connect to database server at " + URL + ".\nCannot save classification.",
                        "DB Unreachable", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            JOptionPane.showMessageDialog(parent,
                    "MySQL JDBC Driver not found. Cannot save classification. Add mysql-connector-java.jar to classpath.",
                    "Driver missing", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // normalize season -> table suffix
        String suf = season.trim().replace('-', '_');
        String tableName = "sailkapena_" + suf;

        // detect column names in model (tolerant)
        int colCount = model.getColumnCount();
        if (colCount <= 0) return false;

        // map available columns
        java.util.Map<String, Integer> idx = new java.util.HashMap<>();
        for (int i = 0; i < colCount; i++) {
            String n = String.valueOf(model.getColumnName(i));
            if (n != null) idx.put(n.toLowerCase(), i);
        }

        java.util.function.Function<String[], Integer> pick = (cands) -> {
            for (String c : cands) {
                Integer v = idx.get(c.toLowerCase());
                if (v != null) return v;
            }
            // fallback: contains
            for (String k : idx.keySet()) {
                for (String c : cands) {
                    if (k.contains(c.toLowerCase())) return idx.get(k);
                }
            }
            return null;
        };

        Integer iTeam = pick.apply(new String[] {"team","taldea","talde","equipo","izena","nombre"});
        Integer iPJ = pick.apply(new String[] {"pj","played","partidak"});
        Integer iG  = pick.apply(new String[] {"g","w","wins","irabazi"});
        Integer iE  = pick.apply(new String[] {"e","d","draw","berd"});
        Integer iP  = pick.apply(new String[] {"p","l","loss","galdu"});
        Integer iGF = pick.apply(new String[] {"gf","golak","for"});
        Integer iGA = pick.apply(new String[] {"ga","against","kontra"});
        Integer iGD = pick.apply(new String[] {"gd","diff","diferentzia"});
        Integer iPTS = pick.apply(new String[] {"pts","points","puntu"});

        if (iTeam == null) {
            JOptionPane.showMessageDialog(parent, "Ezin da 'team/taldea' zutabea aurkitu sailkapen taulan.", "Errorea",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String createSql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                "`team` VARCHAR(128) PRIMARY KEY, `PJ` INT, `G` INT, `E` INT, `P` INT, `GF` INT, `GA` INT, `GD` INT, `PTS` INT)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            try (PreparedStatement create = conn.prepareStatement(createSql)) {
                create.execute();
            }
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM `" + tableName + "`")) {
                del.executeUpdate();
            }

            String insSql = "INSERT INTO `" + tableName + "` (`team`,`PJ`,`G`,`E`,`P`,`GF`,`GA`,`GD`,`PTS`) VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ins = conn.prepareStatement(insSql)) {
                for (int r = 0; r < model.getRowCount(); r++) {
                    Object teamObj = model.getValueAt(r, iTeam);
                    String team = teamObj == null ? "" : String.valueOf(teamObj).trim();
                    if (team.isEmpty() || "Ez da sailkapenik aurkitu.".equalsIgnoreCase(team)) continue;

                    ins.setString(1, team);
                    ins.setInt(2, toIntSafe(model, r, iPJ));
                    ins.setInt(3, toIntSafe(model, r, iG));
                    ins.setInt(4, toIntSafe(model, r, iE));
                    ins.setInt(5, toIntSafe(model, r, iP));
                    ins.setInt(6, toIntSafe(model, r, iGF));
                    ins.setInt(7, toIntSafe(model, r, iGA));
                    int gd = (iGD != null) ? toIntSafe(model, r, iGD) : (toIntSafe(model, r, iGF) - toIntSafe(model, r, iGA));
                    ins.setInt(8, gd);
                    int pts = (iPTS != null) ? toIntSafe(model, r, iPTS) : (toIntSafe(model, r, iG) * 2 + toIntSafe(model, r, iE));
                    ins.setInt(9, pts);
                    ins.addBatch();
                }
                ins.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            recordSqlAttempt("saveSailkapenaTableToDB failed: " + ex.getMessage());
            if (DEBUG) ex.printStackTrace();
            try {
                JOptionPane.showMessageDialog(parent, "Ezin izan da sailkapena gorde DB-n: " + ex.getMessage(),
                        "DB Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ignored) {}
            return false;
        }
    }

    private static int toIntSafe(DefaultTableModel model, int row, Integer col) {
        if (col == null) return 0;
        try {
            Object v = model.getValueAt(row, col);
            return toInt(v);
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Object bat (Number/String) int-era bihurtu, ezin bada 0.
     */
    private static int toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return 0;
        // remove non-numeric separators (e.g. "12,0")
        s = s.replace(',', '.');
        // keep leading sign and digits
        try {
            if (s.contains(".")) {
                double d = Double.parseDouble(s);
                return (int) Math.round(d);
            }
            return Integer.parseInt(s);
        } catch (Exception ex) {
            // extract first integer found
            try {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("-?\\d+").matcher(s);
                if (m.find()) return Integer.parseInt(m.group());
            } catch (Exception ignored) {}
            return 0;
        }
    }
}
