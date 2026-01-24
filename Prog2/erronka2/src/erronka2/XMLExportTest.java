package erronka2;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 * Test fitxategia XML dokumentuak sortzeko taldeen eta jokalarien informazioarekin.
 * Fitxategi honek XML esportazioa probatzen du.
 */
public class XMLExportTest {

    private static final String XML_OUTPUT_FILE = "taldeak_export.xml";

    public static void main(String[] args) {
        System.out.println("=== XML Export Test ===\n");

        // 1. Proba datuak sortu
        List<Taldea> taldeak = sortuProbaDatuak();

        // 2. XML fitxategia sortu
        boolean success = exportToXML(taldeak, XML_OUTPUT_FILE);

        if (success) {
            System.out.println("\n✓ XML fitxategia ongi sortu da: " + XML_OUTPUT_FILE);
            System.out.println("\nXML edukia:");
            System.out.println("─".repeat(50));
            erakutsiXMLEdukia(XML_OUTPUT_FILE);
        } else {
            System.out.println("\n✗ Errorea XML fitxategia sortzerakoan");
        }

        // 3. XML fitxategia irakurri eta balioztatu
        System.out.println("\n\n=== XML Irakurketa Testa ===");
        testXMLRead(XML_OUTPUT_FILE);
    }

    /**
     * Proba datuak sortzen ditu: taldeak eta jokalariak
     */
    private static List<Taldea> sortuProbaDatuak() {
        List<Taldea> taldeak = new ArrayList<>();

        // 1. Taldea: Bidasoa
        Taldea bidasoa = new Taldea("Bidasoa Irun");
        bidasoa.addJugador(createJokalaria("Mikel", "Aguirre", 28, "Espainia", 7, "12345678A", "Irun Kalea 1", "943123456", "Bidasoa Irun", "Atezaina", "1998-03-15"));
        bidasoa.addJugador(createJokalaria("Ander", "Etxeberria", 25, "Espainia", 11, "23456789B", "Hondarribia Etorb. 5", "943234567", "Bidasoa Irun", "Ezker muturreko", "2001-07-22"));
        bidasoa.addJugador(createJokalaria("Jon", "Zabaleta", 30, "Espainia", 3, "34567890C", "Behobia Kalea 12", "943345678", "Bidasoa Irun", "Erdiko ertzekoa", "1996-11-08"));
        taldeak.add(bidasoa);

        // 2. Taldea: Anaitasuna
        Taldea anaitasuna = new Taldea("Anaitasuna");
        anaitasuna.addJugador(createJokalaria("Patxi", "Iriarte", 27, "Espainia", 9, "45678901D", "Iruñea Plaza 8", "948456789", "Anaitasuna", "Eskuin muturreko", "1999-05-30"));
        anaitasuna.addJugador(createJokalaria("Xabier", "Mendoza", 23, "Espainia", 15, "56789012E", "Txantrea Kalea 3", "948567890", "Anaitasuna", "Pibot", "2003-02-14"));
        taldeak.add(anaitasuna);

        // 3. Taldea: Granollers
        Taldea granollers = new Taldea("BM Granollers");
        granollers.addJugador(createJokalaria("Marc", "Fernandez", 26, "Espainia", 21, "67890123F", "Carrer Major 10", "938678901", "BM Granollers", "Erdiko erdikoa", "2000-09-18"));
        granollers.addJugador(createJokalaria("Pol", "Garcia", 24, "Espainia", 5, "78901234G", "Avinguda Catalunya 22", "938789012", "BM Granollers", "Ezker erdikoa", "2002-12-03"));
        granollers.addJugador(createJokalaria("David", "Martinez", 29, "Espainia", 13, "89012345H", "Plaça Porxada 6", "938890123", "BM Granollers", "Atezaina", "1997-04-25"));
        taldeak.add(granollers);

        System.out.println("Sortu dira " + taldeak.size() + " talde proba datuak bezala:");
        for (Taldea t : taldeak) {
            System.out.println("  - " + t.getNombre() + " (" + t.getJugadores().size() + " jokalari)");
        }

        return taldeak;
    }

    /**
     * Jokalari berri bat sortzen du eremu guztiekin
     */
    private static Jokalaria createJokalaria(String nombre, String apellido, int edad, String nacionalidad,
            int dorsal, String nana, String helbidea, String tlfn, String taldea, String posizioa, String jaiotzeData) {
        Jokalaria j = new Jokalaria(nombre, apellido, edad, nacionalidad, dorsal, nana, helbidea, tlfn, taldea);
        j.setPosizioa(posizioa);
        j.setJaiotzeData(jaiotzeData);
        return j;
    }

