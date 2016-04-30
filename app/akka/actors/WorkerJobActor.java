package akka.actors;

import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.message.JobInfoUpdatedMessage;
import com.github.ddth.djs.message.TickMessage;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;
import akka.cluster.pubsub.DistributedPubSubMediator.UnsubscribeAck;
import akka.utils.AkkaConstants;
import play.Logger;

/**
 * Actor on [worker] node(s) that triggers a job's task.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class WorkerJobActor extends UntypedActor {

    public final static String NAME = WorkerJobActor.class.getCanonicalName();

    public static String calcInstanceName(JobInfoBo jobInfo) {
        return NAME + "-" + jobInfo.getId();
    }

    private JobInfoBo jobInfo;

    // subscribe to "TICK" topic, with group=NAME+jobInfo
    private String instanceName, subscribeTopic = AkkaConstants.TOPIC_TICK;

    private ActorRef distributedPubSubMediator = DistributedPubSub.get(getContext().system())
            .mediator();

    public WorkerJobActor(JobInfoBo jobInfo) {
        this.jobInfo = jobInfo;
        instanceName = calcInstanceName(jobInfo);
    }

    public String getInstanceName() {
        return instanceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        distributedPubSubMediator.tell(
                new DistributedPubSubMediator.Subscribe(subscribeTopic, instanceName, getSelf()),
                getSelf());
        Logger.info(
                "Subscribed to topic [" + subscribeTopic + "] as group [" + instanceName + "].");
        super.preStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        // unsubscribe from "TICK" topic
        distributedPubSubMediator.tell(
                new DistributedPubSubMediator.Unsubscribe(subscribeTopic, instanceName, getSelf()),
                getSelf());
        Logger.info("Unsubscribed from topic [" + subscribeTopic + "] as group [" + instanceName
                + "].");
        super.postStop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof DistributedPubSubMediator.SubscribeAck) {
            // subscribed successfully!
            DistributedPubSubMediator.SubscribeAck ack = (SubscribeAck) message;
            Logger.info(
                    "[" + instanceName + "] Subscribed successfully to [" + ack.subscribe() + "].");
        } else if (message instanceof DistributedPubSubMediator.UnsubscribeAck) {
            // unsubscribed successfully!
            DistributedPubSubMediator.UnsubscribeAck ack = (UnsubscribeAck) message;
            Logger.info("[" + instanceName + "] Unsubscribed successfully from ["
                    + ack.unsubscribe() + "].");
        } else if (message instanceof JobInfoUpdatedMessage) {
            _eventJobInfoUpdated((JobInfoUpdatedMessage) message);
        } else if (message instanceof TickMessage) {
            _eventTick((TickMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void _eventJobInfoUpdated(JobInfoUpdatedMessage msg) {
        this.jobInfo = msg.jobInfo;
    }

    private void _eventTick(TickMessage msg) {
        System.out.println("========== [" + jobInfo);
    }
}
