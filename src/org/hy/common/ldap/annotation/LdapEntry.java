package org.hy.common.ldap.annotation;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.TablePartitionRID;
import org.hy.common.comparate.Comparate;
import org.hy.common.comparate.ComparateResult;
import org.hy.common.ldap.LDAP;





/**
 * LDAP条目配置翻译官。
 * 
 *  1. 将Java值对象翻译为LDAP条目
 *     1.1. Java类中的 @Ldap 注解将被翻译为本类。
 *     1.2. 再通过本类的 toEntry(...) 方法即可翻译成为 Apache LDAP API 中的条目对象。
 *     
 *  2. 将LDAP条目翻译为Java值对象的实例
 *     2.1. Java类中的 @Ldap 注解将被翻译为本类。
 *     2.2. 再通过本类的 toObject(...) 方法即可翻译 Apache LDAP API 中的条目对象为Java值对象。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-14
 * @version     v1.0
 *              v2.0  2018-12-06  添加：支持同一属性多个属性值的LDAP特性。
 *                                     Java对象用List<Object>或Set<Object>或数组Object[]定义成员变量的类型，来支持多属性值的LDAP特性。
 *                                     当Java成员变量为String这样的简单时，LDAP中同一属性有多个属性值时，随机取一个给Java成员变量赋值。
 *                                     
 *                                     LDAP中的属性类型一般都是字符，而此类可以翻译为"条目配置翻译官"类指定的成员类型。
 *                                     
 *              v3.0  2018-12-13  添加：RDN属性，指DN逗号最左边的部分，最小的子条目。用于更新特性功能。
 *                                     如，查询所有子及子子条目时，不包括Base DN自己。
 *                                     
 *              v4.0  2018-12-14  添加：支Java对象用Map<Object ,Object>定义成员变量的类型，来支持多属性值情况下，旧值改为新值的场景。并且支持批量。
 *                                添加：相同名称的@Ladp("名称")注解，在同一Java对象的多个成员变量上定义。使其更加灵活好用。
 *                                     如下同一属性名称"mobile"，可以定义在同一个Java对象中。
 *                                         @Ldap("mobile") 
 *                                         private String              tel;     // 用于单手机号的场景
 *                                         
 *                                         @Ldap("mobile")
 *                                         private List<String>        tels;    // 用于多手机号的场景
 *                                         
 *                                         @Ldap("mobile")
 *                                         private Map<String ,String> telMap;  // 用于多手机号有指定性修改的场景
 *                                                                              // Map.key   为旧属性值
 *                                                                              // Map.value 为新属性值
 */
public class LdapEntry
{
    
    /** Java值对象类的元类型。即有 @Ldap 注解的类 */
    private Class<?>          metaClass;
    
    /** 
     * LDAP中的"对象类ObjectClass"的名称组合成的ID。内部有自动排序过。区分大小写。
     * 
     * 格式为："对象名称1,对象名称2,...对象名称n"。如，"top,person"。 
     */
    private String            objectClassesID;

    /** 
     * LDAP中的"对象类ObjectClass"的名称。内部有自动排序过。
     * 
     * 格式为：List.get(index) = "对象名称" 。如，top、person等。
     */
    private List<String>       objectClasses;
    
    /** 指DN逗号最左边的部分，最小的子条目 */
    private String             rdn;
    
    /** 获取DN值的getter方法 */
    private Method             dnGetMethod;
    
    /** 设置DN值的setter方法 */
    private Method             dnSetMethod;
    
    /**
     * LDAP中的"属性Attribute"的名称 与对应的 Java对象的getter()方法
     * 
     * 不包括DN的getter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     */
    private TablePartitionRID<String ,Method>  elementsToLDAP;
    
    /**
     * LDAP中的"属性Attribute"的名称 与对应的 Java对象的setter()方法
     * 
     * 不包括DN的setter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的setter()方法
     */
    private TablePartitionRID<String ,Method>  elementsToObject;

    
    
