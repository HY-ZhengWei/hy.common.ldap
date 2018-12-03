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
package org.apache.directory.api.dsmlv2.response;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.dsmlv2.DsmlDecorator;
import org.apache.directory.api.dsmlv2.ParserUtils;
import org.apache.directory.api.ldap.model.message.Response;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * This class represents the Batch Response. It can be used to generate an the XML String of a BatchResponse.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BatchResponseDsml
{
    /** The Responses list */
    private List<DsmlDecorator<? extends Response>> responses;

    /** The ID of the response */
    private int requestID;


    /**
     * Creates a new instance of BatchResponseDsml.
     */
    public BatchResponseDsml()
    {
        responses = new ArrayList<>();
    }


    /**
     * Gets the current response
     *
     * @return the current response
     */
    public DsmlDecorator<? extends Response> getCurrentResponse()
    {
        return responses.get( responses.size() - 1 );
    }


    /**
     * Adds a request to the Batch Response DSML.
     *
     * @param response the request to add
     * @return true (as per the general contract of the Collection.add method).
     */
    public boolean addResponse( DsmlDecorator<? extends Response> response )
    {
        return responses.add( response );
    }


    /**
     * Removes a request from the Batch Response DSML.
     *
     * @param response the request to remove
     * @return true if this list contained the specified element.
     */
    public boolean removeResponse( DsmlDecorator<Response> response )
    {
        return responses.remove( response );
    }


    /**
     * Gets the ID of the response
     * 
     * @return the ID of the response
     */
    public int getRequestID()
    {
        return requestID;
    }


    /**
     * Sets the ID of the response
     *
     * @param requestID
     *      the ID to set
     */
    public void setRequestID( int requestID )
    {
        this.requestID = requestID;
    }


    /**
     * Gets the List of all the responses
     *
     * @return
     *      the List of all the responses
     */
    public List<DsmlDecorator<? extends Response>> getResponses()
    {
        return responses;
    }


    /**
     * Converts this Batch Response to its XML representation in the DSMLv2 format.
     * The XML document will be formatted for pretty printing by default. 
     * 
     * @return the XML representation in DSMLv2 format
     */
    public String toDsml()
    {
       return toDsml( true ); 
    }
    
    
    /**
     * Converts this Batch Response to its XML representation in the DSMLv2 format.
     * 
     * @param prettyPrint if true, formats the document for pretty printing
     * @return the XML representation in DSMLv2 format
     */
    public String toDsml( boolean prettyPrint )
    {
        Document document = DocumentHelper.createDocument();
        Element element = document.addElement( "batchResponse" );

        element.add( ParserUtils.DSML_NAMESPACE );
        element.add( ParserUtils.XSD_NAMESPACE );
        element.add( ParserUtils.XSI_NAMESPACE );

        // RequestID
        if ( requestID != 0 )
        {
            element.addAttribute( "requestID", Integer.toString( requestID ) );
        }

        for ( DsmlDecorator<? extends Response> response : responses )
        {
            response.toDsml( element );
        }

        if ( prettyPrint )
        {
            document = ParserUtils.styleDocument( document );
        }
        
        return document.asXML();
    }
}
