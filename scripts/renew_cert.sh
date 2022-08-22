#!/usr/bin/env bash

PROJECT_HOME="/home/ec2-user/pdb-annotation"
CERTBOT_HOME="/home/ec2-user"
DOMAIN_NAME="g2s.genomenexus.org"
WEBROOT="/usr/share/tomcat8/webapps/ROOT"
CERT_CA_NAME="g2s"
CERT_PASSWORD="123456"

# stop spring boot
# TODO this assumes that we don't have any java processes other than initiated by spring-boot:run, we should probably be more specific
pkill -f "java"

# start tomcat8 for renewal process
sudo service tomcat8 start

# run certbot to generate a new cert
sudo $CERTBOT_HOME/certbot-auto certonly --webroot --webroot-path $WEBROOT --domains $DOMAIN_NAME

# export the cert
sudo openssl pkcs12 -export -in /etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem -inkey /etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem -out keystore.p12 -name tomcat -CAfile /etc/letsencrypt/live/$DOMAIN_NAME/chain.pem -caname $CERT_CA_NAME -passout pass:$CERT_PASSWORD

# copy credential to the project
cp keystore.p12 $PROJECT_HOME/pdb-alignment-web/src/main/resources

# stop tomcat8 and startup spring-boot
sudo service tomcat8 stop
cd $PROJECT_HOME/pdb-alignment-web
mvn spring-boot:run &