    public LdapEntry()
    {
        this.objectClasses    = new ArrayList<String>();
        this.elementsToLDAP   = new TablePartitionRID<String ,Method>();
        this.elementsToObject = new TablePartitionRID<String ,Method>();
    }
    
    
    
    /**
     * 有部分解释动作的构造器
     *
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_MetaClass      Java值对象类的元类型。即有 @Ldap 注解的类
     * @param i_ObjectClasses  LDAP中的"对象类ObjectClass"的名称。多个有逗号分隔。内部有自动排序过。
     */
    public LdapEntry(Class<?> i_MetaClass ,String i_ObjectClasses)
    {
        this();
        
        this.metaClass       = i_MetaClass;
        this.objectClassesID = StringHelp.replaceAll(i_ObjectClasses ,new String[]{" " ,"\r" ,"\n" ,"\t"} ,new String[]{""});
        String [] v_ObjectClasses = this.objectClassesID.split(",");
        
        for (int i=0; i<v_ObjectClasses.length; i++)
        {
            this.objectClasses.add(v_ObjectClasses[i]);
        }
        
        // 重新排序，生成对象ID
        this.objectClassesID = StringHelp.toString(Help.toSort(this.objectClasses) ,"" ,",");
    }
    
    
    
    /**
     * 获取父节点的DN值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-08
     * @version     v1.0
     *
     * @return
     */
    public String getSuperDNValue(Object i_Values) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        String v_DN = this.getDNValue(i_Values);
        if ( v_DN == null )
        {
            return null;
        }
        
