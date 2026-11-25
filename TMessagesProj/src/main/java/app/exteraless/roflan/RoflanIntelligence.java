package app.exteraless.roflan;

import android.content.Context;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Explicitly invoked OpenAI-compatible assistant. No chat history is uploaded automatically. */
public final class RoflanIntelligence {
 public interface Callback{void onResult(String answer,Throwable error);}
 private static final java.util.concurrent.ExecutorService IO=Executors.newSingleThreadExecutor();
 private RoflanIntelligence(){}
 private static android.content.SharedPreferences p(){return ApplicationLoader.applicationContext.getSharedPreferences("exteraroflan_ai",Context.MODE_PRIVATE);}
 public static String endpoint(){return p().getString("endpoint","https://api.openai.com/v1/chat/completions");}
 public static String model(){return p().getString("model","gpt-4o-mini");}
 public static void configure(String endpoint,String model,String key){p().edit().putString("endpoint",endpoint).putString("model",model).apply();if(key!=null&&!key.trim().isEmpty())saveKey(key.trim());}
 private static SecretKey secretKey()throws Exception{KeyStore s=KeyStore.getInstance("AndroidKeyStore");s.load(null);if(s.containsAlias("ExteraRoflanAIKey"))return((KeyStore.SecretKeyEntry)s.getEntry("ExteraRoflanAIKey",null)).getSecretKey();KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new android.security.keystore.KeyGenParameterSpec.Builder("ExteraRoflanAIKey",android.security.keystore.KeyProperties.PURPOSE_ENCRYPT|android.security.keystore.KeyProperties.PURPOSE_DECRYPT).setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE).build());return g.generateKey();}
 private static void saveKey(String value){try{Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,secretKey());p().edit().putString("secret",Base64.encodeToString(c.doFinal(value.getBytes(StandardCharsets.UTF_8)),Base64.NO_WRAP)).putString("iv",Base64.encodeToString(c.getIV(),Base64.NO_WRAP)).apply();}catch(Throwable ignored){}}
 private static String readKey(){try{String v=p().getString("secret",""),iv=p().getString("iv","");if(v.isEmpty()||iv.isEmpty())return "";Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,secretKey(),new GCMParameterSpec(128,Base64.decode(iv,Base64.NO_WRAP)));return new String(c.doFinal(Base64.decode(v,Base64.NO_WRAP)),StandardCharsets.UTF_8);}catch(Throwable e){return "";}}
 public static boolean hasKey(){return !readKey().isEmpty();}
 public static void ask(String prompt,Callback callback){IO.execute(()->{HttpURLConnection c=null;try{String key=readKey();if(key.isEmpty())throw new IllegalStateException("Сначала укажи API-ключ.");JSONObject body=new JSONObject().put("model",model());JSONArray messages=new JSONArray();messages.put(new JSONObject().put("role","system").put("content","You are ExteraRoflan assistant. Be helpful and concise. Only use context explicitly provided by the user."));messages.put(new JSONObject().put("role","user").put("content",prompt));body.put("messages",messages);c=(HttpURLConnection)new URL(endpoint()).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Authorization","Bearer "+key);try(OutputStream o=c.getOutputStream()){o.write(body.toString().getBytes(StandardCharsets.UTF_8));}int status=c.getResponseCode();InputStream in=status>=200&&status<300?c.getInputStream():c.getErrorStream();StringBuilder raw=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)raw.append(line);}if(status<200||status>=300)throw new IllegalStateException("API HTTP "+status+": "+raw);String answer=new JSONObject(raw.toString()).getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content","Пустой ответ");finish(callback,answer,null);}catch(Throwable e){finish(callback,null,e);}finally{if(c!=null)c.disconnect();}});}
 private static void finish(Callback cb,String answer,Throwable error){new android.os.Handler(android.os.Looper.getMainLooper()).post(()->{if(cb!=null)cb.onResult(answer,error);});}
}