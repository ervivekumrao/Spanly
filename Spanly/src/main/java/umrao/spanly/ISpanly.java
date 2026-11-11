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

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface defining the common functionality for all Time Span Selectors.
 */
public interface ISpanly {

    /**
     * Total minutes in a 24-hour day.
     */
    int MINUTES_IN_DAY = 24 * 60;

    /**
     * Default start time (09:00).
     */
    int DEFAULT_START_MINUTES = 9 * 60;

    /**
     * Default end time (17:00).
     */
    int DEFAULT_END_MINUTES = 17 * 60;

    /**
     * Default snap interval.
     */
    int DEFAULT_THUMB_MINUTE_STEP = 15;

    /**
     * Default display format for the span text.
     */
    String DEFAULT_SPAN_TEXT_FORMAT = "%1$s\n%2$s";

    @ColorInt
    int DEFAULT_TRACK_COLOR = Color.DKGRAY;
    @ColorInt
    int DEFAULT_SPAN_COLOR = Color.YELLOW;
    @ColorInt
    int DEFAULT_SPAN_TEXT_COLOR = Color.BLACK;
    @ColorInt
    int DEFAULT_THUMB_FILL_COLOR = Color.YELLOW;
    @ColorInt
    int DEFAULT_THUMB_STROKE_COLOR = Color.DKGRAY;
    @ColorInt
    int DEFAULT_THUMB_SHADOW_COLOR = Color.BLACK;
    @ColorInt
    int DEFAULT_THUMB_DRAWABLE_TINT_COLOR = Color.TRANSPARENT;
    @ColorInt
    int DEFAULT_TICK_COLOR = Color.BLACK;
    @ColorInt
    int DEFAULT_TICK_LABEL_COLOR = Color.BLACK;

    enum SpanTextPosition {TOP, BOTTOM, CENTER}

    enum TickEdgeStyle {ROUND, BUTT}

    enum Thumb {START, END, SPAN, NONE}

    interface OnSpanChangeListener {
        /**
         * Called when the selected time span changes.
         *
         * @param startMinutes The start time in total minutes from midnight (0-1439).
         * @param endMinutes   The end time in total minutes from midnight (0-1439).
         * @param isOvernight  True if the span spans across midnight.
         */
        void onSpanChanged(int startMinutes, int endMinutes, boolean isOvernight);

        /**
         * Called when the user finishes interacting with the selector.
         *
         * @param startMinutes The start time in total minutes from midnight.
         * @param endMinutes   The end time in total minutes from midnight.
         * @param isOvernight  True if the span spans across midnight.
         */
        void onInteractionFinished(int startMinutes, int endMinutes, boolean isOvernight);
    }

    /**
     * Listener for more granular time and duration changes.
     */
    interface OnTimeChangeListener {
        /**
         * Called when the start time changes.
         *
         * @param startTimeMinutes The new start time in minutes from midnight.
         */
        void onStartTimeChange(int startTimeMinutes);

        /**
         * Called when the end time changes.
         *
         * @param endTimeMinutes The new end time in minutes from midnight.
         */
        void onEndTimeChange(int endTimeMinutes);

        /**
         * Called when the duration of the span changes.
         *
         * @param startTimeMinutes The new start time in minutes from midnight.
         * @param endTimeMinutes   The new end time in minutes from midnight.
         */
        void onDurationChange(int startTimeMinutes, int endTimeMinutes);
    }

    /**
     * Listener for thumb drag interactions.
     */
    interface OnDragChangeListener {
        /**
         * Called when the user starts dragging a thumb.
         *
         * @param thumb The thumb being dragged (START, END, or SPAN).
         * @return True to allow the drag, false to disallow it.
         */
        boolean onDragStart(@NonNull Thumb thumb);

        /**
         * Called when the user stops dragging a thumb.
         *
         * @param thumb The thumb that was being dragged.
         */
        void onDragStop(@NonNull Thumb thumb);
    }

    /**
     * Set the listener to be notified when the time span changes.
     *
     * @param listener The listener to set.
     */
    void setOnSpanChangeListener(@NonNull OnSpanChangeListener listener);

