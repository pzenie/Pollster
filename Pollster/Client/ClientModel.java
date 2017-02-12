import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Client side model for poll system
 * receives notifications from view and server
 *
 * @author  Paul Zenie
 */
public class ClientModel implements ViewListener, ModelListener
{

// Hidden data members.

	private ScheduledExecutorService pool;
	private ViewListener viewListener;
	private ModelListener modelListener;
	private ScheduledFuture<?> timeout;
	private long timeoutNum;
    private long timestamp;

// Exported constructors.

	/**
	 * Construct new client model.
	 */
	public ClientModel()
		{
		pool = Executors.newScheduledThreadPool (1);
            timestamp = 0;
		}

// Exported operations.

	/**
	 * Sets the view listener.
	 *
	 * @param  listener  View listener.
	 */
	public synchronized void setListener(ViewListener listener)
	{
		viewListener = listener;
	}

	/**
	 * sets the model listener
	 * @param listener the model listener
	 */
	public synchronized  void setModelListener(ModelListener listener)
	{
		this.modelListener = listener;
	}

	// view listener functions


	/**
	 * Sends vote to server
	 * @param agree 1 if agree vote else 0
	 * @param disagree 1 if disagree vote else 0
     * @param listener used server side to keep track of clients
     * @param timestamp time that the vote message was sent at
	 */
	public void vote
    (int agree, int disagree, ModelListener listener, long timestamp){
		pool.execute(new Runnable() {
			@Override
			public void run() {
				try
				{
					viewListener.vote(agree, disagree, null, timestamp);
				}
				catch (IOException e)
				{
					// Shouldn't happen
				}
			}
		});
	}


// Exported operations from interface ModelListener.

	/**
	 * Sends poll question to gui
     * starts timer to make sure server still connected
	 * @param question the poll question to send
     * @param timestamp time the message was sent at
	 */
	public synchronized void setPoll(String question, long timestamp){
		if(modelListener != null) {
			try {
				if (timeout != null) {
					timeout.cancel(false);
				}
				++ timeoutNum;
				timeout = pool.schedule (
						new Runnable()
						{
					 		private long num = timeoutNum;
					 		public void run()
					 		{
                                //if no message in 3 seconds assume server down
						 		disconnect(num);
					 		}
				 		}, 3, TimeUnit.SECONDS);
                if(timestamp > this.timestamp)
                {
                    this.timestamp = timestamp;
                    modelListener.setPoll(question, timestamp);
                }
				modelListener.setPoll(question, timestamp);
			}
			catch (IOException e) {
				// Shouldn't happen
			}
		}
	}

    /**
     * Sets the client to starting state
     * @param num the timeout num used to check if same as current timeout num
     */
	private synchronized void disconnect (long num)
	{
		// If the timeout was already canceled, do nothing.
		if (num != timeoutNum) return;
		setPoll(null, 0);
	}
}
