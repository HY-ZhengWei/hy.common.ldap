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

package org.apache.directory.api.ldap.trigger;


/**
 * An enumeration that represents action times
 * for an LDAP trigger specification.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ActionTime
{
    /** The AFTER instance. */
    public static final ActionTime AFTER = new ActionTime( "AFTER" );

    /** The name. */
    private final String name;


    private ActionTime( String name )
    {
        this.name = name;
    }


    /**
     * Returns the name of this action time.
     *
     * @return the name of this action time.
     */
    public String getName()
    {
        return name;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return name;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int h = 37;
        h = h * 17 + ( ( name == null ) ? 0 : name.hashCode() );

        return h;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final ActionTime other = ( ActionTime ) obj;
        if ( name == null )
        {
            if ( other.name != null )
            {
                return false;
            }
        }
        else if ( !name.equals( other.name ) )
        {
            return false;
        }
        return true;
    }
}
