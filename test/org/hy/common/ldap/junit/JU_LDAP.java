package org.hy.common.ldap.junit;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.hy.common.ldap.LDAP;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;





/**
 * 测试：LDAP目录服务的操作类
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-13
 * @version     v1.0
 */
@Xjava(value=XType.XML)
public class JU_LDAP
{
    
    public static void main(String [] args) throws Exception
    {
        XJava.parserAnnotation(JU_LDAP.class.getName());
        
        LDAP           v_LDAP   = (LDAP)XJava.getObject("LDAP");
        LdapConnection v_Conn   = null;
        EntryCursor    v_Cursor = null;
        try
        {
            v_Conn   = v_LDAP.getConnection();
            v_Cursor = v_Conn.search(new Dn("dc=maxcrc,dc=com") ,"(objectclass=*)" ,SearchScope.SUBTREE);
            
            while ( v_Cursor.next() )
            {
                Entry v_Entry = v_Cursor.get();
                
                if ( v_Entry != null )
                {
                    System.out.println(v_Entry);
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            LDAP.closeCursor(    v_Cursor);
            LDAP.closeConnection(v_Conn);
        }
        
        
        
        User v_User = new User();
        v_User.setName(    "ZhengWei");
        v_User.setPassword("1234567890");
        
        v_LDAP.addEntry("ou=ZhengWei,dc=maxcrc,dc=com" ,v_User);
    }
    
}
