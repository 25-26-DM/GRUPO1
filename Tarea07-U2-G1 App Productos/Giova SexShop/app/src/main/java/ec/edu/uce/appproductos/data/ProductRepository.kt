package ec.edu.uce.appproductos.data

import ec.edu.uce.appproductos.model.Product
import java.util.Date

object ProductRepository {
    private val products = mutableListOf(
        Product(1, "Kit de Placer para Parejas", Date(), 49.99, false),
        Product(2, "Aceite de Masaje Comestible (Sabor Fresa)", Date(), 19.99, true),
        Product(3, "Lencería de Encaje (Negro)", Date(), 34.99, false),
        Product(4, "Vela Aromática para Ambiente Íntimo", Date(), 15.00, true),
        Product(5, "Juego de Cartas 'Retos Atrevidos'", Date(), 12.50, true)
    )

    fun getProducts(): List<Product> = products

    fun addProduct(product: Product) {
        products.add(product)
    }

    fun updateProduct(updatedProduct: Product) {
        val index = products.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) {
            products[index] = updatedProduct
        }
    }

    fun deleteProduct(id: Int) {
        products.removeAll { it.id == id }
    }
}
