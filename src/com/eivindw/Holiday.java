package com.eivindw;

import java.util.Date;

public class Holiday implements Comparable {
   private Date date;
   private String name;

   public Holiday(Date date, String name) {
      this.date = date;
      this.name = name;
   }

   public Date getDate() {
      return date;
   }

   public String getName() {
      return name;
   }

   public int compareTo(Object other) {
      return date.compareTo(((Holiday)other).date);
   }
}
