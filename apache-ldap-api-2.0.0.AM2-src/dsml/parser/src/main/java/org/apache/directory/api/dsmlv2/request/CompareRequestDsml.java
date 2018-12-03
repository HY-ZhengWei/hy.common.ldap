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
package org.apache.directory.api.dsmlv2.request;


import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.message.CompareRequest;
import org.apache.directory.api.ldap.model.message.CompareRequestImpl;
import org.apache.directory.api.ldap.model.message.CompareResponse;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.dom4j.Element;


/**
 * DSML Decorator for CompareRequest
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CompareRequestDsml
    extends AbstractResultResponseRequestDsml<CompareRequest, CompareResponse>
    implements CompareRequest
{
    /**
     * Creates a new getDecoratedMessage() of CompareRequestDsml.
     * 
     * @param codec The LDAP Service to use
     */
    public CompareRequestDsml( LdapApiService codec )
    {
        super( codec, new CompareRequestImpl() );
    }


    /**
     * Creates a new getDecoratedMessage() of CompareRequestDsml.
     *
     * @param codec The LDAP Service to use
     * @param ldapMessage the message to decorate
     */
    public CompareRequestDsml( LdapApiService codec, CompareRequest ldapMessage )
    {
        super( codec, ldapMessage );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MessageTypeEnum getType()
    {
        return getDecorated().getType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Element toDsml( Element root )
    {
        Element element = super.toDsml( root );

        CompareRequest request = getDecorated();

        // Dn
        if ( request.getName() != null )
        {
            element.addAttribute( "dn", request.getName().getName() );
        }

        // Assertion
        Element assertionElement = element.addElement( "assertion" );
        if ( request.getAttributeId() != null )
        {
            assertionElement.addAttribute( "name", request.getAttributeId() );
        }
        if ( request.getAssertionValue() != null )
        {
            assertionElement.addElement( "value" ).setText( request.getAssertionValue().getValue() );
        }

        return element;
    }


    /**
     * Get the entry to be compared
     * 
     * @return Returns the entry.
     */
    @Override
    public Dn getName()
    {
        return getDecorated().getName();
    }


    /**
     * Set the entry to be compared
     * 
     * @param entry The entry to set.
     */
    @Override
    public CompareRequest setName( Dn entry )
    {
        getDecorated().setName( entry );

        return this;
    }


    /**
     * Set the assertion value
     * 
     * @param assertionValue The assertionValue to set.
     */
    public void setAssertionValue( Object assertionValue )
    {
        if ( assertionValue instanceof String )
        {
            getDecorated().setAssertionValue( ( String ) assertionValue );
        }
        else
        {
            getDecorated().setAssertionValue( ( byte[] ) assertionValue );
        }
    }


    /**
     * Get the attribute description
     * 
     * @return Returns the attributeDesc.
     */
    public String getAttributeDesc()
    {
        return getDecorated().getAttributeId();
    }


    /**
     * Set the attribute description
     * 
     * @param attributeDesc The attributeDesc to set.
     */
    public void setAttributeDesc( String attributeDesc )
    {
        getDecorated().setAttributeId( attributeDesc );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MessageTypeEnum getResponseType()
    {
        return getDecorated().getResponseType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CompareRequest setAssertionValue( String value )
    {
        getDecorated().setAssertionValue( value );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CompareRequest setAssertionValue( byte[] value )
    {
        getDecorated().setAssertionValue( value );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttributeId()
    {
        return getDecorated().getAttributeId();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CompareRequest setAttributeId( String attrId )
    {
        getDecorated().setAttributeId( attrId );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Value getAssertionValue()
    {
        return getDecorated().getAssertionValue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CompareRequest setMessageId( int messageId )
    {
        super.setMessageId( messageId );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CompareRequest addControl( Control control )
    {
        return ( CompareRequest ) super.addControl( control );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CompareRequest addAllControls( Control[] controls )
    {
        return ( CompareRequest ) super.addAllControls( controls );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CompareRequest removeControl( Control control )
    {
        return ( CompareRequest ) super.removeControl( control );
    }
}
