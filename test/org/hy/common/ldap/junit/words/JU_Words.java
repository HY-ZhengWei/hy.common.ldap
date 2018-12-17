package org.hy.common.ldap.junit.words;

import org.hy.common.ldap.LDAP;
import org.hy.common.ldap.junit.words.bean.Word;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试单元 
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-17
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_Words
{
    private static boolean $isInit = false;
    
    
    
    public JU_Words() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(JU_Words.class.getName());
        }
    }
    
    
    
    @Test
    public void test_Search()
    {
        LDAP v_LDAP = (LDAP)XJava.getObject("LDAP");
        Word v_Word = (Word)v_LDAP.queryEntry("en=toe,ou=words,dc=hy,dc=com");
        
        System.out.println(v_Word.getCreateTime().getFull());
    }
    
}
