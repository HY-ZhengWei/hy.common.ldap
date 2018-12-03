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
package org.apache.directory.api.ldap.codec.api;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.api.ldap.codec.decorators.AbandonRequestDecorator;
import org.apache.directory.api.ldap.codec.decorators.AddRequestDecorator;
import org.apache.directory.api.ldap.codec.decorators.AddResponseDecorator;
import org.apache.directory.api.ldap.codec.decorators.BindRequestDecorator;
import org.apache.directory.api.ldap.codec.decorators.BindResponseDecorator;
import org.apache.directory.api.ldap.codec.decorators.CompareRequestDecorator;
import org.apache.directory.api.ldap.codec.decorators.CompareResponseDecorator;
import org.apache.directory.api.ldap.codec.decorators.DeleteRequestDecorator;
import org.apache.directory.api.ldap.codec.decorators.DeleteResponseDecorator;
import org.apache.directory.api.ldap.codec.decorators.IntermediateResponseDecorator;
import org.apache.directory.api.ldap.codec.decorators.ModifyDnRequestDecorator;
import org.apache.directory.api.ldap.codec.decorators.ModifyDnResponseDecorator;
import org.apache.directory.api.ldap.codec.decorators.ModifyRequestDecorator;
import org.apache.directory.api.ldap.codec.decorators.ModifyResponseDecorator;
import org.apache.directory.api.ldap.codec.decorators.SearchRequestDecorator;
import org.apache.directory.api.ldap.codec.decorators.SearchResultDoneDecorator;
import org.apache.directory.api.ldap.codec.decorators.SearchResultEntryDecorator;
import org.apache.directory.api.ldap.codec.decorators.SearchResultReferenceDecorator;
import org.apache.directory.api.ldap.codec.decorators.UnbindRequestDecorator;
import org.apache.directory.api.ldap.model.message.AbandonRequest;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.CompareRequest;
import org.apache.directory.api.ldap.model.message.CompareResponse;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.DeleteRequest;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.message.IntermediateResponse;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.ModifyDnRequest;
import org.apache.directory.api.ldap.model.message.ModifyDnResponse;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchResultDone;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.message.SearchResultReference;
import org.apache.directory.api.ldap.model.message.UnbindRequest;


