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
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapCodecConstants;
import org.apache.directory.api.ldap.codec.api.LdapEncoder;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.message.SearchResultReference;


/**
 * A decorator for the SearchResultReference message
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultReferenceDecorator extends MessageDecorator<SearchResultReference>
    implements SearchResultReference
{
    /** The search result reference length */
    private int searchResultReferenceLength;


    /**
     * Makes a SearchResultReference encodable.
     *
     * @param codec The LDAP service instance
     * @param decoratedMessage the decorated SearchResultReference
     */
    public SearchResultReferenceDecorator( LdapApiService codec, SearchResultReference decoratedMessage )
    {
        super( codec, decoratedMessage );
    }


    //-------------------------------------------------------------------------
    // The SearchResultReference methods
    //-------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Referral getReferral()
    {
        return getDecorated().getReferral();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setReferral( Referral referral )
    {
        getDecorated().setReferral( referral );
    }


    //-------------------------------------------------------------------------
    // The Decorator methods
    //-------------------------------------------------------------------------

    /**
     * Compute the SearchResultReference length
     * <br>
     * SearchResultReference :
     * <pre>
     * 0x73 L1
     *  |
     *  +--&gt; 0x04 L2 reference
     *  +--&gt; 0x04 L3 reference
     *  +--&gt; ...
     *  +--&gt; 0x04 Li reference
     *  +--&gt; ...
     *  +--&gt; 0x04 Ln reference
     * 
     * L1 = n*Length(0x04) + sum(Length(Li)) + sum(Length(reference[i]))
     * 
     * Length(SearchResultReference) = Length(0x73 + Length(L1) + L1
     * </pre>
     * 
     * @return The encoded length
     */
    @Override
    public int computeLength()
    {
        searchResultReferenceLength = 0;

        // We may have more than one reference.
        Referral referral = getReferral();

        int referralLength = LdapEncoder.computeReferralLength( referral );

        if ( referralLength != 0 )
        {
            setReferral( referral );

            searchResultReferenceLength = referralLength;
        }

        return 1 + TLV.getNbBytes( searchResultReferenceLength ) + searchResultReferenceLength;
    }


    /**
     * Encode the SearchResultReference message to a PDU.
     * <br>
     * SearchResultReference :
     * <pre>
     * 0x73 LL
     *   0x04 LL reference
     *   [0x04 LL reference]*
     * </pre>
     * 
     * @param buffer The buffer where to put the PDU
     * @return The PDU.
     */
    @Override
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        SearchResultReference searchResultReference = getDecorated();
        try
        {
            // The SearchResultReference Tag
            buffer.put( LdapCodecConstants.SEARCH_RESULT_REFERENCE_TAG );
            buffer.put( TLV.getBytes( searchResultReferenceLength ) );

            // The referrals, if any
            Referral referral = searchResultReference.getReferral();

            if ( referral != null )
            {
                // Each referral
                for ( byte[] ldapUrlBytes : referral.getLdapUrlsBytes() )
                {
                    // Encode the current referral
                    BerValue.encode( buffer, ldapUrlBytes );
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
