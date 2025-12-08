import java.io.Serializable;

// Serializable para posible transmisión
public class Producto implements Serializable {
    private Long id; // Identificador
    private String nombre; // Nombre del producto
    private double precio; // Precio unitario
    private int stock; // Cantidad disponible

    // Constructor inicializa todos los campos
    public Producto(Long id, String nombre, double precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    // Muestra los productos que hay en el inventario
    @Override
    public String toString() {
        return String.format("ID: %d | %s | $%.2f | Stock: %d", id, nombre, precio, stock);
    }
}