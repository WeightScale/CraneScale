package com.kostya.cranescale;

/**
 * Created by IntelliJ IDEA.
 * User: Kostya
 * Date: 23.10.11
 * Time: 9:20
 * To change this template use File | Settings | File Templates.
 */

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class BluetoothListAdapter extends ArrayAdapter<BluetoothDevice>
{
    private final ArrayList<BluetoothDevice> items;
    private ArrayAdapter<BluetoothDevice> sitems;

    public BluetoothListAdapter(Context context, ArrayList<BluetoothDevice> items)
    {
        super(context, R.layout.list_item_bluetooth, items);
        this.items = items;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater)super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_bluetooth, null);
        }

        BluetoothDevice o = items.get(position);
        if (o != null)
        {
            TextView tt = (TextView) v.findViewById(R.id.topText);
            TextView bt = (TextView) v.findViewById(R.id.bottomText);
            if (tt != null)
                tt.setText(o.getName());
            if (bt != null)
            {
                String addr = o.getAddress();
                if (addr.equalsIgnoreCase("-"))
                    if (tt != null) {
                        tt.setTextColor(0xFFFF5050);
                    }
                else
                    tt.setTextColor(0xFFFFFFFF);
                bt.setText(addr);
            }
        }

        return v;
    }
}

class DeviceDescriptor
{
	private final String Name;
	private final String Address;

	public DeviceDescriptor(String name, String address)
	{
		Name = name;
		Address = address;
	}

	public String getName()
	{
		return Name;
	}

	public String getAddress()
	{
		return Address;
	}
}
