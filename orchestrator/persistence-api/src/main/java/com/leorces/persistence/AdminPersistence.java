package com.leorces.persistence;

import java.util.function.Supplier;

public interface AdminPersistence {

    <T> T execute(Supplier<T> action);

}
