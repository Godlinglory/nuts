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
import net.thevpc.nuts.reserved.ReservedSimpleCharQueue;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

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
public class DefaultNCommandLine implements NCommandLine {

    protected LinkedList<String> args = new LinkedList<>();
    protected List<NArg> lookahead = new ArrayList<>();
    protected boolean expandSimpleOptions = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected String commandName;
    private int wordIndex = 0;
    private NCommandAutoComplete autoComplete;
    private char eq = '=';
    private NSession session;

    //Constructors
    public DefaultNCommandLine() {

    }

    public DefaultNCommandLine(NApplicationContext context) {
        setArguments(context.getArguments());
        setAutoComplete(context.getAutoComplete());
    }

    public DefaultNCommandLine(String[] args, NCommandAutoComplete autoComplete) {
        setArguments(args);
        setAutoComplete(autoComplete);
    }

    public DefaultNCommandLine(String[] args) {
        setArguments(args);
    }

    public DefaultNCommandLine(List<String> args, NCommandAutoComplete autoComplete) {
        setArguments(args);
        setAutoComplete(autoComplete);
    }

    public DefaultNCommandLine(List<String> args) {
        setArguments(args);
    }

    public NSession getSession() {
        return session;
    }

    public NCommandLine setSession(NSession session) {
        this.session = session;
        return this;
    }

    //End Constructors
    @Override
    public NCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public NCommandLine setAutoComplete(NCommandAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
        return this;
    }

