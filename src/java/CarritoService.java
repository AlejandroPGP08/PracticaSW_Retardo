import java.util.*;
import java.io.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class CarritoService {
    private List<Producto> productos = new ArrayList<>();
    private List<ItemCarrito> carrito = new ArrayList<>();
    private static final String PRODUCTOS_JSON = "src/resources/productos.json";
    
    public CarritoService() {
        cargarProductosDesdeJSON();
    }
    
    private void cargarProductosDesdeJSON() {
        try {
            String contenido = new String(Files.readAllBytes(Paths.get(PRODUCTOS_JSON)));
            contenido = contenido.trim();
            
            if (contenido.startsWith("[") && contenido.endsWith("]")) {
                contenido = contenido.substring(1, contenido.length() - 1).trim();
                String[] productosArray = contenido.split("\\},\\s*\\{");
                
                for (String productoStr : productosArray) {
                    productoStr = productoStr.trim();
                    if (productoStr.startsWith("{")) productoStr = productoStr.substring(1);
                    if (productoStr.endsWith("}")) productoStr = productoStr.substring(0, productoStr.length() - 1);
                    
                    String[] campos = productoStr.split(",");
                    Long id = null;
                    String nombre = "";
                    double precio = 0;
                    int stock = 0;
                    
                    for (String campo : campos) {
                        String[] keyValue = campo.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("\"", "");
                            String value = keyValue[1].trim().replace("\"", "");
                            
                            switch (key) {
                                case "id":
                                    id = Long.parseLong(value);
                                    break;
                                case "nombre":
                                    nombre = value;
                                    break;
                                case "precio":
                                    precio = Double.parseDouble(value);
                                    break;
                                case "stock":
                                    stock = Integer.parseInt(value);
                                    break;
                            }
                        }
                    }
                    
                    if (id != null) {
                        productos.add(new Producto(id, nombre, precio, stock));
                    }
                }
            }
            
            System.out.println("Productos cargados desde JSON: " + productos.size());
            
        } catch (IOException e) {
            System.out.println("Error al cargar productos desde JSON. Usando datos por defecto.");
            inicializarProductosPorDefecto();
            guardarProductosEnJSON();
        } catch (Exception e) {
            System.out.println("Error al parsear JSON: " + e.getMessage());
            inicializarProductosPorDefecto();
            guardarProductosEnJSON();
        }
    }
    
    private void inicializarProductosPorDefecto() {
        productos.clear();
        productos.add(new Producto(1L, "Donas", 25.00, 10));
        productos.add(new Producto(2L, "Panque", 35.00, 10));
        productos.add(new Producto(3L, "Flan", 30.00, 10));
        productos.add(new Producto(4L, "Churros", 35.00, 10));
        productos.add(new Producto(5L, "Crepas", 40.00, 10));
        productos.add(new Producto(6L, "Hot cakes", 38.00, 10));
        productos.add(new Producto(7L, "Pastel", 30.00, 10));
        productos.add(new Producto(8L, "Gelatina", 30.00, 10));
    }
    
    private synchronized void guardarProductosEnJSON() {
        try {
            StringBuilder json = new StringBuilder("[\n");
            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
                json.append(String.format(
                    "  {\"id\":%d,\"nombre\":\"%s\",\"precio\":%.2f,\"stock\":%d}",
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock()
                ));
                if (i < productos.size() - 1) json.append(",\n");
            }
            json.append("\n]");
            
            Files.write(Paths.get(PRODUCTOS_JSON), json.toString().getBytes());
            System.out.println("Productos guardados en JSON");
        } catch (IOException e) {
            System.err.println("Error al guardar productos en JSON: " + e.getMessage());
        }
    }
    
    private synchronized void actualizarStockEnJSON(int productoId, int nuevaCantidad) {
        boolean encontrado = false;
        for (Producto p : productos) {
            if (p.getId() == productoId) {
                p.setStock(nuevaCantidad);
                encontrado = true;
                System.out.println("Actualizando stock de producto ID " + productoId + " a " + nuevaCantidad);
                break;
            }
        }
        
        if (encontrado) {
            guardarProductosEnJSON();
        } else {
            System.err.println("Producto con ID " + productoId + " no encontrado para actualizar stock");
        }
    }
    
    // Los demás métodos permanecen iguales...
    public String listarProductosJSON() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            json.append(String.format(
                "{\"id\":%d,\"nombre\":\"%s\",\"precio\":%.2f,\"stock\":%d}",
                p.getId(), p.getNombre(), p.getPrecio(), p.getStock()
            ));
            if (i < productos.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
    
    // Métodos originales (mantenidos para compatibilidad)
    public String listarProductos() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PRODUCTOS DISPONIBLES ===\n");
        // Recorre cada producto
        for (Producto p : productos) {
            sb.append(p.toString()).append("\n");
        }
        // Retorna el string construido
        return sb.toString();
    }

    // Nuevo método para listar productos en JSON
    /*public String listarProductosJSON() {
        // Inicia array JSON
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            // Formatea cada producto como objeto JSON
            json.append(String.format(
                "{\"id\":%d,\"nombre\":\"%s\",\"precio\":%.2f,\"stock\":%d}",
                p.getId(), p.getNombre(), p.getPrecio(), p.getStock()
            ));
            if (i < productos.size() - 1) json.append(",");
        }
        // Cierra array JSON
        json.append("]");
        return json.toString();
    }*/

    public String agregarAlCarritoJSON(int productoId, int cantidad) {
        // Valida ID
        if (productoId < 1 || productoId > productos.size()) {
            return "{\"error\": \"Producto no encontrado\"}";
        }
        // Obtiene productos
        Producto producto = productos.get(productoId - 1);
        // Valida el stock
        if (cantidad > producto.getStock()) {
            return String.format("{\"error\": \"Stock insuficiente\", \"stock_disponible\": %d}", producto.getStock());
        }
        // Busca si el producto ya está en el carrito
        for (ItemCarrito item : carrito) {
            if (item.getProducto().getId().equals((long) productoId)) {
                int nuevaCantidad = item.getCantidad() + cantidad;
                // Valida stock total
                if (nuevaCantidad > producto.getStock()) {
                    return String.format("{\"error\": \"No hay suficiente stock\", \"stock_disponible\": %d}", producto.getStock());
                }
                item.setCantidad(nuevaCantidad);
                return String.format("{\"status\": \"OK\", \"mensaje\": \"Producto actualizado en carrito\", \"item_id\": %d, \"cantidad_actual\": %d}", 
                                   carrito.indexOf(item) + 1, nuevaCantidad);
            }
        }
        // Si no estaba en carrito, lo agrega
        carrito.add(new ItemCarrito(producto, cantidad));
        return String.format("{\"status\": \"OK\", \"mensaje\": \"Producto agregado al carrito\", \"item_id\": %d}", carrito.size());
    }

    public String verCarritoJSON() {
        // Devuelve el mensaje de que el carrito está vacío
        if (carrito.isEmpty()) {
            return "{\"mensaje\": \"El carrito está vacío\", \"items\": []}";
        }
        // Inicia array JSON
        StringBuilder json = new StringBuilder("{\"items\": [");
        double total = 0;
        // Recorre los productos del carrito
        for (int i = 0; i < carrito.size(); i++) {
            ItemCarrito item = carrito.get(i);
            // Determina el subtotal de cada producto
            double subtotal = item.getSubtotal();
            // Determina el total de todas las cosas en el carrito
            total += subtotal;
            // Formatea cada producto como objeto JSON
            json.append(String.format(
                "{\"item_id\": %d, \"producto_id\": %d, \"nombre\": \"%s\", \"cantidad\": %d, \"precio_unitario\": %.2f, \"subtotal\": %.2f}",
                i + 1, 
                item.getProducto().getId(),
                item.getProducto().getNombre(),
                item.getCantidad(),
                item.getProducto().getPrecio(),
                subtotal
            ));
            if (i < carrito.size() - 1) json.append(",");
        }
        // Cierra array JSON
        json.append(String.format("], \"total\": %.2f}", total));
        return json.toString();
    }

    public String eliminarDelCarritoJSON(int itemId) {
        // Valida si el número está dentro del rango
        if (itemId < 1 || itemId > carrito.size()) {
            return "{\"error\": \"Ítem no encontrado en el carrito\"}";
        }
        // Elimina el producto del carrito
        ItemCarrito itemEliminado = carrito.remove(itemId - 1);
        return String.format("{\"status\": \"OK\", \"mensaje\": \"Producto eliminado del carrito\", \"producto_eliminado\": \"%s\"}", 
                           itemEliminado.getProducto().getNombre());
    }

    public String realizarCompraJSON() {
    // Mensaje de error en caso de que el carrito este vacío
    if (carrito.isEmpty()) {
        return "{\"error\": \"El carrito está vacío\"}";
    }

    // Verificar stock
    for (ItemCarrito item : carrito) {
        Producto producto = item.getProducto();
        if (item.getCantidad() > producto.getStock()) {
            return String.format("{\"error\": \"Stock insuficiente para %s\", \"stock_disponible\": %d}", 
                               producto.getNombre(), producto.getStock());
        }
    }

    // Realizar compra
    double total = 0;
    // Inicia array JSON
    StringBuilder productosComprados = new StringBuilder("[");
    // Recorre todos los productos
    for (int i = 0; i < carrito.size(); i++) {
        ItemCarrito item = carrito.get(i);
        Producto producto = item.getProducto();
        
        // **CORRECCIÓN: Buscar el producto correcto en la lista**
        Producto productoEnLista = null;
        for (Producto p : productos) {
            if (p.getId().equals(producto.getId())) {
                productoEnLista = p;
                break;
            }
        }
        
        if (productoEnLista != null) {
            int nuevoStock = productoEnLista.getStock() - item.getCantidad();
            // **ACTUALIZAR STOCK EN MEMORIA**
            productoEnLista.setStock(nuevoStock);
            // **GUARDAR EN JSON**
            actualizarStockEnJSON(productoEnLista.getId().intValue(), nuevoStock);
        }
        
        // Calcula el total del carrito
        total += item.getSubtotal();
        // Da formato a cada producto comprado
        productosComprados.append(String.format(
            "{\"producto\": \"%s\", \"cantidad\": %d, \"subtotal\": %.2f}",
            producto.getNombre(), item.getCantidad(), item.getSubtotal()
        ));
        if (i < carrito.size() - 1) productosComprados.append(",");
    }
    // Cierra array JSON
    productosComprados.append("]");
    // Vacía el carrito
    carrito.clear();
    
    return String.format("{\"status\": \"OK\", \"mensaje\": \"Compra realizada exitosamente\", \"total\": %.2f, \"productos\": %s}", 
                       total, productosComprados.toString());
}

    public String vaciarCarritoJSON() {
        int cantidadItems = carrito.size();
        carrito.clear();
        return String.format("{\"status\": \"OK\", \"mensaje\": \"Carrito vaciado\", \"items_eliminados\": %d}", cantidadItems);
    }

    // Agrega productos al carrito
    public String agregarAlCarrito(int productoId, int cantidad) {
        // Valida si la opción es correcta
        if (productoId < 1 || productoId > productos.size()) {
            return "ERROR: Producto no encontrado";
        }
        Producto producto = productos.get(productoId - 1);
        // Mensaje de error en caso de que la cantidad exceda de la que está en el stock
        if (cantidad > producto.getStock()) {
            return "ERROR: Stock insuficiente. Stock disponible: " + producto.getStock();
        }
        // Si el producto existe, solo toma la cantidad
        for (ItemCarrito item : carrito) {
            if (item.getProducto().getId().equals((long) productoId)) {
                int nuevaCantidad = item.getCantidad() + cantidad;
                if (nuevaCantidad > producto.getStock()) {
                    return "ERROR: No hay suficiente stock";
                }
                item.setCantidad(nuevaCantidad);
                return "OK: Producto actualizado en carrito";
            }
        }
        // Agrega el producto al carrito
        carrito.add(new ItemCarrito(producto, cantidad));
        return "OK: Producto agregado al carrito";
    }

    public String verCarrito() {
        // Mensaje en caso de que el carrito este vacío
        if (carrito.isEmpty()) {
            return "El carrito está vacío";
        }
        // Inicia array JSON
        StringBuilder sb = new StringBuilder();
        // Formato para ver el carrito
        sb.append("=== TU CARRITO ===\n");
        double total = 0;
        for (int i = 0; i < carrito.size(); i++) {
            ItemCarrito item = carrito.get(i);
            sb.append((i + 1) + ". " + item.toString() + "\n");
            total += item.getSubtotal();
        }
        sb.append(String.format("TOTAL: $%.2f\n", total));
        return sb.toString();
    }

    public String eliminarDelCarrito(int numeroItem) {
        // Mensaje en caso de que el usuario seleccione un valor que este fuera del rango de los productos del carrito
        if (numeroItem < 1 || numeroItem > carrito.size()) {
            return "ERROR: Ítem no encontrado en el carrito";
        }
        // Elimina cierto producto del carrito
        carrito.remove(numeroItem - 1);
        return "OK: Producto eliminado del carrito";
    }

    public String realizarCompra() {
        // Mensaje de error en caso de que el carrito este vacío
        if (carrito.isEmpty()) {
            return "ERROR: El carrito está vacío";
        }
        // Verifica si hay stock suficiente de los productos del carrito
        for (ItemCarrito item : carrito) {
            Producto producto = item.getProducto();
            if (item.getCantidad() > producto.getStock()) {
                return "ERROR: Stock insuficiente para " + producto.getNombre();
            }
        }
        // En caso de haber, entonces disminuye la cantidad del inventario
        for (ItemCarrito item : carrito) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() - item.getCantidad());
        }
        // Calcula el total 
        double total = carrito.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
        // Vacía el carrito
        carrito.clear();
        return String.format("OK: Compra realizada! Total: $%.2f", total);
    }
}

