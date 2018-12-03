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
package org.apache.directory.api.osgi;


import static org.junit.Assert.assertNotNull;

import org.apache.directory.api.i18n.I18n;


public class ApiI18nOsgiTest extends ApiOsgiTestBase
{

    @Override
    protected String getBundleName()
    {
        return "org.apache.directory.api.i18n";
    }


    @Override
    protected void useBundleClasses() throws Exception
    {
        I18n errorCode = I18n.ERR_01200_BAD_TRANSITION_FROM_STATE;
        assertNotNull( errorCode );
        assertNotNull( errorCode.getErrorCode() );
        assertNotNull( I18n.err( errorCode ) );
    }

}
