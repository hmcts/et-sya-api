package uk.gov.hmcts.reform.et.syaapi.healthcheck;

import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;
import java.util.Set;

public class ETCaseApiHealthAggregator implements StatusAggregator {

    @Override
    public Status getAggregateStatus(Set<Status> statuses) {
        return Status.UP;
    }

}
