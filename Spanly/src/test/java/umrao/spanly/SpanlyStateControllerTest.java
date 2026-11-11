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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class SpanlyStateControllerTest {

    private SpanlyStateController controller;
    private boolean stateChangedCalled;

    @Before
    public void setUp() {
        controller = new SpanlyStateController();
        stateChangedCalled = false;
        controller.setStateChangeListener((startChanged, endChanged, durationChanged) -> stateChangedCalled = true);
    }

    @Test
    public void testDefaultState() {
        assertEquals(ISpanly.DEFAULT_START_MINUTES, controller.getStartMinutes());
        assertEquals(ISpanly.DEFAULT_END_MINUTES, controller.getEndMinutes());
        assertTrue(controller.is24HourFormat());
        assertTrue(controller.isOvernightSpanAllowed());
        assertEquals(ISpanly.DEFAULT_THUMB_MINUTE_STEP, controller.getThumbMinuteStep());
        assertEquals(480, controller.getDurationInMinutes());
        assertFalse(controller.isOvernight());
    }

    @Test
    public void testSetStartAndEndMinutes() {
        controller.setStartMinutes(600);
        assertEquals(600, controller.getStartMinutes());
        assertTrue(stateChangedCalled);

        controller.setEndMinutes(1200);
        assertEquals(1200, controller.getEndMinutes());
        assertEquals(600, controller.getDurationInMinutes());
    }

    @Test
    public void testOvernightSpan() {
        controller.setSpanInMinutes(1320, 120); // 10 PM to 2 AM
        assertTrue(controller.isOvernight());
        assertEquals(240, controller.getDurationInMinutes());
    }

    @Test
    public void testDisallowOvernight() {
        controller.setOvernightSpanAllowed(false);
        assertFalse(controller.isOvernightSpanAllowed());

        controller.setSpanInMinutes(1320, 120);
        assertFalse(controller.isOvernight());
    }

    @Test
    public void testShiftSpan() {
        controller.setSpanInMinutes(600, 720); // 10:00 to 12:00 (120 mins)
        controller.shiftSpan(60); // Shift by +1 hr
        assertEquals(660, controller.getStartMinutes());
        assertEquals(780, controller.getEndMinutes());
        assertEquals(120, controller.getDurationInMinutes());
    }

    @Test
    public void testMinMaxDurationConstraints() {
        controller.setMinDurationMinutes(60);
        controller.setMaxDurationMinutes(300);

        controller.setSpanInMinutes(600, 615); // Try 15 min duration
        assertEquals(60, controller.getDurationInMinutes()); // Clamped to min 60

        controller.setSpanInMinutes(600, 1000); // Try 400 min duration
        assertEquals(300, controller.getDurationInMinutes()); // Clamped to max 300
    }

    @Test
    public void testToTimeSpan() {
        controller.setSpanInMinutes(540, 1020);
        TimeSpan span = controller.toTimeSpan();
        assertNotNull(span);
        assertEquals(540, span.getStartMinutes());
        assertEquals(1020, span.getEndMinutes());
        assertFalse(span.isOvernight());
    }

    @Test
    public void testTimeStringParsers() {
        controller.setStartTimeString("08:30");
        assertEquals(510, controller.getStartMinutes());

        controller.setEndTimeString("17:45");
        assertEquals(1065, controller.getEndMinutes());

        controller.setTimeString("09:00", "18:00");
        assertEquals(540, controller.getStartMinutes());
        assertEquals(1080, controller.getEndMinutes());
    }
}
