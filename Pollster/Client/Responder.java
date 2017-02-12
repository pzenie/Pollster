import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Client main program for the poll system.
 *
 * Usage: java Responder pollsterhost pollsterport responderhost responderport
 *
 * @author  Paul Zenie
 */
public class Responder
{

    /**
     *  Parses command line arguments
     *  initializes classes and sets listeners
     * @param args command line arguments
     * @throws Exception
     */
    public static void main
    (String[] args)
            throws Exception
    {
        // Parse command line arguments.
        if (args.length != 4) usage();
        String serverhost = args[0];
        int serverport = 0;
        try {
            serverport = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            System.err.println("ERROR: Port must be an integer.");
            System.exit(1);
        }
        String clienthost = args[2];
        int clientport = 0;
        try {
            clientport = Integer.parseInt(args[3]);
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error: Port must be an integer.");
            System.exit(1);
        }

        // Set up server mailbox address.
        InetSocketAddress serverAddress = null;
        try {
            serverAddress =
                    new InetSocketAddress(serverhost, serverport);
        }
        catch (IllegalArgumentException e)
        {
            System.err.println("ERROR: Port was out of range of acceptable values.");
            System.exit(1);
        }
        // Set up client mailbox.
        DatagramSocket mailbox = null;
        try {
            mailbox = new DatagramSocket
                    (new InetSocketAddress(clienthost, clientport));
        }
        catch (SocketException e)
        {
            System.err.println("ERROR: Could not start socket," +
                    " try a different address/port.");
            System.exit(1);
        }
        // create classes and set listeners
        ResponderUI view = ResponderUI.create();
        ClientModel model = new ClientModel();
        ModelProxy proxy = new ModelProxy(mailbox, serverAddress);
        model.setListener(proxy);
        model.setModelListener(view);
        proxy.setListener(model);
        view.setListener(model);
    }

    /**
     * Print a usage message and exit.
     */
    private static void usage()
    {
        System.err.println ("Usage: java Responder <pollsterhost> " +
                "<pollsterport> <responderhost> <responderport>");
        System.exit (1);
    }

}
