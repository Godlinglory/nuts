/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.List;

/**
 * Simple Command line parser implementation. The command line supports
 * arguments in the following forms :
 * <ul>
 * <li> non option arguments : any argument that does not start with '-'</li>
 *
 * <li>
 * long option arguments : any argument that starts with a single '--' in the
 * form of
 * <pre>--[//][!]?[^=]*[=.*]</pre>
 * <ul>
 * <li>// means disabling the option</li>
 * <li>! means switching (to 'false') the option's value</li>
 * <li>the string before the '=' is the option's key</li>
 * <li>the string after the '=' is the option's value</li>
 * </ul>
 * Examples :
 * <ul>
 * <li>--!enable : option 'enable' with 'false' value</li>
 * <li>--enable=yes : option 'enable' with 'yes' value</li>
 * <li>--!enable=yes : invalid option (no error will be thrown but the result is
 * undefined)</li>
 * </ul>
 * </li>
 * <li>
 * simple option arguments : any argument that starts with a single '-' in the
 * form of
 * <pre>-[//][!]?[a-z][=.*]</pre> This is actually very similar to long options
 * <ul>
 * <li>-!enable (with expandSimpleOptions=false) : option 'enable' with 'false'
 * value</li>
 * <li>--enable=yes : option 'enable' with 'yes' value</li>
 * <li>--!enable=yes : invalid option (no error will be thrown but the result is
 * undefined)</li>
 * </ul>
 *
 * </li>
 * <li>
 * condensed simple option arguments : any argument that starts with a single
 * '-' in the form of
 * <pre>-[//]([!]?[a-z])+[=.*]</pre> This is actually very similar to long
 * options and is parsable when expandSimpleOptions=true. When activating
 * expandSimpleOptions, multi characters key will be expanded as multiple
 * separate simple options Examples :
 * <ul>
 * <li>-!enable (with expandSimpleOptions=false) : option 'enable' with 'false'
 * value</li>
 * <li>--enable=yes : option 'enable' with 'yes' value</li>
 * <li>--!enable=yes : invalid option (no error will be thrown but the result is
 * undefined)</li>
 * </ul>
 *
 * </li>
 *
 * <li>long option arguments : any argument that starts with a '--' </li>
 * </ul>
 * option may start with '!' to switch armed flags expandSimpleOptions : when
 * activated
 *
 *
 *
 * @author vpc
 * @since 0.5.5
 * @category Command Line
 */
public interface NutsCommandLine {

