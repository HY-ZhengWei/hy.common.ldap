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
 * Filter expression tree node for extensible assertions.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtensibleNode extends LeafNode
{
    /** The value of the attribute to match for */
    private Value value;
    
    /** The value as a byte[] */
    protected byte[] bytes;

    /** The matching rules id */
    private String matchingRuleId;

    /** The name of the dn attributes */
    private boolean dnAttributes = false;


    /**
     * Creates a new emptyExtensibleNode object.
     * 
     * @param attributeType the attributeType associated with this node
     */
    public ExtensibleNode( AttributeType attributeType )
    {
        super( attributeType, AssertionType.EXTENSIBLE );

        dnAttributes = false;
    }


    /**
     * Creates a new emptyExtensibleNode object.
     * 
     * @param attribute the attribute associated with this node
     */
    public ExtensibleNode( String attribute )
    {
        super( attribute, AssertionType.EXTENSIBLE );

        dnAttributes = false;
    }


    /**
     * Creates a new ExtensibleNode object.
     * 
     * @param attributeType the attributeType used for the extensible assertion
     * @param value the value to match for
     * @param matchingRuleId the OID of the matching rule
     * @param dnAttributes the dn attributes
     */
    public ExtensibleNode( AttributeType attributeType, Value value, String matchingRuleId, boolean dnAttributes )
    {
        super( attributeType, AssertionType.EXTENSIBLE );

        this.value = value;
        this.matchingRuleId = matchingRuleId;
        this.dnAttributes = dnAttributes;
    }


    /**
     * Creates a new ExtensibleNode object.
     * 
     * @param attribute the attribute used for the extensible assertion
     * @param value the value to match for
     * @param matchingRuleId the OID of the matching rule
     * @param dnAttributes the dn attributes
     */
    public ExtensibleNode( String attribute, Value value, String matchingRuleId, boolean dnAttributes )
    {
        super( attribute, AssertionType.EXTENSIBLE );

        this.value = value;
        this.matchingRuleId = matchingRuleId;
        this.dnAttributes = dnAttributes;
    }


    /**
     * Makes a full clone in new memory space of the current node and children
     * 
     * @return the clone
     */
    @Override
    public ExprNode clone()
    {
        ExprNode clone = super.clone();

        // Copy the value
        if ( value != null )
        {
            ( ( ExtensibleNode ) clone ).value = value.clone();
        }

        return clone;
    }


    /**
     * Gets the Dn attributes.
     * 
     * @return the dn attributes
     */
    public boolean hasDnAttributes()
    {
        return dnAttributes;
    }


    /**
     * Set the dnAttributes flag
     *
     * @param dnAttributes The flag to set
     */
    public void setDnAttributes( boolean dnAttributes )
    {
        this.dnAttributes = dnAttributes;
    }


    /**
     * Gets the matching rule id as an OID string.
     * 
     * @return the OID
     */
    public String getMatchingRuleId()
    {
        return matchingRuleId;
    }


    /**
     * Sets the matching rule id as an OID string.
     * 
     * @param matchingRuleId The maching rule ID
     */
    public void setMatchingRuleId( String matchingRuleId )
    {
        this.matchingRuleId = matchingRuleId;
    }


    /**
     * Gets the value.
     * 
     * @return the value
     */
    public final Value getValue()
    {
        return value;
    }


    /** 
     * @return representation of value, escaped for use in a filter if required 
     */
    public String getEscapedValue()
    {
        if ( value.isHumanReadable() )
        {
            return escapeFilterValue( value.getValue() );
        }
        else
        {
            return escapeFilterValue( value.getAttributeType(), value.getBytes() );
        }
    }


    /**
     * Sets the value.
     * 
     * @param value the value
     */
    public final void setValue( Value value )
    {
        this.value = value;
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

        if ( !( obj instanceof ExtensibleNode ) )
        {
            return false;
        }
        ExtensibleNode that = ( ExtensibleNode ) obj;

        if ( dnAttributes != that.dnAttributes )
        {
            return false;
        }
        if ( !matchingRuleId.equals( that.matchingRuleId ) )
        {
            return false;
        }
        if ( !value.equals( that.value ) )
        {
            return false;
        }

        return super.equals( obj );
    }


    /**
     * @see Object#hashCode()
     * @return the instance's hash code 
     */
    @Override
    public int hashCode()
    {
        int h = 37;

        h = h * 17 + super.hashCode();
        h = h * 17 + ( dnAttributes ? 1 : 0 );
        h = h * 17 + matchingRuleId.hashCode();
        h = h * 17 + value.hashCode();

        return h;
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

        buf.append( "-" );
        buf.append( dnAttributes );
        buf.append( "-EXTENSIBLE-" );
        buf.append( matchingRuleId );
        buf.append( "-" );
        buf.append( value );

        buf.append( super.toString() );

        buf.append( ')' );

        return buf.toString();
    }
}
