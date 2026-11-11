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
import java.util.Objects;

/**
 * Immutable domain model representing a selected time span.
 */
public final class TimeSpan implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int startMinutes;
    private final int endMinutes;
    private final boolean overnight;

    public TimeSpan(int startMinutes, int endMinutes) {
        this(normalize(startMinutes), normalize(endMinutes), normalize(endMinutes) < normalize(startMinutes));
    }

    public TimeSpan(int startMinutes, int endMinutes, boolean isOvernight) {
        this.startMinutes = normalize(startMinutes);
        this.endMinutes = normalize(endMinutes);
        this.overnight = isOvernight;
    }

    private static int normalize(int minutes) {
        return (minutes % TimeUtils.MINUTES_IN_DAY + TimeUtils.MINUTES_IN_DAY) % TimeUtils.MINUTES_IN_DAY;
    }

    /**
     * Start time in minutes from midnight (0-1439).
     */
    public int getStartMinutes() {
        return startMinutes;
    }

    /**
     * End time in minutes from midnight (0-1439).
     */
    public int getEndMinutes() {
        return endMinutes;
    }

    /**
     * True if the time span crosses midnight.
     */
    public boolean isOvernight() {
        return overnight;
    }

    /**
     * Total duration of the span in minutes.
     */
    public int getDurationMinutes() {
        if (endMinutes >= startMinutes && !overnight) {
            return endMinutes - startMinutes;
        }
        return (TimeUtils.MINUTES_IN_DAY - startMinutes) + endMinutes;
    }

    /**
     * Formats the time span into a human-readable display string.
     *
     * @param is24HourFormat True for 24h format, false for 12h format.
     * @param amString       Localized AM string.
     * @param pmString       Localized PM string.
     * @param formatPattern  Pattern with two string arguments (e.g., "%1$s - %2$s").
     * @return Formatted span string.
     */
    @NonNull
    public String format(boolean is24HourFormat, @NonNull String amString, @NonNull String pmString, @NonNull String formatPattern) {
        String startStr = TimeUtils.formatDisplayTime(startMinutes, is24HourFormat, amString, pmString);
        String endStr = TimeUtils.formatDisplayTime(endMinutes, is24HourFormat, amString, pmString);
        return String.format(formatPattern, startStr, endStr);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSpan)) return false;
        TimeSpan timeSpan = (TimeSpan) o;
        return startMinutes == timeSpan.startMinutes &&
                endMinutes == timeSpan.endMinutes &&
                overnight == timeSpan.overnight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startMinutes, endMinutes, overnight);
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeSpan{" +
                "startMinutes=" + startMinutes +
                ", endMinutes=" + endMinutes +
                ", overnight=" + overnight +
                ", duration=" + getDurationMinutes() + " mins" +
                '}';
    }
}
