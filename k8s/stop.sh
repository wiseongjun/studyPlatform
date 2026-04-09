#!/bin/bash

CLUSTER_NAME="backend"

echo "=========================================="
echo " K8s 클러스터 종료"
echo "=========================================="
kind delete cluster --name $CLUSTER_NAME
echo "완료!"
