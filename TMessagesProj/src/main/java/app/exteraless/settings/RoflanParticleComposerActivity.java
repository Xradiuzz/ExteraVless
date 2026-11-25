package app.exteraless.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import app.exteraless.decoration.ChatDecorationConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import java.io.InputStream;
import java.util.ArrayList;

public class RoflanParticleComposerActivity extends BaseFragment {
    private static final int PICK=7311;
    private static final String[] HEADERS={"Без шапки","Детальная новогодняя","Минималистичная","Северное сияние"};
    private ParticlePreview preview;
    private TextView imageStatus,headerStatus,snowStatus;
    @Override public View createView(Context c){
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);actionBar.setTitle("Конструктор частиц");
        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText),false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){@Override public void onItemClick(int id){if(id==-1)finishFragment();}});
        android.widget.ScrollView scroll=new android.widget.ScrollView(c);
        LinearLayout root=new LinearLayout(c);root.setOrientation(1);root.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(24));root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        preview=new ParticlePreview(c);root.addView(preview,LayoutHelper.createLinear(-1,210,0,0,0,14));
        imageStatus=action(c,imageLabel());imageStatus.setOnClickListener(v->pickImages());root.addView(imageStatus,LayoutHelper.createLinear(-1,48,0,0,0,8));
        headerStatus=action(c,"Стиль шапки · "+HEADERS[ChatDecorationConfig.headerStyle()]);headerStatus.setOnClickListener(v->chooseHeader());root.addView(headerStatus,LayoutHelper.createLinear(-1,48,0,0,0,8));
        snowStatus=action(c,"Снег · "+(ChatDecorationConfig.snowEnabled()?"Включён":"Выключен"));snowStatus.setOnClickListener(v->{ChatDecorationConfig.setSnowEnabled(!ChatDecorationConfig.snowEnabled());refresh();});root.addView(snowStatus,LayoutHelper.createLinear(-1,48,0,0,0,8));
        addSlider(root,c,"Плотность",1,60,ChatDecorationConfig.intensity(),ChatDecorationConfig::setIntensity);
        addSlider(root,c,"Размер частиц",50,180,ChatDecorationConfig.particleSize(),ChatDecorationConfig::setParticleSize);
        addSlider(root,c,"Скорость",30,200,ChatDecorationConfig.particleSpeed(),ChatDecorationConfig::setParticleSpeed);
        addSlider(root,c,"Прозрачность",80,255,ChatDecorationConfig.particleOpacity(),ChatDecorationConfig::setParticleOpacity);
        TextView apply=action(c,"Применить эффект в чатах");apply.setTextColor(Color.WHITE);apply.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(12),0xFF229ED9));apply.setOnClickListener(v->{if(ChatDecorationConfig.customParticleUris().length==0){Toast.makeText(c,"Сначала загрузи хотя бы одно изображение",Toast.LENGTH_SHORT).show();return;}ChatDecorationConfig.setMode(ChatDecorationConfig.CUSTOM_IMAGE);finishFragment();});root.addView(apply,LayoutHelper.createLinear(-1,50,0,0,0,12));
        TextView note=label(c,"Можно выбрать до 8 изображений. Они будут чередоваться в полёте; настройки сохраняются автоматически и не требуют повторной загрузки.",12);note.setGravity(Gravity.CENTER);root.addView(note);
        scroll.setFillViewport(true);scroll.addView(root);android.widget.FrameLayout frame=new android.widget.FrameLayout(c);frame.addView(scroll,LayoutHelper.createFrame(-1,-1));frame.addView(actionBar,LayoutHelper.createFrame(-1,-2,Gravity.TOP));fragmentView=frame;return frame;
    }
    private void addSlider(LinearLayout root,Context c,String title,int min,int max,int value,java.util.function.IntConsumer setter){
        TextView t=label(c,title+" · "+value,14);root.addView(t,LayoutHelper.createLinear(-1,-2,0,0,0,2));
        SeekBar bar=new SeekBar(c);bar.setMax(max-min);bar.setProgress(value-min);bar.setProgressTintList(android.content.res.ColorStateList.valueOf(Theme.getColor(Theme.key_featuredStickers_addButton)));bar.setThumbTintList(android.content.res.ColorStateList.valueOf(Theme.getColor(Theme.key_featuredStickers_addButton)));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean from){int v=min+p;t.setText(title+" · "+v);setter.accept(v);if(preview!=null)preview.invalidate();}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        root.addView(bar,LayoutHelper.createLinear(-1,42,0,0,0,8));
    }
    private TextView label(Context c,String s,int size){TextView t=new TextView(c);t.setText(s);t.setTextSize(size);t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));return t;}
    private TextView action(Context c,String s){TextView t=label(c,s,14);t.setGravity(Gravity.CENTER);t.setPadding(AndroidUtilities.dp(12),0,AndroidUtilities.dp(12),0);t.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(12),Theme.getColor(Theme.key_windowBackgroundWhite)));return t;}
    private String imageLabel(){int n=ChatDecorationConfig.customParticleUris().length;return n==0?"Загрузить изображения частиц":"Изображения · "+n+" из 8";}
    private void pickImages(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,PICK);}
    @Override public void onActivityResultFragment(int requestCode,int resultCode,Intent data){super.onActivityResultFragment(requestCode,resultCode,data);if(requestCode!=PICK||resultCode!=Activity.RESULT_OK||data==null)return;ArrayList<String> uris=new ArrayList<>();if(data.getClipData()!=null){for(int i=0;i<data.getClipData().getItemCount()&&uris.size()<8;i++){Uri u=data.getClipData().getItemAt(i).getUri();if(u!=null)uris.add(u.toString());}}else if(data.getData()!=null)uris.add(data.getData().toString());for(String s:uris){try{Uri u=Uri.parse(s);getParentActivity().getContentResolver().takePersistableUriPermission(u,data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}}if(!uris.isEmpty()){ChatDecorationConfig.setCustomParticleUris(uris.toArray(new String[0]));refresh();}}
    private void chooseHeader(){Activity a=getParentActivity();if(a==null)return;new AlertDialog.Builder(a).setTitle("Стиль шапки").setItems(HEADERS,(d,w)->{ChatDecorationConfig.setHeaderStyle(w);refresh();}).setNegativeButton("Отмена",null).show();}
    private void refresh(){if(imageStatus!=null)imageStatus.setText(imageLabel());if(headerStatus!=null)headerStatus.setText("Стиль шапки · "+HEADERS[ChatDecorationConfig.headerStyle()]);if(snowStatus!=null)snowStatus.setText("Снег · "+(ChatDecorationConfig.snowEnabled()?"Включён":"Выключен"));if(preview!=null)preview.reload();}
    @Override public void onFragmentDestroy(){if(preview!=null)preview.release();super.onFragmentDestroy();}

    private static final class ParticlePreview extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);private final RectF r=new RectF();private final float[] x=new float[18],y=new float[18],phase=new float[18];private Bitmap[] bitmaps;private final Context context;private final long start=System.currentTimeMillis();
        ParticlePreview(Context c){super(c);context=c;setLayerType(View.LAYER_TYPE_HARDWARE,null);java.util.Random random=new java.util.Random(73);for(int i=0;i<x.length;i++){x[i]=random.nextFloat();y[i]=random.nextFloat();phase[i]=random.nextFloat()*6.28f;}bitmaps=load(c);}
        private Bitmap[] load(Context c){String[] uris=ChatDecorationConfig.customParticleUris();Bitmap[] out=new Bitmap[uris.length];for(int i=0;i<uris.length;i++){try(InputStream in=c.getContentResolver().openInputStream(Uri.parse(uris[i]))){BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=4;out[i]=BitmapFactory.decodeStream(in,null,o);}catch(Exception ignored){}}return out;}
        void reload(){release();bitmaps=load(context);invalidate();}
        void release(){if(bitmaps!=null)for(Bitmap b:bitmaps)if(b!=null&&!b.isRecycled())b.recycle();bitmaps=new Bitmap[0];}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();p.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));c.drawRoundRect(0,0,w,h,AndroidUtilities.dp(16),AndroidUtilities.dp(16),p);
            p.setColor(0xFF229ED9);c.drawRoundRect(0,0,w,AndroidUtilities.dp(42),AndroidUtilities.dp(16),AndroidUtilities.dp(16),p);p.setColor(Color.WHITE);p.setTextSize(AndroidUtilities.dp(14));p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);c.drawText("Telegram  ·  предпросмотр",AndroidUtilities.dp(14),AndroidUtilities.dp(26),p);
            int header=ChatDecorationConfig.headerStyle();if(header>0){p.setColor(header==3?0xFF8BDDD5:0xFF2E7D4F);p.setStrokeWidth(AndroidUtilities.dp(2));c.drawLine(0,AndroidUtilities.dp(47),w,AndroidUtilities.dp(47),p);}
            float t=(System.currentTimeMillis()-start)/1000f;int count=Math.min(18,ChatDecorationConfig.intensity());for(int i=0;i<count;i++){float px=x[i]*w+(float)Math.sin(t+phase[i])*AndroidUtilities.dp(9);float py=AndroidUtilities.dp(48)+((y[i]*h+t*ChatDecorationConfig.particleSpeed()/160f*h*.25f)%(h-AndroidUtilities.dp(48)));float s=AndroidUtilities.dp(4*ChatDecorationConfig.particleSize()/100f);if(bitmaps.length>0&&bitmaps[i%bitmaps.length]!=null){Bitmap b=bitmaps[i%bitmaps.length];r.set(px-s*1.5f,py-s*1.5f,px+s*1.5f,py+s*1.5f);p.setAlpha(ChatDecorationConfig.particleOpacity());c.drawBitmap(b,null,r,p);p.setAlpha(255);}else{p.setColor(new int[]{0xFFFFC857,0xFF7CC8F5,0xFFF18AB4,0xFF9BE3B2}[i%4]);c.drawCircle(px,py,s,p);}}
            if(ChatDecorationConfig.snowEnabled()){p.setColor(0x99FFFFFF);for(int i=0;i<12;i++){float px=(i*73+t*9)%Math.max(1,w);float py=(i*41+t*17)%(h-AndroidUtilities.dp(48))+AndroidUtilities.dp(48);c.drawCircle(px,py,AndroidUtilities.dp(1.4f),p);}}
            if(isAttachedToWindow())postInvalidateDelayed(34);
        }
    }
}