    /**
     * Get the current span change listener.
     *
     * @return The current listener or null if not set.
     */
    @Nullable
    OnSpanChangeListener getOnSpanChangeListener();

    /**
     * Set the listener for granular time and duration changes.
     *
     * @param listener The listener to set.
     */
    void setOnTimeChangeListener(@Nullable OnTimeChangeListener listener);

    /**
     * Get the current time change listener.
     *
     * @return The current listener or null if not set.
     */
    @Nullable
    OnTimeChangeListener getOnTimeChangeListener();

    /**
     * Set the listener for thumb drag interactions.
     *
     * @param listener The listener to set.
     */
    void setOnDragChangeListener(@Nullable OnDragChangeListener listener);

    /**
     * Get the current drag change listener.
     *
     * @return The current listener or null if not set.
     */
    @Nullable
    OnDragChangeListener getOnDragChangeListener();

    /**
     * Get the start time of the span in minutes from midnight (0-1439).
     *
     * @return Start time in minutes.
     */
    int getSpanStartInMinutes();

    /**
     * Set the start time of the span in minutes from midnight (0-1439).
     *
     * @param startMinutes Start time in minutes.
     */
    void setSpanStartInMinutes(int startMinutes);

    /**
     * Get the end time of the span in minutes from midnight (0-1439).
     *
     * @return End time in minutes.
     */
    int getSpanEndInMinutes();

    /**
     * Set the end time of the span in minutes from midnight (0-1439).
     *
     * @param endMinutes End time in minutes.
     */
    void setSpanEndInMinutes(int endMinutes);

    /**
     * Set both start and end times of the span in minutes from midnight.
     *
     * @param startMinutes Start time in minutes.
     * @param endMinutes   End time in minutes.
     */
    void setSpanInMinutes(int startMinutes, int endMinutes);

    /**
     * Get the currently selected time span as an immutable domain model.
     *
     * @return Current {@link TimeSpan}.
     */
    @NonNull
    default TimeSpan getTimeSpan() {
        return new TimeSpan(getSpanStartInMinutes(), getSpanEndInMinutes(), isOvernight());
    }

    /**
     * Sets the time span using a {@link TimeSpan} model.
     *
     * @param timeSpan The time span to set.
     */
    default void setTimeSpan(@NonNull TimeSpan timeSpan) {
        setSpanInMinutes(timeSpan.getStartMinutes(), timeSpan.getEndMinutes());
    }

    /**
     * Set the start time using a string format (e.g., "HH:mm" or "h:mm a").
     *
     * @param timeString The time string to parse.
     * @throws IllegalArgumentException If the format is invalid.
     */
    void setSpanStartTime(String timeString) throws IllegalArgumentException;

    /**
     * Set the end time using a string format (e.g., "HH:mm" or "h:mm a").
     *
     * @param timeString The time string to parse.
     * @throws IllegalArgumentException If the format is invalid.
     */
    void setSpanEndTime(String timeString) throws IllegalArgumentException;

    /**
     * Set both start and end times using string formats.
     *
     * @param start The start time string.
     * @param end   The end time string.
     * @throws IllegalArgumentException If either format is invalid.
     */
    void setSpanTime(String start, String end) throws IllegalArgumentException;

    /**
     * Get the total duration of the selected span in minutes.
     *
     * @return Duration in minutes.
     */
    int getDurationInMinutes();

    /**
     * Get the minimum allowed duration in minutes.
     *
     * @return Minimum duration.
     */
    int getMinDurationMinutes();

    /**
     * Set the minimum allowed duration in minutes.
     *
     * @param minutes Minimum duration.
     */
    void setMinDurationMinutes(int minutes);

    /**
     * Get the maximum allowed duration in minutes.
     *
     * @return Maximum duration.
     */
    int getMaxDurationMinutes();

    /**
     * Set the maximum allowed duration in minutes.
     *
     * @param minutes Maximum duration.
     */
    void setMaxDurationMinutes(int minutes);

    /**
     * Check if the current span spans across midnight (e.g., 10 PM to 2 AM).
     *
     * @return True if the span is overnight.
     */
    boolean isOvernight();

