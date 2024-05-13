package me.ailama.handler.interfaces;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

public interface Assistant {


    String chat( @MemoryId String id, @UserMessage String message);


    String answer(String query);
}
