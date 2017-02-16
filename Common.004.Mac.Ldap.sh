#!/bin/sh

cd ./bin

rm -R ./org/hy/common/ldap/junit

jar cvfm hy.common.ldap.jar MANIFEST.MF LICENSE org

cp hy.common.ldap.jar ..
rm hy.common.ldap.jar
cd ..

