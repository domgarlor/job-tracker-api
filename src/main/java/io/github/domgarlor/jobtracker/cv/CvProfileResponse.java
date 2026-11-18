package io.github.domgarlor.jobtracker.cv;

import java.time.Instant;

record CvProfileResponse(Long id, String name, String description, Instant createdAt) {

	static CvProfileResponse from(CvProfile profile) {
		return new CvProfileResponse(profile.getId(), profile.getName(), profile.getDescription(),
				profile.getCreatedAt());
	}

}
