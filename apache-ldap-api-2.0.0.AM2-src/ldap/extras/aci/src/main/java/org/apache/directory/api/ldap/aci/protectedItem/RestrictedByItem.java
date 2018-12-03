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
package org.apache.directory.api.ldap.aci.protectedItem;


import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.directory.api.ldap.aci.ProtectedItem;


/**
 * Restricts values added to the attribute type to being values that are
 * already present in the same entry as values of the attribute valuesIn. It
 * is examined if the protected item is an attribute value of the specified
 * type and the permission sought is add. Values of the valuesIn attribute
 * are checked without regard to context or access control and as though the
 * operation which adds the values were successful. If the value to be added
 * is not present in valuesIn the ACI item is treated as not granting add
 * access.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RestrictedByItem extends ProtectedItem
{
    /** The set of restricted elements */
    private final Set<RestrictedByElem> items;

    /**
     * Creates a new instance.
     * 
     * @param items the collection of {@link RestrictedByElem}s.
     */
    public RestrictedByItem( Set<RestrictedByElem> items )
    {
        this.items = Collections.unmodifiableSet( items );
    }


    /**
     * Gets an iterator of all {@link RestrictedByElem}s.
     *
     * @return the iterator of all {@link RestrictedByElem}s
     */
    public Iterator<RestrictedByElem> iterator()
    {
        return items.iterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int hash = 37;
        
        if ( items != null )
        {
            for ( RestrictedByElem item : items )
            {
                if ( item != null )
                {
                    hash = hash * item.hashCode();
                }
                else
                {
                    hash = hash * 37;
                }
            }
        }

        return hash;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o instanceof RestrictedByItem )
        {
            RestrictedByItem that = ( RestrictedByItem ) o;

            if ( items != null )
            {
                if ( that.items == null )
                {
                    return false;
                }
                
                if ( items.size() != that.items.size() )
                {
                    return false;
                }
                
                for ( RestrictedByElem item : items )
                {
                    if ( !that.items.contains( item ) )
                    {
                        return false;
                    }
                }
                
                return true;
            }
            else
            {
                return that.items == null;
            }
        }

        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "restrictedBy {" );

        boolean isFirst = true;

        if ( items != null )
        {
            for ( RestrictedByElem item : items )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    buf.append( ", " );
                }
    
                buf.append( item.toString() );
            }
        }

        buf.append( '}' );

        return buf.toString();
    }
}
