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


import org.apache.directory.api.ldap.model.schema.AttributeType;


/**
 * An element of  MaxValueCount.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MaxValueCountElem
{
    /** The targeted AttributeType */
    private AttributeType attributeType;

    /** The maximum number of accepted values for this attributeType */
    private int maxCount;

    /**
     * Creates a new instance.
     * 
     * @param attributeType the attribute ID to limit the maximum count
     * @param maxCount the maximum count of the attribute allowed
     */
    public MaxValueCountElem( AttributeType attributeType, int maxCount )
    {
        this.attributeType = attributeType;
        this.maxCount = maxCount;
    }


    /**
     * Gets the attribute to limit the maximum count.
     *
     * @return the attribute type
     */
    public AttributeType getAttributeType()
    {
        return attributeType;
    }


    /**
     * Gets the maximum count of the attribute allowed.
     *
     * @return the maximum count of the attribute allowed
     */
    public int getMaxCount()
    {
        return maxCount;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int hash = 37;
        hash = hash * 17 + maxCount;

        if ( attributeType != null )
        {
            hash = hash * 17 + attributeType.hashCode();
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

        if ( o instanceof MaxValueCountElem )
        {
            MaxValueCountElem that = ( MaxValueCountElem ) o;

            if ( maxCount == that.maxCount )
            {
                if ( attributeType == null )
                {
                    return that.attributeType == null;
                }
                else
                {
                    return attributeType.equals( that.attributeType );
                }
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
        StringBuilder sb = new StringBuilder();
        
        sb.append( "{ type " );
        
        if ( attributeType != null )
        {
            sb.append( attributeType.getName() );
        }
        else
        {
            sb.append( "null" );
        }
        
        sb.append( ", maxCount " ).append( maxCount );
        sb.append( "}" );
        
        return sb.toString();
    }
}
