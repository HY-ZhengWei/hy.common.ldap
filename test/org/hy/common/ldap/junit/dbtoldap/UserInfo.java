package org.hy.common.ldap.junit.dbtoldap;

import java.util.List;

import org.hy.common.Date;
import org.hy.common.ldap.annotation.Ldap;
import org.hy.common.ldap.annotation.LdapType;
import org.hy.common.xml.SerializableDef;





/**
 * 用户信息。
 * 
 * 定义ObjectClass。多类名个用英文逗号分隔
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-06
 * @version     v1.0
 */
@Ldap("inetOrgPerson ,organizationalPerson ,person ,top")
public class UserInfo extends SerializableDef
{

    private static final long serialVersionUID = -289778484008103225L;

    /** 用户ID */
    @Ldap(type=LdapType.DN)
    private String       userID;
    
    /** 用户编号 */
    @Ldap("uid")
    private String       userNo;
    
    /** 用户名称 */
    @Ldap("cn")
    private String       userName;
    
    /** 用户姓氏 */
    @Ldap("sn")
    private String       surname;
    
    /** 登陆名称 */
    private List<String> loginName;
    
    /** 登陆密码 */
    private List<String> loginPwd;
    
    /** 组织编号 */
    @Ldap("o")
    private String       groupNo;
    
    /** 组织名称 */
    @Ldap("ou")
    private String       groupName;
    
    /** 工卡编号 */
    @Ldap("employeeNumber")
    private String       cardNo;
    
    /** 最后修改时间 */
    @Ldap("description")
    private Date         lastTime;

    
    
    /**
     * 获取：用户ID
     */
    public String getUserID()
    {
        return userID;
    }

    
    /**
     * 设置：用户ID
     * 
     * @param userID 
     */
    public void setUserID(String userID)
    {
        this.userID = userID;
    }


    /**
     * 获取：用户编号
     */
    public String getUserNo()
    {
        return userNo;
    }

    
    /**
     * 设置：用户编号
     * 
     * @param userNo 
     */
    public void setUserNo(String userNo)
    {
        this.userNo = userNo;
    }

    
    /**
     * 获取：用户名称
     */
    public String getUserName()
    {
        return userName;
    }

    
    /**
     * 设置：用户名称
     * 
     * @param userName 
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    
    /**
     * 获取：用户姓氏
     */
    public String getSurname()
    {
        return surname;
    }

    
    /**
     * 设置：用户姓氏
     * 
     * @param surname 
     */
    public void setSurname(String surname)
    {
        this.surname = surname;
    }

    
    /**
     * 获取：登陆名称
     */
    public List<String> getLoginName()
    {
        return loginName;
    }

    
    /**
     * 设置：登陆名称
     * 
     * @param loginName 
     */
    public void setLoginName(List<String> loginName)
    {
        this.loginName = loginName;
    }

    
    /**
     * 获取：登陆密码
     */
    public List<String> getLoginPwd()
    {
        return loginPwd;
    }

    
    /**
     * 设置：登陆密码
     * 
     * @param loginPwd 
     */
    public void setLoginPwd(List<String> loginPwd)
    {
        this.loginPwd = loginPwd;
    }

    
    /**
     * 获取：组织编号
     */
    public String getGroupNo()
    {
        return groupNo;
    }

    
    /**
     * 设置：组织编号
     * 
     * @param groupNo 
     */
    public void setGroupNo(String groupNo)
    {
        this.groupNo = groupNo;
    }

    
    /**
     * 获取：组织名称
     */
    public String getGroupName()
    {
        return groupName;
    }

    
    /**
     * 设置：组织名称
     * 
     * @param groupName 
     */
    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    
    /**
     * 获取：工卡编号
     */
    public String getCardNo()
    {
        return cardNo;
    }

    
    /**
     * 设置：工卡编号
     * 
     * @param cardNo 
     */
    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }

    
    /**
     * 获取：最后修改时间
     */
    public Date getLastTime()
    {
        return lastTime;
    }

    
    /**
     * 设置：最后修改时间
     * 
     * @param lastTime 
     */
    public void setLastTime(Date lastTime)
    {
        this.lastTime = lastTime;
    }
    
}
