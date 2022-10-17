package tickr.application.apis.purchase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tickr.application.TickrController;
import tickr.persistence.DataModel;
import tickr.persistence.ModelSession;

import java.util.ArrayList;
import java.util.List;

public class NullPurchaseAPI implements IPurchaseAPI {
    static final Logger logger = LogManager.getLogger();

    private DataModel model;

    public NullPurchaseAPI (DataModel model) {
        this.model = model;
    }

    @Override
    public IOrderBuilder makePurchaseBuilder (String orderId) {
        return new OrderBuilder(orderId);
    }

    @Override
    public String registerOrder (IOrderBuilder builder) {
        var orderBuilder = (OrderBuilder)builder;
        logger.info("Registered order {}: ", orderBuilder.getReserveId());
        for (var i : orderBuilder.getItems()) {
            logger.info("Line item: {}, price: {}", i.getItemName(), i.getPrice());
        }
        logger.info("Fulfilling order and returning success url \"{}\"", orderBuilder.getSuccessUrl());

        var session = model.makeSession();
        new TickrController().ticketPurchaseSuccess(session, orderBuilder.getReserveId());
        session.commit();
        session.close();

        return orderBuilder.getSuccessUrl();
    }

    @Override
    public void handleWebhookEvent (TickrController controller, ModelSession session, String requestBody, String sigHeader) {
        logger.warn("Null PurchaseAPI received webhook event???");
    }

    @Override
    public String getSignatureHeader () {
        return "";
    }

    private static class OrderBuilder implements IOrderBuilder {
        private String reserveId;
        private List<LineItem> items;
        private String successUrl;

        public OrderBuilder (String reserveId) {
            this.reserveId = reserveId;
            items = new ArrayList<>();
        }

        @Override
        public IOrderBuilder withLineItem (LineItem lineItem) {
            items.add(lineItem);
            return this;
        }

        @Override
        public IOrderBuilder withUrls (String successUrl, String cancelUrl) {
            this.successUrl = successUrl;
            return this;
        }

        public String getReserveId () {
            return reserveId;
        }

        public List<LineItem> getItems () {
            return items;
        }

        public String getSuccessUrl () {
            return successUrl;
        }
    }
}
