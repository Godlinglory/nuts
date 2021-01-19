package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.*;
import org.junit.jupiter.api.Test;

public class TestDev {
    @Test
    void test(){
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsSession session = ws.createSession();


        NutsString s=session.getWorkspace().formats().text().of(
                NutsMessage.cstyle("%s", "Hello"), session
        );

        session.out().printf("%s\n","Hello");
        session.out().printf("%s",NutsMessage.cstyle("Hello"));
    }
}
