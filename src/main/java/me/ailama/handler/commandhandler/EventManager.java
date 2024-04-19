package me.ailama.handler.commandhandler;

import me.ailama.handler.enums.EventCategoryEnum;
import me.ailama.handler.interfaces.AiLamaEvent;

import java.util.HashMap;
import java.util.List;

public class EventManager {

    /*
        HashMap structure:
        Key: User ID (if guild specific Guild ID + '_guild')
        Value: HashMap
            Key: Event ID (if guild specific, the event id should be the interaction_id + '_' + guild_id)
            Value: List
                0: Event Category
                1: AiLamaEvent (Class that implements AiLamaEvent)
                2: Event Data (Optional)
    */

    /*
        Event ID:
        if event is guild specific, the event id should be the interaction_id + '_' + guild_id
        if event is global to user, the event id should be the interaction_id

        if event is global to guild, the User ID should be the guild_id + "_guild"
    */

    HashMap<String, HashMap<String,List<Object>>> events;

    private static EventManager eventManager;

    public EventManager() {
        events = new HashMap<>();
    }

    public void addEvent(String userId, String interactionId, EventCategoryEnum category, AiLamaEvent event) {
        HashMap<String,List<Object>> userEvents = events.get(userId);
        if(userEvents == null) {
            userEvents = new HashMap<>();
        }
        userEvents.put(interactionId, List.of(category,event));
        events.put(userId,userEvents);
    }

    public void addEventWithData(String userId, String interactionId, EventCategoryEnum category, AiLamaEvent event, Object data) {
        HashMap<String,List<Object>> userEvents = events.get(userId);
        if(userEvents == null) {
            userEvents = new HashMap<>();
        }
        userEvents.put(interactionId, List.of(category,event,data));
        events.put(userId,userEvents);
    }

    public void removeEvent(String userId,String interactionId) {
        HashMap<String,List<Object>> userEvents = events.get(userId);
        if(userEvents == null) {
            return;
        }
        userEvents.remove(interactionId);
        events.put(userId,userEvents);
    }

    public boolean hasEvent(String userId, String interactionId) {
        HashMap<String,List<Object>> userEvents = events.get(userId);
        if(userEvents == null) {
            return false;
        }
        return userEvents.containsKey(interactionId);
    }

    public AiLamaEvent getEvent(String userId, String interactionId) {
        HashMap<String,List<Object>> userEvents = events.get(userId);
        if(userEvents == null) {
            return null;
        }

        return (AiLamaEvent) userEvents.get(interactionId).get(1);
    }

    public Object getEventData(String userId, String interactionId) {
        HashMap<String,List<Object>> userEvents = events.get(userId);

        if(userEvents == null) {
            return null;
        }

        if(userEvents.get(interactionId).size() < 2) {
            return null;
        }

        return userEvents.get(interactionId).get(2);
    }

    public HashMap<String, HashMap<String,List<Object>>> getEvents() {
        return events;
    }

    public static EventManager getEventManager() {

        if(eventManager == null) {
            eventManager = new EventManager();
        }

        return eventManager;
    }
}
