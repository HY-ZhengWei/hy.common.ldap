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
package org.apache.directory.api.ldap.model.schema.normalizers;


import java.io.IOException;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.schema.Normalizer;
import org.apache.directory.api.ldap.model.schema.PrepareString;
import org.apache.directory.api.ldap.model.schema.PreparedNormalizer;


/**
 * Normalizer which trims down whitespace replacing multiple whitespace
 * characters on the edges and within the string with a single space character
 * thereby preserving tokenization order - while doing all this in the same pass
 * it lower cases all characters.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public class DeepTrimToLowerNormalizer extends Normalizer implements PreparedNormalizer
{
    /**
     * Creates a new instance of DeepTrimToLowerNormalizer.
     * 
     * @param oid The MR OID to use with this Normalizer
     */
    public DeepTrimToLowerNormalizer( String oid )
    {
        super( oid );
    }


    /**
     * Creates a new instance of DeepTrimToLowerNormalizer where the OID is
     * set after instantiation.
     */
    public DeepTrimToLowerNormalizer()
    {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String normalize( String value ) throws LdapException
    {
        return normalize( value, PrepareString.AssertionType.ATTRIBUTE_VALUE );
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public String normalize( String value, PrepareString.AssertionType assertionType ) throws LdapException
    {
        if ( value == null )
        {
            return null;
        }

        String normValue = null;

        try
        {
            // Transcoding is useless
            // Map
            String mapped = PrepareString.mapIgnoreCase( value );

            // Normalize
            String normalized = PrepareString.normalize( mapped );
            
            char[] chars = normalized.toCharArray();
            
            // Prohibit
            PrepareString.checkProhibited( chars );
            
            // Bidi is ignored
            
            // Insignificant Characters Handling
            switch ( assertionType )
            {
                case ATTRIBUTE_VALUE :
                    normValue = PrepareString.insignificantSpacesStringValue( chars );
                    break;
                    
                case SUBSTRING_INITIAL :
                    normValue = PrepareString.insignificantSpacesStringInitial( chars );
                    break;
                    
                case SUBSTRING_ANY :
                    normValue = PrepareString.insignificantSpacesStringAny( chars );
                    break;
                    
                case SUBSTRING_FINAL :
                    normValue = PrepareString.insignificantSpacesStringFinal( chars );
                    break;
                    
                default :
                    // Do nothing
                    break;
            }

            return normValue;
        }
        catch ( IOException ioe )
        {
            throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n.err(
                I18n.ERR_13724_INVALID_VALUE, value ), ioe );
        }
    }
}
