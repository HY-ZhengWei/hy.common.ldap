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


import org.apache.directory.api.ldap.model.name.Dn;


/**
 * An entity that represents a stored procedure parameter which can be
 * specified in an LDAP Trigger Specification.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class StoredProcedureParameter
{
    /**
     * The generic LdapContext factory
     */
    public static final class Generic_LDAP_CONTEXT extends StoredProcedureParameter
    {
        private Dn ctxName;


        private Generic_LDAP_CONTEXT( Dn ctxName )
        {
            super( "$ldapContext" );
            this.ctxName = ctxName;
        }


        /**
         * Creates a new instance of StoredProcedureParameter
         * 
         * @param ctxName The context name
         * @return A new instance of StoredProcedureParameter
         */
        public static StoredProcedureParameter instance( Dn ctxName )
        {
            return new Generic_LDAP_CONTEXT( ctxName );
        }


        /**
         * @return The context name
         */
        public Dn getCtxName()
        {
            return ctxName;
        }


        /**
         * @see Object#toString()
         */
        @Override
        public String toString()
        {
            return name + " \"" + ctxName.getName() + "\"";
        }
    }


    /**
     * The generic Operation Principal factory
     */
    public static final class Generic_OPERATION_PRINCIPAL extends StoredProcedureParameter
    {
        private static Generic_OPERATION_PRINCIPAL instance = new Generic_OPERATION_PRINCIPAL( "$operationPrincipal" );


        private Generic_OPERATION_PRINCIPAL( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The generic Operation Principal instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }

    protected final String name;


    protected StoredProcedureParameter( String name )
    {
        this.name = name;
    }


    /**
     * @return the name of this Stored Procedure Parameter.
     */
    public String getName()
    {
        return name;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return name;
    }


    /**
     * @see java.lang.Object#hashCode()
     * @return the instance's hash code 
     */
    @Override
    public int hashCode()
    {
        int h = 37;

        h = h * 17 + ( ( name == null ) ? 0 : name.hashCode() );

        return h;
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( obj == null )
        {
            return false;
        }
        
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        
        StoredProcedureParameter other = ( StoredProcedureParameter ) obj;
        
        if ( name == null )
        {
            if ( other.name != null )
            {
                return false;
            }
        }
        else if ( !name.equals( other.name ) )
        {
            return false;
        }
        
        return true;
    }
    

    /**
     * The Modify Object factory
     */
    public static final class Modify_OBJECT extends StoredProcedureParameter
    {
        private static Modify_OBJECT instance = new Modify_OBJECT( "$object" );


        private Modify_OBJECT( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify Object instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify Modification factory
     */
    public static final class Modify_MODIFICATION extends StoredProcedureParameter
    {
        private static Modify_MODIFICATION instance = new Modify_MODIFICATION( "$modification" );


        private Modify_MODIFICATION( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify Modification instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify Old Entry factory
     */
    public static final class Modify_OLD_ENTRY extends StoredProcedureParameter
    {
        private static Modify_OLD_ENTRY instance = new Modify_OLD_ENTRY( "$oldEntry" );


        private Modify_OLD_ENTRY( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify Old Entry instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify New Entry factory
     */
    public static final class Modify_NEW_ENTRY extends StoredProcedureParameter
    {
        private static Modify_NEW_ENTRY instance = new Modify_NEW_ENTRY( "$newEntry" );


        private Modify_NEW_ENTRY( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify New Entry instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Add Entry factory
     */
    public static final class Add_ENTRY extends StoredProcedureParameter
    {
        private static Add_ENTRY instance = new Add_ENTRY( "$entry" );


        private Add_ENTRY( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Add Entry instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Add Attributes factory
     */
    public static final class Add_ATTRIBUTES extends StoredProcedureParameter
    {
        private static Add_ATTRIBUTES instance = new Add_ATTRIBUTES( "$attributes" );


        private Add_ATTRIBUTES( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Add Attributes instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Delete Name factory
     */
    public static final class Delete_NAME extends StoredProcedureParameter
    {
        private static Delete_NAME instance = new Delete_NAME( "$name" );


        private Delete_NAME( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Delete Name instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Delete Deleted  factory
     */
    public static final class Delete_DELETED_ENTRY extends StoredProcedureParameter
    {
        private static Delete_DELETED_ENTRY instance = new Delete_DELETED_ENTRY( "$deletedEntry" );


        private Delete_DELETED_ENTRY( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Delete Deleted instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify DN Entry factory
     */
    public static final class ModifyDN_ENTRY extends StoredProcedureParameter
    {
        private static ModifyDN_ENTRY instance = new ModifyDN_ENTRY( "$entry" );


        private ModifyDN_ENTRY( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify DN Entry instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify New Rdn factory
     */
    public static final class ModifyDN_NEW_RDN extends StoredProcedureParameter
    {
        private static ModifyDN_NEW_RDN instance = new ModifyDN_NEW_RDN( "$newrdn" );


        private ModifyDN_NEW_RDN( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify New Rdn instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify DN Delete Old RDN factory
     */
    public static final class ModifyDN_DELETE_OLD_RDN extends StoredProcedureParameter
    {
        private static ModifyDN_DELETE_OLD_RDN instance = new ModifyDN_DELETE_OLD_RDN( "$deleteoldrdn" );


        private ModifyDN_DELETE_OLD_RDN( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify DN Delete Old RDN instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify DN New Superior factory
     */
    public static final class ModifyDN_NEW_SUPERIOR extends StoredProcedureParameter
    {
        private static ModifyDN_NEW_SUPERIOR instance = new ModifyDN_NEW_SUPERIOR( "$newSuperior" );


        private ModifyDN_NEW_SUPERIOR( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify DN New Superior instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify DN Old RDN factory
     */
    public static final class ModifyDN_OLD_RDN extends StoredProcedureParameter
    {
        private static ModifyDN_OLD_RDN instance = new ModifyDN_OLD_RDN( "$oldRDN" );


        private ModifyDN_OLD_RDN( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify DN Old RDN instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify DN Old Superior DN factory
     */
    public static final class ModifyDN_OLD_SUPERIOR_DN extends StoredProcedureParameter
    {
        private static ModifyDN_OLD_SUPERIOR_DN instance = new ModifyDN_OLD_SUPERIOR_DN( "$oldRDN" );


        private ModifyDN_OLD_SUPERIOR_DN( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify DN Old Superior DN instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }


    /**
     * The Modify DN New DN factory
     */
    public static final class ModifyDN_NEW_DN extends StoredProcedureParameter
    {
        private static ModifyDN_NEW_DN instance = new ModifyDN_NEW_DN( "$oldRDN" );


        private ModifyDN_NEW_DN( String identifier )
        {
            super( identifier );
        }


        /**
         * @return The Modify DN New DN instance
         */
        public static StoredProcedureParameter instance()
        {
            return instance;
        }
    }
}
