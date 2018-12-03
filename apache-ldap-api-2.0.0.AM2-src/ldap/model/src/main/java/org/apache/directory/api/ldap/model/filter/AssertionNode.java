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


import org.apache.directory.api.i18n.I18n;


/**
 * Node used for the application of arbitrary predicates on return candidates.
 * Applies dynamic and programatic criteria for the selection of candidates for
 * return. Nodes of this type may be introduced into the filter expression to
 * provided the opportunity to constrain the search further without altering the
 * search algorithm.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AssertionNode extends AbstractExprNode
{
    /** The assertion or predicate to apply */
    private final Assertion assertion;

    /** Description of assertion for polish printouts */
    private final String desc;


    // ------------------------------------------------------------------------
    // C O N S T R U C T O R S
    // ------------------------------------------------------------------------

    /**
     * Creates an AssertionNode using an arbitrary candidate assertion.
     * 
     * @param assertion the arbitrary selection logic.
     */
    public AssertionNode( Assertion assertion )
    {
        this( assertion, "ASSERTION" );
    }


    /**
     * Creates an AssertionNode using an arbitrary candidate assertion with a
     * descriptions used for filter AST walker dumps.
     * 
     * @param assertion the arbitrary selection logic.
     * @param desc the printout representation for filter prints.
     */
    public AssertionNode( Assertion assertion, String desc )
    {
        super( AssertionType.ASSERTION );
        this.desc = desc;
        this.assertion = assertion;

        /*
         * We never want this node to ever make it to the point of becoming a
         * candidate for use in an enumeration so we set the scan count to the
         * maximum value.
         */
        set( "count", Long.MAX_VALUE );
    }


    /**
     * Gets the Assertion used by this assertion node.
     * 
     * @return the assertion used by this node
     */
    public Assertion getAssertion()
    {
        return assertion;
    }


    // ------------------------------------------------------------------------
    // A B S T R A C T M E T H O D I M P L E M E N T A T I O N S
    // ------------------------------------------------------------------------

    /**
     * Always returns true since an AssertionNode has no children.
     * 
     * @see ExprNode#isLeaf()
     * @return true if the node is a leaf,false otherwise
     */
    @Override
    public boolean isLeaf()
    {
        return true;
    }


    /**
     * @see ExprNode#printRefinementToBuffer(StringBuilder) 
     */
    @Override
    public StringBuilder printRefinementToBuffer( StringBuilder buf )
    {
        throw new UnsupportedOperationException( I18n.err( I18n.ERR_13304_ASSERTIONNODE_IN_REFINEMENT ) );
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

        if ( !( obj instanceof AssertionNode ) )
        {
            return false;
        }
        AssertionNode that = ( AssertionNode ) obj;
        if ( assertion == null )
        {
            if ( that.assertion != null )
            {
                return false;
            }
        }
        else
        {
            if ( !assertion.equals( that.assertion ) )
            {
                return false;
            }
        }
        if ( desc == null )
        {
            if ( that.desc != null )
            {
                return false;
            }
        }
        else
        {
            if ( !desc.equals( that.desc ) )
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
        h = h * 17 + ( assertion != null ? assertion.hashCode() : 0 );
        h = h * 17 + ( desc != null ? desc.hashCode() : 0 );

        return h;
    }


    /**
     * @see ExprNode#accept(
     *FilterVisitor)
     */
    @Override
    public Object accept( FilterVisitor visitor )
    {
        return visitor.visit( this );
    }


    /**
     * @see Object#toString
     * @return A string representing the AndNode
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "(@" );
        buf.append( desc );
        buf.append( super.toString() );
        buf.append( ')' );

        return buf.toString();
    }
}
