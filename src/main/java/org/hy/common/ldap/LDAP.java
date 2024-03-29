package org.hy.common.ldap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.cursor.Cursor;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddRequestImpl;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.DeleteRequest;
import org.apache.directory.api.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaITImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.hy.common.Help;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.ldap.annotation.LdapAnnotation;
import org.hy.common.ldap.annotation.LdapEntry;
import org.hy.common.xml.XJava;





/**
 * LDAP目录服务的操作类。基于 Apache LDAP API。
 * 
 * 可用于OpenLDAP目录服务的访问操作。
 * 
 * API能通用的原因是：LDAP都是基于X.500标准，目前存在众多版本的LDAP，而最常见的则是V2和V3两个版本，它们分别于1995年和1997年首次发布。
 * 
 * 
 * 主导思想：通过 @Ldap 注解十分方便的实现Java写入、读取LDAP目录服务中的数据，同时不破坏、不改造Java程序原有的数据结构。
 * 
 *   特点1：使用LDAP连接池操作LDAP目录服务。
 *   特点2：内部自动获取连接，自动释放连接，无须外界干预。
 *   特点3：可用XML配置文件配置
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-13
 * @version     v1.0
 *              v2.0  2018-12-06  添加：支持同一属性多个属性值的LDAP特性。
 *                                     Java对象用List<Object>或Set<Object>或数组Object[]定义成员变量的类型，来支持多属性值的LDAP特性。
 *                                     当Java成员变量为String这样的简单时，LDAP中同一属性有多个属性值时，随机取一个给Java成员变量赋值。
 * 
 *                                     LDAP中的属性类型一般都是字符，而此类可以翻译为"条目配置翻译官"类指定的成员类型。
 *              v3.0  2018-12-13  添加：searchEntrys()查询所有子及子子条目时，不包括Base DN自己。
 *              v4.0  2019-01-04  修改：delEntryTree()删除条目及子子条目的功能。
 */
public class LDAP
{
    
    public static final String $ControlOID  = "1.2.840.113556.1.4.805";
    
    /** Entry.get(...) 方法的参数是不区分大小写的，但为了屏蔽歧义和方便固定义此常量 */
    public static final String $ObjectClass = "objectClass";
    
    /** 过滤器中的'与'关系 */
    public static final String $And         = "&";
    
    /** 过滤器中的'或'关系 */
    public static final String $Or          = "|";
    
    
    
    /** LDAP的连接池 */
    private LdapConnectionPool connPool;
    
    
    
    public LDAP(LdapConnectionPool i_ConnPool ,String i_PackageName)
    {
        this.connPool = i_ConnPool;
        LdapAnnotation.parser();
        LdapAnnotation.parser(i_PackageName);
    }
    
    
    
    /**
     * 获取连接池中一个有效连接
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-13
     * @version     v1.0
     *
     * @return      返回对象的具体实现类是：org.apache.directory.ldap.client.api.LdapNetworkConnection
     * @throws LdapException
     */
    public LdapConnection getConnection() throws LdapException
    {
        return this.connPool.getConnection();
    }
    
    
    
    /**
     * 释放或关闭连接
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Conn
     */
    public void closeConnection(LdapConnection i_Conn)
    {
        if ( i_Conn != null )
        {
            try
            {
                this.connPool.releaseConnection(i_Conn);
            }
            catch (Exception exce)
            {
                // Nothing.
            }
            i_Conn = null;
        }
    }
    
    
    
    /**
     * 关闭游标
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Cursor
     */
    public static void closeCursor(Cursor<?> i_Cursor)
    {
        if ( i_Cursor != null )
        {
            try
            {
                if ( !i_Cursor.isClosed() )
                {
                    i_Cursor.close();
                }
            }
            catch (Exception exce)
            {
                // Nothing.
            }
            i_Cursor = null;
        }
    }
    
    
    
    /**
     * 判定LDAP操作是否成功
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_Response
     * @return
     */
    public static boolean isSuccess(ResultResponse i_Response)
    {
        if ( i_Response != null && ResultCodeEnum.SUCCESS.equals(i_Response.getLdapResult().getResultCode()) )
        {
            return true;
        }
        else
        {
            System.out.println(i_Response);
            return false;
        }
    }
    
    
    
