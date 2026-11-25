package app.exteraless.legacyui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import app.exteraless.appearance.AppearanceConfig;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.MainTabsLayout;

import xyz.nextalone.nagram.NaConfig;

/**
 * Isolated compatibility layer for the experimental classic Telegram appearance presets.
 *
 * All preset-only switches and theme selection live here so this experiment can be removed
 * by deleting this class and its single call site in OpenExteraRoflanActivity.
 */
public final class LegacyUiController {
    public static final int PRESET_2018 = 0;
    public static final int PRESET_2021 = 1;
    public static final int PRESET_MODERN = 2;

    private static final String PREFS = "exteraless_legacy_ui";
    private static final String KEY_PREVIOUS_THEME = "previous_theme";

    private LegacyUiController() {}

    public static void apply(int preset) {
        AppearanceConfig.init();
        boolean classic2018 = preset == PRESET_2018;
        boolean classic = preset != PRESET_MODERN;

        rememberCurrentTheme();
        AppearanceConfig.newLoadingStyle.setConfigBool(!classic);
        AppearanceConfig.newChatHeaderStyle.setConfigBool(!classic);
        AppearanceConfig.newNavigationBarStyle.setConfigBool(!classic);
        AppearanceConfig.m3ListItems.setConfigBool(!classic);
        AppearanceConfig.md3Player.setConfigBool(!classic);
        AppearanceConfig.md3MiniPlayer.setConfigBool(!classic);
        AppearanceConfig.iosNavigationBarStyle.setConfigBool(false);
        AppearanceConfig.iosChatHeader.setConfigBool(false);
        AppearanceConfig.squareFab.setConfigBool(!classic);
        AppearanceConfig.separateHeaders.setConfigBool(!classic);
        AppearanceConfig.sectionRadius.setConfigInt(classic2018 ? 0 : (classic ? 8 : 20));
        AppearanceConfig.dividerStyle.setConfigInt(classic ? 1 : 2);
        AppearanceConfig.avatarCorners.setConfigInt(classic ? AppearanceConfig.AVATAR_CORNERS_MAX : 20);
        AppearanceConfig.setCenterTitle(false);
        AppearanceConfig.gooeyAvatarAnimation.setConfigBool(!classic);
        AppearanceConfig.navigationDrawer.setConfigBool(false);
        AppearanceConfig.immersiveDrawerAnimation.setConfigBool(false);

        NaConfig.INSTANCE.getSliderStyle().setConfigInt(classic ? 0 : 2);
        NaConfig.INSTANCE.getSwitchStyle().setConfigInt(classic ? 0
                : org.telegram.ui.Components.Switch.SWITCH_STYLE_M3);

        if (classic) {
            applyClassicTheme();
            restoreStockNavigation();
        } else {
            restorePreviousTheme();
        }
        Theme.applyCommonTheme();
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static void rememberCurrentTheme() {
        if (prefs().contains(KEY_PREVIOUS_THEME)) return;
        Theme.ThemeInfo current = Theme.getActiveTheme();
        if (current != null) {
            prefs().edit().putString(KEY_PREVIOUS_THEME, current.getKey()).apply();
        }
    }

    private static void applyClassicTheme() {
        Theme.ThemeInfo current = Theme.getActiveTheme();
        boolean wasDark = current != null && current.isDark();
        Theme.ThemeInfo classic = Theme.getTheme(wasDark ? "Night" : "Blue");
        if (classic == null) classic = Theme.getTheme("Blue");
        if (classic != null) Theme.applyTheme(classic);
    }

    private static void restorePreviousTheme() {
        String previous = prefs().getString(KEY_PREVIOUS_THEME, "Blue");
        Theme.ThemeInfo theme = Theme.getTheme(previous);
        if (theme != null) Theme.applyTheme(theme);
        prefs().edit().remove(KEY_PREVIOUS_THEME).apply();
    }

    private static void restoreStockNavigation() {
        if (MainTabsLayout.isBottomNavigationFloating()) {
            MainTabsLayout.setBottomNavigationMode(MainTabsLayout.BOTTOM_NAVIGATION_MODE_SHOW);
        }
    }
}
