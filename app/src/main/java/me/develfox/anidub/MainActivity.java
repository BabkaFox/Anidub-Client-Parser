package me.develfox.anidub;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.List;


public class MainActivity extends Activity {

    protected AbsListView listViewNew,listViewTopTV,listViewTopOngoing,listViewTopFilms,listViewTopOva,listViewTopDorams;
    DisplayImageOptions options;

    SharedPreferences sharedPreferences,spLogin;
    ArrayList<Items> saveItemsTopOva = new ArrayList<Items>();
    ArrayList<Items> saveItemsTopDorams = new ArrayList<Items>();
    //Позже переделать =(
    //Храним урл картинок
    ArrayList<String> itemsNewImg = new ArrayList<String>();
    ArrayList<String> itemsTop20TVImg = new ArrayList<String>();
    ArrayList<String> itemsTop20OngoingImg = new ArrayList<String>();
    ArrayList<String> itemsTop20FilmsImg = new ArrayList<String>();
    ArrayList<String> itemsTop20OvaImg = new ArrayList<String>();
    ArrayList<String> itemsTop20DoramsImg = new ArrayList<String>();

    //Храним урл страниц аниме
    ArrayList<String> itemsNew = new ArrayList<String>();
    ArrayList<String> itemsTop20TV = new ArrayList<String>();
    ArrayList<String> itemsTop20Ongoing = new ArrayList<String>();
    ArrayList<String> itemsTop20Films = new ArrayList<String>();
    ArrayList<String> itemsTop20Ova = new ArrayList<String>();
    ArrayList<String> itemsTop20Dorams = new ArrayList<String>();

    //NavigationDrawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    CustomDrawerAdapter adapter;

    List<DrawerItem> dataList;
    private int mLastPosition = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckUpdateOva checkUpdateOva = new CheckUpdateOva();
        checkUpdateOva.execute();

        sharedPreferences = getSharedPreferences("Loading9",MODE_PRIVATE);

        spLogin = getSharedPreferences("cookies",MODE_PRIVATE);
        if (!spLogin.contains("updateSub")) {
            SharedPreferences.Editor editor = spLogin.edit();
            editor.putBoolean("updateSub", true);
            editor.commit();
        }
        if (spLogin.contains("onLogin")&& spLogin.getBoolean("onLogin",false)){
            startService(new Intent(this, MyService.class));
        }

