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
package org.apache.directory.api.ldap.model.constants;


/**
 * Contains constants used for populating the supportedSASLMechanisms 
 * in the RootDSE.
 * Final reference -&gt; class shouldn't be extended
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class SupportedSaslMechanisms
{
    /** CRAM-MD5 mechanism */
    public static final String CRAM_MD5 = "CRAM-MD5";

    /** DIGEST_MD5-MD5 mechanism */
    public static final String DIGEST_MD5 = "DIGEST-MD5";

    /** GSSAPI mechanism */
    public static final String GSSAPI = "GSSAPI";

    /** PLAIN mechanism */
    public static final String PLAIN = "PLAIN";

    /** Not a SASL JDK supported mechanism */
    public static final String NTLM = "NTLM";

    /** Not a SASL JDK supported mechanism */
    public static final String GSS_SPNEGO = "GSS-SPNEGO";

    /** EXTERNAL mechanism */
    public static final String EXTERNAL = "EXTERNAL";

    /**
     *  Ensures no construction of this class, also ensures there is no need for final keyword above
     *  (Implicit super constructor is not visible for default constructor),
     *  but is still self documenting.
     */
    private SupportedSaslMechanisms()
    {
    }
}
