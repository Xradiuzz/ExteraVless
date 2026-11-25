package app.exteraless.fun;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Cells.DialogCell;

import java.util.WeakHashMap;

/**
 * Small visual easter egg for the main dialog list.
 * The animation only affects the rendered cells; dialog data and ordering are untouched.
 */
public final class ChatDanceMode {

    private static final WeakHashMap<View, AnimatorSet> animators = new WeakHashMap<>();
    private static volatile boolean enabled;

    private ChatDanceMode() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void apply(DialogCell cell, boolean shouldDance, int position) {
        if (cell == null) {
            return;
        }

        AnimatorSet current = animators.get(cell);
        if (!shouldDance) {
            if (current != null) {
                current.cancel();
                animators.remove(cell);
            }
            cell.setTranslationY(0f);
            cell.setRotation(0f);
            return;
        }

        if (current != null && current.isRunning()) {
            return;
        }

        ObjectAnimator bounce = ObjectAnimator.ofFloat(
                cell, View.TRANSLATION_Y,
                0f, -AndroidUtilities.dp(13), 0f,
                AndroidUtilities.dp(6), 0f
        );
        bounce.setDuration(720);

        ObjectAnimator tilt = ObjectAnimator.ofFloat(
                cell, View.ROTATION,
                0f, -2.5f, 0f, 2.5f, 0f
        );
        tilt.setDuration(720);

        AnimatorSet dance = new AnimatorSet();
        dance.playTogether(bounce, tilt);
        dance.setInterpolator(new AccelerateDecelerateInterpolator());
        dance.setStartDelay((position % 7) * 65L);
        dance.setStartDelay((position % 7) * 65L);
        dance.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (enabled && cell.getParent() != null) {
                    cell.post(() -> {
                        if (enabled && cell.getParent() != null) {
                            apply(cell, true, position);
                        }
                    });
                }
            }
        });
        animators.put(cell, dance);
        dance.start();
    }
}
