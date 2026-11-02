package vivek.umrao.time.span.selector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Locale;

import vivek.umrao.time.span.selector.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TimeSpanSelector.OnSpanChangeListener listener = new TimeSpanSelector.OnSpanChangeListener() {
            @Override
            public void onSpanChanged(int startMinutes, int endMinutes, boolean isOvernight) {
                updateCallbackText(startMinutes, endMinutes, isOvernight);
            }

            @Override
            public void onInteractionFinished(int startMinutes, int endMinutes, boolean isOvernight) {
                updateCallbackText(startMinutes, endMinutes, isOvernight);
            }
        };

        binding.timeSpanDial.setOnSpanChangeListener(listener);
        binding.linearTimeSpanSelector.setOnSpanChangeListener(listener);

        // Demonstrating granular listeners
        binding.timeSpanDial.setOnTimeChangeListener(new TimeSpanSelector.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(int startTimeMinutes) {
                // Handle specific start change
            }

            @Override
            public void onEndTimeChange(int endTimeMinutes) {
                // Handle specific end change
            }

            @Override
            public void onDurationChange(int startTimeMinutes, int endTimeMinutes) {
                // Handle duration change
            }
        });

        binding.timeSpanDial.setOnDragChangeListener(new TimeSpanSelector.OnDragChangeListener() {
            @Override
            public boolean onDragStart(@NonNull TimeSpanSelector.Thumb thumb) {
                // Optional: show a toast or feedback
                return true;
            }

            @Override
            public void onDragStop(@NonNull TimeSpanSelector.Thumb thumb) {
                // Optional: cleanup
            }
        });

        // Initial update
        updateCallbackText(binding.timeSpanDial.getSpanStartInMinutes(), 
                binding.timeSpanDial.getSpanEndInMinutes(), 
                binding.timeSpanDial.isOvernight());

        binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );
    }

    private void updateCallbackText(int start, int end, boolean overnight) {
        TimeSpan span = new TimeSpan(start, end, overnight);
        String formatted = span.format(false, "AM", "PM", "%1$s - %2$s");
        String text = String.format(Locale.getDefault(), 
            "Span: %s\nDuration: %d mins | Overnight: %b",
            formatted, span.getDurationMinutes(), span.isOvernight());
        
        binding.callbackText.setText(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}