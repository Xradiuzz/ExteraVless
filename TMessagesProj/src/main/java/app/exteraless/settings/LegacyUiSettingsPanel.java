package app.exteraless.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import app.exteraless.appearance.AppearanceConfig;
import app.exteraless.legacyui.LegacyUiController;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

import xyz.nextalone.nagram.NaConfig;

/** Fine-grained classic Telegram appearance controls. */
public class LegacyUiSettingsPanel extends UniversalFragment {
    private static final int PRESET = 1, HEADER = 2, NAV = 3, CHAT_LIST = 4,
            RADIUS = 5, AVATARS = 6, DIVIDERS = 7, FAB = 8, HEADERS = 9,
            PLAYER = 10, SLIDERS = 11, SWITCHES = 12;

    @Override
    protected CharSequence getTitle() {
        return "Старый дизайн Telegram";
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        AppearanceConfig.init();
        items.add(UItem.asHeader("Профиль"));
        items.add(UItem.asButton(PRESET, "Базовый профиль · выбрать"));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader("Компоненты интерфейса"));
        items.add(UItem.asButton(HEADER, "Шапка чата · " + (AppearanceConfig.newChatHeaderStyle.Bool() ? "Material 3" : AppearanceConfig.iosChatHeader.Bool() ? "iOS" : "Классическая")));
        items.add(UItem.asButton(NAV, "Нижняя навигация · " + (AppearanceConfig.newNavigationBarStyle.Bool() ? "Material 3" : AppearanceConfig.iosNavigationBarStyle.Bool() ? "iOS" : "Классическая")));
        items.add(UItem.asButton(CHAT_LIST, "Список чатов · " + (AppearanceConfig.m3ListItems.Bool() ? "Material 3" : "Классический")));
        items.add(UItem.asButton(RADIUS, "Скругление карточек · " + AppearanceConfig.sectionRadius.Int() + " dp"));
        items.add(UItem.asButton(AVATARS, "Скругление аватаров · " + AppearanceConfig.avatarCorners.Int() + "/28"));
        items.add(UItem.asButton(DIVIDERS, "Разделители · " + new String[]{"Скрыты", "Линии", "Сегменты"}[Math.max(0, Math.min(2, AppearanceConfig.dividerStyle.Int()))]));
        items.add(UItem.asButton(FAB, "Плавающая кнопка · " + (AppearanceConfig.squareFab.Bool() ? "Квадратная" : "Круглая")));
        items.add(UItem.asButton(HEADERS, "Заголовки секций · " + (AppearanceConfig.separateHeaders.Bool() ? "Отдельно" : "Внутри списка")));
        items.add(UItem.asButton(PLAYER, "Плеер · " + (AppearanceConfig.md3Player.Bool() ? "Material 3" : "Классический")));
        items.add(UItem.asButton(SLIDERS, "Ползунки · " + (NaConfig.INSTANCE.getSliderStyle().Int() == 0 ? "Классические" : "Material 3")));
        items.add(UItem.asButton(SWITCHES, "Переключатели · " + (NaConfig.INSTANCE.getSwitchStyle().Int() == 0 ? "Классические" : "Material 3")));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case PRESET: preset(); break;
            case HEADER: choose("Шапка чата", new String[]{"Классическая", "Material 3", "iOS"}, v -> {
                AppearanceConfig.newChatHeaderStyle.setConfigBool(v == 1);
                AppearanceConfig.iosChatHeader.setConfigBool(v == 2);
            }); break;
            case NAV: choose("Нижняя навигация", new String[]{"Классическая", "Material 3", "iOS"}, v -> {
                AppearanceConfig.newNavigationBarStyle.setConfigBool(v == 1);
                AppearanceConfig.iosNavigationBarStyle.setConfigBool(v == 2);
            }); break;
            case CHAT_LIST: choose("Список чатов", new String[]{"Классический", "Material 3"}, v -> AppearanceConfig.m3ListItems.setConfigBool(v == 1)); break;
            case RADIUS: number("Радиус карточек (dp)", AppearanceConfig.sectionRadius.Int(), 0, 32, v -> AppearanceConfig.sectionRadius.setConfigInt(v)); break;
            case AVATARS: number("Скругление аватаров (0–28)", AppearanceConfig.avatarCorners.Int(), 0, 28, v -> AppearanceConfig.avatarCorners.setConfigInt(v)); break;
            case DIVIDERS: choose("Разделители", new String[]{"Скрыть", "Линии", "Сегменты"}, AppearanceConfig.dividerStyle::setConfigInt); break;
            case FAB: toggle("Квадратная плавающая кнопка", AppearanceConfig.squareFab.Bool(), AppearanceConfig.squareFab::setConfigBool); break;
            case HEADERS: toggle("Отдельные заголовки секций", AppearanceConfig.separateHeaders.Bool(), AppearanceConfig.separateHeaders::setConfigBool); break;
            case PLAYER: toggle("Новый стиль плеера Material 3", AppearanceConfig.md3Player.Bool(), AppearanceConfig.md3Player::setConfigBool); break;
            case SLIDERS: choose("Стиль ползунков", new String[]{"Классический", "Material 3"}, v -> NaConfig.INSTANCE.getSliderStyle().setConfigInt(v == 0 ? 0 : 2)); break;
            case SWITCHES: choose("Стиль переключателей", new String[]{"Классический", "Material 3"}, v -> NaConfig.INSTANCE.getSwitchStyle().setConfigInt(v == 0 ? 0 : org.telegram.ui.Components.Switch.SWITCH_STYLE_M3)); break;
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private Activity host() { return getParentActivity(); }

    private interface IntAction { void apply(int value); }
    private interface BoolAction { void apply(boolean value); }

    private void choose(String title, String[] values, IntAction action) {
        Activity a = host();
        if (a == null) return;
        new AlertDialog.Builder(a).setTitle(title).setItems(values, (d, which) -> {
            action.apply(which);
            changed();
        }).setNegativeButton("Отмена", null).show();
    }

    private void toggle(String title, boolean current, BoolAction action) {
        action.apply(!current);
        changed();
        Toast.makeText(host(), title + (current ? " выключено" : " включено"), Toast.LENGTH_SHORT).show();
    }

    private void number(String title, int current, int min, int max, IntAction action) {
        Activity a = host();
        if (a == null) return;
        EditText input = new EditText(a);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(current));
        new AlertDialog.Builder(a).setTitle(title).setView(input)
                .setPositiveButton("Применить", (d, w) -> {
                    try {
                        int value = Integer.parseInt(input.getText().toString());
                        if (value < min || value > max) throw new NumberFormatException();
                        action.apply(value);
                        changed();
                    } catch (Exception e) {
                        Toast.makeText(a, "Введи число от " + min + " до " + max, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Отмена", null).show();
    }

    private void preset() {
        Activity a = host();
        if (a == null) return;
        String[] presets = {"Telegram 2018 · максимально классический", "Telegram 2021 · компактный", "Современный"};
        new AlertDialog.Builder(a).setTitle("Базовый профиль").setItems(presets, (d, which) -> {
            LegacyUiController.apply(which);
            changed();
        }).setNegativeButton("Отмена", null).show();
    }

    private void changed() {
        AppearanceConfig.invalidateDividerStyle();
        Theme.applyCommonTheme();
        if (getParentLayout() != null) getParentLayout().rebuildAllFragmentViews(false, false);
        if (listView != null) listView.adapter.update(true);
    }
}
