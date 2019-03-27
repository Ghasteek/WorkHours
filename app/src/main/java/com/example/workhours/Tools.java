package com.example.workhours;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Tools extends AppCompatActivity {

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static int dateStrToInt (String dateInput){
        String[] dateArray = dateInput.split("\\.");
        int dayLength = dateArray[0].length();
        int monthLength = dateArray[1].length();
        if (dayLength == 1) {
            dateArray[0] = "0" + dateArray[0];
        }
        if (monthLength == 1) {
            dateArray[1] = "0" + dateArray[1];
        }
        String dateString = dateArray[2] + dateArray[1] +  dateArray[0];
        return (Integer.parseInt(dateString));
    }

    public static int timeStrToInt (String timeInput){
        String[] timeArray = timeInput.split(":");
        int timeOutput;
        if (timeInput.startsWith("-")){
            timeOutput = (Integer.parseInt(timeArray[0]) * 60) - Integer.parseInt(timeArray[1]);
        } else {
            timeOutput = (Integer.parseInt(timeArray[0]) * 60) + Integer.parseInt(timeArray[1]);
            }
        return (timeOutput);
    }

    public static String dateIntToStr (int dateInput) {
        String dateStr = String.valueOf(dateInput);
        //String newDate = dateStr.substring(6) + "." + dateStr.substring(4, 6) + "." + dateStr.substring(0, 4);
        return (dateStr.substring(6) + "." + dateStr.substring(4, 6) + "." + dateStr.substring(0, 4));
    }

    public static int dateDateToInt (Date dateInput) {
        Calendar helpcal = Calendar.getInstance();
        helpcal.setTime(dateInput);
        String dateOutput = helpcal.get(Calendar.DAY_OF_MONTH) + "." + (helpcal.get(Calendar.MONTH) + 1) + "." + helpcal.get(Calendar.YEAR);
        return(dateStrToInt(dateOutput));
    }

    public static String timeIntToStr (int timeInput){
        boolean isNegative = false;
        String newTime;
        if (timeInput < 0 ) {
            timeInput = Math.abs(timeInput);
            isNegative = true;
        }

        int hours = timeInput / 60;
        int minutes = timeInput - (hours * 60);

        int minutesLength = (int) (Math.log10(minutes) + 1);                     // logarytmicka metoda zjisteni poctu cifer v cisle
        String minutesStr = String.valueOf(minutes) ;
        if (minutesLength == 1){
            minutesStr = "0" + minutesStr;
        }
        if (minutes == 0) {
            minutesStr = "00";
        }
        if (isNegative) {
            newTime = "-" + hours + ":" + minutesStr;
        } else {
            newTime = hours + ":" + minutesStr;
        }
        return (newTime);
    }

    public static String getDayOfWeekStr(int dateInput){
        String [] daysOfWeek = {"Ne", "Po", "Út", "Stř", "Čt", "Pá", "So"};
        Calendar helpCal = Calendar.getInstance();
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Date itemDate = helpCal.getTime();
        try {
            itemDate = originalFormat.parse(String.valueOf(dateInput));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        helpCal.setTime(itemDate);
        int dayOfWeek = helpCal.get(Calendar.DAY_OF_WEEK) - 1;
        return (String.valueOf(daysOfWeek[dayOfWeek]));
    }

    public static String getWorkDaysInMonth (int monthInput, int yearInput) {
        Calendar calendar = Calendar.getInstance();
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int newMonth = monthInput + 1;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String startDateInt = "01-" + newMonth + "-" + yearInput;
        String endDateInt = lastDay + "-" + newMonth + "-" + yearInput;
        Date startDate = null , endDate = null;
        try {
            startDate = sdf.parse(startDateInt);
            endDate = sdf.parse(endDateInt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        int workDaysInMonth = 0;

        while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()) {
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
                    startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                workDaysInMonth++;
            }

            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return(String.valueOf(workDaysInMonth));
    }

    public static String getWorkDaysInPeriod (int startDateInt, int endDateInt) {                   // vrací string s daty jednotlivych pracovnich dni oddelene pomlckami
        //Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        Date startDate = null , endDate = null;
        try {
            startDate = sdf.parse(String.valueOf(startDateInt+1));
            endDate = sdf.parse(String.valueOf(endDateInt-1));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        StringBuilder workDaysInPeriod = new StringBuilder();
        while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()) {
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
                    startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                        int actualDate = Tools.dateDateToInt(startCal.getTime());
                        workDaysInPeriod.append(Tools.dateIntToStr(actualDate)).append("-");
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return(workDaysInPeriod.toString());
    }
}
