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
package org.apache.directory.api.ldap.model.filter;


import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.util.Strings;


/**
 * An encoder for LDAP filters.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class FilterEncoder
{
    private FilterEncoder()
    {
    }


    /**
     * Formats a filter and handles encoding of special characters in the value arguments using the
     * &lt;valueencoding&gt; rule as described in <a href="http://www.ietf.org/rfc/rfc4515.txt">RFC 4515</a>.
     * <p>
     * Example of filter template format: <code>(&amp;(cn={0})(uid={1}))</code>
     * 
     * @param filterTemplate the filter with placeholders
     * @param values the values to encode and substitute
     * @return the formatted filter with escaped values
     * @throws IllegalArgumentException if the number of values does not match the number of placeholders in the template
     */
    public static String format( String filterTemplate, String... values )
    {
        if ( values == null )
        {
            values = Strings.EMPTY_STRING_ARRAY;
        }

        MessageFormat mf = new MessageFormat( filterTemplate, Locale.ROOT );

        // check element count and argument count
        Format[] formats = mf.getFormatsByArgumentIndex();
        
        if ( formats.length != values.length )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13300_BAD_PLACE_HOLDERS_NUMBER, filterTemplate, formats.length, values.length ) );
        }

        // encode arguments
        for ( int i = 0; i < values.length; i++ )
        {
            values[i] = encodeFilterValue( values[i] );
        }

        // format the filter
        return mf.format( values );
    }


    /**
     * Handles encoding of special characters in LDAP search filter assertion values using the
     * &lt;valueencoding&gt; rule as described in <a href="http://www.ietf.org/rfc/rfc4515.txt">RFC 4515</a>.
     *
     * @param value Right hand side of "attrId=value" assertion occurring in an LDAP search filter.
     * @return Escaped version of <code>value</code>
     */
    public static String encodeFilterValue( String value )
    {
        StringBuilder sb = new StringBuilder( value.length() );
        boolean escaped = false;
        boolean hexPair = false;
        char hex = '\0';

        for ( int i = 0; i < value.length(); i++ )
        {
            char ch = value.charAt( i );

            switch ( ch )
            {
                case '*':
                    if ( escaped )
                    {
                        sb.append( "\\5C" );

                        if ( hexPair )
                        {
                            sb.append( hex );
                            hexPair = false;
                        }

                        escaped = false;
                    }

                    sb.append( "\\2A" );
                    break;

                case '(':
                    if ( escaped )
                    {
                        sb.append( "\\5C" );

                        if ( hexPair )
                        {
                            sb.append( hex );
                            hexPair = false;
                        }

                        escaped = false;
                    }

                    sb.append( "\\28" );
                    break;

                case ')':
                    if ( escaped )
                    {
                        sb.append( "\\5C" );

                        if ( hexPair )
                        {
                            sb.append( hex );
                            hexPair = false;
                        }

                        escaped = false;
                    }

                    sb.append( "\\29" );
                    break;

                case '\0':
                    if ( escaped )
                    {
                        sb.append( "\\5C" );

                        if ( hexPair )
                        {
                            sb.append( hex );
                            hexPair = false;
                        }

                        escaped = false;
                    }

                    sb.append( "\\00" );
                    break;

                case '\\':
                    if ( escaped )
                    {
                        sb.append( "\\5C" );
                        escaped = false;
                    }
                    else
                    {
                        escaped = true;
                        hexPair = false;
                    }

                    break;

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    if ( escaped )
                    {
                        if ( hexPair )
                        {
                            sb.append( '\\' ).append( hex ).append( ch );
                            escaped = false;
                            hexPair = false;
                        }
                        else
                        {
                            hexPair = true;
                            hex = ch;
                        }
                    }
                    else
                    {
                        sb.append( ch );
                    }

                    break;

                default:
                    if ( escaped )
                    {
                        sb.append( "\\5C" );

                        if ( hexPair )
                        {
                            sb.append( hex );
                            hexPair = false;
                        }

                        escaped = false;
                    }

                    sb.append( ch );
            }
        }

        if ( escaped )
        {
            sb.append( "\\5C" );
        }

        return sb.toString();
    }
}
