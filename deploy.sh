#!/bin/bash
set -e

echo "==== 开始部署龙虾道具交易平台 ===="

# 检查是否已构建
if [ ! -f "lobster-trade-server/target/lobster-trade-server-1.0.0-SNAPSHOT.jar" ]; then
    echo "后端未构建，开始构建..."
    chmod +x build.sh
    ./build.sh
fi

# 加载环境变量
set -a
source .env.production
set +a

# Docker 构建
echo "启动 Docker 服务..."
docker compose up -d --build

echo "==== 部署完成 ===="
echo "用户端: http://localhost"
echo "管理后台: http://localhost:81"
echo "H5端: http://localhost:3000"
echo "后端API: http://localhost:8080"