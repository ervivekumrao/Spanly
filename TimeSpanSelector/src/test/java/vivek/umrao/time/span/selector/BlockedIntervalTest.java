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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class BlockedIntervalTest {

    @Test
    public void testDaytimeBlockedInterval() {
        BlockedInterval interval = new BlockedInterval(12 * 60, 13 * 60, Color.RED);
        assertEquals(720, interval.getStartMinutes());
        assertEquals(780, interval.getEndMinutes());
        assertEquals(Color.RED, interval.getColor());
        assertFalse(interval.isOvernight());
        assertEquals(60, interval.getDurationMinutes());
    }

    @Test
    public void testOvernightBlockedInterval() {
        BlockedInterval interval = new BlockedInterval(23 * 60, 2 * 60, Color.GRAY);
        assertEquals(1380, interval.getStartMinutes());
        assertEquals(120, interval.getEndMinutes());
        assertTrue(interval.isOvernight());
        assertEquals(180, interval.getDurationMinutes());
    }

    @Test
    public void testEqualsAndHashCode() {
        BlockedInterval b1 = new BlockedInterval(100, 200, Color.RED);
        BlockedInterval b2 = new BlockedInterval(100, 200, Color.RED);
        BlockedInterval b3 = new BlockedInterval(100, 200, Color.BLUE);
        BlockedInterval b4 = new BlockedInterval(100, 300, Color.RED);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
        assertNotEquals(b1, b4);
        assertNotNull(b1);
        assertNotEquals(b1, "other");
    }

    @Test
    public void testToString() {
        BlockedInterval interval = new BlockedInterval(60, 120, Color.BLACK);
        assertTrue(interval.toString().contains("startMinutes=60"));
        assertTrue(interval.toString().contains("endMinutes=120"));
        assertTrue(interval.toString().contains("duration=60 mins"));
    }
}
