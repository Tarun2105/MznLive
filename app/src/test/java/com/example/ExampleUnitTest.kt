package com.example

import com.example.data.model.PromotionalProduct
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun promotionalProduct_savingsCalculation_isCorrect() {
    val product = PromotionalProduct(
        id = 1,
        title = "Banarasi Saree",
        category = "Ethnic Wear",
        shopName = "Royal Heritage",
        priceInr = 1899,
        originalMrpInr = 3499,
        imageUrl = "https://example.com/saree.jpg",
        description = "Festive silk saree"
    )
    assertTrue(product.discountPercent > 0)
    assertTrue(product.originalMrpInr > product.priceInr)
    assertTrue(product.estimatedAmazonPrice > product.priceInr)
    assertTrue(product.estimatedFlipkartPrice > product.priceInr)
  }
}

