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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppUnitTest {

    @Test
    public void testAppTimeSpanFormattingLogic() {
        TimeSpan span = new TimeSpan(540, 1020, false);
        String formatted = span.format(false, "AM", "PM", "%1$s - %2$s");
        assertNotNull(formatted);
        assertEquals("9:00 AM - 5:00 PM", formatted);
        assertEquals(480, span.getDurationMinutes());
    }

    @Test
    public void testAppOvernightSpanLogic() {
        TimeSpan span = new TimeSpan(1320, 120, true);
        assertTrue(span.isOvernight());
        assertEquals(240, span.getDurationMinutes());
    }
}
