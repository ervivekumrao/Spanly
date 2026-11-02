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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LinearTimeSpanSelector extends BaseTimeSpanSelector {

    private float touchOffset = 0f;
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spanTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blockedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF trackRect = new RectF();
    private Thumb activeThumb = Thumb.NONE;
    private int lastSpanDragMinutes = 0;
    private boolean isThumbAmbiguous = false;
    private float downTouchX = 0f;
    private float spanTextY;
    private float tickY;
    private AccessibilityManager accessibilityManager;

    private static class LinearLabelItem {
        final float x;
        final float y;
        final String[] lines;

        LinearLabelItem(float x, float y, String[] lines) {
            this.x = x;
            this.y = y;
            this.lines = lines;
        }
    }

    private final List<LinearLabelItem> cachedTickLabels = new ArrayList<>();
    private final float[] hourTickLines = new float[25 * 4];
    private int hourTickLineCount = 0;
    private final float[] minuteTickLines = new float[72 * 4];
    private int minuteTickLineCount = 0;

    public LinearTimeSpanSelector(Context context) {
        this(context, null);
    }

    public LinearTimeSpanSelector(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearTimeSpanSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initializes the view with default values and custom attributes.
     */
    private void init(Context context, @Nullable AttributeSet attrs) {
        accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        initializeDefaultValues();
        initializeDefaultColors();
        if (attrs != null) {
            initializeFromAttributes(context, attrs);
        }
        setupPaints();
        setFocusable(true);
        setClickable(true);
        setContentDescription(getContext().getString(R.string.tss_content_description_time_span_selector));
    }

    /**
     * Sets up default dimension and color values.
     */
    private void initializeDefaultValues() {
        hourTickHeight = dp(8f);
        minuteTickHeight = dp(4f);
        hourTickWidth = dp(1.5f);
        minuteTickWidth = dp(1f);
        tickLabelSize = sp(12f);
        trackWidth = dp(8f);
        thumbRadius = dp(12f);
        thumbTouchRadiusPadding = hourTickHeight + dp(2f);
        thumbStrokeWidth = dp(0f);
        tickDistanceFromTrack = dp(0);
        tickLabelDistanceFromTick = dp(0);
        spanTextSize = sp(14f);
        thumbShadowDx = dp(2f);
        thumbShadowDy = dp(2f);
        thumbElevation = dp(4f);
        isOvernightSpanAllowed = false; // Linear selector typically doesn't support overnight
    }

    /**
     * Loads view configuration from XML attributes.
     */
    private void initializeFromAttributes(Context context, @NonNull AttributeSet attrs) {
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinearTimeSpanSelector)) {
            thumbMinuteStep = a.getInt(R.styleable.LinearTimeSpanSelector_tss_thumbMinuteStep, DEFAULT_THUMB_MINUTE_STEP);

            currentStartMinutes = getStartTimeFromAttrs(a);
            currentEndMinutes = getEndTimeFromAttrs(a);
            minDurationMinutes = a.getInt(R.styleable.LinearTimeSpanSelector_tss_minDurationMinutes, minDurationMinutes);
            maxDurationMinutes = a.getInt(R.styleable.LinearTimeSpanSelector_tss_maxDurationMinutes, maxDurationMinutes);

            trackWidth = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_trackWidth, trackWidth);
            trackColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_trackColor, trackColor);
            spanColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_spanColor, spanColor);
            thumbFillColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_thumbFillColor, thumbFillColor);
            thumbStrokeColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_thumbStrokeColor, thumbStrokeColor);
            thumbStrokeWidth = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbStrokeWidth, thumbStrokeWidth);

            startThumbDrawable = a.getDrawable(R.styleable.LinearTimeSpanSelector_tss_startThumbDrawable);
            endThumbDrawable = a.getDrawable(R.styleable.LinearTimeSpanSelector_tss_endThumbDrawable);
            startThumbDrawableTintColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_startThumbDrawableTintColor, startThumbDrawableTintColor);
            endThumbDrawableTintColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_endThumbDrawableTintColor, endThumbDrawableTintColor);

            thumbRadius = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbRadius, thumbRadius);
            thumbShadowDx = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbShadowDx, thumbShadowDx);
            thumbShadowDy = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbShadowDy, thumbShadowDy);
            thumbElevation = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbElevation, thumbElevation);
            thumbShadowColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_thumbShadowColor, thumbShadowColor);

            showTicks = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_showTicks, showTicks);
            hourTickColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_hourTickColor, hourTickColor);
            minuteTickColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_minuteTickColor, minuteTickColor);
            tickLabelColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_tickLabelColor, tickLabelColor);
            tickLabelSize = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_tickLabelSize, tickLabelSize);
            tickLabelStyle = a.getInt(R.styleable.LinearTimeSpanSelector_tss_tickLabelStyle, tickLabelStyle);
            tickDistanceFromTrack = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_tickDistanceFromTrack, tickDistanceFromTrack);
            tickLabelDistanceFromTick = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_tickLabelDistanceFromTick, tickLabelDistanceFromTick);

            hourTickHeight = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_hourTickHeight, hourTickHeight);
            minuteTickHeight = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_minuteTickHeight, minuteTickHeight);
            hourTickWidth = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_hourTickWidth, hourTickWidth);
            minuteTickWidth = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_minuteTickWidth, minuteTickWidth);
            thumbTouchRadiusPadding = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbTouchRadiusPadding, thumbTouchRadiusPadding);

            is24HourFormat = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_is24HourFormat, is24HourFormat);
            showAmPmLabels = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_showAmPmLabels, showAmPmLabels);
            showTickLabels = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_showTickLabels, showTickLabels);
            isSpanTextShown = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_showSpanText, isSpanTextShown);

            if (a.hasValue(R.styleable.LinearTimeSpanSelector_tss_textColor)) {
                int commonColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_textColor, Color.BLACK);
                tickLabelColor = commonColor;
                spanTextColor = commonColor;
            }

            spanTextColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_spanTextColor, spanTextColor);
            tickLabelColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_tickLabelColor, tickLabelColor);
            spanTextSize = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_spanTextSize, spanTextSize);
            spanTextStyle = a.getInt(R.styleable.LinearTimeSpanSelector_tss_spanTextStyle, spanTextStyle);

            int pos = a.getInt(R.styleable.LinearTimeSpanSelector_tss_spanTextPosition, 1);
            if (pos == 0) {
                spanTextPosition = SpanTextPosition.TOP;
            } else if (pos == 2) {
                spanTextPosition = SpanTextPosition.CENTER;
            } else {
                spanTextPosition = SpanTextPosition.BOTTOM;
            }

            String format = a.getString(R.styleable.LinearTimeSpanSelector_tss_spanTextFormat);
            if (format != null) spanTextFormat = format;

            int tickStyle = a.getInt(R.styleable.LinearTimeSpanSelector_tss_tickEdgeStyle, 1);
            tickEdgeStyle = (tickStyle == 0) ? TickEdgeStyle.ROUND : TickEdgeStyle.BUTT;
        }
    }

    private int getStartTimeFromAttrs(TypedArray a) {
        int minutes = a.getInt(R.styleable.LinearTimeSpanSelector_tss_spanStartInMinutes, currentStartMinutes);
        String timeStr = a.getString(R.styleable.LinearTimeSpanSelector_tss_spanStartTime);
        if (timeStr != null) {
            try {
                minutes = TimeUtils.convertTimeToMinutes(timeStr);
            } catch (Exception ignored) {
            }
        }
        return TimeUtils.snapToStep(minutes, thumbMinuteStep);
    }

    private int getEndTimeFromAttrs(TypedArray a) {
        int minutes = a.getInt(R.styleable.LinearTimeSpanSelector_tss_spanEndInMinutes, currentEndMinutes);
        String timeStr = a.getString(R.styleable.LinearTimeSpanSelector_tss_spanEndTime);
        if (timeStr != null) {
            try {
                minutes = TimeUtils.convertTimeToMinutes(timeStr);
            } catch (Exception ignored) {
            }
        }
        return TimeUtils.snapToStep(minutes, thumbMinuteStep);
    }

    /**
     * Initializes and configures Paint objects used for drawing the linear slider.
     */
    private void setupPaints() {
        trackPaint.setStyle(Paint.Style.FILL);
        spanPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(thumbFillColor);
        if (thumbElevation > 0) {
            thumbPaint.setShadowLayer(thumbElevation, thumbShadowDx, thumbShadowDy, thumbShadowColor);
        }
        thumbStrokePaint.setStyle(Paint.Style.STROKE);
        thumbStrokePaint.setColor(thumbStrokeColor);
        thumbStrokePaint.setStrokeWidth(thumbStrokeWidth);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(tickEdgeStyle == TickEdgeStyle.ROUND ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        tickLabelPaint.setColor(tickLabelColor);
        tickLabelPaint.setTextSize(tickLabelSize);
        tickLabelPaint.setTextAlign(Paint.Align.CENTER);
        tickLabelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, tickLabelStyle));
        spanTextPaint.setColor(spanTextColor);
        spanTextPaint.setTextSize(spanTextSize);
        spanTextPaint.setTextAlign(Paint.Align.CENTER);
        spanTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, spanTextStyle));
    }

    @Override
    public void setSpanTextColor(int color) {
        super.setSpanTextColor(color);
        spanTextPaint.setColor(spanTextColor);
        invalidate();
    }

    @Override
    public void setTickLabelColor(int color) {
        super.setTickLabelColor(color);
        tickLabelPaint.setColor(tickLabelColor);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);
        float totalHeight = calculateTotalHeight();
        int desiredHeight = (int) (totalHeight + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }

    private float calculateTotalHeight() {
        float h = Math.max(trackWidth, thumbRadius * 2);
        if (showTicks) h += calculateTickSpaceHeight();
        if (isSpanTextShown) h += calculateSpanTextHeight() + dp(8);
        return h;
    }

    private float calculateSpanTextHeight() {
        if (!isSpanTextShown) return 0;
        String[] lines = getCachedSpanTextLines();
        int count = (lines != null) ? lines.length : 1;
        Paint.FontMetrics fm = spanTextPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        return count * lineHeight;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        updateLayout(w, h);
    }

    private void updateLayout(int w, int h) {
        float spanTextH = calculateSpanTextHeight();
        float tickSpaceH = calculateTickSpaceHeight();
        float coreH = Math.max(trackWidth, thumbRadius * 2);
        float margin = dp(8);

        float totalNeeded = calculateTotalHeight();
        float availableH = h - getPaddingTop() - getPaddingBottom();

        float currentY = getPaddingTop() + Math.max(0, (availableH - totalNeeded) / 2f);

        if (spanTextPosition == SpanTextPosition.TOP && isSpanTextShown) {
            spanTextY = currentY;
            currentY += spanTextH + margin;
        }

        float trackCenterY = currentY + coreH / 2f;
        float trackTop = trackCenterY - trackWidth / 2f;
        trackRect.set(getPaddingLeft() + thumbRadius, trackTop, w - getPaddingRight() - thumbRadius, trackTop + trackWidth);
        currentY += coreH;

        if (showTicks) {
            tickY = currentY + tickDistanceFromTrack + dp(2);
            currentY += tickSpaceH;
        } else {
            tickY = 0;
        }

        if (isSpanTextShown && (spanTextPosition == SpanTextPosition.BOTTOM || spanTextPosition == SpanTextPosition.CENTER)) {
            spanTextY = currentY + margin;
        }

        onSpanGradientChanged();
        updateTickGeometry();
    }

    private void updateTickGeometry() {
        if (trackRect.width() <= 0) return;

        hourTickLineCount = 0;
        cachedTickLabels.clear();
        Paint.FontMetrics fm = tickLabelPaint.getFontMetrics();

        for (int m = 0; m <= MINUTES_IN_DAY; m += 60) {
            float x = minutesToPosition(m);
            if (hourTickLineCount + 4 <= hourTickLines.length) {
                hourTickLines[hourTickLineCount++] = x;
                hourTickLines[hourTickLineCount++] = tickY;
                hourTickLines[hourTickLineCount++] = x;
                hourTickLines[hourTickLineCount++] = tickY + hourTickHeight;
            }

            if (showTickLabels) {
                String label = formatTickLabel(m);
                if (!label.isEmpty()) {
                    String[] lines = label.split("\n");
                    float ly = tickY + hourTickHeight + tickLabelDistanceFromTick + dp(4) - fm.ascent;
                    cachedTickLabels.add(new LinearLabelItem(x, ly, lines));
                }
            }
        }

        minuteTickLineCount = 0;
        for (int m = 15; m < MINUTES_IN_DAY; m += 15) {
            if (m % 60 == 0) continue;
            float x = minutesToPosition(m);
            if (minuteTickLineCount + 4 <= minuteTickLines.length) {
                minuteTickLines[minuteTickLineCount++] = x;
                minuteTickLines[minuteTickLineCount++] = tickY;
                minuteTickLines[minuteTickLineCount++] = x;
                minuteTickLines[minuteTickLineCount++] = tickY + minuteTickHeight;
            }
        }
    }

    private float calculateTickSpaceHeight() {
        if (!showTicks) return 0;
        float h = tickDistanceFromTrack + dp(2) + hourTickHeight;
        if (showTickLabels) {
            Paint.FontMetrics fm = tickLabelPaint.getFontMetrics();
            float lineHeight = fm.descent - fm.ascent;
            int maxLines = (showAmPmLabels && !is24HourFormat) ? 2 : 1;
            h += tickLabelDistanceFromTick + dp(4) + maxLines * lineHeight;
        }
        return h;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeThumb = getActiveThumb(x, y);
                if (activeThumb != Thumb.NONE) {
                    float sX = minutesToPosition(currentStartMinutes);
                    float eX = minutesToPosition(currentEndMinutes);
                    float touchR = thumbRadius + thumbTouchRadiusPadding;
                    if (Math.abs(x - sX) <= touchR && Math.abs(x - eX) <= touchR) {
                        isThumbAmbiguous = true;
                        downTouchX = x;
                    } else {
                        isThumbAmbiguous = false;
                    }

                    if (dragChangeListener != null) {
                        if (!dragChangeListener.onDragStart(activeThumb)) {
                            activeThumb = Thumb.NONE;
                            isThumbAmbiguous = false;
                            return false;
                        }
                    }
                    if (activeThumb == Thumb.SPAN) {
                        lastSpanDragMinutes = positionToMinutes(x);
                    } else {
                        float thumbCenterX = minutesToPosition(activeThumb == Thumb.START ? currentStartMinutes : currentEndMinutes);
                        touchOffset = x - thumbCenterX;
                    }
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                    playSoundEffect(SoundEffectConstants.CLICK);
                    return true;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (activeThumb == Thumb.SPAN) {
                    int currentPosMin = positionToMinutes(x);
                    int delta = currentPosMin - lastSpanDragMinutes;
                    int snappedDelta = TimeUtils.snapToStep(delta, thumbMinuteStep);
                    if (snappedDelta != 0) {
                        shiftSpan(snappedDelta);
                        lastSpanDragMinutes = currentPosMin;
                        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                    }
                    return true;
                } else if (activeThumb != Thumb.NONE) {
                    if (isThumbAmbiguous) {
                        float dx = x - downTouchX;
                        if (Math.abs(dx) >= dp(1.5f)) {
                            boolean draggingForward = isRtl() ? (dx < 0) : (dx > 0);
                            Thumb targetThumb = draggingForward ? Thumb.END : Thumb.START;
                            if (targetThumb != activeThumb) {
                                if (dragChangeListener != null) {
                                    dragChangeListener.onDragStop(activeThumb);
                                    dragChangeListener.onDragStart(targetThumb);
                                }
                                activeThumb = targetThumb;
                            }
                            float thumbCenterX = minutesToPosition(activeThumb == Thumb.START ? currentStartMinutes : currentEndMinutes);
                            touchOffset = downTouchX - thumbCenterX;
                            isThumbAmbiguous = false;
                        } else {
                            return true;
                        }
                    }

                    float thumbTargetX = x - touchOffset;
                    int newMin = positionToMinutes(thumbTargetX);
                    newMin = TimeUtils.snapToStep(newMin, thumbMinuteStep);
                    int prevMin = (activeThumb == Thumb.START) ? currentStartMinutes : currentEndMinutes;

                    if (activeThumb == Thumb.START) {
                        if (!isOvernight() && newMin > currentEndMinutes) {
                            int oldEnd = currentEndMinutes;
                            updateSpan(oldEnd, newMin, false);
                            activeThumb = Thumb.END;
                            touchOffset = x - minutesToPosition(newMin);
                            if (dragChangeListener != null) {
                                dragChangeListener.onDragStop(Thumb.START);
                                dragChangeListener.onDragStart(Thumb.END);
                            }
                        } else {
                            updateSpan(newMin, currentEndMinutes, true);
                        }
                    } else if (activeThumb == Thumb.END) {
                        if (!isOvernight() && newMin < currentStartMinutes) {
                            int oldStart = currentStartMinutes;
                            updateSpan(newMin, oldStart, true);
                            activeThumb = Thumb.START;
                            touchOffset = x - minutesToPosition(newMin);
                            if (dragChangeListener != null) {
                                dragChangeListener.onDragStop(Thumb.END);
                                dragChangeListener.onDragStart(Thumb.START);
                            }
                        } else {
                            updateSpan(currentStartMinutes, newMin, false);
                        }
                    }

                    int updatedMin = (activeThumb == Thumb.START) ? currentStartMinutes : currentEndMinutes;
                    if (prevMin != updatedMin) {
                        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isThumbAmbiguous = false;
                if (activeThumb != Thumb.NONE) {
                    if (dragChangeListener != null) {
                        dragChangeListener.onDragStop(activeThumb);
                    }
                    activeThumb = Thumb.NONE;
                    announceSpan();
                    notifySpanChanged(true);
                    performClick();
                }
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void announceSpan() {
        if (accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled()) {
            String start = TimeUtils.formatDisplayTime(currentStartMinutes, is24HourFormat, getContext().getString(R.string.tss_am), getContext().getString(R.string.tss_pm));
            String end = TimeUtils.formatDisplayTime(currentEndMinutes, is24HourFormat, getContext().getString(R.string.tss_am), getContext().getString(R.string.tss_pm));
            announceForAccessibility(getContext().getString(isOvernight() ? R.string.tss_accessibility_span_overnight_selected : R.string.tss_accessibility_span_selected, start, end));
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private Thumb getActiveThumb(float x, float y) {
        float sX = minutesToPosition(currentStartMinutes);
        float eX = minutesToPosition(currentEndMinutes);
        float touchR = thumbRadius + thumbTouchRadiusPadding;
        float dStart = Math.abs(x - sX);
        float dEnd = Math.abs(x - eX);

        if (dStart <= touchR || dEnd <= touchR) {
            if (dStart <= touchR && dEnd <= touchR) {
                return dStart < dEnd ? Thumb.START : Thumb.END;
            }
            return dStart <= touchR ? Thumb.START : Thumb.END;
        }

        if (isSpanDraggingAllowed && trackRect.width() > 0) {
            float verticalHitTolerance = Math.max(trackWidth / 2f, thumbRadius) + thumbTouchRadiusPadding;
            if (Math.abs(y - trackRect.centerY()) <= verticalHitTolerance) {
                boolean inSpan;
                if (isOvernight()) {
                    float x24 = minutesToPosition(MINUTES_IN_DAY);
                    float x0 = minutesToPosition(0);
                    boolean inSegment1 = x >= Math.min(sX, x24) && x <= Math.max(sX, x24);
                    boolean inSegment2 = x >= Math.min(x0, eX) && x <= Math.max(x0, eX);
                    inSpan = inSegment1 || inSegment2;
                } else {
                    inSpan = x >= Math.min(sX, eX) && x <= Math.max(sX, eX);
                }
                if (inSpan) {
                    return Thumb.SPAN;
                }
            }
        }
        return Thumb.NONE;
    }

    private boolean isRtl() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    private float minutesToPosition(int minutes) {
        if (isRtl()) {
            return trackRect.right - ((float) minutes / MINUTES_IN_DAY) * trackRect.width();
        } else {
            return trackRect.left + ((float) minutes / MINUTES_IN_DAY) * trackRect.width();
        }
    }

    private int positionToMinutes(float pos) {
        if (trackRect.width() <= 0) return 0;
        float fraction = isRtl() ? (trackRect.right - pos) / trackRect.width() : (pos - trackRect.left) / trackRect.width();
        return Math.max(0, Math.min(Math.round(fraction * MINUTES_IN_DAY), MINUTES_IN_DAY));
    }

    @Override
    protected void onSpanGradientChanged() {
        if (spanGradientColors != null && spanGradientColors.length >= 2 && trackRect.width() > 0) {
            LinearGradient gradient = new LinearGradient(
                    trackRect.left, trackRect.top, trackRect.right, trackRect.top,
                    spanGradientColors, null, Shader.TileMode.CLAMP);
            spanPaint.setShader(gradient);
        } else {
            spanPaint.setShader(null);
            spanPaint.setColor(spanColor);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (isSpanTextShown && spanTextPosition == SpanTextPosition.TOP) {
            drawSpanText(canvas, spanTextY);
        }
        trackPaint.setColor(trackColor);
        canvas.drawRoundRect(trackRect, trackRect.height() / 2, trackRect.height() / 2, trackPaint);
        drawBlockedIntervals(canvas);
        if (showTicks) drawTicks(canvas);
        float sX = minutesToPosition(currentStartMinutes);
        float eX = minutesToPosition(currentEndMinutes);
        spanPaint.setColor(spanColor);
        if (isOvernight()) {
            float x24 = minutesToPosition(MINUTES_IN_DAY);
            float x0 = minutesToPosition(0);
            canvas.drawRect(Math.min(sX, x24), trackRect.top, Math.max(sX, x24), trackRect.bottom, spanPaint);
            canvas.drawRect(Math.min(x0, eX), trackRect.top, Math.max(x0, eX), trackRect.bottom, spanPaint);
        } else {
            canvas.drawRect(Math.min(sX, eX), trackRect.top, Math.max(sX, eX), trackRect.bottom, spanPaint);
        }
        drawSingleThumb(canvas, sX, trackRect.centerY(), startThumbDrawable, startThumbDrawableTintColor);
        drawSingleThumb(canvas, eX, trackRect.centerY(), endThumbDrawable, endThumbDrawableTintColor);
        if (isSpanTextShown && (spanTextPosition == SpanTextPosition.BOTTOM || spanTextPosition == SpanTextPosition.CENTER)) {
            drawSpanText(canvas, spanTextY);
        }
    }

    private void drawBlockedIntervals(@NonNull Canvas canvas) {
        if (blockedIntervals.isEmpty() || trackRect.width() <= 0) return;
        blockedPaint.setStyle(Paint.Style.FILL);
        for (BlockedInterval interval : blockedIntervals) {
            blockedPaint.setColor(interval.getColor());
            float s = minutesToPosition(interval.getStartMinutes());
            float e = minutesToPosition(interval.getEndMinutes());
            if (interval.isOvernight()) {
                float x24 = minutesToPosition(MINUTES_IN_DAY);
                float x0 = minutesToPosition(0);
                canvas.drawRect(Math.min(s, x24), trackRect.top, Math.max(s, x24), trackRect.bottom, blockedPaint);
                canvas.drawRect(Math.min(x0, e), trackRect.top, Math.max(x0, e), trackRect.bottom, blockedPaint);
            } else {
                canvas.drawRect(Math.min(s, e), trackRect.top, Math.max(s, e), trackRect.bottom, blockedPaint);
            }
        }
    }

    private void drawSpanText(Canvas canvas, float y) {
        String[] lines = getCachedSpanTextLines();
        if (lines == null || lines.length == 0) return;
        Paint.FontMetrics fm = spanTextPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float curY = y - fm.ascent;
        for (String line : lines) {
            canvas.drawText(line, getWidth() / 2f, curY, spanTextPaint);
            curY += lineHeight;
        }
    }

    private void drawSingleThumb(Canvas canvas, float x, float y, Drawable d, int tint) {
        thumbPaint.setColor(thumbFillColor);
        canvas.drawCircle(x, y, thumbRadius, thumbPaint);
        if (d != null) {
            if (tint != Color.TRANSPARENT) DrawableCompat.setTint(d, tint);
            int size = (int) (thumbRadius * 1.2f);
            d.setBounds((int) (x - (float) size / 2), (int) (y - (float) size / 2), (int) (x + (float) size / 2), (int) (y + (float) size / 2));
            d.draw(canvas);
        }
        if (thumbStrokeWidth > 0) {
            thumbStrokePaint.setColor(thumbStrokeColor);
            thumbStrokePaint.setStrokeWidth(thumbStrokeWidth);
            canvas.drawCircle(x, y, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint);
        }
    }

    private void drawTicks(Canvas canvas) {
        if (!showTicks) return;
        tickPaint.setColor(hourTickColor);
        tickPaint.setStrokeWidth(hourTickWidth);
        if (hourTickLineCount > 0) {
            canvas.drawLines(hourTickLines, 0, hourTickLineCount, tickPaint);
        }

        if (showTickLabels && !cachedTickLabels.isEmpty()) {
            Paint.FontMetrics fm = tickLabelPaint.getFontMetrics();
            float lineHeight = fm.descent - fm.ascent;
            for (int i = 0; i < cachedTickLabels.size(); i++) {
                LinearLabelItem item = cachedTickLabels.get(i);
                float ly = item.y;
                for (String line : item.lines) {
                    canvas.drawText(line, item.x, ly, tickLabelPaint);
                    ly += lineHeight;
                }
            }
        }

        tickPaint.setColor(minuteTickColor);
        tickPaint.setStrokeWidth(minuteTickWidth);
        if (minuteTickLineCount > 0) {
            canvas.drawLines(minuteTickLines, 0, minuteTickLineCount, tickPaint);
        }
    }

    private String formatTickLabel(int minutes) {
        int h = (minutes / 60) % 24;
        if (h % 2 != 0) return "";
        if (is24HourFormat) return String.format(Locale.getDefault(), "%02d", h);
        int dh = h % 12;
        if (dh == 0) dh = 12;
        String ap = showAmPmLabels ? getContext().getString(h < 12 ? R.string.tss_am : R.string.tss_pm) : "";
        return ap.isEmpty() ? String.valueOf(dh) : dh + "\n" + ap;
    }

    /**
     * Returns the vertical center coordinate of the track in pixels.
     *
     * @return Center Y in pixels.
     */
    public float getTrackCenterY() {
        return trackRect.centerY();
    }
}
