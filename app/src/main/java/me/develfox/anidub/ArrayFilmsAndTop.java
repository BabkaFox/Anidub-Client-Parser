package me.develfox.anidub;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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
 * Created by max on 11.11.2014.
 */
public class ArrayFilmsAndTop extends Fragment implements AbsListView.OnScrollListener{
    protected AbsListView gridTopAnime,gridAnime;
    DisplayImageOptions options;
    //Храним урл картинок
    ArrayList<String> itemsNewImg = new ArrayList<String>();
    ArrayList<String> itemsTopImg = new ArrayList<String>();
    //Храним урл страниц аниме
    ArrayList<String> itemsNew = new ArrayList<String>();
    ArrayList<String> itemsTop = new ArrayList<String>();

    ArrayList<Items> itemsTopName = new ArrayList<Items>();
    ArrayList<Items> saveItemsTop = new ArrayList<Items>();

    SharedPreferences sharedPreferences;
    LoadImage load;
    LoadDataNext load2;
    MyGridAdapter adapterAnime,adapterTopAmine;
    int str = 2,maxStr = 0;
    String urlThisAction,nameThisAction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.array_film_and_top_layout, container, false);

        sharedPreferences = getActivity().getSharedPreferences("SaveTop20",Context.MODE_PRIVATE);

        urlThisAction = this.getArguments().getString("url");
        nameThisAction = this.getArguments().getString("nameAction");

        gridTopAnime = (GridView) rootView.findViewById(R.id.gridTopAnime);
        gridAnime = (GridView) rootView.findViewById(R.id.gridAnime);

        adapterAnime = new MyGridAdapter(getActivity(),itemsNewImg);
        CheckTopAnime checkTopAnime = new CheckTopAnime();
        checkTopAnime.execute();
        load = new LoadImage();
        load.execute();

        load2 = new LoadDataNext();
        gridAnime.setOnScrollListener(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (load != null)
        load.cancel(false);

    }

    private GridView.OnItemClickListener gridviewListenerNewItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onClickList(position);
        }
    };

    private GridView.OnItemClickListener gridviewListenerTopItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onClickListTop(position);
        }
    };

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisible, int visibleCount, int totalCount) {
        boolean loadMore = firstVisible + visibleCount >= totalCount;
        if (str<maxStr){
            if (loadMore && (load.getStatus() == AsyncTask.Status.FINISHED && (load2.getStatus() == AsyncTask.Status.FINISHED))) {
                try {
                    load2 = new LoadDataNext();
                    load2.execute();
                } catch (Exception e) {
                    System.out.println("Вызвали меня уже!");
                }

            }
        }
    }


    class MyGridAdapter extends BaseAdapter {
        ImageLoader imageLoader = ImageLoader.getInstance();
        private LayoutInflater inflater;
        Context context;
        ArrayList<String> items;


        MyGridAdapter(Context context, ArrayList<String> items) {
            this.context = context;
            this.items = items;
            inflater = LayoutInflater.from(context);
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }
        public void add(ArrayList<String> data){
            this.items.addAll(data);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return items.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.item_grid_image, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }


            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            ImageLoader.getInstance()
                    .displayImage(items.get(position), holder.imageView, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.progressBar.setProgress(0);
                            holder.progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            holder.progressBar.setProgress(Math.round(100.0f * current / total));
                        }
                    });

            return view;
        }

    }
    static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }

    class LoadTopAnime extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            Connection.Response resFilm = null;
            Document docFilm = null;
            try {
                resFilm = Jsoup.connect(urlThisAction).execute();
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
            for (int i = 0; i < 20; ++i) {
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
                saveItemsTop.add(itemsFilm);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Сохраняем полученные данные
            JSONArray array = new JSONArray();
            JSONObject savedItemsFilm = new JSONObject();
            String savedItemsFilmJson = null;
            try {
                for (Items items : saveItemsTop) {
                    JSONObject object = new JSONObject();
                    object.put("name", items.getName());
                    object.put("url", items.getUrlItem());
                    object.put("img", items.getUrlImgItem());
                    array.put(object);
                }
                savedItemsFilm.put(nameThisAction, array);
                savedItemsFilmJson = savedItemsFilm.toString();

            }catch (JSONException e){
                e.printStackTrace();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(nameThisAction,savedItemsFilmJson);  //имя
            editor.commit();
            //Загружаем их в массив и подключаем адаптер
            for (Items items : saveItemsTop){
                itemsTopImg.add(items.getUrlImgItem());
            }
            gridTopAnime.setAdapter(new MyGridAdapter(getActivity(), itemsTopImg));
            gridTopAnime.setOnItemClickListener(gridviewListenerTopItems);

        }
    }


    class CheckTopAnime extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            Connection.Response res = null;
            Document doc = null;
            try {
                res = Jsoup.connect(urlThisAction).execute();
                doc = res.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elements = doc.select(".top20");
            Elements elementName = elements.select("script");
            for (int i = 0; i < 20; i++) {
                for (DataNode node : elementName.get(i).dataNodes()) {
                    itemsTopName.add(new Items(node.getWholeData().split("\"")[1].split("\"")[0]));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (sharedPreferences.getString(nameThisAction, null) != null ) {

                if (!checkEquals(itemsTopName, sharedPreferences.getString(nameThisAction, null), nameThisAction)) {
                    itemsTopName.clear();
                    LoadTopAnime loadDataIndex = new LoadTopAnime();
                    loadDataIndex.execute();
                }else {
                    String savedItems = sharedPreferences.getString(nameThisAction,null);
                    try {
                        JSONObject jObj = new JSONObject(savedItems);
                        JSONArray jArr = jObj.getJSONArray(nameThisAction);
                        for (int i = 0; i < jArr.length(); i++) {
                            itemsTopImg.add(jArr.getJSONObject(i).getString("img"));
                            itemsTop.add(jArr.getJSONObject(i).getString("url"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    gridTopAnime.setAdapter(new MyGridAdapter(getActivity(), itemsTopImg));
                    gridTopAnime.setOnItemClickListener(gridviewListenerTopItems);
                }

            } else {
                itemsTopName.clear();
                LoadTopAnime loadDataIndex = new LoadTopAnime();
                loadDataIndex.execute();
            }
        }
    }


    class LoadImage extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            if (itemsNew.size() == 0) {
                System.out.println("Захожу парсить");
                Connection.Response res = null;
                try {
                    res = Jsoup.connect(urlThisAction).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document docNew = null;
                try {
                    docNew = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Получаем максимальное число страниц
                Elements elements = docNew.select(".navigation");
                if (elements.isEmpty()){
                    maxStr = 0;
                }else {
                    Element element1 = elements.get(0);
                    Elements elements2 = element1.select("a");
                    int siz = elements2.size();
                    maxStr = Integer.parseInt(elements2.get(siz-1).text());
                }

                elements = docNew.select(".poster_img");
                Elements elements1 = elements.select(".posters");
                Elements elements2 = elements.select("a[href]");
                for (int i = 0; i < elements1.size(); i++) {
                    itemsNewImg.add(elements1.get(i).attr("data-original"));
                    itemsNew.add(elements2.get(i).attr("href"));
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            gridAnime.setAdapter(adapterAnime);
            gridAnime.setOnItemClickListener(gridviewListenerNewItems);
            load2.execute();
        }
    }

    class LoadDataNext extends AsyncTask<String,Void,ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            Connection.Response res = null;
            try {
                res = Jsoup.connect(urlThisAction + "/page/" + str + "/").execute();
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
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < elements1.size(); i++) {
                list.add(elements1.get(i).attr("data-original"));
//                    itemsNewImg.add(elements1.get(i).attr("data-original")); //а вот хз как они туда добавляются сами
                itemsNew.add(elements2.get(i).attr("href"));
            }
            ++str;

            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            super.onPostExecute(list);
            adapterAnime.add(list);
            adapterAnime.notifyDataSetChanged();
        }
    }
    public void onClickList(int position){
        Bundle bundle = new Bundle();
        bundle.putString("url", itemsNew.get(position));
        bundle.putString("urlImg", itemsNewImg.get(position));
        Fragment f = new AboutFilmFragment();
        f.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, f);
        ft.addToBackStack("bomzh");
        ft.commit();
    }

    public void onClickListTop(int position){
        Bundle bundle = new Bundle();
        bundle.putString("url", itemsTop.get(position));
        bundle.putString("urlImg", itemsTopImg.get(position));
        Fragment f = new AboutFilmFragment();
        f.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, f);
        ft.addToBackStack("bomzh");
        ft.commit();
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
