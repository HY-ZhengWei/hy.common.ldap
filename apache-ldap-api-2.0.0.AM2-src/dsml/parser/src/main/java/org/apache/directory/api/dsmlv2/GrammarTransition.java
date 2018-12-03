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


/**
 * Define a transition between two states of a grammar. It stores the next
 * state, and the action to execute while transiting.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GrammarTransition
{
    /** The next state in the grammar */
    private Enum<Dsmlv2StatesEnum> nextState;

    /** The action associated to the transition */
    private GrammarAction action;

    /** The current state */
    private Enum<Dsmlv2StatesEnum> currentState;


    /**
     * Creates a new GrammarTransition object.
     * 
     * @param currentState The current transition
     * @param nextState The target state
     * @param action The action to execute. It could be null.
     */
    public GrammarTransition( Enum<Dsmlv2StatesEnum> currentState, Enum<Dsmlv2StatesEnum> nextState,
        GrammarAction action )
    {
        this.currentState = currentState;
        this.nextState = nextState;
        this.action = action;
    }


    /**
     * Gets the target state
     * 
     * @return the target state.
     */
    public Enum<Dsmlv2StatesEnum> getNextState()
    {
        return nextState;
    }


    /**
     * Tells if the transition has an associated action.
     * 
     * @return  <code>true</code> if an action has been associated to the transition
     */
    public boolean hasAction()
    {
        return action != null;
    }


    /**
     * Gets the action associated with the transition
     * 
     * @return the action associated with the transition
     */
    public GrammarAction getAction()
    {
        return action;
    }


    /**
     * Returns a representation of the transition as a string
     * 
     * @param grammar the grammar which state we want a String from
     * @param statesEnum the states enum that contains the states' names
     * @return  a representation of the transition as a string.
     */
    public String toString( int grammar, Enum<Dsmlv2StatesEnum> statesEnum )
    {

        StringBuilder sb = new StringBuilder();

        sb.append( "Transition from <" ).append( currentState ).append( "> to <" ).append(
            nextState ).append( ">, action : " ).append(
            ( action == null ) ? "no action" : action.toString() ).append( ">" );

        return sb.toString();
    }
}