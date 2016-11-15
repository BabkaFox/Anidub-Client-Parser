package me.develfox.anidub;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by max on 04.11.2014.
 */
public class SearchFilm extends Fragment implements AbsListView.OnScrollListener {
    AbsListView listViewNew;
    DisplayImageOptions options;
    //Храним урл картинок
    ArrayList<String> itemsNewImg = new ArrayList<String>();
    //Храним урл страниц аниме
    ArrayList<String> itemsNew = new ArrayList<String>();
    LoadImage load;
    LinearLayout linerNewAll,linerNewAllMain;
    MyGridAdapter adapter;
    EditText editSearch;
    Button btnSearch;

    int str = 2;
    String urlThisAction;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_layout, container, false);
        listViewNew = (GridView) rootView.findViewById(R.id.gridSearchResult);
        editSearch = (EditText) rootView.findViewById(R.id.editSearch);
        btnSearch = (Button) rootView.findViewById(R.id.btnSearch);

        adapter = new  MyGridAdapter(getActivity(), itemsNewImg);








        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load = new LoadImage();
                load.execute(new String[]{editSearch.getText().toString()});
            }
        });
        return rootView;

    }


    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {

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
            if (itemsNew.size()>0){
                itemsNew.clear();
                itemsNewImg.clear();
            }
                Connection.Response res = null;
                try {
                    res = Jsoup.connect("http://online.anidub.com/index.php")
                            .data("do", "search", "subaction", "search", "story", strings[0])
                            .method(Connection.Method.POST)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document docNew = null;
                try {
                    docNew = res.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                Elements elements = docNew.select(".poster_img");
                Elements elements1 = elements.select("img");
                Elements elements2 = docNew.select(".title");
                Elements elements3 = elements2.select("a[href]");

                for (int i = 0; i < elements1.size(); i++) {
                    itemsNewImg.add(elements1.get(i).attr("src"));
                    itemsNew.add(elements3.get(i).attr("href"));
                }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            adapter.notifyDataSetChanged();
            listViewNew.setAdapter(adapter);
            listViewNew.setOnItemClickListener(gridviewListenerNewItems);
        }
    }

}
