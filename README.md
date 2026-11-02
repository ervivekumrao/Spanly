# Spanly

A highly customizable, high-performance, elegant Time Span Selector library for Android, featuring both **Circular** (Radial) and **Linear** designs. Perfect for scheduling, alarm settings, shift planning, and time-duration selection.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Features
- 🕒 **Two Layouts**: Circular and Linear selectors.
- 🌓 **Overnight Support**: Seamlessly handle spans that cross midnight.
- 🎨 **Fully Customizable**: Colors, widths, tick marks, labels, and thumb drawables.
- 📏 **Step Intervals**: Snap to 1, 5, 15, or 30-minute intervals.
- 🕰️ **24/12 Hour Modes**: Supports both formats with optional AM/PM labels.
- 📱 **Interactive**: Smooth touch controls with customizable touch padding.
- 📝 **Span Text**: Display the selected span with custom formatting and positioning.

---

## Installation

Add the JitPack repository to your root `build.gradle` (or `settings.gradle`):

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}
```

Add the dependency to your app's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.vivekumrao:time-span-selector:0.0.6-alpha'
}
```

---

## Theme Awareness & Dark Mode
The library is designed to be **theme-aware**. By default, it uses Material Design theme attributes to resolve colors, ensuring it looks great in both Light and Dark modes without manual configuration.

- **Track Color**: Resolves to `?android:attr/textColorSecondaryNoDisable`.
- **Span & Thumb Fill**: Resolves to `?attr/colorPrimary`.
- **Text & Ticks**: Resolves to `?android:attr/textColorPrimary`.
- **Shadows**: Resolves to `?android:attr/shadowColor`.

To override these, you can either set specific attributes in XML or call the corresponding setter methods programmatically.

---

## Usage

### 1. Circular Time Span Selector
The circular selector is ideal for a classic clock-like interface.

```xml
<vivek.umrao.time.span.selector.CircularTimeSpanSelector
    android:id="@+id/circularSelector"
    android:layout_width="300dp"
    android:layout_height="300dp"
    app:tss_trackColor="#E0E0E0"
    app:tss_spanColor="#FF4081"
    app:tss_is24HourFormat="false"
    app:tss_spanTextPosition="center" />
```

### 2. Linear Time Span Selector
The linear selector provides a sleek horizontal slider for time selection.

```xml
<vivek.umrao.time.span.selector.LinearTimeSpanSelector
    android:id="@+id/linearSelector"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:tss_spanColor="#3F51B5"
    app:tss_spanTextPosition="bottom" />
```

---

## API Reference & Attributes

### 1. Time & Span Configuration
| Property | XML Attribute          | Getter / Setter | Default           | Description                                                             |
| :--- |:-----------------------| :--- |:------------------|:------------------------------------------------------------------------|
| **Start Minutes** | tss_spanStartInMinutes | getSpanStartInMinutes() / setSpanStartInMinutes(int) | 540               | Start time in minutes from midnight (0-1439).                           |
| **End Minutes** | tss_spanEndInMinutes   | getSpanEndInMinutes() / setSpanEndInMinutes(int) | 1020              | End time in minutes from midnight (0-1439).                             |
| **Start Time** | tss_spanStartTime      | setSpanStartTime(String) | "09:00"           | Initial start time (e.g., "08:30").                                     |
| **End Time** | tss_spanEndTime        | setSpanEndTime(String) | "17:00"           | Initial end time (e.g., "17:00").                                       |
| **Minute Step** | tss_thumbMinuteStep    | getThumbMinuteStep() / setThumbMinuteStep(int) | 15                | Snap interval (e.g., 1, 5, 15, 30).                                     |
| **Overnight** | tss_allowOvernightSpan | isOvernightSpanAllowed() / setOvernightSpanAllowed(boolean) | true(C), false(L)             | Allow span to cross midnight. No effect in case of Linear.              |
| **24-Hour** | tss_is24HourFormat     | is24HourFormat() / set24HourFormat(boolean) | true              | Toggle 24h vs 12h display.                                              |
| **Min Duration** | tss_minDurationMinutes | getMinDurationMinutes() / setMinDurationMinutes(int) | 0                 | Minimum allowed span in minutes.                                        |
| **Max Duration** | tss_maxDurationMinutes | getMaxDurationMinutes() / setMaxDurationMinutes(int) | 1440              | Maximum allowed span in minutes.                                        |
| **Duration** | -                      | getDurationInMinutes() | -                 | Returns current duration in minutes.                                    |
| **Is Overnight** | -                      | isOvernight() | true(C), false(L) | Returns true if current span crosses midnight. Always false for Linear. |

