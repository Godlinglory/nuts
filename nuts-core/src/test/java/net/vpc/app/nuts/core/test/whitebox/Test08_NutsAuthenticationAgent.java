/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.test.whitebox;

import net.vpc.app.nuts.NutsAuthenticationAgent;
import net.vpc.app.nuts.NutsEnvProvider;
import net.vpc.app.nuts.core.security.DefaultNutsAuthenticationAgent;
import net.vpc.app.nuts.core.security.PlainNutsAuthenticationAgent;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test08_NutsAuthenticationAgent {

    private void testHelperRetrievable(NutsAuthenticationAgent a) {
        String mySecret = "my-secret";
        NutsEnvProvider envProvider = null;
        String withAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), true, null, envProvider));
        System.out.println(withAllowRetreiveId);
        Assert.assertTrue(withAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-secret".toCharArray(), envProvider);
        try {
            a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray(), envProvider);
            Assert.assertTrue(false);
        } catch (SecurityException ex) {
            Assert.assertTrue(true);
        }
        Assert.assertEquals(mySecret, new String(a.getCredentials(withAllowRetreiveId.toCharArray(), envProvider)));

        String withoutAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), false, null, envProvider));
        System.out.println(withoutAllowRetreiveId);
        Assert.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
    }

    private void testHelperHashed(NutsAuthenticationAgent a, boolean alwaysRetrievable) {
        String mySecret = "my-secret";
        NutsEnvProvider envProvider = null;
        String withoutAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), false, null, envProvider));
        System.out.println(withoutAllowRetreiveId);
        Assert.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-secret".toCharArray(), envProvider);
        try {
            a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray(), envProvider);
            Assert.assertTrue(false);
        } catch (SecurityException ex) {
            Assert.assertTrue(true);
        }
        if (alwaysRetrievable) {
            try {
                a.getCredentials(withoutAllowRetreiveId.toCharArray(), envProvider);
                Assert.assertTrue(true);
            } catch (SecurityException ex) {
                Assert.assertTrue(false);
            }

        } else {
            try {
                a.getCredentials(withoutAllowRetreiveId.toCharArray(), envProvider);
                Assert.assertTrue(false);
            } catch (SecurityException ex) {
                Assert.assertTrue(true);
            }

        }

    }

    @Test
    public void testCredentialsRetrievableDefault() {
        testHelperRetrievable(new DefaultNutsAuthenticationAgent());
        testHelperHashed(new DefaultNutsAuthenticationAgent(),false);
    }

    @Test
    public void testCredentialsRetrievablePlain() {
        testHelperRetrievable(new PlainNutsAuthenticationAgent());
        testHelperHashed(new PlainNutsAuthenticationAgent(),true);
    }

    @Test
    public void testCredentialsHashedDefault() {
        testHelperRetrievable(new DefaultNutsAuthenticationAgent());
        testHelperHashed(new DefaultNutsAuthenticationAgent(),false);
    }

}
