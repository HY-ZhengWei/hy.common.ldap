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

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Container;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.codec.decorators.UnbindRequestDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.UnbindRequest;
import org.apache.directory.api.ldap.model.message.UnbindRequestImpl;
import org.apache.directory.api.util.Strings;
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
public class LdapMessageTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of null length messageId
     */
    @Test
    public void testDecodeMessageLengthNull()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x02 );
        stream.put( new byte[]
            { 0x30, 0x00, // LDAPMessage ::=SEQUENCE {
            } );

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
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point !" );
    }


    /**
     * Test the decoding of null length messageId
     */
    @Test
    public void testDecodeMessageIdLengthNull()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x04 );
        stream.put( new byte[]
            { 0x30, 0x02, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x00 // messageID MessageID
        } );

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
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point !" );
    }


    /**
     * Test the decoding of null length messageId
     */
    @Test
    public void testDecodeMessageIdMinusOne()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x05 );
        stream.put( new byte[]
            { 0x30, 0x03, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                ( byte ) 0xff // messageID MessageID = -1
        } );

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
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point !" );
    }


    /**
     * Test the decoding of messageId which value is -1
     */
    @Test
    public void testDecodeMessageIdMaxInt()
    {

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x08 );
        stream.put( new byte[]
            { 0x30, 0x06, // LDAPMessage ::=SEQUENCE {
                // messageID MessageID = -1
                0x02,
                0x04,
                ( byte ) 0x7f,
                ( byte ) 0xff,
                ( byte ) 0xff,
                ( byte ) 0xff } );

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
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point !" );
    }


    /**
     * Test the decoding of a message with a wrong protocol operation
     */
    @Test
    public void testDecodeWrongProtocolOpMaxInt()
    {

        byte[] buffer = new byte[]
            { 0x30, 0x05, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID = 1
                0x42,
                0x00 // ProtocolOp
        };

        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        for ( int i = 0; i < 256; i++ )
        {
            buffer[5] = ( byte ) i;
            stream.put( buffer );
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
                switch ( i )
                {
                    case 0x42:
                    case 0x4A:
                    case 0x50: // AbandonRequest
                    case 0x60:
                    case 0x61:
                    case 0x63:
                    case 0x64:
                    case 0x65:
                    case 0x66:
                    case 0x67:
                    case 0x68:
                    case 0x69:
                    case 0x6B:
                    case 0x6C:
                    case 0x6D:
                    case 0x6E:
                    case 0x6F:
                    case 0x73:
                    case 0x77:
                    case 0x78:
                        assertTrue( true );
                        break;

                    default:
                        String res = de.getMessage();

                        if ( res.startsWith( "ERR_01200_BAD_TRANSITION_FROM_STATE" )
                            || res.startsWith( "Universal tag " )
                            || res.startsWith( "ERR_01005_TRUNCATED_PDU Truncated PDU" ) )
                        {
                            assertTrue( true );
                        }
                        else
                        {
                            fail( "Bad exception : " + res );
                            return;
                        }

                        break;
                }
            }

            stream.clear();
        }

        assertTrue( true );
    }


    /**
     * Test the decoding of a LdapMessage with a large MessageId
     */
    @Test
    public void testDecodeUnBindRequestNoControls()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x08 );
        stream.put( new byte[]
            { 0x30, 0x06, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x02,
                0x01,
                ( byte ) 0xF4, // messageID MessageID (500)
                0x42,
                0x00, // CHOICE { ..., unbindRequest UnbindRequest,...
            // UnbindRequest ::= [APPLICATION 2] NULL
        } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<UnbindRequestDecorator> container =
            new LdapMessageContainer<UnbindRequestDecorator>( codec );

        try
        {
            ldapDecoder.decode( stream, container );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        Message message = container.getMessage();

        assertEquals( 500, message.getMessageId() );

        // Check the length
        UnbindRequest internalUnbindRequest = new UnbindRequestImpl();
        internalUnbindRequest.setMessageId( message.getMessageId() );

        try
        {
            ByteBuffer bb = encoder.encodeMessage( internalUnbindRequest );

            // Check the length
            assertEquals( 0x08, bb.limit() );

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