    /**
     * Check if span spanning across midnight are allowed.
     *
     * @return True if allowed.
     */
    boolean isOvernightSpanAllowed();

    /**
     * Enable or disable spans spanning across midnight.
     *
     * @param allowed True to allow overnight spans.
     */
    void setOvernightSpanAllowed(boolean allowed);

    /**
     * Get the snap interval in minutes for the thumbs.
     *
     * @return The minute step.
     */
    int getThumbMinuteStep();

    /**
     * Set the snap interval in minutes for the thumbs (e.g., 1, 5, 15, 30).
     *
     * @param thumbMinuteStep The minute step to set.
     */
    void setThumbMinuteStep(int thumbMinuteStep);

    /**
     * Set the accent color, which typically updates both span and thumb colors.
     *
     * @param color The accent color.
     */
    void setAccentColor(@ColorInt int color);

    /**
     * Get the current accent color.
     *
     * @return The accent color.
     */
    @ColorInt
    int getAccentColor();

    /**
     * Set the color of the background track.
     *
     * @param color The track color.
     */
    void setTrackColor(@ColorInt int color);

    /**
     * Get the color of the background track.
     *
     * @return The track color.
     */
    @ColorInt
    int getTrackColor();

    /**
     * Sets the text color for both the span summary text and the tick labels.
     * Shortcut for calling {@link #setSpanTextColor(int)} and {@link #setTickLabelColor(int)}.
     *
     * @param color The color to set.
     */
    void setTextColor(@ColorInt int color);

    /**
     * Check if the span summary text is currently being displayed.
     *
     * @return True if shown.
     */
    boolean isSpanTextShown();

    /**
     * Toggle the visibility of the span summary text.
     *
     * @param showSpanText True to show, false to hide.
     */
    void showSpanText(boolean showSpanText);

    /**
     * Set the color of the span summary text.
     *
     * @param color The text color.
     */
    void setSpanTextColor(@ColorInt int color);

    /**
     * Get the color of the span summary text.
     *
     * @return The text color.
     */
    @ColorInt
    int getSpanTextColor();

    /**
     * Set the size of the span summary text.
     *
     * @param size Size in SP.
     */
    void setSpanTextSize(float size);

    /**
     * Get the size of the span summary text.
     *
     * @return Size in SP.
     */
    float getSpanTextSize();

    /**
     * Set the typeface style for the span summary text (e.g., Typeface.BOLD).
     *
     * @param style The typeface style.
     */
    void setSpanTextStyle(int style);

    /**
     * Get the typeface style of the span summary text.
     *
     * @return The typeface style.
     */
    int getSpanTextStyle();

    /**
     * Set the format string for the span summary text (e.g., "%1$s to %2$s").
     *
     * @param format The format string.
     */
    void setSpanTextFormat(String format);

    /**
     * Get the current format string for the span summary text.
     *
     * @return The format string.
     */
    String getSpanTextFormat();

    /**
     * Set the relative position of the span summary text (TOP, BOTTOM, CENTER).
     *
     * @param position The position to set.
     */
    void setSpanTextPosition(@NonNull SpanTextPosition position);

    /**
     * Get the current relative position of the span summary text.
     *
     * @return The current position.
     */
    @NonNull
    SpanTextPosition getSpanTextPosition();

    /**
     * Set the color of the active span track.
     *
     * @param color The span color.
     */
    void setSpanColor(@ColorInt int color);

    /**
     * Get the color of the active span track.
     *
     * @return The span color.
     */
    @ColorInt
    int getSpanColor();

    /**
     * Set the color of the thumb shadow.
     *
     * @param color The shadow color.
     */
    void setThumbShadowColor(@ColorInt int color);

    /**
     * Get the color of the thumb shadow.
     *
     * @return The shadow color.
     */
    @ColorInt
    int getThumbShadowColor();

    /**
     * Set the horizontal offset of the thumb shadow.
     *
     * @param dx Offset in pixels.
     */
    void setThumbShadowDx(float dx);

    /**
     * Get the horizontal offset of the thumb shadow.
     *
     * @return Offset in pixels.
     */
    float getThumbShadowDx();

