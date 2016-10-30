package org.pg6100.rest.charset;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.utils.web.HttpUtil;
import org.pg6100.utils.web.JBossUtil;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class BestDrinkIT {

    @BeforeClass
    public static void initClass(){
        JBossUtil.waitForJBoss(10);
    }

    @Test
    public void testGetTheBest() throws Exception {

        String message = "GET /charset/drinks/best HTTP/1.1\n";
        message += "Host:localhost:8080\n";
        message += "Accept:text/plain\n";
        message += "\n";

        String response = HttpUtil.executeHttpCommand("localhost",8080, message);
        String body = HttpUtil.getBodyBlock(response);

        assertTrue(body, body.trim().toLowerCase().equals("beer"));
    }


    @Test
    public void testGetTheBestInNorwegian() throws Exception {

        String message = "GET /charset/drinks/best HTTP/1.1\n";
        message += "Host:localhost:8080\n";
        message += "Accept:text/plain\n";
        //for language codes, see http://www.w3schools.com/tags/ref_language_codes.asp
        message += "Accept-Language:no\n";
        message += "\n";

        String response = HttpUtil.executeHttpCommand("localhost",8080, message);
        String body = HttpUtil.getBodyBlock(response);

        assertTrue(body, body.trim().toLowerCase().equals("øl"));
    }

    /*
        There are 3 mains charsets you need to know about:

        ISO-8859-1   https://en.wikipedia.org/wiki/ISO/IEC_8859-1
        UTF-8        https://en.wikipedia.org/wiki/UTF-8
        UTF-16       https://en.wikipedia.org/wiki/UTF-16


        UTF-8 is what you should always use.

        UTF-16 is what Java is using internally when handling String objects, eg "øØåÅæÆ".
        You do not need to care about it, because every-time a string "goes out" (eg save
        to a file or sent in a network connection like TCP), then strings will be
        converted to the chosen charset (typically UTF-8, like this very .java file you
        are reading, but it also depends on the default of the operating system, eg
        the 7-bit US-ASCII).

        ISO-8859-1 is what will give you headaches, and should be avoided at all cost!!!
        Have you ever seen displayed wrong symbols � instead of the characters "øØåÅæÆ"?
        If yes, then most likely someone screwed up and tried to display a
        ISO-8859-1 string with UTF-8.

        Why the need for different charsets?
        - ISO-8859-1 can represent 191 characters (including "øØåÅæÆ"), and use 1 byte.
        - UTF-8 can handle the whole 1,112,064 characters in Unicode, but it is a
          variable length encoding: most common characters need 1 byte, but others (like
          "øØåÅæÆ") can need 2 or more bytes.

        At worst, a document in UTF-8 could be twice as big as ISO-8859-1 (all of the
        191 characters in ISO-8859-1 takes at most 2 bytes in UTF-8).
        However, UTF-8 will avoid not being able to display some special characters (eg,
        think about a chat/forum in which users might want to write in Japanese or
        Chinese). Often, the overhead of UTF-8 is negligible.
        Furthermore, UTF-8 has much larger market share (eg, 87.7% vs 5.8%, see Wikipedia).
        In many systems/programs, the default is UTF-8.
     */

    @Test
    public void testGetWithDifferentCharset() throws Exception{

        String message = "GET /charset/drinks/best HTTP/1.1\n";
        message += "Host:localhost:8080\n";
        message += "Accept:text/plain;charset=ISO-8859-1\n";
        message += "Accept-Language:en\n";
        message += "\n";

        String response = HttpUtil.executeHttpCommand("localhost",8080, message, "UTF-8");

        String type = HttpUtil.getHeaderValue("Content-Type", response);
        assertTrue(type, type.contains("ISO-8859-1"));

        String body = HttpUtil.getBodyBlock(response);

        //no problem, as "beer", being ASCII, has the same bytes in both UTF-8 and ISO-8859-1
        assertTrue(body, body.trim().toLowerCase().equals("beer"));
    }

    @Test
    public void testGetCharsetProblemIso() throws Exception{

        String message = "GET /charset/drinks/best HTTP/1.1\n";
        message += "Host:localhost:8080\n";
        message += "Accept:text/plain;charset=ISO-8859-1\n";
        message += "Accept-Language:no\n";
        message += "\n";

        String response = HttpUtil.executeHttpCommand("localhost",8080, message, "UTF-8");

        String type = HttpUtil.getHeaderValue("Content-Type", response);
        assertTrue(type, type.contains("ISO-8859-1"));

        String body = HttpUtil.getBodyBlock(response);
        body = body.trim();

        //this now fails, as charset conversion problem
        assertFalse(body, body.toLowerCase().equals("øl"));
        assertEquals(2, body.length()); // first invalid, but still 2 characters
        System.out.println("Read value: "+body);
    }

    @Test
    public void testConversionIso() throws Exception{

         /*
            In ISO-8859-1 , "Øl" has byte values 216 and 108

            In UTF-8, the value 108 represent an "l".
            However, 216 is the start of a multi-byte character,
            ie it is a "leading byte" (11xxxxxx).
            But, the following 108, being ASCII (0xxxxxxx), is not
            a "continuation byte" (10xxxxxx),
            and so the system has no idea how to display such invalid
            byte sequence
         */

        String value = "Øl"; // this is in UTF-16

        byte[] asIso = value.getBytes("ISO-8859-1");

        assertEquals(2, asIso.length);

        int first = asIso[0] & 0xFF; //Java does not have unsigned bytes
        int second = asIso[1] & 0xFF;

        assertEquals(216, first);
        assertEquals(108, second);

        String inUtf8 = new String(asIso, Charset.forName("UTF-8"));
        assertNotEquals(value, inUtf8);
        assertEquals("�l", inUtf8);
    }

    @Test
    public void testGetCharsetProblemUtf8() throws Exception{

        String message = "GET /charset/drinks/best HTTP/1.1\n";
        message += "Host:localhost:8080\n";
        message += "Accept:text/plain;charset=UTF-8\n";
        message += "Accept-Language:no\n";
        message += "\n";

        String response = HttpUtil.executeHttpCommand("localhost",8080, message, "ISO-8859-1");

        String type = HttpUtil.getHeaderValue("Content-Type", response);
        assertTrue(type, type.contains("UTF-8"));

        String body = HttpUtil.getBodyBlock(response);
        body = body.trim();

        //this now fails, as charset conversion problem
        assertFalse(body, body.toLowerCase().equals("øl"));
        assertEquals(3, body.length()); // first invalid, but still 2 characters

        assertEquals("Ã\u0098l", body);
        System.out.println("Body: "+ body);
    }

    @Test
    public void testConversionUtf8() throws Exception{

        String value = "Øl"; // this is in UTF-16

        byte[] asUtf = value.getBytes("UTF-8");

        assertEquals(3, asUtf.length);

        int first = asUtf[0] & 0xFF; //Java does not have unsigned bytes
        int second = asUtf[1] & 0xFF;
        int third = asUtf[2] & 0xFF;

        //leading byte for Ø, but in ISO-8859-1 it is Ã
        assertEquals(0b11000011, first);
        assertEquals(195, first);

        //continuation byte for Ø, which in ISO-8859-1 is special
        //control "Start of String" (not an actual character).
        //however, to display it in a Java "" string, as it is UTF-16,
        //I need to use its unicode, which is 0098 (exadecimal), ie \u0098
        assertEquals(0b10011000, second);
        assertEquals(152, second);

        //single byte character (ASCII) as it starts with 0
        assertEquals(0b01101100, third);
        assertEquals(108, third);

        String inIso = new String(asUtf, Charset.forName("ISO-8859-1"));
        assertNotEquals(value, inIso);
        assertEquals(3, inIso.length());

        assertEquals("Ã\u0098l", inIso);
    }

}