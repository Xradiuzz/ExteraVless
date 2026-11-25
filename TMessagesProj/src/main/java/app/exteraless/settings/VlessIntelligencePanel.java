package app.exteraless.settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import app.exteraless.roflan.VlessIntelligence;
import java.util.ArrayList;

/** Explicit, opt-in AI assistant configuration and prompt screen. */
public class VlessIntelligencePanel extends UniversalFragment {
 private static final int CONFIG=1,ASK=2;
 @Override protected CharSequence getTitle(){return "Vless Intelligence";}
 @Override protected void fillItems(ArrayList<UItem> a,UniversalAdapter adapter){a.add(UItem.asHeader("AI assistant"));a.add(UItem.asButton(CONFIG,"API: "+(VlessIntelligence.hasKey()?"ключ сохранён":"не настроен")));a.add(UItem.asButton(ASK,"Задать вопрос / проанализировать текст"));a.add(UItem.asShadow(null));}
 @Override protected void onClick(UItem item,View view,int position,float x,float y){if(item.id==CONFIG)configure();else if(item.id==ASK)ask();}
 @Override protected boolean onLongClick(UItem item,View view,int position,float x,float y){return false;}
 private Activity host(){return getParentActivity();}
 private void configure(){Activity a=host();if(a==null)return;LinearLayout l=new LinearLayout(a);l.setOrientation(1);l.setPadding(AndroidUtilities.dp(16),0,AndroidUtilities.dp(16),0);EditText ep=new EditText(a);ep.setSingleLine(true);ep.setHint("OpenAI-compatible endpoint");ep.setText(VlessIntelligence.endpoint());l.addView(ep);EditText model=new EditText(a);model.setSingleLine(true);model.setHint("Model");model.setText(VlessIntelligence.model());l.addView(model);EditText key=new EditText(a);key.setSingleLine(true);key.setInputType(129);key.setHint(VlessIntelligence.hasKey()?"Ключ сохранён — введи новый для замены":"API key");l.addView(key);new AlertDialog.Builder(a).setTitle("Настройка AI").setMessage("Ключ хранится зашифрованным Android Keystore. Тексты чатов не отправляются автоматически.").setView(l).setPositiveButton("Сохранить",(d,w)->{VlessIntelligence.configure(ep.getText().toString(),model.getText().toString(),key.getText().toString());if(listView!=null)listView.adapter.update(true);Toast.makeText(a,"Настройки сохранены",Toast.LENGTH_SHORT).show();}).setNegativeButton("Отмена",null).show();}
 private void ask(){Activity a=host();if(a==null)return;EditText prompt=new EditText(a);prompt.setMinLines(3);prompt.setHint("Введи вопрос или вставь текст вручную");new AlertDialog.Builder(a).setTitle("Новый запрос").setMessage("Будет отправлен только текст, который ты введёшь сюда.").setView(prompt).setPositiveButton("Отправить",(d,w)->VlessIntelligence.ask(prompt.getText().toString(),(answer,error)->new AlertDialog.Builder(a).setTitle(error==null?"Ответ AI":"Ошибка AI").setMessage(error==null?answer:error.getMessage()).setPositiveButton("OK",null).show())).setNegativeButton("Отмена",null).show();}
}