    /**
     * Set the vertical offset of the thumb shadow.
     *
     * @param dy Offset in pixels.
     */
    void setThumbShadowDy(float dy);

    /**
     * Get the vertical offset of the thumb shadow.
     *
     * @return Offset in pixels.
     */
    float getThumbShadowDy();

    /**
     * Set the elevation (blur radius) of the thumb shadow.
     *
     * @param thumbElevation Elevation in pixels.
     */
    void setThumbElevation(float thumbElevation);

    /**
     * Get the elevation of the thumb shadow.
     *
     * @return Elevation in pixels.
     */
    float getThumbElevation();

    /**
     * Set all thumb shadow properties at once.
     *
     * @param dxDp           Horizontal offset in DP.
     * @param dyDp           Vertical offset in DP.
     * @param thumbElevation Elevation/Blur in pixels.
     * @param color          Shadow color.
     */
    void setThumbShadow(float dxDp, float dyDp, float thumbElevation, @ColorInt int color);

    /**
     * Check if a custom drawable is set for the start thumb.
     *
     * @return True if set.
     */
    boolean isStartThumbDrawableSet();

    /**
     * Set a custom drawable for the start thumb.
     *
     * @param drawable The drawable to set.
     */
    void setStartThumbDrawable(@Nullable Drawable drawable);

    /**
     * Set a custom drawable for the start thumb using a resource ID.
     *
     * @param resId The drawable resource ID.
     */
    void setStartThumbDrawable(@DrawableRes int resId);

    /**
     * Get the current custom drawable for the start thumb.
     *
     * @return The drawable or null.
     */
    @Nullable
    Drawable getStartThumbDrawable();

    /**
     * Check if a custom drawable is set for the end thumb.
     *
     * @return True if set.
     */
    boolean isEndThumbDrawableSet();

    /**
     * Set a custom drawable for the end thumb.
     *
     * @param drawable The drawable to set.
     */
    void setEndThumbDrawable(@Nullable Drawable drawable);

    /**
     * Set a custom drawable for the end thumb using a resource ID.
     *
     * @param resId The drawable resource ID.
     */
    void setEndThumbDrawable(@DrawableRes int resId);

    /**
     * Get the current custom drawable for the end thumb.
     *
     * @return The drawable or null.
     */
    @Nullable
    Drawable getEndThumbDrawable();

    /**
     * Set custom drawables for both thumbs simultaneously.
     *
     * @param startDrawable The start thumb drawable.
     * @param endDrawable   The end thumb drawable.
     */
    void setThumbDrawables(@Nullable Drawable startDrawable, @Nullable Drawable endDrawable);

    /**
     * Set custom drawables for both thumbs using resource IDs.
     *
     * @param startResId The start thumb resource ID.
     * @param endResId   The end thumb resource ID.
     */
    void setThumbDrawables(@DrawableRes int startResId, @DrawableRes int endResId);

    /**
     * Set the tint color for the start thumb icon.
     *
     * @param color Tint color.
     */
    void setStartThumbDrawableTintColor(@ColorInt int color);

    /**
     * Get the tint color of the start thumb icon.
     *
     * @return Tint color.
     */
    @ColorInt
    int getStartThumbDrawableTintColor();

    /**
     * Set the tint color for the end thumb icon.
     *
     * @param color Tint color.
     */
    void setEndThumbDrawableTintColor(@ColorInt int color);

    /**
     * Get the tint color of the end thumb icon.
     *
     * @return Tint color.
     */
    @ColorInt
    int getEndThumbDrawableTintColor();

    /**
     * Set the radius of the thumb handles.
     *
     * @param radius Radius in pixels.
     */
    void setThumbRadius(float radius);

    /**
     * Get the radius of the thumb handles.
     *
     * @return Radius in pixels.
     */
    float getThumbRadius();

    /**
     * Get the extra touch area padding around the thumbs.
     *
     * @return Padding in pixels.
     */
    float getThumbTouchRadiusPadding();

    /**
     * Set the extra touch area padding around the thumbs.
     *
     * @param thumbTouchRadiusPadding Padding in pixels.
     */
    void setThumbTouchRadiusPadding(float thumbTouchRadiusPadding);