/*public class CarritoService {
    // Lista de productos disponibles
    private List<Producto> productos = new ArrayList<>();
    // Lista de items en el carrito
    private List<ItemCarrito> carrito = new ArrayList<>();
    // Constructor: inicializa con productos
    public CarritoService() {
        productos.add(new Producto(1L, "Donas", 25.00, 10));
        productos.add(new Producto(2L, "Panque", 35.00, 10));
        productos.add(new Producto(3L, "Flan", 30.00, 10));
        productos.add(new Producto(4L, "Churros", 35.00, 10));
        productos.add(new Producto(5L, "Crepas", 40.00, 10));
        productos.add(new Producto(6L, "Hot cakes", 38.00, 10));
        productos.add(new Producto(7L, "Pastel", 30.00, 10));
        productos.add(new Producto(8L, "Gelatina", 30.00, 10));
    }

    // Métodos originales (mantenidos para compatibilidad)
    public String listarProductos() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PRODUCTOS DISPONIBLES ===\n");
        // Recorre cada producto
        for (Producto p : productos) {
            sb.append(p.toString()).append("\n");
        }
        // Retorna el string construido
        return sb.toString();
    }

    // Nuevo método para listar productos en JSON
    public String listarProductosJSON() {
        // Inicia array JSON
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            // Formatea cada producto como objeto JSON
            json.append(String.format(
                "{\"id\":%d,\"nombre\":\"%s\",\"precio\":%.2f,\"stock\":%d}",
                p.getId(), p.getNombre(), p.getPrecio(), p.getStock()
            ));
            if (i < productos.size() - 1) json.append(",");
        }
        // Cierra array JSON
        json.append("]");
        return json.toString();
    }

    public String agregarAlCarritoJSON(int productoId, int cantidad) {
        // Valida ID
        if (productoId < 1 || productoId > productos.size()) {
            return "{\"error\": \"Producto no encontrado\"}";
        }
        // Obtiene productos
        Producto producto = productos.get(productoId - 1);
        // Valida el stock
        if (cantidad > producto.getStock()) {
            return String.format("{\"error\": \"Stock insuficiente\", \"stock_disponible\": %d}", producto.getStock());
        }
        // Busca si el producto ya está en el carrito
        for (ItemCarrito item : carrito) {
            if (item.getProducto().getId().equals((long) productoId)) {
                int nuevaCantidad = item.getCantidad() + cantidad;
                // Valida stock total
                if (nuevaCantidad > producto.getStock()) {
                    return String.format("{\"error\": \"No hay suficiente stock\", \"stock_disponible\": %d}", producto.getStock());
                }
                item.setCantidad(nuevaCantidad);
                return String.format("{\"status\": \"OK\", \"mensaje\": \"Producto actualizado en carrito\", \"item_id\": %d, \"cantidad_actual\": %d}", 
                                   carrito.indexOf(item) + 1, nuevaCantidad);
            }
        }
        // Si no estaba en carrito, lo agrega
        carrito.add(new ItemCarrito(producto, cantidad));
        return String.format("{\"status\": \"OK\", \"mensaje\": \"Producto agregado al carrito\", \"item_id\": %d}", carrito.size());
    }

    public String verCarritoJSON() {
        // Devuelve el mensaje de que el carrito está vacío
        if (carrito.isEmpty()) {
            return "{\"mensaje\": \"El carrito está vacío\", \"items\": []}";
        }
        // Inicia array JSON
        StringBuilder json = new StringBuilder("{\"items\": [");
        double total = 0;
        // Recorre los productos del carrito
        for (int i = 0; i < carrito.size(); i++) {
            ItemCarrito item = carrito.get(i);
            // Determina el subtotal de cada producto
            double subtotal = item.getSubtotal();
            // Determina el total de todas las cosas en el carrito
            total += subtotal;
            // Formatea cada producto como objeto JSON
            json.append(String.format(
                "{\"item_id\": %d, \"producto_id\": %d, \"nombre\": \"%s\", \"cantidad\": %d, \"precio_unitario\": %.2f, \"subtotal\": %.2f}",
                i + 1, 
                item.getProducto().getId(),
                item.getProducto().getNombre(),
                item.getCantidad(),
                item.getProducto().getPrecio(),
                subtotal
            ));
            if (i < carrito.size() - 1) json.append(",");
        }
        // Cierra array JSON
        json.append(String.format("], \"total\": %.2f}", total));
        return json.toString();
    }

    public String eliminarDelCarritoJSON(int itemId) {
        // Valida si el número está dentro del rango
        if (itemId < 1 || itemId > carrito.size()) {
            return "{\"error\": \"Ítem no encontrado en el carrito\"}";
        }
        // Elimina el producto del carrito
        ItemCarrito itemEliminado = carrito.remove(itemId - 1);
        return String.format("{\"status\": \"OK\", \"mensaje\": \"Producto eliminado del carrito\", \"producto_eliminado\": \"%s\"}", 
                           itemEliminado.getProducto().getNombre());
    }

    public String realizarCompraJSON() {
        // Mensaje de error en caso de que el carrito este vacío
        if (carrito.isEmpty()) {
            return "{\"error\": \"El carrito está vacío\"}";
        }

        // Verificar stock
        for (ItemCarrito item : carrito) {
            Producto producto = item.getProducto();
            if (item.getCantidad() > producto.getStock()) {
                return String.format("{\"error\": \"Stock insuficiente para %s\", \"stock_disponible\": %d}", 
                                   producto.getNombre(), producto.getStock());
            }
        }

        // Realizar compra
        double total = 0;
        // Inicia array JSON
        StringBuilder productosComprados = new StringBuilder("[");
        // Recorre todos los productos
        for (int i = 0; i < carrito.size(); i++) {
            ItemCarrito item = carrito.get(i);
            Producto producto = item.getProducto();
            // Disminuye el stock del inventario
            producto.setStock(producto.getStock() - item.getCantidad());
            // Calcula el total del carrito
            total += item.getSubtotal();
            // Da formato a cada producto comprado
            productosComprados.append(String.format(
                "{\"producto\": \"%s\", \"cantidad\": %d, \"subtotal\": %.2f}",
                producto.getNombre(), item.getCantidad(), item.getSubtotal()
            ));
            if (i < carrito.size() - 1) productosComprados.append(",");
        }
        // Cierra array JSON
        productosComprados.append("]");
        // Vacía el carrito
        carrito.clear();
        
        return String.format("{\"status\": \"OK\", \"mensaje\": \"Compra realizada exitosamente\", \"total\": %.2f, \"productos\": %s}", 
                           total, productosComprados.toString());
    }

    public String vaciarCarritoJSON() {
        int cantidadItems = carrito.size();
        carrito.clear();
        return String.format("{\"status\": \"OK\", \"mensaje\": \"Carrito vaciado\", \"items_eliminados\": %d}", cantidadItems);
    }

    // Agrega productos al carrito
    public String agregarAlCarrito(int productoId, int cantidad) {
        // Valida si la opción es correcta
        if (productoId < 1 || productoId > productos.size()) {
            return "ERROR: Producto no encontrado";
        }
        Producto producto = productos.get(productoId - 1);
        // Mensaje de error en caso de que la cantidad exceda de la que está en el stock
        if (cantidad > producto.getStock()) {
            return "ERROR: Stock insuficiente. Stock disponible: " + producto.getStock();
        }
        // Si el producto existe, solo toma la cantidad
        for (ItemCarrito item : carrito) {
            if (item.getProducto().getId().equals((long) productoId)) {
                int nuevaCantidad = item.getCantidad() + cantidad;
                if (nuevaCantidad > producto.getStock()) {
                    return "ERROR: No hay suficiente stock";
                }
                item.setCantidad(nuevaCantidad);
                return "OK: Producto actualizado en carrito";
            }
        }
        // Agrega el producto al carrito
        carrito.add(new ItemCarrito(producto, cantidad));
        return "OK: Producto agregado al carrito";
    }

    public String verCarrito() {
        // Mensaje en caso de que el carrito este vacío
        if (carrito.isEmpty()) {
            return "El carrito está vacío";
        }
        // Inicia array JSON
        StringBuilder sb = new StringBuilder();
        // Formato para ver el carrito
        sb.append("=== TU CARRITO ===\n");
        double total = 0;
        for (int i = 0; i < carrito.size(); i++) {
            ItemCarrito item = carrito.get(i);
            sb.append((i + 1) + ". " + item.toString() + "\n");
            total += item.getSubtotal();
        }
        sb.append(String.format("TOTAL: $%.2f\n", total));
        return sb.toString();
    }

    public String eliminarDelCarrito(int numeroItem) {
        // Mensaje en caso de que el usuario seleccione un valor que este fuera del rango de los productos del carrito
        if (numeroItem < 1 || numeroItem > carrito.size()) {
            return "ERROR: Ítem no encontrado en el carrito";
        }
        // Elimina cierto producto del carrito
        carrito.remove(numeroItem - 1);
        return "OK: Producto eliminado del carrito";
    }

    public String realizarCompra() {
        // Mensaje de error en caso de que el carrito este vacío
        if (carrito.isEmpty()) {
            return "ERROR: El carrito está vacío";
        }
        // Verifica si hay stock suficiente de los productos del carrito
        for (ItemCarrito item : carrito) {
            Producto producto = item.getProducto();
            if (item.getCantidad() > producto.getStock()) {
                return "ERROR: Stock insuficiente para " + producto.getNombre();
            }
        }
        // En caso de haber, entonces disminuye la cantidad del inventario
        for (ItemCarrito item : carrito) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() - item.getCantidad());
        }
        // Calcula el total 
        double total = carrito.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
        // Vacía el carrito
        carrito.clear();
        return String.format("OK: Compra realizada! Total: $%.2f", total);
    }
}*/