package net.thevpc.nuts.runtime.standalone.id.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.dependency.NutsDependencyScopes;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyleType;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsUtilStrings;

import java.util.*;

public class DefaultNutsIdFormat extends DefaultFormatBase<NutsIdFormat> implements NutsIdFormat {

    private boolean omitRepository;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitProperties;
    private boolean highlightImportedGroup;
    private Set<String> omittedProperties = new HashSet<>();
    private NutsId id;

    public DefaultNutsIdFormat(NutsSession session) {
        super(session, "id-format");
    }

    public NutsIdFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public boolean isOmitRepository() {
        return omitRepository;
    }

    @Override
    public NutsIdFormat setOmitRepository(boolean value) {
        this.omitRepository = value;
        return this;
    }


    @Override
    public boolean isOmitGroupId() {
        return omitGroup;
    }

    @Override
    public NutsIdFormat setOmitGroupId(boolean value) {
        this.omitGroup = value;
        return this;
    }

    @Override
    public boolean isOmitImportedGroupId() {
        return omitImportedGroup;
    }

    @Override
    public NutsIdFormat setOmitImportedGroupId(boolean value) {
        this.omitImportedGroup = value;
        return this;
    }


    @Override
    public boolean isOmitOtherProperties() {
        return omitProperties;
    }

    @Override
    public NutsIdFormat setOmitOtherProperties(boolean value) {
        this.omitProperties = value;
        return this;
    }


    @Override
    public boolean isOmitFace() {
        return isOmitProperty(NutsConstants.IdProperties.FACE);
    }

    @Override
    public NutsIdFormat setOmitFace(boolean value) {
        return setOmitProperty(NutsConstants.IdProperties.FACE, value);
    }

    @Override
    public boolean isHighlightImportedGroupId() {
        return highlightImportedGroup;
    }

    @Override
    public NutsIdFormat setHighlightImportedGroupId(boolean value) {
        this.highlightImportedGroup = value;
        return this;
    }

    @Override
    public boolean isOmitClassifier() {
        return isOmitProperty(NutsConstants.IdProperties.CLASSIFIER);
    }

    @Override
    public NutsIdFormat setOmitClassifier(boolean value) {
        return setOmitProperty(NutsConstants.IdProperties.CLASSIFIER, value);
    }

    @Override
    public List<String> getOmitProperties() {
        return new ArrayList<>(omittedProperties);
    }

    @Override
    public boolean isOmitProperty(String name) {
        return omittedProperties.contains(name);
    }

    @Override
    public NutsIdFormat setOmitProperty(String name, boolean value) {
        if (value) {
            omittedProperties.add(name);
        } else {
            omittedProperties.remove(name);
        }
        return this;
    }

    @Override
    public NutsId getValue() {
        return id;
    }

