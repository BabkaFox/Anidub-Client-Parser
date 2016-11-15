package me.develfox.anidub;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by max on 27.10.2014.
 */
public class FragmentNew extends Fragment implements AbsListView.OnScrollListener{
    protected AbsListView listViewNew;
    DisplayImageOptions options;
    //Храним урл картинок
    ArrayList<String> itemsNewImg = new ArrayList<String>();
    //Храним урл страниц аниме
    ArrayList<String> itemsNew = new ArrayList<String>();
    ProgressBar progressBar,progressFind;
    LoadImage load;
    LoadDataNext load2;
    LinearLayout linerNewAll,linerNewAllMain;
    MyGridAdapter adapter;
    int str = 2,maxStr = 0;
    String urlThisAction;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.array_films_layout, container, false);

        urlThisAction = this.getArguments().getString("url");

        listViewNew = (GridView) rootView.findViewById(R.id.gridViewAllNew);

        linerNewAll = (LinearLayout) rootView.findViewById(R.id.linerNewAll);
        linerNewAllMain = (LinearLayout) rootView.findViewById(R.id.linerNewAllMain);
        linerNewAll.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarNewView);
        progressFind = (ProgressBar) rootView.findViewById(R.id.progressBar3);

//        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFA917, PorterDuff.Mode.SRC_IN);

        adapter = new  MyGridAdapter(getActivity(), itemsNewImg);

        load = new LoadImage();
        load.execute();

        load2 = new LoadDataNext();

        listViewNew.setOnScrollListener(this);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        load.cancel(false);

    }

    private GridView.OnItemClickListener gridviewListenerNewItems = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
            Bundle bundle = new Bundle();
            bundle.putString("url", itemsNew.get(position));
            bundle.putString("urlImg", itemsNewImg.get(position));
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
            listViewNew.setAdapter(adapter);
            listViewNew.setOnItemClickListener(gridviewListenerNewItems);
            progressBar.setVisibility(View.GONE);
            linerNewAll.setVisibility(View.VISIBLE);
            load2.execute();
        }
    }

    class LoadDataNext extends AsyncTask<String,Void,ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressFind.setVisibility(View.VISIBLE);
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
            progressFind.setVisibility(View.GONE);
            adapter.add(list);
            adapter.notifyDataSetChanged();
        }
    }
}