### 2. Track & Span Appearance
| Property | XML Attribute | Getter / Setter | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Track Color** | tss_trackColor | getTrackColor() / setTrackColor(int) | ?android:attr/textColorSecondaryNoDisable | Color of the background track. |
| **Span Color** | tss_spanColor | getSpanColor() / setSpanColor(int) | ?attr/colorPrimary | Color of the active span. |
| **Track Width** | tss_trackWidth | getTrackWidth() / setTrackWidth(float) | 12dp (Circular), 8dp (Linear) | Thickness of the track. |
| **Accent Color** | - | getAccentColor() / setAccentColor(int) | ?attr/colorPrimary | Sets both Span and Thumb Fill colors. |

### 3. Thumb Customization
| Property | XML Attribute | Getter / Setter | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Radius** | tss_thumbRadius | getThumbRadius() / setThumbRadius(float) | 15dp | Size of the handle. |
| **Fill Color** | tss_thumbFillColor | getThumbFillColor() / setThumbFillColor(int) | ?attr/colorPrimary | Color inside the thumb. |
| **Stroke Color** | tss_thumbStrokeColor | getThumbStrokeColor() / setThumbStrokeColor(int) | ?attr/strokeColor | Thumb border color. |
| **Stroke Width** | tss_thumbStrokeWidth | getThumbStrokeWidth() / setThumbStrokeWidth(float) | 0dp | Thumb border thickness. |
| **Touch Padding**| tss_thumbTouchRadiusPadding | getThumbTouchRadiusPadding() / setThumbTouchRadiusPadding(f) | 10dp (C), 17dp (L) | Extra touch area around thumb. |
| **Elevation** | tss_thumbElevation | getThumbElevation() / setThumbElevation(float) | 2dp | Shadow size. |
| **Shadow Color**| tss_thumbShadowColor | getThumbShadowColor() / setThumbShadowColor(int) | ?android:attr/shadowColor | Color of the thumb shadow. |
| **Shadow DX** | tss_thumbShadowDx | getThumbShadowDx() / setThumbShadowDx(float) | 0dp | Shadow horizontal offset. |
| **Shadow DY** | tss_thumbShadowDy | getThumbShadowDy() / setThumbShadowDy(float) | 1dp | Shadow vertical offset. |
| **Start Icon** | tss_startThumbDrawable | getStartThumbDrawable() / setStartThumbDrawable(Drawable) | null | Custom icon for start thumb. |
| **End Icon** | tss_endThumbDrawable | getEndThumbDrawable() / setEndThumbDrawable(Drawable) | null | Custom icon for end thumb. |
| **Start Tint** | tss_startThumbDrawableTintColor | getStartThumbDrawableTintColor() / setStartThumbDrawableTintColor(int) | ?android:attr/thumbTint | Tint for start icon. |
| **End Tint** | tss_endThumbDrawableTintColor | getEndThumbDrawableTintColor() / setEndThumbDrawableTintColor(int) | ?android:attr/thumbTint | Tint for end icon. |

### 4. Ticks & Labels
| Property | XML Attribute | Getter / Setter | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Show Ticks** | tss_showTicks | isShowTicks() / setShowTicks(boolean) | true | Toggle all tick marks. |
| **Show Labels** | tss_showTickLabels | isShowTickLabels() / setShowTickLabels(boolean) | true | Toggle hour labels. |
| **AM/PM Labels** | tss_showAmPmLabels | isAmPmLabelsShown() / showAmPmLabels(boolean) | false | Show AM/PM in 12h mode. |
| **Hour Tick Color**| tss_hourTickColor | getHourTickColor() / setHourTickColor(int) | ?android:attr/textColorPrimary | Color of hour ticks. |
| **Min Tick Color** | tss_minuteTickColor | getMinuteTickColor() / setMinuteTickColor(int) | ?android:attr/textColorPrimary | Color of 15-min ticks. |
| **Hour Tick H** | tss_hourTickHeight | getHourTickHeight() / setHourTickHeight(float) | 8dp | Height of hour ticks. |
| **Min Tick H** | tss_minuteTickHeight | getMinuteTickHeight() / setMinuteTickHeight(float) | 4dp | Height of 15-min ticks. |
| **Hour Tick W** | tss_hourTickWidth | getHourTickWidth() / setHourTickWidth(float) | 2dp (C), 1.5dp (L) | Thickness of hour ticks. |
| **Min Tick W** | tss_minuteTickWidth | getMinuteTickWidth() / setMinuteTickWidth(float) | 1dp | Thickness of 15-min ticks. |
| **Tick Distance** | tss_tickDistanceFromTrack | getTickDistanceFromTrack() / setTickDistanceFromTrack(f) | 0dp | Gap between track and ticks. |
| **Tick Style** | tss_tickEdgeStyle | getTickEdgeStyle() / setTickEdgeStyle(TickEdgeStyle) | BUTT | ROUND or BUTT caps. |
| **Label Size** | tss_tickLabelSize | getTickLabelSize() / setTickLabelSize(float) | 12sp | Font size of labels. |
| **Label Color** | tss_tickLabelColor | getTickLabelColor() / setTickLabelColor(int) | ?android:attr/textColorPrimary | Color of hour labels. |
| **Label Style** | tss_tickLabelStyle | getTickLabelStyle() / setTickLabelStyle(int) | normal | normal, bold, italic, bold_italic. |
| **Label Distance**| tss_tickLabelDistanceFromTick | getTickLabelDistanceFromTick() / setTickLabelDistanceFromTick(f) | 20dp (C), 0dp (L) | Gap between tick and label. |

