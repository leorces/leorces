---
apply: always
---

**Additional Development Information**:

1. **Method Length**: Methods should be between 1 and 10 lines long (ideally 4–6 lines).
2. **Public Method Design**: Public methods should only be called private methods. Simple if-else statements and short
   loops are allowed in public methods.
3. **Class Size**: If a class exceeds 150 lines, it should be split into smaller, more focused classes.
4. **Javadoc Usage**: Add Javadoc only to public methods. If an interface is present, documentation is not required in
   the implementing class.
5. **Test Structure**: Test methods should be divided into sections: //Given // When // Then // When & Then.
6. **Test Constants**: Extract common constants in tests into variables.
7. **Test Descriptions**: Use @DisplayName from org.junit.jupiter.api.DisplayName for descriptive test method names.
8. **JUnit Features**: Leverage annotations and additional messages provided by JUnit to improve test clarity.
9. **TDD Principles**: Write tests as if following Test-Driven Development (TDD); do not base them solely on existing
   implementation.
10. **Stream Usage**: Avoid nesting streams. If necessary, extract inner stream logic into a separate method.
11. **Builder Usage**: If a model class has a Lombok builder and its constructor takes more than three arguments, prefer
    using the builder.
12. **Inline Variables**: Use inline variables if they are referenced only once.
13. **var Usage in Tests**: Use var for local variables in test methods.
14. **String Concatenation**: Use string templates (e.g., String.format or STR. blocks in Java 21+) instead of manual
    concatenation.
15. **Exception Handling**: Catch specific exceptions rather than generic Exception. Avoid empty catch blocks.
16. **Null Safety**: Use Optional for return types that might be null. Validate parameters with
    Objects.requireNonNull().
17. **Immutability**: Prefer immutable objects and collections. Use record types for data classes when possible.
18. **Constants**: Define constants as `private static final` and use UPPER_SNAKE_CASE naming.
19. **Collections**: Use List.of(), Set.of(), Map.of() for immutable collections. Prefer interfaces over implementations
    in declarations.
20. **Dependency Injection**: Use constructor injection over field injection. Mark injected fields as final.
21. **Logging**: Use SLF4J with placeholders for parameters. Avoid string concatenation in log statements.
22. **Resource Management**: Use try-with-resources for AutoCloseable resources.
23. **Package Structure**: Group related classes by feature, not by layer (controller, service, repository).
24. **Validation**: Use Jakarta Bean Validation annotations for input validation. Group validation logic in validators.
25. **Records**: Prefer records over classes for simple data containers. Add validation in compact constructors when
    needed.
26. **Method Names**: Use descriptive verbs for method names. Boolean methods should start with is/has/can/should.
27. **Lambda Expressions**: Keep lambda expressions simple (1-2 lines). Extract complex logic to separate methods.
28. **Thread Safety**: Clearly document thread safety guarantees. Use concurrent collections when needed.
29. **Error Messages**: Provide clear, actionable error messages. Include relevant context information.
30. **Code Organization**: Group related methods together. Order methods logically (public before private, related
    methods adjacent).
31. **Lombok Data Classes**: Use `@Data` for mutable classes that need getters, setters, equals, hashCode, and toString.
    Prefer records for immutable data containers.
32. **Lombok Value Objects**: Use `@Value` for immutable classes instead of `@Data` when immutability is required.
33. **Lombok Builder**: Use `@Builder` for classes with many optional parameters or complex construction logic. Combine
    with records when possible.
34. **Lombok Constructors**: Use `@NoArgsConstructor`, `@AllArgsConstructor`, and `@RequiredArgsConstructor` to reduce
    boilerplate. Prefer `@RequiredArgsConstructor` for dependency injection.
35. **Lombok Logging**: Use `@Slf4j` instead of manually creating logger instances. Place on class level for consistent
    logging.
36. **Lombok Getters/Setters**: Use `@Getter` and `@Setter` selectively on fields when you don't need full `@Data`
    functionality. Avoid on records.
37. **Lombok NonNull**: Use `@NonNull` on method parameters and fields to generate null checks automatically. Combine
    with Optional for return types.
38. **Lombok ToString**: Customize `@ToString` with `exclude` parameter to avoid sensitive data or circular references
    in logs.
39. **Lombok EqualsAndHashCode**: Use `@EqualsAndHashCode(callSuper = true)` for inheritance hierarchies. Exclude fields
    that shouldn't affect equality.
40. **Lombok Cleanup**: Use `@Cleanup` for automatic resource management when try-with-resources is not applicable.
41. **String Constants**: Extract hardcoded string values into constants when they represent business logic,
    configuration values, or are used multiple times. Log messages and exception messages do not need to be extracted as
    constants.
42. **List Access**: Use `getFirst()` and `getLast()` instead of `get(0)` and `get(size() - 1)` for better readability
    and intent expression when accessing first or last elements of a list.
43. Variables: Do not abbreviate variable names. Use meaningful and descriptive names that clearly express the
    variable’s purpose. The only exception is well-known loop counters in short loops (e.g., i, j, k).