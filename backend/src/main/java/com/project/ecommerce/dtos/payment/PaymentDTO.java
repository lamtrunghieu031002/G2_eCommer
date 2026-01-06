package com.project.ecommerce.dtos.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentDTO {

    @JsonProperty("amount")
    private Long amount;
    @JsonProperty("bankCode")
    private String bankCode;
    @JsonProperty("language")
    private String language;
}
