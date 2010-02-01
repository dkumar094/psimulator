package Main;

import datoveStruktury.IpAdresa;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.print.DocFlavor.STRING;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.Attributes;
import pocitac.AbstractPocitac;
import pocitac.SitoveRozhrani;

/**
 * Na základě SAX událostí rekonstruujte elementy a atributy původního
 * XML dokumentu (včetně původního vnoření elementu a jmenných prostorů).
 * Znaková data ignorujte. Zanorené elementy formátujte odstavením pomocí tabulátoru.
 * pouzito http://www.ksi.mff.cuni.cz/~mlynkova/Y36XML/indexCV.html
 * 
 * @author haldyr
 */
/**
 * Náš vlastní content handler pro obsluhu SAX událostí.
 * Implementuje metody interface ContentHandler.
 */
public class SAXHandler implements ContentHandler {

    // Umožnuje zacílit místo v dokumentu, kde vznikla aktualní událost
    Locator locator;
    String tabs = "";
    String namespaces = "";
    String jmenoElementu = "";
    List vsechno = new ArrayList<List>();
    List pocitac = new ArrayList<List>();
    List pocitac2 = new ArrayList<String[]>();
    List rozhrani = new ArrayList<String>();
    final int velikostPoleRozhrani = 10;
    String[] rozhrani2 = new String[velikostPoleRozhrani]; //naddimenzovano
    // pro vypis kostry xml dokumentu
    boolean vypis = false;
    boolean vypis2 = true;
    public int port = -1;
    List hotovePocitace = new ArrayList<AbstractPocitac>();
    String jmenoPC = "";
    String typPC = "";

    /**
     * Nastaví locator
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    private boolean patriDoRozhrani(String localName) {
        if (localName.equals("jmeno") || localName.equals("ip") || localName.equals("mac") || localName.equals("pripojenoK")
                || localName.equals("maska")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * pomocna metoda
     * @param l
     * @return
     */
    private List zkopirujListStringu(List l) {
        List novy = new ArrayList<String>();
        for (Object o : l) {
            novy.add(o);
        }
        return novy;
    }

    /**
     * Pomocna metoda pro pristup k poli (prvky 1 rozhrani jsou v poli)
     * @param s   - co chceme z pole
     * @return  index v poli
     */
    private int dejIndex(String s) {

        int i = 9;
        if (s.equals("jmeno")) {
            i = 0;
        } else if (s.equals("ip")) {
            i = 1;
        } else if (s.equals("maska")) {
            i = 2;
        } else if (s.equals("mac")) {
            i = 3;
        } else if (s.equals("pripojenoK")) {
            i = 4;
        }

        return i;
    }

