package org.dlangplugin.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.dlangplugin.config.DLangGeneralSettings;
import org.dlangplugin.run.exception.ModuleNotFoundException;
import org.dlangplugin.run.exception.NoDubExecutableException;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class DLangRunDubState extends CommandLineState {
    private DLangRunDubConfiguration config;
    private Executor executor;

    protected DLangRunDubState(@NotNull ExecutionEnvironment environment, @NotNull DLangRunDubConfiguration config) {
        super(environment);
        this.config = config;
    }

    @NotNull
    @Override
    public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        TextConsoleBuilder consoleBuilder = new TextConsoleBuilderImpl(config.getProject());
        setConsoleBuilder(consoleBuilder);
        this.executor = executor;
        return super.execute(executor, runner);
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        try {
            GeneralCommandLine dubCommandLine = getExecutableCommandLine(config);
            return new OSProcessHandler(dubCommandLine.createProcess(), dubCommandLine.getCommandLineString());
        }
        catch (ModuleNotFoundException e) {
            throw new ExecutionException("Run configuration has no module selected.");
        }
        catch (NoDubExecutableException e) {
            throw new ExecutionException("DUB executable is not specified.<a href='configure'>Configure</a> DUB settings");
        }
        catch (ExecutionException e) {
            String message = e.getMessage();
            boolean isEmpty = message.equals("Executable is not specified");
            boolean notCorrect = message.startsWith("Cannot run program");
            if (isEmpty || notCorrect) {
                Notifications.Bus.notify(
                        new Notification("DUB run configuration", "DUB settings",
                                "DUB executable path is " + (isEmpty ? "empty" : "not specified correctly") +
                                        "<br/><a href='configure'>Configure</a> output folder",
                                NotificationType.ERROR), config.getProject());
            }
            throw e;
        }
    }

    /* Build command line to start DUB executable
     */
    private GeneralCommandLine getExecutableCommandLine(DLangRunDubConfiguration config)
            throws ModuleNotFoundException, NoDubExecutableException
    {
        Module module = config.getConfigurationModule().getModule();
        if(module == null) {
            throw new ModuleNotFoundException();
        }

        DLangGeneralSettings generalSettings = DLangGeneralSettings.getInstance(config.getProject());
        if( StringUtil.isEmptyOrSpaces(generalSettings.getDubExecutablePath())) {
            throw new NoDubExecutableException();
        }

        VirtualFile sourcesRoot = getSourceRoot(module);
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(generalSettings.getDubExecutablePath());

        if( !StringUtil.isEmptyOrSpaces( config.getWorkingDir()) ) {
            commandLine.withWorkDirectory(config.getWorkingDir());
        }
        else {
            commandLine.withWorkDirectory(config.getProject().getBasePath());
        }

        //Add command line parameters
        if( !config.isRunAfterBuild() ) {
            commandLine.addParameter("build");
        }
        if( config.isQuiet() ) {
            commandLine.addParameter("-q");
        }
        if( config.isVerbose() ) {
            commandLine.addParameter("-v");
        }

        if( StringUtil.isEmptyOrSpaces(config.getAdditionalParams()) ) {
            commandLine.addParameters(splitArguments(config.getAdditionalParams()));
        }

        return commandLine;
    }

    private String[] splitArguments(String arguments) {
        if( StringUtil.isEmptyOrSpaces(arguments)) {
            return new String[0];
        }

        List<String> argsLst = new LinkedList<String>();
        CommandLineTokenizer tokenizer = new CommandLineTokenizer(arguments);
        while(tokenizer.hasMoreTokens()) {
            argsLst.add(tokenizer.nextToken());
        }
        if(argsLst.size()>0) {
            return (String[]) argsLst.toArray();
        }
        else {
            return new String[0];
        }
    }

    private VirtualFile getSourceRoot(Module module) {
        if (module != null) {
            VirtualFile[] sourcesRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            if (sourcesRoots.length >= 1) {
                return sourcesRoots[0];
            }
        }
        return null;
    }
}
