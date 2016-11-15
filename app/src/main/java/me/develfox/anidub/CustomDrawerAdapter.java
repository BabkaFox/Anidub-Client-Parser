package me.develfox.anidub;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by max on 02.11.2014.
 */
public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {
    Context context;
    List<DrawerItem> drawerItemList;
    int layoutResID;

    public CustomDrawerAdapter(Context context, int layoutResourceID, List<DrawerItem> listItems) {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.drawerItemList = listItems;
        this.layoutResID = layoutResourceID;

    }
    public void setChecked(int pos, boolean checked) {
        drawerItemList.get(pos).checked = checked;
        notifyDataSetChanged();
    }
    public void setCounter(int pos, String count){
        drawerItemList.get(pos).count = count;
        notifyDataSetChanged();
    }

    public void resetarCheck() {
        for (int i = 0; i < drawerItemList.size(); i++) {
            drawerItemList.get(i).checked = false;
        }
        this.notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        DrawerItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.ItemName = (TextView) view.findViewById(R.id.drawer_itemName);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);

            //This My Code
            drawerHolder.count = (TextView) view.findViewById(R.id.counter);
            drawerHolder.viewNavigation = (View) view.findViewById(R.id.viewNavigation);

            view.setTag(drawerHolder);


        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();

        }

        DrawerItem dItem = (DrawerItem) this.drawerItemList.get(position);

        drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));
        drawerHolder.ItemName.setText(dItem.getItemName());
        int countSub = Integer.parseInt(dItem.getCount());
        if (countSub <= 0){
            drawerHolder.count.setVisibility(View.GONE);
        } else {
            drawerHolder.count.setText(dItem.getCount());
        }
        if (dItem.checked) {
            drawerHolder.viewNavigation.setVisibility(View.VISIBLE);
        } else {
            drawerHolder.viewNavigation.setVisibility(View.GONE);
        }
        return view;
    }

    private static class DrawerItemHolder {
        TextView ItemName;
        ImageView icon;
        TextView count;
        View viewNavigation;
    }
}
