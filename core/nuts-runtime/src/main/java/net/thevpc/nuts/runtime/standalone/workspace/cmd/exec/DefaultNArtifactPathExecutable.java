/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNArtifactPathExecutable extends AbstractNExecutableCommand implements Closeable {

    private final NLog LOG;
    String cmdName;
    String[] args;
    List<String> executorOptions;
    List<String> workspaceOptions;
    NExecutionType executionType;
    NRunAs runAs;
    DefaultNExecCommand execCommand;
    DefaultNDefinition nutToRun;
    CharacterizedExecFile c;
    String tempFolder;
    DefaultNExecCommand.NExecutorComponentAndContext executorComponentAndContext;

    public DefaultNArtifactPathExecutable(String cmdName, String[] args, List<String> executorOptions, List<String> workspaceOptions,
                                          NExecutionType executionType, NRunAs runAs, DefaultNExecCommand execCommand,
                                          DefaultNDefinition nutToRun,
                                          CharacterizedExecFile c,
                                          String tempFolder,
                                          DefaultNExecCommand.NExecutorComponentAndContext executorComponentAndContext
    ) {
        super(cmdName,
                NCmdLine.of(args).toString(),
                NExecutableType.ARTIFACT, execCommand);
        LOG = NLog.of(DefaultNArtifactPathExecutable.class, getSession());
        this.c = c;
        this.tempFolder = tempFolder;
        this.runAs = runAs;
        this.nutToRun = nutToRun;
        this.cmdName = cmdName;
        this.args = args;
        this.executionType = executionType;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions;
        this.workspaceOptions = workspaceOptions;
        this.executorComponentAndContext = executorComponentAndContext;
    }

    @Override
    public NId getId() {
        return this.nutToRun.getId();
    }

    @Override
    public int execute() {
        return executeHelper();
    }

    public int executeHelper() {
        try {
            return executorComponentAndContext.component.exec(executorComponentAndContext.executionContext);
        } finally {
            dispose();
        }
    }

    @Override
    public void close() {
        dispose();
    }

    private void dispose() {
        NSession session = getExecCommand().getSession();
        if (tempFolder != null) {
            try {
                CoreIOUtils.delete(session, Paths.get(tempFolder));
            } catch (UncheckedIOException | NIOException e) {
                LOG.with().session(session).level(Level.FINEST).verb(NLogVerb.FAIL)
                        .log(NMsg.ofJ("unable to delete temp folder created for execution : {0}", tempFolder));
            }
        }
        c.close();
    }

    @Override
    public String toString() {
        return "nuts " + cmdName + " " + NCmdLine.of(args).toString();
    }

}
