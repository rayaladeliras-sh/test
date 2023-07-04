export me=`whoami`
export env=$1
export role=$2
sudo scp -r "${me}@${env}${role}001.${env}.com:/etc/stubhub/properties/ENV.stubhub.properties" /etc/stubhub/properties/ENV.stubhub.properties
sudo chown "$me"  /etc/stubhub/properties/ENV.stubhub.properties

sudo scp -r "${me}@${env}${role}001.${env}.com:/etc/stubhub/properties/config/com.stubhub.env.infrastructure.metainfo" /etc/stubhub/properties/config/com.stubhub.env.infrastructure.metainfo
sudo chown "$me"  /etc/stubhub/properties/config/com.stubhub.env.infrastructure.metainfo

export version=`cat /etc/stubhub/properties/config/com.stubhub.env.infrastructure.metainfo | grep 'prop.version' | cut -d'=' -f2`

sudo scp -r "${me}@${env}${role}001.${env}.com:/etc/stubhub/properties/config/com.stubhub.env.infrastructure.properties-${version}.properties" "/etc/stubhub/properties/config/com.stubhub.env.infrastructure.properties-${version}.properties"
sudo chown "$me"  "/etc/stubhub/properties/config/com.stubhub.env.infrastructure.properties-${version}.properties"

#requird for slx
scp -r "${me}@${env}${role}001.${env}.com:/opt/jboss/server/default/deploy/oracle-*.xml" /opt/jboss/server/default/deploy/

