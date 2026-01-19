package erronka2;

public class Arbitroa extends Pertsonak {

	private int esperientziaUrteak;

	public Arbitroa(int pertsonaID, String izena, String abizena, int adina, String nazionalitatea,
			int esperientziaUrteak) {
		super(pertsonaID, izena, abizena, adina, nazionalitatea);
		this.esperientziaUrteak = esperientziaUrteak;
	}

	// Getter eta Setter
	public int getEsperientziaUrteak() {
		return esperientziaUrteak;
	}

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