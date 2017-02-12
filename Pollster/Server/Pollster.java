import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * The server main program for the Poll System application.
 *
 * Usage: java DnsClient serverhost serverport clienthost clientport
 *
 * @author  Paul Zenie
 */
public class Pollster {

    /**
     * parses arguments and then creates view/model/proxy and sets listeners
     * @param args command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        //Parse command line arguments
        if (args.length != 2) usage();
        String pollsterhost = args[0];
        int pollsterport = 0;
        try {
            pollsterport = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            System.err.println("ERROR: Port must be an integer.");
            System.exit(1);
        }
        //Setup mailbox
        DatagramSocket mailbox = null;
        try {mailbox = new DatagramSocket(new InetSocketAddress
                (pollsterhost, pollsterport));
        }
        catch (SocketException e)
        {
            System.err.println("ERROR: Could not start socket," +
                    " try a different address/port.");
            System.exit(1);
        }
        //create classes and set listeners
        PollsterUI view = PollsterUI.create();
        PollsterModel model = new PollsterModel();
        ServerReaderThread reader = new ServerReaderThread(mailbox, model);
        view.setListener(model);
        model.setListener(view);
        reader.start();
    }

    /**
     * Print a usage message and exit.
     */
    private static void usage()
    {
        System.err.println ("Usage: java Pollster <pollsterhost> <pollsterport>");
        System.exit (1);
    }
}
