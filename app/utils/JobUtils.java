package utils;

import java.util.ArrayList;
import java.util.List;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.bo.job.JobTemplateBo;
import com.github.ddth.djs.message.BaseMessage;
import com.github.ddth.djs.message.queue.TaskFireoffMessage;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.IQueueMessage;
import com.github.ddth.queue.impl.universal2.UniversalQueueMessage;

import play.Logger;

public class JobUtils {
    public static int countJobTemplates() {
        return DjsMasterGlobals.registry.getJobDao().getAllJobTemplateIds().length;
    }

    public static int countJobs() {
        return DjsMasterGlobals.registry.getJobDao().getAllJobInfoIds().length;
    }

    public static JobTemplateBo[] getAllJobTemplates() {
        IJobDao jobDao = DjsMasterGlobals.registry.getJobDao();
        String[] idList = jobDao.getAllJobTemplateIds();
        List<JobTemplateBo> result = new ArrayList<>();
        for (String id : idList) {
            JobTemplateBo bo = jobDao.getJobTemplate(id);
            if (bo != null) {
                result.add(bo);
            }
        }
        return result.toArray(JobTemplateBo.EMPTY_ARRAY);
    }

    public static JobInfoBo[] getAllJobs() {
        IJobDao jobDao = DjsMasterGlobals.registry.getJobDao();
        String[] idList = jobDao.getAllJobInfoIds();
        List<JobInfoBo> result = new ArrayList<>();
        for (String id : idList) {
            JobInfoBo bo = jobDao.getJobInfo(id);
            if (bo != null) {
                result.add(bo);
            }
        }
        return result.toArray(JobInfoBo.EMPTY_ARRAY);
    }

    public static byte[] serializeTask(BaseMessage msg) {
        return msg != null ? msg.toBytes() : null;
    }

    public static <T extends BaseMessage> T deserializeTask(byte[] data, Class<T> clazz) {
        try {
            return BaseMessage.deserialize(data, clazz);
        } catch (Exception e) {
            Logger.warn("Cannot deserialize data to [" + clazz + "]: " + e.getMessage(), e);
            return null;
        }
    }

    public static boolean notifyTask(IQueue queue, TaskFireoffMessage taskFireoffMsg) {
        IQueueMessage queueMsg = UniversalQueueMessage.newInstance()
                .content(taskFireoffMsg.toBytes());
        return queue.queue(queueMsg);
    }
}
