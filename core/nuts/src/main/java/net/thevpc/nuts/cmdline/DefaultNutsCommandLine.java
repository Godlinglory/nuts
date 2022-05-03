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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStringUtils;
import net.thevpc.nuts.util.NutsUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * CommandLine args=new CommandLine(Arrays.asList("--!deleteLog","--deploy","/deploy/path","--deploy=/other-deploy/path","some-param"));
 * Argument a;
 * while (args.hasNext()) {
 * if ((a = args.nextBoolean("--deleteLog").orNull()) != null) {
 * deleteLog = a.getBooleanValue().get(session);
 * } else if ((a = args.nextString("--deploy").orNull()) != null) {
 * apps.add(a.getStringValue().get(session));
 * } else if ((a = args.next()) != null) {
 * name = a.getString();
 * } else {
 * args.throwUnexpectedArgument();
 * }
 * }
 * </pre> Created by vpc on 12/7/16.
 */
public class DefaultNutsCommandLine implements NutsCommandLine {

    protected LinkedList<String> args = new LinkedList<>();
    protected List<NutsArgument> lookahead = new ArrayList<>();
    protected boolean expandSimpleOptions = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected String commandName;
    private int wordIndex = 0;
    private NutsCommandAutoComplete autoComplete;
    private char eq = '=';

    //Constructors
    public DefaultNutsCommandLine() {

    }

    public DefaultNutsCommandLine(NutsApplicationContext context) {
        setArguments(context.getArguments());
        setAutoComplete(context.getAutoComplete());
    }

    public DefaultNutsCommandLine(String[] args, NutsCommandAutoComplete autoComplete) {
        setArguments(args);
        setAutoComplete(autoComplete);
    }

    public DefaultNutsCommandLine(String[] args) {
        setArguments(args);
    }

    public DefaultNutsCommandLine(List<String> args, NutsCommandAutoComplete autoComplete) {
        setArguments(args);
        setAutoComplete(autoComplete);
    }

    public DefaultNutsCommandLine(List<String> args) {
        setArguments(args);
    }


