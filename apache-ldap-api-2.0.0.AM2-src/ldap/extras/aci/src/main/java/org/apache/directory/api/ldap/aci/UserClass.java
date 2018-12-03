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
package org.apache.directory.api.ldap.aci;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.directory.api.ldap.model.subtree.SubtreeSpecification;


/**
 * Defines a set of zero or more users the permissions apply to.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class UserClass
{
    /**
     * Every directory user (with possible requirements for
     * authenticationLevel).
     */
    public static final AllUsers ALL_USERS = new AllUsers();

    /**
     * The user with the same distinguished name as the entry being accessed, or
     * if the entry is a member of a family, then additionally the user with the
     * distinguished name of the ancestor.
     */
    public static final ThisEntry THIS_ENTRY = new ThisEntry();

    /**
     * The user as parent (ancestor) of accessed entry.
     */
    public static final ParentOfEntry PARENT_OF_ENTRY = new ParentOfEntry();


    /**
     * Creates a new instance.
     */
    protected UserClass()
    {
    }
    

    /**
     * Every directory user (with possible requirements for
     * authenticationLevel).
     */
    public static final class AllUsers extends UserClass
    {
        /**
         * Creates a new instance of AllUsers.
         */
        private AllUsers()
        {
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "allUsers";
        }
    }
    

    /**
     * The user with the same distinguished name as the entry being accessed, or
     * if the entry is a member of a family, then additionally the user with the
     * distinguished name of the ancestor.
     */
    public static final class ThisEntry extends UserClass
    {
        /**
         * Creates a new instance of ThisEntry.
         */
        private ThisEntry()
        {
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "thisEntry";
        }
    }
    

    /**
     * The user as parent (ancestor) of accessed entry.
     */
    public static final class ParentOfEntry extends UserClass
    {
        /**
         * Creates a new instance of ParentOfEntry.
         */
        private ParentOfEntry()
        {
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "parentOfEntry";
        }
    }
    

    /**
     * A base class for all user classes which has a set of DNs.
     */
    private abstract static class NamedUserClass extends UserClass
    {
        /** The names. */
        protected final Set<String> names;


        /**
         * Creates a new instance.
         * 
         * @param names a set of names
         */
        protected NamedUserClass( Set<String> names )
        {
            if ( names == null )
            {
                this.names = Collections.unmodifiableSet( new HashSet<String>() );
            }
            else
            {
                this.names = Collections.unmodifiableSet( new HashSet<String>( names ) );
            }
        }


        /**
         * Returns the set of all names.
         * 
         * @return The set of all names
         */
        public Set<String> getNames()
        {
            return names;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            
            if ( !( o instanceof NamedUserClass ) )
            {
                return false;
            }

            if ( getClass().isAssignableFrom( o.getClass() ) )
            {
                Name that = ( Name ) o;
                
                return names.equals( that.names );
            }

            return false;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            int result = 37;

            // Use a slightly different hashcode here : we multiple
            // each DN in the set with the result to have a result that
            // is not dependent on the DN order in the Set.
            // In order to avoid result of 0 if one of the DN hashcode,
            // we discard them.
            for ( String dn : names )
            {
                int h = dn.hashCode();
                
                if ( h != 0 )
                {
                    result = result * h;
                }
            }

            return result;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            StringBuilder buffer = new StringBuilder();

            boolean isFirst = true;
            buffer.append( "{ " );

            for ( String name : names )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    buffer.append( ", " );
                }

                buffer.append( '"' );
                buffer.append( name );
                buffer.append( '"' );
            }

            buffer.append( " }" );

            return buffer.toString();
        }
    }
    

    /**
     * The user with the specified distinguished name.
     */
    public static class Name extends NamedUserClass
    {
        /**
         * Creates a new instance.
         * 
         * @param usernames the set of user DNs.
         */
        public Name( Set<String> usernames )
        {
            super( usernames );
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "name " + super.toString();
        }
    }
    

    /**
     * The set of users who are members of the groupOfUniqueNames entry,
     * identified by the specified distinguished name. Members of a group of
     * unique names are treated as individual object names, and not as the names
     * of other groups of unique names.
     */
    public static class UserGroup extends NamedUserClass
    {
        /**
         * Creates a new instance.
         * 
         * @param groupNames the set of group DNs.
         */
        public UserGroup( Set<String> groupNames )
        {
            super( groupNames );
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "userGroup " + super.toString();
        }
    }
    

    /**
     * The set of users whose distinguished names fall within the definition of
     * the (unrefined) subtree.
     */
    public static class Subtree extends UserClass
    {
        /** The subtree specifications. */
        protected final Set<SubtreeSpecification> subtreeSpecifications;


        /**
         * Creates a new instance.
         * 
         * @param subtreeSpecs the collection of unrefined {@link SubtreeSpecification}s.
         */
        public Subtree( Set<SubtreeSpecification> subtreeSpecs )
        {
            subtreeSpecifications = Collections.unmodifiableSet( subtreeSpecs );
        }


        /**
         * Returns the collection of unrefined {@link SubtreeSpecification}s.
         *
         * @return the subtree specifications
         */
        public Set<SubtreeSpecification> getSubtreeSpecifications()
        {
            return subtreeSpecifications;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            int hash = 37;
            hash = hash * 17 + subtreeSpecifications.hashCode();

            return hash;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }

            if ( o instanceof Subtree )
            {
                Subtree that = ( Subtree ) o;
                
                return subtreeSpecifications.equals( that.subtreeSpecifications );
            }

            return false;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            StringBuilder buffer = new StringBuilder();

            boolean isFirst = true;
            buffer.append( "subtree { " );

            for ( SubtreeSpecification ss : subtreeSpecifications )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    buffer.append( ", " );
                }

                ss.toString( buffer );
            }

            buffer.append( " }" );

            return buffer.toString();
        }
    }
}
