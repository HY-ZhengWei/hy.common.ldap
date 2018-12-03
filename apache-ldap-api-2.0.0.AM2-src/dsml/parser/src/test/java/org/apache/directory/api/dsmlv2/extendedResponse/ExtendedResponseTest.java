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

package org.apache.directory.api.dsmlv2.extendedResponse;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Map;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.api.dsmlv2.AbstractResponseTest;
import org.apache.directory.api.dsmlv2.DsmlControl;
import org.apache.directory.api.dsmlv2.Dsmlv2ResponseParser;
import org.apache.directory.api.dsmlv2.response.ExtendedResponseDsml;
import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Tests for the Extended Response parsing
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class ExtendedResponseTest extends AbstractResponseTest
{

    /**
     * Test parsing of a Response with the (optional) requestID attribute
     */
    @Test
    public void testResponseWithRequestId()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_requestID_attribute.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( 456, extendedResponse.getMessageId() );
    }


    /**
     * Test parsing of a Response with the (optional) requestID attribute below 0
     */
    @Test
    public void testResponseWithRequestIdBelow0()
    {
        testParsingFail( ExtendedResponseTest.class, "response_with_requestID_below_0.xml" );
    }


    /**
     * Test parsing of a response with a (optional) Control element
     */
    @Test
    public void testResponseWith1Control()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_1_control.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();
        Map<String, Control> controls = extendedResponse.getControls();

        assertEquals( 1, extendedResponse.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.643" );

        assertNotNull( control );
        assertTrue( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.643", control.getOid() );
        assertEquals( "Some text", Strings.utf8ToString( ( ( DsmlControl<?> ) control ).getValue() ) );
    }


    /**
     * Test parsing of a response with a (optional) Control element with empty value
     */
    @Test
    public void testResponseWith1ControlEmptyValue()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_1_control_empty_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();
        Map<String, Control> controls = extendedResponse.getControls();

        assertEquals( 1, extendedResponse.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.643" );

        assertNotNull( control );
        assertTrue( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.643", control.getOid() );
        assertFalse( ( ( DsmlControl<?> ) control ).hasValue() );
    }


    /**
     * Test parsing of a response with 2 (optional) Control elements
     */
    @Test
    public void testResponseWith2Controls()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_2_controls.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();
        Map<String, Control> controls = extendedResponse.getControls();

        assertEquals( 2, extendedResponse.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.789" );

        assertNotNull( control );
        assertFalse( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.789", control.getOid() );
        assertEquals( "Some other text", Strings.utf8ToString( ( ( DsmlControl<?> ) control ).getValue() ) );
    }


    /**
     * Test parsing of a response with 3 (optional) Control elements without value
     */
    @Test
    public void testResponseWith3ControlsWithoutValue()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_3_controls_without_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();
        Map<String, Control> controls = extendedResponse.getControls();

        assertEquals( 3, extendedResponse.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.456" );

        assertNotNull( control );
        assertTrue( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.456", control.getOid() );
        assertFalse( ( ( DsmlControl<?> ) control ).hasValue() );
    }


    /**
     * Test parsing of a response without Result Code element
     */
    @Test
    public void testResponseWithoutResultCode()
    {
        testParsingFail( ExtendedResponseTest.class, "response_without_result_code.xml" );
    }


    /**
     * Test parsing of a response with Result Code element but a not integer value
     */
    @Test
    public void testResponseWithResultCodeNotInteger()
    {
        testParsingFail( ExtendedResponseTest.class, "response_with_result_code_not_integer.xml" );
    }


    /**
     * Test parsing of a response with Result Code
     */
    @Test
    public void testResponseWithResultCode()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_result_code.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        LdapResult ldapResult = extendedResponse.getLdapResult();

        assertEquals( ResultCodeEnum.PROTOCOL_ERROR, ldapResult.getResultCode() );
    }


    /**
     * Test parsing of a response with Error Message
     */
    @Test
    public void testResponseWithErrorMessage()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_error_message.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        LdapResult ldapResult = extendedResponse.getLdapResult();

        assertEquals( "Unrecognized extended operation EXTENSION_OID: 1.2.6.1.4.1.18060.1.1.1.100.2", ldapResult
            .getDiagnosticMessage() );
    }


    /**
     * Test parsing of a response with empty Error Message
     */
    @Test
    public void testResponseWithEmptyErrorMessage()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_empty_error_message.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        LdapResult ldapResult = extendedResponse.getLdapResult();

        assertNull( ldapResult.getDiagnosticMessage() );
    }


    /**
     * Test parsing of a response with a Referral
     */
    @Test
    public void testResponseWith1Referral()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_1_referral.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        LdapResult ldapResult = extendedResponse.getLdapResult();

        Collection<String> referrals = ldapResult.getReferral().getLdapUrls();

        assertEquals( 1, referrals.size() );

        try
        {
            assertTrue( referrals.contains( new LdapUrl( "ldap://www.apache.org/" ).toString() ) );
        }
        catch ( LdapURLEncodingException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with a empty Referral
     */
    @Test
    public void testResponseWith1EmptyReferral()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_1_empty_referral.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        LdapResult ldapResult = extendedResponse.getLdapResult();

        Collection<String> referrals = ldapResult.getReferral().getLdapUrls();

        assertEquals( 0, referrals.size() );
    }


    /**
     * Test parsing of a response with 2 Referral elements
     */
    @Test
    public void testResponseWith2Referrals()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_2_referrals.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        LdapResult ldapResult = extendedResponse.getLdapResult();

        Collection<String> referrals = ldapResult.getReferral().getLdapUrls();

        assertEquals( 2, referrals.size() );

        try
        {
            assertTrue( referrals.contains( new LdapUrl( "ldap://www.apache.org/" ).toString() ) );
        }
        catch ( LdapURLEncodingException e )
        {
            fail();
        }

        try
        {
            assertTrue( referrals.contains( new LdapUrl( "ldap://www.apple.com/" ).toString() ) );
        }
        catch ( LdapURLEncodingException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with a Referral and an Error Message
     */
    @Test
    public void testResponseWith1ReferralAndAnErrorMessage()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_1_referral_and_error_message.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        LdapResult ldapResult = extendedResponse.getLdapResult();

        Collection<String> referrals = ldapResult.getReferral().getLdapUrls();

        assertEquals( 1, referrals.size() );

        try
        {
            assertTrue( referrals.contains( new LdapUrl( "ldap://www.apache.org/" ).toString() ) );
        }
        catch ( LdapURLEncodingException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with MatchedDN attribute
     */
    @Test
    public void testResponseWithMatchedDNAttribute()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_matchedDN_attribute.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        LdapResult ldapResult = extendedResponse.getLdapResult();

        assertTrue( ldapResult.getMatchedDn().equals( "cn=Bob Rush,ou=Dev,dc=Example,dc=COM" ) );
    }


    /**
     * Test parsing of a response with wrong matched Dn
     */
    @Test
    public void testResponseWithWrongMatchedDN()
    {
        testParsingFail( ExtendedResponseTest.class, "response_with_wrong_matchedDN_attribute.xml" );
    }


    /**
     * Test parsing of a response with Response Name
     */
    @Test
    public void testResponseWithResponseName()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_responseName.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        try
        {
            assertEquals( Oid.fromString( "1.2.3.4.5.6.7.8.9.0" ).toString(), extendedResponse.getResponseName().toString() );
        }
        catch ( DecoderException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with empty Response Name
     */
    @Test
    public void testResponseWithEmptyResponseName()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_empty_responseName.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponse extendedResponse = ( ExtendedResponse ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( "", extendedResponse.getResponseName().toString() );
    }


    /**
     * Test parsing of a response with wrong Response Name
     */
    @Test
    public void testResponseWithWrongResponseName()
    {
        testParsingFail( ExtendedResponseTest.class, "response_with_wrong_responseName.xml" );
    }


    /**
     * Test parsing of a response with Response
     */
    @Test
    public void testResponseWithResponse()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_response.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponseDsml extendedResponse = ( ExtendedResponseDsml ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( "This is a response", Strings.utf8ToString( extendedResponse.getResponseValue() ) );
    }


    /**
     * Test parsing of a response with Base64 Response
     */
    @Test
    public void testResponseWithBase64Response()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput(
                ExtendedResponseTest.class.getResource( "response_with_base64_response.xml" ).openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponseDsml extendedResponse = ( ExtendedResponseDsml ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( "DSMLv2.0 rocks!!", Strings.utf8ToString( extendedResponse.getResponseValue() ) );
    }


    /**
     * Test parsing of a response with empty Response
     */
    @Test
    public void testResponseWithEmptyResponse()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_empty_response.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponseDsml extendedResponse = ( ExtendedResponseDsml ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( "", Strings.utf8ToString( extendedResponse.getResponseValue() ) );
    }


    /**
     * Test parsing of a response with Response Name and Response
     */
    @Test
    public void testResponseWithResponseNameAndResponse()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( ExtendedResponseTest.class.getResource( "response_with_responseName_and_response.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        ExtendedResponseDsml extendedResponse = ( ExtendedResponseDsml ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( "This is a response", Strings.utf8ToString( extendedResponse.getResponseValue() ) );

        try
        {
            assertEquals( Oid.fromString( "1.2.3.4.5.6.7.8.9.0" ).toString(), extendedResponse.getResponseName().toString() );
        }
        catch ( DecoderException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with wrong Descr attribute
     */
    @Test
    public void testResponseWithWrongDescr()
    {
        testParsingFail( ExtendedResponseTest.class, "response_with_wrong_descr.xml" );
    }
}