    //End Constructors
    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
        return this;
    }

    @Override
    public NutsCommandLine unregisterSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }

    @Override
    public String[] getSpecialSimpleOptions() {
        return specialSimpleOptions.toArray(new String[0]);
    }

    @Override
    public NutsCommandLine registerSpecialSimpleOption(String option, NutsSession session) {
        int len = option.length();
        switch (len) {
            case 0: {
                break;
            }
            case 1: {
                if (option.equals("-") || option.equals("+")) {
                    specialSimpleOptions.add(option);
                    return this;
                }
                break;
            }
            default: {
                if ((option.charAt(0) == '-' && option.charAt(1) != '-') || (option.charAt(0) == '+' && option.charAt(1) != '+')) {
                    specialSimpleOptions.add(option);
                    return this;
                }
                break;
            }
        }
        if (session == null) {
            throw new IllegalArgumentException(NutsMessage.ofCstyle("invalid special option %s", option).toString());
        } else {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("invalid special option %s", option));
        }
    }

    @Override
    public boolean isSpecialSimpleOption(String option) {
        for (String x : specialSimpleOptions) {
            if (option.equals(x) || option.startsWith(x + eq)) {
                return true;
            }
            if (option.startsWith("-//" + x.substring(1))) {
                //disabled option
                return true;
            }
            if (option.startsWith("-!" + x.substring(1))) {
                //unarmed
                return true;
            }
            if (option.startsWith("-~" + x.substring(1))) {
                //unarmed
                return true;
            }
        }
        return false;
    }

    @Override
    public int getWordIndex() {
        return wordIndex;
    }

    @Override
    public boolean isExecMode() {
        return autoComplete == null;
    }

    @Override
    public boolean isAutoCompleteMode() {
        return autoComplete != null;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public NutsCommandLine setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    @Override
    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }

    @Override
    public NutsCommandLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }

    @Override
    public NutsCommandLine throwUnexpectedArgument(NutsString errorMessage, NutsSession session) {
        return throwUnexpectedArgument(NutsMessage.ofCstyle("%s", errorMessage), session);
    }

    @Override
    public NutsCommandLine throwUnexpectedArgument(NutsMessage errorMessage, NutsSession session) {
        if (!isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("unexpected argument %s");
            ep.add(highlightText(String.valueOf(peek().orNull())));
            if (errorMessage != null && errorMessage.getMessage() != null && errorMessage.getMessage().length() > 0) {
                sb.append(" , %s");
                ep.add(errorMessage);
            }
            throwError(NutsMessage.ofCstyle(sb.toString(), ep.toArray()), session);
        }
        return this;
    }

    @Override
    public NutsCommandLine throwMissingArgument(NutsSession session) {
        return throwMissingArgument(null, session);
    }

    @Override
    public NutsCommandLine throwMissingArgument(NutsMessage errorMessage, NutsSession session) {
        if (isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("missing argument");
            if (errorMessage != null && errorMessage.getMessage() != null && errorMessage.getMessage().length() > 0) {
                sb.append(" , %s");
                ep.add(errorMessage);
            }
            throwError(NutsMessage.ofCstyle(sb.toString(), ep.toArray()), session);
        }
        return this;
    }

    @Override
    public NutsCommandLine throwUnexpectedArgument(NutsSession session) {
        return throwUnexpectedArgument((NutsMessage) null, session);
    }

    @Override
    public NutsCommandLine pushBack(NutsArgument arg, NutsSession session) {
        NutsUtils.requireNonNull(arg, "argument");
        lookahead.add(0, arg);
        return this;
    }

    @Override
    public NutsOptional<NutsArgument> next() {
        return next(expandSimpleOptions);
    }

    @Override
    public NutsOptional<NutsArgument> next(NutsArgumentName name) {
        return next(name, false);
    }

    @Override
    public NutsOptional<NutsArgument> nextOption(String option) {
        if (!new DefaultNutsArgument(option).isOption()) {
            return errorOptionalCstyle("%s is not an option", option);
        }
        return next(new DefaultArgumentName(option), true);
    }

    @Override
    public boolean isNextOption() {
        return peek().map(NutsArgument::isOption).orElse(false);
    }

    @Override
    public boolean isNextNonOption() {
        return peek().map(NutsArgument::isNonOption).orElse(false);
    }

    @Override
    public NutsOptional<NutsArgument> peek() {
        return get(0);
    }

    @Override
    public boolean hasNext() {
        return !lookahead.isEmpty() || !args.isEmpty();
    }

    @Override
    public NutsOptional<NutsArgument> nextBoolean(String... names) {
        return next(NutsArgumentType.BOOLEAN, names);
    }

    @Override
    public NutsOptional<NutsArgument> nextString(String... names) {
        return next(NutsArgumentType.STRING, names);
    }

    @Override
    public NutsOptional<String> nextStringValueLiteral(String... names) {
        return nextString(names).flatMap(NutsArgument::getStringValue);
    }

    @Override
    public NutsOptional<Boolean> nextBooleanValueLiteral(String... names) {
        return nextBoolean(names).flatMap(NutsArgument::getBooleanValue);
    }

    @Override
    public NutsOptional<NutsValue> nextStringValue(String... names) {
        return nextString(names).map(NutsArgument::getValue);
    }

    @Override
    public NutsOptional<NutsValue> nextBooleanValue(String... names) {
        return nextBoolean(names).map(NutsArgument::getValue);
    }

    @Override
    public NutsOptional<NutsArgument> next(String... names) {
        return next(NutsArgumentType.ANY, names);
    }

    @Override
    public NutsOptional<NutsArgument> next(NutsArgumentType expectValue, String... names) {
        if (expectValue == null) {
            expectValue = NutsArgumentType.ANY;
        }
        if (names.length == 0) {
            if (hasNext()) {
                NutsArgument peeked = peek().orNull();
                NutsOptional<String> string = peeked.getKey().asString();
                if (string.isError()) {
                    return NutsOptional.ofError(string.getMessage());
                }
                if (string.isPresent()) {
                    names = new String[]{string.get()};
                } else {
                    names = new String[0];
                }
            }
        } else {
            if (isAutoCompleteMode()) {
                NutsArgumentCandidate[] candidates = resolveRecommendations(expectValue, names, autoComplete.getCurrentWordIndex());
                for (NutsArgumentCandidate c : candidates) {
                    autoComplete.addCandidate(c);
                }
            }
        }

        for (String nameSeq : names) {
            String[] nameSeqArray = NutsStringUtils.split(nameSeq, " ").toArray(new String[0]);
            if (nameSeqArray.length == 0) {
                continue;
            }
            if (!isPrefixed(nameSeqArray)) {
                continue;
            }
            String name = nameSeqArray[nameSeqArray.length - 1];
            NutsArgument p = get(nameSeqArray.length - 1).orNull();
            if (p != null) {
                NutsOptional<String> pks = p.getKey().asString();
                if (pks.isPresent() && pks.get().equals(name)) {
                    switch (expectValue) {
                        case ANY: {
                            skip(nameSeqArray.length);
                            return NutsOptional.of(p);
                        }
                        case STRING: {
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return NutsOptional.of(p);
                            } else {
                                NutsArgument r2 = peek().orNull();
                                if (r2 != null && !r2.isOption()) {
                                    skip();
                                    return NutsOptional.of(createArgument(p.asString().orElse("") + eq + r2.asString().orElse("")));
                                } else {
                                    return NutsOptional.of(p);
                                }
                            }
                        }
                        case BOOLEAN: {
                            skip(nameSeqArray.length);
                            if (p.isNegated()) {
                                if (p.isKeyValue()) {
                                    //should not happen
                                    boolean x = p.getBooleanValue().orElse(false);
                                    if (pks.isPresent()) {
                                        return NutsOptional.of(createArgument(pks.get() + eq + (!x)));
                                    }
                                } else {
                                    if (pks.isPresent()) {
                                        return NutsOptional.of(createArgument(pks.get() + eq + (false)));
                                    }
                                }
                            } else if (p.isKeyValue()) {
                                return NutsOptional.of(p);
                            } else {
                                if (pks.isPresent()) {
                                    return NutsOptional.of(createArgument(pks.get() + eq + (true)));
                                }
                            }
                            break;
                        }
                        default: {
                            return errorOptionalCstyle("unsupported %s", highlightText(String.valueOf(expectValue)));
                        }
                    }
                }
            }

        }
        return emptyOptionalCstyle("missing argument");
    }

    private <T> NutsOptional<T> emptyOptionalCstyle(String str, Object... args) {
        List<Object> a = new ArrayList<>();
        if (!NutsBlankable.isBlank(getCommandName())) {
            a.add(getCommandName());
            a.addAll(Arrays.asList(args));
            return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("%s : " + str, a.toArray()));
        } else {
            a.addAll(Arrays.asList(args));
        }
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle(str, a.toArray()));
    }

    private <T> NutsOptional<T> errorOptionalCstyle(String str, Object... args) {
        return NutsOptional.ofError(s -> {
            if (!NutsBlankable.isBlank(getCommandName())) {
                return NutsMessage.ofCstyle("%s : %s ", getCommandName(), NutsMessage.ofCstyle(str, args));
            }
            return NutsMessage.ofCstyle(str, args);
        });
    }

    @Override
    public NutsOptional<NutsArgument> nextNonOption(NutsArgumentName name) {
        return next(name, true);
    }

    @Override
    public NutsOptional<NutsArgument> nextNonOption(String name) {
        return nextNonOption(new DefaultArgumentName(name));
    }

    @Override
    public NutsOptional<NutsArgument> nextNonOption() {
        if (hasNext() && !isNextOption()) {
            return next();
        }
        return emptyOptionalCstyle("missing non-option");
    }


    @Override
    public int skipAll() {
        int count = 0;
        while (hasNext()) {
            count += skip(1);
        }
        return count;
    }

    @Override
    public int skip() {
        return skip(1);
    }

    @Override
    public int skip(int count) {
        if (count < 0) {
            count = 0;
        }
        int initialCount = count;
        while (initialCount > 0 && hasNext()) {
            if (next() != null) {
                wordIndex++;
                initialCount--;
            } else {
                break;
            }
        }
        return count;
    }

    @Override
    public boolean accept(String... values) {
        return accept(0, values);
    }

    @Override
    public boolean accept(int index, String... values) {
        for (int i = 0; i < values.length; i++) {
            NutsArgument argument = get(index + i).orNull();
            if (argument == null) {
                return false;
            }
            if (!argument.getKey().asString().orElse("").equals(values[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsOptional<NutsArgument> find(String name) {
        int index = indexOf(name);
        if (index >= 0) {
            return get(index);
        }
        return emptyOptionalCstyle("missing argument");
    }

    @Override
    public NutsOptional<NutsArgument> get(int index) {
        if (index < 0) {
            return emptyOptionalCstyle("missing argument");
        }
        if (index < lookahead.size()) {
            return NutsOptional.of(lookahead.get(index));
        }
        while (!args.isEmpty() && index >= lookahead.size()) {
            if (!ensureNext(isExpandSimpleOptions(), true)) {
                break;
            }
        }
        if (index < lookahead.size()) {
            return NutsOptional.of(lookahead.get(index));
        }
        return emptyOptionalCstyle("missing argument");
    }

    @Override
    public boolean contains(String name) {
        return indexOf(name) >= 0;
    }

    @Override
    public int indexOf(String name) {
        int i = 0;
        while (i < length()) {
            NutsOptional<NutsArgument> g = get(i);
            if (g.isPresent() && g.get().getKey().asString().orElse("").equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int length() {
        return lookahead.size() + args.size();
    }

    @Override
    public boolean isEmpty() {
        return !hasNext();
    }

    @Override
    public String[] toStringArray() {
        return toStringList().toArray(new String[0]);
    }

    @Override
    public List<String> toStringList() {
        List<String> all = new ArrayList<>(length());
        for (NutsArgument nutsArgument : lookahead) {
            all.add(nutsArgument.asString().orElse(""));
        }
        all.addAll(args);
        return all;
    }

    @Override
    public NutsArgument[] toArgumentArray() {
        List<NutsArgument> aa = new ArrayList<>();
        while (hasNext()) {
            aa.add(next().get());
        }
        lookahead.addAll(aa);
        return aa.toArray(new NutsArgument[0]);
    }

    @Override
    public boolean isOption(int index) {
        return get(index).map(NutsArgument::isOption).orElse(false);
    }

    @Override
    public boolean isNonOption(int index) {
        return get(index).map(NutsArgument::isNonOption).orElse(false);
    }

    public NutsCommandLine setArguments(List<String> arguments) {
        if (arguments == null) {
            return setArguments(new String[0]);
        }
        return setArguments(arguments.toArray(new String[0]));
    }

    public NutsCommandLine setArguments(String[] arguments) {
        this.lookahead.clear();
        this.args.clear();
        if (arguments != null) {
            Collections.addAll(this.args, arguments);
        }
        return this;
    }

    @Override
    public void throwError(NutsMessage message, NutsSession session) {
        if (session == null) {
            if (NutsBlankable.isBlank(commandName)) {
                throw new IllegalArgumentException(message.toString());
            }
            throw new IllegalArgumentException(NutsMessage.ofCstyle("%s : %s", commandName, message).toString());
        }
        if (NutsBlankable.isBlank(commandName)) {
            throw new NutsIllegalArgumentException(session, message);
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("%s : %s", commandName, message));
    }

    @Override
    public void throwError(NutsString message, NutsSession session) {
        if (session == null) {
            if (!NutsBlankable.isBlank(commandName)) {
                throw new IllegalArgumentException(NutsMessage.ofCstyle("%s : %s", commandName, message).toString());
            }
            throw new IllegalArgumentException(NutsMessage.ofCstyle("%s", commandName, message).toString());
        }
        NutsTextBuilder m = NutsTexts.of(session).builder();
        if (!NutsBlankable.isBlank(commandName)) {
            m.append(commandName).append(" : ");
        }
        m.append(message);
        throw new NutsIllegalArgumentException(session, NutsMessage.ofNtf(m.build().toString()));
    }

    @Override
    public NutsCommandLineFormat formatter(NutsSession session) {
        return NutsCommandLineFormat.of(session).setValue(this);
    }

    private NutsArgumentCandidate[] resolveRecommendations(NutsArgumentType expectValue, String[] names, int autoCompleteCurrentWordIndex) {
        //nameSeqArray
        List<NutsArgumentCandidate> candidates = new ArrayList<>();
        for (String nameSeq : names) {
            String[] nameSeqArray = NutsStringUtils.split(nameSeq, " ").toArray(new String[0]);
            if (nameSeqArray.length > 0) {
                int i = autoCompleteCurrentWordIndex < nameSeqArray.length ? autoCompleteCurrentWordIndex : nameSeqArray.length - 1;
//                String rec = null;
                boolean skipToNext = false;
                for (int j = 0; j < i; j++) {
                    String a = nameSeqArray[j];
                    NutsArgument x = get(j).orNull();
                    if (x != null) {
                        String xs = x.asString().orElse("");
                        if (xs.length() > 0 && !xs.equals(a)) {
                            skipToNext = true;
                            break;
                        }
                    }
                }
                if (skipToNext) {
                    continue;
                }
                skipToNext = false;
                if (i < nameSeqArray.length - 1) {
                    String a = nameSeqArray[i];
                    NutsArgument x = get(i).orNull();
                    if (x != null) {
                        String xs = x.asString().orElse("");
                        if (xs.length() > 0 && xs.equals(a)) {
//                            switch (expectValue) {
//                                case ANY: {
//                                    candidates.add(createCandidate("<AnyValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                case STRING: {
//                                    candidates.add(createCandidate("<StringValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                case BOOLEAN: {
//                                    candidates.add(createCandidate("<BooleanValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                default: {
//                                    candidates.add(createCandidate("<OtherValueFor" + pgetKey().getString() + ">"));
//                                }
//                            }
                            skipToNext = true;
                        } else if (xs.length() > 0 && a.startsWith(xs) && !xs.equals(a)) {
                            candidates.add(new DefaultNutsArgumentCandidate(a));
                            skipToNext = true;
                        } else {
                            skipToNext = true;
                        }
                    }
                }
                if (skipToNext) {
                    continue;
                }
                if (getWordIndex() + nameSeqArray.length - 1 == autoCompleteCurrentWordIndex) {
                    String name = nameSeqArray[nameSeqArray.length - 1];
                    NutsArgument p = get(nameSeqArray.length - 1).orNull();
                    if (p != null) {
                        if (name.startsWith(p.getKey().asString().orElse(""))) {
                            candidates.add(new DefaultNutsArgumentCandidate(name));
//                            switch (expectValue) {
//                                case ANY: {
//                                    candidates.add(createCandidate("<AnyValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                case STRING: {
//                                    candidates.add(createCandidate("<StringValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                case BOOLEAN: {
//                                    candidates.add(createCandidate("<BooleanValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                default: {
//                                    candidates.add(createCandidate("<OtherValueFor" + p.getStringKey() + ">"));
//                                }
//                            }
                        }
                    } else {
                        candidates.add(new DefaultNutsArgumentCandidate(name));
                    }
                }
            }
        }
        return candidates.toArray(new NutsArgumentCandidate[0]);
    }

    private boolean isPrefixed(String[] nameSeqArray) {
        for (int i = 0; i < nameSeqArray.length - 1; i++) {
            NutsArgument x = get(i).orNull();
            if (x == null || !x.asString().equals(nameSeqArray[i])) {
                return false;
            }
        }
        return true;
    }

    public NutsOptional<NutsArgument> next(NutsArgumentName name, boolean forceNonOption) {
        if (hasNext() && (!forceNonOption || !isNextOption())) {
            if (isAutoComplete()) {
                List<NutsArgumentCandidate> values = name == null ? null : name.getCandidates(getAutoComplete());
                if (values == null || values.isEmpty()) {
                    autoComplete.addCandidate(new DefaultNutsArgumentCandidate(name == null ? "<value>" : name.getName()));
                } else {
                    for (NutsArgumentCandidate value : values) {
                        autoComplete.addCandidate(value);
                    }
                }
            }
            NutsArgument r = peek().orNull();
            skip();
            if (r == null) {
                return emptyOptionalCstyle("expected argument");
            }
            return NutsOptional.of(r);
        } else {
            if (autoComplete != null) {
                if (isAutoComplete()) {
                    List<NutsArgumentCandidate> values = name == null ? null : name.getCandidates(getAutoComplete());
                    if (values == null || values.isEmpty()) {
                        autoComplete.addCandidate(new DefaultNutsArgumentCandidate(name == null ? "<value>" : name.getName()));
                    } else {
                        for (NutsArgumentCandidate value : values) {
                            autoComplete.addCandidate(value);
                        }
                    }
                }
                return NutsOptional.of(createArgument(""));
            }
            if (hasNext() && (!forceNonOption || !isNextOption())) {
                return emptyOptionalCstyle("unexpected option %s", highlightText(String.valueOf(peek().get().asString())));
            }
            return emptyOptionalCstyle("missing argument %s", highlightText(String.valueOf(name == null ? "value" : name.getName())));
        }
        //ignored
    }

    public NutsOptional<NutsArgument> next(boolean expandSimpleOptions) {
        if (ensureNext(expandSimpleOptions, false)) {
            if (!lookahead.isEmpty()) {
                return NutsOptional.of(lookahead.remove(0));
            }
            String v = args.removeFirst();
            return NutsOptional.of(createArgument(v));
        } else {
            return emptyOptionalCstyle("missing argument");
        }
    }

    @Override
    public String toString() {
        return toStringList().stream().map(x -> NutsStringUtils.formatStringLiteral(
                x, NutsStringUtils.QuoteType.DOUBLE, NutsSupportMode.PREFERRED
        )).collect(Collectors.joining(" "));
    }

    private boolean isExpandableOption(String v, boolean expandSimpleOptions) {
        if (!expandSimpleOptions || v.length() <= 2) {
            return false;
        }
        if (isSpecialSimpleOption(v)) {
            return false;
        }
        if (v.charAt(0) == '-') {
            return v.charAt(1) != '-';
        }
        if (v.charAt(0) == '+') {
            return v.charAt(1) != '+';
        }
        return false;
    }

    private String createExpandedSimpleOption(char start, boolean negate, char val) {
        return new String(negate ? new char[]{start, '!', val} : new char[]{start, val});
    }

    private String createExpandedSimpleOption(char start, boolean negate, String val) {
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        if (negate) {
            sb.append('!');
        }
        sb.append(val);
        return sb.toString();
    }

    private boolean ensureNext(boolean expandSimpleOptions, boolean ignoreExistingExpanded) {
        if (!ignoreExistingExpanded) {
            if (!lookahead.isEmpty()) {
                return true;
            }
        }
        if (!args.isEmpty()) {
            // -!abc=true
            String v = args.removeFirst();
            if (isExpandableOption(v, expandSimpleOptions)) {
                char[] chars = v.toCharArray();
                boolean negate = false;
                Character last = null;
                char start = v.charAt(0);
                for (int i = 1; i < chars.length; i++) {
                    char c = chars[i];
                    if (c == '!' || c == '~') {
                        if (last != null) {
                            lookahead.add(createArgument(createExpandedSimpleOption(start, negate, last)));
                            last = null;
                        }
                        negate = true;
                    } else if (chars[i] == eq) {
                        String nextArg = new String(chars, i, chars.length - i);
                        if (last != null) {
                            nextArg = last + nextArg;
                            last = null;
                        }
                        lookahead.add(createArgument(createExpandedSimpleOption(start, negate, nextArg)));
                        i = chars.length;
                    } else if (isPunctuation(chars[i])) {
                        StringBuilder sb = new StringBuilder();
                        if (last != null) {
                            sb.append(last);
                        }
                        sb.append(chars[i]);
                        while (i + 1 < chars.length) {
                            i++;
                            sb.append(chars[i]);
                        }
                        lookahead.add(createArgument(createExpandedSimpleOption(start, negate, sb.toString())));
                        last = null;
                    } else {
                        if (last != null) {
                            lookahead.add(createArgument(createExpandedSimpleOption(start, negate, last)));
                        }
                        last = chars[i];
                    }
                }
                if (last != null) {
                    lookahead.add(createArgument(createExpandedSimpleOption(start, negate, last)));
                }
            } else {
                lookahead.add(createArgument(v));
            }
            return true;
        }
        return false;
    }

    private NutsArgument createArgument(String v) {
        return new DefaultNutsArgument(v, eq);
    }

    private boolean isAutoComplete() {
        return autoComplete != null && getWordIndex() == autoComplete.getCurrentWordIndex();
    }

    public NutsCommandLine copy() {
        DefaultNutsCommandLine c = new DefaultNutsCommandLine(toStringArray(), autoComplete);
        c.eq = this.eq;
        c.commandName = this.commandName;
        return c;
    }

    private NutsMessage highlightText(String text) {
        return NutsMessage.ofStyled(String.valueOf(text), NutsTextStyle.primary3());
    }

    private boolean isPunctuation(char c) {
        int t = Character.getType(c);
        return t != Character.LOWERCASE_LETTER && t != Character.UPPERCASE_LETTER && t != Character.TITLECASE_LETTER;
    }

    @Override
    public Iterator<NutsArgument> iterator() {
        return Arrays.asList(toArgumentArray()).iterator();
    }


    public static NutsOptional<String[]> parseDefaultList(String commandLineString) {
        if (commandLineString == null) {
            return NutsOptional.of(new String[0]);
        }
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        final int START = 0;
        final int IN_WORD = 1;
        final int IN_QUOTED_WORD = 2;
        final int IN_DBQUOTED_WORD = 3;
        int status = START;
        char[] charArray = commandLineString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (status) {
                case START: {
                    switch (c) {
                        case ' ':
                        case '\t': {
                            //ignore
                            break;
                        }
                        case '\r':
                        case '\n': //support multiline commands
                        {
                            //ignore
                            break;
                        }
                        case '\'': {
                            status = IN_QUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '"': {
                            status = IN_DBQUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '\\': {
                            status = IN_WORD;
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            status = IN_WORD;
                            break;
                        }
                    }
                    break;
                }
                case IN_WORD: {
                    switch (c) {
                        case ' ': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            break;
                        }
                        case '\'':
                        case '"': {
                            return NutsOptional.ofError(session -> NutsMessage.ofCstyle("illegal char %s", c));
                        }
                        case '\\': {
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            break;
                        }
                    }
                    break;
                }
                case IN_QUOTED_WORD: {
                    switch (c) {
                        case '\'': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                    break;
                }
                case IN_DBQUOTED_WORD: {
                    switch (c) {
                        case '"': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i = readEscapedArg(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                }
            }
        }
        switch (status) {
            case START: {
                break;
            }
            case IN_WORD: {
                args.add(sb.toString());
                sb.delete(0, sb.length());
                break;
            }
            case IN_QUOTED_WORD: {
                return NutsOptional.ofError(session -> NutsMessage.ofPlain("expected quote"));
            }
        }
        return NutsOptional.of(args.toArray(new String[0]));
    }

    private static int readEscapedArg(char[] charArray, int i, StringBuilder sb) {
        char c = charArray[i];
        switch (c) {
            case '\\':
            case ';':
            case '\"':
            case '\'':
            case '$':
            case ' ':
            case '<':
            case '>':
            case '(':
            case ')':
            case '~':
            case '&':
            case '|': {
                sb.append(c);
                break;
            }
            default: {
                sb.append('\\').append(c);
                break;
            }
        }
        return i;
    }

    @Override
    public NutsCommandLine add(String argument) {
        if (argument != null) {
            args.add(argument);
        }
        return this;
    }

    @Override
    public boolean isBlank() {
        return isEmpty();
    }
}
