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
package org.apache.directory.api.dsmlv2;


import org.apache.directory.api.dsmlv2.request.BatchRequestDsml;
import org.apache.directory.api.dsmlv2.response.BatchResponseDsml;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.xmlpull.v1.XmlPullParser;


/**
 * This class represents the DSML Container.
 * It used by the DSML Parser to store information.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Dsmlv2Container implements Container
{
    /** The current state of the decoding */
    private Enum<Dsmlv2StatesEnum> state;

    /** The current transition */
    private Enum<Dsmlv2StatesEnum> transition;

    /** Store the different states for debug purpose */
    private Enum<Dsmlv2StatesEnum>[] states;

    /** The pool parser */
    private XmlPullParser parser;

    /** The BatchRequest of the parsing */
    private BatchRequestDsml batchRequest;

    /** The BatchResponse of the parsing */
    private BatchResponseDsml batchResponse;

    /**  The associated grammar */
    private AbstractGrammar grammar;

    /** The codec service */
    private final LdapApiService codec;


    /**
     * Creates a new LdapMessageContainer object.
     * 
     * @param codec the Codec used to encode/decode the messages
     */
    public Dsmlv2Container( LdapApiService codec )
    {
        this.codec = codec;
    }


    /**
     * Gets the {@link LdapApiService} associated with this Container.
     *
     * @return The codec used to encode/decode the messages
     */
    public LdapApiService getLdapCodecService()
    {
        return codec;
    }


    /**
     * Gets the DSML Batch Request
     * 
     * @return Returns the Batch Request
     */
    public BatchRequestDsml getBatchRequest()
    {
        return batchRequest;
    }


    /**
     * Sets the DSML Batch Request
     * 
     * @param batchRequest the Batch Request to set
     */
    public void setBatchRequest( BatchRequestDsml batchRequest )
    {
        this.batchRequest = batchRequest;
    }


    /**
     * Gets the DSML Batch Response
     * 
     * @return Returns the Batch Response
     */
    public BatchResponseDsml getBatchResponse()
    {
        return batchResponse;
    }


    /**
     * Sets the DSML Batch Request
     * 
     * @param batchResponse the Batch Response to set
     */
    public void setBatchResponse( BatchResponseDsml batchResponse )
    {
        this.batchResponse = batchResponse;
    }


    /**
     * Gets the parser
     * 
     * @return the parser
     */
    public XmlPullParser getParser()
    {
        return parser;
    }


    /**
     * Sets the parser
     * 
     * @param parser the parser to set
     */
    public void setParser( XmlPullParser parser )
    {
        this.parser = parser;
    }


    /**
     * Get the current grammar state
     * 
     * @return the current grammar state
     */
    @Override
    public Enum<Dsmlv2StatesEnum> getState()
    {
        return state;
    }


    /**
     * Set the new current state
     * 
     * @param state the new state
     */
    @Override
    public void setState( Enum<Dsmlv2StatesEnum> state )
    {
        this.state = state;
    }


    /**
     * Get the transition
     * 
     * @return the transition from the previous state to the new state
     */
    @Override
    public Enum<Dsmlv2StatesEnum> getTransition()
    {
        return transition;
    }


    /**
     * Update the transition from a state to another
     * 
     * @param transition the transition to set
     */
    @Override
    public void setTransition( Enum<Dsmlv2StatesEnum> transition )
    {
        this.transition = transition;
    }


    /**
     * Get the states for this container's grammars
     * 
     * @return the states.
     */
    @Override
    public Enum<Dsmlv2StatesEnum>[] getStates()
    {
        return states;
    }


    /**
     * Gets the grammar
     *
     * @return the grammar
     */
    public AbstractGrammar getGrammar()
    {
        return grammar;
    }


    /**
     * Sets the Grammar
     * 
     * @param grammar the grammar to set
     */
    public void setGrammar( AbstractGrammar grammar )
    {
        this.grammar = grammar;
    }


    /**
     * Get the transition associated with the state and tag
     * 
     * @param currentState the current state
     * @param currentTag the current tag
     * @return a valid transition if any, or null.
     */
    public GrammarTransition getTransition( Enum<Dsmlv2StatesEnum> currentState, Tag currentTag )
    {
        return grammar.getTransition( currentState, currentTag );
    }
}
