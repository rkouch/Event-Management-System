package tickr.application.apis.purchase;

import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tickr.application.TickrController;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;

public class StripeAPI implements IPurchaseAPI {
    static final Logger logger = LogManager.getLogger();

    private static final String API_KEY = "sk_test_51Ltt1uArvJ5MXKVUcYk5wKKUQwqFAsCq0zkmlnI96rB2CRdqtAWqS4EdckBPLLXMaJ7eoYyDEybFrkAlbPc6CXLw00chnKSWQn";

    private final String webhookSecret;

    public StripeAPI (String webhookSecret) {
        Stripe.apiKey = API_KEY;
        this.webhookSecret = webhookSecret;
    }

    @Override
    public IOrderBuilder makePurchaseBuilder (String orderId) {
        return new PaymentSessionBuilder(orderId);
    }

    @Override
    public String registerOrder (IOrderBuilder builder) {
        var session = ((PaymentSessionBuilder)builder).build();

        var url = session.getUrl();
        logger.info("Created stripe session with url {}!", url);
        return url;
    }

    @Override
    public void handleWebhookEvent (TickrController controller, ModelSession session, String requestBody, String sigHeader) {
        logger.info("Webhook event: \n{}", requestBody);
        Event event = null;
        try {
            event = Webhook.constructEvent(requestBody, sigHeader, webhookSecret);
        } catch (JsonSyntaxException e) {
            throw new BadRequestException("Invalid payload!");
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Invalid signature!");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            event.getDataObjectDeserializer()
                    .getObject()
                    .ifPresent(s -> completePayment(controller, session, (Session) s));
        }
    }

    private void completePayment (TickrController controller, ModelSession session, Session stripeSession) {
        var metadata = stripeSession.getMetadata();
        if (!metadata.containsKey("reserve_id")) {
            throw new RuntimeException("Invalid metadata!");
        }

        var orderId = metadata.get("reserve_id");
        controller.ticketPurchaseSuccess(session, orderId);
    }


    private void doPayment (TickrController controller, ModelSession session, String requestBody, String sigHeader) {
        Event event = null;
        /*try {
            event =
        }*/
    }

    @Override
    public String getSignatureHeader () {
        return "Stripe-Signature";
    }

    private static class PaymentSessionBuilder implements IOrderBuilder {
        SessionCreateParams.Builder paramsBuilder;
        String orderId;

        public PaymentSessionBuilder (String orderId) {
            logger.debug("Created builder!");
            paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT);
            this.orderId = orderId;
        }

        @Override
        public IOrderBuilder withLineItem (LineItem lineItem) {
            logger.debug("Added line item!");
            paramsBuilder = paramsBuilder.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(lineItem.getPrice())
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(lineItem.getItemName())
                                    .build())
                            .build())
                    .build());

            return this;
        }

        @Override
        public IOrderBuilder withUrls (String successUrl, String cancelUrl) {
            logger.debug("Setting redirect urls: {}, {}!", successUrl, cancelUrl);
            paramsBuilder = paramsBuilder.setSuccessUrl(successUrl).setCancelUrl(cancelUrl);
            return this;
        }

        public Session build () {
            logger.debug("Building Stripe session!");
            try {
                var params = paramsBuilder.putMetadata("reserve_id", orderId).build();
                return Session.create(params);
            } catch (StripeException e) {
                throw new RuntimeException("Stripe exception while making checkout session!", e);
            }
        }
    }
}
