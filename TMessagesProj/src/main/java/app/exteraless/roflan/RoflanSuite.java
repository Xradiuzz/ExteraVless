package app.exteraless.roflan;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import java.util.Locale;
/** Local feature data services for ExteraRoflan. */
public final class RoflanSuite {
 private RoflanSuite(){}
 private static SharedPreferences p(){return ApplicationLoader.applicationContext.getSharedPreferences("exteraroflan_suite",Context.MODE_PRIVATE);}
 public static JSONArray rules(){try{return new JSONArray(p().getString("rules","[]"));}catch(Exception e){return new JSONArray();}}
 public static void addRule(String word,int action){if(word==null||word.trim().isEmpty())return;JSONArray a=rules();try{a.put(new JSONObject().put("word",word.trim()).put("action",action));p().edit().putString("rules",a.toString()).apply();}catch(Exception ignored){}}
 public static void clearRules(){p().edit().remove("rules").apply();}
 public static int matchRule(String text){if(text==null)return -1;String s=text.toLowerCase(Locale.ROOT);JSONArray a=rules();for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&s.contains(o.optString("word","").toLowerCase(Locale.ROOT)))return o.optInt("action",-1);}return -1;}
 public static boolean inboxEnabled(){return p().getBoolean("inbox",false);}
 public static void setInboxEnabled(boolean v){p().edit().putBoolean("inbox",v).apply();}
 public static int classify(String text,boolean contact,boolean unread){String s=text==null?"":text.toLowerCase(Locale.ROOT);if(s.contains("?")||s.contains("ответь")||s.contains("reply"))return 1;if(contact)return 0;if(unread&&(s.contains("promo")||s.contains("реклама")))return 2;return 3;}
 public static void saveProfile(long id,String name,int accent,boolean quiet,boolean compact){try{p().edit().putString("profile_"+id,new JSONObject().put("name",name).put("accent",accent).put("quiet",quiet).put("compact",compact).toString()).apply();}catch(Exception ignored){}}
 public static JSONObject profile(long id){try{return new JSONObject(p().getString("profile_"+id,"{}"));}catch(Exception e){return new JSONObject();}}
 public static int xp(){return p().getInt("universe_xp",0);}
 public static void addXp(int n){p().edit().putInt("universe_xp",xp()+Math.max(0,n)).apply();}
 public static boolean petEnabled(){return p().getBoolean("pet_enabled",true);}
 public static void setPetEnabled(boolean v){p().edit().putBoolean("pet_enabled",v).apply();}
}
