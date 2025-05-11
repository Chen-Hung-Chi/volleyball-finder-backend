# --- Stage 1: Build ---
# 使用帶有 Maven 和 Java 21 的基礎鏡像作為構建階段
FROM maven:3.9-eclipse-temurin-21 AS builder

# 設定工作目錄
WORKDIR /app

# Step 1: 複製 pom.xml
# 這是為了利用 Docker 的緩存。如果 pom.xml 沒有改變，下面的依賴下載步驟將被緩存重用。
COPY pom.xml .

# Step 2: 下載所有依賴
# 使用 dependency:resolve 確保所有依賴都被下載。-B 是批處理模式，非交互式。
RUN mvn dependency:resolve -B

# Step 3: 複製其餘的源碼
COPY src ./src

# Step 4: 打包應用程式，跳過測試
# -DskipTests 加快構建速度，假設測試在 CI/CD 流程的其他地方運行。
RUN mvn package -DskipTests -B

# --- Stage 2: Run ---
# 使用輕量級的 Java 21 JRE (只有運行環境，沒有開發工具) 基於 Alpine Linux，以減小最終鏡像大小。
FROM eclipse-temurin:21-jre-alpine

# 設定工作目錄
WORKDIR /app

# 從 builder 階段複製構建好的 JAR 文件到運行階段的鏡像中
COPY --from=builder /app/target/volleyball-finder-*.jar app.jar

# Step 5: 創建一個非 root 用戶和組，以提高安全性
# 在 Alpine Linux 中，使用 addgroup -S 和 adduser -S 創建系統組和用戶。
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Step 6: 切換到新創建的非 root 用戶
# 後續的所有命令（包括 ENTRYPOINT）都將以此用戶身份運行。
USER appuser

# 暴露應用程式將監聽的端口
EXPOSE 8080

# Step 7: 定義容器啟動時執行的命令
# 這裡移除了 Java 21 中預設開啟的 -XX:+UseContainerSupport。
# 保留了 -Xmx 和 -Xms 以明確控制 JVM 堆內存，這通常是推薦的做法。
ENTRYPOINT ["java", "-Xmx2g", "-Xms512m", "-jar", "app.jar"]