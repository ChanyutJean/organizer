package app.onepass.organizer.messages;

import app.onepass.apis.EventDuration;
import app.onepass.organizer.entities.EventDurationEntity;
import app.onepass.organizer.utilities.TimeUtil;
import lombok.Getter;

public class EventDurationMessage implements BaseMessage<EventDurationMessage, EventDurationEntity> {

	public EventDurationMessage(EventDuration eventDuration) {
		this.eventDuration = eventDuration;
	}

	@Getter
	EventDuration eventDuration;

	@Override
	public EventDurationEntity parseMessage() {

		java.sql.Timestamp startTime = TimeUtil.toSqlTimestamp(eventDuration.getStart());
		java.sql.Timestamp finishTime = TimeUtil.toSqlTimestamp(eventDuration.getFinish());

		return EventDurationEntity.builder()
				.id(eventDuration.getId())
				.eventId(eventDuration.getEventId())
				.start(startTime)
				.finish(finishTime)
				.build();
	}
}