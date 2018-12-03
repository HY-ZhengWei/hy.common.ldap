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
package org.apache.directory.api.ldap.codec.protocol.mina;


import java.nio.ByteBuffer;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapApiServiceFactory;
import org.apache.directory.api.ldap.codec.api.LdapEncoder;
import org.apache.directory.api.ldap.model.constants.Loggers;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.util.Strings;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A LDAP message encoder. It is based on api-ldap encoder.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapProtocolEncoder implements ProtocolEncoder
{
    /** logger for reporting errors that might not be handled properly upstream */
    private static final Logger CODEC_LOG = LoggerFactory.getLogger( Loggers.CODEC_LOG.getName() );

    /** The stateful encoder */
    private LdapEncoder encoder;


    /**
     * Creates a new instance of LdapProtocolEncoder.
     */
    public LdapProtocolEncoder()
    {
        this( LdapApiServiceFactory.getSingleton() );
    }

    /**
     * Creates a new instance of LdapProtocolEncoder.
     *
     * @param ldapApiService The Service to use
     */
    public LdapProtocolEncoder( LdapApiService ldapApiService )
    {
        this.encoder = new LdapEncoder( ldapApiService );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void encode( IoSession session, Object message, ProtocolEncoderOutput out ) throws Exception
    {
        ByteBuffer buffer = encoder.encodeMessage( ( Message ) message );

        IoBuffer ioBuffer = IoBuffer.wrap( buffer );

        if ( CODEC_LOG.isDebugEnabled() )
        {
            byte[] dumpBuffer = new byte[buffer.limit()];
            buffer.get( dumpBuffer );
            buffer.flip();
            CODEC_LOG.debug( I18n.msg( I18n.MSG_14003_ENCODED_LDAP_MESSAGE, message, Strings.dumpBytes( dumpBuffer ) ) );
        }

        out.write( ioBuffer );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose( IoSession session ) throws Exception
    {
        // Nothing to do
    }
}
