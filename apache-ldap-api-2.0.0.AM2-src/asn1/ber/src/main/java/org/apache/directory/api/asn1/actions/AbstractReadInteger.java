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
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoder;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to read an integer value
 *
 * @param <E> The container type
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractReadInteger<E extends Asn1Container> extends GrammarAction<E>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( AbstractReadInteger.class );

    /** the acceptable minimum value for the expected value to be parsed */
    private int minValue = 0;

    /** the acceptable maximum value for the expected value to be parsed */
    private int maxValue = Integer.MAX_VALUE;


    /**
     * Instantiates a new AbstractReadInteger action.
     * 
     * @param name the action's name
     */
    public AbstractReadInteger( String name )
    {
        super( name );
    }


    /**
     *
     * Creates a new instance of AbstractReadInteger.
     *
     * @param name the action's name
     * @param minValue the acceptable minimum value for the expected value to be read
     * @param maxValue the acceptable maximum value for the value to be read
     */
    public AbstractReadInteger( String name, int minValue, int maxValue )
    {
        super( name );

        this.minValue = minValue;
        this.maxValue = maxValue;
    }


    /**
     *
     * set the integer value to the appropriate field of ASN.1 object present in the container
     *
     * @param value the integer value
     * @param container the ASN.1 object's container
     */
    protected abstract void setIntegerValue( int value, E container );


    /**
     * {@inheritDoc}
     */
    @Override
    public final void action( E container ) throws DecoderException
    {
        TLV tlv = container.getCurrentTLV();

        // The Length should not be null
        if ( tlv.getLength() == 0 )
        {
            String msg = I18n.err( I18n.ERR_01101_NULL_LENGTH );
            
            LOG.error( msg );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( msg );
        }

        BerValue value = tlv.getValue();

        try
        {
            int number = IntegerDecoder.parse( value, minValue, maxValue );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_01100_INTEGER_VALUE, number ) );
            }

            setIntegerValue( number, container );
        }
        catch ( IntegerDecoderException ide )
        {
            LOG.error( I18n.err( I18n.ERR_01102_INVALID_INTEGER, Strings.dumpBytes( value.getData() ), ide
                .getLocalizedMessage() ) );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( ide.getMessage(), ide );
        }
    }
}
