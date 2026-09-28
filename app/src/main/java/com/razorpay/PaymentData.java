package com.razorpay;

import org.json.JSONObject;

public class PaymentData {
    private String paymentId;
    private String orderId;
    private String signature;
    private String userContact;
    private String userEmail;
    private JSONObject data;

    public PaymentData() {}

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getUserContact() { return userContact; }
    public void setUserContact(String userContact) { this.userContact = userContact; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public JSONObject getData() { return data; }
    public void setData(JSONObject data) { this.data = data; }
}
