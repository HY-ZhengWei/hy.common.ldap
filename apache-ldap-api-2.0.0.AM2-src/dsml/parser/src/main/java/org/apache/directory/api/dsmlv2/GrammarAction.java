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
 * A top level grammar class that store meta informations about the actions.
 * Those informations are not mandatory, but they can be usefull for debugging.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class GrammarAction implements Action
{
    /** The action's name */
    private String name;


    /**
     * Creates a new GrammarAction object.
     * 
     * @param name the name of the create daction
     */
    public GrammarAction( String name )
    {
        this.name = name;
    }


    /**
     * Print the action's name
     * 
     * @return the action's name
     */
    @Override
    public String toString()
    {
        return name;
    }
}