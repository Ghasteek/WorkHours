package com.example.workhours;

public class Tools {

    public static int dateStrToInt (String dateInput){                                              // TODO dodelat hlidani vlozeneho datumu
        String[] dateArray = dateInput.split("\\.");
        int dayLenght = (int) (Math.log10(Integer.parseInt(dateArray[0])) + 1);                     // logarytmicka metoda zjisteni poctu cifer v cisle
        int monthLength = (int) (Math.log10(Integer.parseInt(dateArray[1])) + 1);                   // logarytmicka metoda zjisteni poctu cifer v cisle
        if (dayLenght == 1) {
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
}
