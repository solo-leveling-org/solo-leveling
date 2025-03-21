package com.sleepkqq.sololeveling.view.task;

import com.sleepkqq.sololeveling.api.TaskApi;
import com.sleepkqq.sololeveling.api.UserApi;
import com.sleepkqq.sololeveling.proto.user.UserTaskInfo;
import com.sleepkqq.sololeveling.service.auth.TgAuthService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import one.util.streamex.StreamEx;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Tasks")
@Route("tasks")
@Menu(order = 2, icon = LineAwesomeIconUrl.TASKS_SOLID)
@PermitAll
public class TaskView extends Composite<VerticalLayout> {

  public TaskView(
      TaskApi taskApi,
      UserApi userApi,
      TgAuthService tgAuthService
  ) {
    getContent().setWidth("100%");
    getContent().getStyle().set("flex-grow", "1");

    var userId = tgAuthService.getCurrentUser().getId();
    var currentTasks = userApi.getCurrentTasks(userId);

    var tasks = taskApi.getTasks(StreamEx.of(currentTasks).map(UserTaskInfo::getId).toList());
    getContent().add(new SwipedComponent(tasks));
  }
}
