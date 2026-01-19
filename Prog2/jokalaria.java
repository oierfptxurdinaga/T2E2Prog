package erronka2;

import java.io.Serializable;

class Jokalaria implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String apellido;
    private int edad;
    private String nacionalidad;
    private int dorsal;
    // UI-rako eskatutako eremu berriak: NANa, Helbidea (helbidea), Tlfn (telefonoa) eta taldea (talde izena)
    private String nana;
    private String helbidea;
    private String tlfn;
    private String taldea;
    // posizio eta jaiotze data eremuak (DB-tik string moduan datoz)
    private String posizioa;
    private String jaiotzeData;

    public Jokalaria() {
        this.nombre = "";
        this.apellido = "";
        this.edad = 0;
        this.nacionalidad = "";
        this.dorsal = 0;
        this.nana = "";
        this.helbidea = "";
        this.tlfn = "";
        this.taldea = "";
        this.posizioa = "";
        this.jaiotzeData = "";
    }

    public Jokalaria(String nombre, String apellido, int edad, String nacionalidad, int dorsal) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.nacionalidad = nacionalidad;
        this.dorsal = dorsal;
        this.nana = "";
        this.helbidea = "";
        this.tlfn = "";
        this.taldea = "";
        this.posizioa = "";
        this.jaiotzeData = "";
    }

    // eraikin berria barne eremu gehiagorekin
    public Jokalaria(String nombre, String apellido, int edad, String nacionalidad, int dorsal, String nana, String helbidea, String tlfn, String taldea) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.nacionalidad = nacionalidad;
        this.dorsal = dorsal;
        this.nana = nana == null ? "" : nana;
        this.helbidea = helbidea == null ? "" : helbidea;
        this.tlfn = tlfn == null ? "" : tlfn;
        this.taldea = taldea == null ? "" : taldea;
        this.posizioa = "";
        this.jaiotzeData = "";
    }

    // Posizioa eta jaiotze data getters/setters
    public String getPosizioa() { return posizioa; }
    public void setPosizioa(String posizioa) { this.posizioa = posizioa == null ? "" : posizioa; }

    public String getJaiotzeData() { return jaiotzeData; }
    public void setJaiotzeData(String jaiotzeData) { this.jaiotzeData = jaiotzeData == null ? "" : jaiotzeData; }

    // Beste getters/setters kodearen gainerako atalak erabiltzen ditu
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    public int getDorsal() { return dorsal; }
    public void setDorsal(int dorsal) { this.dorsal = dorsal; }

    public String getNana() { return nana; }
    public void setNana(String nana) { this.nana = nana; }

    public String getHelbidea() { return helbidea; }
    public void setHelbidea(String helbidea) { this.helbidea = helbidea; }

    public String getTlfn() { return tlfn; }
    public void setTlfn(String tlfn) { this.tlfn = tlfn; }

    public String getTaldea() { return taldea; }
    public void setTaldea(String taldea) { this.taldea = taldea; }

    @Override
    public String toString() {
        return "Jokalaria{" + "nombre='" + nombre + '\'' + ", apellido='" + apellido + '\'' + ", edad=" + edad + ", nacionalidad='" + nacionalidad + '\'' + ", dorsal=" + dorsal + ", nana='" + nana + '\'' + ", helbidea='" + helbidea + '\'' + ", tlfn='" + tlfn + '\'' + ", taldea='" + taldea + '\'' + ", posizioa='" + posizioa + '\'' + ", jaiotzeData='" + jaiotzeData + '\'' + '}';
    }
}