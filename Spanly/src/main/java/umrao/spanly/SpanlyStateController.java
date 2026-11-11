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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Decoupled controller that manages time span calculations, boundary rules, and duration validation
 * independently of Android View rendering APIs.
 */
public class SpanlyStateController implements Serializable {

    private static final long serialVersionUID = 1L;

    public interface StateChangeListener {
        /**
         * Triggered when internal time span state changes.
         *
         * @param startChanged    True if start minutes changed.
         * @param endChanged      True if end minutes changed.
         * @param durationChanged True if duration changed.
         */
        void onStateChanged(boolean startChanged, boolean endChanged, boolean durationChanged);
    }

    private int startMinutes = ISpanly.DEFAULT_START_MINUTES;
    private int endMinutes = ISpanly.DEFAULT_END_MINUTES;
    private int minDurationMinutes = 0;
    private int maxDurationMinutes = ISpanly.MINUTES_IN_DAY;
    private int thumbMinuteStep = ISpanly.DEFAULT_THUMB_MINUTE_STEP;
    private boolean isOvernightSpanAllowed = true;
    private boolean is24HourFormat = true;

    @Nullable
    private transient StateChangeListener changeListener;

    public SpanlyStateController() {
    }

    public void setStateChangeListener(@Nullable StateChangeListener listener) {
        this.changeListener = listener;
    }

    public int getStartMinutes() {
        return startMinutes;
    }

    public void setStartMinutes(int startMinutes) {
        updateSpan(TimeUtils.snapToStep(startMinutes, thumbMinuteStep), endMinutes, true);
    }

    public int getEndMinutes() {
        return endMinutes;
    }

    public void setEndMinutes(int endMinutes) {
        updateSpan(startMinutes, TimeUtils.snapToStep(endMinutes, thumbMinuteStep), false);
    }

    public void setSpanInMinutes(int startMinutes, int endMinutes) {
        updateSpan(TimeUtils.snapToStep(startMinutes, thumbMinuteStep), TimeUtils.snapToStep(endMinutes, thumbMinuteStep), true);
    }

    public int getMinDurationMinutes() {
        return minDurationMinutes;
    }

    public void setMinDurationMinutes(int minDurationMinutes) {
        this.minDurationMinutes = Math.max(0, minDurationMinutes);
        updateSpan(startMinutes, endMinutes, true);
    }

    public int getMaxDurationMinutes() {
        return maxDurationMinutes;
    }

    public void setMaxDurationMinutes(int maxDurationMinutes) {
        this.maxDurationMinutes = Math.min(ISpanly.MINUTES_IN_DAY, maxDurationMinutes);
        updateSpan(startMinutes, endMinutes, true);
    }

    public int getThumbMinuteStep() {
        return thumbMinuteStep;
    }

    public void setThumbMinuteStep(int thumbMinuteStep) {
        this.thumbMinuteStep = Math.max(1, thumbMinuteStep);
        updateSpan(startMinutes, endMinutes, true);
    }

    public boolean isOvernightSpanAllowed() {
        return isOvernightSpanAllowed;
    }

    public void setOvernightSpanAllowed(boolean allowed) {
        this.isOvernightSpanAllowed = allowed;
        if (!allowed && isOvernight()) {
            updateSpan(startMinutes, startMinutes, true);
        }
    }

    public boolean is24HourFormat() {
        return is24HourFormat;
    }

    public void set24HourFormat(boolean is24HourFormat) {
        this.is24HourFormat = is24HourFormat;
    }

    public boolean isOvernight() {
        return endMinutes < startMinutes;
    }

    public int getDurationInMinutes() {
        return calculateDuration(startMinutes, endMinutes);
    }

    public int calculateDuration(int start, int end) {
        if (end >= start) return end - start;
        return (ISpanly.MINUTES_IN_DAY - start) + end;
    }

    public void setStartTimeString(@NonNull String timeString) throws IllegalArgumentException {
        setStartMinutes(TimeUtils.convertTimeToMinutes(timeString));
    }

    public void setEndTimeString(@NonNull String timeString) throws IllegalArgumentException {
        setEndMinutes(TimeUtils.convertTimeToMinutes(timeString));
    }

    public void setTimeString(@NonNull String start, @NonNull String end) throws IllegalArgumentException {
        setSpanInMinutes(TimeUtils.convertTimeToMinutes(start), TimeUtils.convertTimeToMinutes(end));
    }

    public void shiftSpan(int minuteDelta) {
        if (minuteDelta == 0) return;
        int duration = getDurationInMinutes();
        int newStart = (startMinutes + minuteDelta + ISpanly.MINUTES_IN_DAY) % ISpanly.MINUTES_IN_DAY;
        int newEnd = (endMinutes + minuteDelta + ISpanly.MINUTES_IN_DAY) % ISpanly.MINUTES_IN_DAY;

        if (!isOvernightSpanAllowed) {
            if (newStart + duration > ISpanly.MINUTES_IN_DAY) {
                newStart = ISpanly.MINUTES_IN_DAY - duration;
                newEnd = ISpanly.MINUTES_IN_DAY;
            } else if (newStart < 0) {
                newStart = 0;
                newEnd = duration;
            } else {
                newEnd = newStart + duration;
            }
        }

        if (this.startMinutes != newStart || this.endMinutes != newEnd) {
            boolean startChanged = this.startMinutes != newStart;
            boolean endChanged = this.endMinutes != newEnd;
            int oldDuration = getDurationInMinutes();

            this.startMinutes = newStart;
            this.endMinutes = newEnd;

            if (changeListener != null) {
                changeListener.onStateChanged(startChanged, endChanged, getDurationInMinutes() != oldDuration);
            }
        }
    }

    public void updateSpan(int newStart, int newEnd, boolean movingStart) {
        newStart = (newStart % ISpanly.MINUTES_IN_DAY + ISpanly.MINUTES_IN_DAY) % ISpanly.MINUTES_IN_DAY;
        newEnd = (newEnd % ISpanly.MINUTES_IN_DAY + ISpanly.MINUTES_IN_DAY) % ISpanly.MINUTES_IN_DAY;

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
                        newEnd = Math.min(ISpanly.MINUTES_IN_DAY, newStart + minDurationMinutes);
                    }
                } else {
                    newEnd = Math.min(ISpanly.MINUTES_IN_DAY, newStart + minDurationMinutes);
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
                    newStart = (newEnd - minDurationMinutes + ISpanly.MINUTES_IN_DAY) % ISpanly.MINUTES_IN_DAY;
                } else {
                    newEnd = (newStart + minDurationMinutes) % ISpanly.MINUTES_IN_DAY;
                }
            } else if (duration > maxDurationMinutes) {
                if (movingStart) {
                    newStart = (newEnd - maxDurationMinutes + ISpanly.MINUTES_IN_DAY) % ISpanly.MINUTES_IN_DAY;
                } else {
                    newEnd = (newStart + maxDurationMinutes) % ISpanly.MINUTES_IN_DAY;
                }
            }
        }

        if (this.startMinutes != newStart || this.endMinutes != newEnd) {
            boolean startChanged = this.startMinutes != newStart;
            boolean endChanged = this.endMinutes != newEnd;
            int oldDuration = getDurationInMinutes();

            this.startMinutes = newStart;
            this.endMinutes = newEnd;

            if (changeListener != null) {
                changeListener.onStateChanged(startChanged, endChanged, getDurationInMinutes() != oldDuration);
            }
        }
    }

    public TimeSpan toTimeSpan() {
        return new TimeSpan(startMinutes, endMinutes, isOvernight());
    }
}
