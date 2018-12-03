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
package org.apache.directory.api.ldap.codec.compare;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Map;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.ResponseCarryingException;
import org.apache.directory.api.ldap.codec.decorators.CompareRequestDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.message.CompareRequest;
import org.apache.directory.api.ldap.model.message.CompareResponseImpl;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test the CompareRequest codec
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class CompareRequestTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of a full CompareRequest
     */
    @Test
    public void testDecodeCompareRequestSuccess()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x38 );

        stream.put( new byte[]
            { 0x30,
                0x36, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., compareRequest CompareRequest, ...
                0x6E,
                0x31, // CompareRequest ::= [APPLICATION 14] SEQUENCE {
                // entry LDAPDN,
                0x04,
                0x20,
                'c',
                'n',
                '=',
                't',
                'e',
                's',
                't',
                'M',
                'o',
                'd',
                'i',
                'f',
                'y',
                ',',
                'o',
                'u',
                '=',
                'u',
                's',
                'e',
                'r',
                's',
                ',',
                'o',
                'u',
                '=',
                's',
                'y',
                's',
                't',
                'e',
                'm',
                // ava AttributeValueAssertion }
                0x30,
                0x0D, // AttributeValueAssertion ::= SEQUENCE {
                // attributeDesc AttributeDescription,
                0x04,
                0x04,
                't',
                'e',
                's',
                't',
                // assertionValue AssertionValue }
                0x04,
                0x05,
                'v',
                'a',
                'l',
                'u',
                'e' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<CompareRequestDecorator> container = new LdapMessageContainer<CompareRequestDecorator>(
            codec );

        // Decode the CompareRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded CompareRequest PDU
        CompareRequest compareRequest = container.getMessage();

        assertEquals( 1, compareRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", compareRequest.getName().toString() );
        assertEquals( "test", compareRequest.getAttributeId() );
        assertEquals( "value", compareRequest.getAssertionValue().toString() );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( compareRequest );

            // Check the length
            assertEquals( 0x38, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of an empty CompareRequest
     */
    @Test
    public void testDecodeCompareRequestEmptyRequest()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        stream.put( new byte[]
            { 0x30, 0x05, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., compareRequest CompareRequest, ...
                0x6E,
                0x00 // CompareRequest ::= [APPLICATION 14] SEQUENCE {
        } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<CompareRequestDecorator> container = new LdapMessageContainer<CompareRequestDecorator>(
            codec );

        // Decode the CompareRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
        }
    }


    /**
     * Test the decoding of an empty entry CompareRequest
     */
    @Test
    public void testDecodeCompareRequestEmptyEntry()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x18 );

        stream.put( new byte[]
            { 0x30, 0x16, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., compareRequest CompareRequest, ...
                0x6E,
                0x11, // CompareRequest ::= [APPLICATION 14] SEQUENCE {
                0x04,
                0x00, // entry LDAPDN,
                // ava AttributeValueAssertion }
                0x30,
                0x0D, // AttributeValueAssertion ::= SEQUENCE {
                // attributeDesc AttributeDescription,
                0x04,
                0x04,
                't',
                'e',
                's',
                't',
                // assertionValue AssertionValue }
                0x04,
                0x05,
                'v',
                'a',
                'l',
                'u',
                'e' } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<CompareRequestDecorator> container = new LdapMessageContainer<CompareRequestDecorator>(
            codec );

        // Decode the CompareRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
        }
    }


    /**
     * Test the decoding of an empty ava
     */
    @Test
    public void testDecodeCompareRequestEmptyAVA()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2B );

        stream.put( new byte[]
            { 0x30,
                0x29, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., compareRequest CompareRequest, ...
                0x6E,
                0x24, // CompareRequest ::= [APPLICATION 14] SEQUENCE {
                // entry LDAPDN,
                0x04,
                0x20,
                'c',
                'n',
                '=',
                't',
                'e',
                's',
                't',
                'M',
                'o',
                'd',
                'i',
                'f',
                'y',
                ',',
                'o',
                'u',
                '=',
                'u',
                's',
                'e',
                'r',
                's',
                ',',
                'o',
                'u',
                '=',
                's',
                'y',
                's',
                't',
                'e',
                'm',
                // ava AttributeValueAssertion }
                0x30,
                0x00 // AttributeValueAssertion ::= SEQUENCE {
        } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<CompareRequestDecorator> container = new LdapMessageContainer<CompareRequestDecorator>(
            codec );

        // Decode the CompareRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
        }
    }


    /**
     * Test the decoding of an empty ava
     */
    @Test
    public void testDecodeCompareRequestInvalidDN()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2B );

        stream.put( new byte[]
            { 0x30,
                0x29, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., compareRequest CompareRequest, ...
                0x6E,
                0x24, // CompareRequest ::= [APPLICATION 14] SEQUENCE {
                // entry LDAPDN,
                0x04,
                0x20,
                'c',
                'n',
                ':',
                't',
                'e',
                's',
                't',
                'M',
                'o',
                'd',
                'i',
                'f',
                'y',
                ',',
                'o',
                'u',
                '=',
                'u',
                's',
                'e',
                'r',
                's',
                ',',
                'o',
                'u',
                '=',
                's',
                'y',
                's',
                't',
                'e',
                'm',
                // ava AttributeValueAssertion }
                0x30,
                0x00 // AttributeValueAssertion ::= SEQUENCE {
        } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<CompareRequestDecorator> container = new LdapMessageContainer<CompareRequestDecorator>(
            codec );

        // Decode the CompareRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( de instanceof ResponseCarryingException );
            Message response = ( ( ResponseCarryingException ) de ).getResponse();
            assertTrue( response instanceof CompareResponseImpl );
            assertEquals( ResultCodeEnum.INVALID_DN_SYNTAX, ( ( CompareResponseImpl ) response ).getLdapResult()
                .getResultCode() );
            return;
        }
    }


    /**
     * Test the decoding of an empty attributeDesc ava
     */
    @Test
    public void testDecodeCompareRequestEmptyAttributeDesc()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2D );

        stream.put( new byte[]
            { 0x30,
                0x2B, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., compareRequest CompareRequest, ...
                0x6E,
                0x26, // CompareRequest ::= [APPLICATION 14] SEQUENCE {
                // entry LDAPDN,
                0x04,
                0x20,
                'c',
                'n',
                '=',
                't',
                'e',
                's',
                't',
                'M',
                'o',
                'd',
                'i',
                'f',
                'y',
                ',',
                'o',
                'u',
                '=',
                'u',
                's',
                'e',
                'r',
                's',
                ',',
                'o',
                'u',
                '=',
                's',
                'y',
                's',
                't',
                'e',
                'm',
                // ava AttributeValueAssertion }
                0x30,
                0x02, // AttributeValueAssertion ::= SEQUENCE {
                0x04,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<CompareRequestDecorator> container = new LdapMessageContainer<CompareRequestDecorator>(
            codec );

        // Decode the CompareRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( de instanceof ResponseCarryingException );
            Message response = ( ( ResponseCarryingException ) de ).getResponse();
            assertTrue( response instanceof CompareResponseImpl );
            assertEquals( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, ( ( CompareResponseImpl ) response ).getLdapResult()
                .getResultCode() );
            return;
        }
    }


    /**
     * Test the decoding of an empty attributeValue ava
     */
    @Test
    public void testDecodeCompareRequestEmptyAttributeValue()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x33 );

        stream.put( new byte[]
            { 0x30,
                0x31, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., compareRequest CompareRequest, ...
                0x6E,
                0x2C, // CompareRequest ::= [APPLICATION 14] SEQUENCE {
                // entry LDAPDN,
                0x04,
                0x20,
                'c',
                'n',
                '=',
                't',
                'e',
                's',
                't',
                'M',
                'o',
                'd',
                'i',
                'f',
                'y',
                ',',
                'o',
                'u',
                '=',
                'u',
                's',
                'e',
                'r',
                's',
                ',',
                'o',
                'u',
                '=',
                's',
                'y',
                's',
                't',
                'e',
                'm',
                // ava AttributeValueAssertion }
                0x30,
                0x08, // AttributeValueAssertion ::= SEQUENCE {
                // attributeDesc AttributeDescription,
                0x04,
                0x04,
                't',
                'e',
                's',
                't',
                // assertionValue AssertionValue }
                0x04,
                0x00 } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<CompareRequestDecorator> container = new LdapMessageContainer<CompareRequestDecorator>(
            codec );

        // Decode the CompareRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded CompareRequest PDU
        CompareRequest compareRequest = container.getMessage();

        assertEquals( 1, compareRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", compareRequest.getName().toString() );
        assertEquals( "test", compareRequest.getAttributeId() );
        assertEquals( "", compareRequest.getAssertionValue().toString() );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( compareRequest );

            // Check the length
            assertEquals( 0x33, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of an compare request with controls
     */
    @Test
    public void testDecodeCompareRequestWithControls()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x55 );

        stream.put( new byte[]
            {
                0x30,
                0x53, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., compareRequest CompareRequest, ...
                0x6E,
                0x31, // CompareRequest ::= [APPLICATION 14] SEQUENCE {
                // entry LDAPDN,
                0x04,
                0x20,
                'c',
                'n',
                '=',
                't',
                'e',
                's',
                't',
                'M',
                'o',
                'd',
                'i',
                'f',
                'y',
                ',',
                'o',
                'u',
                '=',
                'u',
                's',
                'e',
                'r',
                's',
                ',',
                'o',
                'u',
                '=',
                's',
                'y',
                's',
                't',
                'e',
                'm',
                // ava AttributeValueAssertion }
                0x30,
                0x0D, // AttributeValueAssertion ::= SEQUENCE {
                // attributeDesc AttributeDescription,
                0x04,
                0x04,
                't',
                'e',
                's',
                't',
                // assertionValue AssertionValue }
                0x04,
                0x05,
                'v',
                'a',
                'l',
                'u',
                'e',
                ( byte ) 0xA0,
                0x1B, // A control
                0x30,
                0x19,
                0x04,
                0x17,
                0x32,
                0x2E,
                0x31,
                0x36,
                0x2E,
                0x38,
                0x34,
                0x30,
                0x2E,
                0x31,
                0x2E,
                0x31,
                0x31,
                0x33,
                0x37,
                0x33,
                0x30,
                0x2E,
                0x33,
                0x2E,
                0x34,
                0x2E,
                0x32 } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<CompareRequestDecorator> container = new LdapMessageContainer<CompareRequestDecorator>(
            codec );

        // Decode the CompareRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Ceck the decoded CompareRequest PDU
        CompareRequest compareRequest = container.getMessage();

        assertEquals( 1, compareRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", compareRequest.getName().toString() );
        assertEquals( "test", compareRequest.getAttributeId() );
        assertEquals( "value", compareRequest.getAssertionValue().toString() );

        // Check the Control
        Map<String, Control> controls = compareRequest.getControls();

        assertEquals( 1, controls.size() );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) controls
            .get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( ( byte[] ) control.getValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( compareRequest );

            // Check the length
            assertEquals( 0x55, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }
}
