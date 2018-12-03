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
package org.apache.directory.api.ldap.codec.actions.request.compare;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.ResponseCarryingException;
import org.apache.directory.api.ldap.codec.decorators.CompareRequestDecorator;
import org.apache.directory.api.ldap.model.message.CompareRequest;
import org.apache.directory.api.ldap.model.message.CompareResponseImpl;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the AssertionValue attributeDescription in a Compare Request
 * <pre>
 * CompareRequest ::= [APPLICATION 14] SEQUENCE {
 *     ...
 *     ava AttributeValueAssertion }
 *
 * AttributeValueAssertion ::= SEQUENCE {
 *     attributeDesc   AttributeDescription,
 *     ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreCompareRequestAttributeDesc extends GrammarAction<LdapMessageContainer<CompareRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreCompareRequestAttributeDesc.class );

    /**
     * Instantiates a new action.
     */
    public StoreCompareRequestAttributeDesc()
    {
        super( "Store CompareRequest assertion description" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<CompareRequestDecorator> container ) throws DecoderException
    {
        // Get the CompareRequest Object
        CompareRequest compareRequest = container.getMessage();

        // Get the Value and store it in the CompareRequest
        TLV tlv = container.getCurrentTLV();

        // We have to handle the special case of a 0 length matched
        // Dn
        if ( tlv.getLength() == 0 )
        {
            String msg = I18n.err( I18n.ERR_05118_NULL_ATTRIBUTE_DESC );
            LOG.error( msg );
            CompareResponseImpl response = new CompareResponseImpl( compareRequest.getMessageId() );

            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX,
                compareRequest.getName(), null );
        }

        String type = Strings.utf8ToString( tlv.getValue().getData() );
        compareRequest.setAttributeId( type );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05122_COMPARING_ATTRIBUTE_DESCRIPTION, compareRequest.getAttributeId() ) );
        }
    }
}
