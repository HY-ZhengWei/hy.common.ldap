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
package org.apache.directory.api.ldap.model.schema.parsers;


import java.text.ParseException;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.schema.MatchingRule;


/**
 * A parser for RFC 4512 matching rule descriptions.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MatchingRuleDescriptionSchemaParser extends AbstractSchemaParser<MatchingRule>
{
    /**
     * Creates a schema parser instance.
     */
    public MatchingRuleDescriptionSchemaParser()
    {
        super( MatchingRule.class, I18n.ERR_13395_CANNOT_PARSE_NULL_MR, I18n.ERR_13396_MR_PARSING_FAILURE, I18n.ERR_13397_MR_DESC_PARSING_FAILURE );
    }


    /**
     * Parses a matching rule description according to RFC 4512:
     * 
     * <pre>
     * MatchingRuleDescription = LPAREN WSP
     *    numericoid                 ; object identifier
     *    [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     *    [ SP "DESC" SP qdstring ]  ; description
     *    [ SP "OBSOLETE" ]          ; not active
     *    SP "SYNTAX" SP numericoid  ; assertion syntax
     *    extensions WSP RPAREN      ; extensions
     * 
     * extensions = *( SP xstring SP qdstrings )
     * xstring = "X" HYPHEN 1*( ALPHA / HYPHEN / USCORE ) 
     * </pre>
     * 
     * @param matchingRuleDescription the matching rule description to be parsed
     * @return the parsed MatchingRuleDescription bean
     * @throws ParseException if there are any recognition errors (bad syntax)
     */
    public MatchingRule parse( String matchingRuleDescription ) throws ParseException
    {
        MatchingRule matchingRule = fastParser.parseMatchingRule( matchingRuleDescription );
        matchingRule.setSpecification( matchingRuleDescription );

        // Update the schemaName
        updateSchemaName( matchingRule );

        return matchingRule;
    }
}
