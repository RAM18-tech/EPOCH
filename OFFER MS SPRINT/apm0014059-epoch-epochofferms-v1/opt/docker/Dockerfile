FROM 256151029476.dkr.ecr.us-east-1.amazonaws.com/dcp-docker-eclipse-temurin-11-jre-alpine:1.0
EXPOSE 8080
COPY target/epochofferms-v1.jar /apps/sb/app.jar
COPY src/main/resources/DMA_Lookup.txt /DMA_Lookup.txt
COPY src/main/resources/LCC_TrialDMA.txt /LCC_TrialDMA.txt
CMD java -Xms1400m -Xmx1400m -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=60 -XX:+UseG1GC -XX:+UseStringDeduplication -jar /apps/sb/app.jar

