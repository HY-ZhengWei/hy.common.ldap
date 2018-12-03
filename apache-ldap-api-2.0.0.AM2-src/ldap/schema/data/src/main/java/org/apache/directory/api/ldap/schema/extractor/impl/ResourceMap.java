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
package org.apache.directory.api.ldap.schema.extractor.impl;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.directory.api.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Lists LDIF resources available from the classpath.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ResourceMap
{
    /** the system property which can be used to load schema from a user specified
     *  resource like a absolute path to a directory or jar file.
     *  This is useful to start embedded DirectoryService in a servlet container environment
     *
     *  usage: -Dschema.resource.location=/tmp/schema
     *                OR
     *         -Dschema.resource.location=/tmp/api-ldap-schema-1.0.0-M13.jar
     *  */
    private static final String SCHEMA_RESOURCE_LOCATION = "schema.resource.location";

    /** The logger. */
    private static final Logger LOG = LoggerFactory.getLogger( ResourceMap.class );


    /**
     * Private constructor.
     */
    private ResourceMap()
    {
    }


    /**
     * For all elements of java.class.path OR from the resource name set in the
     * system property 'schema.resource.location' get a Map of resources
     * Pattern pattern = Pattern.compile(".*").
     * The keys represent resource names and the boolean parameter indicates
     * whether or not the resource is in a Jar file.
     *
     * @param pattern the pattern to match
     * @return the resources with markers - true if resource is in Jar
     */
    public static Map<String, Boolean> getResources( Pattern pattern )
    {
        HashMap<String, Boolean> retval = new HashMap<>();

        String schemaResourceLoc = System.getProperty( SCHEMA_RESOURCE_LOCATION, "" );

        if ( schemaResourceLoc.trim().length() > 0 )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_16008_LOADING_FROM_USER_SCHEMA, schemaResourceLoc ) );
            }

            File file = new File( schemaResourceLoc );

            if ( file.exists() )
            {
                getResources( retval, schemaResourceLoc, pattern );
            }
            else
            {
                LOG.error( I18n.err( I18n.ERR_16043_CANOT_LOAD_SCHEMA, schemaResourceLoc ) );
            }
        }
        else
        {
            getResourcesFromClassloader( retval, pattern );
        }

        return retval;
    }


    private static void getResources( HashMap<String, Boolean> map,
        String element, Pattern pattern )
    {
        File file = new File( element );

        if ( !file.exists() )
        {
            // this may happen if the class path contains an element that doesn't exist
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_16009_ELEMENT_DOES_NOT_EXIST, element ) );
            }

            return;
        }

        if ( file.isDirectory() )
        {
            getResourcesFromDirectory( map, file, pattern );
        }
        else
        {
            getResourcesFromJarFile( map, file, pattern );
        }
    }


    private static void getResourcesFromJarFile( HashMap<String, Boolean> map,
        File file, Pattern pattern )
    {
        ZipFile zf;

        try
        {
            zf = new ZipFile( file );
        }
        catch ( IOException e )
        {
            throw new Error( e );
        }

        Enumeration<? extends ZipEntry> e = zf.entries();

        while ( e.hasMoreElements() )
        {
            ZipEntry ze = e.nextElement();
            String fileName = ze.getName();
            boolean accept = pattern.matcher( fileName ).matches();

            if ( accept )
            {
                map.put( fileName, Boolean.TRUE );
            }
        }
        try
        {
            zf.close();
        }
        catch ( IOException e1 )
        {
            throw new Error( e1 );
        }
    }


    private static void getResourcesFromDirectory(
        HashMap<String, Boolean> map, File directory, Pattern pattern )
    {
        File[] fileList = directory.listFiles();

        if ( fileList != null )
        {
            for ( File file : fileList )
            {
                if ( file.isDirectory() )
                {
                    getResourcesFromDirectory( map, file, pattern );
                }
                else
                {
                    try
                    {
                        String fileName = file.getCanonicalPath();
                        boolean accept = pattern.matcher( fileName ).matches();
    
                        if ( accept )
                        {
                            map.put( fileName, Boolean.FALSE );
                        }
                    }
                    catch ( IOException e )
                    {
                        LOG.error( I18n.err( I18n.ERR_16044_CANNOT_LOAD_FILE, file.getAbsolutePath(), e.getMessage() ) );
    
                        // Continue...
                    }
                }
            }
        }
    }


    private static void getResourcesFromClassloader( HashMap<String, Boolean> map, Pattern pattern )
    {
        try
        {
            ClassLoader cl = ResourceMap.class.getClassLoader();
            Enumeration<URL> indexes = cl.getResources( "META-INF/apacheds-schema.index" );

            while ( indexes.hasMoreElements() )
            {
                URL index = null;

                try
                {
                    index = indexes.nextElement();
                    InputStream in = index.openStream();
                    BufferedReader reader = new BufferedReader( new InputStreamReader( in, StandardCharsets.UTF_8 ) );
                    String line = reader.readLine();

                    while ( line != null )
                    {
                        boolean accept = pattern.matcher( line ).matches();

                        if ( accept )
                        {
                            map.put( line, Boolean.TRUE );
                        }

                        line = reader.readLine();
                    }

                    reader.close();
                }
                catch ( IOException ioe )
                {
                    LOG.error( I18n.err( I18n.ERR_16047_CANNOT_LOAD_RESOURCE, index, ioe.getMessage() ) );
                    // Continue...
                }
            }
        }
        catch ( IOException e )
        {
            LOG.error( I18n.err( I18n.ERR_16045_ERROR_LOADING_RESOURCE, e.getMessage() ) );
            throw new Error( e );
        }
    }
}
