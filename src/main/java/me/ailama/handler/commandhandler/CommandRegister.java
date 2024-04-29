package me.ailama.handler.commandhandler;

import me.ailama.commands.AiCommand;
import me.ailama.commands.DocumentCommand;
import me.ailama.commands.ImageCommand;
import me.ailama.commands.WebCommand;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.*;

public class CommandRegister {

    private static CommandRegister instance;
    public HashMap<String,AiLamaSlashCommand> slashCommands;

    public CommandRegister() {

        this.slashCommands = new HashMap<>();

        addCommand(new AiCommand(), true);
        addCommand(new WebCommand(), SearXNGManager.getInstance().isSearXNGEnabled());
        addCommand(new DocumentCommand(), true);
        addCommand(new ImageCommand(), Automatic1111Manager.getInstance().isAutomatic1111Enabled());
    }

    public AiLamaSlashCommand getCommand(String name) {
        return this.slashCommands.get(name);
    }

    public List<SlashCommandData> getCommandsSlashData() {
        List<SlashCommandData> commands = new ArrayList<>();

        for(AiLamaSlashCommand command : this.slashCommands.values()) {
            commands.add(command.getCommandData());
        }

        return commands;
    }

    public HashMap<String, AiLamaSlashCommand> getCommandMap() {
        return this.slashCommands;
    }

    public void addCommand(AiLamaSlashCommand command, boolean condition) {

        if(!condition) {
            return;
        }

        if(this.slashCommands.containsKey(command.getCommandData().getName())) {
            throw new IllegalArgumentException("Command with name " + command.getCommandData().getName() + " already exists");
        }

        this.slashCommands.put(command.getCommandData().getName(), command);

    }

    public static CommandRegister getInstance() {
        if(instance == null) {
            instance = new CommandRegister();
        }
        return instance;
    }

}
