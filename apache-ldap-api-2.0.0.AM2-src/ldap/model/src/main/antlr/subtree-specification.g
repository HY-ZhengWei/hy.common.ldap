header
{
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


package org.apache.directory.api.ldap.model.subtree;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.filter.ExprNode;
import org.apache.directory.api.ldap.model.filter.LeafNode;
import org.apache.directory.api.ldap.model.filter.BranchNode;
import org.apache.directory.api.ldap.model.filter.AndNode;
import org.apache.directory.api.ldap.model.filter.OrNode;
import org.apache.directory.api.ldap.model.filter.NotNode;
import org.apache.directory.api.ldap.model.filter.EqualityNode;
import org.apache.directory.api.ldap.model.filter.FilterParser;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.subtree.SubtreeSpecification;
import org.apache.directory.api.ldap.model.subtree.SubtreeSpecificationModifier;
import org.apache.directory.api.ldap.model.schema.NormalizerMappingResolver;
import org.apache.directory.api.ldap.model.schema.normalizers.OidNormalizer;
import org.apache.directory.api.util.ComponentsMonitor;
import org.apache.directory.api.util.OptionalComponentsMonitor;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.AttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
}


// ----------------------------------------------------------------------------
// parser class definition
// ----------------------------------------------------------------------------

/**
 * The antlr generated subtree specification parser.
 *
 * @see <a href="http://www.faqs.org/rfcs/rfc3672.html">RFC 3672</a>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
class AntlrSubtreeSpecificationParser extends Parser;


// ----------------------------------------------------------------------------
// parser options
// ----------------------------------------------------------------------------

options
{
    k = 1;
    defaultErrorHandler = false;
}


// ----------------------------------------------------------------------------
// parser initialization
// ----------------------------------------------------------------------------

{
    private static final Logger log = LoggerFactory.getLogger( AntlrSubtreeSpecificationParser.class );
    
    private NormalizerMappingResolver resolver;
    
    private Set<Dn> chopBeforeExclusions = null;
    private Set<Dn> chopAfterExclusions = null;

    private SubtreeSpecificationModifier ssModifier = null;
    
    /** The schemaManager */
    private SchemaManager schemaManager;
    
    /** The ObjectClass AT */
    AttributeType OBJECT_CLASS_AT;
    
    private ComponentsMonitor subtreeSpecificationComponentsMonitor = null;
    
    

    /**
     * Initialize the parser
     *
     * @param schemaManager the SchemaManager instance
     */
    public void init( SchemaManager schemaManager )
    {
        this.schemaManager = schemaManager;
        
        if ( schemaManager != null )
        {
            OBJECT_CLASS_AT = schemaManager.getAttributeType( SchemaConstants.OBJECT_CLASS_AT );
        }
    }
    
    
    public void setNormalizerMappingResolver( NormalizerMappingResolver resolver )
    {
        this.resolver = resolver;
    }
    
    
    public boolean isNormalizing()
    {
        return this.resolver != null;
    }
    

    private int token2Integer( Token token ) throws RecognitionException
    {
        int i = 0;
        
        try
        {
            i = Integer.parseInt( token.getText());
        }
        catch ( NumberFormatException e )
        {
            throw new RecognitionException( "Value of INTEGER token " +
                                            token.getText() +
                                            " cannot be converted to an Integer" );
        }
        
        return i;
    }
}


// ----------------------------------------------------------------------------
// parser productions
// ----------------------------------------------------------------------------

wrapperEntryPoint returns [SubtreeSpecification ss]
{
    log.debug( "entered wrapperEntryPoint()" );
    ss = null;
    SubtreeSpecification tempSs = null;
} :
    tempSs=subtreeSpecification "end"
    {
        ss = tempSs;
    }
    ;

subtreeSpecification returns [SubtreeSpecification ss]
{
    log.debug( "entered subtreeSpecification()" );
    // clear out ss, ssModifier, subtreeSpecificationComponentsMonitor,
    // chopBeforeExclusions and chopAfterExclusions
    // in case something is left from the last parse
    ss = null;
    ssModifier = new SubtreeSpecificationModifier();
    subtreeSpecificationComponentsMonitor = new OptionalComponentsMonitor( 
            new String [] { "base", "specificExclusions", "minimum", "maximum", "specificationFilter" } );
    chopBeforeExclusions = new HashSet<Dn>();
    chopAfterExclusions = new HashSet<Dn>();
    // always create a new filter parser in case we may have some statefulness problems with it
}
    :
    OPEN_CURLY ( SP )*
        ( subtreeSpecificationComponent ( SP )*
            ( SEP ( SP )* subtreeSpecificationComponent ( SP )* )* )?
    CLOSE_CURLY
    {
        ss = ssModifier.getSubtreeSpecification();
    }
    ;

