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
package org.apache.directory.api.ldap.model.schema;


import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;


/**
 * The SchemaObject types
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum SchemaObjectType
{
    /** An AttributeType */
    ATTRIBUTE_TYPE(0),
    
    /** A Comparator */
    COMPARATOR(1),
    
    /** */
    DIT_CONTENT_RULE(2),
    
    /** */
    DIT_STRUCTURE_RULE(3),
    
    /** A Syntax */
    LDAP_SYNTAX(4),
    
    /** A MatchingRule */
    MATCHING_RULE(5),
    
    /** A MatchingRuleUse */
    MATCHING_RULE_USE(6),
    
    /** A NameForm */
    NAME_FORM(7),
    
    /** A Normalizer */
    NORMALIZER(8),
    
    /** An ObjectClass */
    OBJECT_CLASS(9),
    
    /** A SyntaxChecker */
    SYNTAX_CHECKER(10);

    /** The inner value*/
    private int value;


    /**
     * A private constructor to associated a number to the type
     * 
     * @param value the value
     */
    SchemaObjectType( int value )
    {
        this.value = value;
    }


    /**
     * @return The numeric value for this type
     */
    public int getValue()
    {
        return value;
    }


    /**
     * Get the Rdn associated with a schemaObjectType
     *
     * @return The associated Rdn
     */
    public String getRdn()
    {
        String schemaObjectPath;

        switch ( this )
        {
            case ATTRIBUTE_TYPE:
                schemaObjectPath = SchemaConstants.ATTRIBUTE_TYPES_PATH;
                break;

            case COMPARATOR:
                schemaObjectPath = SchemaConstants.COMPARATORS_PATH;
                break;

            case DIT_CONTENT_RULE:
                schemaObjectPath = SchemaConstants.DIT_CONTENT_RULES_PATH;
                break;

            case DIT_STRUCTURE_RULE:
                schemaObjectPath = SchemaConstants.DIT_STRUCTURE_RULES_PATH;
                break;

            case LDAP_SYNTAX:
                schemaObjectPath = SchemaConstants.SYNTAXES_PATH;
                break;

            case MATCHING_RULE:
                schemaObjectPath = SchemaConstants.MATCHING_RULES_PATH;
                break;

            case MATCHING_RULE_USE:
                schemaObjectPath = SchemaConstants.MATCHING_RULE_USE_PATH;
                break;

            case NAME_FORM:
                schemaObjectPath = SchemaConstants.NAME_FORMS_PATH;
                break;

            case NORMALIZER:
                schemaObjectPath = SchemaConstants.NORMALIZERS_PATH;
                break;

            case OBJECT_CLASS:
                schemaObjectPath = SchemaConstants.OBJECT_CLASSES_PATH;
                break;

            case SYNTAX_CHECKER:
                schemaObjectPath = SchemaConstants.SYNTAX_CHECKERS_PATH;
                break;

            default:
                throw new IllegalArgumentException( I18n.err( I18n.ERR_13718_UNEXPECTED_SCHEMA_OBJECT_TYPE, this ) );
        }

        return schemaObjectPath;
    }
}
