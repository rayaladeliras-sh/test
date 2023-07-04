kill -9 $(lsof -t -i:8080) 2>/dev/null

# build
mvn clean package -DskipTests -Denforcer.skip=true -Pdevlocal

# if build failure return
if [ $? -eq 0 ]; then
    echo BUILD OK
else
    echo BUILD FAIL
    exit 1
fi

# deploy war
rm -rf /opt/jboss/server/default/deploy/stubhub-domain/*.war

cp war/target/*.war /opt/jboss/server/default/deploy/stubhub-domain/

# clean log
rm /opt/jboss/server/default/log/*
touch /opt/jboss/server/default/log/jboss.log

# start
export NAS_PROPERTY_HOME=/etc/stubhub/properties

# require 1.7
# sudo scp -r "$(whoami)@slce009slx001.slce009.com:/usr/java/default/jre/lib/security" "$(/usr/libexec/java_home -v 1.7)/jre/lib/"
rm /opt/java
ln -s "$(/usr/libexec/java_home -v 1.7)" /opt/java

sh /opt/jboss/bin/run.sh -c default -b 0.0.0.0 &

#rm /opt/java
#ln -s $JAVA_HOME /opt/java

# tail log
tail -f /opt/jboss/server/default/log/jboss.log
