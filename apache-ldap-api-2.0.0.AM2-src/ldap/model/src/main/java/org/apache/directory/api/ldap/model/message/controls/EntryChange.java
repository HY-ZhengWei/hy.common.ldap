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


import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.name.Dn;


/**
 * A response control that may be returned by Persistent Search entry responses.
 * It contains addition change information to describe the exact change that
 * occurred to an entry. The exact details of this control are covered in section
 * 5 of this (yes) expired draft: <a
 * href="http://www3.ietf.org/proceedings/01aug/I-D/draft-ietf-ldapext-psearch-03.txt">
 * Persistent Search Draft v03</a> which is printed out below for convenience:
 *
 * <pre>
 *    5.  Entry Change Notification Control
 *
 *    This control provides additional information about the change the caused
 *    a particular entry to be returned as the result of a persistent search.
 *    The controlType is &quot;2.16.840.1.113730.3.4.7&quot;.  If the client set the
 *    returnECs boolean to TRUE in the PersistentSearch control, servers MUST
 *    include an EntryChangeNotification control in the Controls portion of
 *    each SearchResultEntry that is returned due to an entry being added,
 *    deleted, or modified.
 *
 *               EntryChangeNotification ::= SEQUENCE
 *               {
 *                         changeType ENUMERATED
 *                         {
 *                                 add             (1),
 *                                 delete          (2),
 *                                 modify          (4),
 *                                 modDN           (8)
 *                         },
 *                         previousDN   LDAPDN OPTIONAL,     -- modifyDN ops. only
 *                         changeNumber INTEGER OPTIONAL     -- if supported
 *               }
 *
 *    changeType indicates what LDAP operation caused the entry to be
 *    returned.
 *
 *    previousDN is present only for modifyDN operations and gives the Dn of
 *    the entry before it was renamed and/or moved.  Servers MUST include this
 *    optional field only when returning change notifications as a result of
 *    modifyDN operations.
 *
 *    changeNumber is the change number [CHANGELOG] assigned by a server for
 *    the change.  If a server supports an LDAP Change Log it SHOULD include
 *    this field.
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface EntryChange extends Control
{
    /** No defined change number */ 
    int UNDEFINED_CHANGE_NUMBER = -1;

    /** The EntryChange control */
    String OID = "2.16.840.1.113730.3.4.7";


    /**
     * @return The ChangeType
     */
    ChangeType getChangeType();


    /**
     * Set the ChangeType
     *
     * @param changeType Add, Delete; Modify or ModifyDN
     */
    void setChangeType( ChangeType changeType );


    /**
     * @return The previous DN
     */
    Dn getPreviousDn();


    /**
     * Sets the previous DN
     * 
     * @param previousDn The previous DN
     */
    void setPreviousDn( Dn previousDn );


    /**
     * @return The change number
     */
    long getChangeNumber();


    /**
     * Sets the ChangeNumber
     * 
     * @param changeNumber The ChanegNumber
     */
    void setChangeNumber( long changeNumber );
}
