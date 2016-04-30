package akka.actors;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.djs.message.TickMessage;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.utils.AkkaConstants;
import utils.DjsMasterConstants;

/**
 * Actor on [master] node(s) that:
 * 
 * <ul>
 * <li>Keeps track of nodes within the cluster.</li>
 * <li>Publishes "tick" message every tick (only if the current node is leader).
 * </li>
 * </ul>
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class MasterFacadeActor extends UntypedActor {

    public final static Props PROPS = Props.create(MasterFacadeActor.class);
    public final static String NAME = MasterFacadeActor.class.getCanonicalName();

    private LoggingAdapter LOGGER = Logging.getLogger(getContext().system(), this);
    private Cluster cluster = Cluster.get(getContext().system());
    private ActorRef distributedPubSubMediator = DistributedPubSub.get(getContext().system())
            .mediator();

    private Cancellable tick = getContext().system().scheduler().schedule(
            DjsMasterConstants.DELAY_INITIAL, DjsMasterConstants.DELAY_TICK, new Runnable() {
                @Override
                public void run() {
                    getSelf().tell(new TickMessage(), ActorRef.noSender());
                }
            }, getContext().dispatcher());

    private SortedSet<Member> clusterLeaders = new TreeSet<Member>(new Comparator<Member>() {
        public int compare(Member a, Member b) {
            if (a.isOlderThan(b)) {
                return -1;
            } else if (b.isOlderThan(a)) {
                return 1;
            } else {
                return 0;
            }
        }
    });
    private SortedSet<Member> clusterWorkers = new TreeSet<Member>(new Comparator<Member>() {
        public int compare(Member a, Member b) {
            if (a.isOlderThan(b)) {
                return -1;
            } else if (b.isOlderThan(a)) {
                return 1;
            } else {
                return 0;
            }
        }
    });

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        // subscribe to cluster changes
        cluster.subscribe(getSelf(), MemberEvent.class);
        super.preStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        if (tick != null) {
            tick.cancel();
        }
        if (cluster != null) {
            cluster.unsubscribe(getSelf());
        }
        super.postStop();
    }

    private void _eventMemberUp(MemberUp msg) {
        Member m = msg.member();
        if (m.hasRole(AkkaConstants.ROLE_MASTER)) {
            clusterLeaders.add(m);
            LOGGER.info("Master node UP [" + m.address().toString()
                    + "]. Number of nodes in cluster: " + clusterLeaders.size());
        }
        if (m.hasRole(AkkaConstants.ROLE_MASTER)) {
            clusterWorkers.add(m);
            LOGGER.info("Worker node UP [" + m.address().toString()
                    + "]. Number of nodes in cluster: " + clusterWorkers.size());
        }
    }

    private void _eventMemberRemoved(MemberRemoved msg) {
        Member m = msg.member();
        if (m.hasRole(AkkaConstants.ROLE_MASTER)) {
            clusterLeaders.remove(m);
            LOGGER.info("Master node REMOVED [" + m.address().toString()
                    + "]. Number of nodes in cluster: " + clusterLeaders.size());
        }
        if (m.hasRole(AkkaConstants.ROLE_MASTER)) {
            clusterWorkers.remove(m);
            LOGGER.info("Worker node REMOVED [" + m.address().toString()
                    + "]. Number of nodes in cluster: " + clusterWorkers.size());
        }
    }

    private AtomicBoolean LOCK = new AtomicBoolean(false);

    private void _eventTick(TickMessage tick) {
        Member leader = null;
        try {
            leader = clusterLeaders.first();
        } catch (NoSuchElementException e) {
            leader = null;
        }

        if (leader == null) {
            final String msg = "Received TICK message, but cluster member is empty! " + tick;
            LOGGER.warning(msg);
        } else {
            if (LOCK.compareAndSet(false, true)) {
                try {
                    String leaderAddr = leader.address().toString();
                    String thisNodeAddr = context().system().provider().getDefaultAddress()
                            .toString();
                    if (StringUtils.equalsIgnoreCase(leaderAddr, thisNodeAddr)) {
                        distributedPubSubMediator.tell(new DistributedPubSubMediator.Publish(
                                AkkaConstants.TOPIC_TICK, new TickMessage(), true), getSelf());
                    } else {
                        // I am not leader!
                    }
                } finally {
                    // unlock
                    LOCK.set(false);
                }
            } else {
                // Busy processing a previous message
            }
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof MemberUp) {
            _eventMemberUp((MemberUp) message);
        } else if (message instanceof MemberRemoved) {
            _eventMemberRemoved((MemberRemoved) message);
        } else if (message instanceof TickMessage) {
            _eventTick((TickMessage) message);
        } else {
            unhandled(message);
        }
    }
}
