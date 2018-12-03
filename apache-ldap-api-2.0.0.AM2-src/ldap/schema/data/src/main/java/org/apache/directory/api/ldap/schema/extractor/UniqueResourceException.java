/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.api.ldap.schema.extractor;


import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


/**
 * Exception for when we detect more than one unqiue schema LDIF file resource
 * on the classpath.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UniqueResourceException extends RuntimeException
{

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The resource name. */
    private final String resourceName;

    /** The urls. */
    private final List<URL> urls;

    /** The resource description. */
    private final String resourceDescription;


    /**
     * Instantiates a new unique resource exception.
     *
     * @param resourceName the resource name
     * @param resourceDescription the resource description
     */
    public UniqueResourceException( String resourceName, String resourceDescription )
    {
        this( resourceName, null, resourceDescription );
    }


    /**
     * Instantiates a new unique resource exception.
     *
     * @param resourceName the resource name
     * @param urls the URLs
     * @param resourceDescription the resource description
     */
    public UniqueResourceException( String resourceName, List<URL> urls, String resourceDescription )
    {
        this.resourceName = resourceName;
        this.urls = urls;
        this.resourceDescription = resourceDescription;
    }


    /**
     * Instantiates a new unique resource exception.
     *
     * @param resourceName the resource name
     * @param first the first
     * @param urlEnum the enum with URLs
     * @param resourceDescription the resource description
     */
    public UniqueResourceException( String resourceName, URL first, Enumeration<URL> urlEnum, String resourceDescription )
    {
        this( resourceName, toList( first, urlEnum ), resourceDescription );
    }


    private static List<URL> toList( URL first, Enumeration<URL> urlEnum )
    {
        ArrayList<URL> urls = new ArrayList<>();
        urls.add( first );
        while ( urlEnum.hasMoreElements() )
        {
            urls.add( urlEnum.nextElement() );
        }
        return urls;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage()
    {
        StringBuilder buf = new StringBuilder( "Problem locating " ).append( resourceDescription ).append( "\n" );
        
        if ( urls == null )
        {
            buf.append( "No resources named '" ).append( resourceName ).append( "' located on classpath" );
        }
        else
        {
            buf.append( "Multiple copies of resource named '" ).append( resourceName ).append(
                "' located on classpath at urls" );
            
            for ( URL url : urls )
            {
                buf.append( "\n    " ).append( url );
            }
        }
        
        return buf.toString();
    }


    /**
     * Gets the resource name.
     *
     * @return the resource name
     */
    public String getResourceName()
    {
        return resourceName;
    }


    /**
     * Gets the URLs.
     *
     * @return the URLs
     */
    public List<URL> getUrls()
    {
        return Collections.unmodifiableList( urls );
    }
}