/**
 * A decorator for the generic LDAP Message
 *
 * @param <E> The message to decorate
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class MessageDecorator<E extends Message> implements Message, Decorator<E>
{
    /** The decorated Message */
    private final E decoratedMessage;

    /** Map of message controls using OID Strings for keys and Control values */
    private final Map<String, Control> controls;

    /** The current control */
    private CodecControl<? extends Control> currentControl;

    /** The encoded Message length */
    protected int messageLength;

    /** The length of the controls */
    private int controlsLength;

    /** The LdapCodecService */
    private final LdapApiService codec;


    /**
     * Makes a Message an Decorator object.
     * 
     * @param codec The LDAP Service instance to use
     * @param decoratedMessage The message to decorate
     */
    protected MessageDecorator( LdapApiService codec, E decoratedMessage )
    {
        this.codec = codec;
        this.decoratedMessage = decoratedMessage;
        controls = new HashMap<>();
    }


    /**
     * Gets the decorator associated with a given message
     * 
     * @param codec The LdapApiService to use
     * @param decoratedMessage The message to decorate
     * @return The decorator instance
     */
    public static MessageDecorator<? extends Message> getDecorator( LdapApiService codec, Message decoratedMessage )
    {
        if ( decoratedMessage instanceof MessageDecorator )
        {
            return ( MessageDecorator<?> ) decoratedMessage;
        }

        MessageDecorator<?> decorator;

        switch ( decoratedMessage.getType() )
        {
            case ABANDON_REQUEST:
                decorator = new AbandonRequestDecorator( codec, ( AbandonRequest ) decoratedMessage );
                break;

            case ADD_REQUEST:
                decorator = new AddRequestDecorator( codec, ( AddRequest ) decoratedMessage );
                break;

            case ADD_RESPONSE:
                decorator = new AddResponseDecorator( codec, ( AddResponse ) decoratedMessage );
                break;

            case BIND_REQUEST:
                decorator = new BindRequestDecorator( codec, ( BindRequest ) decoratedMessage );
                break;

            case BIND_RESPONSE:
                decorator = new BindResponseDecorator( codec, ( BindResponse ) decoratedMessage );
                break;

            case COMPARE_REQUEST:
                decorator = new CompareRequestDecorator( codec, ( CompareRequest ) decoratedMessage );
                break;

            case COMPARE_RESPONSE:
                decorator = new CompareResponseDecorator( codec, ( CompareResponse ) decoratedMessage );
                break;

            case DEL_REQUEST:
                decorator = new DeleteRequestDecorator( codec, ( DeleteRequest ) decoratedMessage );
                break;

            case DEL_RESPONSE:
                decorator = new DeleteResponseDecorator( codec, ( DeleteResponse ) decoratedMessage );
                break;

            case EXTENDED_REQUEST:
                decorator = codec.decorate( ( ExtendedRequest ) decoratedMessage );
                break;

            case EXTENDED_RESPONSE:
                decorator = codec.decorate( ( ExtendedResponse ) decoratedMessage );
                break;

            case INTERMEDIATE_RESPONSE:
                decorator = new IntermediateResponseDecorator( codec, ( IntermediateResponse ) decoratedMessage );
                break;

            case MODIFY_REQUEST:
                decorator = new ModifyRequestDecorator( codec, ( ModifyRequest ) decoratedMessage );
                break;

            case MODIFY_RESPONSE:
                decorator = new ModifyResponseDecorator( codec, ( ModifyResponse ) decoratedMessage );
                break;

            case MODIFYDN_REQUEST:
                decorator = new ModifyDnRequestDecorator( codec, ( ModifyDnRequest ) decoratedMessage );
                break;

            case MODIFYDN_RESPONSE:
                decorator = new ModifyDnResponseDecorator( codec, ( ModifyDnResponse ) decoratedMessage );
                break;

            case SEARCH_REQUEST:
                decorator = new SearchRequestDecorator( codec, ( SearchRequest ) decoratedMessage );
                break;

            case SEARCH_RESULT_DONE:
                decorator = new SearchResultDoneDecorator( codec, ( SearchResultDone ) decoratedMessage );
                break;

            case SEARCH_RESULT_ENTRY:
                decorator = new SearchResultEntryDecorator( codec, ( SearchResultEntry ) decoratedMessage );
                break;

            case SEARCH_RESULT_REFERENCE:
                decorator = new SearchResultReferenceDecorator( codec, ( SearchResultReference ) decoratedMessage );
                break;

            case UNBIND_REQUEST:
                decorator = new UnbindRequestDecorator( codec, ( UnbindRequest ) decoratedMessage );
                break;

            default:
                return null;
        }

        Map<String, Control> controls = decoratedMessage.getControls();

        if ( controls != null )
        {
            for ( Control control : controls.values() )
            {
                decorator.addControl( control );
            }
        }

        return decorator;
    }


    /**
     * @param controlsLength the encoded controls length
     */
    public void setControlsLength( int controlsLength )
    {
        this.controlsLength = controlsLength;
    }


    /**
     * @return the encoded controls length
     */
    public int getControlsLength()
    {
        return controlsLength;
    }


    /**
     * @param messageLength The encoded message length
     */
    public void setMessageLength( int messageLength )
    {
        this.messageLength = messageLength;
    }


    /**
     * @return The encoded message length
     */
    public int getMessageLength()
    {
        return messageLength;
    }


    /**
     * Get the current Control Object
     * 
     * @return The current Control Object
     */
    public CodecControl<? extends Control> getCurrentControl()
    {
        return currentControl;
    }


    //-------------------------------------------------------------------------
    // The Message methods
    //-------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageTypeEnum getType()
    {
        return decoratedMessage.getType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Control> getControls()
    {
        return controls;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Control getControl( String oid )
    {
        return controls.get( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasControl( String oid )
    {
        return controls.containsKey( oid );
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Message addControl( Control control )
    {
        Control decorated;
        CodecControl<? extends Control> controlDecorator;

        if ( control instanceof ControlDecorator )
        {
            controlDecorator = ( org.apache.directory.api.ldap.codec.api.CodecControl<? extends Control> ) control;
            decorated = controlDecorator.getDecorated();
        }
        else
        {
            controlDecorator = codec.newControl( control );
            decorated = control;
        }

        decoratedMessage.addControl( decorated );
        controls.put( control.getOid(), controlDecorator );
        currentControl = controlDecorator;

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Message addAllControls( Control[] controls )
    {
        for ( Control control : controls )
        {
            addControl( control );
        }

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Message removeControl( Control control )
    {
        decoratedMessage.removeControl( control );
        controls.remove( control.getOid() );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getMessageId()
    {
        return decoratedMessage.getMessageId();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object get( Object key )
    {
        return decoratedMessage.get( key );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object put( Object key, Object value )
    {
        return decoratedMessage.put( key, value );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Message setMessageId( int messageId )
    {
        decoratedMessage.setMessageId( messageId );

        return this;
    }


    /**
     * Delegates to the toString() method of the decorated Message.
     */
    @Override
    public String toString()
    {
        return decoratedMessage.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public E getDecorated()
    {
        return decoratedMessage;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LdapApiService getCodecService()
    {
        return codec;
    }
}
