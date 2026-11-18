package io.github.domgarlor.jobtracker.cv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Stores CV files in a local directory (property {@code app.storage.cv-dir}).
 */
@Component
class FileSystemCvFileStorage implements CvFileStorage {

	private final Path root;

	FileSystemCvFileStorage(@Value("${app.storage.cv-dir}") Path root) {
		this.root = root.toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.root);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Cannot create CV storage directory " + this.root, ex);
		}
	}

	@Override
	public void store(String key, byte[] content) {
		Path target = resolve(key);
		try {
			// Write to a temporary file first and then move it, so a crash never leaves a half-written PDF.
			Path temp = Files.createTempFile(root, "upload-", ".tmp");
			Files.write(temp, content);
			Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Cannot store CV file " + key, ex);
		}
	}

	@Override
	public Resource load(String key) {
		Path path = resolve(key);
		if (!Files.isReadable(path)) {
			throw new IllegalStateException("CV file is missing from storage: " + key);
		}
		return new PathResource(path);
	}

	private Path resolve(String key) {
		Path path = root.resolve(key).normalize();
		if (!path.getParent().equals(root)) {
			throw new IllegalArgumentException("Invalid storage key: " + key);
		}
		return path;
	}

}
