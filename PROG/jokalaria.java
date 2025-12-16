package erronka2;

public class jokalaria {

	    private int jokalariID;
	    private String izena;
	    private String abizena;
	    private int adina;
	    private String posizioa;
	    private String nazionalitatea;
	    private int dortsala;

	    public jokalaria(int jokalariID, String izena, String abizena, int adina, String posizioa, String nazionalitatea, int dortsala) {
	        this.jokalariID = jokalariID;
	        this.izena = izena;
	        this.abizena = abizena;
	        this.adina = adina;
	        this.posizioa = posizioa;
	        this.nazionalitatea = nazionalitatea;
	        this.dortsala = dortsala;
	    }

	    public int getJokalariID() {
	        return jokalariID;
	    }

	    public void setJokalariID(int jokalariID) {
	        this.jokalariID = jokalariID;
	    }

	    public String getIzena() {
	        return izena;
	    }

	    public void setIzena(String izena) {
	        this.izena = izena;
	    }

	    public String getAbizena() {
	        return abizena;
	    }

	    public void setAbizena(String abizena) {
	        this.abizena = abizena;
	    }

	    public int getAdina() {
	        return adina;
	    }

	    public void setAdina(int adina) {
	        this.adina = adina;
	    }

	    public String getPosizioa() {
	        return posizioa;
	    }

	    public void setPosizioa(String posizioa) {
	        this.posizioa = posizioa;
	    }

	    public String getNazionalitatea() {
	        return nazionalitatea;
	    }

	    public void setNazionalitatea(String nazionalitatea) {
	        this.nazionalitatea = nazionalitatea;
	    }

	    public int getDortsala() {
	        return dortsala;
	    }

	    public void setDortsala(int dortsala) {
	        this.dortsala = dortsala;
	    }

	    @Override
	    public String toString() {
	        return "Jokalaria{" +
	                "jokalariID=" + jokalariID +
	                ", izena='" + izena + '\'' +
	                ", abizena='" + abizena + '\'' +
	                ", adina=" + adina +
	                ", posizioa='" + posizioa + '\'' +
	                ", nazionalitatea='" + nazionalitatea + '\'' +
	                ", dortsala=" + dortsala +
	                '}';
	    }
	}
