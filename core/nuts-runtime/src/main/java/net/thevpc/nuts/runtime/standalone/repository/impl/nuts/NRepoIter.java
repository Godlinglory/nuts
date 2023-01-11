package net.thevpc.nuts.runtime.standalone.repository.impl.nuts;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;

import java.io.IOException;
import java.io.InputStream;

class NRepoIter extends NIdPathIteratorBase {
    private NFolderRepository r;

    public NRepoIter(NFolderRepository r) {
        this.r = r;
    }

    @Override
    public void undeploy(NId id, NSession session) throws NExecutionException {
        r.undeploy().setId(id).setSession(session)
                //.setFetchMode(NutsFetchMode.LOCAL)
                .run();
    }

    @Override
    public boolean isDescFile(NPath pathname) {
        String name = pathname.getName();
        return name.endsWith(".pom") || name.endsWith(".nuts");
    }

    @Override
    public NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NSession session, NPath rootURL) throws IOException {
        session.getTerminal().printProgress(NMsg.ofC("%-8s %s", "parse", pathname.toCompressedForm()));
        String fn = pathname.getName();
        if (fn.endsWith(".pom")) {
            return MavenUtils.of(session).parsePomXmlAndResolveParents(in, fetchMode, pathname.toString(), repository);
        }else{
            return NDescriptorParser.of(session).setDescriptorStyle(NDescriptorStyle.NUTS).parse(in).get(session);
        }
    }

    @Override
    public NId parseId(NPath pomFile, NPath rootPath, NIdFilter filter, NRepository repository, NSession session) throws IOException {
        String fn = pomFile.getName();
        if (fn.endsWith(".pom")) {
            NPath versionFolder = pomFile.getParent();
            if (versionFolder != null) {
                String vn = versionFolder.getName();
                NPath artifactFolder = versionFolder.getParent();
                if (artifactFolder != null) {
                    String an = artifactFolder.getName();
                    if (fn.equals(an + "-" + vn + ".pom")) {
                        NPath groupFolder = artifactFolder.getParent();
                        if (groupFolder != null) {
                            NPath gg = groupFolder.subpath(rootPath.getPathCount(), groupFolder.getPathCount());
                            StringBuilder gn = new StringBuilder();
                            for (int i = 0; i < gg.getPathCount(); i++) {
                                String ns = gg.getItem(i);
                                if (i > 0) {
                                    gn.append('.');
                                }
                                gn.append(ns);
                            }
                            return validate(
                                    NIdBuilder.of()
                                            .setGroupId(gn.toString())
                                            .setArtifactId(an)
                                            .setVersion(vn)
                                            .build(),
                                    null, pomFile, rootPath, filter, repository, session);
                        }
                    }
                }
            }
        }else if(fn.endsWith(".nuts")){
            NPath versionFolder = pomFile.getParent();
            if (versionFolder != null) {
                String vn = versionFolder.getName();
                NPath artifactFolder = versionFolder.getParent();
                if (artifactFolder != null) {
                    String an = artifactFolder.getName();
                    if (fn.equals(an + "-" + vn + ".nuts")) {
                        NPath groupFolder = artifactFolder.getParent();
                        if (groupFolder != null) {
                            NPath gg = groupFolder.subpath(rootPath.getPathCount(), groupFolder.getPathCount());
                            StringBuilder gn = new StringBuilder();
                            for (int i = 0; i < gg.getPathCount(); i++) {
                                String ns = gg.getItem(i);
                                if (i > 0) {
                                    gn.append('.');
                                }
                                gn.append(ns);
                            }
                            return validate(
                                    NIdBuilder.of()
                                            .setGroupId(gn.toString())
                                            .setArtifactId(an)
                                            .setVersion(vn)
                                            .build(),
                                    null, pomFile, rootPath, filter, repository, session);
                        }
                    }
                }
            }
        }
        return null;
    }
}
