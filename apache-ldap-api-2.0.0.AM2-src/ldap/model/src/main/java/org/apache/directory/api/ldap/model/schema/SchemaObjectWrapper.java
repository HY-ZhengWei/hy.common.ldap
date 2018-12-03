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


/**
 * A class containing a SchemaObject, used by the global registries. As the hash code
 * method of the SchemaObject class is too complex, we had to define a simplest class
 * for this purpose, where the hash code is computed using only the SchemaObject
 * type and its OID.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaObjectWrapper
{
    /** The internal schemaObject */
    private SchemaObject schemaObject;


    /**
     * Creates a new instance of SchemaObjectWrapper.
     *
     * @param schemaObject The contained SchemaObject
     */
    public SchemaObjectWrapper( SchemaObject schemaObject )
    {
        this.schemaObject = schemaObject;
    }


    /**
     * Compute the hash code for this wrapper. We only use the object type
     * and its oid.
     */
    @Override
    public int hashCode()
    {
        int h = 37;
        h += h * 17 + schemaObject.getObjectType().getValue();

        if ( schemaObject.getOid() != null )
        {
            h += h * 17 + schemaObject.getOid().hashCode();
        }

        return h;
    }


    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals( Object o )
    {
        if ( o == this )
        {
            return true;
        }

        if ( !( o instanceof SchemaObjectWrapper ) )
        {
            return false;
        }

        SchemaObject that = ( ( SchemaObjectWrapper ) o ).get();
        SchemaObject current = get();

        // Ultimately, that has to be true, regardless of the OID value
        if ( that.getObjectType() != current.getObjectType() )
        {
            return false;
        }

        // If both OID are null, instances are equals
        if ( that.getOid() == null )
        {
            return current.getOid() == null;
        }

        // The that'oid will never be null, we don't really care if current.oid is null here.
        return that.getOid().equals( current.getOid() );
    }


    /**
     *  @return The interned SchemaObject
     */
    public SchemaObject get()
    {
        return schemaObject;
    }


    /**
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "<" + schemaObject.getObjectType() + "," + schemaObject.getOid() + ">";
    }
}
