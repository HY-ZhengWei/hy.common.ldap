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


import java.util.List;

import org.apache.directory.api.i18n.I18n;


/**
 * Node representing an Not connector in a filter operation
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NotNode extends BranchNode
{
    /**
     * Creates a NotNode using a logical NOT operator and a list of children.
     * 
     * A Not node could contain only one child
     * 
     * @param childList the child nodes under this branch node.
     */
    public NotNode( List<ExprNode> childList )
    {
        super( AssertionType.NOT );

        if ( childList != null )
        {
            setChildren( childList );
        }
    }


    /**
     * Creates a NotNode using a logical NOT operator and the given child.
     * 
     * @param child the child node under this branch node.
     */
    public NotNode( ExprNode child )
    {
        super( AssertionType.NOT );

        if ( child != null )
        {
            addNode( child );
        }
    }


    /**
     * Creates an empty NotNode
     */
    public NotNode()
    {
        this( ( ExprNode ) null );
    }


    /**
     * Adds a child node to this NOT node node
     * 
     * @param node the child expression to add to this NOT node
     */
    @Override
    public void addNode( ExprNode node )
    {
        if ( ( children != null ) && children.isEmpty() )
        {
            children.add( node );
        }
        else
        {
            throw new IllegalStateException( I18n.err( I18n.ERR_13317_NO_MORE_THAN_ONE_ELEM_IN_NOT ) );
        }
    }


    /**
     * Adds a child node to this NOT node at the head rather than the tail. 
     * 
     * @param node the child expression to add to this branch node
     */
    @Override
    public void addNodeToHead( ExprNode node )
    {
        if ( ( children != null ) && children.isEmpty() )
        {
            children.add( node );
        }
        else
        {
            throw new IllegalStateException( I18n.err( I18n.ERR_13317_NO_MORE_THAN_ONE_ELEM_IN_NOT ) );
        }
    }


    /**
     * Sets the list of children under this node.
     * 
     * @param childList the list of children to set.
     */
    @Override
    public void setChildren( List<ExprNode> childList )
    {
        if ( ( childList != null ) && ( childList.size() > 1 ) )
        {
            throw new IllegalStateException( I18n.err( I18n.ERR_13317_NO_MORE_THAN_ONE_ELEM_IN_NOT ) );
        }

        children = childList;
    }


    /**
     * Gets the operator for this branch node.
     * 
     * @return the operator constant.
     */
    public AssertionType getOperator()
    {
        return AssertionType.NOT;
    }


    /**
     * Tests whether or not this node is a disjunction (a OR'ed branch).
     * 
     * @return true if the operation is a OR, false otherwise.
     */
    public boolean isDisjunction()
    {
        return false;
    }


    /**
     * Tests whether or not this node is a conjunction (a AND'ed branch).
     * 
     * @return true if the operation is a AND, false otherwise.
     */
    public boolean isConjunction()
    {
        return false;
    }


    /**
     * Tests whether or not this node is a negation (a NOT'ed branch).
     * 
     * @return true if the operation is a NOT, false otherwise.
     */
    public boolean isNegation()
    {
        return true;
    }


    /**
     * @see ExprNode#printRefinementToBuffer(StringBuilder)
     * 
     * @return The buffer in which the refinement has been appended
     * @throws UnsupportedOperationException if this node isn't a part of a refinement.
     */
    @Override
    public StringBuilder printRefinementToBuffer( StringBuilder buf )
    {
        buf.append( "not: " );

        // There is only one item for a not refinement
        children.get( 0 ).printRefinementToBuffer( buf );

        return buf;
    }


    /**
     * Gets the recursive prefix string represent of the filter from this node
     * down.
     * 
     * @see java.lang.Object#toString()
     * @return A string representing the AndNode
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "(!" );

        buf.append( super.toString() );

        buf.append( getFirstChild() );
        buf.append( ')' );

        return buf.toString();
    }
}