        int v_Index = v_DN.indexOf(",");
        if ( v_Index > 0 )
        {
            return v_DN.substring(v_Index);
        }
        else 
        {
            return "";
        }
    }
    
    
    
    /**
     * 获取DN值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_Values
     * @return
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     */
    public String getDNValue(Object i_Values) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if ( this.dnGetMethod != null )
        {
            Object v_Value = this.dnGetMethod.invoke(i_Values);
            if ( v_Value != null )
            {
                return v_Value.toString();
            }
        }
        
        return null;
    }
    
    
    
    /**
     * Java成员的数值，转成LDAP的属性值，支持同一属性的多个属性值。
     * 
     * 多属性值时，有排序动作
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-07
     * @version     v1.0
     *
     * @param i_Data
     * @return
     */
    public static String [] dataToLDAPAttributes(Object i_Data)
    {
        if ( i_Data == null )
        {
            return new String[] {};
        }
        
        List<String> v_Attrs = new ArrayList<String>();
        
        if ( i_Data instanceof List )
        {
            List<?> v_Datas = (List<?>)i_Data;
            
            for (Object v_Item : v_Datas)
            {
                if ( v_Item != null )
                {
                    String v_ItemValue = v_Item.toString();
                    if ( !Help.isNull(v_ItemValue) )
                    {
                        v_Attrs.add(v_ItemValue);
                    }
                }
            }
        }
        else if ( i_Data instanceof Set )
        {
            Iterator<?> v_Datas = ((Set<?>)i_Data).iterator();
            
            while (v_Datas.hasNext())
            {
                Object v_Item = v_Datas.next();
                if ( v_Item != null )
                {
                    String v_ItemValue = v_Item.toString();
                    if ( !Help.isNull(v_ItemValue) )
                    {
                        v_Attrs.add(v_ItemValue);
                    }
                }
            }
        }
        else if ( i_Data instanceof Object [] )
        {
            Object [] v_Datas = (Object [])i_Data;
            
            for (Object v_Item : v_Datas)
            {
                if ( v_Item != null )
                {
                    String v_ItemValue = v_Item.toString();
                    if ( !Help.isNull(v_ItemValue) )
                    {
                        v_Attrs.add(v_ItemValue);
                    }
                }
            }
        }
        else if ( i_Data instanceof Map )
        {
            Map<? ,?> v_Datas = (Map<? ,?>)i_Data;
            
            for (Object v_Item : v_Datas.keySet())
            {
                if ( v_Item != null )
                {
                    String v_ItemValue = v_Item.toString();
                    if ( !Help.isNull(v_ItemValue) )
                    {
                        v_Attrs.add(v_ItemValue);
                    }
                }
            }
        }
        else
        {
            String v_ItemValue = i_Data.toString();
            if ( !Help.isNull(v_ItemValue) )
            {
                v_Attrs.add(v_ItemValue);
            }
        }
        
        return v_Attrs.toArray(new String[] {});
    }
    
    
    
    /**
     * 将Java值对象翻译为LDAP条目
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *              v2.0  2018-12-06  添加：支持同一属性多个属性值的LDAP特性。
     *                                     Java对象用List<Object>或Set<Object>或数组Object[]定义成员变量的类型，来支持多属性值的LDAP特性。
     *                                     当Java成员变量为String这样的简单类型时，LDAP中同一属性有多个属性值时，随机取一个给Java成员变量赋值。
     *
     * @param i_Values  Java值对象
     * @return
     * @throws LdapException
     */
    public Entry toEntry(Object i_Values) throws LdapException
    {
        DefaultEntry v_Entry = new DefaultEntry();
        String       v_DN    = null;
        
        // 设置LDAP的ObjectClass
        for (int v_Index=0; v_Index<this.objectClasses.size(); v_Index++)
        {
            v_Entry.add(LDAP.$ObjectClass ,this.objectClasses.get(v_Index));
        }
        
        // 设置LDAP的DN
        try
        {
            v_DN = this.getDNValue(i_Values);
            if ( !Help.isNull(v_DN) )
            {
                v_Entry.setDn(v_DN);
            }
        }
        catch (Exception exce)
        {
            System.out.println(Date.getNowTime().getFull() + " LDAP DN get method(" + this.dnGetMethod.getName() + ") value is error.");
            exce.printStackTrace();
            // 不返回，允许DN未设置的情况出现
        }
        
        // 设置LDAP的属性
        for (Map.Entry<String ,Map<String ,Method>> v_Item : this.elementsToLDAP.entrySet())
        {
            for (Map.Entry<String ,Method> v_ItemMethod : v_Item.getValue().entrySet())
            {
                try
                {
                    Object    v_Value      = v_ItemMethod.getValue().invoke(i_Values);
                    String [] v_AttrValues = dataToLDAPAttributes(v_Value);
                    
                    if ( !Help.isNull(v_AttrValues) )
                    {
                        v_Entry.add(v_Item.getKey() ,v_AttrValues);
                    }
                }
                catch (Exception exce)
                {
                    System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") get <" + v_ItemMethod.getKey() + "> method(" + v_ItemMethod.getValue().getName() + ") value is error.");
                    exce.printStackTrace();
                }
            }
        }
        
        return v_Entry;
    }
    
    
    
    /**
     * 将LDAP条目翻译为Java值对象的实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *              v2.0  2018-12-06  添加：支持同一属性多个属性值的LDAP特性。
     *                                     Java对象用List<Object>或Set<Object>或数组Object[]定义成员变量的类型，来支持多属性值的LDAP特性。
     *                                     当Java成员变量为String这样的简单时，LDAP中同一属性有多个属性值时，随机取一个给Java成员变量赋值。
     *
     * @param i_Entry   条目对象
     * @return
     */
    public Object toObject(Entry i_Entry)
    {
        Object v_Ret = this.newObject();
        
        if ( v_Ret == null )
        {
            return v_Ret;
        }
        
        // 设置Java的DN
        if ( this.dnSetMethod != null )
        {
            try
            {
                Object v_MethodParam = Help.toObject(this.dnSetMethod.getParameterTypes()[0] ,i_Entry.getDn().toString());
                this.dnSetMethod.invoke(v_Ret ,v_MethodParam);
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP DN set method(" + this.dnSetMethod.getName() + ") value is error.");
                exce.printStackTrace();
            }
        }
        
        // 设置Java的属性
        for (Map.Entry<String ,Map<String ,Method>> v_Item : this.elementsToObject.entrySet())
        {
            for (Map.Entry<String ,Method> v_ItemMethod : v_Item.getValue().entrySet())
            {
                try
                {
                    Attribute v_Attribute = i_Entry.get(v_Item.getKey());
                    if ( v_Attribute != null )
                    {
                        Object v_MPValue = null;
                        
                        if ( MethodReflect.isExtendImplement(v_ItemMethod.getValue().getParameterTypes()[0] ,List.class) )
                        {
                            Class<?>        v_ListItemClass = MethodReflect.getGenerics(v_ItemMethod.getValue() ,0 ,0);
                            List<Object>    v_Values        = new ArrayList<Object>();
                            Iterator<Value> v_Iter          = v_Attribute.iterator();
                            
                            v_ListItemClass = Help.NVL(v_ListItemClass ,String.class);
                            while (v_Iter.hasNext())
                            {
                                Value v_Value = v_Iter.next();
                                
                                v_Values.add(Help.toObject(v_ListItemClass ,v_Value.getValue()));
                            }
                            
                            v_MPValue = v_Values;
                        }
                        else if ( MethodReflect.isExtendImplement(v_ItemMethod.getValue().getParameterTypes()[0] ,Set.class) )
                        {
                            Class<?>        v_SetItemClass = MethodReflect.getGenerics(v_ItemMethod.getValue() ,0 ,0);
                            Set<Object>     v_Values       = new HashSet<Object>();
                            Iterator<Value> v_Iter         = v_Attribute.iterator();
                            
                            v_SetItemClass = Help.NVL(v_SetItemClass ,String.class);
                            while (v_Iter.hasNext())
                            {
                                Value v_Value = v_Iter.next();
                                
                                v_Values.add(Help.toObject(v_SetItemClass ,v_Value.getValue()));
                            }
                            
                            v_MPValue = v_Values;
                        }
                        else if ( v_ItemMethod.getValue().getParameterTypes()[0].isArray() )
                        {
                            Class<?>        v_ArrayItemClass = v_ItemMethod.getValue().getParameterTypes()[0].getComponentType();
                            Iterator<Value> v_Iter           = v_Attribute.iterator();
                            int             v_Index          = 0;
                            
                            v_ArrayItemClass = Help.NVL(v_ArrayItemClass ,String.class);
                            v_MPValue = Array.newInstance(v_ArrayItemClass ,v_Attribute.size());
                            
                            while (v_Iter.hasNext())
                            {
                                Value v_Value = v_Iter.next();
                                
                                Array.set(v_MPValue ,v_Index++ ,Help.toObject(v_ArrayItemClass ,v_Value.getValue()));
                            }
                        }
                        else if ( MethodReflect.isExtendImplement(v_ItemMethod.getValue().getParameterTypes()[0] ,Map.class) )
                        {
                            Class<?>            v_MapItemClass = MethodReflect.getGenerics(v_ItemMethod.getValue() ,0 ,0);
                            Map<Object ,Object> v_Values       = new HashMap<Object ,Object>();
                            Iterator<Value>     v_Iter         = v_Attribute.iterator();
                            
                            v_MapItemClass = Help.NVL(v_MapItemClass ,String.class);
                            while (v_Iter.hasNext())
                            {
                                Value v_Value = v_Iter.next();
                                
                                Object v_AttrValue = Help.toObject(v_MapItemClass ,v_Value.getValue());
                                v_Values.put(v_AttrValue ,v_AttrValue);
                            }
                            
                            v_MPValue = v_Values;
                        }
                        else
                        {
                            Value v_Value = v_Attribute.get();
                            if ( v_Value != null )
                            {
                                // 1.0.0版本中用的v_Value.getString()
                                v_MPValue = Help.toObject(v_ItemMethod.getValue().getParameterTypes()[0] ,v_Value.getValue());
                            }
                            else
                            {
                                v_MPValue = "";
                            }
                        }
                        
                        v_ItemMethod.getValue().invoke(v_Ret ,v_MPValue);
                    }
                }
                catch (Exception exce)
                {
                    System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") set <" + v_ItemMethod.getKey() + "> method(" + v_ItemMethod.getValue().getName() + ") value is error.");
                    exce.printStackTrace();
                }
            }
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 用于修改条目属性前，先查询LDAP服务中的条目。再与Java新值对比后得出要执行修改动作。
     * 
     * 可自动对比出，哪些是要添加的属性；
     * 可自动对比出，哪些是要修改的属性；
     * 可自动对比出，哪些是要删除的属性；
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
     * @param i_OldValues  LDAP服务中的旧值
     * @param i_NewValues  Java对象中的新值
     * @param i_IsAdd      当LDAP中没有时，是否新增LDAP属性
     * @param i_IsUpdate   当新旧不同时，是否修改LDAP中对应的属性
     * @param i_IsDel      当Java属性值为null时，是否删除LDAP中对应的属性
     * @return
     */
    @SuppressWarnings("unchecked")
    public Return<ModifyRequest> toModify(Object i_OldValues ,Object i_NewValues 
                                 ,boolean i_IsAdd
                                 ,boolean i_IsUpdate
                                 ,boolean i_IsDel)
    {
        Return<ModifyRequest> v_Ret = new Return<ModifyRequest>();
        v_Ret.set(true).setParamInt(0);
        
        if ( i_OldValues == null )
        {
            return v_Ret;
        }
        
        ModifyRequest v_Request = new ModifyRequestImpl();
        String        v_DN      = null;
        int           v_MCount  = 0;
        
        // 设置LDAP的DN
        try
        {
            v_DN = this.getDNValue(i_NewValues);
            if ( !Help.isNull(v_DN) )
            {
                v_Request.setName(new Dn(v_DN));
            }
        }
        catch (Exception exce)
        {
            System.out.println(Date.getNowTime().getFull() + " LDAP DN get method(" + this.dnGetMethod.getName() + ") value is error.");
            exce.printStackTrace();
            // 不返回，允许DN未设置的情况出现
        }
        
        // 设置LDAP的属性
        for (Map.Entry<String ,Map<String ,Method>> v_Item : this.elementsToLDAP.entrySet())
        {
            for (Map.Entry<String ,Method> v_ItemMethod : v_Item.getValue().entrySet())
            {
                try
                {
                    Object v_NewValue = v_ItemMethod.getValue().invoke(i_NewValues);
                    if ( v_NewValue != null )
                    {
                        if ( i_IsAdd || i_IsUpdate )
                        {
                            Object v_OldValue = v_ItemMethod.getValue().invoke(i_OldValues);
                            if ( v_OldValue != null )
                            {
                                String [] v_AttrValues = dataToLDAPAttributes(v_NewValue);
                                if ( !Help.isNull(v_AttrValues) )
                                {
                                    if ( v_AttrValues.length >= 2
                                     && (v_NewValue instanceof List
                                     ||  v_NewValue instanceof Set
                                     ||  v_NewValue instanceof Object []) )
                                    {
                                        String []                  v_AttrOlds = dataToLDAPAttributes(v_OldValue);
                                        ComparateResult<String []> v_CResult  = Comparate.comparate(v_AttrOlds ,v_AttrValues);
                                        
                                        if ( i_IsAdd && !Help.isNull(v_CResult.getNewData()) )
                                        {
                                            // 多个属性值的情况下：添加新的属性值（当新增标记为真时）
                                            v_Request.addModification(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE    ,v_Item.getKey() ,v_CResult.getNewData()));
                                            v_MCount++;
                                        }
                                        if ( i_IsUpdate && !Help.isNull(v_CResult.getDelData()) )
                                        {
                                            // 多个属性值的情况下：删除旧的属性值（当修改标记为真时）
                                            // 注：多属性值的删除，相当于对属性的修改，而不属性删除
                                            v_Request.addModification(new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE ,v_Item.getKey() ,v_CResult.getDelData()));
                                            v_MCount++;
                                        }
                                    }
                                    else if ( v_NewValue instanceof Map )
                                    {
                                        ComparateResult<Map<Object ,Object>> v_CResult = Comparate.comparate((Map<Object ,Object>)v_OldValue ,(Map<Object ,Object>)v_NewValue);
                                        
                                        if ( i_IsAdd && !Help.isNull(v_CResult.getNewData()) )
                                        {
                                            v_AttrValues = dataToLDAPAttributes(Help.toListKeys(v_CResult.getNewData()));
                                            // 多个属性值的情况下：添加新的属性值（当新增标记为真时）
                                            v_Request.addModification(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE ,v_Item.getKey() ,v_AttrValues));
                                            v_MCount++;
                                        }
                                        if ( i_IsUpdate && !Help.isNull(v_CResult.getDelData()) )
                                        {
                                            v_AttrValues = dataToLDAPAttributes(Help.toListKeys(v_CResult.getDelData()));
                                            // 多个属性值的情况下：删除旧的属性值（当修改标记为真时）
                                            // 注：多属性值的删除，相当于对属性的修改，而不属性删除
                                            v_Request.addModification(new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE ,v_Item.getKey() ,v_AttrValues));
                                            v_MCount++;
                                        }
                                        if ( i_IsUpdate && !Help.isNull(v_CResult.getDiffData()) )
                                        {
                                            String [] v_AttrOlds = dataToLDAPAttributes(Help.toListKeys(v_CResult.getDiffData()));
                                            v_AttrValues         = dataToLDAPAttributes(Help.toList(    v_CResult.getDiffData()));
                                            // 多个属性值的情况下：修改旧的属性值（当修改标记为真时）
                                            // 注：先删除，后添加，不能使用 ModificationOperation.REPLACE_ATTRIBUTE ，应为它是全部属性值替换
                                            v_Request.addModification(new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE ,v_Item.getKey() ,v_AttrOlds));
                                            v_Request.addModification(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE    ,v_Item.getKey() ,v_AttrValues));
                                            v_MCount++;
                                        }
                                    }
                                    else if ( i_IsUpdate && !v_NewValue.equals(v_OldValue) )
                                    {
                                        // 修改属性值
                                        v_Request.addModification(new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE ,v_Item.getKey() ,v_AttrValues));
                                        v_MCount++;
                                    }
                                }
                            }
                            else if ( i_IsAdd )
                            {
                                String [] v_AttrValues = dataToLDAPAttributes(v_NewValue);
                                if ( !Help.isNull(v_AttrValues) )
                                {
                                    // 添加属性
                                    v_Request.addModification(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE ,v_Item.getKey() ,v_AttrValues));
                                    v_MCount++;
                                }
                            }
                        }
                    }
                    // 当Java属性值为null时，删除LDAP中对应的属性
                    else if ( i_IsDel )
                    {
                        Object v_OldValue = v_ItemMethod.getValue().invoke(i_OldValues);
                        if ( v_OldValue != null )
                        {
                            // 删除属性
                            v_Request.addModification(new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE ,v_Item.getKey()));
                            v_MCount++;
                        }
                    }
                }
                catch (Exception exce)
                {
                    System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") get <" + v_ItemMethod.getKey() + "> method(" + v_ItemMethod.getValue().getName() + ") value is error.");
                    exce.printStackTrace();
                }
            }
        }
        
        v_Ret.setParamObj(v_Request);
        v_Ret.setParamInt(v_MCount);
        v_Ret.set(v_MCount >= 1);
        
        return v_Ret;
    }
    
    
    
    /**
     * 添加元素
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Name
     * @param i_GetMethod
     * @param i_SetMethod
     */
    public void putElement(String i_Name ,Method i_GetMethod ,Method i_SetMethod)
    {
        if ( i_GetMethod != null )
        {
            this.elementsToLDAP.putRow(i_Name ,i_GetMethod.getReturnType().getName() ,i_GetMethod);
        }
        
        if ( i_SetMethod != null )
        {
            this.elementsToObject.putRow(i_Name ,i_SetMethod.getParameterTypes()[0].getName() ,i_SetMethod);
        }
    }
    
    
    
    /**
     * 构造Java值对象类
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @return
     */
    private Object newObject()
    {
        try
        {
            return this.metaClass.newInstance();
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return null;
    }


    
    /**
     * 获取：Java值对象类的元类型。即有 @Ldap 注解的类
     */
    public Class<?> getMetaClass()
    {
        return metaClass;
    }


    
    /**
     * 设置：Java值对象类的元类型。即有 @Ldap 注解的类
     * 
     * @param metaClass 
     */
    public void setMetaClass(Class<?> metaClass)
    {
        this.metaClass = metaClass;
    }


    
    /**
     * 获取：LDAP中的"对象类ObjectClass"的名称组合成的ID。
     * 
     * 格式为："对象名称1,对象名称2,...对象名称n"。如，"top,person"。
     */
    public String getObjectClassesID()
    {
        return objectClassesID;
    }



    /**
     * 设置：LDAP中的"对象类ObjectClass"的名称组合成的ID。
     * 
     * 格式为："对象名称1,对象名称2,...对象名称n"。如，"top,person"。
     * 
     * @param objectClassesID 
     */
    public void setObjectClassesID(String objectClassesID)
    {
        this.objectClassesID = objectClassesID;
    }



    /**
     * 获取：LDAP中的"对象类ObjectClass"的名称。
     * 
     * 格式为：List.get(index) = "对象名称" 。如，top、person等。
     */
    public List<String> getObjectClasses()
    {
        return objectClasses;
    }


    
    /**
     * 设置：LDAP中的"对象类ObjectClass"的名称。
     * 
     * 格式为：List.get(index) = "对象名称" 。如，top、person等。
     * 
     * @param objectClasses 
     */
    public void setObjectClasses(List<String> objectClasses)
    {
        this.objectClasses = objectClasses;
    }

    
    
    /**
     * 获取：获取DN值的getter方法
     */
    public Method getDnGetMethod()
    {
        return dnGetMethod;
    }


    
    /**
     * 设置：获取DN值的getter方法
     * 
     * @param dnGetMethod 
     */
    public void setDnGetMethod(Method dnGetMethod)
    {
        this.dnGetMethod = dnGetMethod;
    }


    
    /**
     * 获取：设置DN值的setter方法
     */
    public Method getDnSetMethod()
    {
        return dnSetMethod;
    }


    
    /**
     * 设置：设置DN值的setter方法
     * 
     * @param dnSetMethod 
     */
    public void setDnSetMethod(Method dnSetMethod)
    {
        this.dnSetMethod = dnSetMethod;
    }



    /**
     * 获取：LDAP中的"属性Attribute"的名称 与对应的 Java对象的getter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     */
    public TablePartitionRID<String ,Method> getElementsToLDAP()
    {
        return elementsToLDAP;
    }


    
    /**
     * 设置：LDAP中的"属性Attribute"的名称 与对应的 Java对象的getter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     * 
     * @param elementsToLDAP 
     */
    public void setElementsToLDAP(TablePartitionRID<String ,Method> elementsToLDAP)
    {
        this.elementsToLDAP = elementsToLDAP;
    }


    
    /**
     * 获取：LDAP中的"属性Attribute"的名称 与对应的 Java对象的setter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的setter()方法
     */
    public TablePartitionRID<String ,Method> getElementsToObject()
    {
        return elementsToObject;
    }


    
    /**
     * 设置：LDAP中的"属性Attribute"的名称 与对应的 Java对象的setter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的setter()方法
     * 
     * @param elementsToObject 
     */
    public void setElementsToObject(TablePartitionRID<String ,Method> elementsToObject)
    {
        this.elementsToObject = elementsToObject;
    }


    
    /**
     * 获取：指DN逗号最左边的部分，最小的子条目
     */
    public String getRdn()
    {
        return rdn;
    }

    
    
    /**
     * 设置：指DN逗号最左边的部分，最小的子条目
     * 
     * @param rdn 
     */
    public void setRdn(String rdn)
    {
        this.rdn = rdn;
    }
    
}
