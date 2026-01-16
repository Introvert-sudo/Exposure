package com.exposure.repositories;

import com.exposure.models.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long>  {
}