        String savedItems = sharedPreferences.getString("SavedDataNew",null);
        try {
            JSONObject jObj = new JSONObject(savedItems);
            JSONArray jArr = jObj.getJSONArray("New");
            for (int i = 0; i < jArr.length(); i++) {
                itemsNewImg.add(jArr.getJSONObject(i).getString("img"));
                itemsNew.add(jArr.getJSONObject(i).getString("url"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        savedItems = sharedPreferences.getString("SavedDataOngoing",null);
        try {
            JSONObject jObj = new JSONObject(savedItems);
            JSONArray jArr = jObj.getJSONArray("Ongoing");
            for (int i = 0; i < jArr.length(); i++) {
                itemsTop20OngoingImg.add(jArr.getJSONObject(i).getString("img"));
                itemsTop20Ongoing.add(jArr.getJSONObject(i).getString("url"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        savedItems = sharedPreferences.getString("SavedDataTV",null);

        try {
            JSONObject jObj = new JSONObject(savedItems);
            JSONArray jArr = jObj.getJSONArray("TV");
            for (int i = 0; i < jArr.length(); i++) {
                itemsTop20TVImg.add(jArr.getJSONObject(i).getString("img"));
                itemsTop20TV.add(jArr.getJSONObject(i).getString("url"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        savedItems = sharedPreferences.getString("SavedDataFilm",null);

        try {
            JSONObject jObj = new JSONObject(savedItems);
            JSONArray jArr = jObj.getJSONArray("Films");
            for (int i = 0; i < jArr.length(); i++) {
                itemsTop20FilmsImg.add(jArr.getJSONObject(i).getString("img"));
                itemsTop20Films.add(jArr.getJSONObject(i).getString("url"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }



        listViewNew = (GridView) findViewById(R.id.gridNew);
        listViewNew.setOnItemClickListener(gridviewListenerNewItems);

        listViewTopTV = (GridView) findViewById(R.id.gridTopTv);
        listViewTopTV.setOnItemClickListener(gridviewListenerTVItems);

        listViewTopOngoing = (GridView) findViewById(R.id.gridTopOngoing);
        listViewTopOngoing.setOnItemClickListener(gridviewListenerOngoingItems);

        listViewTopFilms = (GridView) findViewById(R.id.gridFilms);
        listViewTopFilms.setOnItemClickListener(gridviewListenerFilmsItems);

        listViewTopOva = (GridView) findViewById(R.id.gridOva);

        listViewTopDorams = (GridView) findViewById(R.id.gridDorams);

        listViewNew.setAdapter(new MyGridAdapter(this,itemsNewImg));
        listViewTopTV.setAdapter(new MyGridAdapter(this,itemsTop20TVImg));
        listViewTopOngoing.setAdapter(new MyGridAdapter(this,itemsTop20OngoingImg));
        listViewTopFilms.setAdapter(new MyGridAdapter(this,itemsTop20FilmsImg));

        listViewTopOva.setAdapter(new ImageAdapter(this));
        listViewTopDorams.setAdapter(new ImageAdapter(this));

        // Initializing
        dataList = new ArrayList<DrawerItem>();
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        // Add Drawer Item to dataList
        dataList.add(new DrawerItem(getString(R.string.title_section2), R.drawable.konata_main,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section1), R.drawable.konata_mypage,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section3), R.drawable.konata_favorit,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section4), R.drawable.konata_sub,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section5), R.drawable.konata_ongoing,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section6), R.drawable.konata_tv,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section7), R.drawable.konata_film,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section8), R.drawable.konata_ova,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section9), R.drawable.konata_dorams,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section10),R.drawable.konata_katalog,"0"));
        dataList.add(new DrawerItem(getString(R.string.title_section11),R.drawable.konata_search,"0"));


//        adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,dataList);
        adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer,dataList);

        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            SelectItem(0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void SelectItem(int position) {

        Bundle bundle;
        Fragment f;
        FragmentManager fragmentManager;
        FragmentTransaction ft = null;
        switch (position){
        /*    case 1:
                bundle = new Bundle();
                bundle.putString("url", "http://online.anidub.com");
                f = new FragmentNew();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
                ft.commit();
                break;*/
            case 0:
//                getFragmentManager().beginTransaction().remove().commit();
              getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
            case 1:
//                f = new MyPageLogin();
                f = new LoginPage();
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
//                ft.commit();
                break;
            case 2:
                bundle = new Bundle();
                bundle.putString("url", "http://online.anidub.com/favorites/");
                f = new MyFilmsFS();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
//                ft.commit();
                break;

            case 3:
                bundle = new Bundle();
                bundle.putString("url", "http://online.anidub.com/post_subscribe/");
                f = new MyFilmsFS();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
//                ft.commit();
                adapter.setCounter(position,"0");
                break;

            case 4:
                bundle = new Bundle();
                bundle.putString("url", "http://online.anidub.com/anime_tv/anime_ongoing/");
                f = new FragmentNew();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
//                ft.commit();
                break;

            case 5:
                bundle = new Bundle();
                bundle.putString("url", "http://online.anidub.com/anime_tv/");
                f = new FragmentNew();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
//                ft.commit();
                break;

            case 6:
                bundle = new Bundle();
                bundle.putString("url", "http://online.anidub.com/anime_movie/");
                f = new FragmentNew();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
//                ft.commit();
                break;

            case 7:
                bundle = new Bundle();
                bundle.putString("url", "http://online.anidub.com/anime_ova/");
                f = new FragmentNew();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
//                ft.commit();
                break;

            case 8:
                bundle = new Bundle();
                bundle.putString("url", "http://online.anidub.com/dorama/");
                f = new FragmentNew();
                f.setArguments(bundle);
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
                break;
            case 9:
                f = new CatalogAnime();
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
                break;
            case 10:
                f = new SearchFilm();
                fragmentManager = getFragmentManager();
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, f);
                ft.addToBackStack("bomzh");
                break;
        }

        adapter.resetarCheck();
        adapter.setChecked(position, true);

//        mDrawerList.setItemChecked(position, true);
        setTitle(dataList.get(position).getItemName());
        mDrawerLayout.closeDrawer(mDrawerList);
        if (ft != null)
            ft.commit();

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return false;
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            SelectItem(position);
//            adapter.resetarCheck();
//            adapter.setChecked(position, true);
//            mDrawerLayout.closeDrawer(mDrawerList);

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
    private GridView.OnItemClickListener gridviewListenerNewItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
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
    };


    private GridView.OnItemClickListener gridviewListenerTVItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
            Bundle bundle = new Bundle();
            bundle.putString("url", itemsTop20TV.get(position));
            bundle.putString("urlImg", itemsTop20TVImg.get(position));
            Fragment f = new AboutFilmFragment();
            f.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, f);
            ft.addToBackStack("bomzh");
            ft.commit();

        }
    };

    private GridView.OnItemClickListener gridviewListenerOngoingItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
            Bundle bundle = new Bundle();
            bundle.putString("url", itemsTop20Ongoing.get(position));
            bundle.putString("urlImg", itemsTop20OngoingImg.get(position));
            Fragment f = new AboutFilmFragment();
            f.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, f);
            ft.addToBackStack("bomzh");
            ft.commit();

        }
    };
    private GridView.OnItemClickListener gridviewListenerFilmsItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
            Bundle bundle = new Bundle();
            bundle.putString("url", itemsTop20Films.get(position));
            bundle.putString("urlImg", itemsTop20FilmsImg.get(position));
            Fragment f = new AboutFilmFragment();
            f.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, f);
            ft.addToBackStack("bomzh");
            ft.commit();

        }
    };
    private GridView.OnItemClickListener gridviewListenerOVAItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
            Bundle bundle = new Bundle();
            bundle.putString("url", itemsTop20Ova.get(position));
            bundle.putString("urlImg", itemsTop20OvaImg.get(position));
            Fragment f = new AboutFilmFragment();
            f.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, f);
            ft.addToBackStack("bomzh");
            ft.commit();

        }
    };
    private GridView.OnItemClickListener gridviewListenerDoramsItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
            Bundle bundle = new Bundle();
            bundle.putString("url", itemsTop20Dorams.get(position));
            bundle.putString("urlImg", itemsTop20DoramsImg.get(position));

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

