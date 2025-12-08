import java.io.Serializable;

// Serializable para posible transmisión
public class ItemCarrito implements Serializable {
    private Producto producto; // Producto asociado
    private int cantidad; // Cantidad seleccionada

    // Constructor inicializa todos los campos
    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return producto.getPrecio() * cantidad; // Calcula subtotal
    }
    // Getters y setters
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    // Muestra los productos que tenemos en el carrito
    @Override
    public String toString() {
        return String.format("%s x %d = $%.2f", producto.getNombre(), cantidad, getSubtotal());
    }
}