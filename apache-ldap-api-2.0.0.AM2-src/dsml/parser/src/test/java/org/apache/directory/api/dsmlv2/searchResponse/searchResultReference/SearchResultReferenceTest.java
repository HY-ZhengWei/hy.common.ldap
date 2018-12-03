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

package org.apache.directory.api.dsmlv2.searchResponse.searchResultReference;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Map;

import org.apache.directory.api.dsmlv2.AbstractResponseTest;
import org.apache.directory.api.dsmlv2.DsmlControl;
import org.apache.directory.api.dsmlv2.Dsmlv2ResponseParser;
import org.apache.directory.api.dsmlv2.response.SearchResponse;
import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchResultReference;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Tests for the Search Result Reference Response parsing
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class SearchResultReferenceTest extends AbstractResponseTest
{
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

            parser.setInput( SearchResultReferenceTest.class.getResource( "response_with_1_control.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        SearchResultReference searchResultReference = ( ( SearchResponse ) parser.getBatchResponse()
            .getCurrentResponse().getDecorated() ).getCurrentSearchResultReference();
        Map<String, Control> controls = searchResultReference.getControls();

        assertEquals( 1, searchResultReference.getControls().size() );

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

            parser.setInput( SearchResultReferenceTest.class.getResource( "response_with_1_control_empty_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        SearchResultReference searchResultReference = ( ( SearchResponse ) parser.getBatchResponse()
            .getCurrentResponse().getDecorated() ).getCurrentSearchResultReference();
        Map<String, Control> controls = searchResultReference.getControls();

        assertEquals( 1, searchResultReference.getControls().size() );

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

            parser.setInput(
                SearchResultReferenceTest.class.getResource( "response_with_2_controls.xml" ).openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        SearchResultReference searchResultReference = ( ( SearchResponse ) parser.getBatchResponse()
            .getCurrentResponse().getDecorated() ).getCurrentSearchResultReference();
        Map<String, Control> controls = searchResultReference.getControls();

        assertEquals( 2, searchResultReference.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.789" );

        assertNotNull( control );
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

            parser.setInput( SearchResultReferenceTest.class.getResource( "response_with_3_controls_without_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        SearchResultReference searchResultReference = ( ( SearchResponse ) parser.getBatchResponse()
            .getCurrentResponse().getDecorated() ).getCurrentSearchResultReference();
        Map<String, Control> controls = searchResultReference.getControls();

        assertEquals( 3, searchResultReference.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.456" );

        assertNotNull( control );
        assertTrue( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.456", control.getOid() );
        assertFalse( ( ( DsmlControl<?> ) control ).hasValue() );
    }


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

            parser.setInput( SearchResultReferenceTest.class.getResource( "response_with_requestID_attribute.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        SearchResultReference searchResultReference = ( ( SearchResponse ) parser.getBatchResponse()
            .getCurrentResponse().getDecorated() ).getCurrentSearchResultReference();

        assertEquals( 456, searchResultReference.getMessageId() );
    }


    /**
     * Test parsing of a Response with the (optional) requestID attribute below 0
     */
    @Test
    public void testResponseWithRequestIdBelow0()
    {
        testParsingFail( SearchResultReferenceTest.class, "response_with_requestID_below_0.xml" );
    }


    /**
     * Test parsing of a response with 0 Ref
     */
    @Test
    public void testResponseWith0Ref()
    {
        testParsingFail( SearchResultReferenceTest.class, "response_with_0_ref.xml" );
    }


    /**
     * Test parsing of a Response with 1 Ref
     */
    @Test
    public void testResponseWith1Ref()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( SearchResultReferenceTest.class.getResource( "response_with_1_ref.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        SearchResultReference searchResultReference = ( ( SearchResponse ) parser.getBatchResponse()
            .getCurrentResponse().getDecorated() ).getCurrentSearchResultReference();

        Collection<String> references = searchResultReference.getReferral().getLdapUrls();

        assertEquals( 1, references.size() );

        try
        {
            assertTrue( references.contains( new LdapUrl( "ldap://localhost" ).toString() ) );
        }
        catch ( LdapURLEncodingException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a Response with 1 Ref
     */
    @Test
    public void testResponseWith1EmptyRef()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( SearchResultReferenceTest.class.getResource( "response_with_1_empty_ref.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        SearchResultReference searchResultReference = ( ( SearchResponse ) parser.getBatchResponse()
            .getCurrentResponse().getDecorated() ).getCurrentSearchResultReference();

        Collection<String> references = searchResultReference.getReferral().getLdapUrls();

        assertEquals( 0, references.size() );
    }


    /**
     * Test parsing of a Response with 2 Ref
     */
    @Test
    public void testResponseWith2Ref()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser( getCodec() );

            parser.setInput( SearchResultReferenceTest.class.getResource( "response_with_2_ref.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        SearchResultReference searchResultReference = ( ( SearchResponse ) parser.getBatchResponse()
            .getCurrentResponse().getDecorated() ).getCurrentSearchResultReference();

        Collection<String> references = searchResultReference.getReferral().getLdapUrls();

        assertEquals( 2, references.size() );

        try
        {
            assertTrue( references.contains( new LdapUrl( "ldap://localhost" ).toString() ) );
        }
        catch ( LdapURLEncodingException e )
        {
            fail();
        }

        try
        {
            assertTrue( references.contains( new LdapUrl( "ldap://www.apache.org" ).toString() ) );
        }
        catch ( LdapURLEncodingException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with 1 wrong Ref
     */
    @Test
    public void testResponseWith1WrongRef()
    {
        testParsingFail( SearchResultReferenceTest.class, "response_with_1_wrong_ref.xml" );
    }
}
