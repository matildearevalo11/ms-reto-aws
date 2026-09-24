package com.retoaws.users;

import java.util.List;

interface UserRepository {

    List<User> findAll();

    void create(User user);
}
