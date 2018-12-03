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

package org.apache.directory.api.ldap.aci;


import java.io.Reader;

import antlr.CharBuffer;
import antlr.LexerSharedInputState;


/**
 * A reusable lexer class extended from antlr generated lexer for an LDAP
 * subtree specification as defined by <a
 * href="http://www.faqs.org/rfcs/rfc3672.html"> RFC 3672</a>. This class
 * enables the reuse of the antlr lexer without having to recreate the it every
 * time as stated in <a
 * href="http://www.antlr.org:8080/pipermail/antlr-interest/2003-April/003631.html">
 * a Antlr Interest Group mail</a> .
 * 
 * @see <a href="http://www.faqs.org/rfcs/rfc3672.html">RFC 3672</a>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
class ReusableAntlrACIItemCheckerLexer extends AntlrACIItemCheckerLexer
{

    /** The saved case sensitive flag. */
    private boolean savedCaseSensitive;

    /** The saved case sensitive literals flag. */
    private boolean savedCaseSensitiveLiterals;


    /**
     * Creates a ReusableAntlrACIItemCheckerLexer instance.
     * 
     * @param in
     *            the input to the lexer
     */
    ReusableAntlrACIItemCheckerLexer( Reader in )
    {
        super( in );
        savedCaseSensitive = getCaseSensitive();
        savedCaseSensitiveLiterals = getCaseSensitiveLiterals();
    }


    /**
     * Resets the state of an antlr lexer and initializes it with new input.
     * 
     * @param in
     *            the input to the lexer
     */
    public void prepareNextInput( Reader in )
    {
        CharBuffer buf = new CharBuffer( in );
        LexerSharedInputState state = new LexerSharedInputState( buf );
        this.setInputState( state );

        this.setCaseSensitive( savedCaseSensitive );

        // no set method for this protected field.
        this.caseSensitiveLiterals = savedCaseSensitiveLiterals;
    }
}
