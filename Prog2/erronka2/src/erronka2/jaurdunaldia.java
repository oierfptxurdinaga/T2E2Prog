package erronka2;

import java.util.Date;

/**
 * Txapelketaren barruko jardunaldi bat irudikatzen du.
 * Partidaren data, taldeak eta egoerari buruzko informazioa gordetzen du.
 */
public class jaurdunaldia {

	private int jaurdunaldiID;
	private Date data;
	private String etxekoTaldea;
	private String kampokoTaldea;
	private String egoera;

	/**
	 * Jardunaldiaren eraikitzaile osoa.
	 * * @param jaurdunaldiID Jardunaldiaren IDa.
	 * @param data Partidaren data.
	 * @param etxekoTaldea Etxeko taldearen izena.
	 * @param kampokoTaldea Kanpoko taldearen izena.
	 * @param egoera Jardunaldiaren/partidaren uneko egoera.
	 */
	public jaurdunaldia(int jaurdunaldiID, Date data, String etxekoTaldea, String kampokoTaldea, String egoera) {
		this.jaurdunaldiID = jaurdunaldiID;
		this.data = data;
		this.etxekoTaldea = etxekoTaldea;
		this.kampokoTaldea = kampokoTaldea;
		this.egoera = egoera;
	}

	// Getters eta Setters
	
	/** @return Jardunaldiaren IDa. */
	public int getJaurdunaldiID() {
		return jaurdunaldiID;
	}

	public void setJaurdunaldiID(int jaurdunaldiID) {
		this.jaurdunaldiID = jaurdunaldiID;
	}

	/** @return Partidaren data. */
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	/** @return Etxeko taldearen izena. */
	public String getEtxekoTaldea() {
		return etxekoTaldea;
	}

	public void setEtxekoTaldea(String etxekoTaldea) {
		this.etxekoTaldea = etxekoTaldea;
	}

	/** @return Kanpoko taldearen izena. */
	public String getKampokoTaldea() {
		return kampokoTaldea;
	}

	public void setKampokoTaldea(String kampokoTaldea) {
		this.kampokoTaldea = kampokoTaldea;
	}

	/** @return Jardunaldiaren egoera. */
	public String getEgoera() {
		return egoera;
	}

	public void setEgoera(String egoera) {
		this.egoera = egoera;
	}

	// ToString
	@Override
	public String toString() {
		return "Jaurdunaldia{" + "jaurdunaldiID=" + jaurdunaldiID + ", data=" + data + ", etxekoTaldea='" + etxekoTaldea
				+ '\'' + ", kampokoTaldea='" + kampokoTaldea + '\'' + ", egoera='" + egoera + '\'' + '}';
	}
}