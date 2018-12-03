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
package org.apache.directory.api.ldap.codec;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.Asn1Container;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.asn1.ber.tlv.TLVStateEnum;
import org.apache.directory.api.ldap.codec.api.LdapDecoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.codec.api.ResponseCarryingException;
import org.apache.directory.api.ldap.codec.decorators.BindRequestDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.exception.ResponseCarryingMessageException;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.util.Strings;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoSession;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * A global Ldap Decoder test
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class LdapDecoderTest extends AbstractCodecServiceTest
{
    /** The ASN 1 decoder instance */
    private static Asn1Decoder asn1Decoder;


    @BeforeClass
    public static void init()
    {
        asn1Decoder = new Asn1Decoder();
    }


    /**
     * Decode an incoming buffer into LDAP messages. The result can be 0, 1 or many
     * LDAP messages, which will be stored into the array the caller has created.
     * 
     * @param buffer The incoming byte buffer
     * @param messageContainer The LdapMessageContainer which will be used to store the
     * message being decoded. If the message is not fully decoded, the ucrrent state
     * is stored into this container
     * @param decodedMessages The list of decoded messages
     * @throws Exception If the decoding failed
     */
    private void decode( ByteBuffer buffer, LdapMessageContainer<MessageDecorator<? extends Message>> messageContainer,
        List<Message> decodedMessages ) throws DecoderException
    {
        buffer.mark();

        while ( buffer.hasRemaining() )
        {
            try
            {
                asn1Decoder.decode( buffer, messageContainer );

                if ( messageContainer.getState() == TLVStateEnum.PDU_DECODED )
                {
                    Message message = messageContainer.getMessage();

                    decodedMessages.add( message );

                    messageContainer.clean();
                }
            }
            catch ( DecoderException de )
            {
                buffer.clear();
                messageContainer.clean();

                if ( de instanceof ResponseCarryingException )
                {
                    // Transform the DecoderException message to a MessageException
                    ResponseCarryingMessageException rcme = new ResponseCarryingMessageException( de.getMessage() );
                    rcme.setResponse( ( ( ResponseCarryingException ) de ).getResponse() );

                    throw rcme;
                }
                else
                {
                    // TODO : This is certainly not the way we should handle such an exception !
                    throw new ResponseCarryingException( de.getMessage() );
                }
            }
        }
    }


    /**
     * Test the decoding of a full PDU
     */
    @Test
    public void testDecodeFull()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();
        LdapMessageContainer<MessageDecorator<? extends Message>> container =
            new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        ByteBuffer stream = ByteBuffer.allocate( 0x35 );
        stream.put( new byte[]
            {
                0x30, 0x33, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x60,
                0x2E, // CHOICE { ..., bindRequest BindRequest, ...
                      // BindRequest ::= APPLICATION[0] SEQUENCE {
                0x02,
                0x01,
                0x03, // version INTEGER (1..127),
                0x04,
                0x1F, // name LDAPDN,
                'u',
                'i',
                'd',
                '=',
                'a',
                'k',
                'a',
                'r',
                'a',
                's',
                'u',
                'l',
                'u',
                ',',
                'd',
                'c',
                '=',
                'e',
                'x',
                'a',
                'm',
                'p',
                'l',
                'e',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                ( byte ) 0x80,
                0x08, // authentication
                      // AuthenticationChoice
                      // AuthenticationChoice ::= CHOICE { simple [0] OCTET STRING,
                      // ...
                'p',
                'a',
                's',
                's',
                'w',
                'o',
                'r',
                'd'
        } );

        stream.flip();

        // Decode a BindRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        assertEquals( TLVStateEnum.PDU_DECODED, container.getState() );

        // Check the decoded PDU
        BindRequest bindRequest = ( BindRequest ) container.getMessage();

        assertEquals( 1, bindRequest.getMessageId() );
        assertTrue( bindRequest.isVersion3() );
        assertEquals( "uid=akarasulu,dc=example,dc=com", bindRequest.getName().toString() );
        assertTrue( bindRequest.isSimple() );
        assertEquals( "password", Strings.utf8ToString( bindRequest.getCredentials() ) );
    }


    /**
     * Test the decoding of two messages in a PDU
     */
    @Test
    public void testDecode2Messages() throws Exception
    {
        LdapMessageContainer<MessageDecorator<? extends Message>> container =
            new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        IoSession dummySession = new DummySession();
        dummySession.setAttribute( LdapDecoder.MESSAGE_CONTAINER_ATTR, container );

        ByteBuffer stream = ByteBuffer.allocate( 0x6A );
        stream.put( new byte[]
            {
                0x30, 0x33, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x60,
                0x2E, // CHOICE { ..., bindRequest BindRequest, ...
                      // BindRequest ::= APPLICATION[0] SEQUENCE {
                0x02,
                0x01,
                0x03, // version INTEGER (1..127),
                0x04,
                0x1F, // name LDAPDN,
                'u',
                'i',
                'd',
                '=',
                'a',
                'k',
                'a',
                'r',
                'a',
                's',
                'u',
                'l',
                'u',
                ',',
                'd',
                'c',
                '=',
                'e',
                'x',
                'a',
                'm',
                'p',
                'l',
                'e',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                ( byte ) 0x80,
                0x08, // authentication
                      // AuthenticationChoice
                      // AuthenticationChoice ::= CHOICE { simple [0] OCTET STRING,
                      // ...
                'p',
                'a',
                's',
                's',
                'w',
                'o',
                'r',
                'd',
                0x30,
                0x33, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x02, // messageID MessageID
                0x60,
                0x2E, // CHOICE { ..., bindRequest BindRequest, ...
                      // BindRequest ::= APPLICATION[0] SEQUENCE {
                0x02,
                0x01,
                0x03, // version INTEGER (1..127),
                0x04,
                0x1F, // name LDAPDN,
                'u',
                'i',
                'd',
                '=',
                'a',
                'k',
                'a',
                'r',
                'a',
                's',
                'u',
                'l',
                'u',
                ',',
                'd',
                'c',
                '=',
                'e',
                'x',
                'a',
                'm',
                'p',
                'l',
                'e',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                ( byte ) 0x80,
                0x08, // authentication
                      // AuthenticationChoice
                      // AuthenticationChoice ::= CHOICE { simple [0] OCTET STRING,
                      // ...
                'p',
                'a',
                's',
                's',
                'w',
                'o',
                'r',
                'd'
        } );

        stream.flip();

        List<Message> result = new ArrayList<Message>();

        // Decode a BindRequest PDU
        try
        {
            decode( stream, container, result );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded PDU
        BindRequest bindRequest = ( BindRequest ) ( result.get( 0 ) );

        assertEquals( 1, bindRequest.getMessageId() );
        assertTrue( bindRequest.isVersion3() );
        assertEquals( "uid=akarasulu,dc=example,dc=com", bindRequest.getName().toString() );
        assertTrue( bindRequest.isSimple() );
        assertEquals( "password", Strings.utf8ToString( bindRequest.getCredentials() ) );

        // The second message
        bindRequest = ( BindRequest ) ( result.get( 1 ) );

        assertEquals( 2, bindRequest.getMessageId() );
        assertTrue( bindRequest.isVersion3() );
        assertEquals( "uid=akarasulu,dc=example,dc=com", bindRequest.getName().toString() );
        assertTrue( bindRequest.isSimple() );
        assertEquals( "password", Strings.utf8ToString( bindRequest.getCredentials() ) );
    }


    /**
     * Test the decoding of a partial PDU
     */
    @Test
    public void testDecodePartial()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 16 );
        stream.put( new byte[]
            { 0x30, 0x33, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x60,
                0x2E, // CHOICE { ..., bindRequest BindRequest, ...
                // BindRequest ::= APPLICATION[0] SEQUENCE {
                0x02,
                0x01,
                0x03, // version INTEGER (1..127),
                0x04,
                0x1F, // name LDAPDN,
                'u',
                'i',
                'd',
                '=' } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<BindRequestDecorator> container = new LdapMessageContainer<BindRequestDecorator>( codec );

        // Decode a BindRequest PDU
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        assertEquals( TLVStateEnum.VALUE_STATE_PENDING, container.getState() );

        // Check the decoded PDU
        BindRequest bindRequest = container.getMessage();

        assertEquals( 1, bindRequest.getMessageId() );
        assertTrue( bindRequest.isVersion3() );
        assertEquals( null, bindRequest.getName() );
        assertTrue( bindRequest.isSimple() );
    }


    /**
     * Test the decoding of a splitted PDU
     */
    @Test
    public void testDecodeSplittedPDU()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 16 );
        stream.put( new byte[]
            { 0x30, 0x33, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x60,
                0x2E, // CHOICE { ..., bindRequest BindRequest, ...
                // BindRequest ::= APPLICATION[0] SEQUENCE {
                0x02,
                0x01,
                0x03, // version INTEGER (1..127),
                0x04,
                0x1F, // name LDAPDN,
                'u',
                'i',
                'd',
                '=' } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<BindRequestDecorator> container = new LdapMessageContainer<BindRequestDecorator>( codec );

        // Decode a BindRequest PDU first block of data
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        assertEquals( TLVStateEnum.VALUE_STATE_PENDING, container.getState() );

        // Second block of data
        stream = ByteBuffer.allocate( 37 );
        stream.put( new byte[]
            { 'a', 'k', 'a', 'r', 'a', 's', 'u', 'l', 'u', ',', 'd', 'c', '=', 'e', 'x', 'a', 'm', 'p', 'l', 'e', ',',
                'd', 'c', '=', 'c', 'o', 'm', ( byte ) 0x80, 0x08, // authentication
                // AuthenticationChoice
                // AuthenticationChoice ::= CHOICE { simple [0] OCTET STRING,
                // ...
                'p',
                'a',
                's',
                's',
                'w',
                'o',
                'r',
                'd' } );

        stream.flip();

        // Decode a BindRequest PDU second block of data
        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        assertEquals( container.getState(), TLVStateEnum.PDU_DECODED );

        // Check the decoded PDU
        BindRequest bindRequest = container.getMessage();

        assertEquals( 1, bindRequest.getMessageId() );
        assertTrue( bindRequest.isVersion3() );
        assertEquals( "uid=akarasulu,dc=example,dc=com", bindRequest.getName().toString() );
        assertTrue( bindRequest.isSimple() );
        assertEquals( "password", Strings.utf8ToString( bindRequest.getCredentials() ) );
    }


    /**
     * Test the decoding of a PDU with a bad Length. The first TLV has a length
     * of 0x32 when the PDU is 0x33 bytes long.
     */
    @Test
    public void testDecodeBadLengthTooSmall()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x35 );
        stream.put( new byte[]
            {
                // Length should be 0x33...
                0x30,
                0x32, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x60,
                0x2E, // CHOICE { ..., bindRequest BindRequest, ...
                // BindRequest ::= APPLICATION[0] SEQUENCE {
                0x02,
                0x01,
                0x03, // version INTEGER (1..127),
                0x04,
                0x1F, // name LDAPDN,
                'u',
                'i',
                'd',
                '=',
                'a',
                'k',
                'a',
                'r',
                'a',
                's',
                'u',
                'l',
                'u',
                ',',
                'd',
                'c',
                '=',
                'e',
                'x',
                'a',
                'm',
                'p',
                'l',
                'e',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                ( byte ) 0x80,
                0x08, // authentication
                // AuthenticationChoice
                // AuthenticationChoice ::= CHOICE { simple [0] OCTET STRING,
                // ...
                'p',
                'a',
                's',
                's',
                'w',
                'o',
                'r',
                'd' } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<MessageDecorator<? extends Message>> ldapMessageContainer =
            new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a BindRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertEquals(
                "ERR_01003_VALUE_LENGTH_ABOVE_EXPECTED_LENGTH The current Value length 48 is above the expected length 47",
                de.getMessage() );
            return;
        }

        fail( "Should never reach this point.." );
    }


    /**
     * Test the decoding of a PDU with a bad primitive Length. The second TLV
     * has a length of 0x02 when the PDU is 0x01 bytes long.
     */
    @Test
    public void testDecodeBadPrimitiveLengthTooBig()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x35 );
        stream.put( new byte[]
            { 0x30,
                0x33, // LDAPMessage ::=SEQUENCE {
                // Length should be 0x01...
                0x02,
                0x02,
                0x01, // messageID MessageID
                0x60,
                0x2E, // CHOICE { ..., bindRequest BindRequest, ...
                // BindRequest ::= APPLICATION[0] SEQUENCE {
                0x02,
                0x01,
                0x03, // version INTEGER (1..127),
                0x04,
                0x1F, // name LDAPDN,
                'u',
                'i',
                'd',
                '=',
                'a',
                'k',
                'a',
                'r',
                'a',
                's',
                'u',
                'l',
                'u',
                ',',
                'd',
                'c',
                '=',
                'e',
                'x',
                'a',
                'm',
                'p',
                'l',
                'e',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                ( byte ) 0x80,
                0x08, // authentication AuthenticationChoice
                // AuthenticationChoice ::= CHOICE { simple [0] OCTET STRING,
                // ...
                'p',
                'a',
                's',
                's',
                'w',
                'o',
                'r' } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer =
            new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a BindRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertEquals( "ERR_01200_BAD_TRANSITION_FROM_STATE Bad transition from state MESSAGE_ID_STATE, tag 0x2E",
                de.getMessage() );
            return;
        }

        fail( "Should never reach this point." );
    }


    /**
     * Test the decoding of a PDU with a bad primitive Length. The second TLV
     * has a length of 0x02 when the PDU is 0x01 bytes long.
     */
    @Test
    public void testDecodeBadTagTransition()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x35 );
        stream.put( new byte[]
            { 0x30,
                0x33, // LDAPMessage ::=SEQUENCE {
                // Length should be 0x01...
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x2D,
                0x2D, // CHOICE { ..., bindRequest BindRequest, ...
                // BindRequest ::= APPLICATION[0] SEQUENCE {
                0x02,
                0x01,
                0x03, // version INTEGER (1..127),
                0x04,
                0x1F, // name LDAPDN,
                'u',
                'i',
                'd',
                '=',
                'a',
                'k',
                'a',
                'r',
                'a',
                's',
                'u',
                'l',
                'u',
                ',',
                'd',
                'c',
                '=',
                'e',
                'x',
                'a',
                'm',
                'p',
                'l',
                'e',
                ',',
                'd',
                'c',
                '=',
                'c',
                'o',
                'm',
                ( byte ) 0x80,
                0x08, // authentication
                // AuthenticationChoice
                // AuthenticationChoice ::= CHOICE { simple [0] OCTET STRING,
                // ...
                'p',
                'a',
                's',
                's',
                'w',
                'o',
                'r',
                'd' } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a BindRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertEquals( "ERR_01200_BAD_TRANSITION_FROM_STATE Bad transition from state MESSAGE_ID_STATE, tag 0x2D",
                de.getMessage() );
            return;
        }

        fail( "Should never reach this point." );
    }


    /**
     * Test the decoding of a split Length.
     * 
     * The length is 3 bytes long, but the PDU has been split
     * just after the first byte
     */
    @Test
    public void testDecodeSplittedLength()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 3 );
        stream.put( new byte[]
            { 0x30, ( byte ) 0x82, 0x01,// LDAPMessage ::=SEQUENCE {
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer<MessageDecorator<? extends Message>>( codec );

        // Decode a BindRequest PDU first block of data
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        assertEquals( TLVStateEnum.LENGTH_STATE_PENDING, ldapMessageContainer.getState() );

        // Second block of data
        stream = ByteBuffer.allocate( 1 );
        stream.put( new byte[]
            { ( byte ) 0x80 // End of the length
        } );

        stream.flip();

        // Decode a BindRequest PDU second block of data
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        assertEquals( TLVStateEnum.TAG_STATE_START, ldapMessageContainer.getState() );

        // Check the decoded length
        assertEquals( 384, ldapMessageContainer.getCurrentTLV().getLength() );
    }
}
