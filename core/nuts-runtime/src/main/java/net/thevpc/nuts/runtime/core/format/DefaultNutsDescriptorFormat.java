package net.thevpc.nuts.runtime.core.format;

import net.thevpc.nuts.*;

import java.io.*;

public class DefaultNutsDescriptorFormat extends DefaultFormatBase<NutsDescriptorFormat> implements NutsDescriptorFormat {

    private boolean compact;
    private NutsDescriptor desc;

    public DefaultNutsDescriptorFormat(NutsWorkspace ws) {
        super(ws, "descriptor-format");
    }

    public NutsDescriptorFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public NutsDescriptorFormat compact(boolean compact) {
        return setCompact(compact);
    }

    @Override
    public NutsDescriptorFormat compact() {
        return compact(true);
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NutsDescriptorFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public NutsDescriptor getDescriptor() {
        return desc;
    }

    public NutsDescriptorFormat setDescriptor(NutsDescriptor desc) {
        this.desc = desc;
        return this;
    }

    public NutsDescriptorFormat value(NutsDescriptor desc) {
        return setDescriptor(desc);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        return false;
    }

    @Override
    public void print(PrintStream out) {
        checkSession();
        if (isNtf()) {
            ByteArrayOutputStream os=new ByteArrayOutputStream();
            getSession().getWorkspace()
                    .formats().element().setNtf(true).setContentType(NutsContentType.JSON)
                    .setValue(desc).setCompact(isCompact())
                    .print(os);
            NutsTextCode r = getSession().getWorkspace().formats().text().forCode("json", os.toString());
            out.print(r);
        } else {
            getSession().getWorkspace()
                    .formats().element().setNtf(false).setContentType(NutsContentType.JSON)
                    .setValue(desc).setCompact(isCompact())
                    .print(out);
        }
    }

}
