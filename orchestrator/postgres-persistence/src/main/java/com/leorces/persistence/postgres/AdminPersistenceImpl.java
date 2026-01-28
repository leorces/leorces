package com.leorces.persistence.postgres;

import com.leorces.persistence.AdminPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Slf4j
@Service
@AllArgsConstructor
public class AdminPersistenceImpl implements AdminPersistence {

    @Override
    @Transactional
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }

}
