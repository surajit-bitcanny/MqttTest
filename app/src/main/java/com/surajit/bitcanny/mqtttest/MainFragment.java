package com.surajit.bitcanny.mqtttest;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.surajit.bitcanny.mqtttest.mqtt.Connection;
import com.surajit.bitcanny.mqtttest.mqtt.ConnectionModel;
import com.surajit.bitcanny.mqtttest.mqtt.Connections;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends BaseFragment {

    private String TAG = this.getClass().getName();
    private Connections connections;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        //MqttMessage

        connections = Connections.getInstance(getActivity());
        ConnectionModel connectionModel = new ConnectionModel();
        //Connection connection = Connection.createConnection()



        return view;
    }

    public MqttAndroidClient createMqttClinet(String host, String port) {
        String clientId = MqttClient.generateClientId();
        /*MqttAndroidClient client =
                new MqttAndroidClient(getActivity().getApplicationContext(), "tcp://broker.hivemq.com:1883",
                        clientId);*/
        MqttAndroidClient client =
                new MqttAndroidClient(getActivity().getApplicationContext(), host + ":" + port, clientId);
        return client;
    }

    public IMqttToken connect(MqttAndroidClient client) {
        IMqttToken token = null;
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            //set mqtt version
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

            //set LWS
            String topic = "users/last/will";
            byte[] payload = "some payload".getBytes();
            options.setWill(topic, payload, 1, false);

            //set username/password
            options.setUserName("USERNAME");
            options.setPassword("PASSWORD".toCharArray());

            //IMqttToken token = client.connect(options);
            token = client.connect();//By default connect with MQTT 3.1.1
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "MQTT connection onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "MQTT connection onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return token;
    }

    public IMqttToken publish(MqttAndroidClient client, String topic, String payload) {
        byte[] encodedPayload = new byte[0];
        IMqttToken token = null;
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);//Publish a retained message
            token = client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
        return token;
    }

    public IMqttToken subscribe(MqttAndroidClient client, String topic, int qos) {
        IMqttToken token = null;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return token;
    }

    public IMqttToken unsubscribe(MqttAndroidClient client, String topic) {
        IMqttToken token = null;
        try {
            token = client.unsubscribe(topic);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return token;
    }

    public IMqttToken disconnect(MqttAndroidClient client){
        IMqttToken disconToken = null;
        try {
            disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return disconToken;
    }
}
