package me.develfox.anidub;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;



/**
 * Created by max on 08.10.2014.
 */
public class MySplashScreen extends Activity {

    ArrayList<Items> saveItemsNew = new ArrayList<Items>();
    ArrayList<Items> saveItemsTopTV = new ArrayList<Items>();
    ArrayList<Items> saveItemsTopOngoing = new ArrayList<Items>();
    ArrayList<Items> saveItemsTopFilms = new ArrayList<Items>();

    int countUpdate = 0;
    int errors = 0;
    SharedPreferences sharedPreferences;


    ImageView vLogo;
    ProgressBar progressBar;
    Button btnReconect;
    TextView txtError;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        vLogo = (ImageView) findViewById(R.id.vLogo);
        Animation anima = AnimationUtils.loadAnimation(this,R.anim.myalpha);
        vLogo.startAnimation(anima);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnReconect = (Button) findViewById(R.id.btmReconect);
        btnReconect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnReconect.setVisibility(View.GONE);
                txtError.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                CheckUpdate update = new CheckUpdate();
                update.execute();
                errors =0;
            }
        });
        txtError = (TextView) findViewById(R.id.txtConectError);

        sharedPreferences = getSharedPreferences("Loading9",MODE_PRIVATE);

        CheckUpdate update = new CheckUpdate();
        update.execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    class CheckUpdate extends AsyncTask<Void, Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection.Response res = null;
                    Document doc = null;
                    try {
                        res = Jsoup.connect("http://online.anidub.com/anime_tv/anime_ongoing/").execute();
                        doc = res.parse();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ++errors;
                    }
                    if (doc != null) {
                        Elements elements = doc.select(".top20");
                        Elements elementName = elements.select("script");
                        for (int i = 0; i < 4; i++) {
                            for (DataNode node : elementName.get(i).dataNodes()) {
                                saveItemsTopOngoing.add(new Items(node.getWholeData().split("\"")[1].split("\"")[0]));
                            }
                        }
                    }else {
                        ++errors;
                    }

                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection.Response res = null;
                    Document doc = null;
                    try {
                        res = Jsoup.connect("http://online.anidub.com/anime_tv/").execute();
                        doc = res.parse();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ++errors;

                    }
                    if (doc != null) {

                        Elements elements = doc.select(".top20");
                        Elements elementName = elements.select("script");
                        for (int i = 0; i < 4; i++) {
                            for (DataNode node : elementName.get(i).dataNodes()) {
                                saveItemsTopTV.add(new Items(node.getWholeData().split("\"")[1].split("\"")[0]));
                            }
                        }
                    }else {
                        ++errors;
                    }
                }
            }).start();

            Connection.Response res = null;
            Document doc = null;
            try {
                res = Jsoup.connect("http://online.anidub.com/anime_movie/").execute();
                doc = res.parse();
            } catch (IOException e) {
                e.printStackTrace();
                ++errors;

            }
            if (doc != null) {

                Elements elements = doc.select(".top20");
                Elements elementName = elements.select("script");
                for (int i = 0; i < 4; i++) {
                    for (DataNode node : elementName.get(i).dataNodes()) {
                        saveItemsTopFilms.add(new Items(node.getWholeData().split("\"")[1].split("\"")[0]));
                    }
                }
                res = null;
                doc = null;
                try {
                    res = Jsoup.connect("http://online.anidub.com").execute();
                    doc = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                    ++errors;

                }
                if (doc != null) {
                    Elements elementsNameTitle = doc.select(".title");
                    for (int i = 0; i < 8; i++) {
                        saveItemsNew.add(new Items(elementsNameTitle.get(i).select("a").text()));
                    }
                }else {
                    ++errors;
                }
            }else {
                ++errors;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (errors >0){
                vLogo.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                txtError.setVisibility(View.VISIBLE);
                btnReconect.setVisibility(View.VISIBLE);
            } else {
                if (sharedPreferences.getString("SavedDataNew", null) != null ||
                        sharedPreferences.getString("SavedDataTV", null) != null ||
                        sharedPreferences.getString("SavedDataOngoing", null) != null ||
                        sharedPreferences.getString("SavedDataFilm", null) != null) {

                    if (!checkEquals(saveItemsNew, sharedPreferences.getString("SavedDataNew", null), "New")) {
                        ++countUpdate;
                        saveItemsNew.clear();
                        LoadDataIndex loadDataIndex = new LoadDataIndex();
                        loadDataIndex.execute();
                    }

                    if (!checkEquals(saveItemsTopTV, sharedPreferences.getString("SavedDataTV", null), "TV")) {
                        ++countUpdate;
                        saveItemsTopTV.clear();
                        LoadDataTopTV loadDataTopTV = new LoadDataTopTV();
                        loadDataTopTV.execute();
                    }

                    if (!checkEquals(saveItemsTopOngoing, sharedPreferences.getString("SavedDataOngoing", null), "Ongoing")) {
                        ++countUpdate;
                        saveItemsTopOngoing.clear();
                        LoadDataTopOngoing loadDataTopOngoing = new LoadDataTopOngoing();
                        loadDataTopOngoing.execute();
                    }

                    if (!checkEquals(saveItemsTopFilms, sharedPreferences.getString("SavedDataFilm", null), "Films")) {
                        ++countUpdate;
                        saveItemsTopFilms.clear();
                        LoadDataTopFilm loadDataTopFilm = new LoadDataTopFilm();
                        loadDataTopFilm.execute();

                    } else {
                        Intent intent = new Intent(MySplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    saveItemsNew.clear();
                    saveItemsTopTV.clear();
                    saveItemsTopOngoing.clear();
                    saveItemsTopFilms.clear();
                    LoadPostersNew loadPostersNew = new LoadPostersNew();
                    loadPostersNew.execute();
                }
            }

        }
    }

    public boolean checkEquals(ArrayList<Items> listItem, String savedData,String content){

        boolean isEqual = false;
        ArrayList<String> list2 = new ArrayList<String>();  //Сюда закидываем имена которые получили при запуске
        ArrayList<String> list3 = new ArrayList<String>();  //Сюда закидываем имена которые хранятся в Preferences

        for (Items items :listItem){
            list2.add(items.getName());
        }
        if (savedData != null){
            try {
                JSONObject jObj = new JSONObject(savedData);
                JSONArray jArr = jObj.getJSONArray(content);
                for (int i = 0; i < jArr.length(); i++) {
                    list3.add(jArr.getJSONObject(i).getString("name"));
                }
                if (list3.containsAll(list2))
                    isEqual = true;

            }catch (JSONException e){
                e.printStackTrace();
                return isEqual;
            }
        } else {
            return isEqual;
        }
        return isEqual;
    }

    class LoadDataIndex extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {

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
                throw new NullPointerException("503");
            }
            Elements elements = docNew.select(".poster_img");
            Elements elements1 = elements.select(".posters");
            Elements elements2 = elements.select("a[href]");
            Elements elementsNameTitle = docNew.select(".title");

            for (int i = 0; i < 8; i++) {
                Items items = new Items(null);
                items.setName(elementsNameTitle.get(i).select("a").text());
                items.setUrlItem(elements2.get(i).attr("href"));
                items.setUrlImgItem(elements1.get(i).attr("data-original"));
                saveItemsNew.add(items);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            --countUpdate;
            JSONArray array = new JSONArray();
            JSONObject savedItemsNew = new JSONObject();
            String savedItemsNewJson = null;
            try {
                for (Items items : saveItemsNew) {
                    JSONObject object = new JSONObject();
                    object.put("name", items.getName());
                    object.put("url", items.getUrlItem());
                    object.put("img", items.getUrlImgItem());
                    array.put(object);
                }
                savedItemsNew.put("New", array);
                savedItemsNewJson = savedItemsNew.toString();
            }catch (JSONException e){
                e.printStackTrace();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("SavedDataNew",savedItemsNewJson);  //имя
            editor.commit();

            if (countUpdate == 0){
                Intent intent = new Intent(MySplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }  //++
    class LoadDataTopTV extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {

            Connection.Response res = null;
            Document doc = null;
            try {
                res = Jsoup.connect("http://online.anidub.com").execute();
                doc = res.parse();
            } catch (IOException e) {
                e.printStackTrace();
                throw new NullPointerException("503");
            }
            Elements elements = doc.select(".top20");
            Elements elements1 = elements.select("a");
            Elements elementName = elements.select("script");

            for (int i = 0; i < 4; ++i) {

                Items items = new Items(null);
                try {
                    res = Jsoup.connect(elements1.get(i).attr("href")).execute();
                    doc = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements elementsImg = doc.getElementsByClass("maincont");                      //урл Постера
                Elements elementsImg1 = elementsImg.select("img[itemprop]");
                Element tag = elementName.get(i);
                for (DataNode node : tag.dataNodes()) {
                    items.setName(node.getWholeData().split("\"")[1].split("\"")[0]);
                }
                items.setUrlItem(elements1.get(i).attr("href"));
                items.setUrlImgItem(elementsImg1.attr("abs:src"));
                saveItemsTopTV.add(items);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            --countUpdate;
            //Сохраняем полученные данные
            JSONArray array = new JSONArray();
            JSONObject savedItemsTV = new JSONObject();
            String savedItemsTVJson = null;

            try {
                for (Items items : saveItemsTopTV) {
                    JSONObject object = new JSONObject();
                    object.put("name", items.getName());
                    object.put("url", items.getUrlItem());
                    object.put("img", items.getUrlImgItem());
                    array.put(object);
                }
                savedItemsTV.put("TV", array);
                savedItemsTVJson = savedItemsTV.toString();
            } catch (JSONException e){
                e.printStackTrace();
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("SavedDataTV",savedItemsTVJson);  //имя
            editor.commit();

            if (countUpdate == 0){
                Intent intent = new Intent(MySplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    } //++
    class LoadDataTopOngoing extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {

            Connection.Response res = null;
            Document doc = null;
            try {
                res = Jsoup.connect("http://online.anidub.com/anime_tv/anime_ongoing/").execute();
                doc = res.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elements = null;
            Elements elementName= null;
            try {
                elements = doc.select(".top20");
                elementName = elements.select("script");
            } catch (NullPointerException e) {
                return "503";
            }
            Elements elements1 = elements.select("a");
            for (int i = 0; i < 4; ++i) {
                Items items = new Items(null);
                try {
                    res = Jsoup.connect(elements1.get(i).attr("href")).execute();
                    doc = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements elementsImg = doc.getElementsByClass("maincont");                      //урл Постера
                Elements elementsImg1 = elementsImg.select("img[itemprop]");
                Element tag = elementName.get(i);
                for (DataNode node : tag.dataNodes()) {
                    items.setName(node.getWholeData().split("\"")[1].split("\"")[0]);
                }
                items.setUrlItem(elements1.get(i).attr("href"));
                items.setUrlImgItem(elementsImg1.attr("abs:src"));
                saveItemsTopOngoing.add(items);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            --countUpdate;
            //Сохраняем полученные данные
            JSONArray array = new JSONArray();
            JSONObject savedItemsOngoing = new JSONObject();
            String savedItemsOngoingJson = null;

            try {
                for (Items items : saveItemsTopOngoing) {
                    JSONObject object = new JSONObject();
                    object.put("name", items.getName());
                    object.put("url", items.getUrlItem());
                    object.put("img", items.getUrlImgItem());
                    array.put(object);
                }
                savedItemsOngoing.put("Ongoing", array);
                savedItemsOngoingJson = savedItemsOngoing.toString();

            }catch (JSONException e){
                e.printStackTrace();
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("SavedDataOngoing",savedItemsOngoingJson);  //имя
            editor.commit();

            if (countUpdate == 0){
                Intent intent = new Intent(MySplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    } //++
    class LoadDataTopFilm extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {

            Connection.Response resFilm = null;
            Document docFilm = null;
            try {
                resFilm = Jsoup.connect("http://online.anidub.com/anime_movie/").execute();
                docFilm = resFilm.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elementsFilm = null;
            Elements elementNameFilm = null;
            try {
                elementsFilm = docFilm.select(".top20");
                elementNameFilm = elementsFilm.select("script");
            } catch (NullPointerException e) {
                return "503";
            }
            Elements elements1Film = elementsFilm.select("a");
            for (int i = 0; i < 4; ++i) {
                Items itemsFilm = new Items(null);
                try {
                    resFilm = Jsoup.connect(elements1Film.get(i).attr("href")).execute();
                    docFilm = resFilm.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements elementsFilmImg = docFilm.getElementsByClass("maincont");
                Elements elements1FilmImg = elementsFilmImg.select("img[itemprop]");
                Element tagFilm = elementNameFilm.get(i);
                for (DataNode node : tagFilm.dataNodes()) {
                    itemsFilm.setName(node.getWholeData().split("\"")[1].split("\"")[0]);
                }
                itemsFilm.setUrlItem(elements1Film.get(i).attr("href"));
                itemsFilm.setUrlImgItem(elements1FilmImg.attr("abs:src"));
                saveItemsTopFilms.add(itemsFilm);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            --countUpdate;
            //Сохраняем полученные данные
            JSONArray array = new JSONArray();
            JSONObject savedItemsFilm = new JSONObject();
            String savedItemsFilmJson = null;
            try {
                for (Items items : saveItemsTopFilms) {
                    JSONObject object = new JSONObject();
                    object.put("name", items.getName());
                    object.put("url", items.getUrlItem());
                    object.put("img", items.getUrlImgItem());
                    array.put(object);
                }
                savedItemsFilm.put("Films", array);
                savedItemsFilmJson = savedItemsFilm.toString();

            }catch (JSONException e){
                e.printStackTrace();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("SavedDataFilm",savedItemsFilmJson);  //имя
            editor.commit();

            if (countUpdate == 0){
                Intent intent = new Intent(MySplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    } //++

    class LoadPostersNew extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {

            try {
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
                            throw new NullPointerException("503");

                        }
                        Elements elements = doc.select(".top20");
                        Elements elements1 = elements.select("a");
                        Elements elementName = elements.select("script");


                        for (int i = 0; i < 4; ++i) {

                            Items items = new Items(null);
                            try {
                                res = Jsoup.connect(elements1.get(i).attr("href")).execute();
                                doc = res.parse();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Elements elementsImg = doc.getElementsByClass("maincont");                      //урл Постера
                            Elements elementsImg1 = elementsImg.select("img[itemprop]");
                            Element tag = elementName.get(i);
                            for (DataNode node : tag.dataNodes()) {
                                items.setName(node.getWholeData().split("\"")[1].split("\"")[0]);
                            }
                            items.setUrlItem(elements1.get(i).attr("href"));
                            items.setUrlImgItem(elementsImg1.attr("abs:src"));
                            saveItemsTopTV.add(items);
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
                            throw new NullPointerException("503");
                        }
                        Elements elements = docNew.select(".poster_img");
                        Elements elements1 = elements.select(".posters");
                        Elements elements2 = elements.select("a[href]");
                        Elements elementsNameTitle = docNew.select(".title");

                        for (int i = 0; i < 8; i++) {
                            Items items = new Items(null);
                            items.setName(elementsNameTitle.get(i).select("a").text());
                            items.setUrlItem(elements2.get(i).attr("href"));
                            items.setUrlImgItem(elements1.get(i).attr("data-original"));
                            saveItemsNew.add(items);
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
                Elements elements = null;
                Elements elementsFilm = null;
                Elements elementName= null;
                Elements elementNameFilm = null;
                try {
                    elements = doc.select(".top20");
                    elementsFilm = docFilm.select(".top20");

                    elementName = elements.select("script");
                    elementNameFilm = elementsFilm.select("script");
                } catch (NullPointerException e) {
                    return "503";
                }

                Elements elements1 = elements.select("a");
                Elements elements1Film = elementsFilm.select("a");

                for (int i = 0; i < 4; ++i) {
                    Items items = new Items(null);
                    Items itemsFilm = new Items(null);
                    try {
                        res = Jsoup.connect(elements1.get(i).attr("href")).execute();
                        resFilm = Jsoup.connect(elements1Film.get(i).attr("href")).execute();
                        doc = res.parse();
                        docFilm = resFilm.parse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Elements elementsImg = doc.getElementsByClass("maincont");                      //урл Постера
                    Elements elementsFilmImg = docFilm.getElementsByClass("maincont");

                    Elements elementsImg1 = elementsImg.select("img[itemprop]");
                    Elements elements1FilmImg = elementsFilmImg.select("img[itemprop]");

                    Element tag = elementName.get(i);
                    for (DataNode node : tag.dataNodes()) {
                        items.setName(node.getWholeData().split("\"")[1].split("\"")[0]);
                    }
                    Element tagFilm = elementNameFilm.get(i);
                    for (DataNode node : tagFilm.dataNodes()) {
                        itemsFilm.setName(node.getWholeData().split("\"")[1].split("\"")[0]);
                    }
                    items.setUrlItem(elements1.get(i).attr("href"));
                    items.setUrlImgItem(elementsImg1.attr("abs:src"));

                    itemsFilm.setUrlItem(elements1Film.get(i).attr("href"));
                    itemsFilm.setUrlImgItem(elements1FilmImg.attr("abs:src"));

                    saveItemsTopOngoing.add(items);
                    saveItemsTopFilms.add(itemsFilm);

                }
            }catch (NullPointerException e){
                return "503";
            }catch (Exception e){
                return "503";
            }
            return "all right";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("503")){
                Toast toast = Toast.makeText(getApplicationContext(), "Связь потеряна :(", Toast.LENGTH_SHORT);
                toast.show();
            }else {
                //Сохраняем полученные данные
                JSONArray array = new JSONArray();
                JSONObject savedItemsNew = new JSONObject();
                JSONObject savedItemsOngoing = new JSONObject();
                JSONObject savedItemsTV = new JSONObject();
                JSONObject savedItemsFilm = new JSONObject();

                String savedItemsNewJson = null;
                String savedItemsOngoingJson = null;
                String savedItemsTVJson = null;
                String savedItemsFilmJson = null;

                try {
                    for (Items items : saveItemsNew) {
                        JSONObject object = new JSONObject();
                        object.put("name", items.getName());
                        object.put("url", items.getUrlItem());
                        object.put("img", items.getUrlImgItem());
                        array.put(object);
                    }
                    savedItemsNew.put("New", array);
                    savedItemsNewJson = savedItemsNew.toString();
                    array = new JSONArray();

                    for (Items items : saveItemsTopOngoing) {
                        JSONObject object = new JSONObject();
                        object.put("name", items.getName());
                        object.put("url", items.getUrlItem());
                        object.put("img", items.getUrlImgItem());
                        array.put(object);
                    }
                    savedItemsOngoing.put("Ongoing", array);
                    savedItemsOngoingJson = savedItemsOngoing.toString();
                    array = new JSONArray();


                    for (Items items : saveItemsTopTV) {
                        JSONObject object = new JSONObject();
                        object.put("name", items.getName());
                        object.put("url", items.getUrlItem());
                        object.put("img", items.getUrlImgItem());
                        array.put(object);
                    }
                    savedItemsTV.put("TV", array);
                    savedItemsTVJson = savedItemsTV.toString();
                    array = new JSONArray();


                    for (Items items : saveItemsTopFilms) {
                        JSONObject object = new JSONObject();
                        object.put("name", items.getName());
                        object.put("url", items.getUrlItem());
                        object.put("img", items.getUrlImgItem());
                        array.put(object);
                    }
                    savedItemsFilm.put("Films", array);
                    savedItemsFilmJson = savedItemsFilm.toString();

                }catch (JSONException e){
                    e.printStackTrace();
                }


                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("SavedDataNew",savedItemsNewJson);  //имя
                editor.putString("SavedDataOngoing",savedItemsOngoingJson);  //имя
                editor.putString("SavedDataTV",savedItemsTVJson);  //имя
                editor.putString("SavedDataFilm",savedItemsFilmJson);  //имя
                editor.commit();

                Intent intent = new Intent(MySplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
    class Items{
        private String name;
        private String urlItem;
        private String urlImgItem;

        Items(String name) {
            this.name = name;
        }

        Items(String name, String urlItem, String urlImgItem) {
            this.name = name;
            this.urlItem = urlItem;
            this.urlImgItem = urlImgItem;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrlItem() {
            return urlItem;
        }

        public void setUrlItem(String urlItem) {
            this.urlItem = urlItem;
        }

        public String getUrlImgItem() {
            return urlImgItem;
        }

        public void setUrlImgItem(String urlImgItem) {
            this.urlImgItem = urlImgItem;
        }
    }

}

