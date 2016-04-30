package modules.registry;

import akka.actor.ActorSystem;
import play.Application;

public interface IRegistry {
    /**
     * Gets the current running Play application.
     * 
     * @return
     */
    public Application getPlayApplication();

    /**
     * Gets the local {@link ActorSystem} instance.
     * 
     * @return
     */
    public ActorSystem getLocalActorSystem();
}
