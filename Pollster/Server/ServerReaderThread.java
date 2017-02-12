import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;

/**
 * Class ServerReaderThread provides a thread that receives messages from
 * clients in the poll system.
 *
 * @author  Alan Kaminsky
 * @modified Paul Zenie
 * @version 15-Mar-2016
 */
public class ServerReaderThread extends Thread
{

// Hidden data members.

    private DatagramSocket mailbox;
    private ViewListener viewListener;
    private HashMap<SocketAddress, ViewProxy> map;

// Exported constructors.

    /**
     * Construct a new server reader thread.
     *
     * @param  mailbox   Mailbox.
     * @param  listener  View listener.
     */
    public ServerReaderThread(DatagramSocket mailbox, ViewListener listener)
    {
        this.mailbox = mailbox;
        this.viewListener = listener;
        map = new HashMap<>();
    }

    /**
     * Run this server reader thread.
     */
    public void run()
    {
        byte[] buf = new byte [128];
        DatagramPacket packet = new DatagramPacket (buf, buf.length);
        ByteArrayInputStream bais;
        DataInputStream in;
        byte b;
        try
        {
            for (;;)
            {
                mailbox.receive (packet);
                bais = new ByteArrayInputStream (buf, 0, packet.getLength());
                in = new DataInputStream (bais);
                b = in.readByte();
                int agree;
                int disagree;
                long timestamp;
                switch (b)
                {
                    case 'V':
                        agree = in.read();
                        disagree = in.read();
                        timestamp = in.readLong();
                        ViewProxy model = map.get(packet.getSocketAddress());
                        if(model == null)
                        {
                            model = new ViewProxy
                                    (mailbox, packet.getSocketAddress());
                            map.put(packet.getSocketAddress(), model);
                        }
                        viewListener.vote(agree, disagree, model, timestamp);
                        break;
                    default:
                        System.err.println ("Bad message");
                        break;
                }
            }
        }
        catch (IOException exc)
        {
        }
        finally
        {
            mailbox.close();
        }
    }

}

