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
package org.brunocvcunha.taskerbox.impl.quartz;

import org.brunocvcunha.taskerbox.core.TaskerboxChannel;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.extern.log4j.Log4j;

@Log4j
public class QuartzChannelJob implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Channel Job Invoked");

    TaskerboxChannel<Long> originChannel =
        (TaskerboxChannel<Long>) context.getJobDetail().getJobDataMap().get("channel");
    originChannel.perform(System.nanoTime());
  }
}
