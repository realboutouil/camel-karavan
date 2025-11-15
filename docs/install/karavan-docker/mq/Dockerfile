FROM --platform=linux/amd64 icr.io/ibm-messaging/mq:9.2.5.0-r3

USER 1001

COPY mqwebuser.xml /etc/mqm/web/installations/Installation1/servers/mqweb/
COPY 20-config.mqsc /etc/mqm/
