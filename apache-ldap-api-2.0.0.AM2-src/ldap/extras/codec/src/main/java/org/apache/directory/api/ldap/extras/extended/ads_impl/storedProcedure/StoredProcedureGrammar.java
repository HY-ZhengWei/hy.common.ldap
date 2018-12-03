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

package org.apache.directory.api.ldap.extras.extended.ads_impl.storedProcedure;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.AbstractGrammar;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.grammar.GrammarTransition;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.asn1.ber.tlv.UniversalTag;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapApiServiceFactory;
import org.apache.directory.api.ldap.extras.extended.storedProcedure.StoredProcedureParameter;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ASN.1 BER Grammar for Stored Procedure Extended Operation
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class StoredProcedureGrammar extends AbstractGrammar<StoredProcedureContainer>
{
    /** The logger */
    static final Logger LOG = LoggerFactory.getLogger( StoredProcedureGrammar.class );

    /** The instance of grammar. StoredProcedureGrammar is a singleton. */
    private static StoredProcedureGrammar instance = new StoredProcedureGrammar();


    //~ Constructors -------------------------------------------------------------------------------

    /**
     * Creates a new StoredProcedureGrammar object.
     */
    @SuppressWarnings("unchecked")
    private StoredProcedureGrammar()
    {
        setName( StoredProcedureGrammar.class.getName() );

        // Create the transitions table
        super.transitions = new GrammarTransition[StoredProcedureStatesEnum.LAST_STORED_PROCEDURE_STATE.ordinal()][256];

        //============================================================================================
        // StoredProcedure Message
        //============================================================================================
        // StoredProcedure ::= SEQUENCE {
        //   ...
        // Nothing to do.
        super.transitions[StoredProcedureStatesEnum.START_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<StoredProcedureContainer>( StoredProcedureStatesEnum.START_STATE,
                StoredProcedureStatesEnum.STORED_PROCEDURE_STATE,
                UniversalTag.SEQUENCE.getValue(),
                null );

        //    language OCTETSTRING, (Tag)
        //    ...
        //
        // Creates the storeProcedure and stores the language
        super.transitions[StoredProcedureStatesEnum.STORED_PROCEDURE_STATE.ordinal()][UniversalTag.OCTET_STRING
            .getValue()] =
            new GrammarTransition<StoredProcedureContainer>( StoredProcedureStatesEnum.STORED_PROCEDURE_STATE,
                StoredProcedureStatesEnum.LANGUAGE_STATE,
                UniversalTag.OCTET_STRING.getValue(),
                new GrammarAction<StoredProcedureContainer>( "Stores the language" )
                {
                    public void action( StoredProcedureContainer container ) throws DecoderException
                    {
                        TLV tlv = container.getCurrentTLV();

                        StoredProcedureRequestDecorator storedProcedure = container.getStoredProcedure();
                        if ( storedProcedure == null )
                        {
                            storedProcedure = new StoredProcedureRequestDecorator( LdapApiServiceFactory.getSingleton() );
                            container.setStoredProcedure( storedProcedure );
                        }

                        // Store the value.
                        if ( tlv.getLength() == 0 )
                        {
                            // We can't have a void language !
                            String msg = I18n.err( I18n.ERR_08207_SP_LANGUAGE_NULL );
                            LOG.error( msg );
                            throw new DecoderException( msg );
                        }
                        else
                        {
                            // Only this field's type is String by default
                            String language = Strings.utf8ToString( tlv.getValue().getData() );

                            if ( LOG.isDebugEnabled() )
                            {
                                LOG.debug( I18n.msg( I18n.MSG_08213_SP_LANGUAGE_FOUND, language ) );
                            }

                            storedProcedure.setLanguage( language );
                        }
                    }
                } );

        //    procedure OCTETSTRING, (Value)
        //    ...
        // Stores the procedure.
        super.transitions[StoredProcedureStatesEnum.LANGUAGE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] =
            new GrammarTransition<StoredProcedureContainer>( StoredProcedureStatesEnum.LANGUAGE_STATE,
                StoredProcedureStatesEnum.PROCEDURE_STATE,
                UniversalTag.OCTET_STRING.getValue(),
                new GrammarAction<StoredProcedureContainer>( "Stores the procedure" )
                {
                    public void action( StoredProcedureContainer container ) throws DecoderException
                    {
                        TLV tlv = container.getCurrentTLV();

                        StoredProcedureRequestDecorator storedProcedure = container.getStoredProcedure();

                        // Store the value.
                        if ( tlv.getLength() == 0 )
                        {
                            // We can't have a void procedure !
                            String msg = I18n.err( I18n.ERR_08208_NULL_PROCEDURE );
                            LOG.error( msg );
                            throw new DecoderException( msg );
                        }
                        else
                        {
                            byte[] procedure = tlv.getValue().getData();

                            storedProcedure.setProcedure( procedure );
                        }

                        if ( LOG.isDebugEnabled() )
                        {
                            LOG.debug( I18n.msg( I18n.MSG_08212_PROCEDURE_FOUND, storedProcedure.getProcedureSpecification() ) );
                        }
                    }
                } );

        // parameters SEQUENCE OF Parameter { (Value)
        //    ...
        // The list of parameters will be created with the first parameter.
        // We can have an empty list of parameters, so the PDU can be empty
        super.transitions[StoredProcedureStatesEnum.PROCEDURE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<StoredProcedureContainer>( StoredProcedureStatesEnum.PROCEDURE_STATE,
                StoredProcedureStatesEnum.PARAMETERS_STATE,
                UniversalTag.SEQUENCE.getValue(),
                new GrammarAction<StoredProcedureContainer>( "Stores the parameters" )
                {
                    public void action( StoredProcedureContainer container )
                    {
                        container.setGrammarEndAllowed( true );
                    }
                } );

        // parameter SEQUENCE OF { (Value)
        //    ...
        // Nothing to do. 
        super.transitions[StoredProcedureStatesEnum.PARAMETERS_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<StoredProcedureContainer>( StoredProcedureStatesEnum.PARAMETERS_STATE,
                StoredProcedureStatesEnum.PARAMETER_STATE,
                UniversalTag.SEQUENCE.getValue(),
                null );

        // Parameter ::= {
        //    type OCTETSTRING, (Value)
        //    ...
        //
        // We can create a parameter, and store its type
        super.transitions[StoredProcedureStatesEnum.PARAMETER_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] =
            new GrammarTransition<StoredProcedureContainer>( StoredProcedureStatesEnum.PARAMETER_STATE,
                StoredProcedureStatesEnum.PARAMETER_TYPE_STATE,
                UniversalTag.OCTET_STRING.getValue(),
                new GrammarAction<StoredProcedureContainer>( "Store parameter type" )
                {
                    public void action( StoredProcedureContainer container ) throws DecoderException
                    {
                        TLV tlv = container.getCurrentTLV();
                        StoredProcedureRequestDecorator storedProcedure = container.getStoredProcedure();

                        // Store the value.
                        if ( tlv.getLength() == 0 )
                        {
                            // We can't have a void parameter type !
                            String msg = I18n.err( I18n.ERR_08209_NULL_PARAMETER_TYPE );
                            LOG.error( msg );
                            throw new DecoderException( msg );
                        }
                        else
                        {
                            StoredProcedureParameter parameter = new StoredProcedureParameter();

                            byte[] parameterType = tlv.getValue().getData();

                            parameter.setType( parameterType );

                            // We store the type in the current parameter.
                            storedProcedure.setCurrentParameter( parameter );

                            if ( LOG.isDebugEnabled() )
                            {
                                LOG.debug( I18n.msg( I18n.MSG_08210_PARAMETER_TYPE_FOUND, Strings.dumpBytes( parameterType ) ) );
                            }

                        }
                    }
                } );

        // Parameter ::= {
        //    ...
        //    value OCTETSTRING (Tag)
        // }
        // Store the parameter value
        super.transitions[StoredProcedureStatesEnum.PARAMETER_TYPE_STATE.ordinal()][UniversalTag.OCTET_STRING
            .getValue()] =
            new GrammarTransition<StoredProcedureContainer>( StoredProcedureStatesEnum.PARAMETER_TYPE_STATE,
                StoredProcedureStatesEnum.PARAMETER_VALUE_STATE,
                UniversalTag.OCTET_STRING.getValue(),
                new GrammarAction<StoredProcedureContainer>( "Store parameter value" )
                {
                    public void action( StoredProcedureContainer container ) throws DecoderException
                    {
                        StoredProcedureContainer storedProcedureContainer = container;

                        TLV tlv = storedProcedureContainer.getCurrentTLV();
                        StoredProcedureRequestDecorator storedProcedure = storedProcedureContainer.getStoredProcedure();

                        // Store the value.
                        if ( tlv.getLength() == 0 )
                        {
                            // We can't have a void parameter value !
                            String msg = I18n.err( I18n.ERR_08210_NULL_PARAMETER_VALUE );
                            LOG.error( msg );
                            throw new DecoderException( msg );
                        }
                        else
                        {
                            byte[] parameterValue = tlv.getValue().getData();

                            if ( parameterValue.length != 0 )
                            {
                                StoredProcedureParameter parameter = storedProcedure.getCurrentParameter();
                                parameter.setValue( parameterValue );

                                // We can now add a new Parameter to the procedure
                                storedProcedure.addParameter( parameter );

                                if ( LOG.isDebugEnabled() )
                                {
                                    LOG.debug( I18n.msg( I18n.MSG_08211_PARAMETER_VALUE_FOUND, Strings.dumpBytes( parameterValue ) ) );
                                }
                            }
                            else
                            {
                                String msg = I18n.err( I18n.ERR_08211_EMPTY_PARAMETER_VALUE );
                                LOG.error( msg );
                                throw new DecoderException( msg );
                            }
                        }

                        // The only possible END state for the grammar is here
                        container.setGrammarEndAllowed( true );
                    }
                } );

        // Parameters ::= SEQUENCE OF Parameter
        // 
        // Loop on next parameter
        super.transitions[StoredProcedureStatesEnum.PARAMETER_VALUE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<StoredProcedureContainer>( StoredProcedureStatesEnum.PARAMETER_VALUE_STATE,
                StoredProcedureStatesEnum.PARAMETER_STATE,
                UniversalTag.SEQUENCE.getValue(),
                null );
    }


    //~ Methods ------------------------------------------------------------------------------------

    /**
     * Get the instance of this grammar
     *
     * @return An instance on the StoredProcedure Grammar
     */
    public static StoredProcedureGrammar getInstance()
    {
        return instance;
    }
}
