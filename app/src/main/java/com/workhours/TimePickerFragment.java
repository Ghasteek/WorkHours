package com.workhours;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String theme = MainActivity.Globals.theme;
        int hour, minute;
        switch (MainActivity.Globals.whichTime) {
            case "IN":
                hour = MainActivity.Globals.timeInHours;//c.get(Calendar.HOUR_OF_DAY);
                minute = MainActivity.Globals.timeInMinutes;//c.get(Calendar.MINUTE);
                switch (theme){
                    case "light":
                        return new TimePickerDialog(getActivity(), R.style.lightTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                    case "dark":
                        return new TimePickerDialog(getActivity(), R.style.darkTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                    default:
                        return new TimePickerDialog(getActivity(), R.style.lightTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                }
                case "OUT":
                hour = MainActivity.Globals.timeOutHours;
                minute = MainActivity.Globals.timeOutMinutes;
                switch (theme){
                    case "light":
                        return new TimePickerDialog(getActivity(), R.style.lightTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                    case "dark":
                        return new TimePickerDialog(getActivity(), R.style.darkTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                    default:
                        return new TimePickerDialog(getActivity(), R.style.lightTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                }
                default:
                Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
                switch (theme){
                    case "light":
                        return new TimePickerDialog(getActivity(), R.style.lightTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                    case "dark":
                        return new TimePickerDialog(getActivity(), R.style.darkTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                    default:
                        return new TimePickerDialog(getActivity(), R.style.lightTimePickerDialogStyle, (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
                }
        }
    }
}