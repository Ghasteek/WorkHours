package com.workhours;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.workhours.data.ShiftsContract;

@SuppressWarnings("WeakerAccess")
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
        TextView dayView = view.findViewById(R.id.dayView);
        TextView shiftLengthView = view.findViewById(R.id.shiftLength);
        TextView dateView = view.findViewById(R.id.dateView);
        //TextView holidayTypeView = view.findViewById(R.id.holidayType);
        TextView overtimeView = view.findViewById(R.id.overtime);
        ProgressBar shiftBar = view.findViewById(R.id.shiftProgressBar);
        ProgressBar overtimeBar =  view.findViewById(R.id.overtimeProgressBar);

        int shiftLength = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH));
        int date = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_DATE));
        int holidayType = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY));
        int overtime = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_OVERTIME));

        dateView.setText(Tools.dateIntToStr(date));
        dayView.setText(Tools.getDayOfWeekStr(date));

        switch (holidayType) {
            case ShiftsContract.ShiftEntry.HOLIDAY_SHIFT:
                shiftLengthView.setText(Tools.timeIntToStr(shiftLength));
                //holidayTypeView.setText(String.valueOf(holidayType));

                if (overtime > 0) {
                    overtimeView.setText(context.getString(R.string.plusNumber, Tools.timeIntToStr(overtime)));
                } else {
                    overtimeView.setText(Tools.timeIntToStr(overtime));
                }
                shiftBar.setProgress(shiftLength);

                if (shiftLength >= 480) {
                    if (shiftLength > 480) {
                        overtimeBar.setVisibility(View.VISIBLE);
                        overtimeBar.setProgress(shiftLength - 480);
                    }
                    Drawable progressDrawable = shiftBar.getProgressDrawable().mutate();
                    progressDrawable.setColorFilter(Color.parseColor("#55A239"), android.graphics.PorterDuff.Mode.SRC_IN);
                    shiftBar.setProgressDrawable(progressDrawable);
                }
                return;
            case ShiftsContract.ShiftEntry.HOLIDAY_COMPENSATION:
                shiftLengthView.setText(context.getString(R.string.compensation).trim());
                //holidayTypeView.setVisibility(View.GONE);
                shiftBar.setVisibility(View.GONE);
                overtimeBar.setVisibility(View.GONE);
                overtimeView.setVisibility(View.VISIBLE);
                overtimeView.setText(Tools.timeIntToStr(overtime));
                return;
            case ShiftsContract.ShiftEntry.HOLIDAY_VACATION:
                shiftLengthView.setText(context.getString(R.string.vacation));
                //holidayTypeView.setVisibility(View.INVISIBLE);
                shiftBar.setVisibility(View.INVISIBLE);
                overtimeBar.setVisibility(View.INVISIBLE);
                overtimeView.setVisibility(View.INVISIBLE);
                return;
            case ShiftsContract.ShiftEntry.HOLIDAY_INCOMPLETE:
                shiftLengthView.setText(context.getString(R.string.incomplete));
                //holidayTypeView.setVisibility(View.INVISIBLE);
                shiftBar.setVisibility(View.INVISIBLE);
                overtimeBar.setVisibility(View.INVISIBLE);
                overtimeView.setVisibility(View.INVISIBLE);
                return;
            case ShiftsContract.ShiftEntry.HOLIDAY_PUBLIC:
                shiftLengthView.setText(context.getString(R.string.publicHoliday));
                //holidayTypeView.setVisibility(View.INVISIBLE);
                shiftBar.setVisibility(View.INVISIBLE);
                overtimeBar.setVisibility(View.INVISIBLE);
                overtimeView.setVisibility(View.INVISIBLE);
        }
    }
}
