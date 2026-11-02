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

package vivek.umrao.time.span.selector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class TimeSpanSelectorTest {

    private CircularTimeSpanSelector circularSelector;
    private LinearTimeSpanSelector linearSelector;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        circularSelector = new CircularTimeSpanSelector(context);
        linearSelector = new LinearTimeSpanSelector(context);
    }

    @Test
    public void testDefaultValues() {
        assertEquals(TimeSpanSelector.DEFAULT_START_MINUTES, circularSelector.getSpanStartInMinutes());
        assertEquals(TimeSpanSelector.DEFAULT_END_MINUTES, circularSelector.getSpanEndInMinutes());
        assertTrue(circularSelector.is24HourFormat());
        assertTrue(circularSelector.isShowTicks());
        assertTrue(circularSelector.isShowTickLabels());
        assertTrue(circularSelector.isSpanTextShown());
        assertTrue(circularSelector.isOvernightSpanAllowed());
        assertEquals(TimeSpanSelector.DEFAULT_THUMB_MINUTE_STEP, circularSelector.getThumbMinuteStep());
    }

    @Test
    public void testSetSpanInMinutes() {
        circularSelector.setSpanInMinutes(600, 1200);
        assertEquals(600, circularSelector.getSpanStartInMinutes());
        assertEquals(1200, circularSelector.getSpanEndInMinutes());
        assertFalse(circularSelector.isOvernight());
    }

    @Test
    public void testOvernightSpan() {
        circularSelector.setSpanInMinutes(1320, 120); // 10 PM to 2 AM
        assertTrue(circularSelector.isOvernight());
        assertEquals(240, circularSelector.getDurationInMinutes());
    }

    @Test
    public void testSetSpanStartTime() {
        circularSelector.setSpanStartTime("08:30");
        assertEquals(8 * 60 + 30, circularSelector.getSpanStartInMinutes());
    }

    @Test
    public void testSetSpanEndTime() {
        circularSelector.setSpanEndTime("18:45");
        assertEquals(18 * 60 + 45, circularSelector.getSpanEndInMinutes());
    }

    @Test
    public void testSetSpanTime() {
        circularSelector.setSpanTime("10:00", "20:00");
        assertEquals(10 * 60, circularSelector.getSpanStartInMinutes());
        assertEquals(20 * 60, circularSelector.getSpanEndInMinutes());
    }

    @Test
    public void testOnSpanChangeListener() {
        TimeSpanSelector.OnSpanChangeListener listener = mock(TimeSpanSelector.OnSpanChangeListener.class);
        circularSelector.setOnSpanChangeListener(listener);
        assertEquals(listener, circularSelector.getOnSpanChangeListener());

        circularSelector.setSpanInMinutes(500, 600);
        verify(listener, atLeastOnce()).onSpanChanged(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    public void testOnTimeChangeListener() {
        TimeSpanSelector.OnTimeChangeListener listener = mock(TimeSpanSelector.OnTimeChangeListener.class);
        circularSelector.setThumbMinuteStep(1); // Disable snapping for this test
        circularSelector.setOnTimeChangeListener(listener);
        assertEquals(listener, circularSelector.getOnTimeChangeListener());

        // Default: Start=540, End=1020, Duration=480
        // Setting both separately to control the order and calls
        circularSelector.setSpanStartInMinutes(100);
        verify(listener, atLeastOnce()).onStartTimeChange(100);
        
        circularSelector.setSpanEndInMinutes(200);
        verify(listener, atLeastOnce()).onEndTimeChange(200);
        
        // Duration was 480, now it's 100
        verify(listener, atLeastOnce()).onDurationChange(100, 200);
    }

    @Test
    public void testOnDragChangeListener() {
        TimeSpanSelector.OnDragChangeListener listener = mock(TimeSpanSelector.OnDragChangeListener.class);
        circularSelector.setOnDragChangeListener(listener);
        assertEquals(listener, circularSelector.getOnDragChangeListener());
    }

    @Test
    public void testOnDragChangeEvents() {
        TimeSpanSelector.OnDragChangeListener listener = mock(TimeSpanSelector.OnDragChangeListener.class);
        org.mockito.Mockito.when(listener.onDragStart(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        circularSelector.setOnDragChangeListener(listener);

        // Layout the view to calculate dimensions
        circularSelector.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                             View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
        circularSelector.layout(0, 0, 1000, 1000);

        // Set start thumb to 0 minutes (12 o'clock)
        circularSelector.setThumbMinuteStep(1);
        circularSelector.setSpanStartInMinutes(0);

        // 12 o'clock in the selector corresponds to angle 0, which is (cx, cy - viewRadius)
        // From CircularTimeSpanSelector.onSizeChanged: cx=500, cy=500
        // viewRadius is approx 500 - 21 = 479
        float touchX = 500f;
        float touchY = 21f;

        // Simulate touch down
        android.view.MotionEvent downEvent = android.view.MotionEvent.obtain(0, 0, android.view.MotionEvent.ACTION_DOWN, touchX, touchY, 0);
        circularSelector.onTouchEvent(downEvent);
        verify(listener).onDragStart(TimeSpanSelector.Thumb.START);

        // Simulate touch up
        android.view.MotionEvent upEvent = android.view.MotionEvent.obtain(0, 0, android.view.MotionEvent.ACTION_UP, touchX, touchY, 0);
        circularSelector.onTouchEvent(upEvent);
        verify(listener).onDragStop(TimeSpanSelector.Thumb.START);
        
        downEvent.recycle();
        upEvent.recycle();
    }

    @Test
    public void testMinMaxDuration() {
        circularSelector.setMinDurationMinutes(60);
        assertEquals(60, circularSelector.getMinDurationMinutes());

        circularSelector.setMaxDurationMinutes(120);
        assertEquals(120, circularSelector.getMaxDurationMinutes());
    }

    @Test
    public void testOvernightAllowed() {
        circularSelector.setOvernightSpanAllowed(false);
        assertFalse(circularSelector.isOvernightSpanAllowed());
    }

    @Test
    public void testThumbMinuteStep() {
        circularSelector.setThumbMinuteStep(30);
        assertEquals(30, circularSelector.getThumbMinuteStep());
    }

    @Test
    public void testColors() {
        circularSelector.setAccentColor(Color.RED);
        assertEquals(Color.RED, circularSelector.getAccentColor());

        circularSelector.setTrackColor(Color.GRAY);
        assertEquals(Color.GRAY, circularSelector.getTrackColor());

        circularSelector.setSpanColor(Color.GREEN);
        assertEquals(Color.GREEN, circularSelector.getSpanColor());

        circularSelector.setSpanTextColor(Color.YELLOW);
        assertEquals(Color.YELLOW, circularSelector.getSpanTextColor());

        circularSelector.setThumbFillColor(Color.WHITE);
        assertEquals(Color.WHITE, circularSelector.getThumbFillColor());

        circularSelector.setThumbStrokeColor(Color.BLACK);
        assertEquals(Color.BLACK, circularSelector.getThumbStrokeColor());

        circularSelector.setThumbShadowColor(Color.DKGRAY);
        assertEquals(Color.DKGRAY, circularSelector.getThumbShadowColor());

        circularSelector.setHourTickColor(Color.RED);
        assertEquals(Color.RED, circularSelector.getHourTickColor());

        circularSelector.setMinuteTickColor(Color.BLUE);
        assertEquals(Color.BLUE, circularSelector.getMinuteTickColor());

        circularSelector.setTickLabelColor(Color.MAGENTA);
        assertEquals(Color.MAGENTA, circularSelector.getTickLabelColor());
    }

    @Test
    public void testTextSettings() {
        circularSelector.showSpanText(false);
        assertFalse(circularSelector.isSpanTextShown());

        circularSelector.setSpanTextSize(20f);
        assertEquals(20f, circularSelector.getSpanTextSize(), 0.1f);

        circularSelector.setSpanTextStyle(Typeface.BOLD);
        assertEquals(Typeface.BOLD, circularSelector.getSpanTextStyle());

        circularSelector.setSpanTextFormat("%s to %s");
        assertEquals("%s to %s", circularSelector.getSpanTextFormat());

        circularSelector.setSpanTextPosition(TimeSpanSelector.SpanTextPosition.CENTER);
        assertEquals(TimeSpanSelector.SpanTextPosition.CENTER, circularSelector.getSpanTextPosition());
    }

    @Test
    public void testThumbSettings() {
        circularSelector.setThumbRadius(15f);
        assertEquals(15f, circularSelector.getThumbRadius(), 0.1f);

        circularSelector.setThumbTouchRadiusPadding(10f);
        assertEquals(10f, circularSelector.getThumbTouchRadiusPadding(), 0.1f);

        circularSelector.setThumbStrokeWidth(2f);
        assertEquals(2f, circularSelector.getThumbStrokeWidth(), 0.1f);

        circularSelector.setThumbElevation(5f);
        assertEquals(5f, circularSelector.getThumbElevation(), 0.1f);

        circularSelector.setThumbShadowDx(1f);
        assertEquals(1f, circularSelector.getThumbShadowDx(), 0.1f);

        circularSelector.setThumbShadowDy(1f);
        assertEquals(1f, circularSelector.getThumbShadowDy(), 0.1f);
    }

    @Test
    public void testTickSettings() {
        circularSelector.setShowTicks(false);
        assertFalse(circularSelector.isShowTicks());

        circularSelector.setShowTickLabels(false);
        assertFalse(circularSelector.isShowTickLabels());

        circularSelector.setTickDistanceFromTrack(10f);
        assertEquals(10f, circularSelector.getTickDistanceFromTrack(), 0.1f);

        circularSelector.setTickLabelDistanceFromTick(5f);
        assertEquals(5f, circularSelector.getTickLabelDistanceFromTick(), 0.1f);

        circularSelector.setTickLabelSize(12f);
        assertEquals(12f, circularSelector.getTickLabelSize(), 0.1f);

        circularSelector.setTickLabelStyle(Typeface.ITALIC);
        assertEquals(Typeface.ITALIC, circularSelector.getTickLabelStyle());

        circularSelector.showAmPmLabels(true);
        assertTrue(circularSelector.isAmPmLabelsShown());

        circularSelector.setHourTickWidth(4f);
        assertEquals(4f, circularSelector.getHourTickWidth(), 0.1f);

        circularSelector.setMinuteTickWidth(2f);
        assertEquals(2f, circularSelector.getMinuteTickWidth(), 0.1f);

        circularSelector.setHourTickHeight(20f);
        assertEquals(20f, circularSelector.getHourTickHeight(), 0.1f);

        circularSelector.setMinuteTickHeight(10f);
        assertEquals(10f, circularSelector.getMinuteTickHeight(), 0.1f);

        circularSelector.setTickEdgeStyle(TimeSpanSelector.TickEdgeStyle.ROUND);
        assertEquals(TimeSpanSelector.TickEdgeStyle.ROUND, circularSelector.getTickEdgeStyle());
    }

    @Test
    public void testTrackSettings() {
        circularSelector.setTrackWidth(10f);
        assertEquals(10f, circularSelector.getTrackWidth(), 0.1f);
    }

    @Test
    public void test24HourFormat() {
        circularSelector.set24HourFormat(false);
        assertFalse(circularSelector.is24HourFormat());
    }

    @Test
    public void testThumbDrawables() {
        circularSelector.setStartThumbDrawable(android.R.drawable.ic_menu_add);
        assertTrue(circularSelector.isStartThumbDrawableSet());
        assertNotNull(circularSelector.getStartThumbDrawable());

        circularSelector.setEndThumbDrawable(android.R.drawable.ic_menu_delete);
        assertTrue(circularSelector.isEndThumbDrawableSet());
        assertNotNull(circularSelector.getEndThumbDrawable());

        circularSelector.setStartThumbDrawableTintColor(Color.RED);
        assertEquals(Color.RED, circularSelector.getStartThumbDrawableTintColor());

        circularSelector.setEndThumbDrawableTintColor(Color.BLUE);
        assertEquals(Color.BLUE, circularSelector.getEndThumbDrawableTintColor());
    }

    @Test
    public void testLinearSelectorApis() {
        // Since both inherit from BaseTimeSpanSelector, testing some specific ones on Linear
        linearSelector.setThumbMinuteStep(1);
        linearSelector.setSpanInMinutes(100, 200);
        assertEquals(100, linearSelector.getSpanStartInMinutes());
        assertEquals(200, linearSelector.getSpanEndInMinutes());

        linearSelector.setAccentColor(Color.CYAN);
        assertEquals(Color.CYAN, linearSelector.getAccentColor());
    }

    @Test
    public void testAttributes() {
        Context context = ApplicationProvider.getApplicationContext();
        // Inflate from layout file to test attribute parsing
        View layout = LayoutInflater.from(context).inflate(R.layout.test_attributes, null);
        CircularTimeSpanSelector selector = layout.findViewById(R.id.circular_selector_attrs);

        assertEquals(120, selector.getSpanStartInMinutes());
        assertEquals(240, selector.getSpanEndInMinutes());
        assertEquals(5, selector.getThumbMinuteStep());
        assertFalse(selector.is24HourFormat());
        assertFalse(selector.isOvernightSpanAllowed());
        assertEquals(30, selector.getMinDurationMinutes());
        assertEquals(600, selector.getMaxDurationMinutes());
        assertEquals(Color.RED, selector.getTrackColor());
        assertEquals(Color.GREEN, selector.getSpanColor());
        assertEquals(context.getResources().getDisplayMetrics().density * 10, selector.getTrackWidth(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 15, selector.getThumbRadius(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 12, selector.getThumbTouchRadiusPadding(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 2, selector.getThumbStrokeWidth(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 20, selector.getHourTickHeight(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 10, selector.getMinuteTickHeight(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 4, selector.getHourTickWidth(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 2, selector.getMinuteTickWidth(), 0.1f);
        assertEquals(Color.BLUE, selector.getHourTickColor());
        assertEquals(Color.YELLOW, selector.getMinuteTickColor());
        assertTrue(selector.isShowTicks());
        assertTrue(selector.isShowTickLabels());
        assertEquals(context.getResources().getDisplayMetrics().density * 8, selector.getTickDistanceFromTrack(), 0.1f);
        assertEquals(TimeSpanSelector.TickEdgeStyle.ROUND, selector.getTickEdgeStyle());
        assertTrue(selector.isAmPmLabelsShown());
        // Using 14sp -> density * 14
        assertEquals(context.getResources().getDisplayMetrics().scaledDensity * 14, selector.getTickLabelSize(), 0.1f);
        assertEquals(Color.MAGENTA, selector.getTickLabelColor());
        assertEquals(Typeface.BOLD, selector.getTickLabelStyle());
        assertEquals(context.getResources().getDisplayMetrics().density * 6, selector.getTickLabelDistanceFromTick(), 0.1f);
        assertEquals(Color.WHITE, selector.getThumbFillColor());
        assertEquals(Color.BLACK, selector.getThumbStrokeColor());
        assertEquals(context.getResources().getDisplayMetrics().density * 3, selector.getThumbShadowDx(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 3, selector.getThumbShadowDy(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 4, selector.getThumbElevation(), 0.1f);
        assertEquals(0xFF888888, selector.getThumbShadowColor());

        assertTrue(selector.isSpanTextShown());
        assertEquals(Color.CYAN, selector.getSpanTextColor());
        assertEquals(context.getResources().getDisplayMetrics().scaledDensity * 18, selector.getSpanTextSize(), 0.1f);
        assertEquals("Selected: %1$s to %2$s", selector.getSpanTextFormat());
        assertEquals(Typeface.ITALIC, selector.getSpanTextStyle());
        assertEquals(TimeSpanSelector.SpanTextPosition.TOP, selector.getSpanTextPosition());

        // Linear Selector
        LinearTimeSpanSelector linearSelector = layout.findViewById(R.id.linear_selector_attrs);
        assertEquals(8 * 60, linearSelector.getSpanStartInMinutes());
        assertEquals(20 * 60, linearSelector.getSpanEndInMinutes());
        assertEquals(0xFF333333, linearSelector.getSpanTextColor());
        assertEquals(0xFF333333, linearSelector.getTickLabelColor());
    }

    @Test
    public void testDurationClampingWithoutOvernight() {
        linearSelector.setThumbMinuteStep(1);
        linearSelector.setOvernightSpanAllowed(false);
        linearSelector.setMinDurationMinutes(60);

        // Attempt to set end before start - should clamp and enforce min duration without wrapping across midnight
        linearSelector.setSpanInMinutes(600, 500);
        assertTrue("End must be >= start", linearSelector.getSpanEndInMinutes() >= linearSelector.getSpanStartInMinutes());
        assertFalse("Must not be overnight", linearSelector.isOvernight());
        assertTrue("Duration must satisfy minDuration", linearSelector.getDurationInMinutes() >= 60);
    }

    @Test
    public void testStateSaveAndRestore() {
        Context context = ApplicationProvider.getApplicationContext();
        CircularTimeSpanSelector original = new CircularTimeSpanSelector(context);
        original.setSpanInMinutes(480, 960);

        android.os.Parcelable savedState = original.onSaveInstanceState();
        assertNotNull(savedState);

        CircularTimeSpanSelector restored = new CircularTimeSpanSelector(context);
        restored.onRestoreInstanceState(savedState);

        assertEquals(480, restored.getSpanStartInMinutes());
        assertEquals(960, restored.getSpanEndInMinutes());
    }
}
