/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdBuilder;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class Test32_Id {

    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        List<NutsId> s = NutsId.ofList("java#[11,[").get(session);
        Assertions.assertEquals(Arrays.asList(NutsId.of("java#[11,[").get(session)), s);
    }

    @Test
    public void test02() {
        List<NutsId> s = NutsId.ofList("java#[11,[ java#[11,[").get(session);
        //removed duplicates...
        Assertions.assertEquals(Arrays.asList(NutsId.of("java#[11,[").get(session)), s);
    }

    @Test
    public void test03() {
        List<NutsId> s = NutsId.ofList("java#[11,[ java#[12,[").get(session);
        Assertions.assertEquals(Arrays.asList(
                NutsId.of("java#[11,[").get(session),
                NutsId.of("java#[12,[").get(session)
        ), s);
    }

    @Test
    public void test04() {
        String t1="net.sourceforge.cobertura:cobertura#${cobertura.version}?exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage&cond-properties='a,b=c'";
        NutsId s = NutsId.of(t1).get(session);
        Assertions.assertEquals("net.sourceforge.cobertura",s.getGroupId());
        Assertions.assertEquals("cobertura",s.getArtifactId());
        Assertions.assertEquals("asm:asm,asm:asm-tree,log4j:log4j,oro:oro",s.getProperties().get("exclusions"));
        Assertions.assertEquals("",s.getCondition().getProperties().get("a"));
        Assertions.assertEquals("c",s.getCondition().getProperties().get("b"));
        TestUtils.println(s);
    }

    @Test
    public void test05() {
        String t1="net.sourceforge.cobertura:cobertura#${cobertura.version}?exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage&cond-properties=a,b\\=c";
        NutsId s = NutsId.of(t1).get(session);
        Assertions.assertEquals("net.sourceforge.cobertura",s.getGroupId());
        Assertions.assertEquals("cobertura",s.getArtifactId());
        Assertions.assertEquals("asm:asm,asm:asm-tree,log4j:log4j,oro:oro",s.getProperties().get("exclusions"));
        Assertions.assertEquals("",s.getCondition().getProperties().get("a"));
        Assertions.assertEquals("c",s.getCondition().getProperties().get("b"));
        TestUtils.println(s);
    }

    @Test
    public void test06() {
        NutsId a = NutsIdBuilder.of()
                .setProperty("a", "?")
                .build();
        Map<String, String> p = a.getProperties();
        TestUtils.println(a.toString());
        Assertions.assertEquals(1,p.size());
        Assertions.assertEquals("?a='?'",a.toString());
    }
}
