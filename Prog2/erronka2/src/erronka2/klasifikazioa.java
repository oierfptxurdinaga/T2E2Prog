package erronka2;

import java.io.Serializable;

/**
 * Talde baten sailkapen-taulako errenkada irudikatzen du.
 * Puntuak, golak eta jokatutako partidak bezalako estatistikak gordetzen ditu.
 */
public class klasifikazioa implements Serializable {

	private static final long serialVersionUID = 1L;

	private int posizioa;
	private String taldeIzena;
	private int puntuak;
	private int jp; // Jokatutako Partidak
	private int ip; // Irabazitako Partidak
	private int gp; // Galdutako Partidak
	private int bp; // Berdindutako Partidak
	private int ag; // Aldeko Golak
	private int kg; // Kontrako Golak
	private int gd; // Gol Diferentzia

	/**
	 * Estatistika guztiekin eraikitzaile osoa.
	 * * @param posizioa Sailkapeneko posizioa.
	 * @param taldeIzena Taldearen izena.
	 * @param puntuak Puntu totalak.
	 * @param jp Jokatutako partidak.
	 * @param ip Irabazitako partidak.
	 * @param gp Galdutako partidak.
	 * @param bp Berdindutako partidak.
	 * @param ag Aldeko golak.
	 * @param kg Kontrako golak.
	 * @param gd Golen diferentzia.
	 */
	public klasifikazioa(int posizioa, String taldeIzena, int puntuak, int jp, int ip, int gp, int bp, int ag, int kg,
			int gd) {
		this.posizioa = posizioa;
		this.taldeIzena = taldeIzena;
		this.puntuak = puntuak;
		this.jp = jp;
		this.ip = ip;
		this.gp = gp;
		this.bp = bp;
		this.ag = ag;
		this.kg = kg;
		this.gd = gd;
	}

	// Getters y Setters
	
	/** @return Ligako uneko posizioa. */
	public int getPosizioa() {
		return posizioa;
	}

	public void setPosizioa(int posizioa) {
		this.posizioa = posizioa;
	}

	/** @return Taldearen izena. */
	public String getTaldeIzena() {
		return taldeIzena;
	}

	public void setTaldeIzena(String taldeIzena) {
		this.taldeIzena = taldeIzena;
	}

	/** @return Metatutako puntuak. */
	public int getPuntuak() {
		return puntuak;
	}

	public void setPuntuak(int puntuak) {
		this.puntuak = puntuak;
	}

	/** @return Jokatutako partidak. */
	public int getJp() {
		return jp;
	}

	public void setJp(int jp) {
		this.jp = jp;
	}

	/** @return Irabazitako partidak. */
	public int getIp() {
		return ip;
	}

	public void setIp(int ip) {
		this.ip = ip;
	}

	/** @return Galdutako partidak. */
	public int getGp() {
		return gp;
	}

	public void setGp(int gp) {
		this.gp = gp;
	}

	/** @return Berdindutako partidak. */
	public int getBp() {
		return bp;
	}

	public void setBp(int bp) {
		this.bp = bp;
	}

	/** @return Aldeko golak. */
	public int getAg() {
		return ag;
	}

	public void setAg(int ag) {
		this.ag = ag;
	}

	/** @return Kontrako golak. */
	public int getKg() {
		return kg;
	}

	public void setKg(int kg) {
		this.kg = kg;
	}

	/** @return Golen diferentzia. */
	public int getGd() {
		return gd;
	}

	public void setGd(int gd) {
		this.gd = gd;
	}

	// ToString
	@Override
	public String toString() {
		return "Klasifikazioa{" + "posizioa=" + posizioa + ", taldeIzena='" + taldeIzena + '\'' + ", puntuak=" + puntuak
				+ ", jp=" + jp + ", ip=" + ip + ", gp=" + gp + ", bp=" + bp + ", ag=" + ag + ", kg=" + kg + ", gd=" + gd
				+ '}';
	}
}