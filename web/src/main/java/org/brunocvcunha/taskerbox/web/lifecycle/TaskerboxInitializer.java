/**
 * Copyright (C) 2015 Bruno Candido Volpato da Cunha (brunocvcunha@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brunocvcunha.taskerbox.web.lifecycle;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.brunocunha.taskerbox.Taskerbox;
import org.brunocunha.taskerbox.core.TaskerboxChannel;

@WebListener
public class TaskerboxInitializer implements ServletContextListener {

  private static final Logger log = Logger.getLogger(TaskerboxInitializer.class.getSimpleName());

  private static Taskerbox taskerboxInstance;

  public static Taskerbox getInstance() {
    if (taskerboxInstance == null) {
      throw new IllegalStateException("Taskerbox was not initialized.");
    }

    return taskerboxInstance;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    log.info("Initializating Taskerbox...");
    
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      log.info("Host name: " + hostName);

      Taskerbox tasker = new Taskerbox();
      log.info("Handling macros...");
      tasker.handleTaskerbox("macros.xml");

      String taskerboxDir = System.getProperty("taskerbox.dir", System.getProperty("user.home") + "/Dropbox/Taskerbox");
      File taskerboxDirFile = new File(taskerboxDir);
      
      File hostNameFile = new File(taskerbox, "taskerbox-" + hostName + ".xml");
      log.info("Handling hostName file: " + hostNameURL);
      if (hostNameURL != null) {
        tasker.handleTaskerbox(hostNameURL);
      }

      URL commonURL = sce.getServletContext().getResource("/WEB-INF/classes/web-taskerbox.xml");
      log.info("Handling common file: " + commonURL);
      if (commonURL != null) {
        tasker.handleTaskerbox(commonURL);
      }
      
      taskerboxInstance = tasker;
    } catch (Exception e) {
      throw new RuntimeException("Exception initializing taskerbox", e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    log.info("Destroying Taskerbox...");
    
    if (taskerboxInstance != null) {
      
      for (TaskerboxChannel<?> channel : taskerboxInstance.getChannels()) {
        channel.getScheduler().shutdown();
      }
      
    }
    
  }


}