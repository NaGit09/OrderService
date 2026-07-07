package com.furniro.OrderService.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsRes {
    private String totalRevenue;
    private String totalOrders;
    private String activeCustomers;
    private String inventoryStock;

    // Changes compared to previous period
    private String revenueChange;
    private String ordersChange;
    private String customersChange;
    private String stockChange;

    private Boolean revenuePositive;
    private Boolean ordersPositive;
    private Boolean customersPositive;
    private Boolean stockPositive;

    private String revenueDesc;
    private String ordersDesc;
    private String customersDesc;
    private String stockDesc;

    private List<ChartDataPoint> chartData;
    private List<CategoryShare> categories;
}
