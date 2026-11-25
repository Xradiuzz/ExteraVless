package app.exteraless.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;

import java.lang.ref.WeakReference;
import java.io.InputStream;
import java.util.Random;

/** Lightweight, touch-transparent overlay for seasonal header art and animated particles. */
public final class ChatSeasonalDecorationView extends View {
    private static WeakReference<ChatSeasonalDecorationView> current;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random(271828);
    private final float[] x = new float[60];
    private final float[] y = new float[60];
    private final float[] speed = new float[60];
    private final float[] size = new float[60];
    private final float[] phase = new float[60];
    private final RectF rect = new RectF();
    private final Path path = new Path();
    private Bitmap[] customBitmaps = new Bitmap[0];
    private String customBitmapKey = "";

    private long start;
    private int mode;
    private int intensity;

    public ChatSeasonalDecorationView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        mode = ChatDecorationConfig.mode();
        intensity = ChatDecorationConfig.intensity();
        current = new WeakReference<>(this);
        start = System.currentTimeMillis();
        for (int i = 0; i < 60; i++) {
            x[i] = random.nextFloat();
            y[i] = random.nextFloat();
            speed[i] = 0.10f + random.nextFloat() * 0.28f;
            size[i] = 2.5f + random.nextFloat() * 5.5f;
            phase[i] = random.nextFloat() * 6.28f;
        }
        loadCustomBitmaps();
        setWillNotDraw(false);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        loadCustomBitmaps();
        if (mode != ChatDecorationConfig.OFF) postInvalidateOnAnimation();
    }

    public static void refreshAll() {
        ChatSeasonalDecorationView view = current == null ? null : current.get();
        if (view != null) {
            view.mode = ChatDecorationConfig.mode();
            view.intensity = ChatDecorationConfig.intensity();
            view.loadCustomBitmaps();
            view.start = System.currentTimeMillis();
            view.invalidate();
            if (view.mode != ChatDecorationConfig.OFF && view.isAttachedToWindow()) {
                view.postInvalidateOnAnimation();
            }
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode == ChatDecorationConfig.OFF) return;

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float t = (System.currentTimeMillis() - start) / 1000f;
        if (ChatDecorationConfig.headerStyle() > 0) drawHeader(canvas, w, t);
        // Snow is the CHRISTMAS effect itself. Drawing it over every other particle
        // mode created two independent particle layers and made them visually pile up.
        if (ChatDecorationConfig.snowEnabled() && mode == ChatDecorationConfig.CHRISTMAS) drawSnow(canvas, w, h, t);
        if (mode != ChatDecorationConfig.OFF && mode != ChatDecorationConfig.CHRISTMAS) {
            int count = Math.min(intensity, 60);
            for (int i = 0; i < count; i++) {
                float drift = (float) Math.sin(t * 0.7f + phase[i]) * AndroidUtilities.dp(18);
                float px = wrap(x[i] * w + drift, w);
                float py = wrap(y[i] * h + t * speed[i] * ChatDecorationConfig.particleSpeed() / 100f * h * 0.34f, h);
                canvas.save();
                canvas.translate(px, py);
                canvas.rotate((float) Math.sin(t * 0.8f + phase[i]) * 70f);
                drawParticle(canvas, i, t);
                canvas.restore();
            }
        }
        if (getVisibility() == VISIBLE && isAttachedToWindow()) postInvalidateDelayed(34);
    }

    private float wrap(float value, float limit) {
        return ((value % limit) + limit) % limit;
    }

    private void drawSnow(Canvas canvas, float width, float height, float t) {
        int count = Math.min(intensity, 60);
        for (int i = 0; i < count; i++) {
            float sway = (float) Math.sin(t * 0.8f + phase[i]) * AndroidUtilities.dp(22);
            float px = wrap(x[i] * width + sway, width);
            float py = wrap(y[i] * height + t * (0.12f + speed[i] * 0.45f) * ChatDecorationConfig.particleSpeed() / 100f * height, height);
            float radius = AndroidUtilities.dp(1.2f + size[i] * 0.34f);
            paint.setColor(i % 4 == 0 ? 0xEFFFFFFF : 0xBFFFFFFF);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(px, py, radius, paint);
            if (i % 5 == 0) {
                paint.setStrokeWidth(AndroidUtilities.dp(1));
                canvas.drawLine(px - radius * 1.7f, py, px + radius * 1.7f, py, paint);
                canvas.drawLine(px, py - radius * 1.7f, px, py + radius * 1.7f, paint);
            }
        }
    }

    private void drawParticle(Canvas canvas, int i, float t) {
        float s = AndroidUtilities.dp(size[i] * ChatDecorationConfig.particleSize() / 100f);
        switch (mode) {
            case ChatDecorationConfig.SAKURA:
                paint.setColor(new int[]{0xD9F48FB1, 0xD9F8BBD0, 0xD9EC789A, 0xD9FFE0E8}[i % 4]);
                canvas.drawOval(-s, -s * 0.48f, s, s * 0.48f, paint);
                paint.setColor(0x77FFFFFF);
                canvas.drawLine(-s * 0.5f, 0, s * 0.55f, 0, paint);
                break;
            case ChatDecorationConfig.CONFETTI:
                paint.setColor(new int[]{0xD9FF5252, 0xD9FFD740, 0xD940C4FF, 0xD966BB6A, 0xD9AB47BC}[i % 5]);
                rect.set(-s * 0.55f, -s, s * 0.55f, s);
                canvas.drawRoundRect(rect, s * 0.18f, s * 0.18f, paint);
                break;
            case ChatDecorationConfig.NUGGETS:
                drawNugget(canvas, s);
                break;
            case ChatDecorationConfig.HEARTS:
                paint.setColor(new int[]{0xFFE85D83, 0xFFFF8FA3, 0xFFD85B9B}[i % 3]);
                drawHeart(canvas, s * 1.35f);
                break;
            case ChatDecorationConfig.STARS:
                paint.setColor(i % 3 == 0 ? 0xFFFFE082 : 0xFFE1F5FE);
                paint.setAlpha(145 + (int) (Math.abs(Math.sin(t * 2.2f + phase[i])) * 110));
                drawStar(canvas, s * 1.35f);
                paint.setAlpha(255);
                break;
            case ChatDecorationConfig.LEAVES:
                paint.setColor(new int[]{0xFFE6A23C, 0xFFC56A32, 0xFFB94E48, 0xFF789B54}[i % 4]);
                drawLeaf(canvas, s * 1.35f);
                break;
            case ChatDecorationConfig.BUBBLES:
                paint.setColor(0x5579D9FF);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(0, 0, s * 1.25f, paint);
                paint.setColor(0xAAFFFFFF);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(AndroidUtilities.dp(1.2f));
                canvas.drawCircle(0, 0, s * 1.25f, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(-s * 0.35f, -s * 0.4f, s * 0.22f, paint);
                break;
            case ChatDecorationConfig.FIRE:
                paint.setColor(new int[]{0xFFFF7043, 0xFFFFCA28, 0xFFFF8A65}[i % 3]);
                drawStar(canvas, s * 0.9f);
                paint.setColor(0xCCFFF3E0);
                canvas.drawCircle(0, 0, s * 0.22f, paint);
                break;
            case ChatDecorationConfig.MUSIC:
                paint.setColor(new int[]{0xFF7E57C2, 0xFF26A69A, 0xFFEF5350}[i % 3]);
                drawMusicNote(canvas, s);
                break;
            case ChatDecorationConfig.CANDY:
                drawCandy(canvas, s);
                break;
            case ChatDecorationConfig.COINS:
                paint.setColor(i % 2 == 0 ? 0xFFFFD54F : 0xFFFFB300);
                canvas.drawCircle(0, 0, s * 1.15f, paint);
                paint.setColor(0xFF9A6500);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(AndroidUtilities.dp(1.2f));
                canvas.drawCircle(0, 0, s * 0.82f, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(s * 1.25f);
                canvas.drawText("$", 0, s * 0.43f, paint);
                break;
            case ChatDecorationConfig.CUSTOM_IMAGE:
                drawCustomParticle(canvas, i, s);
                break;
            default:
                paint.setColor(0xFFFFFFFF);
                canvas.drawCircle(0, 0, s * 0.45f, paint);
                break;
        }
    }

    private void drawCustomParticle(Canvas canvas, int index, float s) {
        if (customBitmaps.length == 0) {
            paint.setColor(0xFFFFFFFF);
            drawStar(canvas, s * 1.2f);
            return;
        }
        Bitmap bitmap = customBitmaps[index % customBitmaps.length];
        if (bitmap == null || bitmap.isRecycled()) return;
        rect.set(-s * 1.45f, -s * 1.45f, s * 1.45f, s * 1.45f);
        paint.setAlpha(ChatDecorationConfig.particleOpacity());
        canvas.drawBitmap(bitmap, null, rect, paint);
        paint.setAlpha(255);
    }

    private void loadCustomBitmaps() {
        String[] uris = ChatDecorationConfig.customParticleUris();
        StringBuilder key = new StringBuilder();
        for (String uri : uris) key.append(uri).append('|');
        if (key.toString().equals(customBitmapKey)) return;
        for (Bitmap bitmap : customBitmaps) if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
        customBitmaps = new Bitmap[uris.length];
        customBitmapKey = key.toString();
        if (getContext() == null) return;
        for (int i = 0; i < uris.length; i++) {
            try (InputStream input = getContext().getContentResolver().openInputStream(Uri.parse(uris[i]))) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                customBitmaps[i] = BitmapFactory.decodeStream(input, null, options);
            } catch (Exception ignored) {}
        }
    }

    private void drawNugget(Canvas canvas, float s) {
        rect.set(-s * 1.35f, -s * 0.82f, s * 1.35f, s * 0.82f);
        paint.setColor(0xFFB96B28);
        canvas.drawRoundRect(rect, s * 0.58f, s * 0.58f, paint);
        rect.inset(s * 0.12f, s * 0.12f);
        paint.setColor(0xFFE6A34B);
        canvas.drawRoundRect(rect, s * 0.5f, s * 0.5f, paint);
        paint.setColor(0x99FFF0B8);
        canvas.drawCircle(-s * 0.48f, -s * 0.25f, s * 0.14f, paint);
        canvas.drawCircle(s * 0.35f, s * 0.24f, s * 0.11f, paint);
        canvas.drawCircle(s * 0.72f, -s * 0.2f, s * 0.09f, paint);
    }

    private void drawHeart(Canvas canvas, float s) {
        path.reset();
        path.moveTo(0, s * 0.9f);
        path.cubicTo(-s * 0.35f, s * 0.55f, -s, 0, -s * 0.72f, -s * 0.42f);
        path.cubicTo(-s * 0.5f, -s * 0.9f, -s * 0.12f, -s * 0.72f, 0, -s * 0.35f);
        path.cubicTo(s * 0.18f, -s * 0.78f, s * 0.65f, -s * 0.9f, s * 0.82f, -s * 0.4f);
        path.cubicTo(s, 0, s * 0.35f, s * 0.62f, 0, s * 0.9f);
        canvas.drawPath(path, paint);
    }

    private void drawStar(Canvas canvas, float s) {
        path.reset();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            float radius = i % 2 == 0 ? s : s * 0.42f;
            float px = (float) Math.cos(angle) * radius;
            float py = (float) Math.sin(angle) * radius;
            if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawLeaf(Canvas canvas, float s) {
        path.reset();
        path.moveTo(-s, 0);
        path.quadTo(-s * 0.15f, -s * 1.05f, s, -s * 0.1f);
        path.quadTo(s * 0.3f, s * 0.9f, -s, 0);
        canvas.drawPath(path, paint);
        paint.setColor(0x66FFF3D0);
        canvas.drawLine(-s * 0.75f, s * 0.05f, s * 0.72f, -s * 0.12f, paint);
    }

    private void drawMusicNote(Canvas canvas, float s) {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(-s * 0.35f, s * 0.35f, s * 0.36f, paint);
        paint.setStrokeWidth(Math.max(AndroidUtilities.dp(1.5f), s * 0.2f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(-s * 0.05f, s * 0.25f, -s * 0.05f, -s, paint);
        canvas.drawLine(-s * 0.05f, -s, s * 0.78f, -s * 0.72f, paint);
        canvas.drawLine(s * 0.78f, -s * 0.72f, s * 0.78f, s * 0.05f, paint);
        canvas.drawCircle(s * 0.48f, s * 0.38f, s * 0.36f, paint);
    }

    private void drawCandy(Canvas canvas, float s) {
        rect.set(-s * 0.72f, -s * 0.72f, s * 0.72f, s * 0.72f);
        paint.setColor(iCandyColor());
        canvas.drawRoundRect(rect, s * 0.28f, s * 0.28f, paint);
        paint.setColor(0xCCFFFFFF);
        paint.setStrokeWidth(s * 0.24f);
        canvas.drawLine(-s * 0.42f, -s * 0.62f, s * 0.42f, s * 0.62f, paint);
        canvas.drawLine(-s * 0.72f, 0, s * 0.72f, 0, paint);
    }

    private int iCandyColor() {
        return 0xFFE85D75;
    }

    private void drawHeader(Canvas canvas, float width, float t) {
        int style = ChatDecorationConfig.headerStyle();
        if (style == 1) {
            drawChristmasHeader(canvas, width, t);
            return;
        }
        float top = AndroidUtilities.dp(10);
        if (style == 2) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(AndroidUtilities.dp(2));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(0x8879B8FF);
            path.reset();
            path.moveTo(0, top + AndroidUtilities.dp(8));
            path.quadTo(width * .5f, top - AndroidUtilities.dp(2), width, top + AndroidUtilities.dp(8));
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 9; i++) {
                float x = width * (i + .5f) / 9f;
                paint.setColor(i % 2 == 0 ? 0xFF8AB4F8 : 0xFFB39DDB);
                canvas.drawCircle(x, top + AndroidUtilities.dp(7 + (i % 2) * 3), AndroidUtilities.dp(2.2f), paint);
            }
        } else if (style == 3) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(AndroidUtilities.dp(4));
            paint.setColor(0x5579E6D2);
            path.reset();
            path.moveTo(0, top + AndroidUtilities.dp(13));
            path.cubicTo(width * .22f, top - AndroidUtilities.dp(3), width * .48f, top + AndroidUtilities.dp(25), width * .7f, top + AndroidUtilities.dp(8));
            path.cubicTo(width * .82f, top - AndroidUtilities.dp(1), width * .92f, top + AndroidUtilities.dp(6), width, top + AndroidUtilities.dp(3));
            canvas.drawPath(path, paint);
            paint.setStrokeWidth(AndroidUtilities.dp(1.5f));
            paint.setColor(0xAA9FE8FF);
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawChristmasHeader(Canvas canvas, float width, float t) {
        int headerStyle = ChatDecorationConfig.headerStyle();
        if (headerStyle == 0) return;
        float top = AndroidUtilities.dp(7);

        // Two layered, gently sagging garlands.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(AndroidUtilities.dp(3));
        paint.setColor(0xFF245B3B);
        path.reset();
        path.moveTo(0, top + AndroidUtilities.dp(3));
        path.cubicTo(width * 0.22f, top + AndroidUtilities.dp(28), width * 0.38f, top - AndroidUtilities.dp(1), width * 0.53f, top + AndroidUtilities.dp(17));
        path.cubicTo(width * 0.72f, top + AndroidUtilities.dp(34), width * 0.83f, top + AndroidUtilities.dp(2), width, top + AndroidUtilities.dp(12));
        canvas.drawPath(path, paint);
        paint.setStrokeWidth(AndroidUtilities.dp(1.4f));
        paint.setColor(0xFFB78B43);
        path.reset();
        path.moveTo(0, top + AndroidUtilities.dp(8));
        path.cubicTo(width * 0.3f, top + AndroidUtilities.dp(30), width * 0.68f, top + AndroidUtilities.dp(2), width, top + AndroidUtilities.dp(18));
        canvas.drawPath(path, paint);

        // Pine needles and small branch clusters along the top edge.
        paint.setStrokeWidth(AndroidUtilities.dp(1.5f));
        paint.setColor(0xFF2F7548);
        for (int i = 0; i < 25; i++) {
            float px = width * i / 24f;
            float py = top + AndroidUtilities.dp(7 + (i % 4) * 3);
            float direction = i % 2 == 0 ? -1f : 1f;
            canvas.drawLine(px, py, px + direction * AndroidUtilities.dp(7), py - AndroidUtilities.dp(5), paint);
            canvas.drawLine(px, py, px - direction * AndroidUtilities.dp(6), py + AndroidUtilities.dp(5), paint);
        }

        // Hanging lights with a subtle twinkle pulse.
        int[] lights = headerStyle == 3 ? new int[]{0xFF6DE7D8,0xFF74B8FF,0xFFB79CFF,0xFFFFB8E5,0xFF9CF2C8} : new int[]{0xFFFF5252,0xFFFFD54F,0xFF40C4FF,0xFFE040FB,0xFF69F0AE,0xFFFF8A65};
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 13; i++) {
            float px = width * (i + 0.5f) / 13f;
            float py = top + AndroidUtilities.dp(19 + (i % 3) * 6);
            paint.setColor(0xBFFFFFFF);
            paint.setStrokeWidth(AndroidUtilities.dp(1));
            canvas.drawLine(px, py - AndroidUtilities.dp(8), px, py - AndroidUtilities.dp(2), paint);
            float pulse = 0.78f + 0.22f * (float) Math.sin(t * 3f + i);
            paint.setColor(lights[i % lights.length]);
            canvas.drawCircle(px, py, AndroidUtilities.dp(3.2f) * pulse, paint);
            paint.setColor(0x77FFFFFF);
            canvas.drawCircle(px - AndroidUtilities.dp(1), py - AndroidUtilities.dp(1), AndroidUtilities.dp(1), paint);
        }

        // Detailed hanging ornaments with caps, hooks and highlights.
        int[] baubles = {0xFFC62828, 0xFF1565C0, 0xFFFFB300, 0xFF8E24AA, 0xFF00897B};
        for (int i = 0; i < 7; i++) {
            float px = width * (i + 0.8f) / 8f;
            float py = top + AndroidUtilities.dp(35 + (i % 2) * 5);
            paint.setColor(0xFFB78B43);
            canvas.drawLine(px, py - AndroidUtilities.dp(10), px, py - AndroidUtilities.dp(5), paint);
            paint.setColor(baubles[i % baubles.length]);
            canvas.drawCircle(px, py, AndroidUtilities.dp(5), paint);
            paint.setColor(0xFFFFE8A3);
            canvas.drawRect(px - AndroidUtilities.dp(2), py - AndroidUtilities.dp(6), px + AndroidUtilities.dp(2), py - AndroidUtilities.dp(4), paint);
            paint.setColor(0x88FFFFFF);
            canvas.drawCircle(px - AndroidUtilities.dp(1.7f), py - AndroidUtilities.dp(1.7f), AndroidUtilities.dp(1.4f), paint);
        }

        drawHangingTree(canvas, width * 0.12f, top + AndroidUtilities.dp(4));
        drawHangingTree(canvas, width * 0.88f, top + AndroidUtilities.dp(4));
        drawBow(canvas, width * 0.34f, top + AndroidUtilities.dp(16));
        drawBow(canvas, width * 0.68f, top + AndroidUtilities.dp(18));

        // Small snow crystals are part of the header even when snowfall is sparse.
        paint.setColor(0xCCFFFFFF);
        for (int i = 0; i < 5; i++) {
            float px = width * (i + 0.5f) / 5f;
            float py = top + AndroidUtilities.dp(49 + (i % 2) * 5);
            drawSnowflake(canvas, px, py, AndroidUtilities.dp(3.5f));
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHangingTree(Canvas canvas, float cx, float top) {
        paint.setColor(0xFFD5B56D);
        paint.setStrokeWidth(AndroidUtilities.dp(1.5f));
        canvas.drawLine(cx, top - AndroidUtilities.dp(8), cx, top + AndroidUtilities.dp(1), paint);
        paint.setColor(0xFF2E7D4F);
        path.reset();
        path.moveTo(cx, top);
        path.lineTo(cx - AndroidUtilities.dp(15), top + AndroidUtilities.dp(24));
        path.lineTo(cx + AndroidUtilities.dp(15), top + AndroidUtilities.dp(24));
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(0xFF1B5E3A);
        path.reset();
        path.moveTo(cx, top + AndroidUtilities.dp(10));
        path.lineTo(cx - AndroidUtilities.dp(19), top + AndroidUtilities.dp(34));
        path.lineTo(cx + AndroidUtilities.dp(19), top + AndroidUtilities.dp(34));
        path.close();
        canvas.drawPath(path, paint);
        int[] ornaments = {0xFFFF5252, 0xFFFFD740, 0xFF40C4FF, 0xFFE040FB};
        for (int i = 0; i < 5; i++) {
            float ox = cx + (i % 2 == 0 ? -1 : 1) * AndroidUtilities.dp(6 + (i % 3) * 3);
            float oy = top + AndroidUtilities.dp(14 + (i / 2) * 9);
            paint.setColor(ornaments[i % ornaments.length]);
            canvas.drawCircle(ox, oy, AndroidUtilities.dp(2.4f), paint);
        }
        paint.setColor(0xFFFFD740);
        canvas.drawCircle(cx, top + AndroidUtilities.dp(1), AndroidUtilities.dp(3), paint);
    }

    private void drawBow(Canvas canvas, float cx, float cy) {
        paint.setColor(0xFFC62828);
        path.reset();
        path.moveTo(cx, cy);
        path.cubicTo(cx - AndroidUtilities.dp(15), cy - AndroidUtilities.dp(13), cx - AndroidUtilities.dp(20), cy + AndroidUtilities.dp(7), cx - AndroidUtilities.dp(3), cy + AndroidUtilities.dp(5));
        path.cubicTo(cx - AndroidUtilities.dp(1), cy + AndroidUtilities.dp(3), cx, cy + AndroidUtilities.dp(2), cx, cy);
        path.cubicTo(cx + AndroidUtilities.dp(15), cy - AndroidUtilities.dp(13), cx + AndroidUtilities.dp(20), cy + AndroidUtilities.dp(7), cx + AndroidUtilities.dp(3), cy + AndroidUtilities.dp(5));
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(0xFFFFD180);
        canvas.drawCircle(cx, cy + AndroidUtilities.dp(2), AndroidUtilities.dp(3), paint);
        paint.setColor(0xFFC62828);
        canvas.drawLine(cx - AndroidUtilities.dp(1), cy + AndroidUtilities.dp(5), cx - AndroidUtilities.dp(7), cy + AndroidUtilities.dp(15), paint);
        canvas.drawLine(cx + AndroidUtilities.dp(1), cy + AndroidUtilities.dp(5), cx + AndroidUtilities.dp(8), cy + AndroidUtilities.dp(14), paint);
    }

    private void drawSnowflake(Canvas canvas, float cx, float cy, float radius) {
        paint.setColor(0xCCFFFFFF);
        paint.setStrokeWidth(AndroidUtilities.dp(1));
        for (int i = 0; i < 3; i++) {
            double angle = i * Math.PI / 3;
            float dx = (float) Math.cos(angle) * radius;
            float dy = (float) Math.sin(angle) * radius;
            canvas.drawLine(cx - dx, cy - dy, cx + dx, cy + dy, paint);
        }
    }
}
