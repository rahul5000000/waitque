#!/bin/bash

kubectl port-forward svc/keycloak 8080:8080 &
kubectl port-forward svc/user-service 8081:8081 &
kubectl port-forward svc/company-service 8082:8082 &
kubectl port-forward svc/customer-service 8083:8083 &
wait