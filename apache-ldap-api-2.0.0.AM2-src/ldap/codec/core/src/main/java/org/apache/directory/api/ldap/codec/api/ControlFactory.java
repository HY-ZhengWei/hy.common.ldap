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
package org.apache.directory.api.ldap.codec.api;


import org.apache.directory.api.ldap.model.message.Control;


/**
 * Implementors of new codec control extensions must implement a factory using
 * this factory interface, Factory implementations for specific controls are
 * then registered with the codec and used by the codec to encode and decode
 * those controls.
 *
 * @param <C> The Control to create
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ControlFactory<C extends Control>
{
    /**
     * @return The OID of the Control this factory creates.
     */
    String getOid();


    /**
     * Creates and returns a decorated version of the Control.
     *
     * @return The {@link CodecControl} decorated version of the Control.
     */
    CodecControl<C> newCodecControl();


    /**
     * Decorates an existing control. Implementors should check to make sure
     * the supplied Control has not already been decorated to prevent needless
     * decorator nesting.
     *
     * @param control The {@link Control} to be decorated.
     * @return The decorator wrapping the Control.
     */
    CodecControl<C> newCodecControl( C control );
}
