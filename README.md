# hy.common.ldap


LDAP目录服务的操作类库。基于 Apache LDAP API。

可用于OpenLDAP目录服务的访问操作。



__主导思想：__ 通过 @Ldap 注解十分方便的实现Java写入、读取LDAP目录服务中的数据，同时不破坏、不改造Java程序原有的数据结构。

  __特点1：__ 使用LDAP连接池操作LDAP目录服务。

  __特点2：__ 内部自动获取连接，自动释放连接，无须外界干预。

  __特点3：__ 可用XML配置文件配置，如下（见LDAP.Config.Template.xml）


	<?xml version="1.0" encoding="UTF-8"?>

	<config>
	
		<import name="xconfig"         class="java.util.ArrayList" />
		<import name="connConfig"      class="org.apache.directory.ldap.client.api.LdapConnectionConfig" />
		<import name="connFactory"     class="org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory" />
		<import name="poolConfig"      class="org.hy.common.ldap.LDAPPoolConfig" />
		<import name="connPool"        class="org.apache.directory.ldap.client.api.LdapConnectionPool" />
		<import name="connPoolFactory" class="org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory" />
		<import name="ldap"            class="org.hy.common.ldap.LDAP" />
		
		
		
		<!-- Apache LDAP API配置信息（可支持对OpenLDAP服务的连接） -->
		<xconfig>
		
			<!-- 连接基本配置 -->
			<connConfig id="LDAPConnConfig">
				<ldapHost>127.0.0.1</ldapHost>               <!-- LDAP服务器IP -->
				<ldapPort>389</ldapPort>                     <!-- 访问端号 -->
				<name>cn=Manager,dc=maxcrc,dc=com</name>     <!-- RootDN 用户名称 -->
				<credentials>secret</credentials>            <!-- RootPW 用户密码 -->
			</connConfig>
			
			
			<!-- LDAP连接工厂 -->
			<connFactory id="LDAPConnFactory">
				<constructor>
					<connConfig ref="LDAPConnConfig" />
				</constructor>
				
				<timeOut>30000</timeOut>
			</connFactory>
			
			
			<!-- 连接池参数，下面都是默认值，即可不设置这些参数 -->
			<poolConfig id="LDAPPoolConfig">
				<lifo>true</lifo>
				<maxWait>-1</maxWait>
				<maxActive>8</maxActive>
				<maxIdle>8</maxIdle>
				<minIdle>0</minIdle>
				<minEvictableIdleTimeMillis>1800000</minEvictableIdleTimeMillis>    <!-- 30分钟 -->
				<numTestsPerEvictionRun>3</numTestsPerEvictionRun>
				<softMinEvictableIdleTimeMillis>-1</softMinEvictableIdleTimeMillis>
				<testOnBorrow>false</testOnBorrow>
				<testOnReturn>false</testOnReturn>
				<testWhileIdle>false</testWhileIdle>
				<timeBetweenEvictionRunsMillis>-1</timeBetweenEvictionRunsMillis>
				<whenExhaustedAction>1</whenExhaustedAction>
			</poolConfig>
			
			
			<!-- 构建LDAP连接池 -->
			<connPool id="LDAPConnPool">
				<constructor>
					<connPoolFactory>
						<constructor>
							<connFactory ref="LDAPConnFactory" />
						</constructor>
					</connPoolFactory>
					
					<poolConfig ref="LDAPPoolConfig" />
				</constructor>
			</connPool>
			
			
			<!-- 构建LDAP统一操作类 -->
			<ldap id="LDAP">
				<constructor>
					<connPool ref="LDAPConnPool" />
				</constructor>
			</ldap>
			
		</xconfig>
	
	</config>



### 定义"条目翻译官"的代码样例
条目翻译官有两种作用

  __作用1：__ 将Java值对象翻译为LDAP条目
  
  __作用2：__ 将LDAP条目翻译为Java值对象的实例


	/** 定义ObjectClass。多类名个用英文逗号分隔。 */
	@Ldap("top ,organizationalUnit")
	public class User
	{
	    
	    /** 定义DN */
	    @Ldap(type=LdapType.DN)
	    private String id;
	
		/** 定义属性ou */
	    @Ldap(name="ou")
	    private String name;
	    
	    /** 定义属性userPassword */
	    @Ldap("userPassword")
	    private String password;
	
	    
	    
	    public String getId()
	    {
	        return id;
	    }
	    
	    public void setId(String id)
	    {
	        this.id = id;
	    }
	
	    public String getName()
	    {
	        return name;
	    }
	    
	    public void setName(String name)
	    {
	        this.name = name;
	    }
	
	    public String getPassword()
	    {
	        return password;
	    }
	    
	    public void setPassword(String password)
	    {
	        this.password = password;
	    }
	}



### 添加条目Entry的代码样例
	LDAP v_LDAP = (LDAP)XJava.getObject("LDAP");
	        
	User v_User = new User();
	v_User.setId(      "ou=ZhengWei,dc=maxcrc,dc=com");
	v_User.setName(    "ZhengWei");
	v_User.setPassword("1234567890");
	
	boolean v_Ret = v_LDAP.addEntry(v_User);
	
	
	
### 批量添加条目Entry的代码样例
	LDAP         v_LDAP   = (LDAP)XJava.getObject("LDAP");
	User         v_User01 = new User();
	User         v_User02 = new User();
	List<Object> v_Values = new ArrayList<Object>();
	
	v_User01.setId(      "ou=Batch01,dc=maxcrc,dc=com");
	v_User01.setName(    "Batch01");
	v_User01.setPassword("1234567890");
	
	v_User02.setId(      "ou=Batch02,dc=maxcrc,dc=com");
	v_User02.setName(    "Batch02");
	v_User02.setPassword("0987654321");
	
	v_Values.add(v_User01);
	v_Values.add(v_User02);
	
	boolean v_Ret = v_LDAP.addEntrys(v_Values);
