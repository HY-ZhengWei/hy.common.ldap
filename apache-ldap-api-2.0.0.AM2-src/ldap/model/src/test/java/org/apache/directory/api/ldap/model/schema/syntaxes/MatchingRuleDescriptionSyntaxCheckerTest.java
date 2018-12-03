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

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.MatchingRuleDescriptionSyntaxChecker;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test cases for MatchingRuleDescriptionSyntaxChecker.
 * 
 * There are also many test cases in SchemaParserMatchingRuleDescriptionTest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class MatchingRuleDescriptionSyntaxCheckerTest
{
    private MatchingRuleDescriptionSyntaxChecker checker = MatchingRuleDescriptionSyntaxChecker.INSTANCE;


    @Test
    public void testValid()
    {
        assertTrue( checker.isValidSyntax( ( "( 2.5.13.5 SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 )" ) ) );
        assertTrue( checker
            .isValidSyntax( ( "( 2.5.13.5 NAME 'caseExactMatch' SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 )" ) ) );
        assertTrue( checker
            .isValidSyntax( ( "( 2.5.13.5 NAME 'caseExactMatch' DESC 'caseExactMatch' SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 )" ) ) );
        assertTrue( checker
            .isValidSyntax( ( "( 2.5.13.5 NAME 'caseExactMatch' DESC 'caseExactMatch' SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 X-ABC-DEF 'test' )" ) ) );

        // spaces
        assertTrue( checker.isValidSyntax( "(2.5.13.5 SYNTAX 1.3.6.1.4.1.1466.115.121.1.15)" ) );
        assertTrue( checker
            .isValidSyntax( "(    2.5.13.5     NAME    'caseExactMatch'     DESC    'caseExactMatch'      SYNTAX       1.3.6.1.4.1.1466.115.121.1.15     X-ABC-DEF     'test')" ) );
    }


    @Test
    public void testInvalid()
    {
        // null 
        assertFalse( checker.isValidSyntax( null ) );

        // empty 
        assertFalse( checker.isValidSyntax( "" ) );

        // missing/invalid OID
        assertFalse( checker.isValidSyntax( "()" ) );
        assertFalse( checker.isValidSyntax( "(  )" ) );
        assertFalse( checker.isValidSyntax( "( . )" ) );
        assertFalse( checker.isValidSyntax( "( 1 )" ) );
        assertFalse( checker.isValidSyntax( "( 1. )" ) );
        assertFalse( checker.isValidSyntax( "( 1.2. )" ) );
        assertFalse( checker.isValidSyntax( "( 1.A )" ) );
        assertFalse( checker.isValidSyntax( "( A.B )" ) );

        // missing right parenthesis
        assertFalse( checker.isValidSyntax( "( 2.5.13.5 SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 " ) );

        // missing quotes
        assertFalse( checker.isValidSyntax( "( 2.5.13.5 DESC Description SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 )" ) );

        // lowercase DESC
        assertFalse( checker.isValidSyntax( "( 2.5.13.5 desc 'Directory String' )" ) );

        // invalid extension
        assertFalse( checker
            .isValidSyntax( "( 2.5.13.5 DESC 'Description' SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 X-ABC-DEF )" ) );
        assertFalse( checker
            .isValidSyntax( "( 2.5.13.5 DESC 'Description' SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 X-ABC-123 'test' )" ) );

        // SYNTAX is required
        assertFalse( checker.isValidSyntax( "( 2.5.13.5 NAME 'caseExactMatch' )" ) );

    }

}
