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
package org.apache.directory.api.ldap.codec.search;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.SearchResultEntryDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Test the SearchResultEntry codec
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class SearchResultEntryTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of a SearchResultEntry
     */
    @Test
    public void testDecodeSearchResultEntrySuccess() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x50 );

        stream.put( new byte[]
            {

                0x30,
                0x4e, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x49, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x2a,
                0x30,
                0x28,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                // vals SET OF AttributeValue }
                0x31,
                0x19,
                // AttributeValue ::= OCTET STRING
                0x04,
                0x03,
                't',
                'o',
                'p',
                // AttributeValue ::= OCTET STRING
                0x04,
                0x12,
                'o',
                'r',
                'g',
                'a',
                'n',
                'i',
                'z',
                'a',
                't',
                'i',
                'o',
                'n',
                'a',
                'l',
                'U',
                'n',
                'i',
                't' } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultEntry.getMessageId() );
        assertEquals( "ou=contacts,dc=iktek,dc=com", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 1, entry.size() );

        for ( int i = 0; i < entry.size(); i++ )
        {
            Attribute attribute = entry.get( "objectclass" );

            assertEquals( Strings.toLowerCaseAscii( "objectClass" ), Strings.toLowerCaseAscii( attribute.getUpId() ) );

            assertTrue( attribute.contains( "top" ) );
            assertTrue( attribute.contains( "organizationalUnit" ) );
        }

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

            // Check the length
            assertEquals( 0x50, bb.limit() );

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
     * Test the decoding of a SearchResultEntry
     */
    @Test
    public void testDecodeSearchResultEntry2AttrsSuccess() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x7b );

        stream.put( new byte[]
            {
                0x30,
                0x79, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x74, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x55,
                0x30,
                0x28,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                // vals SET OF AttributeValue }
                0x31,
                0x19,
                // AttributeValue ::= OCTET STRING
                0x04,
                0x03,
                't',
                'o',
                'p',
                // AttributeValue ::= OCTET STRING
                0x04,
                0x12,
                'o',
                'r',
                'g',
                'a',
                'n',
                'i',
                'z',
                'a',
                't',
                'i',
                'o',
                'n',
                'a',
                'l',
                'U',
                'n',
                'i',
                't',
                0x30,
                0x29,
                // type AttributeDescription,
                0x04,
                0x0c,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                '2',
                // vals SET OF AttributeValue }
                0x31,
                0x19,
                // AttributeValue ::= OCTET STRING
                0x04,
                0x03,
                't',
                'o',
                'p',
                // AttributeValue ::= OCTET STRING
                0x04,
                0x12,
                'o',
                'r',
                'g',
                'a',
                'n',
                'i',
                'z',
                'a',
                't',
                'i',
                'o',
                'n',
                'a',
                'l',
                'U',
                'n',
                'i',
                't' } );

        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultEntry.getMessageId() );
        assertEquals( "ou=contacts,dc=iktek,dc=com", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 2, entry.size() );

        String[] expectedAttributes = new String[]
            { "objectClass", "objectClass2" };

        for ( int i = 0; i < expectedAttributes.length; i++ )
        {
            Attribute attribute = entry.get( expectedAttributes[i] );

            assertEquals(
                Strings.toLowerCaseAscii( expectedAttributes[i] ),
                Strings.toLowerCaseAscii( attribute.getUpId() ) );

            assertTrue( attribute.contains( "top" ) );
            assertTrue( attribute.contains( "organizationalUnit" ) );
        }

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

            // Check the length
            assertEquals( 0x7B, bb.limit() );

            // We can't compare the encodings, the order of the attributes has
            // changed
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a SearchResultEntry with more bytes to be decoded at
     * the end
     */
    @Test
    public void testDecodeSearchResultEntrySuccessWithFollowingMessage() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x66 );

        stream.put( new byte[]
            {
                0x30,
                0x5F, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x02, // messageID MessageID
                0x64,
                0x5A, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x13,
                'u',
                'i',
                'd',
                '=',
                'a',
                'd',
                'm',
                'i',
                'n',
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
                // attributes PartialAttributeList }
                0x30,
                0x43, // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x41,
                // type AttributeDescription,
                0x04,
                0x0B,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                0x31,
                0x32, // vals
                // SET
                // OF
                // AttributeValue
                // }
                // AttributeValue ::= OCTET STRING
                0x04,
                0x0D,
                'i',
                'n',
                'e',
                't',
                'O',
                'r',
                'g',
                'P',
                'e',
                'r',
                's',
                'o',
                'n',
                // AttributeValue ::= OCTET STRING
                0x04,
                0x14,
                'o',
                'r',
                'g',
                'a',
                'n',
                'i',
                'z',
                'a',
                't',
                'i',
                'o',
                'n',
                'a',
                'l',
                'P',
                'e',
                'r',
                's',
                'o',
                'n',
                // AttributeValue ::= OCTET STRING
                0x04,
                0x06,
                'p',
                'e',
                'r',
                's',
                'o',
                'n',
                // AttributeValue ::= OCTET STRING
                0x04,
                0x03,
                't',
                'o',
                'p',
                0x30,
                0x45, // Start of the next
                // message
                0x02,
                0x01,
                0x02 // messageID MessageID ...
        } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 2, searchResultEntry.getMessageId() );
        assertEquals( "uid=admin,ou=system", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 1, entry.size() );

        for ( int i = 0; i < entry.size(); i++ )
        {
            Attribute attribute = entry.get( "objectclass" );

            assertEquals( Strings.toLowerCaseAscii( "objectClass" ), Strings.toLowerCaseAscii( attribute.getUpId() ) );

            assertTrue( attribute.contains( "top" ) );
            assertTrue( attribute.contains( "person" ) );
            assertTrue( attribute.contains( "organizationalPerson" ) );
            assertTrue( attribute.contains( "inetOrgPerson" ) );
        }

        // Check that the next bytes is the first of the next PDU
        assertEquals( 0x30, stream.get( stream.position() ) );
        assertEquals( 0x45, stream.get( stream.position() + 1 ) );
        assertEquals( 0x02, stream.get( stream.position() + 2 ) );
        assertEquals( 0x01, stream.get( stream.position() + 3 ) );
        assertEquals( 0x02, stream.get( stream.position() + 4 ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

            // Check the length
            assertEquals( 0x61, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            // We have to supress the last 5 chars from the decodedPDU, as they
            // belongs to the next message.
            assertEquals( encodedPdu, decodedPdu.substring( 0, 0x61 * 5 ) );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    // Defensive tests

    /**
     * Test the decoding of an empty SearchResultEntry
     */
    @Test
    public void testDecodeSearchResultEntryEmpty()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        stream.put( new byte[]
            {

                0x30, 0x05, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x00 // CHOICE { ..., searchResEntry SearchResultEntry,
            // ...
        } );

        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

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
     * Test the decoding of an SearchResultEntry with an empty object name
     */
    @Test
    public void testDecodeSearchResultEntryEmptyObjectName()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x09 );

        stream.put( new byte[]
            { 0x30, 0x07, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x02, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x00

        } );

        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

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
     * Test the decoding of an SearchResultEntry with an object name alone
     */
    @Test
    public void testDecodeSearchResultEntryObjectNameAlone()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x24 );

        stream.put( new byte[]
            { 0x30,
                0x22, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x1D, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1B,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',

        } );

        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

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
     * Test the decoding of an SearchResultEntry with an empty attributes
     */
    @Test
    public void testDecodeSearchResultEntryEmptyAttributes()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x26 );

        stream.put( new byte[]
            { 0x30,
                0x24, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x1F, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1B,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x00 } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultEntry.getMessageId() );
        assertEquals( "ou=contacts,dc=iktek,dc=com", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 0, entry.size() );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

            // Check the length
            assertEquals( 0x26, bb.limit() );

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
     * Test the decoding of an SearchResultEntry with an empty attributes list
     */
    @Test
    public void testDecodeSearchResultEntryEmptyAttributeList()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x28 );

        stream.put( new byte[]
            { 0x30,
                0x26, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x21, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1B,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x02,
                0x30,
                0x00 } );

        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

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
     * Test the decoding of an SearchResultEntry with an empty attributes list
     * with controls
     */
    @Test
    public void testDecodeSearchResultEntryEmptyAttributeListWithControls()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x45 );

        stream.put( new byte[]
            {
                0x30,
                0x43, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x21, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1B,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x02,
                0x30,
                0x00,
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

        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

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
     * Test the decoding of a SearchResultEntry with an empty type
     */
    @Test
    public void testDecodeSearchResultEntryEmptyType()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2A );

        stream.put( new byte[]
            {

                0x30,
                0x28, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x23, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x04,
                0x30,
                0x02,
                // type AttributeDescription,
                0x04,
                0x00 } );

        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

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
     * Test the decoding of a SearchResultEntry with a type alone
     */
    @Test
    public void testDecodeSearchResultEntryTypeAlone()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x35 );

        stream.put( new byte[]
            {

                0x30,
                0x33, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x2E, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x0F,
                0x30,
                0x0D,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's' } );

        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

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
     * Test the decoding of a SearchResultEntry with an empty vals
     */
    @Test
    public void testDecodeSearchResultEntryEmptyVals() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x37 );

        stream.put( new byte[]
            {

                0x30,
                0x35, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x30, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x11,
                0x30,
                0x0F,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                0x31,
                0x00 } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultEntry.getMessageId() );
        assertEquals( "ou=contacts,dc=iktek,dc=com", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 1, entry.size() );

        for ( int i = 0; i < entry.size(); i++ )
        {
            Attribute attribute = entry.get( "objectclass" );

            assertEquals( Strings.toLowerCaseAscii( "objectClass" ), Strings.toLowerCaseAscii( attribute.getUpId() ) );
            assertEquals( 0, attribute.size() );
        }

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

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
     * Test the decoding of a SearchResultEntry with two empty vals
     */
    @Test
    public void testDecodeSearchResultEntryEmptyVals2() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x48 );

        stream.put( new byte[]
            {

                0x30,
                0x46, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x41, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x22,
                0x30,
                0x0F,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                0x31,
                0x00,
                0x30,
                0x0F,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                'z',
                'z',
                0x31,
                0x00 } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultEntry.getMessageId() );
        assertEquals( "ou=contacts,dc=iktek,dc=com", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 2, entry.size() );

        Attribute attribute = entry.get( "objectclass" );
        assertEquals( Strings.toLowerCaseAscii( "objectClass" ), Strings.toLowerCaseAscii( attribute.getUpId() ) );
        assertEquals( 0, attribute.size() );

        attribute = entry.get( "objectclazz" );
        assertEquals( Strings.toLowerCaseAscii( "objectClazz" ), Strings.toLowerCaseAscii( attribute.getUpId() ) );
        assertEquals( 0, attribute.size() );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

            // Check the length
            assertEquals( 0x48, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            assertEquals( encodedPdu.length(), decodedPdu.length() );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a SearchResultEntry with an empty vals with controls
     */
    @Test
    public void testDecodeSearchResultEntryEmptyValsWithControls() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x54 );

        stream.put( new byte[]
            {

                0x30,
                0x52, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x30, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x11,
                0x30,
                0x0F,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                0x31,
                0x00,
                ( byte ) 0xA0,
                0x1B, // A
                // control
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

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultEntry.getMessageId() );
        assertEquals( "ou=contacts,dc=iktek,dc=com", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 1, entry.size() );

        for ( int i = 0; i < entry.size(); i++ )
        {
            Attribute attribute = entry.get( "objectclass" );

            assertEquals( Strings.toLowerCaseAscii( "objectClass" ), Strings.toLowerCaseAscii( attribute.getUpId() ) );

            assertEquals( 0, attribute.size() );
        }

        // Check the Control
        Map<String, Control> controls = searchResultEntry.getControls();

        assertEquals( 1, controls.size() );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) controls
            .get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( ( byte[] ) control.getValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

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
     * Test the decoding of a SearchResultEntry with an empty attribute value
     */
    @Test
    public void testDecodeSearchResultEntryEmptyAttributeValue() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x39 );

        stream.put( new byte[]
            { 0x30,
                0x37, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x32, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x13,
                0x30,
                0x11,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                // vals SET OF AttributeValue }
                0x31,
                0x02,
                // AttributeValue ::= OCTET STRING
                0x04,
                0x00, } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultEntry.getMessageId() );
        assertEquals( "ou=contacts,dc=iktek,dc=com", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 1, entry.size() );

        for ( int i = 0; i < entry.size(); i++ )
        {
            Attribute attribute = entry.get( "objectclass" );

            assertEquals( Strings.toLowerCaseAscii( "objectClass" ), Strings.toLowerCaseAscii( attribute.getUpId() ) );

            assertTrue( attribute.contains( "" ) );
        }

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

            // Check the length
            assertEquals( 0x39, bb.limit() );

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
     * Test the decoding of a SearchResultEntry with an empty attribute value
     * with controls
     */
    @Test
    public void testDecodeSearchResultEntryEmptyAttributeValueWithControls() throws NamingException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x56 );

        stream.put( new byte[]
            {
                0x30,
                0x54, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x64,
                0x32, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                // SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
                // objectName LDAPDN,
                0x04,
                0x1b,
                'o',
                'u',
                '=',
                'c',
                'o',
                'n',
                't',
                'a',
                'c',
                't',
                's',
                ',',
                'd',
                'c',
                '=',
                'i',
                'k',
                't',
                'e',
                'k',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                // attributes PartialAttributeList }
                // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
                0x30,
                0x13,
                0x30,
                0x11,
                // type AttributeDescription,
                0x04,
                0x0b,
                'o',
                'b',
                'j',
                'e',
                'c',
                't',
                'c',
                'l',
                'a',
                's',
                's',
                // vals SET OF AttributeValue }
                0x31,
                0x02,
                // AttributeValue ::= OCTET STRING
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

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultEntryDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultEntryDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultEntry searchResultEntry = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultEntry.getMessageId() );
        assertEquals( "ou=contacts,dc=iktek,dc=com", searchResultEntry.getObjectName().toString() );

        Entry entry = searchResultEntry.getEntry();

        assertEquals( 1, entry.size() );

        for ( int i = 0; i < entry.size(); i++ )
        {
            Attribute attribute = entry.get( "objectclass" );

            assertEquals( Strings.toLowerCaseAscii( "objectClass" ), Strings.toLowerCaseAscii( attribute.getUpId() ) );

            assertTrue( attribute.contains( "" ) );
        }

        // Check the Control
        Map<String, Control> controls = searchResultEntry.getControls();

        assertEquals( 1, controls.size() );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) controls
            .get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( ( byte[] ) control.getValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultEntry );

            // Check the length
            assertEquals( 0x56, bb.limit() );

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
