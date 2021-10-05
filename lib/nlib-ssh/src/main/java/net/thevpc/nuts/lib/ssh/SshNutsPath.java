package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Arrays;

class SshNutsPath implements NutsPathSPI {
    private SshPath path;
    private NutsSession session;
    private SshListener listener;

    public SshNutsPath(SshPath path, NutsSession session) {
        this.path = path;
        this.session = session;
    }

    @Override
    public NutsPath resolve(String path) {
        String[] others = Arrays.stream(NutsUtilStrings.trim(path).split("[/\\\\]"))
                .filter(x -> x.length() > 0).toArray(String[]::new);
        if (others.length > 0) {
            StringBuilder loc = new StringBuilder(this.path.getPath());
            if (loc.length() == 0 || loc.charAt(loc.length() - 1) != '/') {
                loc.append('/');
            }
            loc.append(String.join("/", others));
            return
                    getSession().io().path(
                            SshPath.toString(
                                    this.path.getHost(),
                                    this.path.getPort(),
                                    loc.toString(),
                                    this.path.getUser(),
                                    this.path.getPassword(),
                                    this.path.getKeyFile()
                            ));
        }
        return getSession().io().path(toString());
    }

    public static String getURLParentPath(String ppath) {
        if (ppath == null) {
            return null;
        }
        while (ppath.endsWith("/")) {
            ppath = ppath.substring(0, ppath.length() - 1);
        }
        if (ppath.isEmpty()) {
            return null;
        }
        int i = ppath.lastIndexOf('/');
        if (i <= 0) {
            ppath = "/";
        } else {
            ppath = ppath.substring(0, i + 1);
        }
        return ppath;
    }

    @Override
    public NutsPath getParent() {
        String loc=getURLParentPath(this.path.getPath());
        if(loc==null){
            return null;
        }
        return
                getSession().io().path(
                        SshPath.toString(
                                this.path.getHost(),
                                this.path.getPort(),
                                loc,
                                this.path.getUser(),
                                this.path.getPassword(),
                                this.path.getKeyFile()
                        ));
    }

    @Override
    public String getProtocol() {
        return "ssh";
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String asString() {
        return path.toString();
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
        return new NutsFormatSPI() {
            @Override
            public void print(NutsPrintStream out) {
                //should implement better formatting...
                NutsTextStyle _sep = NutsTextStyle.separator();
                NutsTextStyle _path = NutsTextStyle.path();
                NutsTextStyle _nbr = NutsTextStyle.number();
//        if(true) {
                NutsTextManager text = session.text();
                NutsTextBuilder sb = text.builder();
                String user=path.getUser();
                String host=path.getHost();
                int port=path.getPort();
                String path0=path.getPath();
                String password=path.getPassword();
                String keyFile=path.getKeyFile();

                sb.append(text.ofStyled("ssh://", _sep));
                if (!(user == null || user.trim().length() == 0)) {
                    sb.append(user)
                            .append(text.ofStyled("@", _sep));
                }
                sb.append(host);
                if (port >= 0) {
                    sb.append(text.ofStyled(":", _sep))
                            .append(text.ofStyled(String.valueOf(port),_nbr));
                }
                if (!path0.startsWith("/")) {
                    sb.append(text.ofStyled('/'+path0,_path));
                }else {
                    sb.append(text.ofStyled(path0,_path));
                }
                if (password != null || keyFile != null) {
                    sb.append(text.ofStyled("?",_sep));
                    boolean first = true;
                    if (password != null) {
                        first = false;
                        sb
                                .append("password")
                                .append(text.ofStyled("=",_sep))
                                .append(password);
                    }
                    if (keyFile != null) {
                        if (!first) {
                            sb.append(text.ofStyled(",",_sep));
                        }
                        sb
                                .append("key-file")
                                .append(text.ofStyled("=",_sep))
                                .append(keyFile);
                    }
                }
                out.print(sb.toText());
            }

            @Override
            public boolean configureFirst(NutsCommandLine commandLine) {
                return false;
            }
        };
    }

    @Override
    public InputStream getInputStream() {
        return new SshFileInputStream(path,session);
    }

    @Override
    public OutputStream getOutputStream() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported output stream for %s",toString()));
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    public void delete(boolean recurse) {
        try (SShConnection session = new SShConnection(path.toAddress(),getSession())
                .addListener(listener)
        ) {
            session.rm(path.getPath(), recurse);
        }
    }

    public void mkdir(boolean parents) {
        try (SShConnection c = new SShConnection(path.toAddress(),getSession())
                .addListener(listener)
        ) {
            c.mkdir(path.getPath(), parents);
        }
    }

    @Override
    public boolean isDirectory() {
        try (SShConnection c = new SShConnection(path.toAddress(),getSession())
                .addListener(listener)
        ) {
            c.grabOutputString();
            int i= c.execStringCommand("file "+path.getPath());
            if(i>0){
                return false;
            }
            String s = c.getOutputString();
            int ii = s.indexOf(':');
            if(ii>0){
                return s.substring(i+1).trim().equals("directory");
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean isRegularFile() {
        try (SShConnection c = new SShConnection(path.toAddress(),getSession())
                .addListener(listener)
        ) {
            c.grabOutputString();
            int i= c.execStringCommand("file "+path.getPath());
            if(i>0){
                return false;
            }
            String s = c.getOutputString();
            int ii = s.indexOf(':');
            if(ii>0){
                return !s.substring(i+1).trim().equals("directory");
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public String getLocation() {
        return path.getPath();
    }

    @Override
    public boolean exists() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported exists for %s",toString()));
    }

    @Override
    public NutsPath[] getChildren() {
        try (SShConnection c = new SShConnection(path.toAddress(),getSession())
                .addListener(listener)
        ) {
            c.grabOutputString();
            int i= c.execStringCommand("ls "+path.getPath());
            if(i>0){
                return new NutsPath[0];
            }
            String[] s = c.getOutputString().split("[\n|\r]");
            return Arrays.stream(s).map(
                    x->{
                        String cc=path.getPath();
                        if(!cc.endsWith("/")){
                            cc+="/";
                        }
                        cc+=x;
                        return getSession().io().path(path.setPath(cc).toString());
                    }
            ).toArray(NutsPath[]::new);
        }catch (Exception e){
            //return false;
        }
        return new NutsPath[0];
    }

    @Override
    public Instant getLastModifiedInstant() {
        return null;
    }
}
