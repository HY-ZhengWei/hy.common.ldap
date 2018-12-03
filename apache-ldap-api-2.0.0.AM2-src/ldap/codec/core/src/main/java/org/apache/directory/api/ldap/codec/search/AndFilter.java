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
import java.util.List;

import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapCodecConstants;


/**
 * And Filter Object to store the And filter.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AndFilter extends ConnectorFilter
{
    /**
     * The constructor. We wont initialize the ArrayList as they may not be
     * used.
     * 
     * @param tlvId The TLV identifier
     */
    public AndFilter( int tlvId )
    {
        super( tlvId );
    }


    /**
     * The constructor. We wont initialize the ArrayList as they may not be
     * used.
     */
    public AndFilter()
    {
        super();
    }


    /**
     * Get the AndFilter.
     * 
     * @return Returns the andFilter.
     */
    public List<Filter> getAndFilter()
    {
        return filterSet;
    }


    /**
     * Compute the AndFilter length 
     * <br>
     * AndFilter :
     * <pre> 
     * 0xA0 L1 super.computeLength()
     * 
     * Length(AndFilter) = Length(0xA0) + Length(super.computeLength()) +
     *          super.computeLength()
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
     * Encode the AndFilter message to a PDU. 
     * <br>
     * AndFilter :
     * <pre> 
     * 0xA0 LL
     *  filter.encode() ... filter.encode()
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
            // The AndFilter Tag
            buffer.put( ( byte ) LdapCodecConstants.AND_FILTER_TAG );
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
     * Return a string compliant with RFC 2254 representing an AND filter
     * 
     * @return The AND filter string
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( '&' ).append( super.toString() );

        return sb.toString();
    }
}
