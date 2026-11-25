package app.exteraless.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

/** RGB color studio for the global chat wallpaper fill. */
public class RoflanColorStudio extends UniversalFragment {
    private static final int CUSTOM = 1, PRESETS = 2, RESET = 3;
    private static final int[] COLORS = {
            0xFFB3E5FC, 0xFFF8BBD0, 0xFFD1C4E9, 0xFFB2DFDB,
            0xFFFFE0B2, 0xFFC5E1A5, 0xFFB2EBF2, 0xFFFFF9C4,
            0xFFCE93D8, 0xFF80CBC4, 0xFFFFAB91, 0xFF90CAF9
    };
    private static final String[] NAMES = {
            "Ледяная голубая", "Сакура", "Лаванда", "Мята",
            "Персик", "Фисташка", "Аква", "Ваниль",
            "Орхидея", "Бирюза", "Коралл", "Небесная"
    };

    @Override protected CharSequence getTitle() { return "Цветовая студия"; }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Фон переписки"));
        items.add(UItem.asButton(CUSTOM, "RGB-палитра · выбрать свой цвет"));
        items.add(UItem.asButton(PRESETS, "Готовые цветные оттенки"));
        items.add(UItem.asButton(RESET, "Вернуть цвет текущей темы"));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader("Подсказка"));
        items.add(UItem.asButton(99, "Цвет заливки применяется к фону чата. Если установлены фотообои, сначала убери их в настройках обоев."));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == CUSTOM) showRgbPicker();
        else if (item.id == PRESETS) showPresets();
        else if (item.id == RESET) {
            Theme.setColor(Theme.key_chat_wallpaper, 0, true);
            saveTheme();
            Toast.makeText(host(), "Цвет фона восстановлен", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected boolean onLongClick(UItem item, View view, int position, float x, float y) { return false; }

    private Activity host() { return getParentActivity(); }

    private void showPresets() {
        Activity a = host();
        if (a == null) return;
        new AlertDialog.Builder(a).setTitle("Цветные темы")
                .setItems(NAMES, (d, which) -> apply(COLORS[which]))
                .setNegativeButton("Отмена", null).show();
    }

    private void showRgbPicker() {
        Activity a = host();
        if (a == null) return;
        int initial = Theme.getColor(Theme.key_chat_wallpaper);
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = org.telegram.messenger.AndroidUtilities.dp(18);
        box.setPadding(pad, pad / 2, pad, 0);
        TextView preview = new TextView(a);
        preview.setText("ПРЕДПРОСМОТР");
        preview.setTextColor(0xFF202020);
        preview.setGravity(android.view.Gravity.CENTER);
        preview.setBackgroundColor(initial);
        box.addView(preview, new LinearLayout.LayoutParams(-1, org.telegram.messenger.AndroidUtilities.dp(48)));
        SeekBar red = slider(a, Color.red(initial));
        SeekBar green = slider(a, Color.green(initial));
        SeekBar blue = slider(a, Color.blue(initial));
        addChannel(a, box, "Красный", red);
        addChannel(a, box, "Зелёный", green);
        addChannel(a, box, "Синий", blue);
        Runnable update = () -> {
            int color = Color.rgb(red.getProgress(), green.getProgress(), blue.getProgress());
            preview.setBackgroundColor(color);
            preview.setText(String.format("#%06X", color & 0xFFFFFF));
        };
        red.setOnSeekBarChangeListener(listener(update));
        green.setOnSeekBarChangeListener(listener(update));
        blue.setOnSeekBarChangeListener(listener(update));
        update.run();
        new AlertDialog.Builder(a).setTitle("Настрой RGB")
                .setView(box)
                .setPositiveButton("Применить", (d, w) -> apply(Color.rgb(red.getProgress(), green.getProgress(), blue.getProgress())))
                .setNegativeButton("Отмена", null).show();
    }

    private SeekBar slider(Activity a, int value) {
        SeekBar bar = new SeekBar(a);
        bar.setMax(255);
        bar.setProgress(value);
        return bar;
    }

    private void addChannel(Activity a, LinearLayout box, String label, SeekBar bar) {
        TextView title = new TextView(a);
        title.setText(label);
        title.setTextSize(13);
        box.addView(title);
        box.addView(bar, new LinearLayout.LayoutParams(-1, org.telegram.messenger.AndroidUtilities.dp(36)));
    }

    private SeekBar.OnSeekBarChangeListener listener(Runnable update) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { update.run(); }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }

    private void apply(int color) {
        Theme.setColor(Theme.key_chat_wallpaper, color, false);
        saveTheme();
        Toast.makeText(host(), "Цвет фона чатов обновлён", Toast.LENGTH_SHORT).show();
    }

    private void saveTheme() {
        Theme.ThemeInfo active = Theme.getActiveTheme();
        if (active != null) Theme.saveCurrentTheme(active, true, false, false);
        Theme.reloadWallpaper(true);
        Theme.applyCommonTheme();
    }
}
