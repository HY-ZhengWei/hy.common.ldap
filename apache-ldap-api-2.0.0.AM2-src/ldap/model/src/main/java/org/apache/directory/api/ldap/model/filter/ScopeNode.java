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


import org.apache.directory.api.ldap.model.message.AliasDerefMode;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;


/**
 * Node used not to represent a published assertion but an assertion on the
 * scope of the search.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ScopeNode extends AbstractExprNode
{
    /** the scope of this node */
    private final SearchScope scope;

    /** the search base */
    private final Dn baseDn;

    /** the search ID */
    private final String baseId;

    /** the alias dereferencing mode */
    private final AliasDerefMode aliasDerefAliases;


    /**
     * Creates a new ScopeNode object.
     * 
     * @param aliasDerefAliases the alias dereferencing mode
     * @param baseDn the search base
     * @param baseId the search ID
     * @param scope the search scope
     */
    public ScopeNode( AliasDerefMode aliasDerefAliases, Dn baseDn, String baseId, SearchScope scope )
    {
        super( AssertionType.SCOPE );
        this.scope = scope;
        this.baseDn = baseDn;
        this.aliasDerefAliases = aliasDerefAliases;
        this.baseId = baseId;
    }


    /**
     * Always returns true since a scope node has no children.
     * 
     * @see ExprNode#isLeaf()
     * @return <code>true</code>
     */
    @Override
    public boolean isLeaf()
    {
        return true;
    }


    /**
     * Gets the search scope.
     * 
     * @return the search scope 
     */
    public SearchScope getScope()
    {
        return scope;
    }


    /**
     * Gets the base dn.
     * 
     * @return the base dn
     */
    public Dn getBaseDn()
    {
        return baseDn;
    }


    /**
     * Gets the base ID.
     * 
     * @return the base ID
     */
    public String getBaseId()
    {
        return baseId;
    }


    /**
     * Gets the alias dereferencing mode type safe enumeration.
     * 
     * @return the alias dereferencing enumeration constant.
     */
    public AliasDerefMode getDerefAliases()
    {
        return aliasDerefAliases;
    }


    /**
     * @see ExprNode#accept(
     *FilterVisitor)
     * 
     * @param visitor the filter expression tree structure visitor
     * @return The modified element
     */
    @Override
    public Object accept( FilterVisitor visitor )
    {
        if ( visitor.canVisit( this ) )
        {
            return visitor.visit( this );
        }
        else
        {
            return null;
        }
    }


    /**
     * Tells if this Node is Schema aware.
     * 
     * @return true if the Node is SchemaAware
     */
    @Override
    public boolean isSchemaAware()
    {
        return true;
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

        if ( !( obj instanceof ScopeNode ) )
        {
            return false;
        }
        ScopeNode that = ( ScopeNode ) obj;
        if ( aliasDerefAliases == null )
        {
            if ( that.aliasDerefAliases != null )
            {
                return false;
            }
        }
        else
        {
            if ( !aliasDerefAliases.equals( that.aliasDerefAliases ) )
            {
                return false;
            }
        }
        if ( baseDn == null )
        {
            if ( that.baseDn != null )
            {
                return false;
            }
        }
        else
        {
            if ( !baseDn.equals( that.baseDn ) )
            {
                return false;
            }
        }
        if ( scope.getScope() != that.scope.getScope() )
        {
            return false;
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
        h = h * 17 + ( aliasDerefAliases != null ? aliasDerefAliases.hashCode() : 0 );
        h = h * 17 + ( baseDn != null ? baseDn.hashCode() : 0 );
        h = h * 17 + scope.getScope();

        return h;
    }


    /**
     * @see Object#toString()
     * @return A string representing the AndNode
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "(#{" );

        switch ( scope )
        {
            case OBJECT:
                buf.append( "OBJECT_SCOPE" );

                break;

            case ONELEVEL:
                buf.append( "ONE_LEVEL_SCOPE" );

                break;

            case SUBTREE:
                buf.append( "SUBTREE_SCOPE (Estimated)" );

                break;

            default:
                buf.append( "UNKNOWN" );
                break;
        }

        buf.append( ", '" );
        buf.append( baseDn );
        buf.append( "', " );
        buf.append( aliasDerefAliases );
        buf.append( "}" );
        buf.append( super.toString() );
        buf.append( ')' );

        return buf.toString();
    }
}
