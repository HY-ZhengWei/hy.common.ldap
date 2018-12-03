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
package org.apache.directory.ldap.client.api;


import java.io.IOException;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The default implementation of LdapConnectionFactory. Allows for the 
 * setting of timeout and {@link LdapApiService} as well as the standard 
 * {@link LdapConnectionConfig}.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DefaultLdapConnectionFactory implements LdapConnectionFactory
{
    private static final Logger LOG = LoggerFactory.getLogger( DefaultLdapConnectionFactory.class );

    private LdapApiService apiService;
    private LdapConnectionConfig connectionConfig;
    private long timeout;


    /**
     * Creates a new instance of DefaultLdapConnectionFactory.
     *
     * @param config The connection config.
     */
    public DefaultLdapConnectionFactory( LdapConnectionConfig config )
    {
        this.connectionConfig = config;
        this.timeout = config.getTimeout();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LdapConnection bindConnection( LdapConnection connection ) throws LdapException
    {
        try
        {
            connection.bind( connectionConfig.getName(), connectionConfig.getCredentials() );
        }
        catch ( LdapException e )
        {
            LOG.error( I18n.err( I18n.ERR_04102_UNABLE_TO_BIND_CONNECTION, e.getMessage() ) );
            
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_04158_UNABLE_TO_BIND, e.getMessage() ) );
            }

            try
            {
                connection.close();
            }
            catch ( IOException ioe )
            {
                LOG.error( I18n.err( I18n.ERR_04103_UNABLE_TO_CLOSE_FAILED_CONNECTION, e.getMessage() ), ioe );

                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_04159_UNABLE_TO_CLOSE_CONNECTION, e.getMessage() ) );
                }
            }

            throw e;
        }

        return connection;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LdapConnection configureConnection( LdapConnection connection )
    {
        connection.setTimeOut( timeout );
        connection.setBinaryAttributeDetector( connectionConfig.getBinaryAttributeDetector() );
        return connection;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LdapApiService getLdapApiService()
    {
        return apiService;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LdapConnection newLdapConnection() throws LdapException
    {
        return bindConnection( newUnboundLdapConnection() );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LdapConnection newUnboundLdapConnection()
    {
        if ( apiService == null )
        {
            return configureConnection( new LdapNetworkConnection( connectionConfig ) );
        }
        else
        {
            return configureConnection( new LdapNetworkConnection( connectionConfig, apiService ) );
        }
    }


    /**
     * Sets the LdapApiService (codec) to be used by the connections created
     * by this factory.
     *
     * @param apiService The codec to used by connections created by this 
     * factory
     */
    public void setLdapApiService( LdapApiService apiService )
    {
        this.apiService = apiService;
    }


    /**
     * Sets the timeout that will be used by all connections created by this
     * factory.
     *
     * @param timeout The timeout in millis.
     * 
     * @see LdapConnection#setTimeOut(long)
     */
    public void setTimeOut( long timeout )
    {
        this.timeout = timeout;
    }
}
