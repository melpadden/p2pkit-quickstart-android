/**
 * Logger.java
 * Kanka-quickstart-android
 * <p/>
 * Created by uepaa on 15/02/16.
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
package ch.uepaa.quickstart.utils;

import android.text.format.DateFormat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Logger with message buffer.
 * Created by uepaa on 15/02/16.
 */
public class Logger {

    public interface LogHandler {
        void handleLogMessage(final String message);
    }

    private static final StringBuffer messages;
    private static final ArrayList<LogHandler> observers;

    static {
        messages = new StringBuffer();
        observers = new ArrayList<>();
    }

    public static void i(final String tag, final String msg) {
        Log.i(tag, msg);

        log("i", tag, msg);
    }

    public static void v(final String tag, final String msg) {
        Log.v(tag, msg);

        log("v", tag, msg);
    }

    public static void w(final String tag, final String msg) {
        Log.w(tag, msg);

        log("WARNING", tag, msg);
    }

    public static void e(final String tag, final String msg) {
        Log.e(tag, msg);

        log("ERROR", tag, msg);
    }

    public static synchronized String getLogs() {
        return messages.toString();
    }

    public static synchronized void clearLogs() {
        messages.setLength(0);
    }

    public static synchronized void addObserver(LogHandler handler) {
        if (!observers.contains(handler)) {
            observers.add(handler);
        }
    }

    public static synchronized void removeObserver(LogHandler handler) {
        if (observers.contains(handler)) {
            observers.remove(handler);
        }
    }

    private static synchronized void log(final String level, final String tag, final String msg) {
        CharSequence currentTime = DateFormat.format("hh:mm:ss", System.currentTimeMillis());
        String message = currentTime + (level.length() > 1 ? (" - " + level) : "") + "\n" + tag + ": " + msg + "\n---";

        messages.insert(0, message + "\n");
        notifyObservers(message);
    }

    private static synchronized void notifyObservers(final String message) {
        for (LogHandler handler : observers) {
            handler.handleLogMessage(message);
        }
    }
}