    /**
     * Taldeak eta jokalariak XML fitxategi batera esportatzen ditu
     */
    public static boolean exportToXML(List<Taldea> taldeak, String fileName) {
        try {
            // DOM dokumentua sortu
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Root elementua: <eskubaloi_liga>
            Element rootElement = doc.createElement("eskubaloi_liga");
            rootElement.setAttribute("sorrera_data", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            rootElement.setAttribute("denboraldia", "2025-2026");
            doc.appendChild(rootElement);

            // Taldeak elementua
            Element taldeakElement = doc.createElement("taldeak");
            taldeakElement.setAttribute("kopurua", String.valueOf(taldeak.size()));
            rootElement.appendChild(taldeakElement);

            // Talde bakoitza gehitu
            for (Taldea taldea : taldeak) {
                Element taldeaElement = doc.createElement("taldea");
                taldeaElement.setAttribute("izena", taldea.getNombre());

                // Talde informazioa
                Element taldeInfo = doc.createElement("informazioa");
                addTextElement(doc, taldeInfo, "izena", taldea.getNombre());
                addTextElement(doc, taldeInfo, "jokalari_kopurua", String.valueOf(taldea.getJugadores().size()));
                taldeaElement.appendChild(taldeInfo);

                // Jokalariak
                Element jokalariak = doc.createElement("jokalariak");
                for (Jokalaria j : taldea.getJugadores()) {
                    Element jokalariaElement = doc.createElement("jokalaria");
                    jokalariaElement.setAttribute("dortsala", String.valueOf(j.getDorsal()));

                    // Datu pertsonalak
                    Element datuPertsonalak = doc.createElement("datu_pertsonalak");
                    addTextElement(doc, datuPertsonalak, "nana", j.getNana());
                    addTextElement(doc, datuPertsonalak, "izena", j.getNombre());
                    addTextElement(doc, datuPertsonalak, "abizena", j.getApellido());
                    addTextElement(doc, datuPertsonalak, "adina", String.valueOf(j.getEdad()));
                    addTextElement(doc, datuPertsonalak, "jaiotze_data", j.getJaiotzeData());
                    addTextElement(doc, datuPertsonalak, "nazionalitatea", j.getNacionalidad());
                    jokalariaElement.appendChild(datuPertsonalak);

                    // Kontaktu datuak
                    Element kontaktuak = doc.createElement("kontaktu_datuak");
                    addTextElement(doc, kontaktuak, "helbidea", j.getHelbidea());
                    addTextElement(doc, kontaktuak, "telefonoa", j.getTlfn());
                    jokalariaElement.appendChild(kontaktuak);

                    // Kirol datuak
                    Element kirolDatuak = doc.createElement("kirol_datuak");
                    addTextElement(doc, kirolDatuak, "dortsala", String.valueOf(j.getDorsal()));
                    addTextElement(doc, kirolDatuak, "posizioa", j.getPosizioa());
                    addTextElement(doc, kirolDatuak, "taldea", j.getTaldea());
                    jokalariaElement.appendChild(kirolDatuak);

                    jokalariak.appendChild(jokalariaElement);
                }
                taldeaElement.appendChild(jokalariak);
                taldeakElement.appendChild(taldeaElement);
            }

            // Estatistikak gehitu
            Element estatistikak = doc.createElement("estatistikak");
            int totalJokalariak = taldeak.stream().mapToInt(t -> t.getJugadores().size()).sum();
            addTextElement(doc, estatistikak, "talde_totala", String.valueOf(taldeak.size()));
            addTextElement(doc, estatistikak, "jokalari_totala", String.valueOf(totalJokalariak));
            rootElement.appendChild(estatistikak);

            // XML fitxategira idatzi
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);

            return true;

        } catch (ParserConfigurationException | TransformerException e) {
            System.err.println("Errorea XML sortzean: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Testu elementu bat gehitzen du XML dokumentura
     */
    private static void addTextElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.setTextContent(textContent != null ? textContent : "");
        parent.appendChild(element);
    }

    /**
     * XML fitxategiaren edukia pantailan erakusten du
     */
    private static void erakutsiXMLEdukia(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Errorea XML irakurtzen: " + e.getMessage());
        }
    }

    /**
     * XML fitxategia irakurri eta balioztatzen du
     */
    private static void testXMLRead(String fileName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            doc.getDocumentElement().normalize();

            System.out.println("Root elementua: " + doc.getDocumentElement().getNodeName());

            // Taldeak irakurri
            NodeList taldeList = doc.getElementsByTagName("taldea");
            System.out.println("\nAurkitutako taldeak: " + taldeList.getLength());

            for (int i = 0; i < taldeList.getLength(); i++) {
                Node taldeNode = taldeList.item(i);
                if (taldeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element taldeElement = (Element) taldeNode;
                    String izena = taldeElement.getAttribute("izena");

                    NodeList jokalariak = taldeElement.getElementsByTagName("jokalaria");
                    System.out.println("\n  Taldea: " + izena);
                    System.out.println("  Jokalariak (" + jokalariak.getLength() + "):");

                    for (int j = 0; j < jokalariak.getLength(); j++) {
                        Element jokalariaElement = (Element) jokalariak.item(j);
                        String dortsala = jokalariaElement.getAttribute("dortsala");

                        // Datu pertsonalak lortu
                        NodeList datuList = jokalariaElement.getElementsByTagName("datu_pertsonalak");
                        if (datuList.getLength() > 0) {
                            Element datuak = (Element) datuList.item(0);
                            String jokIzena = getElementText(datuak, "izena");
                            String abizena = getElementText(datuak, "abizena");
                            String posizioa = "";

                            // Kirol datuak lortu
                            NodeList kirolList = jokalariaElement.getElementsByTagName("kirol_datuak");
                            if (kirolList.getLength() > 0) {
                                Element kirolDatuak = (Element) kirolList.item(0);
                                posizioa = getElementText(kirolDatuak, "posizioa");
                            }

                            System.out.println("    - #" + dortsala + " " + jokIzena + " " + abizena + " (" + posizioa + ")");
                        }
                    }
                }
            }

            // Estatistikak erakutsi
            NodeList estatistikak = doc.getElementsByTagName("estatistikak");
            if (estatistikak.getLength() > 0) {
                Element stats = (Element) estatistikak.item(0);
                System.out.println("\n=== Estatistikak ===");
                System.out.println("  Talde totala: " + getElementText(stats, "talde_totala"));
                System.out.println("  Jokalari totala: " + getElementText(stats, "jokalari_totala"));
            }

            System.out.println("\n✓ XML fitxategia ondo irakurri da!");

        } catch (Exception e) {
            System.err.println("Errorea XML parseatzean: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Elementu baten testu edukia lortzen du
     */
    private static String getElementText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            return list.item(0).getTextContent();
        }
        return "";
    }
}
