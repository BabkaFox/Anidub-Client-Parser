package me.develfox.anidub;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by max on 08.11.2014.
 */
public class LoginPage extends Fragment {
    LinearLayout formLogin, profileView;
    EditText editLogin,editPassword;
    Button btnEnter;
    TextView remindPass,txtNick,txtMessages,txtFavorit,txtSubscribe,txtLaterView1,txtLaterView2,txtLaterView3,txtLaterView4,txtLaterView5,txtBtnExit;
    ImageView imgAvatar;
    ProgressBar progressConnect;
    ImageLoader imageLoader;
    SharedPreferences sharedPreferences;
    ArrayList<LaterWatch> listWatchLater = new ArrayList<LaterWatch>();

    ListView listViewLater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_layout_test, container, false);
        formLogin = (LinearLayout) rootView.findViewById(R.id.formLogin);
        profileView = (LinearLayout) rootView.findViewById(R.id.profileView);

        editLogin = (EditText) rootView.findViewById(R.id.Login);
        editPassword = (EditText) rootView.findViewById(R.id.Password);
        btnEnter = (Button) rootView.findViewById(R.id.btnEnter);
        remindPass = (TextView) rootView.findViewById(R.id.remindMePass);
        txtNick = (TextView) rootView.findViewById(R.id.txtNick);
        txtMessages = (TextView) rootView.findViewById(R.id.txtMessages);
        txtFavorit = (TextView) rootView.findViewById(R.id.txtFavorit);
        txtSubscribe = (TextView) rootView.findViewById(R.id.txtSubscribe);
        txtLaterView1 = (TextView) rootView.findViewById(R.id.laterView1);
        txtLaterView2 = (TextView) rootView.findViewById(R.id.laterView2);
        txtLaterView3 = (TextView) rootView.findViewById(R.id.laterView3);
        txtLaterView4 = (TextView) rootView.findViewById(R.id.laterView4);
        txtLaterView5 = (TextView) rootView.findViewById(R.id.laterView5);
        txtBtnExit = (TextView) rootView.findViewById(R.id.txtBtnExit);
        imgAvatar = (ImageView) rootView.findViewById(R.id.imgAvatar);
        progressConnect = (ProgressBar) rootView.findViewById(R.id.progressConnect);

        listViewLater = (ListView) rootView.findViewById(R.id.listViewLater);

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIN();
            }
        });
        txtBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOUT();
            }
        });
        sharedPreferences = getActivity().getSharedPreferences("cookies", Context.MODE_PRIVATE);

        if (sharedPreferences.contains("onLogin")&& sharedPreferences.getBoolean("onLogin",false)){
            formLogin.setVisibility(View.GONE);
            progressConnect.setVisibility(View.VISIBLE);
            GetDataUser getDataUser = new GetDataUser();
            getDataUser.execute();
        }
        return rootView;
    }




    class MyHTTPRequest extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> list = new ArrayList<String>();
            Connection.Response res = null;
            try {
                res = Jsoup.connect("http://online.anidub.com/index.php")
                        .data("login_name",params[0], "login_password", params[1],"login", "submit")//f2sfrwewf11
                        .method(Connection.Method.POST)
                        .execute();
            } catch (IOException e) {
                list.add("404");
                return list;
            }
            //Получаем куки
            Map<String, String> sessionId = res.cookies();
            //Сохраняем куки
            if (sessionId.get("dle_user_id").equals("deleted")) {
                list.clear();
                list.add("203");
                return list;
            } else{
                String PHPSESSID = sessionId.get("PHPSESSID");
                String dle_newpm = sessionId.get("dle_newpm");
                String dle_password = sessionId.get("dle_password");
                String dle_user_id = sessionId.get("dle_user_id");

                Document document = null;
                try {
                    document = Jsoup.connect("http://online.anidub.com").cookies(sessionId).get();
                } catch (IOException e) {
                    list.clear();
                    list.add("503");
                    return list;
                }

                Elements element = document.select("div#userlogin");
                Elements login = element.select("strong");
                //Save login
                list.add(login.text());

                element = document.getElementsByClass("ava");
                Elements ava = element.select("img");
                //Save urlAvatar
                list.add(ava.attr("abs:src"));

                element = document.select("div .link2");
                Elements element1 = element.select("a");
                //Save messages
                list.add(element1.get(0).text());
                //Save favors
                list.add(element1.get(1).text());
                //Save subscribe
                list.add(element1.get(2).text());
                listWatchLater.clear();
                element = document.select("#wl");
                Elements elements = element.select("a");
                for (Element elements1 : elements){
                    listWatchLater.add(new LaterWatch(elements1.text(), elements1.attr("href")));
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("onLogin", true);
                editor.putString("Login", list.get(0));
                editor.putString("PHPSESSID", PHPSESSID);
                editor.putString("dle_newpm", dle_newpm);
                editor.putString("dle_password", dle_password);
                editor.putString("dle_user_id", dle_user_id);

                editor.putBoolean("update",true);
                editor.commit();
            }

            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            if (s.get(0).equals("404")){
                Toast toast = Toast.makeText(getActivity(), "Ошибка связи с сервером! ", Toast.LENGTH_LONG);
                toast.show();
                progressConnect.setVisibility(View.GONE);
                formLogin.setVisibility(View.VISIBLE);

            } else if (s.get(0).equals("203")){
                Toast toast = Toast.makeText(getActivity(), "Ошибка аутентификации!", Toast.LENGTH_LONG);
                toast.show();
                progressConnect.setVisibility(View.GONE);
                formLogin.setVisibility(View.VISIBLE);

            } else if (s.get(0).equals("503")){
                Toast toast = Toast.makeText(getActivity(), "Сервер недоступен", Toast.LENGTH_LONG);
                toast.show();
                progressConnect.setVisibility(View.GONE);
                formLogin.setVisibility(View.VISIBLE);
            }else {
                txtNick.setText("Привет, " + s.get(0) + "!");
                txtMessages.setText(s.get(2));
                txtFavorit.setText(s.get(3));
                txtSubscribe.setText(s.get(4));
                imageLoader.displayImage(s.get(1), imgAvatar);
                progressConnect.setVisibility(View.GONE);
                listViewLater.setAdapter(new CustomAdapter(getActivity(),R.layout.spiner_row, listWatchLater));

                profileView.setVisibility(View.VISIBLE);

            }


        }
    }

    class GetDataUser extends AsyncTask<Void,Void,ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> list = new ArrayList<String>();

            String PHPSESSID = sharedPreferences.getString("PHPSESSID","");
            String dle_newpm = sharedPreferences.getString("dle_newpm","");
            String dle_password = sharedPreferences.getString("dle_password","");
            String dle_user_id = sharedPreferences.getString("dle_user_id","");

            Map<String,String> sessionId = new HashMap<String,String>();
            sessionId.put("PHPSESSID",PHPSESSID);
            sessionId.put("dle_newpm",dle_newpm);
            sessionId.put("dle_password",dle_password);
            sessionId.put("dle_user_id",dle_user_id);

            Document document = null;
            try {
                document = Jsoup.connect("http://online.anidub.com").cookies(sessionId).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Elements element = document.select("div#userlogin");
            Elements login = element.select("strong");
            //Save login
            list.add(login.text());

            element = document.getElementsByClass("ava");
            Elements ava = element.select("img");
            //Save urlAvatar
            list.add(ava.attr("abs:src"));

            element = document.select("div .link2");
            Elements element1 = element.select("a");
            //Save messages
            list.add(element1.get(0).text());
            //Save favors
            list.add(element1.get(1).text());
            //Save subscribe
            list.add(element1.get(2).text());
            listWatchLater.clear();
            element = document.select("#wl");
            Elements elements = element.select("a");
            for (Element elements1 : elements){
                listWatchLater.add(new LaterWatch(elements1.text(), elements1.attr("href")));
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            if (s.size() != 0 || s.size() != 1) {
                txtNick.setText("Привет, " + s.get(0) + "!");
                txtMessages.setText(s.get(2));
                txtFavorit.setText(s.get(3));
                txtSubscribe.setText(s.get(4));
                imageLoader.displayImage(s.get(1), imgAvatar);
                progressConnect.setVisibility(View.GONE);
                listViewLater.setAdapter(new CustomAdapter(getActivity(),R.layout.spiner_row, listWatchLater));
                listViewLater.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                        Bundle bundle = new Bundle();
                        Fragment f;
                        FragmentManager fragmentManager;
                        FragmentTransaction ft;
                        bundle.putString("url", "http://online.anidub.com/" + listWatchLater.get(position).getUrlName());
                        bundle.putString("urlImg", "");
                        f = new AboutFilmFragment();
                        f.setArguments(bundle);
                        fragmentManager = getFragmentManager();
                        ft = fragmentManager.beginTransaction();
                        ft.replace(R.id.container, f);
                        ft.addToBackStack("bomzh");
                        ft.commit();
                    }
                });

                profileView.setVisibility(View.VISIBLE);
            }
        }
    }
       class CustomAdapter extends ArrayAdapter {
        private Context context;
        private int textViewResourceId;
        private ArrayList<LaterWatch> objects;

        CustomAdapter(Context context, int textViewResourceId,
                             ArrayList<LaterWatch> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            this.textViewResourceId = textViewResourceId;
            this.objects = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(context, textViewResourceId, null);
                TextView tv = (TextView) convertView;
                tv.setTextSize(20);
                tv.setText(objects.get(position).getName());
            return convertView;
        }
    }
    public void logIN(){
        String login = editLogin.getText().toString();
        String pass = editPassword.getText().toString();
        if (login.equals("") || pass.equals("")) {
            Toast toast = Toast.makeText(getActivity(), "Вы не ввели логин или пароль!", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            formLogin.setVisibility(View.GONE);
            progressConnect.setVisibility(View.VISIBLE);
            MyHTTPRequest httpRequest = new MyHTTPRequest();
            httpRequest.execute(new String[]{login, pass});
            Toast toast = Toast.makeText(getActivity(), "Входим! ", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public void logOUT(){
        profileView.setVisibility(View.GONE);
        progressConnect.setVisibility(View.VISIBLE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("onLogin", false);
        editor.remove("Login");
        editor.remove("PHPSESSID");
        editor.remove("dle_newpm");
        editor.remove("dle_password");
        editor.remove("dle_user_id");
        editor.commit();
        progressConnect.setVisibility(View.GONE);
        formLogin.setVisibility(View.VISIBLE);

    }
    class LaterWatch{
        String name;
        String urlName;

        LaterWatch(String name, String urlName) {
            this.name = name;
            this.urlName = urlName;
        }

        public String getName() {
            return name;
        }
        public String getUrlName() {
            return urlName;
        }
    }

}
