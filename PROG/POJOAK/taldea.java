package erronka2;

public class taldea {

	    private int taldeID;
	    private String izena;
	    private String sorreraUrtea;
	    private String hiria;
	    private int jokalariKop;
	    
	    // Kontruktore osoa
	    public taldea(int taldeID, String izena, String sorreraUrtea, String hiria, int jokalariKop) {
	        this.taldeID = taldeID;
	        this.izena = izena;
	        this.sorreraUrtea = sorreraUrtea;
	        this.hiria = hiria;
	        this.jokalariKop = jokalariKop;
	    }

	    public int getTaldeID() {
	        return taldeID;
	    }

	    public void setTaldeID(int taldeID) {
	        this.taldeID = taldeID;
	    }

	    public String getIzena() {
	        return izena;
	    }

	    public void setIzena(String izena) {
	        this.izena = izena;
	    }

	    public String getSorreraUrtea() {
	        return sorreraUrtea;
	    }

	    public void setSorreraUrtea(String sorreraUrtea) {
	        this.sorreraUrtea = sorreraUrtea;
	    }

	    public String getHiria() {
	        return hiria;
	    }

	    public void setHiria(String hiria) {
	        this.hiria = hiria;
	    }

	    public int getJokalariKop() {
	        return jokalariKop;
	    }

	    public void setJokalariKop(int jokalariKop) {
	        this.jokalariKop = jokalariKop;
	    }

	    @Override
	    public String toString() {
	        return "Taldea{" +
	                "taldeID=" + taldeID +
	                ", izena='" + izena + '\'' +
	                ", sorreraUrtea='" + sorreraUrtea + '\'' +
	                ", hiria='" + hiria + '\'' +
	                ", jokalariKop=" + jokalariKop +
	                '}';
	    }
	}