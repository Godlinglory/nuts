package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.bundles.io.URLBuilder;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutsCompressedPath extends NutsPathBase {
    private String compressedForm;
    private NutsString formattedCompressedForm;
    private NutsPath base;

    public NutsCompressedPath(NutsPath base) {
        super(base.getSession());
        this.base = base;
        this.compressedForm = compressUrl(base.toString());
        this.formattedCompressedForm = base.getSession().text().ofStyled(compressedForm, NutsTextStyle.path());
    }

    public NutsCompressedPath(NutsPath base, String compressedForm, NutsString formattedCompressedForm) {
        super(base.getSession());
        this.compressedForm = compressedForm;
        this.formattedCompressedForm = formattedCompressedForm;
        this.base = base;
    }

    @Override
    public NutsPath resolve(String other) {
        return base.resolve(other).toCompressedForm();
    }

    @Override
    public String getProtocol() {
        return base.getProtocol();
    }

    @Override
    public boolean isDirectory() {
        return base.isDirectory();
    }

    @Override
    public boolean isRegularFile() {
        return base.isRegularFile();
    }

    public static String compressUrl(String path) {
        if (path.startsWith("http://")
                || path.startsWith("https://")) {
            URL u = null;
            try {
                u = new URL(path);
            } catch (MalformedURLException e) {
                return path;
            }
            return URLBuilder.buildURLString(u.getProtocol(),u.getAuthority(),
                    u.getPath()!=null?compressPath(u.getPath(), 0, 2):null,
                    u.getQuery()!=null? "?...":null,
                    u.getRef()!=null? "#...":null
            );
        } else {
            return compressPath(path);
        }
    }

    public static String compressPath(String path) {
        return compressPath(path, 2, 2);
    }

    public static String compressPath(String path, int left, int right) {
        String p = System.getProperty("user.home");
        if (path.startsWith("http://") || path.startsWith("https://")) {
            int interr = path.indexOf('?');
            if (interr > 0) {
                path = path.substring(0, interr) + "?...";
            }
        }
        if (path.startsWith(p + File.separator)) {
            path = "~" + path.substring(p.length());
        } else if (path.startsWith("http://")) {
            int x = path.indexOf('/', "http://".length() + 1);
            if (x <= 0) {
                return path;
            }
            return path.substring(0, x) + compressPath(path.substring(x), 0, right);
        } else if (path.startsWith("https://")) {
            int x = path.indexOf('/', "https://".length() + 1);
            if (x <= 0) {
                return path;
            }
            return path.substring(0, x) + compressPath(path.substring(x), 0, right);
        }
        List<String> a = new ArrayList<>(Arrays.asList(path.split("[\\\\/]")));
        int min = left + right + 1;
        if (a.size() > 0 && a.get(0).equals("")) {
            left += 1;
            min += 1;
        }
        if (a.size() > min) {
            a.add(left, "...");
            int len = a.size() - right - left - 1;
            for (int i = 0; i < len; i++) {
                a.remove(left + 1);
            }
        }
        return String.join("/", a);
    }

    @Override
    public String getContentEncoding() {
        return base.getContentEncoding();
    }

    @Override
    public String getContentType() {
        return base.getContentType();
    }

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public String asString() {
        return base.asString();
    }

    @Override
    public String getLocation() {
        return base.getLocation();
    }

    @Override
    public NutsPath toCompressedForm() {
        return this;
    }

    @Override
    public URL toURL() {
        return base.toURL();
    }

    @Override
    public Path toFile() {
        return base.toFile();
    }

//    @Override
//    public NutsInput input() {
//        return base.input();
//    }
//
//    @Override
//    public NutsOutput output() {
//        return base.output();
//    }

    @Override
    public InputStream getInputStream() {
        InputStream is = base.getInputStream();
        NutsInputStreamMetadata m = NutsInputStreamMetadata.of(is);
        NutsInputStreamMetadata m2 = new NutsDefaultInputStreamMetadata(m).setUserKind(getUserKind());
        return InputStreamMetadataAwareImpl.of(is,m2);
    }

    @Override
    public OutputStream getOutputStream() {
        return base.getOutputStream();
    }

    @Override
    public NutsPath delete(boolean recurse) {
        return base.delete(recurse);
    }

    @Override
    public NutsPath mkdir(boolean parents) {
        return base.mkdir(parents);
    }

    @Override
    public boolean exists() {
        return base.exists();
    }

    @Override
    public long getContentLength() {
        return base.getContentLength();
    }

    @Override
    public Instant getLastModifiedInstant() {
        return base.getLastModifiedInstant();
    }

    @Override
    public String toString() {
        return String.valueOf(compressedForm);
    }

    @Override
    public NutsFormat formatter() {
        return new MyPathFormat(this)
                .setSession(getSession())
                ;
    }

    private static class MyPathFormat extends DefaultFormatBase<NutsFormat> {
        private NutsCompressedPath p;

        public MyPathFormat(NutsCompressedPath p) {
            super(p.getSession().getWorkspace(), "path");
            this.p = p;
        }

        public NutsString asFormattedString() {
            return p.base.getSession().text().ofStyled(p.compressedForm, NutsTextStyle.path());
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }
    }

    @Override
    public NutsPathBuilder builder() {
        return base.builder();
    }

    @Override
    public NutsPath[] getChildren() {
        return new NutsPath[0];
    }

    @Override
    public NutsPath getParent() {
        return base.getParent();
    }
}
