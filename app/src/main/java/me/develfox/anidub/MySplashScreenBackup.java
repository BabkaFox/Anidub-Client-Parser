package me.develfox.anidub;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Created by max on 08.10.2014.
 */
public class MySplashScreenBackup extends Activity {
    ArrayList<String> itemsNew = new ArrayList<String>();
    ArrayList<String> itemsTop20TV = new ArrayList<String>();
    ArrayList<String> itemsTop20Ongoing = new ArrayList<String>();
    ArrayList<String> itemsTop20Films = new ArrayList<String>();
//    ArrayList<String> itemsTop20Ova = new ArrayList<String>();



    ImageView vLogo;
    private static int SPLASH_SCREEN_TIMEOUT = 3000;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        vLogo = (ImageView) findViewById(R.id.vLogo);
        Animation anima = AnimationUtils.loadAnimation(this,R.anim.myalpha);
        vLogo.startAnimation(anima);

        LoadPostersNew load = new LoadPostersNew();
        load.execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    class LoadPostersNew extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            //Запускаем поток в потоке_______________________________________________________________________________________________________________
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection.Response res = null;
                    Document doc = null;
                    try {
                        res = Jsoup.connect("http://online.anidub.com").execute();
                        doc = res.parse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Elements elements = doc.select(".top20");
                    Elements elements1 = elements.select("a");
                    ArrayList<String> topAnime = new ArrayList<String>();

                    for (int i = 0; i < 4; i++) {
                        topAnime.add(elements1.get(i).attr("href"));
                    }
                    for (int i = 0; i < 4; ++i) {
                        try {
                            res = Jsoup.connect(topAnime.get(i)).execute();
                            doc = res.parse();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        elements = doc.getElementsByClass("maincont");                      //урл Постера
                        elements1 = elements.select("img[itemprop]");
                        String urlPoster = elements1.attr("abs:src");
                        itemsTop20TV.add(urlPoster);
                    }
                }
            }).start();
            //Запускаем поток в потоке_______________________________________________________________________________________________________________
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection.Response res = null;
                    try {
                        res = Jsoup.connect("http://online.anidub.com").execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Document docNew = null;
                    try {
                        docNew = res.parse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Elements elements = docNew.select(".poster_img");
                    Elements elements1 = elements.select(".posters");
                    Elements elements2 = elements.select("a[href]");

                    for (int i = 0; i < 8; i++) {
                        itemsNew.add(elements1.get(i).attr("data-original"));
                    }

                }
            }).start();
            //запкскаем другой поток_______________________________________________________________________________________________________________
            Connection.Response res = null;
            Connection.Response resFilm = null;
            Document doc = null;
            Document docFilm = null;
            try {
                res = Jsoup.connect("http://online.anidub.com/anime_tv/anime_ongoing/").execute();
                resFilm = Jsoup.connect("http://online.anidub.com/anime_movie/").execute();
                doc = res.parse();
                docFilm = resFilm.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elements = doc.select(".top20");
            Elements elementsFilm = docFilm.select(".top20");

            Elements elements1 = elements.select("a");
            Elements elements1Film = elementsFilm.select("a");
            ArrayList<String> topAnime = new ArrayList<String>();
            ArrayList<String> topAnimeFilm = new ArrayList<String>();

            for (int i = 0; i < 4; i++) {
                topAnime.add(elements1.get(i).attr("href"));
                topAnimeFilm.add(elements1Film.get(i).attr("href"));

            }
            for (int i = 0; i < 4; ++i) {
                try {
                    res = Jsoup.connect(topAnime.get(i)).execute();
                    resFilm = Jsoup.connect(topAnimeFilm.get(i)).execute();
                    doc = res.parse();
                    docFilm = resFilm.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                elements = doc.getElementsByClass("maincont");                      //урл Постера
                elementsFilm = docFilm.getElementsByClass("maincont");

                elements1 = elements.select("img[itemprop]");
                elements1Film = elementsFilm.select("img[itemprop]");
                String urlPoster = elements1.attr("abs:src");

                String urlPosterFilm = elements1Film.attr("abs:src");
                itemsTop20Ongoing.add(urlPoster);
                itemsTop20Films.add(urlPosterFilm);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Intent intent = new Intent(MySplashScreenBackup.this, MainActivity.class);
            intent.putExtra("ListNew" , itemsNew);
            intent.putExtra("ListOngoing" , itemsTop20Ongoing);
            intent.putExtra("ListTV" , itemsTop20TV);
            intent.putExtra("ListFilms" , itemsTop20Films);
            startActivity(intent);
            finish();
        }
    }
}

