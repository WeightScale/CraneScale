package com.kostya.cranescale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.kostya.cranescale.provider.WeighingDbAdapter;
import com.kostya.cranescale.provider.WeightDocDbAdapter;

//import static com.victjava.scales.ActivityCheck.WeightType.*;

//import static com.victjava.scales.ActivityCheck.*;

/*
 * Created by Kostya on 09.03.2015.
 */

public class ListCheckFragment extends ListFragment {

    private TextView viewFirst;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_check, container, false);

        listSetup();
        return v;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void listSetup() {

        Cursor cursor = new WeighingDbAdapter(getActivity()).getAllEntries(2);
        if (cursor == null) {
            return;
        }
        String[] columns = {
                WeightDocDbAdapter.KEY_ID,
                WeightDocDbAdapter.KEY_DATE,
                WeightDocDbAdapter.KEY_TIME};

        int[] to = {
                R.id.checkId,
                R.id.dateCheck,
                R.id.timeCheck};
        SimpleCursorAdapter namesAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_check, cursor, columns, to);
        //namesAdapter.setViewBinder(new ListCheckViewBinder());
        setListAdapter(namesAdapter);
        //MyCursorAdapter namesAdapter = new MyCursorAdapter(getApplicationContext(), R.layout.item_check, cursor, columns, to);
        //setListAdapter(namesAdapter);
        //setTitle(getString(R.string.Checks_closed) + getString(R.string.qty) + listView.getCount()); //установить заголовок

    }
}