subtreeSpecificationComponent
{
    log.debug( "entered subtreeSpecification()" );
}
    :
    ss_base
    {
        subtreeSpecificationComponentsMonitor.useComponent( "base" );
    }
    | ss_specificExclusions
    {
        subtreeSpecificationComponentsMonitor.useComponent( "specificExclusions" );
    }
    | ss_minimum
    {
        subtreeSpecificationComponentsMonitor.useComponent( "minimum" );
    }
    | ss_maximum
    {
        subtreeSpecificationComponentsMonitor.useComponent( "maximum" );
    }
    | ss_specificationFilter
    {
        subtreeSpecificationComponentsMonitor.useComponent( "specificationFilter" );
    }
    ;
    exception
    catch [IllegalArgumentException e]
    {
        throw new RecognitionException( e.getMessage() );
    }

ss_base
{
    log.debug( "entered ss_base()" );
    Dn base = null;
}
    :
    ID_base ( SP )+ base=distinguishedName
    {
        ssModifier.setBase( base );
    }
    ;

ss_specificExclusions
{
    log.debug( "entered ss_specificExclusions()" );
}
    :
    ID_specificExclusions ( SP )+ specificExclusions
    {
        ssModifier.setChopBeforeExclusions( chopBeforeExclusions );
        ssModifier.setChopAfterExclusions( chopAfterExclusions );
    }
    ;

specificExclusions
{
    log.debug( "entered specificExclusions()" );
}
    :
    OPEN_CURLY ( SP )*
        ( specificExclusion ( SP )*
            ( SEP ( SP )* specificExclusion ( SP )* )*
        )?
    CLOSE_CURLY
    ;

specificExclusion
{
    log.debug( "entered specificExclusion()" );
}
    :
    chopBefore | chopAfter
    ;

chopBefore
{
    log.debug( "entered chopBefore()" );
    Dn chopBeforeExclusion = null;
}
    :
    ID_chopBefore ( SP )* COLON ( SP )* chopBeforeExclusion=distinguishedName
    {
        chopBeforeExclusions.add( chopBeforeExclusion );
    }
    ;

chopAfter
{
    log.debug( "entered chopAfter()" );
    Dn chopAfterExclusion = null;
}
    :
    ID_chopAfter ( SP )* COLON ( SP )* chopAfterExclusion=distinguishedName
    {
        chopAfterExclusions.add( chopAfterExclusion );
    }
    ;

ss_minimum
{
    log.debug( "entered ss_minimum()" );
    int minimum = 0;
}
    :
    ID_minimum ( SP )+ minimum=baseDistance
    {
        ssModifier.setMinBaseDistance( minimum );
    }
    ;

ss_maximum
{
    log.debug( "entered ss_maximum()" );
    int maximum = 0;
}
    :
    ID_maximum ( SP )+ maximum=baseDistance
    {
        ssModifier.setMaxBaseDistance( maximum );
    }
    ;

ss_specificationFilter
{
    log.debug( "entered ss_specificationFilter()" );
    ExprNode filterExpr = null;
}
    :
    ID_specificationFilter 
    ( SP )+ 
    (
        ( filterExpr=refinement )
        |
        ( filterExpr=filter )
    )
    { ssModifier.setRefinement( filterExpr ); }
    ;
    
    
filter returns [ ExprNode filterExpr = null ]
{
    log.debug( "entered filter()" );
}
    :
    ( filterToken:FILTER { filterExpr=FilterParser.parse( schemaManager, filterToken.getText() ); } )
    ;
    exception
    catch [Exception e]
    {
        throw new RecognitionException( "filterParser failed. " + e.getMessage() );
    }
    
distinguishedName returns [ Dn name ]
{
    log.debug( "entered distinguishedName()" );
    name = null;
}
    :
    token:SAFEUTF8STRING
    {
        name = new Dn( schemaManager, token.getText() );
        
        log.debug( "recognized a DistinguishedName: " + token.getText() );
    }
    ;
    exception
    catch [Exception e]
    {
        throw new RecognitionException( "dnParser failed for " + token.getText() + " " + e.getMessage() );
    }

baseDistance returns [ int distance ]
{
    log.debug( "entered baseDistance()" );
    distance = 0;
}
    :
    token:INTEGER
    {
        distance = token2Integer( token );
    }
    ;

oid returns [ String result ]
{
    log.debug( "entered oid()" );
    result = null;
    Token token = null;
}
    :
    { token = LT( 1 ); } // an interesting trick goes here ;-)
    ( DESCR | NUMERICOID )
    {
        result = token.getText();
        log.debug( "recognized an oid: " + result );
    }
    ;

refinement returns [ ExprNode node ]
{
    log.debug( "entered refinement()" );
    node = null;
}
    :
    node=item | node=and | node=or | node=not
    ;

item returns [ LeafNode node ]
{
    log.debug( "entered item()" );
    node = null;
    String oid = null;
    ObjectClass objectClass;
}
    :
    ID_item ( SP )* COLON ( SP )* oid=oid
    {
        try
        {
            objectClass = schemaManager.lookupObjectClassRegistry( oid );
        }
        catch ( LdapException le )
        {
              // The oid does not exist
              // TODO : deal with such an exception
        }
        
        node = new EqualityNode( OBJECT_CLASS_AT, new Value( oid ) );
    }
    ;

