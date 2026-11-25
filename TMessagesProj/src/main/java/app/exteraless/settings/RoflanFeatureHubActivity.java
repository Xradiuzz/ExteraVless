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
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import app.exteraless.roflan.RoflanSuite;
import app.exteraless.fun.LizardPetOverlay;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;

public class RoflanFeatureHubActivity extends UniversalFragment {
 private static final int AUTO=1,INBOX=2,PROFILE=3,NETWORK=4,AI=5,UNIVERSE=6,TOOLS=7,ICON=8,NOTIFY=9,DANCE=10,DECOR=11;
 @Override protected CharSequence getTitle(){return "ExteraRoflan Lab";}
 @Override protected void fillItems(ArrayList<UItem> a,UniversalAdapter adapter){
  a.add(UItem.asHeader("Automation"));a.add(UItem.asButton(AUTO,"Roflan Automator · "+RoflanSuite.rules().length()+" rules"));a.add(UItem.asButton(INBOX,"Smart Inbox · "+(RoflanSuite.inboxEnabled()?"ON":"OFF")));a.add(UItem.asShadow(null));
  a.add(UItem.asHeader("Personalization"));a.add(UItem.asButton(PROFILE,"Chat Profiles"));a.add(UItem.asButton(NETWORK,"Network Lab"));a.add(UItem.asShadow(null));
  a.add(UItem.asHeader("Intelligence and fun"));a.add(UItem.asButton(AI,"Roflan AI"));a.add(UItem.asButton(UNIVERSE,"Roflan Universe · "+RoflanSuite.xp()+" XP"));a.add(UItem.asButton(TOOLS,"ExteraRoflan Tools · приватность, история, бэкап"));a.add(UItem.asShadow(null));
  a.add(UItem.asHeader("Creator Studio"));a.add(UItem.asButton(ICON,"Icon Composer · свои ярлыки"));a.add(UItem.asButton(NOTIFY,"Notification Studio · стили и предпросмотр"));a.add(UItem.asButton(DANCE,"Chat Motion Studio · танцующие чаты"));a.add(UItem.asButton(DECOR,"Particle Studio · шапки и эффекты"));a.add(UItem.asShadow(null));
 }
 @Override protected void onClick(UItem item,View view,int position,float x,float y){switch(item.id){case AUTO:auto();break;case INBOX:inbox();break;case PROFILE:profile();break;case NETWORK:network();break;case AI:ai();break;case UNIVERSE:universe();break;case TOOLS:presentFragment(new RoflanUtilityPanel());break;case ICON:presentFragment(new app.exteraless.appicons.RoflanIconComposerActivity());break;case NOTIFY:presentFragment(new RoflanNotificationStudioActivity());break;case DANCE:presentFragment(new RoflanDanceStudioActivity());break;case DECOR:presentFragment(new RoflanDecorationPanel());break;}}
 @Override protected boolean onLongClick(UItem item,View view,int position,float x,float y){return false;}
 private Activity host(){return getParentActivity();}
 private void refresh(){if(listView!=null)listView.adapter.update(true);}
 private void auto(){Activity a=host();if(a==null)return;EditText e=new EditText(a);e.setHint("Ключевое слово или фраза");new AlertDialog.Builder(a).setTitle("Roflan Automator").setMessage("Локальное правило. Выбери действие после ввода условия.").setView(e).setPositiveButton("Далее",(d,w)->new AlertDialog.Builder(a).setTitle("Действие").setItems(new String[]{"Выделить","Тихий режим","Отложить"},(dd,n)->{RoflanSuite.addRule(e.getText().toString(),n);RoflanSuite.addXp(5);refresh();}).show()).setNeutralButton("Очистить",(d,w)->{RoflanSuite.clearRules();refresh();}).setNegativeButton("Отмена",null).show();}
 private void inbox(){Activity a=host();if(a==null)return;boolean b=!RoflanSuite.inboxEnabled();RoflanSuite.setInboxEnabled(b);refresh();new AlertDialog.Builder(a).setTitle("Smart Inbox").setMessage(b?"Включена локальная классификация: важное, требует ответа, шум, позже. Тексты не отправляются на сервер.":"Выключена.").setPositiveButton("OK",null).show();}
 private void profile(){Activity a=host();if(a==null)return;EditText e=new EditText(a);e.setText("Мой профиль");new AlertDialog.Builder(a).setTitle("Chat Profiles").setMessage("Сохраняет локальный шаблон. Привязку к отдельному диалогу добавим через меню чата.").setView(e).setPositiveButton("Сохранить",(d,w)->{RoflanSuite.saveProfile(0,e.getText().toString(),0xff6e9cff,false,false);RoflanSuite.addXp(10);Toast.makeText(a,"Шаблон сохранён",Toast.LENGTH_SHORT).show();}).setNegativeButton("Отмена",null).show();}
 private void network(){Activity a=host();if(a==null)return;new AlertDialog.Builder(a).setTitle("Network Lab").setMessage("Проверяю сеть, DNS и HTTPS…").setPositiveButton("Запустить",(d,w)->new Thread(()->{StringBuilder b=new StringBuilder();try{ConnectivityManager cm=(ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);android.net.Network n=cm.getActiveNetwork();NetworkCapabilities c=n==null?null:cm.getNetworkCapabilities(n);b.append("Сеть: ").append(c==null?"нет":"подключена").append("\n");}catch(Exception e){b.append("Сеть: ошибка\n");}try{long t=System.currentTimeMillis();InetAddress.getByName("telegram.org");b.append("DNS: OK · ").append(System.currentTimeMillis()-t).append(" мс\n");}catch(Exception e){b.append("DNS: ошибка\n");}HttpURLConnection h=null;try{long t=System.currentTimeMillis();h=(HttpURLConnection)new URL("https://www.google.com/generate_204").openConnection();h.setConnectTimeout(4000);h.setReadTimeout(4000);b.append("HTTPS: HTTP ").append(h.getResponseCode()).append(" · ").append(System.currentTimeMillis()-t).append(" мс");}catch(Exception e){b.append("HTTPS: недоступен");}finally{if(h!=null)h.disconnect();}String result=b.toString();AndroidUtilities.runOnUIThread(()->new AlertDialog.Builder(a).setTitle("Диагностика").setMessage(result).setPositiveButton("Закрыть",null).show());}).start()).setNegativeButton("Отмена",null).show();}
 private void ai(){presentFragment(new RoflanIntelligencePanel());}
 private void universe(){Activity a=host();if(a==null)return;new AlertDialog.Builder(a).setTitle("Roflan Universe").setMessage("Опыт: "+RoflanSuite.xp()+" XP\nЯщерица — первый питомец Universe.").setPositiveButton(LizardPetOverlay.isActive()?"Убрать питомца":"Позвать питомца",(d,w)->{LizardPetOverlay.toggle(a);RoflanSuite.addXp(1);refresh();}).setNeutralButton("+5 XP",(d,w)->{RoflanSuite.addXp(5);refresh();}).setNegativeButton("Закрыть",null).show();}
}