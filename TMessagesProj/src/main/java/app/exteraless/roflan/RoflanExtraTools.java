package app.exteraless.roflan;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Local-only helpers for ExteraRoflan's privacy, history and utility tools. */
public final class RoflanExtraTools {
 private static final String PREFS="exteraroflan_extras";
 private static final Pattern URL=Pattern.compile("(?i)https?://[^\\s<>]+");
 private RoflanExtraTools(){}
 private static SharedPreferences p(){return ApplicationLoader.applicationContext.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
 public static void record(String title,String body){try{JSONArray a=history();a.put(new JSONObject().put("title",title==null?"":title).put("body",body==null?"":body).put("time",System.currentTimeMillis()));while(a.length()>100)a.remove(0);p().edit().putString("history",a.toString()).apply();}catch(Exception ignored){}}
 public static JSONArray history(){try{return new JSONArray(p().getString("history","[]"));}catch(Exception e){return new JSONArray();}}
 public static void clearHistory(){p().edit().remove("history").apply();}
 public static int historyCount(){return history().length();}
 /** Heuristic warning only; it does not claim a link is malicious. */
 public static boolean looksSuspicious(String text){if(text==null)return false;java.util.regex.Matcher m=URL.matcher(text);while(m.find()){try{String host=Uri.parse(m.group()).getHost();if(host==null)return true;host=host.toLowerCase(java.util.Locale.ROOT);if(host.startsWith("xn--")||host.contains(".xn--")||host.matches("[0-9.]+")||host.equals("bit.ly")||host.equals("tinyurl.com")||host.equals("t.co")||host.equals("is.gd"))return true;}catch(Exception e){return true;}}return false;}
 public static void setFocusMode(boolean enabled){p().edit().putBoolean("focus",enabled).apply();}
 public static boolean focusMode(){return p().getBoolean("focus",false);}
 public static void allowDialog(long id,boolean allowed){Set<String>s=new HashSet<>(p().getStringSet("focus_allow",new HashSet<>()));if(allowed)s.add(String.valueOf(id));else s.remove(String.valueOf(id));p().edit().putStringSet("focus_allow",s).apply();}
 public static boolean isAllowed(long id){return p().getStringSet("focus_allow",new HashSet<>()).contains(String.valueOf(id));}
 public static String privacyReport(){return "История локальных уведомлений: "+historyCount()+" записей\nFocus Mode: "+(focusMode()?"включён":"выключен")+"\nAI-ключ: хранится в Android Keystore\nАвтоматическая отправка переписок: отключена";}
 public static String exportBackup(){try{JSONObject root=new JSONObject();root.put("version",1);root.put("suite",new JSONObject(ApplicationLoader.applicationContext.getSharedPreferences("exteraroflan_suite",0).getAll()));root.put("notifications",new JSONObject(ApplicationLoader.applicationContext.getSharedPreferences("exteraless_notification_studio",0).getAll()));root.put("extras",new JSONObject(p().getAll()));return root.toString(2);}catch(Exception e){return "{}";}}
 public static boolean importBackup(String json){try{JSONObject root=new JSONObject(json);restore("exteraroflan_suite",root.optJSONObject("suite"));restore("exteraless_notification_studio",root.optJSONObject("notifications"));restore(PREFS,root.optJSONObject("extras"));return true;}catch(Exception e){return false;}}
 private static void restore(String name,JSONObject data){if(data==null)return;SharedPreferences.Editor edit=ApplicationLoader.applicationContext.getSharedPreferences(name,0).edit();java.util.Iterator<String> it=data.keys();while(it.hasNext()){String k=it.next();Object v=data.opt(k);if(v instanceof Boolean)edit.putBoolean(k,(Boolean)v);else if(v instanceof Integer)edit.putInt(k,(Integer)v);else if(v instanceof Long)edit.putLong(k,(Long)v);else if(v instanceof Number)edit.putLong(k,((Number)v).longValue());else if(v instanceof String)edit.putString(k,(String)v);}edit.apply();}
 public static long cacheBytes(){return size(ApplicationLoader.applicationContext.getCacheDir());}
 private static long size(File f){if(f==null||!f.exists())return 0;if(f.isFile())return f.length();long n=0;File[] children=f.listFiles();if(children!=null)for(File child:children)n+=size(child);return n;}
}