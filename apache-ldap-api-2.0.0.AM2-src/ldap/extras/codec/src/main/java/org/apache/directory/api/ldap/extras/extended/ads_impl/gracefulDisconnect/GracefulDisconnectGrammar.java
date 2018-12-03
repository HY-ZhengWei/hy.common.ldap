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
package org.apache.directory.api.ldap.extras.extended.ads_impl.gracefulDisconnect;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.AbstractGrammar;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.grammar.GrammarTransition;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoder;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoderException;
import org.apache.directory.api.asn1.ber.tlv.UniversalTag;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapApiServiceFactory;
import org.apache.directory.api.ldap.extras.extended.gracefulDisconnect.GracefulDisconnectResponseImpl;
import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class implements the Graceful Disconnect. All the actions are declared
 * in this class. As it is a singleton, these declaration are only done once.
 * The grammar is :
 * 
 * <pre>
 *  GracefulDisconnect ::= SEQUENCE {
 *      timeOffline INTEGER (0..720) DEFAULT 0,
 *      delay [0] INTEGER (0..86400) DEFAULT 0,
 *      replicatedContexts Referral OPTIONAL
 * }
 *  
 *  Referral ::= SEQUENCE OF LDAPURL
 *  
 *  LDAPURL ::= LDAPString -- limited to characters permitted in URLs
 *  
 *  LDAPString ::= OCTET STRING
 * </pre>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class GracefulDisconnectGrammar extends AbstractGrammar<GracefulDisconnectContainer>
{
    /** The logger */
    static final Logger LOG = LoggerFactory.getLogger( GracefulDisconnectGrammar.class );

    /** The instance of grammar. GracefulDisconnectnGrammar is a singleton */
    private static GracefulDisconnectGrammar instance = new GracefulDisconnectGrammar();

    /**
     * The action used to store a Time Offline.
     */
    private GrammarAction<GracefulDisconnectContainer> storeDelay =
        new GrammarAction<GracefulDisconnectContainer>( "Set Graceful Disconnect Delay" )
        {
            public void action( GracefulDisconnectContainer container ) throws DecoderException
            {
                BerValue value = container.getCurrentTLV().getValue();

                try
                {
                    int delay = IntegerDecoder.parse( value, 0, 86400 );

                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_08204_DELAY, delay ) );
                    }

                    container.getGracefulDisconnectResponse().setDelay( delay );
                    container.setGrammarEndAllowed( true );
                }
                catch ( IntegerDecoderException ide )
                {
                    String msg = I18n.err( I18n.ERR_08205_CANNOT_DECODE_DELAY, Strings.dumpBytes( value.getData() ) );
                    LOG.error( msg );
                    throw new DecoderException( msg, ide );
                }
            }
        };

    /**
     * The action used to store a referral.
     */
    private GrammarAction<GracefulDisconnectContainer> storeReferral =
        new GrammarAction<GracefulDisconnectContainer>( "Stores a referral" )
        {
            public void action( GracefulDisconnectContainer container ) throws DecoderException
            {
                BerValue value = container.getCurrentTLV().getValue();

                try
                {
                    if ( Strings.isEmpty( value.getData() ) )
                    {
                        String msg = I18n.err( I18n.ERR_08224_NULL_URL_DECODING_FAILURE );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }

                    String url = Strings.utf8ToString( value.getData() );

                    LdapUrl ldapUrl = new LdapUrl( url );
                    container.getGracefulDisconnectResponse().addReplicatedContexts( url );
                    container.setGrammarEndAllowed( true );

                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_08214_STORES_A_REFERRAL, ldapUrl ) );
                    }
                }
                catch ( LdapURLEncodingException luee )
                {
                    String msg = I18n.err( I18n.ERR_08225_URL_DECODING_FAILURE, Strings.dumpBytes( value.getData() ) );
                    LOG.error( msg );
                    throw new DecoderException( msg, luee );
                }
            }
        };

    /**
     * The action used to store a Time Offline.
     */
    private GrammarAction<GracefulDisconnectContainer> storeTimeOffline =
        new GrammarAction<GracefulDisconnectContainer>( "Set Graceful Disconnect time offline" )
        {
            public void action( GracefulDisconnectContainer container ) throws DecoderException
            {
                BerValue value = container.getCurrentTLV().getValue();

                try
                {
                    int timeOffline = IntegerDecoder.parse( value, 0, 720 );

                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_08216_TIME_OFFLINE, timeOffline ) );
                    }

                    container.getGracefulDisconnectResponse().setTimeOffline( timeOffline );
                    container.setGrammarEndAllowed( true );
                }
                catch ( IntegerDecoderException ide )
                {
                    String msg = I18n.err( I18n.ERR_08206_TIME_OFFLINE_DECODING_FAILED, Strings.dumpBytes( value.getData() ) );
                    LOG.error( msg );
                    throw new DecoderException( msg, ide );
                }
            }
        };


    /**
     * Creates a new GracefulDisconnectGrammar object.
     */
    @SuppressWarnings("unchecked")
    private GracefulDisconnectGrammar()
    {
        setName( GracefulDisconnectGrammar.class.getName() );

        // Create the transitions table
        super.transitions = new GrammarTransition[GracefulDisconnectStatesEnum.LAST_GRACEFUL_DISCONNECT_STATE.ordinal()][256];

        /**
         * Transition from init state to graceful disconnect
         * GracefulDisconnect ::= SEQUENCE { 
         *     ... 
         * 
         * Creates the GracefulDisconnect object
         */
        super.transitions[GracefulDisconnectStatesEnum.START_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<GracefulDisconnectContainer>( GracefulDisconnectStatesEnum.START_STATE,
                GracefulDisconnectStatesEnum.GRACEFUL_DISCONNECT_SEQUENCE_STATE,
                UniversalTag.SEQUENCE.getValue(),
                new GrammarAction<GracefulDisconnectContainer>( "Init Graceful Disconnect" )
                {
                    public void action( GracefulDisconnectContainer container )
                    {
                        GracefulDisconnectResponseDecorator gracefulDisconnectResponse = 
                            new GracefulDisconnectResponseDecorator(
                                LdapApiServiceFactory.getSingleton(),
                                new GracefulDisconnectResponseImpl()
                                );
                        container.setGracefulDisconnectResponse( gracefulDisconnectResponse );
                        container.setGrammarEndAllowed( true );
                    }
                } );

        /**
         * Transition from graceful disconnect to time offline
         * 
         * GracefulDisconnect ::= SEQUENCE { 
         *     timeOffline INTEGER (0..720) DEFAULT 0, 
         *     ... 
         *     
         * Set the time offline value into the GracefulDisconnect object.    
         */
        super.transitions[GracefulDisconnectStatesEnum.GRACEFUL_DISCONNECT_SEQUENCE_STATE.ordinal()][UniversalTag.INTEGER
            .getValue()] =
            new GrammarTransition<GracefulDisconnectContainer>(
                GracefulDisconnectStatesEnum.GRACEFUL_DISCONNECT_SEQUENCE_STATE,
                GracefulDisconnectStatesEnum.TIME_OFFLINE_STATE,
                UniversalTag.INTEGER.getValue(), storeTimeOffline );

        /**
         * Transition from graceful disconnect to delay
         * 
         * GracefulDisconnect ::= SEQUENCE { 
         *     ... 
         *     delay [0] INTEGER (0..86400) DEFAULT 0,
         *     ... 
         *     
         * Set the delay value into the GracefulDisconnect object.    
         */
        super.transitions[GracefulDisconnectStatesEnum.GRACEFUL_DISCONNECT_SEQUENCE_STATE.ordinal()][GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG] =
            new GrammarTransition<GracefulDisconnectContainer>(
                GracefulDisconnectStatesEnum.GRACEFUL_DISCONNECT_SEQUENCE_STATE,
                GracefulDisconnectStatesEnum.DELAY_STATE,
                GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG,
                storeDelay );

        /**
         * Transition from graceful disconnect to replicated Contexts
         * 
         * GracefulDisconnect ::= SEQUENCE { 
         *     ... 
         *     replicatedContexts Referral OPTIONAL } 
         *     
         * Referral ::= SEQUENCE OF LDAPURL
         *     
         * Get some replicated contexts. Nothing to do    
         */
        super.transitions[GracefulDisconnectStatesEnum.GRACEFUL_DISCONNECT_SEQUENCE_STATE.ordinal()][UniversalTag.SEQUENCE
            .getValue()] =
            new GrammarTransition<GracefulDisconnectContainer>(
                GracefulDisconnectStatesEnum.GRACEFUL_DISCONNECT_SEQUENCE_STATE,
                GracefulDisconnectStatesEnum.REPLICATED_CONTEXTS_STATE,
                UniversalTag.SEQUENCE.getValue(), null );

        /**
         * Transition from time offline to delay
         * 
         * GracefulDisconnect ::= SEQUENCE { 
         *     ... 
         *     delay [0] INTEGER (0..86400) DEFAULT 0,
         *     ... 
         *     
         * Set the delay value into the GracefulDisconnect object.    
         */
        super.transitions[GracefulDisconnectStatesEnum.TIME_OFFLINE_STATE.ordinal()][GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG] =
            new GrammarTransition<GracefulDisconnectContainer>( GracefulDisconnectStatesEnum.TIME_OFFLINE_STATE,
                GracefulDisconnectStatesEnum.DELAY_STATE,
                GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG,
                storeDelay );

        /**
         * Transition from time offline to replicated Contexts
         * 
         * GracefulDisconnect ::= SEQUENCE { 
         *     ... 
         *     replicatedContexts Referral OPTIONAL } 
         *     
         * Referral ::= SEQUENCE OF LDAPURL
         *     
         * Get some replicated contexts. Nothing to do    
         */
        super.transitions[GracefulDisconnectStatesEnum.TIME_OFFLINE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<GracefulDisconnectContainer>( GracefulDisconnectStatesEnum.TIME_OFFLINE_STATE,
                GracefulDisconnectStatesEnum.REPLICATED_CONTEXTS_STATE,
                UniversalTag.SEQUENCE.getValue(), null );

        /**
         * Transition from delay to replicated contexts
         * 
         * GracefulDisconnect ::= SEQUENCE { 
         *     ... 
         *     replicatedContexts Referral OPTIONAL } 
         *     
         * Referral ::= SEQUENCE OF LDAPURL
         *     
         * Get some replicated contexts. Nothing to do    
         */
        super.transitions[GracefulDisconnectStatesEnum.DELAY_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<GracefulDisconnectContainer>( GracefulDisconnectStatesEnum.DELAY_STATE,
                GracefulDisconnectStatesEnum.REPLICATED_CONTEXTS_STATE,
                UniversalTag.SEQUENCE.getValue(), null );

        /**
         * Transition from replicated contexts to referral
         * 
         * GracefulDisconnect ::= SEQUENCE { 
         *     ... 
         *     replicatedContexts Referral OPTIONAL } 
         *     
         * Referral ::= SEQUENCE OF LDAPURL
         *     
         * Stores the referral
         */
        super.transitions[GracefulDisconnectStatesEnum.REPLICATED_CONTEXTS_STATE.ordinal()][UniversalTag.OCTET_STRING
            .getValue()] =
            new GrammarTransition<GracefulDisconnectContainer>( GracefulDisconnectStatesEnum.REPLICATED_CONTEXTS_STATE,
                GracefulDisconnectStatesEnum.REFERRAL_STATE,
                UniversalTag.OCTET_STRING.getValue(),
                storeReferral );

        /**
         * Transition from referral to referral
         * 
         * GracefulDisconnect ::= SEQUENCE { 
         *     ... 
         *     replicatedContexts Referral OPTIONAL } 
         *     
         * Referral ::= SEQUENCE OF LDAPURL
         *     
         * Stores the referral
         */
        super.transitions[GracefulDisconnectStatesEnum.REFERRAL_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] =
            new GrammarTransition<GracefulDisconnectContainer>( GracefulDisconnectStatesEnum.REFERRAL_STATE,
                GracefulDisconnectStatesEnum.REFERRAL_STATE,
                UniversalTag.OCTET_STRING.getValue(),
                storeReferral );

    }


    /**
     * This class is a singleton.
     * 
     * @return An instance on this grammar
     */
    public static GracefulDisconnectGrammar getInstance()
    {
        return instance;
    }
}
