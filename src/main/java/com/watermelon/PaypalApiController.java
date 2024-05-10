package com.watermelon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PaypalApiController {

	@Autowired
	private PaypalService paypalService;

	@PostMapping("/api/payments/paypal")
	public PaymentDTO.PaypalResponse createPaypal(@RequestBody Order order, HttpServletRequest request) {
		log.info("Payment paypal");
		try {
			String cancelUrl = applicationUrl(request)+"/payment/paypal/cancel";
			String successUrl = applicationUrl(request)+"/payment/paypal/success";
			Payment payment = paypalService.createPayment(Double.valueOf(order.getPrice()), order.getCurrency(),
					order.getMethod(), order.getIntent().toLowerCase(), order.getDescription(), cancelUrl, successUrl);

			String linkPayment = "";
			for (Links links : payment.getLinks()) {
				if (links.getRel().equals("approval_url")) {
					linkPayment= links.getHref();
					break;
				}
			}
			return PaymentDTO.PaypalResponse.builder()
					.status(200)
					.message("Create payment successfully")
					.url(linkPayment)
					.build();
		} catch (PayPalRESTException e) {
			log.error("Error:: {}",e.getMessage());
			return PaymentDTO.PaypalResponse.builder()
					.status(400)
					.message("Payment creation failed")
					.build();
		}

	}
	@GetMapping("/payment/paypal/success")
	public PaymentDTO.PaypalResponse paymentSuccess(
			@RequestParam("paymentId") String paymentId, 
			@RequestParam("PayerID") String payerId) {
		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			if (payment.getState().equals("approved")) {
				return PaymentDTO.PaypalResponse.builder()
						.status(200)
						.message("Payment success")
						.build();
			}
			return null;
		} catch (PayPalRESTException e) {
			log.error("Error: ", e);
			e.printStackTrace();
			return null;
		}
	}
	@GetMapping("/payment/paypal/error")
	public PaymentDTO.PaypalResponse paymentError() {
		return PaymentDTO.PaypalResponse.builder()
				.status(501)
				.message("Payment failed")
				.build();
	}
	@GetMapping("/payment/paypal/cancel")
	public PaymentDTO.PaypalResponse paymentCancel(@RequestParam("token") String token) {
		return PaymentDTO.PaypalResponse.builder()
				.status(304)
				.message("Cancel payment")
				.token(token)
				.build();
	}
	private String applicationUrl(HttpServletRequest request) {
		return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	}

}