### 5. Span Summary Text
| Property | XML Attribute | Getter / Setter | Default | Description                             |
| :--- | :--- | :--- | :--- |:----------------------------------------|
| **Show Summary** | tss_showSpanText | isSpanTextShown() / showSpanText(boolean) | true | Toggle summary text.                    |
| **Text Color** | tss_spanTextColor | getSpanTextColor() / setSpanTextColor(int) | ?android:attr/textColorPrimary | Color of summary text.                  |
| **Text Size** | tss_spanTextSize | getSpanTextSize() / setSpanTextSize(float) | 12sp (C), 14sp (L) | Font size of summary.                   |
| **Text Format** | tss_spanTextFormat | getSpanTextFormat() / setSpanTextFormat(String) | "%1$s\n%2$s" | E.g. "%1$s - %2$s".                     |
| **Text Style** | tss_spanTextStyle | getSpanTextStyle() / setSpanTextStyle(int) | normal | normal, bold, italic, bold_italic.      |
| **Text Position** | tss_spanTextPosition | getSpanTextPosition() / setSpanTextPosition(Enum) | CENTER (C), BOTTOM (L) | TOP, BOTTOM, CENTER.                    |
| **Universal Color**| tss_textColor | setTextColor(int) | - | Color for both summary text and Labels. |

> **Note on Positioning**: 
> - In **Circular** mode: `CENTER` places text inside center of the ring, `TOP`/`BOTTOM` places it above/below outside the ring.
> - In **Linear** mode: `BOTTOM` and `CENTER` both place the text below the tick labels to prevent overlapping. `TOP` places it above the track.

---

## Programmatic Usage

### 1. Setting Span
```java
// Set using minutes (0-1439)
selector.setSpanInMinutes(480, 1020); // 08:00 AM to 05:00 PM

// Set using Strings
selector.setSpanStartTime("08:30");
selector.setSpanEndTime("18:45");
selector.setSpanTime("08:30", "18:45");
```

### 2. Visual Customization
```java
selector.setTrackWidth(12f);           // in dp
selector.setTrackColor(Color.BLACK);
selector.setSpanColor(Color.RED);
selector.setAccentColor(Color.GREEN);   // Sets both span and thumb color

selector.setThumbRadius(18f);           // in dp
selector.setThumbStrokeWidth(2f);
selector.setThumbElevation(8f);

selector.set24HourFormat(false);
selector.showAmPmLabels(true);
selector.setShowTicks(true);
selector.setShowTickLabels(true);

selector.setTextColor(Color.BLUE);      // Sets both summary and label colors
selector.setSpanTextSize(16f);         // in sp
selector.setSpanTextFormat("%1$s to %2$s");
```

### 3. Multi-parameter Setters
For convenience, several batch setters are available:
```java
// Update both tick colors
selector.setTickColors(Color.BLACK, Color.DKGRAY);

// Update both tick widths (in dp)
selector.setTicksWidth(2.5f, 1.5f);

// Update both tick heights (in dp)
selector.setTicksHeight(12f, 6f);

// Set all thumb shadow properties at once
selector.setThumbShadow(2f, 2f, 5f, Color.BLACK); // dx, dy, elevation, color

// Set both thumb drawables
selector.setThumbDrawables(R.drawable.ic_start, R.drawable.ic_end);
```

