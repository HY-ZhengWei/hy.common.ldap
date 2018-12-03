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
package org.apache.directory.api.ldap.model.message.controls;

/**
 * A persistence search object
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PersistentSearchImpl extends AbstractControl implements PersistentSearch
{

    /**
     * If changesOnly is TRUE, the server MUST NOT return any existing entries
     * that match the search criteria. Entries are only returned when they are
     * changed (added, modified, deleted, or subject to a modifyDN operation).
     */
    private boolean changesOnly = true;

    /**
     * If returnECs is TRUE, the server MUST return an Entry Change Notification
     * control with each entry returned as the result of changes.
     */
    private boolean returnECs = false;

    /**
     * As changes are made to the server, the effected entries MUST be returned
     * to the client if they match the standard search criteria and if the
     * operation that caused the change is included in the changeTypes field.
     * The changeTypes field is the logical OR of one or more of these values:
     * add    (1),
     * delete (2),
     * modify (4),
     * modDN  (8).
     */
    private int changeTypes = CHANGE_TYPES_MAX;


    /**
     * Default constructor
     *
     */
    public PersistentSearchImpl()
    {
        super( OID );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setChangesOnly( boolean changesOnly )
    {
        this.changesOnly = changesOnly;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChangesOnly()
    {
        return changesOnly;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setReturnECs( boolean returnECs )
    {
        this.returnECs = returnECs;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReturnECs()
    {
        return returnECs;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setChangeTypes( int changeTypes )
    {
        this.changeTypes = changeTypes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getChangeTypes()
    {
        return changeTypes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNotificationEnabled( ChangeType changeType )
    {
        return ( changeType.getValue() & changeTypes ) > 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void enableNotification( ChangeType changeType )
    {
        changeTypes |= changeType.getValue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void disableNotification( ChangeType changeType )
    {
        changeTypes &= ~changeType.getValue();
    }


    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        int h = super.hashCode();

        h = h * 37 + ( changesOnly ? 1 : 0 );
        h = h * 37 + ( returnECs ? 1 : 0 );
        h = h * 37 + changeTypes;

        return h;
    }


    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals( Object other )
    {
        if ( this == other )
        {
            return true;
        }

        if ( !( other instanceof PersistentSearch ) )
        {
            return false;
        }
        
        PersistentSearch otherControl = ( PersistentSearch ) other;

        return super.equals( other ) 
            && ( changesOnly == otherControl.isChangesOnly() ) 
            && ( returnECs == otherControl.isReturnECs() ) 
            && ( changeTypes == otherControl.getChangeTypes() );
    }


    /**
     * Return a String representing this PSearchControl.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "    Persistant Search Control\n" );
        sb.append( "        oid : " ).append( getOid() ).append( '\n' );
        sb.append( "        critical : " ).append( isCritical() ).append( '\n' );
        sb.append( "        changeTypes : '" ).append( changeTypes ).append( "'\n" );
        sb.append( "        changesOnly : '" ).append( changesOnly ).append( "'\n" );
        sb.append( "        returnECs   : '" ).append( returnECs ).append( "'\n" );

        return sb.toString();
    }
}
