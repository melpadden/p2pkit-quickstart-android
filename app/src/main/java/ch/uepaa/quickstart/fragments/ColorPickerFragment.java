/**
 * ColorPickerFragment.java
 * Kanka-quickstart-android
 * <p/>
 * Created by uepaa on 18/02/16.
 * <p/>
 * <p/>
 * Copyright (c) 2016 by Uepaa AG, Zürich, Switzerland.
 * All rights reserved.
 * <p/>
 * We reserve all rights in this document and in the information contained therein.
 * Reproduction, use, transmission, dissemination or disclosure of this document and/or
 * the information contained herein to third parties in part or in whole by any means
 * is strictly prohibited, unless prior written permission is obtained from Uepaa AG.
 */
package ch.uepaa.quickstart.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;

import ch.uepaa.quickstart.BuildConfig;
import ch.uepaa.quickstart.R;

/**
 * Color picker.
 * Created by uepaa on 18/02/16.
 */
public class ColorPickerFragment extends DialogFragment {

    public interface ColorPickerListener {
        void onColorPicked(int colorCode);
    }

    public static final String FRAGMENT_TAG = "color_fragment";

    private static final String COLOR_KEY = "color";

    private ColorPickerListener mListener;
    private ColorPicker mPicker;

    public static ColorPickerFragment newInstance(int oldColor) {
        ColorPickerFragment dialog = new ColorPickerFragment();

        Bundle args = new Bundle();
        args.putInt(COLOR_KEY, oldColor);
        dialog.setArguments(args);

        return dialog;
    }

    public ColorPickerFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (ColorPickerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ColorPickerListener");
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (BuildConfig.BUILD_CONFIGURATION.equals("prod")) {
            hideSystemBars();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.color_picker_fragment, null);

        int oldColor = getArguments().getInt(COLOR_KEY);

        mPicker = (ColorPicker) view.findViewById(R.id.picker);
        mPicker.setShowOldCenterColor(false);
        mPicker.setColor(oldColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.P2PKitAlertDialog));
        builder.setView(view);
        builder.setTitle(R.string.choose_color);
        builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onColorPicked(mPicker.getColor());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    private void hideSystemBars() {

        if (this.getActivity() == null) {
            return;
        }

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        this.getActivity().getWindow().getDecorView().setSystemUiVisibility(flags);
    }
}
