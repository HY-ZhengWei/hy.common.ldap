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


import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;

import org.apache.directory.api.ldap.model.filter.FilterParser;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test the Filter.toString() method
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class FilterToStringTest
{
    @Test
    public void testSimpleToString() throws ParseException
    {
        String str = "(ou=test)";
        ExprNode node = FilterParser.parse( str );
        assertEquals( str, node.toString() );

        str = "(ou~=test)";
        node = FilterParser.parse( str );
        assertEquals( str, node.toString() );

        str = "(ou>=test)";
        node = FilterParser.parse( str );
        assertEquals( str, node.toString() );

        str = "(ou<=test)";
        node = FilterParser.parse( str );
        assertEquals( str, node.toString() );

        str = "(ou=)";
        node = FilterParser.parse( str );
        assertEquals( str, node.toString() );

        str = "(ou~=)";
        node = FilterParser.parse( str );
        assertEquals( str, node.toString() );

        str = "(ou>=)";
        node = FilterParser.parse( str );
        assertEquals( str, node.toString() );

        str = "(ou<=)";
        node = FilterParser.parse( str );
        assertEquals( str, node.toString() );
    }


    @Test
    public void testToStringWithEscaped() throws ParseException
    {
        String str = "(cn=jims group\\28not bob\\29)";
        ExprNode node = FilterParser.parse( str );
        assertEquals( str, node.toString() );
    }
}
