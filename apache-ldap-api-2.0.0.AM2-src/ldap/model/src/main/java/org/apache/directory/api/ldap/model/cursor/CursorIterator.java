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
package org.apache.directory.api.ldap.model.cursor;


import java.util.Iterator;

import org.apache.directory.api.i18n.I18n;


/**
 * An Iterator over a Cursor so Cursors can be Iterable for using in foreach
 * constructs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @param <E> The type of element on which this cursor will iterate
 */
public class CursorIterator<E> implements Iterator<E>
{
    /** The inner cursor we will iterate */
    private final Cursor<E> cursor;

    /** A flag used to store the cursor state */
    private boolean available;


    /**
     * Creates a new instance of CursorIterator.
     *
     * @param cursor The inner cursor
     */
    public CursorIterator( Cursor<E> cursor )
    {
        this.cursor = cursor;

        try
        {
            this.available = cursor.next();
        }
        catch ( Exception e )
        {
            this.available = false;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext()
    {
        return available;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public E next()
    {
        try
        {
            E element = cursor.get();
            available = cursor.next();
            
            return element;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( I18n.err( I18n.ERR_13100_FAILURE_ON_UNDERLYING_CURSOR ), e );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException( I18n.err( I18n.ERR_13103_REMOVAL_NOT_SUPPORTED ) );
    }
}