### 4. Querying Current State
```java
int start = selector.getSpanStartInMinutes();
int end = selector.getSpanEndInMinutes();
int duration = selector.getDurationInMinutes();
boolean overnight = selector.isOvernight();

// Check if custom drawables are active
boolean hasStartIcon = selector.isStartThumbDrawableSet();
boolean hasEndIcon = selector.isEndThumbDrawableSet();

// Retrieve visual properties
float currentRadius = selector.getThumbRadius();
int currentSpanColor = selector.getSpanColor();
```

### 5. Event Handling

#### Basic Span Listener
```java
selector.setOnSpanChangeListener(new TimeSpanSelector.OnSpanChangeListener() {
    @Override
    public void onSpanChanged(int startMinutes, int endMinutes, boolean isOvernight) {
        // Real-time updates as user drags (0-1439 minutes from midnight)
    }

    @Override
    public void onInteractionFinished(int startMinutes, int endMinutes, boolean isOvernight) {
        // Final selection on touch release
    }
});
```

#### Granular Time Listener
```java
selector.setOnTimeChangeListener(new TimeSpanSelector.OnTimeChangeListener() {
    @Override
    public void onStartTimeChange(int startTimeMinutes) {
        // Called when only start time changes
    }

    @Override
    public void onEndTimeChange(int endTimeMinutes) {
        // Called when only end time changes
    }

    @Override
    public void onDurationChange(int startTimeMinutes, int endTimeMinutes) {
        // Called when total duration changes
    }
});
```

#### Drag Interaction Listener
```java
selector.setOnDragChangeListener(new TimeSpanSelector.OnDragChangeListener() {
    @Override
    public boolean onDragStart(@NonNull TimeSpanSelector.Thumb thumb) {
        // Return true to allow drag, false to intercept/disable
        // thumb can be START, END, or NONE
        return true; 
    }

    @Override
    public void onDragStop(@NonNull TimeSpanSelector.Thumb thumb) {
        // Drag interaction finished
    }
});
```

---

## Full XML Customization Example

Below is a comprehensive example demonstrating both **Circular** and **Linear** selectors with extensive styling, including custom thumb drawables, shadow effects, and specific tick configurations.

```xml
<!-- Example of a highly customized Circular Selector -->
<vivek.umrao.time.span.selector.CircularTimeSpanSelector
    android:id="@+id/timeSpanDial"
    android:layout_width="match_parent"
    android:layout_height="350dp"
    app:tss_is24HourFormat="false"
    app:tss_showAmPmLabels="true"
    app:tss_allowOvernightSpan="true"
    
    <!-- Span & Track -->
    app:tss_spanColor="#66007AFF"
    app:tss_trackColor="#FFE0E0E0"
    app:tss_trackWidth="10dp"
    
    <!-- Thumbs with Custom Drawables & Shadows -->
    app:tss_startThumbDrawable="@drawable/ic_light_mode"
    app:tss_endThumbDrawable="@drawable/ic_dark_mode"
    app:tss_thumbRadius="16dp"
    app:tss_thumbElevation="10dp"
    app:tss_thumbShadowColor="#673AB7"
    app:tss_thumbShadowDx="5dp"
    app:tss_thumbShadowDy="5dp"
    
    <!-- Ticks & Labels -->
    app:tss_showTicks="true"
    app:tss_showTickLabels="true"
    app:tss_hourTickHeight="10dp"
    app:tss_hourTickWidth="2dp"
    app:tss_tickLabelSize="12sp"
    app:tss_tickLabelStyle="bold_italic"
    app:tss_tickLabelDistanceFromTick="35dp"
    
    <!-- Span Summary Text -->
    app:tss_showSpanText="true"
    app:tss_spanTextPosition="center"
    app:tss_spanTextFormat="%1$s\nto\n%2$s"
    app:tss_textColor="#00796B" />

<!-- Example of a customized Linear Selector -->
<vivek.umrao.time.span.selector.LinearTimeSpanSelector
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp"
    app:tss_is24HourFormat="false"
    
    <!-- High-visibility Ticks -->
    app:tss_hourTickColor="#000000"
    app:tss_hourTickHeight="20dp"
    app:tss_hourTickWidth="3dp"
    app:tss_minuteTickHeight="18dp"
    app:tss_minuteTickWidth="2dp"
    
    <!-- Thumb Styling -->
    app:tss_thumbFillColor="#FF007AFF"
    app:tss_thumbStrokeColor="#673AB7"
    app:tss_thumbStrokeWidth="2dp"
    app:tss_thumbElevation="5dp"
    
    <!-- Positioned Span Text -->
    app:tss_spanTextPosition="bottom"
    app:tss_spanTextFormat="Selected:\n%1$s to %2$s"
    app:tss_textColor="#00796B" />
```

---

## License
Apache License 2.0. See `LICENSE` for details.
