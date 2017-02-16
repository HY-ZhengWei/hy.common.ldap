package org.hy.common.ldap.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;





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
 */
public class LdapEntry
{
    
    /** Java值对象类的元类型。即有 @Ldap 注解的类 */
    private Class<?>          metaClass;
    
    /** 
     * LDAP中的"对象类ObjectClass"的名称组合成的ID。有先后顺序
     * 
     * 格式为："对象名称1,对象名称2,...对象名称n"。如，"top,person"。 
     */
    private String            objectClassesID;

    /** 
     * LDAP中的"对象类ObjectClass"的名称。有先后顺序
     * 
     * 格式为：List.get(index) = "对象名称" 。如，top、person等。
     */
    private List<String>       objectClasses;
    
    /**
     * LDAP中的"属性Attribute"的名称 与对应的 Java对象的getter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     */
    private Map<String ,Method>  elementsToLDAP;
    
    /**
     * LDAP中的"属性Attribute"的名称 与对应的 Java对象的setter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的setter()方法
     */
    private Map<String ,Method>  elementsToObject;

    
    
    public LdapEntry()
    {
        this.objectClasses    = new ArrayList<String>();
        this.elementsToLDAP   = new LinkedHashMap<String ,Method>();
        this.elementsToObject = new LinkedHashMap<String ,Method>();
    }
    
    
    
    /**
     * 有部分解释动作的构造器
     *
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_MetaClass      Java值对象类的元类型。即有 @Ldap 注解的类
     * @param i_ObjectClasses  LDAP中的"对象类ObjectClass"的名称。多个有逗号分隔。有先后有顺序。
     */
    public LdapEntry(Class<?> i_MetaClass ,String i_ObjectClasses)
    {
        this();
        
        this.metaClass            = i_MetaClass;
        this.objectClassesID      = StringHelp.replaceAll(i_ObjectClasses ,new String[]{" " ,"\r" ,"\n" ,"\t"} ,new String[]{""});
        String [] v_ObjectClasses = this.objectClassesID.split(",");
        
        for (int i=0; i<v_ObjectClasses.length; i++)
        {
            this.objectClasses.add(v_ObjectClasses[i]);
        }
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
            this.elementsToLDAP  .put(i_Name ,i_GetMethod);
        }
        
        if ( i_SetMethod != null )
        {
            this.elementsToObject.put(i_Name ,i_SetMethod);
        }
    }
    
    
    
    /**
     * 将Java值对象翻译为LDAP条目
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Values  Java值对象
     * @return
     * @throws LdapException
     */
    public Entry toEntry(Object i_Values) throws LdapException
    {
        DefaultEntry v_Entry = new DefaultEntry();
        
        for (int v_Index=0; v_Index<this.objectClasses.size(); v_Index++)
        {
            v_Entry.add("objectClass" ,this.objectClasses.get(v_Index));
        }
        
        for (Map.Entry<String ,Method> v_Item : this.elementsToLDAP.entrySet())
        {
            try
            {
                Object v_Value = v_Item.getValue().invoke(i_Values);
                if ( v_Value != null )
                {
                    v_Entry.add(v_Item.getKey() ,v_Value.toString());
                }
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") get method(" + v_Item.getValue().getName() + ") value is error.");
                exce.printStackTrace();
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
        
        for (Map.Entry<String ,Method> v_Item : this.elementsToObject.entrySet())
        {
            try
            {
                Attribute v_Attribute = i_Entry.get(v_Item.getKey());
                if ( v_Attribute != null )
                {
                    Value<?> v_Value = v_Attribute.get();
                    if ( v_Value != null )
                    {
                        Object v_MethodParam = Help.toObject(v_Item.getValue().getParameterTypes()[0] ,v_Value.getString());
                        v_Item.getValue().invoke(v_Ret ,v_MethodParam);
                    }
                }
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") set method(" + v_Item.getValue().getName() + ") value is error.");
                exce.printStackTrace();
            }
        }
        
        return v_Ret;
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
     * 获取：LDAP中的"属性Attribute"的名称 与对应的 Java对象的getter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     */
    public Map<String ,Method> getElementsToLDAP()
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
    public void setElementsToLDAP(Map<String ,Method> elementsToLDAP)
    {
        this.elementsToLDAP = elementsToLDAP;
    }


    
    /**
     * 获取：LDAP中的"属性Attribute"的名称 与对应的 Java对象的setter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的setter()方法
     */
    public Map<String ,Method> getElementsToObject()
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
    public void setElementsToObject(Map<String ,Method> elementsToObject)
    {
        this.elementsToObject = elementsToObject;
    }
    
}
