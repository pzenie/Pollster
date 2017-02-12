import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Class ResponderUI provides the graphical user interface for the responder
 * program in the Poll System.
 *
 * @author  Alan Kaminsky
 * @version 05-Nov-2016
 */
public class ResponderUI implements ModelListener
{
	private static final int GAP = 10;

	private JFrame frame;
	private JTextField questionField;
	private ButtonGroup buttons;
	private JToggleButton agreeButton;
	private JToggleButton disagreeButton;
	private ViewListener viewListener;

	/**
	 * Construct a new responder UI object.
	 */
	private ResponderUI()
	{
		frame = new JFrame ("Responder");

		JPanel p1 = new JPanel();
		p1.setLayout (new BoxLayout (p1, BoxLayout.Y_AXIS));
		p1.setBorder (BorderFactory.createEmptyBorder (GAP, GAP, GAP, GAP));
		frame.add (p1);

		questionField = new JTextField (40);
		questionField.setEditable (false);
		p1.add (questionField);
		p1.add (Box.createVerticalStrut (GAP));

		JPanel p2 = new JPanel();
		p2.setLayout (new BoxLayout (p2, BoxLayout.X_AXIS));
		p1.add (p2);

		buttons = new ButtonGroup();
		agreeButton = new JToggleButton ("Agree", false);
		agreeButton.setEnabled(false); // buttons start off as disabled
		buttons.add (agreeButton);
		p2.add (agreeButton);

		disagreeButton = new JToggleButton ("Disagree", false);
		disagreeButton.setEnabled(false); // start button off as disabled
		buttons.add (disagreeButton);
		p2.add (disagreeButton);

        // Send current vote selection of responder to pollster every second.
        Timer timer = new Timer (1000, new ActionListener()
        {
            public void actionPerformed (ActionEvent e)
            {
                vote();
            }
        });
        timer.start();

        // Exit program when window closed
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
	 * Sets the question text.
	 * enables buttons and clears selection
	 * @param question poll question
     * @param timestamp what time the poll was sent out
	 */
	public void setPoll(String question, long timestamp)
	{
		onSwingThreadDo(new Runnable() {
			@Override
			public void run() {
                if(question != null) {
                    // If it is a new question enable buttons and reset vote
                    if(!question.equals(questionField.getText())) {
                        Enumeration<AbstractButton> enumeration
                                = buttons.getElements();
                        while (enumeration.hasMoreElements()) {
                            enumeration.nextElement().setEnabled(true);
                        }
                        buttons.clearSelection();
                    }
                    questionField.setText(question);
                }
                //if there is no question disable the buttons and clear votes
                else
                {
                    questionField.setText("");
                    Enumeration<AbstractButton> enumeration
                            = buttons.getElements();
                    while (enumeration.hasMoreElements()) {
                        enumeration.nextElement().setEnabled(false);
                    }
                    buttons.clearSelection();
                }
			}
		});
	}

	// viewlistener methods

	/**
	 * Sends vote to server
	 */
	public void vote()
	{
		try
		{
			if (viewListener != null)
				viewListener.vote
                        (Boolean.compare(agreeButton.isSelected(), false),
                        Boolean.compare(disagreeButton.isSelected(), false),
                        null, System.currentTimeMillis());
		}
		catch (IOException exc)
		{
			exc.printStackTrace (System.err);
			System.exit (1);
		}
	}

	// helpers

	/**
	 * Set the view listener.
	 *
	 * @param  listener  View listener.
	 */
	public void setListener
	(final ViewListener listener)
	{
		onSwingThreadDo (new Runnable()
		{
			public void run()
			{
				viewListener = listener;
			}
		});
	}

	/**
	 * An object holding a reference to a responder UI object.
	 */
	private static class ResponderUIRef
	{
		public ResponderUI ui;
	}

	/**
	 * Construct a new responder UI object.
	 */
	public static ResponderUI create()
		{
		ResponderUIRef ref = new ResponderUIRef();
		onSwingThreadDo (new Runnable()
			{
			public void run()
				{
				ref.ui = new ResponderUI();
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
}
