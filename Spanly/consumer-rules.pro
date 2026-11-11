# Keep custom view classes and their constructors for XML layout inflation
-keep public class umrao.spanly.SpanlyDial {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep public class umrao.spanly.SpanlySlider {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep public interfaces and listeners
-keep public interface umrao.spanly.ISpanly { *; }
-keep public interface umrao.spanly.ISpanly$* { *; }
