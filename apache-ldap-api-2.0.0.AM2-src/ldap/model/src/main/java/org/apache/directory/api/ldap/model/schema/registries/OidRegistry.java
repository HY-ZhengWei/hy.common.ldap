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


import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.SchemaErrorHandler;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Object identifier registry. It stores the OIDs for AT, OC, MR, LS, MRU, DSR, DCR and NF.
 * An OID is unique, and associated with a SO.
 * 
 * @param <T> The type of SchemaObject
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OidRegistry<T extends SchemaObject> implements Iterable<T>
{
    /** static class logger */
    private static final Logger LOG = LoggerFactory.getLogger( OidRegistry.class );

    /** Maps OID to a type of SchemaObject */
    private Map<String, T> byOid = new HashMap<>();
    
    /** A flag indicating that the Registry is relaxed or not */
    private boolean isRelaxed = Registries.STRICT;

    private SchemaErrorHandler errorHandler;

    /**
     * Tells if the given OID is present on this registry
     * 
     * @param oid The OID to lookup
     * @return true if the OID already exists
     */
    public boolean contains( String oid )
    {
        return byOid.containsKey( oid );
    }


    /**
     * Gets the primary name associated with an OID.  The primary name is the
     * first name specified for the OID.
     * 
     * @param oid the object identifier
     * @return the primary name
     * @throws LdapException if oid does not exist
     */
    public String getPrimaryName( String oid ) throws LdapException
    {
        SchemaObject schemaObject = byOid.get( oid );

        if ( schemaObject != null )
        {
            return schemaObject.getName();
        }
        else
        {
            String msg = I18n.err( I18n.ERR_13741_OID_NOT_FOUND_IN_REGISTRY, oid );
            LdapException error = new LdapException( msg );
            errorHandler.handle( LOG, msg, error );
            throw error;
        }
    }


    /**
     * Gets the SchemaObject associated with an OID. 
     * 
     * @param oid the object identifier
     * @return the associated SchemaObject
     * @throws LdapException if oid does not exist
     */
    public T getSchemaObject( String oid ) throws LdapException
    {
        T schemaObject = byOid.get( oid );

        if ( schemaObject != null )
        {
            return schemaObject;
        }
        else
        {
            String msg = I18n.err( I18n.ERR_13742_NO_SCHEMA_OBJECT_WITH_OID, oid );
            LdapException error = new LdapException( msg );
            errorHandler.handle( LOG, msg, error );
            throw error;
        }
    }


    /**
     * Gets the names associated with an OID.  An OID is unique however it may 
     * have many names used to refer to it.  A good example is the cn and
     * commonName attribute names for OID 2.5.4.3.  Within a server one name 
     * within the set must be chosen as the primary name.  This is used to
     * name certain things within the server internally.  If there is more than
     * one name then the first name is taken to be the primary.
     * 
     * @param oid the OID for which we return the set of common names
     * @return a sorted set of names
     * @throws org.apache.directory.api.ldap.model.exception.LdapException if oid does not exist
     */
    public List<String> getNameSet( String oid ) throws LdapException
    {
        SchemaObject schemaObject = byOid.get( oid );

        if ( null == schemaObject )
        {
            String msg = I18n.err( I18n.ERR_13741_OID_NOT_FOUND_IN_REGISTRY, oid );
            LdapException error = new LdapException( msg );
            errorHandler.handle( LOG, msg, error );
            throw error;
        }

        List<String> names = schemaObject.getNames();

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13756_LOOKED_UP_NAME, ArrayUtils.toString( names ), oid ) );
        }

        return names;
    }


    /**
     * Lists all the OIDs within the registry.  This may be a really big list.
     * 
     * @return all the OIDs registered
     */
    public Iterator<String> iteratorOids()
    {
        return Collections.unmodifiableSet( byOid.keySet() ).iterator();
    }


    /**
     * Lists all the SchemaObjects within the registry.  This may be a really big list.
     * 
     * @return all the SchemaObject registered
     */
    @Override
    public Iterator<T> iterator()
    {
        return byOid.values().iterator();
    }

    
    /**
     * Tells if the Registry is permissive or if it must be checked
     * against inconsistencies.
     *
     * @return True if SchemaObjects can be added even if they break the consistency
     */
    public boolean isRelaxed()
    {
        return isRelaxed;
    }


    /**
     * Tells if the Registry is strict.
     *
     * @return True if SchemaObjects cannot be added if they break the consistency
     */
    public boolean isStrict()
    {
        return !isRelaxed;
    }


    /**
     * Change the Registry to a relaxed mode, where invalid SchemaObjects
     * can be registered.
     */
    public void setRelaxed()
    {
        isRelaxed = Registries.RELAXED;
    }


    /**
     * Change the Registry to a strict mode, where invalid SchemaObjects
     * cannot be registered.
     */
    public void setStrict()
    {
        isRelaxed = Registries.STRICT;
    }

    
    public SchemaErrorHandler getErrorHandler()
    {
        return errorHandler;
    }


    public void setErrorHandler( SchemaErrorHandler errorHandler )
    {
        this.errorHandler = errorHandler;
    }


    /**
     * Adds an OID name pair to the registry.
     * 
     * @param schemaObject The SchemaObject the oid belongs to
     * @throws LdapException If something went wrong
     */
    public void register( T schemaObject ) throws LdapException
    {
        if ( schemaObject == null )
        {
            String message = I18n.err( I18n.ERR_13743_CANNOT_REGISTER_NULL_SCHEMA_OBJECT );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( message );
            }
            
            throw new LdapException( message );
        }

        String oid = schemaObject.getOid();

        if ( isStrict() )
        {
            if ( !Oid.isOid( oid ) )
            {
                String message = I18n.err( I18n.ERR_13744_SCHEMA_OBJECT_HAS_NO_VALID_OID );

                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( message );
                }
                
                throw new LdapException( message );
            }
        }
        else
        {
            if ( ( oid == null ) || oid.isEmpty() )
            {
                throw new LdapException( I18n.err( I18n.ERR_00003_INVALID_OID, "" ) );
            }
        }

        /*
         * Update OID Map if it does not already exist
         */
        if ( byOid.containsKey( oid ) )
        {
            errorHandler.handle( LOG, I18n.err( I18n.ERR_13745_SCHEMA_OBJECT_WITH_OID_ALREADY_EXIST, oid ), null );
        }
        else
        {
            byOid.put( oid, schemaObject );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_13742_REGISTERED_SCHEMA_OBJECT, schemaObject, oid ) );
            }
        }
    }


    /**
     * Store the given SchemaObject into the OidRegistry. Available only to 
     * the current package. A weak form (no check is done) of the register 
     * method, define for clone methods.
     *
     * @param schemaObject The SchemaObject to inject into the OidRegistry
     */
    /* No qualifier */void put( T schemaObject )
    {
        byOid.put( schemaObject.getOid(), schemaObject );
    }


    /**
     * Removes an oid from this registry.
     *
     * @param oid the numeric identifier for the object
     * @throws LdapException if the identifier is not numeric
     */
    public void unregister( String oid ) throws LdapException
    {
        // Removes the <OID, names> from the byOID map
        SchemaObject removed = byOid.remove( oid );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_13736_UNREGISTERED_SCHEMA_OBJECT, removed, oid ) );
        }
    }


    /**
     * Copy the OidRegistry, without the contained values
     * 
     * @return A new OidRegistry instance
     */
    public OidRegistry<T> copy()
    {
        OidRegistry<T> copy = new OidRegistry<>();

        // Clone the map
        copy.byOid = new HashMap<>();

        return copy;
    }


    /**
     * @return The number of stored OIDs
     */
    public int size()
    {
        return byOid.size();
    }


    /**
     * Empty the byOid map
     */
    public void clear()
    {
        // remove all the OID
        byOid.clear();
    }


    /**
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if ( byOid != null )
        {
            boolean isFirst = true;

            for ( Map.Entry<String, T> entry : byOid.entrySet() )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    sb.append( ", " );
                }

                sb.append( "<" );

                SchemaObject schemaObject = entry.getValue();

                if ( schemaObject != null )
                {
                    sb.append( schemaObject.getObjectType() );
                    sb.append( ", " );
                    sb.append( schemaObject.getOid() );
                    sb.append( ", " );
                    sb.append( schemaObject.getName() );
                }

                sb.append( ">" );
            }
        }

        return sb.toString();
    }
}
