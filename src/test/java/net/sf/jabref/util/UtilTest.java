package net.sf.jabref.util;

import net.sf.jabref.model.database.BibtexDatabase;
import net.sf.jabref.model.entry.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;
import net.sf.jabref.gui.preftabs.NameFormatterTab;
import net.sf.jabref.importer.fileformat.BibtexParser;
import net.sf.jabref.importer.ParserResult;
import net.sf.jabref.logic.util.DOI;
import net.sf.jabref.logic.util.strings.StringUtil;
import net.sf.jabref.model.entry.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.StringReader;
import java.util.*;
import java.util.List;

public class UtilTest {

    @Test
    public void testNCase() {
        Assert.assertEquals("", Util.capitalizeFirst(""));
        Assert.assertEquals("Hello world", Util.capitalizeFirst("Hello World"));
        Assert.assertEquals("A", Util.capitalizeFirst("a"));
        Assert.assertEquals("Aa", Util.capitalizeFirst("AA"));
    }

    @Test
    public void testGetPublicationDate() {

        Assert.assertEquals("2003-02", net.sf.jabref.util.Util.getPublicationDate(BibtexParser
                .singleFromString("@ARTICLE{HipKro03, year = {2003}, month = #FEB# }")));

        Assert.assertEquals("2003-03", net.sf.jabref.util.Util.getPublicationDate(BibtexParser
                .singleFromString("@ARTICLE{HipKro03, year = {2003}, month = 3 }")));

        Assert.assertEquals("2003", net.sf.jabref.util.Util.getPublicationDate(BibtexParser
                .singleFromString("@ARTICLE{HipKro03, year = {2003}}")));

        Assert.assertEquals(null, net.sf.jabref.util.Util.getPublicationDate(BibtexParser
                .singleFromString("@ARTICLE{HipKro03, month = 3 }")));

        Assert.assertEquals(null, net.sf.jabref.util.Util.getPublicationDate(BibtexParser
                .singleFromString("@ARTICLE{HipKro03, author={bla}}")));

        Assert.assertEquals("2003-12", net.sf.jabref.util.Util.getPublicationDate(BibtexParser
                .singleFromString("@ARTICLE{HipKro03, year = {03}, month = #DEC# }")));

    }

    @Test
    public void testShaveString() {

        Assert.assertEquals(null, StringUtil.shaveString(null));
        Assert.assertEquals("", StringUtil.shaveString(""));
        Assert.assertEquals("aaa", StringUtil.shaveString("   aaa\t\t\n\r"));
        Assert.assertEquals("a", StringUtil.shaveString("  {a}    "));
        Assert.assertEquals("a", StringUtil.shaveString("  \"a\"    "));
        Assert.assertEquals("{a}", StringUtil.shaveString("  {{a}}    "));
        Assert.assertEquals("{a}", StringUtil.shaveString("  \"{a}\"    "));
        Assert.assertEquals("\"{a\"}", StringUtil.shaveString("  \"{a\"}    "));
    }

    @Test
    public void testCheckLegalKey() {
        Assert.assertEquals("AAAA", net.sf.jabref.util.Util.checkLegalKey("AA AA"));
        Assert.assertEquals("SPECIALCHARS", net.sf.jabref.util.Util.checkLegalKey("SPECIAL CHARS#{\\\"}~,^"));
        Assert.assertEquals("", net.sf.jabref.util.Util.checkLegalKey("\n\t\r"));
    }

    @Test
    @Ignore
    public void testReplaceSpecialCharacters() {
        Assert.assertEquals("Hallo Arger", net.sf.jabref.util.Util.replaceSpecialCharacters("Hallo Arger"));
        // Shouldn't German ï¿½ be resolved to Ae
        Assert.assertEquals("AeaeaAAA", net.sf.jabref.util.Util.replaceSpecialCharacters("ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½"));
    }




    BibtexDatabase database;
    BibtexEntry entry;


    @Before
    public void setUp() {
        // Required by BibtexParser -> FieldContentParser
        Globals.prefs = JabRefPreferences.getInstance();

        StringReader reader = new StringReader(
                "@ARTICLE{HipKro03," + "\n" +
                        "  author = {Eric von Hippel and Georg von Krogh}," + "\n" +
                        "  title = {Open Source Software and the \"Private-Collective\" Innovation Model: Issues for Organization Science}," + "\n" +
                        "  journal = {Organization Science}," + "\n" +
                        "  year = {2003}," + "\n" +
                        "  volume = {14}," + "\n" +
                        "  pages = {209--223}," + "\n" +
                        "  number = {2}," + "\n" +
                        "  address = {Institute for Operations Research and the Management Sciences (INFORMS), Linthicum, Maryland, USA}," + "\n" +
                        "  doi = {http://dx.doi.org/10.1287/orsc.14.2.209.14992}," + "\n" +
                        "  issn = {1526-5455}," + "\n" +
                        "  publisher = {INFORMS}" + "\n" +
                        "}"
                );

        BibtexParser parser = new BibtexParser(reader);
        ParserResult result = null;
        try {
            result = parser.parse();
        } catch (Exception e) {
            Assert.fail();
        }
        database = result.getDatabase();
        entry = database.getEntriesByKey("HipKro03")[0];

        Assert.assertNotNull(database);
        Assert.assertNotNull(entry);
    }

