import java.io.IOException;

/**
 * Interface ModelListener specifies the interface for objects that receive
 * notifications from the server pollster program.
 *
 * @author  Paul Zenie
 */
public interface ModelListener
{

    /**
     * Sends the poll to the clients
     *
     * @param question the poll question to send
     * @param timestamp the time the message was sent at
     * @exception  IOException
     *     Thrown if an I/O error occurred.
     */
    void setPoll(String question, long timestamp) throws IOException;

}