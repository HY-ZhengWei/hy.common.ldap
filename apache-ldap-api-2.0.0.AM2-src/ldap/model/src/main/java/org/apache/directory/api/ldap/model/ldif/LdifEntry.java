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
package org.apache.directory.api.ldap.model.ldif;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.util.Base64;
import org.apache.directory.api.util.Strings;


/**
 * A entry to be populated by an ldif parser.
 * 
 * We will have different kind of entries : 
 * <ul>
 * <li>added entries</li>
 * <li>deleted entries</li>
 * <li>modified entries</li>
 * <li>Rdn modified entries</li>
 * <li>Dn modified entries</li>
 * </ul>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEntry implements Cloneable, Externalizable, Iterable<Attribute>
{
    /** Used in toArray() */
    public static final Modification[] EMPTY_MODS = new Modification[0];

    /** the change type */
    private ChangeType changeType;

    /** the modification item list */
    private List<Modification> modificationList;

    /** The map containing all the modifications */
    private Map<String, Modification> modifications;

    /** The new superior */
    private String newSuperior;

    /** The new rdn */
    private String newRdn;

    /** The delete old rdn flag */
    private boolean deleteOldRdn;

    /** the entry */
    private Entry entry;

    /** the DN */
    private Dn entryDn;

    /** The controls */
    private Map<String, LdifControl> controls;

    /** The lengthBeforeParsing of the entry at the time of parsing. This includes
     *  the lengthBeforeParsing of the comments present in entry at the time of parsing
     *  so this lengthBeforeParsing may not always match with the lengthBeforeParsing of the entry
     *  data present in memory.
     */
    private int lengthBeforeParsing = 0;

    /** the position of the entry in the file or given input string*/
    private long offset = 0;


    /**
     * Creates a new LdifEntry object.
     */
    public LdifEntry()
    {
        // Default LDIF content
        changeType = ChangeType.None;
        modificationList = new LinkedList<>();
        modifications = new HashMap<>();
        entry = new DefaultEntry( ( Dn ) null );
        entryDn = null;
        controls = null;
    }


    /**
     * Creates a new schema aware LdifEntry object.
     * 
     * @param schemaManager The SchemaManager
     */
    public LdifEntry( SchemaManager schemaManager )
    {
        // Default LDIF content
        changeType = ChangeType.None;
        modificationList = new LinkedList<>();
        modifications = new HashMap<>();
        entry = new DefaultEntry( schemaManager, ( Dn ) null );
        entryDn = null;
        controls = null;
    }


    /**
     * Creates a new LdifEntry object, storing an Entry
     * 
     * @param entry The entry to encapsulate
     */
    public LdifEntry( Entry entry )
    {
        // Default LDIF content
        changeType = ChangeType.None;
        modificationList = new LinkedList<>();
        modifications = new HashMap<>();
        this.entry = entry;
        entryDn = entry.getDn();
        controls = null;
    }


    /**
     * Creates a LdifEntry using a list of strings representing the Ldif element
     * 
     * @param dn The LdifEntry DN
     * @param avas The Ldif to convert to an LdifEntry
     * @throws LdapInvalidAttributeValueException If either the AttributeType or the associated value
     * is incorrect
     * @throws LdapLdifException If we get any other exception
     */
    public LdifEntry( Dn dn, Object... avas ) throws LdapInvalidAttributeValueException, LdapLdifException
    {
        // First, convert the arguments to a full LDIF
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        boolean valueExpected = false;
        String dnStr = null;

        if ( dn == null )
        {
            dnStr = "";
        }
        else
        {
            dnStr = dn.getName();
        }

        if ( LdifUtils.isLDIFSafe( dnStr ) )
        {
            sb.append( "dn: " ).append( dnStr ).append( '\n' );
        }
        else
        {
            sb.append( "dn:: " ).append( Base64.encode( Strings.getBytesUtf8( dnStr ) ) ).append( '\n' );
        }

        for ( Object ava : avas )
        {
            if ( !valueExpected )
            {
                if ( !( ava instanceof String ) )
                {
                    throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n.err(
                        I18n.ERR_13233_ATTRIBUTE_ID_MUST_BE_A_STRING, pos + 1 ) );
                }

                String attribute = ( String ) ava;
                sb.append( attribute );

                if ( attribute.indexOf( ':' ) != -1 )
                {
                    sb.append( '\n' );
                }
                else
                {
                    valueExpected = true;
                }
            }
            else
            {
                if ( ava instanceof String )
                {
                    sb.append( ": " ).append( ( String ) ava ).append( '\n' );
                }
                else if ( ava instanceof byte[] )
                {
                    sb.append( ":: " );
                    sb.append( new String( Base64.encode( ( byte[] ) ava ) ) );
                    sb.append( '\n' );
                }
                else
                {
                    throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n.err(
                        I18n.ERR_13234_ATTRIBUTE_VAL_STRING_OR_BYTE, pos + 1 ) );
                }

                valueExpected = false;
            }
        }

        if ( valueExpected )
        {
            throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, I18n
                .err( I18n.ERR_13250_VALUE_MISSING_AT_THE_END ) );
        }

        // Now, parse the Ldif and convert it to a LdifEntry
        LdifReader reader = new LdifReader();
        List<LdifEntry> ldifEntries = reader.parseLdif( sb.toString() );

        try
        {
            reader.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        if ( ( ldifEntries != null ) && ( ldifEntries.size() == 1 ) )
        {
            LdifEntry ldifEntry = ldifEntries.get( 0 );

            changeType = ldifEntry.getChangeType();
            controls = ldifEntry.getControls();
            entryDn = ldifEntry.getDn();

            switch ( ldifEntry.getChangeType() )
            {
                case Add:
                    // Fallback
                case None:
                    entry = ldifEntry.getEntry();
                    break;

                case Delete:
                    break;

                case ModDn:
                case ModRdn:
                    newRdn = ldifEntry.getNewRdn();
                    newSuperior = ldifEntry.getNewSuperior();
                    deleteOldRdn = ldifEntry.isDeleteOldRdn();
                    break;

                case Modify:
                    modificationList = ldifEntry.getModifications();
                    modifications = new HashMap<>();

                    for ( Modification modification : modificationList )
                    {
                        modifications.put( modification.getAttribute().getId(), modification );
                    }

                    break;

                default:
                    throw new IllegalArgumentException( I18n.err( I18n.ERR_13431_UNEXPECTED_CHANGETYPE, changeType ) );
            }
        }
    }


    /**
     * Creates a LdifEntry using a list of strings representing the Ldif element
     * 
     * @param dn The LdifEntry DN
     * @param strings The Ldif attributes and values to convert to an LdifEntry
     * @throws LdapInvalidDnException If the Dn is invalid
     * @throws LdapInvalidAttributeValueException If either the AttributeType or the associated value
     * is incorrect
     * @throws LdapLdifException If we get any other exception
     */
    public LdifEntry( String dn, Object... strings )
        throws LdapInvalidAttributeValueException, LdapLdifException, LdapInvalidDnException
    {
        this( new Dn( dn ), strings );
    }


    /**
     * Set the Distinguished Name
     * 
     * @param dn The Distinguished Name
     */
    public void setDn( Dn dn )
    {
        entryDn = dn;
        entry.setDn( dn );
    }


    /**
     * Set the Distinguished Name
     * 
     * @param dn The Distinguished Name
     * @throws LdapInvalidDnException If the Dn is invalid
     */
    public void setDn( String dn ) throws LdapInvalidDnException
    {
        entryDn = new Dn( dn );
        entry.setDn( entryDn );
    }


    /**
     * Set the modification type
     * 
     * @param changeType The change type
     * 
     */
    public void setChangeType( ChangeType changeType )
    {
        this.changeType = changeType;
    }


    /**
     * Set the change type
     * 
     * @param changeType The change type
     */
    public void setChangeType( String changeType )
    {
        if ( "add".equals( changeType ) )
        {
            this.changeType = ChangeType.Add;
        }
        else if ( "modify".equals( changeType ) )
        {
            this.changeType = ChangeType.Modify;
        }
        else if ( "moddn".equals( changeType ) )
        {
            this.changeType = ChangeType.ModDn;
        }
        else if ( "modrdn".equals( changeType ) )
        {
            this.changeType = ChangeType.ModRdn;
        }
        else if ( "delete".equals( changeType ) )
        {
            this.changeType = ChangeType.Delete;
        }
    }


    /**
     * Add a modification item (used by modify operations)
     * 
     * @param modification The modification to be added
     */
    public void addModification( Modification modification )
    {
        if ( changeType == ChangeType.Modify )
        {
            modificationList.add( modification );
            modifications.put( modification.getAttribute().getId(), modification );
        }
    }


    /**
     * Add a modification item (used by modify operations)
     * 
     * @param modOp The operation. One of : 
     * <ul>
     * <li>ModificationOperation.ADD_ATTRIBUTE</li>
     * <li>ModificationOperation.REMOVE_ATTRIBUTE</li>
     * <li>ModificationOperation.REPLACE_ATTRIBUTE</li>
     * </ul>
     * 
     * @param attr The attribute to be added
     */
    public void addModification( ModificationOperation modOp, Attribute attr )
    {
        if ( changeType == ChangeType.Modify )
        {
            Modification item = new DefaultModification( modOp, attr );
            modificationList.add( item );
            modifications.put( attr.getId(), item );
        }
    }


    /**
     * Add a modification with no value
     * 
     * @param modOp The modification operation value. One of : 
     * <ul>
     * <li>ModificationOperation.ADD_ATTRIBUTE</li>
     * <li>ModificationOperation.REMOVE_ATTRIBUTE</li>
     * <li>ModificationOperation.REPLACE_ATTRIBUTE</li>
     * </ul>
     * 
     * @param id The attribute's ID
     */
    public void addModification( ModificationOperation modOp, String id )
    {
        if ( changeType == ChangeType.Modify )
        {
            Attribute attr = new DefaultAttribute( id );

            Modification item = new DefaultModification( modOp, attr );
            modificationList.add( item );
            modifications.put( id, item );
        }
    }


    /**
     * Add a modification
     * 
     * @param modOp The modification operation value. One of : 
     * <ul>
     * <li>ModificationOperation.ADD_ATTRIBUTE</li>
     * <li>ModificationOperation.REMOVE_ATTRIBUTE</li>
     * <li>ModificationOperation.REPLACE_ATTRIBUTE</li>
     * </ul>
     * 
     * @param id The attribute's ID
     * @param value The attribute's value
     */
    public void addModification( ModificationOperation modOp, String id, Object value )
    {
        if ( changeType == ChangeType.Modify )
        {
            Attribute attr;

            if ( value == null )
            {
                value = new Value( ( String ) null );
                attr = new DefaultAttribute( id, ( Value ) value );
            }
            else
            {
                attr = ( Attribute ) value;
            }

            Modification item = new DefaultModification( modOp, attr );
            modificationList.add( item );
            modifications.put( id, item );
        }
    }


    /**
     * Add an attribute to the entry
     * 
     * @param attr The attribute to be added
     * @throws org.apache.directory.api.ldap.model.exception.LdapException if something went wrong
     */
    public void addAttribute( Attribute attr ) throws LdapException
    {
        entry.put( attr );
    }


    /**
     * Add an attribute to the entry
     * 
     * @param id The attribute ID
     * 
     * @param values The attribute values
     * @throws LdapException if something went wrong
     */
    public void addAttribute( String id, Object... values ) throws LdapException
    {
        Attribute attribute = entry.get( id );
        Boolean isHR = null;

        if ( attribute != null )
        {
            isHR = attribute.isHumanReadable();
        }

        if ( values != null )
        {
            for ( Object value : values )
            {
                if ( value instanceof String )
                {
                    if ( isHR != null )
                    {
                        if ( isHR )
                        {
                            entry.add( id, ( String ) value );
                        }
                        else
                        {
                            entry.add( id, Strings.getBytesUtf8( ( String ) value ) );
                        }
                    }
                    else
                    {
                        entry.add( id, ( String ) value );
                    }
                }
                else
                {
                    if ( isHR != null )
                    {
                        if ( isHR )
                        {
                            entry.add( id, Strings.utf8ToString( ( byte[] ) value ) );
                        }
                        else
                        {
                            entry.add( id, ( byte[] ) value );
                        }
                    }
                    else
                    {
                        entry.add( id, ( byte[] ) value );
                    }
                }
            }
        }
        else
        {
            entry.add( id, ( Value ) null );
        }
    }


    /**
     * Remove a list of Attributes from the LdifEntry
     *
     * @param ids The Attributes to remove
     */
    public void removeAttribute( String... ids )
    {
        if ( entry.containsAttribute( ids ) )
        {
            entry.removeAttributes( ids );
        }
    }


    /**
     * Add an attribute value to an existing attribute
     * 
     * @param id The attribute ID
     * 
     * @param value The attribute value
     * @throws org.apache.directory.api.ldap.model.exception.LdapException if something went wrong
     */
    public void putAttribute( String id, Object value ) throws LdapException
    {
        if ( value instanceof String )
        {
            entry.add( id, ( String ) value );
        }
        else
        {
            entry.add( id, ( byte[] ) value );
        }
    }


    /**
     * Get the change type
     * 
     * @return The change type. One of : 
     * <ul>
     * <li>ADD</li>
     * <li>MODIFY</li>
     * <li>MODDN</li>
     * <li>MODRDN</li>
     * <li>DELETE</li>
     * <li>NONE</li>
     * </ul>
     */
    public ChangeType getChangeType()
    {
        return changeType;
    }


    /**
     * @return The list of modification items
     */
    public List<Modification> getModifications()
    {
        return modificationList;
    }


    /**
     * Gets the modification items as an array.
     *
     * @return modification items as an array.
     */
    public Modification[] getModificationArray()
    {
        return modificationList.toArray( EMPTY_MODS );
    }


    /**
     * @return The entry Distinguished name
     */
    public Dn getDn()
    {
        return entryDn;
    }


    /**
     * @return The number of entry modifications
     */
    public int size()
    {
        return modificationList.size();
    }


    /**
     * Returns a attribute given it's id
     * 
     * @param attributeId The attribute Id
     * @return The attribute if it exists
     */
    public Attribute get( String attributeId )
    {
        if ( "dn".equalsIgnoreCase( attributeId ) )
        {
            return new DefaultAttribute( "dn", entry.getDn().getName() );
        }

        return entry.get( attributeId );
    }


    /**
     * Get the entry's entry
     * 
     * @return the stored Entry
     */
    public Entry getEntry()
    {
        if ( isEntry() )
        {
            return entry;
        }
        else
        {
            return null;
        }
    }


    /**
     * @return True, if the old Rdn should be deleted.
     */
    public boolean isDeleteOldRdn()
    {
        return deleteOldRdn;
    }


    /**
     * Set the deleteOldRdn flag
     * 
     * @param deleteOldRdn True if the old Rdn should be deleted
     */
    public void setDeleteOldRdn( boolean deleteOldRdn )
    {
        this.deleteOldRdn = deleteOldRdn;
    }


    /**
     * @return The new Rdn
     */
    public String getNewRdn()
    {
        return newRdn;
    }


    /**
     * Set the new Rdn
     * 
     * @param newRdn The new Rdn
     */
    public void setNewRdn( String newRdn )
    {
        this.newRdn = newRdn;
    }


    /**
     * @return The new superior
     */
    public String getNewSuperior()
    {
        return newSuperior;
    }


    /**
     * Set the new superior
     * 
     * @param newSuperior The new Superior
     */
    public void setNewSuperior( String newSuperior )
    {
        this.newSuperior = newSuperior;
    }


    /**
     * @return True if this is a content ldif
     */
    public boolean isLdifContent()
    {
        return changeType == ChangeType.None;
    }


    /**
     * @return True if there is this is a change ldif
     */
    public boolean isLdifChange()
    {
        return changeType != ChangeType.None;
    }


    /**
     * @return True if the entry is an ADD entry
     */
    public boolean isChangeAdd()
    {
        return changeType == ChangeType.Add;
    }


    /**
     * @return True if the entry is a DELETE entry
     */
    public boolean isChangeDelete()
    {
        return changeType == ChangeType.Delete;
    }


    /**
     * @return True if the entry is a MODDN entry
     */
    public boolean isChangeModDn()
    {
        return changeType == ChangeType.ModDn;
    }


    /**
     * @return True if the entry is a MODRDN entry
     */
    public boolean isChangeModRdn()
    {
        return changeType == ChangeType.ModRdn;
    }


    /**
     * @return True if the entry is a MODIFY entry
     */
    public boolean isChangeModify()
    {
        return changeType == ChangeType.Modify;
    }


    /**
     * Tells if the current entry is a added one
     *
     * @return <code>true</code> if the entry is added
     */
    public boolean isEntry()
    {
        return ( changeType == ChangeType.None ) || ( changeType == ChangeType.Add );
    }


    /**
     * @return true if the entry has some controls
     */
    public boolean hasControls()
    {
        return controls != null;
    }


    /**
     * @return The set of controls for this entry
     */
    public Map<String, LdifControl> getControls()
    {
        return controls;
    }


    /**
     * @param oid The control's OID
     * @return The associated control, if any
     */
    public LdifControl getControl( String oid )
    {
        if ( controls != null )
        {
            return controls.get( oid );
        }

        return null;
    }


    /**
     * Add a control to the entry
     * 
     * @param controls The added controls
     */
    public void addControl( Control... controls )
    {
        if ( controls == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13432_NULL_ADDED_CONTROL ) );
        }

        for ( Control control : controls )
        {
            if ( changeType == ChangeType.None )
            {
                changeType = ChangeType.Add;
            }

            if ( this.controls == null )
            {
                this.controls = new ConcurrentHashMap<>();
            }

            if ( control instanceof LdifControl )
            {
                this.controls.put( control.getOid(), ( LdifControl ) control );
            }
            else
            {
                LdifControl ldifControl = new LdifControl( control.getOid() );
                ldifControl.setCritical( control.isCritical() );
                this.controls.put( control.getOid(), new LdifControl( control.getOid() ) );
            }
        }
    }


    /**
     * Clone method
     * @return a clone of the current instance
     * @exception CloneNotSupportedException If there is some problem while cloning the instance
     */
    @Override
    public LdifEntry clone() throws CloneNotSupportedException
    {
        LdifEntry clone = ( LdifEntry ) super.clone();

        if ( modificationList != null )
        {
            for ( Modification modif : modificationList )
            {
                Modification modifClone = new DefaultModification( modif.getOperation(),
                    modif.getAttribute().clone() );
                clone.modificationList.add( modifClone );
            }
        }

        if ( modifications != null )
        {
            for ( Map.Entry<String, Modification> modificationEntry : modifications.entrySet() )
            {
                Modification modif = modificationEntry.getValue();
                Modification modifClone = new DefaultModification( modif.getOperation(),
                    modif.getAttribute().clone() );
                clone.modifications.put( modificationEntry.getKey(), modifClone );
            }

        }

        if ( entry != null )
        {
            clone.entry = entry.clone();
        }

        return clone;
    }


    /** 
     *  Returns the lengthBeforeParsing of the entry at the time of parsing. This includes
     *  the lengthBeforeParsing of the comments present in entry at the time of parsing
     *  so this lengthBeforeParsing may not always match with the lengthBeforeParsing of the entry
     *  data present in memory.
     *  
     *  @return The entry length, comments included 
     */
    public int getLengthBeforeParsing()
    {
        return lengthBeforeParsing;
    }


    /**
     * @param length the lengthBeforeParsing to set
     */
    /*No qualifier*/ void setLengthBeforeParsing( int length )
    {
        this.lengthBeforeParsing = length;
    }


    /**
     * @return the offset
     */
    public long getOffset()
    {
        return offset;
    }


    /**
     * @param offset the offset to set
     */
    /*No qualifier*/ void setOffset( long offset )
    {
        this.offset = offset;
    }


    /**
     * Returns an enumeration containing the zero or more attributes in the
     * collection. The behavior of the enumeration is not specified if the
     * attribute collection is changed.
     *
     * @return an enumeration of all contained attributes
     */
    @Override
    public Iterator<Attribute> iterator()
    {
        return entry.iterator();
    }


    /**
     * @return a String representing the Entry, as a LDIF 
     */
    @Override
    public String toString()
    {
        try
        {
            return LdifUtils.convertToLdif( this );
        }
        catch ( LdapException ne )
        {
            return "";
        }
    }


    /**
     * @see Object#hashCode()
     * 
     * @return the instance's hash code
     */
    @Override
    public int hashCode()
    {
        int result = 37;

        if ( entry != null && entry.getDn() != null )
        {
            result = result * 17 + entry.getDn().hashCode();
        }

        if ( changeType != null )
        {
            result = result * 17 + changeType.hashCode();

            // Check each different cases
            switch ( changeType )
            {
                case None:
                    // Fall through
                case Add:
                    // Checks the attributes
                    if ( entry != null )
                    {
                        result = result * 17 + entry.hashCode();
                    }

                    break;

                case Delete:
                    // Nothing to compute
                    break;

                case Modify:
                    if ( modificationList != null )
                    {
                        result = result * 17 + modificationList.hashCode();

                        for ( Modification modification : modificationList )
                        {
                            result = result * 17 + modification.hashCode();
                        }
                    }

                    break;

                case ModDn:
                case ModRdn:
                    result = result * 17;

                    if ( deleteOldRdn )
                    {
                        result++;
                    }
                    else
                    {
                        result--;
                    }

                    if ( newRdn != null )
                    {
                        result = result * 17 + newRdn.hashCode();
                    }

                    if ( newSuperior != null )
                    {
                        result = result * 17 + newSuperior.hashCode();
                    }

                    break;

                default:
                    // do nothing
                    break;
            }
        }

        if ( controls != null )
        {
            for ( String control : controls.keySet() )
            {
                result = result * 17 + control.hashCode();
            }
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        // Basic equals checks
        if ( this == o )
        {
            return true;
        }

        if ( o == null )
        {
            return false;
        }

        if ( !( o instanceof LdifEntry ) )
        {
            return false;
        }

        LdifEntry otherEntry = ( LdifEntry ) o;

        // Check the Dn
        Dn thisDn = entryDn;
        Dn dnEntry = otherEntry.getDn();

        if ( !thisDn.equals( dnEntry ) )
        {
            return false;
        }

        // Check the changeType
        if ( changeType != otherEntry.changeType )
        {
            return false;
        }

        // Check each different cases
        switch ( changeType )
        {
            case None:
                // Fall through
            case Add:
                // Checks the attributes
                if ( entry.size() != otherEntry.entry.size() )
                {
                    return false;
                }

                if ( !entry.equals( otherEntry.entry ) )
                {
                    return false;
                }

                break;

            case Delete:
                // Nothing to do, if the DNs are equals
                break;

            case Modify:
                // Check the modificationItems list

                // First, deal with special cases
                if ( modificationList == null )
                {
                    if ( otherEntry.modificationList != null )
                    {
                        return false;
                    }
                    else
                    {
                        break;
                    }
                }

                if ( otherEntry.modificationList == null )
                {
                    return false;
                }

                if ( modificationList.size() != otherEntry.modificationList.size() )
                {
                    return false;
                }

                // Now, compares the contents
                int i = 0;

                for ( Modification modification : modificationList )
                {
                    if ( !modification.equals( otherEntry.modificationList.get( i ) ) )
                    {
                        return false;
                    }

                    i++;
                }

                break;

            case ModDn:
            case ModRdn:
                // Check the deleteOldRdn flag
                if ( deleteOldRdn != otherEntry.deleteOldRdn )
                {
                    return false;
                }

                // Check the newRdn value
                try
                {
                    Rdn thisNewRdn = new Rdn( newRdn );
                    Rdn entryNewRdn = new Rdn( otherEntry.newRdn );

                    if ( !thisNewRdn.equals( entryNewRdn ) )
                    {
                        return false;
                    }
                }
                catch ( LdapInvalidDnException ine )
                {
                    return false;
                }

                // Check the newSuperior value
                try
                {
                    Dn thisNewSuperior = new Dn( newSuperior );
                    Dn entryNewSuperior = new Dn( otherEntry.newSuperior );

                    if ( !thisNewSuperior.equals( entryNewSuperior ) )
                    {
                        return false;
                    }
                }
                catch ( LdapInvalidDnException ine )
                {
                    return false;
                }

                break;

            default:
                // do nothing
                break;
        }

        if ( controls != null )
        {
            Map<String, LdifControl> otherControls = otherEntry.controls;

            if ( otherControls == null )
            {
                return false;
            }

            if ( controls.size() != otherControls.size() )
            {
                return false;
            }

            for ( Map.Entry<String, LdifControl> controlEntry : controls.entrySet() )
            {
                String controlOid = controlEntry.getKey();

                if ( !otherControls.containsKey( controlOid ) )
                {
                    return false;
                }

                Control thisControl = controlEntry.getValue();
                Control otherControl = otherControls.get( controlOid );

                if ( thisControl == null )
                {
                    if ( otherControl != null )
                    {
                        return false;
                    }
                }
                else
                {
                    if ( !thisControl.equals( otherControl ) )
                    {
                        return false;
                    }
                }
            }

            return true;
        }
        else
        {
            return otherEntry.controls == null;
        }
    }


    /**
     * @see Externalizable#readExternal(ObjectInput)
     * 
     * @param in The stream from which the LdifEntry is read
     * @throws IOException If the stream can't be read
     * @throws ClassNotFoundException If the LdifEntry can't be created 
     */
    @Override
    public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
    {
        // Read the changeType
        int type = in.readInt();
        changeType = ChangeType.getChangeType( type );

        // Read the modification
        switch ( changeType )
        {
            case Add:
            case None:
                // Read the entry
                entry.readExternal( in );
                entryDn = entry.getDn();

                break;

            case Delete:
                // Read the Dn
                entryDn = new Dn();
                entryDn.readExternal( in );

                break;

            case ModDn:
                // Fallback
            case ModRdn:
                // Read the Dn
                entryDn = new Dn();
                entryDn.readExternal( in );

                deleteOldRdn = in.readBoolean();

                if ( in.readBoolean() )
                {
                    newRdn = in.readUTF();
                }

                if ( in.readBoolean() )
                {
                    newSuperior = in.readUTF();
                }

                break;

            case Modify:
                // Read the Dn
                entryDn = new Dn();
                entryDn.readExternal( in );

                // Read the modifications
                int nbModifs = in.readInt();

                for ( int i = 0; i < nbModifs; i++ )
                {
                    Modification modification = new DefaultModification();
                    modification.readExternal( in );

                    addModification( modification );
                }

                break;

            default:
                throw new IllegalArgumentException( I18n.err( I18n.ERR_13431_UNEXPECTED_CHANGETYPE, changeType ) );
        }

        int nbControls = in.readInt();

        // We have at least a control
        if ( nbControls > 0 )
        {
            controls = new ConcurrentHashMap<>( nbControls );

            for ( int i = 0; i < nbControls; i++ )
            {
                LdifControl control = new LdifControl();

                control.readExternal( in );

                controls.put( control.getOid(), control );
            }
        }
    }


    /**
     * @see Externalizable#readExternal(ObjectInput)
     * @param out The stream in which the ChangeLogEvent will be serialized.
     * @throws IOException If the serialization fail
     */
    @Override
    public void writeExternal( ObjectOutput out ) throws IOException
    {
        // Write the changeType
        out.writeInt( changeType.getChangeType() );

        // Write the data
        switch ( changeType )
        {
            case Add:
            case None:
                entry.writeExternal( out );
                break;

            // Fallback
            case Delete:
                // we write the Dn
                entryDn.writeExternal( out );
                break;

            case ModDn:
                // Fallback
            case ModRdn:
                // Write the Dn
                entryDn.writeExternal( out );

                out.writeBoolean( deleteOldRdn );

                if ( newRdn == null )
                {
                    out.writeBoolean( false );
                }
                else
                {
                    out.writeBoolean( true );
                    out.writeUTF( newRdn );
                }

                if ( newSuperior != null )
                {
                    out.writeBoolean( true );
                    out.writeUTF( newSuperior );
                }
                else
                {
                    out.writeBoolean( false );
                }
                break;

            case Modify:
                // Write the Dn
                entryDn.writeExternal( out );

                // Write the modifications
                out.writeInt( modificationList.size() );

                for ( Modification modification : modificationList )
                {
                    modification.writeExternal( out );
                }

                break;

            default:
                throw new IllegalArgumentException( I18n.err( I18n.ERR_13431_UNEXPECTED_CHANGETYPE, changeType ) );
        }

        // The controls
        if ( controls != null )
        {
            // Write the control
            out.writeInt( controls.size() );

            for ( LdifControl control : controls.values() )
            {
                control.writeExternal( out );
            }
        }
        else
        {
            // No control, write -1
            out.writeInt( -1 );
        }

        // and flush the result
        out.flush();
    }
}
