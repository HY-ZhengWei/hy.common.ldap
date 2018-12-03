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
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.ldap.model.schema.SchemaObjectType;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SyntaxChecker registry component's service interface.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DefaultSyntaxCheckerRegistry extends DefaultSchemaObjectRegistry<SyntaxChecker>
    implements SyntaxCheckerRegistry
{
    /** static class logger */
    private static final Logger LOG = LoggerFactory.getLogger( DefaultSyntaxCheckerRegistry.class );


    /**
     * Creates a new default SyntaxCheckerRegistry instance.
     */
    public DefaultSyntaxCheckerRegistry()
    {
        super( SchemaObjectType.SYNTAX_CHECKER, new OidRegistry<SyntaxChecker>() );
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
        for ( SyntaxChecker syntaxChecker : this )
        {
            if ( schemaName.equalsIgnoreCase( syntaxChecker.getSchemaName() ) )
            {
                String oid = syntaxChecker.getOid();
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
    public DefaultSyntaxCheckerRegistry copy()
    {
        DefaultSyntaxCheckerRegistry copy = new DefaultSyntaxCheckerRegistry();

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

        for ( Map.Entry<String, SyntaxChecker> entry : byName.entrySet() )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " );
            }

            SyntaxChecker syntaxChecker = entry.getValue();

            String fqcn = syntaxChecker.getFqcn();
            int lastDotPos = fqcn.lastIndexOf( '.' );

            sb.append( '<' ).append( syntaxChecker.getOid() ).append( ", " );

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
