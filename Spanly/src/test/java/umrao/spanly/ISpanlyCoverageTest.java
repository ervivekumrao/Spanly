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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAccessibilityManager;

/**
 * High-coverage unit tests for Spanly ensuring full branch and line coverage.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class ISpanlyCoverageTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testTimeUtilsConstructor() {
        TimeUtils utils = new TimeUtils();
        assertNotNull(utils);
    }

    @Test
    public void testEnums() {
        for (ISpanly.SpanTextPosition pos : ISpanly.SpanTextPosition.values()) {
            assertEquals(pos, ISpanly.SpanTextPosition.valueOf(pos.name()));
        }
        for (ISpanly.Thumb thumb : ISpanly.Thumb.values()) {
            assertEquals(thumb, ISpanly.Thumb.valueOf(thumb.name()));
        }
        for (ISpanly.TickEdgeStyle style : ISpanly.TickEdgeStyle.values()) {
            assertEquals(style, ISpanly.TickEdgeStyle.valueOf(style.name()));
        }
    }

    @Test
    public void testBaseConvenienceMethods() {
        SpanlyDial selector = new SpanlyDial(context);

        selector.setTextColor(Color.CYAN);
        assertEquals(Color.CYAN, selector.getSpanTextColor());
        assertEquals(Color.CYAN, selector.getTickLabelColor());

        selector.setThumbShadow(1f, 2f, 6f, Color.DKGRAY);
        assertEquals(6f, selector.getThumbElevation(), 0.1f);
        assertEquals(1f, selector.getThumbShadowDx(), 0.1f);
        assertEquals(2f, selector.getThumbShadowDy(), 0.1f);
        assertEquals(Color.DKGRAY, selector.getThumbShadowColor());

        selector.setThumbDrawables(android.R.drawable.ic_media_play, android.R.drawable.ic_media_pause);
        assertTrue(selector.isStartThumbDrawableSet());
        assertTrue(selector.isEndThumbDrawableSet());

        selector.setThumbDrawables(new ColorDrawable(Color.RED), new ColorDrawable(Color.GREEN));
        assertTrue(selector.isStartThumbDrawableSet());
        assertTrue(selector.isEndThumbDrawableSet());

        selector.setTickColors(Color.BLUE, Color.MAGENTA);
        assertEquals(Color.BLUE, selector.getHourTickColor());
        assertEquals(Color.MAGENTA, selector.getMinuteTickColor());

        selector.setTicksWidth(3f, 1.5f);
        assertEquals(3f, selector.getHourTickWidth(), 0.1f);
        assertEquals(1.5f, selector.getMinuteTickWidth(), 0.1f);

        selector.setTicksHeight(12f, 6f);
        assertEquals(12f, selector.getHourTickHeight(), 0.1f);
        assertEquals(6f, selector.getMinuteTickHeight(), 0.1f);
    }

    @Test
    public void testBaseTimeSpanSelectorConstructors() {
        BaseSpanly b1 = new BaseSpanly(context) {};
        BaseSpanly b2 = new BaseSpanly(context, null) {};
        assertNotNull(b1);
        assertNotNull(b2);
    }

    @Test
    public void testZeroSizeHandling() {
        SpanlyDial circular = new SpanlyDial(context);
        circular.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY),
                         View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY));
        circular.layout(0, 0, 0, 0);

        SpanlySlider linear = new SpanlySlider(context);
        linear.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY),
                       View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY));
        linear.layout(0, 0, 0, 0);
    }

    @Test
    public void testClampingEdgeCases() {
        SpanlyDial selector = new SpanlyDial(context);
        selector.setThumbMinuteStep(1);
        selector.setOvernightSpanAllowed(false);
        selector.setMinDurationMinutes(120);

        // Clamping start thumb to 0
        selector.setSpanInMinutes(50, 100);
        // Clamping end thumb to MINUTES_IN_DAY
        selector.setSpanInMinutes(1400, 1420);
    }

    @Test
    public void testBaseUpdateSpanBranches() {
        SpanlyDial selector = new SpanlyDial(context);
        selector.setThumbMinuteStep(1);

        // Overnight allowed: duration exceeds max
        selector.setOvernightSpanAllowed(true);
        selector.setMaxDurationMinutes(300); // 5 hours
        selector.setMinDurationMinutes(60);  // 1 hour

        // Move start thumb with excessive duration
        selector.setSpanInMinutes(100, 600); // requested 500 mins -> clamped to 300
        assertEquals(300, selector.getDurationInMinutes());

        // Move end thumb with duration below min
        selector.setSpanStartInMinutes(500);
        selector.setSpanEndInMinutes(520); // requested 20 mins -> clamped to 60
        assertEquals(60, selector.getDurationInMinutes());

        // Overnight disabled: moving start and end with duration clamp
        selector.setOvernightSpanAllowed(false);
        selector.setMinDurationMinutes(60);
        selector.setMaxDurationMinutes(200);

        selector.setSpanInMinutes(1000, 1300); // duration 300 -> clamped to 200
        assertEquals(200, selector.getDurationInMinutes());

        selector.setSpanStartInMinutes(1000);
        selector.setSpanEndInMinutes(1010); // duration 10 -> clamped to 60
        assertEquals(60, selector.getDurationInMinutes());

        // Disabling overnight while span is overnight resets end time
        selector.setMaxDurationMinutes(ISpanly.MINUTES_IN_DAY);
        selector.setMinDurationMinutes(0);
        selector.setOvernightSpanAllowed(true);
        selector.setSpanInMinutes(1200, 200);
        assertTrue(selector.isOvernight());
        selector.setOvernightSpanAllowed(false);
        assertFalse(selector.isOvernight());
    }

    @Test
    public void testRestoreInstanceStateNonBundle() {
        SpanlyDial selector = new SpanlyDial(context);
        View.BaseSavedState emptyState = new View.BaseSavedState(android.view.AbsSavedState.EMPTY_STATE);
        selector.onRestoreInstanceState(emptyState);
        assertNotNull(selector);
    }

    @Test
    public void testCircularConstructorsAndMeasures() {
        SpanlyDial c1 = new SpanlyDial(context);
        SpanlyDial c2 = new SpanlyDial(context, null);
        SpanlyDial c3 = new SpanlyDial(context, null, 0);
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotNull(c3);

        c1.setSpanTextPosition(ISpanly.SpanTextPosition.TOP);
        c1.showSpanText(true);
        c1.measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.AT_MOST),
                   View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.AT_MOST));
        assertTrue(c1.getMeasuredWidth() > 0);
        assertTrue(c1.getMeasuredHeight() > 0);

        c1.setSpanTextPosition(ISpanly.SpanTextPosition.BOTTOM);
        c1.measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.UNSPECIFIED),
                   View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.UNSPECIFIED));
        assertTrue(c1.getMeasuredHeight() > 0);
    }

    @Test
    public void testCircularRenderingAllPositions() {
        SpanlyDial selector = new SpanlyDial(context);
        selector.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                         View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY));
        selector.layout(0, 0, 800, 800);

        Bitmap bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Center text, 24h, with ticks
        selector.setSpanTextPosition(ISpanly.SpanTextPosition.CENTER);
        selector.showAmPmLabels(false);
        selector.set24HourFormat(true);
        selector.setStartThumbDrawable(new ColorDrawable(Color.RED));
        selector.setStartThumbDrawableTintColor(Color.YELLOW);
        selector.draw(canvas);

        // Top text, 12h with AM/PM
        selector.setSpanTextPosition(ISpanly.SpanTextPosition.TOP);
        selector.set24HourFormat(false);
        selector.showAmPmLabels(true);
        selector.setThumbStrokeWidth(2f);
        selector.setThumbElevation(4f);
        selector.draw(canvas);

        // Bottom text, hide text, hide ticks
        selector.setSpanTextPosition(ISpanly.SpanTextPosition.BOTTOM);
        selector.showSpanText(false);
        selector.setShowTicks(false);
        selector.setShowTickLabels(false);
        selector.draw(canvas);

        assertNotNull(bitmap);
    }

    @Test
    public void testCircularTouchEventsFullLifecycle() {
        // Enable touch exploration for full accessibility coverage
        ShadowAccessibilityManager shadowA11y = Shadows.shadowOf(
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE));
        shadowA11y.setTouchExplorationEnabled(true);

        SpanlyDial selector = new SpanlyDial(context);
        selector.setThumbMinuteStep(1);
        selector.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                         View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
        selector.layout(0, 0, 1000, 1000);

        // Disabled view ignores touch
        selector.setEnabled(false);
        MotionEvent downDisabled = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 500f, 25f, 0);
        assertFalse(selector.onTouchEvent(downDisabled));
        downDisabled.recycle();
        selector.setEnabled(true);

        // Touch outside thumb targets returns false
        MotionEvent downOutside = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 100f, 0);
        assertFalse(selector.onTouchEvent(downOutside));
        downOutside.recycle();

        // Drag listener rejection
        ISpanly.OnDragChangeListener dragListener = mock(ISpanly.OnDragChangeListener.class);
        when(dragListener.onDragStart(any())).thenReturn(false);
        selector.setOnDragChangeListener(dragListener);

        selector.setSpanStartInMinutes(0); // 12 o'clock (500, 25)
        MotionEvent downRejected = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 500f, 25f, 0);
        assertFalse(selector.onTouchEvent(downRejected));
        downRejected.recycle();

        // Full drag cycle (Start thumb - non-overnight finish)
        when(dragListener.onDragStart(any())).thenReturn(true);
        ISpanly.OnSpanChangeListener spanListener = mock(ISpanly.OnSpanChangeListener.class);
        selector.setOnSpanChangeListener(spanListener);

        MotionEvent downStart = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 500f, 25f, 0);
        assertTrue(selector.onTouchEvent(downStart));

        MotionEvent moveStart = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, 975f, 500f, 0);
        assertTrue(selector.onTouchEvent(moveStart));

        MotionEvent upStart = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, 975f, 500f, 0);
        assertTrue(selector.onTouchEvent(upStart));
        verify(spanListener).onInteractionFinished(any(Integer.class), any(Integer.class), any(Boolean.class));

        downStart.recycle();
        moveStart.recycle();
        upStart.recycle();

        // Full drag cycle (End thumb with overnight finish)
        selector.setOvernightSpanAllowed(true);
        selector.setSpanStartInMinutes(1200); // 8 PM
        selector.setSpanEndInMinutes(720);   // 12 PM (6 o'clock: 500, 975)

        MotionEvent downEnd = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 500f, 975f, 0);
        assertTrue(selector.onTouchEvent(downEnd));

        MotionEvent moveEnd = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, 25f, 500f, 0); // 9 o'clock (180 mins)
        assertTrue(selector.onTouchEvent(moveEnd));

        MotionEvent upEnd = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, 25f, 500f, 0);
        assertTrue(selector.onTouchEvent(upEnd));

        downEnd.recycle();
        moveEnd.recycle();
        upEnd.recycle();
    }

    @Test
    public void testLinearConstructorsAndMeasures() {
        SpanlySlider l1 = new SpanlySlider(context);
        SpanlySlider l2 = new SpanlySlider(context, null);
        SpanlySlider l3 = new SpanlySlider(context, null, 0);
        assertNotNull(l1);
        assertNotNull(l2);
        assertNotNull(l3);

        l1.setSpanTextPosition(ISpanly.SpanTextPosition.TOP);
        l1.showSpanText(true);
        l1.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                   View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.AT_MOST));
        assertTrue(l1.getMeasuredWidth() > 0);
        assertTrue(l1.getMeasuredHeight() > 0);

        l1.setSpanTextPosition(ISpanly.SpanTextPosition.CENTER);
        l1.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.UNSPECIFIED),
                   View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.UNSPECIFIED));
        assertTrue(l1.getMeasuredHeight() > 0);
    }

    @Test
    public void testLinearRenderingAllConfigurations() {
        SpanlySlider selector = new SpanlySlider(context);
        selector.setThumbMinuteStep(1);
        selector.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                         View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY));
        selector.layout(0, 0, 1000, 300);

        Bitmap bitmap = Bitmap.createBitmap(1000, 300, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // TOP text, 24h format
        selector.setSpanTextPosition(ISpanly.SpanTextPosition.TOP);
        selector.set24HourFormat(true);
        selector.setSpanTextColor(Color.BLUE);
        selector.setTickLabelColor(Color.MAGENTA);
        selector.setStartThumbDrawable(new ColorDrawable(Color.RED));
        selector.setStartThumbDrawableTintColor(Color.GREEN);
        selector.setEndThumbDrawable(new ColorDrawable(Color.YELLOW));
        selector.setEndThumbDrawableTintColor(Color.BLACK);
        selector.setThumbStrokeWidth(2f);
        selector.setThumbElevation(4f);
        selector.draw(canvas);

        // BOTTOM text, 12h format with AM/PM
        selector.setSpanTextPosition(ISpanly.SpanTextPosition.BOTTOM);
        selector.set24HourFormat(false);
        selector.showAmPmLabels(true);
        selector.draw(canvas);

        // CENTER text, hide span text and ticks
        selector.setSpanTextPosition(ISpanly.SpanTextPosition.CENTER);
        selector.showSpanText(false);
        selector.setShowTicks(false);
        selector.setShowTickLabels(false);
        selector.draw(canvas);

        assertNotNull(bitmap);
    }

    @Test
    public void testLinearTouchEventsFullLifecycle() {
        ShadowAccessibilityManager shadowA11y = Shadows.shadowOf(
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE));
        shadowA11y.setTouchExplorationEnabled(true);

        SpanlySlider selector = new SpanlySlider(context);
        selector.setThumbMinuteStep(1);
        selector.setSpanInMinutes(300, 900); // Start: ~220px, End: ~625px across track
        selector.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                         View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY));
        selector.layout(0, 0, 1000, 300);

        // Disabled view ignores touch
        selector.setEnabled(false);
        MotionEvent downDisabled = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 220f, 150f, 0);
        assertFalse(selector.onTouchEvent(downDisabled));
        downDisabled.recycle();
        selector.setEnabled(true);

        // Touch outside any thumb
        MotionEvent downOutside = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        assertFalse(selector.onTouchEvent(downOutside));
        downOutside.recycle();

        // Drag listener rejection
        ISpanly.OnDragChangeListener dragListener = mock(ISpanly.OnDragChangeListener.class);
        when(dragListener.onDragStart(any())).thenReturn(false);
        selector.setOnDragChangeListener(dragListener);

        float startThumbX = selector.getPaddingLeft() + selector.getThumbRadius() +
                ((float) selector.getSpanStartInMinutes() / 1440f) * (1000f - 2 * selector.getThumbRadius());
        MotionEvent downRejected = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, startThumbX, 150f, 0);
        assertFalse(selector.onTouchEvent(downRejected));
        downRejected.recycle();

        // Successful drag of start thumb
        when(dragListener.onDragStart(any())).thenReturn(true);
        MotionEvent downStart = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, startThumbX, 150f, 0);
        assertTrue(selector.onTouchEvent(downStart));

        MotionEvent moveStart = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, startThumbX + 50f, 150f, 0);
        assertTrue(selector.onTouchEvent(moveStart));

        MotionEvent upStart = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, startThumbX + 50f, 150f, 0);
        assertTrue(selector.onTouchEvent(upStart));

        downStart.recycle();
        moveStart.recycle();
        upStart.recycle();

        // Successful drag of end thumb with cancel
        float endThumbX = selector.getPaddingLeft() + selector.getThumbRadius() +
                ((float) selector.getSpanEndInMinutes() / 1440f) * (1000f - 2 * selector.getThumbRadius());
        MotionEvent downEnd = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, endThumbX, 150f, 0);
        assertTrue(selector.onTouchEvent(downEnd));

        MotionEvent moveEnd = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, endThumbX - 30f, 150f, 0);
        assertTrue(selector.onTouchEvent(moveEnd));

        MotionEvent cancelEnd = MotionEvent.obtain(0, 100, MotionEvent.ACTION_CANCEL, endThumbX - 30f, 150f, 0);
        assertTrue(selector.onTouchEvent(cancelEnd));

        downEnd.recycle();
        moveEnd.recycle();
        cancelEnd.recycle();

        assertTrue(selector.performClick());
    }

    @Test
    public void testFullStateRestoration() {
        SpanlyDial original = new SpanlyDial(context);
        original.setSpanInMinutes(360, 1080);
        original.setMinDurationMinutes(120);
        original.setMaxDurationMinutes(900);
        original.setThumbMinuteStep(30);
        original.set24HourFormat(false);
        original.setOvernightSpanAllowed(false);
        original.setShowTicks(false);
        original.setShowTickLabels(false);
        original.showAmPmLabels(true);
        original.showSpanText(true);
        original.setSpanTextFormat("START: %1$s, END: %2$s");
        original.setSpanTextPosition(ISpanly.SpanTextPosition.TOP);

        Parcelable savedState = original.onSaveInstanceState();
        assertNotNull(savedState);

        SpanlyDial restored = new SpanlyDial(context);
        restored.onRestoreInstanceState(savedState);

        assertEquals(360, restored.getSpanStartInMinutes());
        assertEquals(1080, restored.getSpanEndInMinutes());
        assertEquals(120, restored.getMinDurationMinutes());
        assertEquals(900, restored.getMaxDurationMinutes());
        assertEquals(30, restored.getThumbMinuteStep());
        assertFalse(restored.is24HourFormat());
        assertFalse(restored.isOvernightSpanAllowed());
        assertFalse(restored.isShowTicks());
        assertFalse(restored.isShowTickLabels());
        assertTrue(restored.isAmPmLabelsShown());
        assertTrue(restored.isSpanTextShown());
        assertEquals("START: %1$s, END: %2$s", restored.getSpanTextFormat());
        assertEquals(ISpanly.SpanTextPosition.TOP, restored.getSpanTextPosition());
    }

    @Test
    public void testLinearOvernightAndRtlRendering() {
        SpanlySlider selector = new SpanlySlider(context);
        selector.setThumbMinuteStep(1);
        selector.setOvernightSpanAllowed(true);
        selector.setSpanInMinutes(1320, 180); // 22:00 to 03:00 (Overnight)
        assertTrue(selector.isOvernight());

        selector.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                         View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY));
        selector.layout(0, 0, 1000, 300);

        Bitmap bitmap = Bitmap.createBitmap(1000, 300, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        selector.draw(canvas);

        // Test RTL rendering
        selector.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        selector.draw(canvas);
        assertNotNull(bitmap);
    }

    @Test
    public void testTimeSpanModelIntegration() {
        SpanlyDial circular = new SpanlyDial(context);
        TimeSpan span = new TimeSpan(480, 960);
        circular.setTimeSpan(span);

        TimeSpan currentSpan = circular.getTimeSpan();
        assertEquals(480, currentSpan.getStartMinutes());
        assertEquals(960, currentSpan.getEndMinutes());
        assertFalse(currentSpan.isOvernight());
        assertEquals(480, currentSpan.getDurationMinutes());
    }

    @Test
    public void testCachedSpanTextLines() {
        SpanlyDial circular = new SpanlyDial(context);
        circular.setSpanInMinutes(600, 900);
        String[] lines = circular.getCachedSpanTextLines();
        assertNotNull(lines);
        assertTrue(lines.length > 0);

        circular.showSpanText(false);
        assertNull(circular.getCachedSpanTextLines());
    }

    @Test
    public void testLinearOverlappingThumbsMovement() {
        SpanlySlider selector = new SpanlySlider(context);
        selector.setThumbMinuteStep(1);
        selector.setSpanInMinutes(600, 600); // Thumbs exactly overlapping at 10:00
        assertEquals(600, selector.getSpanStartInMinutes());
        assertEquals(600, selector.getSpanEndInMinutes());

        selector.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                         View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY));
        selector.layout(0, 0, 1000, 300);

        // Touch the overlapping thumbs
        float thumbX = selector.getPaddingLeft() + selector.getThumbRadius() +
                (600f / 1440f) * (1000f - 2 * selector.getThumbRadius());
        float thumbY = selector.getTrackCenterY();

        // 1. Dragging right from overlapping position should expand the END thumb
        MotionEvent down1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, thumbX, thumbY, 0);
        assertTrue(selector.onTouchEvent(down1));

        MotionEvent moveRight = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, thumbX + 60f, thumbY, 0);
        assertTrue(selector.onTouchEvent(moveRight));

        MotionEvent up1 = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, thumbX + 60f, thumbY, 0);
        assertTrue(selector.onTouchEvent(up1));

        assertEquals(600, selector.getSpanStartInMinutes());
        assertTrue("End thumb must move right when dragging right from overlap", selector.getSpanEndInMinutes() > 600);

        down1.recycle();
        moveRight.recycle();
        up1.recycle();

        // 2. Dragging left from overlapping position should expand the START thumb
        selector.setSpanInMinutes(600, 600);
        MotionEvent down2 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, thumbX, thumbY, 0);
        assertTrue(selector.onTouchEvent(down2));

        MotionEvent moveLeft = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, thumbX - 60f, thumbY, 0);
        assertTrue(selector.onTouchEvent(moveLeft));

        MotionEvent up2 = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, thumbX - 60f, thumbY, 0);
        assertTrue(selector.onTouchEvent(up2));

        assertEquals(600, selector.getSpanEndInMinutes());
        assertTrue("Start thumb must move left when dragging left from overlap", selector.getSpanStartInMinutes() < 600);

        down2.recycle();
        moveLeft.recycle();
        up2.recycle();

        // 3. Test crossover: dragging START past END swaps thumbs seamlessly
        selector.setSpanInMinutes(300, 500);
        float startX = selector.getPaddingLeft() + selector.getThumbRadius() +
                (300f / 1440f) * (1000f - 2 * selector.getThumbRadius());
        float pastEndX = selector.getPaddingLeft() + selector.getThumbRadius() +
                (600f / 1440f) * (1000f - 2 * selector.getThumbRadius());

        MotionEvent downStart = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, startX, thumbY, 0);
        assertTrue(selector.onTouchEvent(downStart));

        MotionEvent movePastEnd = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, pastEndX, thumbY, 0);
        assertTrue(selector.onTouchEvent(movePastEnd));

        MotionEvent upCrossover = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, pastEndX, thumbY, 0);
        assertTrue(selector.onTouchEvent(upCrossover));

        assertEquals("Old end becomes new start upon crossover", 500, selector.getSpanStartInMinutes());
        assertTrue("Dragged thumb becomes new end", selector.getSpanEndInMinutes() >= 590);

        downStart.recycle();
        movePastEnd.recycle();
        upCrossover.recycle();
    }

    @Test
    public void testCircularOverlappingThumbsMovement() {
        SpanlyDial selector = new SpanlyDial(context);
        selector.setThumbMinuteStep(1);
        selector.setOvernightSpanAllowed(false);
        // Overlapping at 6 o'clock (720 minutes = 12:00 PM)
        selector.setSpanInMinutes(720, 720);
        assertEquals(720, selector.getSpanStartInMinutes());
        assertEquals(720, selector.getSpanEndInMinutes());

        selector.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                         View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
        selector.layout(0, 0, 1000, 1000);

        // 6 o'clock is at (500, 975)
        float thumbX = 500f;
        float thumbY = 975f;

        // 1. Dragging clockwise (towards 7-8-9 o'clock) should expand END thumb
        MotionEvent down1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, thumbX, thumbY, 0);
        assertTrue(selector.onTouchEvent(down1));

        MotionEvent moveClockwise = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, 300f, 950f, 0);
        assertTrue(selector.onTouchEvent(moveClockwise));

        MotionEvent up1 = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, 300f, 950f, 0);
        assertTrue(selector.onTouchEvent(up1));

        assertEquals(720, selector.getSpanStartInMinutes());
        assertTrue("End thumb must move clockwise when dragging clockwise from overlap", selector.getSpanEndInMinutes() > 720);

        down1.recycle();
        moveClockwise.recycle();
        up1.recycle();

        // 2. Dragging counter-clockwise (towards 5-4-3 o'clock) should expand START thumb
        selector.setSpanInMinutes(720, 720);
        MotionEvent down2 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, thumbX, thumbY, 0);
        assertTrue(selector.onTouchEvent(down2));

        MotionEvent moveCounterClockwise = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, 700f, 950f, 0);
        assertTrue(selector.onTouchEvent(moveCounterClockwise));

        MotionEvent up2 = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, 700f, 950f, 0);
        assertTrue(selector.onTouchEvent(up2));

        assertEquals(720, selector.getSpanEndInMinutes());
        assertTrue("Start thumb must move counter-clockwise when dragging counter-clockwise from overlap", selector.getSpanStartInMinutes() < 720);

        down2.recycle();
        moveCounterClockwise.recycle();
        up2.recycle();

        // 3. Test crossover: when overnight is disallowed, dragging START clockwise past END swaps thumbs seamlessly
        selector.setSpanInMinutes(600, 720);
        float startAngleRad = (float) Math.toRadians(150 - 90);
        float sX = 500f + (float) (475f * Math.cos(startAngleRad));
        float sY = 500f + (float) (475f * Math.sin(startAngleRad));

        MotionEvent downStart = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, sX, sY, 0);
        assertTrue(selector.onTouchEvent(downStart));

        MotionEvent movePastEnd = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, 262.5f, 911f, 0);
        assertTrue(selector.onTouchEvent(movePastEnd));

        MotionEvent upCrossover = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, 262.5f, 911f, 0);
        assertTrue(selector.onTouchEvent(upCrossover));

        assertEquals("Old end becomes new start upon crossover", 720, selector.getSpanStartInMinutes());
        assertTrue("Dragged thumb becomes new end", selector.getSpanEndInMinutes() > 720);

        downStart.recycle();
        movePastEnd.recycle();
        upCrossover.recycle();
    }
}
