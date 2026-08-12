package com.muthukumaran.muthukumaranmart.dao;

import com.muthukumaran.muthukumaranmart.exception.DataAccessException;
import com.muthukumaran.muthukumaranmart.model.User;

import java.util.Optional;

public interface UserDAO {
    User insert(User user) throws DataAccessException;
    Optional<User> findById(long id) throws DataAccessException;
    Optional<User> findByEmail(String email) throws DataAccessException;
    java.util.List<User> findAll() throws DataAccessException;
}
