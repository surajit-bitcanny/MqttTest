package com.surajit.bitcanny.mqtttest;

import android.os.Bundle;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoadFragment(MainFragment.newInstance());
    }

    @Override
    protected int getFragmentContainer() {
        return R.id.container;
    }
}
