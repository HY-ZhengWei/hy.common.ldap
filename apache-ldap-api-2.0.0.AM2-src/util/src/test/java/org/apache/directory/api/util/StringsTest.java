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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Tests the Strings class methods.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class StringsTest
{
    private static final byte[] AZERTY = "azerty".getBytes( StandardCharsets.US_ASCII );


    @Test
    public void testTrimConsecutiveToOne()
    {
        String input = null;
        String result = null;

        input = "akarasulu**";
        result = Strings.trimConsecutiveToOne( input, '*' );
        assertEquals( "akarasulu*", result );

        input = "*****akarasulu**";
        result = Strings.trimConsecutiveToOne( input, '*' );
        assertEquals( "*akarasulu*", result );

        input = "**akarasulu";
        result = Strings.trimConsecutiveToOne( input, '*' );
        assertEquals( "*akarasulu", result );

        input = "**akar****asulu**";
        result = Strings.trimConsecutiveToOne( input, '*' );
        assertEquals( "*akar*asulu*", result );

        input = "akarasulu";
        result = Strings.trimConsecutiveToOne( input, '*' );
        assertEquals( "akarasulu", result );

        input = "*a*k*a*r*a*s*u*l*u*";
        result = Strings.trimConsecutiveToOne( input, '*' );
        assertEquals( "*a*k*a*r*a*s*u*l*u*", result );

    }


    @Test
    public void testListToString()
    {
        List<String> list = new ArrayList<String>();

        list.add( "elem1" );
        list.add( "elem2" );
        list.add( "elem3" );

        assertEquals( "elem1, elem2, elem3", Strings.listToString( list ) );
    }


    @Test
    public void testMapToString()
    {
        class Value
        {
            String name;

            int val;


            public Value( String name, int val )
            {
                this.name = name;
                this.val = val;
            }


            public String toString()
            {
                return "[" + name + ", " + val + "]";
            }
        }

        Map<String, Value> map = new HashMap<String, Value>();

        map.put( "elem1", new Value( "name1", 1 ) );
        map.put( "elem2", new Value( "name2", 2 ) );
        map.put( "elem3", new Value( "name3", 3 ) );

        String result = Strings.mapToString( map );

        boolean res = "elem1 = '[name1, 1]', elem2 = '[name2, 2]', elem3 = '[name3, 3]'".equals( result )
            || "elem1 = '[name1, 1]', elem3 = '[name3, 3]', elem2 = '[name2, 2]'".equals( result )
            || "elem2 = '[name2, 2]', elem1 = '[name1, 1]', elem3 = '[name3, 3]'".equals( result )
            || "elem2 = '[name2, 2]', elem3 = '[name3, 3]', elem1 = '[name1, 1]'".equals( result )
            || "elem3 = '[name3, 3]', elem1 = '[name1, 1]', elem2 = '[name2, 2]'".equals( result )
            || "elem3 = '[name3, 3]', elem2 = '[name2, 2]', elem1 = '[name1, 1]'".equals( result );

        assertTrue( res );
    }


    @Test
    public void testDeepTrim()
    {
        assertEquals( "", Strings.deepTrim( " ", false ) );
        assertEquals( "ab", Strings.deepTrim( " ab ", false ) );
        assertEquals( "a b", Strings.deepTrim( " a b ", false ) );
        assertEquals( "a b", Strings.deepTrim( " a  b ", false ) );
        assertEquals( "a b", Strings.deepTrim( "  a  b  ", false ) );
        assertEquals( "ab", Strings.deepTrim( "ab ", false ) );
        assertEquals( "ab", Strings.deepTrim( " ab", false ) );
        assertEquals( "ab", Strings.deepTrim( "ab  ", false ) );
        assertEquals( "ab", Strings.deepTrim( "  ab", false ) );
        assertEquals( "a b", Strings.deepTrim( "a b", false ) );
        assertEquals( "a b", Strings.deepTrim( "a  b", false ) );
        assertEquals( "a b", Strings.deepTrim( " a b", false ) );
        assertEquals( "a b", Strings.deepTrim( "a b ", false ) );
    }


    @Test
    public void testTrim()
    {
        assertEquals( "", Strings.trim( ( String ) null ) );
        assertEquals( "", Strings.trim( "" ) );
        assertEquals( "", Strings.trim( " " ) );
        assertEquals( "", Strings.trim( "  " ) );
        assertEquals( "a", Strings.trim( "a  " ) );
        assertEquals( "a", Strings.trim( "  a" ) );
        assertEquals( "a", Strings.trim( "  a  " ) );
    }


    @Test
    public void testTrimLeft()
    {
        assertEquals( "", Strings.trimLeft( ( String ) null ) );
        assertEquals( "", Strings.trimLeft( "" ) );
        assertEquals( "", Strings.trimLeft( " " ) );
        assertEquals( "", Strings.trimLeft( "  " ) );
        assertEquals( "a  ", Strings.trimLeft( "a  " ) );
        assertEquals( "a", Strings.trimLeft( "  a" ) );
        assertEquals( "a  ", Strings.trimLeft( "  a  " ) );
    }


    @Test
    public void testTrimRight()
    {
        assertEquals( "", Strings.trimRight( ( String ) null ) );
        assertEquals( "", Strings.trimRight( "" ) );
        assertEquals( "", Strings.trimRight( " " ) );
        assertEquals( "", Strings.trimRight( "  " ) );
        assertEquals( "a", Strings.trimRight( "a  " ) );
        assertEquals( "  a", Strings.trimRight( "  a" ) );
        assertEquals( "  a", Strings.trimRight( "  a  " ) );
    }


    @Test
    public void testConvertUUID()
    {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = Strings.uuidToBytes( uuid.toString() );
        String string = Strings.uuidToString( bytes );
        assertEquals( uuid.toString(), string );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsFull()
    {
        // Full compare
        assertEquals( 6, Strings.areEquals( AZERTY, 0, "azerty" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsDiff()
    {
        // First character is !=
        assertEquals( -1, Strings.areEquals( AZERTY, 0, "Azerty" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsEmpty()
    {
        // Compare to an empty string
        assertEquals( -1, Strings.areEquals( AZERTY, 0, "" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsFirstCharDiff()
    {
        // First character is !=
        assertEquals( -1, Strings.areEquals( AZERTY, 0, "Azerty" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsMiddleCharDiff()
    {
        // First character is !=
        assertEquals( -1, Strings.areEquals( AZERTY, 0, "azeRty" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsLastCharDiff()
    {
        // First character is !=
        assertEquals( -1, Strings.areEquals( AZERTY, 0, "azertY" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsCharByChar()
    {
        // Index must be incremented after each comparison
        assertEquals( 1, Strings.areEquals( AZERTY, 0, "a" ) );
        assertEquals( 2, Strings.areEquals( AZERTY, 1, "z" ) );
        assertEquals( 3, Strings.areEquals( AZERTY, 2, "e" ) );
        assertEquals( 4, Strings.areEquals( AZERTY, 3, "r" ) );
        assertEquals( 5, Strings.areEquals( AZERTY, 4, "t" ) );
        assertEquals( 6, Strings.areEquals( AZERTY, 5, "y" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsTooShort()
    {
        // length too short
        assertEquals( -1, Strings.areEquals( AZERTY, 0, "azertyiop" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsTooShortMiddle()
    {
        // length too short
        assertEquals( -1, Strings.areEquals( AZERTY, 0, "ertyiop" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsLastChar()
    {
        // last character
        assertEquals( 6, Strings.areEquals( AZERTY, 5, "y" ) );
    }


    /**
     * Test the DnUtils AreEquals method
     */
    @Test
    public void testAreEqualsMiddle()
    {
        // In the middle
        assertEquals( 4, Strings.areEquals( AZERTY, 2, "er" ) );
    }
}
