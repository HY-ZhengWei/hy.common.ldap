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

package org.apache.directory.api.ldap.trigger;


/**
 * The language schema option of triggered stored procedure.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoredProcedureLanguageSchemeOption implements StoredProcedureOption
{

    private String language;


    /**
     * Instantiates a new stored procedure language scheme option.
     *
     * @param language the language
     */
    public StoredProcedureLanguageSchemeOption( String language )
    {
        this.language = language;
    }


    /**
     * Gets the language.
     *
     * @return the language
     */
    public String getLanguage()
    {
        return language;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int h = 37;

        h = h * 17 + ( ( language == null ) ? 0 : language.hashCode() );

        return h;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        
        if ( obj == null )
        {
            return false;
        }
        
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        
        StoredProcedureLanguageSchemeOption other = ( StoredProcedureLanguageSchemeOption ) obj;
     
        if ( language == null )
        {
            if ( other.language != null )
            {
                return false;
            }
        }
        else if ( !language.equals( other.language ) )
        {
            return false;
        }
        
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "language " + "\"" + language + "\"";
    }
}
