

del /Q hy.common.ldap.jar
del /Q hy.common.ldap-sources.jar


call mvn clean package
cd .\target\classes

rd /s/q .\org\hy\common\ldap\junit


jar cvfm hy.common.ldap.jar META-INF/MANIFEST.MF META-INF org

copy hy.common.ldap.jar ..\..
del /q hy.common.ldap.jar
cd ..\..





cd .\src\main\java
xcopy /S ..\resources\* .
jar cvfm hy.common.ldap-sources.jar META-INF\MANIFEST.MF META-INF org 
copy hy.common.ldap-sources.jar ..\..\..
del /Q hy.common.ldap-sources.jar
rd /s/q META-INF
cd ..\..\..
