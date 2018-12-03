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
package org.apache.directory.api.ldap.codec.api;


import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.asn1.ber.tlv.UniversalTag;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.util.Strings;


/**
 * LDAP BER encoder.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapEncoder
{
    /** The LdapCodecService */
    private LdapApiService codec;


    /**
     * Creates an instance of Ldap message encoder
     * 
     * @param codec The Codec service to use to handle Controls and extended operations,
     * plus to get access to the underlying services.
     */
    public LdapEncoder( LdapApiService codec )
    {
        if ( codec == null )
        {
            throw new NullPointerException( I18n.err( I18n.ERR_05203_CODEC_ARGS_CANNOT_BE_NULL ) );
        }

        this.codec = codec;
    }


    /**
     * Compute the control's encoded length
     * 
     * @param control The control to compute
     * @return the encoded control length
     */
    public static int computeControlLength( Control control )
    {
        // First, compute the control's value length
        int controlValueLength = ( ( CodecControl<?> ) control ).computeLength();

        // Now, compute the envelop length
        // The OID
        int oidLengh = Strings.getBytesUtf8( control.getOid() ).length;
        int controlLength = 1 + TLV.getNbBytes( oidLengh ) + oidLengh;

        // The criticality, only if true
        if ( control.isCritical() )
        {
            // Always 3 for a boolean
            controlLength += 1 + 1 + 1;
        }

        if ( controlValueLength != 0 )
        {
            controlLength += 1 + TLV.getNbBytes( controlValueLength ) + controlValueLength;
        }

        return controlLength;
    }


    /**
     * Encode a control to a byte[]
     * 
     * @param buffer The buffer that will contain the encoded control
     * @param control The control to encode
     * @return The control encoded in a byte[]
     * @throws EncoderException If the encoding failed
     */
    public static ByteBuffer encodeControl( ByteBuffer buffer, Control control ) throws EncoderException
    {
        if ( buffer == null )
        {
            throw new EncoderException( I18n.err( I18n.ERR_08000_CANNOT_PUT_A_PDU_IN_NULL_BUFFER ) );
        }

        try
        {
            // The LdapMessage Sequence
            buffer.put( UniversalTag.SEQUENCE.getValue() );

            // The length has been calculated by the computeLength method
            int controlLength = computeControlLength( control );
            buffer.put( TLV.getBytes( controlLength ) );
        }
        catch ( BufferOverflowException boe )
        {
            throw new EncoderException( I18n.err( I18n.ERR_08212_PDU_BUFFER_TOO_SMALL ), boe );
        }

        // The control type
        BerValue.encode( buffer, Strings.getBytesUtf8( control.getOid() ) );

        // The control criticality, if true
        if ( control.isCritical() )
        {
            BerValue.encode( buffer, control.isCritical() );
        }

        return buffer;
    }


    /**
     * Generate the PDU which contains the encoded object. 
     * 
     * The generation is done in two phases : 
     * - first, we compute the length of each part and the
     * global PDU length 
     * - second, we produce the PDU. 
     * 
     * <pre>
     * 0x30 L1 
     *   | 
     *   +--&gt; 0x02 L2 MessageId  
     *   +--&gt; ProtocolOp 
     *   +--&gt; Controls 
     *   
     * L2 = Length(MessageId)
     * L1 = Length(0x02) + Length(L2) + L2 + Length(ProtocolOp) + Length(Controls)
     * LdapMessageLength = Length(0x30) + Length(L1) + L1
     * </pre>
     * 
     * @param message The message to encode
     * @return A ByteBuffer that contains the PDU
     * @throws EncoderException If anything goes wrong.
     */
    public ByteBuffer encodeMessage( Message message ) throws EncoderException
    {
        MessageDecorator<? extends Message> decorator = MessageDecorator.getDecorator( codec, message );
        int length = computeMessageLength( decorator );
        ByteBuffer buffer = ByteBuffer.allocate( length );

        try
        {
            try
            {
                // The LdapMessage Sequence
                buffer.put( UniversalTag.SEQUENCE.getValue() );

                // The length has been calculated by the computeLength method
                buffer.put( TLV.getBytes( decorator.getMessageLength() ) );
            }
            catch ( BufferOverflowException boe )
            {
                throw new EncoderException( I18n.err( I18n.ERR_08212_PDU_BUFFER_TOO_SMALL ), boe );
            }

            // The message Id
            BerValue.encode( buffer, message.getMessageId() );

            // Add the protocolOp part
            decorator.encode( buffer );

            // Do the same thing for Controls, if any.
            Map<String, Control> controls = decorator.getControls();

            if ( ( controls != null ) && ( controls.size() > 0 ) )
            {
                // Encode the controls
                buffer.put( ( byte ) LdapCodecConstants.CONTROLS_TAG );
                buffer.put( TLV.getBytes( decorator.getControlsLength() ) );

                // Encode each control
                for ( Control control : controls.values() )
                {
                    encodeControl( buffer, control );

                    // The OctetString tag if the value is not null
                    int controlValueLength = ( ( CodecControl<?> ) control ).computeLength();

                    if ( controlValueLength > 0 )
                    {
                        buffer.put( UniversalTag.OCTET_STRING.getValue() );
                        buffer.put( TLV.getBytes( controlValueLength ) );

                        // And now, the value
                        ( ( org.apache.directory.api.ldap.codec.api.CodecControl<?> ) control ).encode( buffer );
                    }
                }
            }
        }
        catch ( EncoderException ee )
        {
            throw new MessageEncoderException( message.getMessageId(), ee.getMessage(), ee );
        }

        buffer.flip();

        return buffer;
    }


    /**
     * Compute the LdapMessage length LdapMessage : 
     * <pre>
     * 0x30 L1 
     *   | 
     *   +--&gt; 0x02 0x0(1-4) [0..2^31-1] (MessageId) 
     *   +--&gt; protocolOp 
     *   [+--&gt; Controls] 
     *   
     * MessageId length = Length(0x02) + length(MessageId) + MessageId.length 
     * L1 = length(ProtocolOp) 
     * LdapMessage length = Length(0x30) + Length(L1) + MessageId length + L1
     * </pre>
     * 
     * @param messageDecorator the decorated Message who's length is to be encoded
     * @return The message length
     */
    private int computeMessageLength( MessageDecorator<? extends Message> messageDecorator )
    {
        // The length of the MessageId. It's the sum of
        // - the tag (0x02), 1 byte
        // - the length of the Id length, 1 byte
        // - the Id length, 1 to 4 bytes
        int ldapMessageLength = 1 + 1 + BerValue.getNbBytes( messageDecorator.getDecorated().getMessageId() );

        // Get the protocolOp length
        ldapMessageLength += messageDecorator.computeLength();

        Map<String, Control> controls = messageDecorator.getControls();

        // Do the same thing for Controls, if any.
        if ( controls.size() > 0 )
        {
            // Controls :
            // 0xA0 L3
            //   |
            //   +--> 0x30 L4
            //   +--> 0x30 L5
            //   +--> ...
            //   +--> 0x30 Li
            //   +--> ...
            //   +--> 0x30 Ln
            //
            // L3 = Length(0x30) + Length(L5) + L5
            // + Length(0x30) + Length(L6) + L6
            // + ...
            // + Length(0x30) + Length(Li) + Li
            // + ...
            // + Length(0x30) + Length(Ln) + Ln
            //
            // LdapMessageLength = LdapMessageLength + Length(0x90)
            // + Length(L3) + L3
            int controlsSequenceLength = 0;

            // We may have more than one control. ControlsLength is L4.
            for ( Control control : controls.values() )
            {
                int controlLength = computeControlLength( control );

                controlsSequenceLength += 1 + TLV.getNbBytes( controlLength ) + controlLength;
            }

            // Computes the controls length
            // 1 + Length.getNbBytes( controlsSequenceLength ) + controlsSequenceLength
            messageDecorator.setControlsLength( controlsSequenceLength );

            // Now, add the tag and the length of the controls length
            ldapMessageLength += 1 + TLV.getNbBytes( controlsSequenceLength ) + controlsSequenceLength;
        }

        // Store the messageLength
        messageDecorator.setMessageLength( ldapMessageLength );

        // finally, calculate the global message size :
        // length(Tag) + Length(length) + length

        return 1 + ldapMessageLength + TLV.getNbBytes( ldapMessageLength );
    }


    /**
     * Encode the Referral message to a PDU.
     * 
     * @param buffer The buffer where to put the PDU
     * @param referral The referral to encode
     * @exception EncoderException If the encoding failed
     */
    public static void encodeReferral( ByteBuffer buffer, Referral referral ) throws EncoderException
    {
        Collection<byte[]> ldapUrlsBytes = referral.getLdapUrlsBytes();

        if ( ( ldapUrlsBytes != null ) && ( !ldapUrlsBytes.isEmpty() ) )
        {
            // Encode the referrals sequence
            // The referrals length MUST have been computed before !
            buffer.put( ( byte ) LdapCodecConstants.LDAP_RESULT_REFERRAL_SEQUENCE_TAG );
            buffer.put( TLV.getBytes( referral.getReferralLength() ) );

            // Each referral
            for ( byte[] ldapUrlBytes : ldapUrlsBytes )
            {
                // Encode the current referral
                BerValue.encode( buffer, ldapUrlBytes );
            }
        }
    }


    /**
     * Compute the referral's encoded length
     * @param referral The referral to encode
     * @return The length of the encoded PDU
     */
    public static int computeReferralLength( Referral referral )
    {
        if ( referral != null )
        {
            Collection<String> ldapUrls = referral.getLdapUrls();

            if ( ( ldapUrls != null ) && ( !ldapUrls.isEmpty() ) )
            {
                int referralLength = 0;

                // Each referral
                for ( String ldapUrl : ldapUrls )
                {
                    byte[] ldapUrlBytes = Strings.getBytesUtf8( ldapUrl );
                    referralLength += 1 + TLV.getNbBytes( ldapUrlBytes.length ) + ldapUrlBytes.length;
                    referral.addLdapUrlBytes( ldapUrlBytes );
                }

                referral.setReferralLength( referralLength );

                return referralLength;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return 0;
        }
    }
}
