package com.medsupply.services;

import com.medsupply.dao.AdminDashboardDAO;

import java.util.HashMap;
import java.util.Map;

/**
 * AdminDashboardService - Business logic for Admin Dashboard analytics
 * Aggregates data from existing tables for business intelligence and reporting
 * 
 * This service provides structured data for the admin dashboard without modifying database structure
 */
public class AdminDashboardService {

    private AdminDashboardDAO dashboardDAO;

    public AdminDashboardService() {
        this.dashboardDAO = new AdminDashboardDAO();
    }

    /**
     * Get complete dashboard overview
     * @return Map containing all dashboard metrics
     */
    public Map<String, Object> getDashboardOverview() throws Exception {
        Map<String, Object> overview = new HashMap<>();

        // Sales Overview
        Map<String, Object> sales = new HashMap<>();
        sales.put("totalOrders", dashboardDAO.getTotalOrders());
        sales.put("totalRevenue", dashboardDAO.getTotalRevenue());
        sales.put("ordersByStatus", dashboardDAO.getOrdersByStatus());
        overview.put("sales", sales);

        // Financial Overview
        Map<String, Object> finance = new HashMap<>();
        finance.put("totalPaymentsReceived", dashboardDAO.getTotalPaymentsReceived());
        finance.put("outstandingReceivables", dashboardDAO.getOutstandingReceivables());
        finance.put("overduePayments", dashboardDAO.getOverduePayments());
        finance.put("clientsWithHighestDebt", dashboardDAO.getClientsWithHighestDebt());
        overview.put("finance", finance);

        // Client Analytics
        Map<String, Object> clients = new HashMap<>();
        clients.put("totalClients", dashboardDAO.getTotalClients());
        clients.put("clientsByType", dashboardDAO.getClientsByType());
        clients.put("mostActiveClients", dashboardDAO.getMostActiveClients());
        overview.put("clients", clients);

        // Product Analytics
        Map<String, Object> products = new HashMap<>();
        products.put("totalProducts", dashboardDAO.getTotalProducts());
        products.put("lowStockProducts", dashboardDAO.getLowStockProducts());
        products.put("mostSoldProducts", dashboardDAO.getMostSoldProducts());
        products.put("productsByCategory", dashboardDAO.getProductsByCategory());
        overview.put("products", products);

        // Logistics Overview
        Map<String, Object> logistics = new HashMap<>();
        logistics.put("ordersInDelivery", dashboardDAO.getOrdersInDelivery());
        logistics.put("deliveredOrders", dashboardDAO.getDeliveredOrders());
        logistics.put("delayedDeliveries", dashboardDAO.getDelayedDeliveries());
        logistics.put("activeDeliveries", dashboardDAO.getActiveDeliveries());
        overview.put("logistics", logistics);

        // Recent Activity
        Map<String, Object> activity = new HashMap<>();
        activity.put("latestOrders", dashboardDAO.getLatestOrders(10));
        activity.put("latestPayments", dashboardDAO.getLatestPayments(10));
        activity.put("latestNotifications", dashboardDAO.getLatestNotifications(10));
        overview.put("activity", activity);

        return overview;
    }

    /**
     * Get sales overview
     * @return Map containing sales metrics
     */
    public Map<String, Object> getSalesOverview() throws Exception {
        Map<String, Object> sales = new HashMap<>();
        sales.put("totalOrders", dashboardDAO.getTotalOrders());
        sales.put("totalRevenue", dashboardDAO.getTotalRevenue());
        sales.put("ordersByStatus", dashboardDAO.getOrdersByStatus());
        return sales;
    }

    /**
     * Get financial overview
     * @return Map containing financial metrics
     */
    public Map<String, Object> getFinancialOverview() throws Exception {
        Map<String, Object> finance = new HashMap<>();
        finance.put("totalPaymentsReceived", dashboardDAO.getTotalPaymentsReceived());
        finance.put("outstandingReceivables", dashboardDAO.getOutstandingReceivables());
        finance.put("overduePayments", dashboardDAO.getOverduePayments());
        finance.put("clientsWithHighestDebt", dashboardDAO.getClientsWithHighestDebt());
        return finance;
    }

    /**
     * Get client analytics
     * @return Map containing client metrics
     */
    public Map<String, Object> getClientAnalytics() throws Exception {
        Map<String, Object> clients = new HashMap<>();
        clients.put("totalClients", dashboardDAO.getTotalClients());
        clients.put("clientsByType", dashboardDAO.getClientsByType());
        clients.put("mostActiveClients", dashboardDAO.getMostActiveClients());
        return clients;
    }

    /**
     * Get product analytics
     * @return Map containing product metrics
     */
    public Map<String, Object> getProductAnalytics() throws Exception {
        Map<String, Object> products = new HashMap<>();
        products.put("totalProducts", dashboardDAO.getTotalProducts());
        products.put("lowStockProducts", dashboardDAO.getLowStockProducts());
        products.put("mostSoldProducts", dashboardDAO.getMostSoldProducts());
        products.put("productsByCategory", dashboardDAO.getProductsByCategory());
        return products;
    }

    /**
     * Get logistics overview
     * @return Map containing logistics metrics
     */
    public Map<String, Object> getLogisticsOverview() throws Exception {
        Map<String, Object> logistics = new HashMap<>();
        logistics.put("ordersInDelivery", dashboardDAO.getOrdersInDelivery());
        logistics.put("deliveredOrders", dashboardDAO.getDeliveredOrders());
        logistics.put("delayedDeliveries", dashboardDAO.getDelayedDeliveries());
        logistics.put("activeDeliveries", dashboardDAO.getActiveDeliveries());
        return logistics;
    }

    /**
     * Get recent activity
     * @return Map containing recent activity data
     */
    public Map<String, Object> getRecentActivity() throws Exception {
        Map<String, Object> activity = new HashMap<>();
        activity.put("latestOrders", dashboardDAO.getLatestOrders(10));
        activity.put("latestPayments", dashboardDAO.getLatestPayments(10));
        activity.put("latestNotifications", dashboardDAO.getLatestNotifications(10));
        return activity;
    }
}
