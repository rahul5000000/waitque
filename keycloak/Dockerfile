FROM quay.io/keycloak/keycloak:26.3.2

USER root
COPY themes /opt/keycloak/providers
USER keycloak

RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev", "--http-port=8080"]
