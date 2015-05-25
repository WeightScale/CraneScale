package com.kostya.cranescale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 08.01.14
 * Time: 17:59
 * To change this template use File | Settings | File Templates.
 */
class WeightListAdapter extends ArrayAdapter<WeightDescriptor> {
    private final ArrayList<WeightDescriptor> items;
    private final int resourceId;
    public WeightListAdapter(Context context,  int textViewResourceId, ArrayList<WeightDescriptor> objects) {
        super(context,  textViewResourceId, objects);
        this.items = objects;
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null){
            LayoutInflater vi = (LayoutInflater)super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = vi.inflate(resourceId, null);
        }

        WeightDescriptor weightDescriptor = items.get(position);
        if(weightDescriptor != null){
            TextView textViewDate = (TextView) v.findViewById(R.id.list_weight_time);
            TextView textViewWeight = (TextView) v.findViewById(R.id.list_weight_weight);
            ImageButton imageButton = (ImageButton)v.findViewById(R.id.imageButton);
            imageButton.setTag(weightDescriptor);
            textViewDate.setText(weightDescriptor.getDate());
            textViewWeight.setText(weightDescriptor.getWeight());
            v.setTag(weightDescriptor);
        }
        return v;
    }
}

class WeightDescriptor {
    private final String date;
    private final String weight;

    public WeightDescriptor(String d, String w){
        this.date = d;
        this.weight = w;
    }

    String getDate(){
        return date;
    }

    String getWeight(){
        return weight;
    }
}
