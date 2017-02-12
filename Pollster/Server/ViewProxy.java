import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * Provides the network proxy for the view object.
 * The view proxy resides in the server and sends messages to the
 * client.
 *
 * @author  Paul Zenie
 */
public class ViewProxy implements ModelListener
{

// Hidden data members.

	private DatagramSocket mailbox;
	private SocketAddress clientAddress;

// Exported constructors.

	/**
	 * Construct a new view proxy.
	 *
	 * @param  mailbox        Mailbox.
	 * @param  clientAddress  Client address.
	 */
	public ViewProxy
		(DatagramSocket mailbox,
		 SocketAddress clientAddress)
	{
		this.mailbox = mailbox;
		this.clientAddress = clientAddress;
	}

	/**
	 * Send a poll question to clients
	 *
	 * @param  question Poll question to send.
	 * @param timestamp the time the poll was sent
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	public void setPoll(String question, long timestamp) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream (baos);
		out.writeByte ('P');
		out.writeUTF (question);
        out.writeLong(timestamp);
		out.flush();
		byte[] buf = baos.toByteArray();
		DatagramPacket packet =
                new DatagramPacket (buf, buf.length, clientAddress);
		mailbox.send (packet);
	}
}
