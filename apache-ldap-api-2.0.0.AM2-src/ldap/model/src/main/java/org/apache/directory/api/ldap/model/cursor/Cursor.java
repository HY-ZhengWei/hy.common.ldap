/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.api.ldap.model.cursor;


import java.io.Closeable;
import java.io.IOException;

import org.apache.directory.api.ldap.model.exception.LdapException;


/**
 * A Cursor for bidirectional traversal over elements in a dataSet. Cursors
 * unlike Iterators or Enumerations may advance to an element by calling
 * next() or previous() which returns true or false if the request succeeds
 * with a viable element at the new position.  Operations for relative
 * positioning in larger increments are provided.  If the cursor can not
 * advance, then the Cursor is either positioned before the first element or
 * after the last element in which case the user of the Cursor must stop
 * advancing in the respective direction.  If an advance succeeds a get()
 * operation retrieves the current object at the Cursors position.
 *
 * Although this interface presumes Cursors can advance bidirectionally,
 * implementations may restrict this by throwing
 * UnsupportedOperationExceptions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @param <E> The type of element on which this cursor will iterate
 */
public interface Cursor<E> extends Iterable<E>, Closeable
{
    /**
     * Determines whether or not a call to get() will succeed.
     *
     * @return true if a call to the get() method will succeed, false otherwise
     */
    boolean available();


    /**
     * Prepares this Cursor, so a subsequent call to Cursor#next() with a
     * true return value, will have positioned the Cursor on a dataSet 
     * element equal to or less than the element argument but not greater.  
     * A call to Cursor#previous() with a true return value will position 
     * the Cursor on a dataSet element less than the argument.  If 
     * Cursor#next() returns false then the Cursor is past the last element 
     * and so all values in the dataSet are less than the argument.  If 
     * Cursor#previous() returns false then the Cursor is positioned before 
     * the first element and all elements in the dataSet are greater than 
     * the argument.
     *
     * @param element the element to be positioned before
     * @throws LdapException if we have get any LDAP exception while operating
     * the cursor
     * @throws CursorException if there are problems advancing to this Cursor before
     * the given element, or if this Cursor is closed 
     */
    void before( E element ) throws LdapException, CursorException;


    /**
     * Prepares this Cursor, so a subsequent call to Cursor#previous() with a
     * true return value, will have positioned the Cursor on a dataSet element
     * equal to or less than the element argument but not greater. A call to
     * Cursor#next() with a true return value will position the Cursor on a
     * dataSet element greater than the argument.  If Cursor#next() returns
     * false then the Cursor is past the last element and so all values in the
     * dataSet are less than or equal to the argument.  If Cursor#previous()
     * returns false then the Cursor is positioned before the first element
     * and all elements in the dataSet are greater than the argument.
     *
     * @param element the element to be positioned after
     * @throws LdapException if we have get any LDAP exception while operating
     * the cursor
     * @throws CursorException if there are problems advancing to this Cursor after
     * the given element, or if this Cursor is closed 
     */
    void after( E element ) throws LdapException, CursorException;


    /**
     * Positions this Cursor before the first element.
     *
     * @throws LdapException if we have get any LDAP exception while operating
     * the cursor
     * @throws CursorException if there are problems advancing to this Cursor before
     * the first position, or if this Cursor is closed 
     */
    void beforeFirst() throws LdapException, CursorException;


    /**
     * Positions this Cursor after the last element.
     *
     * @throws LdapException if we have get any LDAP exception while operating
     * the cursor
     * @throws CursorException if there are problems advancing to this Cursor after
     * the last position, or if this Cursor is closed 
     */
    void afterLast() throws LdapException, CursorException;


    /**
     * Positions this Cursor at the first element.
     *
     * @return true if the position has been successfully changed to the first
     * element, false otherwise
     * @throws LdapException if we have get any LDAP exception while operating
     * the cursor
     * @throws CursorException if there are problems advancing to this Cursor to
     * the first position, or if this Cursor is closed 
     */
    boolean first() throws LdapException, CursorException;


    /**
     * Is this Cursor positioned at the first element.
     *
     * @return true if this cursor is positioned at the first element, 
     * false otherwise
     */
    boolean isFirst();


    /**
     * Is this Cursor positioned before the first element.
     *
     * @return true if this cursor is positioned before the first element, 
     * false otherwise
     */
    boolean isBeforeFirst();


    /**
     * Positions this Cursor at the last element.
     *
     * @return true if the position has been successfully changed to the last
     * element, false otherwise
     * @throws LdapException if we have get any LDAP exception while operating
     * the cursor
     * @throws CursorException if there are problems advancing to this Cursor to
     * the last position, or if this Cursor is closed 
     */
    boolean last() throws LdapException, CursorException;


    /**
     * Is this Cursor positioned at the last element.
     *
     * @return true if this cursor is positioned at the last element, 
     * false otherwise
     */
    boolean isLast();


    /**
     * Is this Cursor positioned after the last element.
     *
     * @return true if this cursor is positioned after the last element, 
     * false otherwise
     */
    boolean isAfterLast();


    /**
     * Checks if this Cursor is closed.  Calls to this operation should not
     * fail with exceptions if and only if the cursor is in the closed state.
     *
     * @return true if this Cursor is closed, false otherwise
     */
    boolean isClosed();


    /**
     * Advances this Cursor to the previous position.  If called before
     * explicitly positioning this Cursor, the position is presumed to be
     * after the last element and this method moves the cursor back to the
     * last element.
     *
     * @return true if the advance succeeded, false otherwise
     * @throws LdapException if we have get any LDAP exception while operating
     * the cursor
     * @throws CursorException if there are problems advancing to this Cursor to
     * the previous position, or if this Cursor is closed 
     */
    boolean previous() throws LdapException, CursorException;


    /**
     * Advances this Cursor to the next position.  If called before
     * explicitly positioning this Cursor, the position is presumed to be
     * before the first element and this method moves the cursor forward to
     * the first element.
     *
     * @return true if the advance succeeded, false otherwise
     * @throws LdapException if we have get any LDAP exception while operating
     * the cursor
     * @throws CursorException if there are problems advancing to this Cursor to
     * the next position, or if this Cursor is closed 
     */
    boolean next() throws LdapException, CursorException;


    /**
     * Gets the object at the current position.  Cursor implementations may
     * choose to reuse element objects by re-populating them on advances
     * instead of creating new objects on each advance.
     *
     * @return the object at the current position
     * @throws CursorException if the object at this Cursor's current position
     * cannot be retrieved, or if this Cursor is closed
     */
    E get() throws CursorException;


    /**
     * Closes this Cursor and frees any resources it my have allocated.
     * Repeated calls to this method after this Cursor has already been
     * called should not fail with exceptions.  The reason argument is 
     * the Exception instance thrown instead of the standard 
     * CursorClosedException.
     *
     * @param reason exception thrown when this Cursor is accessed after close
     * @throws IOException If we can't close the Cursor 
     */
    void close( Exception reason ) throws IOException;


    /**
     * Sets a non-null closure monitor to associate with this Cursor.
     *
     * @param monitor the monitor to use for detecting Cursor close events
     */
    void setClosureMonitor( ClosureMonitor monitor );


    /**
     * Pretty-print a cursor and its wrapped cursor.
     * @param tabs The spaces to add at each level
     * @return The cursor and all it's wrapped elements, recursively printed
     */
    String toString( String tabs );
}
