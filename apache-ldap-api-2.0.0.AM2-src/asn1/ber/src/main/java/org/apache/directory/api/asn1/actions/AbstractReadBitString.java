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
package org.apache.directory.api.asn1.actions;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.Asn1Container;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used read a BITSTRING from a TLV
 * 
 * @param <C> The container type
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractReadBitString<C extends Asn1Container> extends GrammarAction<C>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( AbstractReadBitString.class );


    /**
     * Instantiates a new AbstractReadByteArray action.
     * 
     * @param name the action's name
     */
    public AbstractReadBitString( String name )
    {
        super( name );
    }


    /**
     * Gives a byte array to be set to the appropriate field of the ASN.1 object
     * present in the container
     *
     * @param data the data of the read TLV present in byte array format
     * @param container the container holding the ASN.1 object
     */
    protected abstract void setBitString( byte[] data, C container );


    /**
     * {@inheritDoc}
     */
    @Override
    public final void action( C container ) throws DecoderException
    {
        TLV tlv = container.getCurrentTLV();

        // The Length should not be null, and should be 5
        if ( tlv.getLength() != 5 )
        {
            String msg = I18n.err( I18n.ERR_01100_INCORRECT_LENGTH, 5, tlv.getLength() );
            
            LOG.error( msg );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( msg );
        }

        byte[] data = tlv.getValue().getData();
        setBitString( data, container );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_01101_BITSTRING_VALUE, Strings.dumpBytes( data ) ) );
        }
    }
}
