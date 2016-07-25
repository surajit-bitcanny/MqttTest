package com.surajit.bitcanny.mqtttest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.surajit.bitcanny.mqtttest.mqtt.ActionListener;
import com.surajit.bitcanny.mqtttest.mqtt.ActivityConstants;
import com.surajit.bitcanny.mqtttest.mqtt.Connection;
import com.surajit.bitcanny.mqtttest.mqtt.ConnectionModel;
import com.surajit.bitcanny.mqtttest.mqtt.Connections;
import com.surajit.bitcanny.mqtttest.mqtt.MqttCallbackHandler;
import com.surajit.bitcanny.mqtttest.mqtt.MqttTraceCallback;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by surajit on 24/7/16.
 */
public abstract class MqttActivity extends BaseActivity {
    /**
     * This class ensures that the user interface is updated as the Connection objects change their states
     *
     *
     */
    private String TAG = "MqttActivity";
    private ChangeListener changeListener = new ChangeListener();

    private ArrayList<String> populateConnectionList(){

        // get all the available connections
        Map<String, Connection> connections = Connections.getInstance(this)
                .getConnections();
        ArrayList<String> connectionMap = new ArrayList<String>();

        Iterator connectionIterator = connections.entrySet().iterator();
        while (connectionIterator.hasNext()){
            Map.Entry pair = (Map.Entry) connectionIterator.next();
            connectionMap.add((String) pair.getKey());
        }
        return connectionMap;
    }


    public void updateAndConnect(ConnectionModel model){
        Map<String, Connection> connections = Connections.getInstance(this)
                .getConnections();

        Log.i(TAG, "Updating connection: " + connections.keySet().toString());
        try {
            Connection connection = connections.get(model.getClientHandle());
            // First disconnect the current instance of this connection
            if(connection.isConnected()){
                connection.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTING);
                connection.getClient().disconnect();
            }
            // Update the connection.
            connection.updateConnection(model.getClientId(), model.getServerHostName(), model.getServerPort(), model.isTlsConnection());
            connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);
            connection.getClient().setCallback(new MqttCallbackHandler(this, model.getClientHandle()));
            connection.getClient().setTraceCallback(new MqttTraceCallback());
            MqttConnectOptions connOpts = optionsFromModel(model);
            connection.addConnectionOptions(connOpts);
            Connections.getInstance(this).updateConnection(connection);
            connection.getClient().connect(connOpts, null, newCallbackInstance(connection));

        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }


    /**
     * Takes a {@link ConnectionModel} and uses it to connect
     * and then persist.
     * @param model
     */
    public void persistAndConnect(ConnectionModel model){
        Log.i(TAG, "Persisting new connection:" + model.getClientHandle());
        Connection connection = Connection.createConnection(model.getClientHandle(),model.getClientId(),model.getServerHostName(),model.getServerPort(),this,model.isTlsConnection());
        connection.registerChangeListener(changeListener);
        connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);
        connection.getClient().setCallback(new MqttCallbackHandler(this, model.getClientHandle()));
        connection.getClient().setTraceCallback(new MqttTraceCallback());
        MqttConnectOptions connOpts = optionsFromModel(model);
        connection.addConnectionOptions(connOpts);
        Connections.getInstance(this).addConnection(connection);
        //connectionMap.add(model.getClientHandle());

        try {
            connection.getClient().connect(connOpts, null, newCallbackInstance(connection));
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "MqttException Occured", e);
        }

    }


    private MqttConnectOptions optionsFromModel(ConnectionModel model){

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(model.isCleanSession());
        connOpts.setConnectionTimeout(model.getTimeout());
        connOpts.setKeepAliveInterval(model.getKeepAlive());
        if(!model.getUsername().equals(ActivityConstants.empty)){
            connOpts.setUserName(model.getUsername());
        }

        if(!model.getPassword().equals(ActivityConstants.empty)){
            connOpts.setPassword(model.getPassword().toCharArray());
        }
        if(!model.getLwtTopic().equals(ActivityConstants.empty) && !model.getLwtMessage().equals(ActivityConstants.empty)){
            connOpts.setWill(model.getLwtTopic(), model.getLwtMessage().getBytes(), model.getLwtQos(), model.isLwtRetain());
        }
        //   if(tlsConnection){
        //       // TODO Add Keys to conOpts here
        //       //connOpts.setSocketFactory();
        //   }
        return connOpts;
    }


    private ActionListener newCallbackInstance(Connection connection){
        String[] actionArgs = new String[1];
        actionArgs[0] = connection.getId();
        ActionListener callback = new ActionListener(this,
                ActionListener.Action.CONNECT, connection, actionArgs);
        return callback;
    }


    public void connect(Connection connection) {
        connection.getClient().setCallback(new MqttCallbackHandler(this, connection.handle()));
        try {
            connection.getClient().connect(connection.getConnectionOptions(), null, newCallbackInstance(connection));
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "MqttException Occured", e);
        }
    }

    public void disconnect(Connection connection){
        try {
            connection.getClient().disconnect(null,newCallbackInstance(connection));
        } catch( MqttException ex){
            Log.e(TAG, "Exception occured during disconnect: " + ex.getMessage());
        }
    }

    public void publish(Connection connection, String topic, String message, int qos, boolean retain){
        try {
            connection.getClient().publish(topic, message.getBytes(), qos, retain, null, newCallbackInstance(connection));
        } catch( MqttException ex){
            Log.e(TAG, "Exception occured during publish: " + ex.getMessage());
        }
    }

    public void subscribe(Connection connection, String topic, int qos){

        try {
            connection.getClient().subscribe(topic, qos,null,newCallbackInstance(connection));
        } catch( MqttException ex){
            Log.e(TAG, "Exception occured during subscribe: " + ex.getMessage());
        }
    }


    private class ChangeListener implements PropertyChangeListener {

        /**
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent event) {

            if (!event.getPropertyName().equals(ActivityConstants.ConnectionStatusProperty)) {
                return;
            }
            /*mainActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mainActivity.drawerFragment.notifyDataSetChanged();
                }

            });*/

        }
    }
}
