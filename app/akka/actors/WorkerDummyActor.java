package akka.actors;

import com.github.ddth.djs.message.TickMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.utils.AkkaConstants;

public class WorkerDummyActor extends UntypedActor {

    public final static Props PROPS = Props.create(WorkerDummyActor.class);
    public final static String NAME = WorkerDummyActor.class.getCanonicalName();

    private LoggingAdapter LOGGER = Logging.getLogger(getContext().system(), this);
    // private Cluster cluster = Cluster.get(getContext().system());
    private ActorRef distributedPubSubMediator = DistributedPubSub.get(getContext().system())
            .mediator();

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        // subscribe to "TICK" topic, with group=NAME
        final String topic = AkkaConstants.TOPIC_TICK;
        final String group = NAME;
        distributedPubSubMediator
                .tell(new DistributedPubSubMediator.Subscribe(topic, group, getSelf()), getSelf());
        super.preStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        // unsubscribe to "TICK" topic
        final String topic = AkkaConstants.TOPIC_TICK;
        final String group = NAME;
        distributedPubSubMediator.tell(
                new DistributedPubSubMediator.Unsubscribe(topic, group, getSelf()), getSelf());
        super.postStop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof DistributedPubSubMediator.SubscribeAck) {
            // subscribed successfully!
            LOGGER.info("WORKER: subcribed successfully.");
        } else if (message instanceof DistributedPubSubMediator.UnsubscribeAck) {
            // unsubscribed successfully!
            LOGGER.info("WORKER: unsubcribed successfully.");
        } else if (message instanceof TickMessage) {
            String msg = "Worker TICK! " + message;
            LOGGER.info(msg);
        }
        unhandled(message);
    }
}
