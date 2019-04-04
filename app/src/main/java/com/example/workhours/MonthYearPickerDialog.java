package com.example.workhours;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import java.util.Calendar;

/**
 * Created by ankititjunkies on 20/03/18.
 */

@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public class MonthYearPickerDialog extends DialogFragment {
    SharedPreferences pref;

    //private static final int MAX_YEAR = 2099;
    private DatePickerDialog.OnDateSetListener listener;

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        pref = requireActivity().getSharedPreferences("Settings", 0);
        String savedLayout = "light";
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        if (pref.contains("layout")) { savedLayout = pref.getString("layout", "light");}
        if (savedLayout != null) {
            switch (savedLayout) {
                case "light":
                    builder = new AlertDialog.Builder(requireActivity());
                    break;
                case "dark":
                    builder = new AlertDialog.Builder(requireActivity(), R.style.darkDialogTheme);
                    break;
                default:
                    builder = new AlertDialog.Builder(requireActivity(), R.style.darkDialogTheme);
            }
        }
        // Get the layout inflater

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final Calendar cal = Calendar.getInstance();

        View dialog = inflater.inflate(R.layout.month_year_picker_dialog, null);
        final NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);

        int monthHelp = ShiftTable.month;

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(monthHelp);

        int yearHelp = ShiftTable.year;

        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(year);
        yearPicker.setValue(yearHelp);

        builder.setView(dialog)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), 0);
                    }
                })
                .setNeutralButton(R.string.today, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int todayYear = cal.get(Calendar.YEAR);
                        int todayMonth = cal.get(Calendar.MONTH) + 1;
                        listener.onDateSet(null, todayYear, todayMonth, 0);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MonthYearPickerDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}