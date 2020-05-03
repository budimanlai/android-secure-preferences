/*
 * Created by Budiman Lai (budiman.lai@gmail.com) on 4/25/20 10:35 PM
 *  Web: http://budimanlai.com, https://github.com/budimanlai
 *  Copyright (c) 2020 . All rights reserved.
 *  Last modified 4/25/20 9:26 PM
 */

package com.budimanlai.securepreferences;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.budimanlai.securepreferences", appContext.getPackageName());
    }
}
