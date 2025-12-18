package erronka2;

public class Pertsonak {

    protected int pertsonaID;
    protected String izena;
    protected String abizena;
    protected int adina;
    protected String nazionalitatea;

    public Pertsonak() {}

    public Pertsonak(int pertsonaID, String izena, String abizena,
                      int adina, String nazionalitatea) {
        this.pertsonaID = pertsonaID;
        this.izena = izena;
        this.abizena = abizena;
        this.adina = adina;
        this.nazionalitatea = nazionalitatea;
    }
}
