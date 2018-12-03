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
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to read an OCTET STRING value
 *
 * @param <C> The container type
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractReadOctetString<C extends Asn1Container> extends GrammarAction<C>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( AbstractReadOctetString.class );

    /** the acceptable maximum value for the expected value to be parsed */
    private boolean canBeNull = Boolean.FALSE;


    /**
     * Instantiates a new AbstractReadInteger action.
     * 
     * @param name the action's name
     */
    public AbstractReadOctetString( String name )
    {
        super( name );
    }


    /**
     * Instantiates a new AbstractReadInteger action.
     *
     * @param name the action's name
     * @param canBeNull Tells if the byte array can be null or not
     */
    public AbstractReadOctetString( String name, boolean canBeNull )
    {
        super( name );

        this.canBeNull = canBeNull;
    }


    /**
     * Sets the OCTET STRING value to the appropriate field of ASN.1 object present in the container
     *
     * @param value the OCTET STRING value
     * @param container the ASN.1 object's container
     */
    protected abstract void setOctetString( byte[] value, C container );


    /**
     * {@inheritDoc}
     */
    @Override
    public final void action( C container ) throws DecoderException
    {
        TLV tlv = container.getCurrentTLV();

        // The Length should not be null
        if ( ( tlv.getLength() == 0 ) && ( !canBeNull ) )
        {
            String msg = I18n.err( I18n.ERR_01101_NULL_LENGTH );
            
            LOG.error( msg );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( msg );
        }

        BerValue value = tlv.getValue();

        // The data should not be null
        if ( ( value.getData() == null ) && ( !canBeNull ) )
        {
            String msg = I18n.err( I18n.ERR_01101_NULL_LENGTH );
            
            LOG.error( msg );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( msg );
        }

        setOctetString( value.getData(), container );
    }
}
