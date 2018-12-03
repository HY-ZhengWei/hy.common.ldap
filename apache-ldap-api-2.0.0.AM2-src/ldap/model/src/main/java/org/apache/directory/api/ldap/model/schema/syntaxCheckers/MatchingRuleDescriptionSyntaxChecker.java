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
import org.apache.directory.api.ldap.model.schema.parsers.MatchingRuleDescriptionSchemaParser;
import org.apache.directory.api.util.Strings;


/**
 * A SyntaxChecker which verifies that a value follows the
 * matching rule description syntax according to RFC 4512, par 4.2.3:
 * 
 * <pre>
 * MatchingRuleDescription = LPAREN WSP
 *    numericoid                 ; object identifier
 *    [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
 *    [ SP "DESC" SP qdstring ]  ; description
 *    [ SP "OBSOLETE" ]          ; not active
 *    SP "SYNTAX" SP numericoid  ; assertion syntax
 *    extensions WSP RPAREN      ; extensions
 * 
 * extensions = *( SP xstring SP qdstrings )
 * xstring = "X" HYPHEN 1*( ALPHA / HYPHEN / USCORE ) 
 * </pre>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class MatchingRuleDescriptionSyntaxChecker extends SyntaxChecker
{
    /** The schema parser used to parse the MatchingRuleDescription Syntax */
    private transient MatchingRuleDescriptionSchemaParser schemaParser = new MatchingRuleDescriptionSchemaParser();
    
    /**
     * A static instance of MatchingRuleDescriptionSyntaxChecker
     */
    public static final MatchingRuleDescriptionSyntaxChecker INSTANCE = 
        new MatchingRuleDescriptionSyntaxChecker( SchemaConstants.MATCHING_RULE_DESCRIPTION_SYNTAX );
    
    /**
     * A static Builder for this class
     */
    public static final class Builder extends SCBuilder<MatchingRuleDescriptionSyntaxChecker>
    {
        /**
         * The Builder constructor
         */
        private Builder()
        {
            super( SchemaConstants.MATCHING_RULE_DESCRIPTION_SYNTAX );
        }
        
        
        /**
         * Create a new instance of MatchingRuleDescriptionSyntaxChecker
         * @return A new instance of MatchingRuleDescriptionSyntaxChecker
         */
        @Override
        public MatchingRuleDescriptionSyntaxChecker build()
        {
            return new MatchingRuleDescriptionSyntaxChecker( oid );
        }
    }

    
    /**
     * Creates a new instance of MatchingRuleDescriptionSchemaParser.
     *
     * @param oid The OID to use for this SyntaxChecker
     */
    private MatchingRuleDescriptionSyntaxChecker( String oid )
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
