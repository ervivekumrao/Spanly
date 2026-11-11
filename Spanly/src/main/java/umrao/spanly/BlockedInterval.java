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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a restricted or blocked time interval (e.g. booked slots, unavailable hours).
 */
public final class BlockedInterval implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int startMinutes;
    private final int endMinutes;
    @ColorInt
    private final int color;

    public BlockedInterval(int startMinutes, int endMinutes, @ColorInt int color) {
        this.startMinutes = (startMinutes % TimeUtils.MINUTES_IN_DAY + TimeUtils.MINUTES_IN_DAY) % TimeUtils.MINUTES_IN_DAY;
        this.endMinutes = (endMinutes % TimeUtils.MINUTES_IN_DAY + TimeUtils.MINUTES_IN_DAY) % TimeUtils.MINUTES_IN_DAY;
        this.color = color;
    }

    public int getStartMinutes() {
        return startMinutes;
    }

    public int getEndMinutes() {
        return endMinutes;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    public boolean isOvernight() {
        return endMinutes < startMinutes;
    }

    public int getDurationMinutes() {
        if (endMinutes >= startMinutes) {
            return endMinutes - startMinutes;
        }
        return (TimeUtils.MINUTES_IN_DAY - startMinutes) + endMinutes;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockedInterval)) return false;
        BlockedInterval that = (BlockedInterval) o;
        return startMinutes == that.startMinutes &&
                endMinutes == that.endMinutes &&
                color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startMinutes, endMinutes, color);
    }

    @NonNull
    @Override
    public String toString() {
        return "BlockedInterval{" +
                "startMinutes=" + startMinutes +
                ", endMinutes=" + endMinutes +
                ", color=" + color +
                ", duration=" + getDurationMinutes() + " mins" +
                '}';
    }
}
