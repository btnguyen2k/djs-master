package modules.registry;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.log.ITaskLogDao;
import com.github.ddth.queue.IQueue;

import akka.actor.ActorSystem;
import bo.user.IUserDao;
import play.Application;
import queue.IQueueService;

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
     * Gets {@link IUserDao} instance.
     * 
     * @return
     */
    public IUserDao getUserDao();

    /**
     * Gets {@link IQueueService} to push/retrieve task fireoff notifications.
     * 
     * @return
     */
    public IQueueService getQueueService();

    /**
     * Gets {@link IQueue} to buffer task results/feedbacks from workers.
     * 
     * @return
     */
    public IQueue getQueueTaskFeedback();

}
