package tickr.application.apis.purchase;

import tickr.util.Pair;

import java.util.Map;
import java.util.UUID;

public class PurchaseResult {
    private String redirectUrl;

    private Map<UUID, Pair<String, Long>> charges;

    public PurchaseResult (String redirectUrl, Map<UUID, Pair<String, Long>> charges) {
        this.redirectUrl = redirectUrl;
        this.charges = charges;
    }

    public String getRedirectUrl () {
        return redirectUrl;
    }

    public Map<UUID, Pair<String, Long>> getCharges () {
        return charges;
    }
}
