package modules.registry;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.log.ITaskLogDao;
import com.github.ddth.kafka.KafkaClient;

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

    /**
     * Gets {@link IJobDao} instance.
     * 
     * @return
     */
    public IJobDao getJobDao();

    /**
     * Gets {@link ITaskLogDao} instance.
     * 
     * @return
     */
    public ITaskLogDao getTaskLogDao();

    /**
     * Gets {@link KafkaClient} instance to push task notifications to workers.
     * 
     * @return
     */
    public KafkaClient getKafkaClientForTasks();

    /**
     * Gets {@link KafkaClient} instance to receive task results/notifications
     * from workers.
     * 
     * @return
     * 
     */
    public KafkaClient getKafkaClientForFeedback();
}
