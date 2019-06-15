package net.vpc.app.nuts.core.security;

import net.vpc.app.nuts.core.spi.NutsAuthenticationAgentSpi;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.util.Arrays;

@NutsSingleton
public class PlainNutsAuthenticationAgent implements NutsAuthenticationAgent, NutsAuthenticationAgentSpi {

    private NutsWorkspace ws;

    @Override
    public String getId() {
        return "plain";
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws = workspace;
    }

    @Override
    public boolean removeCredentials(char[] credentialsId, NutsEnvProvider envProvider) {
        extractId(credentialsId);
        return true;
    }

    @Override
    public int getSupportLevel(String authenticationAgent) {
        return DEFAULT_SUPPORT - 1;
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password, NutsEnvProvider envProvider) {
        if (CoreStringUtils.isBlank(password)) {
            throw new NutsSecurityException(ws, "Missing old password");
        }
        char[] iid = extractId(credentialsId);
        if (Arrays.equals(iid, password)) {
            return;
        }
        throw new NutsSecurityException(ws, "Invalid login or password");
    }

    private char[] extractId(char[] a) {
        if (!CoreStringUtils.isBlank(a)) {
            char[] idc = (getId()).toCharArray();
            if (a.length > idc.length + 1) {
                boolean ok = true;
                for (int i = 0; i < idc.length; i++) {
                    if (a[i] != idc[i]) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    return Arrays.copyOfRange(a, idc.length, a.length);
                }
            }
        }
        throw new NutsSecurityException(ws, "credential id must start with " + getId() + ":");
    }

    @Override
    public char[] getCredentials(char[] credentialsId, NutsEnvProvider envProvider) {
        return extractId(credentialsId);
    }

    @Override
    public char[] createCredentials(
            char[] credentials,
            boolean allowRetreive,
            char[] credentialId,
             NutsEnvProvider envProvider
    ) {
        if (CoreStringUtils.isBlank(credentials)) {
            return null;
        } else {
            char[] val = credentials;
            String id = getId();
            char[] r = new char[id.length() + 1 + val.length];
            System.arraycopy(id.toCharArray(), 0, r, 0, id.length());
            r[id.length()] = ':';
            System.arraycopy(val, 0, r, id.length() + 1, val.length);
            return r;
        }
    }
}