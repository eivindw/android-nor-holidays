package com.eivindw;

import java.util.*;

import static java.util.Calendar.*;

public class NorwegianDateUtil {

   public static List<Holiday> getHolidays(int year) {
      List<Holiday> holidayList = new ArrayList<Holiday>();

      // Add set holidays.
      holidayList.add(holiday(getDate(1, JANUARY, year), "1. Nytt\u00e5rsdag"));
      holidayList.add(holiday(getDate(1, MAY, year), "Arbeidernes dag"));
      holidayList.add(holiday(getDate(17, MAY, year), "Nasjonaldag"));
      holidayList.add(holiday(getDate(25, DECEMBER, year), "1. Juledag"));
      holidayList.add(holiday(getDate(26, DECEMBER, year), "2. Juledag"));

      // Add movable holidays - based on easter day.
      Calendar easterDay = getEasterDay(year);

      // Sunday before easter.
      holidayList.add(holiday(rollGetDate(easterDay, -7), "Palmes\u00f8ndag"));

      // Thurday before easter.
      holidayList.add(holiday(rollGetDate(easterDay, -3), "Skj\u00e6rtorsdag"));

      // Friday before easter.
      holidayList.add(holiday(rollGetDate(easterDay, -2), "Langfredag"));

      // Easter day.
      holidayList.add(holiday(easterDay.getTime(), "1. P\u00e5skedag"));

      // Second easter day.
      holidayList.add(holiday(rollGetDate(easterDay, 1), "2. P\u00e5skedag"));

      // "Kristi himmelfart" day.
      holidayList.add(holiday(rollGetDate(easterDay, 39), "Kristi himmelfartsdag"));

      // "Pinse" day.
      holidayList.add(holiday(rollGetDate(easterDay, 49), "1. Pinsedag"));

      // Second "Pinse" day.
      holidayList.add(holiday(rollGetDate(easterDay, 50), "2. Pinsedag"));

      Collections.sort(holidayList);
      
      return holidayList;
   }

   private static Holiday holiday(Date date, String name) {
      return new Holiday(date, name);
   }

   /**
    * Calculates easter day (sunday) by using Spencer Jones formula found here:
    * <a href="http://no.wikipedia.org/wiki/P%C3%A5skeformelen">Wikipedia -
    * Påskeformelen</a>
    *
    * @param year The year to calculate from.
    * @return The Calendar object representing easter day for the given year.
    */
   private static Calendar getEasterDay(int year) {
      int a = year % 19;
      int b = year / 100;
      int c = year % 100;
      int d = b / 4;
      int e = b % 4;
      int f = (b + 8) / 25;
      int g = (b - f + 1) / 3;
      int h = ((19 * a) + b - d - g + 15) % 30;
      int i = c / 4;
      int k = c % 4;
      int l = (32 + (2 * e) + (2 * i) - h - k) % 7;
      int m = (a + (11 * h) + (22 * l)) / 451;
      int n = (h + l - (7 * m) + 114) / 31; // This is the month number.
      int p = (h + l - (7 * m) + 114) % 31; // This is the date minus one.

      return getCalendar(p + 1, n - 1, year);
   }

   private static Date rollGetDate(Calendar calendar, int days) {
      Calendar calCopy = (Calendar) calendar.clone();
      calCopy.add(DATE, days);
      return calCopy.getTime();
   }

   private static Date getDate(int day, int month, int year) {
      return getCalendar(day, month, year).getTime();
   }

   private static Calendar getCalendar(int day, int month, int year) {
      Calendar cal = getInstance(new SimpleTimeZone(0, "GMT"));
      cal.clear();
      cal.set(YEAR, year);
      cal.set(MONTH, month);
      cal.set(DAY_OF_MONTH, day);
      return cal;
   }
}