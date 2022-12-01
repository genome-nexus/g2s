#!/usr/bin/env bash

MAVEN_BINARY="/usr/local/apache-maven/apache-maven-3.6.3/bin/mvn"
PROJECT_HOME="/home/ec2-user/pdb-annotation"
CERTBOT_HOME="/home/ec2-user"
DOMAIN_NAME="g2s.genomenexus.org"
WEBROOT="/usr/share/tomcat8/webapps/ROOT"
CERT_CA_NAME="g2s"
CERT_PASSWORD="123456"
MAX_WAIT_SHUTDOWN_SEC=8
MAX_WAIT_STARTUP_SEC=8
POST_STARTUP_WAIT_SEC=3
RECOVERY_ATTEMPT_WAIT_SEC=60

function start_up_spring_boot() {
    cd $PROJECT_HOME/pdb-alignment-web
    $MAVEN_BINARY spring-boot:run >/dev/null 2>&1 &
}

function kill_spring_boot() {
    # TODO this assumes that we don't have any java processes other than initiated by spring-boot:run, we should probably be more specific
    pkill -f "java"
}

function spring_boot_is_now_running() {
    pgrep -f "java" > /dev/null 2>&1
}

function start_tomcat() {
    sudo service tomcat8 start
}

function kill_tomcat() {
    sudo service tomcat8 stop
}

function tomcat_is_now_running() {
    pgrep -f "tomcat" > /dev/null 2>&1
}

function shutdown_spring_boot_with_short_wait() {
    kill_spring_boot
    i=0
    while spring_boot_is_now_running ; do
        sleep 1
        i=$((i+1))
        if [ $i -ge $MAX_WAIT_SHUTDOWN_SEC ] ; then
            echo "warning : spring boot was still running $MAX_WAIT_SHUTDOWN_SEC seconds after the shutdown attempt. Continuing anyway." >&2
            break
        fi
    done
}

function start_up_tomcat_with_short_wait() {
    start_tomcat
    i=0
    while ! tomcat_is_now_running ; do
        sleep 1
        i=$((i+1))
        if [ $i -ge $MAX_WAIT_STARTUP_SEC ] ; then
            echo "warning : tomcat was not running $MAX_WAIT_STARTUP_SEC seconds after the start up attempt. Continuing anyway." >&2
            break
        fi
    done
    sleep $POST_STARTUP_WAIT_SEC
}

function shutdown_tomcat_with_short_wait() {
    kill_tomcat
    i=0
    while tomcat_is_now_running ; do
        sleep 1
        i=$((i+1))
        if [ $i -ge $MAX_WAIT_SHUTDOWN_SEC ] ; then
            echo "warning : tomcat was still running $MAX_WAIT_SHUTDOWN_SEC seconds after the shutdown attempt. Continuing anyway." >&2
            break
        fi
    done
}

function renew_certificate() {
    # run certbot to generate a new cert
    sudo $CERTBOT_HOME/certbot-auto certonly --webroot --webroot-path $WEBROOT --domains $DOMAIN_NAME
    # export the cert
    sudo openssl pkcs12 -export -in /etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem -inkey /etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem -out keystore.p12 -name tomcat -CAfile /etc/letsencrypt/live/$DOMAIN_NAME/chain.pem -caname $CERT_CA_NAME -passout pass:$CERT_PASSWORD
    # copy credential to the project
    cp -a keystore.p12 $PROJECT_HOME/pdb-alignment-web/src/main/resources
}

function start_up_spring_boot_with_recovery() {
    start_up_spring_boot
    sleep $RECOVERY_ATTEMPT_WAIT_SEC
    if ! spring_boot_is_now_running ; then
        echo "Warning : spring boot startup seems to have failed. Process is not running after $RECOVERY_ATTEMPT_WAIT_SEC seconds. Attempting emergency restart."
        sleep $POST_STARTUP_WAIT_SEC
        shutdown_spring_boot_with_short_wait
        shutdown_tomcat_with_short_wait
        sleep $POST_STARTUP_WAIT_SEC
        start_up_spring_boot
        sleep $POST_STARTUP_WAIT_SEC
        if spring_boot_is_now_running ; then
            echo "emergency restart seems to have succeeded."
        else
            echo "emergency restart seems to have failed."
        fi
    fi
}

function main() {
    shutdown_spring_boot_with_short_wait
    start_up_tomcat_with_short_wait
    renew_certificate
    shutdown_tomcat_with_short_wait
    start_up_spring_boot_with_recovery
}

main
