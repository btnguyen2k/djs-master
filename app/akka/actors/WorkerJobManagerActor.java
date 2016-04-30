package akka.actors;

import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.message.JobInfoAddedMessage;
import com.github.ddth.djs.message.JobInfoRemovedMessage;
import com.github.ddth.djs.message.JobInfoUpdatedMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Actor on [worker] node(s) that manages (create/update/delete) job actors.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class WorkerJobManagerActor extends UntypedActor {

    public final static Props PROPS = Props.create(WorkerJobManagerActor.class);
    public final static String NAME = WorkerJobManagerActor.class.getCanonicalName();

    private void _eventJobInfoAdded(JobInfoAddedMessage msg) {
        JobInfoBo jobInfo = msg.jobInfo;
        Props props = Props.create(WorkerJobActor.class, jobInfo);
        getContext().actorOf(props, WorkerJobActor.calcInstanceName(jobInfo));
    }

    private ActorRef lookupChild(JobInfoBo jobInfo) {
        String path = WorkerJobActor.calcInstanceName(jobInfo);
        return getContext().actorSelection(path).anchor();
    }

    private void _eventJobInfoRemoved(JobInfoRemovedMessage msg) {
        ActorRef actorRef = lookupChild(msg.jobInfo);
        if (actorRef != null) {
            getContext().stop(actorRef);
        }
    }

    private void _eventJobInfoUpdated(JobInfoUpdatedMessage msg) {
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
            unhandled(message);
        }
    }
}
