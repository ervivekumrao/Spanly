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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TimeSpanTest {

    @Test
    public void testDaytimeSpanDuration() {
        TimeSpan span = new TimeSpan(9 * 60, 17 * 60); // 09:00 to 17:00
        assertEquals(540, span.getStartMinutes());
        assertEquals(1020, span.getEndMinutes());
        assertFalse(span.isOvernight());
        assertEquals(480, span.getDurationMinutes()); // 8 hours = 480 mins
    }

    @Test
    public void testOvernightSpanDuration() {
        TimeSpan span = new TimeSpan(22 * 60, 6 * 60); // 22:00 to 06:00
        assertEquals(1320, span.getStartMinutes());
        assertEquals(360, span.getEndMinutes());
        assertTrue(span.isOvernight());
        assertEquals(480, span.getDurationMinutes()); // 2h before midnight + 6h after = 480 mins
    }

    @Test
    public void testZeroDuration() {
        TimeSpan span = new TimeSpan(600, 600, false);
        assertEquals(0, span.getDurationMinutes());
        assertFalse(span.isOvernight());
    }

    @Test
    public void testWrapAroundMinuteNormalization() {
        TimeSpan span = new TimeSpan(-60, 1440 + 120);
        assertEquals(1380, span.getStartMinutes()); // 23:00
        assertEquals(120, span.getEndMinutes());    // 02:00
        assertTrue(span.isOvernight());
    }

    @Test
    public void testFormatting() {
        TimeSpan span = new TimeSpan(540, 1020);
        String formatted24h = span.format(true, "AM", "PM", "%1$s - %2$s");
        assertEquals("09:00 - 17:00", formatted24h);

        String formatted12h = span.format(false, "AM", "PM", "%1$s to %2$s");
        assertEquals("9:00 AM to 5:00 PM", formatted12h);
    }

    @Test
    public void testEqualsAndHashCode() {
        TimeSpan s1 = new TimeSpan(100, 200, false);
        TimeSpan s2 = new TimeSpan(100, 200, false);
        TimeSpan s3 = new TimeSpan(100, 200, true);
        TimeSpan s4 = new TimeSpan(100, 300, false);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
        assertNotEquals(s1, s4);
        assertNotEquals(s1, null);
        assertNotEquals(s1, "other");
    }

    @Test
    public void testToString() {
        TimeSpan span = new TimeSpan(60, 120, false);
        assertTrue(span.toString().contains("startMinutes=60"));
        assertTrue(span.toString().contains("endMinutes=120"));
        assertTrue(span.toString().contains("duration=60 mins"));
    }
}
