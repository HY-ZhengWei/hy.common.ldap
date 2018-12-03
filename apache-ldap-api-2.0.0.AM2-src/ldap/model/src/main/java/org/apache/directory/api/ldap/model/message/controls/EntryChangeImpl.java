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


import org.apache.directory.api.ldap.model.name.Dn;


/**
 * A simple implementation of the EntryChange response control.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryChangeImpl extends AbstractControl implements EntryChange
{
    /** The changeType */
    private ChangeType changeType = ChangeType.ADD;

    private long changeNumber = UNDEFINED_CHANGE_NUMBER;

    /** The previous Dn */
    private Dn previousDn = null;


    /**
     *
     * Creates a new instance of EntryChangeControl.
     *
     */
    public EntryChangeImpl()
    {
        super( OID );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeType getChangeType()
    {
        return changeType;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setChangeType( ChangeType changeType )
    {
        this.changeType = changeType;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Dn getPreviousDn()
    {
        return previousDn;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreviousDn( Dn previousDn )
    {
        this.previousDn = previousDn;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long getChangeNumber()
    {
        return changeNumber;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setChangeNumber( long changeNumber )
    {
        this.changeNumber = changeNumber;
    }


    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        int h = super.hashCode();

        h = h * 37 + ( int ) changeNumber;
        h = h * 37 + ( changeType == null ? 0 : changeType.hashCode() );
        h = h * 37 + ( previousDn == null ? 0 : previousDn.hashCode() );

        return h;
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
        
        if ( !( o instanceof EntryChange ) )
        {
            return false;
        }

        EntryChange otherControl = ( EntryChange ) o;

        return super.equals( o ) 
            && ( changeNumber == otherControl.getChangeNumber() ) 
            && ( changeType == otherControl.getChangeType() )
            && ( previousDn.equals( otherControl.getPreviousDn() ) );
    }


    /**
     * Return a String representing this EntryChangeControl.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "    Entry Change Control\n" );
        sb.append( "        oid : " ).append( getOid() ).append( '\n' );
        sb.append( "        critical : " ).append( isCritical() ).append( '\n' );
        sb.append( "        changeType   : '" ).append( changeType ).append( "'\n" );
        sb.append( "        previousDN   : '" ).append( previousDn ).append( "'\n" );

        if ( changeNumber == UNDEFINED_CHANGE_NUMBER )
        {
            sb.append( "        changeNumber : '" ).append( "UNDEFINED" ).append( "'\n" );
        }
        else
        {
            sb.append( "        changeNumber : '" ).append( changeNumber ).append( "'\n" );
        }

        return sb.toString();
    }
}
