package com.surajit.bitcanny.mqtttest.mqtt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

/**
 * This Class handles receiving information from the
 * {@link MqttAndroidClient} and updating the {@link Connection} associated with
 * the action
 */
public class ActionListener implements IMqttActionListener {

    private static final String TAG = "ActionListener";

    /**
     * Actions that can be performed Asynchronously <strong>and</strong> associated with a
     * {@link ActionListener} object
     */
    public enum Action {
        /**
         * Connect Action
         **/
        CONNECT,
        /**
         * Disconnect Action
         **/
        DISCONNECT,
        /**
         * Subscribe Action
         **/
        SUBSCRIBE,
        /**
         * Publish Action
         **/
        PUBLISH
    }

    /**
     * The {@link Action} that is associated with this instance of
     * <code>ActionListener</code>
     **/
    private Action action;

    private Connection connection;
    /**
     * Handle of the {@link Connection} this action was being executed on
     **/
    private String clientHandle;
    /**
     * {@link Context} for performing various operations
     **/
    private Context context;

    /**
     * Creates a generic action listener for actions performed form any activity
     *
     * @param context        The application context
     * @param action         The action that is being performed
     * @param connection     The connection
     */
    public ActionListener(Context context, Action action,
                          Connection connection) {
        this.context = context;
        this.action = action;
        this.connection = connection;
        this.clientHandle = connection.handle();
    }

    /**
     * The action associated with this listener has been successful.
     *
     * @param asyncActionToken This argument is not used
     */
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        switch (action) {
            case CONNECT:
                connect(asyncActionToken);
                break;
            case DISCONNECT:
                disconnect(asyncActionToken);
                break;
            case SUBSCRIBE:
                subscribe(asyncActionToken);
                break;
            case PUBLISH:
                publish(asyncActionToken);
                break;
        }

    }

    /**
     * A publish action has been successfully completed, update connection
     * object associated with the client this action belongs to, then notify the
     * user of success
     */
    private void publish(IMqttToken actionToken) {

        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        String[] topics = actionToken.getTopics();
        if(topics!=null&& topics.length>0) {
            c.topicPublished(topics[0],true);
        }
        String actionTaken = "published";
        c.addAction(actionTaken);
        //Notify.toast(context, actionTaken, Toast.LENGTH_SHORT);
        System.out.print("Published");

    }

    /**
     * A addNewSubscription action has been successfully completed, update the connection
     * object associated with the client this action belongs to and then notify
     * the user of success
     */
    private void subscribe(IMqttToken actionToken) {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        String[] topics = actionToken.getTopics();
        if(topics!=null&& topics.length>0) {
            c.topicSubscribed(topics[0],true);
        }
        String actionTaken = "New subscription is added";
        c.addAction(actionTaken);
        //Notify.toast(context, actionTaken, Toast.LENGTH_SHORT);
        System.out.print(actionTaken);

    }

    /**
     * A disconnection action has been successfully completed, update the
     * connection object associated with the client this action belongs to and
     * then notify the user of success.
     */
    private void disconnect(IMqttToken actionToken) {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        c.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
        String actionTaken = "disconnected";
        c.addAction(actionTaken);
        Log.i(TAG, c.handle() + " disconnected.");
    }

    /**
     * A connection action has been successfully completed, update the
     * connection object associated with the client this action belongs to and
     * then notify the user of success.
     */
    private void connect(IMqttToken actionToken) {

        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        c.changeConnectionStatus(Connection.ConnectionStatus.CONNECTED);
        c.addAction("Client Connected");
        Log.i(TAG, c.handle() + " connected.");
        try {

            ArrayList<Subscription> subscriptions = connection.getSubscriptions();
            for (Subscription sub : subscriptions) {
                Log.i(TAG, "Auto-subscribing to: " + sub.getTopic() + "@ QoS: " + sub.getQos());
                connection.getClient().subscribe(sub.getTopic(), sub.getQos());
            }
        } catch (MqttException ex){
            Log.e(TAG, "Failed to Auto-Subscribe: " + ex.getMessage());
        }

    }

    /**
     * The action associated with the object was a failure
     *
     * @param token     This argument is not used
     * @param exception The exception which indicates why the action failed
     */
    @Override
    public void onFailure(IMqttToken token, Throwable exception) {
        switch (action) {
            case CONNECT:
                connect(token,exception);
                break;
            case DISCONNECT:
                disconnect(token,exception);
                break;
            case SUBSCRIBE:
                subscribe(token,exception);
                break;
            case PUBLISH:
                publish(token,exception);
                break;
        }

    }

    /**
     * A publish action was unsuccessful, notify user and update client history
     *
     * @param exception This argument is not used
     */
    private void publish(IMqttToken actionToken,Throwable exception) {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        String[] topics = actionToken.getTopics();
        if(topics!=null&& topics.length>0) {
            c.topicPublished(topics[0],false);
        }
        String action = "Publish error:"+exception.getMessage();
        c.addAction(action);
        //Notify.toast(context, action, Toast.LENGTH_SHORT);
        System.out.print("Publish failed");

    }

    /**
     * A addNewSubscription action was unsuccessful, notify user and update client history
     *
     * @param exception This argument is not used
     */
    private void subscribe(IMqttToken actionToken,Throwable exception) {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        String[] topics = actionToken.getTopics();
        if(topics!=null&& topics.length>0) {
            c.topicSubscribed(topics[0],false);
        }
        String action = "Subscription error :"+exception.getMessage();
        c.addAction(action);
        //Notify.toast(context, action, Toast.LENGTH_SHORT);
        System.out.print(action);

    }

    /**
     * A disconnect action was unsuccessful, notify user and update client history
     *
     * @param exception This argument is not used
     */
    private void disconnect(IMqttToken actionToken,Throwable exception) {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        c.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
        c.addAction("Disconnect Failed - an error occured");

    }

    /**
     * A connect action was unsuccessful, notify the user and update client history
     *
     * @param exception This argument is not used
     */
    private void connect(IMqttToken actionToken,Throwable exception) {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        c.changeConnectionStatus(Connection.ConnectionStatus.ERROR);
        c.addAction("Client failed to connect");
        System.out.println("Client failed to connect");

    }

}