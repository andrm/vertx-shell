require 'vertx-shell/job'
require 'vertx-shell/cli_token'
require 'vertx-shell/session'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.shell.system.ShellSession
module VertxShell
  #  An interactive session between a consumer and a shell.<p/>
  class ShellSession
    # @private
    # @param j_del [::VertxShell::ShellSession] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxShell::ShellSession] the underlying java delegate
    def j_del
      @j_del
    end
    #  @return the user session
    # @return [::VertxShell::Session]
    def session
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:session, []).call(),::VertxShell::Session)
      end
      raise ArgumentError, "Invalid arguments when calling session()"
    end
    #  @return the jobs active in this session
    # @return [Set<::VertxShell::Job>]
    def jobs
      if !block_given?
        return ::Vertx::Util::Utils.to_set(@j_del.java_method(:jobs, []).call()).map! { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxShell::Job) }
      end
      raise ArgumentError, "Invalid arguments when calling jobs()"
    end
    #  Returns an active job in this session by its .
    # @param [Fixnum] id the job id
    # @return [::VertxShell::Job] the job of  when not found
    def get_job(id=nil)
      if id.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:getJob, [Java::int.java_class]).call(id),::VertxShell::Job)
      end
      raise ArgumentError, "Invalid arguments when calling get_job(id)"
    end
    #  @see #createJob(List)
    # @overload createJob(line)
    #   @param [Array<::VertxShell::CliToken>] line the command line creating this job
    # @overload createJob(line)
    #   @param [String] line 
    # @return [::VertxShell::Job]
    def create_job(param_1=nil)
      if param_1.class == Array && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:createJob, [Java::JavaUtil::List.java_class]).call(param_1.map { |element| element.j_del }),::VertxShell::Job)
      elsif param_1.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:createJob, [Java::java.lang.String.java_class]).call(param_1),::VertxShell::Job)
      end
      raise ArgumentError, "Invalid arguments when calling create_job(param_1)"
    end
  end
end