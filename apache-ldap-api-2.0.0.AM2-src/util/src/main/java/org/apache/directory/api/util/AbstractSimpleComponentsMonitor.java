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

package org.apache.directory.api.util;


import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.api.i18n.I18n;


/**
 * Abstract implementation of a components monitor.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractSimpleComponentsMonitor implements ComponentsMonitor
{

    /** The components. */
    private List<String> components;


    /**
     * Instantiates a new abstract simple components monitor.
     *
     * @param components the components
     */
    public AbstractSimpleComponentsMonitor( String[] components )
    {
        // register components
        this.components = new LinkedList<>( Arrays.asList( components ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsMonitor useComponent( String component )
    {
        if ( !components.remove( component ) )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_17026_UNREGISTRED_COMPONENT, component ) );
        }

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allComponentsUsed()
    {
        return components.isEmpty();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRemainingComponents()
    {
        return Collections.unmodifiableList( components );
    }

}
