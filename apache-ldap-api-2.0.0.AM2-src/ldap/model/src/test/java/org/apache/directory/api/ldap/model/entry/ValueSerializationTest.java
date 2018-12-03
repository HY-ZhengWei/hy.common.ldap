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
package org.apache.directory.api.ldap.model.entry;


import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.Normalizer;
import org.apache.directory.api.ldap.model.schema.PrepareString;
import org.apache.directory.api.ldap.model.schema.comparators.ByteArrayComparator;
import org.apache.directory.api.ldap.model.schema.comparators.StringComparator;
import org.apache.directory.api.ldap.model.schema.normalizers.DeepTrimToLowerNormalizer;
import org.apache.directory.api.ldap.model.schema.syntaxCheckers.OctetStringSyntaxChecker;
import org.apache.directory.api.util.Strings;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Test the Value Serialization
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class ValueSerializationTest
{
    private static byte[] data = new byte[]
        { 0x01, 0x02, 0x03, 0x04 };
    Value bv1 = new Value( data );
    Value bv2 = new Value( Strings.EMPTY_BYTES );
    Value bv3 = new Value( ( byte[] ) null );
    Value bv1n = new Value( data );
    Value bv2n = new Value( Strings.EMPTY_BYTES );
    Value bv3n = new Value( ( byte[] ) null );
    Value sv1 = new Value( "test" );
    Value sv2 = new Value( "" );
    Value sv3 = new Value( ( String ) null );
    Value sv1n = new Value( "test" );
    Value sv2n = new Value( "" );
    Value sv3n = new Value( ( String ) null );

    private EntryUtils.S sb;
    private EntryUtils.AT atb;
    private EntryUtils.MR mrb;

    private EntryUtils.S ss;
    private EntryUtils.AT ats;
    private EntryUtils.MR mrs;


    /**
     * Initialize an AttributeType and the associated MatchingRule 
     * and Syntax
     */
    @Before
    public void initAT()
    {
        sb = new EntryUtils.S( "1.1.1.1", false );
        sb.setSyntaxChecker( OctetStringSyntaxChecker.INSTANCE );
        mrb = new EntryUtils.MR( "1.1.2.1" );
        mrb.setSyntax( sb );

        mrb.setLdapComparator( new ByteArrayComparator( "1.1.1" ) );
        mrb.setNormalizer( new Normalizer( "1.1.1" )
        {
            public static final long serialVersionUID = 1L;


            public String normalize( String value ) throws LdapException
            {
                return normalize( value, PrepareString.AssertionType.ATTRIBUTE_VALUE );
            }


            public String normalize( String value, PrepareString.AssertionType assertionType ) throws LdapException
            {
                byte[] val = Strings.getBytesUtf8( value );
                // each byte will be changed to be > 0, and spaces will be trimmed
                byte[] newVal = new byte[val.length];
                int i = 0;

                for ( byte b : val )
                {
                    newVal[i++] = ( byte ) ( b & 0x007F );
                }

                return Strings.utf8ToString( Strings.trim( newVal ) );
            }
        } );

        atb = new EntryUtils.AT( "1.1.3.1" );
        atb.setEquality( mrb );
        atb.setOrdering( mrb );
        atb.setSubstring( mrb );
        atb.setSyntax( sb );

        ss = new EntryUtils.S( "1.1.1.1", true );
        ss.setSyntaxChecker( OctetStringSyntaxChecker.INSTANCE );
        mrs = new EntryUtils.MR( "1.1.2.1" );
        mrs.setSyntax( ss );
        mrs.setLdapComparator( new StringComparator( "1.1.2.1" ) );
        mrs.setNormalizer( new DeepTrimToLowerNormalizer( "1.1.2.1" ) );
        ats = new EntryUtils.AT( "1.1.3.1" );
        ats.setEquality( mrs );
        ats.setOrdering( mrs );
        ats.setSubstring( mrs );
        ats.setSyntax( ss );
    }


    @Test
    public void testBinaryValueWithDataSerialization() throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );

        bv1.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value bvDeser = new Value( ( AttributeType ) null );
        bvDeser.readExternal( in );

        assertEquals( bv1, bvDeser );
    }


    @Test
    public void testBinaryValueWithEmptyDataSerialization() throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );

        bv2.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value bvDeser = new Value( ( AttributeType ) null );
        bvDeser.readExternal( in );

        assertEquals( bv2, bvDeser );
    }


    @Test
    public void testBinaryValueNoDataSerialization() throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );

        bv3.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value bvDeser = new Value( ( AttributeType ) null );
        bvDeser.readExternal( in );

        assertEquals( bv3, bvDeser );
    }


    @Test
    public void testStringValueWithDataSerialization() throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );

        sv1.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value svDeser = new Value( ( AttributeType ) null );
        svDeser.readExternal( in );

        assertEquals( sv1, svDeser );
    }


    @Test
    public void testStringValueWithEmptyDataSerialization() throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );

        sv2.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value svDeser = new Value( ( AttributeType ) null );
        svDeser.readExternal( in );

        assertEquals( sv2, svDeser );
    }


    @Test
    public void testStringValueNoDataSerialization() throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );

        sv3.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value svDeser = new Value( ( AttributeType ) null );
        svDeser.readExternal( in );

        assertEquals( sv3, svDeser );
    }


    @Test
    public void testBinaryValueWithDataNormalizedSerialization() throws IOException, LdapException,
        ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );
        Value value = new Value( atb, bv1n );

        value.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value bvDeser = new Value( ( AttributeType ) null );
        bvDeser.readExternal( in );

        assertEquals( value, bvDeser );
    }


    @Test
    public void testBinaryValueWithEmptyDataNormalizedSerialization() throws IOException, LdapException,
        ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );
        Value value = new Value( atb, bv2n );

        value.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value bvDeser = new Value( ( AttributeType ) null );
        bvDeser.readExternal( in );

        assertEquals( value, bvDeser );
    }


    @Test
    public void testBinaryValueNoDataNormalizedSerialization() throws IOException, LdapException,
        ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );
        Value value = new Value( atb, bv3n );

        value.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value bvDeser = new Value( ( AttributeType ) null );
        bvDeser.readExternal( in );

        assertEquals( value, bvDeser );
    }


    @Test
    public void testStringValueWithDataNormalizedSerialization() throws IOException, LdapException,
        ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );
        Value value = new Value( ats, sv1n );

        value.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value svDeser = new Value( ats );
        svDeser.readExternal( in );

        assertEquals( value, svDeser );
    }


    @Test
    public void testStringValueWithEmptyDataNormalizedSerialization() throws IOException, LdapException,
        ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );
        Value value = new Value( ats, sv2n );

        value.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value svDeser = new Value( ats );
        svDeser.readExternal( in );

        assertEquals( value, svDeser );
    }


    @Test
    public void testStringValueNoDataNormalizedSerialization() throws IOException, LdapException,
        ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );
        Value value = new Value( ats, sv3n );

        value.writeExternal( out );

        ObjectInputStream in = null;

        byte[] data = baos.toByteArray();
        in = new ObjectInputStream( new ByteArrayInputStream( data ) );

        Value svDeser = new Value( ats );
        svDeser.readExternal( in );

        assertEquals( value, svDeser );
    }


    @Test
    public void testStringValueWithDataNormalizedSerializationBytes() throws IOException, LdapException,
        ClassNotFoundException
    {
        byte[] buffer = new byte[128];
        Value value = new Value( ats, sv1n );

        int pos = value.serialize( buffer, 0 );

        Value svDeser = new Value( ats );

        int pos2 = svDeser.deserialize( buffer, 0 );

        assertEquals( pos, pos2 );
        assertEquals( value, svDeser );
    }


    @Test
    public void testStringValueWithEmptyDataNormalizedSerializationBytes() throws IOException, LdapException,
        ClassNotFoundException
    {
        byte[] buffer = new byte[128];
        Value value = new Value( ats, sv2n );

        int pos = value.serialize( buffer, 0 );

        Value svDeser = new Value( ats );

        int pos2 = svDeser.deserialize( buffer, 0 );

        assertEquals( pos, pos2 );
        assertEquals( value, svDeser );
    }


    @Test
    public void testStringValueNoDataNormalizedSerializationBytes() throws IOException, LdapException,
        ClassNotFoundException
    {
        byte[] buffer = new byte[128];
        Value value = new Value( ats, sv3n );

        int pos = value.serialize( buffer, 0 );

        Value svDeser = new Value( ats );
        int pos2 = svDeser.deserialize( buffer, 0 );

        assertEquals( pos, pos2 );
        assertEquals( value, svDeser );
    }


    @Test
    public void testStringValueWithDataSerializationBytes() throws IOException, ClassNotFoundException, LdapInvalidAttributeValueException
    {
        byte[] buffer = new byte[128];

        int pos = sv1.serialize( buffer, 0 );

        Value svDeser = new Value( ( AttributeType ) null );

        int pos2 = svDeser.deserialize( buffer, 0 );

        assertEquals( pos, pos2 );
        assertEquals( sv1, svDeser );
    }


    @Test
    public void testStringValueWithEmptyDataSerializationBytes() throws IOException, ClassNotFoundException, LdapInvalidAttributeValueException
    {
        byte[] buffer = new byte[128];

        int pos = sv2.serialize( buffer, 0 );

        Value svDeser = new Value( ( AttributeType ) null );

        int pos2 = svDeser.deserialize( buffer, 0 );

        assertEquals( pos, pos2 );
        assertEquals( sv2, svDeser );
    }


    @Test
    public void testStringValueNoDataSerializationBytes() throws IOException, ClassNotFoundException, LdapInvalidAttributeValueException
    {
        byte[] buffer = new byte[128];

        int pos = sv3.serialize( buffer, 0 );

        Value svDeser = new Value( ( AttributeType ) null );

        int pos2 = svDeser.deserialize( buffer, 0 );

        assertEquals( pos, pos2 );
        assertEquals( sv3, svDeser );
    }


    @Ignore
    @Test
    public void testStringValueWithDataNormalizedSerializationPerf() throws IOException, LdapException,
        ClassNotFoundException
    {
        Value value = new Value( ats, sv1n );
        Value svDeser = new Value( ats );

        long t0 = System.currentTimeMillis();

        for ( int i = 0; i < 10000000; i++ )
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream( baos );
            value.writeExternal( out );
            out.close();
            byte[] data = baos.toByteArray();
            ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream( data ) );
            svDeser.readExternal( in );
            in.close();
        }

        long t1 = System.currentTimeMillis();

        System.out.println( "Delta ser slow = " + ( t1 - t0 ) );
    }


    @Ignore
    @Test
    public void testStringValueWithDataNormalizedSerializationBytesPerf() throws IOException, LdapException,
        ClassNotFoundException
    {
        Value value = new Value( ats, sv1n );
        Value svDeser = new Value( ats );

        long t0 = System.currentTimeMillis();

        for ( int i = 0; i < 10000000; i++ )
        {
            byte[] buffer = new byte[128];
            value.serialize( buffer, 0 );
            svDeser.deserialize( buffer, 0 );
        }

        long t1 = System.currentTimeMillis();

        System.out.println( "Delta ser fast = " + ( t1 - t0 ) );
    }
}
