package org.pg6100.news.constraint;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

public class CountryList {

    private static final List<String> countries;

    static {

        URL url = Resources.getResource(CountryList.class, "/country/country_list.txt");
        List<String> list = null;

        try {
            list = Resources.readLines(url, Charset.forName("UTF-8"));
        } catch (IOException e) {
           throw new RuntimeException("Failed to load country list: " + e.toString(), e);
        }

        countries = Collections.unmodifiableList(list);
    }

    public static List<String> getCountries(){
        return countries;
    }

    public static boolean isValidCountry(String country){
        if(country == null){
            return false;
        }
        return getCountries().stream()
                .anyMatch(s -> s.equalsIgnoreCase(country));
    }
}
