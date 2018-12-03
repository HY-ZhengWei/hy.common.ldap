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
package org.apache.directory.api.util;


import javax.naming.NamingEnumeration;

import java.util.NoSuchElementException;


/**
 * A NamingEnumeration over a single element.
 * 
 * @param <T> The element in the enumeration
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingletonEnumeration<T> implements NamingEnumeration<T>
{
    /** The singleton element to return */
    private final T element;

    /** Can we return a element */
    private boolean hasMore = true;


    /**
     * Creates a NamingEnumeration over a single element.
     * 
     * @param element The element to store in this enumeration
     */
    public SingletonEnumeration( final T element )
    {
        this.element = element;
    }


    /**
     * Makes calls to hasMore to false even if we had more.
     * 
     * @see javax.naming.NamingEnumeration#close()
     */
    @Override
    public void close()
    {
        hasMore = false;
    }


    /**
     * @see javax.naming.NamingEnumeration#hasMore()
     */
    @Override
    public boolean hasMore()
    {
        return hasMore;
    }


    /**
     * @see javax.naming.NamingEnumeration#next()
     */
    @Override
    public T next()
    {
        if ( hasMore )
        {
            hasMore = false;
            return element;
        }

        throw new NoSuchElementException();
    }


    /**
     * @see java.util.Enumeration#hasMoreElements()
     */
    @Override
    public boolean hasMoreElements()
    {
        return hasMore;
    }


    /**
     * @see java.util.Enumeration#nextElement()
     */
    @Override
    public T nextElement()
    {
        return next();
    }
}
