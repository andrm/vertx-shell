/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.shell.io;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
/**
 * A pseudo terminal used for controlling a {@link io.vertx.groovy.ext.shell.io.Tty}. This interface acts as a pseudo
 * terminal master, {@link io.vertx.groovy.ext.shell.io.Pty#slave} returns the assocated slave pseudo terminal.
*/
@CompileStatic
public class Pty {
  private final def io.vertx.ext.shell.io.Pty delegate;
  public Pty(Object delegate) {
    this.delegate = (io.vertx.ext.shell.io.Pty) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a new pseudo terminal with no terminal type.
   * @return 
   */
  public static Pty create() {
    def ret= InternalHelper.safeCreate(io.vertx.ext.shell.io.Pty.create(), io.vertx.groovy.ext.shell.io.Pty.class);
    return ret;
  }
  /**
   * Create a new pseudo terminal.
   * @param terminalType the terminal type, for instance 
   * @return the created pseudo terminal
   */
  public static Pty create(String terminalType) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.shell.io.Pty.create(terminalType), io.vertx.groovy.ext.shell.io.Pty.class);
    return ret;
  }
  /**
   * @return the standard input of the terminal
   * @return 
   */
  public Stream stdin() {
    def ret= InternalHelper.safeCreate(this.delegate.stdin(), io.vertx.groovy.ext.shell.io.Stream.class);
    return ret;
  }
  public Pty setStdout(Stream stdout) {
    def ret= InternalHelper.safeCreate(this.delegate.setStdout((io.vertx.ext.shell.io.Stream)stdout.getDelegate()), io.vertx.groovy.ext.shell.io.Pty.class);
    return ret;
  }
  /**
   * Resize the terminal.
   * @param width 
   * @param height 
   * @return the new standard input of the terminal
   */
  public Pty setSize(int width, int height) {
    this.delegate.setSize(width, height);
    return this;
  }
  /**
   * @return the pseudo terminal slave
   * @return 
   */
  public Tty slave() {
    def ret= InternalHelper.safeCreate(this.delegate.slave(), io.vertx.groovy.ext.shell.io.Tty.class);
    return ret;
  }
}