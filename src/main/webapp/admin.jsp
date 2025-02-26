<%@ include file="WEB-INF/jspf/common/imports/base.jspf" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi" dir="ltr">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>QNFood | Admin Dashboard</title>
        <!-- Locale for JSTL fmt tags -->
        <fmt:setLocale value="vi" scope="session"/>
        <%@ include file="WEB-INF/jspf/common/imports/resources.jspf" %>
        <%@ include file="WEB-INF/jspf/admin/imports/dataTablesStyle.jspf" %>

    </head>
    <body>
        <div class="container-fluid m-0 p-0">
            <div class="d-flex flex-row m-0">
                <%@ include file="WEB-INF/jspf/admin/components/addFood.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/updateFood.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/deleteFood.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/addUser.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/updateUser.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/deleteUser.jspf" %>               
                <%@ include file="WEB-INF/jspf/admin/components/addRole.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/updateRole.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/deleteRole.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/addVoucher.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/deleteVoucher.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/updatevoucher.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/failure.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/success.jspf" %>  
                <%@ include file="WEB-INF/jspf/admin/components/updateOrder.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/deleteOrder.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/nextOrder.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/history.jspf" %>
                <%@ include file="WEB-INF/jspf/admin/components/adminSidebar.jspf" %>
                <%@ include file="WEB-INF/jspf/common/components/toast.jspf" %>
                
                <!-- Main Content -->
                <main class="w-100 p-4 bg-surface">
                    <div class="tab-content" data-initial-tab="${tabID}" data-order-history="${orderHistory}">
                    <%
                      if (session != null && session.getAttribute("tabID") != null) {
                        session.removeAttribute("tabID");
                      }
                    %>
                        <!-- Home Tab Content -->
                        <%@ include file="WEB-INF/jspf/admin/home.jspf" %>
                        <!-- Food & Drinks Tab Content -->
                        <%@ include file="WEB-INF/jspf/admin/foods.jspf" %>
                        <!-- Admin&Role Tab Content -->
                        <%@ include file="WEB-INF/jspf/admin/roles.jspf" %>
                        <!-- Voucher Tab Content -->
                        <%@ include file="WEB-INF/jspf/admin/voucher.jspf" %>
                        <!-- Users Tab Content -->
                        <%@ include file="WEB-INF/jspf/admin/users.jspf" %>
                        <!-- Orders Tab Content -->
                        <%@ include file="WEB-INF/jspf/admin/orders.jspf" %>
                    </div>
                </main>
            </div>
        </div>
        <%@ include file="WEB-INF/jspf/common/imports/validation.jspf" %>
        <%@ include file="WEB-INF/jspf/common/imports/javascript.jspf" %>
        <%@ include file="WEB-INF/jspf/admin/imports/dataTablesScript.jspf" %>
    </body>
</html>
