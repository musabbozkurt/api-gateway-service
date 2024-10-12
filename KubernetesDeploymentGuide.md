## Kubernetes Deployment Guide

1. Run `brew install minikube` command to install `minikube` for Mac
2. Run `minikube start --driver=docker` command to start a cluster using the docker driver
3. Run `minikube status` command to get the status of the local `Kubernetes` cluster
4. Run `eval $(minikube docker-env)` command to allow `Kubernetes` to read `Docker` repository
5. Run `cd api-gateway/` command to enter the directory
6. Run `docker build -t api-gateway .` command to create `Docker` image
7. Run `kubectl apply -f ./.helm/templates/k8s-deployment.yml` command to create `k8s deployment` kind
8. Run `kubectl get deployments` command to get `deployments`
9. Run `kubectl get pods` command to get `pods`
10. Run `kubectl logs pod-name` command to reach specific `pod logs`
11. Run `kubectl apply -f ./.helm/templates/k8s-service.yml` command to create `k8s service` kind
12. Run `kubectl get service` or `kubectl get svc` command to get `service`
13. Run `kubectl get nodes -o wide` command to verify `node ip`. `INTERNAL-IP` is the same as the `minikube ip`
14. Run `minikube service api-gateway-service` command to get `URL`
    - For example: if the `URL` is http://127.0.0.1:50651/, then the `Swagger` is http://127.0.0.1:50651/swagger-ui.html
15. Run `minikube dashboard` command to open `minikube dashboard via a browser`
