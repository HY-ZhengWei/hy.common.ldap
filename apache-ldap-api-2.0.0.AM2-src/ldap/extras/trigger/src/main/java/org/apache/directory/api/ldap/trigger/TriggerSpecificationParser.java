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

package org.apache.directory.api.ldap.trigger;


import java.io.StringReader;
import java.text.ParseException;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.schema.Normalizer;
import org.apache.directory.api.ldap.model.schema.NormalizerMappingResolver;
import org.apache.directory.api.util.Strings;

import antlr.RecognitionException;
import antlr.TokenStreamException;


/**
 * A reusable wrapper around the ANTLR generated parser for a
 * TriggerSpecification. This class enables the reuse of the antlr parser/lexer
 * pair without having to recreate them every time.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TriggerSpecificationParser
{
    /** the antlr generated parser being wrapped */
    private ReusableAntlrTriggerSpecificationParser parser;

    /** the antlr generated lexer being wrapped */
    private ReusableAntlrTriggerSpecificationLexer lexer;

    private final boolean isNormalizing;


    /**
     * Creates a TriggerSpecification parser.
     */
    public TriggerSpecificationParser()
    {
        this.lexer = new ReusableAntlrTriggerSpecificationLexer( new StringReader( "" ) );
        this.parser = new ReusableAntlrTriggerSpecificationParser( lexer );

        // this method MUST be called while we cannot do
        // constructor overloading for antlr generated parser
        this.parser.init();
        this.isNormalizing = false;
    }


    /**
     * Creates a normalizing TriggerSpecification parser.
     *
     * @param resolver the resolver
     */
    public TriggerSpecificationParser( NormalizerMappingResolver<Normalizer> resolver )
    {
        this.lexer = new ReusableAntlrTriggerSpecificationLexer( new StringReader( "" ) );
        this.parser = new ReusableAntlrTriggerSpecificationParser( lexer );

        this.parser.setNormalizerMappingResolver( resolver );
        // this method MUST be called while we cannot do
        // constructor overloading for ANTLR generated parser
        this.parser.init();
        this.isNormalizing = true;
    }


    /**
     * Initializes the plumbing by creating a pipe and coupling the parser/lexer
     * pair with it.
     * 
     * @param
     *          spec the specification to be parsed
     */
    private synchronized void reset( String spec )
    {
        StringReader in = new StringReader( spec );
        this.lexer.prepareNextInput( in );
        this.parser.resetState();
    }


    /**
     * Parses an TriggerSpecification without exhausting the parser.
     * 
     * @param spec
     *          the specification to be parsed
     * @return the specification bean
     * @throws ParseException
     *          if there are any recognition errors (bad syntax)
     */
    public synchronized TriggerSpecification parse( String spec ) throws ParseException
    {
        TriggerSpecification triggerSpecification;

        if ( Strings.isEmpty( spec ) )
        {
            return null;
        }

        // reset and initialize the parser / lexer pair
        reset( spec );

        try
        {
            triggerSpecification = this.parser.wrapperEntryPoint();
        }
        catch ( TokenStreamException e )
        {
            String msg = I18n.err( I18n.ERR_11002_TRIGGER_SPECIFICATION_PARSER_FAILURE, spec, e.getLocalizedMessage() );
            
            throw new ParseException( msg, 0 );
        }
        catch ( RecognitionException e )
        {
            String msg = I18n.err( I18n.ERR_11002_TRIGGER_SPECIFICATION_PARSER_FAILURE, spec, e.getLocalizedMessage() );
            
            throw new ParseException( msg, e.getColumn() );
        }

        return triggerSpecification;

    }


    /**
     * Tests to see if this parser is normalizing.
     * 
     * @return true if it normalizes false otherwise
     */
    public boolean isNormizing()
    {
        return this.isNormalizing;
    }
}
