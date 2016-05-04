package akka.actors;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.message.bus.JobInfoUpdatedMessage;
import com.github.ddth.djs.message.bus.TickMessage;
import com.github.ddth.djs.utils.CronFormat;

import akka.utils.AkkaConstants;
import modules.registry.IRegistry;
import play.Logger;

/**
 * Actor on [worker] node(s) that triggers a job's task.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class WorkerJobActor extends BaseDjsActor {

    public final static String NAME = WorkerJobActor.class.getSimpleName();

    /**
     * Calculates an "instance" actor name according to the associated
     * {@link JobInfoBo} instance.
     * 
     * @param jobInfo
     * @return
     */
    public static String calcInstanceName(JobInfoBo jobInfo) {
        return NAME + "-" + jobInfo.getId();
    }

    private JobInfoBo jobInfo;
    private CronFormat cronFormat;

    // subscribe to "TICK" topic, with group=NAME+jobInfo
    private String instanceName, subscribeTopic = AkkaConstants.TOPIC_TICK;

    public WorkerJobActor(IRegistry registry, JobInfoBo jobInfo) {
        super(registry);
        this.jobInfo = jobInfo;
        instanceName = calcInstanceName(jobInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getActorName() {
        return instanceName;
    }

    protected JobInfoBo getJobInfo() {
        return jobInfo;
    }

    protected CronFormat getCronFormat() {
        if (cronFormat == null) {
            synchronized (this) {
                cronFormat = jobInfo.getCronFormat();
            }
        }
        return cronFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        subscribeToTopic(subscribeTopic, instanceName);
        super.preStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        unsubscribeFromTopic(subscribeTopic, instanceName);
        super.postStop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof JobInfoUpdatedMessage) {
            _eventJobInfoUpdated((JobInfoUpdatedMessage) message);
        } else if (message instanceof TickMessage) {
            _eventTick((TickMessage) message);
        } else {
            super.onReceive(message);
        }
    }

    protected void _eventJobInfoUpdated(JobInfoUpdatedMessage msg) {
        JobInfoBo jobInfo = msg.jobInfo;
        if (StringUtils.equalsIgnoreCase(jobInfo.getId(), this.jobInfo.getId())) {
            this.jobInfo.fromMap(jobInfo.toMap());
            synchronized (this) {
                this.cronFormat = null;
            }
        }
    }

    /*----------------------------------------------------------------------*/

    private TickMessage lastTick;

    /**
     * Checks if "tick" matches job's cron format.
     * 
     * @param tick
     * @return
     */
    protected boolean isTickMatched(TickMessage tick) {
        if (tick.timestampMillis + 30000L > System.currentTimeMillis()) {
            if (lastTick == null || lastTick.timestampMillis < tick.timestampMillis) {
                return getCronFormat().matches(tick.timestampMillis);
            }
        }
        return false;
    }

    /**
     * Main "tick" processing method.
     * 
     * @param tick
     */
    protected void doTick(TickMessage tick) {
        // Date d = new Date(tick.timestampMillis);
        // System.out.println("=========={" + getActorName() + "} TICK matches
        // [" + d + "] against ["
        // + jobInfo.getCron() + "]");
        // TODO
    }

    private AtomicBoolean LOCK = new AtomicBoolean(false);

    protected void _eventTick(TickMessage msg) {
        if (isTickMatched(msg)) {
            if (LOCK.compareAndSet(false, true)) {
                try {
                    lastTick = msg;
                    doTick(msg);
                } finally {
                    LOCK.set(false);
                }
            } else {
                // Busy processing a previous message
                final String logMsg = "{" + instanceName
                        + "} Received TICK message, but I am busy! " + msg;
                Logger.warn(logMsg);
            }
        }
    }
}
