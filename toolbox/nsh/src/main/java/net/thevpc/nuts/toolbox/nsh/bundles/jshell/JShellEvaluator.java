/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

/**
 *
 * @author thevpc
 */
public interface JShellEvaluator {

    int evalSuffixOperation(String opString, JShellCommandNode node, JShellFileContext context);

    int evalSuffixAndOperation(JShellCommandNode node, JShellFileContext context);

    int evalBinaryAndOperation(JShellCommandNode left, JShellCommandNode right, JShellFileContext context);

    int evalBinaryOperation(String opString, JShellCommandNode left, JShellCommandNode right, JShellFileContext context);

    int evalBinaryOrOperation(JShellCommandNode left, JShellCommandNode right, JShellFileContext context);

    int evalBinaryPipeOperation(JShellCommandNode left, JShellCommandNode right, final JShellFileContext context);

    int evalBinarySuiteOperation(JShellCommandNode left, JShellCommandNode right, JShellFileContext context);

    String evalCommandAndReturnString(JShellCommandNode left, JShellFileContext context);


    String evalDollarSharp(JShellFileContext context);

    String evalDollarName(String name, JShellFileContext context);

    String evalDollarInterrogation(JShellFileContext context);

    String evalDollarInteger(int index, JShellFileContext context);

    String evalDollarExpression(String stringExpression, JShellFileContext context);

    String evalSimpleQuotesExpression(String expressionString, JShellFileContext context);

    String evalDoubleQuotesExpression(String stringExpression, JShellFileContext context);

    String evalAntiQuotesExpression(String stringExpression, JShellFileContext context);

    String evalNoQuotesExpression(String stringExpression, JShellFileContext context);

    String expandEnvVars(String stringExpression, boolean escapeResultPath, JShellFileContext context);

}
