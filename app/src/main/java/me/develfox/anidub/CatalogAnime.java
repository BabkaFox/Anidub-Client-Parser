package me.develfox.anidub;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by max on 08.11.2014.
 */
public class CatalogAnime extends Fragment {
    Spinner spinnerGenre,spinnerDubbres,spinnerYears;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.catalog_layout, container, false);
        spinnerGenre = (Spinner) rootView.findViewById(R.id.spinnerGenre);
        spinnerDubbres = (Spinner) rootView.findViewById(R.id.spinnerDubbers);
        spinnerYears = (Spinner) rootView.findViewById(R.id.spinnerYears);

        Resources res = getResources();
        String[] genre = res.getStringArray(R.array.genre);
        String[] dubbres = res.getStringArray(R.array.dubbers);
        String[] years = res.getStringArray(R.array.years);
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String> list2 = new ArrayList<String>();
        ArrayList<String> list3 = new ArrayList<String>();
        for (String s:genre)
            list.add(s);
        for (String s:dubbres)
            list2.add(s);
        for (String s:years)
            list3.add(s);

        spinnerGenre.setAdapter(new CustomAdapter(getActivity(),R.layout.spiner_row,list));
        spinnerDubbres.setAdapter(new CustomAdapter(getActivity(),R.layout.spiner_row,list2));
        spinnerYears.setAdapter(new CustomAdapter(getActivity(),R.layout.spiner_row,list3));

        return rootView;
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
