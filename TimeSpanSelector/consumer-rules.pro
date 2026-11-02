# Keep custom view classes and their constructors for XML layout inflation
-keep public class vivek.umrao.time.span.selector.CircularTimeSpanSelector {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep public class vivek.umrao.time.span.selector.LinearTimeSpanSelector {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep public interfaces and listeners
-keep public interface vivek.umrao.time.span.selector.TimeSpanSelector { *; }
-keep public interface vivek.umrao.time.span.selector.TimeSpanSelector$* { *; }
