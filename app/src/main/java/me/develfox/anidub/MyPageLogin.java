package me.develfox.anidub;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by max on 28.10.2014.
 */
public class MyPageLogin extends Fragment {
    TextView textLogin;
    TextView textPassword;
    Button btnLogin,btnRazlogin;
    TextView txtLogi, textViewLogin, textViewMessages, textViewFavorits, textViewSubscribe;
    ImageView imageAvatarka;
    GridLayout gridData;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    static String loginName ="";
    ImageLoader imageLoader;
    String urlAvatar,messages,favors,subscribe;
    LinearLayout loginLiner;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_layout, container, false);

        textLogin = (TextView) rootView.findViewById(R.id.editLogin);
        textPassword = (TextView) rootView.findViewById(R.id.editPassword);
        txtLogi = (TextView) rootView.findViewById(R.id.txtLogi);
        btnLogin = (Button) rootView.findViewById(R.id.btnLogin);
        btnRazlogin = (Button) rootView.findViewById(R.id.btnRazlLogin);
        btnRazlogin.setEnabled(false);
        btnRazlogin.setVisibility((View.INVISIBLE));
        gridData = (GridLayout) rootView.findViewById(R.id.gridData);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressLoging);
//        progressBar.setVisibility(View.INVISIBLE);
        loginLiner = (LinearLayout) rootView.findViewById(R.id.loginLinerLayout);

        loginLiner.removeView(progressBar);

        textViewLogin = (TextView) rootView.findViewById(R.id.viewLogin);
        textViewLogin.setVisibility((View.INVISIBLE));

        textViewMessages = (TextView) rootView.findViewById(R.id.viewMessages);
        textViewMessages.setVisibility((View.INVISIBLE));

        textViewFavorits = (TextView) rootView.findViewById(R.id.viewFavorits);
        textViewFavorits.setVisibility((View.INVISIBLE));

        textViewSubscribe = (TextView) rootView.findViewById(R.id.viewSubcribe);
        textViewSubscribe.setVisibility((View.INVISIBLE));
        imageAvatarka = (ImageView) rootView.findViewById(R.id.imageAvatarka);
        imageAvatarka.setVisibility((View.INVISIBLE));

        sharedPreferences = getActivity().getSharedPreferences("cookies", Context.MODE_PRIVATE);

        imageAvatarka = (ImageView) rootView.findViewById(R.id.imageAvatarka);
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
        final GetDataUser getDataUser = new GetDataUser();

        boolean onLogin = sharedPreferences.contains("onLogin");
        if (onLogin && sharedPreferences.getBoolean("onLogin",false)) {
            btnLogin.setEnabled(false);
            loginLiner.removeView(gridData);
            loginLiner.removeView(progressBar);
            loginLiner.removeView(btnLogin);
            String myLogin = sharedPreferences.getString("Login","");
            textViewLogin.setVisibility(View.VISIBLE);
            textViewMessages.setVisibility(View.VISIBLE);
            textViewFavorits.setVisibility(View.VISIBLE);
            textViewSubscribe.setVisibility(View.VISIBLE);
            textViewLogin.setText("Привет, " + myLogin + "!");
            btnRazlogin.setEnabled(true);
            btnRazlogin.setVisibility((View.VISIBLE));
            getDataUser.execute();


        }

        btnRazlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("onLogin", false);
                editor.remove("Login");
                editor.remove("UrlAvatar");
                editor.remove("PHPSESSID");
                editor.remove("dle_newpm");
                editor.remove("dle_password");
                editor.remove("dle_user_id");
                editor.commit();
                loginLiner.addView(btnLogin);
                loginLiner.addView(gridData);
                btnLogin.setEnabled(true);
                btnRazlogin.setEnabled(false);
                textLogin.setEnabled(false);
                textPassword.setEnabled(false);
                btnRazlogin.setVisibility(View.INVISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
                gridData.setVisibility(View.VISIBLE);
                textViewLogin.setVisibility(View.INVISIBLE);
                textViewMessages.setVisibility(View.INVISIBLE);
                textViewFavorits.setVisibility(View.INVISIBLE);
                textViewSubscribe.setVisibility(View.INVISIBLE);
                imageAvatarka.setVisibility(View.INVISIBLE);
                if (getDataUser.getStatus() != AsyncTask.Status.FINISHED){
                    getDataUser.cancel(true);
                }

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = textLogin.getText().toString();
                String pass = textPassword.getText().toString();
                if (login.equals("") || pass.equals("")){
                    Toast toast = Toast.makeText(getActivity(), "Вы не ввели логин или пароль!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    loginLiner.addView(progressBar);
                    MyHTTPRequest httpRequest = new MyHTTPRequest();
                    httpRequest.execute(new String[]{login, pass});
                    txtLogi.setText("Входим");
                    Toast toast = Toast.makeText(getActivity(), "Началось! ", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        return rootView;
    }



    class MyHTTPRequest extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            Connection.Response res = null;
            try {
                res = Jsoup.connect("http://online.anidub.com/index.php")
                        .data("login_name",params[0], "login_password", params[1],"login", "submit")//f2sfrwewf11
                        .method(Connection.Method.POST)
                        .execute();
            } catch (IOException e) {
                return "404";
            }
            //Получаем куки
            Map<String, String> sessionId = res.cookies();
            //Сохраняем куки
            if (sessionId.get("dle_user_id").equals("deleted")) {
            return "203";
            } else{
                String PHPSESSID = sessionId.get("PHPSESSID");
                String dle_newpm = sessionId.get("dle_newpm");
                String dle_password = sessionId.get("dle_password");
                String dle_user_id = sessionId.get("dle_user_id");


                Document document = null;
                try {
                    document = Jsoup.connect("http://online.anidub.com").cookies(sessionId).get();
                } catch (IOException e) {
                    return "503";
                }

                Elements element = document.select("div#userlogin");
                Elements login = element.select("strong");
                loginName = login.text();

                element = document.getElementsByClass("ava");
                Elements ava = element.select("img");
                urlAvatar = ava.attr("abs:src");

                element = document.select("div .link2");
                Elements element1 = element.select("a");
                messages = element1.get(0).text();
                favors = element1.get(1).text();
                subscribe = element1.get(2).text();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("onLogin", true);
                editor.putString("Login", loginName);
                editor.putString("PHPSESSID", PHPSESSID);
                editor.putString("dle_newpm", dle_newpm);
                editor.putString("dle_password", dle_password);
                editor.putString("dle_user_id", dle_user_id);
                editor.commit();
            }

            return loginName;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("404")){
                txtLogi.setText("Ошибка связи с свервером");
                loginLiner.removeView(progressBar);

            } else if (s.equals("203")){
                txtLogi.setText("Ошибка аутентификации");
                loginLiner.removeView(progressBar);

            } else if (s.equals("503")){
                txtLogi.setText("Сервер недоступен");
                loginLiner.removeView(progressBar);

            }else {
                txtLogi.setText("Добро пожаловать " + s);
                btnLogin.setEnabled(false);
                loginLiner.removeView(btnLogin);
                loginLiner.removeView(gridData);
                loginLiner.removeView(progressBar);
                textLogin.setEnabled(false);
                textPassword.setEnabled(false);
                btnRazlogin.setEnabled(true);
                btnRazlogin.setVisibility((View.VISIBLE));
                textViewLogin.setVisibility(View.VISIBLE);
                textViewLogin.setText("Привет, " + s + "!");
                imageAvatarka.setVisibility(View.VISIBLE);

                textViewFavorits.setVisibility(View.VISIBLE);
                textViewSubscribe.setVisibility(View.VISIBLE);
                textViewMessages.setVisibility(View.VISIBLE);

                textViewMessages.setText(messages);
                textViewSubscribe.setText(subscribe);
                textViewFavorits.setText(favors);

                imageLoader.displayImage(urlAvatar,imageAvatarka);

            }


        }
    }
    class GetDataUser extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {

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
            loginName = login.text();

            element = document.getElementsByClass("ava");
            Elements ava = element.select("img");
            urlAvatar = ava.attr("abs:src");

            element = document.select("div .link2");
            Elements element1 = element.select("a");
            messages = element1.get(0).text();
            favors = element1.get(1).text();
            subscribe = element1.get(2).text();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            textViewMessages.setText(messages);
            textViewFavorits.setText(favors);
            textViewSubscribe.setText(subscribe);
            imageLoader.displayImage(urlAvatar,imageAvatarka);
        }
    }

}
