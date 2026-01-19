package erronka2;

public class klasifikazioa {

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

	// Kontruktore osoa
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
	public int getPosizioa() {
		return posizioa;
	}

	public void setPosizioa(int posizioa) {
		this.posizioa = posizioa;
	}

	public String getTaldeIzena() {
		return taldeIzena;
	}

	public void setTaldeIzena(String taldeIzena) {
		this.taldeIzena = taldeIzena;
	}

	public int getPuntuak() {
		return puntuak;
	}

	public void setPuntuak(int puntuak) {
		this.puntuak = puntuak;
	}

	public int getJp() {
		return jp;
	}

	public void setJp(int jp) {
		this.jp = jp;
	}

	public int getIp() {
		return ip;
	}

	public void setIp(int ip) {
		this.ip = ip;
	}

	public int getGp() {
		return gp;
	}

	public void setGp(int gp) {
		this.gp = gp;
	}

	public int getBp() {
		return bp;
	}

	public void setBp(int bp) {
		this.bp = bp;
	}

	public int getAg() {
		return ag;
	}

	public void setAg(int ag) {
		this.ag = ag;
	}

	public int getKg() {
		return kg;
	}

	public void setKg(int kg) {
		this.kg = kg;
	}

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
