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


import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapCodecConstants;


/**
 * Not Filter Object to store the Not filter.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NotFilter extends ConnectorFilter
{
    /**
     * The constructor.
     * 
     * @param tlvId The TLV identifier
     */
    public NotFilter( int tlvId )
    {
        super( tlvId );
    }


    /**
     * The constructor.
     */
    public NotFilter()
    {
        super();
    }


    /**
     * Subclass the addFilterMethod, as this is specific for a NotFilter (we
     * cannot have more than one elements).
     * 
     * @param filter The Filter to add
     */
    @Override
    public void addFilter( Filter filter ) throws DecoderException
    {
        if ( filterSet != null )
        {
            throw new DecoderException( I18n.err( I18n.ERR_05501_MORE_THAN_ONE_FILTER_FOR_NOT_FILTER ) );
        }

        super.addFilter( filter );
    }


    /**
     * Get the NotFilter
     * 
     * @return Returns the notFilter.
     */
    public Filter getNotFilter()
    {
        return filterSet.get( 0 );
    }


    /**
     * Set the NotFilter
     * 
     * @param notFilter The notFilter to set.
     * @throws DecoderException If the NotFilter is already containing a filter
     */
    public void setNotFilter( Filter notFilter ) throws DecoderException
    {
        if ( filterSet != null )
        {
            throw new DecoderException( I18n.err( I18n.ERR_05501_MORE_THAN_ONE_FILTER_FOR_NOT_FILTER ) );
        }

        super.addFilter( notFilter );
    }


    /**
     * Compute the NotFilter length 
     * <br>
     * NotFilter :
     * <pre> 
     * 0xA2 L1 super.computeLength()
     * 
     * Length(NotFilter) = Length(0xA2) + Length(super.computeLength()) +
     *      super.computeLength()
     * </pre>
     * 
     * @return The encoded length
     */
    @Override
    public int computeLength()
    {
        filtersLength = super.computeLength();

        return 1 + TLV.getNbBytes( filtersLength ) + filtersLength;
    }


    /**
     * Encode the NotFilter message to a PDU. 
     * <br>
     * NotFilter :
     * <pre> 
     * 0xA2 LL filter.encode()
     * </pre>
     * 
     * @param buffer The buffer where to put the PDU
     * @return The PDU.
     */
    @Override
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        if ( buffer == null )
        {
            throw new EncoderException( I18n.err( I18n.ERR_08000_CANNOT_PUT_A_PDU_IN_NULL_BUFFER ) );
        }

        try
        {
            // The NotFilter Tag
            buffer.put( ( byte ) LdapCodecConstants.NOT_FILTER_TAG );
            buffer.put( TLV.getBytes( filtersLength ) );
        }
        catch ( BufferOverflowException boe )
        {
            throw new EncoderException( I18n.err( I18n.ERR_08212_PDU_BUFFER_TOO_SMALL ), boe );
        }

        super.encode( buffer );

        return buffer;
    }


    /**
     * Return a string compliant with RFC 2254 representing a NOT filter
     * 
     * @return The NOT filter string
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( '!' ).append( super.toString() );

        return sb.toString();
    }
}
