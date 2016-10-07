package org.pg6100.rest.weather;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pg6100.rest.weather.automated.metno.Weather;
import org.pg6100.xmlandjson.Converter;
import org.pg6100.xmlandjson.ConverterImpl;

import java.net.URL;

public class XmlExampleTest {


    @Test
    public void testParsing() throws Exception{

        URL url = Resources.getResource("metno_example.xml");
        String xml = Resources.toString(url, Charsets.UTF_8);
        assertNotNull(xml);

        Converter<Weather> converter = new ConverterImpl<>(Weather.class, "/textforecast.xsd");
        Weather weather = converter.fromXML(xml);

        //Data sent by Meteorologisk Institutt (http://met.no/) does not even adhere to their own schemas...
        assertNull(weather);
    }
}
