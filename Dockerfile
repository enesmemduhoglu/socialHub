# --- Asama 1: derleme (JDK + Maven wrapper) ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Once sadece wrapper + pom kopyalanir: kaynak kod degistiginde bagimlilik
# indirme katmani cache'ten gelir, build cok daha hizli olur.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

# Jar'i katmanlara ayir (dependencies / loader / snapshot / application):
# runtime imajinda her katman ayri COPY olur, sadece degisen katman yeniden yuklenir.
RUN java -Djarmode=tools -jar target/socialhub-*.jar extract --layers --launcher --destination extracted

# --- Asama 2: calisma (sadece JRE, root olmayan kullanici) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

EXPOSE 8080

# Actuator health uzerinden container saglik kontrolu (compose depends_on bunu kullanir).
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
