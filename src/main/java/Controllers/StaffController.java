/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DAOs.CustomerDAO;
import DAOs.FoodDAO;
import DAOs.OrderDAO;
import DAOs.OrderLogDAO;
import Models.Food;
import Models.Order;
import Models.OrderLog;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

@MultipartConfig
public class StaffController extends HttpServlet {

    // send request function
    private String sendGetRequest(String apiURL) {
        try {
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON array response
                JSONArray jsonArray = new JSONArray(response.toString());

                // Check if the array is not empty
                if (jsonArray.length() > 0) {
                    // Get the first object from the array
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    // Extract payment_time from the object
                    String paymentTime = jsonObject.getString("payment_time");
                    return paymentTime;
                } else {
                    // Handle empty JSON array (no elements found)
                    return null;
                }

            } else {
                // Xử lý trường hợp không thành công khi gọi API
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void doGetList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        FoodDAO foodDAO = new FoodDAO();
        List<Food> foodList = foodDAO.getAllList();

        CustomerDAO customerDAO = new CustomerDAO();
        OrderDAO orderDAO = new OrderDAO();
        List<Order> orderList = orderDAO.getAllList();
        // Kiểm tra và xử lý hình ảnh
        validateFoodImages(request, foodList);

        request.setAttribute("foodList", foodList);
        request.setAttribute("orderList", orderList);
        request.getRequestDispatcher("/staff.jsp").forward(request, response);

    }

    private void validateFoodImages(HttpServletRequest request, List<Food> foodList) {
        try {
            // Đường dẫn trong webapp (đường dẫn gốc)
            String webappPath = request.getServletContext().getRealPath("/");
            String uploadPathWebapp = webappPath + "assets" + File.separator + "img";

            // Đường dẫn thư mục ngoài (đường dẫn mới)
            String uploadPathExternal = "C:\\Chuyên ngành 5\\SWP391\\QNFood\\src\\main\\webapp\\assets\\img";

            // Tạo thư mục nếu chưa tồn tại
            File uploadDirWebapp = new File(uploadPathWebapp);
            File uploadDirExternal = new File(uploadPathExternal);

            if (!uploadDirWebapp.exists()) {
                if (!uploadDirWebapp.mkdirs()) {
                    System.out.println("Warning: Could not create webapp directory: " + uploadPathWebapp);
                }
            }

            if (!uploadDirExternal.exists()) {
                if (!uploadDirExternal.mkdirs()) {
                    System.out.println("Warning: Could not create external directory: " + uploadPathExternal);
                }
            }

            for (Food food : foodList) {
                if (food.getImageURL() != null && !food.getImageURL().isEmpty()) {
                    try {
                        String fileName = food.getImageURL().substring(food.getImageURL().lastIndexOf("/") + 1);

                        // Kiểm tra file trong thư mục external trước
                        String fullImagePathExternal = uploadPathExternal + File.separator + fileName;
                        File imgFileExternal = new File(fullImagePathExternal);

                        // Kiểm tra file trong webapp
                        String fullImagePathWebapp = uploadPathWebapp + File.separator + fileName;
                        File imgFileWebapp = new File(fullImagePathWebapp);

                        // Nếu file tồn tại ở external path
                        if (imgFileExternal.exists()) {
                            validateImageFile(imgFileExternal, food);

                            // Copy file từ external sang webapp nếu chưa có
                            if (!imgFileWebapp.exists()) {
                                Files.copy(imgFileExternal.toPath(), imgFileWebapp.toPath(),
                                        StandardCopyOption.REPLACE_EXISTING);
                            }
                        } // Nếu file chỉ tồn tại ở webapp
                        else if (imgFileWebapp.exists()) {
                            validateImageFile(imgFileWebapp, food);

                            // Copy file từ webapp sang external
                            Files.copy(imgFileWebapp.toPath(), imgFileExternal.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                        } // Nếu không tìm thấy file ở cả hai nơi
                        else {
                            System.out.println("Warning: Image file not found for: " + food.getFoodName());
                            // Giữ nguyên đường dẫn ảnh trong database
                            // food.setImageURL("assets/img/default-food-image.jpg"); // Có thể set ảnh mặc định nếu cần
                        }

                    } catch (Exception e) {
                        System.out.println("Error processing image for food: " + food.getFoodName());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in validateFoodImages");
            e.printStackTrace();
        }
    }

    // Phương thức phụ để validate file ảnh
    private void validateImageFile(File imgFile, Food food) {
        try {
            String mimeType = Files.probeContentType(imgFile.toPath());
            if (mimeType == null || !mimeType.startsWith("image/")) {
                System.out.println("Warning: Invalid image file type for: " + food.getFoodName());
            }
        } catch (IOException e) {
            System.out.println("Error checking mime type for: " + food.getFoodName());
            e.printStackTrace();
        }
    }

    private void doPostAddFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        byte foodTypeID = Byte.parseByte(request.getParameter("txtFoodTypeID"));
        String foodName = request.getParameter("txtFoodName");
        String foodDescription = request.getParameter("txtFoodDescription");
        BigDecimal foodPrice = BigDecimal.valueOf(Double.parseDouble(request.getParameter("txtFoodPrice")));
        byte discountPercent = Byte.parseByte(request.getParameter("txtDiscountPercent"));
        byte foodRate = Byte.parseByte(request.getParameter("txtFoodRate"));
        Short foodQuantity = Short.parseShort(request.getParameter("txtFoodQuantity"));
        byte foodStatus = Byte.parseByte(request.getParameter("txtFoodStatus"));

        String imageURL = null;

        // Determine if the image is being uploaded or provided via URL
        String imageOption = request.getParameter("imageOption");
        if ("upload".equals(imageOption)) {
            Part filePart = request.getPart("fileUpload");  // Change from "txtImageURL" to "fileUpload"
            if (filePart != null && filePart.getSize() > 0) {
                try {
                    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                    // Validate and clean file name
                    fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

                    // Check file format
                    if (!fileName.toLowerCase().endsWith(".jpg")
                            && !fileName.toLowerCase().endsWith(".jpeg")
                            && !fileName.toLowerCase().endsWith(".png")) {
                        HttpSession session = request.getSession();
                        session.setAttribute("toastMessage", "error-invalid-file-type");
                        response.sendRedirect("/staff");
                        return;
                    }

                    // Define upload directory
                    String uploadDir = "C:\\Chuyên ngành 5\\SWP391\\QNFood\\src\\main\\webapp\\assets\\img";
                    File uploadDirFile = new File(uploadDir);
                    if (!uploadDirFile.exists() && !uploadDirFile.mkdirs()) {
                        throw new IOException("Cannot create upload directory");
                    }

                    String uploadPath = uploadDir + File.separator + fileName;

                    // Check if file already exists
                    File newFile = new File(uploadPath);
                    if (newFile.exists()) {
                        // Add timestamp to the filename to avoid collisions
                        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                        String fileExt = fileName.substring(fileName.lastIndexOf('.'));
                        fileName = fileNameWithoutExt + "_" + System.currentTimeMillis() + fileExt;
                        uploadPath = uploadDir + File.separator + fileName;
                    }

                    // Check file size
                    if (filePart.getSize() > 5 * 1024 * 1024) { // 5MB limit
                        HttpSession session = request.getSession();
                        session.setAttribute("toastMessage", "error-file-too-large");
                        response.sendRedirect("/staff");
                        return;
                    }

                    // Save the file
                    filePart.write(uploadPath);
                    imageURL = "assets/img/" + fileName;  // Relative path to save in the database

                } catch (Exception e) {
                    e.printStackTrace();
                    HttpSession session = request.getSession();
                    session.setAttribute("toastMessage", "error-upload-failed");
                    response.sendRedirect("/staff");
                    return;
                }
            }
        } else if ("url".equals(imageOption)) {
            // If the image is provided via URL, retrieve it from the form
            imageURL = request.getParameter("txtImageURL");
            if (imageURL == null || imageURL.isEmpty()) {
                HttpSession session = request.getSession();
                session.setAttribute("toastMessage", "error-invalid-url");
                response.sendRedirect("/staff");
                return;
            }
        }

        FoodDAO foodDAO = new FoodDAO();
        Food food = new Food(foodName, foodDescription, foodPrice, foodStatus, foodRate, discountPercent, imageURL, foodTypeID);
        food.setQuantity(foodQuantity);

        HttpSession session = request.getSession();

        // Check if the food item already exists
        if (foodDAO.getFood(foodName) != null) {
            session.setAttribute("toastMessage", "error-add-food-existing-food");
            response.sendRedirect("/staff");
            return;
        }

        int result = foodDAO.add(food);

        if (result >= 1) {
            session.setAttribute("toastMessage", "success-add-food");
        } else {
            session.setAttribute("toastMessage", "error-add-food");
        }

        response.sendRedirect("/staff");
    }

    private void doPostUpdateFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Retrieve food item ID from the request as a short
        short foodID = Short.parseShort(request.getParameter("txtFoodID"));

        byte foodTypeID = Byte.parseByte(request.getParameter("txtFoodTypeID"));
        String foodName = request.getParameter("txtFoodName");
        String foodDescription = request.getParameter("txtFoodDescription");
        BigDecimal foodPrice = BigDecimal.valueOf(Double.parseDouble(request.getParameter("txtFoodPrice")));
        byte discountPercent = Byte.parseByte(request.getParameter("txtDiscountPercent"));
        byte foodRate = Byte.parseByte(request.getParameter("txtFoodRate"));
        short foodQuantity = Short.parseShort(request.getParameter("txtFoodQuantity"));
        byte foodStatus = Byte.parseByte(request.getParameter("txtFoodStatus"));

        // Retrieve existing food record to get current imageURL if no new image is provided
        FoodDAO foodDAO = new FoodDAO();
        Food existingFood = foodDAO.getFood(foodID); // Retrieve the existing food item from the database

        if (existingFood == null) {
            HttpSession session = request.getSession();
            session.setAttribute("toastMessage", "error-update-food-not-found");
            response.sendRedirect("/staff");
            return;
        }

        // Initialize imageURL with the existing value from the database
        String imageURL = existingFood.getImageURL();

        // Determine if the image is being updated or provided via URL
        String imageOption = request.getParameter("imageOption");
        if ("upload".equals(imageOption)) {
            Part filePart = request.getPart("fileUpload");  // Part name should match the form input name
            if (filePart != null && filePart.getSize() > 0) {
                try {
                    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                    // Validate and clean file name
                    fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

                    // Check file format
                    if (!fileName.toLowerCase().endsWith(".jpg")
                            && !fileName.toLowerCase().endsWith(".jpeg")
                            && !fileName.toLowerCase().endsWith(".png")) {
                        HttpSession session = request.getSession();
                        session.setAttribute("toastMessage", "error-invalid-file-type");
                        response.sendRedirect("/staff");
                        return;
                    }

                    // Define upload directory
                    String uploadDir = "C:\\Chuyên ngành 5\\SWP391\\QNFood\\src\\main\\webapp\\assets\\img";
                    File uploadDirFile = new File(uploadDir);
                    if (!uploadDirFile.exists() && !uploadDirFile.mkdirs()) {
                        throw new IOException("Cannot create upload directory");
                    }

                    String uploadPath = uploadDir + File.separator + fileName;

                    // Check if file already exists
                    File newFile = new File(uploadPath);
                    if (newFile.exists()) {
                        // Add timestamp to the filename to avoid collisions
                        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                        String fileExt = fileName.substring(fileName.lastIndexOf('.'));
                        fileName = fileNameWithoutExt + "_" + System.currentTimeMillis() + fileExt;
                        uploadPath = uploadDir + File.separator + fileName;
                    }

                    // Check file size
                    if (filePart.getSize() > 5 * 1024 * 1024) { // 5MB limit
                        HttpSession session = request.getSession();
                        session.setAttribute("toastMessage", "error-file-too-large");
                        response.sendRedirect("/staff");
                        return;
                    }

                    // Save the file
                    filePart.write(uploadPath);
                    imageURL = "assets/img/" + fileName;  // Update imageURL with the new file path

                } catch (Exception e) {
                    e.printStackTrace();
                    HttpSession session = request.getSession();
                    session.setAttribute("toastMessage", "error-upload-failed");
                    response.sendRedirect("/staff");
                    return;
                }
            }
        } else if ("url".equals(imageOption)) {
            // If the image is provided via URL, retrieve it from the form
            imageURL = request.getParameter("txtImageURL");
            if (imageURL == null || imageURL.isEmpty()) {
                HttpSession session = request.getSession();
                session.setAttribute("toastMessage", "error-invalid-url");
                response.sendRedirect("/staff");
                return;
            }
        }

        Food food = new Food(foodName, foodDescription, foodPrice, foodStatus, foodRate, discountPercent, imageURL, foodTypeID);
        food.setQuantity(foodQuantity);
        food.setFoodID(foodID);  // Set ID for updating

        HttpSession session = request.getSession();

        int result = foodDAO.update(food); // Assuming the update method returns the number of rows affected

        if (result >= 1) {
            session.setAttribute("toastMessage", "success-update-food");
        } else {
            session.setAttribute("toastMessage", "error-update-food");
        }

        response.sendRedirect("/staff");
    }

    private void doPostDeleteFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the string of food IDs from the request
        String[] foodIDs = request.getParameter("foodData").split(",");

        // Convert the strings to numbers
        List<Short> foodIDList = new ArrayList<>();
        for (int i = 0; i < foodIDs.length; i++) {
            foodIDList.add(Short.parseShort(foodIDs[i]));
        }
        // Delete each food item, and count deleted items
        FoodDAO dao = new FoodDAO();
        int result = dao.deleteMultiple(foodIDList);
        HttpSession session = request.getSession();

        if (result >= 1) {
            session.setAttribute("toastMessage", "success-delete-food");
            response.sendRedirect("/staff");
        } else {
            session.setAttribute("toastMessage", "error-delete-food");
            response.sendRedirect("/staff");
        }

    }

    private void doPostUpdateOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int orderID = Integer.parseInt(request.getParameter("txtOrderID"));
        String phonenumber = request.getParameter("txtPhoneNumber");
        String address = request.getParameter("txtOrderAddress");
        String paymentmethod = request.getParameter("txtPaymentMethod");
        String note = request.getParameter("txtOrderNote");
        String status = request.getParameter("txtOrderStatus");
        Double orderTotal = Double.parseDouble(request.getParameter("txtOrderTotal"));

        BigDecimal orderTotalPay = BigDecimal.valueOf(orderTotal);
        HttpSession session = request.getSession();
        byte orderStatusID = 5;
        if (status.equals("Chờ xác nhận")) {
            orderStatusID = 1;
        } else if (status.equals("Đang chuẩn bị món")) {
            orderStatusID = 2;
        } else if (status.equals("Đang giao")) {
            orderStatusID = 3;
        } else if (status.equals("Đã giao")) {
            orderStatusID = 4;
        }

        byte paymentMethodID = 3;
        if (paymentmethod.equals("Thẻ tín dụng")) {
            paymentMethodID = 1;
        } else if (paymentmethod.equals("Thẻ ghi nợ")) {
            paymentMethodID = 2;
        }
        OrderDAO orderDAO = new OrderDAO();
        Order order = new Order(orderID, orderStatusID, paymentMethodID, phonenumber, address, note, orderTotalPay);

        int result = orderDAO.updateForAdmin(order);

        if (result >= 1) {
            LocalDateTime currentTime = LocalDateTime.now();
            Timestamp logTime = Timestamp.valueOf(currentTime);
            byte staffID = (byte) session.getAttribute("staffID");
            OrderLog log = new OrderLog(orderID, "Cập nhật thông tin đơn hàng", logTime);
            log.setStaff_id(staffID);
            OrderLogDAO logDAO = new OrderLogDAO();
            logDAO.addStaffLog(log);

            session.setAttribute("toastMessage", "success-update-order");
            response.sendRedirect("/staff");
            return;
        } else {
            session.setAttribute("toastMessage", "error-update-order");
            response.sendRedirect("/staff");
            return;
        }
    }

    private void doPostNextOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the string of food IDs from the request
        String[] orderIDs = request.getParameter("orderData").split(",");

        // Convert the strings to numbers
        List<Integer> orderIDList = new ArrayList<>();
        for (int i = 0; i < orderIDs.length; i++) {
            orderIDList.add(Integer.parseInt(orderIDs[i]));
        }

        // Delete each food item, and count deleted items
        HttpSession session = request.getSession();
        OrderDAO dao = new OrderDAO();
        int result = dao.changeStatusMultiple(orderIDList);

        if (result >= 1) {
            OrderLogDAO logDAO = new OrderLogDAO();
            LocalDateTime currentTime = LocalDateTime.now();
            byte staffID = (byte) session.getAttribute("staffID");
            Timestamp logTime = Timestamp.valueOf(currentTime);
            for (int i = 0; i < orderIDList.size(); i++) {
                OrderLog log = new OrderLog(orderIDList.get(i), "Cập nhật trạng thái đơn hàng", logTime);
                log.setStaff_id(staffID);
                logDAO.addStaffLog(log);
            }
            session.setAttribute("toastMessage", "success-next-order");
            response.sendRedirect("/staff");
            return;
        } else {
            session.setAttribute("toastMessage", "error-next-order");
            response.sendRedirect("/staff");
            return;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("tabID") == null) {
            session.setAttribute("tabID", 0);
        }
        if (path.endsWith("/staff")) {
            doGetList(request, response);
        } else if (path.endsWith("/staff/")) {
            response.sendRedirect("/staff");
        } else {
            // response.setContentType("text/css");
            request.getRequestDispatcher("/staff.jsp").forward(request, response);
        }

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (request.getParameter("btnSubmit") != null) {
            switch (request.getParameter("btnSubmit")) {
                case "SubmitAddFood":
                    doPostAddFood(request, response);
                    session.setAttribute("tabID", 1);
                    break;
                case "SubmitUpdateFood":
                    doPostUpdateFood(request, response);
                    session.setAttribute("tabID", 1);
                    break;
                case "SubmitDeleteFood":
                    doPostDeleteFood(request, response);
                    session.setAttribute("tabID", 1);
                    break;
                case "SubmitUpdateOrder":
                    session.setAttribute("tabID", 2);
                    doPostUpdateOrder(request, response);
                    break;
                case "SubmitNextOrder":
                    session.setAttribute("tabID", 2);
                    doPostNextOrder(request, response);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
