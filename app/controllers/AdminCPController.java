package controllers;

import java.util.ArrayList;
import java.util.List;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.bo.job.JobTemplateBo;

import compositions.AdminAuthRequired;
import forms.FormCreateEditJobInfo;
import forms.FormCreateEditJobTemplate;
import forms.FormLogin;
import models.JobInfoModel;
import models.JobTemplateModel;
import play.data.Form;
import play.mvc.Result;
import play.twirl.api.Html;
import utils.DjsMasterConstants;
import utils.JobUtils;
import utils.UserUtils;

/**
 * AdminCP controller.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class AdminCPController extends BaseController {
    /*----------------------------------------------------------------------*/
    public final static String VIEW_LOGIN = "login";

    /*
     * Handle GET:/login?returnUrl=xxx
     */
    public Result login(final String returnUrl) throws Exception {
        Form<FormLogin> form = formFactory.form(FormLogin.class);
        Html html = render(VIEW_LOGIN, form);
        return ok(html);
    }

    /*
     * Handle POST:/login?returnUrl=xxx
     */
    public Result loginSubmit(final String returnUrl) throws Exception {
        Form<FormLogin> form = formFactory.form(FormLogin.class).bindFromRequest();
        if (form.hasErrors()) {
            Html html = render(VIEW_LOGIN, form);
            return ok(html);
        }
        FormLogin model = form.get();
        UserUtils.loginAdmin(model.user, session());
        if (returnUrl != null && returnUrl.startsWith("/")) {
            return redirect(returnUrl);
        } else {
            return redirect(controllers.routes.AdminCPController.home());
        }
    }

    /*----------------------------------------------------------------------*/
    public Result index() throws Exception {
        return redirect(controllers.routes.AdminCPController.home());
    }

    public final static String VIEW_HOME = "home";

    @AdminAuthRequired
    public Result home() throws Exception {
        Html html = render(VIEW_HOME);
        return ok(html);
    }

    /*----------------------------------------------------------------------*/
    public final static String VIEW_JOB_TEMPLATE_LIST = "job_template_list";

    @AdminAuthRequired
    public Result jobTemplateList() throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        String[] jobTplIds = jobDao.getAllJobTemplateIds();
        List<JobTemplateBo> jobTemplates = new ArrayList<>();
        for (String id : jobTplIds) {
            JobTemplateBo jobTpl = jobDao.getJobTemplate(id);
            if (jobTpl != null) {
                jobTemplates.add(jobTpl);
            }
        }
        Html html = render(VIEW_JOB_TEMPLATE_LIST,
                (Object) JobTemplateModel.newInstances(jobTemplates));
        return ok(html);
    }

    public final static String VIEW_CREATE_JOB_TEMPLATE = "create_job_template";

    @AdminAuthRequired
    public Result createJobTemplate() throws Exception {
        Form<FormCreateEditJobTemplate> form = formFactory.form(FormCreateEditJobTemplate.class)
                .bind(FormCreateEditJobTemplate.defaultInstance.toMap());
        form.discardErrors();
        Html html = render(VIEW_CREATE_JOB_TEMPLATE, form);
        return ok(html);
    }

    @AdminAuthRequired
    public Result createJobTemplateSubmit() throws Exception {
        Form<FormCreateEditJobTemplate> form = formFactory.form(FormCreateEditJobTemplate.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            Html html = render(VIEW_CREATE_JOB_TEMPLATE, form);
            return ok(html);
        }

        FormCreateEditJobTemplate model = form.get();
        JobTemplateBo jobTemplate = JobTemplateBo.newInstance();
        jobTemplate.setId(model.id).setDescription(model.description).setParams(model.params);
        IJobDao jobDao = registry.get().getJobDao();
        jobDao.create(jobTemplate);

        flash(VIEW_JOB_TEMPLATE_LIST,
                calcMessages().at("msg.job_template.create.done", jobTemplate.getId()));

        return redirect(routes.AdminCPController.jobTemplateList());
    }

    public final static String VIEW_EDIT_JOB_TEMPLATE = "edit_job_template";

    @AdminAuthRequired
    public Result editJobTemplate(String id) throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        JobTemplateBo jobTemplate = jobDao.getJobTemplate(id);
        if (jobTemplate == null) {
            flash(VIEW_JOB_TEMPLATE_LIST, DjsMasterConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.job_template.not_found", id));

            return redirect(routes.AdminCPController.jobTemplateList());
        }

        Form<FormCreateEditJobTemplate> form = formFactory.form(FormCreateEditJobTemplate.class)
                .bind(FormCreateEditJobTemplate.newInstance(jobTemplate).toMap());
        form.discardErrors();
        Html html = render(VIEW_EDIT_JOB_TEMPLATE, form);
        return ok(html);
    }

    @AdminAuthRequired
    public Result editJobTemplateSubmit(String id) throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        JobTemplateBo jobTemplate = jobDao.getJobTemplate(id);
        if (jobTemplate == null) {
            flash(VIEW_JOB_TEMPLATE_LIST, DjsMasterConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.job_template.not_found", id));

            return redirect(routes.AdminCPController.jobTemplateList());
        }

        Form<FormCreateEditJobTemplate> form = formFactory.form(FormCreateEditJobTemplate.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            Html html = render(VIEW_JOB_TEMPLATE_LIST, form);
            return ok(html);
        }

        FormCreateEditJobTemplate model = form.get();
        jobTemplate.setDescription(model.description).setParams(model.params);
        jobDao.update(jobTemplate);

        flash(VIEW_JOB_TEMPLATE_LIST,
                calcMessages().at("msg.job_template.edit.done", jobTemplate.getId()));

        return redirect(routes.AdminCPController.jobTemplateList());
    }

    public final static String VIEW_DELETE_JOB_TEMPLATE = "delete_job_template";

    @AdminAuthRequired
    public Result deleteJobTemplate(String id) throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        JobTemplateBo jobTemplate = jobDao.getJobTemplate(id);
        if (jobTemplate == null) {
            flash(VIEW_JOB_TEMPLATE_LIST, DjsMasterConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.job_template.not_found", id));

            return redirect(routes.AdminCPController.jobTemplateList());
        }

        JobTemplateModel model = JobTemplateModel.newInstance(jobTemplate);
        Html html = render(VIEW_DELETE_JOB_TEMPLATE, model);
        return ok(html);
    }

    @AdminAuthRequired
    public Result deleteJobTemplateSubmit(String id) throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        JobTemplateBo jobTemplate = jobDao.getJobTemplate(id);
        if (jobTemplate == null) {
            flash(VIEW_JOB_TEMPLATE_LIST, DjsMasterConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.job_template.not_found", id));

            return redirect(routes.AdminCPController.jobTemplateList());
        }

        jobDao.delete(jobTemplate);

        flash(VIEW_JOB_TEMPLATE_LIST,
                calcMessages().at("msg.job_template.delete.done", jobTemplate.getId()));

        return redirect(routes.AdminCPController.jobTemplateList());
    }
    /*----------------------------------------------------------------------*/

    public final static String VIEW_JOB_LIST = "job_list";

    @AdminAuthRequired
    public Result jobList() throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        String[] jobInfoIds = jobDao.getAllJobInfoIds();
        List<JobInfoBo> jobInfoList = new ArrayList<>();
        for (String id : jobInfoIds) {
            JobInfoBo jobInfo = jobDao.getJobInfo(id);
            if (jobInfo != null) {
                jobInfoList.add(jobInfo);
            }
        }
        Html html = render(VIEW_JOB_LIST, (Object) JobInfoModel.newInstances(jobInfoList));
        return ok(html);
    }

    public final static String VIEW_CREATE_JOB = "create_job";

    @AdminAuthRequired
    public Result createJob() throws Exception {
        Form<FormCreateEditJobInfo> form = formFactory.form(FormCreateEditJobInfo.class)
                .bind(FormCreateEditJobInfo.defaultInstance.toMap());
        form.discardErrors();

        Html html = render(VIEW_CREATE_JOB, form,
                JobTemplateModel.newInstances(JobUtils.getAllJobTemplates()));
        return ok(html);
    }

    @AdminAuthRequired
    public Result createJobSubmit() throws Exception {
        Form<FormCreateEditJobInfo> form = formFactory.form(FormCreateEditJobInfo.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            Html html = render(VIEW_CREATE_JOB, form,
                    JobTemplateModel.newInstances(JobUtils.getAllJobTemplates()));
            return ok(html);
        }

        FormCreateEditJobInfo model = form.get();
        JobInfoBo jobInfo = JobInfoBo.newInstance();
        jobInfo.setId(model.id).setDescription(model.description).setTemplateId(model.templateId)
                .setCron(model.cron).setParams(model.paramsMap).setTags(model.tags);
        IJobDao jobDao = registry.get().getJobDao();
        jobDao.create(jobInfo);

        flash(VIEW_JOB_TEMPLATE_LIST,
                calcMessages().at("msg.job_info.create.done", jobInfo.getId()));

        return redirect(routes.AdminCPController.jobList());
    }

    public final static String VIEW_EDIT_JOB = "edit_job";

    @AdminAuthRequired
    public Result editJob(String id) throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        JobInfoBo jobInfo = jobDao.getJobInfo(id);
        if (jobInfo == null) {
            flash(VIEW_JOB_LIST, DjsMasterConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.job_info.not_found", id));

            return redirect(routes.AdminCPController.jobList());
        }

        Form<FormCreateEditJobInfo> form = formFactory.form(FormCreateEditJobInfo.class)
                .bind(FormCreateEditJobInfo.newInstance(jobInfo).toMap());
        form.discardErrors();
        Html html = render(VIEW_EDIT_JOB, form,
                JobTemplateModel.newInstances(JobUtils.getAllJobTemplates()));
        return ok(html);
    }

    @AdminAuthRequired
    public Result editJobSubmit(String id) throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        JobInfoBo jobInfo = jobDao.getJobInfo(id);
        if (jobInfo == null) {
            flash(VIEW_JOB_LIST, DjsMasterConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.job_info.not_found", id));

            return redirect(routes.AdminCPController.jobList());
        }

        Form<FormCreateEditJobInfo> form = formFactory.form(FormCreateEditJobInfo.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            Html html = render(VIEW_JOB_LIST, form);
            return ok(html);
        }

        FormCreateEditJobInfo model = form.get();
        jobInfo.setDescription(model.description).setTemplateId(model.templateId)
                .setCron(model.cron).setParams(model.paramsMap).setTags(model.tags);
        jobDao.update(jobInfo);

        flash(VIEW_JOB_LIST, calcMessages().at("msg.job_info.edit.done", jobInfo.getId()));

        return redirect(routes.AdminCPController.jobList());
    }

    public final static String VIEW_DELETE_JOB = "delete_job";

    @AdminAuthRequired
    public Result deleteJob(String id) throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        JobInfoBo jobInfo = jobDao.getJobInfo(id);
        if (jobInfo == null) {
            flash(VIEW_JOB_LIST, DjsMasterConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.job_info.not_found", id));

            return redirect(routes.AdminCPController.jobList());
        }

        JobInfoModel model = JobInfoModel.newInstance(jobInfo);
        Html html = render(VIEW_DELETE_JOB, model);
        return ok(html);
    }

    @AdminAuthRequired
    public Result deleteJobSubmit(String id) throws Exception {
        IJobDao jobDao = registry.get().getJobDao();
        JobInfoBo jobInfo = jobDao.getJobInfo(id);
        if (jobInfo == null) {
            flash(VIEW_JOB_LIST, DjsMasterConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.job_info.not_found", id));

            return redirect(routes.AdminCPController.jobList());
        }

        jobDao.delete(jobInfo);

        flash(VIEW_JOB_LIST, calcMessages().at("msg.job_info.delete.done", jobInfo.getId()));

        return redirect(routes.AdminCPController.jobList());
    }

    @AdminAuthRequired
    public Result apiStartJob() {
        String id = request().getQueryString("id");
        IJobDao jobDao = registry.get().getJobDao();
        JobInfoBo jobInfo = jobDao.getJobInfo(id);
        if (jobInfo == null) {
            return doResponseJson(404, calcMessages().at("error.job_info.not_found", id));
        }
        jobInfo.setIsRunning(true);
        jobDao.update(jobInfo);
        return doResponseJson(200, "Successful", jobInfo.isRunning());
    }

    @AdminAuthRequired
    public Result apiStopJob() {
        String id = request().getQueryString("id");
        IJobDao jobDao = registry.get().getJobDao();
        JobInfoBo jobInfo = jobDao.getJobInfo(id);
        if (jobInfo == null) {
            return doResponseJson(404, calcMessages().at("error.job_info.not_found", id));
        }
        jobInfo.setIsRunning(false);
        jobDao.update(jobInfo);
        return doResponseJson(200, "Successful", jobInfo.isRunning());
    }
}
