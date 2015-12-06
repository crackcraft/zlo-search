package info.xonix.utils.daemon;

import info.xonix.utils.TimeUtils;
import org.apache.log4j.Logger;

/**
 * User: gubarkov
 * Date: 28.04.12
 * Time: 19:01
 */
public abstract class ForumIteratingDaemon extends DaemonBase {
    private static final Logger log = Logger.getLogger(ForumIteratingDaemon.class);

    private int doPerTime;
    private long sleepPeriod;
    private long retryPeriod;
    private Runnable updateStateRunnable;

    protected ForumIteratingDaemon(String forumId, int doPerTime, long sleepPeriod, long retryPeriod) {
        super(forumId);

        this.doPerTime = doPerTime;
        this.sleepPeriod = sleepPeriod;
        this.retryPeriod = retryPeriod;
    }

    public String getForumId() {
        return getId();
    }

    protected abstract int getFromIndex() throws Exception;

    protected abstract int getEndIndex() throws Exception;

    protected abstract void perform(int from, int to) throws Exception;

    @Override
    public void perform() {
        while (true) {
            try {
                doOneIteration();
            } catch (InterruptedException e) {
                log.info(getForumId() + " - Process interrupted.");
            }

            if (isExiting()) {
                log.info(getForumId() + " - Performing cleanup...");
                cleanUp();

                break;
            }
        }
    }

    private int indexFrom = -1;
    private int end = -1;

    private void doOneIteration() throws InterruptedException {
        try {
            if (updateStateRunnable != null) {
                try {
                    indexFrom = -1;
                    end = -1;
                    updateStateRunnable.run();
                } finally {
                    updateStateRunnable = null;
                }
            }

            if (indexFrom == -1) {
                indexFrom = getFromIndex() + 1;
            }

            stopIfExiting();

            if (end == -1) {
                end = getEndIndex();
            }

            stopIfExiting();

            int indexTo = indexFrom + doPerTime - 1;

            if (indexTo > end) {
                indexTo = end;
            }

            if (indexFrom <= indexTo) {
                doPerform(indexFrom, indexTo);
                indexFrom = indexTo + 1;
            }

            stopIfExiting();

            while (indexFrom > end) {
                log.info(getForumId() + " - Sleeping " + TimeUtils.toMinutesSeconds(sleepPeriod) + "...");
                doSleep(sleepPeriod);
                end = getEndIndex();
            }
        } catch (InterruptedException e) {
            throw e;// exiting
        } catch (Exception e) {
            saveLastException(e);

            if (!processException(e)) {
                log.error("(" + getForumId() + ") Unknown exception", e);
            }

            stopIfExiting();

            log.info(getForumId() + " - Retry in " + TimeUtils.toMinutesSeconds(retryPeriod));
            doSleep(retryPeriod);
        }
    }

    private void doPerform(int from, int to) throws Exception {
        setStateIfNotExiting(DaemonState.PERFORMING);

        perform(from, to);
    }


    /**
     * This runnable will be called 1 time at the start of next daemon iteration.
     */
    public void updateState(Runnable runnable) {
        updateStateRunnable = runnable;
        interruptIfSleeping();
    }
}
