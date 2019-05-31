/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Command Line Argument
 * @author vpc
 * @since 0.5.5
 */
public interface NutsArgument extends NutsTokenFilter{

    /**
     * true if the argument starts with '-'
     * @return true if the argument starts with '-'
     */
    boolean isOption();

    /**
     * true if the argument do not start with '-'
     * @return true if the argument do not start with '-'
     */
    boolean isNonOption();

    /**
     * string representation of the argument or null
     * @return string representation of the argument or null
     */
    String getString();

    /**
     * string representation of the argument or the given defaultValue
     * @param defaultValue
     * @return string representation of the argument or the given defaultValue
     */
    String getString(String defaultValue);

    /**
     * true if option is in one of the following forms :
     * <ul>
     * <li>-!name</li>
     * <li>--!name</li>
     * <li>!name</li>
     * </ul>
     * where name is any valid identifier
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
     * @return true if the argument is enable and false if it is commented
     */
    boolean isEnabled();

    /**
     * test if the argument is valid integer
     * @return true if the argument is valid integer
     */
    boolean isInt();

    int getInt();

    int getInt(int defaultValue);

    /**
     * test if the argument is valid long integer
     * @return true if the argument is valid long integer
     */
    boolean isLong();

    long getLong();

    long getLong(long defaultValue);

    /**
     * test if the argument is valid long double
     * @return true if the argument is valid double
     */
    boolean isDouble();

    double getDouble();

    double getDouble(double defaultValue);

    boolean getBoolean();

    /**
     * test if the argument is valid boolean.
     * a valid boolean mush match one of the following regular expressions :
     * "true|enable|enabled|yes|always|y|on|ok" : will be evaluated as true boolean.
     * "false|disable|disabled|no|none|never|n|off|ko" : will be evaluated as false boolean.
     * In both cases, this method returns true. Otherwise, it will return false.
     * @return true if the argument is valid boolean
     */
    boolean isBoolean();

    /**
     * return boolean value if the current argument can be parsed as valid boolean of defaultValue if not
     * @param defaultValue default value
     * @return boolean value of the current argument
     */
    Boolean getBoolean(Boolean defaultValue);

    /**
     * Throw an exception if the argument is null
     * @return this instance
     */
    NutsArgument required() throws NutsIllegalArgumentException;

    /**
     * true if the argument is in the form key=value
     * @return true if the argument is in the form key=value
     */
    boolean isKeyValue();

    /**
     * return new instance (never null) of the key part of the argument.
     * The key does not include neither ! nor // or = argument parts as they are parsed separately.
     * Here example of getKey result of some arguments
     * <ul>
     * <li>Argument("key").getKey() ==&gt; Argument("key") </li>
     * <li>Argument("key=value").getKey()  ==&gt; Argument("key") </li>
     * <li>Argument("--key=value").getKey()  ==&gt; Argument("--key") </li>
     * <li>Argument("--!key=value").getKey()  ==&gt; Argument("--key") </li>
     * <li>Argument("--!//key=value").getKey()  ==&gt; Argument("--key") </li>
     * </ul>
     * @return 
     */
    NutsArgument getKey();

    /**
     * return new instance (never null) of the value part of the argument (after =).
     * Here example of getValue result of some arguments
     * <ul>
     * <li>Argument("key").getValue() ==&gt; Argument(null) </li>
     * <li>Argument("key=value").getValue()  ==&gt; Argument("value") </li>
     * <li>Argument("key=").getValue()  ==&gt; Argument("") </li>
     * <li>Argument("--key=value").getValue()  ==&gt; Argument("value") </li>
     * <li>Argument("--!key=value").getValue()  ==&gt; Argument("value") </li>
     * <li>Argument("--!//key=value").getValue()  ==&gt; Argument("value") </li>
     * </ul>
     * @return 
     */
    NutsArgument getValue();
}
