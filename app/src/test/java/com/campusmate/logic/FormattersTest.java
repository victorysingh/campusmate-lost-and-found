package com.campusmate.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Calendar;

public class FormattersTest {

    private static long day(int year, int month, int dayOfMonth, int hour) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month, dayOfMonth, hour, 0);
        return c.getTimeInMillis();
    }

    @Test
    public void relativeDays() {
        long now = day(2026, Calendar.NOVEMBER, 16, 10);
        assertEquals("Today", Formatters.relativeDay(day(2026, Calendar.NOVEMBER, 16, 1), now));
        assertEquals("Yesterday", Formatters.relativeDay(day(2026, Calendar.NOVEMBER, 15, 23), now));
        assertEquals("3 days ago", Formatters.relativeDay(day(2026, Calendar.NOVEMBER, 13, 9), now));
        assertEquals("2 Nov", Formatters.relativeDay(day(2026, Calendar.NOVEMBER, 2, 9), now));
        assertEquals("16 Nov 2026", Formatters.date(now));
    }
}
