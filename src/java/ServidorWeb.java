import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class ServidorWeb {
    private static CarritoService carritoService = new CarritoService();
    private static final String PRODUCTOS_JSON_PATH = "src/resources/productos.json";

    public static void main(String[] args) throws IOException {
        // Puerto donde escucha el servidor
        int puerto = 8080;
        // Crea socket servidor
        ServerSocket serverSocket = new ServerSocket(puerto);
        System.out.println("Servidor REST iniciado en http://localhost:" + puerto);
        System.out.println("📁 Ruta JSON: " + new File(PRODUCTOS_JSON_PATH).getAbsolutePath());
        
        // Bucle infinito para aceptar conexiones
        while (true) {
            // Espera y acepta conexión entrante
            Socket clientSocket = serverSocket.accept();
            // Procesa la solicitud del cliente
            manejarRequest(clientSocket);
        }
    }

    private static void servirArchivoEstatico(String path, PrintWriter out, BufferedOutputStream dataOut) {
        try {
            // Si la ruta es / o /index.html, servir index.html
            if (path.equals("/") || path.equals("/index.html")) {
                path = "index.html";
            }
            
            // Leer el archivo
            File file = new File(path);
            if (!file.exists()) {
                // Si no existe, enviar 404
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<h1>404 - Archivo no encontrado</h1>");
                return;
            }
            
            byte[] fileData = Files.readAllBytes(file.toPath());
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            // Enviar respuesta HTTP
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + mimeType);
            out.println("Content-Length: " + fileData.length);
            out.println();
            out.flush();
            
            // Enviar contenido del archivo
            dataOut.write(fileData, 0, fileData.length);
            dataOut.flush();
            
        } catch (IOException e) {
            System.err.println("Error al servir archivo: " + e.getMessage());
        }
    }

    private static void manejarRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream());
        
        String requestLine = in.readLine();
        if (requestLine == null) {
            clientSocket.close();
            return;
        }
        
        System.out.println("Request: " + requestLine);
        
        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String path = parts[1];
        
        // **SERVIDOR DE ARCHIVOS ESTÁTICOS**
        if (method.equals("GET") && (path.equals("/") || path.equals("/index.html") || 
            path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js"))) {
            servirArchivoEstatico(path, out, dataOut);
            clientSocket.close();
            return;
        }
        
        // Leer todos los headers
        String line;
        StringBuilder headers = new StringBuilder();
        try {
            while (!(line = in.readLine()).isEmpty()) {
                headers.append(line).append("\n");
                System.out.println("Header: " + line);
            }
        } catch (Exception e) {
            // Fin de headers
        }
        
        // **MANEJAR PREFLIGHT OPTIONS REQUEST**
        if (method.equals("OPTIONS")) {
            System.out.println("Procesando preflight OPTIONS request");
            out.println("HTTP/1.1 200 OK");
            out.println("Access-Control-Allow-Origin: *");
            out.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
            out.println("Access-Control-Allow-Headers: Content-Type, Authorization");
            out.println("Access-Control-Max-Age: 86400");
            out.println("Content-Length: 0");
            out.println();
            out.flush();
            clientSocket.close();
            return;
        }
        
        // Generar respuesta normal
        String response = generarResponse(method, path);
        int statusCode = obtenerStatusCode(response);
        
        // Encabezados HTTP
        out.println("HTTP/1.1 " + statusCode + " " + obtenerMensajeEstado(statusCode));
        out.println("Content-Type: application/json; charset=utf-8");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type, Authorization");
        out.println(); // Línea en blanco separa encabezados del cuerpo
        out.println(response); // Escribe el cuerpo de la respuesta
        out.flush();
        
        // Cierra la conexión
        clientSocket.close();
    }

    private static String generarResponse(String method, String path) {
        try {
            // Ruteo de endpoints
            // PRODUCTOS
            if (path.equals("/productos") && method.equals("GET")) {
                return carritoService.listarProductosJSON(); // Devuelve productos
            }
            // CARRITO - Ver contenido
            else if (path.equals("/carrito") && method.equals("GET")) {
                return carritoService.verCarritoJSON(); // Devuelve carrito
            }
            // CARRITO - Agregar item (POST con parámetros en path por simplicidad)
            else if (path.startsWith("/carrito/items/") && method.equals("POST")) {
                String[] partes = path.split("/"); // Divide: /carrito/items/1/2
                if (partes.length == 5) {
                    int productoId = Integer.parseInt(partes[3]); // Extrae ID producto
                    int cantidad = Integer.parseInt(partes[4]); // Extrae cantidad
                    return carritoService.agregarAlCarritoJSON(productoId, cantidad);
                }
                return "{\"error\": \"Formato inválido. Use: /carrito/items/{id}/{cantidad}\"}";
            }
            // CARRITO - Eliminar item
            else if (path.startsWith("/carrito/items/") && method.equals("DELETE")) {
                String[] partes = path.split("/");
                if (partes.length == 4) {
                    int itemId = Integer.parseInt(partes[3]); // Extrae ID producto
                    return carritoService.eliminarDelCarritoJSON(itemId);
                }
                return "{\"error\": \"Formato inválido. Use: /carrito/items/{id}\"}";
            }
            // CARRITO - Realizar compra
            else if (path.equals("/carrito/comprar") && method.equals("POST")) {
                return carritoService.realizarCompraJSON(); // Devuelve la compra realizada
            }
            // CARRITO - Vaciar carrito
            else if (path.equals("/carrito") && method.equals("DELETE")) {
                return carritoService.vaciarCarritoJSON(); // Devuelve el carrito vacío
            }
            
            return "{\"error\": \"Endpoint no encontrado\", \"endpoints\": [" +
                   "{\"method\": \"GET\", \"path\": \"/productos\", \"desc\": \"Listar productos\"}," +
                   "{\"method\": \"GET\", \"path\": \"/carrito\", \"desc\": \"Ver carrito\"}," +
                   "{\"method\": \"POST\", \"path\": \"/carrito/items/{id}/{cantidad}\", \"desc\": \"Agregar al carrito\"}," +
                   "{\"method\": \"DELETE\", \"path\": \"/carrito/items/{id}\", \"desc\": \"Eliminar del carrito\"}," +
                   "{\"method\": \"POST\", \"path\": \"/carrito/comprar\", \"desc\": \"Realizar compra\"}," +
                   "{\"method\": \"DELETE\", \"path\": \"/carrito\", \"desc\": \"Vaciar carrito\"}" +
                   "]}";
                   
        } catch (NumberFormatException e) {
            return "{\"error\": \"Parámetros numéricos inválidos\"}";
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private static int obtenerStatusCode(String response) {
        if (response.contains("\"error\"")) {
            return 400; // Bad Request
        } else if (response.contains("no encontrado") || response.contains("no existe")) {
            return 404; // Not Found
        }
        return 200; // OK
    }

    private static String obtenerMensajeEstado(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            default: return "OK";
        }
    }
}
