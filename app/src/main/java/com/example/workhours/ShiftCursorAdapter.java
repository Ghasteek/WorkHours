package com.example.workhours;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.workhours.data.ShiftsContract;

public class ShiftCursorAdapter extends CursorAdapter {

    public ShiftCursorAdapter (Context context, Cursor c) {
        super(context, c,  0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView shiftLengthView = (TextView) view.findViewById(R.id.shiftLenght);
        TextView dateView = (TextView) view.findViewById(R.id.date);
        TextView holidayTypeView = (TextView) view.findViewById(R.id.holidayType);
        int shiftLength = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGHT));
        int date = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_DATE));
        int holidayType = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY));
        shiftLengthView.setText(String.valueOf(shiftLength));
        dateView.setText(String.valueOf(date));
        holidayTypeView.setText(String.valueOf(holidayType));
    }
}
