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


import java.text.ParseException;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;
import org.apache.directory.api.ldap.model.schema.parsers.AttributeTypeDescriptionSchemaParser;
import org.apache.directory.api.util.Strings;


/**
 * A SyntaxChecker which verifies that a value follows the
 * attribute type descripton syntax according to RFC 4512, par 4.2.2:
 * 
*  <pre>
 * AttributeTypeDescription = LPAREN WSP
 *     numericoid                    ; object identifier
 *     [ SP "NAME" SP qdescrs ]      ; short names (descriptors)
 *     [ SP "DESC" SP qdstring ]     ; description
 *     [ SP "OBSOLETE" ]             ; not active
 *     [ SP "SUP" SP oid ]           ; supertype
 *     [ SP "EQUALITY" SP oid ]      ; equality matching rule
 *     [ SP "ORDERING" SP oid ]      ; ordering matching rule
 *     [ SP "SUBSTR" SP oid ]        ; substrings matching rule
 *     [ SP "SYNTAX" SP noidlen ]    ; value syntax
 *     [ SP "SINGLE-VALUE" ]         ; single-value
 *     [ SP "COLLECTIVE" ]           ; collective
 *     [ SP "NO-USER-MODIFICATION" ] ; not user modifiable
 *     [ SP "USAGE" SP usage ]       ; usage
 *     extensions WSP RPAREN         ; extensions
 * 
 * usage = "userApplications"     /  ; user
 *         "directoryOperation"   /  ; directory operational
 *         "distributedOperation" /  ; DSA-shared operational
 *         "dSAOperation"            ; DSA-specific operational     
 * 
 * extensions = *( SP xstring SP qdstrings )
 * xstring = "X" HYPHEN 1*( ALPHA / HYPHEN / USCORE ) 
 * 
 * Each attribute type description must contain at least one of the SUP
 * or SYNTAX fields. 
 * 
 * COLLECTIVE requires usage userApplications.
 * 
 * NO-USER-MODIFICATION requires an operational usage.
 * </pre>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class AttributeTypeDescriptionSyntaxChecker extends SyntaxChecker
{
    /** The schema parser used to parse the AttributeTypeDescription Syntax */
    private transient AttributeTypeDescriptionSchemaParser schemaParser = new AttributeTypeDescriptionSchemaParser();
    
    /**
     * A static instance of AttributeTypeDescriptionSyntaxChecker
     */
    public static final AttributeTypeDescriptionSyntaxChecker INSTANCE = new AttributeTypeDescriptionSyntaxChecker( 
        SchemaConstants.ATTRIBUTE_TYPE_DESCRIPTION_SYNTAX );

    /**
     * A static Builder for this class
     */
    public static final class Builder extends SCBuilder<AttributeTypeDescriptionSyntaxChecker>
    {
        /**
         * The Builder constructor
         */
        private Builder()
        {
            super( SchemaConstants.ATTRIBUTE_TYPE_DESCRIPTION_SYNTAX );
        }
        
        
        /**
         * Create a new instance of AttributeTypeDescriptionSyntaxChecker
         * @return A new instance of AttributeTypeDescriptionSyntaxChecker
         */
        @Override
        public AttributeTypeDescriptionSyntaxChecker build()
        {
            return new AttributeTypeDescriptionSyntaxChecker( oid );
        }
    }


    /**
     * Creates a new instance of AttributeTypeDescriptionSchemaParser.
     * 
     * @param oid The OID to use for this SyntaxChecker
     *
     */
    private AttributeTypeDescriptionSyntaxChecker( String oid )
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

        try
        {
            schemaParser.parse( strValue );
            
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13701_SYNTAX_VALID, value ) );
            }
            
            return true;
        }
        catch ( ParseException pe )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
            }
            
            return false;
        }
    }
}
