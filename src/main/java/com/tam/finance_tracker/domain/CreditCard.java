package com.tam.finance_tracker.domain;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // Đánh dấu đây là một thực thể JPA, sẽ được ánh xạ tới một bảng trong cơ sở dữ liệu
@Table(name = "credit_cards") // Tùy chọn: Đặt tên bảng trong cơ sở dữ liệu, nếu không sẽ mặc định là "credit_card"
@Getter @Setter
@NoArgsConstructor // Tạo constructor không tham số để JPA có thể khởi tạo đối tượng
@AllArgsConstructor // Tạo constructor với tất cả tham số để dễ dàng tạo đối tượng trong code
public class CreditCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên thẻ không được để trống")
    private String cardName;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal limitAmount;

    @Min(1) @Max(31)
    private Integer statementDay; // Ngày chốt sao kê hàng tháng

    @Min(0)
    private Integer dueDateOffset; // Ví dụ: 15 ngày sau sao kê thì phải trả tiền
    
}
