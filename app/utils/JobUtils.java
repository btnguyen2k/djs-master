package utils;

import java.util.ArrayList;
import java.util.List;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.bo.job.JobTemplateBo;

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
}
