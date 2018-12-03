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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.schema.Normalizer;
import org.apache.directory.api.ldap.model.schema.PrepareString;


/**
 * A Normalizer that uses Perl5 based regular expressions to normalize values.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public class RegexNormalizer extends Normalizer
{
    /** the perl 5 regex engine */
    private final Pattern[] regexes;

    /** the set of regular expressions used to transform values */
    private final transient Matcher[] matchers;


    /**
     * Creates a Perl5 regular expression based normalizer.
     * 
     * @param oid The MR OID to use for this Normalizer
     * @param regexes the set of regular expressions used to transform values
     */
    public RegexNormalizer( String oid, Pattern[] regexes )
    {
        super( oid );
        if ( regexes != null )
        {
            this.regexes = new Pattern[regexes.length];
            System.arraycopy( regexes, 0, this.regexes, 0, regexes.length );

            matchers = new Matcher[regexes.length];

            for ( int i = 0; i < regexes.length; i++ )
            {
                matchers[i] = regexes[i].matcher( "" );
            }
        }
        else
        {
            this.regexes = null;
            matchers = new Matcher[0];
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String normalize( String value )
    {
        return normalize( value, PrepareString.AssertionType.ATTRIBUTE_VALUE );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String normalize( String value, PrepareString.AssertionType assertionType )
    {
        if ( value == null )
        {
            return null;
        }

        String str = value;

        for ( int i = 0; i < matchers.length; i++ )
        {
            str = matchers[i].replaceAll( str );
        }

        return str;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "RegexNormalizer( " );

        for ( int i = 0; i < regexes.length; i++ )
        {
            buf.append( regexes[i] );

            if ( i < regexes.length - 1 )
            {
                buf.append( ", " );
            }
        }

        buf.append( " )" );
        return buf.toString();
    }
}
