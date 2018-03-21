.PHONY: build clean push oc-run grafana-ds grafana-dashboard wait20 wait5 all os-deploy-keycloak os-add-service-account os-deploy-monitoring os-deploy-equoid

.PHONY: clean
clean:
	./mvnw clean

.PHONY: build
build:
	./mvnw package -Pprod,prometheus,no-liquibase -DskipTests dockerfile:build

.PHONY: push
push:
	docker tag equoid:0.0.1 $(USER)/equoid
	docker push $(USER)/equoid

.PHONY: oc-run
oc-run:
	-docker kill `docker ps -q` || true
	oc cluster up
	METRICS=1 ./ocp/ocp-apply.sh
	#./ocp/ocp-apply.sh

.PHONY: grafana-ds
grafana-ds:
	curl -i -u admin:equoid \
     -H "Content-Type: application/json;charset=UTF-8" \
     -d '{"Name":"equoid-app","Type":"prometheus","Url":"http://equoid-prometheus:9090","Access":"proxy","basicAuth":false,"isDefault":true}' \
     --trace-ascii /dev/stdout \
     'http://'`oc get routes -l app=equoid-grafana --no-headers | awk '{printf $$2}'`'/api/datasources'

.PHONY: grafana-dashboard
grafana-dashboard:
	curl -i -u admin:equoid -H "Content-Type: application/json;charset=UTF-8" \
     -d @grafana-dashboard.json --trace-ascii /dev/stdout \
     'http://'`oc get routes -l app=equoid-grafana --no-headers | awk '{printf $$2}'`'/api/dashboards/db'

.PHONY: os-deploy-keycloak
os-deploy-keycloak:
	oc secrets new kc-realm jhipster-realm.json=./src/main/docker/realm-config/jhipster-realm.json
	oc secrets new kc-users jhipster-users-0.json=./src/main/docker/realm-config/jhipster-users-0.json
	oc process -f ./ocp/keycloak/keycloak.yml | oc apply -f -

.PHONY: os-add-service-account
os-add-service-account:
	oc new-project equoid
	oc login -u system:admin
	oc adm policy add-scc-to-user anyuid system:serviceaccount:equoid:equoid
	oc process -f ./ocp/registry/scc-config.yml | oc apply -f -
	oc login -u developer

.PHONY: os-deploy-monitoring
os-deploy-monitoring:
	oc process -f ./ocp/monitoring/metrics.yml | oc apply -f -

.PHONY: os-deploy-equoid
os-deploy-equoid:
	oc process -f ./ocp/equoid/equoid-deployment.yml | oc apply -f -

.PHONY: wait20
wait20:
	sleep 20

.PHONY: wait5
wait5:
	sleep 5

.PHONY: all
all: clean build push oc-run

.PHONY: all-metrics
all-metrics: all wait15 grafana-ds wait5 grafana-dashboard

.PHONY: kc-only
kc-only: oc-run os-add-service-account os-deploy-keycloak

.PHONY: local-services
local-services:
	docker-compose -f src/main/docker/ispn.yml -f src/main/docker/keycloak.yml up