import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClienteConsola {
    public static void main(String[] args) throws IOException {
        // Lee entrada del usuario
        Scanner scanner = new Scanner(System.in);
        // URL base del servidor
        String baseUrl = "http://localhost:8080";

        System.out.println("=== CLIENTE CARRITO DE COMPRAS (RESTful) ===");
        
        while (true) {
            // Menu con las distintas opciones
            System.out.println("\nOpciones:");
            System.out.println("1. Ver productos");
            System.out.println("2. Ver carrito");
            System.out.println("3. Agregar al carrito");
            System.out.println("4. Eliminar del carrito");
            System.out.println("5. Realizar compra");
            System.out.println("6. Vaciar carrito");
            System.out.println("7. Salir");
            System.out.print("Seleccione: ");
            // El cliente selecciona una de las siguientes opciones
            int opcion = scanner.nextInt();
            // Menu de opciones
            switch (opcion) {
                case 1:
                    // Lista de productos del inventario
                    hacerRequest("GET", baseUrl + "/productos");
                    break;
                case 2:
                    // Lista de productos que hay en el carrito
                    hacerRequest("GET", baseUrl + "/carrito");
                    break;
                case 3:
                    // El cliente selecciona el ID del producto que quiere 
                    hacerRequest("GET", baseUrl + "/productos");
                    System.out.print("ID del producto: ");
                    int id = scanner.nextInt();
                    // El cliente selecciona la cantidad
                    System.out.print("Cantidad: ");
                    int cantidad = scanner.nextInt();
                    hacerRequest("POST", baseUrl + "/carrito/items/" + id + "/" + cantidad);
                    break;
                case 4:
                    // El cliente selecciona la ID del producto que quire eliminar del carrito
                    hacerRequest("GET", baseUrl + "/carrito");
                    System.out.print("ID del ítem a eliminar: ");
                    int itemId = scanner.nextInt();
                    hacerRequest("DELETE", baseUrl + "/carrito/items/" + itemId);
                    break;
                case 5:
                    // Realiza la compra
                    hacerRequest("POST", baseUrl + "/carrito/comprar");
                    break;
                case 6:
                    // Vaciar el carrito
                    hacerRequest("DELETE", baseUrl + "/carrito");
                    break;
                case 7:
                    // Mensaje de salida del sistema
                    System.out.println("¡Hasta luego!");
                    return;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    private static void hacerRequest(String method, String urlString) {
        try {
            // Crea objeto URL
            URL url = new URL(urlString);
            // Abre conexión HTTP
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Establece método HTTP
            conn.setRequestMethod(method); 
            
            // Para métodos que no sean GET
            if (!method.equals("GET")) {
                conn.setDoOutput(true); // Habilita envío de datos
            }

            // Lee respuesta del servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            
            // Formatear y mostrar JSON de manera legible
            System.out.println("\nRespuesta del servidor:");
            System.out.println(formatearJSON(response.toString()));
            
            in.close();

        } catch (ConnectException e) {
            System.out.println("Error: No se puede conectar al servidor. Asegúrate de que ServidorWeb.java esté ejecutándose.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static String formatearJSON(String json) {
        // Formateo básico para hacer el JSON más legible
        return json.replace("{", "{\n  ")
                  .replace("}", "\n}")
                  .replace(",", ",\n  ")
                  .replace("[", "[\n  ")
                  .replace("]", "\n]");
    }
}