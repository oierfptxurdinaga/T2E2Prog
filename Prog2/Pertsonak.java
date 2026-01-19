package erronka2;

public class Pertsonak {

	protected int pertsonaID;
	protected String izena;
	protected String abizena;
	protected int adina;
	protected String nazionalitatea;

	// konstruktore osoa
	public Pertsonak(int pertsonaID, String izena, String abizena, int adina, String nazionalitatea) {
		this.pertsonaID = pertsonaID;
		this.izena = izena;
		this.abizena = abizena;
		this.adina = adina;
		this.nazionalitatea = nazionalitatea;
	}

	// Getters eta setters
	public int getPertsonaID() {
		return pertsonaID;
	}

	public String getIzena() {
		return izena;
	}

	public String getAbizena() {
		return abizena;
	}

	public int getAdina() {
		return adina;
	}

	public String getNazionalitatea() {
		return nazionalitatea;
	}

	// Setters
	public void setPertsonaID(int pertsonaID) {
		this.pertsonaID = pertsonaID;
	}

	public void setIzena(String izena) {
		this.izena = izena;
	}

	public void setAbizena(String abizena) {
		this.abizena = abizena;
	}

	public void setAdina(int adina) {
		this.adina = adina;
	}

	public void setNazionalitatea(String nazionalitatea) {
		this.nazionalitatea = nazionalitatea;
	}

	@Override
	public String toString() {
		return "Pertsonak{" + "pertsonaID=" + pertsonaID + ", izena='" + izena + '\'' + ", abizena='" + abizena + '\''
				+ ", adina=" + adina + ", nazionalitatea='" + nazionalitatea + '\'' + '}';
	}
}
