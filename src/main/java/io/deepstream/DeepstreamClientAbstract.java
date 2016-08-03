package io.deepstream;

import com.google.gson.JsonElement;
import io.deepstream.constants.ConnectionState;
import io.deepstream.constants.Event;
import io.deepstream.constants.Topic;

abstract class DeepstreamClientAbstract {
    private UtilAckTimeoutRegistry utilAckTimeoutRegistry;
    private DeepstreamRuntimeErrorHandler deepstreamRuntimeErrorHandler;

    abstract DeepstreamClientAbstract addConnectionChangeListener(ConnectionStateListener connectionStateListener);
    abstract DeepstreamClientAbstract removeConnectionChangeListener(ConnectionStateListener connectionStateListener);
    abstract ConnectionState getConnectionState();
    abstract DeepstreamClientAbstract login(JsonElement data )throws DeepstreamLoginException ;
    abstract DeepstreamClientAbstract login(JsonElement data, LoginCallback loginCallback ) throws DeepstreamLoginException ;
    abstract DeepstreamClientAbstract close();
    abstract String getUid();

    UtilAckTimeoutRegistry getAckTimeoutRegistry() {
        if(  utilAckTimeoutRegistry == null ) {
            utilAckTimeoutRegistry = new UtilAckTimeoutRegistry( this );
        }
        return utilAckTimeoutRegistry;
    }

    /**
     * Adds a {@link DeepstreamRuntimeErrorHandler} that will catch all RuntimeErrors such as AckTimeouts and allow
     * the user to gracefully handle them.
     *
     * @param deepstreamRuntimeErrorHandler The listener to set
     */
    public void setRuntimeErrorHandler( DeepstreamRuntimeErrorHandler deepstreamRuntimeErrorHandler )  {
        this.deepstreamRuntimeErrorHandler = deepstreamRuntimeErrorHandler;
    }

    void onError(Topic topic, Event event, String msg) throws DeepstreamException {
        /*
         * Help to diagnose the problem quicker by checking for
         * some mon problems
         */
        if( event.equals( Event.ACK_TIMEOUT ) || event.equals( Event.RESPONSE_TIMEOUT ) ) {
            if( getConnectionState().equals( ConnectionState.AWAITING_AUTHENTICATION ) ) {
                String errMsg = "Your message timed out because you\'re not authenticated. Have you called login()?";
                onError( Topic.ERROR, Event.NOT_AUTHENTICATED, errMsg );
                return;
            }
        }

        if( deepstreamRuntimeErrorHandler != null ) {
            deepstreamRuntimeErrorHandler.onException( topic, event, msg );
        } else {
            System.out.println( "Throwing a client exception: " + topic + " " +  event + " " +  msg );
            throw new DeepstreamException( topic, event, msg );
        }

    }
}
