package info.xonix.zlo.search.daemon;

import info.xonix.zlo.search.logic.exceptions.ExceptionsLogger;
import info.xonix.zlo.search.spring.AppSpringContext;
import info.xonix.zlo.search.utils.TimeUtils;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Author: Vovan
 * Date: 17.11.2007
 * Time: 20:00:32
 */
public abstract class Daemon {
    private static final Logger log = Logger.getLogger(Daemon.class);

    protected ExceptionsLogger exceptionsLogger = AppSpringContext.get(ExceptionsLogger.class);

    private Process process;

    private int doPerTime;
    private long sleepPeriod;
    private long retryPeriod;

    private Exception lastException;
    private Date lastExceptionTime;

    private DaemonState daemonState;

    private String forumId;

    protected Daemon(String forumId,
                     int doPerTime, long sleepPeriod, long retryPeriod) {
        this.forumId = forumId;

        this.doPerTime = doPerTime;
        this.sleepPeriod = sleepPeriod;
        this.retryPeriod = retryPeriod;
    }

    protected abstract Logger getLogger();

    protected void setExiting() {
        daemonState = DaemonState.EXITING;
    }

    protected boolean isExiting() {
        return daemonState == DaemonState.EXITING;
    }

    private void saveLastException(Exception e) {
        lastException = e;
        lastExceptionTime = new Date();
    }

    public Exception getLastException() {
        return lastException;
    }

    public Date getLastExceptionTime() {
        return lastExceptionTime;
    }

    public DaemonState getDaemonState() {
        return daemonState;
    }

    protected abstract Process createProcess();

    synchronized Process getProcess() {
        if (process == null) {
            process = createProcess();
        }
        return process;
    }

    public String describe() {
        return getClass().getSimpleName() + "-" + forumId;
    }

    public void reset() {
        try {
            process.cleanUp();
        } catch (Exception e) {
            log.error("Error cleanUp", e);
        }

        process = null;
    }

    protected abstract class Process extends Thread {

        public Process() {
            super(describe());
        }

        public void run() {
            while (true) {
                try {
                    doOneIteration();
                } catch (InterruptedException e) {
                    getLogger().info(forumId + " - Process interrupted.");
                }

                if (isExiting()) {
                    getLogger().info(forumId + " - Performing cleanup...");
                    cleanUp();

                    break;
                }
            }
        }

        protected abstract int getFromIndex() throws Exception;

        protected abstract int getEndIndex() throws Exception;

        protected abstract void perform(int from, int to) throws Exception;

        protected abstract boolean processException(Exception ex);

        private int indexFrom = -1;
        private int end = -1;

        private void doOneIteration() throws InterruptedException {
            final Logger logger = getLogger();
            try {
                if (indexFrom == -1) {
                    indexFrom = getFromIndex() + 1;
                }

                if (isExiting()) {
                    return;
                }

                if (end == -1) {
                    end = getEndIndex();
                }

                if (isExiting()) {
                    return;
                }

                int indexTo = indexFrom + doPerTime - 1;

                if (indexTo > end) {
                    indexTo = end;
                }

                if (indexFrom <= indexTo) {
                    doPerform(indexFrom, indexTo);
                    indexFrom = indexTo + 1;
                }

                if (isExiting()) {
                    return;
                }

                while (indexFrom > end) {
                    logger.info(forumId + " - Sleeping " + TimeUtils.toMinutesSeconds(sleepPeriod) + "...");
                    doSleep(sleepPeriod);
                    end = getEndIndex();
                }
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                saveLastException(e);

                if (!processException(e)) {
                    logger.error("(" + forumId + ") Unknown exception", e);
                }

                logger.info(forumId + " - Retry in " + TimeUtils.toMinutesSeconds(retryPeriod));
                doSleep(retryPeriod);
            }
        }

        protected abstract void cleanUp();

        protected void doSleep(long millis) throws InterruptedException {
            DaemonState prevState = daemonState;

            setStateIfNotExiting(DaemonState.SLEEPING);

            sleep(millis);

            setStateIfNotExiting(prevState);
        }

        private void doPerform(int from, int to) throws Exception {
            setStateIfNotExiting(DaemonState.PERFORMING);

            perform(from, to);
        }

        private void setStateIfNotExiting(DaemonState newDaemonState) {
            if (!isExiting()) {
                daemonState = newDaemonState;
            }
        }
    }

    public String getForumId() {
        return forumId;
    }

    public boolean finishedAbnormally() {
        return getProcess().getState() == Thread.State.TERMINATED
                && !isExiting();
    }


    protected void doOnStart() {
    }

    void start() {
        doOnStart();

        Process t = getProcess();
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY); // so daemons not slowing search
        t.start();
    }
}
