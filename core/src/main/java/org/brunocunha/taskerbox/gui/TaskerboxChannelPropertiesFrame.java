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
package org.brunocunha.taskerbox.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import lombok.extern.log4j.Log4j;

import org.brunocunha.taskerbox.core.TaskerboxChannel;
import org.brunocunha.taskerbox.core.annotation.TaskerboxField;
import org.brunocunha.taskerbox.core.utils.TaskerboxReflectionUtils;
import org.brunocunha.taskerbox.gui.components.JSetterTextField;
import org.brunocunha.taskerbox.impl.twitter.TwitterChannel;
import org.brunocvcunha.inutils4j.MyStringUtils;

@Log4j
public class TaskerboxChannelPropertiesFrame extends JFrame {

  private JPanel contentPane;

  private List<JSetterTextField> setters = new ArrayList<JSetterTextField>();

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    final TwitterChannel twitter = new TwitterChannel();
    twitter.setId("twitter");
    twitter.setConsumerKey("aaa");

    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          TaskerboxChannelPropertiesFrame frame =
              new TaskerboxChannelPropertiesFrame(null, twitter);
          frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   * 
   * @throws IntrospectionException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public TaskerboxChannelPropertiesFrame(final TaskerboxControlFrame frame,
      final TaskerboxChannel<?> channel) throws IntrospectionException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    setTitle("Propriedades do Canal " + channel.getId());

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(100, 100, 450, 300);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);

    JPanel panel = new JPanel();
    contentPane.add(panel, BorderLayout.NORTH);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    contentPane.add(tabbedPane, BorderLayout.CENTER);

    JPanel panel_2 = new JPanel();
    FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
    flowLayout.setHgap(100);
    flowLayout.setAlignment(FlowLayout.LEADING);
    tabbedPane.addTab("Propriedades", null, panel_2, null);

    for (Field field : channel.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(TaskerboxField.class)) {
        TaskerboxField annot = field.getAnnotation(TaskerboxField.class);

        log.info("Adding field " + field.getName());

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BorderLayout());
        JLabel label = new JLabel(annot.value() + ": ");
        fieldPanel.add(label, BorderLayout.WEST);

        PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), channel.getClass());
        Method readMethod = descriptor.getReadMethod();
        Method writeMethod =
            new PropertyDescriptor(field.getName(), channel.getClass()).getWriteMethod();

        JSetterTextField textField = new JSetterTextField(writeMethod);
        textField.setColumns(10);

        if (annot.readOnly()) {
          textField.setEditable(false);
        }

        Object value = readMethod.invoke(channel);

        if (value instanceof String[]) {
          String[] array = (String[]) value;
          textField.setText(MyStringUtils.join(array, ","));
        } else {
          textField.setText(String.valueOf(value));
        }

        fieldPanel.add(textField, BorderLayout.EAST);

        setters.add(textField);
        panel_2.add(fieldPanel);
      }
    }

    JPanel panel_1 = new JPanel();
    contentPane.add(panel_1, BorderLayout.SOUTH);

    JButton btnSalvar = new JButton("Salvar");
    btnSalvar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {


        for (JSetterTextField setter : setters) {
          log.info("Setting field " + setter.getSetter().getName() + ": " + setter.getText());
          channel.getPropertyBag().put(setter.getSetter().getName(), setter.getText());
          try {
            TaskerboxReflectionUtils.invokeSetter(setter.getSetter(), channel, setter.getText());
          } catch (IllegalAccessException e1) {
            e1.printStackTrace();
          } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
          } catch (InvocationTargetException e1) {
            e1.printStackTrace();
          } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
          }
        }

        frame.updateChannels();
      }

    });

    panel_1.add(btnSalvar);
  }


}
