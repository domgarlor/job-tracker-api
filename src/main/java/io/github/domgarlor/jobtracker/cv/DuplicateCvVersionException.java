package io.github.domgarlor.jobtracker.cv;

import io.github.domgarlor.jobtracker.shared.ConflictException;

/**
 * The uploaded PDF is byte-for-byte identical to a version that already exists.
 */
public class DuplicateCvVersionException extends ConflictException {

	private final long existingVersionId;

	public DuplicateCvVersionException(long existingVersionId) {
		super("This PDF was already uploaded as CV version %d".formatted(existingVersionId));
		this.existingVersionId = existingVersionId;
	}

	public long getExistingVersionId() {
		return existingVersionId;
	}

}
