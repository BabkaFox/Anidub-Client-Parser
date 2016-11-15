package me.develfox.anidub;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by max on 24.10.2014.
 */
public class AboutFilmFragment extends Fragment {

    DisplayImageOptions options;
    AbsListView listViewRecomend;
    ArrayList<String> itemsRecomend = new ArrayList<String>();
    ArrayList<String> itemsRecomendImg = new ArrayList<String>();
    ArrayList<String> series = new ArrayList<String>();
    ArrayList<String> urlSeries = new ArrayList<String>();
    CustomAdapter arrayAdapter;
    Spinner spinnerSeries;
    String urlThisFilm,urlThisImg,urlThisFavorits,urlThisSubscribe,isAddFavorits = "add",isAddSubscribe="add",idThisFilm;
    ImageView posterImage,icoSub;
    TextView txtFilmName, txtYear, txtCountSeries, txtGenre,txtDubbers,txtRating,txtFilmDescriptionsMini,txtBtnInFavor,txtBtnWatch;
    FilmObject film = new FilmObject();
    LoadRecomend loadRecomend;
    LoadContent load;
    LinearLayout about_film;
    ScrollView scrollView;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    ImageLoader imageLoader;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about_film_layout, container, false);

        sharedPreferences = getActivity().getSharedPreferences("cookies", Context.MODE_PRIVATE);

        urlThisFilm = this.getArguments().getString("url");
        urlThisImg = this.getArguments().getString("urlImg");

        about_film = (LinearLayout) rootView.findViewById(R.id.about_film);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
        scrollView.setVisibility(View.INVISIBLE);

