package com.example.workhours.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.lang.reflect.Array;

public final class ShiftsContract {

    private ShiftsContract(){}

    public final static String CONTENT_AUTHORITY = "com.example.workhours";
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public final static String PATH_SHIFTS = "shifts";


    public static final class ShiftEntry implements BaseColumns{
        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SHIFTS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + ShiftsContract.CONTENT_AUTHORITY + "/" + ShiftsContract.PATH_SHIFTS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + ShiftsContract.CONTENT_AUTHORITY + "/" + ShiftsContract.PATH_SHIFTS;

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
        public final static int[] arrayHolidayOptions = {0, 1, 2, 3};

        public static boolean isValidHoliday(int holiday) {
            if (holiday == HOLIDAY_SHIFT || holiday == HOLIDAY_COMPENSATION || holiday == HOLIDAY_PUBLIC || holiday == HOLIDAY_VACATION ) {
                return true;
            }
            return false;
        }
    }
}