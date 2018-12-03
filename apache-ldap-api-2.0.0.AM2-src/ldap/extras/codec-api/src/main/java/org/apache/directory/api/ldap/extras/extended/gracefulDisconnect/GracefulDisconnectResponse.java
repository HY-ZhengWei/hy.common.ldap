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
package org.apache.directory.api.ldap.extras.extended.gracefulDisconnect;


import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.message.Referral;


/**
 * An unsolicited notification, extended response, intended for notifying
 * clients of upcoming disconnection due to intended service windows. Unlike the
 * {@link org.apache.directory.api.ldap.model.message.extended.NoticeOfDisconnect} this response contains additional information about
 * the amount of time the server will be offline and exactly when it intends to
 * shutdown.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface GracefulDisconnectResponse extends ExtendedResponse
{
    /** The OID for the graceful disconnect extended operation response. */
    String EXTENSION_OID = "1.3.6.1.4.1.18060.0.1.5";


    /**
     * Gets the delay before disconnection, in seconds.
     *
     * @return the delay before disconnection
     */
    int getDelay();


    /**
     * Sets the delay before disconnection, in seconds.
     *
     * @param delay the new delay before disconnection
     */
    void setDelay( int delay );


    /**
     * Gets the offline time after disconnection, in minutes.
     *
     * @return the offline time after disconnection
     */
    int getTimeOffline();


    /**
     * Sets the time offline after disconnection, in minutes.
     *
     * @param timeOffline the new time offline after disconnection
     */
    void setTimeOffline( int timeOffline );


    /**
     * Gets the replicated contexts.
     *
     * @return the replicated contexts
     */
    Referral getReplicatedContexts();


    /**
     * Add a new URL of a replicated server
     * 
     * @param replicatedContext The replicated server to add.
     */
    void addReplicatedContexts( String replicatedContext );
}