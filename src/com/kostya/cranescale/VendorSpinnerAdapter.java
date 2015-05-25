package com.kostya.cranescale;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Kostya
 * Date: 24.10.11
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
class VendorSpinnerAdapter extends ArrayAdapter<VendorSpinnerDescriptor> {
    private final ArrayList<VendorSpinnerDescriptor> items;
    public VendorSpinnerAdapter(Context context, int textViewResourceId, ArrayList<VendorSpinnerDescriptor> items){

        super(context, textViewResourceId, items);
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if (v == null){
            LayoutInflater vi = (LayoutInflater)super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.vendor_spinner_item, null);
            VendorSpinnerDescriptor vendorSpinnerDescriptor = items.get(position);
            if (vendorSpinnerDescriptor != null){
                TextView tt = (TextView) v.findViewById(R.id.topText);
                TextView bt = (TextView) v.findViewById(R.id.bottomText);
                tt.setTextColor(Color.BLACK);
                tt.setText(vendorSpinnerDescriptor.getName());
                if (bt != null){
                    String addr = vendorSpinnerDescriptor.getAddress();
                    if (addr.equalsIgnoreCase("-"))
                        tt.setTextColor(0xFFFF5050);
                    else
                        tt.setTextColor(0xFF000000);
                    bt.setText(addr);
                    bt.setVisibility(View.GONE);
                }
            }
        }
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if (v == null){
            LayoutInflater vi = (LayoutInflater)super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.vendor_spinner_drop_item, null);
            VendorSpinnerDescriptor vendorSpinnerDescriptor = items.get(position);
            if (vendorSpinnerDescriptor != null){
                TextView tt = (TextView) v.findViewById(R.id.topText);
                tt.setTextColor(Color.BLACK);
                TextView bt = (TextView) v.findViewById(R.id.bottomText);
                tt.setText(vendorSpinnerDescriptor.getName());
                if (bt != null){
                    String addr = vendorSpinnerDescriptor.getAddress();
                    if (addr.equalsIgnoreCase("-"))
                        tt.setTextColor(0xFFFF5050);
                    else
                        tt.setTextColor(0xFF000000);
                    bt.setText(addr);
                }
            }
        }
        return  v;
    }
}

//======================================================================================================================
class VendorSpinnerDescriptor
{
	private final String Name;
	private final String Address;

	public VendorSpinnerDescriptor(String name, String address){
        //super(context,textViewResourceId,null);
		Name = name;
		Address = address;
	}

	public String getName(){
		return Name;
	}
    public String getAddress(){
		return Address;
	}
}
//======================================================================================================================
