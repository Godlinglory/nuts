package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NutsMinimalCommandLine {
    protected LinkedList<String> args = new LinkedList<>();

    public NutsMinimalCommandLine(String[] args) {
        this.args.addAll(Arrays.asList(args));
    }

    public static String escapeArgument(String arg) {
        StringBuilder sb = new StringBuilder();
        if (arg != null) {
            for (char c : arg.toCharArray()) {
                switch (c) {
                    case '\\':
                        sb.append('\\');
                        break;
                    case '\'':
                        sb.append("\\'");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\r':
                        sb.append("\\r");
                    case '\f':
                        sb.append("\\f");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }
        return sb.toString();
    }

    public static String escapeArguments(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(escapeArgument(arg));
        }
        return sb.toString();
    }

    public void clear() {
        args.clear();
    }

    public List<String> getArgs() {
        return new ArrayList<>(args);
    }

    public List<String> removeAll() {
        List<String> x = getArgs();
        clear();
        return x;
    }

    public String getValueFor(Arg cmdArg) {
        String v = cmdArg.getValue();
        if (v == null) {
            Arg next = next();
            if (next == null) {
                throw new NutsIllegalArgumentException("Missing argument for : " + cmdArg.getKey());
            }
            v = next.getArg();
        }
        return v;
    }

    public Arg next() {
        if (!args.isEmpty()) {
            return new Arg(args.removeFirst());
        }
        return null;
    }

    public static String[] parseCommandLine(String commandLineString) {
        if (commandLineString == null) {
            return new String[0];
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
                        case ' ': {
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
                        case '\'': {
                            throw new IllegalArgumentException("Illegal char " + c);
                        }
                        case '"': {
                            throw new IllegalArgumentException("Illegal char " + c);
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
                        case '\\': {
                            i=readEscaped(charArray, i+1, sb);
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
                            i=readEscaped(charArray, i+1, sb);
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
                throw new IllegalArgumentException("Expected '");
            }
        }
        return args.toArray(new String[0]);
    }

    private static int readEscaped(char[] charArray,int i,StringBuilder sb){
        char c = charArray[i];
        switch (c){
            case 'n':{
                sb.append('\n');
                break;
            }
            case 't':{
                sb.append('\t');
                break;
            }
            case 'r':{
                sb.append('\r');
                break;
            }
            case 'f':{
                sb.append('\f');
                break;
            }
            default:{
                sb.append(c);
            }
        }
        return i;
    }

    public static class Arg {
        private String line;

        public Arg(String line) {
            this.line = line;
        }

        public boolean isOption() {
            return line.startsWith("-");
        }

        public String getArg() {
            return line;
        }

        public String getKey() {
            int x = line.indexOf('=');
            if (x >= 0) {
                return line.substring(0, x);
            }
            return line;
        }

        public String getValue() {
            int x = line.indexOf('=');
            if (x >= 0) {
                return line.substring(x + 1);
            }
            return null;
        }
    }
}