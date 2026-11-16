package com.cineticket.scheduler;

import com.cineticket.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderExpirationScheduler.class);

    private final OrderService orderService;

    public OrderExpirationScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRateString = "${app.order.expiration-check-interval-ms:60000}")
    public void expireStaleOrders() {
        try {
            orderService.expireStalePendingOrders();
        } catch (Exception ex) {
            log.error("Failed to expire stale pending orders", ex);
        }
    }
}
