package org.rakam.automation.action;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.rakam.automation.AutomationAction;
import org.rakam.collection.Event;
import org.rakam.plugin.EventMapper;
import org.rakam.plugin.EventStore;
import org.rakam.plugin.user.User;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SendEventAutomationAction implements AutomationAction<SendEventAutomationAction.SendEventAction> {

    private final Set<EventMapper> eventMappers;
    private final EventStore eventStore;

    @Inject
    public SendEventAutomationAction(Set<EventMapper> eventMappers, EventStore eventStore) {
        this.eventMappers = eventMappers;
        this.eventStore = eventStore;
    }

    public String process(Supplier<User> user, SendEventAction sendEventAction) {
        new Event(user.get().project, sendEventAction.collection, null, null);
        return null;
    }

    public static class SendEventAction {
        public final String collection;
        public final Map<String, Object> properties;

        @JsonCreator
        public SendEventAction(@JsonProperty("collection") String collection,
                               @JsonProperty("properties") Map<String, Object> properties) {
            this.collection = collection;
            this.properties = properties;
        }
    }
}