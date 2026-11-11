/*
 * Copyright 2025 Vivek Umrao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package umrao.spanly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Instrumented tests for TimeSpanSelector library.
 * Provides full coverage of the public API and ensures correct behavior in a real Android environment.
 */
@RunWith(AndroidJUnit4.class)
public class ISpanlyUITest {

    private Context context;
    private float density;
    private float scaledDensity;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        density = context.getResources().getDisplayMetrics().density;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    }

    private float dp(float val) {
        return val * density;
    }

    private float sp(float val) {
        return val * scaledDensity;
    }

    @Test
    public void testLiveTestActivityLaunch() {
        try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
            // Brief pause so the live UI is visible on screen during test runs
            android.os.SystemClock.sleep(1500);

            scenario.onActivity(activity -> {
                assertNotNull("TestActivity should be launched", activity);
                assertNotNull("SpanlyDial should be bound", activity.getCircularSelector());
                assertNotNull("SpanlySlider should be bound", activity.getLinearSelector());
                assertNotNull("Callback TextView should be bound", activity.getCallbackText());

                // Trigger a span change on the circular selector
                activity.getCircularSelector().setSpanInMinutes(360, 720);
                assertTrue("Callback text should reflect updated span",
                        activity.getCallbackText().getText().toString().contains("360 to 720"));
            });

            // Capture screenshot during test execution and save to app storage
            captureActivityScreenshot(scenario, "live_ui_test_screenshot");

            // Additional pause to keep UI visible before activity closes
            android.os.SystemClock.sleep(1500);
        }
    }

    private void captureActivityScreenshot(ActivityScenario<TestActivity> scenario, String name) {
        scenario.onActivity(activity -> {
            try {
                android.view.View rootView = activity.getWindow().getDecorView().getRootView();
                int width = rootView.getWidth();
                int height = rootView.getHeight();
                if (width <= 0 || height <= 0) {
                    return;
                }
                android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                rootView.draw(canvas);

                java.io.File dir = activity.getExternalFilesDir("screenshots");
                if (dir != null) {
                    if (!dir.exists() && !dir.mkdirs()) {
                        android.util.Log.w("SpanlyUITest", "Could not create screenshot directory");
                    }
                    java.io.File file = new java.io.File(dir, name + ".png");
                    try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
                        android.util.Log.d("SpanlyUITest", "Saved UI screenshot to: " + file.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("SpanlyUITest", "Failed to capture screenshot", e);
            }
        });
    }

    @Test
    public void testLinearSelectorInflation() {
        SpanlySlider selector = new SpanlySlider(context);
        assertNotNull("SpanlySlider should be inflatable", selector);

        // Default values from TimeSpanSelector.java
        assertEquals("Default start time", 540, selector.getSpanStartInMinutes());
        assertEquals("Default end time", 1020, selector.getSpanEndInMinutes());
    }

    @Test
    public void testCircularSelectorInflation() {
        SpanlyDial selector = new SpanlyDial(context);
        assertNotNull("SpanlyDial should be inflatable", selector);

        assertEquals("Default start time", 540, selector.getSpanStartInMinutes());
        assertEquals("Default end time", 1020, selector.getSpanEndInMinutes());
    }

    @Test
    public void testProgrammaticSetters() {
        SpanlyDial selector = new SpanlyDial(context);
        selector.setThumbMinuteStep(1);

        selector.setSpanInMinutes(180, 300); // 03:00 to 05:00

        assertEquals(180, selector.getSpanStartInMinutes());
        assertEquals(300, selector.getSpanEndInMinutes());
        assertEquals(120, selector.getDurationInMinutes());
        assertFalse(selector.isOvernight());
    }

    @Test
    public void testStringAPI() {
        SpanlySlider selector = new SpanlySlider(context);

        selector.setSpanStartTime("10:00");
        selector.setSpanEndTime("14:30");

        assertEquals(600, selector.getSpanStartInMinutes());
        assertEquals(870, selector.getSpanEndInMinutes());

        selector.setSpanTime("08:00", "18:00");
        assertEquals(480, selector.getSpanStartInMinutes());
        assertEquals(1080, selector.getSpanEndInMinutes());
    }

    @Test
    public void testDurationConstraints() {
        SpanlyDial selector = new SpanlyDial(context);
        selector.setThumbMinuteStep(1);

        selector.setMinDurationMinutes(60);
        selector.setMaxDurationMinutes(120);

        assertEquals(60, selector.getMinDurationMinutes());
        assertEquals(120, selector.getMaxDurationMinutes());

        // Setting span less than min should snap to min
        selector.setSpanInMinutes(100, 110);
        assertEquals(50, selector.getSpanStartInMinutes());
        assertEquals(110, selector.getSpanEndInMinutes());
    }

    @Test
    public void testStateRestoration() {
        SpanlySlider selector = new SpanlySlider(context);
        selector.setThumbMinuteStep(1);
        selector.setSpanInMinutes(60, 180);

        Parcelable state = selector.onSaveInstanceState();
        assertNotNull("State should not be null", state);

        SpanlySlider newSelector = new SpanlySlider(context);
        newSelector.onRestoreInstanceState(state);

        assertEquals(60, newSelector.getSpanStartInMinutes());
        assertEquals(180, newSelector.getSpanEndInMinutes());
    }

    @Test
    public void testVisualAttributes() {
        SpanlyDial selector = new SpanlyDial(context);

        selector.setAccentColor(Color.RED);
        assertEquals(Color.RED, selector.getAccentColor());
        assertEquals(Color.RED, selector.getSpanColor());

        selector.setTrackColor(Color.GRAY);
        assertEquals(Color.GRAY, selector.getTrackColor());

        selector.setTrackWidth(20f);
        assertEquals(dp(20f), selector.getTrackWidth(), 0.01f);

        selector.setThumbRadius(15f);
        assertEquals(dp(15f), selector.getThumbRadius(), 0.01f);

        selector.setSpanTextColor(Color.BLUE);
        assertEquals(Color.BLUE, selector.getSpanTextColor());

        selector.setTextColor(Color.YELLOW);
        assertEquals(Color.YELLOW, selector.getSpanTextColor());
        assertEquals(Color.YELLOW, selector.getTickLabelColor());
    }

    @Test
    public void testThumbShadowAttributes() {
        SpanlySlider selector = new SpanlySlider(context);
        selector.setThumbShadowColor(Color.BLACK);
        assertEquals(Color.BLACK, selector.getThumbShadowColor());

        selector.setThumbShadowDx(5f);
        assertEquals(dp(5f), selector.getThumbShadowDx(), 0.01f);

        selector.setThumbShadowDy(5f);
        assertEquals(dp(5f), selector.getThumbShadowDy(), 0.01f);

        selector.setThumbElevation(10f);
        assertEquals(dp(10f), selector.getThumbElevation(), 0.01f);

        selector.setThumbShadow(2f, 2f, 8f, Color.DKGRAY);
        assertEquals(Color.DKGRAY, selector.getThumbShadowColor());
    }

    @Test
    public void testThumbDrawableAttributes() {
        SpanlyDial selector = new SpanlyDial(context);
        Drawable d1 = new ColorDrawable(Color.RED);
        Drawable d2 = new ColorDrawable(Color.BLUE);

        selector.setStartThumbDrawable(d1);
        assertTrue(selector.isStartThumbDrawableSet());
        assertEquals(d1, selector.getStartThumbDrawable());

        selector.setEndThumbDrawable(d2);
        assertTrue(selector.isEndThumbDrawableSet());
        assertEquals(d2, selector.getEndThumbDrawable());

        selector.setStartThumbDrawableTintColor(Color.GREEN);
        assertEquals(Color.GREEN, selector.getStartThumbDrawableTintColor());

        selector.setEndThumbDrawableTintColor(Color.CYAN);
        assertEquals(Color.CYAN, selector.getEndThumbDrawableTintColor());

        selector.setThumbDrawables(d2, d1);
        assertEquals(d2, selector.getStartThumbDrawable());
        assertEquals(d1, selector.getEndThumbDrawable());
    }

    @Test
    public void testThumbHandleAttributes() {
        SpanlySlider selector = new SpanlySlider(context);
        selector.setThumbFillColor(Color.MAGENTA);
        assertEquals(Color.MAGENTA, selector.getThumbFillColor());

        selector.setThumbStrokeWidth(2f);
        assertEquals(dp(2f), selector.getThumbStrokeWidth(), 0.01f);

        selector.setThumbStrokeColor(Color.WHITE);
        assertEquals(Color.WHITE, selector.getThumbStrokeColor());

        selector.setThumbTouchRadiusPadding(10f);
        assertEquals(dp(10f), selector.getThumbTouchRadiusPadding(), 0.01f);
    }

    @Test
    public void testTickAttributes() {
        SpanlyDial selector = new SpanlyDial(context);

        selector.setShowTicks(true);
        assertTrue(selector.isShowTicks());

        selector.setShowTickLabels(false);
        assertFalse(selector.isShowTickLabels());

        selector.setHourTickColor(Color.GREEN);
        assertEquals(Color.GREEN, selector.getHourTickColor());

        selector.setMinuteTickColor(Color.YELLOW);
        assertEquals(Color.YELLOW, selector.getMinuteTickColor());

        selector.setTickLabelSize(12f);
        assertEquals(sp(12f), selector.getTickLabelSize(), 0.01f);

        selector.setTickLabelStyle(Typeface.BOLD);
        assertEquals(Typeface.BOLD, selector.getTickLabelStyle());

        selector.setTickDistanceFromTrack(5f);
        assertEquals(dp(5f), selector.getTickDistanceFromTrack(), 0.01f);

        selector.setTickLabelDistanceFromTick(10f);
        assertEquals(dp(10f), selector.getTickLabelDistanceFromTick(), 0.01f);

        selector.setTickEdgeStyle(ISpanly.TickEdgeStyle.ROUND);
        assertEquals(ISpanly.TickEdgeStyle.ROUND, selector.getTickEdgeStyle());

        selector.setTickColors(Color.RED, Color.BLUE);
        assertEquals(Color.RED, selector.getHourTickColor());
        assertEquals(Color.BLUE, selector.getMinuteTickColor());
    }

    @Test
    public void testTickSizeAttributes() {
        SpanlySlider selector = new SpanlySlider(context);
        selector.setHourTickWidth(4f);
        assertEquals(dp(4f), selector.getHourTickWidth(), 0.01f);

        selector.setMinuteTickWidth(2f);
        assertEquals(dp(2f), selector.getMinuteTickWidth(), 0.01f);

        selector.setHourTickHeight(20f);
        assertEquals(dp(20f), selector.getHourTickHeight(), 0.01f);

        selector.setMinuteTickHeight(10f);
        assertEquals(dp(10f), selector.getMinuteTickHeight(), 0.01f);

        selector.setTicksWidth(6f, 3f);
        assertEquals(dp(6f), selector.getHourTickWidth(), 0.01f);
        assertEquals(dp(3f), selector.getMinuteTickWidth(), 0.01f);

        selector.setTicksHeight(30f, 15f);
        assertEquals(dp(30f), selector.getHourTickHeight(), 0.01f);
        assertEquals(dp(15f), selector.getMinuteTickHeight(), 0.01f);
    }

    @Test
    public void testFormatAndVisibility() {
        SpanlySlider selector = new SpanlySlider(context);

        selector.set24HourFormat(false);
        assertFalse(selector.is24HourFormat());

        selector.showSpanText(false);
        assertFalse(selector.isSpanTextShown());

        selector.showAmPmLabels(true);
        assertTrue(selector.isAmPmLabelsShown());

        selector.setSpanTextFormat("From %1$s To %2$s");
        assertEquals("From %1$s To %2$s", selector.getSpanTextFormat());

        selector.setSpanTextPosition(ISpanly.SpanTextPosition.TOP);
        assertEquals(ISpanly.SpanTextPosition.TOP, selector.getSpanTextPosition());

        selector.setSpanTextSize(18f);
        assertEquals(sp(18f), selector.getSpanTextSize(), 0.01f);

        selector.setSpanTextStyle(Typeface.ITALIC);
        assertEquals(Typeface.ITALIC, selector.getSpanTextStyle());
    }

    @Test
    public void testOvernightSpanLogic() {
        SpanlyDial selector = new SpanlyDial(context);
        selector.setOvernightSpanAllowed(true);
        assertTrue(selector.isOvernightSpanAllowed());

        // 10:00 PM (1320) to 02:00 AM (120)
        selector.setSpanInMinutes(1320, 120);
        assertTrue(selector.isOvernight());
        assertEquals(1320, selector.getSpanStartInMinutes());
        assertEquals(120, selector.getSpanEndInMinutes());
        assertEquals(240, selector.getDurationInMinutes());
    }

    @Test
    public void testListeners() {
        SpanlySlider selector = new SpanlySlider(context);
        selector.setThumbMinuteStep(1);

        AtomicInteger startValue = new AtomicInteger(-1);
        AtomicInteger endValue = new AtomicInteger(-1);

        selector.setOnSpanChangeListener(new ISpanly.OnSpanChangeListener() {
            @Override
            public void onSpanChanged(int startMinutes, int endMinutes, boolean isOvernight) {
                startValue.set(startMinutes);
                endValue.set(endMinutes);
            }

            @Override
            public void onInteractionFinished(int startMinutes, int endMinutes, boolean isOvernight) {
            }
        });

        selector.setSpanInMinutes(100, 200);
        assertEquals(100, startValue.get());
        assertEquals(200, endValue.get());

        assertNotNull(selector.getOnSpanChangeListener());

        selector.setOnTimeChangeListener(new ISpanly.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(int startTimeMinutes) {
            }

            @Override
            public void onEndTimeChange(int endTimeMinutes) {
            }

            @Override
            public void onDurationChange(int startTimeMinutes, int endTimeMinutes) {
            }
        });
        assertNotNull(selector.getOnTimeChangeListener());

        selector.setOnDragChangeListener(new ISpanly.OnDragChangeListener() {
            @Override
            public boolean onDragStart(@NonNull ISpanly.Thumb thumb) {
                return true;
            }

            @Override
            public void onDragStop(@NonNull ISpanly.Thumb thumb) {
            }
        });
        assertNotNull(selector.getOnDragChangeListener());
    }

    @Test
    public void testBoundaryValues() {
        SpanlyDial selector = new SpanlyDial(context);
        selector.setThumbMinuteStep(1);

        selector.setSpanInMinutes(0, 1439);
        assertEquals(0, selector.getSpanStartInMinutes());
        assertEquals(1439, selector.getSpanEndInMinutes());
    }
}
