package com.example.customersupport.repositories;

import com.example.customersupport.entities.SupportRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RuleRepository extends JpaRepository<SupportRule, UUID> {

    List<SupportRule> findByEnabledOrderByPriorityDesc(Boolean enabled);

    List<SupportRule> findByKeywordContainingIgnoreCaseAndEnabled(String keyword, Boolean enabled);

}