    /**
     * 获取XJava对象池中的"条目翻译官"。
     * 
     * 用于：将Java值对象翻译为LDAP条目
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Class  有 @Ldap 注解的Java元类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LdapEntry getLdapEntry(Class<?> i_Class)
    {
        return ((Map<Class<?> ,LdapEntry>)XJava.getObject(LdapAnnotation.$LdapEntryClasses)).get(i_Class);
    }
    
    
    
    /**
     * 获取XJava对象池中的"条目翻译官"。
     * 
     * 用于：将LDAP条目翻译为Java值对象的实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_ObjectClassesID  在定义Java类名称前用 @Ldap 注解的LDAP ObjectClass名称组成的ID。
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LdapEntry getLdapEntry(String i_ObjectClassesID)
    {
        LdapEntry v_LdapEntry = ((Map<String ,LdapEntry>)XJava.getObject(LdapAnnotation.$LdapEntryClassIDs)).get(i_ObjectClassesID);
        
        if ( v_LdapEntry == null )
        {
            System.err.println("LDAP.getLdapEntry('" + i_ObjectClassesID + "') is not find Java Object(LdapEntry).");
        }
        
        return v_LdapEntry;
    }
    
    
    
    /**
     * 获取XJava对象池中的"条目翻译官"。
     * 
     * 用于：将LDAP条目翻译为Java值对象的实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_Entry  条目。将解释条目中的ObjectClass，并组合成ID，再获取"条目翻译官"。
     * @return
     */
    public static LdapEntry getLdapEntry(Entry i_Entry)
    {
        if ( i_Entry == null )
        {
            return null;
        }
        
        Attribute       v_Attribute     = i_Entry.get(LDAP.$ObjectClass);
        Iterator<Value> v_Iter          = v_Attribute.iterator();
        List<String>    v_ObjectClasses = new ArrayList<String>();
        
        while (v_Iter.hasNext())
        {
            Value v_Value = v_Iter.next();
            
            // 1.0.0    版本中用的是v_Value.getString()
            // 2.0.0.AM2版本中用的是v_Value.getValue()
            v_ObjectClasses.add(v_Value.getString());
        }
        
        Help.toSort(v_ObjectClasses);
        
        String v_ObjectClassesID = StringHelp.toString(v_ObjectClasses ,"" ,",");
        if ( Help.isNull(v_ObjectClassesID) )
        {
            return null;
        }
        else
        {
            return getLdapEntry(v_ObjectClassesID);
        }
    }
    
    
    
    /**
     * 按DN查询条目。只返回条目本身。即只返回一条记录(树目录的一个节点)。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_Values
     * @return
     */
    public Object queryEntry(Object i_Values)
    {
        List<?> v_Ret = this.queryEntrys(i_Values ,SearchScope.OBJECT);
        
        if ( Help.isNull(v_Ret) )
        {
            return null;
        }
        else
        {
            return v_Ret.get(0);
        }
    }
    
    
    
    /**
     * 按DN查询条目。只返回条目本身。即只返回一条记录(树目录的一个节点)。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @return
     */
    public Object queryEntry(String i_DN)
    {
        List<?> v_Ret = this.queryEntrys(i_DN ,SearchScope.OBJECT);
        
        if ( Help.isNull(v_Ret) )
        {
            return null;
        }
        else
        {
            return v_Ret.get(0);
        }
    }
    
    
    
    /**
     * 按DN查询条目。返回直接隶属于i_DN的子条目，不返回子子条目。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_Values
     * @return
     */
    public List<?> queryEntryChilds(Object i_Values)
    {
        return this.queryEntrys(i_Values ,SearchScope.ONELEVEL);
    }
    
    
    
    /**
     * 按DN查询条目。返回直接隶属于i_DN的子条目，不返回子子条目。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @return
     */
    public List<?> queryEntryChilds(String i_DN)
    {
        return this.queryEntrys(i_DN ,SearchScope.ONELEVEL);
    }
    
    
    
    /**
     * 按DN查询所有子条目。返回直接或间接隶属于i_DN的子条目及子子条目。
     * 
     * 即将i_DN下面的树结构上的所有条目都返回。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_Values
     * @return
     */
    public List<?> queryEntryTrees(Object i_Values)
    {
        return this.queryEntrys(i_Values ,SearchScope.SUBTREE);
    }
    
    
    
    /**
     * 按DN查询所有子条目。返回直接或间接隶属于i_DN的子条目及子子条目。
     * 
     * 即将i_DN下面的树结构上的所有条目都返回。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @return
     */
    public List<?> queryEntryTrees(String i_DN)
    {
        return this.queryEntrys(i_DN ,SearchScope.SUBTREE);
    }
    
    
    
