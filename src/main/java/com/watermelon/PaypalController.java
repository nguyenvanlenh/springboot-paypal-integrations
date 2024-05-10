package com.watermelon;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment/paypal")
public class PaypalController {

	private final PaypalService paypalService;

	@GetMapping("/")
	public String home() {
		return "paymentForm";
	}

	@PostMapping("/create")
	public RedirectView createPayment(@ModelAttribute Order order) {
		try {
			String cancelUrl = "http://localhost:8080/payment/paypal/cancel";
			String successUrl = "http://localhost:8080/payment/paypal/success";
			Payment payment = paypalService.createPayment(
					Double.valueOf(order.getPrice()),
					order.getCurrency(),
					order.getMethod(),
					order.getIntent().toLowerCase(),
					order.getDescription(),
					cancelUrl,
					successUrl);

			for (Links links : payment.getLinks()) {
				if (links.getRel().equals("approval_url")) {
					return new RedirectView(links.getHref());
				}
			}
		} catch (PayPalRESTException e) {
			log.error("Error : ", e);
		}
		return new RedirectView("/payment/paypal/error");
	}

	@GetMapping("/success")
	public String paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			if (payment.getState().equals("approved")) {
				return "paymentSuccess";
			}
		} catch (PayPalRESTException e) {
			log.error("Error: ", e);
		}
		return "paymentSuccess";
	}

	@GetMapping("/cancel")
	public String paymentCancel() {
		return "paymentCancel";
	}

	@GetMapping("/error")
	public String paymentError() {
		return "paymentError";
	}
}