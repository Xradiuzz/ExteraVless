package app.exteraless.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import app.exteraless.decoration.ChatDecorationConfig;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import java.util.ArrayList;

public class RoflanDecorationPanel extends UniversalFragment {
    private static final int MODE=1,DENSITY=2,HEADER=3,SNOW=4,COMPOSER=5;
    private static final String[] MODES={
        "Выключено","🎄 Новый год · гирлянда + снег","🌸 Падающая сакура","🎊 Конфетти",
        "🍗 Летящие наггетсы","💗 Парящие сердечки","✨ Звёздная пыль","🍂 Осенние листья",
        "🫧 Мыльные пузыри","🔥 Искры и огоньки","🎵 Музыкальные ноты","🍬 Леденцы и сладости",
        "🪙 Золотые монетки","🖼️ Свои изображения"
    };
    private static final String[] HEADERS={"Без шапки","Детальная новогодняя","Минималистичная","Северное сияние"};
    @Override protected CharSequence getTitle(){return "Декорации чатов";}
    @Override protected void fillItems(ArrayList<UItem> items,UniversalAdapter adapter){
        items.add(UItem.asHeader("Шапка чатов и эффекты"));
        items.add(UItem.asButton(MODE,"Эффект · "+MODES[ChatDecorationConfig.mode()]));
        items.add(UItem.asButton(DENSITY,"Количество частиц · "+ChatDecorationConfig.intensity()));
        items.add(UItem.asButton(HEADER,"Шапка · "+HEADERS[ChatDecorationConfig.headerStyle()]));
        items.add(UItem.asButton(SNOW,"Падающий снег · "+(ChatDecorationConfig.snowEnabled()?"Включён":"Выключен")));
        items.add(UItem.asButton(COMPOSER,"Открыть конструктор частиц"));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader("Пользовательские эффекты"));
        items.add(UItem.asButton(90,"Загрузи до 8 изображений, настрой масштаб, скорость, прозрачность, плотность и стиль шапки. Предпросмотр обновляется до применения."));
    }
    @Override protected void onClick(UItem item,View view,int position,float x,float y){
        Activity a=getParentActivity();if(a==null)return;
        if(item.id==MODE)new android.app.AlertDialog.Builder(a).setTitle("Выбери эффект").setItems(MODES,(d,w)->{
            if(w==ChatDecorationConfig.CUSTOM_IMAGE){presentFragment(new RoflanParticleComposerActivity());}
            else{ChatDecorationConfig.setMode(w);refresh();}
        }).setNegativeButton("Отмена",null).show();
        else if(item.id==DENSITY){EditText e=new EditText(a);e.setSingleLine(true);e.setInputType(2);e.setText(String.valueOf(ChatDecorationConfig.intensity()));
            new android.app.AlertDialog.Builder(a).setTitle("Количество частиц · 1–60").setView(e).setPositiveButton("Сохранить",(d,w)->{try{int n=Integer.parseInt(e.getText().toString());if(n<1||n>60)throw new NumberFormatException();ChatDecorationConfig.setIntensity(n);refresh();}catch(Exception ex){Toast.makeText(a,"Введи число от 1 до 60",Toast.LENGTH_SHORT).show();}}).setNegativeButton("Отмена",null).show();}
        else if(item.id==HEADER)new android.app.AlertDialog.Builder(a).setTitle("Стиль шапки").setItems(HEADERS,(d,w)->{ChatDecorationConfig.setHeaderStyle(w);refresh();}).setNegativeButton("Отмена",null).show();
        else if(item.id==SNOW){ChatDecorationConfig.setSnowEnabled(!ChatDecorationConfig.snowEnabled());refresh();}
        else if(item.id==COMPOSER)presentFragment(new RoflanParticleComposerActivity());
    }
    @Override protected boolean onLongClick(UItem item,View view,int position,float x,float y){return false;}
    private void refresh(){if(listView!=null)listView.adapter.update(true);if(getParentLayout()!=null)getParentLayout().rebuildAllFragmentViews(false,false);}
}
