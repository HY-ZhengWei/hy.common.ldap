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

package org.apache.directory.api.dsmlv2.compareRequest;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.apache.directory.api.dsmlv2.AbstractTest;
import org.apache.directory.api.dsmlv2.DsmlControl;
import org.apache.directory.api.dsmlv2.Dsmlv2Parser;
import org.apache.directory.api.ldap.model.message.CompareRequest;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Tests for the Compare Request parsing
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class CompareRequestTest extends AbstractTest
{
    /**
     * Test parsing of a request without the dn attribute
     */
    @Test
    public void testRequestWithoutDn()
    {
        testParsingFail( CompareRequestTest.class, "request_without_dn_attribute.xml" );
    }


    /**
     * Test parsing of a request with the dn attribute
     */
    @Test
    public void testRequestWithDn()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser.setInput( CompareRequestTest.class.getResource( "request_with_dn_attribute.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();

        assertTrue( compareRequest.getName().equals( "cn=Bob Rush,ou=Dev,dc=Example,dc=COM" ) );
    }


    /**
     * Test parsing of a request with the (optional) requestID attribute
     */
    @Test
    public void testRequestWithRequestId()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser.setInput( CompareRequestTest.class.getResource( "request_with_requestID_attribute.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();

        assertEquals( 456, compareRequest.getMessageId() );
    }


    /**
     * Test parsing of a request with the (optional) requestID attribute below 0
     */
    @Test
    public void testRequestWithRequestIdBelow0()
    {
        testParsingFail( CompareRequestTest.class, "request_with_requestID_below_0.xml" );
    }


    /**
     * Test parsing of a request with a (optional) Control element
     */
    @Test
    public void testRequestWith1Control()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser
                .setInput( CompareRequestTest.class.getResource( "request_with_1_control.xml" ).openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();
        Map<String, Control> controls = compareRequest.getControls();

        assertEquals( 1, compareRequest.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.643" );

        assertNotNull( control );
        assertTrue( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.643", control.getOid() );
        assertEquals( "Some text", Strings.utf8ToString( ( ( DsmlControl<?> ) control ).getValue() ) );
    }


    /**
     * Test parsing of a request with a (optional) Control element with Base64 value
     */
    @Test
    public void testRequestWith1ControlBase64Value()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser
                .setInput( CompareRequestTest.class.getResource( "request_with_1_control.xml" ).openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();
        Map<String, Control> controls = compareRequest.getControls();

        assertEquals( 1, compareRequest.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.643" );

        assertNotNull( control );
        assertTrue( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.643", control.getOid() );
        assertEquals( "Some text", Strings.utf8ToString( ( ( DsmlControl<?> ) control ).getValue() ) );
    }


    /**
     * Test parsing of a request with a (optional) Control element with empty value
     */
    @Test
    public void testRequestWith1ControlEmptyValue()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser.setInput( CompareRequestTest.class.getResource( "request_with_1_control_empty_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();
        Map<String, Control> controls = compareRequest.getControls();

        assertEquals( 1, compareRequest.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.643" );

        assertNotNull( control );
        assertTrue( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.643", control.getOid() );
        assertFalse( ( ( DsmlControl<?> ) control ).hasValue() );
    }


    /**
     * Test parsing of a request with 2 (optional) Control elements
     */
    @Test
    public void testRequestWith2Controls()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser.setInput( CompareRequestTest.class.getResource( "request_with_2_controls.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();
        Map<String, Control> controls = compareRequest.getControls();

        assertEquals( 2, compareRequest.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.789" );

        assertNotNull( control );
        assertFalse( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.789", control.getOid() );
        assertEquals( "Some other text", Strings.utf8ToString( ( ( DsmlControl<?> ) control ).getValue() ) );
    }


    /**
     * Test parsing of a request with 3 (optional) Control elements without value
     */
    @Test
    public void testRequestWith3ControlsWithoutValue()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser.setInput( CompareRequestTest.class.getResource( "request_with_3_controls_without_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();
        Map<String, Control> controls = compareRequest.getControls();

        assertEquals( 3, compareRequest.getControls().size() );

        Control control = controls.get( "1.2.840.113556.1.4.456" );

        assertNotNull( control );
        assertTrue( control.isCritical() );
        assertEquals( "1.2.840.113556.1.4.456", control.getOid() );
        assertFalse( ( ( DsmlControl<?> ) control ).hasValue() );
    }


    /**
     * Test parsing of a request with an Attr elements with value
     */
    @Test
    public void testRequestWith1CompleteAssertion()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser.setInput( CompareRequestTest.class.getResource( "request_with_1_complete_assertion.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();

        assertTrue( compareRequest.getName().equals( "cn=Bob Rush,ou=Dev,dc=Example,dc=COM" ) );

        assertEquals( "sn", compareRequest.getAttributeId() );

        assertEquals( "Johnson", compareRequest.getAssertionValue().getValue() );
    }


    /**
     * Test parsing of a request with a complete assertion with base64 value
     */
    @Test
    public void testRequestWith1CompleteAssertionBase64Value()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser.setInput( CompareRequestTest.class
                .getResource( "request_with_1_complete_assertion_base64_value.xml" ).openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();

        assertTrue( compareRequest.getName().equals( "cn=Bob Rush,ou=Dev,dc=Example,dc=COM" ) );

        assertEquals( "sn", compareRequest.getAttributeId() );

        assertEquals( "DSMLv2.0 rocks!!", compareRequest.getAssertionValue().getValue() );
    }


    /**
     * Test parsing of a request with an Attr elements with empty value
     */
    @Test
    public void testRequestWith1AssertionEmptyValue()
    {
        Dsmlv2Parser parser = null;
        try
        {
            parser = newParser();

            parser.setInput( CompareRequestTest.class.getResource( "request_with_1_assertion_empty_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareRequest compareRequest = ( CompareRequest ) parser.getBatchRequest().getCurrentRequest();

        assertTrue( compareRequest.getName().equals( "cn=Bob Rush,ou=Dev,dc=Example,dc=COM" ) );

        assertEquals( "sn", compareRequest.getAttributeId() );

        assertNull( compareRequest.getAssertionValue() );
    }


    /**
     * Test parsing of a request without the Assertion element
     */
    @Test
    public void testRequestWithoutAssertion()
    {
        testParsingFail( CompareRequestTest.class, "request_without_assertion.xml" );
    }


    /**
     * Test parsing of a request with 2 complete Assertion element
     */
    @Test
    public void testRequestWith2CompleteAssertions()
    {
        testParsingFail( CompareRequestTest.class, "request_with_2_complete_assertions.xml" );
    }


    /**
     * Test parsing of a request with 1 Assertion element without the name attribute
     */
    @Test
    public void testRequestWith1AssertionWithoutNameAttribute()
    {
        testParsingFail( CompareRequestTest.class, "request_with_1_assertion_without_name_attribute.xml" );
    }


    /**
     * Test parsing of a request with 1 Assertion element without the Value element
     */
    @Test
    public void testRequestWith1AssertionWithoutValue()
    {
        testParsingFail( CompareRequestTest.class, "request_with_1_assertion_without_value.xml" );
    }


    /**
     * Test parsing of a request with 1 Assertion element with 2 Value elements
     */
    @Test
    public void testRequestWith1AssertionWith2Value()
    {
        testParsingFail( CompareRequestTest.class, "request_with_1_assertion_with_2_values.xml" );
    }


    /**
     * Test parsing of a request with a needed requestID attribute
     * 
     * DIRSTUDIO-1
     */
    @Test
    public void testRequestWithNeededRequestId()
    {
        testParsingFail( CompareRequestTest.class, "request_with_needed_requestID.xml" );
    }
}
