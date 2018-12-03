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
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.util.Strings;


/**
 * A decorator for the ExtendedRequest message
 *
 * @param <Q> The extended request to decorate
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtendedRequestDecorator<Q extends ExtendedRequest>
    extends SingleReplyRequestDecorator<Q> implements ExtendedRequest
{
    /** The extended request length */
    private int extendedRequestLength;

    /** The OID bytes */
    private byte[] requestNameBytes;

    /** The ExtendedRequest value */
    protected byte[] requestValue;


    /**
     * Makes a ExtendedRequest a MessageDecorator.
     *
     * @param codec The LDAP service instance
     * @param decoratedMessage the decorated ExtendedRequest
     */
    public ExtendedRequestDecorator( LdapApiService codec, Q decoratedMessage )
    {
        super( codec, decoratedMessage );
    }


    //-------------------------------------------------------------------------
    // The ExtendedRequest methods
    //-------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestName()
    {
        return getDecorated().getRequestName();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedRequest setRequestName( String oid )
    {
        getDecorated().setRequestName( oid );

        return this;
    }


    /**
     * Gets the Extended request payload 
     * 
     * @return The extended payload
     */
    public byte[] getRequestValue()
    {
        return requestValue;
    }


    /**
     * sets the Extended request payload 
     * 
     * @param requestValue The extended payload
     */
    public void setRequestValue( byte[] requestValue )
    {
        this.requestValue = requestValue;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedRequest setMessageId( int messageId )
    {
        super.setMessageId( messageId );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
public ExtendedRequest addControl( Control control )
    {
        return ( ExtendedRequest ) super.addControl( control );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedRequest addAllControls( Control[] controls )
    {
        return ( ExtendedRequest ) super.addAllControls( controls );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedRequest removeControl( Control control )
    {
        return ( ExtendedRequest ) super.removeControl( control );
    }


    //-------------------------------------------------------------------------
    // The Decorator methods
    //-------------------------------------------------------------------------

    /**
     * Compute the ExtendedRequest length
     * <br>
     * ExtendedRequest :
     * <pre>
     * 0x77 L1
     *  |
     *  +--&gt; 0x80 L2 name
     *  [+--&gt; 0x81 L3 value]
     * 
     * L1 = Length(0x80) + Length(L2) + L2
     *      [+ Length(0x81) + Length(L3) + L3]
     * 
     * Length(ExtendedRequest) = Length(0x77) + Length(L1) + L1
     * </pre>
     */
    @Override
    public int computeLength()
    {
        requestNameBytes = Strings.getBytesUtf8( getRequestName() );

        extendedRequestLength = 1 + TLV.getNbBytes( requestNameBytes.length ) + requestNameBytes.length;

        if ( getRequestValue() != null )
        {
            extendedRequestLength += 1 + TLV.getNbBytes( getRequestValue().length )
                + getRequestValue().length;
        }

        return 1 + TLV.getNbBytes( extendedRequestLength ) + extendedRequestLength;
    }


    /**
     * Encode the ExtendedRequest message to a PDU. 
     * 
     * ExtendedRequest :
     * 
     * 0x80 LL resquest name
     * [0x81 LL request value]
     * 
     * @param buffer The buffer where to put the PDU
     * @return The PDU.
     */
    @Override
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        try
        {
            // The BindResponse Tag
            buffer.put( LdapCodecConstants.EXTENDED_REQUEST_TAG );
            buffer.put( TLV.getBytes( extendedRequestLength ) );

            // The requestName, if any
            if ( requestNameBytes == null )
            {
                throw new EncoderException( I18n.err( I18n.ERR_05000_NULL_REQUEST_NAME ) );
            }

            buffer.put( ( byte ) LdapCodecConstants.EXTENDED_REQUEST_NAME_TAG );
            buffer.put( TLV.getBytes( requestNameBytes.length ) );

            if ( requestNameBytes.length != 0 )
            {
                buffer.put( requestNameBytes );
            }

            // The requestValue, if any
            if ( getRequestValue() != null )
            {
                buffer.put( ( byte ) LdapCodecConstants.EXTENDED_REQUEST_VALUE_TAG );

                buffer.put( TLV.getBytes( getRequestValue().length ) );

                if ( getRequestValue().length != 0 )
                {
                    buffer.put( getRequestValue() );
                }
            }
        }
        catch ( BufferOverflowException boe )
        {
            throw new EncoderException( I18n.err( I18n.ERR_08212_PDU_BUFFER_TOO_SMALL ), boe );
        }

        return buffer;
    }
}
