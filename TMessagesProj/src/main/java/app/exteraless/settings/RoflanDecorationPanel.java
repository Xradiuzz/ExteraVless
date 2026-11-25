package app.exteraless.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import app.exteraless.decoration.ChatDecorationConfig;

import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

/** Settings for seasonal, animated chat-list decorations. */
public class RoflanDecorationPanel extends UniversalFragment {
    private static final int MODE = 1, DENSITY = 2;
    private static final String[] MODES = {"Выключено", "Новогодняя гирлянда и игрушки", "Падающая сакура", "Лёгкое конфетти"};

    @Override protected CharSequence getTitle() { return "Декорации чатов"; }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        int mode = ChatDecorationConfig.mode();
        items.add(UItem.asHeader("Анимированные украшения"));
        items.add(UItem.asButton(MODE, "Эффект · " + MODES[mode]));
        items.add(UItem.asButton(DENSITY, "Количество частиц · " + ChatDecorationConfig.intensity()));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader("Как это работает"));
        items.add(UItem.asButton(99, "Украшения рисуются поверх списка чатов, не перехватывают нажатия и работают только на главном экране чатов."));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        Activity a = getParentActivity();
        if (a == null) return;
        if (item.id == MODE) {
            new AlertDialog.Builder(a).setTitle("Выбери декорацию").setItems(MODES, (d, which) -> {
                ChatDecorationConfig.setMode(which);
                refresh();
            }).setNegativeButton("Отмена", null).show();
        } else if (item.id == DENSITY) {
            EditText input = new EditText(a);
            input.setSingleLine(true);
            input.setInputType(2);
            input.setText(String.valueOf(ChatDecorationConfig.intensity()));
            new AlertDialog.Builder(a).setTitle("Количество частиц (1–60)").setView(input)
                    .setPositiveButton("Сохранить", (d, w) -> {
                        try {
                            ChatDecorationConfig.setIntensity(Integer.parseInt(input.getText().toString()));
                            refresh();
                        } catch (Exception e) {
                            Toast.makeText(a, "Введи число от 1 до 60", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("Отмена", null).show();
        }
    }

    @Override protected boolean onLongClick(UItem item, View view, int position, float x, float y) { return false; }

    private void refresh() {
        if (listView != null) listView.adapter.update(true);
        if (getParentLayout() != null) getParentLayout().rebuildAllFragmentViews(false, false);
    }
}
