package ec.edu.uce.appproductos.data

import ec.edu.uce.appproductos.model.Product

class ProductRepository(private val productDao: ProductDao) {
    suspend fun getProducts(): List<Product> = productDao.getAllProducts()

    suspend fun addProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(updatedProduct: Product) {
        productDao.updateProduct(updatedProduct)
    }

    suspend fun deleteProduct(id: Int) {
        productDao.deleteProductById(id)
    }
}
