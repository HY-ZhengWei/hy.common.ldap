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


import java.util.Map;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.LdapComparator;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.ldap.model.schema.SchemaObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Comparator registry service default implementation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DefaultComparatorRegistry extends DefaultSchemaObjectRegistry<LdapComparator<?>>
    implements ComparatorRegistry
{
    /** static class logger */
    private static final Logger LOG = LoggerFactory.getLogger( DefaultComparatorRegistry.class );

    /**
     * Creates a new default ComparatorRegistry instance.
     */
    public DefaultComparatorRegistry()
    {
        super( SchemaObjectType.COMPARATOR, new OidRegistry<LdapComparator<?>>() );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterSchemaElements( String schemaName ) throws LdapException
    {
        if ( schemaName == null )
        {
            return;
        }

        // Loop on all the SchemaObjects stored and remove those associated
        // with the give schemaName
        for ( LdapComparator<?> comparator : this )
        {
            if ( schemaName.equalsIgnoreCase( comparator.getSchemaName() ) )
            {
                String oid = comparator.getOid();
                SchemaObject removed = unregister( oid );

                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13702_REMOVED_FROM_REGISTRY, removed, oid ) );
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultComparatorRegistry copy()
    {
        DefaultComparatorRegistry copy = new DefaultComparatorRegistry();

        // Copy the base data
        copy.copy( this );

        return copy;
    }


    /**
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( schemaObjectType ).append( ": " );
        boolean isFirst = true;

        for ( Map.Entry<String, LdapComparator<?>> entry : byName.entrySet() )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " );
            }

            LdapComparator<?> comparator = entry.getValue();

            String fqcn = comparator.getFqcn();
            int lastDotPos = fqcn.lastIndexOf( '.' );

            sb.append( '<' ).append( comparator.getOid() ).append( ", " );

            if ( lastDotPos > 0 )
            {
                sb.append( fqcn.substring( lastDotPos + 1 ) );
            }
            else
            {
                sb.append( fqcn );
            }

            sb.append( '>' );
        }

        return sb.toString();
    }
}
