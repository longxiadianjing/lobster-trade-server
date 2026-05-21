#!/bin/bash
set -e

echo "==== 开始构建龙虾道具交易平台 ===="

# 后端构建
echo "[1/3] 构建后端..."
cd lobster-trade-server
mvn clean package -DskipTests -q
cd ..

# 前端构建
echo "[2/3] 构建用户端..."
cd lobster-trade-web
npm install --legacy-peer-deps
npm run build
cd ..

# 管理后台构建
echo "[3/3] 构建管理后台..."
cd lobster-trade-admin
npm install --legacy-peer-deps
npm run build
cd ..

echo "==== 构建完成 ===="
