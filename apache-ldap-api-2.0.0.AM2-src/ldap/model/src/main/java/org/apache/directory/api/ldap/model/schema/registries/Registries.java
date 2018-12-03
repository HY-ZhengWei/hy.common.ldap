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
package org.apache.directory.api.ldap.model.schema.registries;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.constants.MetaSchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapProtocolErrorException;
import org.apache.directory.api.ldap.model.exception.LdapSchemaException;
import org.apache.directory.api.ldap.model.exception.LdapSchemaExceptionCodes;
import org.apache.directory.api.ldap.model.exception.LdapSchemaViolationException;
import org.apache.directory.api.ldap.model.exception.LdapUnwillingToPerformException;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.DitContentRule;
import org.apache.directory.api.ldap.model.schema.DitStructureRule;
import org.apache.directory.api.ldap.model.schema.LdapComparator;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.LoadableSchemaObject;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.MatchingRuleUse;
import org.apache.directory.api.ldap.model.schema.MutableAttributeType;
import org.apache.directory.api.ldap.model.schema.MutableMatchingRule;
import org.apache.directory.api.ldap.model.schema.NameForm;
import org.apache.directory.api.ldap.model.schema.Normalizer;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaErrorHandler;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.ldap.model.schema.SchemaObjectWrapper;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;
import org.apache.directory.api.ldap.model.schema.registries.helper.AttributeTypeHelper;
import org.apache.directory.api.ldap.model.schema.registries.helper.DitContentRuleHelper;
import org.apache.directory.api.ldap.model.schema.registries.helper.DitStructureRuleHelper;
import org.apache.directory.api.ldap.model.schema.registries.helper.LdapSyntaxHelper;
import org.apache.directory.api.ldap.model.schema.registries.helper.MatchingRuleHelper;
import org.apache.directory.api.ldap.model.schema.registries.helper.MatchingRuleUseHelper;
import org.apache.directory.api.ldap.model.schema.registries.helper.NameFormHelper;
import org.apache.directory.api.ldap.model.schema.registries.helper.ObjectClassHelper;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Document this class.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Registries implements SchemaLoaderListener, Cloneable
{
    /** A logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( Registries.class );

    /**
     * A String name to Schema object map for the schemas loaded into this
     * registry. The loaded schemas may be disabled.
     */
    protected Map<String, Schema> loadedSchemas = new HashMap<>();

    /** The AttributeType registry */
    protected DefaultAttributeTypeRegistry attributeTypeRegistry;

    /** The ObjectClass registry */
    protected DefaultObjectClassRegistry objectClassRegistry;

    /** The LdapSyntax registry */
    protected DefaultComparatorRegistry comparatorRegistry;

    /** The DitContentRule registry */
    protected DefaultDitContentRuleRegistry ditContentRuleRegistry;

    /** The DitStructureRule registry */
    protected DefaultDitStructureRuleRegistry ditStructureRuleRegistry;

    /** The MatchingRule registry */
    protected DefaultMatchingRuleRegistry matchingRuleRegistry;

    /** The MatchingRuleUse registry */
    protected DefaultMatchingRuleUseRegistry matchingRuleUseRegistry;

    /** The NameForm registry */
    protected DefaultNameFormRegistry nameFormRegistry;

    /** The Normalizer registry */
    protected DefaultNormalizerRegistry normalizerRegistry;

    /** The global OID registry */
    protected OidRegistry<SchemaObject> globalOidRegistry;

    /** The SyntaxChecker registry */
    protected DefaultSyntaxCheckerRegistry syntaxCheckerRegistry;

    /** The LdapSyntax registry */
    protected DefaultLdapSyntaxRegistry ldapSyntaxRegistry;

    /** A map storing all the schema objects associated with a schema */
    private Map<String, Set<SchemaObjectWrapper>> schemaObjects;

    /** A flag indicating that the Registries is relaxed or not */
    private boolean isRelaxed;

    /** A flag indicating that disabled SchemaObject are accepted */
    private boolean disabledAccepted;

    private SchemaErrorHandler errorHandler;

    /** Two flags for RELAXED and STRICT modes */
    /** The strict mode */
    public static final boolean STRICT = false;
    
    /** The relaxed mode */
    public static final boolean RELAXED = true;

    /**
     *  A map storing a relation between a SchemaObject and all the
     *  referencing SchemaObjects.
     */
    protected Map<SchemaObjectWrapper, Set<SchemaObjectWrapper>> usedBy;

    /**
     *  A map storing a relation between a SchemaObject and all the
     *  SchemaObjects it uses.
     */
    protected Map<SchemaObjectWrapper, Set<SchemaObjectWrapper>> using;


    /**
     * Creates a new instance of Registries.
     */
    public Registries()
    {
        globalOidRegistry = new OidRegistry<>();
        attributeTypeRegistry = new DefaultAttributeTypeRegistry();
        comparatorRegistry = new DefaultComparatorRegistry();
        ditContentRuleRegistry = new DefaultDitContentRuleRegistry();
        ditStructureRuleRegistry = new DefaultDitStructureRuleRegistry();
        ldapSyntaxRegistry = new DefaultLdapSyntaxRegistry();
        matchingRuleRegistry = new DefaultMatchingRuleRegistry();
        matchingRuleUseRegistry = new DefaultMatchingRuleUseRegistry();
        nameFormRegistry = new DefaultNameFormRegistry();
        normalizerRegistry = new DefaultNormalizerRegistry();
        objectClassRegistry = new DefaultObjectClassRegistry();
        syntaxCheckerRegistry = new DefaultSyntaxCheckerRegistry();
        schemaObjects = new HashMap<>();
        usedBy = new HashMap<>();
        using = new HashMap<>();

        isRelaxed = STRICT;
        disabledAccepted = false;
    }


    /**
     * @return The AttributeType registry
     */
    public AttributeTypeRegistry getAttributeTypeRegistry()
    {
        return attributeTypeRegistry;
    }


    /**
     * @return The Comparator registry
     */
    public ComparatorRegistry getComparatorRegistry()
    {
        return comparatorRegistry;
    }


    /**
     * @return The DitContentRule registry
     */
    public DitContentRuleRegistry getDitContentRuleRegistry()
    {
        return ditContentRuleRegistry;
    }


    /**
     * @return The DitStructureRule registry
     */
    public DitStructureRuleRegistry getDitStructureRuleRegistry()
    {
        return ditStructureRuleRegistry;
    }


    /**
     * @return The MatchingRule registry
     */
    public MatchingRuleRegistry getMatchingRuleRegistry()
    {
        return matchingRuleRegistry;
    }


    /**
     * @return The MatchingRuleUse registry
     */
    public MatchingRuleUseRegistry getMatchingRuleUseRegistry()
    {
        return matchingRuleUseRegistry;
    }


    /**
     * @return The NameForm registry
     */
    public NameFormRegistry getNameFormRegistry()
    {
        return nameFormRegistry;
    }


    /**
     * @return The Normalizer registry
     */
    public NormalizerRegistry getNormalizerRegistry()
    {
        return normalizerRegistry;
    }


    /**
     * @return The ObjectClass registry
     */
    public ObjectClassRegistry getObjectClassRegistry()
    {
        return objectClassRegistry;
    }


    /**
     * @return The global Oid registry
     */
    public OidRegistry<SchemaObject> getGlobalOidRegistry()
    {
        return globalOidRegistry;
    }


    /**
     * @return The SyntaxChecker registry
     */
    public SyntaxCheckerRegistry getSyntaxCheckerRegistry()
    {
        return syntaxCheckerRegistry;
    }


    /**
     * @return The LdapSyntax registry
     */
    public LdapSyntaxRegistry getLdapSyntaxRegistry()
    {
        return ldapSyntaxRegistry;
    }


    /**
     * Get an OID from a name. As we have many possible registries, we
     * have to look in all of them to get the one containing the OID.
     *
     * @param name The name we are looking at
     * @return The associated OID
     */
    public String getOid( String name )
    {
        // we have many possible Registries to look at.
        // AttributeType
        try
        {
            AttributeType attributeType = attributeTypeRegistry.lookup( name );

            if ( attributeType != null )
            {
                return attributeType.getOid();
            }
        }
        catch ( LdapException ne )
        {
            // Fall down to the next registry
        }

        // ObjectClass
        try
        {
            ObjectClass objectClass = objectClassRegistry.lookup( name );

            if ( objectClass != null )
            {
                return objectClass.getOid();
            }
        }
        catch ( LdapException ne )
        {
            // Fall down to the next registry
        }

        // LdapSyntax
        try
        {
            LdapSyntax ldapSyntax = ldapSyntaxRegistry.lookup( name );

            if ( ldapSyntax != null )
            {
                return ldapSyntax.getOid();
            }
        }
        catch ( LdapException ne )
        {
            // Fall down to the next registry
        }

        // MatchingRule
        try
        {
            MatchingRule matchingRule = matchingRuleRegistry.lookup( name );

            if ( matchingRule != null )
            {
                return matchingRule.getOid();
            }
        }
        catch ( LdapException ne )
        {
            // Fall down to the next registry
        }

        // MatchingRuleUse
        try
        {
            MatchingRuleUse matchingRuleUse = matchingRuleUseRegistry.lookup( name );

            if ( matchingRuleUse != null )
            {
                return matchingRuleUse.getOid();
            }
        }
        catch ( LdapException ne )
        {
            // Fall down to the next registry
        }

        // NameForm
        try
        {
            NameForm nameForm = nameFormRegistry.lookup( name );

            if ( nameForm != null )
            {
                return nameForm.getOid();
            }
        }
        catch ( LdapException ne )
        {
            // Fall down to the next registry
        }

        // DitContentRule
        try
        {
            DitContentRule ditContentRule = ditContentRuleRegistry.lookup( name );

            if ( ditContentRule != null )
            {
                return ditContentRule.getOid();
            }
        }
        catch ( LdapException ne )
        {
            // Fall down to the next registry
        }

        // DitStructureRule
        try
        {
            DitStructureRule ditStructureRule = ditStructureRuleRegistry.lookup( name );

            if ( ditStructureRule != null )
            {
                return ditStructureRule.getOid();
            }
        }
        catch ( LdapException ne )
        {
            // No more registries to look at...
        }

        return null;
    }


    /**
     * Gets a schema that has been loaded into these Registries.
     * 
     * @param schemaName the name of the schema to lookup
     * @return the loaded Schema if one corresponding to the name exists
     */
    public Schema getLoadedSchema( String schemaName )
    {
        return loadedSchemas.get( Strings.toLowerCaseAscii( schemaName ) );
    }


    /**
     * Checks to see if a particular Schema is loaded.
     *
     * @param schemaName the name of the Schema to check
     * @return true if the Schema is loaded, false otherwise
     */
    public boolean isSchemaLoaded( String schemaName )
    {
        return loadedSchemas.containsKey( Strings.toLowerCaseAscii( schemaName ) );
    }


    // ------------------------------------------------------------------------
    // Code used to sanity check the resolution of entities in registries
    // ------------------------------------------------------------------------
    /**
     * Attempts to resolve the dependent schema objects of all entities that
     * refer to other objects within the registries.  Null references will be
     * handed appropriately.
     * The order in which the SchemaObjects must be :
     * <ul>
     *   <li>1) Normalizers, Comparators and SyntaxCheckers (as they depend on nothing)</li>
     *   <li>2) Syntaxes (depend on SyntaxCheckers)</li>
     *   <li>3) MatchingRules (depend on Syntaxes, Normalizers and Comparators</li>
     *   <li>4) AttributeTypes (depend on MatchingRules, Syntaxes and AttributeTypes : in this case, we first handle the superior)</li>
     *   <li>5) ObjectClasses (depend on AttributeTypes and ObjectClasses)</li>
     * </ul>
     * <br><br>
     * Later, when we will support them :
     * <ul>
     *   <li>6) MatchingRuleUses (depend on matchingRules and AttributeTypes)</li>
     *   <li>7) DitContentRules (depend on ObjectClasses and AttributeTypes)</li>
     *   <li>8) NameForms (depends on ObjectClasses and AttributeTypes)</li>
     *   <li>9) DitStructureRules (depends onNameForms and DitStructureRules)</li>
     * </ul>
     */
    public void checkRefInteg()
    {
        // Step 1 :
        // We start with Normalizers, Comparators and SyntaxCheckers
        // as they depend on nothing
        // Check the Normalizers
        for ( Normalizer normalizer : normalizerRegistry )
        {
            resolve( normalizer );
        }

        // Check the Comparators
        for ( LdapComparator<?> comparator : comparatorRegistry )
        {
            resolve( comparator );
        }

        // Check the SyntaxCheckers
        for ( SyntaxChecker syntaxChecker : syntaxCheckerRegistry )
        {
            resolve( syntaxChecker );
        }

        // Step 2 :
        // Check the LdapSyntaxes
        for ( LdapSyntax ldapSyntax : ldapSyntaxRegistry )
        {
            resolve( ldapSyntax );
        }

        // Step 3 :
        // Check the matchingRules
        for ( MatchingRule matchingRule : matchingRuleRegistry )
        {
            resolve( matchingRule );
        }

        // Step 4 :
        // Check the AttributeTypes
        for ( AttributeType attributeType : attributeTypeRegistry )
        {
            resolve( attributeType );
        }

        //  Step 5 :
        // Check the ObjectClasses
        for ( ObjectClass objectClass : objectClassRegistry )
        {
            resolve( objectClass );
        }

        // Step 6-9 aren't yet defined
    }


    /**
     * Add the SchemaObjectReferences. This method does nothing, it's just
     * a catch all. The other methods will be called for each specific
     * schemaObject
     *
    public void addCrossReferences( SchemaObject schemaObject )
    {
        // Do nothing : it's a catch all method.
    }
    
    
    /**
     * Delete the AT references (using and usedBy) :
     * AT -&gt; MR (for EQUALITY, ORDERING and SUBSTR)
     * AT -&gt; S
     * AT -&gt; AT
     * 
     * @param attributeType The AttributeType to remove
     */
    public void delCrossReferences( AttributeType attributeType )
    {
        if ( attributeType.getEquality() != null )
        {
            delReference( attributeType, attributeType.getEquality() );
        }

        if ( attributeType.getOrdering() != null )
        {
            delReference( attributeType, attributeType.getOrdering() );
        }

        if ( attributeType.getSubstring() != null )
        {
            delReference( attributeType, attributeType.getSubstring() );
        }

        if ( attributeType.getSyntax() != null )
        {
            delReference( attributeType, attributeType.getSyntax() );
        }

        if ( attributeType.getSuperior() != null )
        {
            delReference( attributeType, attributeType.getSuperior() );
        }
    }


    /**
     * Build the AttributeType references. This has to be done recursively, as
     * an AttributeType may inherit its parent's MatchingRules. The references
     * to update are :
     * - EQUALITY MR
     * - ORDERING MR
     * - SUBSTRING MR
     * - SUP AT
     * - SYNTAX
     */
    private void buildAttributeTypeReferences()
    {
        for ( AttributeType attributeType : attributeTypeRegistry )
        {
            if ( ( getUsing( attributeType ) == null ) || getUsing( attributeType ).isEmpty() )
            {
                buildReference( attributeType );
            }
        }
    }


    /**
     * Build the Comparator references
     */
    private void buildComparatorReferences()
    {
        for ( LdapComparator<?> comparator : comparatorRegistry )
        {
            buildReference( comparator );
        }
    }


    /**
     * Build the DitContentRule references
     */
    private void buildDitContentRuleReferences()
    {
        // TODO: implement
    }


    /**
     * Build the DitStructureRule references
     */
    private void buildDitStructureRuleReferences()
    {
        // TODO: implement
    }


    /**
     * Delete the MR references (using and usedBy) :
     * MR -&gt; C
     * MR -&gt; N
     * MR -&gt; S
     * 
     * @param matchingRule The MatchinRule refere ce to delete
     */
    public void delCrossReferences( MatchingRule matchingRule )
    {
        if ( matchingRule.getLdapComparator() != null )
        {
            delReference( matchingRule, matchingRule.getLdapComparator() );
        }

        if ( matchingRule.getNormalizer() != null )
        {
            delReference( matchingRule, matchingRule.getNormalizer() );
        }

        if ( matchingRule.getSyntax() != null )
        {
            delReference( matchingRule, matchingRule.getSyntax() );
        }
    }


    /**
     * Build the SchemaObject references
     * 
     * @param schemaObject The SchemaObject to add
     */
    public void buildReference( SchemaObject schemaObject )
    {
        try
        {
            switch ( schemaObject.getObjectType() )
            {
                case ATTRIBUTE_TYPE:
                    AttributeTypeHelper.addToRegistries( ( MutableAttributeType ) schemaObject, errorHandler, this );
                    break;

                case DIT_CONTENT_RULE:
                    DitContentRuleHelper.addToRegistries( ( DitContentRule ) schemaObject, errorHandler, this );
                    break;

                case DIT_STRUCTURE_RULE:
                    DitStructureRuleHelper.addToRegistries( ( DitStructureRule ) schemaObject, errorHandler, this );
                    break;

                case LDAP_SYNTAX:
                    LdapSyntaxHelper.addToRegistries( ( LdapSyntax ) schemaObject, errorHandler, this );
                    break;

                case MATCHING_RULE:
                    MatchingRuleHelper.addToRegistries( ( MutableMatchingRule ) schemaObject, errorHandler, this );
                    break;

                case MATCHING_RULE_USE:
                    MatchingRuleUseHelper.addToRegistries( ( MatchingRuleUse ) schemaObject, errorHandler, this );
                    break;

                case NAME_FORM:
                    NameFormHelper.addToRegistries( ( NameForm ) schemaObject, errorHandler, this );
                    break;

                case OBJECT_CLASS:
                    ObjectClassHelper.addToRegistries( ( ObjectClass ) schemaObject, errorHandler, this );
                    break;

                case SYNTAX_CHECKER:
                case NORMALIZER:
                case COMPARATOR:
                    // Those are not registered
                    break;

                default:
                    throw new IllegalArgumentException( 
                        I18n.err( I18n.ERR_13718_UNEXPECTED_SCHEMA_OBJECT_TYPE, schemaObject.getObjectType() ) );
            }
        }
        catch ( LdapException ne )
        {
            // Not allowed.
            String msg = I18n.err( I18n.ERR_13746_CANNOT_BUILD_REFERENCES, schemaObject.getName(), ne.getLocalizedMessage() );

            LdapProtocolErrorException error = new LdapProtocolErrorException( msg, ne );
            errorHandler.handle( LOG, msg, error );
        }
    }


    /**
     * Unlink the SchemaObject references
     * 
     * @param schemaObject The SchemaObject to remove
     */
    public void removeReference( SchemaObject schemaObject )
    {
        try
        {
            switch ( schemaObject.getObjectType() )
            {
                case ATTRIBUTE_TYPE:
                    AttributeTypeHelper.removeFromRegistries( ( AttributeType ) schemaObject, errorHandler, this );
                    break;

                case LDAP_SYNTAX:
                    LdapSyntaxHelper.removeFromRegistries( ( LdapSyntax ) schemaObject, errorHandler, this );
                    break;

                case MATCHING_RULE:
                    MatchingRuleHelper.removeFromRegistries( ( MatchingRule ) schemaObject, errorHandler, this );
                    break;

                case OBJECT_CLASS:
                    ObjectClassHelper.removeFromRegistries( ( ObjectClass ) schemaObject, errorHandler, this );
                    break;
                    
                case DIT_CONTENT_RULE :
                    // TODO
                    break;
                    
                case DIT_STRUCTURE_RULE :
                    // TODO
                    break;
                    
                case NAME_FORM :
                    // TODO
                    break;
                    
                case MATCHING_RULE_USE :
                    // TODO
                    break;

                case SYNTAX_CHECKER:
                case NORMALIZER:
                case COMPARATOR:
                    // Those were not registered
                    break;

                default:
                    throw new IllegalArgumentException( 
                        I18n.err( I18n.ERR_13718_UNEXPECTED_SCHEMA_OBJECT_TYPE, schemaObject.getObjectType() ) );
            }
        }
        catch ( LdapException ne )
        {
            // Not allowed.
            String msg = I18n.err( I18n.ERR_13747_CANNOT_REMOVE_REFERENCES, schemaObject.getName(), ne.getLocalizedMessage() );

            LdapSchemaViolationException error = new LdapSchemaViolationException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, msg, ne );
            errorHandler.handle( LOG, msg, error );
        }
    }


    /**
     * Build the MatchingRule references
     */
    private void buildMatchingRuleReferences()
    {
        for ( MatchingRule matchingRule : matchingRuleRegistry )
        {
            buildReference( matchingRule );
        }
    }


    /**
     * Build the MatchingRuleUse references
     */
    private void buildMatchingRuleUseReferences()
    {
        for ( MatchingRuleUse matchingRuleUse : matchingRuleUseRegistry )
        {
            buildReference( matchingRuleUse );
        }
    }


    /**
     * Build the NameForm references
     */
    private void buildNameFormReferences()
    {
        // TODO: implement
    }


    /**
     * Build the Normalizer references
     */
    private void buildNormalizerReferences()
    {
        for ( Normalizer normalizer : normalizerRegistry )
        {
            buildReference( normalizer );
        }
    }


    /**
     * Build the ObjectClasses references
     */
    private void buildObjectClassReferences()
    {
        // Remember the OC we have already processed
        Set<String> done = new HashSet<>();

        // The ObjectClass
        for ( ObjectClass objectClass : objectClassRegistry )
        {
            if ( done.contains( objectClass.getOid() ) )
            {
                continue;
            }
            else
            {
                done.add( objectClass.getOid() );
            }

            buildReference( objectClass );
        }
    }


    /**
     * Build the Syntax references
     */
    private void buildLdapSyntaxReferences()
    {
        for ( LdapSyntax syntax : ldapSyntaxRegistry )
        {
            buildReference( syntax );
        }
    }


    /**
     * Build the SyntaxChecker references
     */
    private void buildSyntaxCheckerReferences()
    {
        for ( SyntaxChecker syntaxChecker : syntaxCheckerRegistry )
        {
            buildReference( syntaxChecker );
        }
    }


    /**
     * Build the usedBy and using references from the stored elements.
     */
    public void buildReferences()
    {
        // The Comparator references
        buildComparatorReferences();

        // The Normalizer references
        buildNormalizerReferences();

        // The SyntaxChecker references
        buildSyntaxCheckerReferences();

        // The Syntax references
        buildLdapSyntaxReferences();

        // The MatchingRules references
        buildMatchingRuleReferences();

        // The AttributeType references
        buildAttributeTypeReferences();

        // The MatchingRuleUse references
        buildMatchingRuleUseReferences();

        // The ObjectClasses references
        buildObjectClassReferences();

        // The DitContentRules references
        buildDitContentRuleReferences();

        // The NameForms references
        buildNameFormReferences();

        // The DitStructureRules references
        buildDitStructureRuleReferences();
    }


    /**
     * Attempts to resolve the SyntaxChecker associated with a Syntax.
     *
     * @param syntax the LdapSyntax to resolve the SyntaxChecker of
     */
    private void resolve( LdapSyntax syntax )
    {
        // A LdapSyntax must point to a valid SyntaxChecker
        // or to the OctetString SyntaxChecker
        try
        {
            LdapSyntaxHelper.addToRegistries( syntax, errorHandler, this );
        }
        catch ( LdapException e )
        {
            errorHandler.handle( LOG, e.getMessage(), e );
        }
    }


    /**
     * Attempts to resolve the Normalizer
     *
     * @param normalizer the Normalizer
     */
    private void resolve( Normalizer normalizer )
    {
        // This is currently doing nothing.
    }


    /**
     * Attempts to resolve the LdapComparator
     *
     * @param comparator the LdapComparator
     */
    private void resolve( LdapComparator<?> comparator )
    {
        // This is currently doing nothing.
    }


    /**
     * Attempts to resolve the SyntaxChecker
     *
     * @param syntaxChecker the SyntaxChecker
     */
    private void resolve( SyntaxChecker syntaxChecker )
    {
        // This is currently doing nothing.
    }


    /**
     * Check if the Comparator, Normalizer and the syntax are
     * existing for a matchingRule.
     * 
     * @param matchingRule The matching rule to use
     */
    private void resolve( MatchingRule matchingRule )
    {
        // Process the Syntax. It can't be null
        String syntaxOid = matchingRule.getSyntaxOid();

        if ( syntaxOid != null )
        {
            // Check if the Syntax is present in the registries
            try
            {
                ldapSyntaxRegistry.lookup( syntaxOid );
            }
            catch ( LdapException ne )
            {
                // This MR's syntax has not been loaded into the Registries.
                LdapSchemaException ldapSchemaException = new LdapSchemaException(
                    LdapSchemaExceptionCodes.OID_ALREADY_REGISTERED, I18n.err( I18n.ERR_13748_MATCHING_RULE_NO_SYNTAX, matchingRule.getOid() ),
                    ne );
                ldapSchemaException.setSourceObject( matchingRule );
                errorHandler.handle( LOG, ldapSchemaException.getMessage(), ldapSchemaException );
            }
        }
        else
        {
            // This is an error.
            LdapSchemaException ldapSchemaException = new LdapSchemaException(
                LdapSchemaExceptionCodes.OID_ALREADY_REGISTERED, I18n.err( I18n.ERR_13748_MATCHING_RULE_NO_SYNTAX, matchingRule.getOid() ) );
            ldapSchemaException.setSourceObject( matchingRule );
            errorHandler.handle( LOG, ldapSchemaException.getMessage(), ldapSchemaException );
        }

        // Process the Normalizer
        Normalizer normalizer = matchingRule.getNormalizer();

        if ( normalizer == null )
        {
            // Ok, no normalizer, this is an error
            LdapSchemaViolationException error = new LdapSchemaViolationException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n.err(
                I18n.ERR_13220_NO_NORMALIZER, matchingRule.getOid() ) );
            errorHandler.handle( LOG, error.getMessage(), error );
        }

        // Process the Comparator
        LdapComparator<?> comparator = matchingRule.getLdapComparator();

        if ( comparator == null )
        {
            // Ok, no comparator, this is an error
            LdapSchemaViolationException error = new LdapSchemaViolationException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n.err(
                I18n.ERR_13413_MR_DOES_NOT_HAVE_A_COMP, matchingRule.getOid() ) );
            errorHandler.handle( LOG, error.getMessage(), error );
        }
    }


    /**
     * Check AttributeType referential integrity
     * 
     * @param attributeType The AttributeType
     * @param processed The set of superior to check
     */
    private void resolveRecursive( AttributeType attributeType, Set<String> processed )
    {
        // Process the Superior, if any
        String superiorOid = attributeType.getSuperiorOid();

        AttributeType superior = null;

        if ( superiorOid != null )
        {
            // Check if the Superior is present in the registries
            try
            {
                superior = attributeTypeRegistry.lookup( superiorOid );
            }
            catch ( LdapException ne )
            {
                // This AT's superior has not been loaded into the Registries.
                if ( !processed.contains( superiorOid ) )
                {
                    errorHandler.handle( LOG, ne.getMessage(), ne );
                }
            }

            // We now have to process the superior, if it hasn't been
            // processed yet.
            if ( superior != null )
            {
                if ( !processed.contains( superiorOid ) )
                {
                    resolveRecursive( superior, processed );
                    processed.add( attributeType.getOid() );
                }
                else
                {
                    // Not allowed : we have a cyle
                    LdapSchemaViolationException error = new LdapSchemaViolationException( ResultCodeEnum.OTHER, 
                        I18n.err( I18n.ERR_13749_AT_WITH_CYCLE, attributeType.getOid() ) );
                    errorHandler.handle( LOG, error.getMessage(), error );
                    return;
                }
            }
        }

        // Process the Syntax. If it's null, the attributeType must have
        // a Superior.
        String syntaxOid = attributeType.getSyntaxOid();

        if ( syntaxOid != null )
        {
            // Check if the Syntax is present in the registries
            try
            {
                ldapSyntaxRegistry.lookup( syntaxOid );
            }
            catch ( LdapException ne )
            {
                // This AT's syntax has not been loaded into the Registries.
                errorHandler.handle( LOG, ne.getMessage(), ne );
            }
        }
        else
        {
            // No Syntax : get it from the AttributeType's superior
            if ( superior == null )
            {
                // This is an error. if the AT does not have a Syntax,
                // then it must have a superior, which syntax is get from.
                LdapSchemaViolationException error = new LdapSchemaViolationException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n.err(
                    I18n.ERR_13414_AT_DOES_NOT_HAVE_A_SUPERIOR_NOR_SYNTAX, attributeType.getOid() ) );
                errorHandler.handle( LOG, error.getMessage(), error );
            }
        }

        // Process the EQUALITY MatchingRule. It may be null, but if it's not
        // it must have been processed before
        String equalityOid = attributeType.getEqualityOid();

        if ( equalityOid != null )
        {
            // Check if the MatchingRule is present in the registries
            try
            {
                matchingRuleRegistry.lookup( equalityOid );
            }
            catch ( LdapException ne )
            {
                // This AT's EQUALITY matchingRule has not been loaded into the Registries.
                errorHandler.handle( LOG, ne.getMessage(), ne );
            }
        }

        // Process the ORDERING MatchingRule. It may be null, but if it's not
        // it must have been processed before
        String orderingOid = attributeType.getOrderingOid();

        if ( orderingOid != null )
        {
            // Check if the MatchingRule is present in the registries
            try
            {
                matchingRuleRegistry.lookup( orderingOid );
            }
            catch ( LdapException ne )
            {
                // This AT's ORDERING matchingRule has not been loaded into the Registries.
                errorHandler.handle( LOG, ne.getMessage(), ne );
            }
        }

        // Process the SUBSTR MatchingRule. It may be null, but if it's not
        // it must have been processed before
        String substringOid = attributeType.getSubstringOid();

        if ( substringOid != null )
        {
            // Check if the MatchingRule is present in the registries
            try
            {
                matchingRuleRegistry.lookup( substringOid );
            }
            catch ( LdapException ne )
            {
                // This AT's SUBSTR matchingRule has not been loaded into the Registries.
                errorHandler.handle( LOG, ne.getMessage(), ne );
            }
        }
    }


    /**
     * Check the inheritance, and the existence of MatchingRules and LdapSyntax
     * for an attribute
     * 
     * @param attributeType The AttributeType
     */
    private void resolve( AttributeType attributeType )
    {
        // This set is used to avoid having more than one error
        // for an AttributeType. It's mandatory when processing
        // a Superior, as it may be broken and referenced more than once.
        Set<String> processed = new HashSet<>();

        // Store the AttributeType itself in the processed, to avoid cycle
        processed.add( attributeType.getOid() );

        // Call the recursive method, as we may have superiors to deal with
        resolveRecursive( attributeType, processed );
    }


    private List<AttributeType> getMustRecursive( List<AttributeType> musts, Set<ObjectClass> processed,
        ObjectClass objectClass )
    {
        if ( objectClass != null )
        {
            if ( processed.contains( objectClass ) )
            {
                // We have found a cycle. It has already been reported,
                // don't add a new error, just exit.
                return null;
            }

            processed.add( objectClass );

            for ( AttributeType must : objectClass.getMustAttributeTypes() )
            {
                musts.add( must );
            }

            for ( ObjectClass superior : objectClass.getSuperiors() )
            {
                getMustRecursive( musts, processed, superior );
            }
        }

        return musts;
    }


    private void resolve( ObjectClass objectClass )
    {
        // This set is used to avoid having more than one error
        // for an ObjectClass. It's mandatory when processing
        // the Superiors, as they may be broken and referenced more than once.
        Set<String> processed = new HashSet<>();

        // Store the ObjectClass itself in the processed, to avoid cycle
        processed.add( objectClass.getOid() );

        // Call the recursive method, as we may have superiors to deal with
        resolveRecursive( objectClass, processed );

        // Check that the MAY and MUST AT are consistent (no AT in MAY and in MUST
        // in one of its superior
        List<AttributeType> musts = getMustRecursive( new ArrayList<AttributeType>(), new HashSet<ObjectClass>(),
            objectClass );

        if ( musts != null )
        {
            for ( AttributeType may : objectClass.getMayAttributeTypes() )
            {
                if ( musts.contains( may ) )
                {
                    // This is not allowed.
                    LdapSchemaException ldapSchemaException = new LdapSchemaException(
                        LdapSchemaExceptionCodes.OC_DUPLICATE_AT_IN_MAY_AND_MUST );
                    ldapSchemaException.setSourceObject( objectClass );
                    ldapSchemaException.setOtherObject( may );
                    errorHandler.handle( LOG, ldapSchemaException.getMessage(), ldapSchemaException );
                }
            }
        }
    }


    private void resolveRecursive( ObjectClass objectClass, Set<String> processed )
    {
        // Process the Superiors, if any
        List<String> superiorOids = objectClass.getSuperiorOids();
        ObjectClass superior = null;

        for ( String superiorOid : superiorOids )
        {
            // Check if the Superior is present in the registries
            try
            {
                superior = objectClassRegistry.lookup( superiorOid );
            }
            catch ( LdapException ne )
            {
                // This OC's superior has not been loaded into the Registries.
                if ( !processed.contains( superiorOid ) )
                {
                    LdapSchemaException ldapSchemaException = new LdapSchemaException(
                        LdapSchemaExceptionCodes.OC_NONEXISTENT_SUPERIOR, ne );
                    ldapSchemaException.setSourceObject( objectClass );
                    ldapSchemaException.setRelatedId( superiorOid );
                    errorHandler.handle( LOG, ldapSchemaException.getMessage(), ldapSchemaException );
                }
            }

            // We now have to process the superior, if it hasn't been
            // processed yet.
            if ( superior != null )
            {
                if ( !processed.contains( superior.getOid() ) )
                {
                    resolveRecursive( superior, processed );
                    processed.add( objectClass.getOid() );
                }
                else
                {
                    // Not allowed : we have a cyle
                    LdapSchemaException ldapSchemaException = new LdapSchemaException(
                        LdapSchemaExceptionCodes.OC_CYCLE_CLASS_HIERARCHY );
                    ldapSchemaException.setSourceObject( objectClass );
                    ldapSchemaException.setOtherObject( superior );
                    errorHandler.handle( LOG, ldapSchemaException.getMessage(), ldapSchemaException );
                    return;
                }
            }
        }

        // Process the MAY attributeTypes.
        for ( String mayOid : objectClass.getMayAttributeTypeOids() )
        {
            // Check if the MAY AttributeType is present in the registries
            try
            {
                attributeTypeRegistry.lookup( mayOid );
            }
            catch ( LdapException ne )
            {
                // This AT has not been loaded into the Registries.
                errorHandler.handle( LOG, ne.getMessage(), ne );
            }
        }

        // Process the MUST attributeTypes.
        for ( String mustOid : objectClass.getMustAttributeTypeOids() )
        {
            // Check if the MUST AttributeType is present in the registries
            try
            {
                attributeTypeRegistry.lookup( mustOid );
            }
            catch ( LdapException ne )
            {
                // This AT has not been loaded into the Registries.
                errorHandler.handle( LOG, ne.getMessage(), ne );
            }
        }

        // All is done for this ObjectClass, let's apply the registries
        ObjectClassHelper.addToRegistries( objectClass, errorHandler, this );
    }


    /**
     * Applies the added SchemaObject to the given register
     *
     * @param schemaObject The SchemaObject to add
     * @param check A flag set when we want the schema checks to be done
     * @throws LdapException If we weren't able to add the SchemaObject
     */
    public void add( SchemaObject schemaObject, boolean check ) throws LdapException
    {
        // Relax the registries
        boolean wasRelaxed = isRelaxed;
        setRelaxed();

        // Register the SchemaObject in the registries
        register( schemaObject );

        // Associate the SchemaObject with its schema
        associateWithSchema( schemaObject );

        // Build the SchemaObject references
        if ( check )
        {
            buildReference( schemaObject );
        }

        // Lock the SchemaObject
        schemaObject.lock();

        if ( check && ( !errorHandler.wasError() ) )
        {
            // Check the registries now
            checkRefInteg();
        }

        // Get back to Strict mode
        if ( !wasRelaxed )
        {
            setStrict();
        }
    }


    /**
     * Remove the given SchemaObject from the registries
     * 
     * @param schemaObject The SchemaObject to delete
     * @throws LdapException If the deletion failed
     */
    public void delete( SchemaObject schemaObject ) throws LdapException
    {
        // Relax the registries
        boolean wasRelaxed = isRelaxed;
        setRelaxed();

        // Remove the SchemaObject from the registries
        SchemaObject removed = unregister( schemaObject );

        // Remove the SchemaObject from its schema
        dissociateFromSchema( removed );

        // Unlink the SchemaObject references
        removeReference( removed );

        if ( !errorHandler.wasError() )
        {
            // Check the registries now
            checkRefInteg();
        }

        // Restore the previous registries state
        if ( !wasRelaxed )
        {
            setStrict();
        }
    }


    /**
     * Merely adds the schema to the set of loaded schemas.  Does not
     * actually do any work to add schema objects to registries.
     * 
     * {@inheritDoc}
     */
    @Override
    public void schemaLoaded( Schema schema )
    {
        this.loadedSchemas.put( Strings.toLowerCaseAscii( schema.getSchemaName() ), schema );
    }


    /**
     * Merely removes the schema from the set of loaded schemas.  Does not
     * actually do any work to remove schema objects from registries.
     * 
     * {@inheritDoc}
     */
    @Override
    public void schemaUnloaded( Schema schema )
    {
        this.loadedSchemas.remove( Strings.toLowerCaseAscii( schema.getSchemaName() ) );
    }


    /**
     * Gets an unmodifiable Map of schema names to loaded Schema objects.
     * 
     * @return the map of loaded Schema objects
     */
    public Map<String, Schema> getLoadedSchemas()
    {
        return Collections.unmodifiableMap( loadedSchemas );
    }


    /**
     * @return Gets a reference to the Map associating a schemaName to
     * its contained SchemaObjects
     */
    public Map<String, Set<SchemaObjectWrapper>> getObjectBySchemaName()
    {
        return schemaObjects;
    }


    /**
     * Retrieve the schema name for a specific SchemaObject, or return "other" if none is found.
     * 
     * @param schemaObject The SchemaObject
     * @return The associated Schema
     */
    private String getSchemaName( SchemaObject schemaObject )
    {
        String schemaName = Strings.toLowerCaseAscii( schemaObject.getSchemaName() );

        if ( loadedSchemas.containsKey( schemaName ) )
        {
            return schemaName;
        }
        else
        {
            return MetaSchemaConstants.SCHEMA_OTHER;
        }
    }


    /**
     * Tells if the given SchemaObject is present in one schema. The schema
     * may be disabled.
     *
     * @param schemaObject The schemaObject we are looking for
     * @return true if the schemaObject is present in a schema
     */
    public boolean contains( SchemaObject schemaObject )
    {
        String schemaName = schemaObject.getSchemaName();

        Set<SchemaObjectWrapper> setSchemaObjects = schemaObjects.get( schemaName );

        if ( ( setSchemaObjects == null ) || setSchemaObjects.isEmpty() )
        {
            return false;
        }

        SchemaObjectWrapper wrapper = new SchemaObjectWrapper( schemaObject );

        return setSchemaObjects.contains( wrapper );
    }


    /**
     * Create a new schema association with its content
     *
     * @param schemaName The schema name
     * @return A set containing the associations
     */
    public Set<SchemaObjectWrapper> addSchema( String schemaName )
    {
        Set<SchemaObjectWrapper> content = new HashSet<>();
        schemaObjects.put( schemaName, content );

        return content;
    }


    /**
     * Register the given SchemaObject into the associated Registry
     * 
     * @param schemaObject The SchemaObject to register
     * @throws LdapException If the SchemaObject cannot be registered
     */
    private void register( SchemaObject schemaObject ) throws LdapException
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13720_REGISTRING, schemaObject.getObjectType(), schemaObject.getOid() ) );
        }

        // Check that the SchemaObject is not already registered
        if ( !( schemaObject instanceof LoadableSchemaObject ) && globalOidRegistry.contains( schemaObject.getOid() ) )
        {
            String msg = I18n.err( I18n.ERR_13750_REGISTERING_FAILED_ALREADY_PRESENT, schemaObject.getObjectType(), schemaObject.getOid() );
            LOG.error( msg );
            LdapUnwillingToPerformException error = new LdapUnwillingToPerformException( ResultCodeEnum.UNWILLING_TO_PERFORM, msg );
            errorHandler.handle( LOG, msg, error );
            return;
        }

        try
        {
            // First call the specific registry's register method
            switch ( schemaObject.getObjectType() )
            {
                case ATTRIBUTE_TYPE:
                    attributeTypeRegistry.register( ( AttributeType ) schemaObject );
                    break;

                case COMPARATOR:
                    comparatorRegistry.register( ( LdapComparator<?> ) schemaObject );
                    break;

                case DIT_CONTENT_RULE:
                    ditContentRuleRegistry.register( ( DitContentRule ) schemaObject );
                    break;

                case DIT_STRUCTURE_RULE:
                    ditStructureRuleRegistry.register( ( DitStructureRule ) schemaObject );
                    break;

                case LDAP_SYNTAX:
                    ldapSyntaxRegistry.register( ( LdapSyntax ) schemaObject );
                    break;

                case MATCHING_RULE:
                    matchingRuleRegistry.register( ( MatchingRule ) schemaObject );
                    break;

                case MATCHING_RULE_USE:
                    matchingRuleUseRegistry.register( ( MatchingRuleUse ) schemaObject );
                    break;

                case NAME_FORM:
                    nameFormRegistry.register( ( NameForm ) schemaObject );
                    break;

                case NORMALIZER:
                    normalizerRegistry.register( ( Normalizer ) schemaObject );
                    break;

                case OBJECT_CLASS:
                    objectClassRegistry.register( ( ObjectClass ) schemaObject );
                    break;

                case SYNTAX_CHECKER:
                    syntaxCheckerRegistry.register( ( SyntaxChecker ) schemaObject );
                    break;

                default:
                    throw new IllegalArgumentException( 
                        I18n.err( I18n.ERR_13718_UNEXPECTED_SCHEMA_OBJECT_TYPE, schemaObject.getObjectType() ) );
            }
        }
        catch ( Exception e )
        {
            errorHandler.handle( LOG, e.getMessage(), e );
        }
    }


    /**
     * Store the given SchemaObject in the Map associating SchemaObjetcs to their
     * related Schema.
     *
     * @param schemaObject The schemaObject to register
     */
    public void associateWithSchema( SchemaObject schemaObject )
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13720_REGISTRING, schemaObject.getObjectType(), schemaObject.getOid() ) );
        }

        // Check that the SchemaObject is not already registered
        if ( !( schemaObject instanceof LoadableSchemaObject ) && globalOidRegistry.contains( schemaObject.getOid() ) )
        {
            String msg = I18n.err( I18n.ERR_13750_REGISTERING_FAILED_ALREADY_PRESENT, schemaObject.getObjectType(), schemaObject.getOid() );
            LOG.error( msg );
            LdapUnwillingToPerformException error = new LdapUnwillingToPerformException( ResultCodeEnum.UNWILLING_TO_PERFORM, msg );
            errorHandler.handle( LOG, msg, error );
            return;
        }

        // Get a normalized form of schema name
        String schemaName = getSchemaName( schemaObject );

        // And register the schemaObject within its schema
        Set<SchemaObjectWrapper> content = schemaObjects.get( schemaName );

        if ( content == null )
        {
            content = new HashSet<>();
            schemaObjects.put( Strings.toLowerCaseAscii( schemaName ), content );
        }

        SchemaObjectWrapper schemaObjectWrapper = new SchemaObjectWrapper( schemaObject );

        if ( content.contains( schemaObjectWrapper ) )
        {
            // Already present !
            // What should we do ?
            errorHandler.handle( LOG, I18n.msg( I18n.MSG_13719_REGISTRING_FAILED_ALREADY_PRESENT,
                    schemaObject.getObjectType(), schemaObject.getOid() ), null );
        }
        else
        {
            // Create the association
            content.add( schemaObjectWrapper );

            // Update the global OidRegistry if the SchemaObject is not
            // an instance of LoadableSchemaObject
            if ( !( schemaObject instanceof LoadableSchemaObject ) )
            {
                try
                {
                    globalOidRegistry.register( schemaObject );
                }
                catch ( LdapException ne )
                {
                    errorHandler.handle( LOG, ne.getMessage(), ne );
                    return;
                }
            }

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13731_REGISTRED_FOR_OID, schemaObject.getName(), schemaObject.getOid() ) );
            }
        }
    }


    /**
     * Store the given SchemaObject in the Map associating SchemaObjetcs to their
     * related Schema.
     *
     * @param schemaObject The schemaObject to register
     * @throws LdapException If there is a problem
     */

    public void dissociateFromSchema( SchemaObject schemaObject ) throws LdapException
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13741_UNREGISTRING, schemaObject.getObjectType(), schemaObject.getOid() ) );
        }

        // Check that the SchemaObject is already registered
        if ( !( schemaObject instanceof LoadableSchemaObject ) && !globalOidRegistry.contains( schemaObject.getOid() ) )
        {
            String msg = I18n.err( I18n.ERR_13751_UNREGISTERING_FAILED_NOT_PRESENT, schemaObject.getObjectType(), schemaObject.getOid() );
            LOG.error( msg );
            Throwable error = new LdapUnwillingToPerformException( ResultCodeEnum.UNWILLING_TO_PERFORM, msg );
            errorHandler.handle( LOG, msg, error );
            return;
        }

        // Get a normalized form of schema name
        String schemaName = getSchemaName( schemaObject );
        String oid = schemaObject.getOid();

        // And unregister the schemaObject from its schema
        Set<SchemaObjectWrapper> content = schemaObjects.get( schemaName );

        SchemaObjectWrapper schemaObjectWrapper = new SchemaObjectWrapper( schemaObject );

        if ( !content.contains( schemaObjectWrapper ) )
        {
            // Not present !
            // What should we do ?
            if ( LOG.isInfoEnabled() )
            {
                LOG.info( I18n.msg( I18n.MSG_13739_UNREGISTERED_FAILED_NOT_PRESENT, schemaObject.getObjectType(),
                    schemaObject.getOid() ) );
            }
        }
        else
        {
            // Remove the association
            content.remove( schemaObjectWrapper );

            // Update the global OidRegistry if the SchemaObject is not
            // an instance of LoadableSchemaObject
            if ( !( schemaObject instanceof LoadableSchemaObject ) )
            {
                try
                {
                    globalOidRegistry.unregister( oid );
                }
                catch ( LdapException ne )
                {
                    errorHandler.handle( LOG, ne.getMessage(), ne );
                    return;
                }
            }

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13737_UNREGISTERED_FOR_OID, schemaObject.getName(), schemaObject.getOid() ) );
            }
        }
    }


    /**
     * Unregister a SchemaObject from the registries
     *
     * @param schemaObject The SchemaObject we want to deregister
     * @return The unregistred SchemaObject
     * @throws LdapException If the removal failed
     */
    private SchemaObject unregister( SchemaObject schemaObject ) throws LdapException
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13741_UNREGISTRING, schemaObject.getObjectType(), schemaObject.getOid() ) );
        }

        // Check that the SchemaObject is present in the registries
        if ( !( schemaObject instanceof LoadableSchemaObject ) && !globalOidRegistry.contains( schemaObject.getOid() ) )
        {
            String msg = I18n.err( I18n.ERR_13751_UNREGISTERING_FAILED_NOT_PRESENT, schemaObject.getObjectType(), schemaObject.getOid() );
            LOG.error( msg );
            throw new LdapUnwillingToPerformException( ResultCodeEnum.UNWILLING_TO_PERFORM, msg );
        }

        SchemaObject unregistered;

        // First call the specific registry's register method
        switch ( schemaObject.getObjectType() )
        {
            case ATTRIBUTE_TYPE:
                unregistered = attributeTypeRegistry.unregister( ( AttributeType ) schemaObject );
                break;

            case COMPARATOR:
                unregistered = comparatorRegistry.unregister( ( LdapComparator<?> ) schemaObject );
                break;

            case DIT_CONTENT_RULE:
                unregistered = ditContentRuleRegistry.unregister( ( DitContentRule ) schemaObject );
                break;

            case DIT_STRUCTURE_RULE:
                unregistered = ditStructureRuleRegistry.unregister( ( DitStructureRule ) schemaObject );
                break;

            case LDAP_SYNTAX:
                unregistered = ldapSyntaxRegistry.unregister( ( LdapSyntax ) schemaObject );
                break;

            case MATCHING_RULE:
                unregistered = matchingRuleRegistry.unregister( ( MatchingRule ) schemaObject );
                break;

            case MATCHING_RULE_USE:
                unregistered = matchingRuleUseRegistry.unregister( ( MatchingRuleUse ) schemaObject );
                break;

            case NAME_FORM:
                unregistered = nameFormRegistry.unregister( ( NameForm ) schemaObject );
                break;

            case NORMALIZER:
                unregistered = normalizerRegistry.unregister( ( Normalizer ) schemaObject );
                break;

            case OBJECT_CLASS:
                unregistered = objectClassRegistry.unregister( ( ObjectClass ) schemaObject );
                break;

            case SYNTAX_CHECKER:
                unregistered = syntaxCheckerRegistry.unregister( ( SyntaxChecker ) schemaObject );
                break;

            default:
                throw new IllegalArgumentException( 
                    I18n.err( I18n.ERR_13718_UNEXPECTED_SCHEMA_OBJECT_TYPE, schemaObject.getObjectType() ) );
        }

        return unregistered;
    }


    /**
     * Checks if a specific SchemaObject is referenced by any other SchemaObject.
     *
     * @param schemaObject The SchemaObject we are looking for
     * @return true if there is at least one SchemaObjetc referencing the given one
     */
    public boolean isReferenced( SchemaObject schemaObject )
    {
        SchemaObjectWrapper wrapper = new SchemaObjectWrapper( schemaObject );

        Set<SchemaObjectWrapper> set = usedBy.get( wrapper );

        boolean referenced = ( set != null ) && !set.isEmpty();

        if ( LOG.isDebugEnabled() )
        {
            if ( referenced )
            {
                LOG.debug( I18n.msg( I18n.MSG_13735_REFERENCED, schemaObject.getObjectType(), schemaObject.getOid() ) );
            }
            else
            {
                LOG.debug( I18n.msg( I18n.MSG_13734_NOT_REFERENCED, schemaObject.getObjectType(), schemaObject.getOid() ) );
            }
        }

        return referenced;
    }


    /**
     * Gets the Set of SchemaObjects referencing the given SchemaObject
     *
     * @param schemaObject The SchemaObject we are looking for
     * @return The Set of referencing SchemaObject, or null
     */
    public Set<SchemaObjectWrapper> getUsedBy( SchemaObject schemaObject )
    {
        SchemaObjectWrapper wrapper = new SchemaObjectWrapper( schemaObject );

        return usedBy.get( wrapper );
    }


    /**
     * Dump the UsedBy data structure as a String
     * 
     * @return The UsedBy data structure
     */
    public String dumpUsedBy()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "USED BY :\n" );

        try
        {
            for ( Map.Entry<SchemaObjectWrapper, Set<SchemaObjectWrapper>> entry : usedBy.entrySet() )
            {
                SchemaObjectWrapper wrapper = entry.getKey();

                sb.append( wrapper.get().getObjectType() ).append( '[' ).append( wrapper.get().getOid() )
                    .append( "] : {" );

                boolean isFirst = true;

                for ( SchemaObjectWrapper uses : entry.getValue() )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        sb.append( ", " );
                    }

                    sb.append( uses.get().getObjectType() ).append( '[' ).append( wrapper.get().getOid() ).append( "]" );
                }

                sb.append( "}\n" );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return sb.toString();
    }


    /**
     * Dump the Using data structure as a String
     * 
     * @return The Using data structure
     */
    public String dumpUsing()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "USING :\n" );

        try
        {
            for ( Map.Entry<SchemaObjectWrapper, Set<SchemaObjectWrapper>> entry : using.entrySet() )
            {
                SchemaObjectWrapper wrapper = entry.getKey();

                sb.append( wrapper.get().getObjectType() ).append( '[' ).append( wrapper.get().getOid() )
                    .append( "] : {" );

                boolean isFirst = true;

                for ( SchemaObjectWrapper uses : entry.getValue() )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        sb.append( ", " );
                    }

                    sb.append( uses.get().getObjectType() ).append( '[' ).append( wrapper.get().getOid() ).append( "]" );
                }

                sb.append( "}\n" );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return sb.toString();
    }


    /**
     * Gets the Set of SchemaObjects referenced by the given SchemaObject
     *
     * @param schemaObject The SchemaObject we are looking for
     * @return The Set of referenced SchemaObject, or null
     */
    public Set<SchemaObjectWrapper> getUsing( SchemaObject schemaObject )
    {
        SchemaObjectWrapper wrapper = new SchemaObjectWrapper( schemaObject );

        return using.get( wrapper );
    }


    /**
     * Add an association between a SchemaObject an the SchemaObject it refers
     *
     * @param reference The base SchemaObject
     * @param referee The SchemaObject pointing on the reference
     */
    private void addUsing( SchemaObject reference, SchemaObject referee )
    {
        if ( ( reference == null ) || ( referee == null ) )
        {
            return;
        }

        SchemaObjectWrapper wrapper = new SchemaObjectWrapper( reference );

        Set<SchemaObjectWrapper> uses = getUsing( reference );

        if ( uses == null )
        {
            uses = new HashSet<>();
        }

        uses.add( new SchemaObjectWrapper( referee ) );

        // Put back the set (this is a concurrentHashMap, it won't be replaced implicitly
        using.put( wrapper, uses );
    }


    /**
     * Add an association between a SchemaObject an the SchemaObject it refers
     *
     * @param base The base SchemaObject
     * @param referenced The referenced SchemaObject
     */
    public void addReference( SchemaObject base, SchemaObject referenced )
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( dump( "add", base, referenced ) );
        }

        addUsing( base, referenced );
        addUsedBy( referenced, base );

        // do not change to debug mode, this makes the server logs hard to read and useless
        // and even prevents the server from starting up
        if ( LOG.isTraceEnabled() )
        {
            LOG.trace( dumpUsedBy() );
            LOG.trace( dumpUsing() );
        }
    }


    /**
     * Add an association between a SchemaObject an the SchemaObject that refers it
     *
     * @param reference The base SchemaObject
     * @param referee The SchemaObject pointing on the reference
     */
    private void addUsedBy( SchemaObject referee, SchemaObject reference )
    {
        if ( ( reference == null ) || ( referee == null ) )
        {
            return;
        }

        SchemaObjectWrapper wrapper = new SchemaObjectWrapper( referee );

        Set<SchemaObjectWrapper> uses = getUsedBy( referee );

        if ( uses == null )
        {
            uses = new HashSet<>();
        }

        uses.add( new SchemaObjectWrapper( reference ) );

        // Put back the set (this is a concurrentHashMap, it won't be replaced implicitly
        usedBy.put( wrapper, uses );
    }


    /**
     * Del an association between a SchemaObject an the SchemaObject it refers
     *
     * @param reference The base SchemaObject
     * @param referee The SchemaObject pointing on the reference
     */
    private void delUsing( SchemaObject reference, SchemaObject referee )
    {
        if ( ( reference == null ) || ( referee == null ) )
        {
            return;
        }

        Set<SchemaObjectWrapper> uses = getUsing( reference );

        if ( uses == null )
        {
            return;
        }

        uses.remove( new SchemaObjectWrapper( referee ) );

        SchemaObjectWrapper wrapper = new SchemaObjectWrapper( reference );

        if ( uses.isEmpty() )
        {
            using.remove( wrapper );
        }
        else
        {
            using.put( wrapper, uses );
        }
    }


    /**
     * Del an association between a SchemaObject an the SchemaObject that refers it
     *
     * @param reference The base SchemaObject
     * @param referee The SchemaObject pointing on the reference
     */
    private void delUsedBy( SchemaObject referee, SchemaObject reference )
    {
        if ( ( reference == null ) || ( referee == null ) )
        {
            return;
        }

        Set<SchemaObjectWrapper> uses = getUsedBy( referee );

        if ( uses == null )
        {
            return;
        }

        uses.remove( new SchemaObjectWrapper( reference ) );

        SchemaObjectWrapper wrapper = new SchemaObjectWrapper( referee );

        if ( uses.isEmpty() )
        {
            usedBy.remove( wrapper );
        }
        else
        {
            usedBy.put( wrapper, uses );
        }
    }


    /**
     * Delete an association between a SchemaObject an the SchemaObject it refers
     *
     * @param base The base SchemaObject
     * @param referenced The referenced SchemaObject
     */
    public void delReference( SchemaObject base, SchemaObject referenced )
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( dump( "del", base, referenced ) );
        }

        delUsing( base, referenced );
        delUsedBy( referenced, base );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( dumpUsedBy() );
            LOG.debug( dumpUsing() );
        }
    }


    /**
     * Dump the reference operation as a String
     * 
     * @param op The operation
     * @param reference The reference
     * @param referee The referee
     * @return The resulting string
     */
    private String dump( String op, SchemaObject reference, SchemaObject referee )
    {
        return op + " : " + reference.getObjectType() + "[" + reference.getOid() + "]/[" + referee.getObjectType()
            + "[" + referee.getOid() + "]";
    }


    private boolean checkReferences( SchemaObject reference, SchemaObject referee, String message )
    {
        SchemaObjectWrapper referenceWrapper = new SchemaObjectWrapper( reference );
        SchemaObjectWrapper refereeWrapper = new SchemaObjectWrapper( referee );

        // Check the references : Syntax -> SyntaxChecker
        if ( !using.containsKey( referenceWrapper ) )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( 
                    I18n.msg( I18n.MSG_13730_SYN_DOES_NOT_REFERENCE, reference.getObjectType(), reference.getOid(), message ) );
            }

            return false;
        }

        Set<SchemaObjectWrapper> usings = using.get( referenceWrapper );

        if ( !usings.contains( refereeWrapper ) )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13732_NOT_REFERENCE_ANY, reference.getObjectType(), reference.getOid(), message ) );
            }

            return false;
        }

        // Check the referees : SyntaxChecker -> Syntax
        if ( !usedBy.containsKey( refereeWrapper ) )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13733_NOT_REFERENCED_BY_ANY, referee.getObjectType(), referee.getOid(), message ) );
            }

            return false;
        }

        Set<SchemaObjectWrapper> used = usedBy.get( refereeWrapper );

        if ( !used.contains( referenceWrapper ) )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13733_NOT_REFERENCED_BY_ANY, referee.getObjectType(), referee.getOid(), message ) );
            }

            return false;
        }

        return true;
    }


    /**
     * Check the registries for invalid relations. This check stops at the first error.
     *
     * @return true if the Registries is consistent, false otherwise
     */
    public boolean check()
    {
        // Check the Syntaxes : check for a SyntaxChecker
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13717_CHECKING_SYNTAXES ) );
        }

        for ( LdapSyntax syntax : ldapSyntaxRegistry )
        {
            // Check that each Syntax has a SyntaxChecker
            if ( syntax.getSyntaxChecker() == null )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13729_SYN_WITH_NO_SYNTAX_CHECKER, syntax ) );
                }

                return false;
            }

            if ( !syntaxCheckerRegistry.contains( syntax.getSyntaxChecker().getOid() ) )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13713_CANT_FIND_SC_FOR_SYN, syntax.getSyntaxChecker().getOid(),
                        syntax ) );
                }

                return false;
            }

            // Check the references : Syntax -> SyntaxChecker and SyntaxChecker -> Syntax
            if ( !checkReferences( syntax, syntax.getSyntaxChecker(), "SyntaxChecker" ) )
            {
                return false;
            }
        }

        // Check the MatchingRules : check for a Normalizer, a Comparator and a Syntax
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13715_CHECKING_MATCHING_RULES ) );
        }

        for ( MatchingRule matchingRule : matchingRuleRegistry )
        {
            // Check that each MatchingRule has a Normalizer
            if ( matchingRule.getNormalizer() == null )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13727_MR_WITH_NO_NORMALIZER, matchingRule ) );
                }

                return false;
            }

            // Check that each MatchingRule has a Normalizer
            if ( !normalizerRegistry.contains( matchingRule.getNormalizer().getOid() ) )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13709_CANT_FIND_NORM_FOR_MR, matchingRule.getNormalizer()
                        .getOid(), matchingRule ) );
                }

                return false;
            }

            // Check that each MatchingRule has a Comparator
            if ( matchingRule.getLdapComparator() == null )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13726_MR_WITH_NO_COMPARATOR, matchingRule ) );
                }

                return false;
            }

            if ( !comparatorRegistry.contains( matchingRule.getLdapComparator().getOid() ) )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13707_CANT_FIND_AT_FOR_MR, matchingRule.getLdapComparator().getOid(), 
                        matchingRule ) );
                } 

                return false;
            }

            // Check that each MatchingRule has a Syntax
            if ( matchingRule.getSyntax() == null )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13728_MR_WITH_NO_SYNTAX, matchingRule ) );
                }

                return false;
            }

            if ( !ldapSyntaxRegistry.contains( matchingRule.getSyntax().getOid() ) )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13712_CANT_FIND_SYN_FOR_MR, matchingRule.getSyntax().getOid(),
                        matchingRule ) );
                }
                    
                return false;
            }

            // Check the references : MR -> S and S -> MR
            if ( !checkReferences( matchingRule, matchingRule.getSyntax(), "Syntax" ) )
            {
                return false;
            }

            // Check the references : MR -> N
            if ( !checkReferences( matchingRule, matchingRule.getNormalizer(), "Normalizer" ) )
            {
                return false;
            }

            // Check the references : MR -> C and C -> MR
            if ( !checkReferences( matchingRule, matchingRule.getLdapComparator(), "Comparator" ) )
            {
                return false;
            }
        }

        // Check the ObjectClasses : check for MAY, MUST, SUPERIORS
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13716_CHECKING_OBJECT_CLASSES ) );
        }

        for ( ObjectClass objectClass : objectClassRegistry )
        {
            // Check that each ObjectClass has all the MAY AttributeTypes
            if ( objectClass.getMayAttributeTypes() != null )
            {
                for ( AttributeType may : objectClass.getMayAttributeTypes() )
                {
                    if ( !attributeTypeRegistry.contains( may.getOid() ) )
                    {
                        if ( LOG.isDebugEnabled() )
                        {
                            LOG.debug( I18n.msg( I18n.MSG_13705_CANT_FIND_AT_IN_MAY, may, objectClass ) );
                        }

                        return false;
                    }

                    // Check the references : OC -> AT  and AT -> OC (MAY)
                    if ( !checkReferences( objectClass, may, "AttributeType" ) )
                    {
                        return false;
                    }
                }
            }

            // Check that each ObjectClass has all the MUST AttributeTypes
            if ( objectClass.getMustAttributeTypes() != null )
            {
                for ( AttributeType must : objectClass.getMustAttributeTypes() )
                {
                    if ( !attributeTypeRegistry.contains( must.getOid() ) )
                    {
                        if ( LOG.isDebugEnabled() )
                        {
                            LOG.debug( I18n.msg( I18n.MSG_13706_CANT_FIND_AT_IN_MUST, must, objectClass ) );
                        }

                        return false;
                    }

                    // Check the references : OC -> AT  and AT -> OC (MUST)
                    if ( !checkReferences( objectClass, must, "AttributeType" ) )
                    {
                        return false;
                    }
                }
            }

            // Check that each ObjectClass has all the SUPERIORS ObjectClasses
            if ( objectClass.getSuperiors() != null )
            {
                for ( ObjectClass superior : objectClass.getSuperiors() )
                {
                    if ( !objectClassRegistry.contains( objectClass.getOid() ) )
                    {
                        if ( LOG.isDebugEnabled() )
                        {
                            LOG.debug( I18n.msg( I18n.MSG_13710_CANT_FIND_OC_WITH_SUPERIOR, superior, objectClass ) );
                        }

                        return false;
                    }

                    // Check the references : OC -> OC  and OC -> OC (SUPERIORS)
                    if ( !checkReferences( objectClass, superior, "ObjectClass" ) )
                    {
                        return false;
                    }
                }
            }
        }

        // Check the AttributeTypes : check for MatchingRules, Syntaxes
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13714_CHECKING_ATTRIBUTE_TYPES ) );
        }

        for ( AttributeType attributeType : attributeTypeRegistry )
        {
            // Check that each AttributeType has a SYNTAX
            if ( attributeType.getSyntax() == null )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13725_AT_WITH_NO_SYNTAX, attributeType ) );
                }

                return false;
            }

            if ( !ldapSyntaxRegistry.contains( attributeType.getSyntax().getOid() ) )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_13711_CANT_FIND_SYN_FOR_AT, attributeType.getSyntax().getOid(),
                        attributeType ) );
                }

                return false;
            }

            // Check the references for AT -> S and S -> AT
            if ( !checkReferences( attributeType, attributeType.getSyntax(), "AttributeType" ) )
            {
                return false;
            }

            // Check the EQUALITY MatchingRule
            if ( attributeType.getEquality() != null )
            {
                if ( !matchingRuleRegistry.contains( attributeType.getEquality().getOid() ) )
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_13708_CANT_FIND_MR_FOR_AT, attributeType.getEquality()
                            .getOid(), attributeType ) );
                    }

                    return false;
                }

                // Check the references for AT -> MR and MR -> AT
                if ( !checkReferences( attributeType, attributeType.getEquality(), "AttributeType" ) )
                {
                    return false;
                }
            }

            // Check the ORDERING MatchingRule
            if ( attributeType.getOrdering() != null )
            {
                if ( !matchingRuleRegistry.contains( attributeType.getOrdering().getOid() ) )
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_13708_CANT_FIND_MR_FOR_AT, attributeType.getOrdering()
                            .getOid(), attributeType ) );
                    }

                    return false;
                }

                // Check the references for AT -> MR and MR -> AT
                if ( !checkReferences( attributeType, attributeType.getOrdering(), "AttributeType" ) )
                {
                    return false;
                }
            }

            // Check the SUBSTR MatchingRule
            if ( attributeType.getSubstring() != null )
            {
                if ( !matchingRuleRegistry.contains( attributeType.getSubstring().getOid() ) )
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_13708_CANT_FIND_MR_FOR_AT, attributeType.getSubstring()
                            .getOid(), attributeType ) );
                    }

                    return false;
                }

                // Check the references for AT -> MR and MR -> AT
                if ( !checkReferences( attributeType, attributeType.getSubstring(), "AttributeType" ) )
                {
                    return false;
                }
            }

            // Check the SUP
            if ( attributeType.getSuperior() != null )
            {
                AttributeType superior = attributeType.getSuperior();

                if ( !attributeTypeRegistry.contains( superior.getOid() ) )
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_13704_CANT_FIND_AT_WITH_SUPERIOR, superior, attributeType ) );
                    }

                    return false;
                }

                // Check the references : AT -> AT  and AT -> AT (SUPERIOR)
                if ( !checkReferences( attributeType, superior, "AttributeType" ) )
                {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * Clone the Registries. This is done in two steps :
     * - first clone the SchemaObjetc registries
     * - second restore the relation between them
     */
    // False positive
    @Override
    public Registries clone() throws CloneNotSupportedException
    {
        // First clone the structure
        Registries clone = ( Registries ) super.clone();

        // Now, clone the oidRegistry
        clone.globalOidRegistry = globalOidRegistry.copy();

        // We have to clone every SchemaObject registries now
        clone.attributeTypeRegistry = attributeTypeRegistry.copy();
        clone.comparatorRegistry = comparatorRegistry.copy();
        clone.ditContentRuleRegistry = ditContentRuleRegistry.copy();
        clone.ditStructureRuleRegistry = ditStructureRuleRegistry.copy();
        clone.ldapSyntaxRegistry = ldapSyntaxRegistry.copy();
        clone.matchingRuleRegistry = matchingRuleRegistry.copy();
        clone.matchingRuleUseRegistry = matchingRuleUseRegistry.copy();
        clone.nameFormRegistry = nameFormRegistry.copy();
        clone.normalizerRegistry = normalizerRegistry.copy();
        clone.objectClassRegistry = objectClassRegistry.copy();
        clone.syntaxCheckerRegistry = syntaxCheckerRegistry.copy();
        clone.errorHandler = errorHandler;

        // Store all the SchemaObjects into the globalOid registry
        for ( AttributeType attributeType : clone.attributeTypeRegistry )
        {
            clone.globalOidRegistry.put( attributeType );
        }

        for ( DitContentRule ditContentRule : clone.ditContentRuleRegistry )
        {
            clone.globalOidRegistry.put( ditContentRule );
        }

        for ( DitStructureRule ditStructureRule : clone.ditStructureRuleRegistry )
        {
            clone.globalOidRegistry.put( ditStructureRule );
        }

        for ( MatchingRule matchingRule : clone.matchingRuleRegistry )
        {
            clone.globalOidRegistry.put( matchingRule );
        }

        for ( MatchingRuleUse matchingRuleUse : clone.matchingRuleUseRegistry )
        {
            clone.globalOidRegistry.put( matchingRuleUse );
        }

        for ( NameForm nameForm : clone.nameFormRegistry )
        {
            clone.globalOidRegistry.put( nameForm );
        }

        for ( ObjectClass objectClass : clone.objectClassRegistry )
        {
            clone.globalOidRegistry.put( objectClass );
        }

        for ( LdapSyntax syntax : clone.ldapSyntaxRegistry )
        {
            clone.globalOidRegistry.put( syntax );
        }

        // Clone the schema list
        clone.loadedSchemas = new HashMap<>();

        for ( Map.Entry<String, Set<SchemaObjectWrapper>> entry : schemaObjects.entrySet() )
        {
            // We don't clone the schemas
            clone.loadedSchemas.put( entry.getKey(), loadedSchemas.get( entry.getKey() ) );
        }

        // Clone the Using and usedBy structures
        // They will be empty
        clone.using = new HashMap<>();
        clone.usedBy = new HashMap<>();

        // Last, rebuild the using and usedBy references
        clone.buildReferences();

        // Now, check the registries. We don't care about errors
        clone.checkRefInteg();

        clone.schemaObjects = new HashMap<>();

        // Last, not least, clone the SchemaObjects Map, and reference all the copied
        // SchemaObjects
        for ( Map.Entry<String, Set<SchemaObjectWrapper>> entry : schemaObjects.entrySet() )
        {
            Set<SchemaObjectWrapper> objects = new HashSet<>();

            for ( SchemaObjectWrapper schemaObjectWrapper : entry.getValue() )
            {
                SchemaObject original = schemaObjectWrapper.get();

                try
                {
                    if ( !( original instanceof LoadableSchemaObject ) )
                    {
                        SchemaObject copy = clone.globalOidRegistry.getSchemaObject( original.getOid() );
                        SchemaObjectWrapper newWrapper = new SchemaObjectWrapper( copy );
                        objects.add( newWrapper );
                    }
                    else
                    {
                        SchemaObjectWrapper newWrapper = new SchemaObjectWrapper( original );
                        objects.add( newWrapper );
                    }
                }
                catch ( LdapException ne )
                {
                    // Nothing to do
                }
            }

            clone.schemaObjects.put( entry.getKey(), objects );
        }

        return clone;
    }


    /**
     * Tells if the Registries is permissive or if it must be checked
     * against inconsistencies.
     *
     * @return True if SchemaObjects can be added even if they break the consistency
     */
    public boolean isRelaxed()
    {
        return isRelaxed;
    }


    /**
     * Tells if the Registries is strict.
     *
     * @return True if SchemaObjects cannot be added if they break the consistency
     */
    public boolean isStrict()
    {
        return !isRelaxed;
    }


    /**
     * Change the Registries to a relaxed mode, where invalid SchemaObjects
     * can be registered.
     */
    public void setRelaxed()
    {
        isRelaxed = RELAXED;
        globalOidRegistry.setRelaxed();
        attributeTypeRegistry.setRelaxed();
        comparatorRegistry.setRelaxed();
        ditContentRuleRegistry.setRelaxed();
        ditStructureRuleRegistry.setRelaxed();
        ldapSyntaxRegistry.setRelaxed();
        matchingRuleRegistry.setRelaxed();
        matchingRuleUseRegistry.setRelaxed();
        nameFormRegistry.setRelaxed();
        normalizerRegistry.setRelaxed();
        objectClassRegistry.setRelaxed();
        syntaxCheckerRegistry.setRelaxed();
    }


    /**
     * Change the Registries to a strict mode, where invalid SchemaObjects
     * cannot be registered.
     */
    public void setStrict()
    {
        isRelaxed = STRICT;
        globalOidRegistry.setStrict();
        attributeTypeRegistry.setStrict();
        comparatorRegistry.setStrict();
        ditContentRuleRegistry.setStrict();
        ditStructureRuleRegistry.setStrict();
        ldapSyntaxRegistry.setStrict();
        matchingRuleRegistry.setStrict();
        matchingRuleUseRegistry.setStrict();
        nameFormRegistry.setStrict();
        normalizerRegistry.setStrict();
        objectClassRegistry.setStrict();
        syntaxCheckerRegistry.setStrict();
    }


    public SchemaErrorHandler getErrorHandler()
    {
        return errorHandler;
    }


    public void setErrorHandler( SchemaErrorHandler errorHandler )
    {
        this.errorHandler = errorHandler;
        globalOidRegistry.setErrorHandler( errorHandler );
        attributeTypeRegistry.setErrorHandler( errorHandler );
        comparatorRegistry.setErrorHandler( errorHandler );
        ditContentRuleRegistry.setErrorHandler( errorHandler );
        ditStructureRuleRegistry.setErrorHandler( errorHandler );
        ldapSyntaxRegistry.setErrorHandler( errorHandler );
        matchingRuleRegistry.setErrorHandler( errorHandler );
        matchingRuleUseRegistry.setErrorHandler( errorHandler );
        nameFormRegistry.setErrorHandler( errorHandler );
        normalizerRegistry.setErrorHandler( errorHandler );
        objectClassRegistry.setErrorHandler( errorHandler );
        syntaxCheckerRegistry.setErrorHandler( errorHandler );
    }


    /**
     * Tells if the Registries accept disabled elements.
     *
     * @return True if disabled SchemaObjects can be added
     */
    public boolean isDisabledAccepted()
    {
        return disabledAccepted;
    }


    /**
     * Check that we can remove a given SchemaObject without breaking some of its references.
     * We will return the list of refereing objects.
     *
     * @param schemaObject The SchemaObject to remove
     * @return The list of SchemaObjects referencing the SchemaObjetc we want to remove
     */
    public Set<SchemaObjectWrapper> getReferencing( SchemaObject schemaObject )
    {
        SchemaObjectWrapper schemaObjectWrapper = new SchemaObjectWrapper( schemaObject );

        return usedBy.get( schemaObjectWrapper );
    }


    /**
     * Change the Registries behavior regarding disabled SchemaObject element.
     *
     * @param disabledAccepted If <code>false</code>, then the Registries won't accept
     * disabled SchemaObject or enabled SchemaObject from disabled schema
     */
    public void setDisabledAccepted( boolean disabledAccepted )
    {
        this.disabledAccepted = disabledAccepted;
    }


    /**
     * Clear the registries from all its elements
     *
     * @throws LdapException If something goes wrong
     */
    public void clear() throws LdapException
    {
        // The AttributeTypeRegistry
        if ( attributeTypeRegistry != null )
        {
            attributeTypeRegistry.clear();
        }

        // The ComparatorRegistry
        if ( comparatorRegistry != null )
        {
            comparatorRegistry.clear();
        }

        // The DitContentRuleRegistry
        if ( ditContentRuleRegistry != null )
        {
            ditContentRuleRegistry.clear();
        }

        // The DitStructureRuleRegistry
        if ( ditStructureRuleRegistry != null )
        {
            ditStructureRuleRegistry.clear();
        }

        // The MatchingRuleRegistry
        if ( matchingRuleRegistry != null )
        {
            matchingRuleRegistry.clear();
        }

        // The MatchingRuleUseRegistry
        if ( matchingRuleUseRegistry != null )
        {
            matchingRuleUseRegistry.clear();
        }

        // The NameFormRegistry
        if ( nameFormRegistry != null )
        {
            nameFormRegistry.clear();
        }

        // The NormalizerRegistry
        if ( normalizerRegistry != null )
        {
            normalizerRegistry.clear();
        }

        // The ObjectClassRegistry
        if ( objectClassRegistry != null )
        {
            objectClassRegistry.clear();
        }

        // The SyntaxRegistry
        if ( ldapSyntaxRegistry != null )
        {
            ldapSyntaxRegistry.clear();
        }

        // The SyntaxCheckerRegistry
        if ( syntaxCheckerRegistry != null )
        {
            syntaxCheckerRegistry.clear();
        }

        // Clear the schemaObjects map
        for ( Map.Entry<String, Set<SchemaObjectWrapper>> entry : schemaObjects.entrySet() )
        {
            entry.getValue().clear();
        }

        schemaObjects.clear();

        // Clear the usedBy map
        for ( Map.Entry<SchemaObjectWrapper, Set<SchemaObjectWrapper>> entry : usedBy.entrySet() )
        {
            entry.getValue().clear();
        }

        usedBy.clear();

        // Clear the using map
        for ( Map.Entry<SchemaObjectWrapper, Set<SchemaObjectWrapper>> entry : using.entrySet() )
        {
            entry.getValue().clear();
        }

        using.clear();

        // Clear the global OID registry
        globalOidRegistry.clear();

        // Clear the loadedSchema Map
        loadedSchemas.clear();
    }


    /**
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "Registries [" );

        if ( isRelaxed )
        {
            sb.append( "RELAXED," );
        }
        else
        {
            sb.append( "STRICT," );
        }

        if ( disabledAccepted )
        {
            sb.append( " Disabled accepted] :\n" );
        }
        else
        {
            sb.append( " Disabled forbidden] :\n" );
        }

        sb.append( "loaded schemas [" );
        boolean isFirst = true;

        for ( String schema : loadedSchemas.keySet() )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " );
            }

            sb.append( schema );
        }

        sb.append( "]\n" );

        sb.append( "AttributeTypes : " ).append( attributeTypeRegistry.size() ).append( "\n" );
        sb.append( "Comparators : " ).append( comparatorRegistry.size() ).append( "\n" );
        sb.append( "DitContentRules : " ).append( ditContentRuleRegistry.size() ).append( "\n" );
        sb.append( "DitStructureRules : " ).append( ditStructureRuleRegistry.size() ).append( "\n" );
        sb.append( "MatchingRules : " ).append( matchingRuleRegistry.size() ).append( "\n" );
        sb.append( "MatchingRuleUses : " ).append( matchingRuleUseRegistry.size() ).append( "\n" );
        sb.append( "NameForms : " ).append( nameFormRegistry.size() ).append( "\n" );
        sb.append( "Normalizers : " ).append( normalizerRegistry.size() ).append( "\n" );
        sb.append( "ObjectClasses : " ).append( objectClassRegistry.size() ).append( "\n" );
        sb.append( "Syntaxes : " ).append( ldapSyntaxRegistry.size() ).append( "\n" );
        sb.append( "SyntaxCheckers : " ).append( syntaxCheckerRegistry.size() ).append( "\n" );

        sb.append( "GlobalOidRegistry : " ).append( globalOidRegistry.size() ).append( '\n' );

        return sb.toString();
    }
}