    /**
     * Set the background fill color of the thumb handles.
     *
     * @param color Fill color.
     */
    void setThumbFillColor(@ColorInt int color);

    /**
     * Get the background fill color of the thumb handles.
     *
     * @return Fill color.
     */
    @ColorInt
    int getThumbFillColor();

    /**
     * Set the border thickness of the thumb handles.
     *
     * @param width Width in pixels.
     */
    void setThumbStrokeWidth(float width);

    /**
     * Get the border thickness of the thumb handles.
     *
     * @return Width in pixels.
     */
    float getThumbStrokeWidth();

    /**
     * Set the border color of the thumb handles.
     *
     * @param color Stroke color.
     */
    void setThumbStrokeColor(@ColorInt int color);

    /**
     * Get the border color of the thumb handles.
     *
     * @return Stroke color.
     */
    @ColorInt
    int getThumbStrokeColor();

    /**
     * Check if tick marks are currently enabled.
     *
     * @return True if ticks are shown.
     */
    boolean isShowTicks();

    /**
     * Toggle the visibility of all tick marks.
     *
     * @param showTicks True to show.
     */
    void setShowTicks(boolean showTicks);

    /**
     * Check if hour labels are currently enabled.
     *
     * @return True if labels are shown.
     */
    boolean isShowTickLabels();

    /**
     * Toggle the visibility of the hour labels (e.g., 12, 14, 16).
     *
     * @param show True to show.
     */
    void setShowTickLabels(boolean show);

    /**
     * Get the distance between the track and the tick marks.
     *
     * @return Distance in pixels.
     */
    float getTickDistanceFromTrack();

    /**
     * Set the distance between the track and the tick marks.
     *
     * @param distance Distance in pixels.
     */
    void setTickDistanceFromTrack(float distance);

    /**
     * Get the distance between tick marks and their respective labels.
     *
     * @return Distance in pixels.
     */
    float getTickLabelDistanceFromTick();

    /**
     * Set the distance between tick marks and their respective labels.
     *
     * @param distance Distance in pixels.
     */
    void setTickLabelDistanceFromTick(float distance);

    /**
     * Set the typeface style for the tick labels (e.g., Typeface.ITALIC).
     *
     * @param style Typeface style.
     */
    void setTickLabelStyle(int style);

    /**
     * Get the typeface style for the tick labels.
     *
     * @return Typeface style.
     */
    int getTickLabelStyle();

    /**
     * Set the font size of the tick labels.
     *
     * @param size Size in SP.
     */
    void setTickLabelSize(float size);

    /**
     * Get the font size of the tick labels.
     *
     * @return Size in SP.
     */
    float getTickLabelSize();

    /**
     * Check if AM/PM labels are shown in 12-hour mode.
     *
     * @return True if shown.
     */
    boolean isAmPmLabelsShown();

    /**
     * Toggle the visibility of AM/PM labels (applicable only in 12-hour format).
     *
     * @param show True to show.
     */
    void showAmPmLabels(boolean show);

    /**
     * Set the color of the hour tick marks.
     *
     * @param color Tick color.
     */
    void setHourTickColor(@ColorInt int color);

    /**
     * Get the color of the hour tick marks.
     *
     * @return Tick color.
     */
    @ColorInt
    int getHourTickColor();

    /**
     * Set the color of the minute tick marks (e.g., 15-min intervals).
     *
     * @param color Tick color.
     */
    void setMinuteTickColor(@ColorInt int color);

    /**
     * Get the color of the minute tick marks.
     *
     * @return Tick color.
     */
    @ColorInt
    int getMinuteTickColor();

    /**
     * Set colors for both hour and minute ticks.
     *
     * @param hourColor   Color for hour ticks.
     * @param minuteColor Color for minute ticks.
     */
    void setTickColors(@ColorInt int hourColor, @ColorInt int minuteColor);

    /**
     * Set the thickness of the hour tick marks.
     *
     * @param width Width in pixels.
     */
    void setHourTickWidth(float width);

    /**
     * Get the thickness of the hour tick marks.
     *
     * @return Width in pixels.
     */
    float getHourTickWidth();

