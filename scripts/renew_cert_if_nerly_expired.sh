#!/usr/bin/env bash

MAX_SEC_TO_EXPIRY_WITHOUT_WARNING=$((11*24*60*60)) # 11 days
TMP_DIR="/tmp/g2s_check"
G2S_HOSTNAME="g2s.genomenexus.org"
RESPONSE_FILENAME="$TMP_DIR/g2s_cert_response.txt"
PEM_FILENAME="$TMP_DIR/g2s_certs.pem"
CERTIFICATE_TEXT_FILENAME="$TMP_DIR/cert.txt"
RENEW_CERT_SCRIPT_FILEPATH="/home/ec2-user/renew_cert.sh"

function make_tmp_dir_if_necessary() {
    if ! [ -d "$TMP_DIR" ] ; then
        if ! mkdir -p "$TMP_DIR" ; then
            echo "Error : could not create tmp directory '$TMP_DIR'" >&2
        exit 1
        fi
    fi
}

function request_certificate() {
    if ! openssl s_client -showcerts -connect $G2S_HOSTNAME:443 < /dev/null > "$RESPONSE_FILENAME" 2> /dev/null ; then
        echo "error : could not fetch certificate from server $G2S_HOSTNAME"
        exit 1
    fi
}

function trim_certificate() {
    if ! sed -n -e '/-.BEGIN/,/-.END/ p' "$RESPONSE_FILENAME" > "$PEM_FILENAME" ; then
        echo "error : could not extract pem content from response from $G2S_HOSTNAME response"
        exit 1
    fi
}

function extract_certificate_text() {
    if ! openssl x509 -in "$PEM_FILENAME" -text > "$CERTIFICATE_TEXT_FILENAME" ; then
        echo "error : pem content from $G2S_HOSTNAME is not in recognizable format"
        exit 1
    fi
}

function extract_expiration_date() {
    unset cert_expiry_date
    cert_expiry_date=$(grep -A1 "Not Before" "$CERTIFICATE_TEXT_FILENAME" | head -n 2 | grep "Not After" | sed "s/.*[ ]:[ ]//")
    if [ -z "$cert_expiry_date" ] ; then
        echo "error : could not locate expiration date (Not After) from $G2S_HOSTNAME"
        exit 1
    fi
}

function computes_seconds_until_expiration() {
    unset sec_to_expiry
    cert_expiry_sec_since_epoc=$(date --date="$cert_expiry_date" +%s)
    current_sec_since_epoc=$(date +%s)
    sec_to_expiry=$(( $cert_expiry_sec_since_epoc - $current_sec_since_epoc ))
}

function renew_cert_if_necessary() {
    if [ "$sec_to_expiry" -lt "$MAX_SEC_TO_EXPIRY_WITHOUT_WARNING" ] ; then
	date
	echo "attempting to renew certificate"
        $RENEW_CERT_SCRIPT_FILEPATH
    fi
}

function clean_up() {
    rm -f "$G2S_HOSTNAME" "$RESPONSE_FILENAME" "$PEM_FILENAME" "$CERTIFICATE_TEXT_FILENAME"
}

function main() {
    make_tmp_dir_if_necessary
    request_certificate
    trim_certificate
    extract_certificate_text
    extract_expiration_date
    computes_seconds_until_expiration
    renew_cert_if_necessary
    clean_up
}

main