//        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFA917, PorterDuff.Mode.SRC_IN);

        txtFilmName = (TextView) rootView.findViewById(R.id.txtNameFilm);
        txtYear = (TextView) rootView.findViewById(R.id.txtYear);
        txtCountSeries = (TextView) rootView.findViewById(R.id.txtCountSeries);
        txtGenre = (TextView) rootView.findViewById(R.id.txtGenre);
        txtDubbers = (TextView) rootView.findViewById(R.id.txtDubbers);
        txtRating = (TextView) rootView.findViewById(R.id.txtRating);
        txtFilmDescriptionsMini = (TextView) rootView.findViewById(R.id.txtFilmDescriptionsMini);
        spinnerSeries = (Spinner) rootView.findViewById(R.id.spinnerSeries);


        txtBtnInFavor = (TextView) rootView.findViewById(R.id.inFavor);
        txtBtnWatch = (TextView) rootView.findViewById(R.id.watchNow);
        icoSub = (ImageView) rootView.findViewById(R.id.icoSub);

        txtBtnInFavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sharedPreferences.getBoolean("onLogin",false)){
                    Toast toast = Toast.makeText(getActivity(), "Для добавления в закладки необходимо войти!", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    AddFavors addFavors = new AddFavors();
                    if (isAddFavorits.equals("add")) {
                        Toast toast = Toast.makeText(getActivity(), "Добавляется в закладки...", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(getActivity(), "Удаление из закладок...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    addFavors.execute();
                }
            }
        });
        icoSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sharedPreferences.getBoolean("onLogin", false)) {
                    Toast toast = Toast.makeText(getActivity(), "Для добавления в подписку необходимо войти!", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    AddSubscribe addSubscribe = new AddSubscribe();
                    if (isAddSubscribe.equals("add")) {
                        Toast toast = Toast.makeText(getActivity(), "Добавляется в подписку...", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(getActivity(), "Удаление из подписок...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    addSubscribe.execute();
                }
            }
        });

        txtBtnWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                Fragment f;
                FragmentManager fragmentManager;
                FragmentTransaction ft;
                bundle.putString("url", urlSeries.get(spinnerSeries.getSelectedItemPosition()));
                f = new WatchFilmNow();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
                ft.commit();


            }
        });
        listViewRecomend = (GridView) rootView.findViewById(R.id.gridRecomend);

        load = new LoadContent();
        loadRecomend = new LoadRecomend();
        load.execute();
        loadRecomend.execute();

        posterImage = (ImageView) rootView.findViewById(R.id.posterImg);
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
//        if (urlThisImg != null)
//        imageLoader.displayImage(urlThisImg,posterImage);



        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
            loadRecomend.cancel(true);
            load.cancel(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
            loadRecomend.cancel(true);
            load.cancel(true);
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
    class LoadContent extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {

            Connection.Response res = null;
            Document doc = null;
            Map<String,String> sessionId = new HashMap<String,String>();
            if (sharedPreferences.getBoolean("onLogin",false)) {
                String PHPSESSID = sharedPreferences.getString("PHPSESSID","");
                String dle_newpm = sharedPreferences.getString("dle_newpm","");
                String dle_password = sharedPreferences.getString("dle_password","");
                String dle_user_id = sharedPreferences.getString("dle_user_id","");

                sessionId.put("PHPSESSID",PHPSESSID);
                sessionId.put("dle_newpm",dle_newpm);
                sessionId.put("dle_password",dle_password);
                sessionId.put("dle_user_id",dle_user_id);
                try {
                    res = Jsoup.connect(urlThisFilm).cookies(sessionId).execute();
                    doc = res.parse();
                } catch (IOException e){
                    e.printStackTrace();
                }
                Elements elements = doc.select(".newsicon.reset");
                Elements elements1 = elements.select("a[href]");
                urlThisFavorits = elements1.attr("href");   //Ссылка на добавление в закладки. для удаления из закладок add изменить на del
                isAddFavorits = urlThisFavorits.split("doaction=")[1].split("&")[0];

                elements = doc.select(".player_buts");
                elements1 = elements.select("a[href]");
                urlThisSubscribe = elements1.attr("href");   //Ссылка на добавление в подписку. для удаления из подписки add изменить на del пока не удаляется XD
                isAddSubscribe = urlThisSubscribe.split("doaction=")[1].split("&")[0];
                idThisFilm = urlThisSubscribe.split("id=")[1];

            }

            try {
                res = Jsoup.connect(urlThisFilm).execute();
                doc = res.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elements = doc.getElementsByClass("titlfull");
            film.nameFilm = elements.text();
            if (urlThisImg.equals("")) {
                elements = doc.getElementsByClass("maincont");                      //урл Постера
                Elements elements1 = elements.select("img[itemprop]");
                urlThisImg = elements1.attr("abs:src");
            }
            elements = doc.getElementsByClass("news_full");
            Elements elements1 = elements.select("ul");
            Elements elements2 = elements1.select("span");
            film.year = elements2.get(0).text();
            film.genre = elements2.get(1).text();
            film.series = elements2.get(3).text();
            film.dubbers = elements2.get(7).text();

            elements = doc.getElementsByClass("news_full");
            elements1 = elements.select("b[itemprop]");
            film.rating = elements1.text();

            elements = doc.select(".news_full [itemprop*=description]");
            film.description = elements.text();

            elements = doc.select("div#vk1");
            elements1 = elements.select("option");

            series.clear();
            for (Element element: elements1) {
                series.add(element.text());
                urlSeries.add(element.attr("value").split("\\|")[0]);
            }
            System.out.println(urlSeries);
            arrayAdapter = new CustomAdapter(getActivity(),R.layout.spiner_row,series);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            txtFilmName.setText(film.nameFilm);
            txtYear.setText("Год: " + film.year);
            txtCountSeries.setText("Количество серий: " + film.series);
            txtGenre.setText("Жанр: " + film.genre);
            txtDubbers.setText("Озвучивание: " + film.dubbers);
            txtRating.setText("Рейтинг: " + film.rating);
            txtFilmDescriptionsMini.setText(film.description);
            spinnerSeries.setAdapter(arrayAdapter);
//            about_film.removeView(progressBar);
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            imageLoader.displayImage(urlThisImg,posterImage);

            if (isAddFavorits.equals("del")){
                txtBtnInFavor.setText("Из закладок");
            }
            if (isAddSubscribe.equals("del")){
                icoSub.setImageResource(R.drawable.minus_sub);
            }


        }
    }
    class LoadRecomend extends AsyncTask<String,Void,String>{
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (!isCancelled()) {
                Connection.Response res = null;
                Document doc = null;
                try {
                    res = Jsoup.connect(urlThisFilm).execute();
                    doc = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements elements = doc.select(".relink.reset");
                Elements elements1 = elements.select("a");
                for (int i = 0; i < 4; i++) {
                    try {
                        res = Jsoup.connect(elements1.get(i).attr("href")).execute();
                        doc = res.parse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Elements elementsImg = doc.getElementsByClass("maincont");                      //урл Постера
                    Elements elements1Img = elementsImg.select("img[itemprop]");
                    itemsRecomendImg.add(elements1Img.attr("abs:src"));
                    itemsRecomend.add(elements1.get(i).attr("href"));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            if (!isCancelled()) {  //На всякий случай =)
                try {
                    System.out.println(isCancelled());
                    listViewRecomend.setAdapter(new MyGridAdapter(getActivity(), itemsRecomendImg));
                    listViewRecomend.setOnItemClickListener(gridviewListenerRecomendItems);
                } catch (NullPointerException e) {
                    System.out.println("Случилась апож :(");
                    e.printStackTrace();
                }
            }
            }
    }
    class AddFavors extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            Map<String, String> sessionId = new HashMap<String, String>();
            if (sharedPreferences.getBoolean("onLogin", false)) {
                String PHPSESSID = sharedPreferences.getString("PHPSESSID", "");
                String dle_newpm = sharedPreferences.getString("dle_newpm", "");
                String dle_password = sharedPreferences.getString("dle_password", "");
                String dle_user_id = sharedPreferences.getString("dle_user_id", "");

                sessionId.put("PHPSESSID", PHPSESSID);
                sessionId.put("dle_newpm", dle_newpm);
                sessionId.put("dle_password", dle_password);
                sessionId.put("dle_user_id", dle_user_id);

                Connection.Response res = null;
                Document doc = null;
                try {
                    Jsoup.connect(urlThisFavorits).cookies(sessionId).method(Connection.Method.GET).get();
                    res = Jsoup.connect(urlThisFilm).cookies(sessionId).execute();
                    doc = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements elements = doc.select(".newsicon.reset");
                Elements elements1 = elements.select("a[href]");
                urlThisFavorits = elements1.attr("href");   //Ссылка на добавление в закладки. для удаления из закладок add изменить на del
                isAddFavorits = urlThisFavorits.split("doaction=")[1].split("&")[0];

            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isAddFavorits.equals("del")){
                txtBtnInFavor.setText("Из закладок");
                Toast toast = Toast.makeText(getActivity(), "Добавлено!", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                txtBtnInFavor.setText("В закладки");
                Toast toast = Toast.makeText(getActivity(), "Удалено!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    class AddSubscribe extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            Map<String, String> sessionId = new HashMap<String, String>();
            if (sharedPreferences.getBoolean("onLogin", false)) {
                String PHPSESSID = sharedPreferences.getString("PHPSESSID", "");
                String dle_newpm = sharedPreferences.getString("dle_newpm", "");
                String dle_password = sharedPreferences.getString("dle_password", "");
                String dle_user_id = sharedPreferences.getString("dle_user_id", "");

                sessionId.put("PHPSESSID", PHPSESSID);
                sessionId.put("dle_newpm", dle_newpm);
                sessionId.put("dle_password", dle_password);
                sessionId.put("dle_user_id", dle_user_id);
                String addOrMinus = "";
                if (isAddSubscribe.equals("add")){
                    addOrMinus = "plus";
                } else {
                    addOrMinus = "minus";
                }
                Connection.Response res = null;
                Document doc = null;
                try {
                    Jsoup.connect("http://online.anidub.com/engine/ajax/post_subscribe.php?sub_id="+idThisFilm+"&action="+addOrMinus+"&skin=Anidub_online").cookies(sessionId).method(Connection.Method.GET).get();
                    res = Jsoup.connect(urlThisFilm).cookies(sessionId).execute();
                    doc = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements elements = doc.select(".player_buts");
                Elements elements1 = elements.select("a[href]");
                urlThisSubscribe = elements1.attr("href");   //Ссылка на добавление в подписку. для удаления из закладок add изменить на del
                isAddSubscribe = urlThisSubscribe.split("doaction=")[1].split("&")[0];

            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isAddSubscribe.equals("del")){
                icoSub.setImageResource(R.drawable.minus_sub);
                Toast toast = Toast.makeText(getActivity(), "Добавлено!", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                icoSub.setImageResource(R.drawable.add_sub);
                Toast toast = Toast.makeText(getActivity(), "Удалено!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    private GridView.OnItemClickListener gridviewListenerRecomendItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
            Bundle bundle = new Bundle();
            bundle.putString("url", itemsRecomend.get(position));
            bundle.putString("urlImg", itemsRecomendImg.get(position));
            // Sending image id to FullScreenActivity
            Fragment f = new AboutFilmFragment();
            f.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, f);
            ft.addToBackStack("bomzh");
            ft.commit();

        }
    };
    class FilmObject{
        String urlFilm      = "";
        String nameFilm     = "";
        String year         = "";
        String genre        = "";
        String country      = "";
        String series       = "";
        String reliseDate   = "";
        String director     = "";
        String screenWriter = "";
        String dubbers      = "";
        String timing       = "";
        String translate    = "";
        String rating       = "";
        String urlPoster    = "";
        String description  = "";
        String addFavorits  = "";
        String addSubscribt = "";
        LinkedHashMap<String,String> urlAndNameSeries = new LinkedHashMap<String, String>();

    }
    class CustomAdapter extends ArrayAdapter {
        private Context context;
        private int textViewResourceId;
        private ArrayList<String> objects;
        public boolean flag = true;
        public CustomAdapter(Context context, int textViewResourceId,
                             ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            this.textViewResourceId = textViewResourceId;
            this.objects = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(context, textViewResourceId, null);
            if (flag != false) {
                TextView tv = (TextView) convertView;
                tv.setTextSize(20);
                tv.setText(objects.get(position));
            }
            return convertView;
        }
    }
}