class CheckUpdateOva extends AsyncTask<Void, Void,String>{
    @Override
    protected String doInBackground(Void... voids) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Чек апдэйт пошел");
                Connection.Response res = null;
                Document doc = null;
                try {
                    res = Jsoup.connect("http://online.anidub.com/anime_ova/").execute();
                    doc = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements elements = doc.select(".top20");
                Elements elementName = elements.select("script");

                for (int i = 0;i<4;i++){
                    for (DataNode node : elementName.get(i).dataNodes()) {
                        saveItemsTopOva.add(new Items(node.getWholeData().split("\"")[1].split("\"")[0]));
                    }
                }
            }
        }).start();
        Connection.Response res = null;
        Document doc = null;
        try {
            res = Jsoup.connect("http://online.anidub.com/dorama/").execute();
            doc = res.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements elements = doc.select(".top20");
        Elements elementName = elements.select("script");
        for (Element tag : elementName){
            for (DataNode node : tag.dataNodes()) {
                saveItemsTopDorams.add(new Items(node.getWholeData().split("\"")[1].split("\"")[0]));
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println("Чек закончен");                      //удалить
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String> list2 = new ArrayList<String>();
        for (Items items :saveItemsTopOva){
            list2.add(items.getName());
        }
        String savedItemsOvaJson = sharedPreferences.getString("SavedDataOva",null);
        if (savedItemsOvaJson != null){
            try {
                JSONObject jObj = new JSONObject(savedItemsOvaJson);
                JSONArray jArr = jObj.getJSONArray("Ova");
                for (int i = 0; i < jArr.length(); i++) {
                    list.add(jArr.getJSONObject(i).getString("name"));
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            System.out.println("Ща буду проверять");
            if (list.containsAll(list2)){
                String savedItems = sharedPreferences.getString("SavedDataOva",null);
                    try {
                        JSONObject jObj = new JSONObject(savedItems);
                        JSONArray jArr = jObj.getJSONArray("Ova");
                        for (int i = 0; i < jArr.length(); i++) {
                            itemsTop20OvaImg.add(jArr.getJSONObject(i).getString("img"));
                            itemsTop20Ova.add(jArr.getJSONObject(i).getString("url"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    savedItems = sharedPreferences.getString("SavedDataDorams",null);

                    try {
                        JSONObject jObj = new JSONObject(savedItems);
                        JSONArray jArr = jObj.getJSONArray("Dorams");
                        for (int i = 0; i < jArr.length(); i++) {
                            itemsTop20DoramsImg.add(jArr.getJSONObject(i).getString("img"));
                            itemsTop20Dorams.add(jArr.getJSONObject(i).getString("url"));
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                listViewTopOva.setAdapter(new MyGridAdapter(MainActivity.this, itemsTop20OvaImg));
                listViewTopDorams.setAdapter(new MyGridAdapter(MainActivity.this, itemsTop20DoramsImg));

                listViewTopDorams.setOnItemClickListener(gridviewListenerDoramsItems);
                listViewTopOva.setOnItemClickListener(gridviewListenerOVAItems);

            } else {
                System.out.println("Неа, иду парсить");
                saveItemsTopOva.clear();
                saveItemsTopDorams.clear();
                LoadPost loadPostersNew = new LoadPost();
                loadPostersNew.execute();
            }
        } else {
            System.out.println("Равно нулю было поэтому сюды");
            saveItemsTopOva.clear();
            saveItemsTopDorams.clear();
            LoadPost loadPostersNew = new LoadPost();
            loadPostersNew.execute();
        }
    }
}
    class LoadPost extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            Connection.Response res = null;
            Connection.Response resFilm = null;
            Document doc = null;
            Document docFilm = null;
            try {
                res = Jsoup.connect("http://online.anidub.com/anime_ova/").execute();
                resFilm = Jsoup.connect("http://online.anidub.com/dorama/").execute();
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

                saveItemsTopOva.add(items);
                saveItemsTopDorams.add(itemsFilm);

            }
            return "all right";

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("503")) {
                Toast toast = Toast.makeText(getApplicationContext(), "Связь потеряна :(", Toast.LENGTH_SHORT);
                toast.show();
            } else{

                //Сохраняем полученные данные
                JSONArray array = new JSONArray();
                JSONObject savedItemsNew = new JSONObject();
                JSONObject savedItemsOngoing = new JSONObject();

                String savedItemsOvaJson = null;
                String savedItemsDoramsJson = null;

                try {
                    for (Items items : saveItemsTopOva) {
                        JSONObject object = new JSONObject();
                        object.put("name", items.getName());
                        object.put("url", items.getUrlItem());
                        object.put("img", items.getUrlImgItem());
                        array.put(object);
                    }
                    savedItemsNew.put("Ova", array);
                    savedItemsOvaJson = savedItemsNew.toString();
                    array = new JSONArray();

                    for (Items items : saveItemsTopDorams) {
                        JSONObject object = new JSONObject();
                        object.put("name", items.getName());
                        object.put("url", items.getUrlItem());
                        object.put("img", items.getUrlImgItem());
                        array.put(object);
                    }
                    savedItemsOngoing.put("Dorams", array);
                    savedItemsDoramsJson = savedItemsOngoing.toString();

                }catch (JSONException e){
                    e.printStackTrace();
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("SavedDataOva",savedItemsOvaJson);
                editor.putString("SavedDataDorams",savedItemsDoramsJson);
                editor.commit();

                for (int i = 0; i < 4; i++) {
                    itemsTop20Ova.add(saveItemsTopOva.get(i).getUrlItem());
                    itemsTop20OvaImg.add(saveItemsTopOva.get(i).getUrlImgItem());

                    itemsTop20Dorams.add(saveItemsTopDorams.get(i).getUrlItem());
                    itemsTop20DoramsImg.add(saveItemsTopDorams.get(i).getUrlImgItem());
                }
                if (itemsTop20OvaImg.size() != 0 && itemsTop20DoramsImg.size() != 0) {
                    listViewTopOva.setAdapter(new MyGridAdapter(MainActivity.this, itemsTop20OvaImg));
                    listViewTopDorams.setAdapter(new MyGridAdapter(MainActivity.this, itemsTop20DoramsImg));

                    listViewTopDorams.setOnItemClickListener(gridviewListenerDoramsItems);
                    listViewTopOva.setOnItemClickListener(gridviewListenerOVAItems);
                }
            }
        }
    }
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return 4;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ProgressBar imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ProgressBar(mContext);
            } else {
                imageView = (ProgressBar) convertView;
            }

//            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }
    }

    public void openAll(View view){
        Bundle bundle = new Bundle();
        Fragment f = null;

        switch (view.getId()){
            case R.id.btnNew:
                bundle.putString("url", "http://online.anidub.com");
                bundle.putString("nameAction", "AnimeNew");
                f = new ArrayFilmsAndTop();
                f.setArguments(bundle);
                break;

            case R.id.btnTopTV:
                bundle.putString("url", "http://online.anidub.com/anime_tv/");
                bundle.putString("nameAction", "AnimeTV");
                f = new ArrayFilmsAndTop();
                f.setArguments(bundle);
                break;
            case R.id.btnTopOngoing:
                bundle.putString("url", "http://online.anidub.com/anime_tv/anime_ongoing/");
                bundle.putString("nameAction", "AnimeOngoing");
                f = new ArrayFilmsAndTop();
                f.setArguments(bundle);
                break;
            case R.id.btnTopFilm:
                bundle.putString("url", "http://online.anidub.com/anime_movie/");
                bundle.putString("nameAction", "AnimeFilm");
                f = new ArrayFilmsAndTop();
                f.setArguments(bundle);
                break;
            case R.id.btnTopOva:
                bundle.putString("url", "http://online.anidub.com/anime_ova/");
                bundle.putString("nameAction", "AnimeOva");
                f = new ArrayFilmsAndTop();
                f.setArguments(bundle);
                break;
            case R.id.btnTopDorams:
                bundle.putString("url", "http://online.anidub.com/dorama/");
                bundle.putString("nameAction", "AnimeDorams");
                f = new ArrayFilmsAndTop();
                f.setArguments(bundle);
                break;
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (f!=null) {
            ft.replace(R.id.container, f);
            ft.addToBackStack("bomzh");
            ft.commit();
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