    @Override
    public NutsIdFormat setValue(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsString format() {
        checkSession();
        if (id == null) {
            return isNtf() ?
                    NutsTexts.of(getSession()).ofStyled("<null>", NutsTextStyle.of(NutsTextStyleType.BOOLEAN))
                    : NutsTexts.of(getSession()).ofPlain("<null>")
                    ;
        }
        Map<String, String> queryMap = id.getProperties();
        String scope = queryMap.remove(NutsConstants.IdProperties.SCOPE);
        String optional = queryMap.remove(NutsConstants.IdProperties.OPTIONAL);
        String classifier = id.getClassifier();
        String exclusions = queryMap.remove(NutsConstants.IdProperties.EXCLUSIONS);
        String repo = queryMap.remove(NutsConstants.IdProperties.REPO);
        NutsIdBuilder idBuilder = id.builder();
        if (isOmitOtherProperties()) {
            idBuilder.setProperties(new LinkedHashMap<>());
        }
        if (isOmitFace()) {
            idBuilder.setProperty(NutsConstants.IdProperties.FACE, null);
        }
        id = idBuilder.build();
        NutsTextBuilder sb = NutsTexts.of(getSession()).builder();
        if (!isOmitGroupId()) {
            if (!NutsBlankable.isBlank(id.getGroupId())) {
                boolean importedGroup2 = NutsConstants.Ids.NUTS_GROUP_ID.equals(id.getGroupId());
                boolean importedGroup = getSession().imports().getAllImports().contains(id.getGroupId());
                if (!(importedGroup && isOmitImportedGroupId())) {
                    if (importedGroup || importedGroup2) {
                        sb.append(id.getGroupId(), NutsTextStyle.pale());
                    } else {
                        sb.append(id.getGroupId());
                    }
                    sb.append(":", NutsTextStyle.separator());
                }
            }
        }
        sb.append(id.getArtifactId(), NutsTextStyle.primary1());
        if (!NutsBlankable.isBlank(id.getVersion().getValue())) {
            sb.append("#", NutsTextStyle.separator());
            sb.append(id.getVersion());
        }
        boolean firstQ = true;

        if (!NutsBlankable.isBlank(classifier)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("classifier", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encodeKey(classifier));
        }

//        if (highlightScope) {
        if (!NutsDependencyScopes.isDefaultScope(scope)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("scope", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encodeKey(scope));
        }
//        }
//        if (highlightOptional) {
        if (!NutsBlankable.isBlank(optional) && !"false".equalsIgnoreCase(optional)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("optional", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encodeKey(optional));
        }
//        }
        if (!isOmitRepository()) {
            if (!NutsBlankable.isBlank(id.getRepository())) {
                if (firstQ) {
                    sb.append("?", NutsTextStyle.separator());
                    firstQ = false;
                } else {
                    sb.append("&", NutsTextStyle.separator());
                }
                sb.append("repo", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
                sb.append(_encodeKey(id.getRepository()), NutsTextStyle.pale());
            }
        }
        for (Map.Entry<String, String> e : CoreFilterUtils.toMap(id.getCondition(), getSession()).entrySet()) {
            String kk = e.getKey();
            String kv = e.getValue();
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append(_encodeKey(kk), NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encodeValue(kv));
        }
        if (!NutsBlankable.isBlank(exclusions)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("exclusions", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encodeKey(exclusions), NutsTextStyle.warn());
        }
        if (!NutsBlankable.isBlank(id.getPropertiesQuery())) {
            Set<String> otherKeys = new TreeSet<>(queryMap.keySet());
            for (String k : otherKeys) {
                String v2 = queryMap.get(k);
                if (v2 != null) {
                    if (firstQ) {
                        sb.append("?", NutsTextStyle.separator());
                        firstQ = false;
                    } else {
                        sb.append("&", NutsTextStyle.separator());
                    }
                    sb.append(_encodeKey(k), NutsTextStyle.pale());
                    sb.append("=", NutsTextStyle.separator());
                    sb.append(_encodeValue(v2));
                }
            }
        }
        if (isNtf()) {
            return sb.immutable();
        } else {
            return NutsTexts.of(getSession()).ofPlain(sb.filteredText());
        }
    }

    private String _encodeValue(String s) {
        return NutsUtilStrings.formatStringLiteral(s, NutsUtilStrings.QuoteType.SIMPLE, NutsSupportMode.PREFERRED);
    }

    private String _encodeKey(String s) {
        return NutsUtilStrings.formatStringLiteral(s, NutsUtilStrings.QuoteType.SIMPLE, NutsSupportMode.PREFERRED);
    }

    @Override
    public void print(NutsPrintStream out) {
        out.print(format());
    }

    @Override
    public String toString() {
        return "NutsIdFormat{"
                + "omitRepository=" + omitRepository
                + ", omitGroup=" + omitGroup
                + ", omitImportedGroup=" + omitImportedGroup
                + ", omitProperties=" + omitProperties
                + ", highlightImportedGroup=" + highlightImportedGroup
                + ", omittedProperties=" + omittedProperties
                + ", id=" + id
                + '}';
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsSession session = getSession();
        NutsArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch(a.getStringKey().orElse("")) {
            case "--omit-env": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    setOmitOtherProperties(val);
                }
                return true;
            }
            case "--omit-face": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    setOmitFace(val);
                }
                return true;
            }
            case "--omit-group": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    setOmitGroupId(val);
                }
                return true;
            }
            case "--omit-imported-group": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    setOmitImportedGroupId(val);
                }
                return true;
            }
            case "--omit-repo": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    setOmitRepository(val);
                }
                return true;
            }
            case "--highlight-imported-group": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    setHighlightImportedGroupId(val);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
