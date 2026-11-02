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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Base class for time span selectors containing shared logic and attributes.
 */
public abstract class BaseTimeSpanSelector extends View implements TimeSpanSelector {

    private static final String KEY_SUPER_STATE = "super_state";
    private static final String KEY_START_MINUTES = "start_minutes";
    private static final String KEY_END_MINUTES = "end_minutes";
    private static final String KEY_MIN_DURATION = "min_duration";
    private static final String KEY_MAX_DURATION = "max_duration";
    private static final String KEY_THUMB_STEP = "thumb_step";
    private static final String KEY_IS_24_HOUR = "is_24_hour";
    private static final String KEY_ALLOW_OVERNIGHT = "allow_overnight";
    private static final String KEY_SHOW_TICKS = "show_ticks";
    private static final String KEY_SHOW_TICK_LABELS = "show_tick_labels";
    private static final String KEY_SHOW_AM_PM_LABELS = "show_am_pm_labels";
    private static final String KEY_SHOW_SPAN_TEXT = "show_span_text";
    private static final String KEY_SPAN_TEXT_FORMAT = "span_text_format";
    private static final String KEY_SPAN_TEXT_POSITION = "span_text_position";

    @Nullable
    protected String[] cachedSpanTextLines;

    protected OnSpanChangeListener spanChangeListener;
    protected OnTimeChangeListener timeChangeListener;
    protected OnDragChangeListener dragChangeListener;

    protected boolean showTicks = true;
    protected boolean showTickLabels = true;
    protected boolean showAmPmLabels = false;
    protected boolean isSpanTextShown = true;
    protected boolean is24HourFormat = true;
    protected boolean isOvernightSpanAllowed = true;

    protected int currentStartMinutes = DEFAULT_START_MINUTES;
    protected int currentEndMinutes = DEFAULT_END_MINUTES;
    protected int thumbMinuteStep = DEFAULT_THUMB_MINUTE_STEP;

    protected int minDurationMinutes = 0;
    protected int maxDurationMinutes = MINUTES_IN_DAY;

    @ColorInt
    protected int trackColor;
    @ColorInt
    protected int spanColor;
    @ColorInt
    protected int spanTextColor;
    @ColorInt
    protected int thumbFillColor;
    @ColorInt
    protected int thumbStrokeColor;
    @ColorInt
    protected int thumbShadowColor;
    @ColorInt
    protected int startThumbDrawableTintColor;
    @ColorInt
    protected int endThumbDrawableTintColor;
    @ColorInt
    protected int hourTickColor;
    @ColorInt
    protected int minuteTickColor;
    @ColorInt
    protected int tickLabelColor;

    protected float trackWidth;
    protected float thumbRadius;
    protected float thumbTouchRadiusPadding;
    protected float thumbStrokeWidth;
    protected float thumbShadowDx;
    protected float thumbShadowDy;
    protected float thumbElevation;
    protected float hourTickHeight;
    protected float minuteTickHeight;
    protected float hourTickWidth;
    protected float minuteTickWidth;
    protected float tickLabelSize;
    protected float tickDistanceFromTrack;
    protected float tickLabelDistanceFromTick;
    protected float spanTextSize;

    protected int tickLabelStyle = Typeface.NORMAL;
    protected int spanTextStyle = Typeface.NORMAL;
    protected String spanTextFormat = DEFAULT_SPAN_TEXT_FORMAT;
    protected SpanTextPosition spanTextPosition = SpanTextPosition.BOTTOM;
    protected TickEdgeStyle tickEdgeStyle = TickEdgeStyle.BUTT;

    @Nullable
    protected Drawable startThumbDrawable;
    @Nullable
    protected Drawable endThumbDrawable;

    public BaseTimeSpanSelector(Context context) {
        this(context, null);
    }

