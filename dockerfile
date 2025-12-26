# 1. 使用官方 Java 17 运行时（不需要 JDK）
FROM eclipse-temurin:17-jre

# 2. 设置工作目录
WORKDIR /app

# 3. 拷贝 jar 包
COPY target/LLM_System-0.0.1-SNAPSHOT.jar app.jar

# 4. 暴露端口
EXPOSE 8080

# 5. 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
