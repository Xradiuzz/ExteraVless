package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    private static final String PREFS = "launcher_icon_controller";
    private static final String SELECTED_ICON = "selected_icon";

    /**
     * PackageManager state is an IPC. The previous implementation queried every
     * launcher alias on every cold start. Remember the selected alias and validate
     * only that one; repair it if Android reset the component state.
     */
    public static void tryFixLauncherIconIfNeeded() {
        Context ctx = ApplicationLoader.applicationContext;
        android.content.SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String selected = prefs.getString(SELECTED_ICON, LauncherIcon.EXTERALESS.name());
        LauncherIcon icon;
        try {
            icon = LauncherIcon.valueOf(selected);
        } catch (IllegalArgumentException ignored) {
            icon = LauncherIcon.EXTERALESS;
        }
        if (!isEnabled(icon)) {
            setIcon(icon);
        }
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        // Пока пользователь ничего не выбирал, включённой считается наша иконка:
        // именно она стоит у <application> в манифесте, и переключатель должен
        // показывать выбранным то, что человек видит на рабочем столе.
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.EXTERALESS;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(SELECTED_ICON, icon.name())
                .apply();
    }

    public enum LauncherIcon {
        EXTERALESS("ExteralessIcon", R.drawable.exteraless_icon_background,
                R.drawable.exteraless_icon_foreground, R.string.AppIconExteraless),
        BLUEPRINT("BlueprintIcon", R.drawable.blueprint_icon_background,
                R.drawable.blueprint_icon_foreground, R.string.AppIconBlueprint),
        RED("RedIcon", R.drawable.red_icon_background,
                R.drawable.red_icon_foreground, R.string.AppIconRed),
        NYA("NyaIcon", R.drawable.nya_icon_background,
                R.drawable.nya_icon_foreground, R.string.AppIconNya),
        AYU("AyuIcon", R.drawable.ayu_icon_background,
                R.drawable.ayu_icon_foreground, R.string.AppIconAyu),
        QUACK("QuackIcon", R.drawable.quack_icon_background,
                R.drawable.quack_icon_foreground, R.string.AppIconQuack),
        GO("GoIcon", R.drawable.go_icon_background,
                R.drawable.app_icon_empty_foreground, R.string.AppIconGo),
        HAND("HandIcon", R.drawable.hand_icon_background,
                R.drawable.app_icon_empty_foreground, R.string.AppIconHand),
        MONO("MonoIcon", R.drawable.mono_icon_background,
                R.drawable.app_icon_empty_foreground, R.string.AppIconMono),
        NOTHING("NothingIcon", R.drawable.nothing_icon_background,
                R.drawable.app_icon_empty_foreground, R.string.AppIconNothing),
        PLUS("PlusIcon", R.drawable.plus_icon_background,
                R.drawable.app_icon_empty_foreground, R.string.AppIconPlus),
        TELEGRAM("TelegramIcon", R.drawable.icon_background_sa, R.mipmap.icon_foreground_sa, R.string.AppIconTelegramOriginal),
        VINTAGE("VintageIcon", R.drawable.icon_6_background_sa, R.mipmap.icon_6_foreground_sa, R.string.AppIconVintage),
        AQUA("AquaIcon", R.drawable.icon_4_background_sa, R.mipmap.icon_foreground_sa, R.string.AppIconAqua),
        PREMIUM("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground_sa, R.string.AppIconPremium),
        TURBO("TurboIcon", R.drawable.icon_5_background_sa, R.mipmap.icon_5_foreground_sa, R.string.AppIconTurbo),
        NOX("NoxIcon", R.mipmap.icon_2_background_sa, R.mipmap.icon_foreground_sa, R.string.AppIconNox);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }

    }
}
