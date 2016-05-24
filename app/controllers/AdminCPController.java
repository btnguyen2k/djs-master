package controllers;

import java.util.ArrayList;
import java.util.List;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.job.JobTemplateBo;

import compositions.AdminAuthRequired;
import forms.FormCreateEditJobTemplate;
import forms.FormLogin;
import models.JobTemplateModel;
import play.data.Form;
import play.mvc.Result;
import play.twirl.api.Html;
import utils.DjsMasterConstants;
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
        Html html = render(VIEW_JOB_LIST);
        return ok(html);
    }

    // /*----------------------------------------------------------------------*/
    // public final static String VIEW_BUILDS_CONFIG = "builds_config";
    //
    // @AdminAuthRequired
    // public static Result buildsConfig() throws Exception {
    // Form<FormConfigBuilds> form = Form.form(FormConfigBuilds.class).bind(
    // FormConfigBuilds.newInstance().toMap());
    // form.discardErrors();
    // Html html = render(VIEW_BUILDS_CONFIG, form);
    // return ok(html);
    // }
    //
    // @AdminAuthRequired
    // public static Result buildsConfigSubmit() throws Exception {
    // Form<FormConfigBuilds> form =
    // Form.form(FormConfigBuilds.class).bindFromRequest();
    // if (form.hasErrors()) {
    // Html html = render(VIEW_BUILDS_CONFIG, form);
    // return ok(html);
    // }
    //
    // FormConfigBuilds model = form.get();
    // IConfDao confDao = Registry.getConfDao();
    // confDao.updateOrCreate(XeiuConstants.CONF_IOS_BUILD_MIN,
    // model.iosBuildMin);
    // confDao.updateOrCreate(XeiuConstants.CONF_IOS_BUILD_MAX,
    // model.iosBuildMax);
    // confDao.updateOrCreate(XeiuConstants.CONF_AOS_BUILD_MIN,
    // model.aosBuildMin);
    // confDao.updateOrCreate(XeiuConstants.CONF_AOS_BUILD_MAX,
    // model.aosBuildMax);
    //
    // flash(VIEW_BUILDS_CONFIG, Messages.get("msg.builds.config.update.done"));
    //
    // return redirect(controllers.routes.AdminCPController.buildsConfig());
    // }
    //
    // /*----------------------------------------------------------------------*/
    // public final static String VIEW_MAINTENANCE_CONFIG =
    // "maintenance_config";
    //
    // @AdminAuthRequired
    // public static Result maintenanceConfig() throws Exception {
    // Form<FormConfigMaintenance> form =
    // Form.form(FormConfigMaintenance.class).bind(
    // FormConfigMaintenance.newInstance().toMap());
    // form.discardErrors();
    // Html html = render(VIEW_MAINTENANCE_CONFIG, form);
    // return ok(html);
    // }
    //
    // @AdminAuthRequired
    // public static Result maintenanceConfigSubmit() throws Exception {
    // Form<FormConfigMaintenance> form =
    // Form.form(FormConfigMaintenance.class).bindFromRequest();
    // if (form.hasErrors()) {
    // Html html = render(VIEW_MAINTENANCE_CONFIG, form);
    // return ok(html);
    // }
    //
    // FormConfigMaintenance model = form.get();
    // IConfDao confDao = Registry.getConfDao();
    // confDao.updateOrCreate(XeiuConstants.CONF_MAINTENANCE, model.toJson());
    //
    // flash(VIEW_MAINTENANCE_CONFIG,
    // Messages.get("msg.maintenance.config.update.done"));
    //
    // return
    // redirect(controllers.routes.AdminCPController.maintenanceConfig());
    // }
    //
    // /*----------------------------------------------------------------------*/
    // public final static String VIEW_POPUP_EVENT_CONFIG =
    // "popup_event_config";
    //
    // @AdminAuthRequired
    // public static Result popupEventConfig() throws Exception {
    // Form<FormConfigPopupEvent> form =
    // Form.form(FormConfigPopupEvent.class).bind(
    // FormConfigPopupEvent.newInstance().toMap());
    // form.discardErrors();
    // Html html = render(VIEW_POPUP_EVENT_CONFIG, form);
    // return ok(html);
    // }
    //
    // @AdminAuthRequired
    // public static Result popupEventConfigSubmit() throws Exception {
    // Form<FormConfigPopupEvent> form =
    // Form.form(FormConfigPopupEvent.class).bindFromRequest();
    // if (form.hasErrors()) {
    // Html html = render(VIEW_POPUP_EVENT_CONFIG, form);
    // return ok(html);
    // }
    //
    // FormConfigPopupEvent model = form.get();
    // IConfDao confDao = Registry.getConfDao();
    // confDao.updateOrCreate(XeiuConstants.CONF_POPUP_EVENT, model.toJson());
    //
    // flash(VIEW_POPUP_EVENT_CONFIG,
    // Messages.get("msg.popup_event.config.update.done"));
    //
    // return redirect(controllers.routes.AdminCPController.popupEventConfig());
    // }
}
