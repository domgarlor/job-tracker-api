package io.github.domgarlor.jobtracker.cv;

import org.springframework.core.io.Resource;

/**
 * Port for storing CV files. The service does not know whether files live on disk, in S3 or in MinIO.
 */
public interface CvFileStorage {

	/**
	 * Stores the content under the given key. Storing the same key twice with the same content is harmless.
	 */
	void store(String key, byte[] content);

	Resource load(String key);

}
