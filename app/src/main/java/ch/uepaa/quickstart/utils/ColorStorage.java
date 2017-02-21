/**
 * ColorStorage.java
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
package ch.uepaa.quickstart.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Base64;

import java.util.Random;

/**
 * Color storage.
 * Created by uepaa on 18/02/16.
 */
public class ColorStorage {

    private final String COLOR_KEY = "COLOR_KEY";

    private final SharedPreferences prefs;

    public ColorStorage(final Context context) {
        this.prefs = context.getSharedPreferences("color_storage", Context.MODE_PRIVATE);
    }

    public synchronized void saveColor(int colorCode) {
        byte[] data = getColorBytes(colorCode);
        String encoded = Base64.encodeToString(data, Base64.DEFAULT);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(COLOR_KEY, encoded);
        editor.apply();
    }

    public synchronized byte[] loadColor() {
        byte[] data = null;

        if (prefs.contains(COLOR_KEY)) {
            String encoded = prefs.getString(COLOR_KEY, "");
            if (encoded.length() > 0) {
                data = Base64.decode(encoded, Base64.DEFAULT);
            }
        }

        if (data != null && data.length == 3) {
            return data;
        }

        return null;
    }

    public static byte[] getColorBytes(int color) {
        return new byte[]{(byte) Color.red(color), (byte) Color.green(color), (byte) Color.blue(color)};
    }

    public static int getOrCreateColorCode(byte[] colorData, int defaultColor) {
        if (colorData == null || colorData.length != 3) {
            return defaultColor;
        }

        return Color.argb(255, colorData[0] & 0xFF, colorData[1] & 0xFF, colorData[2] & 0xFF);
    }

    public static int createRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
}
