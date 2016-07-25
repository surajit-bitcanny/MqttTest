package com.surajit.bitcanny.mqtttest.mqtt;

/**
 * Created by Surajit Sarkar on 25/7/16.
 * Company : Bitcanny Technologies Pvt. Ltd.
 * Email   : surajit@bitcanny.com
 */
public interface IConnectionStatusListener {
    public void onConnected();
    public void onDisconnected();
}
