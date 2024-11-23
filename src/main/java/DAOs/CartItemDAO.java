/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import Models.CartItem;
import Models.Food;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hung
 */
public class CartItemDAO {

    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    public CartItemDAO() {
        conn = DBConnection.DBConnection.getConnection();
    }

    public ResultSet getAll() {
        String sql = "select * from CartItem";
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException ex) {
            Logger.getLogger(CartItemDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public CartItem getCartItem(int cartItemID) {
        CartItem cartItem = null;
        try {
            ps = conn.prepareStatement("select * from CartItem where cart_item_id = ?");
            ps.setInt(1, cartItemID);
            rs = ps.executeQuery();
            FoodDAO fooddao = new FoodDAO();
            if (rs.next()) {
                Food food = fooddao.getFood(rs.getShort("food_id"));
                cartItem = new CartItem(rs.getInt("cart_item_id"), rs.getInt("cart_id"), food, rs.getInt("food_quantity"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(CartItemDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cartItem;
    }

    public int add(CartItem cartItem) {
        String sql = "insert into CartItem (cart_id, food_id, food_price, food_quantity) values(?, ?, ?, ?)";
        int result = 0;
        Food food = cartItem.getFood();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cartItem.getCartID());
            ps.setShort(2, food.getFoodID());
            ps.setBigDecimal(3, food.getFoodPrice());
            ps.setInt(4, cartItem.getFoodQuantity());

            System.out.println("carID" + cartItem.getCartID());
            System.out.println("foodID" + food.getFoodID());
            System.out.println("fpoodprice" + food.getFoodPrice());
            System.out.println("foodquan" + cartItem.getFoodQuantity());
            result = ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(CartItemDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int delete(int id) {
        int ketqua = 0;
        String sql = "delete from CartItem where cart_item_id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ketqua = ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(CartItemDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ketqua;
    }

    public int update(CartItem cartItem) {
        String sql = "update CartItem set cart_id = ?, food_id = ?, food_price = ?, food_quantity = ? where cart_item_id = ?";
        int result = 0;
        try {
            Food food = cartItem.getFood();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cartItem.getCartID());
            ps.setShort(2, food.getFoodID());
            ps.setBigDecimal(3, food.getFoodPrice());
            ps.setInt(4, cartItem.getFoodQuantity());
            ps.setInt(5, cartItem.getCartItemID());
            result = ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(CartItemDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public CartItem getLatestCartItem() {
        CartItem cartItem = null;
        try {

            ps = conn.prepareStatement("select * from CartItem where cart_item_id = (select max(cart_item_id) from CartItem)");
            rs = ps.executeQuery();
            FoodDAO fooddao = new FoodDAO();

            if (rs.next()) {
                Food food = fooddao.getFood(rs.getShort("food_id"));
                cartItem = new CartItem(rs.getInt("cart_item_id"), rs.getInt("cart_id"), food, rs.getInt("food_quantity"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(CustomerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cartItem;
    }

    public List<Map<String, Object>> getTop5MostPurchasedItems() {
        List<Map<String, Object>> topFoods = new ArrayList<>();
        String sql = "SELECT TOP 5 f.food_name, SUM(ci.food_quantity) AS total_quantity, SUM(ci.food_quantity * ci.food_price) AS total_price "
                + "FROM CartItem ci "
                + "JOIN Food f ON ci.food_id = f.food_id "
                + "GROUP BY f.food_name "
                + "ORDER BY total_quantity DESC";

        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> foodData = new HashMap<>();
                foodData.put("food_name", rs.getString("food_name"));
                foodData.put("total_quantity", rs.getInt("total_quantity"));
                foodData.put("total_price", rs.getBigDecimal("total_price")); // Lưu tổng tiền
                topFoods.add(foodData);
            }
        } catch (SQLException ex) {
            Logger.getLogger(CartItemDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(CartItemDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return topFoods;
    }

}
