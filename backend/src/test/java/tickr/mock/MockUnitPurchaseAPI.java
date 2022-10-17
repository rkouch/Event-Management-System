package tickr.mock;

import tickr.application.TickrController;
import tickr.persistence.DataModel;

public class MockUnitPurchaseAPI extends AbstractMockPurchaseAPI {

    private TickrController controller;
    private DataModel model;

    public MockUnitPurchaseAPI (TickrController controller, DataModel model) {
        super();
        this.controller = controller;
        this.model = model;
    }

    @Override
    protected void onSuccess (Order order) {

    }

    @Override
    protected void onFailure (Order order) {

    }

    @Override
    protected void onCancel (Order order) {

    }
}
