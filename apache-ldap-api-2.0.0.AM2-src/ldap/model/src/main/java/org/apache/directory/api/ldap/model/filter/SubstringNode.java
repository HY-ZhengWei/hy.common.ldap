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


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.Normalizer;
import org.apache.directory.api.ldap.model.schema.PrepareString;


/**
 * Filter expression tree node used to represent a substring assertion.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SubstringNode extends LeafNode
{
    /** The initial fragment before any wildcard */
    private String initialPattern;

    /** The end fragment after wildcard */
    private String finalPattern;

    /** List of fragments between wildcard */
    private List<String> anyPattern;


    /**
     * Creates a new SubstringNode object with only one wildcard and no internal
     * any fragments between wildcards.
     * 
     * @param attributeType the name of the attributeType to substring assert
     * @param initialPattern the initial fragment
     * @param finalPattern the final fragment
     */
    public SubstringNode( AttributeType attributeType, String initialPattern, String finalPattern )
    {
        super( attributeType, AssertionType.SUBSTRING );

        anyPattern = new ArrayList<>( 2 );
        this.finalPattern = finalPattern;
        this.initialPattern = initialPattern;
    }


    /**
     * Creates a new SubstringNode object with only one wildcard and no internal
     * any fragments between wildcards.
     * 
     * @param attribute the name of the attribute to substring assert
     * @param initialPattern the initial fragment
     * @param finalPattern the final fragment
     */
    public SubstringNode( String attribute, String initialPattern, String finalPattern )
    {
        super( attribute, AssertionType.SUBSTRING );

        anyPattern = new ArrayList<>( 2 );
        this.finalPattern = finalPattern;
        this.initialPattern = initialPattern;
    }


    /**
     * Creates a new SubstringNode object without any value
     * 
     * @param attribute the name of the attribute to substring assert
     */
    public SubstringNode( AttributeType attribute )
    {
        super( attribute, AssertionType.SUBSTRING );

        anyPattern = new ArrayList<>( 2 );
        this.finalPattern = null;
        this.initialPattern = null;
    }


    /**
     * Creates a new SubstringNode object without any value
     * 
     * @param attributeType the attributeType to substring assert
     */
    public SubstringNode( String attributeType )
    {
        super( attributeType, AssertionType.SUBSTRING );

        anyPattern = new ArrayList<>( 2 );
        this.finalPattern = null;
        this.initialPattern = null;
    }


    /**
     * Creates a new SubstringNode object more than one wildcard and an any
     * list.
     * 
     * @param anyPattern list of internal fragments between wildcards
     * @param attributeType the attributeType to substring assert
     * @param initialPattern the initial fragment
     * @param finalPattern the final fragment
     */
    public SubstringNode( List<String> anyPattern, AttributeType attributeType, String initialPattern,
        String finalPattern )
    {
        super( attributeType, AssertionType.SUBSTRING );

        this.anyPattern = anyPattern;
        this.finalPattern = finalPattern;
        this.initialPattern = initialPattern;
    }


    /**
     * Creates a new SubstringNode object more than one wildcard and an any
     * list.
     * 
     * @param anyPattern list of internal fragments between wildcards
     * @param attribute the name of the attribute to substring assert
     * @param initialPattern the initial fragment
     * @param finalPattern the final fragment
     */
    public SubstringNode( List<String> anyPattern, String attribute, String initialPattern, String finalPattern )
    {
        super( attribute, AssertionType.SUBSTRING );

        this.anyPattern = anyPattern;
        this.finalPattern = finalPattern;
        this.initialPattern = initialPattern;
    }


    /**
     * Creates a regular expression from an LDAP substring assertion filter
     * specification.
     *
     * @param initialPattern
     *            the initial fragment before wildcards
     * @param anyPattern
     *            fragments surrounded by wildcards if any
     * @param finalPattern
     *            the final fragment after last wildcard if any
     * @return the regular expression for the substring match filter
     * @throws java.util.regex.PatternSyntaxException
     *             if a syntactically correct regular expression cannot be
     *             compiled
     */
    public static Pattern getRegex( String initialPattern, String[] anyPattern, String finalPattern )
    {
        StringBuilder buf = new StringBuilder();

        if ( initialPattern != null )
        {
            buf.append( '^' ).append( Pattern.quote( initialPattern ) );
        }

        if ( anyPattern != null )
        {
            for ( int i = 0; i < anyPattern.length; i++ )
            {
                buf.append( ".*" ).append( Pattern.quote( anyPattern[i] ) );
            }
        }

        if ( finalPattern != null )
        {
            buf.append( ".*" ).append( Pattern.quote( finalPattern ) );
        }
        else
        {
            buf.append( ".*" );
        }

        return Pattern.compile( buf.toString() );
    }


    /**
     * Clone the Node
     */
    @Override
    public ExprNode clone()
    {
        ExprNode clone = super.clone();

        if ( anyPattern != null )
        {
            ( ( SubstringNode ) clone ).anyPattern = new ArrayList<>();

            for ( String any : anyPattern )
            {
                ( ( SubstringNode ) clone ).anyPattern.add( any );
            }
        }

        return clone;
    }


    /**
     * Gets the initial fragment.
     * 
     * @return the initial prefix
     */
    public final String getInitial()
    {
        return initialPattern;
    }


    /**
     * Set the initial pattern
     * @param initialPattern The initial pattern
     */
    public void setInitial( String initialPattern )
    {
        this.initialPattern = initialPattern;
    }


    /**
     * Gets the final fragment or suffix.
     * 
     * @return the suffix
     */
    public final String getFinal()
    {
        return finalPattern;
    }


    /**
     * Set the final pattern
     * @param finalPattern The final pattern
     */
    public void setFinal( String finalPattern )
    {
        this.finalPattern = finalPattern;
    }


    /**
     * Gets the list of wildcard surrounded any fragments.
     * 
     * @return the any fragments
     */
    public final List<String> getAny()
    {
        return anyPattern;
    }


    /**
     * Set the any patterns
     * @param anyPattern The any patterns
     */
    public void setAny( List<String> anyPattern )
    {
        this.anyPattern = anyPattern;
    }


    /**
     * Add an any pattern
     * @param anyPattern The any pattern
     */
    public void addAny( String anyPattern )
    {
        this.anyPattern.add( anyPattern );
    }


    /**
     * Gets the compiled regular expression for the substring expression.
     * 
     * @param normalizer the normalizer to use for pattern component normalization
     * @return the equivalent compiled regular expression
     * @throws LdapException if there are problems while normalizing
     */
    public final Pattern getRegex( Normalizer normalizer ) throws LdapException
    {
        if ( ( anyPattern != null ) && ( !anyPattern.isEmpty() ) )
        {
            String[] any = new String[anyPattern.size()];

            for ( int i = 0; i < any.length; i++ )
            {
                any[i] = normalizer.normalize( anyPattern.get( i ), PrepareString.AssertionType.SUBSTRING_ANY );

                if ( any[i].length() == 0 )
                {
                    any[i] = " ";
                }
            }

            String initialStr = null;

            if ( initialPattern != null )
            {
                initialStr = normalizer.normalize( initialPattern, PrepareString.AssertionType.SUBSTRING_INITIAL );
            }

            String finalStr = null;

            if ( finalPattern != null )
            {
                finalStr = normalizer.normalize( finalPattern, PrepareString.AssertionType.SUBSTRING_FINAL );
            }

            return getRegex( initialStr, any, finalStr );
        }

        String initialStr = null;

        if ( initialPattern != null )
        {
            initialStr = normalizer.normalize( initialPattern, PrepareString.AssertionType.SUBSTRING_INITIAL );
        }

        String finalStr = null;

        if ( finalPattern != null )
        {
            finalStr = normalizer.normalize( finalPattern, PrepareString.AssertionType.SUBSTRING_FINAL );
        }

        return getRegex( initialStr, null, finalStr );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }

        if ( !( obj instanceof SubstringNode ) )
        {
            return false;
        }
        
        SubstringNode that = ( SubstringNode ) obj;

        if ( initialPattern == null )
        {
            if ( that.initialPattern != null )
            {
                return false;
            }
        }
        else
        {
            if ( !initialPattern.equals( that.initialPattern ) )
            {
                return false;
            }
        }

        if ( finalPattern == null )
        {
            if ( that.finalPattern != null )
            {
                return false;
            }
        }
        else
        {
            if ( !finalPattern.equals( that.finalPattern ) )
            {
                return false;
            }
        }

        return super.equals( obj );
    }


    /**
     * @see Object#hashCode()
     * @return the instance's hash code 
     */
    @Override
    public int hashCode()
    {
        int h = 37;

        h = h * 17 + super.hashCode();
        h = h * 17 + ( initialPattern != null ? initialPattern.hashCode() : 0 );

        if ( anyPattern != null )
        {
            for ( String pattern : anyPattern )
            {
                h = h * 17 + pattern.hashCode();
            }
        }

        h = h * 17 + ( finalPattern != null ? finalPattern.hashCode() : 0 );

        return h;
    }


    /**
     * @see java.lang.Object#toString()
     * @return A string representing the AndNode
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append( '(' );

        if ( attributeType != null )
        {
            buf.append( attributeType.getName() );
        }
        else
        {
            buf.append( attribute );
        }

        buf.append( '=' );

        if ( null != initialPattern )
        {
            buf.append( escapeFilterValue( initialPattern ) ).append( '*' );
        }
        else
        {
            buf.append( '*' );
        }

        if ( null != anyPattern )
        {
            for ( String any : anyPattern )
            {
                buf.append( escapeFilterValue( any ) );
                buf.append( '*' );
            }
        }

        if ( null != finalPattern )
        {
            buf.append( escapeFilterValue( finalPattern ) );
        }

        buf.append( super.toString() );

        buf.append( ')' );

        return buf.toString();
    }
}
