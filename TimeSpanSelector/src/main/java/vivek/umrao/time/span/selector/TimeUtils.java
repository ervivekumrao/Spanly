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

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for time-related calculations and formatting.
 */
public class TimeUtils {

    /**
     * Total minutes in a single 24-hour day.
     */
    public static final int MINUTES_IN_DAY = 24 * 60;

    private static final Pattern TIME_24H_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):([0-5]\\d)$");
    private static final Pattern TIME_12H_PATTERN = Pattern.compile("^(0?[1-9]|1[0-2]):([0-5]\\d)\\s*([AaPp][Mm])$");

    /**
     * Formats a total number of minutes into a human-readable string.
     *
     * @param totalMinutes   The minutes to format.
     * @param is24HourFormat True for 24-hour, false for 12-hour.
     * @param amString       Localized string for AM.
     * @param pmString       Localized string for PM.
     * @return A formatted time string (e.g., "14:30" or "2:30 PM").
     */
    public static String formatDisplayTime(int totalMinutes, boolean is24HourFormat, String amString, String pmString) {
        int minutesInDay = (totalMinutes % MINUTES_IN_DAY + MINUTES_IN_DAY) % MINUTES_IN_DAY;

        int hours = minutesInDay / 60;
        int minutes = minutesInDay % 60;

        if (is24HourFormat) {
            return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
        } else {
            String amPm = (hours < 12) ? amString : pmString;
            int displayHour = hours % 12;
            if (displayHour == 0) displayHour = 12;
            return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minutes, amPm);
        }
    }

    /**
     * Converts a time string ("HH:mm" or "h:mm a") into total minutes from midnight.
     *
     * @param timeString The time string to parse (e.g., "09:45", "14:30", "2:30 PM", "9:45 AM").
     * @return Total minutes (0-1439).
     * @throws IllegalArgumentException If the format is invalid.
     */
    public static int convertTimeToMinutes(@NonNull String timeString) throws IllegalArgumentException {
        String trimmed = timeString.trim();

        Matcher matcher24 = TIME_24H_PATTERN.matcher(trimmed);
        if (matcher24.matches()) {
            String g1 = matcher24.group(1);
            String g2 = matcher24.group(2);
            if (g1 != null && g2 != null) {
                int hours = Integer.parseInt(g1);
                int minutes = Integer.parseInt(g2);
                return hours * 60 + minutes;
            }
        }

        Matcher matcher12 = TIME_12H_PATTERN.matcher(trimmed);
        if (matcher12.matches()) {
            String g1 = matcher12.group(1);
            String g2 = matcher12.group(2);
            String g3 = matcher12.group(3);
            if (g1 != null && g2 != null && g3 != null) {
                int hours = Integer.parseInt(g1);
                int minutes = Integer.parseInt(g2);
                String amPm = g3.toUpperCase(Locale.US);
                if (hours == 12) {
                    hours = "AM".equals(amPm) ? 0 : 12;
                } else if ("PM".equals(amPm)) {
                    hours += 12;
                }
                return hours * 60 + minutes;
            }
        }

        throw new IllegalArgumentException("Invalid time format. Expected 'HH:mm' (24h) or 'h:mm a' (12h). Received: " + timeString);
    }

    /**
     * Snaps the given minutes to the nearest step interval.
     *
     * @param minutes The minutes to snap.
     * @param step    The interval (e.g., 15 for 15-minute intervals).
     * @return The snapped minute value.
     */
    public static int snapToStep(int minutes, int step) {
        if (step <= 0) return minutes;
        return Math.round((float) minutes / step) * step;
    }
}
