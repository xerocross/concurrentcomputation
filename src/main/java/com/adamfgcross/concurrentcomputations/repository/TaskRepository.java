package com.adamfgcross.concurrentcomputations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adamfgcross.concurrentcomputations.domain.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

}
