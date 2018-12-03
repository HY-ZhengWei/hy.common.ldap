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


import java.io.IOException;

import javax.naming.directory.Attributes;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.AttributeUtils;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Base64;
import org.apache.directory.api.util.Strings;


/**
 * Some LDIF helper methods.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class LdifUtils
{
    /** The array that will be used to match the first char.*/
    private static final boolean[] LDIF_SAFE_STARTING_CHAR_ALPHABET = new boolean[128];

    /** The array that will be used to match the other chars.*/
    private static final boolean[] LDIF_SAFE_OTHER_CHARS_ALPHABET = new boolean[128];

    /** The default length for a line in a ldif file */
    private static final int DEFAULT_LINE_LENGTH = 80;

    /** The file separator */
    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

    static
    {
        // Initialization of the array that will be used to match the first char.
        for ( int i = 0; i < 128; i++ )
        {
            LDIF_SAFE_STARTING_CHAR_ALPHABET[i] = true;
        }

        // 0 (NUL)
        LDIF_SAFE_STARTING_CHAR_ALPHABET[0] = false;
        // 10 (LF)
        LDIF_SAFE_STARTING_CHAR_ALPHABET[10] = false;
        // 13 (CR)
        LDIF_SAFE_STARTING_CHAR_ALPHABET[13] = false;
        // 32 (SPACE)
        LDIF_SAFE_STARTING_CHAR_ALPHABET[32] = false;
        // 58 (:)
        LDIF_SAFE_STARTING_CHAR_ALPHABET[58] = false;
        // 60 (>)
        LDIF_SAFE_STARTING_CHAR_ALPHABET[60] = false;

        // Initialization of the array that will be used to match the other chars.
        for ( int i = 0; i < 128; i++ )
        {
            LDIF_SAFE_OTHER_CHARS_ALPHABET[i] = true;
        }

        // 0 (NUL)
        LDIF_SAFE_OTHER_CHARS_ALPHABET[0] = false;
        // 10 (LF)
        LDIF_SAFE_OTHER_CHARS_ALPHABET[10] = false;
        // 13 (CR)
        LDIF_SAFE_OTHER_CHARS_ALPHABET[13] = false;
    }


    /**
     * Private constructor.
     */
    private LdifUtils()
    {
    }


    /**
     * Checks if the input String contains only safe values, that is, the data
     * does not need to be encoded for use with LDIF. The rules for checking safety
     * are based on the rules for LDIF (LDAP Data Interchange Format) per RFC 2849.
     * The data does not need to be encoded if all the following are true:
     *
     * The data cannot start with the following char values:
     * <ul>
     * <li>00 (NUL)</li>
     * <li>10 (LF)</li>
     * <li>13 (CR)</li>
     * <li>32 (SPACE)</li>
     * <li>58 (:)</li>
     * <li>60 (&lt;)</li>
     * <li>Any character with value greater than 127</li>
     * </ul>
     *
     * The data cannot contain any of the following char values:
     * <ul>
     * <li>00 (NUL)</li>
     * <li>10 (LF)</li>
     * <li>13 (CR)</li>
     * <li>Any character with value greater than 127</li>
     * </ul>
     *
     * The data cannot end with a space.
     *
     * @param str the String to be checked
     * @return true if encoding not required for LDIF
     */
    public static boolean isLDIFSafe( String str )
    {
        if ( Strings.isEmpty( str ) )
        {
            // A null string is LDIF safe
            return true;
        }

        // Checking the first char
        char currentChar = str.charAt( 0 );

        if ( ( currentChar > 127 ) || !LDIF_SAFE_STARTING_CHAR_ALPHABET[currentChar] )
        {
            return false;
        }

        // Checking the other chars
        for ( int i = 1; i < str.length(); i++ )
        {
            currentChar = str.charAt( i );

            if ( ( currentChar > 127 ) || !LDIF_SAFE_OTHER_CHARS_ALPHABET[currentChar] )
            {
                return false;
            }
        }

        // The String cannot end with a space
        return currentChar != ' ';
    }


    /**
     * Convert an Attributes as LDIF
     * 
     * @param attrs the Attributes to convert
     * @return the corresponding LDIF code as a String
     * @throws LdapException If a naming exception is encountered.
     */
    public static String convertToLdif( Attributes attrs ) throws LdapException
    {
        return convertAttributesToLdif( AttributeUtils.toEntry( attrs, null ), DEFAULT_LINE_LENGTH );
    }


    /**
     * Convert an Attributes as LDIF
     * 
     * @param attrs the Attributes to convert
     * @param length The ldif line length
     * @return the corresponding LDIF code as a String
     * @throws LdapException If a naming exception is encountered.
     */
    public static String convertToLdif( Attributes attrs, int length ) throws LdapException
    {
        return convertAttributesToLdif( AttributeUtils.toEntry( attrs, null ), length );
    }


    /**
     * Convert an Attributes as LDIF. The Dn is written.
     * 
     * @param attrs the Attributes to convert
     * @param dn The Dn for this entry
     * @param length The ldif line length
     * @return the corresponding LDIF code as a String
     * @throws LdapException If a naming exception is encountered.
     */
    public static String convertToLdif( Attributes attrs, Dn dn, int length ) throws LdapException
    {
        return convertToLdif( AttributeUtils.toEntry( attrs, dn ), length );
    }


    /**
     * Convert an Attributes as LDIF. The Dn is written.
     * 
     * @param attrs the Attributes to convert
     * @param dn The Dn for this entry
     * @return the corresponding LDIF code as a String
     * @throws LdapException If a naming exception is encountered.
     */
    public static String convertToLdif( Attributes attrs, Dn dn ) throws LdapException
    {
        return convertToLdif( AttributeUtils.toEntry( attrs, dn ), DEFAULT_LINE_LENGTH );
    }


    /**
     * Convert an Entry to LDIF
     * 
     * @param entry the Entry to convert
     * @return the corresponding LDIF code as a String
     */
    public static String convertToLdif( Entry entry )
    {
        return convertToLdif( entry, DEFAULT_LINE_LENGTH );
    }


    /**
     * Convert an Entry to LDIF including a version number at the top
     * 
     * @param entry the Entry to convert
     * @param includeVersionInfo flag to tell whether to include version number or not
     * @return the corresponding LDIF code as a String
     */
    public static String convertToLdif( Entry entry, boolean includeVersionInfo )
    {
        String ldif = convertToLdif( entry, DEFAULT_LINE_LENGTH );

        if ( includeVersionInfo )
        {
            ldif = "version: 1" + LINE_SEPARATOR + ldif;
        }

        return ldif;
    }


    /**
     * Convert all the Entry's attributes to LDIF. The Dn is not written
     * 
     * @param entry the Entry to convert
     * @return the corresponding LDIF code as a String
     */
    public static String convertAttributesToLdif( Entry entry )
    {
        return convertAttributesToLdif( entry, DEFAULT_LINE_LENGTH );
    }


    /**
     * Convert a LDIF String to a JNDI attributes.
     *
     * @param ldif The LDIF string containing an attribute value
     * @return An Attributes instance
     * @exception LdapLdifException If the LDIF String cannot be converted to an Attributes
     */
    public static Attributes getJndiAttributesFromLdif( String ldif ) throws LdapLdifException
    {
        try ( LdifAttributesReader reader = new LdifAttributesReader() )
        {
            return AttributeUtils.toAttributes( reader.parseEntry( ldif ) );
        }
        catch ( IOException ioe )
        {
            throw new LdapLdifException( ioe.getMessage(), ioe );
        }
    }


    /**
     * Convert an Entry as LDIF
     * 
     * @param entry the Entry to convert
     * @param length the expected line length
     * @return the corresponding LDIF code as a String
     */
    public static String convertToLdif( Entry entry, int length )
    {
        StringBuilder sb = new StringBuilder();

        if ( entry.getDn() != null )
        {
            // First, dump the Dn
            if ( isLDIFSafe( entry.getDn().getName() ) )
            {
                sb.append( stripLineToNChars( "dn: " + entry.getDn().getName(), length ) );
            }
            else
            {
                sb.append( stripLineToNChars( "dn:: " + encodeBase64( entry.getDn().getName() ), length ) );
            }

            sb.append( '\n' );
        }

        // Then all the attributes
        for ( Attribute attribute : entry )
        {
            sb.append( convertToLdif( attribute, length ) );
        }

        return sb.toString();
    }


    /**
     * Convert the Entry's attributes to LDIF. The Dn is not written.
     * 
     * @param entry the Entry to convert
     * @param length the expected line length
     * @return the corresponding LDIF code as a String
     */
    public static String convertAttributesToLdif( Entry entry, int length )
    {
        StringBuilder sb = new StringBuilder();

        // Then all the attributes
        for ( Attribute attribute : entry )
        {
            sb.append( convertToLdif( attribute, length ) );
        }

        return sb.toString();
    }


    /**
     * Convert an LdifEntry to LDIF
     * 
     * @param entry the LdifEntry to convert
     * @return the corresponding LDIF as a String
     * @throws LdapException If a naming exception is encountered.
     */
    public static String convertToLdif( LdifEntry entry ) throws LdapException
    {
        return convertToLdif( entry, DEFAULT_LINE_LENGTH );
    }


    /**
     * Convert an LdifEntry to LDIF
     * 
     * @param entry the LdifEntry to convert
     * @param length The maximum line's length
     * @return the corresponding LDIF as a String
     * @throws LdapException If a naming exception is encountered.
     */
    public static String convertToLdif( LdifEntry entry, int length ) throws LdapException
    {
        StringBuilder sb = new StringBuilder();

        // First, dump the Dn
        if ( isLDIFSafe( entry.getDn().getName() ) )
        {
            sb.append( stripLineToNChars( "dn: " + entry.getDn(), length ) );
        }
        else
        {
            sb.append( stripLineToNChars( "dn:: " + encodeBase64( entry.getDn().getName() ), length ) );
        }

        sb.append( '\n' );

        // Dump the ChangeType
        String changeType = Strings.toLowerCaseAscii( entry.getChangeType().toString() );

        if ( entry.getChangeType() != ChangeType.None )
        {
            // First dump the controls if any
            if ( entry.hasControls() )
            {
                for ( LdifControl control : entry.getControls().values() )
                {
                    StringBuilder controlStr = new StringBuilder();

                    controlStr.append( "control: " ).append( control.getOid() );
                    controlStr.append( " " ).append( control.isCritical() );

                    if ( control.hasValue() )
                    {
                        controlStr.append( "::" ).append( Base64.encode( control.getValue() ) );
                    }

                    sb.append( stripLineToNChars( controlStr.toString(), length ) );
                    sb.append( '\n' );
                }
            }

            sb.append( stripLineToNChars( "changetype: " + changeType, length ) );
            sb.append( '\n' );
        }

        switch ( entry.getChangeType() )
        {
            case None:
                if ( entry.hasControls() )
                {
                    sb.append( stripLineToNChars( "changetype: " + ChangeType.Add, length ) );
                }

                // Fallthrough

            case Add:
                if ( entry.getEntry() == null )
                {
                    throw new LdapException( I18n.err( I18n.ERR_13472_ENTRY_WITH_NO_ATTRIBUTE ) );
                }

                // Now, iterate through all the attributes
                for ( Attribute attribute : entry.getEntry() )
                {
                    sb.append( convertToLdif( attribute, length ) );
                }

                break;

            case Delete:
                if ( entry.getEntry() != null )
                {
                    throw new LdapException( I18n.err( I18n.ERR_13471_DELETED_ENTRY_WITH_ATTRIBUTES ) );
                }

                break;

            case ModDn:
            case ModRdn:
                if ( entry.getEntry() != null )
                {
                    throw new LdapException( I18n.err( I18n.ERR_13473_MODDN_WITH_ATTRIBUTES ) );
                }

                // Stores the new Rdn
                Attribute newRdn = new DefaultAttribute( "newrdn", entry.getNewRdn() );
                sb.append( convertToLdif( newRdn, length ) );

                // Stores the deleteoldrdn flag
                sb.append( "deleteoldrdn: " );

                if ( entry.isDeleteOldRdn() )
                {
                    sb.append( "1" );
                }
                else
                {
                    sb.append( "0" );
                }

                sb.append( '\n' );

                // Stores the optional newSuperior
                if ( !Strings.isEmpty( entry.getNewSuperior() ) )
                {
                    Attribute newSuperior = new DefaultAttribute( "newsuperior", entry.getNewSuperior() );
                    sb.append( convertToLdif( newSuperior, length ) );
                }

                break;

            case Modify:
                boolean isFirst = true;
                
                for ( Modification modification : entry.getModifications() )
                {
                    
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        sb.append( "-\n" );
                    }

                    switch ( modification.getOperation() )
                    {
                        case ADD_ATTRIBUTE:
                            sb.append( "add: " );
                            break;

                        case REMOVE_ATTRIBUTE:
                            sb.append( "delete: " );
                            break;

                        case REPLACE_ATTRIBUTE:
                            sb.append( "replace: " );
                            break;

                        default:
                            throw new IllegalArgumentException( I18n.err( I18n.ERR_13434_UNEXPECTED_MOD_OPERATION, modification.getOperation() ) );
                    }

                    sb.append( modification.getAttribute().getUpId() );
                    sb.append( '\n' );

                    sb.append( convertToLdif( modification.getAttribute(), length ) );
                }

                sb.append( '-' );
                break;

            default:
                throw new IllegalArgumentException( I18n.err( I18n.ERR_13431_UNEXPECTED_CHANGETYPE, entry.getChangeType() ) );
        }

        sb.append( '\n' );

        return sb.toString();
    }


    /**
     * Base64 encode a String
     * 
     * @param str The string to encode
     * @return the base 64 encoded string
     */
    private static String encodeBase64( String str )
    {
        // force encoding using UTF-8 charset, as required in RFC2849 note 7
        return new String( Base64.encode( Strings.getBytesUtf8( str ) ) );
    }


    /**
     * Converts an EntryAttribute to LDIF
     * 
     * @param attr the EntryAttribute to convert
     * @return the corresponding LDIF code as a String
     */
    public static String convertToLdif( Attribute attr )
    {
        return convertToLdif( attr, DEFAULT_LINE_LENGTH );
    }


    /**
     * Converts an EntryAttribute as LDIF
     * 
     * @param attr the EntryAttribute to convert
     * @param length the expected line length
     * @return the corresponding LDIF code as a String
     */
    public static String convertToLdif( Attribute attr, int length )
    {
        StringBuilder sb = new StringBuilder();
        
        if ( attr.size() == 0 )
        {
            // Special case : we don't have any value
            return "";
        }

        for ( Value value : attr )
        {
            StringBuilder lineBuffer = new StringBuilder();

            lineBuffer.append( attr.getUpId() );

            // First, deal with null value (which is valid)
            if ( value.isNull() )
            {
                lineBuffer.append( ':' );
            }
            else if ( value.isHumanReadable() )
            {
                // It's a String but, we have to check if encoding isn't required
                String str = value.getValue();

                if ( !LdifUtils.isLDIFSafe( str ) )
                {
                    lineBuffer.append( ":: " ).append( encodeBase64( str ) );
                }
                else
                {
                    lineBuffer.append( ':' );

                    if ( str != null )
                    {
                        lineBuffer.append( ' ' ).append( str );
                    }
                }
            }
            else
            {
                // It is binary, so we have to encode it using Base64 before adding it
                char[] encoded = Base64.encode( value.getBytes() );

                lineBuffer.append( ":: " + new String( encoded ) );
            }

            lineBuffer.append( '\n' );
            sb.append( stripLineToNChars( lineBuffer.toString(), length ) );
        }

        return sb.toString();
    }


    /**
     * Strips the String every n specified characters
     * 
     * @param str the string to strip
     * @param nbChars the number of characters
     * @return the stripped String
     */
    public static String stripLineToNChars( String str, int nbChars )
    {
        int strLength = str.length();

        if ( strLength <= nbChars )
        {
            return str;
        }

        if ( nbChars < 2 )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13474_LINE_LENGTH_TOO_SHORT ) );
        }

        // We will first compute the new size of the LDIF result
        // It's at least nbChars chars plus one for \n
        int charsPerLine = nbChars - 1;

        int remaining = ( strLength - nbChars ) % charsPerLine;

        int nbLines = 1 + ( ( strLength - nbChars ) / charsPerLine ) + ( remaining == 0 ? 0 : 1 );

        int nbCharsTotal = strLength + nbLines + nbLines - 2;

        char[] buffer = new char[nbCharsTotal];
        char[] orig = str.toCharArray();

        int posSrc = 0;
        int posDst = 0;

        System.arraycopy( orig, posSrc, buffer, posDst, nbChars );
        posSrc += nbChars;
        posDst += nbChars;

        for ( int i = 0; i < nbLines - 2; i++ )
        {
            buffer[posDst++] = '\n';
            buffer[posDst++] = ' ';

            System.arraycopy( orig, posSrc, buffer, posDst, charsPerLine );
            posSrc += charsPerLine;
            posDst += charsPerLine;
        }

        buffer[posDst++] = '\n';
        buffer[posDst++] = ' ';
        System.arraycopy( orig, posSrc, buffer, posDst, remaining == 0 ? charsPerLine : remaining );

        return new String( buffer );
    }


    /**
     * Build a new Attributes instance from a LDIF list of lines. The values can be
     * either a complete Ava, or a couple of AttributeType ID and a value (a String or
     * a byte[]). The following sample shows the three cases :
     *
     * <pre>
     * Attribute attr = AttributeUtils.createAttributes(
     *     "objectclass: top",
     *     "cn", "My name",
     *     "jpegPhoto", new byte[]{0x01, 0x02} );
     * </pre>
     *
     * @param avas The AttributeType and Values, using a ldif format, or a couple of
     * Attribute ID/Value
     * @return An Attributes instance
     * @throws LdapException If the data are invalid
     */
    public static Attributes createJndiAttributes( Object... avas ) throws LdapException
    {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        boolean valueExpected = false;

        for ( Object ava : avas )
        {
            if ( !valueExpected )
            {
                if ( !( ava instanceof String ) )
                {
                    throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n.err(
                        I18n.ERR_13233_ATTRIBUTE_ID_MUST_BE_A_STRING, pos + 1 ) );
                }

                String attribute = ( String ) ava;
                sb.append( attribute );

                if ( attribute.indexOf( ':' ) != -1 )
                {
                    sb.append( '\n' );
                }
                else
                {
                    valueExpected = true;
                }
            }
            else
            {
                if ( ava instanceof String )
                {
                    sb.append( ": " ).append( ( String ) ava ).append( '\n' );
                }
                else if ( ava instanceof byte[] )
                {
                    sb.append( ":: " );
                    sb.append( new String( Base64.encode( ( byte[] ) ava ) ) );
                    sb.append( '\n' );
                }
                else
                {
                    throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n.err(
                        I18n.ERR_13234_ATTRIBUTE_VAL_STRING_OR_BYTE, pos + 1 ) );
                }

                valueExpected = false;
            }
        }

        if ( valueExpected )
        {
            throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n
                .err( I18n.ERR_13234_ATTRIBUTE_VAL_STRING_OR_BYTE ) );
        }

        try ( LdifAttributesReader reader = new LdifAttributesReader() ) 
        {
            return AttributeUtils.toAttributes( reader.parseEntry( sb.toString() ) );
        }
        catch ( IOException ioe )
        {
            throw new LdapLdifException( ioe.getMessage(), ioe );
        }
    }
}