    /**
     * 按DN查询条目。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_Values       条目标识
     * @param i_SearchScope  查询范围
     *                       搜索范围01：SearchScope.OBJECT    返回输入给定DN，如果它存在的话。
     *                       搜索范围02：SearchScope.ONELEVEL  返回低于目前DN的所有子元素，不包括当前DN，也不包括与当前DN无直接关系的DN，即树目录深度为1。
     *                       搜索范围03：SearchScope.SUBTREE   返回所有元素从给出的DN，包括与DN相关的元素，无论树的深度。
     * @return
     */
    private List<?> queryEntrys(Object i_Values ,SearchScope i_SearchScope)
    {
        LdapEntry v_LdapEntry = getLdapEntry(i_Values.getClass());
        
        if ( v_LdapEntry == null )
        {
            return new ArrayList<Object>();
        }
        
        try
        {
            return this.queryEntrys(v_LdapEntry.getDNValue(i_Values) ,i_SearchScope);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return new ArrayList<Object>();
    }
    
    
    
    /**
     * 按DN查询条目。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @param i_SearchScope  查询范围
     *                       搜索范围01：SearchScope.OBJECT    返回输入给定DN，如果它存在的话。
     *                       搜索范围02：SearchScope.ONELEVEL  返回低于目前DN的所有子元素，不包括当前DN，也不包括与当前DN无直接关系的DN，即树目录深度为1。
     *                       搜索范围03：SearchScope.SUBTREE   返回所有元素从给出的DN，包括与DN相关的元素，无论树的深度。
     * @return
     */
    private List<?> queryEntrys(String i_DN ,SearchScope i_SearchScope)
    {
        LdapConnection v_Conn   = null;
        EntryCursor    v_Cursor = null;
        List<Object>   v_Ret    = new ArrayList<Object>();
        
        try
        {
            v_Conn   = this.getConnection();
            v_Cursor = v_Conn.search(new Dn(i_DN) ,"(" + LDAP.$ObjectClass + "=*)" ,i_SearchScope);
            
            while ( v_Cursor.next() )
            {
                Entry     v_Entry     = v_Cursor.get();
                LdapEntry v_LdapEntry = getLdapEntry(v_Entry);
                
                if ( v_LdapEntry != null )
                {
                    v_Ret.add(v_LdapEntry.toObject(v_Entry));
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
            this.closeConnection(v_Conn);
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 基于父节点Base DN，查询符合条件的条目。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-08
     * @version     v1.0
     *
     * @param i_Values  用@Ldap标记的Java对象实例（必要属性值DN、及其它过滤查询的属性值）
     * @return          多属性间的过滤条件为'与'，同一属性的多属性值为'或'关系的精确查询。
     */
    public List<?> searchEntrys(Object i_Values)
    {
        return this.searchEntrys(i_Values ,SearchScope.SUBTREE ,true ,false ,false ,false);
    }
    
    
    
    /**
     * 基于父节点Base DN，查询符合条件的条目。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-08
     * @version     v1.0
     *
     * @param i_Values            用@Ldap标记的Java对象实例（必要属性值DN、及其它过滤查询的属性值）
     *                              1. 当没有DN时，返回null
     *                              2. 当只有DN时，按DN查询，这就与LDAP.query...()开头的方法相同了。
     * @param i_SearchScope       查询范围（注：查询的Base DN为 i_Values 的DN）
     *                            搜索范围01：SearchScope.OBJECT    返回输入给定DN，如果它存在的话。
     *                            搜索范围02：SearchScope.ONELEVEL  返回低于目前DN的所有子元素，不包括当前DN，也不包括与当前DN无直接关系的DN，即树目录深度为1。
     *                            搜索范围03：SearchScope.SUBTREE   返回所有元素从给出的DN，包括与DN相关的元素，无论树的深度。
     * @param i_IsAnd             不属性间的属性值的过滤关系，是'与'、'或'关系？
     * @param i_IsAndByMultValue  同一属性的多个属性值间的过滤关系，是'与'、'或'关系？
     * @param i_IsLikeBefore      属性值的前缀是否为模糊匹配
     * @param i_IsLikeAfter       属性值的后缀是否为模糊匹配
     * @return
     */
    public List<?> searchEntrys(Object i_Values ,SearchScope i_SearchScope ,boolean i_IsAnd ,boolean i_IsAndByMultValue ,boolean i_IsLikeBefore ,boolean i_IsLikeAfter)
    {
        List<?> v_Ret = null;
        
        if ( i_Values == null )
        {
            return v_Ret;
        }
        
        LdapEntry v_LdapEntry = getLdapEntry(i_Values.getClass());
        if ( v_LdapEntry == null )
        {
            return v_Ret;
        }
        
        try
        {
            String v_BaseDN = v_LdapEntry.getDNValue(i_Values);
            if ( Help.isNull(v_BaseDN) )
            {
                return v_Ret;
            }
            
            String v_Filter = makeSearchFilter(i_Values ,i_IsAnd ,i_IsAndByMultValue ,i_IsLikeBefore ,i_IsLikeAfter);
            if ( Help.isNull(v_Filter) )
            {
                if ( !Help.isNull(v_LdapEntry.getRdn()) )
                {
                    // ApacheDS的DN属性名称为：entryDN
                    v_Filter = "(!(" + v_LdapEntry.getRdn() + "=" + v_BaseDN + "))";
                    v_Filter = "(" + LDAP.$ObjectClass + "=*)";
                }
                else
                {
                    v_Filter = "(" + LDAP.$ObjectClass + "=*)";
                }
            }
            
            v_Ret = this.searchEntrys(v_BaseDN ,v_Filter ,i_SearchScope);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 基于父节点Base DN，查询符合条件的条目。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-08
     * @version     v1.0
     *
     * @param i_BaseDN       父节点标识
     * @param i_Filter       过滤条件
     * @param i_SearchScope  查询范围
     *                       搜索范围01：SearchScope.OBJECT    返回输入给定DN，如果它存在的话。
     *                       搜索范围02：SearchScope.ONELEVEL  返回低于目前DN的所有子元素，不包括当前DN，也不包括与当前DN无直接关系的DN，即树目录深度为1。
     *                       搜索范围03：SearchScope.SUBTREE   返回所有元素从给出的DN，包括与DN相关的元素，无论树的深度。
     * @return
     */
    private List<?> searchEntrys(String i_BaseDN ,String i_Filter ,SearchScope i_SearchScope)
    {
        LdapConnection v_Conn   = null;
        EntryCursor    v_Cursor = null;
        List<Object>   v_Ret    = new ArrayList<Object>();
        
        try
        {
            v_Conn   = this.getConnection();
            v_Cursor = v_Conn.search(new Dn(i_BaseDN) ,i_Filter ,i_SearchScope);
            
            while ( v_Cursor.next() )
            {
                Entry     v_Entry     = v_Cursor.get();
                LdapEntry v_LdapEntry = getLdapEntry(v_Entry);
                
                if ( v_LdapEntry != null )
                {
                    v_Ret.add(v_LdapEntry.toObject(v_Entry));
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
            this.closeConnection(v_Conn);
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 用Java对象(@Ldap)生成查询的过滤器条件。
     * 
     * Java对象的成员属性name和pwd，属性值为HY和123456，对应LDAP属性名cn和gn。
     * 
     * 与关系时生成：(&(cn=HY)(gn=123456))
     * 或关系时生成：(|(cn=HY)(gn=123456))
     * 
     * 与关系带前缀模糊匹配：(&(cn=*HY )(gn=*123456))
     * 与关系带后缀模糊匹配：(&(cn= HY*)(gn= 123456*))
     * 与关系带全模糊匹配：  (&(cn=*HY*)(gn=*123456*))
     * 
     * 当同一属性有多个属性值时，要符合上面的规则。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-08
     * @version     v1.0
     *
     * @param i_Values            用@Ldap注解过的Java对象
     * @param i_IsAnd             不属性间的属性值的过滤关系，是'与'、'或'关系？
     * @param i_IsAndByMultValue  同一属性的多个属性值间的过滤关系，是'与'、'或'关系？
     * @param i_IsLikeBefore      属性值的前缀是否为模糊匹配
     * @param i_IsLikeAfter       属性值的后缀是否为模糊匹配
     * @return
     */
    public static String makeSearchFilter(Object i_Values ,boolean i_IsAnd ,boolean i_IsAndByMultValue ,boolean i_IsLikeBefore ,boolean i_IsLikeAfter)
    {
        LdapEntry     v_LdapEntry = getLdapEntry(i_Values.getClass());
        StringBuilder v_Filter    = new StringBuilder();
        
        for (Map.Entry<String ,Map<String ,Method>> v_Item : v_LdapEntry.getElementsToLDAP().entrySet())
        {
            for (Map.Entry<String ,Method> v_ItemMethod : v_Item.getValue().entrySet())
            {
                try
                {
                    Object v_Value = v_ItemMethod.getValue().invoke(i_Values);
                    if ( v_Value == null )
                    {
                        continue;
                    }
                    
                    List<String> v_FilterValues = new ArrayList<String>();
                    if ( v_Value instanceof List )
                    {
                        for (Object v_VItem : (List<?>)v_Value)
                        {
                            if ( v_VItem != null && !Help.isNull(v_VItem.toString()) )
                            {
                                v_FilterValues.add(v_VItem.toString());
                            }
                        }
                    }
                    else if ( v_Value instanceof Set )
                    {
                        for (Object v_VItem : (Set<?>)v_Value)
                        {
                            if ( v_VItem != null && !Help.isNull(v_VItem.toString()) )
                            {
                                v_FilterValues.add(v_VItem.toString());
                            }
                        }
                    }
                    else if ( v_Value instanceof Object [] )
                    {
                        for (Object v_VItem : (Object [])v_Value)
                        {
                            if ( v_VItem != null && !Help.isNull(v_VItem.toString()) )
                            {
                                v_FilterValues.add(v_VItem.toString());
                            }
                        }
                    }
                    else if ( v_Value instanceof Map )
                    {
                        ////////////////////////////////////
                    }
                    else
                    {
                        if ( !Help.isNull(v_Value.toString()) )
                        {
                            v_FilterValues.add(v_Value.toString());
                        }
                    }
                    
                    if ( Help.isNull(v_FilterValues) )
                    {
                        continue;
                    }
                    
                    if ( v_FilterValues.size() >= 2 )
                    {
                        v_Filter.append("(").append(i_IsAndByMultValue ? $And : $Or);
                    }
                    
                    for (String v_FilterValue : v_FilterValues)
                    {
                        v_Filter.append("(").append(v_Item.getKey()).append("=");
                        if ( i_IsLikeBefore )
                        {
                            v_Filter.append("*");
                        }
                        v_Filter.append(v_FilterValue);
                        if ( i_IsLikeAfter )
                        {
                            v_Filter.append("*");
                        }
                        v_Filter.append(")");
                    }
                    
                    if ( v_FilterValues.size() >= 2 )
                    {
                        v_Filter.append(")");
                    }
                }
                catch (Exception exce)
                {
                    exce.printStackTrace();
                }
            }
        }
        
        String v_Ret = v_Filter.toString();
        if ( !Help.isNull(v_Ret) )
        {
            v_Ret = "(" + (i_IsAnd ? $And : $Or) + v_Ret + ")";
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 批量添加条目。i_ValuesMap集合中的每个元素可以是不同类型的，对应不同类型的LDAP类。
     * 
     * 注1：有顺序的添加。这样可以实现先添加父条目，再添加子条目的功能。
     * 注2：没有事务机构，这不是LDAP的长项。不要指望LDAP可以作到。
     *      某个元素执行异常后，前面的不回滚，其后的不再执行添加。
     * 注3：批量添加时只占用一个连接。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * 注：不存时，才新增写入条目。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_ValuesMap
     * @return   返回实际新增的条目数量。小于0，表示异常。
     *           -3表示：入参i_ValuesMap中有重复的DN值。
     */
    public int addEntrys(List<?> i_ValuesMap)
    {
        if ( Help.isNull(i_ValuesMap) )
        {
            return -1;
        }
        
        int         v_AddCount = 0;
        List<Entry> v_Entrys   = new ArrayList<Entry>();
        Set<String> v_DNs      = new HashSet<String>();
        
        try
        {
            for (Object v_Values : i_ValuesMap)
            {
                if ( v_Values == null )
                {
                    continue;
                }
                
                LdapEntry v_LdapEntry = getLdapEntry(v_Values.getClass());
                if ( v_LdapEntry == null )
                {
                    return -2;
                }
                
                String v_DN = v_LdapEntry.getDNValue(v_Values);
                if ( v_DNs.contains(v_DN) )
                {
                    System.err.println("LDAP.addEntrys() DN[" + v_DN + "] is duplicate.");
                    return -3;
                }
                v_DNs.add(v_DN);
                
                Object v_OldValues = this.queryEntry(v_DN);
                if ( v_OldValues == null )  // 不存时，才新增写入条目
                {
                    Entry v_Entry = v_LdapEntry.toEntry(v_Values);
                    if ( v_Entry == null )
                    {
                        return -4;
                    }
                    v_Entrys.add(v_Entry);
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            return -5;
        }
        
        
        v_DNs.clear();
        v_DNs = null;
        LdapConnection v_Conn = null;
        
        try
        {
            v_Conn = this.getConnection();
            
            for (Entry v_Entry : v_Entrys)
            {
                AddRequest  v_AddRequest = new AddRequestImpl();
                AddResponse v_Response   = null;
                
                v_AddRequest.setEntry(  v_Entry);
                v_AddRequest.addControl(new ManageDsaITImpl());
                
                v_Response = v_Conn.add(v_AddRequest);
                
                if ( !LDAP.isSuccess(v_Response) )
                {
                    return -6;
                }
                
                v_AddCount++;
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            return -7;
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return v_AddCount;
    }
    
    
    
    /**
     * 添加条目。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Values
     * @return
     */
    public boolean addEntry(Object i_Values)
    {
        LdapEntry v_LdapEntry = getLdapEntry(i_Values.getClass());
        
        if ( v_LdapEntry == null )
        {
            return false;
        }
        
        Entry v_Entry = null;
        
        try
        {
            v_Entry = v_LdapEntry.toEntry(i_Values);
            
            return this.addEntry(v_Entry);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return false;
    }
    
    
    
    /**
     * 添加条目
     * 
     * Apache LDAP 主要提供三种添加条目的方法。当前用：同步的、服务器有返回响应的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_DN        条目标识
     * @param i_Elements  条目属性
     * @return            添加成功返回true。
     */
    public boolean addEntry(String i_DN ,Object ... i_Elements)
    {
        Entry v_Entry = null;
        
        try
        {
            v_Entry = new DefaultEntry(i_DN ,i_Elements);
            
            return this.addEntry(v_Entry);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return false;
    }
    
    
    
    /**
     * 添加条目
     * 
     * Apache LDAP 主要提供三种添加条目的方法。当前用：同步的、服务器有返回响应的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     * 
     * @param i_Entry
     * @return            添加成功返回true。
     */
    public boolean addEntry(Entry i_Entry)
    {
        LdapConnection v_Conn       = null;
        AddRequest     v_AddRequest = new AddRequestImpl();
        AddResponse    v_Response   = null;
        
        try
        {
            v_AddRequest.setEntry(  i_Entry);
            v_AddRequest.addControl(new ManageDsaITImpl());
            
            v_Conn = this.getConnection();
            v_Response = v_Conn.add(v_AddRequest);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return LDAP.isSuccess(v_Response);
    }
    
    
    
    /**
     * 判断条目是否存在
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_DN  条目标识
     * @return
     */
    public boolean isExists(String i_DN)
    {
        LdapConnection v_Conn = null;
        boolean        v_Ret  = false;
        
        try
        {
            v_Conn = this.getConnection();
            v_Ret  = v_Conn.exists(i_DN);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 批量删除条目。i_Datas集合中的每个元素可对应不同类型的LDAP类。
     * 
     * 注1：有顺序的删除。这样可以实现先删除子条目，再添加父条目的功能。
     * 注2：没有事务机构，这不是LDAP的长项。不要指望LDAP可以作到。
     *      某个元素执行异常后，前面的不回滚，其后的不再执行删除。
     * 注3：批量删除时只占用一个连接。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-04
     * @version     v1.0
     *
     * @param i_Datas  支持三种大的分类：
     *                 1. List<String> 形式的入参，这时集合元素应当 DN 含义，元素为 DN 的值。
     *                 2. List<Object> 形式的入参，这时集合元素应当 @Ldap 注解的Java值对象。
     *                                 并且每个元素的类型可以不一样。
     *                 3. List<?>      混合的入参，元素可以是String、@Ldap 注解的Java值对象，并且每个元素的类型可以不一样。
     * @return   >=0 表示删除的条目数量。小于0时，表示异常。
     */
    public int delEntrys(List<?> i_Datas)
    {
        LdapConnection v_Conn  = null;
        int            v_Count = 0;
        
        if ( Help.isNull(i_Datas) )
        {
            return v_Count;
        }
        
        try
        {
            v_Conn = this.getConnection();
            
            for (Object v_Data : i_Datas)
            {
                if ( v_Data == null )
                {
                    continue;
                }
                
                DeleteRequest  v_Request  = new DeleteRequestImpl();
                DeleteResponse v_Response = null;
                
                if ( v_Data instanceof String )
                {
                    v_Request.setName(new Dn(v_Data.toString()));
                }
                else
                {
                    LdapEntry v_LdapEntry = getLdapEntry(v_Data.getClass());
                    v_Request.setName(new Dn(v_LdapEntry.getDNValue(v_Data)));
                }
                
                v_Response = v_Conn.delete(v_Request);
                
                if ( !LDAP.isSuccess(v_Response) )
                {
                    v_Count = -1;
                    break;
                }
                
                v_Count++;
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return v_Count;
    }
    
    
    
    /**
     * 删除条目
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-04
     * @version     v1.0
     *
     * @param i_Values
     * @return
     */
    public boolean delEntry(Object i_Values)
    {
        LdapEntry v_LdapEntry = getLdapEntry(i_Values.getClass());
        
        if ( v_LdapEntry == null )
        {
            return false;
        }
        
        try
        {
            String v_DN = v_LdapEntry.getDNValue(i_Values);
            
            return this.delEntry(v_DN);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        return false;
    }
    
    
    
    /**
     * 删除条目
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_DN  条目标识
     * @return
     */
    public boolean delEntry(String i_DN)
    {
        LdapConnection v_Conn     = null;
        DeleteRequest  v_Request  = new DeleteRequestImpl();
        DeleteResponse v_Response = null;
        
        try
        {
            v_Request.setName(new Dn(i_DN));
            
            v_Conn = this.getConnection();
            v_Response = v_Conn.delete(v_Request);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return LDAP.isSuccess(v_Response);
    }
    
    
    
    /**
     * 删除条目树（递归删除条目）
     * 
     * LdapNetworkConnection类中是一个deleteTree()方法的，但没有返回值，真不知道开发者是怎么考虑的。
     * 看过它的源码，发现它是通过delete()方法实现的，只是添加一个控制对象。所以本方法实现思路相同，如下。
     * 
     * @see org.apache.directory.ldap.client.api.LdapNetworkConnection.deleteTree()
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_DN  条目标识
     * @return
     */
    public int delEntryTree(String i_DN)
    {
        // 下面的方法也不能删除带在子条目的父条目，官方好像没有出删除树的API
        // LdapNetworkConnection v_ConnNetwork = (LdapNetworkConnection)((MonitoringLdapConnection)v_Conn).wrapped();
        // v_ConnNetwork.deleteTree(i_DN);
        
        try
        {
            // ApacheDS的DN属性名称为：entryDN
            int v_RetCount = this.delEntrys(this.searchEntrys(i_DN ,"(!(entryDN=" + i_DN + "))" ,SearchScope.SUBTREE));
            if ( v_RetCount >= 0 )
            {
                if ( this.delEntry(i_DN) )
                {
                    return v_RetCount + 1;
                }
                else
                {
                    return -2;
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return -1;
    }
    
    
    
    /**
     * 删除条目树（递归删除条目），但不删除自己
     * 
     * LdapNetworkConnection类中是一个deleteTree()方法的，但没有返回值，真不知道开发者是怎么考虑的。
     * 看过它的源码，发现它是通过delete()方法实现的，只是添加一个控制对象。所以本方法实现思路相同，如下。
     * 
     * @see org.apache.directory.ldap.client.api.LdapNetworkConnection.deleteTree()
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-04
     * @version     v1.0
     *
     * @param i_DN  条目标识
     * @return
     */
    public int delEntryChildTree(String i_DN)
    {
        // 下面的方法也不能删除带在子条目的父条目，官方好像没有出删除树的API
        // LdapNetworkConnection v_ConnNetwork = (LdapNetworkConnection)((MonitoringLdapConnection)v_Conn).wrapped();
        // v_ConnNetwork.deleteTree(i_DN);
        
        try
        {
            // ApacheDS的DN属性名称为：entryDN
            return this.delEntrys(this.searchEntrys(i_DN ,"(!(entryDN=" + i_DN + "))" ,SearchScope.SUBTREE));
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return -1;
    }
    
    
    
    /**
     * 对条目的添加多个属性。
     * 
     *   1. 自动识别要添加的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_NewValues
     * @return   返回添加的属性个数。小于0表示异常
     */
    public int addAttributes(Object i_NewValues)
    {
        return modifyEntry(i_NewValues ,true ,false ,false);
    }
    
    
    
    /**
     * 条目添加属性
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @param i_AttributeValue  属性值(可为多个)
     * @return
     */
    public boolean addAttribute(String i_DN ,String i_AttributeName ,String ... i_AttributeValue)
    {
        return this.modifyEntry(ModificationOperation.ADD_ATTRIBUTE ,i_DN ,i_AttributeName ,i_AttributeValue);
    }
    
    
    
    /**
     * 修改条目的多个属性值。（只修改属性，不添加属性，不删除属性）
     * 
     *   1. 自动识别要修改的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_NewValues
     * @return  修改的属性个数。小于0表示异常
     */
    public int modifyAttributes(Object i_NewValues)
    {
        return modifyEntry(i_NewValues ,false ,true ,false);
    }
    
    
    
    /**
     * 修改条目的属性值
     * 
     * 注意：如果LDAP库中已有同一属性的多个属性值，旧值1、旧值2、旧值x，
     *      执行此方法后，LDAP库中的属性值将改为，新值1、新值2、新值x
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @param i_AttributeValue  属性值(可为多个，新值1、新值2、新值x)
     * @return
     */
    public boolean modifyAttribute(String i_DN ,String i_AttributeName ,String ... i_AttributeValue)
    {
        return this.modifyEntry(ModificationOperation.REPLACE_ATTRIBUTE ,i_DN ,i_AttributeName ,i_AttributeValue);
    }
    
    
    
    /**
     * 删除条目的多个属性。
     * 
     *   1. 自动识别要删除的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_NewValues
     * @return   删除的属性个数。小于0表示异常
     */
    public int delAttributes(Object i_NewValues)
    {
        return modifyEntry(i_NewValues ,false ,false ,true);
    }
    
    
    
    /**
     * 删除条目的属性。
     * 
     * 有能力删除某一属性名称下多个属性值的某一项属性值（通过属性值匹配）。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-14
     * @version     v1.0
     *
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @param i_AttributeValue  属性值
     * @return
     */
    public boolean delAttribute(String i_DN ,String i_AttributeName ,String ... i_AttributeValue)
    {
        return this.modifyEntry(ModificationOperation.REMOVE_ATTRIBUTE ,i_DN ,i_AttributeName ,i_AttributeValue);
    }
    
    
    
    /**
     * 删除条目的属性。
     * 
     * 注：当同一属性有多个属性值时，将一起同被删除。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @return
     */
    public boolean delAttribute(String i_DN ,String i_AttributeName)
    {
        return this.modifyEntry(ModificationOperation.REMOVE_ATTRIBUTE ,i_DN ,i_AttributeName);
    }
    
    
    
    /**
     * 批量修改条目的多个属性。i_ValuesMap集合中的每个元素可以是不同类型的，对应不同类型的LDAP类。
     * （只添加属性、或只修改属性，不删除属性）
     * 
     * 注1：有顺序的修改。这样可以实现先添加父条目，再添加子条目的功能。
     * 注2：没有事务机构，这不是LDAP的长项。不要指望LDAP可以作到。
     *      某个元素执行异常后，前面的不回滚，其后的不再执行添加。
     * 注3：批量修改时占用多个连接。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     *   1. 自动识别要添加的多个属性
     *   2. 自动识别要修改的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     *
     * @param i_ValuesMap
     * @return             修改的条目的个数。小于0表示异常
     */
    public int modifyEntrys(List<?> i_ValuesMap)
    {
        return modifyEntrys(i_ValuesMap ,true ,true ,false);
    }
    
    
    
    /**
     * 批量修改条目的多个属性。i_ValuesMap集合中的每个元素可以是不同类型的，对应不同类型的LDAP类。
     * 
     * 注1：有顺序的修改。这样可以实现先添加父条目，再添加子条目的功能。
     * 注2：没有事务机构，这不是LDAP的长项。不要指望LDAP可以作到。
     *      某个元素执行异常后，前面的不回滚，其后的不再执行添加。
     * 注3：批量修改时占用多个连接。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     *   1. 自动识别要添加的多个属性
     *   2. 自动识别要修改的多个属性
     *   3. 自动识别要删除的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     *
     * @param i_ValuesMap
     * @param i_IsAdd      当LDAP中没有时，是否新增LDAP属性
     * @param i_IsUpdate   当新旧不同时，是否修改LDAP中对应的属性
     * @param i_IsDel      当Java属性值为null时，是否删除LDAP中对应的属性
     * @return             修改的条目的个数。小于0表示异常
     */
    public int modifyEntrys(List<?> i_ValuesMap ,boolean i_IsAdd ,boolean i_IsUpdate ,boolean i_IsDel)
    {
        if ( Help.isNull(i_ValuesMap) )
        {
            return -1;
        }
        
        int v_ModEntryCount = 0;
        int v_ModAttrCount  = 0;
        
        try
        {
            for (Object v_Values : i_ValuesMap)
            {
                if ( v_Values == null )
                {
                    continue;
                }
                
                v_ModAttrCount = this.modifyEntry(v_Values ,i_IsAdd ,i_IsUpdate ,i_IsDel);
                
                if ( v_ModAttrCount < 0 )
                {
                    System.err.println("LDAP.modifyEntrys() is error.\n" + v_Values.toString());
                    return -1;
                }
                else if ( v_ModAttrCount > 0 )
                {
                    v_ModEntryCount++;
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            return -1;
        }
        
        return v_ModEntryCount;
    }
    
    
    
    /**
     * 修改条目的多个属性。（只添加属性、或只修改属性，不删除属性）
     * 
     *   1. 自动识别要添加的多个属性
     *   2. 自动识别要修改的多个属性
     *   3. 自动识别要删除的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-15
     * @version     v1.0
     *
     * @param i_NewValues
     * @return             修改的属性个数。小于0表示异常
     */
    public int modifyEntry(Object i_NewValues)
    {
        return modifyEntry(i_NewValues ,true ,true ,false);
    }
    
    
    
    /**
     * 修改条目的多个属性。
     * 
     *   1. 自动识别要添加的多个属性
     *   2. 自动识别要修改的多个属性
     *   3. 自动识别要删除的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *              v2.0  2018-12-07  添加：支持同一属性的多个属性值的修改、删除。
     *                                     注：在多属性值的情况下，只有添加属性值、删除属性值的操作，没有替换修改属性值的操作
     *                                     1. ( i_IsAdd &&  i_IsUpdate)为真时，新的插入、旧的删除：将LDAP数据库中的属性值修改为与i_NewValues属性值一样，不存在的将删除。
     *                                     2. ( i_IsAdd && !i_IsUpdate)为真时，新的插入、旧的保留：只向LDAP数据库中添加新的属性值，原LDAP数据库中的属性值将保留。
     *                                     3. (!i_IsAdd &&  i_IsUpdate)为真时，没有插入、旧的删除：不存于i_NewValues的属性值，将被删除。并不向LDAP数据库中添加任何新属性值。
     * 
     *              v3.0  2018-12-14  添加：支Java对象用Map<Object ,Object>定义成员变量的类型，来支持多属性值情况下，旧值改为新值的场景。并且支持批量。
     *
     * @param i_NewValues
     * @param i_IsAdd      当LDAP中没有时，是否新增LDAP属性
     * @param i_IsUpdate   当新旧不同时，是否修改LDAP中对应的属性
     * @param i_IsDel      当Java属性值为null时，是否删除LDAP中对应的属性
     * @return             修改的属性个数。小于0表示异常
     */
    public int modifyEntry(Object i_NewValues ,boolean i_IsAdd ,boolean i_IsUpdate ,boolean i_IsDel)
    {
        LdapEntry v_LdapEntry = getLdapEntry(i_NewValues.getClass());
        
        if ( v_LdapEntry == null )
        {
            return -1;
        }
        
        LdapConnection        v_Conn     = null;
        Return<ModifyRequest> v_Request  = null;
        ModifyResponse        v_Response = null;
        
        try
        {
            Object v_OldValues = this.queryEntry(v_LdapEntry.getDNValue(i_NewValues));
            if ( v_OldValues == null )
            {
                // 没有旧对象，也算是修改成功
                return 0;
            }
            
            v_Request = v_LdapEntry.toModify(v_OldValues ,i_NewValues ,i_IsAdd ,i_IsUpdate ,i_IsDel);
            if ( !v_Request.booleanValue() )
            {
                return v_Request.paramInt <= 0 ? 0 : -1;
            }
            
            v_Conn     = this.getConnection();
            v_Response = v_Conn.modify(v_Request.getParamObj());
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return LDAP.isSuccess(v_Response) ? v_Request.paramInt : -1;
    }

    
    
    /**
     * 修改条目(准确是修改条目的属性)。可包括如下操作。
     *   1. ModificationOperation.ADD_ATTRIBUTE：    添加属性
     *   2. ModificationOperation.REMOVE_ATTRIBUTE： 删除属性
     *   3. ModificationOperation.REPLACE_ATTRIBUTE：替换属性值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_Operation       操作类型
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @param i_AttributeValue  属性值(可为多个)
     * @return
     */
    private boolean modifyEntry(ModificationOperation i_Operation ,String i_DN ,String i_AttributeName ,String ... i_AttributeValue)
    {
        LdapConnection v_Conn     = null;
        ModifyRequest  v_Request  = new ModifyRequestImpl();
        ModifyResponse v_Response = null;
        
        try
        {
            v_Request.setName(new Dn(i_DN));
            v_Request.addModification(new DefaultModification(i_Operation ,i_AttributeName ,i_AttributeValue));
            
            v_Conn = this.getConnection();
            v_Response = v_Conn.modify(v_Request);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return LDAP.isSuccess(v_Response);
    }
    
}
