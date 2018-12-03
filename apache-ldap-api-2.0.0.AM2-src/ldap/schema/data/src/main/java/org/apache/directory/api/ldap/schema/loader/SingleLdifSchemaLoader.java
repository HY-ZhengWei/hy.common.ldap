/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.api.ldap.schema.loader;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.schema.registries.AbstractSchemaLoader;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A schema loader based on a single monolithic ldif file containing all the schema partition elements
 * 
 * Performs better than any other existing LDIF schema loaders. NOT DOCUMENTED atm
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingleLdifSchemaLoader extends AbstractSchemaLoader
{
    /** 
     * Pattern for start of schema Dn.
     * java.util.regex.Pattern is immutable so only one instance is needed for all uses.
     */
    private static final Pattern SCHEMA_START_PATTERN = Pattern
        .compile( "cn\\s*=\\s*[a-z0-9-_]*\\s*,\\s*ou\\s*=\\s*schema" );

    /** The logger. */
    private static final Logger LOG = LoggerFactory.getLogger( SingleLdifSchemaLoader.class );

    /** The schema object Rdn attribute types. */
    private String[] schemaObjectTypeRdns = new String[]
        { "attributetypes", "comparators", "ditContentRules", "ditStructureRules", "matchingRules", "matchingRuleUse",
            "nameForms", "normalizers", "objectClasses", "syntaxes", "syntaxCheckers" };

    /** The map containing ... */
    private Map<String, Map<String, List<Entry>>> scObjEntryMap = new HashMap<>();


    /**
     * Instantiates a new single LDIF schema loader.
     */
    public SingleLdifSchemaLoader()
    {
        try
        {
            URL resource = getClass().getClassLoader().getResource( "schema-all.ldif" );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_16012_URL_SCHEMA_ALL_LDIF, resource ) );
            }

            for ( String s : schemaObjectTypeRdns )
            {
                scObjEntryMap.put( s, new HashMap<String, List<Entry>>() );
            }

            InputStream in = resource.openStream();

            initializeSchemas( in );
        }
        catch ( LdapException | IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    
    /**
     * Instantiates a new single LDIF schema loader.
     * 
     * @param schemaFile The Schema to load
     */
    public SingleLdifSchemaLoader( String schemaFile )
    {
        try
        {
            for ( String s : schemaObjectTypeRdns )
            {
                scObjEntryMap.put( s, new HashMap<String, List<Entry>>() );
            }

            InputStream in = Files.newInputStream( Paths.get( schemaFile ) );

            initializeSchemas( in );
        }
        catch ( LdapException | IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    
    /**
     * Instantiates a new single LDIF schema loader.
     * 
     * @param schemaUrl The URL of the schema to load
     */
    public SingleLdifSchemaLoader( URL schemaUrl )
    {
        try
        {
            for ( String s : schemaObjectTypeRdns )
            {
                scObjEntryMap.put( s, new HashMap<String, List<Entry>>() );
            }

            InputStream in = schemaUrl.openStream();

            initializeSchemas( in );
        }
        catch ( LdapException | IOException e )
        {
            throw new RuntimeException( e );
        }
    }


    /**
     * Initialize the Schema object from a Single LDIF file
     * 
     * @param in The input stream to process
     * @throws LdapException If the schemas can't be initialized
     * @throws IOException If we had an issue processing the InputStream
     */
    private void initializeSchemas( InputStream in ) throws LdapException, IOException
    {
        try ( LdifReader ldifReader = new LdifReader( in ) )
        {
            Schema currentSchema = null;
    
            while ( ldifReader.hasNext() )
            {
                LdifEntry ldifEntry = ldifReader.next();
                String dn = ldifEntry.getDn().getName();
                
                if ( SCHEMA_START_PATTERN.matcher( dn ).matches() )
                {
                    Schema schema = getSchema( ldifEntry.getEntry() );
                    schemaMap.put( schema.getSchemaName(), schema );
                    currentSchema = schema;
                }
                else
                {
                    if ( currentSchema == null )
                    {
                        throw new LdapException( I18n.err( I18n.ERR_16076_NOT_A_SCHEMA_DEFINITION ) );
                    }
                    
                    loadSchemaObject( currentSchema.getSchemaName(), ldifEntry );
                }
            }
        }
    }


    /**
     * Load all the schemaObjects
     * 
     * @param schemaName The schema name
     * @param ldifEntry The entry to load
     */
    private void loadSchemaObject( String schemaName, LdifEntry ldifEntry )
    {
        for ( String scObjTypeRdn : schemaObjectTypeRdns )
        {
            Pattern regex = Pattern.compile( "m-oid\\s*=\\s*[0-9\\.]*\\s*" + ",\\s*ou\\s*=\\s*" + scObjTypeRdn
                + "\\s*,\\s*cn\\s*=\\s*" + schemaName
                + "\\s*,\\s*ou=schema\\s*", Pattern.CASE_INSENSITIVE );

            String dn = ldifEntry.getDn().getName();

            if ( regex.matcher( dn ).matches() )
            {
                Map<String, List<Entry>> m = scObjEntryMap.get( scObjTypeRdn );
                List<Entry> entryList = m.get( schemaName );
                
                if ( entryList == null )
                {
                    entryList = new ArrayList<>();
                    entryList.add( ldifEntry.getEntry() );
                    m.put( schemaName, entryList );
                }
                else
                {
                    entryList.add( ldifEntry.getEntry() );
                }

                break;
            }
        }
    }


    private List<Entry> loadSchemaObjects( String schemaObjectType, Schema... schemas )
    {
        Map<String, List<Entry>> m = scObjEntryMap.get( schemaObjectType );
        List<Entry> atList = new ArrayList<>();

        for ( Schema s : schemas )
        {
            List<Entry> preLoaded = m.get( s.getSchemaName() );
            
            if ( preLoaded != null )
            {
                atList.addAll( preLoaded );
            }
        }

        return atList;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadAttributeTypes( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "attributetypes", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadComparators( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "comparators", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadDitContentRules( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "ditContentRules", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadDitStructureRules( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "ditStructureRules", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadMatchingRules( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "matchingRules", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadMatchingRuleUses( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "matchingRuleUse", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadNameForms( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "nameForms", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadNormalizers( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "normalizers", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadObjectClasses( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "objectClasses", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadSyntaxes( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "syntaxes", schemas );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadSyntaxCheckers( Schema... schemas ) throws LdapException, IOException
    {
        return loadSchemaObjects( "syntaxCheckers", schemas );
    }

}

class SchemaMarker
{
    /** The start marker. */
    private int start;

    /** The end marker. */
    private int end;


    SchemaMarker( int start )
    {
        this.start = start;
    }


    public void setEnd( int end )
    {
        this.end = end;
    }


    public int getStart()
    {
        return start;
    }


    public int getEnd()
    {
        return end;
    }
}
