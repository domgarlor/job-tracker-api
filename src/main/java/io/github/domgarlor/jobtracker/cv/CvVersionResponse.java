package io.github.domgarlor.jobtracker.cv;

import java.time.Instant;

record CvVersionResponse(Long id, Long profileId, String originalFilename, long sizeBytes, String sha256, String notes,
		Instant uploadedAt) {

	static CvVersionResponse from(CvVersion version) {
		return new CvVersionResponse(version.getId(), version.getProfile().getId(), version.getOriginalFilename(),
				version.getSizeBytes(), version.getSha256(), version.getNotes(), version.getUploadedAt());
	}

}
