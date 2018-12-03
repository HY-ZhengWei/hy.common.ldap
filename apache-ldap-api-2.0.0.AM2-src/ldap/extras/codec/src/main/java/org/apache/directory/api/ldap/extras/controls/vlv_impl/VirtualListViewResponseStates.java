/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.api.ldap.extras.controls.vlv_impl;


import org.apache.directory.api.asn1.ber.grammar.States;


/**
 * This class store the VirtualListViewResponse grammar constants. It is also used for
 * debugging purposes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum VirtualListViewResponseStates implements States
{
    /** Initial state */
    START_STATE,
    
    /** VirtualListViewResponse ::= SEQUENCE transition */
    VLV_SEQUENCE_STATE,
    
    /** targetPosition    INTEGER (0 .. maxInt) transition */
    VLV_TARGET_POSITION_STATE,

    /** contentCount     INTEGER (0 .. maxInt) transition */
    VLV_CONTENT_COUNT_STATE,

    /** virtualListViewResult ENUMERATED transition */
    VLV_VIRTUAL_LIST_VIEW_RESULT_STATE,

    /** contextID     OCTET STRING OPTIONAL transition */
    VLV_CONTEXT_ID_STATE,
    
    /** Final state */
    END_STATE;

    /**
     * Get the grammar name
     * 
     * @return The grammar name
     */
    public String getGrammarName()
    {
        return "VLV_RESPONSE_GRAMMAR";
    }


    /**
     * Get the string representing the state
     * 
     * @param state The state number
     * @return The String representing the state
     */
    public String getState( int state )
    {
        return ( state == END_STATE.ordinal() ) ? "VLV_RESPONSE_END_STATE" : name();
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
    public Enum<?> getStartState()
    {
        return START_STATE;
    }
}
