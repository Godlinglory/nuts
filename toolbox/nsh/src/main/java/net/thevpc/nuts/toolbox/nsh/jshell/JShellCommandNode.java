package net.thevpc.nuts.toolbox.nsh.jshell;

public interface JShellCommandNode extends JShellNode {
    int eval(JShellContext context);
}