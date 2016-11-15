package me.develfox.anidub;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.View;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by max on 04.11.2014.
 */
public class MyService extends Service {
    private final int NOTIFICATION_ID = 102;
    private NotificationManager nm;
    private SharedPreferences sharedPreferences;
    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sharedPreferences = getSharedPreferences("cookies", Context.MODE_PRIVATE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            System.out.println("Проверяю на обнову подписки");
            CheckSubscribe checkSubscribe = new CheckSubscribe();
            checkSubscribe.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    public void showNotify(String nameFilm){
        Notification.Builder builder = new Notification.Builder(this);
        Intent intent = new Intent(this,SearchFilm.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        builder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.lcon)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.lcon))
                .setTicker("Обновление статьи")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(nameFilm)
                .setContentText("Нажмите, чтобы узнать подробнее..");
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_ALL;
        nm.notify(NOTIFICATION_ID,notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    class CheckSubscribe extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            while (sharedPreferences.getBoolean("updateSub",false)) {
               try {
                   System.out.println("Зашли в асинк");
                   String PHPSESSID = sharedPreferences.getString("PHPSESSID", "");
                   String dle_newpm = sharedPreferences.getString("dle_newpm", "");
                   String dle_password = sharedPreferences.getString("dle_password", "");
                   String dle_user_id = sharedPreferences.getString("dle_user_id", "");

                   Map<String, String> sessionId = new HashMap<String, String>();
                   sessionId.put("PHPSESSID", PHPSESSID);
                   sessionId.put("dle_newpm", dle_newpm);
                   sessionId.put("dle_password", dle_password);
                   sessionId.put("dle_user_id", dle_user_id);

                   Document document = null;
                   try {
                       document = Jsoup.connect("http://online.anidub.com/post_subscribe/").cookies(sessionId).get();
//                document = Jsoup.connect("http://online.anidub.com").cookies(sessionId).get();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   Elements element = document.select(".newsinfo");
                   Elements elements = element.get(0).select(".str");
                   String data = elements.get(0).text();
                   if (sharedPreferences.contains("dataSub") && !data.equals(sharedPreferences.getString("dataSub", ""))) {
                       Elements elementsNameTitle = document.select(".title");
                       String nameFilm = elementsNameTitle.get(0).select("a").text();
                       showNotify(nameFilm);

                       SharedPreferences.Editor editor = sharedPreferences.edit();
                       editor.putString("dataSub", data);
                       editor.commit();
                   } else if (!sharedPreferences.contains("dataSub")) {
                       SharedPreferences.Editor editor = sharedPreferences.edit();
                       editor.putString("dataSub", data);
                       editor.commit();
                   }
                   try {
                       Thread.sleep(10000*60);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }catch (Exception e){
                   e.printStackTrace();
               }//main try

            }//while
            return null;
        }


    }
}
