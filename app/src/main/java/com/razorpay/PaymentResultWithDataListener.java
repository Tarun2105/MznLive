package com.razorpay;

public interface PaymentResultWithDataListener {
    void onPaymentSuccess(String razorpayPaymentId, PaymentData paymentData);
    void onPaymentError(int code, String response, PaymentData paymentData);
}
