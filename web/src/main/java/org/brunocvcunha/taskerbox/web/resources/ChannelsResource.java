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
package org.brunocvcunha.taskerbox.web.resources;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.brunocunha.taskerbox.Taskerbox;
import org.brunocunha.taskerbox.core.TaskerboxChannel;
import org.brunocunha.taskerbox.core.annotation.TaskerboxField;
import org.brunocvcunha.taskerbox.web.lifecycle.TaskerboxInitializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Resource that is used to control Channels
 * 
 * @author Bruno Candido Volpato da Cunha
 * 
 */
@Path("/channels")
public class ChannelsResource {

  /**
   * Logger
   */
  private static final Logger log = Logger.getLogger(ChannelsResource.class.getSimpleName());


  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response handleRequest() {
    log.info("Listing...");

    try {

      Taskerbox tasker = TaskerboxInitializer.getInstance();

      JsonArray array = new JsonArray();

      for (TaskerboxChannel<?> channel : tasker.getChannels()) {
        JsonObject jsonChannel = getChannelJson(channel);


        log.info("Added Channel to Return: " + jsonChannel.toString());

        array.add(jsonChannel);
      }

      return Response.ok().entity(array.toString()).build();
    } catch (Exception e) {
      log.warn("Error in request: " + e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }



  @GET
  @Path("/{channel}/pause")
  @Produces(MediaType.APPLICATION_JSON)
  public Response handlePause(@PathParam("channel") String channelName) {
    log.info("Pausing channel " + channelName);
    return handlePauseStatus(channelName, true);

  }

  @GET
  @Path("/{channel}/unpause")
  @Produces(MediaType.APPLICATION_JSON)
  public Response handleUnpause(@PathParam("channel") String channelName) {
    log.info("Unausing channel " + channelName);
    return handlePauseStatus(channelName, false);

  }

  @GET
  @Path("/{channel}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response handleChannel(@PathParam("channel") String channelName) {
    log.info("Getting channel " + channelName);
    try {

      Taskerbox tasker = TaskerboxInitializer.getInstance();

      for (TaskerboxChannel<?> channel : tasker.getChannels()) {

        if (channel.getId().equalsIgnoreCase(channelName)) {
          return Response.ok().entity(getChannelJson(channel).toString()).build();
        }

      }

      return Response.status(Status.BAD_REQUEST).entity("Not found channel " + channelName).build();
    } catch (Exception e) {
      log.warn("Error in request: " + e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

  }

  @GET
  @Path("/{channel}/force")
  @Produces(MediaType.APPLICATION_JSON)
  public Response handleForce(@PathParam("channel") String channelName) {
    log.info("Forcing channel " + channelName);
    try {

      Taskerbox tasker = TaskerboxInitializer.getInstance();

      for (TaskerboxChannel<?> channel : tasker.getChannels()) {

        if (channel.getId().equalsIgnoreCase(channelName)) {
          channel.check(channel.isPaused());
          return Response.ok().entity(getChannelJson(channel).toString()).build();
        }

      }

      return Response.status(Status.BAD_REQUEST).entity("Not found channel " + channelName).build();
    } catch (Exception e) {
      log.warn("Error in request: " + e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

  }

  private Response handlePauseStatus(String channelName, boolean paused) {
    try {

      Taskerbox tasker = TaskerboxInitializer.getInstance();


      for (TaskerboxChannel<?> channel : tasker.getChannels()) {

        if (channel.getId().equalsIgnoreCase(channelName)) {
          channel.setPaused(paused);
          return Response.ok().entity(getChannelJson(channel).toString()).build();
        }

      }

      return Response.status(Status.BAD_REQUEST).entity("Not found channel " + channelName).build();
    } catch (Exception e) {
      log.warn("Error in request: " + e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }


  private JsonObject getChannelJson(TaskerboxChannel<?> channel) throws IntrospectionException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    JsonObject jsonChannel = new JsonObject();
    jsonChannel.addProperty("id", channel.getId());
    jsonChannel.addProperty("displayName", channel.getDisplayName());
    jsonChannel.addProperty("daemon", channel.isDaemon());
    jsonChannel.addProperty("running", channel.isRunning());
    jsonChannel.addProperty("paused", channel.isPaused());
    jsonChannel.addProperty("checkCount", channel.getCheckCount());
    jsonChannel.addProperty("every", channel.getEvery());
    jsonChannel.addProperty("timeout", channel.getTimeout());



    JsonObject channelData = new JsonObject();

    for (Field field : channel.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(TaskerboxField.class)) {

        PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), channel.getClass());
        Method readMethod = descriptor.getReadMethod();

        Object dataVal = readMethod.invoke(channel);
        if (dataVal != null) {
          if (dataVal instanceof String || dataVal instanceof Character) {
            channelData.addProperty(field.getName(), (String) dataVal);
          } else if (dataVal instanceof Number) {
            channelData.addProperty(field.getName(), (Number) dataVal);
          } else if (dataVal instanceof Boolean) {
            channelData.addProperty(field.getName(), (Boolean) dataVal);
          } else if (dataVal.getClass().isArray()) {
            JsonArray dataArray = new JsonArray();

            for (Object obj : (Object[]) dataVal) {
              dataArray.add(new JsonPrimitive(obj.toString()));
            }

            channelData.add(field.getName(), dataArray);
          } else {
            channelData.addProperty(field.getName(), dataVal.toString());
          }
        }
      }
    }

    jsonChannel.add("data", channelData);
    return jsonChannel;

  }

}