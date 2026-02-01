package com.smarthome.repository;

import com.smarthome.domain.AutomationRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRuleEntity, Long> {
    List<AutomationRuleEntity> findByIsEnabledOrderByPriorityDesc(Boolean isEnabled);
    List<AutomationRuleEntity> findByNameContaining(String name);
}
