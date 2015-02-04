package com.ist.login.testloginapp;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<Item> {

    protected Context context;
    protected ArrayList<Item> items;
    protected LayoutInflater vi;

    public CustomAdapter(Context context,ArrayList<Item> items) {
        super(context,0, items);
        this.context = context;
        this.items = items;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        final Item i = items.get(position);
        if (i != null) {
            ActivityModel ei = (ActivityModel)i;
            v = vi.inflate(R.layout.list_item, null);
            final TextView title = (TextView)v.findViewById(R.id.activity_name);
            final TextView subtitle = (TextView)v.findViewById(R.id.activity_points);
            //final TextView date = (TextView)v.findViewById(R.id.activity_date);
            final ImageView icon_fb = (ImageView)v.findViewById(R.id.img);

            if (title != null)
                title.setText(ei.getName());
            if(subtitle != null)
                subtitle.setText(ei.getFormattedPoints() + " - " + ei.getDate());
           /* if (date != null)
                date.setText(ei.getDate());*/
            if(icon_fb != null)
                icon_fb.setVisibility(ei.getIsFbPost()? View.VISIBLE: View.INVISIBLE);

        }
        return v;
    }

}