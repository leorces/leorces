package com.leorces.engine.core;

public interface ResultCommandHandler<T extends ExecutionCommand, R> extends CommandHandler<T> {

    /**
     * Handles the command and returns a result.
     *
     * @param command the command to handle
     * @return the result of the command execution
     */
    R execute(T command);

    @Override
    default void handle(T command) {
        execute(command);
    }

}
