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
package org.apache.directory.api.ldap.codec.add;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Container;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.codec.api.ResponseCarryingException;
import org.apache.directory.api.ldap.codec.decorators.AddRequestDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddResponseImpl;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Test the AddRequest codec
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class AddRequestTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of a AddRequest
     */
    @Test
    public void testDecodeAddRequestSuccess() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x59 );

        stream.put( new byte[]
            {
                0x30,
                0x57, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x52, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x2E, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x0c, // attribute 1
                0x04,
                0x01,
                'l', // type AttributeDescription,
                0x31,
                0x07, // vals SET OF AttributeValue }
                0x04,
                0x05,
                'P',
                'a',
                'r',
                'i',
                's',

                0x30,
                0x1E, // attribute 2
                // type AttributeDescription,
                0x04,
                0x05,
                'a',
                't',
                't',
                'r',
                's',
                0x31,
                0x15, // vals SET
                // OF
                // AttributeValue
                // }
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '1',
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '2',
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '3', } );

        Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<AddRequestDecorator> container =
            new LdapMessageContainer<AddRequestDecorator>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        AddRequest addRequest = container.getMessage();

        // Check the decoded message
        assertEquals( 1, addRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", addRequest.getEntryDn().toString() );

        Entry entry = addRequest.getEntry();

        assertEquals( 2, entry.size() );

        Set<String> expectedTypes = new HashSet<String>();

        expectedTypes.add( "l" );
        expectedTypes.add( "attrs" );

        Map<String, Set<String>> typesVals = new HashMap<String, Set<String>>();

        Set<String> lVal1 = new HashSet<String>();
        lVal1.add( "Paris" );
        typesVals.put( "l", lVal1 );

        Set<String> lVal2 = new HashSet<String>();
        lVal2.add( "test1" );
        lVal2.add( "test2" );
        lVal2.add( "test3" );
        typesVals.put( "attrs", lVal2 );

        Attribute attribute = entry.get( "l" );

        assertTrue( expectedTypes.contains( Strings.toLowerCaseAscii( attribute.getId() ) ) );

        Set<String> vals = ( Set<String> ) typesVals.get( Strings.toLowerCaseAscii( attribute.getId() ) );

        for ( Value value : attribute )
        {
            assertTrue( vals.contains( value.getValue() ) );

            vals.remove( value.getValue() );
        }

        attribute = entry.get( "attrs" );

        assertTrue( expectedTypes.contains( Strings.toLowerCaseAscii( attribute.getId() ) ) );

        vals = ( Set<String> ) typesVals.get( Strings.toLowerCaseAscii( attribute.getId() ) );

        for ( Value value : attribute )
        {
            assertTrue( vals.contains( value.getValue() ) );

            vals.remove( value.getValue() );
        }

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( addRequest );

            // Check the length
            assertEquals( 0x59, bb.limit() );

            // We cannot compare the PDU, as the attributes order is not
            // kept. Let's decode again and compare the resulting AddRequest
            try
            {
                ldapDecoder.decode( bb, container );
            }
            catch ( DecoderException de )
            {
                de.printStackTrace();
                fail( de.getMessage() );
            }

            AddRequest addRequest2 = container.getMessage();
            assertEquals( addRequest, addRequest2 );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a AddRequest with a null body
     */
    @Test
    public void testDecodeAddRequestNullBody()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        stream.put( new byte[]
            { 0x30, 0x05, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x00 // CHOICE { ..., addRequest AddRequest, ...
        } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddRequest with a null entry
     */
    @Test
    public void testDecodeAddRequestNullEntry()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x39 );

        stream.put( new byte[]
            { 0x30,
                0x37, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x26, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
                // entry LDAPDN,
                0x04,
                0x00,
                // attributes AttributeList }
                0x30,
                0x2E, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x0c, // attribute 1
                0x04,
                0x01,
                'l', // type AttributeDescription,
                0x31,
                0x07, // vals SET OF AttributeValue }
                0x04,
                0x05,
                'P',
                'a',
                'r',
                'i',
                's',

                0x30,
                0x1E, // attribute 2
                // type AttributeDescription,
                0x04,
                0x05,
                'a',
                't',
                't',
                'r',
                's',
                0x31,
                0x15, // vals SET
                // OF
                // AttributeValue
                // }
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '1',
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '2',
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '3', } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( de instanceof ResponseCarryingException );
            Message response = ( ( ResponseCarryingException ) de ).getResponse();
            assertTrue( response instanceof AddResponseImpl );
            assertEquals( ResultCodeEnum.NAMING_VIOLATION, ( ( AddResponseImpl ) response ).getLdapResult()
                .getResultCode() );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddRequest
     */
    @Test
    public void testDecodeAddRequestbadDN()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x59 );

        stream.put( new byte[]
            {
                0x30,
                0x57, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x52, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x2E, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x0c, // attribute 1
                0x04,
                0x01,
                'l', // type AttributeDescription,
                0x31,
                0x07, // vals SET OF AttributeValue }
                0x04,
                0x05,
                'P',
                'a',
                'r',
                'i',
                's',

                0x30,
                0x1E, // attribute 2
                // type AttributeDescription,
                0x04,
                0x05,
                'a',
                't',
                't',
                'r',
                's',
                0x31,
                0x15, // vals SET
                // OF
                // AttributeValue
                // }
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '1',
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '2',
                0x04,
                0x05,
                't',
                'e',
                's',
                't',
                '3', } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( de instanceof ResponseCarryingException );
            Message response = ( ( ResponseCarryingException ) de ).getResponse();
            assertTrue( response instanceof AddResponseImpl );
            assertEquals( ResultCodeEnum.INVALID_DN_SYNTAX, ( ( AddResponseImpl ) response ).getLdapResult()
                .getResultCode() );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddRequest with a null attributeList
     */
    @Test
    public void testDecodeAddRequestNullAttributes()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2B );

        stream.put( new byte[]
            { 0x30,
                0x29, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x24, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x00, // AttributeList ::= SEQUENCE OF SEQUENCE {
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddRequest with a empty attributeList
     */
    @Test
    public void testDecodeAddRequestNullAttributeList()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2D );

        stream.put( new byte[]
            { 0x30,
                0x2B, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x26, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x02, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x00, // AttributeList ::= SEQUENCE OF SEQUENCE {
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddRequest with a empty attributeList
     */
    @Test
    public void testDecodeAddRequestNullType()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2F );

        stream.put( new byte[]
            { 0x30,
                0x2D, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x28, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x04, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x02, // attribute 1
                0x04,
                0x00, // type AttributeDescription,
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( de instanceof ResponseCarryingException );
            Message response = ( ( ResponseCarryingException ) de ).getResponse();
            assertTrue( response instanceof AddResponseImpl );
            assertEquals( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, ( ( AddResponseImpl ) response ).getLdapResult()
                .getResultCode() );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddRequest with a empty attributeList
     */
    @Test
    public void testDecodeAddRequestNoVals()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x30 );

        stream.put( new byte[]
            { 0x30,
                0x2E, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x29, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x05, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x03, // attribute 1
                0x04,
                0x01,
                'A', // type AttributeDescription,
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddRequest with a empty attributeList
     */
    @Test
    public void testDecodeAddRequestNullVals()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x32 );

        stream.put( new byte[]
            { 0x30,
                0x30, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x2B, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x07, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x05, // attribute 1
                0x04,
                0x01,
                'A', // type AttributeDescription,
                0x31,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddRequest with a empty attributeList
     */
    @Test
    public void testDecodeAddRequestEmptyAttributeValue() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x34 );

        stream.put( new byte[]
            { 0x30,
                0x32, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x2D, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x09, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x07, // attribute 1
                0x04,
                0x01,
                'l', // type AttributeDescription,
                0x31,
                0x02,
                0x04,
                0x00 } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<AddRequestDecorator> container = new LdapMessageContainer<AddRequestDecorator>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        AddRequest addRequest = container.getMessage();

        // Check the decoded message
        assertEquals( 1, addRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", addRequest.getEntryDn().toString() );

        Entry entry = addRequest.getEntry();

        assertEquals( 1, entry.size() );

        Attribute attribute = entry.get( "l" );

        assertEquals( "l", Strings.toLowerCaseAscii( attribute.getId() ) );

        for ( Value value : attribute )
        {
            assertEquals( "", value.getValue() );
        }

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( addRequest );

            // Check the length
            assertEquals( 0x34, bb.limit() );

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
     * Test the decoding of a AddRequest with a empty attributeList and a
     * control
     */
    @Test
    public void testDecodeAddRequestEmptyAttributeValueWithControl() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x51 );

        stream.put( new byte[]
            {
                0x30,
                0x4F, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x68,
                0x2D, // CHOICE { ..., addRequest AddRequest, ...
                // AddRequest ::= [APPLICATION 8] SEQUENCE {
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
                // attributes AttributeList }
                0x30,
                0x09, // AttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x07, // attribute 1
                0x04,
                0x01,
                'l', // type AttributeDescription,
                0x31,
                0x02,
                0x04,
                0x00,
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
        LdapMessageContainer<AddRequestDecorator> container = new LdapMessageContainer<AddRequestDecorator>( codec );

        // Decode a AddRequest message
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        AddRequest addRequest = container.getMessage();

        // Check the decoded message
        assertEquals( 1, addRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", addRequest.getEntryDn().toString() );

        Entry entry = addRequest.getEntry();

        assertEquals( 1, entry.size() );

        Attribute attribute = entry.get( "l" );

        assertEquals( "l", Strings.toLowerCaseAscii( attribute.getId() ) );

        for ( Value value : attribute )
        {
            assertEquals( "", value.getValue() );
        }

        // Check the Control
        Map<String, Control> controls = addRequest.getControls();

        assertEquals( 1, controls.size() );

        assertTrue( addRequest.hasControl( "2.16.840.1.113730.3.4.2" ) );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) controls
            .get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( ( byte[] ) control.getValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( addRequest );

            // Check the length
            assertEquals( 0x51, bb.limit() );

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
