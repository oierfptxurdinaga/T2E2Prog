package erronka2;

import java.util.Date;

public class jaurdunaldia {

	    private int jaurdunaldiID;
	    private Date data;
	    private String etxekoTaldea;
	    private String kampokoTaldea;
	    private String egoera;

	    // Kontruktore osoa
	    public jaurdunaldia(int jaurdunaldiID, Date data, String etxekoTaldea, String kampokoTaldea, String egoera) {
	        this.jaurdunaldiID = jaurdunaldiID;
	        this.data = data;
	        this.etxekoTaldea = etxekoTaldea;
	        this.kampokoTaldea = kampokoTaldea;
	        this.egoera = egoera;
	    }

	    // Getters y Setters
	    public int getJaurdunaldiID() {
	        return jaurdunaldiID;
	    }

	    public void setJaurdunaldiID(int jaurdunaldiID) {
	        this.jaurdunaldiID = jaurdunaldiID;
	    }

	    public Date getData() {
	        return data;
	    }

	    public void setData(Date data) {
	        this.data = data;
	    }

	    public String getEtxekoTaldea() {
	        return etxekoTaldea;
	    }

	    public void setEtxekoTaldea(String etxekoTaldea) {
	        this.etxekoTaldea = etxekoTaldea;
	    }

	    public String getKampokoTaldea() {
	        return kampokoTaldea;
	    }

	    public void setKampokoTaldea(String kampokoTaldea) {
	        this.kampokoTaldea = kampokoTaldea;
	    }

	    public String getEgoera() {
	        return egoera;
	    }

	    public void setEgoera(String egoera) {
	        this.egoera = egoera;
	    }

	    // ToString
	    @Override
	    public String toString() {
	        return "Jaurdunaldia{" +
	                "jaurdunaldiID=" + jaurdunaldiID +
	                ", data=" + data +
	                ", etxekoTaldea='" + etxekoTaldea + '\'' +
	                ", kampokoTaldea='" + kampokoTaldea + '\'' +
	                ", egoera='" + egoera + '\'' +
	                '}';
	    }
	}


