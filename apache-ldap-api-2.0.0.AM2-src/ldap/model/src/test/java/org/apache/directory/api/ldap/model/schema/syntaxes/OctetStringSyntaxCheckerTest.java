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
package org.apache.directory.api.ldap.model.schema.syntaxes;


import static org.junit.Assert.assertTrue;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.OctetStringSyntaxChecker;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test cases for OctetStringSyntaxChecker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class OctetStringSyntaxCheckerTest
{
    OctetStringSyntaxChecker checker = OctetStringSyntaxChecker.INSTANCE;


    @Test
    public void testNullString()
    {
        assertTrue( checker.isValidSyntax( null ) );
    }


    @Test
    public void testEmptyOctetString()
    {
        assertTrue( checker.isValidSyntax( Strings.EMPTY_BYTES ) );
    }


    @Test
    public void testStringOctetString()
    {
        assertTrue( checker.isValidSyntax( "" ) );
    }


    @Test
    public void testCorrectCase()
    {
        byte[] array = new byte[256];

        for ( int i = 0; i < 256; i++ )
        {
            array[i] = ( byte ) i;
        }

        assertTrue( checker.isValidSyntax( array ) );
    }
}
