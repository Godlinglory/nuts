/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

import java.util.NoSuchElementException;

/**
 * Command Line Argument
 *
 * @author thevpc
 * @since 0.5.5
 * @category Command Line
 */
public interface NutsArgument extends NutsTokenFilter {

    /**
     * true if the argument starts with '-' or '+'
     *
     * @return true if the argument starts with '-'
     */
    boolean isOption();

    /**
     * true if the argument do not start with '-' or '+' or is blank. this is
     * equivalent to {@code !isOption()}.
     *
     * @return true if the argument do not start with '-' or '+'
     */
    boolean isNonOption();

    /**
     * string representation of the argument or null
     *
     * @return string representation of the argument or null
     */
    String getString();

    /**
     * string representation of the argument or the given defaultValue
     *
     * @param defaultValue returned when this argument references null value
     * @return string representation of the argument or the given defaultValue
     */
    String getString(String defaultValue);

    /**
     * true if option is in one of the following forms :
     * <ul>
     * <li>-!name[=...]</li>
     * <li>--!name[=...]</li>
     * <li>!name[=...]</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is negated
     */
    boolean isNegated();

    /**
     * false if option is in one of the following forms :
     * <ul>
     * <li>-//name</li>
     * <li>--//name</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is enable and false if it is commented
     */
    boolean isEnabled();

    /**
     * test if the argument is valid integer
     *
     * @return true if the argument is valid integer
     */
    boolean isInt();

    /**
     * parse number and return integer.
     *
     * @return parsed integer or error
     * @throws NumberFormatException if not parsable
     */
    int getInt() throws NumberFormatException;

    long getLongValue();

    double getDoubleValue();

    /**
     * parse number and return integer or {@code defaultValue} if not parsable.
     *
     * @param defaultValue defaultValue
     * @return parsed integer or {@code defaultValue} if not parsable
     */
    int getInt(int defaultValue);

    int getIntValue(int defaultValue);

    int getIntValue();

    /**
     * test if the argument is valid long integer
     *
     * @return true if the argument is valid long integer
     */
    boolean isLong();

    /**
     * parse number and return long.
     *
     * @return parsed long or error
     * @throws NumberFormatException if not parsable
     */
    long getLong();

    /**
     * parse number and return long or {@code defaultValue} if not parsable.
     *
     * @param defaultValue defaultValue
     * @return parsed integer or {@code defaultValue} if not parsable
     */
    long getLong(long defaultValue);

    /**
     * test if the argument is valid long double
     *
     * @return true if the argument is valid double
     */
    boolean isDouble();

    /**
     * parse number and return double.
     *
     * @return parsed double or error
     * @throws NumberFormatException if not parsable
     */
    double getDouble();

    /**
     * parse number and return double or {@code defaultValue} if not parsable.
     *
     * @param defaultValue defaultValue
     * @return parsed integer or {@code defaultValue} if not parsable
     */
    double getDouble(double defaultValue);

    /**
     * parse boolean. equivalent to {@code getBoolean(false)}
     *
     * @return parsed boolean
     */
    boolean getBoolean();

    /**
     * test if the argument is valid boolean. a valid boolean mush match one of
     * the following regular expressions :
     * "true|enable|enabled|yes|always|y|on|ok|t" : will be evaluated as true
     * boolean. "false|disable|disabled|no|none|never|n|off|ko" : will be
     * evaluated as false boolean. In both cases, this method returns true.
     * Otherwise, it will return false.
     *
     * @return true if the argument is valid boolean
     */
    boolean isBoolean();

    /**
     * return boolean value if the current argument can be parsed as valid
     * boolean of defaultValue if not
     *
     * "true|enable|enabled|yes|always|y|on|ok|t" are considered 'true'.
     * "false|disable|disabled|no|none|never|n|off|ko" are considered 'false'.
     *
     * @param defaultValue default value
     * @return boolean value of the current argument
     */
    Boolean getBoolean(Boolean defaultValue);

    /**
     * Throw an exception if the argument is null
     *
     * @return {@code this} instance
     */
    NutsArgument required() throws NoSuchElementException;

    /**
     * true if the argument is in the form key=value
     *
     * @return true if the argument is in the form key=value
     */
    boolean isKeyValue();

    /**
     * return new instance (never null) of the key part of the argument. The key
     * does not include neither ! nor // or = argument parts as they are parsed
     * separately. Here example of getArgumentKey result of some arguments
     * <ul>
     * <li>Argument("key").getArgumentKey() ==&gt; Argument("key") </li>
     * <li>Argument("key=value").getArgumentKey() ==&gt; Argument("key") </li>
     * <li>Argument("--key=value").getArgumentKey() ==&gt; Argument("--key")
     * </li>
     * <li>Argument("--!key=value").getArgumentKey() ==&gt; Argument("--key")
     * </li>
     * <li>Argument("--!//key=value").getArgumentKey() ==&gt; Argument("--key")
     * </li>
     * </ul>
     *
     * @return new instance (never null) of the key part of the argument
     */
    NutsArgument getArgumentKey();

    /**
     * return option prefix part  ('-' and '--')
     * @return option prefix part  ('-' and '--')
     * @since 0.5.7
     */
    String getStringOptionPrefix();

    /**
     * return query value separator
     * @return query value separator
     * @since 0.5.7
     */
    String getKeyValueSeparator();

    /**
     * return option key part excluding prefix ('-' and '--')
     * @return option key part excluding prefix ('-' and '--')
     * @since 0.5.7
     */
    NutsArgument getArgumentOptionName();

    /**
     * return option key part excluding prefix ('-' and '--')
     * @return option key part excluding prefix ('-' and '--')
     * @since 0.5.7
     */
    String getStringOptionName();

    /**
     * return new instance (never null) of the value part of the argument (after
     * =). However Argument's value may be null (
     * {@code getArgumentValue().getString() == null}). Here are some examples of
     * getArgumentValue() result for some common arguments
     * <ul>
     * <li>Argument("key").getArgumentValue() ==&gt; Argument(null) </li>
     * <li>Argument("key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("key=").getArgumentValue() ==&gt; Argument("") </li>
     * <li>Argument("--key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("--!key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("--!//key=value").getArgumentValue() ==&gt;
     * Argument("value") </li>
     * </ul>
     *
     * @return new instance (never null) of the value part of the argument
     * (after =)
     */
    NutsArgument getArgumentValue();

    /**
     *
     * return key part (never null) of the argument. The key does not include
     * neither ! nor // or = argument parts as they are parsed separately. Here
     * are some examples of getStringKey() result for some common arguments
     * <ul>
     * <li>Argument("key").getArgumentKey() ==&gt; "key" </li>
     * <li>Argument("key=value").getArgumentKey() ==&gt; "key" </li>
     * <li>Argument("--key=value").getArgumentKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--!key=value").getArgumentKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--!//key=value").getArgumentKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--//!key=value").getArgumentKey() ==&gt; "--key"
     * </li>
     * </ul>
     * equivalent to {@code getArgumentKey().getString()}
     *
     * @return string key
     */
    String getStringKey();

    /**
     * equivalent to {@code getArgumentValue().getString()}
     *
     * @return string value (may be null)
     */
    String getStringValue();

    Boolean getBooleanValue(Boolean defaultValue);

    /**
     * equivalent to {@code getArgumentValue().getString(value)}
     *
     * @param defaultValue default value
     * @return string value
     */
    String getStringValue(String defaultValue);

    /**
     * parse argument's value as boolean equivalent to
     * {@code getArgumentValue().getBoolean()}
     *
     * @return boolean value
     */
    boolean getBooleanValue();
}
