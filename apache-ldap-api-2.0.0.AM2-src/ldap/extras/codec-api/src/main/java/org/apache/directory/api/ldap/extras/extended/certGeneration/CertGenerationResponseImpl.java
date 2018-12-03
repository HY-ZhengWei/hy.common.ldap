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
package org.apache.directory.api.ldap.extras.extended.certGeneration;


import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.message.ExtendedResponseImpl;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;


/**
 * 
 * The response sent back from the server after the CertGeneration extended operation is performed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertGenerationResponseImpl extends ExtendedResponseImpl implements CertGenerationResponse
{
    /**
     * Create a new CertGenerationResponseImpl instance
     * 
     * @param messageId The request's message ID
     * @param rcode The result code
     */
    public CertGenerationResponseImpl( int messageId, ResultCodeEnum rcode )
    {
        super( messageId, EXTENSION_OID );

        switch ( rcode )
        {
            case SUCCESS:
            case OPERATIONS_ERROR:
            case INSUFFICIENT_ACCESS_RIGHTS:
                break;

            default:
                throw new IllegalArgumentException( I18n.err( I18n.ERR_13503_RESULT_CODE_SHOULD_BE_IN, ResultCodeEnum.SUCCESS,
                    ResultCodeEnum.OPERATIONS_ERROR, ResultCodeEnum.INSUFFICIENT_ACCESS_RIGHTS ) );
        }

        super.getLdapResult().setMatchedDn( null );
        super.getLdapResult().setResultCode( rcode );
    }


    /**
     * Create a new CertGenerationResponseImpl instance
     * 
     * @param messageId The request's message ID
     */
    public CertGenerationResponseImpl( int messageId )
    {
        super( messageId, EXTENSION_OID );
        super.getLdapResult().setMatchedDn( null );
        super.getLdapResult().setResultCode( ResultCodeEnum.SUCCESS );
    }


    /**
     * Create a new CertGenerationResponseImpl instance
     */
    public CertGenerationResponseImpl()
    {
        super( EXTENSION_OID );
        super.getLdapResult().setMatchedDn( null );
        super.getLdapResult().setResultCode( ResultCodeEnum.SUCCESS );
    }


    /**
     * Gets the OID uniquely identifying this extended response (a.k.a. its
     * name).
     * 
     * @return the OID of the extended response type.
     */
    @Override
    public String getResponseName()
    {
        return EXTENSION_OID;
    }


    /**
     * Sets the OID uniquely identifying this extended response (a.k.a. its
     * name).
     * 
     * @param oid the OID of the extended response type.
     */
    @Override
    public void setResponseName( String oid )
    {
        throw new UnsupportedOperationException( I18n.err( I18n.ERR_13504_FIX_OID, EXTENSION_OID ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int hash = 37;
        // Seems simple but look at the equals() method ...
        hash = hash * 17 + getClass().getName().hashCode();

        return hash;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }

        return obj instanceof CertGenerationResponseImpl;
    }
}