    @Test
    public void testParseMethodCalls() {

        Assert.assertEquals(1, net.sf.jabref.util.Util.parseMethodsCalls("bla").size());
        Assert.assertEquals("bla", (net.sf.jabref.util.Util.parseMethodsCalls("bla").get(0))[0]);

        Assert.assertEquals(1, net.sf.jabref.util.Util.parseMethodsCalls("bla,").size());
        Assert.assertEquals("bla", (net.sf.jabref.util.Util.parseMethodsCalls("bla,").get(0))[0]);

        Assert.assertEquals(1, net.sf.jabref.util.Util.parseMethodsCalls("_bla.bla.blub,").size());
        Assert.assertEquals("_bla.bla.blub", (net.sf.jabref.util.Util.parseMethodsCalls("_bla.bla.blub,").get(0))[0]);

        Assert.assertEquals(2, net.sf.jabref.util.Util.parseMethodsCalls("bla,foo").size());
        Assert.assertEquals("bla", (net.sf.jabref.util.Util.parseMethodsCalls("bla,foo").get(0))[0]);
        Assert.assertEquals("foo", (net.sf.jabref.util.Util.parseMethodsCalls("bla,foo").get(1))[0]);

        Assert.assertEquals(2, net.sf.jabref.util.Util.parseMethodsCalls("bla(\"test\"),foo(\"fark\")").size());
        Assert.assertEquals("bla", (net.sf.jabref.util.Util.parseMethodsCalls("bla(\"test\"),foo(\"fark\")").get(0))[0]);
        Assert.assertEquals("foo", (net.sf.jabref.util.Util.parseMethodsCalls("bla(\"test\"),foo(\"fark\")").get(1))[0]);
        Assert.assertEquals("test", (net.sf.jabref.util.Util.parseMethodsCalls("bla(\"test\"),foo(\"fark\")").get(0))[1]);
        Assert.assertEquals("fark", (net.sf.jabref.util.Util.parseMethodsCalls("bla(\"test\"),foo(\"fark\")").get(1))[1]);

        Assert.assertEquals(2, net.sf.jabref.util.Util.parseMethodsCalls("bla(test),foo(fark)").size());
        Assert.assertEquals("bla", (net.sf.jabref.util.Util.parseMethodsCalls("bla(test),foo(fark)").get(0))[0]);
        Assert.assertEquals("foo", (net.sf.jabref.util.Util.parseMethodsCalls("bla(test),foo(fark)").get(1))[0]);
        Assert.assertEquals("test", (net.sf.jabref.util.Util.parseMethodsCalls("bla(test),foo(fark)").get(0))[1]);
        Assert.assertEquals("fark", (net.sf.jabref.util.Util.parseMethodsCalls("bla(test),foo(fark)").get(1))[1]);
    }

    @Test
    @Ignore
    public void testFieldAndFormat() {
        Assert.assertEquals("Eric von Hippel and Georg von Krogh", net.sf.jabref.util.Util.getFieldAndFormat("[author]", entry, database));

        Assert.assertEquals("Eric von Hippel and Georg von Krogh", net.sf.jabref.util.Util.getFieldAndFormat("author", entry, database));

        Assert.assertEquals(null, net.sf.jabref.util.Util.getFieldAndFormat("[unknownkey]", entry, database));

        Assert.assertEquals(null, net.sf.jabref.util.Util.getFieldAndFormat("[:]", entry, database));

        Assert.assertEquals(null, net.sf.jabref.util.Util.getFieldAndFormat("[:lower]", entry, database));

        Assert.assertEquals("eric von hippel and georg von krogh", net.sf.jabref.util.Util.getFieldAndFormat("[author:lower]", entry, database));

        Assert.assertEquals("HipKro03", net.sf.jabref.util.Util.getFieldAndFormat("[bibtexkey]", entry, database));

        Assert.assertEquals("HipKro03", net.sf.jabref.util.Util.getFieldAndFormat("[bibtexkey:]", entry, database));
    }

