package com.surajit.bitcanny.mqtttest;

/**
 * Created by Surajit Sarkar on 20/7/16.
 */
public interface FragmentBackPressedListener {
    /**
     *
     * @return true if event is captured and you do not want to pass this to the containing activity.
     * false  otherwise
     */
    public boolean onBackPressed();
}
