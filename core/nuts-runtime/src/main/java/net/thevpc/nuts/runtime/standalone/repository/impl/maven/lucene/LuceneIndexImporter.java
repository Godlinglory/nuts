package net.thevpc.nuts.runtime.standalone.repository.impl.maven.lucene;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.index.ArtifactsIndexDB;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LuceneIndexImporter {
    private NutsSession session;

    public LuceneIndexImporter(NutsSession session) {
        this.session = session;

    }

    public long importGzURL(URL url, String repository,NutsSession session) {
//        NutsWorkspace ws = session.getWorkspace();
        String tempGzFile = NutsTmp.of(session).createTempFile("lucene-repository.gz").toString();
        NutsCp.of(session)
                .setSession(session)
                .from(url).to(tempGzFile).run();
        String tempFolder = NutsTmp.of(session).createTempFolder("lucene-repository").toString();
        NutsUncompress.of(session).from(tempGzFile).to(
                tempFolder
        ).setFormat("gz").run();
        try {
            long[] ref=new long[1];
            Files.list(Paths.get(tempFolder)).forEach(
                    x -> {
                        ref[0]+=importFile(x.toString(),repository,session);
                    }
            );
            return ref[0];
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public long importFile(String filePath,String repository,NutsSession session) {
        ArtifactsIndexDB adb = ArtifactsIndexDB.of(session);
        NutsIdParser idParser = NutsIdParser.of(session);
        int addedCount=0;
        int allCount=0;
        try (DirtyLuceneIndexParser p = new DirtyLuceneIndexParser(new FileInputStream(filePath),session)) {
            while (p.hasNext()) {
                NutsId id = idParser.parse(p.next()).builder().setRepository(repository).build();
                if (!adb.contains(id,session)) {
                    addedCount++;
                    adb.add(id,session);
                }
                allCount++;
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        } finally {
            adb.flush(session);
        }
        return addedCount;
    }
}
