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
package org.apache.directory.api.ldap.codec.decorators;


import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapCodecConstants;
import org.apache.directory.api.ldap.model.message.ModifyDnResponse;


/**
 * A decorator for the ModifyDnResponse message
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModifyDnResponseDecorator extends ResponseDecorator<ModifyDnResponse>
    implements ModifyDnResponse
{
    /** The encoded modifyDnResponse length */
    private int modifyDnResponseLength;


    /**
     * Makes a ModifyDnResponse encodable.
     *
     * @param codec The LDAP service instance
     * @param decoratedMessage the decorated ModifyDnResponse
     */
    public ModifyDnResponseDecorator( LdapApiService codec, ModifyDnResponse decoratedMessage )
    {
        super( codec, decoratedMessage );
    }


    //-------------------------------------------------------------------------
    // The Decorator methods
    //-------------------------------------------------------------------------
    /**
     * Compute the ModifyDNResponse length 
     * <br>
     * ModifyDNResponse : 
     * <pre>
     * 0x6D L1 
     *   | 
     *   +--&gt; LdapResult 
     *   
     * L1 = Length(LdapResult) 
     * Length(ModifyDNResponse) = Length(0x6D) + Length(L1) + L1
     * </pre>
     */
    @Override
    public int computeLength()
    {
        modifyDnResponseLength = ( ( LdapResultDecorator ) getLdapResult() ).computeLength();

        return 1 + TLV.getNbBytes( modifyDnResponseLength ) + modifyDnResponseLength;
    }


    /**
     * Encode the ModifyDnResponse message to a PDU.
     * 
     * @param buffer The buffer where to put the PDU
     */
    @Override
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        try
        {
            // The ModifyResponse Tag
            buffer.put( LdapCodecConstants.MODIFY_DN_RESPONSE_TAG );
            buffer.put( TLV.getBytes( modifyDnResponseLength ) );

            // The LdapResult
            ( ( LdapResultDecorator ) getLdapResult() ).encode( buffer );
        }
        catch ( BufferOverflowException boe )
        {
            throw new EncoderException( I18n.err( I18n.ERR_08212_PDU_BUFFER_TOO_SMALL ), boe );
        }

        return buffer;
    }
}
