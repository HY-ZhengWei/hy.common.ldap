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

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.LdapResultImpl;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.message.ReferralImpl;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Tests the methods of the LdapResultImpl class.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 *         $Rev: 946251 $
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class LdapResultImplTest
{
    /**
     * Tests to make sure the two same objects are seen as equal.
     */
    @Test
    public void testEqualsSameObj()
    {
        LdapResultImpl r0 = new LdapResultImpl();
        assertTrue( "same object should be equal", r0.equals( r0 ) );
    }


    /**
     * Tests to make sure a default LdapResultImpl equals another one just
     * created.
     */
    @Test
    public void testEqualsDefaultCopy()
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        assertTrue( "default copy should be equal", r0.equals( r1 ) );
        assertTrue( "default copy should be equal", r1.equals( r0 ) );
    }


    /**
     * Tests for equality when the lockable parent is not the same.
     */
    @Test
    public void testEqualsDiffLockableParent()
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        assertTrue( "default copy with different lockable parents " + "should be equal", r0.equals( r1 ) );
        assertTrue( "default copy with different lockable parents " + "should be equal", r1.equals( r0 ) );
    }


    /**
     * Tests for equality when the lockable parent is the same.
     */
    @Test
    public void testEqualsDiffImpl()
    {
        LdapResultImpl r0 = new LdapResultImpl();

        LdapResult r1 = new LdapResult()
        {
            public ResultCodeEnum getResultCode()
            {
                return ResultCodeEnum.SUCCESS;
            }


            public void setResultCode( ResultCodeEnum a_resultCode )
            {
            }


            public Dn getMatchedDn()
            {
                return null;
            }


            public void setMatchedDn( Dn dn )
            {
            }


            public String getDiagnosticMessage()
            {
                return null;
            }


            public void setDiagnosticMessage( String diagnosticMessage )
            {
            }


            public boolean isReferral()
            {
                return false;
            }


            public Referral getReferral()
            {
                return null;
            }


            public void setReferral( Referral referral )
            {
            }


            public boolean isDefaultSuccess()
            {
                return false;
            }
        };

        assertTrue( "r0 equals should see other impl r1 as equal", r0.equals( r1 ) );
        assertFalse( "r1 impl uses Object.equals() so it should not see " + "r0 as the same object", r1.equals( r0 ) );
    }


    /**
     * Tests two non default carbon copies for equality.
     */
    @Test
    public void testEqualsCarbonCopy() throws LdapException
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        r0.setDiagnosticMessage( "blah blah blah" );
        r1.setDiagnosticMessage( "blah blah blah" );

        r0.setMatchedDn( new Dn( "dc=example,dc=com" ) );
        r1.setMatchedDn( new Dn( "dc=example,dc=com" ) );

        r0.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );
        r1.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );

        Referral refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://someserver.com" );
        refs0.addLdapUrl( "ldap://anotherserver.org" );

        Referral refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://someserver.com" );
        refs1.addLdapUrl( "ldap://anotherserver.org" );

        assertTrue( "exact copy should be equal", r0.equals( r1 ) );
        assertTrue( "exact copy should be equal", r1.equals( r0 ) );
    }


    /**
     * Tests to make sure the two same objects have equal HashCode.
     */
    @Test
    public void testHashCodeSameObj()
    {
        LdapResultImpl r0 = new LdapResultImpl();
        assertTrue( r0.hashCode() == r0.hashCode() );
    }


    /**
     * Tests to make sure a default LdapResultImpl has equal hashCode another one just
     * created.
     */
    @Test
    public void testHashCodeDefaultCopy()
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        assertTrue( r0.hashCode() == r1.hashCode() );
    }


    /**
     * Tests for equal hashCode when the lockable parent is not the same.
     */
    @Test
    public void testHashCodeDiffLockableParent()
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        assertTrue( r0.hashCode() == r1.hashCode() );
    }


    /**
     * Tests two non default carbon copies for equal hashCode.
     */
    @Test
    public void testHashCodeCarbonCopy() throws LdapException
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        r0.setDiagnosticMessage( "blah blah blah" );
        r1.setDiagnosticMessage( "blah blah blah" );

        r0.setMatchedDn( new Dn( "dc=example,dc=com" ) );
        r1.setMatchedDn( new Dn( "dc=example,dc=com" ) );

        r0.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );
        r1.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );

        Referral refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://someserver.com" );
        refs0.addLdapUrl( "ldap://anotherserver.org" );

        Referral refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://someserver.com" );
        refs1.addLdapUrl( "ldap://anotherserver.org" );

        assertTrue( r0.hashCode() == r1.hashCode() );
    }


    /**
     * Tests for inequality when the error message is different.
     */
    @Test
    public void testNotEqualsDiffErrorMessage() throws LdapException
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        r0.setDiagnosticMessage( "blah blah blah" );
        r1.setDiagnosticMessage( "blah" );

        r0.setMatchedDn( new Dn( "dc=example,dc=com" ) );
        r1.setMatchedDn( new Dn( "dc=example,dc=com" ) );

        r0.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );
        r1.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );

        Referral refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://someserver.com" );
        refs0.addLdapUrl( "ldap://anotherserver.org" );

        Referral refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://someserver.com" );
        refs1.addLdapUrl( "ldap://anotherserver.org" );

        assertFalse( "results with different error messages should " + "not be equal", r0.equals( r1 ) );
        assertFalse( "results with different error messages should " + "not be equal", r1.equals( r0 ) );
    }


    /**
     * Tests for inequality when the matchedDn properties are not the same.
     */
    @Test
    public void testNotEqualsDiffMatchedDn() throws LdapException
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        r0.setDiagnosticMessage( "blah blah blah" );
        r1.setDiagnosticMessage( "blah blah blah" );

        r0.setMatchedDn( new Dn( "dc=example,dc=com" ) );
        r1.setMatchedDn( new Dn( "dc=apache,dc=org" ) );

        r0.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );
        r1.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );

        Referral refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://someserver.com" );
        refs0.addLdapUrl( "ldap://anotherserver.org" );

        Referral refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://someserver.com" );
        refs1.addLdapUrl( "ldap://anotherserver.org" );

        assertFalse( "results with different matchedDn properties " + "should not be equal", r0.equals( r1 ) );
        assertFalse( "results with different matchedDn properties " + "should not be equal", r1.equals( r0 ) );
    }


    /**
     * Tests for inequality when the resultCode properties are not the same.
     */
    @Test
    public void testNotEqualsDiffResultCode() throws LdapException
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        r0.setDiagnosticMessage( "blah blah blah" );
        r1.setDiagnosticMessage( "blah blah blah" );

        r0.setMatchedDn( new Dn( "dc=example,dc=com" ) );
        r1.setMatchedDn( new Dn( "dc=example,dc=com" ) );

        r0.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );
        r1.setResultCode( ResultCodeEnum.SIZE_LIMIT_EXCEEDED );

        Referral refs0 = new ReferralImpl();
        refs0.addLdapUrl( "ldap://someserver.com" );
        refs0.addLdapUrl( "ldap://anotherserver.org" );

        Referral refs1 = new ReferralImpl();
        refs1.addLdapUrl( "ldap://someserver.com" );
        refs1.addLdapUrl( "ldap://anotherserver.org" );

        assertFalse( "results with different result codes should not be equal", r0.equals( r1 ) );
        assertFalse( "results with different result codes should not be equal", r1.equals( r0 ) );
    }


    /**
     * Tests for inequality when the referrals are not the same.
     */
    @Test
    public void testNotEqualsDiffReferrals() throws LdapException
    {
        LdapResultImpl r0 = new LdapResultImpl();
        LdapResultImpl r1 = new LdapResultImpl();

        r0.setDiagnosticMessage( "blah blah blah" );
        r1.setDiagnosticMessage( "blah blah blah" );

        r0.setMatchedDn( new Dn( "dc=example,dc=com" ) );
        r1.setMatchedDn( new Dn( "dc=example,dc=com" ) );

        r0.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );
        r1.setResultCode( ResultCodeEnum.TIME_LIMIT_EXCEEDED );

        Referral refs0 = new ReferralImpl();
        r0.setReferral( refs0 );
        refs0.addLdapUrl( "ldap://someserver.com" );
        refs0.addLdapUrl( "ldap://anotherserver.org" );

        Referral refs1 = new ReferralImpl();
        r1.setReferral( refs1 );
        refs1.addLdapUrl( "ldap://abc.com" );
        refs1.addLdapUrl( "ldap://anotherserver.org" );

        assertFalse( "results with different referrals should not be equal", r0.equals( r1 ) );
        assertFalse( "results with different referrals should not be equal", r1.equals( r0 ) );
    }
}
