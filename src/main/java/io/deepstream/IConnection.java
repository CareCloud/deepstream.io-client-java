package io.deepstream;

import io.deepstream.constants.Actions;
import io.deepstream.constants.Topic;

/**
 * TODO: Remove somehow?
 */
interface IConnection {
    void sendMsg( Topic topic, Actions action, String[] data );
    void send( String message );
}
