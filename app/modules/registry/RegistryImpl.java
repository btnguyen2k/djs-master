package modules.registry;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import akka.actor.ActorSystem;
import play.Application;
import play.Logger;
import play.inject.ApplicationLifecycle;

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
        this.playApp = playApp;
        this.actorSystem = actorSystem;

        init();

        lifecycle.addStopHook(() -> {
            destroy();
            return null;
        });
    }

    private void initApplicationContext() {
        String configFile = System.getProperty("spring.config.file", "conf/spring/beans.xml");
        if (!StringUtils.isBlank(configFile)) {
            File springConfigFile = configFile.startsWith("/") ? new File(configFile)
                    : new File(playApp.path(), configFile);
            AbstractApplicationContext applicationContext = new FileSystemXmlApplicationContext(
                    "file:" + springConfigFile.getAbsolutePath());
            applicationContext.start();
            appContext = applicationContext;
        }
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
}
