package com.leorces.persistence.postgres;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Admin Persistence Integration Tests")
class AdminPersistenceIT extends RepositoryIT {

    @Test
    @DisplayName("Should execute action in transaction")
    void execute() {
        // Given
        var expected = "result";

        // When
        var result = adminPersistence.execute(() -> expected);

        // Then
        assertThat(result).isEqualTo(expected);
    }

}
