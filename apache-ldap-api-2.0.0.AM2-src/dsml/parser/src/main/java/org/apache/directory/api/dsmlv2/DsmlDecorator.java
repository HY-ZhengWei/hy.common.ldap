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
package org.apache.directory.api.dsmlv2;


import org.dom4j.Element;


/**
 * This interface defines the methods that must be implemented to define a DSML Decorator
 * 
 * @param <M> The message to decorate
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface DsmlDecorator<M>
{
    /**
     * Converts the request/reponse to its XML representation in the DSMLv2 format
     * 
     * @param root the root dom4j Element
     * @return the dom4j Element corresponding to the entry.
     */
    Element toDsml( Element root );


    /**
     * Gets the Message this DsmlDecorator decorates.
     * 
     * @return The decorated Message instance
     */
    M getDecorated();
}
