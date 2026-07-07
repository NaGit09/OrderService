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
public class CategoryShare {
    private String name;
    private Integer share; // percentage
    private String value; // formatted currency string, e.g. "$12,345"
    private Long count;
    private String color;
}
