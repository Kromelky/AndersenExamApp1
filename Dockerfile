FROM jolokia/alpine-jre-8
COPY /target/helloworld-0.0.1-SNAPSHOT.jar /usr/src/myapp/helloworld-0.0.1-SNAPSHOT.jar
WORKDIR /usr/src/myapp
EXPOSE 8080
CMD "java" "-jar" "helloworld-0.0.1-SNAPSHOT.jar"