package com.exposure.repositories;

import com.exposure.models.SessionMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionMemberRepository extends JpaRepository<SessionMember, Long> {
}
