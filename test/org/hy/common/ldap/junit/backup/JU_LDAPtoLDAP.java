package org.hy.common.ldap.junit.backup;

import java.util.List;

import org.hy.common.Help;
import org.hy.common.ldap.LDAP;
import org.hy.common.ldap.junit.dbtoldap.UserInfo;
import org.hy.common.xml.XJava;
import org.hy.common.xml.plugins.AppInitConfig;
import org.junit.Test;





/**
 * 测试单元：备份LDAP01的数据到LDAP02服务上
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-29
 * @version     v1.0
 */
public class JU_LDAPtoLDAP extends AppInitConfig
{
    
    private static boolean $Init = false;
    
    
    
    public JU_LDAPtoLDAP()
    {
        this.init();
    }
    
    
    
    /**
     * 加载配置
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-29
     * @version     v1.0
     */
    private synchronized void init()
    {
        if ( !$Init )
        {
            $Init = true;
            
            try
            {
                this.init("sys.LDAP01.Config.xml");
                this.init("sys.LDAP02.Config.xml");
                this.init("org.hy.common.ldap.junit.dbtoldap");
            }
            catch (Exception exce)
            {
                System.out.println(exce.getMessage());
                exce.printStackTrace();
            }
        }
    }
    
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_Backup()
    {
        LDAP     v_LDAP01   = (LDAP)XJava.getObject("LDAP01");
        LDAP     v_LDAP02   = (LDAP)XJava.getObject("LDAP02");
        UserInfo v_UserInfo = new UserInfo();
        
        v_UserInfo.setUserID("ou=users,dc=wzyb,dc=com");
        
        List<UserInfo> v_Datas = (List<UserInfo>)v_LDAP01.searchEntrys(v_UserInfo);
        int            v_Count = 0;
        
        if ( !Help.isNull(v_Datas) )
        {
            v_Count = v_LDAP02.addEntrys(v_Datas);
        }
        
        System.out.println("应备份 " + v_Datas.size() + " 条数据，实际成功备份 " + v_Count + " 条数据。");
    }
    
}