    public BaseTimeSpanSelector(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseTimeSpanSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Resolves a color attribute from the current theme, falling back to a default value if not resolved.
     */
    protected int resolveThemeColor(int attr, int defaultColor) {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, tv, true)) {
            if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                return tv.data;
            } else if (tv.resourceId != 0) {
                try {
                    return ContextCompat.getColor(getContext(), tv.resourceId);
                } catch (Exception ignored) {
                }
            }
        }
        return defaultColor;
    }

    /**
     * Initializes default colors from resources and theme.
     */
    protected void initializeDefaultColors() {
        trackColor = resolveThemeColor(android.R.attr.textColorSecondaryNoDisable, DEFAULT_TRACK_COLOR);
        spanColor = resolveThemeColor(android.R.attr.colorPrimary, DEFAULT_SPAN_COLOR);
        spanTextColor = resolveThemeColor(android.R.attr.textColorPrimary, DEFAULT_SPAN_TEXT_COLOR);
        thumbFillColor = resolveThemeColor(android.R.attr.colorPrimary, DEFAULT_THUMB_FILL_COLOR);
        thumbStrokeColor = resolveThemeColor(android.R.attr.strokeColor, DEFAULT_THUMB_STROKE_COLOR);
        thumbShadowColor = resolveThemeColor(android.R.attr.shadowColor, DEFAULT_THUMB_SHADOW_COLOR);
        startThumbDrawableTintColor = resolveThemeColor(android.R.attr.thumbTint, DEFAULT_THUMB_DRAWABLE_TINT_COLOR);
        endThumbDrawableTintColor = resolveThemeColor(android.R.attr.thumbTint, DEFAULT_THUMB_DRAWABLE_TINT_COLOR);
        hourTickColor = resolveThemeColor(android.R.attr.textColorPrimary, DEFAULT_TICK_COLOR);
        minuteTickColor = resolveThemeColor(android.R.attr.textColorPrimary, DEFAULT_TICK_COLOR);
        tickLabelColor = resolveThemeColor(android.R.attr.textColorPrimary, DEFAULT_TICK_LABEL_COLOR);
    }

    /**
     * Updates and caches the formatted text lines to avoid allocations in onDraw.
     */
    public void updateCachedSpanText() {
        if (!isSpanTextShown) {
            cachedSpanTextLines = null;
            return;
        }
        String text = getFormattedSpanText();
        cachedSpanTextLines = text.split("\n");
    }

    /**
     * Retrieves the cached span text lines, initializing them if needed.
     */
    @Nullable
    public String[] getCachedSpanTextLines() {
        if (cachedSpanTextLines == null && isSpanTextShown) {
            updateCachedSpanText();
        }
        return cachedSpanTextLines;
    }

    /**
     * Converts DP (Density-independent Pixels) to physical pixels.
     *
     * @param value The value in DP.
     * @return The value in pixels.
     */
    protected float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    /**
     * Converts SP (Scale-independent Pixels) to physical pixels.
     *
     * @param value The value in SP.
     * @return The value in pixels.
     */
    protected float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }

    /**
     * Set the listener to be notified when the time span changes.
     *
     * @param listener The listener to set.
     */
    @Override
    public void setOnSpanChangeListener(@NonNull OnSpanChangeListener listener) {
        this.spanChangeListener = listener;
    }

    /**
     * Get the current span change listener.
     *
     * @return The current listener or null if not set.
     */
    @Override
    @Nullable
    public OnSpanChangeListener getOnSpanChangeListener() {
        return spanChangeListener;
    }

    /**
     * Set the listener for granular time and duration changes.
     *
     * @param listener The listener to set.
     */
    @Override
    public void setOnTimeChangeListener(@Nullable OnTimeChangeListener listener) {
        this.timeChangeListener = listener;
    }

    /**
     * Get the current time change listener.
     *
     * @return The current listener or null if not set.
     */
    @Override
    @Nullable
    public OnTimeChangeListener getOnTimeChangeListener() {
        return timeChangeListener;
    }

    /**
     * Set the listener for thumb drag interactions.
     *
     * @param listener The listener to set.
     */
    @Override
    public void setOnDragChangeListener(@Nullable OnDragChangeListener listener) {
        this.dragChangeListener = listener;
    }

    /**
     * Get the current drag change listener.
     *
     * @return The current listener or null if not set.
     */
    @Override
    @Nullable
    public OnDragChangeListener getOnDragChangeListener() {
        return dragChangeListener;
    }

    /**
     * Get the start time of the span in minutes from midnight (0-1439).
     *
     * @return Start time in minutes.
     */
    @Override
    public int getSpanStartInMinutes() {
        return currentStartMinutes;
    }

    /**
     * Set the start time of the span in minutes from midnight (0-1439).
     *
     * @param startMinutes Start time in minutes.
     */
    @Override
    public void setSpanStartInMinutes(int startMinutes) {
        updateSpan(TimeUtils.snapToStep(startMinutes, thumbMinuteStep), currentEndMinutes, true);
    }

    /**
     * Get the end time of the span in minutes from midnight (0-1439).
     *
     * @return End time in minutes.
     */
    @Override
    public int getSpanEndInMinutes() {
        return currentEndMinutes;
    }

    /**
     * Set the end time of the span in minutes from midnight (0-1439).
     *
     * @param endMinutes End time in minutes.
     */
    @Override
    public void setSpanEndInMinutes(int endMinutes) {
        updateSpan(currentStartMinutes, TimeUtils.snapToStep(endMinutes, thumbMinuteStep), false);
    }

    /**
     * Set both start and end times of the span in minutes from midnight.
     *
     * @param startMinutes Start time in minutes.
     * @param endMinutes   End time in minutes.
     */
    @Override
    public void setSpanInMinutes(int startMinutes, int endMinutes) {
        updateSpan(TimeUtils.snapToStep(startMinutes, thumbMinuteStep), TimeUtils.snapToStep(endMinutes, thumbMinuteStep), true);
    }

    /**
     * Updates the internal span state, enforcing duration constraints and optional overnight rules.
     *
     * @param newStart    Proposed start time in minutes.
     * @param newEnd      Proposed end time in minutes.
     * @param movingStart True if the start thumb was moved, false if the end thumb.
     */
    protected void updateSpan(int newStart, int newEnd, boolean movingStart) {
        newStart = (newStart % MINUTES_IN_DAY + MINUTES_IN_DAY) % MINUTES_IN_DAY;
        newEnd = (newEnd % MINUTES_IN_DAY + MINUTES_IN_DAY) % MINUTES_IN_DAY;

        if (!isOvernightSpanAllowed) {
            // Prevent overnight wrap when overnight spans are not permitted
            if (newEnd < newStart) {
                if (movingStart) {
                    newStart = newEnd;
                } else {
                    newEnd = newStart;
                }
            }

            int duration = newEnd - newStart;
            if (duration < minDurationMinutes) {
                if (movingStart) {
                    newStart = Math.max(0, newEnd - minDurationMinutes);
                    if (newEnd - newStart < minDurationMinutes) {
                        newEnd = Math.min(MINUTES_IN_DAY, newStart + minDurationMinutes);
                    }
                } else {
                    newEnd = Math.min(MINUTES_IN_DAY, newStart + minDurationMinutes);
                    if (newEnd - newStart < minDurationMinutes) {
                        newStart = Math.max(0, newEnd - minDurationMinutes);
                    }
                }
            } else if (duration > maxDurationMinutes) {
                if (movingStart) {
                    newStart = newEnd - maxDurationMinutes;
                } else {
                    newEnd = newStart + maxDurationMinutes;
                }
            }
        } else {
            int duration = calculateDuration(newStart, newEnd);

            if (duration < minDurationMinutes) {
                if (movingStart) {
                    newStart = (newEnd - minDurationMinutes + MINUTES_IN_DAY) % MINUTES_IN_DAY;
                } else {
                    newEnd = (newStart + minDurationMinutes) % MINUTES_IN_DAY;
                }
            } else if (duration > maxDurationMinutes) {
                if (movingStart) {
                    newStart = (newEnd - maxDurationMinutes + MINUTES_IN_DAY) % MINUTES_IN_DAY;
                } else {
                    newEnd = (newStart + maxDurationMinutes) % MINUTES_IN_DAY;
                }
            }
        }

        if (this.currentStartMinutes != newStart || this.currentEndMinutes != newEnd) {
            boolean startChanged = this.currentStartMinutes != newStart;
            boolean endChanged = this.currentEndMinutes != newEnd;
            int oldDuration = getDurationInMinutes();

            this.currentStartMinutes = newStart;
            this.currentEndMinutes = newEnd;
            updateCachedSpanText();
            invalidate();
            notifySpanChanged(false);

            if (timeChangeListener != null) {
                if (startChanged) {
                    timeChangeListener.onStartTimeChange(newStart);
                }
                if (endChanged) {
                    timeChangeListener.onEndTimeChange(newEnd);
                }
                if (getDurationInMinutes() != oldDuration) {
                    timeChangeListener.onDurationChange(newStart, newEnd);
                }
            }
        }
    }

    /**
     * Calculates the duration between two time points, accounting for midnight wraps.
     *
     * @param start Start time in minutes.
     * @param end   End time in minutes.
     * @return Duration in minutes.
     */
    protected int calculateDuration(int start, int end) {
        if (end >= start) return end - start;
        return (MINUTES_IN_DAY - start) + end;
    }

    /**
     * Set the start time using a string format (e.g., "HH:mm" or "h:mm a").
     *
     * @param timeString The time string to parse.
     * @throws IllegalArgumentException If the format is invalid.
     */
    @Override
    public void setSpanStartTime(String timeString) throws IllegalArgumentException {
        setSpanStartInMinutes(TimeUtils.convertTimeToMinutes(timeString));
    }

    /**
     * Set the end time using a string format (e.g., "HH:mm" or "h:mm a").
     *
     * @param timeString The time string to parse.
     * @throws IllegalArgumentException If the format is invalid.
     */
    @Override
    public void setSpanEndTime(String timeString) throws IllegalArgumentException {
        setSpanEndInMinutes(TimeUtils.convertTimeToMinutes(timeString));
    }

    /**
     * Set both start and end times using string formats.
     *
     * @param start The start time string.
     * @param end   The end time string.
     * @throws IllegalArgumentException If either format is invalid.
     */
    @Override
    public void setSpanTime(String start, String end) throws IllegalArgumentException {
        setSpanInMinutes(TimeUtils.convertTimeToMinutes(start), TimeUtils.convertTimeToMinutes(end));
    }

    /**
     * Get the total duration of the selected span in minutes.
     *
     * @return Duration in minutes.
     */
    @Override
    public int getDurationInMinutes() {
        return calculateDuration(currentStartMinutes, currentEndMinutes);
    }

    /**
     * Get the minimum allowed duration in minutes.
     *
     * @return Minimum duration.
     */
    @Override
    public int getMinDurationMinutes() {
        return minDurationMinutes;
    }

    /**
     * Set the minimum allowed duration in minutes.
     *
     * @param minutes Minimum duration.
     */
    @Override
    public void setMinDurationMinutes(int minutes) {
        this.minDurationMinutes = Math.max(0, minutes);
        setSpanInMinutes(currentStartMinutes, currentEndMinutes);
    }

    /**
     * Get the maximum allowed duration in minutes.
     *
     * @return Maximum duration.
     */
    @Override
    public int getMaxDurationMinutes() {
        return maxDurationMinutes;
    }

    /**
     * Set the maximum allowed duration in minutes.
     *
     * @param minutes Maximum duration.
     */
    @Override
    public void setMaxDurationMinutes(int minutes) {
        this.maxDurationMinutes = Math.min(MINUTES_IN_DAY, minutes);
        setSpanInMinutes(currentStartMinutes, currentEndMinutes);
    }

    /**
     * Check if the current span spans across midnight (e.g., 10 PM to 2 AM).
     *
     * @return True if the span is overnight.
     */
    @Override
    public boolean isOvernight() {
        return currentEndMinutes < currentStartMinutes;
    }

    /**
     * Check if span spanning across midnight are allowed.
     *
     * @return True if allowed.
     */
    @Override
    public boolean isOvernightSpanAllowed() {
        return isOvernightSpanAllowed;
    }

    /**
     * Enable or disable spans spanning across midnight.
     *
     * @param allowed True to allow overnight spans.
     */
    @Override
    public void setOvernightSpanAllowed(boolean allowed) {
        this.isOvernightSpanAllowed = allowed;
        if (!allowed && isOvernight()) {
            currentEndMinutes = currentStartMinutes;
        }
        invalidate();
    }

    /**
     * Get the snap interval in minutes for the thumbs.
     *
     * @return The minute step.
     */
    @Override
    public int getThumbMinuteStep() {
        return thumbMinuteStep;
    }

    /**
     * Set the snap interval in minutes for the thumbs (e.g., 1, 5, 15, 30).
     *
     * @param thumbMinuteStep The minute step to set.
     */
    @Override
    public void setThumbMinuteStep(int thumbMinuteStep) {
        this.thumbMinuteStep = Math.max(1, thumbMinuteStep);
        setSpanInMinutes(currentStartMinutes, currentEndMinutes);
    }

    /**
     * Set the accent color, which typically updates both span and thumb colors.
     *
     * @param color The accent color.
     */
    @Override
    public void setAccentColor(@ColorInt int color) {
        this.spanColor = color;
        this.thumbFillColor = color;
        invalidate();
    }

    /**
     * Get the current accent color.
     *
     * @return The accent color.
     */
    @Override
    @ColorInt
    public int getAccentColor() {
        return spanColor;
    }

    /**
     * Set the color of the background track.
     *
     * @param color The track color.
     */
    @Override
    public void setTrackColor(@ColorInt int color) {
        this.trackColor = color;
        invalidate();
    }

    /**
     * Get the color of the background track.
     *
     * @return The track color.
     */
    @Override
    @ColorInt
    public int getTrackColor() {
        return trackColor;
    }

    /**
     * Sets the text color for both the span summary text and the tick labels.
     * Shortcut for calling {@link #setSpanTextColor(int)} and {@link #setTickLabelColor(int)}.
     *
     * @param color The color to set.
     */
    @Override
    public void setTextColor(@ColorInt int color) {
        setSpanTextColor(color);
        setTickLabelColor(color);
    }

    /**
     * Check if the span summary text is currently being displayed.
     *
     * @return True if shown.
     */
    @Override
    public boolean isSpanTextShown() {
        return isSpanTextShown;
    }

    /**
     * Toggle the visibility of the span summary text.
     *
     * @param showSpanText True to show, false to hide.
     */
    @Override
    public void showSpanText(boolean showSpanText) {
        this.isSpanTextShown = showSpanText;
        updateCachedSpanText();
        requestLayout();
        invalidate();
    }

    /**
     * Set the color of the span summary text.
     *
     * @param color The text color.
     */
    @Override
    public void setSpanTextColor(@ColorInt int color) {
        this.spanTextColor = color;
        invalidate();
    }

    /**
     * Get the color of the span summary text.
     *
     * @return The text color.
     */
    @Override
    @ColorInt
    public int getSpanTextColor() {
        return spanTextColor;
    }

    /**
     * Set the size of the span summary text.
     *
     * @param size Size in SP.
     */
    @Override
    public void setSpanTextSize(float size) {
        this.spanTextSize = sp(size);
        requestLayout();
        invalidate();
    }

    /**
     * Get the size of the span summary text.
     *
     * @return Size in SP.
     */
    @Override
    public float getSpanTextSize() {
        return spanTextSize;
    }

    /**
     * Set the typeface style for the span summary text (e.g., Typeface.BOLD).
     *
     * @param style The typeface style.
     */
    @Override
    public void setSpanTextStyle(int style) {
        this.spanTextStyle = style;
        requestLayout();
        invalidate();
    }

    /**
     * Get the typeface style of the span summary text.
     *
     * @return The typeface style.
     */
    @Override
    public int getSpanTextStyle() {
        return spanTextStyle;
    }

    /**
     * Set the format string for the span summary text (e.g., "%1$s to %2$s").
     *
     * @param format The format string.
     */
    @Override
    public void setSpanTextFormat(String format) {
        this.spanTextFormat = format != null ? format : DEFAULT_SPAN_TEXT_FORMAT;
        updateCachedSpanText();
        invalidate();
    }

    /**
     * Get the current format string for the span summary text.
     *
     * @return The format string.
     */
    @Override
    public String getSpanTextFormat() {
        return spanTextFormat;
    }

    /**
     * Set the relative position of the span summary text (TOP, BOTTOM, CENTER).
     *
     * @param position The position to set.
     */
    @Override
    public void setSpanTextPosition(@NonNull SpanTextPosition position) {
        this.spanTextPosition = position;
        requestLayout();
        invalidate();
    }

    /**
     * Get the current relative position of the span summary text.
     *
     * @return The current position.
     */
    @Override
    @NonNull
    public SpanTextPosition getSpanTextPosition() {
        return spanTextPosition;
    }

    /**
     * Set the color of the active span track.
     *
     * @param color The span color.
     */
    @Override
    public void setSpanColor(@ColorInt int color) {
        this.spanColor = color;
        invalidate();
    }

    /**
     * Get the color of the active span track.
     *
     * @return The span color.
     */
    @Override
    @ColorInt
    public int getSpanColor() {
        return spanColor;
    }

    /**
     * Set the color of the thumb shadow.
     *
     * @param color The shadow color.
     */
    @Override
    public void setThumbShadowColor(@ColorInt int color) {
        this.thumbShadowColor = color;
        invalidate();
    }

    /**
     * Get the color of the thumb shadow.
     *
     * @return The shadow color.
     */
    @Override
    @ColorInt
    public int getThumbShadowColor() {
        return thumbShadowColor;
    }

    /**
     * Set the horizontal offset of the thumb shadow.
     *
     * @param dx Offset in DP.
     */
    @Override
    public void setThumbShadowDx(float dx) {
        this.thumbShadowDx = dp(dx);
        invalidate();
    }

    /**
     * Get the horizontal offset of the thumb shadow in pixels.
     *
     * @return Offset in pixels.
     */
    @Override
    public float getThumbShadowDx() {
        return thumbShadowDx;
    }

    /**
     * Set the vertical offset of the thumb shadow.
     *
     * @param dy Offset in DP.
     */
    @Override
    public void setThumbShadowDy(float dy) {
        this.thumbShadowDy = dp(dy);
        invalidate();
    }

    /**
     * Get the vertical offset of the thumb shadow in pixels.
     *
     * @return Offset in pixels.
     */
    @Override
    public float getThumbShadowDy() {
        return thumbShadowDy;
    }

    /**
     * Set the elevation (blur radius) of the thumb shadow.
     *
     * @param thumbElevation Elevation in DP.
     */
    @Override
    public void setThumbElevation(float thumbElevation) {
        this.thumbElevation = dp(thumbElevation);
        invalidate();
    }

    /**
     * Get the elevation of the thumb shadow in pixels.
     *
     * @return Elevation in pixels.
     */
    @Override
    public float getThumbElevation() {
        return thumbElevation;
    }

    /**
     * Set all thumb shadow properties at once.
     *
     * @param dxDp           Horizontal offset in DP.
     * @param dyDp           Vertical offset in DP.
     * @param thumbElevation Elevation/Blur in DP.
     * @param color          Shadow color.
     */
    @Override
    public void setThumbShadow(float dxDp, float dyDp, float thumbElevation, @ColorInt int color) {
        this.thumbShadowDx = dp(dxDp);
        this.thumbShadowDy = dp(dyDp);
        this.thumbElevation = dp(thumbElevation);
        this.thumbShadowColor = color;
        invalidate();
    }

    /**
     * Check if a custom drawable is set for the start thumb.
     *
     * @return True if set.
     */
    @Override
    public boolean isStartThumbDrawableSet() {
        return startThumbDrawable != null;
    }

    /**
     * Set a custom drawable for the start thumb.
     *
     * @param drawable The drawable to set.
     */
    @Override
    public void setStartThumbDrawable(@Nullable Drawable drawable) {
        this.startThumbDrawable = drawable;
        invalidate();
    }

    /**
     * Set a custom drawable for the start thumb using a resource ID.
     *
     * @param resId The drawable resource ID.
     */
    @Override
    public void setStartThumbDrawable(@DrawableRes int resId) {
        setStartThumbDrawable(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Get the current custom drawable for the start thumb.
     *
     * @return The drawable or null.
     */
    @Override
    @Nullable
    public Drawable getStartThumbDrawable() {
        return startThumbDrawable;
    }

    /**
     * Check if a custom drawable is set for the end thumb.
     *
     * @return True if set.
     */
    @Override
    public boolean isEndThumbDrawableSet() {
        return endThumbDrawable != null;
    }

    /**
     * Set a custom drawable for the end thumb.
     *
     * @param drawable The drawable to set.
     */
    @Override
    public void setEndThumbDrawable(@Nullable Drawable drawable) {
        this.endThumbDrawable = drawable;
        invalidate();
    }

    /**
     * Set a custom drawable for the end thumb using a resource ID.
     *
     * @param resId The drawable resource ID.
     */
    @Override
    public void setEndThumbDrawable(@DrawableRes int resId) {
        setEndThumbDrawable(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Get the current custom drawable for the end thumb.
     *
     * @return The drawable or null.
     */
    @Override
    @Nullable
    public Drawable getEndThumbDrawable() {
        return endThumbDrawable;
    }

    /**
     * Set custom drawables for both thumbs simultaneously.
     *
     * @param startDrawable The start thumb drawable.
     * @param endDrawable   The end thumb drawable.
     */
    @Override
    public void setThumbDrawables(@Nullable Drawable startDrawable, @Nullable Drawable endDrawable) {
        this.startThumbDrawable = startDrawable;
        this.endThumbDrawable = endDrawable;
        invalidate();
    }

    /**
     * Set custom drawables for both thumbs using resource IDs.
     *
     * @param startResId The start thumb resource ID.
     * @param endResId   The end thumb resource ID.
     */
    @Override
    public void setThumbDrawables(@DrawableRes int startResId, @DrawableRes int endResId) {
        setThumbDrawables(ContextCompat.getDrawable(getContext(), startResId), ContextCompat.getDrawable(getContext(), endResId));
    }

    /**
     * Set the tint color for the start thumb icon.
     *
     * @param color Tint color.
     */
    @Override
    public void setStartThumbDrawableTintColor(@ColorInt int color) {
        this.startThumbDrawableTintColor = color;
        invalidate();
    }

    /**
     * Get the tint color of the start thumb icon.
     *
     * @return Tint color.
     */
    @Override
    @ColorInt
    public int getStartThumbDrawableTintColor() {
        return startThumbDrawableTintColor;
    }

    /**
     * Set the tint color for the end thumb icon.
     *
     * @param color Tint color.
     */
    @Override
    public void setEndThumbDrawableTintColor(@ColorInt int color) {
        this.endThumbDrawableTintColor = color;
        invalidate();
    }

    /**
     * Get the tint color of the end thumb icon.
     *
     * @return Tint color.
     */
    @Override
    @ColorInt
    public int getEndThumbDrawableTintColor() {
        return endThumbDrawableTintColor;
    }

    /**
     * Set the radius of the thumb handles.
     *
     * @param radius Radius in DP.
     */
    @Override
    public void setThumbRadius(float radius) {
        this.thumbRadius = dp(radius);
        requestLayout();
        invalidate();
    }

    /**
     * Get the radius of the thumb handles in pixels.
     *
     * @return Radius in pixels.
     */
    @Override
    public float getThumbRadius() {
        return thumbRadius;
    }

    /**
     * Get the extra touch area padding around the thumbs in pixels.
     *
     * @return Padding in pixels.
     */
    @Override
    public float getThumbTouchRadiusPadding() {
        return thumbTouchRadiusPadding;
    }

    /**
     * Set the extra touch area padding around the thumbs.
     *
     * @param thumbTouchRadiusPadding Padding in DP.
     */
    @Override
    public void setThumbTouchRadiusPadding(float thumbTouchRadiusPadding) {
        this.thumbTouchRadiusPadding = dp(thumbTouchRadiusPadding);
        invalidate();
    }

    /**
     * Set the background fill color of the thumb handles.
     *
     * @param color Fill color.
     */
    @Override
    public void setThumbFillColor(@ColorInt int color) {
        this.thumbFillColor = color;
        invalidate();
    }

    /**
     * Get the background fill color of the thumb handles.
     *
     * @return Fill color.
     */
    @Override
    @ColorInt
    public int getThumbFillColor() {
        return thumbFillColor;
    }

    /**
     * Set the border thickness of the thumb handles.
     *
     * @param width Width in DP.
     */
    @Override
    public void setThumbStrokeWidth(float width) {
        this.thumbStrokeWidth = dp(width);
        invalidate();
    }

    /**
     * Get the border thickness of the thumb handles in pixels.
     *
     * @return Width in pixels.
     */
    @Override
    public float getThumbStrokeWidth() {
        return thumbStrokeWidth;
    }

    /**
     * Set the border color of the thumb handles.
     *
     * @param color Stroke color.
     */
    @Override
    public void setThumbStrokeColor(@ColorInt int color) {
        this.thumbStrokeColor = color;
        invalidate();
    }

    /**
     * Get the border color of the thumb handles.
     *
     * @return Stroke color.
     */
    @Override
    @ColorInt
    public int getThumbStrokeColor() {
        return thumbStrokeColor;
    }

    /**
     * Check if tick marks are currently enabled.
     *
     * @return True if ticks are shown.
     */
    @Override
    public boolean isShowTicks() {
        return showTicks;
    }

    /**
     * Toggle the visibility of all tick marks.
     *
     * @param showTicks True to show.
     */
    @Override
    public void setShowTicks(boolean showTicks) {
        this.showTicks = showTicks;
        requestLayout();
        invalidate();
    }

    /**
     * Check if hour labels are currently enabled.
     *
     * @return True if labels are shown.
     */
    @Override
    public boolean isShowTickLabels() {
        return showTickLabels;
    }

    /**
     * Toggle the visibility of the hour labels (e.g., 12, 14, 16).
     *
     * @param show True to show.
     */
    @Override
    public void setShowTickLabels(boolean show) {
        this.showTickLabels = show;
        requestLayout();
        invalidate();
    }

    /**
     * Get the distance between the track and the tick marks in pixels.
     *
     * @return Distance in pixels.
     */
    @Override
    public float getTickDistanceFromTrack() {
        return tickDistanceFromTrack;
    }

    /**
     * Set the distance between the track and the tick marks.
     *
     * @param distance Distance in DP.
     */
    @Override
    public void setTickDistanceFromTrack(float distance) {
        this.tickDistanceFromTrack = dp(distance);
        requestLayout();
        invalidate();
    }

    /**
     * Get the distance between tick marks and their respective labels in pixels.
     *
     * @return Distance in pixels.
     */
    @Override
    public float getTickLabelDistanceFromTick() {
        return tickLabelDistanceFromTick;
    }

    /**
     * Set the distance between tick marks and their respective labels.
     *
     * @param distance Distance in DP.
     */
    @Override
    public void setTickLabelDistanceFromTick(float distance) {
        this.tickLabelDistanceFromTick = dp(distance);
        requestLayout();
        invalidate();
    }

    /**
     * Set the typeface style for the tick labels (e.g., Typeface.ITALIC).
     *
     * @param style Typeface style.
     */
    @Override
    public void setTickLabelStyle(int style) {
        this.tickLabelStyle = style;
        requestLayout();
        invalidate();
    }

    /**
     * Get the typeface style for the tick labels.
     *
     * @return Typeface style.
     */
    @Override
    public int getTickLabelStyle() {
        return tickLabelStyle;
    }

    /**
     * Set the font size of the tick labels.
     *
     * @param size Size in SP.
     */
    @Override
    public void setTickLabelSize(float size) {
        this.tickLabelSize = sp(size);
        requestLayout();
        invalidate();
    }

    /**
     * Get the font size of the tick labels in pixels.
     *
     * @return Size in pixels.
     */
    @Override
    public float getTickLabelSize() {
        return tickLabelSize;
    }

    /**
     * Check if AM/PM labels are shown in 12-hour mode.
     *
     * @return True if shown.
     */
    @Override
    public boolean isAmPmLabelsShown() {
        return showAmPmLabels;
    }

    /**
     * Toggle the visibility of AM/PM labels (applicable only in 12-hour format).
     *
     * @param show True to show.
     */
    @Override
    public void showAmPmLabels(boolean show) {
        this.showAmPmLabels = show;
        requestLayout();
        invalidate();
    }

    /**
     * Set the color of the hour tick marks.
     *
     * @param color Tick color.
     */
    @Override
    public void setHourTickColor(@ColorInt int color) {
        this.hourTickColor = color;
        invalidate();
    }

    /**
     * Get the color of the hour tick marks.
     *
     * @return Tick color.
     */
    @Override
    @ColorInt
    public int getHourTickColor() {
        return hourTickColor;
    }

    /**
     * Set the color of the minute tick marks (e.g., 15-min intervals).
     *
     * @param color Tick color.
     */
    @Override
    public void setMinuteTickColor(@ColorInt int color) {
        this.minuteTickColor = color;
        invalidate();
    }

    /**
     * Get the color of the minute tick marks.
     *
     * @return Tick color.
     */
    @Override
    @ColorInt
    public int getMinuteTickColor() {
        return minuteTickColor;
    }

    /**
     * Set colors for both hour and minute ticks.
     *
     * @param hourColor   Color for hour ticks.
     * @param minuteColor Color for minute ticks.
     */
    @Override
    public void setTickColors(@ColorInt int hourColor, @ColorInt int minuteColor) {
        this.hourTickColor = hourColor;
        this.minuteTickColor = minuteColor;
        invalidate();
    }

    /**
     * Set the thickness of the hour tick marks.
     *
     * @param width Width in DP.
     */
    @Override
    public void setHourTickWidth(float width) {
        this.hourTickWidth = dp(width);
        invalidate();
    }

    /**
     * Get the thickness of the hour tick marks in pixels.
     *
     * @return Width in pixels.
     */
    @Override
    public float getHourTickWidth() {
        return hourTickWidth;
    }

    /**
     * Set the thickness of the minute tick marks.
     *
     * @param width Width in DP.
     */
    @Override
    public void setMinuteTickWidth(float width) {
        this.minuteTickWidth = dp(width);
        invalidate();
    }

    /**
     * Get the thickness of the minute tick marks in pixels.
     *
     * @return Width in pixels.
     */
    @Override
    public float getMinuteTickWidth() {
        return minuteTickWidth;
    }

    /**
     * Set thickness for both hour and minute ticks simultaneously.
     *
     * @param hourTickWidth   Width for hour ticks in DP.
     * @param minuteTickWidth Width for minute ticks in DP.
     */
    @Override
    public void setTicksWidth(float hourTickWidth, float minuteTickWidth) {
        this.hourTickWidth = dp(hourTickWidth);
        this.minuteTickWidth = dp(minuteTickWidth);
        invalidate();
    }

    /**
     * Set the length of the hour tick marks.
     *
     * @param height Length in DP.
     */
    @Override
    public void setHourTickHeight(float height) {
        this.hourTickHeight = dp(height);
        requestLayout();
        invalidate();
    }

    /**
     * Get the length of the hour tick marks in pixels.
     *
     * @return Length in pixels.
     */
    @Override
    public float getHourTickHeight() {
        return hourTickHeight;
    }

    /**
     * Set the length of the minute tick marks.
     *
     * @param height Length in DP.
     */
    @Override
    public void setMinuteTickHeight(float height) {
        this.minuteTickHeight = dp(height);
        requestLayout();
        invalidate();
    }

    /**
     * Get the length of the minute tick marks in pixels.
     *
     * @return Length in pixels.
     */
    @Override
    public float getMinuteTickHeight() {
        return minuteTickHeight;
    }

    /**
     * Set lengths for both hour and minute ticks simultaneously.
     *
     * @param hourTickHeight   Length for hour ticks in DP.
     * @param minuteTickHeight Length for minute ticks in DP.
     */
    @Override
    public void setTicksHeight(float hourTickHeight, float minuteTickHeight) {
        this.hourTickHeight = dp(hourTickHeight);
        this.minuteTickHeight = dp(minuteTickHeight);
        requestLayout();
        invalidate();
    }

    /**
     * Set the color of the hour labels.
     *
     * @param color Label color.
     */
    @Override
    public void setTickLabelColor(@ColorInt int color) {
        this.tickLabelColor = color;
        invalidate();
    }

    /**
     * Get the color of the hour labels.
     *
     * @return Label color.
     */
    @Override
    @ColorInt
    public int getTickLabelColor() {
        return tickLabelColor;
    }

    /**
     * Set the edge style (cap style) for the tick marks (ROUND or BUTT).
     *
     * @param style Edge style.
     */
    @Override
    public void setTickEdgeStyle(TickEdgeStyle style) {
        this.tickEdgeStyle = style;
        invalidate();
    }

    /**
     * Get the current edge style of the tick marks.
     *
     * @return Edge style.
     */
    @Override
    @NonNull
    public TickEdgeStyle getTickEdgeStyle() {
        return tickEdgeStyle;
    }

    /**
     * Set the thickness of the background track.
     *
     * @param width Width in DP.
     */
    @Override
    public void setTrackWidth(float width) {
        this.trackWidth = dp(width);
        requestLayout();
        invalidate();
    }

    /**
     * Get the thickness of the background track in pixels.
     *
     * @return Width in pixels.
     */
    @Override
    public float getTrackWidth() {
        return trackWidth;
    }

    /**
     * Check if the selector is currently using 24-hour format.
     */
    @Override
    public boolean is24HourFormat() {
        return is24HourFormat;
    }

    /**
     * Toggle between 24-hour and 12-hour time formats.
     *
     * @param is24HourFormat True for 24-hour, false for 12-hour.
     */
    @Override
    public void set24HourFormat(boolean is24HourFormat) {
        this.is24HourFormat = is24HourFormat;
        updateCachedSpanText();
        requestLayout();
        invalidate();
    }

    /**
     * Notifies the listener about a span change event.
     *
     * @param finished True if the user has finished the interaction, false otherwise.
     */
    protected void notifySpanChanged(boolean finished) {
        if (spanChangeListener != null) {
            if (finished) {
                spanChangeListener.onInteractionFinished(currentStartMinutes, currentEndMinutes, isOvernight());
            } else {
                spanChangeListener.onSpanChanged(currentStartMinutes, currentEndMinutes, isOvernight());
            }
        }
    }

    /**
     * Returns the formatted string representation of the currently selected time span.
     *
     * @return Formatted span text.
     */
    protected String getFormattedSpanText() {
        String am = getContext().getString(R.string.tss_am);
        String pm = getContext().getString(R.string.tss_pm);
        String startStr = TimeUtils.formatDisplayTime(currentStartMinutes, is24HourFormat, am, pm);
        String endStr = TimeUtils.formatDisplayTime(currentEndMinutes, is24HourFormat, am, pm);
        return String.format(spanTextFormat, startStr, endStr);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(KEY_START_MINUTES, currentStartMinutes);
        bundle.putInt(KEY_END_MINUTES, currentEndMinutes);
        bundle.putInt(KEY_MIN_DURATION, minDurationMinutes);
        bundle.putInt(KEY_MAX_DURATION, maxDurationMinutes);
        bundle.putInt(KEY_THUMB_STEP, thumbMinuteStep);
        bundle.putBoolean(KEY_IS_24_HOUR, is24HourFormat);
        bundle.putBoolean(KEY_ALLOW_OVERNIGHT, isOvernightSpanAllowed);
        bundle.putBoolean(KEY_SHOW_TICKS, showTicks);
        bundle.putBoolean(KEY_SHOW_TICK_LABELS, showTickLabels);
        bundle.putBoolean(KEY_SHOW_AM_PM_LABELS, showAmPmLabels);
        bundle.putBoolean(KEY_SHOW_SPAN_TEXT, isSpanTextShown);
        bundle.putString(KEY_SPAN_TEXT_FORMAT, spanTextFormat);
        bundle.putString(KEY_SPAN_TEXT_POSITION, spanTextPosition.name());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            currentStartMinutes = bundle.getInt(KEY_START_MINUTES, DEFAULT_START_MINUTES);
            currentEndMinutes = bundle.getInt(KEY_END_MINUTES, DEFAULT_END_MINUTES);
            minDurationMinutes = bundle.getInt(KEY_MIN_DURATION, minDurationMinutes);
            maxDurationMinutes = bundle.getInt(KEY_MAX_DURATION, maxDurationMinutes);
            thumbMinuteStep = bundle.getInt(KEY_THUMB_STEP, thumbMinuteStep);
            is24HourFormat = bundle.getBoolean(KEY_IS_24_HOUR, is24HourFormat);
            isOvernightSpanAllowed = bundle.getBoolean(KEY_ALLOW_OVERNIGHT, isOvernightSpanAllowed);
            showTicks = bundle.getBoolean(KEY_SHOW_TICKS, showTicks);
            showTickLabels = bundle.getBoolean(KEY_SHOW_TICK_LABELS, showTickLabels);
            showAmPmLabels = bundle.getBoolean(KEY_SHOW_AM_PM_LABELS, showAmPmLabels);
            isSpanTextShown = bundle.getBoolean(KEY_SHOW_SPAN_TEXT, isSpanTextShown);
            spanTextFormat = bundle.getString(KEY_SPAN_TEXT_FORMAT, spanTextFormat);
            String posName = bundle.getString(KEY_SPAN_TEXT_POSITION);
            if (posName != null) {
                try {
                    spanTextPosition = SpanTextPosition.valueOf(posName);
                } catch (IllegalArgumentException ignored) {
                }
            }
            updateCachedSpanText();
            super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_STATE));
            invalidate();
            requestLayout();
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
