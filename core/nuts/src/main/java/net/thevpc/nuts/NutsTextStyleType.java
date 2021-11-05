/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * @app.category Format
 */
public enum NutsTextStyleType implements NutsEnum {
    PLAIN(true),//f
    UNDERLINED(true),//_
    ITALIC(true),// /
    STRIKED(true),// -
    REVERSED(true),//v
    BOLD(true),//d
    BLINK(true),//k
    FORE_COLOR(true),//f
    BACK_COLOR(true),//b
    FORE_TRUE_COLOR(true),//f
    BACK_TRUE_COLOR(true),//b

    PRIMARY(false), //p
    SECONDARY(false),//s
    ERROR(false),
    WARN(false),
    INFO(false),
    CONFIG(false),
    COMMENTS(false),
    STRING(false),
    NUMBER(false),
    DATE(false),
    BOOLEAN(false),
    KEYWORD(false),
    OPTION(false),
    INPUT(false),
    SEPARATOR(false),
    OPERATOR(false),
    SUCCESS(false),
    FAIL(false),
    DANGER(false),
    VAR(false),
    PALE(false),
    PATH(false),
    VERSION(false),
    TITLE(false);
    private final boolean basic;
    private final String id;

    NutsTextStyleType(boolean basic) {
        this.basic = basic;
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsTextStyleType parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsTextStyleType parseLenient(String value, NutsTextStyleType emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsTextStyleType parseLenient(String value, NutsTextStyleType emptyValue, NutsTextStyleType errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_').replace("_","");
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        switch (value.toLowerCase()) {
            case "f":
            case "foreground":
            case "foregroundcolor": {
                return FORE_COLOR;
            }
            case "plain": {
                return PLAIN;
            }
            case "foregroundx":
            case "foregroundtruecolor":
            {
                return FORE_TRUE_COLOR;
            }
            case "b":
            case "background":
            case "back_color":
            case "backcolor":
            case "backgroundcolor": {
                return BACK_COLOR;
            }
            case "backtruecolor":
            case "backgroundx":
            case "backgroundtruecolor":
            {
                return BACK_TRUE_COLOR;

            }

            case "p":
            case "primary": {
                return PRIMARY;
            }
            case "s":
            case "secondary": {
                return SECONDARY;
            }
            case "underlined": {
                return UNDERLINED;
            }
            case "bold": {
                return BOLD;
            }
            case "boolean":
            case "bool":
            {
                return BOOLEAN;
            }
            case "blink": {
                return BLINK;
            }
            case "comment":
            case "comments":
            {
                return COMMENTS;
            }
            case "config": {
                return CONFIG;
            }
            case "danger": {
                return DANGER;
            }
            case "date": {
                return DATE;
            }
            case "number": {
                return NUMBER;
            }
            case "error": {
                return ERROR;
            }
            case "warning":
            case "warn":
            {
                return WARN;
            }
            case "version": {
                return VERSION;
            }
            case "var":
            case "variable":
            {
                return VAR;
            }
            case "input": {
                return INPUT;
            }
            case "title": {
                return TITLE;
            }
            case "success": {
                return SUCCESS;
            }
            case "string": {
                return STRING;
            }
            case "strike":
            case "striked": {
                return STRIKED;
            }
            case "sep":
            case "separator":
            {
                return SEPARATOR;
            }
            case "reversed": {
                return REVERSED;
            }
            case "path": {
                return PATH;
            }
            case "option": {
                return OPTION;
            }
            case "pale": {
                return PALE;
            }
            case "operator": {
                return OPERATOR;
            }
            case "kw":
            case "keyword":
            {
                return KEYWORD;
            }
            case "italic": {
                return ITALIC;
            }
            case "information":
            case "info":
            {
                return INFO;
            }
            case "fail": {
                return FAIL;
            }
        }
        try {
            return NutsTextStyleType.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsTextStyleType parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsTextStyleType parse(String value, NutsTextStyleType emptyValue, NutsSession session) {
        NutsTextStyleType v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v,value,NutsTextStyleType.class,session);
        return v;
    }

    @Override
    public String id() {
        return id;
    }

    public boolean basic() {
        return basic;
    }
}
