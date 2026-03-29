package com.bida.repository;

import com.bida.entity.Session;
import com.bida.entity.SessionSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionSegmentRepository extends JpaRepository<SessionSegment, Long> {

    List<SessionSegment> findBySession(Session session);
}
