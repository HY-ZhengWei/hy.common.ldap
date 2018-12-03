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
package org.apache.directory.api.ldap.extras.controls.vlv_impl;


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to initialize the VirtualListViewRequestContainer object
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitVirtualListViewRequest extends GrammarAction<VirtualListViewRequestContainer>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( InitVirtualListViewRequest.class );

    /**
     * Instantiates a new VirtualListViewRequest action.
     */
    public InitVirtualListViewRequest()
    {
        super( "Initialize the VirtualListViewRequestContainer" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( VirtualListViewRequestContainer container )
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_08107_VLV_REQUEST_CONTAINER_INITIALIZED ) );
        }
    }
}
