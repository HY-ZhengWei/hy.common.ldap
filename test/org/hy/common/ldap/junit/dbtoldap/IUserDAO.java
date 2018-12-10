package org.hy.common.ldap.junit.dbtoldap;

import java.util.List;

import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.annotation.Xsql;





/**
 * 用户信息的数据库操作DAO
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-06
 * @version     v1.0
 */
@Xjava(id="UserDAO" ,value=XType.XSQL)
public interface IUserDAO
{
    
    /**
     * 从关系型数据库中查询所有用户信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     *
     * @return
     */
    @Xsql("XSQL_LDAP_User_QueryAll")
    public List<UserInfo> queryAll();
    
    
    
    /**
     * 从关系型数据库中查询所有用户信息（第二个数据库中的用户信息）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     *
     * @return
     */
    @Xsql("XSQL_LDAP_UnionA_User_QueryAll")
    public List<UserInfo> queryUnionA();
    
    
    
    /**
     * 从关系型数据库中查询所有用户信息（第三个数据库中的用户信息）
     * 
     * 登陆名登陆
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     *
     * @return
     */
    @Xsql("XSQL_LDAP_UnionB_01_User_QueryAll")
    public List<UserInfo> queryUnionB01();
    
    
    
    /**
     * 从关系型数据库中查询所有用户信息（第三个数据库中的用户信息）
     * 
     * 工号登陆
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-07
     * @version     v1.0
     *
     * @return
     */
    @Xsql("XSQL_LDAP_UnionB_02_User_QueryAll")
    public List<UserInfo> queryUnionB02();
    
    
    
    /**
     * 从关系型数据库中查询所有用户信息（第四个数据库中的用户信息）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-10
     * @version     v1.0
     *
     * @return
     */
    @Xsql("XSQL_LDAP_UnionC_User_QueryAll")
    public List<UserInfo> queryUnionC();
    
}
