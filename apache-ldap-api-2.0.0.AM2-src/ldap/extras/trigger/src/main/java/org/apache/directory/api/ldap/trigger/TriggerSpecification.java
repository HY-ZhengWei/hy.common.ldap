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


import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.apache.directory.api.i18n.I18n;


/**
 * The Trigger Specification Bean.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TriggerSpecification
{

    private LdapOperation ldapOperation;

    private ActionTime actionTime;

    private List<SPSpec> spSpecs;


    /**
     * Instantiates a new trigger specification.
     *
     * @param ldapOperation the LDAP operation
     * @param actionTime the action time
     * @param spSpecs the stored procedure specs
     */
    public TriggerSpecification( LdapOperation ldapOperation, ActionTime actionTime, List<SPSpec> spSpecs )
    {
        super();
        
        if ( ( ldapOperation == null ) || ( actionTime == null ) || ( spSpecs == null ) )
        {
            throw new NullArgumentException( I18n.err( I18n.ERR_11000_TRIGGER_SPECIFICATION_INIT_WITH_NULL ) );
        }
        
        if ( spSpecs.isEmpty() )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_11001_TRIGGER_SPECIFICATION_INIT_WITH_EPTY_SPEC_LIST ) );
        }
        
        this.ldapOperation = ldapOperation;
        this.actionTime = actionTime;
        this.spSpecs = spSpecs;
    }


    /**
     * Gets the action time.
     *
     * @return the action time
     */
    public ActionTime getActionTime()
    {
        return actionTime;
    }


    /**
     * Gets the LDAP operation.
     *
     * @return the LDAP operation
     */
    public LdapOperation getLdapOperation()
    {
        return ldapOperation;
    }


    /**
     * Gets the stored procedure specs.
     *
     * @return the stored procedure specs
     */
    public List<SPSpec> getSPSpecs()
    {
        return spSpecs;
    }

    /**
     * The stored procedure spec bean.
     */
    public static class SPSpec
    {
        private String name;

        private List<StoredProcedureOption> options;

        private List<StoredProcedureParameter> parameters;


        /**
         * Instantiates a new stored procedure spec.
         *
         * @param name the name
         * @param options the options
         * @param parameters the parameters
         */
        public SPSpec( String name, List<StoredProcedureOption> options, List<StoredProcedureParameter> parameters )
        {
            super();
            this.name = name;
            this.options = options;
            this.parameters = parameters;
        }


        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName()
        {
            return name;
        }


        /**
         * Gets the options.
         *
         * @return the options
         */
        public List<StoredProcedureOption> getOptions()
        {
            return options;
        }


        /**
         * Gets the parameters.
         *
         * @return the parameters
         */
        public List<StoredProcedureParameter> getParameters()
        {
            return parameters;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            int h = 37;

            h = h * 17 + ( ( name == null ) ? 0 : name.hashCode() );
            h = h * 17 + ( ( options == null ) ? 0 : options.hashCode() );
            h = h * 17 + ( ( parameters == null ) ? 0 : parameters.hashCode() );
            
            return h;
        }


        /**
         * {@inheritDoc}
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
            
            SPSpec other = ( SPSpec ) obj;
            
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
            
            if ( options == null )
            {
                if ( other.options != null )
                {
                    return false;
                }
            }
            else if ( !options.equals( other.options ) )
            {
                return false;
            }
            
            if ( parameters == null )
            {
                if ( other.parameters != null )
                {
                    return false;
                }
            }
            else if ( !parameters.equals( other.parameters ) )
            {
                return false;
            }
            
            return true;
        }
    }
}
