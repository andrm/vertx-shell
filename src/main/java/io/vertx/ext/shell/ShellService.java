package io.vertx.ext.shell;

import io.termd.core.tty.TtyConnection;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.shell.command.BaseCommands;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.impl.SSHServer;
import io.vertx.ext.shell.impl.TelnetServer;
import io.vertx.ext.shell.registry.CommandRegistry;
import io.vertx.ext.shell.impl.Shell;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface ShellService {

  static ShellService create(Vertx vertx, ShellServiceOptions options) {
    CommandRegistry mgr = CommandRegistry.get(vertx);
    mgr.registerCommand(BaseCommands.echo());
    mgr.registerCommand(BaseCommands.fs_cd());
    mgr.registerCommand(BaseCommands.fs_pwd());
    mgr.registerCommand(BaseCommands.fs_ls());
    mgr.registerCommand(BaseCommands.sleep());
    mgr.registerCommand(BaseCommands.help());
    mgr.registerCommand(BaseCommands.server_ls());
    mgr.registerCommand(BaseCommands.local_map_get());
    mgr.registerCommand(BaseCommands.local_map_put());
    mgr.registerCommand(BaseCommands.local_map_rm());
    mgr.registerCommand(BaseCommands.bus_send());
    mgr.registerCommand(BaseCommands.bus_tail());
    mgr.registerCommand(BaseCommands.verticle_ls());
    mgr.registerCommand(BaseCommands.verticle_deploy());
    mgr.registerCommand(BaseCommands.verticle_undeploy());
    mgr.registerCommand(BaseCommands.verticle_factories());

    // Register builtin commands so they are listed in help
    mgr.registerCommand(Command.command("exit").processHandler(process -> {}));
    mgr.registerCommand(Command.command("logout").processHandler(process -> {}));
    mgr.registerCommand(Command.command("jobs").processHandler(process -> {}));
    mgr.registerCommand(Command.command("fg").processHandler(process -> {}));
    mgr.registerCommand(Command.command("bg").processHandler(process -> {}));

    Consumer<TtyConnection> shellBoostrap = conn -> {
      Shell shell = new Shell(vertx, conn, mgr);
      conn.setCloseHandler(v -> {
        shell.close();
      });
      shell.setWelcome(options.getWelcomeMessage());
      shell.init();
    };

    //
    return new ShellService() {
      TelnetServer telnet;
      SSHServer ssh;
      @Override
      public void start() throws Exception {
        TelnetOptions telnetOptions = options.getTelnet();
        if (telnetOptions != null) {
          telnet = new TelnetServer(telnetOptions);
          telnet.setHandler(shellBoostrap);
          telnet.listen(vertx);
        }
        SSHOptions sshOptions = options.getSSH();
        if (sshOptions != null) {
          ssh = new SSHServer(sshOptions);
          ssh.setHandler(shellBoostrap);
          ssh.listen(vertx);
        }
      }

      @Override
      public void close() {
        if (telnet != null) {
          telnet.close();
        }
        if (ssh != null) {
          ssh.close();
        }
      }
    };
  }

  void start() throws Exception;

  void close();

}
