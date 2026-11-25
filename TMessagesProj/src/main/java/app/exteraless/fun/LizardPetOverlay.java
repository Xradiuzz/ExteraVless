package app.exteraless.fun;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;

/**
 * A tiny screen pet for the developer menu.
 *
 * It is intentionally self-contained: removing this class and its single debug-menu hook
 * removes the feature without touching Telegram's UI implementation.
 */
public final class LizardPetOverlay extends View {
    private static LizardPetOverlay active;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint detail = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF body = new RectF();
    private final RectF head = new RectF();
    private final ValueAnimator crawl;
    private ValueAnimator petAnimation;
    private float progress;
    private float pet = 0f;
    private boolean touched;
    private float touchX;
    private float touchY;

    private LizardPetOverlay(Activity activity) {
        super(activity);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setContentDescription("Маленькая ящерица. Коснитесь её, чтобы погладить.");
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        crawl = ValueAnimator.ofFloat(0f, 1f);
        crawl.setDuration(12000);
        crawl.setRepeatCount(ValueAnimator.INFINITE);
        crawl.setRepeatMode(ValueAnimator.REVERSE);
        crawl.setInterpolator(new LinearInterpolator());
        crawl.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        crawl.start();
    }

    public static boolean isActive() {
        return active != null && active.getParent() != null;
    }

    public static void toggle(Activity activity) {
        if (activity == null) return;
        if (active != null) {
            active.detach();
            return;
        }
        View decor = activity.getWindow().getDecorView();
        if (!(decor instanceof ViewGroup)) return;
        LizardPetOverlay pet = new LizardPetOverlay(activity);
        active = pet;
        ((ViewGroup) decor).addView(pet, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        pet.bringToFront();
    }

    private void detach() {
        crawl.cancel();
        if (petAnimation != null) petAnimation.cancel();
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }
        if (active == this) active = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float unit = AndroidUtilities.dpf2(1f);
        float size = AndroidUtilities.dp(1);
        float x = AndroidUtilities.dp(68) + progress * Math.max(0, getWidth() - AndroidUtilities.dp(136));
        float y = getHeight() * 0.70f + (float) Math.sin(progress * Math.PI * 4.0) * AndroidUtilities.dp(34);
        float bounce = (float) Math.sin(SystemClock.uptimeMillis() / 190.0) * AndroidUtilities.dp(2);
        float scale = 1f + pet * 0.12f;
        canvas.save();
        canvas.translate(x, y + bounce - pet * AndroidUtilities.dp(8));
        canvas.scale(scale, scale);

        // Tail, drawn first so it tucks naturally behind the body.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(AndroidUtilities.dp(9));
        paint.setColor(0xFF398A54);
        Path tail = new Path();
        tail.moveTo(-AndroidUtilities.dp(26), AndroidUtilities.dp(2));
        tail.cubicTo(-AndroidUtilities.dp(48), -AndroidUtilities.dp(5),
                -AndroidUtilities.dp(57), AndroidUtilities.dp(19),
                -AndroidUtilities.dp(77), AndroidUtilities.dp(12));
        canvas.drawPath(tail, paint);
        paint.setStrokeWidth(AndroidUtilities.dp(5));
        paint.setColor(0xFF66B879);
        canvas.drawPath(tail, paint);

        // Four short, gently moving legs.
        paint.setStrokeWidth(AndroidUtilities.dp(5));
        paint.setColor(0xFF398A54);
        float legSwing = (float) Math.sin(SystemClock.uptimeMillis() / 130.0) * AndroidUtilities.dp(3);
        for (int side : new int[]{-1, 1}) {
            float anchorX = side < 0 ? -AndroidUtilities.dp(15) : AndroidUtilities.dp(17);
            for (int frontBack : new int[]{-1, 1}) {
                float anchorY = frontBack < 0 ? -AndroidUtilities.dp(1) : AndroidUtilities.dp(7);
                Path leg = new Path();
                leg.moveTo(anchorX, anchorY);
                leg.quadTo(anchorX + side * AndroidUtilities.dp(7),
                        anchorY + AndroidUtilities.dp(7) + legSwing * side,
                        anchorX + side * AndroidUtilities.dp(11),
                        anchorY + AndroidUtilities.dp(12));
                canvas.drawPath(leg, paint);
            }
        }

        body.set(-AndroidUtilities.dp(34), -AndroidUtilities.dp(12),
                AndroidUtilities.dp(34), AndroidUtilities.dp(13));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF398A54);
        paint.setShadowLayer(AndroidUtilities.dp(5), 0, AndroidUtilities.dp(3), 0x55000000);
        canvas.drawOval(body, paint);
        paint.clearShadowLayer();
        body.inset(AndroidUtilities.dp(3), AndroidUtilities.dp(3));
        paint.setColor(0xFF74C987);
        canvas.drawOval(body, paint);

