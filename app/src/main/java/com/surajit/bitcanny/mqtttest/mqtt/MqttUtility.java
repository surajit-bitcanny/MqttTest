package com.surajit.bitcanny.mqtttest.mqtt;

import android.content.Context;
import android.util.Log;
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
public abstract class MqttUtility{
    /**
     * This class ensures that the user interface is updated as the Connection objects change their states
     *
     *
     */
    private static String TAG = "MqttUtility";
    //private ChangeListener changeListener = new ChangeListener();

    public static ArrayList<String> populateConnectionList(Context context){

        // get all the available connections
        Map<String, Connection> connections = Connections.getInstance(context)
                .getConnections();
        ArrayList<String> connectionMap = new ArrayList<String>();

        Iterator connectionIterator = connections.entrySet().iterator();
        while (connectionIterator.hasNext()){
            Map.Entry pair = (Map.Entry) connectionIterator.next();
            connectionMap.add((String) pair.getKey());
        }
        return connectionMap;
    }


    public static void updateAndConnect(Context context,ConnectionModel model){
        Map<String, Connection> connections = Connections.getInstance(context)
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
            MqttConnectOptions connOpts = optionsFromModel(model);
            connection.addConnectionOptions(connOpts);
            Connections.getInstance(context).updateConnection(connection);
            connect(context,connection);

        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }


    /**
     * Takes a {@link ConnectionModel} and uses it to connect
     * and then persist.
     * @param model
     */
    public static Connection persistAndConnect(Context context,ConnectionModel model){
        Log.i(TAG, "Persisting new connection:" + model.getClientHandle());
        Connection connection = Connection.createConnection(model.getClientHandle(),model.getClientId(),model.getServerHostName(),model.getServerPort(),context,model.isTlsConnection());
        //connection.registerChangeListener(changeListener);
        MqttConnectOptions connOpts = optionsFromModel(model);
        connection.addConnectionOptions(connOpts);
        Connections.getInstance(context).addConnection(connection);
        //connectionMap.add(model.getClientHandle());
        connect(context,connection);
        return connection;
    }

    public static Connection onlyConnect(Context context,ConnectionModel model){
        Log.i(TAG, "Persisting new connection:" + model.getClientHandle());
        Connection connection = Connection.createConnection(model.getClientHandle(),model.getClientId(),model.getServerHostName(),model.getServerPort(),context,model.isTlsConnection());
        //connection.registerChangeListener(changeListener);
        MqttConnectOptions connOpts = optionsFromModel(model);
        connection.addConnectionOptions(connOpts);
        Connections.getInstance(context).addConnection(connection,false);
        //connectionMap.add(model.getClientHandle());
        connect(context,connection);
        return connection;
    }


    private static MqttConnectOptions optionsFromModel(ConnectionModel model){

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


    private static ActionListener newCallbackInstance(Context context, Connection connection,
                                                      ActionListener.Action action){
        ActionListener callback = new ActionListener(context, action, connection);
        return callback;
    }


    private static void connect(Context context,Connection connection) {
        connection.getClient().setCallback(new MqttCallbackHandler(context, connection.handle()));
        connection.getClient().setTraceCallback(new MqttTraceCallback());
        connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);
        try {
            connection.getClient().connect(connection.getConnectionOptions(),
                    null, newCallbackInstance(context,connection, ActionListener.Action.CONNECT));
        }
        catch (MqttException e) {
            Log.e(TAG, "MqttException Occured", e);
        }
    }

    public static void disconnect(Context context,Connection connection){
        try {
            connection.getClient().disconnect(null,
                    newCallbackInstance(context,connection, ActionListener.Action.DISCONNECT));
        } catch( MqttException ex){
            Log.e(TAG, "Exception occured during disconnect: " + ex.getMessage());
        }
    }

    public static void publish(Context context,Connection connection, String topic, String message, int qos, boolean retain){
        try {
            connection.getClient().publish(topic, message.getBytes(), qos, retain, null,
                    newCallbackInstance(context,connection, ActionListener.Action.PUBLISH));
        } catch( MqttException ex){
            Log.e(TAG, "Exception occured during publish: " + ex.getMessage());
        }
    }

    public static void subscribe(Context context,Connection connection, String topic, int qos){

        try {
            connection.getClient().subscribe(topic, qos,null,
                    newCallbackInstance(context,connection, ActionListener.Action.SUBSCRIBE));
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
