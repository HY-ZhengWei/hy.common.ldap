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
package org.apache.directory.api.ldap.model.ldif;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.name.Ava;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.MutableAttributeType;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.util.Base64;
import org.apache.directory.api.util.Chars;
import org.apache.directory.api.util.Strings;
import org.apache.directory.api.util.exception.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <pre>
 *  &lt;ldif-file&gt; ::= &quot;version:&quot; &lt;fill&gt; &lt;number&gt; &lt;seps&gt; &lt;dn-spec&gt; &lt;sep&gt;
 *  &lt;ldif-content-change&gt;
 *
 *  &lt;ldif-content-change&gt; ::=
 *    &lt;number&gt; &lt;oid&gt; &lt;options-e&gt; &lt;value-spec&gt; &lt;sep&gt;
 *    &lt;attrval-specs-e&gt; &lt;ldif-attrval-record-e&gt; |
 *    &lt;alpha&gt; &lt;chars-e&gt; &lt;options-e&gt; &lt;value-spec&gt; &lt;sep&gt;
 *    &lt;attrval-specs-e&gt; &lt;ldif-attrval-record-e&gt; |
 *    &quot;control:&quot; &lt;fill&gt; &lt;number&gt; &lt;oid&gt; &lt;spaces-e&gt;
 *    &lt;criticality&gt; &lt;value-spec-e&gt; &lt;sep&gt; &lt;controls-e&gt;
 *        &quot;changetype:&quot; &lt;fill&gt; &lt;changerecord-type&gt; &lt;ldif-change-record-e&gt; |
 *    &quot;changetype:&quot; &lt;fill&gt; &lt;changerecord-type&gt; &lt;ldif-change-record-e&gt;
 *
 *  &lt;ldif-attrval-record-e&gt; ::= &lt;seps&gt; &lt;dn-spec&gt; &lt;sep&gt; &lt;attributeType&gt;
 *    &lt;options-e&gt; &lt;value-spec&gt; &lt;sep&gt; &lt;attrval-specs-e&gt;
 *    &lt;ldif-attrval-record-e&gt; | e
 *
 *  &lt;ldif-change-record-e&gt; ::= &lt;seps&gt; &lt;dn-spec&gt; &lt;sep&gt; &lt;controls-e&gt;
 *    &quot;changetype:&quot; &lt;fill&gt; &lt;changerecord-type&gt; &lt;ldif-change-record-e&gt; | e
 *
 *  &lt;dn-spec&gt; ::= &quot;dn:&quot; &lt;fill&gt; &lt;safe-string&gt; | &quot;dn::&quot; &lt;fill&gt; &lt;base64-string&gt;
 *
 *  &lt;controls-e&gt; ::= &quot;control:&quot; &lt;fill&gt; &lt;number&gt; &lt;oid&gt; &lt;spaces-e&gt; &lt;criticality&gt;
 *    &lt;value-spec-e&gt; &lt;sep&gt; &lt;controls-e&gt; | e
 *
 *  &lt;criticality&gt; ::= &quot;true&quot; | &quot;false&quot; | e
 *
 *  &lt;oid&gt; ::= '.' &lt;number&gt; &lt;oid&gt; | e
 *
 *  &lt;attrval-specs-e&gt; ::= &lt;number&gt; &lt;oid&gt; &lt;options-e&gt; &lt;value-spec&gt;
 *  &lt;sep&gt; &lt;attrval-specs-e&gt; |
 *    &lt;alpha&gt; &lt;chars-e&gt; &lt;options-e&gt; &lt;value-spec&gt; &lt;sep&gt; &lt;attrval-specs-e&gt; | e
 *
 *  &lt;value-spec-e&gt; ::= &lt;value-spec&gt; | e
 *
 *  &lt;value-spec&gt; ::= ':' &lt;fill&gt; &lt;safe-string-e&gt; |
 *    &quot;::&quot; &lt;fill&gt; &lt;base64-chars&gt; |
 *    &quot;:&lt;&quot; &lt;fill&gt; &lt;url&gt;
 *
 *  &lt;attributeType&gt; ::= &lt;number&gt; &lt;oid&gt; | &lt;alpha&gt; &lt;chars-e&gt;
 *
 *  &lt;options-e&gt; ::= ';' &lt;char&gt; &lt;chars-e&gt; &lt;options-e&gt; |e
 *
 *  &lt;chars-e&gt; ::= &lt;char&gt; &lt;chars-e&gt; |  e
 *
 *  &lt;changerecord-type&gt; ::= &quot;add&quot; &lt;sep&gt; &lt;attributeType&gt;
 *  &lt;options-e&gt; &lt;value-spec&gt; &lt;sep&gt; &lt;attrval-specs-e&gt; |
 *    &quot;delete&quot; &lt;sep&gt; |
 *    &quot;modify&quot; &lt;sep&gt; &lt;mod-type&gt; &lt;fill&gt; &lt;attributeType&gt;
 *    &lt;options-e&gt; &lt;sep&gt; &lt;attrval-specs-e&gt; &lt;sep&gt; '-' &lt;sep&gt; &lt;mod-specs-e&gt; |
 *    &quot;moddn&quot; &lt;sep&gt; &lt;newrdn&gt; &lt;sep&gt; &quot;deleteoldrdn:&quot;
 *    &lt;fill&gt; &lt;0-1&gt; &lt;sep&gt; &lt;newsuperior-e&gt; &lt;sep&gt; |
 *    &quot;modrdn&quot; &lt;sep&gt; &lt;newrdn&gt; &lt;sep&gt; &quot;deleteoldrdn:&quot;
 *    &lt;fill&gt; &lt;0-1&gt; &lt;sep&gt; &lt;newsuperior-e&gt; &lt;sep&gt;
 *
 *  &lt;newrdn&gt; ::= ':' &lt;fill&gt; &lt;safe-string&gt; | &quot;::&quot; &lt;fill&gt; &lt;base64-chars&gt;
 *
 *  &lt;newsuperior-e&gt; ::= &quot;newsuperior&quot; &lt;newrdn&gt; | e
 *
 *  &lt;mod-specs-e&gt; ::= &lt;mod-type&gt; &lt;fill&gt; &lt;attributeType&gt; &lt;options-e&gt;
 *    &lt;sep&gt; &lt;attrval-specs-e&gt; &lt;sep&gt; '-' &lt;sep&gt; &lt;mod-specs-e&gt; | e
 *
 *  &lt;mod-type&gt; ::= &quot;add:&quot; | &quot;delete:&quot; | &quot;replace:&quot;
 *
 *  &lt;url&gt; ::= &lt;a Uniform Resource Locator, as defined in [6]&gt;
 *
 *
 *
 *  LEXICAL
 *  -------
 *
 *  &lt;fill&gt;           ::= ' ' &lt;fill&gt; | e
 *  &lt;char&gt;           ::= &lt;alpha&gt; | &lt;digit&gt; | '-'
 *  &lt;number&gt;         ::= &lt;digit&gt; &lt;digits&gt;
 *  &lt;0-1&gt;            ::= '0' | '1'
 *  &lt;digits&gt;         ::= &lt;digit&gt; &lt;digits&gt; | e
 *  &lt;digit&gt;          ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
 *  &lt;seps&gt;           ::= &lt;sep&gt; &lt;seps-e&gt;
 *  &lt;seps-e&gt;         ::= &lt;sep&gt; &lt;seps-e&gt; | e
 *  &lt;sep&gt;            ::= 0x0D 0x0A | 0x0A
 *  &lt;spaces&gt;         ::= ' ' &lt;spaces-e&gt;
 *  &lt;spaces-e&gt;       ::= ' ' &lt;spaces-e&gt; | e
 *  &lt;safe-string-e&gt;  ::= &lt;safe-string&gt; | e
 *  &lt;safe-string&gt;    ::= &lt;safe-init-char&gt; &lt;safe-chars&gt;
 *  &lt;safe-init-char&gt; ::= [0x01-0x09] | 0x0B | 0x0C | [0x0E-0x1F] | [0x21-0x39] | 0x3B | [0x3D-0x7F]
 *  &lt;safe-chars&gt;     ::= &lt;safe-char&gt; &lt;safe-chars&gt; | e
 *  &lt;safe-char&gt;      ::= [0x01-0x09] | 0x0B | 0x0C | [0x0E-0x7F]
 *  &lt;base64-string&gt;  ::= &lt;base64-char&gt; &lt;base64-chars&gt;
 *  &lt;base64-chars&gt;   ::= &lt;base64-char&gt; &lt;base64-chars&gt; | e
 *  &lt;base64-char&gt;    ::= 0x2B | 0x2F | [0x30-0x39] | 0x3D | [0x41-9x5A] | [0x61-0x7A]
 *  &lt;alpha&gt;          ::= [0x41-0x5A] | [0x61-0x7A]
 *
 *  COMMENTS
 *  --------
 *  - The ldap-oid VN is not correct in the RFC-2849. It has been changed from 1*DIGIT 0*1(&quot;.&quot; 1*DIGIT) to
 *  DIGIT+ (&quot;.&quot; DIGIT+)*
 *  - The mod-spec lacks a sep between *attrval-spec and &quot;-&quot;.
 *  - The BASE64-UTF8-STRING should be BASE64-CHAR BASE64-STRING
 *  - The ValueSpec rule must accept multilines values. In this case, we have a LF followed by a
 *  single space before the continued value.
 * </pre>
 * The relaxed mode is used when a SchemaManager is injected.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifReader implements Iterable<LdifEntry>, Closeable
{
    /** A logger */
    private static final Logger LOG = LoggerFactory.getLogger( LdifReader.class );

    /** A list of read lines */
    protected List<String> lines;

    /** The current position */
    protected int position;

    /** The ldif file version default value */
    protected static final int DEFAULT_VERSION = 1;

    /** The ldif version */
    protected int version;

    /** Type of element read : ENTRY */
    protected static final int LDIF_ENTRY = 0;

    /** Type of element read : CHANGE */
    protected static final int CHANGE = 1;

    /** Type of element read : UNKNOWN */
    protected static final int UNKNOWN = 2;

    /** Size limit for file contained values */
    protected long sizeLimit = SIZE_LIMIT_DEFAULT;

    /** The default size limit : 1Mo */
    protected static final long SIZE_LIMIT_DEFAULT = 1024000;

    /** State values for the modify operation : MOD_SPEC */
    protected static final int MOD_SPEC = 0;

    /** State values for the modify operation : ATTRVAL_SPEC */
    protected static final int ATTRVAL_SPEC = 1;

    /** State values for the modify operation : ATTRVAL_SPEC_OR_SEP */
    protected static final int ATTRVAL_SPEC_OR_SEP = 2;

    /** Iterator prefetched entry */
    protected LdifEntry prefetched;

    /** The ldif Reader */
    protected Reader reader;

    /** A flag set if the ldif contains entries */
    protected boolean containsEntries;

    /** A flag set if the ldif contains changes */
    protected boolean containsChanges;

    /** The SchemaManager instance, if any */
    protected SchemaManager schemaManager;

    /**
     * An Exception to handle error message, has Iterator.next() can't throw
     * exceptions
     */
    protected Exception error;

    /** total length of an LDIF entry including the comments */
    protected int entryLen = 0;

    /** the parsed entry's starting position */
    protected long entryOffset = 0;

    /** the current offset of the reader */
    protected long offset = 0;

    /** the numer of the current line being parsed by the reader */
    protected int lineNumber;

    /** flag to turn on/off of the DN validation. By default DNs are validated after parsing */
    protected boolean validateDn = true;
    
    /** A counter used to create facked OIDs */
    private int oidCounter = 0;


    /**
     * Constructors
     */
    public LdifReader()
    {
        lines = new ArrayList<>();
        position = 0;
        version = DEFAULT_VERSION;
    }


    /**
     * Creates a Schema aware reader
     * 
     * @param schemaManager The SchemaManager
     */
    public LdifReader( SchemaManager schemaManager )
    {
        lines = new ArrayList<>();
        position = 0;
        version = DEFAULT_VERSION;
        this.schemaManager = schemaManager;
    }


    /**
     * A constructor which takes a file name. Default charset is used.
     *
     * @param ldifFileName A file name containing ldif formated input
     * @throws LdapLdifException If the file cannot be processed or if the format is incorrect
     */
    public LdifReader( String ldifFileName ) throws LdapLdifException
    {
        this( new File( ldifFileName ) );
    }


    /**
     * A constructor which takes a Reader.
     *
     * @param in A Reader containing ldif formated input
     * @throws LdapException If the file cannot be processed or if the format is incorrect
     */
    public LdifReader( Reader in ) throws LdapException
    {
        initReader( new BufferedReader( in ) );
    }


    /**
     * A constructor which takes an InputStream. Default charset is used.
     *
     * @param in An InputStream containing ldif formated input
     * @throws LdapException If the file cannot be processed or if the format is incorrect
     */
    public LdifReader( InputStream in ) throws LdapException
    {
        initReader( new BufferedReader( new InputStreamReader( in, Charset.defaultCharset() ) ) );
    }


    /**
     * A constructor which takes a File. Default charset is used.
     *
     * @param file A File containing ldif formated input
     * @throws LdapLdifException If the file cannot be processed or if the format is incorrect
     */
    public LdifReader( File file ) throws LdapLdifException
    {
        this( file, null );
    }


    /**
     * A constructor which takes a File and a SchemaManager. Default charset is used.
     *
     * @param file A File containing ldif formated input
     * @param schemaManager The SchemaManager instance to use
     * @throws LdapLdifException If the file cannot be processed or if the format is incorrect
     */
    public LdifReader( File file, SchemaManager schemaManager ) throws LdapLdifException
    {
        if ( !file.exists() )
        {
            String msg = I18n.err( I18n.ERR_13443_CANNOT_FIND_FILE, file.getAbsoluteFile() );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }

        if ( !file.canRead() )
        {
            String msg = I18n.err( I18n.ERR_13444_CANNOT_READ_FILE, file.getName() );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }

        this.schemaManager = schemaManager;

        try
        {
            InputStream is = Files.newInputStream( Paths.get( file.getPath() ) );
            initReader(
                new BufferedReader( new InputStreamReader( is, Charset.defaultCharset() ) ) );
        }
        catch ( FileNotFoundException fnfe )
        {
            String msg = I18n.err( I18n.ERR_13443_CANNOT_FIND_FILE, file.getAbsoluteFile() );
            LOG.error( msg );
            throw new LdapLdifException( msg, fnfe );
        }
        catch ( LdapInvalidDnException lide )
        {
            throw new LdapLdifException( lide.getMessage(), lide );
        }
        catch ( IOException ioe )
        {
            throw new LdapLdifException( ioe.getMessage(), ioe );
        }
        catch ( LdapException le )
        {
            throw new LdapLdifException( le.getMessage(), le );
        }
    }


    /**
     * Store the reader and initialize the LdifReader
     * 
     * @param reader The reader to use
     * @throws LdapException If the initialization failed
     */
    private void initReader( BufferedReader reader ) throws LdapException
    {
        this.reader = reader;
        init();
    }


    /**
     * Initialize the LdifReader
     * 
     * @throws LdapException If the initialization failed
     */
    public void init() throws LdapException
    {
        lines = new ArrayList<>();
        position = 0;
        version = DEFAULT_VERSION;
        containsChanges = false;
        containsEntries = false;

        // First get the version - if any -
        version = parseVersion();
        prefetched = parseEntry();
    }


    /**
     * @return The ldif file version
     */
    public int getVersion()
    {
        return version;
    }


    /**
     * @return The maximum size of a file which is used into an attribute value.
     */
    public long getSizeLimit()
    {
        return sizeLimit;
    }


    /**
     * Set the maximum file size that can be accepted for an attribute value
     *
     * @param sizeLimit The size in bytes
     */
    public void setSizeLimit( long sizeLimit )
    {
        this.sizeLimit = sizeLimit;
    }


    // <fill> ::= ' ' <fill> | e
    private void parseFill( char[] document )
    {
        while ( Chars.isCharASCII( document, position, ' ' ) )
        {
            position++;
        }
    }


    /**
     * Parse a number following the rules :
     *
     * &lt;number&gt; ::= &lt;digit&gt; &lt;digits&gt; &lt;digits&gt; ::= &lt;digit&gt; &lt;digits&gt; | e &lt;digit&gt;
     * ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
     *
     * Check that the number is in the interval
     *
     * @param document The document containing the number to parse
     * @return a String representing the parsed number
     */
    private String parseNumber( char[] document )
    {
        int initPos = position;

        while ( true )
        {
            if ( Chars.isDigit( document, position ) )
            {
                position++;
            }
            else
            {
                break;
            }
        }

        if ( position == initPos )
        {
            return null;
        }
        else
        {
            return new String( document, initPos, position - initPos );
        }
    }


    /**
     * Parse the changeType
     *
     * @param line The line which contains the changeType
     * @return The operation.
     */
    protected ChangeType parseChangeType( String line )
    {
        ChangeType operation = ChangeType.Add;

        String modOp = Strings.trim( line.substring( "changetype:".length() ) );

        if ( "add".equalsIgnoreCase( modOp ) )
        {
            operation = ChangeType.Add;
        }
        else if ( "delete".equalsIgnoreCase( modOp ) )
        {
            operation = ChangeType.Delete;
        }
        else if ( "modify".equalsIgnoreCase( modOp ) )
        {
            operation = ChangeType.Modify;
        }
        else if ( "moddn".equalsIgnoreCase( modOp ) )
        {
            operation = ChangeType.ModDn;
        }
        else if ( "modrdn".equalsIgnoreCase( modOp ) )
        {
            operation = ChangeType.ModRdn;
        }

        return operation;
    }


    /**
     * Parse the Dn of an entry
     *
     * @param line The line to parse
     * @return A Dn
     * @throws LdapLdifException If the Dn is invalid
     */
    protected String parseDn( String line ) throws LdapLdifException
    {
        String dn;

        String lowerLine = Strings.toLowerCaseAscii( line );

        if ( lowerLine.startsWith( "dn:" ) || lowerLine.startsWith( "Dn:" ) )
        {
            // Ok, we have a Dn. Is it base 64 encoded ?
            int length = line.length();

            if ( length == 3 )
            {
                // The Dn is empty : it's a rootDSE
                dn = "";
            }
            else if ( line.charAt( 3 ) == ':' )
            {
                if ( length > 4 )
                {
                    // This is a base 64 encoded Dn.
                    String trimmedLine = line.substring( 4 ).trim();

                    dn = Strings.utf8ToString( Base64.decode( trimmedLine.toCharArray() ) );
                }
                else
                {
                    // The Dn is empty : error
                    LOG.error( I18n.err( I18n.ERR_13404_EMPTY_DN_NOT_ALLOWED, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13445_NO_DN ) );
                }
            }
            else
            {
                dn = line.substring( 3 ).trim();
            }
        }
        else
        {
            LOG.error( I18n.err( I18n.ERR_13405_DN_EXPECTED, lineNumber ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13445_NO_DN ) );
        }

        // Check that the Dn is valid. If not, an exception will be thrown
        if ( validateDn && !Dn.isValid( dn ) )
        {
            String message = I18n.err( I18n.ERR_13446_INVALID_DN, dn, lineNumber );
            LOG.error( message );
            throw new LdapLdifException( message );
        }

        return dn;
    }


    /**
     * Parse the value part.
     *
     * @param line The line which contains the value
     * @param pos The starting position in the line
     * @return A String or a byte[], depending of the kind of value we get
     */
    protected static Object parseSimpleValue( String line, int pos )
    {
        if ( line.length() > pos + 1 )
        {
            char c = line.charAt( pos + 1 );

            if ( c == ':' )
            {
                String value = Strings.trim( line.substring( pos + 2 ) );

                return Base64.decode( value.toCharArray() );
            }
            else
            {
                return Strings.trim( line.substring( pos + 1 ) );
            }
        }
        else
        {
            return null;
        }
    }

    
    private Object getValue( String attributeName, byte[] value )
    {
        if ( schemaManager != null )
        {
            AttributeType attributeType = schemaManager.getAttributeType( attributeName );
            
            if ( attributeType != null )
            {
                if ( attributeType.getSyntax().isHumanReadable() )
                {
                    return Strings.utf8ToString( value );
                }
                else
                {
                    return value;
                }
            }
            else
            {
                return value;
            }
        }
        else
        {
            return value;
        }
    }
    

    /**
     * Parse the value part.
     *
     * @param attributeName The attribute name
     * @param line The line which contains the value
     * @param pos The starting position in the line
     * @return A String or a byte[], depending of the kind of value we get
     * @throws LdapLdifException If something went wrong
     */
    protected Object parseValue( String attributeName, String line, int pos ) throws LdapLdifException
    {
        if ( line.length() > pos + 1 )
        {
            char c = line.charAt( pos + 1 );

            if ( c == ':' )
            {
                String value = Strings.trim( line.substring( pos + 2 ) );

                byte[] decoded = Base64.decode( value.toCharArray() );
                
                return getValue( attributeName, decoded );
            }
            else if ( c == '<' )
            {
                String urlName = Strings.trim( line.substring( pos + 2 ) );

                try
                {
                    URL url = new URL( urlName );

                    if ( "file".equals( url.getProtocol() ) )
                    {
                        String fileName = url.getFile();

                        File file = new File( fileName );

                        if ( !file.exists() )
                        {
                            LOG.error( I18n.err( I18n.ERR_13406_FILE_NOT_FOUND, fileName, lineNumber ) );
                            throw new LdapLdifException( I18n.err( I18n.ERR_13447_BAD_URL_FILE_NOT_FOUND ) );
                        }
                        else
                        {
                            long length = file.length();

                            if ( length > sizeLimit )
                            {
                                String message = I18n.err( I18n.ERR_13448_FILE_TOO_BIG, fileName, lineNumber );
                                LOG.error( message );
                                throw new LdapLdifException( message );
                            }
                            else
                            {
                                byte[] data = new byte[( int ) length];

                                try ( DataInputStream inf = new DataInputStream( 
                                    Files.newInputStream( Paths.get( fileName ) ) ) )
                                {
                                    inf.readFully( data );

                                    return getValue( attributeName, data );
                                }
                                catch ( FileNotFoundException fnfe )
                                {
                                    // We can't reach this point, the file
                                    // existence has already been
                                    // checked
                                    LOG.error( I18n.err( I18n.ERR_13406_FILE_NOT_FOUND, fileName, lineNumber ) );
                                    throw new LdapLdifException( I18n.err( I18n.ERR_13447_BAD_URL_FILE_NOT_FOUND ),
                                        fnfe );
                                }
                                catch ( IOException ioe )
                                {
                                    LOG.error( I18n.err( I18n.ERR_13407_ERROR_READING_FILE, fileName, lineNumber ) );
                                    throw new LdapLdifException( I18n.err( I18n.ERR_13449_ERROR_READING_BAD_URL ), ioe );
                                }
                            }
                        }
                    }
                    else
                    {
                        LOG.error( I18n.err( I18n.ERR_13408_BAD_PROTOCOL ) );
                        throw new LdapLdifException( I18n.err( I18n.ERR_13451_UNSUPPORTED_PROTOCOL, lineNumber ) );
                    }
                }
                catch ( MalformedURLException mue )
                {
                    String message = I18n.err( I18n.ERR_13452_BAD_URL, urlName, lineNumber );
                    LOG.error( message );
                    throw new LdapLdifException( message, mue );
                }
            }
            else
            {
                String value = Strings.trimLeft( line.substring( pos + 1 ) );
                int end = value.length();

                for ( int i = value.length() - 1; i > 0; i-- )
                {
                    char cc = value.charAt( i );

                    if ( cc == ' ' )
                    {
                        if ( value.charAt( i - 1 ) == '\\' )
                        {
                            // Escaped space : do nothing
                            break;
                        }
                        else
                        {
                            end = i;
                        }
                    }
                    else
                    {
                        break;
                    }
                }

                String result = null;

                if ( end > 0 )
                {
                    result = value.substring( 0, end );
                }

                return result;
            }
        }
        else
        {
            return null;
        }
    }


    /**
     * Parse a control. The grammar is :
     * <pre>
     * &lt;control&gt; ::= "control:" &lt;fill&gt; &lt;ldap-oid&gt; &lt;critical-e&gt; &lt;value-spec-e&gt; &lt;sep&gt;
     * &lt;critical-e&gt; ::= &lt;spaces&gt; &lt;boolean&gt; | e
     * &lt;boolean&gt; ::= "true" | "false"
     * &lt;value-spec-e&gt; ::= &lt;value-spec&gt; | e
     * &lt;value-spec&gt; ::= ":" &lt;fill&gt; &lt;SAFE-STRING-e&gt; | "::" &lt;fill&gt; &lt;BASE64-STRING&gt; | ":&lt;" &lt;fill&gt; &lt;url&gt;
     * </pre>
     *
     * It can be read as :
     * <pre>
     * "control:" &lt;fill&gt; &lt;ldap-oid&gt; [ " "+ ( "true" |
     * "false") ] [ ":" &lt;fill&gt; &lt;SAFE-STRING-e&gt; | "::" &lt;fill&gt; &lt;BASE64-STRING&gt; | ":&lt;"
     * &lt;fill&gt; &lt;url&gt; ]
     * </pre>
     *
     * @param line The line containing the control
     * @return A control
     * @exception LdapLdifException If the control has no OID or if the OID is incorrect,
     * of if the criticality is not set when it's mandatory.
     */
    private Control parseControl( String line ) throws LdapLdifException
    {
        String lowerLine = Strings.toLowerCaseAscii( line ).trim();
        char[] controlValue = line.trim().toCharArray();
        int pos = 0;
        int length = controlValue.length;

        // Get the <ldap-oid>
        if ( pos > length )
        {
            // No OID : error !
            String msg = I18n.err( I18n.ERR_13409_CONTROL_WITHOUT_OID, lineNumber );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }

        int initPos = pos;

        while ( Chars.isCharASCII( controlValue, pos, '.' ) || Chars.isDigit( controlValue, pos ) )
        {
            pos++;
        }

        if ( pos == initPos )
        {
            // Not a valid OID !
            String msg = I18n.err( I18n.ERR_13409_CONTROL_WITHOUT_OID, lineNumber );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }

        // Create and check the OID
        String oidString = lowerLine.substring( 0, pos );

        if ( !Oid.isOid( oidString ) )
        {
            String message = I18n.err( I18n.ERR_13453_INVALID_OID, oidString, lineNumber );
            LOG.error( message );
            throw new LdapLdifException( message );
        }

        LdifControl control = new LdifControl( oidString );

        // Get the criticality, if any
        // Skip the <fill>
        while ( Chars.isCharASCII( controlValue, pos, ' ' ) )
        {
            pos++;
        }

        // Check if we have a "true" or a "false"
        int criticalPos = lowerLine.indexOf( ':' );

        int criticalLength;

        if ( criticalPos == -1 )
        {
            criticalLength = length - pos;
        }
        else
        {
            criticalLength = criticalPos - pos;
        }

        if ( ( criticalLength == 4 ) && ( "true".equalsIgnoreCase( lowerLine.substring( pos, pos + 4 ) ) ) )
        {
            control.setCritical( true );
        }
        else if ( ( criticalLength == 5 ) && ( "false".equalsIgnoreCase( lowerLine.substring( pos, pos + 5 ) ) ) )
        {
            control.setCritical( false );
        }
        else if ( criticalLength != 0 )
        {
            // If we have a criticality, it should be either "true" or "false",
            // nothing else
            String msg = I18n.err( I18n.ERR_13410_INVALID_CRITICALITY, lineNumber );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }

        if ( criticalPos > 0 )
        {
            // We have a value. It can be a normal value, a base64 encoded value
            // or a file contained value
            if ( Chars.isCharASCII( controlValue, criticalPos + 1, ':' ) )
            {
                // Base 64 encoded value

                // Skip the <fill>
                pos = criticalPos + 2;

                while ( Chars.isCharASCII( controlValue, pos, ' ' ) )
                {
                    pos++;
                }

                byte[] value = Base64.decode( line.substring( pos ).toCharArray() );
                control.setValue( value );
            }
            else if ( Chars.isCharASCII( controlValue, criticalPos + 1, '<' ) )
            {
                // File contained value
                throw new NotImplementedException( I18n.err( I18n.ERR_13433_SEE_DIRSERVER_1547 ) );
            }
            else
            {
                // Skip the <fill>
                pos = criticalPos + 1;

                while ( Chars.isCharASCII( controlValue, pos, ' ' ) )
                {
                    pos++;
                }

                // Standard value
                byte[] value = new byte[length - pos];

                for ( int i = 0; i < length - pos; i++ )
                {
                    value[i] = ( byte ) controlValue[i + pos];
                }

                control.setValue( value );
            }
        }

        return control;
    }


    /**
     * Parse an AttributeType/AttributeValue
     *
     * @param line The line to parse
     * @return the parsed Attribute
     */
    public static Attribute parseAttributeValue( String line )
    {
        int colonIndex = line.indexOf( ':' );

        if ( colonIndex != -1 )
        {
            String attributeType = line.substring( 0, colonIndex );
            Object attributeValue = parseSimpleValue( line, colonIndex );

            // Create an attribute
            if ( attributeValue instanceof String )
            {
                return new DefaultAttribute( attributeType, ( String ) attributeValue );
            }
            else
            {
                return new DefaultAttribute( attributeType, ( byte[] ) attributeValue );
            }
        }
        else
        {
            return null;
        }
    }


    /**
     * Parse an AttributeType/AttributeValue
     *
     * @param entry The entry where to store the value
     * @param line The line to parse
     * @param lowerLine The same line, lowercased
     * @throws LdapException If anything goes wrong
     */
    public void parseAttributeValue( LdifEntry entry, String line, String lowerLine ) throws LdapException
    {
        int colonIndex = line.indexOf( ':' );

        String attributeType = lowerLine.substring( 0, colonIndex );

        // We should *not* have a Dn twice
        if ( "dn".equals( attributeType ) )
        {
            LOG.error( I18n.err( I18n.ERR_13400_ENTRY_WITH_TWO_DNS, lineNumber ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13439_LDIF_ENTRY_WITH_TWO_DNS ) );
        }

        Object attributeValue = parseValue( attributeType, line, colonIndex );

        if ( schemaManager != null )
        {
            AttributeType at = schemaManager.getAttributeType( attributeType );

            if ( at != null )
            {
                if ( at.getSyntax().isHumanReadable() )
                {
                    if ( attributeValue == null )
                    {
                        attributeValue = "";
                    }
                    else if ( attributeValue instanceof byte[] )
                    {
                        attributeValue = Strings.utf8ToString( ( byte[] ) attributeValue );
                    }
                }
                else
                {
                    if ( attributeValue instanceof String )
                    {
                        attributeValue = Strings.getBytesUtf8( ( String ) attributeValue );
                    }
                }
            }
        }

        // Update the entry
        try
        {
            entry.addAttribute( attributeType, attributeValue );
        }
        catch ( Exception e )
        {
            // The attribute does not exist already, create a fake one 
            if ( ( schemaManager != null ) && schemaManager.isRelaxed() )
            {
                MutableAttributeType newAttributeType = new MutableAttributeType( "1.3.6.1.4.1.18060.0.9999." + oidCounter++ );
                newAttributeType.setNames( attributeType );
                newAttributeType.setSyntax( schemaManager.getLdapSyntaxRegistry().get( SchemaConstants.DIRECTORY_STRING_SYNTAX ) );
                schemaManager.add( newAttributeType );
                entry.addAttribute( attributeType, attributeValue );
            }
        }
    }


    /**
     * Parse a ModRDN operation
     *
     * @param entry The entry to update
     * @param iter The lines iterator
     * @throws LdapLdifException If anything goes wrong
     */
    private void parseModRdn( LdifEntry entry, Iterator<String> iter ) throws LdapLdifException
    {
        // We must have two lines : one starting with "newrdn:" or "newrdn::",
        // and the second starting with "deleteoldrdn:"
        if ( iter.hasNext() )
        {
            String line = iter.next();
            String lowerLine = Strings.toLowerCaseAscii( line );

            if ( lowerLine.startsWith( "newrdn::" ) || lowerLine.startsWith( "newrdn:" ) )
            {
                int colonIndex = line.indexOf( ':' );
                Object attributeValue = parseValue( null, line, colonIndex );

                if ( attributeValue instanceof String )
                {
                    entry.setNewRdn( ( String ) attributeValue );
                }
                else
                {
                    entry.setNewRdn( Strings.utf8ToString( ( byte[] ) attributeValue ) );
                }
            }
            else
            {
                String msg = I18n.err( I18n.ERR_13411_BAD_MODRDN_OPERATION, lineNumber );
                LOG.error( msg );
                throw new LdapLdifException( msg );
            }
        }
        else
        {
            String msg = I18n.err( I18n.ERR_13411_BAD_MODRDN_OPERATION, lineNumber );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }

        if ( iter.hasNext() )
        {
            String line = iter.next();
            String lowerLine = Strings.toLowerCaseAscii( line );

            if ( lowerLine.startsWith( "deleteoldrdn:" ) )
            {
                int colonIndex = line.indexOf( ':' );
                Object attributeValue = parseValue( null, line, colonIndex );
                entry.setDeleteOldRdn( "1".equals( attributeValue ) );
            }
            else
            {
                String msg = I18n.err( I18n.ERR_13412_NO_DELETEOLDRDN, lineNumber );
                LOG.error( msg );
                throw new LdapLdifException( msg );
            }
        }
        else
        {
            String msg = I18n.err( I18n.ERR_13412_NO_DELETEOLDRDN, lineNumber );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }
    }


    /**
     * Parse a modify change type.
     *
     * The grammar is :
     * <pre>
     * &lt;changerecord&gt; ::= "changetype:" FILL "modify" SEP &lt;mod-spec&gt; &lt;mod-specs-e&gt;
     * &lt;mod-spec&gt; ::= "add:" &lt;mod-val&gt; | "delete:" &lt;mod-val-del&gt; | "replace:" &lt;mod-val&gt;
     * &lt;mod-specs-e&gt; ::= &lt;mod-spec&gt;
     * &lt;mod-specs-e&gt; | e
     * &lt;mod-val&gt; ::= FILL ATTRIBUTE-DESCRIPTION SEP ATTRVAL-SPEC &lt;attrval-specs-e&gt; "-" SEP
     * &lt;mod-val-del&gt; ::= FILL ATTRIBUTE-DESCRIPTION SEP &lt;attrval-specs-e&gt; "-" SEP
     * &lt;attrval-specs-e&gt; ::= ATTRVAL-SPEC &lt;attrval-specs&gt; | e
     * </pre>
     *
     * @param entry The entry to feed
     * @param iter The lines
     * @exception LdapLdifException If the modify operation is invalid
     */
    private void parseModify( LdifEntry entry, Iterator<String> iter ) throws LdapLdifException
    {
        int state = MOD_SPEC;
        String modified = null;
        ModificationOperation modificationType = ModificationOperation.ADD_ATTRIBUTE;
        Attribute attribute = null;

        // The following flag is used to deal with empty modifications
        boolean isEmptyValue = true;

        while ( iter.hasNext() )
        {
            String line = iter.next();
            String lowerLine = Strings.toLowerCaseAscii( line );

            if ( lowerLine.startsWith( "-" ) )
            {
                if ( ( state != ATTRVAL_SPEC_OR_SEP ) && ( state != ATTRVAL_SPEC ) )
                {
                    String msg = I18n.err( I18n.ERR_13413_BAD_MODIFY_SEPARATOR, lineNumber );
                    LOG.error( msg );
                    throw new LdapLdifException( msg );
                }
                else
                {
                    if ( isEmptyValue )
                    {
                        if ( state == ATTRVAL_SPEC_OR_SEP )
                        {
                            entry.addModification( modificationType, modified );
                        }
                        else
                        {
                            // Update the entry with a null value
                            entry.addModification( modificationType, modified, null );
                        }
                    }
                    else
                    {
                        // Update the entry with the attribute
                        entry.addModification( modificationType, attribute );
                    }

                    state = MOD_SPEC;
                    isEmptyValue = true;
                }
            }
            else if ( lowerLine.startsWith( "add:" ) )
            {
                if ( ( state != MOD_SPEC ) && ( state != ATTRVAL_SPEC ) )
                {
                    String msg = I18n.err( I18n.ERR_13414_BAD_MODIFY_SEPARATOR_2, lineNumber );
                    LOG.error( msg );
                    throw new LdapLdifException( msg );
                }

                modified = Strings.trim( line.substring( "add:".length() ) );
                modificationType = ModificationOperation.ADD_ATTRIBUTE;
                attribute = new DefaultAttribute( modified );

                state = ATTRVAL_SPEC;
            }
            else if ( lowerLine.startsWith( "delete:" ) )
            {
                if ( ( state != MOD_SPEC ) && ( state != ATTRVAL_SPEC ) )
                {
                    String msg = I18n.err( I18n.ERR_13414_BAD_MODIFY_SEPARATOR_2, lineNumber );
                    LOG.error( msg );
                    throw new LdapLdifException( msg );
                }

                modified = Strings.trim( line.substring( "delete:".length() ) );
                modificationType = ModificationOperation.REMOVE_ATTRIBUTE;
                attribute = new DefaultAttribute( modified );
                isEmptyValue = false;

                state = ATTRVAL_SPEC_OR_SEP;
            }
            else if ( lowerLine.startsWith( "replace:" ) )
            {
                if ( ( state != MOD_SPEC ) && ( state != ATTRVAL_SPEC ) )
                {
                    String msg = I18n.err( I18n.ERR_13414_BAD_MODIFY_SEPARATOR_2, lineNumber );
                    LOG.error( msg );
                    throw new LdapLdifException( msg );
                }

                modified = Strings.trim( line.substring( "replace:".length() ) );
                modificationType = ModificationOperation.REPLACE_ATTRIBUTE;
                
                if ( schemaManager != null )
                {
                    AttributeType attributeType = schemaManager.getAttributeType( modified );
                    attribute = new DefaultAttribute( modified, attributeType );
                }
                else
                {
                    attribute = new DefaultAttribute( modified );
                }

                state = ATTRVAL_SPEC_OR_SEP;
            }
            else
            {
                if ( ( state != ATTRVAL_SPEC ) && ( state != ATTRVAL_SPEC_OR_SEP ) )
                {
                    String msg = I18n.err( I18n.ERR_13413_BAD_MODIFY_SEPARATOR, lineNumber );
                    LOG.error( msg );
                    throw new LdapLdifException( msg );
                }

                // A standard AttributeType/AttributeValue pair
                int colonIndex = line.indexOf( ':' );

                String attributeType = line.substring( 0, colonIndex );

                if ( !attributeType.equalsIgnoreCase( modified ) )
                {
                    LOG.error( I18n.err( I18n.ERR_13415_MOD_ATTR_AND_VALUE_SPEC_NOT_EQUAL, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13454_BAD_MODIFY_ATTRIBUTE ) );
                }

                // We should *not* have a Dn twice
                if ( "dn".equalsIgnoreCase( attributeType ) )
                {
                    LOG.error( I18n.err( I18n.ERR_13400_ENTRY_WITH_TWO_DNS, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13439_LDIF_ENTRY_WITH_TWO_DNS ) );
                }

                Object attributeValue = parseValue( attributeType, line, colonIndex );

                try
                {
                    if ( attributeValue instanceof String )
                    {
                        attribute.add( ( String ) attributeValue );
                    }
                    else
                    {
                        attribute.add( ( byte[] ) attributeValue );
                    }
                }
                catch ( LdapInvalidAttributeValueException liave )
                {
                    throw new LdapLdifException( liave.getMessage(), liave );
                }

                isEmptyValue = false;

                state = ATTRVAL_SPEC_OR_SEP;
            }
        }

        if ( state != MOD_SPEC )
        {
            String msg = I18n.err( I18n.ERR_13414_BAD_MODIFY_SEPARATOR_2, lineNumber );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }
    }


    /**
     * Parse a change operation. We have to handle different cases depending on
     * the operation.
     * <ul>
     * <li>1) Delete : there should *not* be any line after the "changetype: delete" </li>
     * <li>2) Add : we must have a list of AttributeType : AttributeValue elements </li>
     * <li>3) ModDN : we must have two following lines: a "newrdn:" and a "deleteoldrdn:" </li>
     * <li>4) ModRDN : the very same, but a "newsuperior:" line is expected </li>
     * <li>5) Modify</li>
     * </ul>
     *
     * The grammar is :
     * <pre>
     * &lt;changerecord&gt; ::= "changetype:" FILL "add" SEP &lt;attrval-spec&gt; &lt;attrval-specs-e&gt; |
     *     "changetype:" FILL "delete" |
     *     "changetype:" FILL "modrdn" SEP &lt;newrdn&gt; SEP &lt;deleteoldrdn&gt; SEP |
     *     // To be checked
     *     "changetype:" FILL "moddn" SEP &lt;newrdn&gt; SEP &lt;deleteoldrdn&gt; SEP &lt;newsuperior&gt; SEP |
     *     "changetype:" FILL "modify" SEP &lt;mod-spec&gt; &lt;mod-specs-e&gt;
     * &lt;newrdn&gt; ::= "newrdn:" FILL Rdn | "newrdn::" FILL BASE64-Rdn
     * &lt;deleteoldrdn&gt; ::= "deleteoldrdn:" FILL "0" | "deleteoldrdn:" FILL "1"
     * &lt;newsuperior&gt; ::= "newsuperior:" FILL Dn | "newsuperior::" FILL BASE64-Dn
     * &lt;mod-specs-e&gt; ::= &lt;mod-spec&gt; &lt;mod-specs-e&gt; | e
     * &lt;mod-spec&gt; ::= "add:" &lt;mod-val&gt; | "delete:" &lt;mod-val&gt; | "replace:" &lt;mod-val&gt;
     * &lt;mod-val&gt; ::= FILL ATTRIBUTE-DESCRIPTION SEP ATTRVAL-SPEC &lt;attrval-specs-e&gt; "-" SEP
     * &lt;attrval-specs-e&gt; ::= ATTRVAL-SPEC &lt;attrval-specs&gt; | e
     * </pre>
     *
     * @param entry The entry to feed
     * @param iter The lines iterator
     * @param operation The change operation (add, modify, delete, moddn or modrdn)
     * @exception LdapException If the change operation is invalid
     */
    private void parseChange( LdifEntry entry, Iterator<String> iter, ChangeType operation ) throws LdapException
    {
        // The changetype and operation has already been parsed.
        entry.setChangeType( operation );

        switch ( operation )
        {
            case Delete:
                // The change type will tell that it's a delete operation,
                // the dn is used as a key.
                return;

            case Add:
                // We will iterate through all attribute/value pairs
                while ( iter.hasNext() )
                {
                    String line = iter.next();
                    String lowerLine = Strings.toLowerCaseAscii( line );
                    parseAttributeValue( entry, line, lowerLine );
                }

                return;

            case Modify:
                parseModify( entry, iter );
                return;

            case ModDn:
                // They are supposed to have the same syntax :
                // No break !
            case ModRdn:
                // First, parse the modrdn part
                parseModRdn( entry, iter );

                // The next line should be the new superior, if we have one
                if ( iter.hasNext() )
                {
                    String line = iter.next();
                    String lowerLine = Strings.toLowerCaseAscii( line );

                    if ( lowerLine.startsWith( "newsuperior:" ) )
                    {
                        int colonIndex = line.indexOf( ':' );
                        Object attributeValue = parseValue( null, line, colonIndex );

                        if ( attributeValue instanceof String )
                        {
                            entry.setNewSuperior( ( String ) attributeValue );
                        }
                        else
                        {
                            entry.setNewSuperior( Strings.utf8ToString( ( byte[] ) attributeValue ) );
                        }
                    }
                    else
                    {
                        if ( operation == ChangeType.ModDn )
                        {
                            LOG.error( I18n.err( I18n.ERR_13416_NEW_SUPERIOR_NEEDED, lineNumber ) );
                            throw new LdapLdifException( I18n.err( I18n.ERR_13455_BAD_MODDN_NO_SUPERIOR ) );
                        }
                    }
                }

                return;

            default:
                // This is an error
                LOG.error( I18n.err( I18n.ERR_13417_UNKNOWN_OPERATION, lineNumber ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13456_BAD_OPERATION ) );
        }
    }


    /**
     * Parse a ldif file. The following rules are processed :
     * <pre>
     * &lt;ldif-file&gt; ::= &lt;ldif-attrval-record&gt; &lt;ldif-attrval-records&gt; |
     *     &lt;ldif-change-record&gt; &lt;ldif-change-records&gt;
     * &lt;ldif-attrval-record&gt; ::= &lt;dn-spec&gt; &lt;sep&gt; &lt;attrval-spec&gt; &lt;attrval-specs&gt;
     * &lt;ldif-change-record&gt; ::= &lt;dn-spec&gt; &lt;sep&gt; &lt;controls-e&gt; &lt;changerecord&gt;
     * &lt;dn-spec&gt; ::= "dn:" &lt;fill&gt; &lt;distinguishedName&gt; | "dn::" &lt;fill&gt; &lt;base64-distinguishedName&gt;
     * &lt;changerecord&gt; ::= "changetype:" &lt;fill&gt; &lt;change-op&gt;
     * </pre>
     *
     * @return the parsed ldifEntry
     * @exception LdapException If the ldif file does not contain a valid entry
     */
    protected LdifEntry parseEntry() throws LdapException
    {
        if ( ( lines == null ) || lines.isEmpty() )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13408_END_OF_LDIF ) );
            }

            return null;
        }

        // The entry must start with a dn: or a dn::
        String line = lines.get( 0 );

        lineNumber -= ( lines.size() - 1 );

        String name = parseDn( line );

        Dn dn = null;
        
        try
        {
            dn = new Dn( schemaManager, name );
        }
        catch ( LdapInvalidDnException lide )
        {
            // Deal with the RDN whihc is not in the schema
            // First parse the DN without the schema
            dn = new Dn( name );
            
            Rdn rdn = dn.getRdn();
            
            // Process each Ava
            for ( Ava ava : rdn )
            {
                if ( ( schemaManager != null ) && ( schemaManager.getAttributeType( ava.getType() ) == null ) 
                    && schemaManager.isRelaxed() )
                {
                    // Not found : create a new one
                    MutableAttributeType newAttributeType = new MutableAttributeType( "1.3.6.1.4.1.18060.0.9999." + oidCounter++ );
                    newAttributeType.setNames( ava.getType() );
                    newAttributeType.setSyntax( schemaManager.getLdapSyntaxRegistry().get( SchemaConstants.DIRECTORY_STRING_SYNTAX ) );
                    schemaManager.add( newAttributeType );
                }
            }
            
            dn = new Dn( schemaManager, name );
        }

        // Ok, we have found a Dn
        LdifEntry entry = createLdifEntry( schemaManager );
        entry.setLengthBeforeParsing( entryLen );
        entry.setOffset( entryOffset );

        entry.setDn( dn );

        // We remove this dn from the lines
        lines.remove( 0 );

        // Now, let's iterate through the other lines
        Iterator<String> iter = lines.iterator();

        // This flag is used to distinguish between an entry and a change
        int type = LDIF_ENTRY;

        // The following boolean is used to check that a control is *not*
        // found elswhere than just after the dn
        boolean controlSeen = false;

        // We use this boolean to check that we do not have AttributeValues
        // after a change operation
        boolean changeTypeSeen = false;

        ChangeType operation = ChangeType.Add;
        String lowerLine;
        Control control;

        while ( iter.hasNext() )
        {
            lineNumber++;

            // Each line could start either with an OID, an attribute type, with
            // "control:" or with "changetype:"
            line = iter.next();
            lowerLine = Strings.toLowerCaseAscii( line );

            // We have three cases :
            // 1) The first line after the Dn is a "control:"
            // 2) The first line after the Dn is a "changeType:"
            // 3) The first line after the Dn is anything else
            if ( lowerLine.startsWith( "control:" ) )
            {
                if ( containsEntries )
                {
                    LOG.error( I18n.err( I18n.ERR_13401_CHANGE_NOT_ALLOWED, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13440_NO_CHANGE ) );
                }

                containsChanges = true;

                if ( controlSeen )
                {
                    LOG.error( I18n.err( I18n.ERR_13418_CONTROL_ALREADY_FOUND, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13457_MISPLACED_CONTROL ) );
                }

                // Parse the control
                control = parseControl( line.substring( "control:".length() ) );
                entry.addControl( control );
            }
            else if ( lowerLine.startsWith( "changetype:" ) )
            {
                if ( containsEntries )
                {
                    LOG.error( I18n.err( I18n.ERR_13401_CHANGE_NOT_ALLOWED, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13440_NO_CHANGE ) );
                }

                containsChanges = true;

                if ( changeTypeSeen )
                {
                    LOG.error( I18n.err( I18n.ERR_13419_CHANGETYPE_ALREADY_FOUND, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13458_MISPLACED_CHANGETYPE ) );
                }

                // A change request
                type = CHANGE;
                controlSeen = true;

                operation = parseChangeType( line );

                // Parse the change operation in a separate function
                parseChange( entry, iter, operation );
                changeTypeSeen = true;
            }
            else if ( line.indexOf( ':' ) > 0 )
            {
                if ( containsChanges )
                {
                    LOG.error( I18n.err( I18n.ERR_13401_CHANGE_NOT_ALLOWED, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13440_NO_CHANGE ) );
                }

                containsEntries = true;

                if ( controlSeen || changeTypeSeen )
                {
                    LOG.error( I18n.err( I18n.ERR_13420_AT_VALUE_NOT_ALLOWED_AFTER_CONTROL, lineNumber ) );
                    throw new LdapLdifException( I18n.err( I18n.ERR_13459_MISPLACED_ATTRIBUTETYPE ) );
                }

                parseAttributeValue( entry, line, lowerLine );
                type = LDIF_ENTRY;
            }
            else
            {
                // Invalid attribute Value
                LOG.error( I18n.err( I18n.ERR_13421_ATTRIBUTE_TYPE_EXPECTED, lineNumber ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13460_BAD_ATTRIBUTE ) );
            }
        }

        if ( type == LDIF_ENTRY )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13406_READ_ENTRY, entry ) );
            }
        }
        else if ( type == CHANGE )
        {
            entry.setChangeType( operation );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13404_READ_MODIF, entry ) );
            }
        }
        else
        {
            LOG.error( I18n.err( I18n.ERR_13422_UNKNOWN_ENTRY_TYPE, lineNumber ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13461_UNKNOWN_ENTRY ) );
        }

        return entry;
    }


    /**
     * Parse the version from the ldif input.
     *
     * @return A number representing the version (default to 1)
     * @throws LdapLdifException If the version is incorrect or if the input is incorrect
     */
    protected int parseVersion() throws LdapLdifException
    {
        int ver = DEFAULT_VERSION;

        // First, read a list of lines
        readLines();

        if ( lines.isEmpty() )
        {
            if ( LOG.isWarnEnabled() )
            {
                LOG.warn( I18n.msg( I18n.MSG_13414_LDIF_FILE_EMPTY ) );
            }
            
            return ver;
        }

        // get the first line
        String line = lines.get( 0 );

        // <ldif-file> ::= "version:" <fill> <number>
        char[] document = line.toCharArray();
        String versionNumber;

        if ( line.startsWith( "version:" ) )
        {
            position += "version:".length();
            parseFill( document );

            // Version number. Must be '1' in this version
            versionNumber = parseNumber( document );

            // We should not have any other chars after the number
            if ( position != document.length )
            {
                LOG.error( I18n.err( I18n.ERR_13423_VERSION_NOT_A_NUMBER, lineNumber ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13462_LDIF_PARSING_ERROR ) );
            }

            try
            {
                ver = Integer.parseInt( versionNumber );
            }
            catch ( NumberFormatException nfe )
            {
                LOG.error( I18n.err( I18n.ERR_13423_VERSION_NOT_A_NUMBER, lineNumber ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13462_LDIF_PARSING_ERROR ), nfe );
            }

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13400_LDIF_VERSION, versionNumber ) );
            }

            // We have found the version, just discard the line from the list
            lines.remove( 0 );

            // and read the next lines if the current buffer is empty
            if ( lines.isEmpty() )
            {
                // include the version line as part of the first entry
                int tmpEntryLen = entryLen;

                readLines();

                entryLen += tmpEntryLen;
            }
        }
        else
        {
            if ( LOG.isInfoEnabled() )
            {
                LOG.info( I18n.msg( I18n.MSG_13413_NO_VERSION_ASSUMING_1 ) );
            }
        }

        return ver;
    }


    /**
     * gets a line from the underlying data store
     *
     * @return a line of characters or null if EOF reached
     * @throws IOException on read failure
     */
    protected String getLine() throws IOException
    {
        return ( ( BufferedReader ) reader ).readLine();
    }


    /**
     * Reads an entry in a ldif buffer, and returns the resulting lines, without
     * comments, and unfolded.
     *
     * The lines represent *one* entry.
     *
     * @throws LdapLdifException If something went wrong
     */
    protected void readLines() throws LdapLdifException
    {
        String line;
        boolean insideComment = true;
        boolean isFirstLine = true;

        lines.clear();
        entryLen = 0;
        entryOffset = offset;

        StringBuilder sb = new StringBuilder();

        try
        {
            while ( ( line = getLine() ) != null )
            {
                lineNumber++;

                if ( line.length() == 0 )
                {
                    if ( isFirstLine )
                    {
                        continue;
                    }
                    else
                    {
                        // The line is empty, we have read an entry
                        insideComment = false;
                        offset++;
                        break;
                    }
                }

                // We will read the first line which is not a comment
                switch ( line.charAt( 0 ) )
                {
                    case '#':
                        insideComment = true;
                        break;

                    case ' ':
                        isFirstLine = false;

                        if ( insideComment )
                        {
                            continue;
                        }
                        else if ( sb.length() == 0 )
                        {
                            LOG.error( I18n.err( I18n.ERR_13424_EMPTY_CONTINUATION_LINE, lineNumber ) );
                            throw new LdapLdifException( I18n.err( I18n.ERR_13462_LDIF_PARSING_ERROR ) );
                        }
                        else
                        {
                            sb.append( line.substring( 1 ) );
                        }

                        insideComment = false;
                        break;

                    default:
                        isFirstLine = false;

                        // We have found a new entry
                        // First, stores the previous one if any.
                        if ( sb.length() != 0 )
                        {
                            lines.add( sb.toString() );
                        }

                        sb = new StringBuilder( line );
                        insideComment = false;
                        break;
                }

                byte[] data = Strings.getBytesUtf8( line );
                // FIXME might fail on windows in the new line issue, yet to check
                offset += ( data.length + 1 );
                entryLen += ( data.length + 1 );
            }
        }
        catch ( IOException ioe )
        {
            throw new LdapLdifException( I18n.err( I18n.ERR_13463_ERROR_WHILE_READING_LDIF_LINE ), ioe );
        }

        // Stores the current line if necessary.
        if ( sb.length() != 0 )
        {
            lines.add( sb.toString() );
        }
    }


    /**
     * Parse a ldif file (using the default encoding).
     *
     * @param fileName The ldif file
     * @return A list of entries
     * @throws LdapLdifException If the parsing fails
     */
    public List<LdifEntry> parseLdifFile( String fileName ) throws LdapLdifException
    {
        return parseLdifFile( fileName, Strings.getDefaultCharsetName() );
    }


    /**
     * Parse a ldif file, decoding it using the given charset encoding
     *
     * @param fileName The ldif file
     * @param encoding The charset encoding to use
     * @return A list of entries
     * @throws LdapLdifException If the parsing fails
     */
    public List<LdifEntry> parseLdifFile( String fileName, String encoding ) throws LdapLdifException
    {
        if ( Strings.isEmpty( fileName ) )
        {
            String msg = I18n.err( I18n.ERR_13425_EMPTY_FILE_NAME );
            LOG.error( msg );
            throw new LdapLdifException( msg );
        }

        File file = new File( fileName );

        if ( !file.exists() )
        {
            LOG.error( I18n.err( I18n.ERR_13426_CANNOT_PARSE_INEXISTANT_FILE, fileName ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13464_FILENAME_NOT_FOUND, fileName ) );
        }

        // Open the file and then get a channel from the stream
        try ( InputStream is = Files.newInputStream( Paths.get( fileName ) );
            BufferedReader bufferReader = new BufferedReader(
                new InputStreamReader( is, Charset.forName( encoding ) ) ) )
        {
            return parseLdif( bufferReader );
        }
        catch ( FileNotFoundException fnfe )
        {
            LOG.error( I18n.err( I18n.ERR_13427_CANNOT_FIND_FILE, fileName ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13464_FILENAME_NOT_FOUND, fileName ), fnfe );
        }
        catch ( LdapException le )
        {
            throw new LdapLdifException( le.getMessage(), le );
        }
        catch ( IOException ioe )
        {
            throw new LdapLdifException( ioe.getMessage(), ioe );
        }
    }


    /**
     * A method which parses a ldif string and returns a list of entries.
     *
     * @param ldif The ldif string
     * @return A list of entries, or an empty List
     * @throws LdapLdifException If something went wrong
     */
    public List<LdifEntry> parseLdif( String ldif ) throws LdapLdifException
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13407_STARTS_PARSING_LDIF ) );
        }

        if ( Strings.isEmpty( ldif ) )
        {
            return new ArrayList<>();
        }

        try ( BufferedReader bufferReader = new BufferedReader( new StringReader( ldif ) ) )
        {
            List<LdifEntry> entries = parseLdif( bufferReader );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13403_PARSED_N_ENTRIES, Integer.valueOf( entries.size() ) ) );
            }

            return entries;
        }
        catch ( LdapLdifException ne )
        {
            LOG.error( I18n.err( I18n.ERR_13428_CANNOT_PARSE_LDIF, ne.getLocalizedMessage() ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13442_ERROR_PARSING_LDIF_BUFFER ), ne );
        }
        catch ( LdapException le )
        {
            throw new LdapLdifException( le.getMessage(), le );
        }
        catch ( IOException ioe )
        {
            throw new LdapLdifException( I18n.err( I18n.ERR_13450_CANNOT_CLOSE_FILE ), ioe );
        }
    }


    // ------------------------------------------------------------------------
    // Iterator Methods
    // ------------------------------------------------------------------------
    /**
     * Gets the next LDIF on the channel.
     *
     * @return the next LDIF as a String.
     */
    private LdifEntry nextInternal()
    {
        try
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13411_NEXT_CALLED ) );
            }

            LdifEntry entry = prefetched;
            readLines();

            try
            {
                prefetched = parseEntry();
            }
            catch ( LdapLdifException ne )
            {
                error = ne;
                throw new NoSuchElementException( ne.getMessage() );
            }
            catch ( LdapException le )
            {
                throw new NoSuchElementException( le.getMessage() );
            }

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13412_NEXT_RETURNING_LDIF, entry ) );
            }

            return entry;
        }
        catch ( LdapLdifException ne )
        {
            LOG.error( I18n.err( I18n.ERR_13430_PREMATURE_LDIF_ITERATOR_TERMINATION ) );
            error = ne;
            return null;
        }
    }


    /**
     * Gets the next LDIF on the channel.
     *
     * @return the next LDIF as a String.
     */
    public LdifEntry next()
    {
        return nextInternal();
    }


    /**
     * Gets the current entry, but don't move forward.
     *
     * @return the pre-fetched entry 
     */
    public LdifEntry fetch()
    {
        return prefetched;
    }


    /**
     * Tests to see if another LDIF is on the input channel.
     *
     * @return true if another LDIF is available false otherwise.
     */
    private boolean hasNextInternal()
    {
        return null != prefetched;
    }


    /**
     * Tests to see if another LDIF is on the input channel.
     *
     * @return true if another LDIF is available false otherwise.
     */
    public boolean hasNext()
    {
        if ( LOG.isDebugEnabled() )
        {
            if ( prefetched != null )
            {
                LOG.debug( I18n.msg( I18n.MSG_13410_HAS_NEXT_TRUE ) );
            }
            else
            {
                LOG.debug( I18n.msg( I18n.MSG_13409_HAS_NEXT_FALSE ) );
            }
        }

        return hasNextInternal();
    }


    /**
     * Always throws UnsupportedOperationException!
     *
     * @see java.util.Iterator#remove()
     */
    private void removeInternal()
    {
        throw new UnsupportedOperationException();
    }


    /**
     * Always throws UnsupportedOperationException!
     *
     * @see java.util.Iterator#remove()
     */
    public void remove()
    {
        removeInternal();
    }


    /**
     * @return An iterator on the file
     */
    @Override
    public Iterator<LdifEntry> iterator()
    {
        return new Iterator<LdifEntry>()
        {
            @Override
            public boolean hasNext()
            {
                return hasNextInternal();
            }


            @Override
            public LdifEntry next()
            {
                try
                {
                    return nextInternal();
                }
                catch ( NoSuchElementException nse )
                {
                    LOG.error( nse.getMessage() );
                    return null;
                }
            }


            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }


    /**
     * @return True if an error occurred during parsing
     */
    public boolean hasError()
    {
        return error != null;
    }


    /**
     * @return The exception that occurs during an entry parsing
     */
    public Exception getError()
    {
        return error;
    }


    /**
     * The main entry point of the LdifParser. It reads a buffer and returns a
     * List of entries.
     *
     * @param reader The buffer being processed
     * @return A list of entries
     * @throws LdapException If something went wrong
     */
    public List<LdifEntry> parseLdif( BufferedReader reader ) throws LdapException
    {
        // Create a list that will contain the read entries
        List<LdifEntry> entries = new ArrayList<>();

        this.reader = reader;

        // First get the version - if any -
        version = parseVersion();
        prefetched = parseEntry();

        // When done, get the entries one by one.
        for ( LdifEntry entry : this )
        {
            if ( entry != null )
            {
                entries.add( entry );
            }
            else
            {
                throw new LdapLdifException( I18n.err( I18n.ERR_13429_ERROR_PARSING_LDIF, error.getLocalizedMessage() ) );
            }
        }

        return entries;
    }


    /**
     * @return True if the ldif file contains entries, fals if it contains changes
     */
    public boolean containsEntries()
    {
        return containsEntries;
    }


    /**
     * @return the current line that is being processed by the reader
     */
    public int getLineNumber()
    {
        return lineNumber;
    }


    /**
     * Creates a schema aware LdifEntry
     * 
     * @param schemaManager The SchemaManager
     * @return an LdifEntry that is schema aware
     */
    protected LdifEntry createLdifEntry( SchemaManager schemaManager )
    {
        if ( schemaManager != null )
        {
            return new LdifEntry( schemaManager );
        }
        else
        {
            return new LdifEntry();
        }
    }


    /**
     * @return true if the DN validation is turned on
     */
    public boolean isValidateDn()
    {
        return validateDn;
    }


    /**
     * Turns on/off the DN validation
     * 
     * @param validateDn the boolean flag
     */
    public void setValidateDn( boolean validateDn )
    {
        this.validateDn = validateDn;
    }


    /**
     * @param schemaManager the schemaManager to set
     */
    public void setSchemaManager( SchemaManager schemaManager )
    {
        this.schemaManager = schemaManager;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        if ( reader != null )
        {
            position = 0;
            reader.close();
            containsEntries = false;
            containsChanges = false;
            offset = 0;
            entryOffset = 0;
            lineNumber = 0;
        }
    }
}
