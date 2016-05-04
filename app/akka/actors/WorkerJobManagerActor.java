package akka.actors;

import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.message.bus.JobInfoAddedMessage;
import com.github.ddth.djs.message.bus.JobInfoRemovedMessage;
import com.github.ddth.djs.message.bus.JobInfoUpdatedMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.utils.AkkaConstants;
import modules.registry.IRegistry;

/**
 * Actor on [worker] node(s) that manages (create/update/delete) job actors.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class WorkerJobManagerActor extends BaseDjsActor {

    public final static String NAME = WorkerJobManagerActor.class.getSimpleName();

    public WorkerJobManagerActor(IRegistry registry) {
        super(registry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        subscribeToTopic(AkkaConstants.TOPIC_JOBEVENT);
        super.preStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        unsubscribeFromTopic(AkkaConstants.TOPIC_JOBEVENT);
        super.postStop();
    }

    protected ActorRef lookupChild(JobInfoBo jobInfo) {
        String path = WorkerJobActor.calcInstanceName(jobInfo);
        return getContext().actorSelection(path).anchor();
    }

    protected void _eventJobInfoAdded(JobInfoAddedMessage msg) {
        JobInfoBo jobInfo = msg.jobInfo;
        Props props = Props.create(WorkerJobActor.class, jobInfo);
        getContext().actorOf(props, WorkerJobActor.calcInstanceName(jobInfo));
    }

    protected void _eventJobInfoRemoved(JobInfoRemovedMessage msg) {
        ActorRef actorRef = lookupChild(msg.jobInfo);
        if (actorRef != null) {
            getContext().stop(actorRef);
        }
    }

    protected void _eventJobInfoUpdated(JobInfoUpdatedMessage msg) {
        ActorRef actorRef = lookupChild(msg.jobInfo);
        if (actorRef != null) {
            actorRef.tell(msg, getSelf());
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof JobInfoAddedMessage) {
            _eventJobInfoAdded((JobInfoAddedMessage) message);
        } else if (message instanceof JobInfoRemovedMessage) {
            _eventJobInfoRemoved((JobInfoRemovedMessage) message);
        } else if (message instanceof JobInfoUpdatedMessage) {
            _eventJobInfoUpdated((JobInfoUpdatedMessage) message);
        } else {
            super.onReceive(message);
        }
    }
}
