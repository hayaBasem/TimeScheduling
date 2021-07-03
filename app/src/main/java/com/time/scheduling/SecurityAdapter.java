package com.time.scheduling;


import android.content.Context;
import android.widget.ArrayAdapter;

public class SecurityAdapter  extends ArrayAdapter<String> {

    public SecurityAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        // TODO Auto-generated constructor stub

    }

    @Override
    public int getCount() {

        // TODO Auto-generated method stub
        int count = super.getCount();

        return count>0 ? count-1 : count ;


    }


}