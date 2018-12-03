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


import org.apache.directory.api.asn1.ber.grammar.States;


/**
 * This class store the GracefulDisconnect's grammar constants. It is also used
 * for debugging purposes.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum GracefulDisconnectStatesEnum implements States
{
    // ~ Static fields/initializers
    // -----------------------------------------------------------------

    /** The END_STATE */
    END_STATE,

    // =========================================================================
    // GracefulDisconnect grammar states
    // =========================================================================
    /** Initial state */
    START_STATE,

    /** Sequence */
    GRACEFUL_DISCONNECT_SEQUENCE_STATE,

    /** Time offline */
    TIME_OFFLINE_STATE,

    /** Delay */
    DELAY_STATE,

    /** Replicated contexts */
    REPLICATED_CONTEXTS_STATE,

    /** Referral */
    REFERRAL_STATE,

    /** terminal state */
    LAST_GRACEFUL_DISCONNECT_STATE;

    /**
     * Get the grammar name
     * 
     * @return The grammar name
     */
    public String getGrammarName()
    {
        return "GRACEFUL_DISCONNECT_GRAMMAR";
    }


    /**
     * Get the string representing the state
     * 
     * @param state The state number
     * @return The String representing the state
     */
    public String getState( int state )
    {
        return ( state == END_STATE.ordinal() ) ? "GRACEFUL_DISCONNECT_END_STATE" : name();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEndState()
    {
        return this == END_STATE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public GracefulDisconnectStatesEnum getStartState()
    {
        return START_STATE;
    }
}
