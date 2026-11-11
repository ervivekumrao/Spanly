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
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class TimeUtilsTest {

    @Test
    public void testFormatDisplayTime24h() {
        assertEquals("09:30", TimeUtils.formatDisplayTime(570, true, "AM", "PM"));
        assertEquals("21:45", TimeUtils.formatDisplayTime(1305, true, "AM", "PM"));
        assertEquals("00:00", TimeUtils.formatDisplayTime(0, true, "AM", "PM"));
        assertEquals("23:59", TimeUtils.formatDisplayTime(1439, true, "AM", "PM"));
        assertEquals("00:00", TimeUtils.formatDisplayTime(1440, true, "AM", "PM")); // Wrap around
        assertEquals("12:00", TimeUtils.formatDisplayTime(720, true, "AM", "PM"));
        assertEquals("00:00", TimeUtils.formatDisplayTime(-1440, true, "AM", "PM")); // Negative wrap
    }

    @Test
    public void testFormatDisplayTime12h() {
        assertEquals("9:30 AM", TimeUtils.formatDisplayTime(570, false, "AM", "PM"));
        assertEquals("9:45 PM", TimeUtils.formatDisplayTime(1305, false, "AM", "PM"));
        assertEquals("12:00 AM", TimeUtils.formatDisplayTime(0, false, "AM", "PM"));
        assertEquals("12:00 PM", TimeUtils.formatDisplayTime(720, false, "AM", "PM"));
        assertEquals("11:59 PM", TimeUtils.formatDisplayTime(1439, false, "AM", "PM"));
        assertEquals("12:00 AM", TimeUtils.formatDisplayTime(1440, false, "AM", "PM"));
    }

    @Test
    public void testConvertTimeToMinutes() {
        assertEquals(570, TimeUtils.convertTimeToMinutes("09:30"));
        assertEquals(1305, TimeUtils.convertTimeToMinutes("21:45"));
        assertEquals(0, TimeUtils.convertTimeToMinutes("00:00"));
        assertEquals(1439, TimeUtils.convertTimeToMinutes("23:59"));
        assertEquals(720, TimeUtils.convertTimeToMinutes("12:00"));

        // 12-hour format support
        assertEquals(570, TimeUtils.convertTimeToMinutes("9:30 AM"));
        assertEquals(570, TimeUtils.convertTimeToMinutes("09:30 AM"));
        assertEquals(1305, TimeUtils.convertTimeToMinutes("9:45 PM"));
        assertEquals(0, TimeUtils.convertTimeToMinutes("12:00 AM"));
        assertEquals(720, TimeUtils.convertTimeToMinutes("12:00 PM"));
    }

    @Test
    public void testConvertTimeToMinutesInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.convertTimeToMinutes("9:30"));
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.convertTimeToMinutes("24:00"));
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.convertTimeToMinutes("23:60"));
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.convertTimeToMinutes("abc"));
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.convertTimeToMinutes(""));
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.convertTimeToMinutes("12:3"));
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.convertTimeToMinutes("12:345"));
    }

    @Test
    public void testSnapToStep() {
        assertEquals(15, TimeUtils.snapToStep(10, 15));
        assertEquals(0, TimeUtils.snapToStep(7, 15));
        assertEquals(30, TimeUtils.snapToStep(25, 15));
        assertEquals(60, TimeUtils.snapToStep(60, 15));
        assertEquals(45, TimeUtils.snapToStep(45, 0)); // No step
        assertEquals(45, TimeUtils.snapToStep(45, -1)); // Negative step
        assertEquals(10, TimeUtils.snapToStep(10, 1)); // Step of 1
    }
}
