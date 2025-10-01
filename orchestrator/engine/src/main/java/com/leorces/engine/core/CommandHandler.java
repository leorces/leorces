package com.leorces.engine.core;

public interface CommandHandler<T extends ExecutionCommand> {

    void handle(T command);

    Class<T> getCommandType();

}