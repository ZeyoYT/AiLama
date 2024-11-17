package me.ailama.handler.commandhandler;

import me.ailama.commands.contextcommands.AiContextCommand;
import me.ailama.commands.contextcommands.ReplyCommand;
import me.ailama.commands.slashcommands.*;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import me.ailama.handler.interfaces.AiLamaMessageContextCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.*;

public class CommandRegister {

    private static CommandRegister instance;
    public HashMap<String,AiLamaSlashCommand> slashCommands;
    public HashMap<String, AiLamaMessageContextCommand> messageContextCommands;

    public CommandRegister() {

        this.slashCommands = new HashMap<>();
        this.messageContextCommands = new HashMap<>();

        // Slash Commands
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

        // Context Menu Commands
        addCommand(new AiContextCommand(), true);
        addCommand(new ReplyCommand(), true);
    }

    public AiLamaSlashCommand getSlashCommand(String name) {
        return this.slashCommands.get(name);
    }

    public AiLamaMessageContextCommand getUserContextCommand(String name) {
        return this.messageContextCommands.get(name);
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

    /*
        Get all Context menu commands data

        @return List<CommandData>
    */
    public List<CommandData> getContextCommandsData() {
        List<CommandData> commands = new ArrayList<>();

        for(AiLamaMessageContextCommand command : this.messageContextCommands.values()) {
            commands.add(command.getCommandData());
        }

        return commands;
    }

    /*
        Combine all commands data into one list
    */
    public List<CommandData> getAllCommandsData() {
        List<CommandData> commands = new ArrayList<>();

        commands.addAll(getContextCommandsData());
        commands.addAll(getCommandsSlashData());

        return commands;
    }

    public HashMap<String, AiLamaSlashCommand> getSlashCommandMap() {
        return this.slashCommands;
    }

    public HashMap<String, AiLamaMessageContextCommand> getMessageContextCommandMap() {
        return this.messageContextCommands;
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

    /*
        Add command to the list and enable it to user based on condition
        if condition is false, command will not be shown to user

        @param command AiLamaUserContextCommand
        @param condition boolean
    */
    public void addCommand(AiLamaMessageContextCommand command, boolean condition) {

        if(!condition) {
            return;
        }

        if(this.messageContextCommands.containsKey(command.getCommandData().getName())) {
            throw new IllegalArgumentException("Command with name " + command.getCommandData().getName() + " already exists");
        }

        this.messageContextCommands.put(command.getCommandData().getName(), command);

    }

    public static CommandRegister getInstance() {
        if(instance == null) {
            instance = new CommandRegister();
        }
        return instance;
    }

}
