package erronka2;

import java.io.Serializable;
import java.util.Date;

/**
 * Kirol-denboraldi bat irudikatzen du.
 * Hasiera eta amaiera datak, eta denboraldiaren egoera (hasita/amaituta) kudeatzen ditu.
 */
public class Denboraldia implements Serializable {

	private static final long serialVersionUID = 1L;

	private int denboraldiID;
	private int denboraldiUrtea;
	private Date denboraldiHasiera;
	private Date denboraldiAmaiera;
	private boolean hasita;
	private boolean amaituta;

	/**
	 * Eraikitzaile osoa.
	 * * @param denboraldiID Denboraldiaren IDa.
	 * @param denboraldiUrtea Denboraldiaren urtea.
	 * @param denboraldiHasiera Hasiera data.
	 * @param denboraldiAmaiera Amaiera data.
	 * @param hasita Denboraldia hasi den ala ez.
	 * @param amaituta Denboraldia amaitu den ala ez.
	 */
	public Denboraldia(int denboraldiID, int denboraldiUrtea, Date denboraldiHasiera, Date denboraldiAmaiera,
			boolean hasita, Boolean amaituta) {
		this.denboraldiID = denboraldiID;
		this.denboraldiUrtea = denboraldiUrtea;
		this.denboraldiHasiera = denboraldiHasiera;
		this.denboraldiAmaiera = denboraldiAmaiera;
		this.hasita = hasita;
		this.amaituta = amaituta;
	}

	// Getters eta Setters
	
	/** @return Denboraldiaren IDa. */
	public int getDenboraldiID() {
		return denboraldiID;
	}

	/** @param denboraldiID Denboraldiaren ID berria. */
	public void setDenboraldiID(int denboraldiID) {
		this.denboraldiID = denboraldiID;
	}

	/** @return Denboraldiaren urtea. */
	public int getdenboraldiUrtea() {
		return denboraldiUrtea;
	}

	/** @param denboraldiurtea Denboraldiaren urte berria. */
	public void setdenboraldiUrtea(int denboraldiurtea) {
		this.denboraldiUrtea = denboraldiurtea;
	}

	/** @return Hasiera data. */
	public Date getDenboraldiHasiera() {
		return denboraldiHasiera;
	}

	/** @param denboraldiHasiera Hasiera data berria. */
	public void setDenboraldiHasiera(Date denboraldiHasiera) {
		this.denboraldiHasiera = denboraldiHasiera;
	}

	/** @return Amaiera data. */
	public Date getDenboraldiAmaiera() {
		return denboraldiAmaiera;
	}

	/** @param denboraldiAmaiera Amaiera data berria. */
	public void setDenboraldiAmaiera(Date denboraldiAmaiera) {
		this.denboraldiAmaiera = denboraldiAmaiera;
	}

	/** @return true denboraldia hasi bada. */
	public boolean isHasita() {
		return hasita;
	}

	/** @param hasita Hasiera egoera. */
	public void setHasita(boolean hasita) {
		this.hasita = hasita;
	}

	/** @return true denboraldia amaitu bada. */
	public Boolean getAmaituta() {
		return amaituta;
	}

	/** @param amaituta Amaiera egoera. */
	public void setAmaituta(Boolean amaituta) {
		this.amaituta = amaituta;
	}

	// ToString
	@Override
	public String toString() {
		return "Denboraldia{" + "denboraldiID=" + denboraldiID + ", denboraldiUrtea=" + denboraldiUrtea
				+ ", denboraldiHasiera=" + denboraldiHasiera + ", denboraldiAmaiera=" + denboraldiAmaiera + ", hasita="
				+ hasita + ", amaituta=" + amaituta + '}';
	}
}