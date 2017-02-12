import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The model object for the Pollster application.
 * The model object resides in the server.
 * The model object is responsible for keeping track of clients and their votes
 * The model object communicates between the view and client
 *
 * @author  Paul Zenie
 */
public class PollsterModel implements ViewListener, ModelListener
{

    private ViewListener viewListener = null;
    private HashMap<ModelListener, ResponderStateModel> responderMap;
    private ScheduledExecutorService pool;

// Exported constructors.

    /**
     * Construct a new Pollster model.
     */
    public PollsterModel()
    {
        responderMap = new HashMap<>();
        pool = Executors.newScheduledThreadPool (1);
    }

// Exported operations.

    /**
     * Set the view listener.
     *
     * @param  listener  View listener.
     */
    public synchronized void setListener(ViewListener listener)
    {
        viewListener = listener;
    }

    // model listener functions

    /**
     * Send the poll question
     *
     * @param  question poll question to send
     * @param timestamp the time the message was sent
     * @exception  IOException
     *     Thrown if an I/O error occurred.
     */
    public synchronized void setPoll(String question, long timestamp)
    {
        if(responderMap != null && responderMap.keySet().size() > 0) {
            try {
                // send question to all clients
                for (ModelListener listener : responderMap.keySet()) {
                    listener.setPoll(question, timestamp);
                }
            } catch (IOException exc) {
                // Shouldn't happen
            }
        }
    }

    // View listener functions

    /**
     * Deciphers the vote, adds client model to list if not already present
     * then calls the vote function of the clients ResponderStateModel
     * @param agree 1 if new vote was agree else 0
     * @param disagree 1 if new vote was disagree else 0
     * @param listener the client which sent the vote
     * @param timestamp the time the vote was sent
     */
    public synchronized void vote
    (int agree, int disagree, ModelListener listener, long timestamp)
    {
        if(viewListener != null && listener != null) {
            // check whether its an agree or disagree
            String vote;
            if (agree == 1) {
                vote = "agree";
            } else if (disagree == 1) {
                vote = "disagree";
            } else {
                vote = "";
            }
            ResponderStateModel responder = responderMap.get(listener);
            if(responder == null)
            {
                responder = new ResponderStateModel(vote, timestamp);
                responderMap.put(listener, responder);
            }
            responder.vote(vote, timestamp);
        }
    }

    /**
     * Counts all the votes from the responder state models
     * sends the counts to the view to update the displayed values
     */
    public synchronized void CountVote()
    {
        int agree = 0;
        int disagree = 0;
        for (ResponderStateModel responder: responderMap.values()) {
            if(Objects.equals(responder.getVote(), "agree"))
            {
                agree++;
            }
            else if(Objects.equals(responder.getVote(), "disagree"))
            {
                disagree++;
            }
        }
        try {
            viewListener.vote(agree, disagree, null, 0);
        }
        catch (IOException exc)
        {
            // Shouldn't happen
        }
    }

    // Hidden helper classes.

    /**
     * Class ResponderStateModel encapsulates the state of
     * one of the responder clients.
     */
    private class ResponderStateModel {
        private String vote;
        private long timestamp;
        private ScheduledFuture<?> timeout;
        private long timeoutNum;

        /**
         * Construct a new responder state model object.
         * @param vote the current vote of the responder
         * @param timestamp the last time a vote was sent by the responder
         */
        public ResponderStateModel(String vote, long timestamp) {
            this.vote = vote;
            this.timestamp = timestamp;
        }

        /**
         * @return the current vote of the responder
         */
        public String getVote()
        {
            return vote;
        }

        /**
         * starts the timer for the responder
         * if after three seconds a vote hasn't been sent by the responder
         * the server removes the vote of the responder
         * @param vote the responders new vote
         * @param timestamp the time the responder sent the vote
         */
        public synchronized void vote(String vote, long timestamp) {
            // Cancel previous timeout, start a new 3-second timeout.
            if (timeout != null) timeout.cancel(false);
            ++timeoutNum;
            timeout = pool.schedule(
                new Runnable() {
                    private long num = timeoutNum;

                    public void run() {
                        disconnect(num);
                    }
                }, 3, TimeUnit.SECONDS);

            // Detect whether the vote changed
            // Ignores messages with out-of-date time stamps.
            if (timestamp > this.timestamp) {
                this.timestamp = timestamp;
                if (!Objects.equals(vote, this.vote)) {
                    this.vote = vote;
                    CountVote();
                }
            }
        }

        /**
         * Removes this responders vote and re tallies the votes
         * @param num used to check if this is the current timeout
         */
        private synchronized void disconnect (long num)
        {
            // If the timeout was already canceled, do nothing.
            if (num != timeoutNum) return;
            vote = "";
            CountVote();
        }
    }
}
