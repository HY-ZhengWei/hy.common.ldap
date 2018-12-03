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
 * A SyntaxChecker for the Comparator schema element
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class ComparatorSyntaxChecker extends SyntaxChecker
{
    /**
     * A static instance of ComparatorSyntaxChecker
     */
    public static final ComparatorSyntaxChecker INSTANCE = new ComparatorSyntaxChecker( SchemaConstants.COMPARATOR_SYNTAX );
    
    /**
     * A static Builder for this class
     */
    public static final class Builder extends SCBuilder<ComparatorSyntaxChecker>
    {
        /**
         * The Builder constructor
         */
        private Builder()
        {
            super( SchemaConstants.COMPARATOR_SYNTAX );
        }
        
        
        /**
         * Create a new instance of ComparatorSyntaxChecker
         * @return A new instance of ComparatorSyntaxChecker
         */
        @Override
        public ComparatorSyntaxChecker build()
        {
            return new ComparatorSyntaxChecker( oid );
        }
    }

    
    /**
     * Creates a new instance of ComparatorSyntaxChecker.
     *
     * @param oid The OID to use for this SyntaxChecker
     */
    private ComparatorSyntaxChecker( String oid )
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
            
            return true;
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

        boolean result = Strings.isIA5String( strValue );

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