    @Test
    @Ignore
    public void testUserFieldAndFormat() {

        String[] names = Globals.prefs.getStringArray(NameFormatterTab.NAME_FORMATER_KEY);
        if (names == null) {
            names = new String[] {};
        }

        String[] formats = Globals.prefs.getStringArray(NameFormatterTab.NAME_FORMATTER_VALUE);
        if (formats == null) {
            formats = new String[] {};
        }

        try {

            List<String> f = new LinkedList<>(Arrays.asList(formats));
            List<String> n = new LinkedList<>(Arrays.asList(names));

            n.add("testMe123454321");
            f.add("*@*@test");

            String[] newNames = n.toArray(new String[n.size()]);
            String[] newFormats = f.toArray(new String[f.size()]);

            Globals.prefs.putStringArray(NameFormatterTab.NAME_FORMATER_KEY, newNames);
            Globals.prefs.putStringArray(NameFormatterTab.NAME_FORMATTER_VALUE, newFormats);

            Assert.assertEquals("testtest", net.sf.jabref.util.Util.getFieldAndFormat("[author:testMe123454321]", entry, database));

        } finally {
            Globals.prefs.putStringArray(NameFormatterTab.NAME_FORMATER_KEY, names);
            Globals.prefs.putStringArray(NameFormatterTab.NAME_FORMATTER_VALUE, formats);
        }
    }

    @Test
    public void testExpandBrackets() {

        Assert.assertEquals("", net.sf.jabref.util.Util.expandBrackets("", entry, database));

        Assert.assertEquals("dropped", net.sf.jabref.util.Util.expandBrackets("drop[unknownkey]ped", entry, database));

        Assert.assertEquals("Eric von Hippel and Georg von Krogh",
                net.sf.jabref.util.Util.expandBrackets("[author]", entry, database));

        Assert.assertEquals("Eric von Hippel and Georg von Krogh are two famous authors.",
                net.sf.jabref.util.Util.expandBrackets("[author] are two famous authors.", entry, database));

        Assert.assertEquals("Eric von Hippel and Georg von Krogh are two famous authors.",
                net.sf.jabref.util.Util.expandBrackets("[author] are two famous authors.", entry, database));

        Assert.assertEquals("Eric von Hippel and Georg von Krogh have published Open Source Software and the \"Private-Collective\" Innovation Model: Issues for Organization Science in Organization Science.",
                net.sf.jabref.util.Util.expandBrackets("[author] have published [title] in [journal].", entry, database));
    }

    @Test
    public void testSanitizeUrl() {

        Assert.assertEquals("http://www.vg.no", net.sf.jabref.util.Util.sanitizeUrl("http://www.vg.no"));
        Assert.assertEquals("http://www.vg.no/fil%20e.html", net.sf.jabref.util.Util.sanitizeUrl("http://www.vg.no/fil e.html"));
        Assert.assertEquals("http://www.vg.no/fil%20e.html", net.sf.jabref.util.Util.sanitizeUrl("http://www.vg.no/fil%20e.html"));
        Assert.assertEquals("www.vg.no/fil%20e.html", net.sf.jabref.util.Util.sanitizeUrl("www.vg.no/fil%20e.html"));

        Assert.assertEquals("www.vg.no/fil%20e.html", net.sf.jabref.util.Util.sanitizeUrl("\\url{www.vg.no/fil%20e.html}"));

        /**
         * Doi Test cases
         */
        Assert.assertEquals(DOI.RESOLVER.resolve("/10.1109/VLHCC.2004.20").toASCIIString(), net.sf.jabref.util.Util.sanitizeUrl("10.1109/VLHCC.2004.20"));
        Assert.assertEquals(DOI.RESOLVER.resolve("/10.1109/VLHCC.2004.20").toASCIIString(), net.sf.jabref.util.Util.sanitizeUrl("doi://10.1109/VLHCC.2004.20"));
        Assert.assertEquals(DOI.RESOLVER.resolve("/10.1109/VLHCC.2004.20").toASCIIString(), net.sf.jabref.util.Util.sanitizeUrl("doi:/10.1109/VLHCC.2004.20"));
        Assert.assertEquals(DOI.RESOLVER.resolve("/10.1109/VLHCC.2004.20").toASCIIString(), net.sf.jabref.util.Util.sanitizeUrl("doi:10.1109/VLHCC.2004.20"));

        /**
         * Additional testcases provided by Hannes Restel and Micha Beckmann.
         */
        Assert.assertEquals("ftp://www.vg.no", net.sf.jabref.util.Util.sanitizeUrl("ftp://www.vg.no"));
        Assert.assertEquals("file://doof.txt", net.sf.jabref.util.Util.sanitizeUrl("file://doof.txt"));
        Assert.assertEquals("file:///", net.sf.jabref.util.Util.sanitizeUrl("file:///"));
        Assert.assertEquals("/src/doof.txt", net.sf.jabref.util.Util.sanitizeUrl("/src/doof.txt"));
        Assert.assertEquals("/", net.sf.jabref.util.Util.sanitizeUrl("/"));
        Assert.assertEquals("/home/user/example.txt", net.sf.jabref.util.Util.sanitizeUrl("/home/user/example.txt"));
    }

    @Test
    public void getSeparatedKeywords() {
        String keywords = "w1, w2a w2b, w3";
        ArrayList<String> separatedKeywords = net.sf.jabref.util.Util.getSeparatedKeywords(keywords);
        String[] expected = new String[]{"w1", "w2a w2b", "w3"};
        Assert.assertArrayEquals(expected, separatedKeywords.toArray());
    }

}
