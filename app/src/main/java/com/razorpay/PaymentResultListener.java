package com.razorpay;

public interface PaymentResultListener {
    void onPaymentSuccess(String razorpayPaymentId);
    void onPaymentError(int code, String response);
}
