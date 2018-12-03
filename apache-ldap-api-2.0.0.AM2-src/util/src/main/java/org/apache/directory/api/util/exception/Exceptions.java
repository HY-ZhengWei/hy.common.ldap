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
package org.apache.directory.api.util.exception;


import java.util.List;


/**
 * Provides utilities for manipulating and examining <code>Throwable</code>
 * objects.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Exceptions
{
    /**
     * Private constructor
     */
    private Exceptions()
    {
    }


    /**
     * Appends the messages of each Throwable to a string, separated by a new line.
     *
     * @param errors the errors
     * @return the string with all error message
     */
    public static String printErrors( List<Throwable> errors )
    {
        StringBuilder sb = new StringBuilder();

        for ( Throwable error : errors )
        {
            sb.append( "Error : " ).append( error.getMessage() ).append( "\n" );
        }

        return sb.toString();
    }
}
