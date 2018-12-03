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
package org.apache.directory.api.ldap.model.schema.syntaxCheckers;


import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;
import org.apache.directory.api.util.Strings;


/**
 * A SyntaxChecker which verifies that a value is a Printable String according to RFC 4517.
 * <p>
 * From RFC 4517 :
 * <pre>
 * PrintableString    = 1*PrintableCharacter
 * PrintableCharacter = ALPHA | DIGIT | SQUOTE | LPAREN | RPAREN |
 *                          PLUS | COMMA | HYPHEN | DOT | EQUALS |
 *                          SLASH | COLON | QUESTION | SPACE
 *                          
 * SLASH   = %x2F                ; forward slash ("/")
 * COLON   = %x3A                ; colon (":")
 * QUESTION= %x3F                ; question mark ("?")
 * </pre>
 * From RFC 4512 :
 * <pre>
 * ALPHA   = %x41-5A | %x61-7A   ; "A"-"Z" / "a"-"z"
 * DIGIT   = %x30 | LDIGIT       ; "0"-"9"
 * LDIGIT  = %x31-39             ; "1"-"9"
 * SQUOTE  = %x27                ; single quote ("'")
 * LPAREN  = %x28                ; left paren ("(")
 * RPAREN  = %x29                ; right paren (")")
 * PLUS    = %x2B                ; plus sign ("+")
 * COMMA   = %x2C                ; comma (",")
 * HYPHEN  = %x2D                ; hyphen ("-")
 * DOT     = %x2E                ; period (".")
 * EQUALS  = %x3D                ; equals sign ("=")
 * SPACE   = %x20                ; space (" ")
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class PrintableStringSyntaxChecker extends SyntaxChecker
{
    /**
     * A static instance of PrintableStringSyntaxChecker
     */
    public static final PrintableStringSyntaxChecker INSTANCE = 
        new PrintableStringSyntaxChecker( SchemaConstants.PRINTABLE_STRING_SYNTAX );
    
    /**
     * A static Builder for this class
     */
    public static final class Builder extends SCBuilder<PrintableStringSyntaxChecker>
    {
        /**
         * The Builder constructor
         */
        private Builder()
        {
            super( SchemaConstants.PRINTABLE_STRING_SYNTAX );
        }
        
        
        /**
         * Create a new instance of PrintableStringSyntaxChecker
         * @return A new instance of PrintableStringSyntaxChecker
         */
        @Override
        public PrintableStringSyntaxChecker build()
        {
            return new PrintableStringSyntaxChecker( oid );
        }
    }

    
    /**
     * Creates a new instance of PrintableStringSyntaxChecker.
     * 
     * @param oid The OID to use for this SyntaxChecker
     */
    private PrintableStringSyntaxChecker( String oid )
    {
        super( oid );
    }

    
    /**
     * @return An instance of the Builder for this class
     */
    public static Builder builder()
    {
        return new Builder();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidSyntax( Object value )
    {
        String strValue;

        if ( value == null )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, "null" ) );
            }
            
            return false;
        }

        if ( value instanceof String )
        {
            strValue = ( String ) value;
        }
        else if ( value instanceof byte[] )
        {
            strValue = Strings.utf8ToString( ( byte[] ) value );
        }
        else
        {
            strValue = value.toString();
        }

        if ( strValue.length() == 0 )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
            }
            
            return false;
        }

        // We must have at least one char
        if ( strValue.length() == 0 )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
            }
            
            return false;
        }

        boolean result = Strings.isPrintableString( strValue );

        if ( LOG.isDebugEnabled() )
        {
            if ( result )
            {
                LOG.debug( I18n.msg( I18n.MSG_13701_SYNTAX_VALID, value ) );
            }
            else
            {
                LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
            }
        }

        return result;
    }
}
