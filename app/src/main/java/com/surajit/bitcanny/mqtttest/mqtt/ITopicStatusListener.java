package com.surajit.bitcanny.mqtttest.mqtt;

/**
 * Created by surajit on 25/7/16.
 */
public interface ITopicStatusListener {
    public void onTopicSubscribed(String topic, boolean status);
    public void onTopicPublished(String topic, boolean status);
}
