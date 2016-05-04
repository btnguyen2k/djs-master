package akka.utils;

import akka.actors.WorkerJobManagerActor;

public class AkkaConstants {
    public final static String ROLE_MASTER = "master";
    public final static String ROLE_WORKER = "worker";

    /**
     * A "tick" message is published to this topic for every "TICK".
     */
    public final static String TOPIC_TICK = "TICK";

    /**
     * {@link WorkerJobManagerActor} listens to this topic for job's events
     * (add/remove/update).
     */
    public final static String TOPIC_JOBEVENT = "JOB_EVENT";
}
