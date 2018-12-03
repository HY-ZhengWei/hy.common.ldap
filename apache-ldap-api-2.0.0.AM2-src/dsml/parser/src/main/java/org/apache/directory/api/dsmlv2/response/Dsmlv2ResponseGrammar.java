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


import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.api.dsmlv2.AbstractDsmlMessageDecorator;
import org.apache.directory.api.dsmlv2.AbstractGrammar;
import org.apache.directory.api.dsmlv2.DsmlControl;
import org.apache.directory.api.dsmlv2.DsmlDecorator;
import org.apache.directory.api.dsmlv2.Dsmlv2Container;
import org.apache.directory.api.dsmlv2.Dsmlv2StatesEnum;
import org.apache.directory.api.dsmlv2.Grammar;
import org.apache.directory.api.dsmlv2.GrammarAction;
import org.apache.directory.api.dsmlv2.GrammarTransition;
import org.apache.directory.api.dsmlv2.ParserUtils;
import org.apache.directory.api.dsmlv2.Tag;
import org.apache.directory.api.dsmlv2.response.ErrorResponse.ErrorResponseType;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.message.AddResponseImpl;
import org.apache.directory.api.ldap.model.message.BindResponseImpl;
import org.apache.directory.api.ldap.model.message.CompareResponseImpl;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.DeleteResponseImpl;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.message.ExtendedResponseImpl;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.ModifyDnResponseImpl;
import org.apache.directory.api.ldap.model.message.ModifyResponseImpl;
import org.apache.directory.api.ldap.model.message.ReferralImpl;
import org.apache.directory.api.ldap.model.message.Response;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntryImpl;
import org.apache.directory.api.ldap.model.message.SearchResultReference;
import org.apache.directory.api.ldap.model.message.SearchResultReferenceImpl;
import org.apache.directory.api.ldap.model.message.controls.OpaqueControl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Base64;
import org.apache.directory.api.util.Strings;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * This Class represents the DSMLv2 Response Grammar
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Dsmlv2ResponseGrammar extends AbstractGrammar implements Grammar
{
    /** The instance of grammar. Dsmlv2ResponseGrammar is a singleton */
    private static Dsmlv2ResponseGrammar instance = new Dsmlv2ResponseGrammar();

    /** The DSMLv2 description tags */
    private static final Set<String> DSMLV2_DESCR_TAGS;
    static
    {
        DSMLV2_DESCR_TAGS = new HashSet<>();
        DSMLV2_DESCR_TAGS.add( "success" );
        DSMLV2_DESCR_TAGS.add( "operationsError" );
        DSMLV2_DESCR_TAGS.add( "protocolError" );
        DSMLV2_DESCR_TAGS.add( "timeLimitExceeded" );
        DSMLV2_DESCR_TAGS.add( "sizeLimitExceeded" );
        DSMLV2_DESCR_TAGS.add( "compareFalse" );
        DSMLV2_DESCR_TAGS.add( "compareTrue" );
        DSMLV2_DESCR_TAGS.add( "authMethodNotSupported" );
        DSMLV2_DESCR_TAGS.add( "strongAuthRequired" );
        DSMLV2_DESCR_TAGS.add( "referral" );
        DSMLV2_DESCR_TAGS.add( "adminLimitExceeded" );
        DSMLV2_DESCR_TAGS.add( "unavailableCriticalExtension" );
        DSMLV2_DESCR_TAGS.add( "confidentialityRequired" );
        DSMLV2_DESCR_TAGS.add( "saslBindInProgress" );
        DSMLV2_DESCR_TAGS.add( "noSuchAttribute" );
        DSMLV2_DESCR_TAGS.add( "undefinedAttributeType" );
        DSMLV2_DESCR_TAGS.add( "inappropriateMatching" );
        DSMLV2_DESCR_TAGS.add( "constraintViolation" );
        DSMLV2_DESCR_TAGS.add( "attributeOrValueExists" );
        DSMLV2_DESCR_TAGS.add( "invalidAttributeSyntax" );
        DSMLV2_DESCR_TAGS.add( "noSuchObject" );
        DSMLV2_DESCR_TAGS.add( "aliasProblem" );
        DSMLV2_DESCR_TAGS.add( "invalidDNSyntax" );
        DSMLV2_DESCR_TAGS.add( "aliasDereferencingProblem" );
        DSMLV2_DESCR_TAGS.add( "inappropriateAuthentication" );
        DSMLV2_DESCR_TAGS.add( "invalidCredentials" );
        DSMLV2_DESCR_TAGS.add( "insufficientAccessRights" );
        DSMLV2_DESCR_TAGS.add( "busy" );
        DSMLV2_DESCR_TAGS.add( "unavailable" );
        DSMLV2_DESCR_TAGS.add( "unwillingToPerform" );
        DSMLV2_DESCR_TAGS.add( "loopDetect" );
        DSMLV2_DESCR_TAGS.add( "namingViolation" );
        DSMLV2_DESCR_TAGS.add( "objectClassViolation" );
        DSMLV2_DESCR_TAGS.add( "notAllowedOnNonLeaf" );
        DSMLV2_DESCR_TAGS.add( "notAllowedOnRDN" );
        DSMLV2_DESCR_TAGS.add( "entryAlreadyExists" );
        DSMLV2_DESCR_TAGS.add( "objectClassModsProhibited" );
        DSMLV2_DESCR_TAGS.add( "affectMultipleDSAs" );
        DSMLV2_DESCR_TAGS.add( "other" );
    }

    /**
     * GrammarAction that creates the Batch Response
     */
    private final GrammarAction batchResponseCreation = new GrammarAction( "Create Batch Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            BatchResponseDsml batchResponse = new BatchResponseDsml();

            container.setBatchResponse( batchResponse );

            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                batchResponse.setRequestID( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }
        }
    };

    /**
     * GrammarAction that creates the Add Response
     */
    private final GrammarAction addResponseCreation = new GrammarAction( "Create Add Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            AddResponseDsml addResponse = new AddResponseDsml(
                container.getLdapCodecService(), new AddResponseImpl() );
            container.getBatchResponse().addResponse( addResponse );

            LdapResult ldapResult = addResponse.getLdapResult();

            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                addResponse.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }

            // MatchedDN
            attributeValue = xpp.getAttributeValue( "", "matchedDN" );

            if ( attributeValue != null )
            {
                try
                {
                    ldapResult.setMatchedDn( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
        }
    };

    /**
     * GrammarAction that creates the Auth Response
     */
    private final GrammarAction authResponseCreation = new GrammarAction( "Create Auth Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            BindResponseDsml bindResponse = new BindResponseDsml(
                container.getLdapCodecService(), new BindResponseImpl() );
            container.getBatchResponse().addResponse( bindResponse );

            LdapResult ldapResult = bindResponse.getLdapResult();

            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                bindResponse.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );

            }

            // MatchedDN
            attributeValue = xpp.getAttributeValue( "", "matchedDN" );

            if ( attributeValue != null )
            {
                try
                {
                    ldapResult.setMatchedDn( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
        }
    };

    /**
     * GrammarAction that creates the Compare Response
     */
    private final GrammarAction compareResponseCreation = new GrammarAction( "Create Compare Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            CompareResponseDsml compareResponse = new CompareResponseDsml(
                container.getLdapCodecService(), new CompareResponseImpl() );
            container.getBatchResponse().addResponse( compareResponse );

            LdapResult ldapResult = compareResponse.getLdapResult();

            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                compareResponse.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }

            // MatchedDN
            attributeValue = xpp.getAttributeValue( "", "matchedDN" );

            if ( attributeValue != null )
            {
                try
                {
                    ldapResult.setMatchedDn( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
        }
    };

    /**
     * GrammarAction that creates the Del Response
     */
    private final GrammarAction delResponseCreation = new GrammarAction( "Create Del Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            DelResponseDsml delResponse = new DelResponseDsml(
                container.getLdapCodecService(), new DeleteResponseImpl() );
            container.getBatchResponse().addResponse( delResponse );

            LdapResult ldapResult = delResponse.getLdapResult();

            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                delResponse.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }

            // MatchedDN
            attributeValue = xpp.getAttributeValue( "", "matchedDN" );

            if ( attributeValue != null )
            {
                try
                {
                    ldapResult.setMatchedDn( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
        }
    };

    /**
     * GrammarAction that creates the Modify Response
     */
    private final GrammarAction modifyResponseCreation = new GrammarAction( "Create Modify Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            ModifyResponseDsml modifyResponse = new ModifyResponseDsml(
                container.getLdapCodecService(), new ModifyResponseImpl() );
            container.getBatchResponse().addResponse( modifyResponse );

            LdapResult ldapResult = modifyResponse.getLdapResult();

            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                modifyResponse.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }

            // MatchedDN
            attributeValue = xpp.getAttributeValue( "", "matchedDN" );

            if ( attributeValue != null )
            {
                try
                {
                    ldapResult.setMatchedDn( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
        }
    };

    /**
     * GrammarAction that creates the Mod Dn Response
     */
    private final GrammarAction modDNResponseCreation = new GrammarAction( "Create Mod Dn Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            ModDNResponseDsml modDNResponse = new ModDNResponseDsml(
                container.getLdapCodecService(), new ModifyDnResponseImpl() );
            container.getBatchResponse().addResponse( modDNResponse );

            LdapResult ldapResult = modDNResponse.getLdapResult();

            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                modDNResponse.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }

            // MatchedDN
            attributeValue = xpp.getAttributeValue( "", "matchedDN" );

            if ( attributeValue != null )
            {
                try
                {
                    ldapResult.setMatchedDn( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
        }
    };

    /**
     * GrammarAction that creates the Extended Response
     */
    private final GrammarAction extendedResponseCreation = new GrammarAction( "Create Extended Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            ExtendedResponseDsml extendedResponse;

            // Checking and adding the batchRequest's attributes
            String attributeValue;

            XmlPullParser xpp = container.getParser();

            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                extendedResponse = new ExtendedResponseDsml(
                    container.getLdapCodecService(), new ExtendedResponseImpl(
                        ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) ) );
            }
            else
            {
                extendedResponse = new ExtendedResponseDsml(
                    container.getLdapCodecService(), new ExtendedResponseImpl( -1 ) );
            }

            container.getBatchResponse().addResponse( extendedResponse );

            LdapResult ldapResult = extendedResponse.getLdapResult();

            // MatchedDN
            attributeValue = xpp.getAttributeValue( "", "matchedDN" );

            if ( attributeValue != null )
            {
                try
                {
                    ldapResult.setMatchedDn( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
        }
    };

    /**
     * GrammarAction that creates the Error Response
     */
    private final GrammarAction errorResponseCreation = new GrammarAction( "Create Error Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            ErrorResponse errorResponse = null;
            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                errorResponse = new ErrorResponse( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ), null );

                container.getBatchResponse().addResponse( errorResponse );
            }
            
            // type
            attributeValue = xpp.getAttributeValue( "", "type" );
            
            if ( attributeValue != null )
            {
                if ( attributeValue.equals( errorResponse.getTypeDescr( ErrorResponseType.NOT_ATTEMPTED ) ) )
                {
                    errorResponse.setErrorType( ErrorResponseType.NOT_ATTEMPTED );
                }
                else if ( attributeValue.equals( errorResponse.getTypeDescr( ErrorResponseType.COULD_NOT_CONNECT ) ) )
                {
                    errorResponse.setErrorType( ErrorResponseType.COULD_NOT_CONNECT );
                }
                else if ( attributeValue.equals( errorResponse.getTypeDescr( ErrorResponseType.CONNECTION_CLOSED ) ) )
                {
                    errorResponse.setErrorType( ErrorResponseType.CONNECTION_CLOSED );
                }
                else if ( attributeValue.equals( errorResponse.getTypeDescr( ErrorResponseType.MALFORMED_REQUEST ) ) )
                {
                    errorResponse.setErrorType( ErrorResponseType.MALFORMED_REQUEST );
                }
                else if ( attributeValue
                    .equals( errorResponse.getTypeDescr( ErrorResponseType.GATEWAY_INTERNAL_ERROR ) ) )
                {
                    errorResponse.setErrorType( ErrorResponseType.GATEWAY_INTERNAL_ERROR );
                }
                else if ( attributeValue.equals( errorResponse.getTypeDescr( ErrorResponseType.AUTHENTICATION_FAILED ) ) )
                {
                    errorResponse.setErrorType( ErrorResponseType.AUTHENTICATION_FAILED );
                }
                else if ( attributeValue.equals( errorResponse.getTypeDescr( ErrorResponseType.UNRESOLVABLE_URI ) ) )
                {
                    errorResponse.setErrorType( ErrorResponseType.UNRESOLVABLE_URI );
                }
                else if ( attributeValue.equals( errorResponse.getTypeDescr( ErrorResponseType.OTHER ) ) )
                {
                    errorResponse.setErrorType( ErrorResponseType.OTHER );
                }
                else
                {
                    throw new XmlPullParserException( I18n.err( I18n.ERR_03004_UNKNOWN_TYPE ), xpp, null );
                }
            }
            else
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03005_REQUIRE_ATTRIBUTE_TYPE ), xpp, null );
            }
        }
    };

    /**
     * GrammarAction that adds Message to an Error Response
     */
    private final GrammarAction errorResponseAddMessage = new GrammarAction( "Add Message to Error Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            ErrorResponse errorResponse = ( ErrorResponse ) container.getBatchResponse().getCurrentResponse();

            XmlPullParser xpp = container.getParser();
            try
            {
                String nextText = xpp.nextText();
                if ( !Strings.isEmpty( nextText ) )
                {
                    errorResponse.setMessage( nextText.trim() );
                }
            }
            catch ( IOException ioe )
            {
                throw new XmlPullParserException( ioe.getMessage(), xpp, ioe );
            }
        }
    };

    /**
     * GrammarAction that adds Detail to an Error Response
     */
    // TODO Look for documentation about this Detail element (the DSML documentation doesn't give enough information)
    private static final GrammarAction ERROR_RESPONSE_ADD_DETAIL = null;


    /**
     * GrammarAction that creates a Control for LDAP Result
     */
    private final GrammarAction ldapResultControlCreation = new GrammarAction( "Create Control for LDAP Result" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            AbstractDsmlMessageDecorator<? extends Message> message =
                ( AbstractDsmlMessageDecorator<? extends Message> )
                container.getBatchResponse().getCurrentResponse();

            if ( message instanceof SearchResponseDsml )
            {
                createAndAddControl( container,
                    ( ( SearchResponse ) ( ( SearchResponseDsml ) message ).getDecorated() ).getSearchResultDone() );
            }
            else
            {
                createAndAddControl( container, message );
            }
        }
    };

    /**
     * GrammarAction that creates a Control for Search Result Entry
     */
    private final GrammarAction searchResultEntryControlCreation = new GrammarAction(
        "Create Control for Search Result Entry" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResponse response = ( SearchResponse )
                ( ( SearchResponseDsml ) container.getBatchResponse()
                    .getCurrentResponse() ).getDecorated();

            createAndAddControl( container, response.getCurrentSearchResultEntry() );
        }
    };

    /**
     * GrammarAction that creates a Control for Search Result Entry
     */
    private final GrammarAction searchResultReferenceControlCreation = new GrammarAction(
        "Create Control for Search Result Reference" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResponse response = ( SearchResponse )
                ( ( SearchResponseDsml ) container.getBatchResponse()
                    .getCurrentResponse() ).getDecorated();

            createAndAddControl( container, response.getCurrentSearchResultReference() );
        }
    };

    /**
     * GrammarAction that creates a Control Value for LDAP Result
     */
    private final GrammarAction ldapResultControlValueCreation = new GrammarAction(
        "Add ControlValue to Control for LDAP Result" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            AbstractDsmlMessageDecorator<? extends Response> response
            = ( AbstractDsmlMessageDecorator<? extends Response> )
                container.getBatchResponse().getCurrentResponse();

            if ( response instanceof SearchResponseDsml )
            {
                SearchResponse searchResponse = ( SearchResponse )
                    response.getDecorated();
                createAndAddControlValue( container,
                    searchResponse.getSearchResultDone() );
            }
            else
            {
                createAndAddControlValue( container, response );
            }
        }
    };

    /**
     * GrammarAction that creates a Control Value for Search Result Entry
     */
    private final GrammarAction searchResultEntryControlValueCreation = new GrammarAction(
        "Add ControlValue to Control for Search Result Entry" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResponse response = ( SearchResponse )
                container.getBatchResponse().getCurrentResponse().getDecorated();
            createAndAddControlValue( container,
                response.getCurrentSearchResultEntry() );
        }
    };

    /**
     * GrammarAction that creates a Control Value for Search Result Reference
     */
    private final GrammarAction searchResultReferenceControlValueCreation = new GrammarAction(
        "Add ControlValue to Control for Search Result Entry" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResponseDsml response = ( SearchResponseDsml )
                container.getBatchResponse().getCurrentResponse();
            createAndAddControlValue( container,
                ( ( SearchResponse ) response.getDecorated() ).getCurrentSearchResultReference() );
        }
    };

    /**
     * GrammarAction that adds a Result Code to a LDAP Result
     */
    private final GrammarAction ldapResultAddResultCode = new GrammarAction( "Add ResultCode to LDAP Result" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            DsmlDecorator<? extends Response> ldapResponse =
                container.getBatchResponse().getCurrentResponse();

            LdapResult ldapResult;

            // Search Response is a special case
            // ResultCode can only occur in a case of Search Result Done in a Search Response
            if ( ldapResponse.getDecorated() instanceof SearchResponse )
            {
                SearchResponse searchResponse = ( SearchResponse ) ldapResponse.getDecorated();
                ldapResult = searchResponse.getSearchResultDone().getLdapResult();
            }
            else
            {
                ldapResult = ( ( ResultResponse ) ldapResponse.getDecorated() ).getLdapResult();
            }

            XmlPullParser xpp = container.getParser();

            // Checking and adding the request's attributes
            String attributeValue;
            // code
            attributeValue = xpp.getAttributeValue( "", "code" );

            if ( attributeValue != null )
            {
                try
                {
                    ldapResult.setResultCode( ResultCodeEnum.getResultCode( Integer.parseInt( attributeValue ) ) );
                }
                catch ( NumberFormatException nfe )
                {
                    throw new XmlPullParserException( I18n.err( I18n.ERR_03009_RESULT_CODE_NOT_INTEGER ), xpp, nfe );
                }
            }
            else
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03010_CODE_ATTRIBUTE_REQUIRED ), xpp, null );
            }

            // descr
            attributeValue = xpp.getAttributeValue( "", "descr" );

            if ( ( attributeValue != null ) && !DSMLV2_DESCR_TAGS.contains( attributeValue ) )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03011_DESCR_DOESNT_MATCH_VALUES, attributeValue ), xpp, null );
            }
        }
    };

    /**
     * GrammarAction that adds a Error Message to a LDAP Result
     */
    private final GrammarAction ldapResultAddErrorMessage = new GrammarAction( "Add Error Message to LDAP Result" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            DsmlDecorator<? extends Response> ldapResponse =
                container.getBatchResponse().getCurrentResponse();

            LdapResult ldapResult;

            // Search Response is a special case
            // ResultCode can only occur in a case of Search Result Done in a Search Response
            if ( ldapResponse.getDecorated() instanceof SearchResponse )
            {
                SearchResponse searchResponse = ( SearchResponse ) ldapResponse.getDecorated();
                ldapResult = searchResponse.getSearchResultDone().getLdapResult();
            }
            else
            {
                ldapResult = ( ( ResultResponse ) ldapResponse.getDecorated() ).getLdapResult();
            }

            XmlPullParser xpp = container.getParser();

            try
            {
                String nextText = xpp.nextText();

                if ( !Strings.isEmpty( nextText ) )
                {
                    ldapResult.setDiagnosticMessage( nextText.trim() );
                }
            }
            catch ( IOException ioe )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03008_UNEXPECTED_ERROR, ioe.getMessage() ), xpp, ioe );
            }
        }
    };

    /**
     * GrammarAction that adds a Referral to a LDAP Result
     */
    private final GrammarAction ldapResultAddReferral = new GrammarAction( "Add Referral to LDAP Result" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            DsmlDecorator<? extends Response> ldapResponse =
                container.getBatchResponse().getCurrentResponse();

            LdapResult ldapResult;

            // Search Response is a special case
            // ResultCode can only occur in a case of Search Result Done in a Search Response
            if ( ldapResponse.getDecorated() instanceof SearchResponse )
            {
                SearchResponse searchResponse = ( SearchResponse ) ldapResponse.getDecorated();
                ldapResult = searchResponse.getSearchResultDone().getLdapResult();
            }
            else
            {
                ldapResult = ( ( ResultResponse ) ldapResponse.getDecorated() ).getLdapResult();
            }

            // Initialization of the Referrals if needed
            if ( ldapResult.getReferral() == null )
            {
                ldapResult.setReferral( new ReferralImpl() );
            }

            XmlPullParser xpp = container.getParser();

            try
            {
                String nextText = xpp.nextText();

                if ( !Strings.isEmpty( nextText ) )
                {
                    try
                    {
                        String urlStr = nextText.trim();
                        LdapUrl ldapUrl = new LdapUrl( urlStr );
                        ldapResult.getReferral().addLdapUrl( ldapUrl.toString() );
                    }
                    catch ( LdapURLEncodingException luee )
                    {
                        throw new XmlPullParserException( luee.getMessage(), xpp, luee );
                    }
                }
            }
            catch ( IOException ioe )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03008_UNEXPECTED_ERROR, ioe.getMessage() ), xpp, ioe );
            }
        }
    };

    /**
     * GrammarAction that creates the Search Response
     */
    private final GrammarAction searchResponseCreation = new GrammarAction( "Create Search Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            XmlPullParser xpp = container.getParser();
            SearchResponse searchResponse;

            // Checking and adding the batchRequest's attributes
            String attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                searchResponse = new SearchResponse(
                    ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }
            else
            {
                searchResponse = new SearchResponse();
            }

            container.getBatchResponse().addResponse( new SearchResponseDsml(
                container.getLdapCodecService(), searchResponse ) );
        }
    };

    /**
     * GrammarAction that creates a Search Result Entry
     */
    private final GrammarAction searchResultEntryCreation = new GrammarAction(
        "Add Search Result Entry to Search Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResultEntryDsml searchResultEntry =
                new SearchResultEntryDsml( container.getLdapCodecService(),
                    new SearchResultEntryImpl() );
            SearchResponseDsml searchResponse = ( SearchResponseDsml )
                container.getBatchResponse().getCurrentResponse();
            searchResponse.addResponse( searchResultEntry );

            XmlPullParser xpp = container.getParser();

            // Checking and adding the request's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                searchResultEntry.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }

            // dn
            attributeValue = xpp.getAttributeValue( "", "dn" );

            if ( attributeValue != null )
            {
                try
                {
                    searchResultEntry.setObjectName( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
            else
            {
                throw new XmlPullParserException( "dn attribute is required", xpp, null );
            }
        }
    };

    /**
     * GrammarAction that creates a Search Result Reference
     */
    private final GrammarAction searchResultReferenceCreation = new GrammarAction(
        "Add Search Result Reference to Search Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResultReferenceDsml searchResultReference =
                new SearchResultReferenceDsml(
                    container.getLdapCodecService(),
                    new SearchResultReferenceImpl() );

            SearchResponseDsml searchResponseDsml = ( SearchResponseDsml )
                container.getBatchResponse().getCurrentResponse();

            searchResponseDsml.addResponse( searchResultReference );

            XmlPullParser xpp = container.getParser();

            // Checking and adding the request's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                searchResultReference.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }
        }
    };

    /**
     * GrammarAction that creates a Search Result Done
     */
    private final GrammarAction searchResultDoneCreation = new GrammarAction(
        "Add Search Result Done to Search Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResultDoneDsml searchResultDone =
                new SearchResultDoneDsml( container.getLdapCodecService(),
                    new SearchResultDoneImpl() );

            SearchResponseDsml searchResponseDsml = ( SearchResponseDsml )
                container.getBatchResponse().getCurrentResponse();
            searchResponseDsml.addResponse( searchResultDone );

            XmlPullParser xpp = container.getParser();

            // Checking and adding the batchRequest's attributes
            String attributeValue;
            // requestID
            attributeValue = xpp.getAttributeValue( "", "requestID" );

            if ( attributeValue != null )
            {
                searchResultDone.setMessageId( ParserUtils.parseAndVerifyRequestID( attributeValue, xpp ) );
            }

            // MatchedDN
            attributeValue = xpp.getAttributeValue( "", "matchedDN" );

            if ( attributeValue != null )
            {
                try
                {
                    searchResultDone.getLdapResult().setMatchedDn( new Dn( attributeValue ) );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new XmlPullParserException( lide.getMessage(), xpp, lide );
                }
            }
        }
    };

    /**
     * GrammarAction that adds an Attr to a Search Result Entry
     */
    private final GrammarAction searchResultEntryAddAttr = new GrammarAction( "Add Attr to Search Result Entry" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResponse searchResponse = ( SearchResponse )
                container.getBatchResponse().getCurrentResponse().getDecorated();

            SearchResultEntryDsml searchResultEntry = searchResponse.getCurrentSearchResultEntry();

            XmlPullParser xpp = container.getParser();

            // Checking and adding the request's attributes
            String attributeValue;
            // name
            attributeValue = xpp.getAttributeValue( "", "name" );

            if ( attributeValue != null )
            {
                try
                {
                    searchResultEntry.addAttribute( attributeValue );
                }
                catch ( LdapException le )
                {
                    throw new XmlPullParserException( I18n.err( I18n.ERR_03002_NAME_ATTRIBUTE_REQUIRED ), xpp, le );
                }
            }
            else
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03002_NAME_ATTRIBUTE_REQUIRED ), xpp, null );
            }
        }
    };

    /**
     * GrammarAction that adds a Value to an Attr of a Search Result Entry
     */
    private final GrammarAction searchResultEntryAddValue = new GrammarAction(
        "Add a Value to an Attr of a Search Result Entry" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResponse searchResponse = ( SearchResponse )
                container.getBatchResponse().getCurrentResponse().getDecorated();
            SearchResultEntryDsml searchResultEntry = searchResponse.getCurrentSearchResultEntry();

            XmlPullParser xpp = container.getParser();

            try
            {
                // We have to catch the type Attribute Value before going to the next Text node
                String typeValue = ParserUtils.getXsiTypeAttributeValue( xpp );

                // Getting the value
                String nextText = xpp.nextText();

                try
                {
                    if ( ParserUtils.isBase64BinaryValue( xpp, typeValue ) )
                    {
                        searchResultEntry.addAttributeValue( Base64.decode( nextText.toCharArray() ) );
                    }
                    else
                    {
                        searchResultEntry.addAttributeValue( nextText );
                    }
                }
                catch ( LdapException le )
                {
                    throw new XmlPullParserException( le.getMessage(), xpp, le );
                }
            }
            catch ( IOException ioe )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03008_UNEXPECTED_ERROR, ioe.getMessage() ), xpp, ioe );
            }
        }
    };

    /**
     * GrammarAction that adds a Ref to a Search Result Reference
     */
    private final GrammarAction searchResultReferenceAddRef = new GrammarAction(
        "Add a Ref to a Search Result Reference" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            SearchResponse searchResponse = ( SearchResponse )
                container.getBatchResponse().getCurrentResponse().getDecorated();
            SearchResultReference searchResultReference = searchResponse.getCurrentSearchResultReference();

            XmlPullParser xpp = container.getParser();

            try
            {
                String nextText = xpp.nextText();

                if ( !Strings.isEmpty( nextText ) )
                {
                    LdapUrl ldapUrl = new LdapUrl( nextText );

                    searchResultReference.getReferral().addLdapUrl( ldapUrl.toString() );
                }
            }
            catch ( IOException ioe )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03008_UNEXPECTED_ERROR, ioe.getMessage() ), xpp, ioe );
            }
            catch ( LdapURLEncodingException luee )
            {
                throw new XmlPullParserException( luee.getMessage(), xpp, luee );
            }
        }
    };

    /**
     * GrammarAction that adds Result Code to an Extended Response
     */
    private final GrammarAction extendedResponseAddResultCode = ldapResultAddResultCode;

    /**
     * GrammarAction that creates the Search Response
     */
    private final GrammarAction extendedResponseAddErrorMessage = ldapResultAddErrorMessage;

    /**
     * GrammarAction that adds a Referral to an Extended Response
     */
    private final GrammarAction extendedResponseAddReferral = ldapResultAddReferral;

    /**
     * GrammarAction that adds a Response Name to an Extended Response
     */
    private final GrammarAction extendedResponseAddResponseName = new GrammarAction(
        "Add Response Name to Extended Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            ExtendedResponse extendedResponse = ( ExtendedResponse ) container.getBatchResponse().getCurrentResponse();

            XmlPullParser xpp = container.getParser();

            try
            {
                String nextText = xpp.nextText();

                if ( !Strings.isEmpty( nextText ) )
                {
                    extendedResponse.setResponseName( Oid.fromString( nextText.trim() ).toString() );
                }

            }
            catch ( IOException ioe )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03008_UNEXPECTED_ERROR, ioe.getMessage() ), xpp, ioe );
            }
            catch ( DecoderException de )
            {
                throw new XmlPullParserException( de.getMessage(), xpp, de );
            }
        }
    };

    /**
     * GrammarAction that adds a Response to an Extended Response
     */
    private final GrammarAction extendedResponseAddResponse = new GrammarAction( "Add Response to Extended Response" )
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            ExtendedResponseDsml extendedResponse = ( ExtendedResponseDsml ) container.getBatchResponse()
                .getCurrentResponse();

            XmlPullParser xpp = container.getParser();

            try
            {
                // We have to catch the type Attribute Value before going to the next Text node
                String typeValue = ParserUtils.getXsiTypeAttributeValue( xpp );

                // Getting the value
                String nextText = xpp.nextText();

                if ( ParserUtils.isBase64BinaryValue( xpp, typeValue ) )
                {
                    extendedResponse.setResponseValue( Base64.decode( nextText.trim().toCharArray() ) );
                }
                else
                {
                    extendedResponse.setResponseValue( Strings.getBytesUtf8( nextText.trim() ) );
                }
            }
            catch ( IOException ioe )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03008_UNEXPECTED_ERROR, ioe.getMessage() ), xpp, ioe );
            }
        }
    };


    @SuppressWarnings("unchecked")
    private Dsmlv2ResponseGrammar()
    {
        name = Dsmlv2ResponseGrammar.class.getName();

        // Create the transitions table
        super.transitions = ( HashMap<Tag, GrammarTransition>[] ) Array.newInstance( HashMap.class, 300 );

        //====================================================
        //  Transitions concerning : BATCH RESPONSE
        //====================================================
        super.transitions[Dsmlv2StatesEnum.INIT_GRAMMAR_STATE.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // ** OPEN BATCH Reponse **
        // State: [INIT_GRAMMAR_STATE] - Tag: <batchResponse>
        super.transitions[Dsmlv2StatesEnum.INIT_GRAMMAR_STATE.ordinal()].put( new Tag( "batchResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.INIT_GRAMMAR_STATE, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                batchResponseCreation ) );

        //====================================================
        //  Transitions concerning : BATCH RESPONSE LOOP
        //====================================================
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [BATCH_RESPONSE_LOOP] - Tag: <addResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "addResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.LDAP_RESULT,
                addResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: <authResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "authResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.LDAP_RESULT,
                authResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: <compareResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "compareResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.LDAP_RESULT,
                compareResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: <delResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "delResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.LDAP_RESULT,
                delResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: <modifyResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "modifyResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.LDAP_RESULT,
                modifyResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: <modDNResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "modDNResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.LDAP_RESULT,
                modDNResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: <extendedResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put(
            new Tag( "extendedResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.EXTENDED_RESPONSE,
                extendedResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: <errorResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "errorResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.ERROR_RESPONSE,
                errorResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: <searchReponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "searchResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.SEARCH_RESPONSE,
                searchResponseCreation ) );

        // State: [BATCH_RESPONSE_LOOP] - Tag: </batchResponse>
        super.transitions[Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP.ordinal()].put( new Tag( "batchResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, Dsmlv2StatesEnum.GRAMMAR_END, null ) );

        //====================================================
        //  Transitions concerning : ERROR RESPONSE
        //====================================================
        super.transitions[Dsmlv2StatesEnum.ERROR_RESPONSE.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.MESSAGE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.DETAIL_START.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.DETAIL_END.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [ERROR_RESPONSE] - Tag: <message>
        super.transitions[Dsmlv2StatesEnum.ERROR_RESPONSE.ordinal()].put( new Tag( "message", Tag.START ),
            new GrammarTransition(
                Dsmlv2StatesEnum.ERROR_RESPONSE, Dsmlv2StatesEnum.MESSAGE_END, errorResponseAddMessage ) );

        // State: [ERROR_RESPONSE] - Tag: <detail>
        super.transitions[Dsmlv2StatesEnum.ERROR_RESPONSE.ordinal()].put( new Tag( "detail", Tag.START ),
            new GrammarTransition(
                Dsmlv2StatesEnum.ERROR_RESPONSE, Dsmlv2StatesEnum.DETAIL_START, ERROR_RESPONSE_ADD_DETAIL ) );

        // State: [MESSAGE_END] - Tag: </errorResponse>
        super.transitions[Dsmlv2StatesEnum.MESSAGE_END.ordinal()].put( new Tag( "errorResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.MESSAGE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [MESSAGE_END] - Tag: <detail>
        super.transitions[Dsmlv2StatesEnum.MESSAGE_END.ordinal()].put( new Tag( "detail", Tag.START ),
            new GrammarTransition(
                Dsmlv2StatesEnum.MESSAGE_END, Dsmlv2StatesEnum.DETAIL_START, ERROR_RESPONSE_ADD_DETAIL ) );

        // State: [DETAIL_START] - Tag: </detail>
        super.transitions[Dsmlv2StatesEnum.DETAIL_START.ordinal()].put( new Tag( "detail", Tag.END ),
            new GrammarTransition(
                Dsmlv2StatesEnum.DETAIL_START, Dsmlv2StatesEnum.DETAIL_END, null ) );

        // State: [DETAIL_END] - Tag: <detail>
        super.transitions[Dsmlv2StatesEnum.DETAIL_END.ordinal()].put( new Tag( "detail", Tag.END ),
            new GrammarTransition(
                Dsmlv2StatesEnum.DETAIL_END, Dsmlv2StatesEnum.DETAIL_END, ERROR_RESPONSE_ADD_DETAIL ) );

        // State: [ERROR_RESPONSE] - Tag: </errorResponse>
        super.transitions[Dsmlv2StatesEnum.ERROR_RESPONSE.ordinal()].put( new Tag( "errorResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.ERROR_RESPONSE, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        //====================================================
        //  Transitions concerning : EXTENDED RESPONSE
        //====================================================
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_START.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_VALUE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_START.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.RESPONSE_NAME_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.RESPONSE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [EXTENDED_RESPONSE] - Tag: <control>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE.ordinal()].put( new Tag( "control", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_START, ldapResultControlCreation ) );

        // State: [EXTENDED_RESPONSE_CONTROL_START] - Tag: <controlValue>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_START.ordinal()].put( new Tag( "controlValue",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_START,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_VALUE_END, ldapResultControlValueCreation ) );

        // State: [EXTENDED_RESPONSE_CONTROL_VALUE_END] - Tag: </control>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_VALUE_END.ordinal()].put( new Tag( "control",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_VALUE_END,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_END, null ) );

        // State: [EXTENDED_RESPONSE_CONTROL_START] - Tag: </control>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_START.ordinal()].put(
            new Tag( "control", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_START,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_END, null ) );

        // State: [EXTENDED_RESPONSE_CONTROL_END] - Tag: <control>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_END.ordinal()].put(
            new Tag( "control", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_END,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_START, ldapResultControlCreation ) );

        // State: [EXTENDED_RESPONSE_CONTROL_END] - Tag: <resultCode>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_END.ordinal()].put( new Tag( "resultCode",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_CONTROL_END,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_START, extendedResponseAddResultCode ) );

        // State: [EXTENDED_RESPONSE] - Tag: <resultCode>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE.ordinal()].put( new Tag( "resultCode", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_START, extendedResponseAddResultCode ) );

        // State: [EXTENDED_RESPONSE_RESULT_CODE_START] - Tag: </resultCode>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_START.ordinal()].put( new Tag( "resultCode",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_START,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END, null ) );

        // State: [EXTENDED_RESPONSE_RESULT_CODE_END] - Tag: <errorMessage>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END.ordinal()].put(
            new Tag( "errorMessage", Tag.START ), new GrammarTransition(
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END, extendedResponseAddErrorMessage ) );

        // State: [EXTENDED_RESPONSE_RESULT_CODE_END] - Tag: <referral>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END.ordinal()].put( new Tag( "referral",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END, extendedResponseAddReferral ) );

        // State: [EXTENDED_RESPONSE_RESULT_CODE_END] - Tag: <responseName>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END.ordinal()].put(
            new Tag( "responseName", Tag.START ), new GrammarTransition(
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END, Dsmlv2StatesEnum.RESPONSE_NAME_END,
                extendedResponseAddResponseName ) );

        // State: [EXTENDED_RESPONSE_RESULT_CODE_END] - Tag: <response>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END.ordinal()].put( new Tag( "response",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END, Dsmlv2StatesEnum.RESPONSE_END,
                extendedResponseAddResponse ) );

        // State: [EXTENDED_RESPONSE_RESULT_CODE_END] - Tag: </extendedResponse>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END.ordinal()].put(
            new Tag( "extendedResponse", Tag.END ), new GrammarTransition(
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_RESULT_CODE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [EXTENDED_RESPONSE_ERROR_MESSAGE_END] - Tag: <referral>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END.ordinal()].put( new Tag( "referral",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END, extendedResponseAddReferral ) );

        // State: [EXTENDED_RESPONSE_ERROR_MESSAGE_END] - Tag: <responseName>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END.ordinal()].put(
            new Tag( "responseName", Tag.START ), new GrammarTransition(
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END, Dsmlv2StatesEnum.RESPONSE_NAME_END,
                extendedResponseAddResponseName ) );

        // State: [EXTENDED_RESPONSE_ERROR_MESSAGE_END] - Tag: <response>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END.ordinal()].put( new Tag( "response",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END, Dsmlv2StatesEnum.RESPONSE_END,
                extendedResponseAddResponse ) );

        // State: [EXTENDED_RESPONSE_ERROR_MESSAGE_END] - Tag: </extendedResponse>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END.ordinal()].put( new Tag(
            "extendedResponse",
            Tag.END ), new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_ERROR_MESSAGE_END,
            Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [EXTENDED_RESPONSE_REFERRAL_END] - Tag: <referral>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END.ordinal()].put( new Tag( "referral",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END,
                Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END, extendedResponseAddReferral ) );

        // State: [EXTENDED_RESPONSE_REFERRAL_END] - Tag: <responseName>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END.ordinal()].put( new Tag( "responseName",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END, Dsmlv2StatesEnum.RESPONSE_NAME_END,
                extendedResponseAddResponseName ) );

        // State: [EXTENDED_RESPONSE_REFERRAL_END] - Tag: <reponse>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END.ordinal()].put(
            new Tag( "reponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END, Dsmlv2StatesEnum.RESPONSE_END,
                extendedResponseAddResponse ) );

        // State: [EXTENDED_RESPONSE_REFERRAL_END] - Tag: </extendedResponse>
        super.transitions[Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END.ordinal()].put( new Tag( "extendedResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.EXTENDED_RESPONSE_REFERRAL_END,
                Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [RESPONSE_NAME_END] - Tag: <response>
        super.transitions[Dsmlv2StatesEnum.RESPONSE_NAME_END.ordinal()].put( new Tag( "response", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.RESPONSE_NAME_END, Dsmlv2StatesEnum.RESPONSE_END,
                extendedResponseAddResponse ) );

        // State: [RESPONSE_NAME_END] - Tag: </extendedResponse>
        super.transitions[Dsmlv2StatesEnum.RESPONSE_NAME_END.ordinal()].put( new Tag( "extendedResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.RESPONSE_NAME_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [RESPONSE_END] - Tag: </extendedResponse>
        super.transitions[Dsmlv2StatesEnum.RESPONSE_END.ordinal()].put( new Tag( "extendedResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.RESPONSE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        //====================================================
        //  Transitions concerning : LDAP RESULT
        //====================================================
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_START.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_VALUE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_START.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_DONE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [LDAP_RESULT] - Tag: <control>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT.ordinal()].put( new Tag( "control", Tag.START ),
            new GrammarTransition(
                Dsmlv2StatesEnum.LDAP_RESULT, Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_START, ldapResultControlCreation ) );

        // State: [LDAP_RESULT] - Tag: <resultCode>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT.ordinal()]
            .put( new Tag( "resultCode", Tag.START ), new GrammarTransition(
                Dsmlv2StatesEnum.LDAP_RESULT, Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_START, ldapResultAddResultCode ) );

        // State: [LDAP_RESULT_CONTROL_START] - Tag: <controlValue>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_START.ordinal()].put(
            new Tag( "controlValue", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_START,
                Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_VALUE_END, ldapResultControlValueCreation ) );

        // State: [LDAP_RESULT_CONTROL_VALUE_END] - Tag: </control>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_VALUE_END.ordinal()].put( new Tag( "control", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_VALUE_END,
                Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_END, null ) );

        // State: [LDAP_RESULT_CONTROL_START] - Tag: </control>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_START.ordinal()].put( new Tag( "control", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_START,
                Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_END, null ) );

        // State: [LDAP_RESULT_CONTROL_END] - Tag: <control>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_END.ordinal()].put( new Tag( "control", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_END,
                Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_START, ldapResultControlCreation ) );

        // State: [LDAP_RESULT_CONTROL_END] - Tag: <resultCode>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_END.ordinal()].put( new Tag( "resultCode", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_CONTROL_END,
                Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_START, ldapResultAddResultCode ) );

        // State: [LDAP_RESULT_RESULT_CODE_START] - Tag: </resultCode>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_START.ordinal()].put(
            new Tag( "resultCode", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_START,
                Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END, null ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: <errorMessage>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put( new Tag( "errorMessage",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END,
                Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END, ldapResultAddErrorMessage ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: <referral>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put(
            new Tag( "referral", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END,
                Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, ldapResultAddReferral ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: </addResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put(
            new Tag( "addResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: </authResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put(
            new Tag( "authResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: </compareResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put( new Tag( "compareResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: </delResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put(
            new Tag( "delResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: </modifyResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put( new Tag( "modifyResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: </modDNResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put( new Tag( "modDNResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_RESULT_CODE_END] - Tag: </searchResultDone>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END.ordinal()].put( new Tag( "searchResultDone",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_RESULT_CODE_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_DONE_END, null ) );

        // State: [SEARCH_RESULT_DONE_END] - Tag: </searchResponse>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_DONE_END.ordinal()]
            .put( new Tag( "searchResponse", Tag.END ), new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_DONE_END,
                Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [LDAP_RESULT_ERROR_MESSAGE_END] - Tag: <referral>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()].put(
            new Tag( "referral", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, ldapResultAddReferral ) );

        // State: [LDAP_RESULT_ERROR_MESSAGE_END] - Tag: </addResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()].put( new Tag( "addResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [LDAP_RESULT_ERROR_MESSAGE_END] - Tag: </authResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()].put( new Tag( "authResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [LDAP_RESULT_ERROR_MESSAGE_END] - Tag: </compareResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()].put( new Tag( "compareResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [LDAP_RESULT_ERROR_MESSAGE_END] - Tag: </delResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()].put( new Tag( "delResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [LDAP_RESULT_ERROR_MESSAGE_END] - Tag: </modifyResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()].put( new Tag( "modifyResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [LDAP_RESULT_ERROR_MESSAGE_END] - Tag: </modDNResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()].put( new Tag( "modDNResponse",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP, null ) );

        // State: [LDAP_RESULT_ERROR_MESSAGE_END] - Tag: </searchResultDone>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END.ordinal()].put( new Tag( "searchResultDone",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_ERROR_MESSAGE_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_DONE_END, null ) );

        // State: [LDAP_RESULT_REFERRAL_END] - Tag: <referral>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()].put( new Tag( "referral", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END,
                Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, ldapResultAddReferral ) );

        // State: [LDAP_RESULT_REFERRAL_END] - Tag: </addResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()].put( new Tag( "addResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_REFERRAL_END] - Tag: </authResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()].put( new Tag( "authResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_REFERRAL_END] - Tag: </compareResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()].put(
            new Tag( "compareResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_REFERRAL_END] - Tag: </delResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()].put( new Tag( "delResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_REFERRAL_END] - Tag: </modifyResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()].put(
            new Tag( "modifyResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_REFERRAL_END] - Tag: </modDNResponse>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()].put(
            new Tag( "modDNResponse", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                null ) );

        // State: [LDAP_RESULT_REFERRAL_END] - Tag: </searchResultDone>
        super.transitions[Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END.ordinal()].put( new Tag( "searchResultDone",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.LDAP_RESULT_REFERRAL_END, Dsmlv2StatesEnum.SEARCH_RESULT_DONE_END,
                null ) );

        //====================================================
        //  Transitions concerning : SEARCH RESPONSE
        //====================================================
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESPONSE.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [SEARCH_REPONSE] - Tag: <searchResultEntry>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESPONSE.ordinal()].put( new Tag( "searchResultEntry", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESPONSE, Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY,
                searchResultEntryCreation ) );

        // State: [SEARCH_REPONSE] - Tag: <searchResultReference>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESPONSE.ordinal()].put(
            new Tag( "searchResultReference", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESPONSE, Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE,
                searchResultReferenceCreation ) );

        // State: [SEARCH_REPONSE] - Tag: <searchResultDone>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESPONSE.ordinal()].put( new Tag( "searchResultDone", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESPONSE, Dsmlv2StatesEnum.LDAP_RESULT,
                searchResultDoneCreation ) );

        //====================================================
        //  Transitions concerning : SEARCH RESULT ENTRY
        //====================================================
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_START.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_VALUE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_START.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_VALUE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [SEARCH_RESULT_ENTRY] - Tag: <control>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY.ordinal()].put( new Tag( "control", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_START, searchResultEntryControlCreation ) );

        // State: [SEARCH_RESULT_ENTRY] - Tag: <attr>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY.ordinal()].put( new Tag( "attr", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_START, searchResultEntryAddAttr ) );

        // State: [SEARCH_RESULT_ENTRY] - Tag: </searchResultEntry>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY.ordinal()].put( new Tag( "searchResultEntry", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY, Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP,
                null ) );

        // State: [SEARCH_RESULT_ENTRY_CONTROL_START] - Tag: <controlValue>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_START.ordinal()].put(
            new Tag( "controlValue", Tag.START ), new GrammarTransition(
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_START,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_VALUE_END, searchResultEntryControlValueCreation ) );

        // State: [SEARCH_RESULT_ENTRY_CONTROL_VALUE_END] - Tag: </control>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_VALUE_END.ordinal()].put( new Tag( "control",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_VALUE_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END, null ) );

        // State: [SEARCH_RESULT_ENTRY_CONTROL_START] - Tag: </control>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_START.ordinal()].put( new Tag( "control",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_START,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END, null ) );

        // State: [SEARCH_RESULT_ENTRY_CONTROL_END] - Tag: <control>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END.ordinal()].put( new Tag( "control",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_START, searchResultEntryControlCreation ) );

        // State: [SEARCH_RESULT_ENTRY_CONTROL_END] - Tag: </searchResultEntry>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END.ordinal()].put(
            new Tag( "searchResultEntry", Tag.END ), new GrammarTransition(
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END, Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP, null ) );

        // State: [SEARCH_RESULT_ENTRY_CONTROL_END] - Tag: <attr>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END.ordinal()].put(
            new Tag( "attr", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_CONTROL_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_START, null ) );

        // State: [SEARCH_RESULT_ENTRY_ATTR_START] - Tag: </attr>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_START.ordinal()].put( new Tag( "attr", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_START,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_END, null ) );

        // State: [SEARCH_RESULT_ENTRY_ATTR_START] - Tag: <value>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_START.ordinal()].put(
            new Tag( "value", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_START,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_VALUE_END, searchResultEntryAddValue ) );

        // State: [SEARCH_RESULT_ENTRY_ATTR_END] - Tag: <attr>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_END.ordinal()].put( new Tag( "attr", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_START, searchResultEntryAddAttr ) );

        // State: [SEARCH_RESULT_ENTRY_ATTR_END] - Tag: </searchResultEntry>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_END.ordinal()].put( new Tag( "searchResultEntry",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP, null ) );

        // State: [SEARCH_RESULT_ENTRY_VALUE_END] - Tag: <value>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_VALUE_END.ordinal()].put( new Tag( "value", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_VALUE_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_VALUE_END, searchResultEntryAddValue ) );

        // State: [SEARCH_RESULT_ENTRY_VALUE_END] - Tag: </attr>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_VALUE_END.ordinal()].put( new Tag( "attr", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_VALUE_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_ATTR_END, null ) );

        //====================================================
        //  Transitions concerning : SEARCH RESULT ENTRY LOOP
        //====================================================
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [SEARCH_RESULT_ENTRY_LOOP] - Tag: <searchResultEntry>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP.ordinal()].put( new Tag( "searchResultEntry",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP, Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY,
                searchResultEntryCreation ) );

        // State: [SEARCH_RESULT_ENTRY_LOOP] - Tag: <searchResultReference>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP.ordinal()].put(
            new Tag( "searchResultReference", Tag.START ), new GrammarTransition(
                Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP, Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE,
                searchResultReferenceCreation ) );

        // State: [SEARCH_RESULT_ENTRY_LOOP] - Tag: <searchResultDone>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP.ordinal()].put( new Tag( "searchResultDone",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_ENTRY_LOOP, Dsmlv2StatesEnum.LDAP_RESULT,
                searchResultDoneCreation ) );

        //====================================================
        //  Transitions concerning : SEARCH RESULT REFERENCE
        //====================================================
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_START.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_VALUE_END.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_REF_END.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [SEARCH_RESULT_REFERENCE] - Tag: <control>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE.ordinal()].put( new Tag( "control", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE,
                Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_START, searchResultReferenceControlCreation ) );

        // State: [SEARCH_RESULT_REFERENCE] - Tag: <ref>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE.ordinal()].put( new Tag( "ref", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE,
                Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_REF_END, searchResultReferenceAddRef ) );

        // State: [SEARCH_RESULT_REFERENCE_CONTROL_START] - Tag: <controlValue>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_START.ordinal()].put( new Tag(
            "controlValue",
            Tag.START ), new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_START,
            Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_VALUE_END, searchResultReferenceControlValueCreation ) );

        // State: [sEARCH_RESULT_REFERENCE_CONTROL_VALUE_END] - Tag: </control>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_VALUE_END.ordinal()].put(
            new Tag( "control", Tag.END ), new GrammarTransition(
                Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_VALUE_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_END, null ) );

        // State: [SEARCH_RESULT_REFERENCE_CONTROL_START] - Tag: </control>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_START.ordinal()].put( new Tag( "control",
            Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_START,
                Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_END, null ) );

        // State: [SEARCH_RESULT_REFERENCE_CONTROL_END] - Tag: <control>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_END.ordinal()].put( new Tag( "control",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_START, searchResultReferenceControlCreation ) );

        // State: [SEARCH_RESULT_REFERENCE_CONTROL_END] - Tag: <ref>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_END.ordinal()].put( new Tag( "ref",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_CONTROL_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_REF_END, searchResultReferenceAddRef ) );

        // State: [SEARCH_RESULT_REFERENCE_REF_END] - Tag: <ref>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_REF_END.ordinal()].put( new Tag( "ref", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_REF_END,
                Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_REF_END, searchResultReferenceAddRef ) );

        // State: [SEARCH_RESULT_REFERENCE_REF_END] - Tag: </searchResultReference>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_REF_END.ordinal()].put( new Tag(
            "searchResultReference",
            Tag.END ), new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_REF_END,
            Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_LOOP, null ) );

        //==========================================================
        //  Transitions concerning : SEARCH RESULT REFERENCE LOOP
        //==========================================================
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_LOOP.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [SEARCH_RESULT_REFERENCE_LOOP] - Tag: <searchResultReference>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_LOOP.ordinal()].put( new Tag(
            "searchResultReference",
            Tag.START ), new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_LOOP,
            Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE, searchResultReferenceCreation ) );

        // State: [SEARCH_RESULT_REFERENCE_LOOP] - Tag: <searchResultDone>
        super.transitions[Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_LOOP.ordinal()].put( new Tag( "searchResultDone",
            Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SEARCH_RESULT_REFERENCE_LOOP, Dsmlv2StatesEnum.LDAP_RESULT,
                searchResultDoneCreation ) );

        //------------------------------------------ handle SOAP envelopes --------------------------
        super.transitions[Dsmlv2StatesEnum.SOAP_ENVELOPE_START_TAG.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SOAP_HEADER_START_TAG.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SOAP_HEADER_END_TAG.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SOAP_BODY_START_TAG.ordinal()] = new HashMap<Tag, GrammarTransition>();
        super.transitions[Dsmlv2StatesEnum.SOAP_BODY_END_TAG.ordinal()] = new HashMap<Tag, GrammarTransition>();

        super.transitions[Dsmlv2StatesEnum.GRAMMAR_END.ordinal()] = new HashMap<Tag, GrammarTransition>();

        // State: [INIT_GRAMMAR_STATE] - Tag: <envelope>
        super.transitions[Dsmlv2StatesEnum.INIT_GRAMMAR_STATE.ordinal()].put( new Tag( "envelope", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.INIT_GRAMMAR_STATE, Dsmlv2StatesEnum.SOAP_ENVELOPE_START_TAG,
                null ) );

        // state: [SOAP_ENVELOPE_START_TAG] -> Tag: <header>
        super.transitions[Dsmlv2StatesEnum.SOAP_ENVELOPE_START_TAG.ordinal()].put( new Tag( "header", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SOAP_ENVELOPE_START_TAG, Dsmlv2StatesEnum.SOAP_HEADER_START_TAG,
                ParserUtils.READ_SOAP_HEADER ) );

        // state: [SOAP_HEADER_START_TAG] -> Tag: </header>
        super.transitions[Dsmlv2StatesEnum.SOAP_HEADER_START_TAG.ordinal()]
            .put( new Tag( "header", Tag.END ),
                new GrammarTransition( Dsmlv2StatesEnum.SOAP_HEADER_START_TAG, Dsmlv2StatesEnum.SOAP_HEADER_END_TAG,
                    null ) );

        // state: [SOAP_HEADER_END_TAG] -> Tag: <body>
        super.transitions[Dsmlv2StatesEnum.SOAP_HEADER_END_TAG.ordinal()].put( new Tag( "body", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SOAP_HEADER_END_TAG, Dsmlv2StatesEnum.SOAP_BODY_START_TAG, null ) );

        // state: [SOAP_BODY_START_TAG] -> Tag: <batchResponse>
        super.transitions[Dsmlv2StatesEnum.SOAP_BODY_START_TAG.ordinal()].put( new Tag( "batchResponse", Tag.START ),
            new GrammarTransition( Dsmlv2StatesEnum.SOAP_BODY_START_TAG, Dsmlv2StatesEnum.BATCH_RESPONSE_LOOP,
                batchResponseCreation ) );

        // the optional transition if no soap header is present
        // state: [SOAP_ENVELOPE_START_TAG] -> Tag: <body>
        super.transitions[Dsmlv2StatesEnum.SOAP_ENVELOPE_START_TAG.ordinal()]
            .put( new Tag( "body", Tag.START ),
                new GrammarTransition( Dsmlv2StatesEnum.SOAP_ENVELOPE_START_TAG, Dsmlv2StatesEnum.SOAP_BODY_START_TAG,
                    null ) );

        // the below two transitions are a bit unconventional, technically the container's state is set to GRAMMAR_END
        // when the </batchRequest> tag is encountered by the parser and the corresponding action gets executed but in
        // a SOAP envelop we still have two more end tags(</body> and </envelope>) are left so we set those corresponding
        // current and next transition states always to GRAMMAR_END
        super.transitions[Dsmlv2StatesEnum.GRAMMAR_END.ordinal()].put( new Tag( "body", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.GRAMMAR_END, Dsmlv2StatesEnum.GRAMMAR_END, null ) );

        super.transitions[Dsmlv2StatesEnum.GRAMMAR_END.ordinal()].put( new Tag( "envelope", Tag.END ),
            new GrammarTransition( Dsmlv2StatesEnum.GRAMMAR_END, Dsmlv2StatesEnum.GRAMMAR_END, null ) );

        //------------------------------------------
    }


    /**
     * Get the instance of this grammar
     * 
     * @return
     *      an instance on this grammar
     */
    public static Dsmlv2ResponseGrammar getInstance()
    {
        return instance;
    }


    /**
     * Creates a Control Value parsing the current node and adds it to the given parent 
     * @param container the DSMLv2Container
     * @param parent the parent 
     * @throws XmlPullParserException When the parsing fails
     */
    private void createAndAddControlValue( Dsmlv2Container container,
        AbstractDsmlMessageDecorator<? extends Message> parent )
        throws XmlPullParserException
    {
        DsmlControl<? extends Control> control = parent.getCurrentControl();

        XmlPullParser xpp = container.getParser();
        try
        {
            // We have to catch the type Attribute Value before going to the next Text node
            String typeValue = ParserUtils.getXsiTypeAttributeValue( xpp );

            // Getting the value
            String nextText = xpp.nextText();

            if ( !Strings.isEmpty( nextText ) )
            {
                if ( ParserUtils.isBase64BinaryValue( xpp, typeValue ) )
                {
                    control.setValue( Base64.decode( nextText.trim().toCharArray() ) );
                }
                else
                {
                    control.setValue( Strings.getBytesUtf8( nextText.trim() ) );
                }
            }
        }
        catch ( IOException ioe )
        {
            throw new XmlPullParserException( I18n.err( I18n.ERR_03008_UNEXPECTED_ERROR, ioe.getMessage() ), xpp, ioe );
        }
    }
    
    
    /**
     * Creates a Control parsing the current node and adds it to the given parent 
     * @param container the DSMLv2Container
     * @param parent the parent 
     * @throws XmlPullParserException When the parsing fails
     */
    private void createAndAddControl( Dsmlv2Container container,
        AbstractDsmlMessageDecorator<? extends Message> parent ) throws XmlPullParserException
    {
        CodecControl<? extends Control> control;

        XmlPullParser xpp = container.getParser();

        // Checking and adding the Control's attributes
        String attributeValue;
        // TYPE
        attributeValue = xpp.getAttributeValue( "", "type" );

        if ( attributeValue != null )
        {
            if ( !Oid.isOid( attributeValue ) )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03006_INCORRECT_TYPE_ATTRIBUTE_VALUE ), xpp, null );
            }

            control = container.getLdapCodecService().newControl( new OpaqueControl( attributeValue ) );
            parent.addControl( control );
        }
        else
        {
            throw new XmlPullParserException( I18n.err( I18n.ERR_03005_REQUIRE_ATTRIBUTE_TYPE ), xpp, null );
        }
        
        // CRITICALITY
        attributeValue = xpp.getAttributeValue( "", "criticality" );

        if ( attributeValue != null )
        {
            if ( "true".equals( attributeValue ) )
            {
                control.setCritical( true );
            }
            else if ( "false".equals( attributeValue ) )
            {
                control.setCritical( false );
            }
            else
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03007_INCORRECT_CRITICALITY_VALUE ), xpp, null );
            }
        }
    }
}
