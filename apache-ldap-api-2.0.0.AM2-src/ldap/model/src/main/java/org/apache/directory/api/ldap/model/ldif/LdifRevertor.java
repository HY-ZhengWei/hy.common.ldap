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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.AttributeUtils;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Ava;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;


/**
 * A helper class which provides methods to reverse a LDIF modification operation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class LdifRevertor
{
    /** Flag used when we want to delete the old Rdn */
    public static final boolean DELETE_OLD_RDN = true;

    /** Flag used when we want to keep the old Rdn */
    public static final boolean KEEP_OLD_RDN = false;


    /**
     * Private constructor.
     */
    private LdifRevertor()
    {
    }


    /**
     * Compute a reverse LDIF of an AddRequest. It's simply a delete request
     * of the added entry
     *
     * @param dn the dn of the added entry
     * @return a reverse LDIF
     */
    public static LdifEntry reverseAdd( Dn dn )
    {
        LdifEntry entry = new LdifEntry();
        entry.setChangeType( ChangeType.Delete );
        entry.setDn( dn );
        return entry;
    }


    /**
     * Compute a reverse LDIF of a DeleteRequest. We have to get the previous
     * entry in order to restore it.
     *
     * @param dn The deleted entry Dn
     * @param deletedEntry The entry which has been deleted
     * @return A reverse LDIF
     * @throws LdapException If something went wrong
     */
    public static LdifEntry reverseDel( Dn dn, Entry deletedEntry ) throws LdapException
    {
        LdifEntry entry = new LdifEntry();

        entry.setDn( dn );
        entry.setChangeType( ChangeType.Add );

        for ( Attribute attribute : deletedEntry )
        {
            entry.addAttribute( attribute );
        }

        return entry;
    }


    /**
     *
     * Compute the reversed LDIF for a modify request. We will deal with the
     * three kind of modifications :
     * <ul>
     * <li>add</li>
     * <li>remove</li>
     * <li>replace</li>
     * </ul>
     * 
     * As the modifications should be issued in a reversed order ( ie, for
     * the initials modifications {A, B, C}, the reversed modifications will
     * be ordered like {C, B, A}), we will change the modifications order.
     *
     * @param dn the dn of the modified entry
     * @param forwardModifications the modification items for the forward change
     * @param modifiedEntry The modified entry. Necessary for the destructive modifications
     * @return A reversed LDIF
     * @throws LdapException If something went wrong
     */
    public static LdifEntry reverseModify( Dn dn, List<Modification> forwardModifications, Entry modifiedEntry )
        throws LdapException
    {
        // First, protect the original entry by cloning it : we will modify it
        Entry clonedEntry = modifiedEntry.clone();

        LdifEntry entry = new LdifEntry();
        entry.setChangeType( ChangeType.Modify );

        entry.setDn( dn );

        // As the reversed modifications should be pushed in reversed order,
        // we create a list to temporarily store the modifications.
        List<Modification> reverseModifications = new ArrayList<>();

        // Loop through all the modifications. For each modification, we will
        // have to apply it to the modified entry in order to be able to generate
        // the reversed modification
        for ( Modification modification : forwardModifications )
        {
            switch ( modification.getOperation() )
            {
                case ADD_ATTRIBUTE:
                    Attribute mod = modification.getAttribute();

                    Attribute previous = clonedEntry.get( mod.getId() );

                    if ( mod.equals( previous ) )
                    {
                        continue;
                    }

                    Modification reverseModification = new DefaultModification( ModificationOperation.REMOVE_ATTRIBUTE,
                        mod );
                    reverseModifications.add( 0, reverseModification );
                    break;

                case REMOVE_ATTRIBUTE:
                    mod = modification.getAttribute();

                    previous = clonedEntry.get( mod.getId() );

                    if ( previous == null )
                    {
                        // Nothing to do if the previous attribute didn't exist
                        continue;
                    }

                    if ( mod.get() == null )
                    {
                        reverseModification = new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, previous );
                        reverseModifications.add( 0, reverseModification );
                        break;
                    }

                    reverseModification = new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, mod );
                    reverseModifications.add( 0, reverseModification );
                    break;

                case REPLACE_ATTRIBUTE:
                    mod = modification.getAttribute();

                    previous = clonedEntry.get( mod.getId() );

                    /*
                     * The server accepts without complaint replace
                     * modifications to non-existing attributes in the
                     * entry.  When this occurs nothing really happens
                     * but this method freaks out.  To prevent that we
                     * make such no-op modifications produce the same
                     * modification for the reverse direction which should
                     * do nothing as well.
                     */
                    if ( ( mod.get() == null ) && ( previous == null ) )
                    {
                        reverseModification = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE,
                            new DefaultAttribute( mod.getId() ) );
                        reverseModifications.add( 0, reverseModification );
                        continue;
                    }

                    if ( mod.get() == null )
                    {
                        reverseModification = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE,
                            previous );
                        reverseModifications.add( 0, reverseModification );
                        continue;
                    }

                    if ( previous == null )
                    {
                        Attribute emptyAttribute = new DefaultAttribute( mod.getId() );
                        reverseModification = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE,
                            emptyAttribute );
                        reverseModifications.add( 0, reverseModification );
                        continue;
                    }

                    reverseModification = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, previous );
                    reverseModifications.add( 0, reverseModification );
                    break;

                default:
                    // Do nothing
                    break;

            }

            AttributeUtils.applyModification( clonedEntry, modification );

        }

        // Special case if we don't have any reverse modifications
        if ( reverseModifications.isEmpty() )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13465_CANT_DEDUCE_REVERSE_FOR_MOD, forwardModifications ) );
        }

        // Now, push the reversed list into the entry
        for ( Modification modification : reverseModifications )
        {
            entry.addModification( modification );
        }

        // Return the reverted entry
        return entry;
    }


    /**
     * Compute a reverse LDIF for a forward change which if in LDIF format
     * would represent a Move operation. Hence there is no newRdn in the
     * picture here.
     *
     * @param newSuperiorDn the new parent dn to be (must not be null)
     * @param modifiedDn the dn of the entry being moved (must not be null)
     * @return a reverse LDIF
     * @throws LdapException if something went wrong
     */
    public static LdifEntry reverseMove( Dn newSuperiorDn, Dn modifiedDn ) throws LdapException
    {
        LdifEntry entry = new LdifEntry();
        Dn currentParent;
        Rdn currentRdn;
        Dn newDn;

        if ( newSuperiorDn == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13466_NEW_SUPERIOR_DN_NULL ) );
        }

        if ( modifiedDn == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13467_NULL_MODIFIED_DN ) );
        }

        if ( modifiedDn.size() == 0 )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13468_DONT_MOVE_ROOTDSE ) );
        }

        currentParent = modifiedDn;
        currentRdn = currentParent.getRdn();
        currentParent = currentParent.getParent();

        newDn = newSuperiorDn;
        newDn = newDn.add( modifiedDn.getRdn() );

        entry.setChangeType( ChangeType.ModDn );
        entry.setDn( newDn );
        entry.setNewRdn( currentRdn.getName() );
        entry.setNewSuperior( currentParent.getName() );
        entry.setDeleteOldRdn( false );
        return entry;
    }


    /**
     * A small helper class to compute the simple revert.
     * 
     * @param entry The entry to revert
     * @param newDn The new Dn
     * @param newSuperior The new superior, if it has changed (null otherwise)
     * @param oldRdn The old Rdn
     * @param newRdn The new RDN if the RDN has changed
     * @return The reverted entry
     * @throws LdapInvalidDnException If the Dn is invalid
     */
    private static LdifEntry revertEntry( Entry entry, Dn newDn, Dn newSuperior, Rdn oldRdn, Rdn newRdn )
        throws LdapInvalidDnException
    {
        LdifEntry reverted = new LdifEntry();

        // We have a composite old Rdn, something like A=a+B=b
        // It does not matter if the RDNs overlap
        reverted.setChangeType( ChangeType.ModRdn );

        if ( newSuperior != null )
        {
            Dn restoredDn = newSuperior.add( newRdn );
            reverted.setDn( restoredDn );
        }
        else
        {
            reverted.setDn( newDn );
        }

        reverted.setNewRdn( oldRdn.getName() );

        // Is the newRdn's value present in the entry ?
        // ( case 3, 4 and 5)
        // If keepOldRdn = true, we cover case 4 and 5
        boolean keepOldRdn = entry.contains( newRdn.getNormType(), newRdn.getValue() );

        reverted.setDeleteOldRdn( !keepOldRdn );

        if ( newSuperior != null )
        {
            Dn oldSuperior = entry.getDn();

            oldSuperior = oldSuperior.getParent();
            reverted.setNewSuperior( oldSuperior.getName() );
        }

        return reverted;
    }


    /**
     * A helper method to generate the modified attribute after a rename.
     * 
     * @param parentDn The parent Dn
     * @param entry The entry to revert
     * @param oldRdn The old Rdn
     * @param newRdn The new Rdn
     * @return The modified entry
     */
    private static LdifEntry generateModify( Dn parentDn, Entry entry, Rdn oldRdn, Rdn newRdn )
    {
        LdifEntry restored = new LdifEntry();
        restored.setChangeType( ChangeType.Modify );

        // We have to use the parent Dn, the entry has already
        // been renamed
        restored.setDn( parentDn );

        for ( Ava ava : newRdn )
        {
            // No need to add something which has already been added
            // in the previous modification
            if ( !entry.contains( ava.getNormType(), ava.getValue().getValue() )
                && !( ava.getNormType().equals( oldRdn.getNormType() ) && ava.getValue().getValue().equals(
                    oldRdn.getValue() ) ) )
            {
                // Create the modification, which is an Remove
                Modification modification = new DefaultModification( ModificationOperation.REMOVE_ATTRIBUTE,
                    new DefaultAttribute( ava.getType(), ava.getValue().getValue() ) );

                restored.addModification( modification );
            }
        }

        return restored;
    }


    /**
     * A helper method which generates a reverted entry for a MODDN operation
     * 
     * @param newSuperior The new superior, if it has changed (null otherwise)
     * @param newRdn The new RDN if the RDN has changed
     * @param newDn The new Dn
     * @param oldRdn The old Rdn
     * @param deleteOldRdn If the old Rdn attributes must be deleted or not
     * @return The reverted entry
     * @throws LdapInvalidDnException If the DN is invalid
     */
    private static LdifEntry generateReverted( Dn newSuperior, Rdn newRdn, Dn newDn, Rdn oldRdn, boolean deleteOldRdn )
        throws LdapInvalidDnException
    {
        LdifEntry reverted = new LdifEntry();
        reverted.setChangeType( ChangeType.ModRdn );

        if ( newSuperior != null )
        {
            Dn restoredDn = newSuperior.add( newRdn );
            reverted.setDn( restoredDn );
        }
        else
        {
            reverted.setDn( newDn );
        }

        reverted.setNewRdn( oldRdn.getName() );

        if ( newSuperior != null )
        {
            Dn oldSuperior = newDn;

            oldSuperior = oldSuperior.getParent();
            reverted.setNewSuperior( oldSuperior.getName() );
        }

        // Delete the newRDN values
        reverted.setDeleteOldRdn( deleteOldRdn );

        return reverted;
    }


    /**
     * Revert a Dn to it's previous version by removing the first Rdn and adding the given Rdn.
     * It's a rename operation. The biggest issue is that we have many corner cases, depending
     * on the RDNs we are manipulating, and on the content of the initial entry.
     * 
     * @param entry The initial Entry
     * @param newRdn The new Rdn
     * @param deleteOldRdn A flag which tells to delete the old Rdn AVAs
     * @return A list of LDIF reverted entries
     * @throws LdapInvalidDnException If the name reverting failed
     */
    public static List<LdifEntry> reverseRename( Entry entry, Rdn newRdn, boolean deleteOldRdn )
        throws LdapInvalidDnException
    {
        return reverseMoveAndRename( entry, null, newRdn, deleteOldRdn );
    }


    /**
     * Revert a Dn to it's previous version by removing the first Rdn and adding the given Rdn.
     * It's a rename operation. The biggest issue is that we have many corner cases, depending
     * on the RDNs we are manipulating, and on the content of the initial entry.
     * 
     * @param entry The initial Entry
     * @param newSuperior The new superior Dn (can be null if it's just a rename)
     * @param newRdn The new Rdn
     * @param deleteOldRdn A flag which tells to delete the old Rdn AVAs
     * @return A list of LDIF reverted entries
     * @throws LdapInvalidDnException If the name reverting failed
     */
    public static List<LdifEntry> reverseMoveAndRename( Entry entry, Dn newSuperior, Rdn newRdn, boolean deleteOldRdn )
        throws LdapInvalidDnException
    {
        Dn parentDn = entry.getDn();
        Dn newDn;

        if ( newRdn == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13469_NULL_READ_DN ) );
        }

        if ( parentDn == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13467_NULL_MODIFIED_DN ) );
        }

        if ( parentDn.size() == 0 )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_13470_DONT_RENAME_ROOTDSE ) );
        }

        parentDn = entry.getDn();
        Rdn oldRdn = parentDn.getRdn();

        newDn = parentDn;
        newDn = newDn.getParent();
        newDn = newDn.add( newRdn );

        List<LdifEntry> entries = new ArrayList<>( 1 );
        LdifEntry reverted;

        // Start with the cases here
        if ( newRdn.size() == 1 )
        {
            // We have a simple new Rdn, something like A=a
            reverted = revertEntry( entry, newDn, newSuperior, oldRdn, newRdn );

            entries.add( reverted );
        }
        else
        {
            // We have a composite new Rdn, something like A=a+B=b
            if ( oldRdn.size() == 1 )
            {
                // The old Rdn is simple
                boolean existInEntry = false;

                // Does it overlap ?
                // Is the new Rdn AVAs contained into the entry?
                for ( Ava atav : newRdn )
                {
                    if ( !atav.equals( oldRdn.getAva() )
                        && ( entry.contains( atav.getNormType(), atav.getValue().getValue() ) ) )
                    {
                        existInEntry = true;
                    }
                }

                // The new Rdn includes the old one
                if ( existInEntry )
                {
                    // Some of the new Rdn AVAs existed in the entry
                    // We have to restore them, but we also have to remove
                    // the new values
                    reverted = generateReverted( newSuperior, newRdn, newDn, oldRdn, KEEP_OLD_RDN );

                    entries.add( reverted );

                    // Now, restore the initial values
                    LdifEntry restored = generateModify( parentDn, entry, oldRdn, newRdn );

                    entries.add( restored );
                }
                else
                {
                    // This is the simplest case, we don't have to restore
                    // some existing values (case 8.1 and 9.1)
                    reverted = generateReverted( newSuperior, newRdn, newDn, oldRdn, DELETE_OLD_RDN );

                    entries.add( reverted );
                }
            }
            else
            {
                // We have a composite new Rdn, something like A=a+B=b
                // Does the Rdn overlap ?
                boolean overlapping = false;
                boolean existInEntry = false;

                Set<Ava> oldAtavs = new HashSet<>();

                // We first build a set with all the oldRDN ATAVs
                for ( Ava atav : oldRdn )
                {
                    oldAtavs.add( atav );
                }

                // Now we loop on the newRDN ATAVs to evaluate if the Rdns are overlaping
                // and if the newRdn ATAVs are present in the entry
                for ( Ava atav : newRdn )
                {
                    if ( oldAtavs.contains( atav ) )
                    {
                        overlapping = true;
                    }
                    else if ( entry.contains( atav.getNormType(), atav.getValue().getValue() ) )
                    {
                        existInEntry = true;
                    }
                }

                if ( overlapping )
                {
                    // They overlap
                    if ( existInEntry )
                    {
                        // In this case, we have to reestablish the removed ATAVs
                        // (Cases 12.2 and 13.2)
                        reverted = generateReverted( newSuperior, newRdn, newDn, oldRdn, KEEP_OLD_RDN );

                        entries.add( reverted );
                    }
                    else
                    {
                        // We can simply remove all the new Rdn atavs, as the
                        // overlapping values will be re-created.
                        // (Cases 12.1 and 13.1)
                        reverted = generateReverted( newSuperior, newRdn, newDn, oldRdn, DELETE_OLD_RDN );

                        entries.add( reverted );
                    }
                }
                else
                {
                    // No overlapping
                    if ( existInEntry )
                    {
                        // In this case, we have to reestablish the removed ATAVs
                        // (Cases 10.2 and 11.2)
                        reverted = generateReverted( newSuperior, newRdn, newDn, oldRdn, KEEP_OLD_RDN );

                        entries.add( reverted );

                        LdifEntry restored = generateModify( parentDn, entry, oldRdn, newRdn );

                        entries.add( restored );
                    }
                    else
                    {
                        // We are safe ! We can delete all the new Rdn ATAVs
                        // (Cases 10.1 and 11.1)
                        reverted = generateReverted( newSuperior, newRdn, newDn, oldRdn, DELETE_OLD_RDN );

                        entries.add( reverted );
                    }
                }
            }
        }

        return entries;
    }
}
