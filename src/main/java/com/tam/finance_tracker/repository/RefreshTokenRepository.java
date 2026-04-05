package com.tam.finance_tracker.repository;

import org.springframework.data.repository.CrudRepository;

import com.tam.finance_tracker.document.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    // Redis sẽ tự hiểu các thao tác cơ bản nhờ CrudRepository
}
