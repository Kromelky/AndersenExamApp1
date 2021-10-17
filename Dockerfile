FROM jolokia/alpine-jre-8
COPY /target/helloworld-0.0.1-${BUILD_NUMBER}.jar /usr/src/myapp/helloworld-0.0.1-${BUILD_NUMBER}.jar
COPY /target/application.properties /usr/src/myappapplication.properties
WORKDIR /usr/src/myapp
EXPOSE 8080
CMD "java" "-jar" "helloworld-0.0.1-${BUILD_NUMBER}.jar"
