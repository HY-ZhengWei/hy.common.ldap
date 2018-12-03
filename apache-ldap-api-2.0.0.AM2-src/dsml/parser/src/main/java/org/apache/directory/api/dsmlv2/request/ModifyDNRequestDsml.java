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
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.ModifyDnRequest;
import org.apache.directory.api.ldap.model.message.ModifyDnRequestImpl;
import org.apache.directory.api.ldap.model.message.ModifyDnResponse;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.dom4j.Element;


/**
 * DSML Decorator for ModifyDNRequest
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModifyDNRequestDsml
    extends AbstractResultResponseRequestDsml<ModifyDnRequest, ModifyDnResponse>
    implements ModifyDnRequest
{
    /**
     * Creates a new getDecoratedMessage() of ModifyDNRequestDsml.
     * 
     * @param codec The LDAP Service to use
     */
    public ModifyDNRequestDsml( LdapApiService codec )
    {
        super( codec, new ModifyDnRequestImpl() );
    }


    /**
     * Creates a new getDecoratedMessage() of ModifyDNRequestDsml.
     *
     * @param codec The LDAP Service to use
     * @param ldapMessage the message to decorate
     */
    public ModifyDNRequestDsml( LdapApiService codec, ModifyDnRequest ldapMessage )
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

        ModifyDnRequest request = getDecorated();

        // Dn
        if ( request.getName() != null )
        {
            element.addAttribute( "dn", request.getName().getName() );
        }

        // NewRDN
        if ( request.getNewRdn() != null )
        {
            element.addAttribute( "newrdn", request.getNewRdn().getName() );
        }

        // DeleteOldRDN
        element.addAttribute( "deleteoldrdn", request.getDeleteOldRdn() ? "true" : "false" );

        // NewSuperior
        if ( request.getNewRdn() != null )
        {
            element.addAttribute( "newSuperior", request.getNewSuperior().getName() );
        }

        return element;
    }


    /**
     * Get the modification's Dn
     * 
     * @return Returns the name.
     */
    @Override
    public Dn getName()
    {
        return getDecorated().getName();
    }


    /**
     * Set the modification Dn.
     * 
     * @param name The name to set.
     */
    public void setEntry( Dn name )
    {
        getDecorated().setName( name );
    }


    /**
     * Tells if the old Rdn is to be deleted
     * 
     * @return Returns the deleteOldRDN.
     */
    public boolean isDeleteOldRDN()
    {
        return getDecorated().getDeleteOldRdn();
    }


    /**
     * Set the flag to delete the old Rdn
     * 
     * @param deleteOldRDN The deleteOldRDN to set.
     */
    public void setDeleteOldRDN( boolean deleteOldRDN )
    {
        getDecorated().setDeleteOldRdn( deleteOldRDN );
    }


    /**
     * Get the new Rdn
     * 
     * @return Returns the newRDN.
     */
    public Rdn getNewRDN()
    {
        return getDecorated().getNewRdn();
    }


    /**
     * Set the new Rdn
     * 
     * @param newRdn The newRdn to set.
     */
    public void setNewRDN( Rdn newRdn )
    {
        getDecorated().setNewRdn( newRdn );
    }


    /**
     * Get the newSuperior
     * 
     * @return Returns the newSuperior.
     */
    @Override
    public Dn getNewSuperior()
    {
        return getDecorated().getNewSuperior();
    }


    /**
     * Set the new superior
     * 
     * @param newSuperior The newSuperior to set.
     */
    @Override
    public ModifyDnRequest setNewSuperior( Dn newSuperior )
    {
        getDecorated().setNewSuperior( newSuperior );

        return this;
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
    public ModifyDnRequest setName( Dn name )
    {
        getDecorated().setName( name );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rdn getNewRdn()
    {
        return getDecorated().getNewRdn();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ModifyDnRequest setNewRdn( Rdn newRdn )
    {
        getDecorated().setNewRdn( newRdn );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDeleteOldRdn()
    {
        return getDecorated().getDeleteOldRdn();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ModifyDnRequest setDeleteOldRdn( boolean deleteOldRdn )
    {
        getDecorated().setDeleteOldRdn( deleteOldRdn );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMove()
    {
        return getDecorated().isMove();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ModifyDnRequest setMessageId( int messageId )
    {
        super.setMessageId( messageId );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ModifyDnRequest addControl( Control control )
    {
        return ( ModifyDnRequest ) super.addControl( control );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ModifyDnRequest addAllControls( Control[] controls )
    {
        return ( ModifyDnRequest ) super.addAllControls( controls );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ModifyDnRequest removeControl( Control control )
    {
        return ( ModifyDnRequest ) super.removeControl( control );
    }
}
