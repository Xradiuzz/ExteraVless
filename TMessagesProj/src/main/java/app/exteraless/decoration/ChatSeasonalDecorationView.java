package app.exteraless.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;

import java.util.Random;
import java.lang.ref.WeakReference;

/** Lightweight, touch-transparent seasonal overlay for the chat list. */
public final class ChatSeasonalDecorationView extends View {
    private static WeakReference<ChatSeasonalDecorationView> current;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random(271828);
    private final float[] x = new float[60], y = new float[60], speed = new float[60], size = new float[60], phase = new float[60];
    private final int[] colors = new int[60];
    private final Path path = new Path();
    private long start;
    private int mode, intensity;

    public ChatSeasonalDecorationView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        mode = ChatDecorationConfig.mode();
        intensity = ChatDecorationConfig.intensity();
        current = new WeakReference<>(this);
        start = System.currentTimeMillis();
        int[] sakura = {0xD9F48FB1, 0xD9F8BBD0, 0xD9EC789A, 0xD9FFE0E8};
        int[] confetti = {0xB9FF5252, 0xB9FFD740, 0xB940C4FF, 0xB966BB6A, 0xB9AB47BC};
        for (int i = 0; i < 60; i++) {
            x[i] = random.nextFloat();
            y[i] = random.nextFloat();
            speed[i] = 0.10f + random.nextFloat() * 0.28f;
            size[i] = 2.5f + random.nextFloat() * 5.5f;
            phase[i] = random.nextFloat() * 6.28f;
            colors[i] = mode == ChatDecorationConfig.SAKURA ? sakura[i % sakura.length] : confetti[i % confetti.length];
        }
        setWillNotDraw(false);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mode != ChatDecorationConfig.OFF) postInvalidateOnAnimation();
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public static void refreshAll() {
        ChatSeasonalDecorationView view = current == null ? null : current.get();
        if (view != null) {
            view.mode = ChatDecorationConfig.mode();
            view.intensity = ChatDecorationConfig.intensity();
            view.start = System.currentTimeMillis();
            view.invalidate();
            if (view.mode != ChatDecorationConfig.OFF && view.isAttachedToWindow()) view.postInvalidateOnAnimation();
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode == ChatDecorationConfig.OFF) return;
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        float t = (System.currentTimeMillis() - start) / 1000f;
        if (mode == ChatDecorationConfig.CHRISTMAS) drawChristmas(canvas, w);
        else {
            int count = Math.min(intensity, 60);
            for (int i = 0; i < count; i++) {
                float px = (x[i] * w + (float)Math.sin(t * 0.7f + phase[i]) * AndroidUtilities.dp(18)) % w;
                float py = (y[i] * h + t * speed[i] * h * 0.35f) % h;
                paint.setColor(colors[i]);
                canvas.save();
                canvas.translate(px, py);
                canvas.rotate((float)Math.sin(t + phase[i]) * 55f);
                if (mode == ChatDecorationConfig.SAKURA) {
                    canvas.drawOval(-size[i], -size[i] * 0.55f, size[i], size[i] * 0.55f, paint);
                } else {
                    canvas.drawCircle(0, 0, size[i] * 0.45f, paint);
                }
                canvas.restore();
            }
        }
        if (getVisibility() == VISIBLE && isAttachedToWindow()) postInvalidateDelayed(34);
    }

    private void drawChristmas(Canvas canvas, float width) {
        float top = AndroidUtilities.dp(7);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(AndroidUtilities.dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xD92E704B);
        Path garland = new Path();
        garland.moveTo(0, top);
        for (int i = 0; i <= 12; i++) {
            float px = width * i / 12f;
            float py = top + AndroidUtilities.dp(12) + (i % 2 == 0 ? 0 : AndroidUtilities.dp(7));
            garland.quadTo(px - width / 24f, py, px, top + (i % 2 == 0 ? AndroidUtilities.dp(5) : AndroidUtilities.dp(17)));
        }
        canvas.drawPath(garland, paint);
        paint.setStyle(Paint.Style.FILL);
        int[] baubles = {0xFFFF5252, 0xFFFFD740, 0xFF40C4FF, 0xFFE040FB, 0xFF69F0AE};
        for (int i = 0; i < 11; i++) {
            float px = width * (i + 0.5f) / 11f;
            float py = top + AndroidUtilities.dp(20 + (i % 3) * 7);
            paint.setColor(0xBFFFFFFF);
            canvas.drawLine(px, py - AndroidUtilities.dp(10), px, py - AndroidUtilities.dp(3), paint);
            paint.setColor(baubles[i % baubles.length]);
            canvas.drawCircle(px, py, AndroidUtilities.dp(4 + (i % 2)), paint);
            paint.setColor(0x66FFFFFF);
            canvas.drawCircle(px - AndroidUtilities.dp(1), py - AndroidUtilities.dp(1), AndroidUtilities.dp(1.2f), paint);
        }
        paint.setColor(0xD92E704B);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(AndroidUtilities.dp(1.5f));
        for (int i = 0; i < 16; i++) {
            float px = width * i / 15f;
            canvas.drawLine(px, top + AndroidUtilities.dp(2), px + (i % 2 == 0 ? -AndroidUtilities.dp(7) : AndroidUtilities.dp(7)), top + AndroidUtilities.dp(13), paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }
}
