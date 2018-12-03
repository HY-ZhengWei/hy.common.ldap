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
package org.apache.directory.api.ldap.model.schema.registries;


import java.util.Iterator;
import java.util.List;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapUnwillingToPerformException;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObjectType;


/**
 * An immutable wrapper of the ObjectClass registry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImmutableObjectClassRegistry implements ObjectClassRegistry, Cloneable
{
    /** The wrapped ObjectClass registry */
    private ObjectClassRegistry immutableObjectClassRegistry;


    /**
     * Creates a new instance of ImmutableAttributeTypeRegistry.
     *
     * @param ocRegistry The wrapped Attrib uteType registry
     */
    public ImmutableObjectClassRegistry( ObjectClassRegistry ocRegistry )
    {
        immutableObjectClassRegistry = ocRegistry;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDescendants( String ancestorId ) throws LdapException
    {
        return immutableObjectClassRegistry.hasDescendants( ancestorId );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<ObjectClass> descendants( String ancestorId ) throws LdapException
    {
        return immutableObjectClassRegistry.descendants( ancestorId );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDescendants( ObjectClass objectClass, List<ObjectClass> ancestors ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13710_CANNOT_MODIFY_OC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterDescendants( ObjectClass attributeType, List<ObjectClass> ancestors ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13710_CANNOT_MODIFY_OC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void register( ObjectClass objectClass ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13710_CANNOT_MODIFY_OC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectClass unregister( String numericOid ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13710_CANNOT_MODIFY_OC_REGISTRY_COPY ) );
    }


    /**
     * Clone the ObjectClassRegistry
     */
    @Override
    public ImmutableObjectClassRegistry copy()
    {
        return ( ImmutableObjectClassRegistry ) immutableObjectClassRegistry.copy();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return immutableObjectClassRegistry.size();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains( String oid )
    {
        return immutableObjectClassRegistry.contains( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getOidByName( String name ) throws LdapException
    {
        return immutableObjectClassRegistry.getOidByName( name );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchemaName( String oid ) throws LdapException
    {
        return immutableObjectClassRegistry.getSchemaName( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SchemaObjectType getType()
    {
        return immutableObjectClassRegistry.getType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<ObjectClass> iterator()
    {
        return immutableObjectClassRegistry.iterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectClass lookup( String oid ) throws LdapException
    {
        return immutableObjectClassRegistry.lookup( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> oidsIterator()
    {
        return immutableObjectClassRegistry.oidsIterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void renameSchema( String originalSchemaName, String newSchemaName ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13710_CANNOT_MODIFY_OC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterSchemaElements( String schemaName ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13710_CANNOT_MODIFY_OC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectClass get( String oid )
    {
        return immutableObjectClassRegistry.get( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13710_CANNOT_MODIFY_OC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectClass unregister( ObjectClass schemaObject ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13710_CANNOT_MODIFY_OC_REGISTRY_COPY ) );
    }
}
