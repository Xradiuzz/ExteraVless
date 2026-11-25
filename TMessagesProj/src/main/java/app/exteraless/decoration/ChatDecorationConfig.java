package app.exteraless.decoration;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.telegram.messenger.ApplicationLoader;

public final class ChatDecorationConfig {
    public static final int OFF=0, CHRISTMAS=1, SAKURA=2, CONFETTI=3, NUGGETS=4, HEARTS=5,
            STARS=6, LEAVES=7, BUBBLES=8, FIRE=9, MUSIC=10, CANDY=11, COINS=12, CUSTOM_IMAGE=13;
    public static final int MAX_MODE=CUSTOM_IMAGE;
    private static final String PREFS="exteraroflan_decorations";
    private ChatDecorationConfig(){}

    private static SharedPreferences prefs(){
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }
    public static int mode(){return Math.max(OFF,Math.min(MAX_MODE,prefs().getInt("mode",OFF)));}
    public static void setMode(int value){prefs().edit().putInt("mode",Math.max(OFF,Math.min(MAX_MODE,value))).apply();ChatSeasonalDecorationView.refreshAll();}
    public static int intensity(){return Math.max(1,Math.min(60,prefs().getInt("intensity",24)));}
    public static void setIntensity(int value){prefs().edit().putInt("intensity",Math.max(1,Math.min(60,value))).apply();ChatSeasonalDecorationView.refreshAll();}
    public static int headerStyle(){return Math.max(0,Math.min(3,prefs().getInt("header_style",1)));}
    public static void setHeaderStyle(int value){prefs().edit().putInt("header_style",Math.max(0,Math.min(3,value))).apply();ChatSeasonalDecorationView.refreshAll();}
    public static boolean snowEnabled(){return prefs().getBoolean("snow_enabled",true);}
    public static void setSnowEnabled(boolean value){prefs().edit().putBoolean("snow_enabled",value).apply();ChatSeasonalDecorationView.refreshAll();}
    public static int particleSize(){return Math.max(50,Math.min(180,prefs().getInt("particle_size",100)));}
    public static void setParticleSize(int value){prefs().edit().putInt("particle_size",Math.max(50,Math.min(180,value))).apply();ChatSeasonalDecorationView.refreshAll();}
    public static int particleSpeed(){return Math.max(30,Math.min(200,prefs().getInt("particle_speed",100)));}
    public static void setParticleSpeed(int value){prefs().edit().putInt("particle_speed",Math.max(30,Math.min(200,value))).apply();ChatSeasonalDecorationView.refreshAll();}
    public static int particleOpacity(){return Math.max(80,Math.min(255,prefs().getInt("particle_opacity",230)));}
    public static void setParticleOpacity(int value){prefs().edit().putInt("particle_opacity",Math.max(80,Math.min(255,value))).apply();ChatSeasonalDecorationView.refreshAll();}
    public static String[] customParticleUris(){
        try{JSONArray a=new JSONArray(prefs().getString("custom_particle_uris","[]"));String[] out=new String[Math.min(8,a.length())];for(int i=0;i<out.length;i++)out[i]=a.optString(i,"");return out;}
        catch(Exception e){return new String[0];}
    }
    public static void setCustomParticleUris(String[] values){
        JSONArray a=new JSONArray();if(values!=null)for(String value:values){if(value!=null&&!value.trim().isEmpty()&&a.length()<8)a.put(value);}
        prefs().edit().putString("custom_particle_uris",a.toString()).putInt("mode",CUSTOM_IMAGE).apply();
        ChatSeasonalDecorationView.refreshAll();
    }
}
