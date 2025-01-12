package com.group18.dao;

import com.group18.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Data Access Object (DAO) for managing product-related operations including retrieving,
 * adding, updating, deleting products, and managing stock levels.
 */
public class ProductDAO {
    private Connection connection;

    /**
     * Constructs a new ProductDAO object and initializes the database connection.
     */
    public ProductDAO() {
        this.connection = DBConnection.getConnection();
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param productId the ID of the product
     * @return the Product object corresponding to the specified ID, or null if not found
     */
    public Product findById(int productId) {
        String query = "SELECT * FROM products WHERE product_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all products from the database.
     *
     * @return a list of all Product objects
     */
    public List<Product> getAllProducts() {
        String query = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Retrieves all products of a specified type.
     *
     * @param type the product type to filter by
     * @return a list of Product objects matching the specified type
     */
    public List<Product> getProductsByType(String type) {
        String query = "SELECT * FROM products WHERE product_type = ?";
        List<Product> products = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Decreases the stock of a product by a specified quantity.
     *
     * @param productId the ID of the product
     * @param quantity  the quantity to decrease
     * @return true if the stock was successfully decreased, false otherwise
     */
    public boolean decreaseStock(int productId, int quantity) {
        String query = "UPDATE products SET stock = stock - ? WHERE product_id = ? AND stock >= ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Increases the stock of a product by a specified quantity.
     *
     * @param productId the ID of the product
     * @param quantity  the quantity to increase
     * @return true if the stock was successfully increased, false otherwise
     */
    public boolean increaseStock(int productId, int quantity) {
        String query = "UPDATE products SET stock = stock + ? WHERE product_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a new product to the database.
     *
     * @param product the Product object to be added
     * @return the added Product object, or null if the product could not be added
     */
    public Product addProduct(Product product) {
        String query = "INSERT INTO products (product_name, product_type, price, stock, image_data) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getProductType());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setBytes(5, product.getImageData());  // Changed to setBytes for BLOB

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setProductId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }

            return product;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes a product from the database by its ID.
     *
     * @param productId the ID of the product to be deleted
     * @return true if the product was successfully deleted, false otherwise
     */
    public boolean deleteProduct(int productId) {
        String query = "DELETE FROM products WHERE product_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing product in the database.
     *
     * @param product the Product object with updated details
     * @return the updated Product object, or null if the update failed
     */
    public Product updateProduct(Product product) {
        String query = "UPDATE products SET product_name = ?, product_type = ?, price = ?, stock = ?, image_data = ? WHERE product_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getProductType());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setBytes(5, product.getImageData());
            stmt.setInt(6, product.getProductId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if a product has active orders that are not processed.
     *
     * @param product the Product object to check
     * @return true if the product has active orders, false otherwise
     */
    public boolean hasActiveOrders(Product product) {
        String query = """
        SELECT COUNT(*) 
        FROM order_items 
        WHERE product_id = ? 
        AND item_type = 'product' 
        AND order_id IN (
            SELECT order_id 
            FROM orders 
            WHERE status != 'PROCESSED'
        )
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, product.getProductId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Extracts a Product object from a ResultSet.
     *
     * @param rs the ResultSet containing the product data
     * @return the Product object extracted from the ResultSet
     * @throws SQLException if an error occurs while extracting data from the ResultSet
     */
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setProductType(rs.getString("product_type"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setImageData(rs.getBytes("image_data"));
        return product;
    }
}