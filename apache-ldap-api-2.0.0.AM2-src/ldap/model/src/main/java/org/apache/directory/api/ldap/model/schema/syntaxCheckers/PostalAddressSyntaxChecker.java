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
 * A SyntaxChecker which verifies that a value is a PostalAddress according to 
 * RFC 4517 :
 * <pre>
 * &lt;postal-address&gt; = &lt;dstring&gt; &lt;dstring-list&gt;
 * &lt;dstring-list&gt; = "$" &lt;dstring&gt; &lt;dstring-list&gt; | e
 * </pre>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class PostalAddressSyntaxChecker extends SyntaxChecker
{
    /**
     * A static instance of PostalAddressSyntaxChecker
     */
    public static final PostalAddressSyntaxChecker INSTANCE = 
        new PostalAddressSyntaxChecker( SchemaConstants.POSTAL_ADDRESS_SYNTAX );
    
    /**
     * A static Builder for this class
     */
    public static final class Builder extends SCBuilder<PostalAddressSyntaxChecker>
    {
        /**
         * The Builder constructor
         */
        private Builder()
        {
            super( SchemaConstants.POSTAL_ADDRESS_SYNTAX );
        }
        
        
        /**
         * Create a new instance of PostalAddressSyntaxChecker
         * @return A new instance of PostalAddressSyntaxChecker
         */
        @Override
        public PostalAddressSyntaxChecker build()
        {
            return new PostalAddressSyntaxChecker( oid );
        }
    }

    
    /**
     * Creates a new instance of PostalAddressSyntaxChecker.
     * 
     * @param oid The OID to use for this SyntaxChecker
     */
    private PostalAddressSyntaxChecker( String oid )
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

        // Search for the '$' separator
        int dollar = strValue.indexOf( '$' );

        if ( dollar == -1 )
        {
            // No '$' => only a dstring
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13701_SYNTAX_VALID, value ) );
            }
            
            return true;
        }

        int pos = 0;
        do
        {
            // check that the element between each '$' is not empty
            String address = strValue.substring( pos, dollar );

            if ( Strings.isEmpty( address ) )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
                }
                
                return false;
            }

            pos = dollar + 1;

            if ( pos == strValue.length() )
            {
                // we should not have a '$' at the end
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
                }
                
                return false;
            }

            dollar = strValue.indexOf( '$', pos );
        }
        while ( dollar > -1 );

        return true;
    }
}
