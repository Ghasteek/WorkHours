package com.example.workhours;

public class Tools {

    public static int dateStrToInt (String dateInput){                                              // TODO dodelat hlidani vlozeneho datumu
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
        return ((Integer.parseInt(timeArray[0]) * 60) + Integer.parseInt(timeArray[1]));
    }

    public static String dateIntToStr (int dateInput) {
        String dateStr = String.valueOf(dateInput);
        String newDate = dateStr.substring(6) + "." + dateStr.substring(4, 6) + "." + dateStr.substring(0, 4);
        return (newDate);
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
}
