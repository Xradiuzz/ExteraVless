package app.exteraless.decoration;

import android.content.Context;
import android.content.SharedPreferences;
import org.telegram.messenger.ApplicationLoader;

public final class ChatDecorationConfig {
    public static final int OFF = 0, CHRISTMAS = 1, SAKURA = 2, CONFETTI = 3;
    private static final String PREFS = "exteraroflan_decorations";
    private ChatDecorationConfig() {}
    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    public static int mode() { return Math.max(OFF, Math.min(CONFETTI, prefs().getInt("mode", OFF))); }
    public static void setMode(int mode) { prefs().edit().putInt("mode", Math.max(OFF, Math.min(CONFETTI, mode))).apply(); ChatSeasonalDecorationView.refreshAll(); }
    public static int intensity() { return Math.max(1, Math.min(60, prefs().getInt("intensity", 24))); }
    public static void setIntensity(int value) { prefs().edit().putInt("intensity", Math.max(1, Math.min(60, value))).apply(); ChatSeasonalDecorationView.refreshAll(); }
}