and returns [ BranchNode node ]
{
    log.debug( "entered and()" );
    node = null;
    List<ExprNode> children = null; 
}
    :
    ID_and ( SP )* COLON ( SP )* children=refinements
    {
        node = new AndNode( children );
    }
    ;

or returns [ BranchNode node ]
{
    log.debug( "entered or()" );
    node = null;
    List<ExprNode> children = null; 
}
    :
    ID_or ( SP )* COLON ( SP )* children=refinements
    {
        node = new OrNode( children );
    }
    ;

not returns [ BranchNode node ]
{
    log.debug( "entered not()" );
    node = null;
    ExprNode child = null;
}
    :
    ID_not ( SP )* COLON ( SP )* child=refinement
    {
        node = new NotNode( child );
    }
    ;

refinements returns [ List<ExprNode> children ]
{
    log.debug( "entered refinements()" );
    children = null;
    ExprNode child = null;
    List<ExprNode> tempChildren = new ArrayList<ExprNode>();
}
    :
    OPEN_CURLY ( SP )*
    (
        child=refinement ( SP )*
        {
            tempChildren.add( child );
        }
        ( SEP ( SP )* child=refinement ( SP )*
        {
            tempChildren.add( child );
        } )*
    )? CLOSE_CURLY
    {
        children = tempChildren;
    }
    ;


// ----------------------------------------------------------------------------
// lexer class definition
// ----------------------------------------------------------------------------

/**
 * The parser's primary lexer.
 *
 * @see <a href="http://www.faqs.org/rfcs/rfc3672.html">RFC 3672</a>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
class AntlrSubtreeSpecificationLexer extends Lexer;


// ----------------------------------------------------------------------------
// lexer options
// ----------------------------------------------------------------------------

options
{
    k = 5;

    charVocabulary = '\u0001'..'\u0127';
}

tokens
{
    ID_base = "base";
    ID_specificExclusions = "specificExclusions";
    ID_chopBefore = "chopBefore";
    ID_chopAfter = "chopAfter";
    ID_minimum = "minimum";
    ID_maximum = "maximum";
    ID_specificationFilter = "specificationFilter";
    ID_item = "item";
    ID_and = "and";
    ID_or = "or";
    ID_not = "not";
}


//----------------------------------------------------------------------------
// lexer initialization
//----------------------------------------------------------------------------

{
    private static final Logger log = LoggerFactory.getLogger( AntlrSubtreeSpecificationLexer.class );
}


// ----------------------------------------------------------------------------
// attribute description lexer rules from models
// ----------------------------------------------------------------------------

SP : ' ';

COLON : ':' { log.debug( "matched COLON(':')" ); } ;

OPEN_CURLY : '{' { log.debug( "matched LBRACKET('{')" ); } ;

CLOSE_CURLY : '}' { log.debug( "matched RBRACKET('}')" ); } ;

SEP : ',' { log.debug( "matched SEP(',')" ); } ;

SAFEUTF8STRING : '"'! ( SAFEUTF8CHAR )* '"'! { log.debug( "matched SAFEUTF8CHAR: \"" + getText() + "\"" ); } ;

DESCR : ALPHA ( ALPHA | DIGIT | '-' )* { log.debug( "matched DESCR" ); } ;

INTEGER_OR_NUMERICOID
    :
    ( INTEGER DOT ) => NUMERICOID
    {
        $setType( NUMERICOID );
    }
    |
    INTEGER
    {
        $setType( INTEGER );
    }
    ;

protected INTEGER: DIGIT | ( LDIGIT ( DIGIT )+ ) { log.debug( "matched INTEGER: " + getText() ); } ;

protected NUMERICOID: INTEGER ( DOT INTEGER )+ { log.debug( "matched NUMERICOID: " + getText() ); } ;

protected DOT: '.' ;

protected DIGIT: '0' | LDIGIT ;

protected LDIGIT: '1'..'9' ;

protected ALPHA: 'A'..'Z' | 'a'..'z' ;

// This is all messed up - could not figure out how to get antlr to represent
// the safe UTF-8 character set from RFC 3642 for production SafeUTF8Character

protected SAFEUTF8CHAR:
    '\u0001'..'\u0021' |
    '\u0023'..'\u007F' |
    '\u00c0'..'\u00d6' |
    '\u00d8'..'\u00f6' |
    '\u00f8'..'\u00ff' |
    '\u0100'..'\u1fff' |
    '\u3040'..'\u318f' |
    '\u3300'..'\u337f' |
    '\u3400'..'\u3d2d' |
    '\u4e00'..'\u9fff' |
    '\uf900'..'\ufaff' ;

FILTER : '(' ( ( '&' (SP)* (FILTER)+ ) | ( '|' (SP)* (FILTER)+ ) | ( '!' (SP)* FILTER ) | FILTER_VALUE ) ')' (SP)* ;

protected FILTER_VALUE : (options{greedy=true;}: ~( ')' | '(' | '&' | '|' | '!' ) ( ~(')') )* ) ;
