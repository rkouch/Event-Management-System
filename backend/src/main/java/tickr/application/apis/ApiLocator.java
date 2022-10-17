package tickr.application.apis;

import tickr.application.apis.email.GmailAPI;
import tickr.application.apis.email.IEmailAPI;
import tickr.application.apis.email.SendGridAPI;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.apis.purchase.NullPurchaseAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ApiLocator {
    private static ApiLocator INSTANCE = null;

    private final Map<Class<?>, CachedLocator> locators;

    private static synchronized ApiLocator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ApiLocator();
            addDefaultLocators();
        }

        return INSTANCE;
    }

    private static void addDefaultLocators () {
        var instance = getInstance();
        //instance.addLocatorInt(IEmailAPI.class, GmailAPI::new);
        instance.addLocatorInt(IEmailAPI.class, SendGridAPI::new);
        //instance.addLocatorInt(IPurchaseAPI.class, NullPurchaseAPI::new);
    }

    public static <T> void addLocator (Class<T> tClass, Supplier<T> locator) {
        getInstance().addLocatorInt(tClass, locator::get);
    }

    public static <T> T locateApi (Class<T> tClass) {
        return getInstance().locateApiInt(tClass);
    }

    public static void resetLocators () {
        addDefaultLocators();
    }

    private ApiLocator () {
        locators = new HashMap<>();
    }

    private void addLocatorInt (Class<?> tClass, Supplier<Object> locator) {
        synchronized (locators) {
            locators.put(tClass, new CachedLocator(locator));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T locateApiInt (Class<T> tClass) {
        synchronized (locators) {
            if (locators.containsKey(tClass)) {
                return (T)locators.get(tClass).locate();
            } else {
                throw new RuntimeException("Failed to locate implementation for service " + tClass.getCanonicalName() + "!");
            }
        }
    }

    private static class CachedLocator {
        private Object api = null;
        private final Supplier<Object> locator;

        public CachedLocator (Supplier<Object> locator) {
            this.locator = locator;
        }

        public Object locate () {
            if (api == null) {
                api = locator.get();
            }

            return api;
        }
    }
}
