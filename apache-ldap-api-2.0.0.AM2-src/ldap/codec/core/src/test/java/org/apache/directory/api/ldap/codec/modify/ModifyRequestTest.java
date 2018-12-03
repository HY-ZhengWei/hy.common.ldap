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
package org.apache.directory.api.ldap.codec.modify;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.ResponseCarryingException;
import org.apache.directory.api.ldap.codec.decorators.ModifyRequestDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyResponseImpl;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Test the ModifyRequest codec
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class ModifyRequestTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of a ModifyRequest
     */
    @Test
    public void testDecodeModifyRequest2AttrsSuccess() throws LdapException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x54 );

        stream.put( new byte[]
            { 0x30,
                0x52, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x66,
                0x4d, // CHOICE { ..., modifyRequest ModifyRequest, ...
                // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
                // object LDAPDN,
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
                0x30,
                0x29,
                // modification SEQUENCE OF SEQUENCE {
                0x30,
                0x11,
                0x0A,
                0x01,
                0x02, // operation ENUMERATED {
                // add (0),
                // delete (1),
                // replace (2) },
                // modification AttributeTypeAndValues } }
                0x30,
                0x0c, // AttributeTypeAndValues ::= SEQUENCE {
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
                0x14, // modification SEQUENCE OF *SEQUENCE* {
                0x0A,
                0x01,
                0x00, // operation ENUMERATED {
                // add (0),
                // delete (1),
                // replace (2) },
                // modification AttributeTypeAndValues } }
                0x30,
                0x0f, // AttributeTypeAndValues ::= SEQUENCE {
                // type AttributeDescription,
                0x04,
                0x05,
                'a',
                't',
                't',
                'r',
                's',
                0x31,
                0x06, // vals SET OF AttributeValue }
                0x04,
                0x04,
                't',
                'e',
                's',
                't' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded PDU
        ModifyRequest modifyRequest = ldapMessageContainer.getMessage();

        assertEquals( 1, modifyRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", modifyRequest.getName().toString() );

        Collection<Modification> modifications = modifyRequest.getModifications();

        assertEquals( 2, modifications.size() );

        for ( Modification modification : modifications )
        {
            Attribute attribute = modification.getAttribute();

            if ( "l".equalsIgnoreCase( attribute.getUpId() ) )
            {
                String attrValue = attribute.getString();
                assertEquals( "Paris", attrValue );
            }
            else if ( "attrs".equalsIgnoreCase( attribute.getUpId() ) )
            {
                String attrValue = attribute.getString();
                assertEquals( "test", attrValue );
            }
        }

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( modifyRequest );

            // Check the length
            assertEquals( 0x54, bb.limit() );

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
     * Test the decoding of a ModifyRequest
     */
    @Test
    public void testDecodeModifyRequestBadDN()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x54 );

        stream.put( new byte[]
            { 0x30,
                0x52, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x66,
                0x4d, // CHOICE { ..., modifyRequest ModifyRequest, ...
                // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
                // object LDAPDN,
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
                0x30,
                0x29,
                // modification SEQUENCE OF SEQUENCE {
                0x30,
                0x11,
                0x0A,
                0x01,
                0x02, // operation ENUMERATED {
                // add (0),
                // delete (1),
                // replace (2) },
                // modification AttributeTypeAndValues } }
                0x30,
                0x0c, // AttributeTypeAndValues ::= SEQUENCE {
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
                0x14, // modification SEQUENCE OF *SEQUENCE* {
                0x0A,
                0x01,
                0x00, // operation ENUMERATED {
                // add (0),
                // delete (1),
                // replace (2) },
                // modification AttributeTypeAndValues } }
                0x30,
                0x0f, // AttributeTypeAndValues ::= SEQUENCE {
                // type AttributeDescription,
                0x04,
                0x05,
                'a',
                't',
                't',
                'r',
                's',
                0x31,
                0x06, // vals SET OF AttributeValue }
                0x04,
                0x04,
                't',
                'e',
                's',
                't' } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( de instanceof ResponseCarryingException );
            Message response = ( ( ResponseCarryingException ) de ).getResponse();
            assertTrue( response instanceof ModifyResponseImpl );
            assertEquals( ResultCodeEnum.INVALID_DN_SYNTAX, ( ( ModifyResponseImpl ) response ).getLdapResult()
                .getResultCode() );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a ModifyRequest, with different operations
     */
    @Test
    public void testDecodeModifyRequestManyOperations() throws LdapException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x18C );

        stream.put( new byte[]
            { 0x30, ( byte ) 0x81, ( byte ) 0x89, 0x02, 0x01, 0x15, 0x66,
                0x67,
                0x04,
                0x2B, // ModifyRequest object : cn=Tori Amos,ou=playground,dc=apache,dc=org
                'c',
                'n',
                '=',
                'T',
                'o',
                'r',
                'i',
                ' ',
                'A',
                'm',
                'o',
                's',
                ',',
                'o',
                'u',
                '=',
                'p',
                'l',
                'a',
                'y',
                'g',
                'r',
                'o',
                'u',
                'n',
                'd',
                ',',
                'd',
                'c',
                '=',
                'a',
                'p',
                'a',
                'c',
                'h',
                'e',
                ',',
                'd',
                'c',
                '=',
                'o',
                'r',
                'g',
                0x30,
                0x38, // Modifications
                0x30,
                0x24, // Modification
                0x0A,
                0x01,
                0x00, // Operation = ADD
                0x30,
                0x1F, // type : telephoneNumber
                0x04,
                0x0F,
                't',
                'e',
                'l',
                'e',
                'p',
                'h',
                'o',
                'n',
                'e',
                'N',
                'u',
                'm',
                'b',
                'e',
                'r',
                0x31,
                0x0C, // vals : 1234567890
                0x04,
                0x0A,
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                '0',
                0x30,
                0x10, // Modification
                0x0A,
                0x01,
                0x02, // Operation = REPLACE
                0x30,
                0x0B, // type : cn
                0x04,
                0x02,
                'c',
                'n',
                0x31,
                0x05, // vals : XXX
                0x04,
                0x03,
                'X',
                'X',
                'X',
                ( byte ) 0xA0,
                0x1B, // Control : 2.16.840.1.113730.3.4.2
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

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded PDU
        ModifyRequest modifyRequest = ldapMessageContainer.getMessage();

        assertEquals( 21, modifyRequest.getMessageId() );
        assertEquals( "cn=Tori Amos,ou=playground,dc=apache,dc=org", modifyRequest.getName().toString() );

        Object[] modifications = modifyRequest.getModifications().toArray();

        assertEquals( 2, modifications.length );

        Modification modification = ( Modification ) modifications[0];
        Attribute attributeValue = modification.getAttribute();

        assertEquals( "telephonenumber", Strings.toLowerCaseAscii( attributeValue.getId() ) );

        String attrValue = attributeValue.getString();
        assertEquals( "1234567890", attrValue );

        modification = ( Modification ) modifications[1];
        attributeValue = modification.getAttribute();

        assertEquals( "cn", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );

        attrValue = attributeValue.getString();
        assertEquals( "XXX", attrValue );

        // Check the encoding, by decoding and re-encoding the result
        try
        {
            ByteBuffer bb = encoder.encodeMessage( modifyRequest );

            // Check the length
            assertEquals( 0x8C, bb.limit() );

            String decodedPdu1 = Strings.dumpBytes( bb.array() );

            try
            {
                ldapDecoder.decode( bb, ldapMessageContainer );
            }
            catch ( DecoderException de )
            {
                de.printStackTrace();
                fail( de.getMessage() );
            }

            ModifyRequest modifyRequest2 = ldapMessageContainer.getMessage();

            bb = encoder.encodeMessage( modifyRequest2 );
            String decodedPdu2 = Strings.dumpBytes( bb.array() );

            assertEquals( decodedPdu1, decodedPdu2 );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a ModifyRequest, with different operations, take 2
     */
    @Test
    public void testDecodeModifyRequestManyOperations2() throws LdapException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x18C );

        stream.put( new byte[]
            {
                0x30,
                ( byte ) 0x81,
                ( byte ) 0xB6, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                ( byte ) 0x81,
                ( byte ) 0x93, // ModifyRequest
                0x04,
                0x2B, // object : cn=Tori Amos,ou=playground,dc=apache,dc=org
                'c',
                'n',
                '=',
                'T',
                'o',
                'r',
                'i',
                ' ',
                'A',
                'm',
                'o',
                's',
                ',',
                'o',
                'u',
                '=',
                'p',
                'l',
                'a',
                'y',
                'g',
                'r',
                'o',
                'u',
                'n',
                'd',
                ',',
                'd',
                'c',
                '=',
                'a',
                'p',
                'a',
                'c',
                'h',
                'e',
                ',',
                'd',
                'c',
                '=',
                'o',
                'r',
                'g',
                0x30,
                0x64, // Modifications
                0x30,
                0x14, // Modification
                0x0A,
                0x01,
                0x01, // Operation : Delete
                0x30,
                0x0F, // type : description
                0x04,
                0x0B,
                0x64,
                0x65,
                0x73,
                0x63,
                0x72,
                0x69,
                0x70,
                0x74,
                0x69,
                0x6F,
                0x6E,
                0x31,
                0x00, // Vals = null
                0x30,
                0x25, // Modification
                0x0A,
                0x01,
                0x00, // Operation : Add
                0x30,
                0x20, // type : telephoneNumber
                0x04,
                0x0F,
                't',
                'e',
                'l',
                'e',
                'p',
                'h',
                'o',
                'n',
                'e',
                'N',
                'u',
                'm',
                'b',
                'e',
                'r',
                0x31,
                0x0D, // Vals : 01234567890
                0x04,
                0x0B,
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                '0',
                0x30,
                0x25, // Modification
                0x0A,
                0x01,
                0x00, // Operation : Add
                0x30,
                0x20, // type : telephoneNumber
                0x04,
                0x0F,
                't',
                'e',
                'l',
                'e',
                'p',
                'h',
                'o',
                'n',
                'e',
                'N',
                'u',
                'm',
                'b',
                'e',
                'r',
                0x31,
                0x0D, // Vals : 01234567890
                0x04,
                0x0B,
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                '0',
                ( byte ) 0xA0,
                0x1B, // Controls : 2.16.840.1.113730.3.4.2
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

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded PDU
        ModifyRequest modifyRequest = ldapMessageContainer.getMessage();

        assertEquals( 49, modifyRequest.getMessageId() );
        assertEquals( "cn=Tori Amos,ou=playground,dc=apache,dc=org", modifyRequest.getName().toString() );

        Object[] modifications = modifyRequest.getModifications().toArray();

        assertEquals( 3, modifications.length );

        Modification modification = ( Modification ) modifications[0];
        Attribute attributeValue = modification.getAttribute();

        assertEquals( "description", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );
        assertEquals( 0, attributeValue.size() );

        modification = ( Modification ) modifications[1];
        attributeValue = modification.getAttribute();

        String attrValue = attributeValue.getString();

        assertEquals( "telephonenumber", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );

        assertEquals( "01234567890", attrValue );

        modification = ( Modification ) modifications[2];
        attributeValue = modification.getAttribute();

        attrValue = attributeValue.getString();

        assertEquals( "telephonenumber", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );

        attrValue = attributeValue.getString();
        assertEquals( "01234567890", attrValue );

        // Check the encoding, by decoding and re-encoding the result
        try
        {
            ByteBuffer bb = encoder.encodeMessage( modifyRequest );

            // Check the length
            assertEquals( 0xB9, bb.limit() );

            String decodedPdu1 = Strings.dumpBytes( bb.array() );

            try
            {
                ldapDecoder.decode( bb, ldapMessageContainer );
            }
            catch ( DecoderException de )
            {
                de.printStackTrace();
                fail( de.getMessage() );
            }

            ModifyRequest modifyRequest2 = ldapMessageContainer.getMessage();

            bb = encoder.encodeMessage( modifyRequest2 );
            String decodedPdu2 = Strings.dumpBytes( bb.array() );

            assertEquals( decodedPdu1, decodedPdu2 );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a ModifyRequest
     */
    @Test
    public void testDecodeModifyRequest2Attrs3valsSuccess() throws LdapException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x5C );

        stream.put( new byte[]
            { 0x30,
                0x5A, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x66,
                0x55, // CHOICE { ..., modifyRequest ModifyRequest, ...
                // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
                // object LDAPDN,
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
                0x30,
                0x31, // modification SEQUENCE OF SEQUENCE {
                0x30,
                0x19,
                0x0A,
                0x01,
                0x02, // operation ENUMERATED {
                // add (0),
                // delete (1),
                // replace (2) },
                // modification AttributeTypeAndValues } }
                0x30,
                0x14, // AttributeTypeAndValues ::= SEQUENCE {
                0x04,
                0x01,
                'l', // type AttributeDescription,
                0x31,
                0x0F, // vals SET OF AttributeValue }
                0x04,
                0x05,
                'P',
                'a',
                'r',
                'i',
                's',
                0x04,
                0x06,
                'L',
                'o',
                'n',
                'd',
                'o',
                'n',
                0x30,
                0x14, // modification SEQUENCE OF *SEQUENCE*  {
                0x0A,
                0x01,
                0x00, // operation ENUMERATED {
                // add (0),
                // delete (1),
                // replace (2) },
                // modification AttributeTypeAndValues } }
                0x30,
                0x0f, // AttributeTypeAndValues ::= SEQUENCE {
                // type AttributeDescription,
                0x04,
                0x05,
                'a',
                't',
                't',
                'r',
                's',
                0x31,
                0x06, // vals SET OF AttributeValue }
                0x04,
                0x04,
                't',
                'e',
                's',
                't' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded PDU
        ModifyRequest modifyRequest = ldapMessageContainer.getMessage();

        assertEquals( 1, modifyRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", modifyRequest.getName().toString() );

        Object[] modifications = modifyRequest.getModifications().toArray();

        assertEquals( 2, modifications.length );

        Modification modification = ( Modification ) modifications[0];
        Attribute attributeValue = modification.getAttribute();

        assertEquals( "l", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );

        assertTrue( attributeValue.contains( "Paris" ) );
        assertTrue( attributeValue.contains( "London" ) );

        modification = ( Modification ) modifications[1];
        attributeValue = modification.getAttribute();

        assertEquals( "attrs", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );

        String attrValue = attributeValue.getString();
        assertEquals( "test", attrValue );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( modifyRequest );

            // Check the length
            assertEquals( 0x5C, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    // Defensive tests

    /**
     * Test the decoding of a ModifyRequest with an empty body
     */
    @Test
    public void testDecodeModifyRequestEmptyBody()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        stream.put( new byte[]
            { 0x30, 0x05, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x00 // ModifyRequest
        } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an empty object
     */
    @Test
    public void testDecodeModifyRequestEmptyObject()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x09 );

        stream.put( new byte[]
            { 0x30, 0x07, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x02, // ModifyRequest
                0x04,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an object and nothing else
     */
    @Test
    public void testDecodeModifyRequestObjectAlone()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x29 );

        stream.put( new byte[]
            { 0x30,
                0x27, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x22, // ModifyRequest
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
                'm' } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an empty modification
     */
    @Test
    public void testDecodeModifyRequestEmptyModification()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2B );

        stream.put( new byte[]
            { 0x30,
                0x29, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x24, // ModifyRequest
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
                0x30,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an empty operation
     */
    @Test
    public void testDecodeModifyRequestEmptyOperation()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2D );

        stream.put( new byte[]
            { 0x30,
                0x2B, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x26, // ModifyRequest
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
                0x30,
                0x02,
                0x30,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an wrong empty operation
     */
    @Test
    public void testDecodeModifyRequestWrongOperationEmpty()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2F );

        stream.put( new byte[]
            {
                0x30,
                0x2D, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x28, // ModifyRequest
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
                0x30,
                0x04,
                0x30,
                0x02,
                0x0A,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an wrong operation
     */
    @Test
    public void testDecodeModifyRequestWrongOperation()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x30 );

        stream.put( new byte[]
            {
                0x30,
                0x2E, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x29, // ModifyRequest
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
                0x30,
                0x05,
                0x30,
                0x03,
                0x0A,
                0x01,
                0x04 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an add operation, and nothing
     * more
     */
    @Test
    public void testDecodeModifyRequestAddOperationEnd()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x30 );

        stream.put( new byte[]
            {
                0x30,
                0x2E, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x29, // ModifyRequest
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
                0x30,
                0x05,
                0x30,
                0x03,
                0x0A,
                0x01,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an add operation, and an empty
     * modification
     */
    @Test
    public void testDecodeModifyRequestAddOperationEmptyModification()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x32 );

        stream.put( new byte[]
            {
                0x30,
                0x30, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x2B, // ModifyRequest
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
                0x30,
                0x07,
                0x30,
                0x05,
                0x0A,
                0x01,
                0x00,
                0x30,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an add operation, and a
     * modification with an empty type
     */
    @Test
    public void testDecodeModifyRequestAddOperationModificationEmptyType()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x34 );

        stream.put( new byte[]
            {
                0x30,
                0x32, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x2D, // ModifyRequest
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
                0x30,
                0x09,
                0x30,
                0x07,
                0x0A,
                0x01,
                0x00,
                0x30,
                0x02,
                0x04,
                0x00 } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( de instanceof ResponseCarryingException );
            Message response = ( ( ResponseCarryingException ) de ).getResponse();
            assertTrue( response instanceof ModifyResponseImpl );
            assertEquals( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, ( ( ModifyResponseImpl ) response ).getLdapResult()
                .getResultCode() );
            return;
        }
    }


    /**
     * Test the decoding of a ModifyRequest with an add operation, and a
     * modification with a type and no vals
     */
    @Test
    public void testDecodeModifyRequestAddOperationModificationTypeNoVals()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x35 );

        stream.put( new byte[]
            {
                0x30,
                0x33, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x2E, // ModifyRequest
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
                0x30,
                0x0A,
                0x30,
                0x08,
                0x0A,
                0x01,
                0x00,
                0x30,
                0x03,
                0x04,
                0x01,
                'l' } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
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
     * Test the decoding of a ModifyRequest with an add operation, and a
     * modification with a type and an empty vals
     */
    @Test
    public void testDecodeModifyRequestAddOperationModificationTypeEmptyVals()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x37 );

        stream.put( new byte[]
            {
                0x30,
                0x35, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x30, // ModifyRequest
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
                0x30,
                0x0C,
                0x30,
                0x0A,
                0x0A,
                0x01,
                0x00,
                0x30,
                0x05,
                0x04,
                0x01,
                'l',
                0x31,
                0x00 } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded PDU
        ModifyRequest modifyRequest = ldapMessageContainer.getMessage();

        assertEquals( 49, modifyRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", modifyRequest.getName().toString() );

        Object[] modifications = modifyRequest.getModifications().toArray();

        assertEquals( 1, modifications.length );

        Modification modification = ( Modification ) modifications[0];
        Attribute attributeValue = modification.getAttribute();

        assertEquals( "l", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );
        assertEquals( 0, attributeValue.size() );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( modifyRequest );

            // Check the length
            assertEquals( 0x37, bb.limit() );

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
     * Test the decoding of a ModifyRequest with an add operation, and a
     * modification with a type and an empty vals wuth controls
     */
    @Test
    public void testDecodeModifyRequestAddOperationModificationTypeEmptyValsWithControls()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x54 );

        stream.put( new byte[]
            {
                0x30,
                0x52, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x30, // ModifyRequest
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
                0x30,
                0x0C,
                0x30,
                0x0A,
                0x0A,
                0x01,
                0x00,
                0x30,
                0x05,
                0x04,
                0x01,
                'l',
                0x31,
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
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded PDU
        ModifyRequest modifyRequest = ldapMessageContainer.getMessage();

        assertEquals( 49, modifyRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", modifyRequest.getName().toString() );

        Object[] modifications = modifyRequest.getModifications().toArray();

        assertEquals( 1, modifications.length );

        Modification modification = ( Modification ) modifications[0];
        Attribute attributeValue = modification.getAttribute();

        assertEquals( "l", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );
        assertEquals( 0, attributeValue.size() );

        // Check the Control
        Map<String, Control> controls = modifyRequest.getControls();

        assertEquals( 1, controls.size() );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) modifyRequest
            .getControl( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( ( byte[] ) control.getValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( modifyRequest );

            // Check the length
            assertEquals( 0x54, bb.limit() );

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
     * Test the decoding of a ModifyRequest with an add operation, and a
     * modification with a type and two vals
     */
    @Test
    public void testDecodeModifyRequestAddOperationModificationType2Vals() throws LdapException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x3D );

        stream.put( new byte[]
            {
                0x30,
                0x3B, // LdapMessage
                0x02,
                0x01,
                0x31, // Message ID : 49
                0x66,
                0x36, // ModifyRequest
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
                0x30,
                0x12,
                0x30,
                0x10,
                0x0A,
                0x01,
                0x00,
                0x30,
                0x0B,
                0x04,
                0x01,
                'l',
                0x31,
                0x06,
                0x04,
                0x01,
                'a',
                0x04,
                0x01,
                'b' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<ModifyRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<ModifyRequestDecorator>( codec );

        // Decode a ModifyRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded PDU
        ModifyRequest modifyRequest = ldapMessageContainer.getMessage();

        assertEquals( 49, modifyRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", modifyRequest.getName().toString() );

        Object[] modifications = modifyRequest.getModifications().toArray();

        assertEquals( 1, modifications.length );

        Modification modification = ( Modification ) modifications[0];
        Attribute attributeValue = modification.getAttribute();

        assertEquals( "l", Strings.toLowerCaseAscii( attributeValue.getUpId() ) );
        assertEquals( 2, attributeValue.size() );

        assertTrue( attributeValue.contains( "a" ) );
        assertTrue( attributeValue.contains( "b" ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( modifyRequest );

            // Check the length
            assertEquals( 0x3D, bb.limit() );

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
