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

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import umrao.spanly.test.R;

import java.util.Locale;

/**
 * Launchable Test Activity for Spanly instrumented live UI testing.
 */
public class TestActivity extends AppCompatActivity {

    private SpanlyDial circularSelector;
    private SpanlySlider linearSelector;
    private TextView callbackText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_layout);

        circularSelector = findViewById(R.id.timeSpanDial);
        linearSelector = findViewById(R.id.spanlySlider);
        callbackText = findViewById(R.id.test_callback_text);

        ISpanly.OnSpanChangeListener listener = new ISpanly.OnSpanChangeListener() {
            @Override
            public void onSpanChanged(int startMinutes, int endMinutes, boolean isOvernight) {
                if (callbackText != null) {
                    callbackText.setText(String.format(Locale.US, "Span: %d to %d (overnight: %b)",
                            startMinutes, endMinutes, isOvernight));
                }
            }

            @Override
            public void onInteractionFinished(int startMinutes, int endMinutes, boolean isOvernight) {
            }
        };

        if (circularSelector != null) {
            circularSelector.setOnSpanChangeListener(listener);
        }
        if (linearSelector != null) {
            linearSelector.setOnSpanChangeListener(listener);
        }
    }

    public SpanlyDial getCircularSelector() {
        return circularSelector;
    }

    public SpanlySlider getLinearSelector() {
        return linearSelector;
    }

    public TextView getCallbackText() {
        return callbackText;
    }
}