    /**
     * Obsluha události "zacátek elementu".
     * @param uri URI jmenného prostoru elementu (prázdné, pokud element není v žádném jmenném prostoru)
     * @param localName Lokální jméno elementu (vždy neprázdné)
     * @param qName Kvalifikované jméno (tj. prefix-uri + ':' + localName, pokud je element v nejakém jmenném prostoru, nebo localName, pokud element není v žádném jmenném prostoru)
     * @param atts Atributy elementu
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        jmenoElementu = localName;
        String attsStr = "";
        // pro vypis
        for (int i = 0; i < atts.getLength(); i++) {
            attsStr += (" " + atts.getQName(i) + "=\"" + atts.getValue(i) + "\"");
        }

        if (localName.equals("pocitac")) {

            jmenoPC = "";// dulezite
            typPC = "";// dulezite

            for (int i = 0; i < atts.getLength(); i++) {
                if (atts.getQName(i).equals("jmeno")) {
                    jmenoPC = atts.getValue(i);
                }
                if (atts.getQName(i).equals("typ")) {
                    typPC = atts.getValue(i);
                }
            }
        }

        if (localName.equals("pocitac")) {
//            pocitac.clear();
            pocitac2.clear();


        }

        if (localName.equals("rozhrani")) {
//            rozhrani.clear();

            // vymaz rozhrani


            for (int i = 0; i
                    < rozhrani2.length; i++) {
                rozhrani2[i] = "";
            }
        }

        if (vypis) {
            System.out.printf("%s<%s%s%s>\n", tabs, qName, namespaces, attsStr);
        }

        tabs += "\t";
    }

    /**
     * Obsluha události "konec elementu"
     * Parametry mají stejný význam jako u @see startElement
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        tabs = tabs.substring(1);

        if (localName.equals("rozhrani")) {



            String[] pole = new String[rozhrani2.length];
            for (int i = 0; i < rozhrani2.length; i++) {
                pole[i] = rozhrani2[i];
            }
            pocitac2.add(pole);


            /*
            List rozhraniNove = new ArrayList<String>();

            for (Object o : rozhrani) {
            rozhraniNove.add(o);
            }
            pocitac.add(rozhraniNove);
             */

        }


        if (localName.equals("pocitac")) {

//            List pocitacNovy = new ArrayList<List>();
            List pocitacNovy = new ArrayList<String[]>();

            String[] jmenoPCaTyp = new String[2];
            jmenoPCaTyp[0] = jmenoPC;
            jmenoPCaTyp[1] = typPC;
            pocitacNovy.add(jmenoPCaTyp);

            for (Object o : pocitac2) { // v pocitaci je nekolik rozhrani=pole stringu

                String[] p = (String[]) o;
                String[] pnove = new String[p.length];
                for (int i = 0; i < p.length; i++) {
                    pnove[i] = p[i];
                }
                pocitacNovy.add(pnove);

//                String jm = (String)(((List)o).get(0));
//                ((List)o).remove(0);
//                List l = zkopirujListStringu((List) o);
//                l.add(0, jm);
//                pocitacNovy.add(zkopirujListStringu((List) o));
//                pocitacNovy.add(l);
            } //            System.out.println("pocitacNovy: " + pocitacNovy);

            vsechno.add(pocitacNovy);


        }

        if (vypis) {
            System.out.printf("%s</%s>\n", tabs, qName);


        }

    }

    /**
     * Obsluha události "znaková data".
     * SAX parser muže znaková data dávkovat jak chce. Nelze tedy pocítat s tím, že je celý text dorucen v rámci jednoho volání.
     * Text je v poli (ch) na pozicich (start) az (start+length-1).
     * @param ch Pole se znakovými daty
     * @param start Index zacátku úseku platných znakových dat v poli.
     * @param length Délka úseku platných znakových dat v poli.
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {


        String s = new String(ch, start, length);



        if (vypis) { // tisk
            System.out.println(tabs + s);


        }

        if (jmenoElementu.equals("port") && s.length() > 2) {
//            System.out.println("jsem u portu: "+s+" "+s.length());
            try {
                port = Integer.valueOf(s);


            } catch (Exception e) {
                System.out.println("Specifikace portu musi byt ciselna.\nChyba: " + s);
                System.exit(1);


            }

            return;
        }

//        if (jmenoElementu.equals("typ")) {
//            pocitac.add(s);
//        }

//        if (jmenoElementu.equals("maska")) {
//            System.out.println("velikost: " + s.length());


//        } //        System.out.println("jmenoElementu: "+jmenoElementu);

        if (patriDoRozhrani(jmenoElementu)) {
//            rozhrani.add(s);

            rozhrani2[dejIndex(jmenoElementu)] = s;

        }

    }

    /**
     * Obsluha události "deklarace jmenného prostoru".
     * @param prefix Prefix prirazený jmennému prostoru.
     * @param uri URI jmenného prostoru.
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    /**
     * Obsluha události "konec platnosti deklarace jmenného prostoru".
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * Obsluha události "ignorované bílé znaky".
     * Stejné chování a parametry jako @see characters
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    /**
     * Obsluha události "instrukce pro zpracování".
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
    }

    /**
     * Obsluha události "nezpracovaná entita"
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    /**
     * Obsluha události "zacátek dokumentu"
     */
    @Override
    public void startDocument() throws SAXException {
        // ...
    }

    /**
     * Obsluha události "konec dokumentu"
     */
    @Override
    public void endDocument() throws SAXException {

//        System.out.println("vsechno:  " + vsechno);

        String jmenoPC = "";



        for (Object pc : (List) vsechno) { // prichazim pocitace
            List pcList = (List) pc;

            AbstractPocitac absPocitac = new AbstractPocitac(port++);

            String PCjmeno = "";
            String PCtyp = "";
            //TODO: nastavit typ PC (linux|cisco)

            if (vypis2) {
                System.out.println("PC");
            }

            for (Object rozh : pcList) { // prochazim rozhrani u 1 PC
                String[] iface = (String[]) rozh;

                if (iface.length == 2) { // pole se jmenem a typem pocitace
                    PCjmeno = iface[0];
                    PCtyp = iface[1];

                    if (vypis2) {
                        System.out.println(" jmeno: "+PCjmeno);
                        System.out.println(" typ:   "+PCtyp);
                    }

                    continue;
                }

                if (vypis2) {
                    System.out.println("  jmeno: " + iface[dejIndex("jmeno")]);
                    System.out.println("  ip:    " + iface[dejIndex("ip")]);
                    System.out.println("  maska: " + iface[dejIndex("maska")]);
                    System.out.println("  mac:   " + iface[dejIndex("mac")]);
                    System.out.println("  conn:  " + iface[dejIndex("pripojenoK")]);
                    System.out.println("");
                }

                //TODO: kontrola?? Spis bych to nechal kontrolat nekde nahore, at to tady neni moc slozity

                SitoveRozhrani sr = new SitoveRozhrani(iface[dejIndex("jmeno")], absPocitac, iface[dejIndex("mac")]);
                IpAdresa ip = new IpAdresa(iface[dejIndex("ip")], iface[dejIndex("maska")]);
                sr.ip = ip;

                absPocitac.pridejRozhrani(sr);

                //TODO: nastvavit pripojenoK
            }

            hotovePocitace.add(absPocitac);
        }

    }

    public Object vratNastaveni() {
        return vsechno;

    }
}