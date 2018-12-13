package org.hy.common.ldap.junit.dbtoldap;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.Date;
import org.hy.common.Help;
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
    @Ldap(type=LdapType.DN ,name="entryDN")
    private String       userID;
    
    /** 用户编号 */
    @Ldap("uid")
    private String       userNo;
    
    /** 用户真实名称 */
    @Ldap("title")
    private String       userTrueName;
    
    /** 用户名称 */
    @Ldap("cn")
    private List<String> userNames;
    
    /** 用户标记 */
    @Ldap("postalCode")
    private String       userType;
    
    /** 用户姓氏 */
    @Ldap("sn")
    private String       surname;
    
    /** 性别 */
    @Ldap("employeeType")
    private String       sex;
    
    /** 登陆名称 */
    @Ldap("givenName")
    private List<String> loginNames;
    
    /** 登陆密码 */
    @Ldap("carLicense")
    private List<String> loginPwds;
    
    /** 联系电话 */
    @Ldap("mobile")
    private List<String> tels;
    
    /** 组织编号 */
    @Ldap("o")
    private String       groupNo;
    
    /** 组织名称 */
    @Ldap("ou")
    private String       groupName;
    
    /** 籍贯 */
    @Ldap("street")
    private String       city;
    
    /** 工卡编号 */
    @Ldap("employeeNumber")
    private String       cardNo;
    
    /** 微信的OpenID */
    @Ldap("displayName")
    private String       openID;
    
    /** Avplan中的内部ID */
    @Ldap("businessCategory")
    private String       innerID;
    
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
     * 获取：用户真实名称
     */
    public String getUserTrueName()
    {
        return userTrueName;
    }

    
    /**
     * 设置：用户真实名称
     * 
     * @param userTrueName 
     */
    public void setUserTrueName(String userTrueName)
    {
        this.userTrueName = userTrueName;
    }


    /**
     * 获取：用户名称
     */
    public List<String> getUserNames()
    {
        return userNames;
    }

    
    /**
     * 设置：用户名称
     * 
     * @param userNames 
     */
    public void setUserNames(List<String> userNames)
    {
        this.userNames = userNames;
    }
    
    
    /**
     * 设置：用户名称
     * 
     * @param i_UserName 
     */
    public synchronized void setUserName(String i_UserName)
    {
        if ( Help.isNull(i_UserName) )
        {
            return;
        }
        
        if ( this.userNames == null )
        {
            this.userNames = new ArrayList<String>();
        }
        
        this.userNames.add(i_UserName);
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
    public List<String> getLoginNames()
    {
        return loginNames;
    }

    
    /**
     * 设置：登陆名称
     * 
     * @param loginNames
     */
    public void setLoginNames(List<String> loginNames)
    {
        this.loginNames = loginNames;
    }
    
    
    /**
     * 设置：登陆名称
     * 
     * @param i_LoginName 
     */
    public synchronized void setLoginName(String i_LoginName)
    {
        if ( Help.isNull(i_LoginName) )
        {
            return;
        }
        
        if ( this.loginNames == null )
        {
            this.loginNames = new ArrayList<String>();
        }
        
        this.loginNames.add(i_LoginName);
    }

    
    /**
     * 获取：登陆密码
     */
    public List<String> getLoginPwds()
    {
        return loginPwds;
    }

    
    /**
     * 设置：登陆密码
     * 
     * @param loginPwds 
     */
    public void setLoginPwds(List<String> loginPwds)
    {
        this.loginPwds = loginPwds;
    }
    
    
    /**
     * 设置：登陆密码
     * 
     * @param i_LoginPwd 
     */
    public synchronized void setLoginPwd(String i_LoginPwd)
    {
        if ( Help.isNull(i_LoginPwd) )
        {
            return;
        }
        
        if ( this.loginPwds == null )
        {
            this.loginPwds = new ArrayList<String>();
        }
        
        this.loginPwds.add(i_LoginPwd);
    }
    
    
    /**
     * 获取：联系电话
     */
    public List<String> getTels()
    {
        return tels;
    }

    
    /**
     * 设置：联系电话
     * 
     * @param tels
     */
    public void setTels(List<String> tels)
    {
        this.tels = tels;
    }
    
    
    /**
     * 设置：联系电话
     * 
     * @param i_Tel 
     */
    public synchronized void setTel(String i_Tel)
    {
        if ( Help.isNull(i_Tel) )
        {
            return;
        }
        
        if ( this.tels == null )
        {
            this.tels = new ArrayList<String>();
        }
        
        this.tels.add(i_Tel);
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

    
    /**
     * 获取：性别
     */
    public String getSex()
    {
        return sex;
    }


    /**
     * 设置：性别
     * 
     * @param sex 
     */
    public void setSex(String sex)
    {
        this.sex = sex;
    }

    
    /**
     * 获取：籍贯
     */
    public String getCity()
    {
        return city;
    }

    
    /**
     * 设置：籍贯
     * 
     * @param city 
     */
    public void setCity(String city)
    {
        this.city = city;
    }

    
    /**
     * 获取：用户标记
     */
    public String getUserType()
    {
        return userType;
    }


    /**
     * 设置：用户标记
     * 
     * @param userType 
     */
    public void setUserType(String userType)
    {
        this.userType = userType;
    }


    /**
     * 获取：微信的OpenID
     */
    public String getOpenID()
    {
        return openID;
    }

    
    /**
     * 设置：微信的OpenID
     * 
     * @param openID 
     */
    public void setOpenID(String openID)
    {
        this.openID = openID;
    }

    
    /**
     * 获取：Avplan中的内部ID
     */
    public String getInnerID()
    {
        return innerID;
    }

    
    /**
     * 设置：Avplan中的内部ID
     * 
     * @param innerID 
     */
    public void setInnerID(String innerID)
    {
        this.innerID = innerID;
    }
    
}
