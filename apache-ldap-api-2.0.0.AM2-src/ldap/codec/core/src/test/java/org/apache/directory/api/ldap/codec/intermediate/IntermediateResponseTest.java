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
package org.apache.directory.api.ldap.codec.intermediate;


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
import org.apache.directory.api.ldap.codec.decorators.IntermediateResponseDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.IntermediateResponse;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test the IntermediateResponse codec
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class IntermediateResponseTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of a full IntermediateResponse
     */
    @Test
    public void testDecodeIntermediateResponseSuccess()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x1D );

        stream.put( new byte[]
            { 0x30, 0x1B, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x16, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                // responseName [0] LDAPOID,
                ( byte ) 0x80,
                0x0D,
                '1',
                '.',
                '3',
                '.',
                '6',
                '.',
                '1',
                '.',
                '5',
                '.',
                '5',
                '.',
                '2',
                // responseValue [1] OCTET STRING OPTIONAL }
                ( byte ) 0x81,
                0x05,
                'v',
                'a',
                'l',
                'u',
                'e' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode the IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded IntermediateResponse PDU
        IntermediateResponse intermediateResponse = ldapMessageContainer.getMessage();

        assertEquals( 1, intermediateResponse.getMessageId() );
        assertEquals( "1.3.6.1.5.5.2", intermediateResponse.getResponseName() );
        assertEquals( "value", Strings.utf8ToString( intermediateResponse.getResponseValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( intermediateResponse );

            // Check the length
            assertEquals( 0x1D, bb.limit() );

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
     * Test the decoding of a full IntermediateResponse with controls
     */
    @Test
    public void testDecodeIntermediateResponseWithControls()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x3A );

        stream.put( new byte[]
            { 0x30,
                0x38, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x16, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                // responseName [0] LDAPOID,
                ( byte ) 0x80,
                0x0D,
                '1',
                '.',
                '3',
                '.',
                '6',
                '.',
                '1',
                '.',
                '5',
                '.',
                '5',
                '.',
                '2',
                // requestValue [1] OCTET STRING OPTIONAL }
                ( byte ) 0x81,
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
                '2',
                '.',
                '1',
                '6',
                '.',
                '8',
                '4',
                '0',
                '.',
                '1',
                '.',
                '1',
                '1',
                '3',
                '7',
                '3',
                '0',
                '.',
                '3',
                '.',
                '4',
                '.',
                '2' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode the IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded IntermediateResponse PDU
        IntermediateResponse intermediateResponse = ldapMessageContainer.getMessage();

        assertEquals( 1, intermediateResponse.getMessageId() );
        assertEquals( "1.3.6.1.5.5.2", intermediateResponse.getResponseName() );
        assertEquals( "value", Strings.utf8ToString( intermediateResponse.getResponseValue() ) );

        // Check the Control
        Map<String, Control> controls = intermediateResponse.getControls();

        assertEquals( 1, controls.size() );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) controls
            .get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( ( byte[] ) control.getValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( intermediateResponse );

            // Check the length
            assertEquals( 0x3A, bb.limit() );

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
     * Test the decoding of a full IntermediateResponse with no value and with
     * controls
     */
    @Test
    public void testDecodeIntermediateResponseNoValueWithControls()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x33 );

        stream.put( new byte[]
            { 0x30,
                0x31, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x0F, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                // responseName [0] LDAPOID,
                ( byte ) 0x80,
                0x0D,
                '1',
                '.',
                '3',
                '.',
                '6',
                '.',
                '1',
                '.',
                '5',
                '.',
                '5',
                '.',
                '2',
                // requestValue [1] OCTET STRING OPTIONAL }
                ( byte ) 0xA0,
                0x1B, // A control
                0x30,
                0x19,
                0x04,
                0x17,
                '2',
                '.',
                '1',
                '6',
                '.',
                '8',
                '4',
                '0',
                '.',
                '1',
                '.',
                '1',
                '1',
                '3',
                '7',
                '3',
                '0',
                '.',
                '3',
                '.',
                '4',
                '.',
                '2' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode the IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded IntermediateResponse PDU
        IntermediateResponse intermediateResponse = ldapMessageContainer.getMessage();

        assertEquals( 1, intermediateResponse.getMessageId() );
        assertEquals( "1.3.6.1.5.5.2", intermediateResponse.getResponseName() );
        assertEquals( "", Strings.utf8ToString( intermediateResponse.getResponseValue() ) );

        // Check the Control
        Map<String, Control> controls = intermediateResponse.getControls();

        assertEquals( 1, controls.size() );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) controls
            .get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( ( byte[] ) control.getValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( intermediateResponse );

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
     * Test the decoding of an empty IntermediateResponse
     */
    @Test
    public void testDecodeIntermediateResponseEmpty()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        stream.put( new byte[]
            { 0x30, 0x05, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x00, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode a IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
        }
    }


    /**
     * Test the decoding of an empty OID
     */
    @Test
    public void testDecodeEmptyOID()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x09 );

        stream.put( new byte[]
            { 0x30, 0x07, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x02, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                ( byte ) 0x80,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode a IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
        }
    }


    /**
     * Test the decoding of a bad name 
     */
    @Test
    public void testDecodeExtendedBadRequestName()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x16 );

        stream.put( new byte[]
            { 0x30, 0x14, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x0F, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                // responseName [0] LDAPOID,
                ( byte ) 0x80,
                0x0D,
                '1',
                '-',
                '3',
                '.',
                '6',
                '.',
                '1',
                '.',
                '5',
                '.',
                '5',
                '.',
                '2', } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode a IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
        }
    }


    /**
     * Test the decoding of a name only IntermediateResponse
     */
    @Test
    public void testDecodeIntermediateResponseName()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x16 );

        stream.put( new byte[]
            { 0x30, 0x14, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x0F, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                // responseName [0] LDAPOID,
                ( byte ) 0x80,
                0x0D,
                '1',
                '.',
                '3',
                '.',
                '6',
                '.',
                '1',
                '.',
                '5',
                '.',
                '5',
                '.',
                '2', } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode the IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded IntermediateResponse PDU
        IntermediateResponse intermediateResponse = ldapMessageContainer.getMessage();

        assertEquals( 1, intermediateResponse.getMessageId() );
        assertEquals( "1.3.6.1.5.5.2", intermediateResponse.getResponseName() );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( intermediateResponse );

            // Check the length
            assertEquals( 0x16, bb.limit() );

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
     * Test the decoding of an empty value IntermediateResponse
     */
    @Test
    public void testDecodeIntermediateResponseEmptyValue()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x18 );

        stream.put( new byte[]
            { 0x30,
                0x16, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x11, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                // responseName [0] LDAPOID,
                ( byte ) 0x80,
                0x0D,
                '1',
                '.',
                '3',
                '.',
                '6',
                '.',
                '1',
                '.',
                '5',
                '.',
                '5',
                '.',
                '2',
                ( byte ) 0x81,
                0x00 } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode the IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded IntermediateResponse PDU
        IntermediateResponse intermediateResponse = ldapMessageContainer.getMessage();

        assertEquals( 1, intermediateResponse.getMessageId() );
        assertEquals( "1.3.6.1.5.5.2", intermediateResponse.getResponseName() );
        assertEquals( "", Strings.utf8ToString( intermediateResponse.getResponseValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( intermediateResponse );

            // Check the length
            assertEquals( 0x18, bb.limit() );

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
     * Test the decoding of an IntermediateResponse without name
     */
    @Test
    public void testDecodeIntermediateResponseNoName()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0E );

        stream.put( new byte[]
            { 0x30, 0x0C, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x07, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                // responseValue [1] OCTET STRING OPTIONAL,
                ( byte ) 0x81,
                0x05,
                'v',
                'a',
                'l',
                'u',
                'e' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode the IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded IntermediateResponse PDU
        IntermediateResponse intermediateResponse = ldapMessageContainer.getMessage();

        assertEquals( 1, intermediateResponse.getMessageId() );
        assertEquals( "", intermediateResponse.getResponseName() );
        assertEquals( "value", Strings.utf8ToString( intermediateResponse.getResponseValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( intermediateResponse );

            // Check the length
            assertEquals( 0x0E, bb.limit() );

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
     * Test the decoding of an IntermediateResponse with no value
     */
    @Test
    public void testDecodeIntermediateResponseNoValue()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x16 );

        stream.put( new byte[]
            { 0x30, 0x14, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., intermediateResponse IntermediateResponse, ...
                0x79,
                0x0F, // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
                // responseName [0] LDAPOID,
                ( byte ) 0x80,
                0x0D,
                '1',
                '.',
                '3',
                '.',
                '6',
                '.',
                '1',
                '.',
                '5',
                '.',
                '5',
                '.',
                '2', } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<IntermediateResponseDecorator<?>> ldapMessageContainer =
            new LdapMessageContainer<IntermediateResponseDecorator<?>>( codec );

        // Decode the IntermediateResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded IntermediateResponse PDU
        IntermediateResponse intermediateResponse = ldapMessageContainer.getMessage();

        assertEquals( 1, intermediateResponse.getMessageId() );
        assertEquals( "1.3.6.1.5.5.2", intermediateResponse.getResponseName() );
        assertEquals( "", Strings.utf8ToString( intermediateResponse.getResponseValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( intermediateResponse );

            // Check the length
            assertEquals( 0x16, bb.limit() );

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
