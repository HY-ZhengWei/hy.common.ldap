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
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;
import org.apache.directory.api.util.Strings;


/**
 * A SyntaxChecker which verifies that a value is a valid Name and Optional UID.
 * <p>
 * This element is a composition of two parts, a {@link Dn} and an optional UID :
 * <pre>
 * NameAndOptionalUID = distinguishedName [ SHARP BitString ]
 * </pre>
 * Both part already have their syntax checkers, so we will just call them
 * after having split the element in two ( if necessary)
 * <p>
 * We just check that the {@link Dn} is valid, we don't need to verify each of the {@link Rdn}
 * syntax.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class NameAndOptionalUIDSyntaxChecker extends SyntaxChecker
{
    /**
     * A static instance of NameAndOptionalUIDSyntaxChecker
     */
    public static final NameAndOptionalUIDSyntaxChecker INSTANCE = 
        new NameAndOptionalUIDSyntaxChecker( SchemaConstants.NAME_AND_OPTIONAL_UID_SYNTAX );
    
    /**
     * A static Builder for this class
     */
    public static final class Builder extends SCBuilder<NameAndOptionalUIDSyntaxChecker>
    {
        /**
         * The Builder constructor
         */
        private Builder()
        {
            super( SchemaConstants.NAME_AND_OPTIONAL_UID_SYNTAX );
        }
        
        
        /**
         * Create a new instance of NameAndOptionalUIDSyntaxChecker
         * @return A new instance of NameAndOptionalUIDSyntaxChecker
         */
        @Override
        public NameAndOptionalUIDSyntaxChecker build()
        {
            return new NameAndOptionalUIDSyntaxChecker( oid );
        }
    }

    
    /**
     * Creates a new instance of NameAndOptionalUIDSyntaxChecker.
     * 
     * @param oid The OID to use for this SyntaxChecker
     */
    private NameAndOptionalUIDSyntaxChecker( String oid )
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

        // Let's see if we have an UID part
        int sharpPos = strValue.lastIndexOf( '#' );

        if ( sharpPos != -1 )
        {
            // Now, check that we don't have another '#'
            if ( strValue.indexOf( '#' ) != sharpPos )
            {
                // Yes, we have one : this is not allowed, it should have been
                // escaped.
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
                }
                
                return false;
            }

            // This is an UID if the '#' is immediately
            // followed by a BitString, except if the '#' is
            // on the last position
            if ( BitStringSyntaxChecker.isValid( strValue.substring( sharpPos + 1 ) )
                && ( sharpPos < strValue.length() ) )
            {
                // Ok, we have a BitString, now check the Dn,
                // except if the '#' is in first position
                if ( sharpPos > 0 )
                {
                    boolean result = Dn.isValid( strValue.substring( 0, sharpPos ) );

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
                else
                {
                    // The Dn must not be null ?
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
                    }
                    
                    return false;
                }
            }
            else
            {
                // We have found a '#' but no UID part.
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.err( I18n.ERR_13210_SYNTAX_INVALID, value ) );
                }
                
                return false;
            }
        }
        else
        {
            // No UID, the strValue is a Dn
            // Check that the value is a valid Dn
            boolean result = Dn.isValid( strValue );

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
}
