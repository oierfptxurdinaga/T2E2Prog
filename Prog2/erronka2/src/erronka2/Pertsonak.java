package erronka2;

import java.io.Serializable;

/**
 * Sisteman pertsona orokor bat irudikatzen duen oinarrizko klasea.
 * Identifikazioa, izena eta nazionalitatea bezalako ezaugarri komunak ditu.
 * {@link Serializable} inplementatzen du objektuen iraunkortasuna ahalbidetzeko.
 * * @author ZureIzena
 * @version 1.0
 */
public class Pertsonak implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Pertsonaren identifikatzaile bakarra. */
	protected int pertsonaID;
	
	/** Pertsonaren izena. */
	protected String izena;
	
	/** Pertsonaren abizena. */
	protected String abizena;
	
	/** Pertsonaren adina urteetan. */
	protected int adina;
	
	/** Pertsonaren nazionalitatea. */
	protected String nazionalitatea;

	/**
	 * Pertsona bat hasieratzeko eraikitzaile osoa.
	 * * @param pertsonaID Identifikatzaile bakarra.
	 * @param izena Izena.
	 * @param abizena Abizena.
	 * @param adina Adina.
	 * @param nazionalitatea Nazionalitatea.
	 */
	public Pertsonak(int pertsonaID, String izena, String abizena, int adina, String nazionalitatea) {
		this.pertsonaID = pertsonaID;
		this.izena = izena;
		this.abizena = abizena;
		this.adina = adina;
		this.nazionalitatea = nazionalitatea;
	}

	// Getters eta setters

	/**
	 * Pertsonaren IDa lortzen du.
	 * @return Identifikatzaile numerikoa.
	 */
	public int getPertsonaID() {
		return pertsonaID;
	}

	/**
	 * Izena lortzen du.
	 * @return Pertsonaren izena.
	 */
	public String getIzena() {
		return izena;
	}

	/**
	 * Abizena lortzen du.
	 * @return Pertsonaren abizena.
	 */
	public String getAbizena() {
		return abizena;
	}

	/**
	 * Adina lortzen du.
	 * @return Adina urteetan.
	 */
	public int getAdina() {
		return adina;
	}

	/**
	 * Nazionalitatea lortzen du.
	 * @return Nazionalitatea.
	 */
	public String getNazionalitatea() {
		return nazionalitatea;
	}

	// Setters

	/**
	 * Pertsonaren IDa ezartzen du.
	 * @param pertsonaID Identifikatzaile berria.
	 */
	public void setPertsonaID(int pertsonaID) {
		this.pertsonaID = pertsonaID;
	}

	/**
	 * Izena ezartzen du.
	 * @param izena Izen berria.
	 */
	public void setIzena(String izena) {
		this.izena = izena;
	}

	/**
	 * Abizena ezartzen du.
	 * @param abizena Abizen berria.
	 */
	public void setAbizena(String abizena) {
		this.abizena = abizena;
	}

	/**
	 * Adina ezartzen du.
	 * @param adina Adin berria.
	 */
	public void setAdina(int adina) {
		this.adina = adina;
	}

	/**
	 * Nazionalitatea ezartzen du.
	 * @param nazionalitatea Nazionalitate berria.
	 */
	public void setNazionalitatea(String nazionalitatea) {
		this.nazionalitatea = nazionalitatea;
	}

	/**
	 * Objektuaren testu-irudikapena itzultzen du.
	 * @return Pertsonaren datuak dituen katea.
	 */
	@Override
	public String toString() {
		return "Pertsonak{" + "pertsonaID=" + pertsonaID + ", izena='" + izena + '\'' + ", abizena='" + abizena + '\''
				+ ", adina=" + adina + ", nazionalitatea='" + nazionalitatea + '\'' + '}';
	}
}