import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.*;

/**
 * Class PollsterUI provides the graphical user interface for the pollster
 * program in the Poll System.
 *
 * @author  Alan Kaminsky
 * @version 05-Nov-2016
 */
public class PollsterUI implements ViewListener
{
    private static final int GAP = 10;

    private JFrame frame;
    private JTextField questionField;
    private JButton goButton;
    private JTextField agreeCountField;
    private JTextField disagreeCountField;
    private ModelListener modelListener;
    private Timer timer;

    /**
     * Construct a new pollster UI object.
     */
    private PollsterUI()
    {
        frame = new JFrame ("Pollster");

        JPanel p1 = new JPanel();
        p1.setLayout (new BoxLayout (p1, BoxLayout.Y_AXIS));
        p1.setBorder (BorderFactory.createEmptyBorder (GAP, GAP, GAP, GAP));
        frame.add (p1);

        JPanel p2 = new JPanel();
        p2.setLayout (new BoxLayout (p2, BoxLayout.X_AXIS));
        p1.add (p2);

        questionField = new JTextField (40);
        p2.add (questionField);
        p2.add (Box.createHorizontalStrut (GAP));

        goButton = new JButton ("Go");
        p2.add (goButton);
        p1.add (Box.createVerticalStrut (GAP));

        JPanel p3 = new JPanel();
        p3.setLayout (new BoxLayout (p3, BoxLayout.X_AXIS));
        p1.add (p3);

        p3.add (new JLabel ("Agree:"));
        agreeCountField = new JTextField (5);
        agreeCountField.setEditable (false);
        Dimension d = agreeCountField.getPreferredSize();
        agreeCountField.setMinimumSize (d);
        agreeCountField.setMaximumSize (d);
        agreeCountField.setText("0"); // Initial value to 0
        p3.add (agreeCountField);
        p3.add (Box.createHorizontalStrut (2*GAP));

        p3.add (new JLabel ("Disagree:"));
        disagreeCountField = new JTextField (5);
        disagreeCountField.setEditable (false);
        d = disagreeCountField.getPreferredSize();
        disagreeCountField.setMinimumSize (d);
        disagreeCountField.setMaximumSize (d);
        disagreeCountField.setText("0"); // Initial value to 0
        p3.add (disagreeCountField);

        //send out poll question when go clicked
        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPoll(questionField.getText());
            }
        });

        // Exit server when window closed.
        frame.addWindowListener (new WindowAdapter()
        {
            public void windowClosing (WindowEvent e)
            {
                System.exit (0);
            }
        });

        frame.pack();
        frame.setVisible (true);
    }

    /**
     * An object holding a reference to a pollster UI object.
     */
    private static class PollsterUIRef
    {
        public PollsterUI ui;
    }

    /**
     * Construct a new pollster UI object.
     */
    public static PollsterUI create()
    {
        PollsterUIRef ref = new PollsterUIRef();
        onSwingThreadDo (new Runnable()
        {
            public void run()
            {
                ref.ui = new PollsterUI();
            }
        });
        return ref.ui;
    }

    /**
     * Execute the given runnable object on the Swing thread.
     */
    private static void onSwingThreadDo
    (Runnable task)
    {
        try
        {
            SwingUtilities.invokeAndWait (task);
        }
        catch (Throwable exc)
        {
            exc.printStackTrace (System.err);
            System.exit (1);
        }
    }

    /**
     * Set the model listener object for this Pollster UI.
     *
     * @param  modelListener  Model listener.
     */
    public void setListener
    (final ModelListener modelListener)
    {
        onSwingThreadDo (new Runnable()
        {
            public void run()
            {
                PollsterUI.this.modelListener = modelListener;
            }
        });
    }

    // model listener functions

    /**
     * Sends out the poll question to all clients.
     * @param question the poll question to send to teh clients
     */
    public void setPoll(String question)
    {
        if(timer != null)
        {
            timer.stop();
        }
        // Send poll state to clients every second
        timer = new Timer (1000, new ActionListener()
        {
            public void actionPerformed (ActionEvent e)
            {
                try
                {
                    if (modelListener != null) {
                        modelListener.setPoll(
                                question, System.currentTimeMillis());
                    }
                }
        catch (IOException exc)
                {
                    exc.printStackTrace (System.err);
                    System.exit (1);
                }
            }
        });
        timer.start();
    }

    // view listener functions

    /**
     * Sets the text on the agree and disagree fields based on client votes
     * @param agree number of clients which have agree selected
     * @param disagree number of clients which have disagree selected
     * @param listener the view proxy connected to the originating client
     * @param timestamp the time the vote was sent at
     */
    public void vote
    (int agree, int disagree, ModelListener listener, long timestamp)
    {
        onSwingThreadDo(new Runnable() {
            @Override
            public void run() {
                agreeCountField.setText(Integer.toString(agree));
                disagreeCountField.setText(Integer.toString(disagree));
            }
        });
    }
}