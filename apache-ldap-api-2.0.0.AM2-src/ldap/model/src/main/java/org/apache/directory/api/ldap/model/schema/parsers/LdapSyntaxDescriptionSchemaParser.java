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
import org.apache.directory.api.ldap.model.schema.LdapSyntax;


/**
 * A parser for RFC 4512 LDAP syntx descriptions.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapSyntaxDescriptionSchemaParser extends AbstractSchemaParser<LdapSyntax>
{

    /**
     * Creates a schema parser instance.
     */
    public LdapSyntaxDescriptionSchemaParser()
    {
        super( LdapSyntax.class, I18n.ERR_13392_CANNOT_PARSE_NULL_SYNTAX, I18n.ERR_13393_SYNTAX_PARSING_FAILURE, I18n.ERR_13394_SYNTAX_DESC_PARSING_FAILURE );
    }


    /**
     * Parses a LDAP syntax description according to RFC 4512:
     * 
     * <pre>
     * SyntaxDescription = LPAREN WSP
     *    numericoid                 ; object identifier
     *    [ SP "DESC" SP qdstring ]  ; description
     *    extensions WSP RPAREN      ; extensions
     * </pre>
     * 
     * @param ldapSyntaxDescription the LDAP syntax description to be parsed
     * @return the parsed LdapSyntax bean
     * @throws ParseException if there are any recognition errors (bad syntax)
     */
    public LdapSyntax parse( String ldapSyntaxDescription ) throws ParseException
    {
        LdapSyntax ldapSyntax = fastParser.parseLdapSyntax( ldapSyntaxDescription );
        ldapSyntax.setSpecification( ldapSyntaxDescription );

        // Update the schemaName
        updateSchemaName( ldapSyntax );

        return ldapSyntax;
    }
}
