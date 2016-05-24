package modules.registry;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.log.ITaskLogDao;
import com.github.ddth.kafka.KafkaClient;

import akka.actor.ActorSystem;
import bo.user.IUserDao;
import play.Application;
import play.Logger;
import play.inject.ApplicationLifecycle;
import utils.DjsMasterGlobals;

@Singleton
public class RegistryImpl implements IRegistry {

    private ApplicationContext appContext;
    private Application playApp;
    private ActorSystem actorSystem;

    /**
     * {@inheritDoc}
     */
    @Inject
    public RegistryImpl(ApplicationLifecycle lifecycle, Application playApp,
            ActorSystem actorSystem) {
        DjsMasterGlobals.registry = this;
        DjsMasterGlobals.appConfig = playApp.configuration();

        this.playApp = playApp;
        this.actorSystem = actorSystem;

        init();

        lifecycle.addStopHook(() -> {
            destroy();
            return null;
        });
    }

    private void initApplicationContext() {
        String configFile = playApp.configuration().getString("spring.conf");
        File springConfigFile = configFile.startsWith("/") ? new File(configFile)
                : new File(playApp.path(), configFile);
        AbstractApplicationContext applicationContext = new FileSystemXmlApplicationContext(
                "file:" + springConfigFile.getAbsolutePath());
        applicationContext.start();
        appContext = applicationContext;
    }

    private void initActors() {
    }

    private void init() {
        initApplicationContext();
        initActors();
    }

    private void destroyApplicationContext() {
        if (appContext != null) {
            try {
                ((AbstractApplicationContext) appContext).destroy();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            } finally {
                appContext = null;
            }
        }
    }

    private void destroyActors() {
    }

    private void destroy() {
        destroyActors();
        destroyApplicationContext();
    }

    /*----------------------------------------------------------------------*/
    /**
     * {@inheritDoc}
     */
    @Override
    public Application getPlayApplication() {
        return playApp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActorSystem getLocalActorSystem() {
        return actorSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJobDao getJobDao() {
        return appContext.getBean(IJobDao.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITaskLogDao getTaskLogDao() {
        return appContext.getBean(ITaskLogDao.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUserDao getUserDao() {
        return appContext.getBean(IUserDao.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KafkaClient getKafkaClientForTasks() {
        return appContext.getBean("KAFKA_CLIENT_TASKS", KafkaClient.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KafkaClient getKafkaClientForFeedback() {
        return appContext.getBean("KAFKA_CLIENT_FEEDBACK", KafkaClient.class);
    }
}
