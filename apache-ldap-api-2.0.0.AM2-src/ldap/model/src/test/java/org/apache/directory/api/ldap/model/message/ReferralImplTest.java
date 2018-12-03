/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.api.ldap.model.message;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;

import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.message.ReferralImpl;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tests the ReferralImpl class.
 * 
 * @author <a href="mailto:dev@directory.apache.org"> Apache Directory Project</a>
 *         $Rev: 946251 $
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class ReferralImplTest
{
    /**
     * Tests to make sure the equals method works for the same exact object.
     */
    @Test
    public void testEqualsSameObject()
    {
        ReferralImpl refs = new ReferralImpl();
        assertTrue( "equals method should work for the same object", refs.equals( refs ) );
    }


    /**
     * Tests to make sure the equals method works for two objects that are the
     * same exact copy of one another.
     */
    @Test
    public void testEqualsExactCopy()
    {
        ReferralImpl refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://blah0" );
        refs0.addLdapUrl( "ldap://blah1" );
        refs0.addLdapUrl( "ldap://blah2" );
        ReferralImpl refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://blah0" );
        refs1.addLdapUrl( "ldap://blah1" );
        refs1.addLdapUrl( "ldap://blah2" );
        assertTrue( "exact copies of Referrals should be equal", refs0.equals( refs1 ) );
        assertTrue( "exact copies of Referrals should be equal", refs1.equals( refs0 ) );
    }


    /**
     * Tests to make sure the equals method works for two objects that are the
     * same exact copy of one another but there are redundant entries.
     */
    @Test
    public void testEqualsExactCopyWithRedundancy()
    {
        ReferralImpl refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://blah0" );
        refs0.addLdapUrl( "ldap://blah1" );
        refs0.addLdapUrl( "ldap://blah2" );
        refs0.addLdapUrl( "ldap://blah2" );
        ReferralImpl refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://blah0" );
        refs1.addLdapUrl( "ldap://blah1" );
        refs1.addLdapUrl( "ldap://blah2" );
        refs1.addLdapUrl( "ldap://blah2" );
        assertTrue( "exact copies of Referrals should be equal", refs0.equals( refs1 ) );
        assertTrue( "exact copies of Referrals should be equal", refs1.equals( refs0 ) );
    }


    /**
     * Tests to make sure to get equal hashCode for the same exact object.
     */
    @Test
    public void testHashCodeSameObject()
    {
        ReferralImpl refs = new ReferralImpl();
        assertTrue( refs.hashCode() == refs.hashCode() );
    }


    /**
     * Tests to make sure to get equal hashCode for two objects that are the
     * same exact copy of one another.
     */
    @Test
    public void testHashCodeExactCopy()
    {
        ReferralImpl refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://blah0" );
        refs0.addLdapUrl( "ldap://blah1" );
        refs0.addLdapUrl( "ldap://blah2" );
        ReferralImpl refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://blah0" );
        refs1.addLdapUrl( "ldap://blah1" );
        refs1.addLdapUrl( "ldap://blah2" );
        assertTrue( refs0.hashCode() == refs1.hashCode() );
    }


    /**
     * Tests to make sure to get equal hashCode for two objects that are the
     * same exact copy of one another but there are redundant entries.
     */
    @Test
    public void testHashCodeExactCopyWithRedundancy()
    {
        ReferralImpl refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://blah0" );
        refs0.addLdapUrl( "ldap://blah1" );
        refs0.addLdapUrl( "ldap://blah2" );
        refs0.addLdapUrl( "ldap://blah2" );
        ReferralImpl refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://blah0" );
        refs1.addLdapUrl( "ldap://blah1" );
        refs1.addLdapUrl( "ldap://blah2" );
        refs1.addLdapUrl( "ldap://blah2" );
        assertTrue( refs0.hashCode() == refs1.hashCode() );
    }


    /**
     * Tests to make sure the equals method works for two objects that are the
     * not exact copies of one another but have the same number of URLs.
     */
    @Test
    public void testEqualsSameNumberButDifferentUrls()
    {
        ReferralImpl refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://blah0" );
        refs0.addLdapUrl( "ldap://blah1" );
        refs0.addLdapUrl( "ldap://blah2" );
        refs0.addLdapUrl( "ldap://blah3" );
        ReferralImpl refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://blah0" );
        refs1.addLdapUrl( "ldap://blah1" );
        refs1.addLdapUrl( "ldap://blah2" );
        refs1.addLdapUrl( "ldap://blah4" );
        assertFalse( "Referrals should not be equal", refs0.equals( refs1 ) );
        assertFalse( "Referrals should not be equal", refs1.equals( refs0 ) );
    }


    /**
     * Tests to make sure the equals method works for two objects that are the
     * not exact copies of one another and one has a subset of the urls of the
     * other.
     */
    @Test
    public void testEqualsSubset()
    {
        ReferralImpl refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://blah0" );
        refs0.addLdapUrl( "ldap://blah1" );
        refs0.addLdapUrl( "ldap://blah2" );
        refs0.addLdapUrl( "ldap://blah3" );
        ReferralImpl refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://blah0" );
        refs1.addLdapUrl( "ldap://blah1" );
        assertFalse( "Referrals should not be equal", refs0.equals( refs1 ) );
        assertFalse( "Referrals should not be equal", refs1.equals( refs0 ) );
    }


    @Test
    public void testEqualsDifferentImpls()
    {
        Referral refs0 = new Referral()
        {
            public Collection<String> getLdapUrls()
            {
                return Collections.emptyList();
            }


            public void addLdapUrl( String url )
            {
            }


            public void removeLdapUrl( String url )
            {
            }


            public void addLdapUrlBytes( byte[] urlBytes )
            {
            }


            public Collection<byte[]> getLdapUrlsBytes()
            {
                return null;
            }


            public int getReferralLength()
            {
                return 0;
            }


            public void setReferralLength( int referralLength )
            {
            }
        };

        ReferralImpl refs1 = new ReferralImpl();

        assertFalse( "Object.equals() in effect because we did not redefine " + " equals for the new impl above", refs0
            .equals( refs1 ) );
        assertTrue( "Empty Referrals should be equal even if they are different" + " implementation classes", refs1
            .equals( refs0 ) );
    }
}
