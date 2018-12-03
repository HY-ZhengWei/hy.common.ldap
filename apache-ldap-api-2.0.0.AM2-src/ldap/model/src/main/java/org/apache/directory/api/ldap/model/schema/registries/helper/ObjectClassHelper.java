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
package org.apache.directory.api.ldap.model.schema.registries.helper;

import java.util.List;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapSchemaException;
import org.apache.directory.api.ldap.model.exception.LdapSchemaExceptionCodes;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.SchemaErrorHandler;
import org.apache.directory.api.ldap.model.schema.registries.AttributeTypeRegistry;
import org.apache.directory.api.ldap.model.schema.registries.ObjectClassRegistry;
import org.apache.directory.api.ldap.model.schema.registries.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An helper class used to store all the methods associated with an ObjectClass
 * in relation with the Registries and SchemaManager.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ObjectClassHelper
{
    /** A logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( ObjectClassHelper.class );

    private ObjectClassHelper()
    {
    }


    /**
     * Inject the ObjectClass into the registries, updating the references to
     * other SchemaObject
     *
     * @param objectClass The ObjectClass to add to the Registries
     * @param errorHandler Error handler
     * @param registries The Registries
     */
    public static void addToRegistries( ObjectClass objectClass, SchemaErrorHandler errorHandler, Registries registries )
    {
        if ( registries != null )
        {
            try
            {
                objectClass.unlock();
                
                // The superiors
                buildSuperiors( objectClass, errorHandler, registries );
    
                // The MAY AttributeTypes
                buildMay( objectClass, errorHandler, registries );
    
                // The MUST AttributeTypes
                buildMust( objectClass, errorHandler, registries );
    
                /**
                 * Add the OC references (using and usedBy) :
                 * OC -> AT (MAY and MUST)
                 * OC -> OC (SUPERIORS)
                 */
                for ( AttributeType mayAttributeType : objectClass.getMayAttributeTypes() )
                {
                    registries.addReference( objectClass, mayAttributeType );
                }
    
                for ( AttributeType mustAttributeType : objectClass.getMustAttributeTypes() )
                {
                    registries.addReference( objectClass, mustAttributeType );
                }
    
                for ( ObjectClass superiorObjectClass : objectClass.getSuperiors() )
                {
                    registries.addReference( objectClass, superiorObjectClass );
                }
            }
            finally
            {
                objectClass.lock();
            }
        }
    }


    /**
     * Build the references to this ObjectClass SUPERIORS, checking that the type
     * hierarchy is correct.
     * 
     * @param objectClass The oOjectClass to process
     * @param errorHandler The error handler
     * @param registries The Registries instance
     */
    private static void buildSuperiors( ObjectClass objectClass, SchemaErrorHandler errorHandler, Registries registries )
    {
        ObjectClassRegistry ocRegistry = registries.getObjectClassRegistry();
        List<String> superiorOids = objectClass.getSuperiorOids();

        if ( superiorOids != null )
        {
            objectClass.getSuperiors().clear();

            for ( String superiorName : superiorOids )
            {
                try
                {
                    ObjectClass superior = ocRegistry.lookup( ocRegistry.getOidByName( superiorName ) );

                    // Before adding the superior, check that the ObjectClass type is consistent
                    switch ( objectClass.getType() )
                    {
                        case ABSTRACT:
                            if ( superior.getType() != ObjectClassTypeEnum.ABSTRACT )
                            {
                                // An ABSTRACT OC can only inherit from ABSTRACT OCs
                                String msg = I18n.err( I18n.ERR_13766_ABSTRACT_OC_CANNOT_INHERIT_FROM_OC, 
                                    objectClass.getOid(), superior.getObjectType(), superior );

                                LdapSchemaException ldapSchemaException = new LdapSchemaException(
                                    LdapSchemaExceptionCodes.OC_ABSTRACT_MUST_INHERIT_FROM_ABSTRACT_OC, msg );
                                ldapSchemaException.setSourceObject( objectClass );
                                errorHandler.handle( LOG, msg, ldapSchemaException );

                                continue;
                            }

                            break;

                        case AUXILIARY:
                            if ( superior.getType() == ObjectClassTypeEnum.STRUCTURAL )
                            {
                                // An AUXILIARY OC cannot inherit from STRUCTURAL OCs
                                String msg = I18n.err( I18n.ERR_13767_AUX_OC_CANNOT_INHERIT_FROM_STRUCT_OC, objectClass.getOid(), superior );

                                LdapSchemaException ldapSchemaException = new LdapSchemaException(
                                    LdapSchemaExceptionCodes.OC_AUXILIARY_CANNOT_INHERIT_FROM_STRUCTURAL_OC, msg );
                                ldapSchemaException.setSourceObject( objectClass );
                                errorHandler.handle( LOG, msg, ldapSchemaException );

                                continue;
                            }

                            break;

                        case STRUCTURAL:
                            if ( superior.getType() == ObjectClassTypeEnum.AUXILIARY )
                            {
                                // A STRUCTURAL OC cannot inherit from AUXILIARY OCs
                                String msg = I18n.err( I18n.ERR_13768_STRUCT_OC_CANNOT_INHERIT_FROM_AUX_OC, objectClass.getOid(), superior );

                                LdapSchemaException ldapSchemaException = new LdapSchemaException(
                                    LdapSchemaExceptionCodes.OC_STRUCTURAL_CANNOT_INHERIT_FROM_AUXILIARY_OC, msg );
                                ldapSchemaException.setSourceObject( objectClass );
                                errorHandler.handle( LOG, msg, ldapSchemaException );

                                continue;
                            }

                            break;

                        default:
                            throw new IllegalArgumentException( I18n.err( I18n.ERR_13717_UNEXPECTED_OBJECT_CLASS_TYPE_ENUM, 
                                objectClass.getType() ) );
                    }

                    objectClass.getSuperiors().add( superior );
                }
                catch ( LdapException ne )
                {
                    // Cannot find the OC
                    String msg = I18n.err( I18n.ERR_13769_CANNOT_REGISTER_SUPERIOR_MISSING, 
                        objectClass.getOid(), superiorName );

                    LdapSchemaException ldapSchemaException = new LdapSchemaException(
                        LdapSchemaExceptionCodes.OC_NONEXISTENT_SUPERIOR, msg, ne );
                    ldapSchemaException.setSourceObject( objectClass );
                    ldapSchemaException.setRelatedId( superiorName );
                    errorHandler.handle( LOG, msg, ldapSchemaException );

                    return;
                }
            }
        }
    }


    /**
     * Build and check the MUST AT for this ObjectClass.
     * 
     * @param objectClass The oOjectClass to process
     * @param errorHandler The error handler
     * @param registries The Registries instance
     */
    private static void buildMust( ObjectClass objectClass, SchemaErrorHandler errorHandler, Registries registries )
    {
        AttributeTypeRegistry atRegistry = registries.getAttributeTypeRegistry();
        List<String> mustAttributeTypeOids = objectClass.getMustAttributeTypeOids();

        if ( mustAttributeTypeOids != null )
        {
            objectClass.getMustAttributeTypes().clear();

            for ( String mustAttributeTypeName : mustAttributeTypeOids )
            {
                try
                {
                    AttributeType attributeType = atRegistry.lookup( mustAttributeTypeName );

                    if ( attributeType.isCollective() )
                    {
                        // Collective Attributes are not allowed in MAY or MUST
                        String msg = I18n.err( I18n.ERR_13778_COLLECTIVE_NOT_ALLOWED_IN_MUST, mustAttributeTypeName,
                            objectClass.getOid() );

                        LdapSchemaException ldapSchemaException = new LdapSchemaException(
                            LdapSchemaExceptionCodes.OC_COLLECTIVE_NOT_ALLOWED_IN_MUST, msg );
                        ldapSchemaException.setSourceObject( objectClass );
                        ldapSchemaException.setRelatedId( mustAttributeTypeName );
                        errorHandler.handle( LOG, msg, ldapSchemaException );

                        continue;
                    }

                    if ( objectClass.getMustAttributeTypes().contains( attributeType ) )
                    {
                        // Already registered : this is an error
                        String msg = I18n.err( I18n.ERR_13772_CANNOT_REGISTER_DUPLICATE_AT_IN_MUST, 
                            objectClass.getOid(), mustAttributeTypeName );

                        LdapSchemaException ldapSchemaException = new LdapSchemaException(
                            LdapSchemaExceptionCodes.OC_DUPLICATE_AT_IN_MUST, msg );
                        ldapSchemaException.setSourceObject( objectClass );
                        ldapSchemaException.setRelatedId( mustAttributeTypeName );
                        errorHandler.handle( LOG, msg, ldapSchemaException );

                        continue;
                    }

                    // Check that the MUST AT is not also present in the MAY AT
                    if ( objectClass.getMayAttributeTypes().contains( attributeType ) )
                    {
                        // Already registered : this is an error
                        String msg = I18n.err( I18n.ERR_13773_CANNOT_REGISTER_DUPLICATE_AT_IN_MAY_AND_MUST, 
                            objectClass.getOid(), mustAttributeTypeName );

                        LdapSchemaException ldapSchemaException = new LdapSchemaException(
                            LdapSchemaExceptionCodes.OC_DUPLICATE_AT_IN_MAY_AND_MUST,
                            msg );
                        ldapSchemaException.setSourceObject( objectClass );
                        ldapSchemaException.setRelatedId( mustAttributeTypeName );
                        errorHandler.handle( LOG, msg, ldapSchemaException );

                        continue;
                    }

                    objectClass.getMustAttributeTypes().add( attributeType );
                }
                catch ( LdapException ne )
                {
                    // Cannot find the AT
                    String msg = I18n.err( I18n.ERR_13774_CANNOT_REGISTER_AT_IN_MUST_DOES_NOT_EXIST, 
                        objectClass.getOid(), mustAttributeTypeName );

                    LdapSchemaException ldapSchemaException = new LdapSchemaException(
                        LdapSchemaExceptionCodes.OC_NONEXISTENT_MUST_AT, msg, ne );
                    ldapSchemaException.setSourceObject( objectClass );
                    ldapSchemaException.setRelatedId( mustAttributeTypeName );
                    errorHandler.handle( LOG, msg, ldapSchemaException );
                }
            }
        }
    }
    
    
    /**
     * Build and check the MAY AT for this ObjectClass
     * 
     * @param objectClass The oOjectClass to process
     * @param errorHandler The error handler
     * @param registries The Registries instance
     */
    private static void buildMay( ObjectClass objectClass, SchemaErrorHandler errorHandler, Registries registries )
    {
        AttributeTypeRegistry atRegistry = registries.getAttributeTypeRegistry();
        List<String> mayAttributeTypeOids = objectClass.getMayAttributeTypeOids();

        if ( mayAttributeTypeOids != null )
        {
            objectClass.getMayAttributeTypes().clear();

            for ( String mayAttributeTypeName : mayAttributeTypeOids )
            {
                try
                {
                    AttributeType attributeType = atRegistry.lookup( mayAttributeTypeName );

                    if ( attributeType.isCollective() )
                    {
                        // Collective Attributes are not allowed in MAY or MUST
                        String msg = I18n.err( I18n.ERR_13779_COLLECTIVE_NOT_ALLOWED_IN_MAY, mayAttributeTypeName, objectClass.getOid() );

                        LdapSchemaException ldapSchemaException = new LdapSchemaException(
                            LdapSchemaExceptionCodes.OC_COLLECTIVE_NOT_ALLOWED_IN_MAY, msg );
                        ldapSchemaException.setSourceObject( objectClass );
                        ldapSchemaException.setRelatedId( mayAttributeTypeName );
                        errorHandler.handle( LOG, msg, ldapSchemaException );

                        continue;
                    }

                    if ( objectClass.getMayAttributeTypes().contains( attributeType ) )
                    {
                        // Already registered : this is an error
                        String msg = I18n.err( 
                            I18n.ERR_13770_CANNOT_REGISTER_DUPLICATE_AT_IN_MAY, objectClass.getOid(), mayAttributeTypeName );

                        LdapSchemaException ldapSchemaException = new LdapSchemaException(
                            LdapSchemaExceptionCodes.OC_DUPLICATE_AT_IN_MAY, msg );
                        ldapSchemaException.setSourceObject( objectClass );
                        ldapSchemaException.setRelatedId( mayAttributeTypeName );
                        errorHandler.handle( LOG, msg, ldapSchemaException );

                        continue;
                    }

                    objectClass.getMayAttributeTypes().add( attributeType );
                }
                catch ( LdapException ne )
                {
                    // Cannot find the AT
                    String msg = I18n.err( I18n.ERR_13771_CANNOT_REGISTER_AT_IN_MAY_DOES_NOT_EXIST, objectClass.getOid(), mayAttributeTypeName );

                    LdapSchemaException ldapSchemaException = new LdapSchemaException(
                        LdapSchemaExceptionCodes.OC_NONEXISTENT_MAY_AT, msg, ne );
                    ldapSchemaException.setSourceObject( objectClass );
                    ldapSchemaException.setRelatedId( mayAttributeTypeName );
                    errorHandler.handle( LOG, msg, ldapSchemaException );
                }
            }
        }
    }
    
    
    /**
     * Remove the ObjectClass from the registries, updating the references to
     * other SchemaObject.
     *
     * If one of the referenced SchemaObject does not exist (SUPERIORS, MAY, MUST),
     * an exception is thrown.
     *
     * @param objectClass The ObjectClass to remove fro the registries
     * @param errorHandler Error handler
     * @param registries The Registries
     * @throws LdapException If the ObjectClass is not valid
     */
    public static void removeFromRegistries( ObjectClass objectClass, SchemaErrorHandler errorHandler, Registries registries ) throws LdapException
    {
        if ( registries != null )
        {
            ObjectClassRegistry objectClassRegistry = registries.getObjectClassRegistry();

            // Unregister this ObjectClass into the Descendant map
            objectClassRegistry.unregisterDescendants( objectClass, objectClass.getSuperiors() );

            /**
             * Remove the OC references (using and usedBy) :
             * OC -> AT (for MAY and MUST)
             * OC -> OC
             */
            if ( objectClass.getMayAttributeTypes() != null )
            {
                for ( AttributeType may : objectClass.getMayAttributeTypes() )
                {
                    registries.delReference( objectClass, may );
                }
            }

            if ( objectClass.getMustAttributeTypes() != null )
            {
                for ( AttributeType must : objectClass.getMustAttributeTypes() )
                {
                    registries.delReference( objectClass, must );
                }
            }

            if ( objectClass.getSuperiors() != null )
            {
                for ( ObjectClass superior : objectClass.getSuperiors() )
                {
                    registries.delReference( objectClass, superior );
                }
            }
        }
    }
}
