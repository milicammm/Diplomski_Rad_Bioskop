package com.cineticket.dto.response;

import com.cineticket.entity.Order;
import com.cineticket.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminPaymentTransactionResponse {

    private Long paymentId;
    private String paymentIntentId;
    private Payment.PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDateTime paidAt;

    private Long orderId;
    private Order.OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private String currency;

    private Long customerId;
    private String customerUsername;
    private String customerEmail;

    private List<AdminPaymentItemResponse> purchasedItems = new ArrayList<>();

    public AdminPaymentTransactionResponse() {
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public Payment.PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Payment.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Order.OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Order.OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<AdminPaymentItemResponse> getPurchasedItems() { return purchasedItems; }
    public void setPurchasedItems(List<AdminPaymentItemResponse> purchasedItems) { this.purchasedItems = purchasedItems; }
}
