/**
 * ConsoleFragment.java
 * Kanka-quickstart-android
 * <p/>
 * Created by uepaa on 09/02/16.
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
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import ch.uepaa.p2pkit.P2PKit;
import ch.uepaa.p2pkit.discovery.DiscoveryListener;
import ch.uepaa.quickstart.R;
import ch.uepaa.quickstart.utils.Logger;

/**
 * Console fragment.
 * Created by uepaa on 09/02/16.
 */
public class ConsoleFragment extends DialogFragment implements Logger.LogHandler {

    public interface ConsoleListener {
        void startDiscovery();
        void stopDiscovery();
    }

    public static final String FRAGMENT_TAG = "console_fragment";

    public static ConsoleFragment newInstance() {
        return new ConsoleFragment();
    }

    private TextView mLogView;
    private ConsoleListener listener;
    private View mainView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.console_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {

        mainView = view;
        getDialog().setTitle(R.string.console);

        configureSwitches();

        mLogView = (TextView) view.findViewById(R.id.logTextView);
        TextView clearLogs = (TextView) view.findViewById(R.id.clearTextView);
        clearLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLogs();
            }
        });
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        try {
            listener = (ConsoleListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(activity.toString() + " must implement ConsoleListener", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        String logs = Logger.getLogs();
        mLogView.setText(logs);
        Logger.addObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.removeObserver(this);
        mLogView.setText("");
    }

    public void handleLogMessage(String message) {
        String updated = message + "\n" + mLogView.getText();
        mLogView.setText(updated);
    }

    private void clearLogs() {
        Logger.clearLogs();
        mLogView.setText("");
    }

    private void configureSwitches() {

        final Switch discoverySwitch = (Switch) mainView.findViewById(R.id.p2pSwitch);

        if (!getKitEnabled()) {
            discoverySwitch.setChecked(false);
            discoverySwitch.setEnabled(false);
            return;
        }

        discoverySwitch.setChecked(getDiscoveryEnabled());

        discoverySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    listener.startDiscovery();
                } else {
                    listener.stopDiscovery();
                }
            }
        });
    }

    private boolean getKitEnabled() {
        return P2PKit.isEnabled();
    }
    private boolean getDiscoveryEnabled() {

        if (getKitEnabled()) {
            return !(P2PKit.getDiscoveryState() == DiscoveryListener.STATE_OFF);
        }else{
            return false;
        }
    }

}
