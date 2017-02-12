import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Network proxy for the model object in the poll system
 * The model proxy resides in the client and communicates with the server.
 *
 * @author  Alan Kaminsky
 * @Modified Paul Zenie
 * @version 15-Mar-2016
 */
public class ModelProxy implements ViewListener
{

// Hidden data members.

	private DatagramSocket mailbox;
	private InetSocketAddress serverAddress;
	private ModelListener modelListener;

// Exported constructors.

	/**
	 * Construct a new model proxy.
	 *
	 * @param  mailbox        Mailbox.
	 * @param  serverAddress  Server address.
	 */
	public ModelProxy(DatagramSocket mailbox, InetSocketAddress serverAddress)
	{
		this.mailbox = mailbox;
		this.serverAddress = serverAddress;
	}

// Exported operations.

	/**
	 * Set the model listener object for this model proxy.
	 *
	 * @param  listener  Model listener.
	 */
	public void setListener(ModelListener listener)
	{
		modelListener = listener;
		new ReaderThread() .start();
	}


    /**
     * Sends the vote to the server
     * @param agree 1 if agree vote else 0
     * @param disagree 1 if disagree vote else 0
     * @param listener used server side to keep track of clients
     * @param timestamp the time the message was sent at
     * @throws IOException
     */
    public void vote
    (int agree, int disagree, ModelListener listener, long timestamp)
            throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream (baos);
        out.writeByte ('V');
        out.write(agree);
        out.write(disagree);
        out.writeLong(timestamp);
        out.flush();
        byte[] buf = baos.toByteArray();
        DatagramPacket packet =
                new DatagramPacket (buf, buf.length, serverAddress);
        mailbox.send (packet);
    }


// Hidden helper classes.

	/**
	 * Class ReaderThread receives messages from the network, decodes them, and
	 * invokes the proper methods to process them.
     * @Author Alan Kaminsky
     * @Modified Paul Zenie
	 */
	private class ReaderThread extends Thread
    {
        /**
         * Reads and process' messages from the server
         */
		public void run()
        {
			byte[] buf = new byte [128];
			DatagramPacket packet = new DatagramPacket (buf, buf.length);
			ByteArrayInputStream bais;
			DataInputStream in;
			byte b;
			String question;
            long timestamp;
			try
            {
				for (;;)
                {
					mailbox.receive (packet);
					bais = new ByteArrayInputStream
						(buf, 0, packet.getLength());
					in = new DataInputStream (bais);
					b = in.readByte();
					switch (b)
                    {
						case 'P':
							question = in.readUTF();
                            timestamp = in.readLong();
							if(question != null && !question.isEmpty()) {
								modelListener.setPoll(question, timestamp);
							}
							break;
						default:
							System.err.println ("Bad message");
							break;
                    }
                }
            }
			catch (IOException exc)
            {
                //shouldn't happen
            }
			finally
            {
				mailbox.close();
            }
        }
    }
}
