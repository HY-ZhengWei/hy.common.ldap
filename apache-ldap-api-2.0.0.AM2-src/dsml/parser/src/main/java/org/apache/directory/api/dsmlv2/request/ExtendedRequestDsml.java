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


import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.api.dsmlv2.ParserUtils;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;


/**
 * DSML Decorator for ExtendedRequest
 *
 * @param <Q> The extended request type
 * @param <P> The extended response type
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtendedRequestDsml<Q extends ExtendedRequest, P extends ExtendedResponse>
    extends AbstractResultResponseRequestDsml<Q, P>
    implements ExtendedRequest
{
    private byte[] requestValue;


    /**
     * Creates a new getDecoratedMessage() of ExtendedRequestDsml.
     *
     * @param codec The LDAP Service to use
     * @param ldapMessage the message to decorate
     */
    public ExtendedRequestDsml( LdapApiService codec, Q ldapMessage )
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

        // Request Name
        if ( getDecorated().getRequestName() != null )
        {
            element.addElement( "requestName" ).setText(
                getDecorated().getRequestName() );
        }

        // Request Value        
        Namespace xsdNamespace = new Namespace( "xsd", ParserUtils.XML_SCHEMA_URI );
        Namespace xsiNamespace = new Namespace( "xsi", ParserUtils.XML_SCHEMA_INSTANCE_URI );
        element.getDocument().getRootElement().add( xsdNamespace );
        element.getDocument().getRootElement().add( xsiNamespace );

        Element valueElement = element.addElement( "requestValue" ).addText(
            ParserUtils.base64Encode( getRequestValue() ) );
        valueElement.addAttribute( new QName( "type", xsiNamespace ),
            "xsd:" + ParserUtils.BASE64BINARY );

        return element;
    }


    /**
     * Get the extended request name
     * 
     * @return Returns the request name.
     */
    @Override
    public String getRequestName()
    {
        return getDecorated().getRequestName();
    }


    /**
     * Set the extended request name
     * 
     * @param requestName The request name to set.
     */
    public void setRequestName( Oid requestName )
    {
        getDecorated().setRequestName( requestName.toString() );
    }


    /**
     * Get the extended request value
     * 
     * @return Returns the request value.
     */
    public byte[] getRequestValue()
    {
        return this.requestValue;
    }


    /**
     * Set the extended request value
     * 
     * @param requestValue The request value to set.
     */
    public void setRequestValue( byte[] requestValue )
    {
        this.requestValue = requestValue;
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
    public ExtendedRequest setRequestName( String oid )
    {
        getDecorated().setRequestName( oid );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedRequest setMessageId( int messageId )
    {
        super.setMessageId( messageId );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedRequest addControl( Control control )
    {
        return ( ExtendedRequest ) super.addControl( control );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedRequest addAllControls( Control[] controls )
    {
        return ( ExtendedRequest ) super.addAllControls( controls );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedRequest removeControl( Control control )
    {
        return ( ExtendedRequest ) super.removeControl( control );
    }
}