    @Override
    public NCommandLine unregisterSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }

    @Override
    public String[] getSpecialSimpleOptions() {
        return specialSimpleOptions.toArray(new String[0]);
    }

    @Override
    public NCommandLine registerSpecialSimpleOption(String option) {
        if (option.length() > 2) {
            char c0 = option.charAt(0);
            char c1 = option.charAt(1);
            char c2 = option.charAt(2);
            if ((c0 == '-' || c0 == '+') && DefaultNArg.isSimpleKey(c1) && DefaultNArg.isSimpleKey(c2)) {
                specialSimpleOptions.add(option);
                return this;
            }
        }
        throwError(NMsg.ofC("invalid special option %s", option));
        return this;
    }

    @Override
    public boolean isSpecialSimpleOption(String option) {
        if (option == null) {
            return false;
        }
        DefaultNArg a = new DefaultNArg(option);
        String p = a.getOptionPrefix().asString().orNull();
        if (p == null || p.length() != 1) {
            return false;
        }
        String o = a.getKey().asString().orNull();
        if (o == null) {
            return false;
        }
        for (String registered : specialSimpleOptions) {
            if (registered.equals(o)) {
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
    public NCommandLine setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    @Override
    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }

    @Override
    public NCommandLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }

    @Override
    public NCommandLine throwUnexpectedArgument(NString errorMessage) {
        return throwUnexpectedArgument(NMsg.ofC("%s", errorMessage));
    }

    @Override
    public NCommandLine throwUnexpectedArgument(NMsg errorMessage) {
        if (!isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("unexpected argument %s");
            ep.add(highlightText(String.valueOf(peek().orNull())));
            if (errorMessage != null) {
                sb.append(", %s");
                ep.add(errorMessage);
            }
            throwError(NMsg.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }

    @Override
    public NCommandLine throwMissingArgument() {
        return throwMissingArgument(null);
    }

    @Override
    public NCommandLine throwMissingArgumentByName(String argumentName) {
        if (NBlankable.isBlank(argumentName)) {
            throwMissingArgument();
        } else {
            if (isEmpty()) {
                if (autoComplete != null) {
                    skipAll();
                    return this;
                }
                throwError(NMsg.ofC("missing argument %s", NMsg.ofStyled(argumentName, NTextStyle.keyword())));
            }
            return this;
        }
        return this;
    }

    @Override
    public NCommandLine throwMissingArgument(NMsg errorMessage) {
        if (isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("missing argument");
            if (errorMessage != null) {
                sb.append(", %s");
                ep.add(errorMessage);
            }
            throwError(NMsg.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }

    @Override
    public NCommandLine throwUnexpectedArgument() {
        return throwUnexpectedArgument((NMsg) null);
    }

    @Override
    public NCommandLine pushBack(NArg arg) {
        NAssert.requireNonNull(arg, "argument");
        lookahead.add(0, arg);
        return this;
    }

    @Override
    public NOptional<NArg> next() {
        return next(expandSimpleOptions);
    }

    @Override
    public NOptional<NArg> next(NArgName name) {
        return next(name, false);
    }

    @Override
    public NOptional<NArg> nextOption(String option) {
        if (!new DefaultNArg(option).isOption()) {
            return errorOptionalCformat("%s is not an option", option);
        }
        return next(new DefaultNArgName(option), true);
    }

    @Override
    public boolean isNextOption() {
        return peek().map(NArg::isOption).orElse(false);
    }

    @Override
    public boolean isNextNonOption() {
        return peek().map(NArg::isNonOption).orElse(false);
    }

    @Override
    public NOptional<NArg> peek() {
        return get(0);
    }

    @Override
    public boolean hasNext() {
        return !lookahead.isEmpty() || !args.isEmpty();
    }

    @Override
    public NOptional<NArg> nextBoolean(String... names) {
        return next(NArgumentType.BOOLEAN, names);
    }

    @Override
    public NOptional<NArg> nextString(String... names) {
        return next(NArgumentType.STRING, names);
    }

    @Override
    public NOptional<NArg> nextString() {
        return nextString(new String[0]);
    }

    @Override
    public NOptional<NArg> nextBoolean() {
        return nextBoolean(new String[0]);
    }

    @Override
    public boolean withNextOptionalBoolean(NArgProcessor<NOptional<Boolean>> consumer) {
        NOptional<NArg> v = nextBoolean();
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getBooleanValue(), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextOptionalBoolean(NArgProcessor<NOptional<Boolean>> consumer, String... names) {
        NOptional<NArg> v = nextBoolean(names);
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getBooleanValue(), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextOptionalString(NArgProcessor<NOptional<String>> consumer) {
        NOptional<NArg> v = nextString();
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getStringValue(), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextOptionalString(NArgProcessor<NOptional<String>> consumer, String... names) {
        NOptional<NArg> v = nextString(names);
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getStringValue(), a, session);
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean withNextBoolean(NArgProcessor<Boolean> consumer) {
        NOptional<NArg> v = nextBoolean();
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getBooleanValue().get(session), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextTrue(NArgProcessor<Boolean> consumer) {
        return withNextBoolean((value, arg, session1) -> {
            if (value) {
                consumer.run(true, arg, session1);
            }
        });
    }

    @Override
    public boolean withNextTrue(NArgProcessor<Boolean> consumer, String... names) {
        return withNextBoolean((value, arg, session) -> {
            if (value) {
                consumer.run(true, arg, session);
            }
        }, names);
    }

    @Override
    public boolean withNextBoolean(NArgProcessor<Boolean> consumer, String... names) {
        NOptional<NArg> v = nextBoolean(names);
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getBooleanValue().get(session), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextString(NArgProcessor<String> consumer) {
        NOptional<NArg> v = nextString();
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getStringValue().get(session), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextString(NArgProcessor<String> consumer, String... names) {
        NOptional<NArg> v = nextString(names);
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getStringValue().get(session), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextStringLiteral(NArgProcessor<NLiteral> consumer) {
        NOptional<NArg> v = nextString();
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getValue(), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextStringLiteral(NArgProcessor<NLiteral> consumer, String... names) {
        NOptional<NArg> v = nextString(names);
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getValue(), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean withNextLiteral(NArgProcessor<NLiteral> consumer) {
        NOptional<NArg> v = next();
        if (v.isPresent()) {
            NArg a = v.get(session);
            if (a.isActive()) {
                consumer.run(a.getValue(), a, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public NOptional<NLiteral> nextStringLiteral() {
        return nextStringLiteral(new String[0]);
    }

    @Override
    public NOptional<NLiteral> nextBooleanLiteral() {
        return nextBooleanLiteral(new String[0]);
    }

    @Override
    public NOptional<NLiteral> nextStringLiteral(String... names) {
        return nextString(names).map(NArg::getValue);
    }

    @Override
    public NOptional<NLiteral> nextBooleanLiteral(String... names) {
        return nextBoolean(names).map(NArg::getValue);
    }

    @Override
    public NOptional<NArg> next(String... names) {
        return next(NArgumentType.DEFAULT, names);
    }

    @Override
    public NOptional<NArg> next(NArgumentType expectValue, String... names) {
        if (expectValue == null) {
            expectValue = NArgumentType.DEFAULT;
        }
        if (names.length == 0) {
            if (hasNext()) {
                NArg peeked = peek().orNull();
                NOptional<String> string = peeked.getKey().asString();
                if (string.isError()) {
                    return NOptional.ofError(string.getMessage());
                }
                if (string.isPresent()) {
                    names = new String[]{string.get()};
                } else {
                    names = new String[0];
                }
            }
        } else {
            if (isAutoCompleteMode()) {
                NArgCandidate[] candidates = resolveRecommendations(expectValue, names, autoComplete.getCurrentWordIndex());
                for (NArgCandidate c : candidates) {
                    autoComplete.addCandidate(c);
                }
            }
        }

        for (String nameSeq : names) {
            String[] nameSeqArray = NStringUtils.split(nameSeq, " ").toArray(new String[0]);
            if (nameSeqArray.length == 0) {
                continue;
            }
            if (!isPrefixed(nameSeqArray)) {
                continue;
            }
            String name = nameSeqArray[nameSeqArray.length - 1];
            NArg p = get(nameSeqArray.length - 1).orNull();
            if (p != null) {
                NOptional<String> pks = p.getKey().asString();
                if (pks.isPresent() && pks.get().equals(name)) {
                    switch (expectValue) {
                        case DEFAULT: {
                            skip(nameSeqArray.length);
                            return NOptional.of(p);
                        }
                        case STRING: {
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return NOptional.of(p);
                            } else {
                                NArg r2 = peek().orNull();
                                if (r2 != null && !r2.isOption()) {
                                    skip();
                                    return NOptional.of(createArgument(p.asString().orElse("") + eq + r2.asString().orElse("")));
                                } else {
                                    return NOptional.of(p);
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
                                        return NOptional.of(createArgument(pks.get() + eq + (!x)));
                                    }
                                } else {
                                    if (pks.isPresent()) {
                                        return NOptional.of(createArgument(pks.get() + eq + (false)));
                                    }
                                }
                            } else if (p.isKeyValue()) {
                                return NOptional.of(p);
                            } else {
                                if (pks.isPresent()) {
                                    return NOptional.of(createArgument(pks.get() + eq + (true)));
                                }
                            }
                            break;
                        }
                        default: {
                            return errorOptionalCformat("unsupported %s", highlightText(String.valueOf(expectValue)));
                        }
                    }
                }
            }

        }
        return emptyOptionalCformat("missing argument");
    }

    private <T> NOptional<T> emptyOptionalCformat(String str, Object... args) {
        List<Object> a = new ArrayList<>();
        if (!NBlankable.isBlank(getCommandName())) {
            a.add(getCommandName());
            a.addAll(Arrays.asList(args));
            return NOptional.ofEmpty(s -> NMsg.ofC("%s : " + str, a.toArray()));
        } else {
            a.addAll(Arrays.asList(args));
        }
        return NOptional.ofEmpty(s -> NMsg.ofC(str, a.toArray()));
    }

    private <T> NOptional<T> errorOptionalCformat(String str, Object... args) {
        return NOptional.ofError(s -> {
            if (!NBlankable.isBlank(getCommandName())) {
                return NMsg.ofC("%s : %s ", getCommandName(), NMsg.ofC(str, args));
            }
            return NMsg.ofC(str, args);
        });
    }

    @Override
    public NOptional<NArg> nextNonOption(NArgName name) {
        return next(name, true);
    }

    @Override
    public NOptional<NArg> nextNonOption(String name) {
        return nextNonOption(new DefaultNArgName(name));
    }

    @Override
    public NOptional<NArg> nextNonOption() {
        if (hasNext() && !isNextOption()) {
            return next();
        }
        return emptyOptionalCformat("missing non-option");
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
            NArg argument = get(index + i).orNull();
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
    public NOptional<NArg> find(String name) {
        int index = indexOf(name);
        if (index >= 0) {
            return get(index);
        }
        return emptyOptionalCformat("missing argument");
    }

    @Override
    public NOptional<NArg> get(int index) {
        if (index < 0) {
            return emptyOptionalCformat("missing argument");
        }
        if (index < lookahead.size()) {
            return NOptional.of(lookahead.get(index));
        }
        while (!args.isEmpty() && index >= lookahead.size()) {
            if (!ensureNext(isExpandSimpleOptions(), true)) {
                break;
            }
        }
        if (index < lookahead.size()) {
            return NOptional.of(lookahead.get(index));
        }
        return emptyOptionalCformat("missing argument");
    }

    @Override
    public boolean contains(String name) {
        return indexOf(name) >= 0;
    }

    @Override
    public int indexOf(String name) {
        int i = 0;
        while (i < length()) {
            NOptional<NArg> g = get(i);
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
        for (NArg nutsArgument : lookahead) {
            all.add(nutsArgument.asString().orElse(""));
        }
        all.addAll(args);
        return all;
    }

    @Override
    public NArg[] toArgumentArray() {
        List<NArg> aa = new ArrayList<>();
        while (hasNext()) {
            aa.add(next().get());
        }
        lookahead.addAll(aa);
        return aa.toArray(new NArg[0]);
    }

    @Override
    public boolean isOption(int index) {
        return get(index).map(NArg::isOption).orElse(false);
    }

    @Override
    public boolean isNonOption(int index) {
        return get(index).map(NArg::isNonOption).orElse(false);
    }

    public NCommandLine setArguments(List<String> arguments) {
        if (arguments == null) {
            return setArguments(new String[0]);
        }
        return setArguments(arguments.toArray(new String[0]));
    }

    public NCommandLine setArguments(String[] arguments) {
        this.lookahead.clear();
        this.args.clear();
        if (arguments != null) {
            Collections.addAll(this.args, arguments);
        }
        return this;
    }

    @Override
    public void throwError(NMsg message) {
        if (session == null) {
            if (NBlankable.isBlank(commandName)) {
                throw new IllegalArgumentException(message.toString());
            }
            throw new IllegalArgumentException(NMsg.ofC("%s : %s", commandName, message).toString());
        }
        if (NBlankable.isBlank(commandName)) {
            throw new NIllegalArgumentException(session, message);
        }
        throw new NIllegalArgumentException(session, NMsg.ofC("%s : %s", commandName, message));
    }

    @Override
    public void throwError(NString message) {
        if (session == null) {
            if (!NBlankable.isBlank(commandName)) {
                throw new IllegalArgumentException(NMsg.ofC("%s : %s", commandName, message).toString());
            }
            throw new IllegalArgumentException(NMsg.ofC("%s", commandName, message).toString());
        }
        NTextBuilder m = NTexts.of(session).ofBuilder();
        if (!NBlankable.isBlank(commandName)) {
            m.append(commandName).append(" : ");
        }
        m.append(message);
        throw new NIllegalArgumentException(session, NMsg.ofNtf(m.build().toString()));
    }

    @Override
    public NCommandLineFormat formatter(NSession session) {
        return NCommandLineFormat.of(session != null ? session : this.session).setValue(this);
    }


    private NArgCandidate[] resolveRecommendations(NArgumentType expectValue, String[] names, int autoCompleteCurrentWordIndex) {
        //nameSeqArray
        List<NArgCandidate> candidates = new ArrayList<>();
        for (String nameSeq : names) {
            String[] nameSeqArray = NStringUtils.split(nameSeq, " ").toArray(new String[0]);
            if (nameSeqArray.length > 0) {
                int i = autoCompleteCurrentWordIndex < nameSeqArray.length ? autoCompleteCurrentWordIndex : nameSeqArray.length - 1;
//                String rec = null;
                boolean skipToNext = false;
                for (int j = 0; j < i; j++) {
                    String a = nameSeqArray[j];
                    NArg x = get(j).orNull();
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
                    NArg x = get(i).orNull();
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
                            candidates.add(new DefaultNArgCandidate(a));
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
                    NArg p = get(nameSeqArray.length - 1).orNull();
                    if (p != null) {
                        if (name.startsWith(p.getKey().asString().orElse(""))) {
                            candidates.add(new DefaultNArgCandidate(name));
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
                        candidates.add(new DefaultNArgCandidate(name));
                    }
                }
            }
        }
        return candidates.toArray(new NArgCandidate[0]);
    }

    private boolean isPrefixed(String[] nameSeqArray) {
        for (int i = 0; i < nameSeqArray.length - 1; i++) {
            NArg x = get(i).orNull();
            if (x == null || !x.asString().orElse("").equals(nameSeqArray[i])) {
                return false;
            }
        }
        return true;
    }

    public NOptional<NArg> next(NArgName name, boolean forceNonOption) {
        if (hasNext() && (!forceNonOption || !isNextOption())) {
            if (isAutoComplete()) {
                List<NArgCandidate> values = name == null ? null : name.getCandidates(getAutoComplete());
                if (values == null || values.isEmpty()) {
                    autoComplete.addCandidate(new DefaultNArgCandidate(name == null ? "<value>" : name.getName()));
                } else {
                    for (NArgCandidate value : values) {
                        autoComplete.addCandidate(value);
                    }
                }
            }
            NArg r = peek().orNull();
            skip();
            if (r == null) {
                return emptyOptionalCformat("expected argument");
            }
            return NOptional.of(r);
        } else {
            if (autoComplete != null) {
                if (isAutoComplete()) {
                    List<NArgCandidate> values = name == null ? null : name.getCandidates(getAutoComplete());
                    if (values == null || values.isEmpty()) {
                        autoComplete.addCandidate(new DefaultNArgCandidate(name == null ? "<value>" : name.getName()));
                    } else {
                        for (NArgCandidate value : values) {
                            autoComplete.addCandidate(value);
                        }
                    }
                }
                return NOptional.of(createArgument(""));
            }
            if (hasNext() && (!forceNonOption || !isNextOption())) {
                return emptyOptionalCformat("unexpected option %s", highlightText(String.valueOf(peek().get().asString())));
            }
            return emptyOptionalCformat("missing argument %s", highlightText(String.valueOf(name == null ? "value" : name.getName())));
        }
        //ignored
    }

    public NOptional<NArg> next(boolean expandSimpleOptions) {
        if (ensureNext(expandSimpleOptions, false)) {
            if (!lookahead.isEmpty()) {
                return NOptional.of(lookahead.remove(0));
            }
            String v = args.removeFirst();
            return NOptional.of(createArgument(v));
        } else {
            return emptyOptionalCformat("missing argument");
        }
    }

    @Override
    public String toString() {
        return toStringList().stream().map(x -> NStringUtils.formatStringLiteral(x, NStringUtils.QuoteType.DOUBLE, NSupportMode.PREFERRED)).collect(Collectors.joining(" "));
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
            if (expandSimpleOptions && v.length() > 2 && !isSpecialSimpleOption(v) && ((v.charAt(0) == '-' && v.charAt(1) != '-') || (v.charAt(0) == '+' && v.charAt(1) != '+')) && (v.charAt(1) != '/' || v.charAt(2) == '/')) {
                ReservedSimpleCharQueue vv = new ReservedSimpleCharQueue(v.toCharArray());
                char start = vv.read();
                char negChar = '\0';
                boolean negate = false;
                if (vv.peek() == '!' || vv.peek() == '~') {
                    negChar = vv.read();
                    negate = true;
                }
                while (vv.hasNext()) {
                    char c = vv.read();
                    StringBuilder cc = new StringBuilder();
                    cc.append(start);
                    if (negate) {
                        cc.append(negChar);
                    }
                    cc.append(c);
                    if (DefaultNArg.isSimpleKey(c)) {
                        while (vv.hasNext() && (vv.peek() != eq && !DefaultNArg.isSimpleKey(vv.peek()))) {
                            cc.append(vv.read());
                        }
                        if (vv.hasNext() && vv.peek() == eq) {
                            while (vv.hasNext()) {
                                cc.append(vv.read());
                            }
                            lookahead.add(createArgument(cc.toString()));
                        } else {
                            lookahead.add(createArgument(cc.toString()));
                        }
                    } else {
                        while (vv.hasNext()) {
                            cc.append(vv.read());
                        }
                        lookahead.add(createArgument(cc.toString()));
                    }
                }
            } else {
                lookahead.add(createArgument(v));
            }
            return true;
        }
        return false;
    }

    private NArg createArgument(String v) {
        return new DefaultNArg(v, eq);
    }

    private boolean isAutoComplete() {
        return autoComplete != null && getWordIndex() == autoComplete.getCurrentWordIndex();
    }

    public NCommandLine copy() {
        DefaultNCommandLine c = new DefaultNCommandLine(toStringArray(), autoComplete);
        c.setSession(session);
        c.eq = this.eq;
        c.commandName = this.commandName;
        return c;
    }

    private NMsg highlightText(String text) {
        return NMsg.ofStyled(String.valueOf(text), NTextStyle.primary3());
    }

    private boolean isPunctuation(char c) {
        switch (Character.getType(c)) {
            case Character.DASH_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.SPACE_SEPARATOR:
            case Character.START_PUNCTUATION:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.MODIFIER_SYMBOL:
            case Character.CONTROL:
                return true;
        }
        return false;
    }

    @Override
    public Iterator<NArg> iterator() {
        return Arrays.asList(toArgumentArray()).iterator();
    }


    public static NOptional<String[]> parseDefaultList(String commandLineString) {
        if (commandLineString == null) {
            return NOptional.of(new String[0]);
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
                            return NOptional.ofError(session -> NMsg.ofC("illegal char %s", c));
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
                return NOptional.ofError(session -> NMsg.ofPlain("expected quote"));
            }
        }
        return NOptional.of(args.toArray(new String[0]));
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
    public NCommandLine add(String argument) {
        if (argument != null) {
            args.add(argument);
        }
        return this;
    }

    @Override
    public NCommandLine addAll(List<String> arguments) {
        if (arguments != null) {
            for (String argument : arguments) {
                add(argument);
            }
        }
        return this;
    }

    @Override
    public boolean isBlank() {
        return isEmpty();
    }

    @Override
    public void process(NCommandLineProcessor processor, NCommandLineContext context) {
        NCommandLine cmd = this;
        NArg a;
        processor.onCmdInitParsing(cmd, context);
        while (cmd.hasNext()) {
            a = cmd.peek().get(session);
            boolean consumed;
            if (a.isOption()) {
                consumed = processor.onCmdNextOption(a, cmd, context);
            } else {
                consumed = processor.onCmdNextNonOption(a, cmd, context);
            }
            if (consumed) {
                NArg next = cmd.peek().orNull();
                //reference equality!
                if (next == a) {
                    //was not consumed!
                    throwError(NMsg.ofC("%s must consume the option: %s",
                            (a.isOption() ? "nextOption" : "nextNonOption"),
                            a));
                }
            } else if (!context.configureFirst(cmd)) {
                cmd.throwUnexpectedArgument();
            }
        }
        processor.onCmdFinishParsing(cmd, context);

        // test if application is running in exec mode
        // (and not in autoComplete mode)
        if (this.isExecMode()) {
            //do the good staff here
            processor.onCmdExec(cmd, context);
        } else if (this.getAutoComplete() != null) {
            processor.onCmdAutoComplete(this.getAutoComplete(), context);
        }
    }
}