        head.set(AndroidUtilities.dp(19), -AndroidUtilities.dp(18),
                AndroidUtilities.dp(53), AndroidUtilities.dp(12));
        paint.setColor(0xFF4FA968);
        canvas.drawOval(head, paint);
        paint.setColor(0xFF9AE3A2);
        canvas.drawOval(AndroidUtilities.dp(27), -AndroidUtilities.dp(14),
                AndroidUtilities.dp(48), -AndroidUtilities.dp(5), paint);

        // Large friendly eye with a small catchlight.
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(AndroidUtilities.dp(42), -AndroidUtilities.dp(8), AndroidUtilities.dp(5), paint);
        paint.setColor(0xFF183B2B);
        canvas.drawCircle(AndroidUtilities.dp(43), -AndroidUtilities.dp(8), AndroidUtilities.dp(2.5f), paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(AndroidUtilities.dp(44), -AndroidUtilities.dp(9), AndroidUtilities.dp(1), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(AndroidUtilities.dp(1.4f));
        paint.setColor(0xFF245E3A);
        Path smile = new Path();
        smile.moveTo(AndroidUtilities.dp(45), AndroidUtilities.dp(2));
        smile.quadTo(AndroidUtilities.dp(49), AndroidUtilities.dp(5), AndroidUtilities.dp(52), AndroidUtilities.dp(1));
        canvas.drawPath(smile, paint);

        // Tiny back spots make the silhouette read as a gecko rather than a generic blob.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF2E8050);
        for (int i = 0; i < 5; i++) {
            canvas.drawCircle(-AndroidUtilities.dp(20) + i * AndroidUtilities.dp(9),
                    -AndroidUtilities.dp(3) + (i % 2) * AndroidUtilities.dp(5),
                    AndroidUtilities.dp(1.8f), paint);
        }
        if (pet > 0.05f) {
            paint.setColor(0xFFFFE58A);
            paint.setTextSize(AndroidUtilities.dp(17));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("♥", AndroidUtilities.dp(31), -AndroidUtilities.dp(25) - pet * AndroidUtilities.dp(13), paint);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float lizardX = AndroidUtilities.dp(68) + progress * Math.max(0, getWidth() - AndroidUtilities.dp(136));
        float lizardY = getHeight() * 0.70f + (float) Math.sin(progress * Math.PI * 4.0) * AndroidUtilities.dp(34);
        boolean hit = Math.abs(x - lizardX) <= AndroidUtilities.dp(76)
                && Math.abs(y - lizardY) <= AndroidUtilities.dp(42);
        if (!hit) return false;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        if (petAnimation != null) petAnimation.cancel();
        petAnimation = ValueAnimator.ofFloat(1f, 0f);
        petAnimation.setDuration(900);
        petAnimation.setInterpolator(new android.view.animation.DecelerateInterpolator());
        petAnimation.addUpdateListener(a -> {
            pet = (float) a.getAnimatedValue();
            invalidate();
        });
        petAnimation.start();
        return true;
    }
}
