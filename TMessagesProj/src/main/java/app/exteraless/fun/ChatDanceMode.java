package app.exteraless.fun;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Cells.DialogCell;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

public final class ChatDanceMode {
    public static final int BOUNCE=0,SWING=1,WAVE=2,PULSE=3,CHAOS=4;
    public static final String[] MODE_NAMES={"Пружинка","Маятник","Волна","Пульсация","Хаос"};
    private static final WeakHashMap<View,AnimatorSet> animators=new WeakHashMap<>();
    private static final String PREFS="exteraroflan_chat_dance";
    private static volatile boolean enabled=prefs().getBoolean("enabled",false);
    private ChatDanceMode(){}
    private static android.content.SharedPreferences prefs(){return ApplicationLoader.applicationContext.getSharedPreferences(PREFS,0);}
    public static boolean isEnabled(){return enabled;}
    public static void setEnabled(boolean value){enabled=value;prefs().edit().putBoolean("enabled",value).apply();if(!value)resetAll();}
    public static int mode(){return Math.max(BOUNCE,Math.min(CHAOS,prefs().getInt("mode",WAVE)));}
    public static void setMode(int value){prefs().edit().putInt("mode",Math.max(BOUNCE,Math.min(CHAOS,value))).apply();resetAll();}
    public static int intensity(){return Math.max(1,Math.min(3,prefs().getInt("intensity",2)));}
    public static void setIntensity(int value){prefs().edit().putInt("intensity",Math.max(1,Math.min(3,value))).apply();resetAll();}
    private static void resetAll(){for(Map.Entry<View,AnimatorSet> e:new ArrayList<>(animators.entrySet())){View v=e.getKey();AnimatorSet a=e.getValue();if(a!=null)a.cancel();if(v!=null)reset(v);}animators.clear();}
    private static void reset(View v){v.setTranslationX(0);v.setTranslationY(0);v.setRotation(0);v.setScaleX(1);v.setScaleY(1);v.setAlpha(1);}
    public static void apply(DialogCell cell,boolean shouldDance,int position){
        if(cell==null)return;AnimatorSet current=animators.get(cell);
        if(!shouldDance||!enabled){if(current!=null)current.cancel();animators.remove(cell);reset(cell);return;}
        if(current!=null&&current.isRunning())return;
        float a=AndroidUtilities.dp(3+intensity()*3);ArrayList<Animator> list=new ArrayList<>();
        switch(mode()){
            case SWING:list.add(ObjectAnimator.ofFloat(cell,View.ROTATION,0,-3.2f*intensity(),0,3.2f*intensity(),0));list.add(ObjectAnimator.ofFloat(cell,View.TRANSLATION_Y,0,-a*.35f,0));break;
            case WAVE:list.add(ObjectAnimator.ofFloat(cell,View.TRANSLATION_Y,0,-a,0,a*.35f,0));list.add(ObjectAnimator.ofFloat(cell,View.ROTATION,0,-1.8f,0,1.8f,0));break;
            case PULSE:list.add(ObjectAnimator.ofFloat(cell,View.SCALE_X,1,1+.025f*intensity(),1));list.add(ObjectAnimator.ofFloat(cell,View.SCALE_Y,1,1+.025f*intensity(),1));list.add(ObjectAnimator.ofFloat(cell,View.ALPHA,1,.86f,1));break;
            case CHAOS:list.add(ObjectAnimator.ofFloat(cell,View.TRANSLATION_X,0,a*.7f,-a*.7f,0));list.add(ObjectAnimator.ofFloat(cell,View.TRANSLATION_Y,0,-a,a*.55f,0));list.add(ObjectAnimator.ofFloat(cell,View.ROTATION,0,-4,3,0));break;
            default:list.add(ObjectAnimator.ofFloat(cell,View.TRANSLATION_Y,0,-a,0,a*.45f,0));list.add(ObjectAnimator.ofFloat(cell,View.ROTATION,0,-1.4f,0,1.4f,0));break;
        }
        AnimatorSet dance=new AnimatorSet();dance.playTogether(list);dance.setDuration(620L+(position%4)*55L);dance.setInterpolator(new AccelerateDecelerateInterpolator());dance.setStartDelay((position%9)*45L);
        dance.addListener(new AnimatorListenerAdapter(){boolean cancelled;@Override public void onAnimationCancel(Animator animation){cancelled=true;}@Override public void onAnimationEnd(Animator animation){if(animators.get(cell)==dance)animators.remove(cell);if(!cancelled&&enabled&&cell.getParent()!=null)cell.postDelayed(()->{if(enabled&&cell.getParent()!=null)apply(cell,true,position);else reset(cell);},120L);else reset(cell);}});
        animators.put(cell,dance);dance.start();
    }
}
