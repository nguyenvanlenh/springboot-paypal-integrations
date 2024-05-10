package com.watermelon;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

public interface PaymentDTO {
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class PaypalResponse {
        public int status;
        public String message;
        public String url;
        public String token;
    }
}
