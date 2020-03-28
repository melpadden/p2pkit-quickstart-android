/**
 * DroidUtils.java
 * Kanka-quickstart-android
 * <p/>
 * Created by uepaa on 10/02/16.
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
package com.quovirus.proximityalarm.utils;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Various Android utils.
 * Created by uepaa on 10/02/16.
 */
public class DroidUtils {

    public static float getFloatConstant(final Resources res, final int constant) {
        TypedValue outValue = new TypedValue();
        res.getValue(constant, outValue, true);
        return outValue.getFloat();
    }
}
