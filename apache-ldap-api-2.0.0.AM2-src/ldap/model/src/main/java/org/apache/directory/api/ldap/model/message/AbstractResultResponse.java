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
package org.apache.directory.api.ldap.model.message;


/**
 * Abstract base for a ResultResponse message.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractResultResponse extends AbstractResponse implements ResultResponse
{
    /** Response result components */
    protected LdapResult ldapResult = new LdapResultImpl();


    // ------------------------------------------------------------------------
    // Response Interface Method Implementations
    // ------------------------------------------------------------------------

    /**
     * Allows subclasses based on the abstract type to create a response to a
     * request.
     * 
     * @param id the response eliciting this Request
     * @param type the message type of the response
     */
    protected AbstractResultResponse( final int id, final MessageTypeEnum type )
    {
        super( id, type );
    }


    // ------------------------------------------------------------------------
    // Response Interface Method Implementations
    // ------------------------------------------------------------------------
    /**
     * Gets the LdapResult components of this Response.
     * 
     * @return the LdapResult for this Response.
     */
    @Override
    public LdapResult getLdapResult()
    {
        return ldapResult;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int hash = 37;
        if ( getLdapResult() != null )
        {
            hash = hash * 17 + getLdapResult().hashCode();
        }
        hash = hash * 17 + super.hashCode();

        return hash;
    }


    /**
     * Checks to see if an object is equal to this AbstractResultResponse. First
     * the object is checked to see if it is this AbstractResultResponse
     * instance if so it returns true. Next it checks if the super method
     * returns false and if it does false is returned. It then checks if the
     * LDAPResult's are equal. If not false is returned and if they match true
     * is returned.
     * 
     * @param obj
     *            the object to compare to this LdapResult containing response
     * @return true if they objects are equivalent false otherwise
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }

        if ( !super.equals( obj ) )
        {
            return false;
        }

        if ( !( obj instanceof ResultResponse ) )
        {
            return false;
        }

        ResultResponse resp = ( ResultResponse ) obj;

        return ( ( ldapResult != null ) && ldapResult.equals( resp.getLdapResult() ) ) 
            || ( resp.getLdapResult() == null );
    }


    /**
     * Get a String representation of an Response
     * 
     * @return An Response String
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( ldapResult );

        if ( ( controls != null ) && ( controls.size() != 0 ) )
        {
            for ( Control control : controls.values() )
            {
                sb.append( control );
            }
        }

        return sb.toString();
    }
}
