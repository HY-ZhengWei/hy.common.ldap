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
package org.apache.directory.api.ldap.codec.controls.search.pagedSearch;


import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.directory.api.asn1.Asn1Object;
import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.asn1.ber.tlv.UniversalTag;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.ControlDecorator;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.api.util.Strings;


/**
 * A codec decorator for the {@link PagedResultsImpl}.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PagedResultsDecorator extends ControlDecorator<PagedResults> implements PagedResults
{
    /** The entry change global length */
    private int pscSeqLength;

    /** An instance of this decoder */
    private static final Asn1Decoder DECODER = new Asn1Decoder();


    /**
     * Creates a new instance of PagedResultsDecorator with a newly created decorated
     * PagedResults Control.
     * 
     * @param codec The LDAP service instance
     */
    public PagedResultsDecorator( LdapApiService codec )
    {
        this( codec, new PagedResultsImpl() );
    }


    /**
     * Creates a new instance of PagedResultsDecorator using the supplied PagedResults
     * Control to be decorated.
     *
     * @param codec The LDAP service instance
     * @param  pagedResults The PagedResults Control to be decorated.
     */
    public PagedResultsDecorator( LdapApiService codec, PagedResults pagedResults )
    {
        super( codec, pagedResults );
    }


    /**
     * Compute the PagedSearchControl length, which is the sum
     * of the control length and the value length.
     * 
     * <pre>
     * PagedSearchControl value length :
     * 
     * 0x30 L1 
     *   | 
     *   +--&gt; 0x02 0x0(1-4) [0..2^63-1] (size) 
     *   +--&gt; 0x04 L2 (cookie)
     * </pre>
     *  
     * @return the control length.
     */
    @Override
    public int computeLength()
    {
        int sizeLength = 1 + 1 + BerValue.getNbBytes( getSize() );

        int cookieLength;

        if ( getCookie() != null )
        {
            cookieLength = 1 + TLV.getNbBytes( getCookie().length ) + getCookie().length;
        }
        else
        {
            cookieLength = 1 + 1;
        }

        pscSeqLength = sizeLength + cookieLength;
        valueLength = 1 + TLV.getNbBytes( pscSeqLength ) + pscSeqLength;

        return valueLength;
    }


    /**
     * Encodes the paged search control.
     * 
     * @param buffer The encoded sink
     * @return A ByteBuffer that contains the encoded PDU
     * @throws EncoderException If anything goes wrong.
     */
    @Override
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        if ( buffer == null )
        {
            throw new EncoderException( I18n.err( I18n.ERR_08000_CANNOT_PUT_A_PDU_IN_NULL_BUFFER ) );
        }

        // Now encode the PagedSearch specific part
        buffer.put( UniversalTag.SEQUENCE.getValue() );
        buffer.put( TLV.getBytes( pscSeqLength ) );

        BerValue.encode( buffer, getSize() );
        BerValue.encode( buffer, getCookie() );

        return buffer;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getValue()
    {
        if ( value == null )
        {
            try
            {
                computeLength();
                ByteBuffer buffer = ByteBuffer.allocate( valueLength );

                // Now encode the PagedSearch specific part
                buffer.put( UniversalTag.SEQUENCE.getValue() );
                buffer.put( TLV.getBytes( pscSeqLength ) );

                BerValue.encode( buffer, getSize() );
                BerValue.encode( buffer, getCookie() );

                value = buffer.array();
            }
            catch ( Exception e )
            {
                return null;
            }
        }

        return value;
    }


    /**
     * @return The requested or returned number of entries
     */
    @Override
    public int getSize()
    {
        return getDecorated().getSize();
    }


    /**
     * Set the number of entry requested or returned
     *
     * @param size The number of entries 
     */
    @Override
    public void setSize( int size )
    {
        getDecorated().setSize( size );
    }


    /**
     * @return The stored cookie
     */
    @Override
    public byte[] getCookie()
    {
        return getDecorated().getCookie();
    }


    /**
     * Set the cookie
     *
     * @param cookie The cookie to store in this control
     */
    @Override
    public void setCookie( byte[] cookie )
    {
        // Copy the bytes
        if ( !Strings.isEmpty( cookie ) )
        {
            byte[] copy = new byte[cookie.length];
            System.arraycopy( cookie, 0, copy, 0, cookie.length );
            getDecorated().setCookie( copy );
        }
        else
        {
            getDecorated().setCookie( null );
        }
    }


    /**
     * @return The integer value for the current cookie
     */
    @Override
    public int getCookieValue()
    {
        int value = 0;

        switch ( getCookie().length )
        {
            case 1:
                value = getCookie()[0] & 0x00FF;
                break;

            case 2:
                value = ( ( getCookie()[0] & 0x00FF ) << 8 ) + ( getCookie()[1] & 0x00FF );
                break;

            case 3:
                value = ( ( getCookie()[0] & 0x00FF ) << 16 ) + ( ( getCookie()[1] & 0x00FF ) << 8 )
                    + ( getCookie()[2] & 0x00FF );
                break;

            case 4:
                value = ( ( getCookie()[0] & 0x00FF ) << 24 ) + ( ( getCookie()[1] & 0x00FF ) << 16 )
                    + ( ( getCookie()[2] & 0x00FF ) << 8 ) + ( getCookie()[3] & 0x00FF );
                break;

            default:
                break;

        }

        return value;
    }


    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        int hash = super.hashCode();

        hash = hash * 17 + pscSeqLength;

        return hash;
    }


    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !super.equals( o ) )
        {
            return false;
        }

        PagedResults otherControl = ( PagedResults ) o;

        return ( getSize() == otherControl.getSize() ) && Arrays.equals( getCookie(), otherControl.getCookie() );
    }


    /**
     * Return a String representing this PagedSearchControl.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "    Paged Search Control\n" );
        sb.append( "        oid : " ).append( getOid() ).append( '\n' );
        sb.append( "        critical : " ).append( isCritical() ).append( '\n' );
        sb.append( "        size   : '" ).append( getSize() ).append( "'\n" );
        sb.append( "        cookie   : '" ).append( Strings.dumpBytes( getCookie() ) ).append( "'\n" );

        return sb.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Asn1Object decode( byte[] controlBytes ) throws DecoderException
    {
        ByteBuffer bb = ByteBuffer.wrap( controlBytes );
        PagedResultsContainer container = new PagedResultsContainer( getCodecService(), this );
        DECODER.decode( bb, container );
        return this;
    }
}
