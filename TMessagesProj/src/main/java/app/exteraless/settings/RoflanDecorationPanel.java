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

/** Settings for seasonal chat-header decorations and animated particle effects. */
public class RoflanDecorationPanel extends UniversalFragment {
    private static final int MODE = 1, DENSITY = 2;
    private static final String[] MODES = {
            "Выключено",
            "🎄 Новый год · детальная гирлянда + снег",
            "🌸 Падающая сакура",
            "🎊 Конфетти",
            "🍗 Летящие наггетсы",
            "💗 Парящие сердечки",
            "✨ Звёздная пыль",
            "🍂 Осенние листья",
            "🫧 Мыльные пузыри",
            "🔥 Искры и огоньки",
            "🎵 Музыкальные ноты",
            "🍬 Леденцы и сладости",
            "🪙 Золотые монетки"
    };

    @Override protected CharSequence getTitle() { return "Декорации чатов"; }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        int mode = ChatDecorationConfig.mode();
        items.add(UItem.asHeader("Шапка чатов и эффекты"));
        items.add(UItem.asButton(MODE, "Эффект · " + MODES[mode]));
        items.add(UItem.asButton(DENSITY, "Интенсивность · " + ChatDecorationConfig.intensity()));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader("О режимах"));
        items.add(UItem.asButton(99, "Новогодний режим сочетает украшенную гирлянду, веточки, игрушки, бантики и падающий снег. Остальные режимы — самостоятельные эффекты частиц."));
        items.add(UItem.asButton(100, "Эффекты рисуются поверх списка чатов, не перехватывают нажатия и автоматически обновляются при смене режима."));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        Activity a = getParentActivity();
        if (a == null) return;
        if (item.id == MODE) {
            new AlertDialog.Builder(a).setTitle("Выбери эффект").setItems(MODES, (d, which) -> {
                ChatDecorationConfig.setMode(which);
                refresh();
            }).setNegativeButton("Отмена", null).show();
        } else if (item.id == DENSITY) {
            EditText input = new EditText(a);
            input.setSingleLine(true);
            input.setInputType(2);
            input.setText(String.valueOf(ChatDecorationConfig.intensity()));
            new AlertDialog.Builder(a).setTitle("Интенсивность частиц (1–60)").setView(input)
                    .setPositiveButton("Сохранить", (d, w) -> {
                        try {
                            int value = Integer.parseInt(input.getText().toString());
                            if (value < 1 || value > 60) throw new NumberFormatException();
                            ChatDecorationConfig.setIntensity(value);
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
