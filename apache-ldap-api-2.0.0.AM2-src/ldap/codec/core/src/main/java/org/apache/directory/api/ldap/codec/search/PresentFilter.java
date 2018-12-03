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

import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapCodecConstants;
import org.apache.directory.api.util.Strings;


/**
 * Object to store the filter. A filter is seen as a tree with a root.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PresentFilter extends Filter
{
    /** The attribute description. */
    private String attributeDescription;

    /** Temporary storage for attribute description bytes */
    private byte[] attributeDescriptionBytes;


    /**
     * The constructor.
     * 
     * @param tlvId The TLV identifier
     */
    public PresentFilter( int tlvId )
    {
        super( tlvId );
    }


    /**
     * The constructor.
     */
    public PresentFilter()
    {
        super();
    }


    /**
     * Get the attribute
     * 
     * @return Returns the attributeDescription.
     */
    public String getAttributeDescription()
    {
        return attributeDescription;
    }


    /**
     * Set the attributeDescription
     * 
     * @param attributeDescription The attributeDescription to set.
     */
    public void setAttributeDescription( String attributeDescription )
    {
        this.attributeDescription = attributeDescription;
    }


    /**
     * Compute the PresentFilter length 
     * <br>
     * PresentFilter :
     * <pre> 
     * 0x87 L1 present
     * 
     * Length(PresentFilter) = Length(0x87) + Length(super.computeLength()) +
     *      super.computeLength()
     * </pre>
     * 
     * @return The encoded length
     */
    @Override
    public int computeLength()
    {
        attributeDescriptionBytes = Strings.getBytesUtf8( attributeDescription );
        return 1 + TLV.getNbBytes( attributeDescriptionBytes.length ) + attributeDescriptionBytes.length;
    }


    /**
     * Encode the PresentFilter message to a PDU. PresentFilter : 0x87 LL
     * attributeDescription
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
            // The PresentFilter Tag
            buffer.put( ( byte ) LdapCodecConstants.PRESENT_FILTER_TAG );
            buffer.put( TLV.getBytes( attributeDescriptionBytes.length ) );
            buffer.put( attributeDescriptionBytes );
        }
        catch ( BufferOverflowException boe )
        {
            throw new EncoderException( I18n.err( I18n.ERR_08212_PDU_BUFFER_TOO_SMALL ), boe );
        }

        return buffer;
    }


    /**
     * Return a string compliant with RFC 2254 representing a Present filter
     * 
     * @return The Present filter string
     */
    @Override
    public String toString()
    {

        StringBuilder sb = new StringBuilder();

        sb.append( attributeDescription ).append( "=*" );

        return sb.toString();
    }
}
