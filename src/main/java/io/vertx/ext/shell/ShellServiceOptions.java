package io.vertx.ext.shell;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.net.SSHOptions;
import io.vertx.ext.shell.net.TelnetOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class ShellServiceOptions {

  public static final String DEFAULT_WELCOME_MESSAGE;

  static {
    String welcome = "Welcome to Vert.x Shell";
    InputStream in = ShellServiceOptions.class.getResourceAsStream("vertx-banner.txt");
    if (in != null) {
      ByteArrayOutputStream tmp = new ByteArrayOutputStream();
      byte[] buf = new byte[256];
      try {
        while (true) {
          int len = in.read(buf);
          if (len == -1) {
            break;
          }
          tmp.write(buf, 0, len);
        }
        welcome = tmp.toString();
      } catch (Exception ignore) {
        // Could not load
      }
    }
    DEFAULT_WELCOME_MESSAGE = welcome + "\n\n";
  }

  private String welcomeMessage;
  private TelnetOptions telnet;
  private SSHOptions ssh;

  public ShellServiceOptions() {
    welcomeMessage = DEFAULT_WELCOME_MESSAGE;
  }

  public ShellServiceOptions(ShellServiceOptions that) {
    this.telnet = that.telnet != null ? new TelnetOptions(that.telnet) : null;
    this.welcomeMessage = that.welcomeMessage;
  }

  public ShellServiceOptions(JsonObject json) {
    welcomeMessage = json.getString("welcomeMessage", DEFAULT_WELCOME_MESSAGE);
    telnet = json.getJsonObject("telnet") != null ? new TelnetOptions(json.getJsonObject("telnet")) : null;
    ssh = json.getJsonObject("ssh") != null ? new SSHOptions(json.getJsonObject("ssh")) : null;
  }

  public String getWelcomeMessage() {
    return welcomeMessage;
  }

  public ShellServiceOptions setWelcomeMessage(String welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
    return this;
  }

  public TelnetOptions getTelnet() {
    return telnet;
  }

  public ShellServiceOptions setTelnet(TelnetOptions telnet) {
    this.telnet = telnet;
    return this;
  }

  public SSHOptions getSSH() {
    return ssh;
  }

  public ShellServiceOptions setSSH(SSHOptions ssh) {
    this.ssh = ssh;
    return this;
  }
}
