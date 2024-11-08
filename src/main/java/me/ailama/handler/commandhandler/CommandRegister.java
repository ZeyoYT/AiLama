package me.ailama.handler.commandhandler;

import me.ailama.commands.*;
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
        addCommand(new ImageCommand(), true);
        addCommand(new ImageGenerateCommand(), Automatic1111Manager.getInstance().isAutomatic1111Enabled());
        addCommand(new ModelCommand(), true);
        addCommand(new ModelsCommand(), true);
        addCommand(new InfoCommand(), true);
        addCommand(new ChangeConnectionCommand(), true);
        addCommand(new ResetSession(), true);
    }

    public AiLamaSlashCommand getCommand(String name) {
        return this.slashCommands.get(name);
    }

    /*
        Get all commands slash data

        @return List<SlashCommandData>
    */
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

    /*
         Add command to the list and enable it to user based on condition
         if condition is false, command will not be shown to user

            @param command AiLamaSlashCommand
            @param condition boolean
    */
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
