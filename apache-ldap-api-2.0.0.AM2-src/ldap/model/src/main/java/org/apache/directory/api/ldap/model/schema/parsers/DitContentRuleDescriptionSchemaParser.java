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
import org.apache.directory.api.ldap.model.schema.DitContentRule;

/**
 * A parser for RFC 4512 DIT content rule descriptions.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DitContentRuleDescriptionSchemaParser extends AbstractSchemaParser<DitContentRule>
{

    /**
     * Creates a schema parser instance.
     */
    public DitContentRuleDescriptionSchemaParser()
    {
        super( DitContentRule.class, I18n.ERR_13833_CANNOT_PARSE_NULL_DCR, I18n.ERR_13834_DCR_PARSING_FAILURE, I18n.ERR_13385_DCR_DESC_PARSING_FAILURE );
    }


    /**
     * Parses a DIT content rule description according to RFC 4512:
     * 
     * <pre>
     * DITContentRuleDescription = LPAREN WSP
     *    numericoid                 ; object identifier
     *    [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     *    [ SP "DESC" SP qdstring ]  ; description
     *    [ SP "OBSOLETE" ]          ; not active
     *    [ SP "AUX" SP oids ]       ; auxiliary object classes
     *    [ SP "MUST" SP oids ]      ; attribute types
     *    [ SP "MAY" SP oids ]       ; attribute types
     *    [ SP "NOT" SP oids ]       ; attribute types
     *    extensions WSP RPAREN      ; extensions
     * </pre>
     * 
     * @param ditContentRuleDescription the DIT content rule description to be parsed
     * @return the parsed DITContentRuleDescription bean
     * @throws ParseException if there are any recognition errors (bad syntax)
     */
    public DitContentRule parse( String ditContentRuleDescription ) throws ParseException
    {
        DitContentRule ditContentRule = fastParser.parseDitContentRule( ditContentRuleDescription );
        ditContentRule.setSpecification( ditContentRuleDescription );

        // Update the schemaName
        updateSchemaName( ditContentRule );

        return ditContentRule;
    }
}
