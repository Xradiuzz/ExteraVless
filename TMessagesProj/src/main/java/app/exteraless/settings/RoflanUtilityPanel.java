package app.exteraless.settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import app.exteraless.roflan.RoflanExtraTools;
import app.exteraless.roflan.RoflanIntelligence;
import java.util.ArrayList;
import java.io.File;

public class RoflanUtilityPanel extends UniversalFragment {
 private static final int SCAM=1,FOCUS=2,REPLAY=3,STATS=4,BACKUP=5,MEDIA=6,PRIVACY=7,REPLY=8,TRANSLATE=9;
 @Override protected CharSequence getTitle(){return "ExteraRoflan Tools";}
 @Override protected void fillItems(ArrayList<UItem>a,UniversalAdapter adapter){a.add(UItem.asHeader("Safety and focus"));a.add(UItem.asButton(SCAM,"Anti-Scam · проверка ссылки"));a.add(UItem.asButton(FOCUS,"Focus Mode · "+(RoflanExtraTools.focusMode()?"ON":"OFF")));a.add(UItem.asButton(PRIVACY,"Privacy Center"));a.add(UItem.asShadow(null));a.add(UItem.asHeader("History and intelligence"));a.add(UItem.asButton(REPLAY,"Chat Replay · локальная история"));a.add(UItem.asButton(REPLY,"Smart Reply Box"));a.add(UItem.asButton(TRANSLATE,"Chat Translator"));a.add(UItem.asButton(STATS,"Chat Statistics"));a.add(UItem.asShadow(null));a.add(UItem.asHeader("Storage"));a.add(UItem.asButton(BACKUP,"Backup / Restore"));a.add(UItem.asButton(MEDIA,"Media Manager · кэш"));a.add(UItem.asShadow(null));}
 @Override protected void onClick(UItem item,View v,int p,float x,float y){switch(item.id){case SCAM:scam();break;case FOCUS:focus();break;case REPLAY:replay();break;case STATS:stats();break;case BACKUP:backup();break;case MEDIA:media();break;case PRIVACY:show("Privacy Center",RoflanExtraTools.privacyReport());break;case REPLY:reply();break;case TRANSLATE:translate();break;}}
 @Override protected boolean onLongClick(UItem item,View v,int p,float x,float y){return false;}
 private Activity host(){return getParentActivity();}
 private void show(String title,String body){Activity a=host();if(a!=null)new AlertDialog.Builder(a).setTitle(title).setMessage(body).setPositiveButton("OK",null).show();}
 private void scam(){Activity a=host();if(a==null)return;EditText e=new EditText(a);e.setMinLines(2);e.setHint("Вставь ссылку или сообщение");new AlertDialog.Builder(a).setTitle("Anti-Scam").setMessage("Локальная эвристика: punycode, IP-адреса и популярные сокращатели ссылок. Это предупреждение, а не антивирусная гарантия.").setView(e).setPositiveButton("Проверить",(d,w)->show("Результат",RoflanExtraTools.looksSuspicious(e.getText().toString())?"⚠️ Ссылка выглядит подозрительно. Не открывай её, пока не проверишь домен.":"Явных признаков из локального списка не найдено. Это не доказывает безопасность ссылки.")).setNegativeButton("Отмена",null).show();}
 private void focus(){boolean b=!RoflanExtraTools.focusMode();RoflanExtraTools.setFocusMode(b);if(listView!=null)listView.adapter.update(true);show("Focus Mode",b?"Режим фокуса включён. Разрешённые диалоги можно будет добавить из меню чатов.":"Режим фокуса выключен.");}
 private void replay(){JSONArray a=RoflanExtraTools.history();StringBuilder b=new StringBuilder();for(int i=Math.max(0,a.length()-25);i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null)b.append(o.optString("title")).append(": ").append(o.optString("body")).append("\n\n");}show("Chat Replay · последние уведомления",b.length()==0?"История пока пуста. Сохраняются только уведомления, полученные после установки этой версии.":b.toString());}
 private void reply(){Activity a=host();if(a==null)return;JSONArray h=RoflanExtraTools.history();StringBuilder b=new StringBuilder();for(int i=Math.max(0,h.length()-30);i<h.length();i++){JSONObject o=h.optJSONObject(i);if(o!=null&&o.optString("body").contains("?"))b.append(o.optString("title")).append(": ").append(o.optString("body")).append("\n");}EditText e=new EditText(a);e.setMinLines(3);e.setText(b.toString());e.setHint("Вставь сообщение, на которое нужно ответить");new AlertDialog.Builder(a).setTitle("Smart Reply Box").setMessage("Собери контекст вручную и отправь его AI по своему желанию.").setView(e).setPositiveButton("Предложить ответ",(d,w)->RoflanIntelligence.ask("Предложи несколько вариантов ответа на сообщение:\n"+e.getText(),(answer,error)->show(error==null?"Варианты ответа":"Ошибка AI",error==null?answer:error.getMessage()))).setNegativeButton("Отмена",null).show();}
 private void translate(){Activity a=host();if(a==null)return;EditText e=new EditText(a);e.setMinLines(3);e.setHint("Текст для перевода");new AlertDialog.Builder(a).setTitle("Chat Translator").setMessage("Перевод через настроенный Vless Intelligence API.").setView(e).setPositiveButton("Перевести",(d,w)->RoflanIntelligence.ask("Переведи на русский язык, сохранив смысл и тон:\n"+e.getText(),(answer,error)->show(error==null?"Перевод":"Ошибка AI",error==null?answer:error.getMessage()))).setNegativeButton("Отмена",null).show();}
 private void stats(){long bytes=RoflanExtraTools.cacheBytes();show("Chat Statistics","Локально сохранённых уведомлений: "+RoflanExtraTools.historyCount()+"\nКэш приложения: "+(bytes/1024/1024)+" МБ\n\nСтатистика строится только по уведомлениям, сохранённым после установки ExteraRoflan Lab.");}
 private void backup(){Activity a=host();if(a==null)return;EditText e=new EditText(a);e.setText(RoflanExtraTools.exportBackup());e.setMinLines(5);e.setTextSize(11);new AlertDialog.Builder(a).setTitle("Backup / Restore").setMessage("Экспортируй JSON и сохрани его в надёжном месте. AI-ключ не входит в резервную копию. Для восстановления вставь JSON в поле.").setView(e).setPositiveButton("Импортировать",(d,w)->{boolean ok=RoflanExtraTools.importBackup(e.getText().toString());show("Восстановление",ok?"Настройки импортированы.":"Не удалось прочитать резервную копию.");}).setNeutralButton("Копировать",(d,w)->{ClipboardManager cm=(ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText("ExteraRoflan backup",e.getText()));Toast.makeText(a,"Резервная копия скопирована",Toast.LENGTH_SHORT).show();}).setNegativeButton("Закрыть",null).show();}
 private void media(){Activity a=host();if(a==null)return;new AlertDialog.Builder(a).setTitle("Media Manager").setMessage("Кэш приложения: "+(RoflanExtraTools.cacheBytes()/1024/1024)+" МБ. Очистка затрагивает только временный кэш, не удаляет сообщения или облачные медиа.").setPositiveButton("Очистить кэш",(d,w)->{delete(a.getCacheDir());show("Media Manager","Кэш очищен.");}).setNegativeButton("Отмена",null).show();}
 private void delete(File f){if(f==null||!f.exists())return;if(f.isDirectory()){File[] files=f.listFiles();if(files!=null)for(File child:files)delete(child);}f.delete();}
}