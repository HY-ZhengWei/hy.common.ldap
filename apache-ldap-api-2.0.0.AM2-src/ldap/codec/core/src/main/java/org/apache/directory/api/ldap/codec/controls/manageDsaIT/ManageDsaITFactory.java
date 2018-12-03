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
package org.apache.directory.api.ldap.codec.controls.manageDsaIT;


import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.codec.api.ControlFactory;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaIT;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaITImpl;


/**
 * A codec {@link ControlFactory} implementation for {@link ManageDsaIT} control.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class ManageDsaITFactory implements ControlFactory<ManageDsaIT>
{
    /** The LDAP codec responsible for encoding and decoding ManageDsaIT Control */
    private LdapApiService codec;


    /**
     * Creates a new instance of ManageDsaITFactory.
     *
     * @param codec The LDAP codec
     */
    public ManageDsaITFactory( LdapApiService codec )
    {
        this.codec = codec;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getOid()
    {
        return ManageDsaIT.OID;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CodecControl<ManageDsaIT> newCodecControl()
    {
        return new ManageDsaITDecorator( codec, new ManageDsaITImpl() );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CodecControl<ManageDsaIT> newCodecControl( ManageDsaIT control )
    {
        return new ManageDsaITDecorator( codec, control );
    }
}
