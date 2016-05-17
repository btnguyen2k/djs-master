package modules.initdb;

import javax.inject.Inject;

import com.github.ddth.djs.bo.log.ITaskLogDao;
import com.github.ddth.djs.bo.log.TaskLogBo;
import com.google.inject.Provider;

import modules.registry.IRegistry;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

public class InitDbModule extends Module {

    public final static class InitDbBootstrap {
        @Inject
        public InitDbBootstrap(Provider<IRegistry> registry) {
            TaskLogBo taskLog = TaskLogBo.newInstance();
            ITaskLogDao taskLogDao = registry.get().getTaskLogDao();
            System.out.println("========== Task Log: " + taskLogDao.getTaskLog(taskLog.getId()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq<Binding<?>> bindings(Environment env, Configuration conf) {
        Seq<Binding<?>> bindings = seq(bind(InitDbBootstrap.class).toSelf().eagerly());
        return bindings;
    }

}
