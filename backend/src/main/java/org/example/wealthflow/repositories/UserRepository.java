package org.example.wealthflow.repositories;

import jakarta.persistence.EnumType;
import lombok.RequiredArgsConstructor;
import org.example.wealthflow.models.User;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import org.jooq.Record;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final DSLContext dsl;

    private final Table<?> USERS = table("users");
    private final Field<Long> ID = field("id", Long.class);
    private final Field<String> ROLE = field("role", String.class);
    private final Field<String> LOGIN = field("login", String.class);
    private final Field<String> PASSWORD_HASH = field("password_hash", String.class);
    private final Field<String> SALT = field("salt", String.class);
    private final Field<String> EMAIL = field("email", String.class);
    private final Field<String> FULL_NAME = field("full_name", String.class);
    private final Field<Boolean> IS_DELETED = field("is_deleted", Boolean.class);

    public Optional<User> findById(Long id) {
        return dsl.selectFrom(USERS)
                .where(ID.eq(id))
                .fetchOptional(this::mapUser);
    }

    public Optional<User> findByLogin(String login) {
        return dsl.selectFrom(USERS)
                .where(LOGIN.eq(login))
                .fetchOptional(this::mapUser);
    }

    public Optional<User> findByEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(EMAIL.eq(email))
                .fetchOptional(this::mapUser);
    }

    public boolean existsByLogin(String login) {
        return dsl.fetchExists(
                dsl.selectFrom(USERS).where(LOGIN.eq(login))
        );
    }

    public boolean existsByEmail(String email) {
        return dsl.fetchExists(
                dsl.selectFrom(USERS).where(EMAIL.eq(email))
        );
    }

    public User save(User user) {
        if (user.getId() == null) {
            Long id = dsl.insertInto(USERS)
                    .set(ROLE, user.getRole().name())
                    .set(LOGIN, user.getLogin())
                    .set(PASSWORD_HASH, user.getPasswordHash())
                    .set(SALT, user.getSalt())
                    .set(EMAIL, user.getEmail())
                    .set(FULL_NAME, user.getFullName())
                    .set(IS_DELETED, false)
                    .returning(ID)
                    .fetchOne(ID);
            user.setId(id);
        } else {
            dsl.update(USERS)
                    .set(ROLE, user.getRole().name())
                    .set(LOGIN, user.getLogin())
                    .set(PASSWORD_HASH, user.getPasswordHash())
                    .set(SALT, user.getSalt())
                    .set(EMAIL, user.getEmail())
                    .set(FULL_NAME, user.getFullName())
                    .set(IS_DELETED, user.isDeleted())
                    .where(ID.eq(user.getId()))
                    .execute();
        }
        return user;
    }

    public boolean deleteById(Long id) {
        int deletedRows = dsl.deleteFrom(USERS)
                .where(ID.eq(id))
                .execute();
        return deletedRows > 0;
    }

    public boolean delete(User user) {
        if(user.getId() == null) {
            return false;
        } else {
            return deleteById(user.getId());
        }
    }

    public boolean softDeleteById(Long id) {
        int softDeletedRows = dsl.update(USERS)
                .set(IS_DELETED, true)
                .where(ID.eq(id))
                .execute();
        return softDeletedRows > 0;
    }

    public boolean softDelete(User user) {
        if(user.getId() == null) {
            return false;
        } else {
            return softDeleteById(user.getId());
        }
    }

    public boolean restoreById(Long id) {
        int rows = dsl.update(USERS)
                .set(IS_DELETED, false)
                .where(ID.eq(id))
                .execute();
        return rows > 0;
    }

    public List<User> findAll() {
        return dsl.selectFrom(USERS)
                .fetch(this::mapUser);
    }

    public List<User> findAllActive() {
        return dsl.selectFrom(USERS)
                .where(IS_DELETED.eq(false))
                .fetch(this::mapUser);
    }

    private User mapUser(Record record) {
        return User.builder()
                .id(record.get(ID))
                .role(User.Role.valueOf(record.get(ROLE)))
                .login(record.get(LOGIN))
                .passwordHash(record.get(PASSWORD_HASH))
                .salt(record.get(SALT))
                .email(record.get(EMAIL))
                .fullName(record.get(FULL_NAME))
                .deleted(record.get(IS_DELETED))
                .build();
    }
}
