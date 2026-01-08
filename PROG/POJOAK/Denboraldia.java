package erronka2;

import java.util.Date;

public class Denboraldia {

	    private int denboraldiID;
	    private int denboraldiUrtea;
	    private Date denboraldiHasiera;
	    private Date denboraldiAmaiera;
	    private boolean hasita;
	    private boolean amaituta;

	    // Konstruktore osoa
	    public Denboraldia(int denboraldiID, int denboraldiUrtea, Date denboraldiHasiera, Date denboraldiAmaiera, boolean hasita, Boolean amaituta) {
	        this.denboraldiID = denboraldiID;
	        this.denboraldiUrtea = denboraldiUrtea;
	        this.denboraldiHasiera = denboraldiHasiera;
	        this.denboraldiAmaiera = denboraldiAmaiera;
	        this.hasita = hasita;
	        this.amaituta = amaituta;
	    }

	    // Getters y Setters
	    public int getDenboraldiID() {
	        return denboraldiID;
	    }

	    public void setDenboraldiID(int denboraldiID) {
	        this.denboraldiID = denboraldiID;
	    }

	    public int getdenboraldiUrtea() {
	        return denboraldiUrtea;
	    }

	    public void setdenboraldiUrtea(int denboraldiurtea) {
	        this.denboraldiUrtea = denboraldiurtea;
	    }

	    public Date getDenboraldiHasiera() {
	        return denboraldiHasiera;
	    }

	    public void setDenboraldiHasiera(Date denboraldiHasiera) {
	        this.denboraldiHasiera = denboraldiHasiera;
	    }

	    public Date getDenboraldiAmaiera() {
	        return denboraldiAmaiera;
	    }

	    public void setDenboraldiAmaiera(Date denboraldiAmaiera) {
	        this.denboraldiAmaiera = denboraldiAmaiera;
	    }

	    public boolean isHasita() {
	        return hasita;
	    }

	    public void setHasita(boolean hasita) {
	        this.hasita = hasita;
	    }

	    public Boolean getAmaituta() {
	        return amaituta;
	    }

	    public void setAmaituta(Boolean amaituta) {
	        this.amaituta = amaituta;
	    }

	    // ToString
	    @Override
	    public String toString() {
	        return "Denboraldia{" +
	                "denboraldiID=" + denboraldiID +
	                ", denboraldiUrtea=" + denboraldiUrtea +
	                ", denboraldiHasiera=" + denboraldiHasiera +
	                ", denboraldiAmaiera=" + denboraldiAmaiera +
	                ", hasita=" + hasita +
	                ", amaituta=" + amaituta +
	                '}';
	    }
}
