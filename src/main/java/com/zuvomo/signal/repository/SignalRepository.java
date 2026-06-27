package com.zuvomo.signal.repository;

import com.zuvomo.signal.entity.TradingSignal;
import com.zuvomo.signal.enums.SignalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalRepository extends JpaRepository<TradingSignal, String> {

    List<TradingSignal> findAllByStatus(SignalStatus status);
}
