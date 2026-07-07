package com.furniro.OrderService.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataPoint {
    private String label;
    private BigDecimal revenue;
    private Long orders;
}
