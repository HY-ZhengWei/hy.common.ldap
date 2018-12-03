/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.api.ldap.model.ldif.anonymizer;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.schema.AttributeType;


/**
 * A default anonymizer for attributes that are HR. It covers DirectoryString, Ia5String, ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StringAnonymizer extends AbstractAnonymizer<String>
{
    /** The latest anonymized String value map */
    private Map<Integer, String> latestStringMap;

    /**
     * Creates a new instance of StringAnonymizer.
     */
    public StringAnonymizer()
    {
        latestStringMap = new HashMap<>();
        caseSensitive = false;
    }

    
    /**
     * Creates a new instance of StringAnonymizer.
     * 
     * @param latestStringMap The map containing the latest value for each length 
     */
    public StringAnonymizer( Map<Integer, String> latestStringMap )
    {
        if ( latestStringMap == null ) 
        {
            this.latestStringMap = new HashMap<>();
        }
        else
        {
            this.latestStringMap = latestStringMap;
        }

        caseSensitive = false;
    }
    
    
    /**
     * Anonymize an attribute using pure random values (either chars of bytes, depending on the Attribute type)
     */
    @Override
    public Attribute anonymize( Map<Value, Value> valueMap, Set<Value> valueSet, Attribute attribute )
    {
        AttributeType attributeType = attribute.getAttributeType();
        Attribute result = new DefaultAttribute( attributeType );

        for ( Value value : attribute )
        {
            if ( value.isHumanReadable() )
            {
                Value anonymized =  valueMap.get( value );
                
                if ( anonymized != null )
                {
                    try
                    {
                        result.add( anonymized );
                    }
                    catch ( LdapInvalidAttributeValueException e )
                    {
                        // TODO : handle that
                    }
                }
                else
                {
                    String strValue = value.getValue();
                    String newValue = computeNewValue( strValue );
                    
                    try
                    {
                        result.add( newValue );
                        Value anonValue = new Value( attribute.getAttributeType(), newValue );
                        valueMap.put( ( Value ) value, anonValue );
                        valueSet.add( anonValue );
                    }
                    catch ( LdapInvalidAttributeValueException e )
                    {
                        throw new RuntimeException( I18n.err( I18n.ERR_13436_ERROR_ANONYMIZING_VALUE, strValue ) );
                    }
                }
            }
        }

        return result;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> getLatestStringMap()
    {
        return latestStringMap;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setLatestStringMap( Map<Integer, String> latestStringMap )
    {
        this.latestStringMap = latestStringMap;
    }
}
