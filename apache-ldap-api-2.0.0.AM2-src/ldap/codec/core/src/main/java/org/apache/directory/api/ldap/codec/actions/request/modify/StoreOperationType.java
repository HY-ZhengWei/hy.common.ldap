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
package org.apache.directory.api.ldap.codec.actions.request.modify;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoder;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapCodecConstants;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.ModifyRequestDecorator;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the ModificationRequest operation type
 * <pre>
 * ModifyRequest ::= [APPLICATION 6] SEQUENCE {
 *     ...
 *     modification SEQUENCE OF SEQUENCE {
 *         operation  ENUMERATED {
 *             ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreOperationType extends GrammarAction<LdapMessageContainer<ModifyRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreOperationType.class );

    /**
     * Instantiates a new action.
     */
    public StoreOperationType()
    {
        super( "Store Modify request operation type" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<ModifyRequestDecorator> container ) throws DecoderException
    {
        ModifyRequestDecorator modifyRequestDecorator = container.getMessage();

        TLV tlv = container.getCurrentTLV();

        // Decode the operation type
        int operation = 0;

        try
        {
            operation = IntegerDecoder.parse( tlv.getValue(), 0, 2 );
        }
        catch ( IntegerDecoderException ide )
        {
            String msg = I18n.err( I18n.ERR_05124_INVALID_OPERATION, Strings.dumpBytes( tlv.getValue().getData() ) );
            LOG.error( msg );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( msg, ide );
        }

        // Store the current operation.
        modifyRequestDecorator.setCurrentOperation( operation );

        if ( LOG.isDebugEnabled() )
        {
            switch ( operation )
            {
                case LdapCodecConstants.OPERATION_ADD:
                    LOG.debug( I18n.msg( I18n.MSG_05133_MODIFY_OPERATION, "ADD" ) );
                    break;

                case LdapCodecConstants.OPERATION_DELETE:
                    LOG.debug( I18n.msg( I18n.MSG_05133_MODIFY_OPERATION, "DELETE" ) );
                    break;

                case LdapCodecConstants.OPERATION_REPLACE:
                    LOG.debug( I18n.msg( I18n.MSG_05133_MODIFY_OPERATION, "REPLACE" ) );
                    break;

                default:
                    LOG.debug( I18n.msg( I18n.MSG_05133_MODIFY_OPERATION, "UNKNOWN" ) );
            }
        }
    }
}
