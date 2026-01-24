package erronka2;

import java.io.Serializable;

/**
 * Arbitro bat irudikatzen du, pertsona baten oinarrizko informazioa hedatuz.
 * Esperientzia-urteak bezalako ezaugarri espezifikoak barne hartzen ditu.
 * * @see Pertsonak
 */
public class Arbitroa extends Pertsonak implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** Arbitro profesionalaren esperientzia urteak. */
	private int esperientziaUrteak;

	/**
	 * Arbitro bat sortzeko eraikitzaile osoa.
	 * * @param pertsonaID Pertsonaren IDa.
	 * @param izena Izena.
	 * @param abizena Abizena.
	 * @param adina Adina.
	 * @param nazionalitatea Nazionalitatea.
	 * @param esperientziaUrteak Arbitraje-esperientzia urteak.
	 */
	public Arbitroa(int pertsonaID, String izena, String abizena, int adina, String nazionalitatea,
			int esperientziaUrteak) {
		super(pertsonaID, izena, abizena, adina, nazionalitatea);
		this.esperientziaUrteak = esperientziaUrteak;
	}

	// Getter eta Setter

	/**
	 * Esperientzia urteak lortzen ditu.
	 * @return Urte kopurua.
	 */
	public int getEsperientziaUrteak() {
		return esperientziaUrteak;
	}

	/**
	 * Esperientzia urteak ezartzen ditu.
	 * @param esperientziaUrteak Urte kopurua.
	 */
	public void setEsperientziaUrteak(int esperientziaUrteak) {
		this.esperientziaUrteak = esperientziaUrteak;
	}

	@Override
	public String toString() {
		return "Arbitroa{" + "pertsonaID=" + getPertsonaID() + ", izena='" + getIzena() + '\'' + ", abizena='"
				+ getAbizena() + '\'' + ", adina=" + getAdina() + ", nazionalitatea='" + getNazionalitatea() + '\''
				+ ", esperientziaUrteak=" + esperientziaUrteak + '}';
	}
}