package com.bida.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class InvoiceResponseDTO {
    private Long id;
    private String invoiceNumber;
    private String tableName;
    private String tableType;
    private BigDecimal tableCharge;
    private BigDecimal serviceCharge;
    private BigDecimal discount;
    private BigDecimal codeDiscountAmount;
    private String discountCode;
    private BigDecimal totalAmount;
    private String createdAt;
    private String staffName;
}
