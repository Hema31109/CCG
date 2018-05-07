package com.newtechs.locations.cg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Dell on 05-05-2018.
 */

public class Custom extends BaseAdapter {
    ArrayList arrayList;
    Context context;
    LayoutInflater inflater;
    public Custom(Context context,ArrayList arrayList)
    {
     this.context=context;
     this.arrayList=arrayList;
     inflater=LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        convertView=inflater.inflate(R.layout.custom,null);
        TextView textView=convertView.findViewById(R.id.text);
        return convertView;
    }
}
