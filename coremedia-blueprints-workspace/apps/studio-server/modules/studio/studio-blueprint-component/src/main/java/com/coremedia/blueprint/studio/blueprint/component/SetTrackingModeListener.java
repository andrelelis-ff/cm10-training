package com.coremedia.blueprint.studio.blueprint.component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;
import java.util.Collections;

@WebListener
public class SetTrackingModeListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    sce.getServletContext().setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
  }
}