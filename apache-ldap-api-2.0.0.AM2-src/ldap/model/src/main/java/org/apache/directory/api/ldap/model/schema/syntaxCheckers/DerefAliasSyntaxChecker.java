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
 * A SyntaxChecker which verifies that a value is a valid DerefAlias. We
 * have four possible values :
 * <ul>
 * <li>NEVER</li>
 * <li>SEARCHING</li>
 * <li>FINDING</li>
 * <li>ALWAYS</li>
 * </ul>
 * The value is case insensitive
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class DerefAliasSyntaxChecker extends SyntaxChecker
{
    /**
     * A static instance of DerefAliasSyntaxChecker
     */
    public static final DerefAliasSyntaxChecker INSTANCE = 
        new DerefAliasSyntaxChecker( SchemaConstants.DEREF_ALIAS_SYNTAX );
    
    /**
     * A static Builder for this class
     */
    public static final class Builder extends SCBuilder<DerefAliasSyntaxChecker>
    {
        /**
         * The Builder constructor
         */
        private Builder()
        {
            super( SchemaConstants.DEREF_ALIAS_SYNTAX );
        }
        
        
        /**
         * Create a new instance of DerefAliasSyntaxChecker
         * @return A new instance of DerefAliasSyntaxChecker
         */
        @Override
        public DerefAliasSyntaxChecker build()
        {
            return new DerefAliasSyntaxChecker( oid );
        }
    }

    /**
     * Creates a new instance of DerefAliasSyntaxChecker.
     * 
     * @param oid The OID to use for this SyntaxChecker
     */
    private DerefAliasSyntaxChecker( String oid )
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

        strValue = Strings.trim( Strings.toLowerCaseAscii( strValue ) );

        return "never".equals( strValue ) || "finding".equals( strValue ) || "searching".equals( strValue ) || "always"
            .equals( strValue );
    }
}
