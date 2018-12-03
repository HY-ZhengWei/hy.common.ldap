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
package org.apache.directory.api.ldap.model.filter;


import org.apache.directory.api.ldap.model.schema.AttributeType;


/**
 * Filter expression tree node representing a filter attribute value assertion
 * for presence.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class PresenceNode extends LeafNode
{
    /**
     * Creates a PresenceNode object based on an attribute.
     * 
     * @param attributeType the attributeType to assert the presence of
     */
    public PresenceNode( AttributeType attributeType )
    {
        super( attributeType, AssertionType.PRESENCE );
    }


    /**
     * Creates a PresenceNode object based on an attribute.
     * 
     * @param attribute the attribute to assert the presence of
     */
    public PresenceNode( String attribute )
    {
        super( attribute, AssertionType.PRESENCE );
    }


    /**
     * @see java.lang.Object#toString()
     * @return A string representing the AndNode
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append( '(' );

        if ( attributeType != null )
        {
            buf.append( attributeType.getName() );
        }
        else
        {
            buf.append( attribute );
        }

        buf.append( "=*" );

        buf.append( super.toString() );

        buf.append( ')' );

        return buf.toString();
    }
}
