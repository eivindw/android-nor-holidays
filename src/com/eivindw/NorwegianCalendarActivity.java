package com.eivindw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NorwegianCalendarActivity extends Activity {

   private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
   protected static final String DEBUG_TAG = "NorwegianCalendarActivity";

   private static final int START_YEAR = 1900;
   private static final int END_YEAR = 2100;

   private static final int DIALOG_ALERT_ID = 0;
   private static final String ERROR_NO_CALENDAR =
      "Finner ikke kalender 'Helligdager'. Denne opprettes manuelt i Google Kalender.";

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      findViewById(R.id.addbtn).setOnClickListener(addBtnListener);

      Spinner spinner = (Spinner) findViewById(R.id.spinner);
      ArrayAdapter<CharSequence> adapter =
         new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
      for (int i = START_YEAR; i < END_YEAR; i++) {
         adapter.add(String.valueOf(i));
      }
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner.setAdapter(adapter);
      spinner.setSelection(currentYear() - START_YEAR);
      spinner.setOnItemSelectedListener(spinnerListener);
   }

   protected Dialog onCreateDialog(int id) {
      Dialog dialog;
      switch (id) {
         case DIALOG_ALERT_ID:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            dialog = builder.setMessage(ERROR_NO_CALENDAR).setCancelable(true).create();
            break;
         default:
            dialog = null;
      }
      return dialog;
   }

   private AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         printHolidays(getYear());
      }

      public void onNothingSelected(AdapterView<?> adapterView) {
      }
   };

   private View.OnClickListener addBtnListener = new View.OnClickListener() {
      public void onClick(View v) {
         if (findHolidaysCalendar() == 0) {
            showDialog(DIALOG_ALERT_ID);
         } else {
            int num = addYearToCalendar(getYear());
            toast("La til " + num + " helligdager i kalenderen.");
         }
      }
   };

   private int addYearToCalendar(int year) {
      int added = 0;
      for (Holiday holiday : NorwegianDateUtil.getHolidays(year)) {
         if(addCalendarEntry(holiday.getDate(), holiday.getName())) {
            added++;
         }
      }
      return added;
   }

   private void printHolidays(int year) {
      TextView tv = (TextView) findViewById(R.id.text);
      String text = "Helligdager " + year + ":\n\n";
      for (Holiday holiday : NorwegianDateUtil.getHolidays(year)) {
         text += format(holiday.getDate()) + " - " + holiday.getName() + "\n";
      }
      tv.setText(text);
   }

   private int getYear() {
      Spinner yrv = (Spinner) findViewById(R.id.spinner);
      return Integer.parseInt(yrv.getSelectedItem().toString());
   }

   private int currentYear() {
      return Calendar.getInstance().get(Calendar.YEAR);
   }

   private String format(Date holiday) {
      return DATE_FORMAT.format(holiday);
   }

   private void toast(String msg) {
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
   }

   private int findHolidaysCalendar() {
      int result = 0;
      String[] projection = new String[]{"_id", "name"};
      String selection = "selected=1";

      Cursor managedCursor = getCalendarManagedCursor(projection, selection);

      if (managedCursor != null && managedCursor.moveToFirst()) {
         int nameColumn = managedCursor.getColumnIndex("name");
         int idColumn = managedCursor.getColumnIndex("_id");

         do {
            String calName = managedCursor.getString(nameColumn);
            String calId = managedCursor.getString(idColumn);
            if (calName != null && calName.contains("Helligdager")) {
               result = Integer.parseInt(calId);
            }
         } while (managedCursor.moveToNext());
      } else {
         Log.i(DEBUG_TAG, "No Calendars");
      }

      return result;
   }

   private boolean addCalendarEntry(Date date, String name) {
      ContentValues event = new ContentValues();

      event.put("calendar_id", findHolidaysCalendar());
      event.put("title", name);
      event.put("description", "Norsk helligdag.");

      event.put("dtstart", date.getTime());
      event.put("dtend", date.getTime());

      event.put("allDay", 1);

      Uri eventsUri = Uri.parse(getCalendarUriBase() + "events");

      Cursor check = managedQuery(
         eventsUri, null, "title=? and dtstart=?", new String[]{name, String.valueOf(date.getTime())}, null);
      if(check == null || check.getCount() == 0) {
         getContentResolver().insert(eventsUri, event);
         Log.i(DEBUG_TAG, "Adding: " + name + " " + date);
         return true;
      } else {
         check.moveToFirst();
         long dtstart = check.getLong(check.getColumnIndex("dtstart"));
         String title = check.getString(check.getColumnIndex("title"));
         Log.i(DEBUG_TAG, "Present: " + title + " " + new Date(dtstart));
         return false;
      }
   }

   private Cursor getCalendarManagedCursor(String[] projection, String selection) {
      Uri calendars = Uri.parse(getCalendarUriBase() + "calendars");

      Cursor managedCursor = null;
      try {
         managedCursor = managedQuery(calendars, projection, selection, null, null);
      } catch (IllegalArgumentException e) {
         Log.w(DEBUG_TAG, "Failed to get provider at [" + calendars.toString() + "]");
      }

      return managedCursor;
   }

   private String getCalendarUriBase() {
      String calendarUriBase = null;
      Uri calendars = Uri.parse("content://calendar/calendars");
      Cursor managedCursor = null;
      try {
         managedCursor = managedQuery(calendars, null, null, null, null);
      } catch (Exception e) {
         // eat
      }

      if (managedCursor != null) {
         calendarUriBase = "content://calendar/";
      } else {
         calendars = Uri.parse("content://com.android.calendar/calendars");
         try {
            managedCursor = managedQuery(calendars, null, null, null, null);
         } catch (Exception e) {
            // eat
         }

         if (managedCursor != null) {
            calendarUriBase = "content://com.android.calendar/";
         }
      }
      return calendarUriBase;
   }

}
