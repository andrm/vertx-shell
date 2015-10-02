/*
 * Copyright 2015 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *
 * Copyright (c) 2015 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 *
 */

package io.vertx.ext.shell.command.base;

import io.vertx.core.Vertx;
import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandProcess;
import io.vertx.ext.shell.io.EventType;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Name("sleep")
@Summary("Suspend execution for an interval of time")
public class Sleep implements Command {

  private String seconds;

  @Description("the number of seconds to wait")
  @Argument(index = 0, argName = "seconds")
  public void setSeconds(String seconds) {
    this.seconds = seconds;
  }

  @Override
  public void process(CommandProcess process) {
    int timeout = -1;
    try {
      timeout = Integer.parseInt(seconds);
    } catch (NumberFormatException ignore) {
    }
    scheduleSleep(process, timeout * 1000L);
  }

  private void scheduleSleep(CommandProcess process, long millis) {
    Vertx vertx = process.vertx();
    if (millis > 0) {
      long now = System.currentTimeMillis();
      AtomicLong remaining = new AtomicLong(-1);
      long id = process.vertx().setTimer(millis, v -> {
        process.end();
      });
      process.eventHandler(EventType.SIGINT, v -> {
        if (vertx.cancelTimer(id)) {
          process.end();
        }
      });
      process.eventHandler(EventType.SIGTSTP, v -> {
        if (vertx.cancelTimer(id)) {
          remaining.set(millis - (System.currentTimeMillis() - now));
        }
      });
      process.eventHandler(EventType.SIGCONT, v -> {
        scheduleSleep(process, remaining.get());
      });
    } else {
      process.end();
    }
  }
}
