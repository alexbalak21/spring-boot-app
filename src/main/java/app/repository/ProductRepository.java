package app.repository;

import app.model.Product;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Long> {
    
    // Find products by name (case-insensitive)
    @Query("SELECT * FROM products WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Find products by price less than or equal to
    @Query("SELECT * FROM products WHERE price <= :maxPrice")
    List<Product> findByPriceLessThanEqual(@Param("maxPrice") double maxPrice);
    
    // Find products by price range
    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceBetween(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
}