    /**
     * Set the thickness of the minute tick marks.
     *
     * @param width Width in pixels.
     */
    void setMinuteTickWidth(float width);

    /**
     * Get the thickness of the minute tick marks.
     *
     * @return Width in pixels.
     */
    float getMinuteTickWidth();

    /**
     * Set thickness for both hour and minute ticks simultaneously.
     *
     * @param hourTickWidth   Width for hour ticks.
     * @param minuteTickWidth Width for minute ticks.
     */
    void setTicksWidth(float hourTickWidth, float minuteTickWidth);

    /**
     * Set the length of the hour tick marks.
     *
     * @param height Length in pixels.
     */
    void setHourTickHeight(float height);

    /**
     * Get the length of the hour tick marks.
     *
     * @return Length in pixels.
     */
    float getHourTickHeight();

    /**
     * Set the length of the minute tick marks.
     *
     * @param height Length in pixels.
     */
    void setMinuteTickHeight(float height);

    /**
     * Get the length of the minute tick marks.
     *
     * @return Length in pixels.
     */
    float getMinuteTickHeight();

    /**
     * Set lengths for both hour and minute ticks simultaneously.
     *
     * @param hourTickHeight   Length for hour ticks.
     * @param minuteTickHeight Length for minute ticks.
     */
    void setTicksHeight(float hourTickHeight, float minuteTickHeight);

    /**
     * Set the color of the hour labels.
     *
     * @param color Label color.
     */
    void setTickLabelColor(@ColorInt int color);

    /**
     * Get the color of the hour labels.
     *
     * @return Label color.
     */
    @ColorInt
    int getTickLabelColor();

    /**
     * Set the edge style (cap style) for the tick marks (ROUND or BUTT).
     *
     * @param style Edge style.
     */
    void setTickEdgeStyle(TickEdgeStyle style);

    /**
     * Get the current edge style of the tick marks.
     *
     * @return Edge style.
     */
    @NonNull
    TickEdgeStyle getTickEdgeStyle();

    /**
     * Set the thickness of the background track.
     *
     * @param width Width in pixels.
     */
    void setTrackWidth(float width);

    /**
     * Get the thickness of the background track.
     *
     * @return Width in pixels.
     */
    float getTrackWidth();

    /**
     * Check if the selector is currently using 24-hour format.
     *
     * @return True if 24-hour format.
     */
    boolean is24HourFormat();

    /**
     * Toggle between 24-hour and 12-hour time formats.
     *
     * @param is24HourFormat True for 24-hour, false for 12-hour.
     */
    void set24HourFormat(boolean is24HourFormat);

    /**
     * Set whether sliding along the active span can drag the entire time window.
     *
     * @param allowed True to allow dragging the whole span, false to disable.
     */
    void setSpanDraggingAllowed(boolean allowed);

    /**
     * Check if whole-span dragging is allowed.
     *
     * @return True if allowed.
     */
    boolean isSpanDraggingAllowed();

    /**
     * Shifts the entire time span by the given minute offset, preserving duration.
     *
     * @param minuteDelta Positive to shift forward in time, negative to shift backward.
     */
    void shiftSpan(int minuteDelta);

    /**
     * Set custom gradient colors for the active span track.
     *
     * @param colors Array of colors (at least 2), or null to use a solid color.
     */
    void setSpanGradientColors(@Nullable @ColorInt int[] colors);

    /**
     * Get the custom gradient colors for the active span track.
     *
     * @return Array of colors or null.
     */
    @Nullable
    @ColorInt
    int[] getSpanGradientColors();

    /**
     * Adds a blocked or unavailable interval rendered on the track.
     *
     * @param startMinutes Start time in minutes from midnight (0-1439).
     * @param endMinutes   End time in minutes from midnight (0-1439).
     * @param color        Color used to render the blocked segment.
     */
    void addBlockedInterval(int startMinutes, int endMinutes, @ColorInt int color);

    /**
     * Clears all blocked intervals.
     */
    void clearBlockedIntervals();

    /**
     * Returns the list of configured blocked intervals.
     *
     * @return List of blocked intervals.
     */
    @NonNull
    java.util.List<BlockedInterval> getBlockedIntervals();
}
