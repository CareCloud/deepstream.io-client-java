package io.deepstream;

import com.google.gson.JsonElement;
import io.deepstream.constants.ConnectionState;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.UUID;

/**
 * deepstream.io java client
 */
public class DeepstreamClient extends DeepstreamClientAbstract {

    private String uuid;
    private final Connection connection;
    private final Properties config;

    public final RecordHandler record;
    public final EventHandler event;
    public final RpcHandler rpc;

    /**
     * deepstream.io javascript client, defaults to using default properties
     * {@link DeepstreamClient#DeepstreamClient(String, Properties)}
     *
     * @throws URISyntaxException Thrown if the url in incorrect
     * @throws IOException Thrown if the default properties file is not found
     */
    public DeepstreamClient( final String url ) throws URISyntaxException, IOException {
        this( url, new Properties() );
    }

    /**
     * deepstream.io java client
     * @param url URL to connect to. The protocol can be omited, e.g. <host>:<port>
     * @param options A map of options that extend the ones specified in DefaultConfig.properties
     * @throws URISyntaxException Thrown if the url in incorrect
     * @throws IOException Thrown if the default properties file is not found
     */
    public DeepstreamClient( final String url, Properties options ) throws URISyntaxException, IOException {
        this.config = getConfig( options );
        this.connection = new Connection( url, this.config, this );
        this.event = new EventHandler( config, this.connection, this );
        this.rpc = new RpcHandler( config, this.connection, this );
        this.record = new RecordHandler( config, this.connection, this );
    }

    /**
     * Adds a {@link DeepstreamRuntimeErrorHandler} that will catch all RuntimeErrors such as AckTimeouts and allow
     * the user to gracefully handle them.
     *
     * @param deepstreamRuntimeErrorHandler The listener to set
     */
    public void setRuntimeErrorHandler( DeepstreamRuntimeErrorHandler deepstreamRuntimeErrorHandler )  {
        super.setRuntimeErrorHandler( deepstreamRuntimeErrorHandler );
    }

    /**
     * @see DeepstreamClient#login(JsonElement, LoginCallback)
     *
     * Does not call the login callback, used mainly for anonymous logins where your guaranteed login
     *
     * @param authParams JSON.serializable authentication data
     * @throws DeepstreamLoginException Thrown if the user can no longer login due to multiple attempts or other
     * fatal reasons
     * @return The deepstream client
     */
    public DeepstreamClient login( JsonElement authParams ) throws DeepstreamLoginException {
        this.connection.authenticate( authParams, null );
        return this;
    }

    /**
     * Send authentication parameters to the client to fully open
     * the connection.
     *
     * Please note: Authentication parameters are send over an already established
     * connection, rather than appended to the server URL. This means the parameters
     * will be encrypted when used with a WSS / HTTPS connection. If the deepstream server
     * on the other side has message logging enabled it will however be written to the logs in
     * plain text. If additional security is a requirement it might therefor make sense to hash
     * the password on the client.
     *
     * If the connection is not yet established the authentication parameter will be
     * stored and send once it becomes available
     *
     * authParams can be any JSON serializable data structure and its up for the
     * permission handler on the server to make sense of them, although something
     * like { username: 'someName', password: 'somePass' } will probably make the most sense.
     *
     * login can be called multiple times until either the connection is authenticated or
     * forcefully closed by the server since its maxAuthAttempts threshold has been exceeded
     *
     * @param authParams JSON.serializable authentication data
     * @param loginCallback Thrown if the user can no longer login due to multiple attempts or other
     * fatal reasons
     * @throws DeepstreamLoginException Thrown if the user can no longer login due to multiple attempts or other
     * fatal reasons
     * @return The deepstream client
     */
    public DeepstreamClient login( JsonElement authParams, LoginCallback loginCallback ) throws DeepstreamLoginException {
        this.connection.authenticate( authParams, loginCallback );
        return this;
    }

    /**
     * Closes the connection to the server.
     * @return The deepstream client
     */
    public DeepstreamClient close() {
        this.connection.close();
        return this;
    }

    /**
     * Add a listener that can be notified via {@link ConnectionStateListener#connectionStateChanged(ConnectionState)}
     * whenever the {@link ConnectionState} changes
     * @param connectionStateListener The listener to add
     * @return The deepstream client
     */
    public DeepstreamClient addConnectionChangeListener( ConnectionStateListener connectionStateListener) {
        this.connection.addConnectionChangeListener(connectionStateListener);
        return this;
    }

    /**
     * Removes a {@link ConnectionStateListener} added via {@link DeepstreamClient#addConnectionChangeListener(ConnectionStateListener)}
     * @param connectionStateListener The listener to remove
     * @return The deepstream client
     */
    public DeepstreamClient removeConnectionChangeListener( ConnectionStateListener connectionStateListener) {
        this.connection.removeConnectionChangeListener(connectionStateListener);
        return this;
    }

    /**
     * Returns the current state of the connection.
     * @return The connection state
     */
    public ConnectionState getConnectionState() {
        return this.connection.getConnectionState();
    }

    /**
     * Returns a random string. The first block of characters
     * is a timestamp, in order to allow databases to optimize for semi-
     * sequentuel numberings
     * @return A unique id
     */
    public String getUid() {
        if( uuid == null ) {
            uuid = UUID.randomUUID().toString();
            return uuid;
        }
        return uuid;
    }

    /**
     * TODO: Load from a default map instead of config file
     *
     * Creates a new options map by extending default
     * options with the passed in options
     * @param properties The properties to merge into the default
     * @throws IOException Thrown if properties file not found
     * @return Loaded properties
     */
    private Properties getConfig( Properties properties ) throws IOException {
        Properties config = new Properties();
        FileInputStream in = new FileInputStream( "DefaultConfig.properties" );
        config.load( in );
        config.putAll( properties );
        in.close();
        return config;
    }
}
