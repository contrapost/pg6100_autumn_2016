package org.pg6100.news.country;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CountryListTest {

    @Test
    public void testList() {
        List<String> list = CountryList.getCountries();
        assertTrue("Wrong size: " + list.size(), list.size() > 200);
        assertTrue(list.stream().anyMatch(s -> s.equalsIgnoreCase("Norway")));
    }
}