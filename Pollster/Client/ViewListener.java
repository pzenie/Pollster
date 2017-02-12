import java.io.IOException;

/**
 * Interface ViewListener specifies the interface for objects that
 * receive notifications from the client responder program
 *
 * @author  Paul Zenie
 */
public interface ViewListener
{

    /**
     * updates votes on server when client vote changes
     * @param agree 1 if agree vote else 0
     * @param disagree 1 if disagree vote else 0
     * @param listener Used in the server program to keep track of clients
     * @param  timestamp the time the message was sent at
     * @exception IOException
     *      Thrown if an I/O error occurred.
     */
    void vote(int agree, int disagree, ModelListener listener, long timestamp)
            throws IOException;

}