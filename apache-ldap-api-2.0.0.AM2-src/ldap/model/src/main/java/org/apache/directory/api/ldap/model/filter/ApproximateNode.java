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


import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.schema.AttributeType;


/**
 * A simple assertion value node.
 * 
 * @param <T> The Value type
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApproximateNode<T> extends SimpleNode<T>
{
    /**
     * Creates a new ApproximateNode object.
     * 
     * @param attributeType the attribute type
     * @param value the value to test for
     */
    public ApproximateNode( AttributeType attributeType, Value value )
    {
        super( attributeType, value, AssertionType.APPROXIMATE );
    }


    /**
     * Creates a new ApproximateNode object.
     * 
     * @param attribute the attribute name
     * @param value the value to test for
     */
    public ApproximateNode( String attribute, String value )
    {
        super( attribute, value, AssertionType.APPROXIMATE );
    }


    /**
     * Creates a new ApproximateNode object.
     * 
     * @param attribute the attribute name
     * @param value the value to test for
     */
    public ApproximateNode( String attribute, byte[] value )
    {
        super( attribute, value, AssertionType.APPROXIMATE );
    }


    /**
     * @see Object#toString()
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

        buf.append( "~=" );

        String escapedValue = getEscapedValue();
        
        if ( escapedValue != null )
        {
            buf.append( escapedValue );
        }

        buf.append( super.toString() );

        buf.append( ')' );

        return buf.toString();
    }
}
