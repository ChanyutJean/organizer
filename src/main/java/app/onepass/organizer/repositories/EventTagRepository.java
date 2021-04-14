package app.onepass.organizer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import app.onepass.organizer.entities.EventTagEntity;

@Repository
public interface EventTagRepository extends JpaRepository<EventTagEntity, Integer> {

	EventTagEntity findByEventIdAndTagId(int eventId, int tagId);
}
