package app.exteraless.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import app.exteraless.roflan.RoflanSuite;
import app.exteraless.fun.LizardPetOverlay;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;

/** Dedicated control panel for ExteraRoflan's six new feature systems. */
public class RoflanFeatureHubActivity extends UniversalFragment {
 private static final int AUTOMATOR=1, INBOX=2, NETWORK=3, PROFILES=4, INTELLIGENCE=5, UNIVERSE=6;
 @Override protected CharSequence getTitle(){return "ExteraRoflan Lab";}
 @Override protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter){
  items.add(UItem.asHeader("Automation and organization"));
  items.add(UItem.asButton(AUTOMATOR,"Vless Automator · "+RoflanSuite.rules().length()+" rules"));
  items.add(UItem.asButton(INBOX,"Smart Inbox · "+(RoflanSuite.inboxEnabled()?"Включена":"Выключена")));
  items.add(UItem.asShadow(null));
  items.add(UItem.asHeader("Personalization and diagnostics"));
  items.add(UItem.asButton(PROFILES,"Chat Profiles · локальные профили"));
  items.add(UItem.asButton(NETWORK,"Network Lab · диагностика сети"));
  items.add(UItem.asShadow(null));
  items.add(UItem.asHeader("Intelligence and universe"));
  items.add(UItem.asButton(INTELLIGENCE,"Vless Intelligence · настройки AI"));
  items.add(UItem.asButton(UNIVERSE,"Vless Universe · "+RoflanSuite.xp()+" XP"));
  items.add(UItem.asShadow(null));
 }
 @Override protected void onClick(UItem item,View view,int position,float x,float y){
  switch(item.id){case AUTOMATOR:automator();break;case INBOX:inbox();break;case NETWORK:network();break;case PROFILES:profiles();break;case INTELLIGENCE:intelligence();break;case UNIVERSE:universe();break;}
 }
 private Activity host(){return getParentActivity();}
 private void automator(){
  Activity a=host();if(a==null)return;EditText word=new EditText(a);word.setHint("Слово или фраза для поиска");
  new AlertDialog.Builder(a).setTitle("Новое правило Automator").setMessage("Правило хранится локально. Доступные действия: выделить, тихий режим, отложить.").setView(word)
   .setPositiveButton("Далее",(d,w)->new AlertDialog.Builder(a).setTitle("Действие").setItems(new String[]{"Выделить","Тихий режим","Отложить"},(dd,which)->{RoflanSuite.addRule(word.getText().toString(),which);RoflanSuite.addXp(5);refresh();Toast.makeText(a,"Правило сохранено",Toast.LENGTH_SHORT).show();}).show())
   .setNeutralButton("Очистить правила",(d,w)->{RoflanSuite.clearRules();refresh();}).setNegativeButton("Отмена",null).show();
 }
 private void inbox(){
  Activity a=host();if(a==null)return;boolean enabled=!RoflanSuite.inboxEnabled();RoflanSuite.setInboxEnabled(enabled);refresh();
  new AlertDialog.Builder(a).setTitle("Smart Inbox").setMessage(enabled?"Локальная классификация включена. Категории: важное, требует ответа, шум, позже. Текст переписок не отправляется наружу.":"Smart Inbox выключена.").setPositiveButton("OK",null).show();
 }
 private void profiles(){
  Activity a=host();if(a==null)return;EditText name=new EditText(a);name.setText("Мой профиль");name.setHint("Название профиля");
  new AlertDialog.Builder(a).setTitle("Chat Profiles").setMessage("Создание шаблона профиля. Привязка к конкретному диалогу будет доступна из меню самого чата.").setView(name)
   .setPositiveButton("Сохранить шаблон",(d,w)->{RoflanSuite.saveProfile(0,name.getText().toString(),0xff6e9cff,false,false);RoflanSuite.addXp(10);refresh();Toast.makeText(a,"Шаблон сохранён локально",Toast.LENGTH_SHORT).show();}).setNegativeButton("Отмена",null).show();
 }
 private void network(){
  Activity a=host();if(a==null)return;AlertDialog progress=new AlertDialog.Builder(a).setTitle("Network Lab").setMessage("Проверяю подключение, DNS и HTTPS…").setCancelable(false).create();progress.show();
  new Thread(()->{StringBuilder out=new StringBuilder();try{ConnectivityManager cm=(ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);android.net.Network n=cm.getActiveNetwork();NetworkCapabilities c=n==null?null:cm.getNetworkCapabilities(n);out.append("Сеть: ").append(c==null?"не обнаружена":"подключена").append("\n");}catch(Exception e){out.append("Сеть: ошибка\n");}
   try{long t=System.currentTimeMillis();InetAddress.getByName("telegram.org");out.append("DNS: OK · ").append(System.currentTimeMillis()-t).append(" мс\n");}catch(Exception e){out.append("DNS: ошибка\n");}
   HttpURLConnection h=null;try{long t=System.currentTimeMillis();h=(HttpURLConnection)new URL("https://www.google.com/generate_204").openConnection();h.setConnectTimeout(4000);h.setReadTimeout(4000);out.append("HTTPS: HTTP ").append(h.getResponseCode()).append(" · ").append(System.currentTimeMillis()-t).append(" мс");}catch(Exception e){out.append("HTTPS: недоступен");}finally{if(h!=null)h.disconnect();}
   final String report=out.toString();AndroidUtilities.runOnUIThread(()->{if(progress.isShowing())progress.dismiss();new AlertDialog.Builder(a).setTitle("Результат диагностики").setMessage(report).setPositiveButton("Закрыть",null).show();});
  }).start();
 }
 private void intelligence(){
  Activity a=host();if(a==null)return;LinearLayout box=new LinearLayout(a);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(AndroidUtilities.dp(16),0,AndroidUtilities.dp(16),0);
  EditText endpoint=new EditText(a);endpoint.setSingleLine(true);endpoint.setHint("OpenAI-compatible API endpoint");endpoint.setText(RoflanSuite.aiEndpoint());box.addView(endpoint);
  EditText model=new EditText(a);model.setSingleLine(true);model.setHint("Модель, например gpt-4o-mini");model.setText(RoflanSuite.aiModel());box.addView(model);
  new AlertDialog.Builder(a).setTitle("Vless Intelligence").setMessage("Укажи совместимый endpoint и модель. AI-запросы и передача текста будут добавлены после настройки безопасного хранения ключа. Пока ничего не отправляется.").setView(box)
   .setPositiveButton("Сохранить",(d,w)->{RoflanSuite.setAiConfig(endpoint.getText().toString(),model.getText().toString());Toast.makeText(a,"Конфигурация сохранена",Toast.LENGTH_SHORT).show();}).setNegativeButton("Отмена",null).show();
 }
 private void universe(){
  Activity a=host();if(a==null)return;new AlertDialog.Builder(a).setTitle("Vless Universe")
   .setMessage("Опыт: "+RoflanSuite.xp()+" XP\nПитомец: "+(RoflanSuite.petEnabled()?"разрешён":"отключён")+"\n\nЯщерица — первый компаньон Universe.")
   .setPositiveButton(RoflanSuite.petEnabled()?"Отключить питомца":"Включить питомца",(d,w)->{boolean on=!RoflanSuite.petEnabled();RoflanSuite.setPetEnabled(on);if(on)LizardPetOverlay.toggle(a);else if(LizardPetOverlay.isActive())LizardPetOverlay.toggle(a);refresh();})
   .setNeutralButton("Получить 5 XP",(d,w)->{RoflanSuite.addXp(5);refresh();}).setNegativeButton("Закрыть",null).show();
 }
 private void refresh(){if(getAdapter()!=null)getAdapter().update(true);}
}