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

package org.apache.directory.api.asn1;


/**
 * Thrown when there is a failure condition during the encoding process. This
 * exception is thrown when an Encoder encounters a encoding specific exception
 * such as invalid data, inability to calculate a checksum, characters outside
 * of the expected range.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EncoderException extends Exception
{
    /** Declares the Serial Version Uid */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance of this exception with an useful message.
     * 
     * @param message a useful message relating to the encoder specific error.
     */
    public EncoderException( String message )
    {
        super( message );
    }


    /**
     * Creates a new instance of this exception with an useful message.
     * 
     * @param message a useful message relating to the encoder specific error.
     * @param cause The parent exception
     */
    public EncoderException( String message, Exception cause )
    {
        super( message, cause );
    }
}
