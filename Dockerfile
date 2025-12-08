# Usar imagen con JDK
FROM eclipse-temurin:11-jdk-alpine

# Crear directorio de trabajo
WORKDIR /app

# Crear estructura de directorios
RUN mkdir -p classes

# Copiar archivos fuente Java desde la carpeta src/java
COPY src/java/*.java ./

# Copiar archivos de recursos
COPY src/resources/productos.json src/resources/

# Copiar el archivo HTML
COPY web/index.html ./

# Compilar el proyecto
RUN javac -d classes *.java

# Puerto que expone el servidor
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-cp", "classes", "ServidorWeb"]