package tickr.mock;

import tickr.application.apis.purchase.IPurchaseAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMockPurchaseAPI implements IPurchaseAPI {

    private final Map<String, Customer> customers;
    private final Map<String, Order> orders;

    public AbstractMockPurchaseAPI () {
        customers = new HashMap<>();
        orders = new HashMap<>();
    }

    public void addCustomer (String customerId, float defaultBalance) {
        customers.put(customerId, new Customer(customerId, defaultBalance));
    }

    public Customer getCustomer (String customerId) {
        return customers.get(customerId);
    }

    public boolean isUrlValid (String orderUrl) {
        return orders.containsKey(orderUrl);
    }

    public String fulfillOrder (String orderUrl, String customerId) {
        var order = getOrder(orderUrl);
        orders.remove(orderUrl);

        var customer = getCustomer(customerId);

        if (!customer.deductBalance(order.getPrice())) {
            onFailure(order);
            return order.getCancelUrl();
        } else {
            onSuccess(order);
            return order.getSuccessUrl();
        }
    }

    public String cancelOrder (String orderUrl) {
        var order = getOrder(orderUrl);
        onCancel(order);
        orders.remove(orderUrl);
        return order.getCancelUrl();
    }

    public Order getOrder (String orderUrl) {
        return orders.get(orderUrl);
    }

    protected void addOrder (Order order) {
        orders.put(order.getOrderId(), order);
    }

    abstract protected void onSuccess (Order order);
    abstract protected void onFailure (Order order);
    abstract protected void onCancel (Order order);

    public static class Customer {
        private float balance;
        private final String customerId;
        public Customer (String customerId, float balance) {
            this.customerId = customerId;
            this.balance = balance;
        }

        public float getBalance () {
            return balance;
        }

        public String getCustomerId () {
            return customerId;
        }

        public boolean deductBalance (float amount) {
            assert amount >= 0;
            if (balance < amount) {
                return false;
            }

            balance -= amount;

            return true;
        }

        public void deposit (float amount) {
            assert amount >= 0;

            balance += amount;
        }
    }

    public static class Order {
        private final List<OrderItem> items;
        private String orderId;
        private String successUrl;
        private String cancelUrl;

        public Order (List<OrderItem> items) {
            this.items = items;
        }

        public float getPrice () {
            return (float)items.stream()
                    .map(OrderItem::getPrice)
                    .reduce(0L, Long::sum) / 100;
        }

        public String getOrderId () {
            return orderId;
        }

        public String getSuccessUrl () {
            return successUrl;
        }

        public String getCancelUrl () {
            return cancelUrl;
        }
    }

    public static class OrderItem {
        private String itemName;
        private long price;

        public String getItemName () {
            return itemName;
        }

        public long getPrice () {
            return price;
        }
    }
}
