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
package org.apache.directory.api.ldap.model.schema;


import java.util.HashSet;
import java.util.Set;

import org.apache.directory.api.util.Strings;


/**
 * An structure containing a couple of attributeType and options. A search request
 * can contain a list of attribute to return, those attribute could be associated
 * with options.
 * 
 * Those options are stored into a Set.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeOptions
{
    /** The attributeType */
    private AttributeType attributeType;

    /** The options, if any */
    private Set<String> options;


    /**
     * Creates a new instance of AttributeTypeOptions, containing an attributeType, 
     * but no options.
     *
     * @param attributeType The associated AttributeType
     */
    public AttributeTypeOptions( AttributeType attributeType )
    {
        this.attributeType = attributeType;
    }


    /**
     * Creates a new instance of AttributeTypeOptions, containing an attributeType, 
     * and options.
     *
     * @param attributeType the associated AttributeType
     * @param options the associated options
     */
    public AttributeTypeOptions( AttributeType attributeType, Set<String> options )
    {
        this.attributeType = attributeType;
        this.options = options;
    }


    /**
     * @return the inner attributeType
     */
    public AttributeType getAttributeType()
    {
        return attributeType;
    }


    /**
     * @return the associated options
     */
    public Set<String> getOptions()
    {
        return options;
    }


    /**
     * @return <code>true</code> if the attributeType has at least one option
     */
    public boolean hasOption()
    {
        return ( options != null ) && !options.isEmpty();
    }


    /**
     * @param option the option to check
     * @return <code>true</code> if the attributeType has the given option
     */
    public boolean hasOption( String option )
    {
        if ( hasOption() )
        {
            return options.contains( Strings.toLowerCaseAscii( Strings.trim( option ) ) );
        }
        else
        {
            return false;
        }
    }


    /**
     * Add a new option to the option set for this attributeType.
     *
     * @param option the option to add
     */
    public void addOption( String option )
    {
        if ( options == null )
        {
            options = new HashSet<>();
        }

        options.add( Strings.toLowerCaseAscii( Strings.trim( option ) ) );
    }


    /**
     * Add a set of optionS to the option set for this attributeType.
     *
     * @param optionsToAdd the options to add
     */
    public void addOptions( Set<String> optionsToAdd )
    {
        if ( this.options == null )
        {
            this.options = optionsToAdd;
        }
        else
        {
            this.options.addAll( optionsToAdd );
        }
    }


    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return attributeType.hashCode();
    }
    
    
    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals( Object o )
    {
        // Short circuit
        if ( this == o )
        {
            return true;
        }

        if ( !( o instanceof AttributeTypeOptions ) )
        {
            return false;
        }

        AttributeTypeOptions that = ( AttributeTypeOptions ) o;
        
        return attributeType.equals( that.attributeType );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "<" ).append( attributeType.getName() );

        if ( hasOption() )
        {
            for ( String option : options )
            {
                sb.append( ";" ).append( option );
            }
        }

        return sb.append( ">" ).toString();
    }
}
