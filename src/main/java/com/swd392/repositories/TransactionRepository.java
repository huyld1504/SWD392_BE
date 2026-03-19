package com.swd392.repositories;

import com.swd392.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository
    extends JpaRepository<Transaction, Integer>, JpaSpecificationExecutor<Transaction> {
}
