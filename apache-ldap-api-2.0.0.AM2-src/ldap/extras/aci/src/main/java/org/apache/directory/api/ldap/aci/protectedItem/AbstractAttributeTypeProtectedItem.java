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
import org.apache.directory.api.ldap.model.schema.AttributeType;


/**
 * A base class for all items which protects attribute types (or its values)
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractAttributeTypeProtectedItem extends ProtectedItem
{
    /** The attribute types. */
    protected final Set<AttributeType> attributeTypes;

    /**
     * Creates a new instance.
     * 
     * @param attributeTypes the collection of attribute IDs
     */
    protected AbstractAttributeTypeProtectedItem( Set<AttributeType> attributeTypes )
    {
        this.attributeTypes = Collections.unmodifiableSet( attributeTypes );
    }


    /**
     * Gets an iterator of all attribute types.
     *
     * @return the iterator of all attribute types
     */
    public Iterator<AttributeType> iterator()
    {
        return attributeTypes.iterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int hash = 37;
        
        if ( attributeTypes != null )
        {
            for ( AttributeType attributeType : attributeTypes )
            {
                hash = hash * 17 + attributeType.hashCode();
            }
        }

        hash = hash * 17 + getClass().getName().hashCode();

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

        if ( o == null )
        {
            return false;
        }

        if ( getClass().isAssignableFrom( o.getClass() ) )
        {
            AbstractAttributeTypeProtectedItem that = ( AbstractAttributeTypeProtectedItem ) o;
            
            if ( attributeTypes != null )
            {
                if ( ( that.attributeTypes == null ) || ( that.attributeTypes.size() != attributeTypes.size() ) )
                {
                    return false;
                }
                
                for ( AttributeType attributeType : attributeTypes )
                {
                    if ( !that.attributeTypes.contains( attributeType ) )
                    {
                        return false;
                    }
                }
                
                return true;
            }
            else
            {
                return that.attributeTypes == null;
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

        buf.append( "{ " );
        boolean isFirst = true;

        if ( attributeTypes != null )
        {
            for ( AttributeType attributeType : attributeTypes )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    buf.append( ", " );
                }
    
                buf.append( attributeType.getName() );
            }
        }

        buf.append( " }" );

        return buf.toString();
    }
}
