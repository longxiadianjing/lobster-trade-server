#!/bin/bash
export JAVA_OPTS="-Xms512m -Xmx1024m"
export SPRING_PROFILES_ACTIVE=prod

# 如果没有设置环境变量，使用默认值
: ${DB_HOST:=localhost}
: ${DB_PORT:=3306}
: ${DB_NAME:=lobster_trade}
: ${DB_USERNAME:=root}
: ${DB_PASSWORD:=root}
: ${REDIS_HOST:=localhost}
: ${REDIS_PORT:=6379}
: ${JWT_SECRET:=lobster-default-secret-change-in-production}
: ${PAYMENT_SECRET:=lobster-default-payment-secret}

java $JAVA_OPTS -jar target/lobster-trade-server-1.0.0-SNAPSHOT.jar