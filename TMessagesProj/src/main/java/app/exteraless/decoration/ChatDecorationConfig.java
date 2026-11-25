package app.exteraless.decoration;

import android.content.Context;
import android.content.SharedPreferences;
import org.telegram.messenger.ApplicationLoader;

public final class ChatDecorationConfig {
    public static final int OFF = 0;
    public static final int CHRISTMAS = 1;
    public static final int SAKURA = 2;
    public static final int CONFETTI = 3;
    public static final int NUGGETS = 4;
    public static final int HEARTS = 5;
    public static final int STARS = 6;
    public static final int LEAVES = 7;
    public static final int BUBBLES = 8;
    public static final int FIRE = 9;
    public static final int MUSIC = 10;
    public static final int CANDY = 11;
    public static final int COINS = 12;
    public static final int MAX_MODE = COINS;

    private static final String PREFS = "exteraroflan_decorations";

    private ChatDecorationConfig() {}

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int mode() {
        return Math.max(OFF, Math.min(MAX_MODE, prefs().getInt("mode", OFF)));
    }

    public static void setMode(int mode) {
        prefs().edit().putInt("mode", Math.max(OFF, Math.min(MAX_MODE, mode))).apply();
        ChatSeasonalDecorationView.refreshAll();
    }

    public static int intensity() {
        return Math.max(1, Math.min(60, prefs().getInt("intensity", 24)));
    }

    public static void setIntensity(int value) {
        prefs().edit().putInt("intensity", Math.max(1, Math.min(60, value))).apply();
        ChatSeasonalDecorationView.refreshAll();
    }
}