    /**
     * set autocomplete instance
     *
     * @param autoComplete autocomplete instance
     * @return {@code this} instance
     */
    NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete);

    /**
     * autocomplete instance
     *
     * @return autocomplete instance
     */
    NutsCommandAutoComplete getAutoComplete();

    /**
     * unregister {@code options} as simple (with simple '-') option. This
     * method helps considering '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option option
     * @return {@code this} instance
     */
    NutsCommandLine unregisterSpecialSimpleOption(String option);

    /**
     * list of registered simple options
     *
     * @return list of registered simple options
     */
    String[] getSpecialSimpleOptions();

    /**
     * register {@code options} as simple (with simple '-') option. This method
     * helps considering '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option option
     * @return {@code this} instance
     */
    NutsCommandLine registerSpecialSimpleOption(String option);

    /**
     * test if the option is a registered simple option This method helps
     * considering '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option option
     * @return {@code this} instance
     */
    boolean isSpecialSimpleOption(String option);

    /**
     * current word index
     *
     * @return current word index
     */
    int getWordIndex();

    /**
     * true if auto complete instance is not registered (is null)
     *
     * @return true if auto complete instance is not registered (is null)
     */
    boolean isExecMode();

    /**
     * true if auto complete instance is registered (is not null)
     *
     * @return true if auto complete instance is registered (is not null)
     */
    boolean isAutoCompleteMode();

    /**
     * @return command name that will be used as an extra info in thrown
     * exceptions
     */
    String getCommandName();

    /**
     * set command name that will be used as an extra info in thrown exceptions
     *
     * @param commandName commandName
     * @return {@code this} instance
     */
    NutsCommandLine setCommandName(String commandName);

    /**
     * true if simple option expansion is enabled
     *
     * @return true if simple option expansion is enabled
     */
    boolean isExpandSimpleOptions();

    /**
     * enable or disable simple option expansion
     *
     * @param expand expand
     * @return {@code this} instance
     */
    NutsCommandLine setExpandSimpleOptions(boolean expand);

    /**
     * throw exception if command line is empty or the first word is an option
     *
     * @return {@code this} instance
     */
    NutsCommandLine requireNonOption();

    /**
     * throw exception if command line is not empty
     *
     * @param errorMessage message to throw
     * @return {@code this} instance
     */
    NutsCommandLine unexpectedArgument(String errorMessage);

    /**
     * throw exception if command line is not empty
     *
     * @return {@code this} instance
     */
    NutsCommandLine unexpectedArgument();

    /**
     * throw exception if command line is empty
     *
     * @return {@code this} instance
     */
    NutsCommandLine required();

    /**
     * throw exception if command line is empty
     *
     * @param errorMessage message to throw
     * @return {@code this} instance
     */
    NutsCommandLine required(String errorMessage);

    /**
     * push back argument so that it will be first to be retrieved (using next
     * methods)
     *
     * @param arg argument
     * @return {@code this} instance
     */
    NutsCommandLine pushBack(NutsArgument arg);

    /**
     * consume (remove) the first argument and return it return null if not
     * argument is left
     *
     * @return next argument
     */
    NutsArgument next();

    /**
     * consume (remove) the first argument and return it while adding a hint to
     * Auto Complete about expected argument candidates return null if not
     * argument is left
     *
     * @param name expected argument name
     * @return next argument
     */
    NutsArgument next(NutsArgumentName name);

    /**
     * the first argument to consume without removing/consuming it or null if
     * not argument is left
     *
     * @return the first argument to consume without removing/consuming it
     */
    NutsArgument peek();

    /**
     * true if there still at least one argument to consume
     *
     * @return true if there still at least one argument to consume
     */
    boolean hasNext();

    /**
     * next argument with boolean value equivalent to
     * next(NutsArgumentType.STRING,names)
     *
     * @param names names
     * @return next argument
     */
    NutsArgument nextBoolean(String... names);

    /**
     * next argument with string value. equivalent to
     * next(NutsArgumentType.STRING,names)
     *
     * @param names names
     * @return next argument
     */
    NutsArgument nextString(String... names);

    /**
     * next argument with any value type (may having not a value). equivalent to
     * {@code next(NutsArgumentType.ANY,names)}
     *
     * @param names names
     * @return next argument
     */
    NutsArgument next(String... names);

    /**
     * next argument with any value type (may having not a value).
     *
     * @param expectValue expected value type
     * @param names names
     * @return next argument
     */
    NutsArgument next(NutsArgumentType expectValue, String... names);

    /**
     * next argument if it exists and it is a non option. Throw an error in all
     * other cases.
     *
     * @param name argument specification (may be null)
     * @return next argument if it exists and it is a non option
     */
    NutsArgument nextRequiredNonOption(NutsArgumentName name);

    /**
     * next argument if it exists and it is a non option. Return null in all
     * other cases.
     *
     * @return next argument if it exists and it is a non option
     */
    NutsArgument nextNonOption();

    /**
     * next argument if it exists and it is a non option. Return null in all
     * other cases.
     *
     * @param name argument specification (may be null)
     * @return next argument if it exists and it is a non option
     */
    NutsArgument nextNonOption(NutsArgumentName name);

    /**
     * consume all words and return consumed count
     *
     * @return consumed count
     */
    int skipAll();

    /**
     * skip next argument
     *
     * @return words count
     */
    int skip();

    /**
     * consume {@code count} words and return how much it was able to consume
     *
     * @param count count
     * @return consumed count
     */
    int skip(int count);

    /**
     * true if arguments start with the given suite.
     *
     * @param values arguments suite
     * @return true if arguments start with the given suite.
     */
    boolean accept(String... values);

    /**
     * true if arguments start at index {@code index} with the given suite.
     *
     * @param index starting index
     * @param values arguments suite
     * @return true if arguments start with the given suite.
     */
    boolean accept(int index, String... values);

    /**
     * find first argument with argument key name
     *
     * @param name argument key name
     * @return find first argument with argument key name
     */
    NutsArgument find(String name);

    /**
     * return argument at given index
     *
     * @param index argument index
     * @return argument at given index
     */
    NutsArgument get(int index);

    /**
     * return true if any argument is equal to the given name
     *
     * @param name argument name
     * @return true if any argument is equal to the given name
     */
    boolean contains(String name);

    /**
     * first argument index (or -1 if not found) with value {@code name}
     *
     * @param name argument key name
     * @return first argument index (or -1 if not found) with value {@code name}
     */
    int indexOf(String name);

    /**
     * number of arguments available to retrieve
     *
     * @return number of arguments available to retrieve
     */
    int length();

    /**
     * true if no more arguments are available
     *
     * @return true if no more arguments are available
     */
    boolean isEmpty();

    /**
     * returns un-parsed (or partially parsed) available arguments
     *
     * @return returns un-parsed (or partially parsed) available arguments
     */
    String[] toArray();

    /**
     * true if the argument and index exists and is option
     *
     * @param index index
     * @return true if the argument and index exists and is option
     */
    boolean isOption(int index);

    /**
     * true if the argument and index exists and is non option
     *
     * @param index index
     * @return true if the argument and index exists and is non option
     */
    boolean isNonOption(int index);

    /**
     * reset this instance with the given parsed arguments
     *
     * @param commandLine to parse
     * @return reset this instance with the given parsed arguments
     */
    NutsCommandLine parseLine(String commandLine);

    /**
     * reset this instance with the given arguments
     *
     * @param arguments to parse
     * @return reset this instance with the given arguments
     */
    NutsCommandLine setArguments(List<String> arguments);

    /**
     * reset this instance with the given arguments
     *
     * @param arguments to parse
     * @return reset this instance with the given arguments
     */
    NutsCommandLine setArguments(String[] arguments);

    void throwError(String message);

    /**
     * run the processor and fall back to defaultConfigurable
     * @param defaultConfigurable default configurable
     * @param processor processor 
     */
    void process(NutsConfigurable defaultConfigurable, NutsCommandLineProcessor processor);

}
