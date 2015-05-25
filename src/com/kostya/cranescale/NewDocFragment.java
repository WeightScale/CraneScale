package com.kostya.cranescale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;


import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

//import static com.victjava.scales.ActivityCheck.WeightType.*;

//import static com.victjava.scales.ActivityCheck.*;

/*
 * Created by Kostya on 09.03.2015.
 */

public class NewDocFragment extends Fragment {

    private TextView viewFirst;
    private Spinner spinnerContact;
    LinearLayout defaultTape;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_doc, container, false);
        defaultTape = (LinearLayout)v.findViewById(R.id.defaultTape);
        spinnerContact = (Spinner)v.findViewById(R.id.spinnerVendor);
        setupSpinnerContact();
        final CheckedTextView checkedBoxTape = (CheckedTextView) v.findViewById(R.id.checkBoxTape);
        checkedBoxTape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedBoxTape.isChecked()){
                    defaultTape.setVisibility(View.GONE);
                    checkedBoxTape.setChecked(false);
                }
                else{
                    defaultTape.setVisibility(View.VISIBLE);
                    checkedBoxTape.setChecked(true);
                }

            }
        });

        final CheckedTextView checkedAutoSum = (CheckedTextView) v.findViewById(R.id.checkBoxAutoSum);
        checkedAutoSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedAutoSum.isChecked())
                    checkedAutoSum.setChecked(false);
                else
                    checkedAutoSum.setChecked(true);
            }
        });
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

    public void setupSpinnerContact() {

        String[] from;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            from = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_URI};
        else
            from = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY};
        int[] to = {R.id.contactName, R.id.contactPhoto};
        SimpleCursorAdapter vendorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.vendor_spinner_item, getContact(), from, to);
        vendorAdapter.setDropDownViewResource(R.layout.vendor_spinner_drop_item);
        spinnerContact.setAdapter(vendorAdapter);

    }

    Cursor getContact() {
        return getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
    }
}
