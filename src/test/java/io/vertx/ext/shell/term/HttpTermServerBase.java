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

package io.vertx.ext.shell.term;

import io.vertx.core.Vertx;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Base64;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class HttpTermServerBase {

  protected Vertx vertx;
  private TermServer server;
  private final String basePath;

  public HttpTermServerBase(String basePath) {
    this.basePath = basePath;
  }

  @Before
  public void before() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  protected abstract TermServer createServer(TestContext context, HttpTermOptions options);

  @Test
  public void testServerWrite(TestContext context) {
    Async async = context.async();
    server = createServer(context, new HttpTermOptions().setPort(8080));
    server.termHandler(term -> {
      term.stdout().write("hello_from_server");
    });
    server.listen(context.asyncAssertSuccess(server -> {
      HttpClient client = vertx.createHttpClient();
      client.websocket(8080, "localhost", basePath + "/shell/websocket", ws -> {
        ws.handler(buf -> {
          context.assertEquals("hello_from_server", buf.toString());
          async.complete();
        });
      }, context::fail);
    }));
  }

  @Test
  public void testServerRead(TestContext context) {
    Async async = context.async();
    server = createServer(context, new HttpTermOptions().setPort(8080));
    server.termHandler(term -> {
      term.setStdin(buf -> {
        context.assertEquals("hello_from_client", buf);
        async.complete();
      });
    });
    server.listen(context.asyncAssertSuccess(server -> {
      HttpClient client = vertx.createHttpClient();
      client.websocket(8080, "localhost", basePath + "/shell/websocket", ws -> {
        ws.writeFinalTextFrame(new JsonObject().put("action", "read").put("data", "hello_from_client").encode());
      }, context::fail);
    }));
  }

  @Test
  public void testInitialSize(TestContext context) {
    testSize(context, basePath + "/shell/websocket?cols=100&rows=50", 100, 50);
  }

  @Test
  public void testInitialSizeCols(TestContext context) {
    testSize(context, basePath + "/shell/websocket?cols=100", 100, 24);
  }

  @Test
  public void testInitialSizeRows(TestContext context) {
    testSize(context, basePath + "/shell/websocket?rows=50", 80, 50);
  }

  @Test
  public void testInitialSizeNegative1(TestContext context) {
    testSize(context, basePath + "/shell/websocket?cols=100&rows=-50", 80, 24);
  }

  @Test
  public void testInitialSizeNegative2(TestContext context) {
    testSize(context, basePath + "/shell/websocket?cols=-100&rows=50", 80, 24);
  }

  @Test
  public void testInitialSizeNegative3(TestContext context) {
    testSize(context, basePath + "/shell/websocket?rows=-50", 80, 24);
  }

  @Test
  public void testInitialSizeNegative4(TestContext context) {
    testSize(context, basePath + "/shell/websocket?cols=-100", 80, 24);
  }

  @Test
  public void testInitialSizeInvalid1(TestContext context) {
    testSize(context, basePath + "/shell/websocket?cols=100&rows=abc", 80, 24);
  }

  @Test
  public void testInitialSizeInvalid2(TestContext context) {
    testSize(context, basePath + "/shell/websocket?cols=abc&rows=50", 80, 24);
  }

  @Test
  public void testInitialSizeInvalid3(TestContext context) {
    testSize(context, basePath + "/shell/websocket?rows=abc", 80, 24);
  }

  @Test
  public void testInitialSizeInvalid4(TestContext context) {
    testSize(context, basePath + "/shell/websocket?cols=abc", 80, 24);
  }

  private void testSize(TestContext context, String uri, int expectedCols, int expectedRows) {
    Async async = context.async();
    server = createServer(context, new HttpTermOptions().setPort(8080));;
    server.termHandler(term -> {
      context.assertEquals(expectedCols, term.width());
      context.assertEquals(expectedRows, term.height());
      async.complete();
    });
    server.listen(context.asyncAssertSuccess(server -> {
      HttpClient client = vertx.createHttpClient();
      client.websocket(8080, "localhost", uri, ws -> {
      }, context::fail);
    }));
  }

  @Test
  public void testResize(TestContext context) {
    testResize(context, new JsonObject().put("action", "resize").put("cols", 100).put("rows", 50), 100, 50);
  }

  @Test
  public void testResizeCols(TestContext context) {
    testResize(context, new JsonObject().put("action", "resize").put("cols", 100), 100, 24);
  }

  @Test
  public void testResizeRows(TestContext context) {
    testResize(context, new JsonObject().put("action", "resize").put("rows", 50), 80, 50);
  }

  private void testResize(TestContext context, JsonObject event, int expectedCols, int expectedRows) {
    Async async = context.async();
    server = createServer(context, new HttpTermOptions().setPort(8080));;
    server.termHandler(term -> {
      term.resizehandler(v -> {
        context.assertEquals(expectedCols, term.width());
        context.assertEquals(expectedRows, term.height());
        async.complete();
      });
    });
    server.listen(context.asyncAssertSuccess(server -> {
      HttpClient client = vertx.createHttpClient();
      client.websocket(8080, "localhost", basePath + "/shell/websocket", ws -> {
        ws.writeFinalTextFrame(event.encode());
      }, context::fail);
    }));
  }

  @Test
  public void testResizeInvalid(TestContext context) {
    Async async = context.async();
    server = createServer(context, new HttpTermOptions().setPort(8080));;
    server.termHandler(term -> {
      term.resizehandler(v -> {
        context.fail();
      });
    });
    server.listen(context.asyncAssertSuccess(server -> {
      HttpClient client = vertx.createHttpClient();
      client.websocket(8080, "localhost", basePath + "/shell/websocket", ws -> {
        ws.writeFinalTextFrame(new JsonObject().put("action", "resize").put("cols", -50).encode());
        vertx.setTimer(1000, id -> {
          async.complete();
        });
      }, context::fail);
    }));
  }

  @Test
  public void testSecure(TestContext context) {
    Async async = context.async();
    server = createServer(context, new HttpTermOptions().setShiroAuthOptions(
        new ShiroAuthOptions().
            setType(ShiroAuthRealmType.PROPERTIES).
            setConfig(new JsonObject().put("properties_path", "classpath:test-auth.properties"))).setPort(8080));
    server.termHandler(term -> {
      term.stdout().write("hello");
    });
    server.listen(context.asyncAssertSuccess(server -> {
      HttpClient client = vertx.createHttpClient();
      client.websocket(8080, "localhost", basePath + "/shell/websocket", ws -> {
        context.fail();
      }, err -> {
        // Retry now with auth
        client.websocket(8080, "localhost", basePath + "/shell/websocket",
            new CaseInsensitiveHeaders().add("Authorization", "Basic " + Base64.getEncoder().encodeToString("tim:sausages".getBytes())),
            ws -> {
              ws.handler(buf -> {
                context.assertEquals("hello", buf.toString());
                async.complete();
              });
            }, context::fail);
      });
    }));
  }
}