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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <pre>
 *  &lt;ldif-file&gt; ::= &quot;version:&quot; &lt;fill&gt; &lt;number&gt; &lt;seps&gt; &lt;dn-spec&gt; &lt;sep&gt;
 *  &lt;ldif-content-change&gt;
 *
 *  &lt;ldif-content-change&gt; ::=
 *    &lt;number&gt; &lt;oid&gt; &lt;options-e&gt; &lt;value-spec&gt; &lt;sep&gt; &lt;attrval-specs-e&gt;
 *    &lt;ldif-attrval-record-e&gt; |
 *    &lt;alpha&gt; &lt;chars-e&gt; &lt;options-e&gt; &lt;value-spec&gt; &lt;sep&gt; &lt;attrval-specs-e&gt;
 *    &lt;ldif-attrval-record-e&gt; |
 *    &quot;control:&quot; &lt;fill&gt; &lt;number&gt; &lt;oid&gt; &lt;spaces-e&gt; &lt;criticality&gt;
 *    &lt;value-spec-e&gt; &lt;sep&gt; &lt;controls-e&gt;
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
 *  &lt;attrval-specs-e&gt; ::= &lt;number&gt; &lt;oid&gt; &lt;options-e&gt; &lt;value-spec&gt; &lt;sep&gt;
 *  &lt;attrval-specs-e&gt; |
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
 *  &lt;changerecord-type&gt; ::= &quot;add&quot; &lt;sep&gt; &lt;attributeType&gt; &lt;options-e&gt; &lt;value-spec&gt;
 *  &lt;sep&gt; &lt;attrval-specs-e&gt; |
 *    &quot;delete&quot; &lt;sep&gt; |
 *    &quot;modify&quot; &lt;sep&gt; &lt;mod-type&gt; &lt;fill&gt; &lt;attributeType&gt; &lt;options-e&gt; &lt;sep&gt;
 *    &lt;attrval-specs-e&gt; &lt;sep&gt; '-' &lt;sep&gt; &lt;mod-specs-e&gt; |
 *    &quot;moddn&quot; &lt;sep&gt; &lt;newrdn&gt; &lt;sep&gt; &quot;deleteoldrdn:&quot; &lt;fill&gt; &lt;0-1&gt; &lt;sep&gt;
 *    &lt;newsuperior-e&gt; &lt;sep&gt; |
 *    &quot;modrdn&quot; &lt;sep&gt; &lt;newrdn&gt; &lt;sep&gt; &quot;deleteoldrdn:&quot; &lt;fill&gt; &lt;0-1&gt; &lt;sep&gt;
 *    &lt;newsuperior-e&gt; &lt;sep&gt;
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
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifAttributesReader extends LdifReader
{
    /** A logger */
    private static final Logger LOG = LoggerFactory.getLogger( LdifAttributesReader.class );


    /**
     * Constructors
     */
    public LdifAttributesReader()
    {
        lines = new ArrayList<String>();
        position = 0;
        version = DEFAULT_VERSION;
    }


    /**
     * Parse an AttributeType/AttributeValue
     *
     * @param attributes The entry where to store the value
     * @param line The line to parse
     * @param lowerLine The same line, lowercased
     * @throws LdapLdifException If anything goes wrong
     */
    private void parseAttribute( Attributes attributes, String line, String lowerLine ) throws LdapLdifException
    {
        int colonIndex = line.indexOf( ':' );

        String attributeType = lowerLine.substring( 0, colonIndex );

        // We should *not* have a Dn twice
        if ( "dn".equals( attributeType ) )
        {
            LOG.error( I18n.err( I18n.ERR_13400_ENTRY_WITH_TWO_DNS ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13439_LDIF_ENTRY_WITH_TWO_DNS ) );
        }

        Object attributeValue = parseValue( attributeType, line, colonIndex );

        // Update the entry
        javax.naming.directory.Attribute attribute = attributes.get( attributeType );

        if ( attribute == null )
        {
            attributes.put( attributeType, attributeValue );
        }
        else
        {
            attribute.add( attributeValue );
        }
    }


    /**
     * Parse an AttributeType/AttributeValue
     *
     * @param schemaManager The SchemaManager
     * @param entry The entry where to store the value
     * @param line The line to parse
     * @param lowerLine The same line, lowercased
     * @throws LdapLdifException If anything goes wrong
     */
    private void parseEntryAttribute( SchemaManager schemaManager, Entry entry, String line, String lowerLine )
        throws LdapLdifException
    {
        int colonIndex = line.indexOf( ':' );

        String attributeName = lowerLine.substring( 0, colonIndex );
        AttributeType attributeType = null;

        // We should *not* have a Dn twice
        if ( "dn".equals( attributeName ) )
        {
            LOG.error( I18n.err( I18n.ERR_13400_ENTRY_WITH_TWO_DNS ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13439_LDIF_ENTRY_WITH_TWO_DNS ) );
        }

        if ( schemaManager != null )
        {
            attributeType = schemaManager.getAttributeType( attributeName );

            if ( attributeType == null )
            {
                String msg = I18n.err( I18n.ERR_13475_UNKNOWN_ATTRIBUTETYPE,  attributeName );
                LOG.error( msg );
                throw new LdapLdifException( msg );
            }
        }

        Object attributeValue = parseValue( attributeName, line, colonIndex );

        // Update the entry
        Attribute attribute;

        if ( schemaManager == null )
        {
            attribute = entry.get( attributeName );
        }
        else
        {
            attribute = entry.get( attributeType );
        }

        if ( attribute == null )
        {
            if ( schemaManager == null )
            {
                if ( attributeValue instanceof String )
                {
                    entry.put( attributeName, ( String ) attributeValue );
                }
                else
                {
                    entry.put( attributeName, ( byte[] ) attributeValue );
                }
            }
            else
            {
                try
                {
                    if ( attributeValue instanceof String )
                    {
                        entry.put( attributeName, attributeType, ( String ) attributeValue );
                    }
                    else
                    {
                        entry.put( attributeName, attributeType, ( byte[] ) attributeValue );
                    }
                }
                catch ( LdapException le )
                {
                    throw new LdapLdifException( I18n.err( I18n.ERR_13460_BAD_ATTRIBUTE ), le );
                }
            }
        }
        else
        {
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
        }
    }


    /**
     * Parse a ldif file. The following rules are processed :
     *
     * &lt;ldif-file&gt; ::= &lt;ldif-attrval-record&gt; &lt;ldif-attrval-records&gt; |
     * &lt;ldif-change-record&gt; &lt;ldif-change-records&gt; &lt;ldif-attrval-record&gt; ::=
     * &lt;dn-spec&gt; &lt;sep&gt; &lt;attrval-spec&gt; &lt;attrval-specs&gt; &lt;ldif-change-record&gt; ::=
     * &lt;dn-spec&gt; &lt;sep&gt; &lt;controls-e&gt; &lt;changerecord&gt; &lt;dn-spec&gt; ::= "dn:" &lt;fill&gt;
     * &lt;distinguishedName&gt; | "dn::" &lt;fill&gt; &lt;base64-distinguishedName&gt;
     * &lt;changerecord&gt; ::= "changetype:" &lt;fill&gt; &lt;change-op&gt;
     *
     * @param schemaManager The SchemaManager
     * @return The read entry
     * @throws LdapLdifException If the entry can't be read or is invalid
     */
    private Entry parseEntry( SchemaManager schemaManager ) throws LdapLdifException
    {
        if ( ( lines == null ) || lines.isEmpty() )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13408_END_OF_LDIF ) );
            }
            
            return null;
        }

        Entry entry = new DefaultEntry( schemaManager );

        // Now, let's iterate through the other lines
        for ( String line : lines )
        {
            // Each line could start either with an OID, an attribute type, with
            // "control:" or with "changetype:"
            String lowerLine = Strings.toLowerCaseAscii( line );

            // We have three cases :
            // 1) The first line after the Dn is a "control:" -> this is an error
            // 2) The first line after the Dn is a "changeType:" -> this is an error
            // 3) The first line after the Dn is anything else
            if ( lowerLine.startsWith( "control:" ) )
            {
                LOG.error( I18n.err( I18n.ERR_13401_CHANGE_NOT_ALLOWED ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13440_NO_CHANGE ) );
            }
            else if ( lowerLine.startsWith( "changetype:" ) )
            {
                LOG.error( I18n.err( I18n.ERR_13401_CHANGE_NOT_ALLOWED ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13440_NO_CHANGE ) );
            }
            else if ( line.indexOf( ':' ) > 0 )
            {
                parseEntryAttribute( schemaManager, entry, line, lowerLine );
            }
            else
            {
                // Invalid attribute Value
                LOG.error( I18n.err( I18n.ERR_13402_EXPECTING_ATTRIBUTE_TYPE ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13441_BAD_ATTRIBUTE ) );
            }
        }

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13405_READ_ATTR, entry ) );
        }

        return entry;
    }


    /**
     * Parse a ldif file. The following rules are processed :
     *
     * <pre>
     * &lt;ldif-file&gt; ::= &lt;ldif-attrval-record&gt; &lt;ldif-attrval-records&gt; |
     * &lt;ldif-change-record&gt; &lt;ldif-change-records&gt; &lt;ldif-attrval-record&gt; ::=
     * &lt;dn-spec&gt; &lt;sep&gt; &lt;attrval-spec&gt; &lt;attrval-specs&gt; &lt;ldif-change-record&gt; ::=
     * &lt;dn-spec&gt; &lt;sep&gt; &lt;controls-e&gt; &lt;changerecord&gt; &lt;dn-spec&gt; ::= "dn:" &lt;fill&gt;
     * &lt;distinguishedName&gt; | "dn::" &lt;fill&gt; &lt;base64-distinguishedName&gt;
     * &lt;changerecord&gt; ::= "changetype:" &lt;fill&gt; &lt;change-op&gt;
     * </pre>
     * 
     * @return The read entry
     * @throws LdapLdifException If the entry can't be read or is invalid
     */
    private Attributes parseAttributes() throws LdapLdifException
    {
        if ( ( lines == null ) || lines.isEmpty() )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13408_END_OF_LDIF ) );
            }

            return null;
        }

        Attributes attributes = new BasicAttributes( true );

        // Now, let's iterate through the other lines
        for ( String line : lines )
        {
            // Each line could start either with an OID, an attribute type, with
            // "control:" or with "changetype:"
            String lowerLine = Strings.toLowerCaseAscii( line );

            // We have three cases :
            // 1) The first line after the Dn is a "control:" -> this is an error
            // 2) The first line after the Dn is a "changeType:" -> this is an error
            // 3) The first line after the Dn is anything else
            if ( lowerLine.startsWith( "control:" ) )
            {
                LOG.error( I18n.err( I18n.ERR_13401_CHANGE_NOT_ALLOWED ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13440_NO_CHANGE ) );
            }
            else if ( lowerLine.startsWith( "changetype:" ) )
            {
                LOG.error( I18n.err( I18n.ERR_13401_CHANGE_NOT_ALLOWED ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13440_NO_CHANGE ) );
            }
            else if ( line.indexOf( ':' ) > 0 )
            {
                parseAttribute( attributes, line, lowerLine );
            }
            else
            {
                // Invalid attribute Value
                LOG.error( I18n.err( I18n.ERR_13402_EXPECTING_ATTRIBUTE_TYPE ) );
                throw new LdapLdifException( I18n.err( I18n.ERR_13441_BAD_ATTRIBUTE ) );
            }
        }

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13405_READ_ATTR, attributes ) );
        }

        return attributes;
    }


    /**
     * A method which parses a ldif string and returns a list of Attributes.
     *
     * @param ldif The ldif string
     * @return A list of Attributes, or an empty List
     * @throws LdapLdifException If something went wrong
     */
    public Attributes parseAttributes( String ldif ) throws LdapLdifException
    {
        lines = new ArrayList<String>();
        position = 0;

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13407_STARTS_PARSING_LDIF ) );
        }

        if ( Strings.isEmpty( ldif ) )
        {
            return new BasicAttributes( true );
        }

        StringReader strIn = new StringReader( ldif );
        reader = new BufferedReader( strIn );

        try
        {
            readLines();

            Attributes attributes = parseAttributes();

            if ( LOG.isDebugEnabled() )
            {
                if ( attributes == null )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13401_PARSED_NO_ENTRY ) );
                }
                else
                {
                    LOG.debug( I18n.msg( I18n.MSG_13402_PARSED_ONE_ENTRY ) );
                }
            }

            return attributes;
        }
        catch ( LdapLdifException ne )
        {
            LOG.error( I18n.err( I18n.ERR_13403_CANNOT_PARSE_LDIF_BUFFER, ne.getLocalizedMessage() ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13442_ERROR_PARSING_LDIF_BUFFER ), ne );
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch ( IOException ioe )
            {
                throw new LdapLdifException( I18n.err( I18n.ERR_13450_CANNOT_CLOSE_FILE ), ioe );
            }
        }
    }


    /**
     * A method which parses a ldif string and returns an Entry.
     *
     * @param ldif The ldif string
     * @return An entry
     * @throws LdapLdifException If something went wrong
     */
    public Entry parseEntry( String ldif ) throws LdapLdifException
    {
        lines = new ArrayList<String>();
        position = 0;

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13407_STARTS_PARSING_LDIF ) );
        }

        if ( Strings.isEmpty( ldif ) )
        {
            return new DefaultEntry();
        }

        StringReader strIn = new StringReader( ldif );
        reader = new BufferedReader( strIn );

        try
        {
            readLines();

            Entry entry = parseEntry( ( SchemaManager ) null );

            if ( LOG.isDebugEnabled() )
            {
                if ( entry == null )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13401_PARSED_NO_ENTRY ) );
                }
                else
                {
                    LOG.debug( I18n.msg( I18n.MSG_13402_PARSED_ONE_ENTRY ) );
                }
            }

            return entry;
        }
        catch ( LdapLdifException ne )
        {
            LOG.error( I18n.err( I18n.ERR_13403_CANNOT_PARSE_LDIF_BUFFER, ne.getLocalizedMessage() ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13442_ERROR_PARSING_LDIF_BUFFER ), ne );
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch ( IOException ioe )
            {
                throw new LdapLdifException( I18n.err( I18n.ERR_13450_CANNOT_CLOSE_FILE ), ioe );
            }
        }
    }


    /**
     * A method which parses a ldif string and returns an Entry.
     *
     * @param schemaManager The SchemaManager
     * @param ldif The ldif string
     * @return An entry
     * @throws LdapLdifException If something went wrong
     */
    public Entry parseEntry( SchemaManager schemaManager, String ldif ) throws LdapLdifException
    {
        lines = new ArrayList<String>();
        position = 0;

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13407_STARTS_PARSING_LDIF ) );
        }

        if ( Strings.isEmpty( ldif ) )
        {
            return new DefaultEntry( schemaManager );
        }

        StringReader strIn = new StringReader( ldif );
        reader = new BufferedReader( strIn );

        try
        {
            readLines();

            Entry entry = parseEntry( schemaManager );

            if ( LOG.isDebugEnabled() )
            {
                if ( entry == null )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13401_PARSED_NO_ENTRY ) );
                }
                else
                {
                    LOG.debug( I18n.msg( I18n.MSG_13402_PARSED_ONE_ENTRY ) );
                }

            }

            return entry;
        }
        catch ( LdapLdifException ne )
        {
            LOG.error( I18n.err( I18n.ERR_13403_CANNOT_PARSE_LDIF_BUFFER, ne.getLocalizedMessage() ) );
            throw new LdapLdifException( I18n.err( I18n.ERR_13442_ERROR_PARSING_LDIF_BUFFER ), ne );
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch ( IOException ioe )
            {
                throw new LdapLdifException( I18n.err( I18n.ERR_13450_CANNOT_CLOSE_FILE ), ioe );
            }
        }
    }
}