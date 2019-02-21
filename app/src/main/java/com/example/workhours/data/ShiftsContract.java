package com.example.workhours.data;

import android.provider.BaseColumns;

public final class ShiftsContract {

    private ShiftsContract(){}

    public static final class ShiftEntry implements BaseColumns{

        public final static String TABLE_NAME = "shifts";

        public final static String _ID = BaseColumns._ID;               //integer
        public final static String COLUMN_DATE = "date";                //integer, date in format RRRRMMDD
        public final static String COLUMN_ARRIVAL = "arrival";          //integer, time of arrival in seconds
        public final static String COLUMN_DEPARTURE = "departure";      //integer, time of departure in seconds
        public final static String COLUMN_BREAK_LENGHT = "break";       //integer, lenght of break (launch, doctor, etc.) in seconds
        public final static String COLUMN_SHIFT_LENGHT = "shift";       //integer, lengt of shift in seconds
        public final static String COLUMN_HOLIDAY = "holiday";          //integer, description for not be in work, viz. down

        //possible values for COLUMN_HOLIDAY

        public final static int HOLIDAY_SHIFT = 0;                                                      // bez dovolene = normalni smena
        public final static int HOLIDAY_COMPENSATION = 1;                                               // nahradni volno
        public final static int HOLIDAY_PUBLIC = 2;                                                     // statni svatek
        public final static int HOLIDAY_VACATION = 3;                                                   // dovolena
    }
}