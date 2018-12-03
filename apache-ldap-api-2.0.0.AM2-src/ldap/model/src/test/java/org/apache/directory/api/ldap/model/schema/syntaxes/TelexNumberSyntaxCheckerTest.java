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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.TelexNumberSyntaxChecker;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test cases for TelexNumberSyntaxChecker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class TelexNumberSyntaxCheckerTest
{
    TelexNumberSyntaxChecker checker = TelexNumberSyntaxChecker.INSTANCE;


    @Test
    public void testNullString()
    {
        assertFalse( checker.isValidSyntax( null ) );
    }


    @Test
    public void testEmptyString()
    {
        assertFalse( checker.isValidSyntax( "" ) );
    }


    @Test
    public void testWrongCase() throws Exception
    {
        assertFalse( checker.isValidSyntax( "test" ) );
        assertFalse( checker.isValidSyntax( "test$test" ) );
        assertFalse( checker.isValidSyntax( "test$test$" ) );
        assertFalse( checker.isValidSyntax( "test$$test" ) );
        assertFalse( checker.isValidSyntax( "$test$test" ) );
        assertFalse( checker.isValidSyntax( "test$test$test$test" ) );
        assertFalse( checker.isValidSyntax( new String( new byte[]
            { 't', 'e', 's', 't', '$', 0x00, 0x7F, ( byte ) 0x80, '$', 't', 'e', 's', 't' }, StandardCharsets.UTF_8 ) ) );
    }


    @Test
    public void testCorrectCase()
    {
        assertTrue( checker.isValidSyntax( "test$test$test" ) );
        assertTrue( checker.isValidSyntax( "t$t$t" ) );
    }
}
