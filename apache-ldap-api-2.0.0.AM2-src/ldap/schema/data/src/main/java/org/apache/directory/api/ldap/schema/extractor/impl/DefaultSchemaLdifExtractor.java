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
package org.apache.directory.api.ldap.schema.extractor.impl;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.schema.extractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.extractor.UniqueResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extracts LDIF files for the schema repository onto a destination directory.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DefaultSchemaLdifExtractor implements SchemaLdifExtractor
{
    /** The base path. */
    private static final String BASE_PATH = "";

    /** The schema sub-directory. */
    private static final String SCHEMA_SUBDIR = "schema";

    /** The logger. */
    private static final Logger LOG = LoggerFactory.getLogger( DefaultSchemaLdifExtractor.class );

    /**
     * The pattern to extract the schema from LDIF files.
     * java.util.regex.Pattern is immutable so only one instance is needed for all uses.
     */
    private static final Pattern EXTRACT_PATTERN = Pattern.compile( ".*schema" + "[/\\Q\\\\E]" + "ou=schema.*\\.ldif" );

    /** The extracted flag. */
    private boolean extracted;

    /** The output directory. */
    private File outputDirectory;


    /**
     * Creates an extractor which deposits files into the specified output
     * directory.
     *
     * @param outputDirectory the directory where the schema root is extracted
     */
    public DefaultSchemaLdifExtractor( File outputDirectory )
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_16000_BASE_PATH, BASE_PATH, outputDirectory ) );
        }
        
        this.outputDirectory = outputDirectory;
        File schemaDirectory = new File( outputDirectory, SCHEMA_SUBDIR );

        if ( !outputDirectory.exists() )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_16001_CREATING_DIR, outputDirectory ) );
            }
            
            if ( !outputDirectory.mkdir() )
            {
                LOG.error( I18n.err( I18n.ERR_16042_OUTPUT_DIR_CREATION_FAIL, outputDirectory ) );
            }
        }
        else
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_16002_DIR_EXISTS ) );
            }
        }

        if ( !schemaDirectory.exists() )
        {
            if ( LOG.isInfoEnabled() )
            {
                LOG.info( I18n.msg( I18n.MSG_16004_SCHEMA_DIR_ABSENT, schemaDirectory ) );
            }
            
            extracted = false;
        }
        else
        {
            if ( LOG.isInfoEnabled() )
            {
                LOG.info( I18n.msg( I18n.MSG_16005_SCHEMA_DIR_PRESENT, schemaDirectory ) );
            }
            
            extracted = true;
        }
    }


    /**
     * Gets whether or not schema folder has been created or not.
     *
     * @return true if schema folder has already been extracted.
     */
    @Override
    public boolean isExtracted()
    {
        return extracted;
    }


    /**
     * Extracts the LDIF files from a Jar file or copies exploded LDIF resources.
     *
     * @param overwrite over write extracted structure if true, false otherwise
     * @throws IOException if schema already extracted and on IO errors
     */
    @Override
    public void extractOrCopy( boolean overwrite ) throws IOException
    {
        if ( !outputDirectory.exists() && !outputDirectory.mkdirs() )
        {
            throw new IOException( I18n.err( I18n.ERR_16006_DIRECTORY_CREATION_FAILED, outputDirectory
                .getAbsolutePath() ) );
        }

        File schemaDirectory = new File( outputDirectory, SCHEMA_SUBDIR );

        if ( !schemaDirectory.exists() )
        {
            if ( !schemaDirectory.mkdirs() )
            {
                throw new IOException( I18n.err( I18n.ERR_16006_DIRECTORY_CREATION_FAILED, schemaDirectory
                    .getAbsolutePath() ) );
            }
        }
        else if ( !overwrite )
        {
            throw new IOException( I18n.err( I18n.ERR_16000_CANNOT_OVEWRITE_SCHEMA, schemaDirectory.getAbsolutePath() ) );
        }

        Map<String, Boolean> list = ResourceMap.getResources( EXTRACT_PATTERN );

        for ( Entry<String, Boolean> entry : list.entrySet() )
        {
            if ( entry.getValue() )
            {
                extractFromClassLoader( entry.getKey() );
            }
            else
            {
                File resource = new File( entry.getKey() );
                copyFile( resource, getDestinationFile( resource ) );
            }
        }
    }


    /**
     * Extracts the LDIF files from a Jar file or copies exploded LDIF
     * resources without overwriting the resources if the schema has
     * already been extracted.
     *
     * @throws IOException if schema already extracted and on IO errors
     */
    @Override
    public void extractOrCopy() throws IOException
    {
        extractOrCopy( false );
    }


    /**
     * Copies a file line by line from the source file argument to the 
     * destination file argument.
     *
     * @param source the source file to copy
     * @param destination the destination to copy the source to
     * @throws IOException if there are IO errors or the source does not exist
     */
    private void copyFile( File source, File destination ) throws IOException
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_16003_COPYFILE, source, destination ) );
        }

        if ( !destination.getParentFile().exists() && !destination.getParentFile().mkdirs() )
        {
            throw new IOException( I18n.err( I18n.ERR_16006_DIRECTORY_CREATION_FAILED, destination.getParentFile()
                .getAbsolutePath() ) );
        }

        if ( !source.getParentFile().exists() )
        {
            throw new FileNotFoundException( I18n.err( I18n.ERR_16001_CANNOT_COPY_NON_EXISTENT, source.getAbsolutePath() ) );
        }

        try ( Writer out = new OutputStreamWriter( Files.newOutputStream( Paths.get( destination.getPath() ) ), 
            Charset.defaultCharset() );
            LdifReader ldifReader = new LdifReader( source ) )
        {
            boolean first = true;
            LdifEntry ldifEntry = null;

            while ( ldifReader.hasNext() )
            {
                if ( first )
                {
                    ldifEntry = ldifReader.next();

                    if ( ldifEntry.get( SchemaConstants.ENTRY_UUID_AT ) == null )
                    {
                        // No UUID, let's create one
                        UUID entryUuid = UUID.randomUUID();
                        ldifEntry.addAttribute( SchemaConstants.ENTRY_UUID_AT, entryUuid.toString() );
                    }

                    first = false;
                }
                else
                {
                    // throw an exception : we should not have more than one entry per schema ldif file
                    String msg = I18n.err( I18n.ERR_16002_MORE_THAN_ONE_ENTRY, source );
                    LOG.error( msg );
                    throw new InvalidObjectException( msg );
                }
            }

            // Add the version at the first line, to avoid a warning
            String ldifString;
            
            if ( ldifEntry != null )
            {
                ldifString = "version: 1\n" + ldifEntry.toString();
            }
            else
            {
                ldifString = "version: 1\n";
            }

            out.write( ldifString );
            out.flush();
        }
        catch ( LdapException le )
        {
            String msg = I18n.err( I18n.ERR_16003_ERROR_PARSING_LDIF, source, le.getLocalizedMessage() );
            LOG.error( msg );
            throw new InvalidObjectException( msg );
        }
    }


    /**
     * Assembles the destination file by appending file components previously
     * pushed on the fileComponentStack argument.
     *
     * @param fileComponentStack stack containing pushed file components
     * @return the assembled destination file
     */
    private File assembleDestinationFile( Deque<String> fileComponentStack )
    {
        File destinationFile = outputDirectory.getAbsoluteFile();

        while ( !fileComponentStack.isEmpty() )
        {
            destinationFile = new File( destinationFile, fileComponentStack.pop() );
        }

        return destinationFile;
    }


    /**
     * Calculates the destination file.
     *
     * @param resource the source file
     * @return the destination file's parent directory
     */
    private File getDestinationFile( File resource )
    {
        File parent = resource.getParentFile();
        Deque<String> fileComponentStack = new ArrayDeque<>();
        fileComponentStack.push( resource.getName() );

        while ( parent != null )
        {
            if ( "schema".equals( parent.getName() ) )
            {
                // All LDIF files besides the schema.ldif are under the 
                // schema/schema base path. So we need to add one more 
                // schema component to all LDIF files minus this schema.ldif
                fileComponentStack.push( "schema" );

                return assembleDestinationFile( fileComponentStack );
            }

            fileComponentStack.push( parent.getName() );

            if ( parent.equals( parent.getParentFile() ) || parent.getParentFile() == null )
            {
                throw new IllegalStateException( I18n.err( I18n.ERR_16004_ROOT_WITHOUT_SCHEMA ) );
            }

            parent = parent.getParentFile();
        }

        throw new IllegalStateException( I18n.err( I18n.ERR_16005_PARENT_NULL ) );
    }


    /**
     * Gets the unique schema file resource from the class loader off the base path.  If 
     * the same resource exists multiple times then an error will result since the resource
     * is not unique.
     *
     * @param resourceName the file name of the resource to load
     * @param resourceDescription human description of the resource
     * @return the InputStream to read the contents of the resource
     * @throws IOException if there are problems reading or finding a unique copy of the resource
     */
    public static InputStream getUniqueResourceAsStream( String resourceName, String resourceDescription )
        throws IOException
    {
        URL result = getUniqueResource( BASE_PATH + resourceName, resourceDescription );
        return result.openStream();
    }


    /**
     * Gets a unique resource from the class loader.
     * 
     * @param resourceName the name of the resource
     * @param resourceDescription the description of the resource
     * @return the URL to the resource in the class loader
     * @throws IOException if there is an IO error
     */
    public static URL getUniqueResource( String resourceName, String resourceDescription ) throws IOException
    {
        Enumeration<URL> resources = DefaultSchemaLdifExtractor.class.getClassLoader().getResources( resourceName );
        if ( !resources.hasMoreElements() )
        {
            throw new UniqueResourceException( resourceName, resourceDescription );
        }
        URL result = resources.nextElement();
        if ( resources.hasMoreElements() )
        {
            throw new UniqueResourceException( resourceName, result, resources, resourceDescription );
        }
        return result;
    }


    /**
     * Extracts the LDIF schema resource from class loader.
     *
     * @param resource the LDIF schema resource
     * @throws IOException if there are IO errors
     */
    private void extractFromClassLoader( String resource ) throws IOException
    {
        byte[] buf = new byte[512];
        InputStream in = DefaultSchemaLdifExtractor.getUniqueResourceAsStream( resource,
            "LDIF file in schema repository" );

        try
        {
            File destination = new File( outputDirectory, resource );

            /*
             * Do not overwrite an LDIF file if it has already been extracted.
             */
            if ( destination.exists() )
            {
                return;
            }

            if ( !destination.getParentFile().exists() && !destination.getParentFile().mkdirs() )
            {
                throw new IOException( I18n.err( I18n.ERR_16006_DIRECTORY_CREATION_FAILED, destination
                    .getParentFile().getAbsolutePath() ) );
            }

            OutputStream out = Files.newOutputStream( Paths.get( destination.getPath() ) );
            
            try
            {
                while ( in.available() > 0 )
                {
                    int readCount = in.read( buf );
                    out.write( buf, 0, readCount );
                }
                out.flush();
            }
            finally
            {
                out.close();
            }
        }
        finally
        {
            in.close();
        }
    }
}
