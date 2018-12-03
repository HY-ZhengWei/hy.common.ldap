/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.api.ldap.model.ldif;


import org.apache.directory.api.i18n.I18n;


/**
 * A type safe enumeration for an LDIF record's change type.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum ChangeType
{
    /** The Add changeType */
    Add(0),

    /** The Modify changeType */
    Modify(1),

    /** The ModDn changeType */
    ModDn(2),

    /** The ModRdn changeType */
    ModRdn(3),

    /** The Delete changeType */
    Delete(4),

    /** A place-holder when we have no changeType */
    None(-1);

    /** Add ordinal value */
    public static final int ADD_ORDINAL = 0;

    /** Modify ordinal value */
    public static final int MODIFY_ORDINAL = 1;

    /** ModDN ordinal value */
    public static final int MODDN_ORDINAL = 2;

    /** ModRDN ordinal value */
    public static final int MODRDN_ORDINAL = 3;

    /** Delete ordinal value */
    public static final int DELETE_ORDINAL = 4;

    /** None ordinal value */
    public static final int NONE_ORDINAL = -1;

    /** the ordinal value for a change type */
    private final int changeType;


    /**
     * Creates a new instance of ChangeType.
     *
     * @param changeType The associated value 
     */
    ChangeType( int changeType )
    {
        this.changeType = changeType;
    }


    /**
     * Get's the ordinal value for a ChangeType.
     * 
     * @return the changeType
     */
    public int getChangeType()
    {
        return changeType;
    }


    /**
     * Get the ChangeType instance from an integer value 
     *
     * @param val The value for the ChangeType we are looking for
     * @return The associated ChangeType instance
     */
    public static ChangeType getChangeType( int val )
    {
        switch ( val )
        {
            case -1:
                return None;

            case 0:
                return Add;

            case 1:
                return Modify;

            case 2:
                return ModDn;

            case 3:
                return ModRdn;

            case 4:
                return Delete;

            default:
                throw new IllegalArgumentException( I18n.err( I18n.ERR_13438_UNKNOWN_CHANGE_TYPE, val ) );
        }
    }
}
