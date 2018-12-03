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


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapNoSuchAttributeException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.SchemaObjectType;
import org.apache.directory.api.ldap.model.schema.normalizers.NoOpNormalizer;
import org.apache.directory.api.ldap.model.schema.normalizers.OidNormalizer;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An AttributeType registry service default implementation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DefaultAttributeTypeRegistry extends DefaultSchemaObjectRegistry<AttributeType> implements
    AttributeTypeRegistry
{
    /** static class logger */
    private static final Logger LOG = LoggerFactory.getLogger( DefaultAttributeTypeRegistry.class );

    /** cached Oid/normalizer mapping */
    private Map<String, OidNormalizer> oidNormalizerMap;

    /** maps OIDs to a Set of descendants for that OID */
    private Map<String, Set<AttributeType>> oidToDescendantSet;


    /**
     * Creates a new default AttributeTypeRegistry instance.
     */
    public DefaultAttributeTypeRegistry()
    {
        super( SchemaObjectType.ATTRIBUTE_TYPE, new OidRegistry<AttributeType>() );
        oidNormalizerMap = new HashMap<>();
        oidToDescendantSet = new HashMap<>();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, OidNormalizer> getNormalizerMapping()
    {
        return Collections.unmodifiableMap( oidNormalizerMap );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDescendants( String ancestorId ) throws LdapException
    {
        try
        {
            String oid = getOidByName( ancestorId );
            Set<AttributeType> descendants = oidToDescendantSet.get( oid );
            return ( descendants != null ) && !descendants.isEmpty();
        }
        catch ( LdapException ne )
        {
            throw new LdapNoSuchAttributeException( ne.getMessage(), ne );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDescendants( AttributeType ancestor ) throws LdapException
    {
        String oid = ancestor.getOid();
        Set<AttributeType> descendants = oidToDescendantSet.get( oid );
        return ( descendants != null ) && !descendants.isEmpty();
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<AttributeType> descendants( String ancestorId ) throws LdapException
    {
        try
        {
            String oid = getOidByName( ancestorId );
            Set<AttributeType> descendants = oidToDescendantSet.get( oid );

            if ( descendants == null )
            {
                return Collections.EMPTY_SET.iterator();
            }

            return descendants.iterator();
        }
        catch ( LdapException ne )
        {
            throw new LdapNoSuchAttributeException( ne.getMessage(), ne );
        }
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<AttributeType> descendants( AttributeType ancestor ) throws LdapException
    {
        String oid = ancestor.getOid();
        Set<AttributeType> descendants = oidToDescendantSet.get( oid );

        if ( descendants == null )
        {
            return Collections.EMPTY_SET.iterator();
        }

        return descendants.iterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDescendants( AttributeType attributeType, AttributeType ancestor ) throws LdapException
    {
        // add this attribute to descendant list of other attributes in superior chain
        if ( ancestor == null )
        {
            return;
        }

        // Get the ancestor's descendant, if any
        Set<AttributeType> descendants = oidToDescendantSet.get( ancestor.getOid() );

        // Initialize the descendant Set to store the descendants for the attributeType
        if ( descendants == null )
        {
            descendants = new HashSet<>( 1 );
            oidToDescendantSet.put( ancestor.getOid(), descendants );
        }

        // Add the current type as a descendant
        descendants.add( attributeType );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterDescendants( AttributeType attributeType, AttributeType ancestor ) throws LdapException
    {
        // add this attribute to descendant list of other attributes in superior chain
        if ( ancestor == null )
        {
            return;
        }

        // Get the ancestor's descendant, if any
        Set<AttributeType> descendants = oidToDescendantSet.get( ancestor.getOid() );

        if ( descendants != null )
        {
            descendants.remove( attributeType );

            if ( descendants.isEmpty() )
            {
                oidToDescendantSet.remove( ancestor.getOid() );
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeType unregister( String numericOid ) throws LdapException
    {
        try
        {
            AttributeType removed = super.unregister( numericOid );

            removeMappingFor( removed );

            // Deleting an AT which might be used as a superior means we have
            // to recursively update the descendant map. We also have to remove
            // the at.oid -> descendant relation
            oidToDescendantSet.remove( numericOid );

            // Now recurse if needed
            unregisterDescendants( removed, removed.getSuperior() );

            return removed;
        }
        catch ( LdapException ne )
        {
            throw new LdapNoSuchAttributeException( ne.getMessage(), ne );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addMappingFor( AttributeType attributeType ) throws LdapException
    {
        MatchingRule equality = attributeType.getEquality();
        OidNormalizer oidNormalizer;
        String oid = attributeType.getOid();

        if ( equality == null )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13703_AT_WITHOUT_EQ_MR, attributeType.getName() ) );
            }
            
            oidNormalizer = new OidNormalizer( oid, new NoOpNormalizer( attributeType.getOid() ) );
        }
        else
        {
            oidNormalizer = new OidNormalizer( oid, equality.getNormalizer() );
        }

        oidNormalizerMap.put( oid, oidNormalizer );

        // Also inject the attributeType's short names in the map
        for ( String name : attributeType.getNames() )
        {
            oidNormalizerMap.put( Strings.toLowerCaseAscii( name ), oidNormalizer );
        }
    }


    /**
     * Remove the AttributeType normalizer from the OidNormalizer map 
     */
    @Override
    public void removeMappingFor( AttributeType attributeType ) throws LdapException
    {
        if ( attributeType == null )
        {
            return;
        }

        oidNormalizerMap.remove( attributeType.getOid() );

        // We also have to remove all the short names for this attribute
        for ( String name : attributeType.getNames() )
        {
            oidNormalizerMap.remove( Strings.toLowerCaseAscii( name ) );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeType lookup( String oid ) throws LdapException
    {
        try
        {
            return super.lookup( oid );
        }
        catch ( LdapException ne )
        {
            throw new LdapNoSuchAttributeException( ne.getMessage(), ne );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultAttributeTypeRegistry copy()
    {
        DefaultAttributeTypeRegistry copy = new DefaultAttributeTypeRegistry();

        // Copy the base data
        copy.copy( this );

        return copy;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        // First clear the shared elements
        super.clear();

        // clear the OidNormalizer map
        oidNormalizerMap.clear();

        // and clear the descendant
        for ( Map.Entry<String, Set<AttributeType>> entry : oidToDescendantSet.entrySet() )
        {
            Set<AttributeType> descendants = entry.getValue();

            if ( descendants != null )
            {
                descendants.clear();
            }
        }

        oidToDescendantSet.clear();
    }
}
