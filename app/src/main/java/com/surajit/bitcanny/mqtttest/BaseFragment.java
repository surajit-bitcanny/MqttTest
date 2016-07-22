package com.surajit.bitcanny.mqtttest;

import android.support.v4.app.Fragment;

/**
 * Created by Surajit Sarkar on 22/7/16.
 * Company : Bitcanny Technologies Pvt. Ltd.
 * Email   : surajit@bitcanny.com
 */
public class BaseFragment extends Fragment implements FragmentBackPressedListener {
    @Override
    public boolean onBackPressed() {
        return false;
    }
}
